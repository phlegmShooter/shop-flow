package com.shopflow.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopflow.common.constant.ErrorCode;
import com.shopflow.common.constant.RedisKeyConstant;
import com.shopflow.common.dto.OrderCreateDTO;
import com.shopflow.common.exception.BaseException;
import com.shopflow.common.utils.SnowflakeIdGenerator;
import com.shopflow.order.config.RabbitMQConfig;
import com.shopflow.order.dto.CartItemSnapshot;
import com.shopflow.order.entity.Order;
import com.shopflow.order.entity.OrderItem;
import com.shopflow.order.mapper.OrderMapper;
import com.shopflow.order.service.OrderService;
import com.shopflow.order.service.OrderItemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 订单服务实现类
 */
@Slf4j
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SnowflakeIdGenerator snowflakeIdGenerator;

    // 订单状态常量
    private static final int STATUS_PENDING_PAY = 0;
    private static final int STATUS_PAID = 1;
    private static final int STATUS_SHIPPED = 2;
    private static final int STATUS_COMPLETED = 3;
    private static final int STATUS_CANCELLED = 4;

    // 防超卖 Lua 脚本
    private static final String DEDUCT_STOCK_SCRIPT =
            "local stock = tonumber(redis.call('GET', KEYS[1])) " +
            "if stock == nil then return -1 end " +
            "if stock < tonumber(ARGV[1]) then return 0 end " +
            "redis.call('DECRBY', KEYS[1], ARGV[1]) " +
            "return 1";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createOrder(Long userId, OrderCreateDTO orderCreateDTO) {
        // 1. 参数校验
        if (orderCreateDTO.getProductIds() == null || orderCreateDTO.getProductIds().isEmpty()) {
            throw new BaseException(ErrorCode.CART_EMPTY);
        }
        if (orderCreateDTO.getAddress() == null || orderCreateDTO.getAddress().isBlank()) {
            throw new BaseException(ErrorCode.ADDRESS_REQUIRED);
        }

        // 2. 从 Redis 购物车读取选中商品（购物车 key: cart:{userId}）
        String cartKey = RedisKeyConstant.buildCartKey(userId);
        Map<Object, Object> cartEntries = stringRedisTemplate.opsForHash().entries(cartKey);

        if (cartEntries.isEmpty()) {
            throw new BaseException(ErrorCode.CART_EMPTY);
        }

        // 3. 过滤出本次下单的商品（productIds 列表）
        Set<String> targetProductIds = orderCreateDTO.getProductIds().stream()
                .map(String::valueOf)
                .collect(Collectors.toSet());

        List<CartItemSnapshot> selectedItems = new ArrayList<>();
        for (Map.Entry<Object, Object> entry : cartEntries.entrySet()) {
            String productIdKey = String.valueOf(entry.getKey());
            if (!targetProductIds.contains(productIdKey)) {
                continue;
            }
            try {
                CartItemSnapshot item = objectMapper.readValue(
                        (String) entry.getValue(), CartItemSnapshot.class);
                if (Boolean.TRUE.equals(item.getSelected())) {
                    selectedItems.add(item);
                }
            } catch (JsonProcessingException e) {
                log.warn("解析购物车项失败: userId={}, productId={}", userId, productIdKey, e);
            }
        }

        if (selectedItems.isEmpty()) {
            throw new BaseException(ErrorCode.CART_EMPTY);
        }

        // 4. 尝试扣减库存（Redis Lua 原子操作）
        List<CartItemSnapshot> deductedItems = new ArrayList<>();
        try {
            for (CartItemSnapshot item : selectedItems) {
                deductStock(item.getProductId(), item.getQuantity());
                deductedItems.add(item);
            }
        } catch (BaseException e) {
            // 回滚已扣减的库存
            for (CartItemSnapshot deducted : deductedItems) {
                restoreStock(deducted.getProductId(), deducted.getQuantity());
            }
            throw e;
        }

        // 5. 生成订单号
        String orderNo = generateOrderNo();

        // 6. 计算订单总金额
        BigDecimal totalAmount = selectedItems.stream()
                .map(item -> item.getPrice().multiply(new BigDecimal(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 7. 写入 t_order
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setStatus(STATUS_PENDING_PAY);
        order.setAddress(orderCreateDTO.getAddress());
        order.setRemark(orderCreateDTO.getRemark());
        orderMapper.insert(order);

        // 8. 写入 t_order_item
        List<OrderItem> orderItems = selectedItems.stream().map(item -> {
            OrderItem oi = new OrderItem();
            oi.setOrderId(order.getId());
            oi.setProductId(item.getProductId());
            oi.setProductName(item.getProductName());
            oi.setPrice(item.getPrice());
            oi.setQuantity(item.getQuantity());
            oi.setSubtotal(item.getPrice().multiply(new BigDecimal(item.getQuantity())));
            oi.setImage(item.getImage());  // 图片URL快照，订单页直接展示
            return oi;
        }).collect(Collectors.toList());
        orderItemService.saveBatch(orderItems);
        order.setItems(orderItems);

        // 9. 清除购物车中已购商品
        String[] productIdKeys = targetProductIds.toArray(new String[0]);
        stringRedisTemplate.opsForHash().delete(cartKey, (Object[]) productIdKeys);

        // 10. 发送订单创建 MQ 消息
        sendOrderMessage(RabbitMQConfig.ORDER_CREATED_ROUTING_KEY, order);

        // 11. 设置订单超时取消 Key（30 分钟后触发 Redis 过期事件）
        String timeoutKey = RedisKeyConstant.buildOrderTimeoutCancelKey(orderNo);
        stringRedisTemplate.opsForValue().set(
                timeoutKey,
                String.valueOf(userId),
                RedisKeyConstant.TTL.ORDER_TIMEOUT_CANCEL,
                TimeUnit.SECONDS
        );

        log.info("订单创建成功: orderNo={}, userId={}, totalAmount={}, itemCount={}",
                orderNo, userId, totalAmount, orderItems.size());
        return orderNo;
    }

    @Override
    public IPage<Order> getOrderList(Page<Order> page, Long userId, Integer status) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getUserId, userId);
        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }
        wrapper.orderByDesc(Order::getCreatedAt);

        IPage<Order> result = orderMapper.selectPage(page, wrapper);

        // 填充订单项
        for (Order order : result.getRecords()) {
            List<OrderItem> items = orderItemService.list(
                    new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, order.getId())
            );
            order.setItems(items);
        }

        return result;
    }

    @Override
    public Order getOrderDetail(String orderNo, Long userId) {
        Order order = getOne(new LambdaQueryWrapper<Order>()
                .eq(Order::getOrderNo, orderNo)
                .eq(Order::getUserId, userId));
        if (order == null) {
            throw new BaseException(ErrorCode.ORDER_NOT_FOUND);
        }

        List<OrderItem> items = orderItemService.list(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, order.getId())
        );
        order.setItems(items);

        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void payOrder(String orderNo, Long userId) {
        Order order = getOrderDetail(orderNo, userId);

        if (order.getStatus() != STATUS_PENDING_PAY) {
            throw new BaseException(ErrorCode.ORDER_STATUS_ERROR, "订单状态异常，无法支付");
        }

        order.setStatus(STATUS_PAID);
        order.setPaidAt(LocalDateTime.now());
        orderMapper.updateById(order);

        // 删除超时取消 Key
        stringRedisTemplate.delete(RedisKeyConstant.buildOrderTimeoutCancelKey(orderNo));

        // 发送支付成功消息
        sendOrderMessage(RabbitMQConfig.ORDER_PAID_ROUTING_KEY, order);

        log.info("订单支付成功: orderNo={}, userId={}", orderNo, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(String orderNo, Long userId) {
        Order order = getOrderDetail(orderNo, userId);

        if (order.getStatus() != STATUS_PENDING_PAY) {
            throw new BaseException(ErrorCode.ORDER_CANNOT_BE_CANCELLED);
        }

        order.setStatus(STATUS_CANCELLED);
        orderMapper.updateById(order);

        // 删除超时取消 Key
        stringRedisTemplate.delete(RedisKeyConstant.buildOrderTimeoutCancelKey(orderNo));

        // 归还库存
        if (order.getItems() != null) {
            for (OrderItem item : order.getItems()) {
                restoreStock(item.getProductId(), item.getQuantity());
            }
        }

        // 发送订单取消消息
        sendOrderMessage(RabbitMQConfig.ORDER_CANCELLED_ROUTING_KEY, order);

        log.info("订单取消成功: orderNo={}, userId={}", orderNo, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmOrder(String orderNo, Long userId) {
        Order order = getOrderDetail(orderNo, userId);

        if (order.getStatus() != STATUS_SHIPPED) {
            throw new BaseException(ErrorCode.ORDER_STATUS_ERROR, "订单状态异常，无法确认收货");
        }

        order.setStatus(STATUS_COMPLETED);
        orderMapper.updateById(order);

        log.info("订单确认收货成功: orderNo={}, userId={}", orderNo, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void timeoutCancel(String orderNo) {
        Order order = getOne(new LambdaQueryWrapper<Order>().eq(Order::getOrderNo, orderNo));
        if (order == null || order.getStatus() != STATUS_PENDING_PAY) {
            log.warn("订单超时取消跳过: orderNo={}, status={}", orderNo,
                    order != null ? order.getStatus() : null);
            return;
        }

        order.setStatus(STATUS_CANCELLED);
        orderMapper.updateById(order);

        // 填充订单项，归还库存
        List<OrderItem> items = orderItemService.list(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, order.getId())
        );
        for (OrderItem item : items) {
            restoreStock(item.getProductId(), item.getQuantity());
        }

        sendOrderMessage(RabbitMQConfig.ORDER_CANCELLED_ROUTING_KEY, order);

        log.info("订单超时取消成功: orderNo={}", orderNo);
    }

    // ===== 私有方法 =====

    /**
     * 生成订单号（雪花算法）
     */
    private String generateOrderNo() {
        return "SF" + snowflakeIdGenerator.nextId();
    }

    /**
     * 扣减 Redis 库存（Lua 原子操作）
     */
    private void deductStock(Long productId, Integer quantity) {
        String stockKey = RedisKeyConstant.buildProductStockKey(productId);

        org.springframework.data.redis.core.script.DefaultRedisScript<Long> script =
                new org.springframework.data.redis.core.script.DefaultRedisScript<>();
        script.setScriptText(DEDUCT_STOCK_SCRIPT);
        script.setResultType(Long.class);

        Long result = stringRedisTemplate.execute(
                script,
                Collections.singletonList(stockKey),
                String.valueOf(quantity)
        );

        if (result == null || result == 0) {
            throw new BaseException(ErrorCode.STOCK_NOT_ENOUGH);
        }

        if (result == -1) {
            // 缓存未预热，直接允许（数据库有乐观锁兜底）
            log.warn("库存缓存未命中，跳过 Redis 扣减: productId={}", productId);
        }
    }

    /**
     * 归还 Redis 库存
     */
    private void restoreStock(Long productId, Integer quantity) {
        String stockKey = RedisKeyConstant.buildProductStockKey(productId);
        try {
            stringRedisTemplate.opsForValue().increment(stockKey, quantity);
            log.info("库存归还成功: productId={}, quantity={}", productId, quantity);
        } catch (Exception e) {
            log.error("库存归还失败: productId={}, quantity={}", productId, quantity, e);
        }
    }

    /**
     * 发送订单消息到 RabbitMQ（非关键路径：MQ 不可用时只记日志，不影响下单）
     */
    private void sendOrderMessage(String routingKey, Order order) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("orderNo", order.getOrderNo());
            message.put("userId", order.getUserId());
            message.put("status", order.getStatus());
            message.put("totalAmount", order.getTotalAmount());
            message.put("timestamp", System.currentTimeMillis());

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.ORDER_EXCHANGE,
                    routingKey,
                    objectMapper.writeValueAsString(message)
            );
            log.debug("订单消息发送成功: orderNo={}, routingKey={}", order.getOrderNo(), routingKey);
        } catch (Exception e) {
            // MQ 连接失败或序列化失败均不影响订单创建，只记录告警日志
            log.warn("订单消息发送失败（订单已正常创建）: orderNo={}, cause={}",
                    order.getOrderNo(), e.getMessage());
        }
    }
}
