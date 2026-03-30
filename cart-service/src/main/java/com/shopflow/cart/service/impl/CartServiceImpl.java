package com.shopflow.cart.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopflow.cart.dto.CartItemVO;
import com.shopflow.cart.dto.CartVO;
import com.shopflow.cart.service.CartService;
import com.shopflow.common.constant.RedisKeyConstant;
import com.shopflow.common.dto.CartItemDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 购物车服务实现类
 */
@Slf4j
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public CartVO getCart(Long userId) {
        String cartKey = RedisKeyConstant.buildCartKey(userId);
        HashOperations<String, Object, Object> hashOps = redisTemplate.opsForHash();

        Map<Object, Object> entries = hashOps.entries(cartKey);
        List<CartItemVO> items = new ArrayList<>();
        Integer totalCount = 0;
        Integer selectedCount = 0;
        BigDecimal selectedAmount = BigDecimal.ZERO;

        for (Map.Entry<Object, Object> entry : entries.entrySet()) {
            try {
                CartItemVO item = objectMapper.readValue(
                        (String) entry.getValue(),
                        new TypeReference<CartItemVO>() {}
                );
                items.add(item);

                // 计算小计
                if (item.getPrice() != null && item.getQuantity() != null) {
                    item.setSubtotal(item.getPrice().multiply(new BigDecimal(item.getQuantity())));
                }

                totalCount += item.getQuantity();
                if (Boolean.TRUE.equals(item.getSelected())) {
                    selectedCount += item.getQuantity();
                    if (item.getSubtotal() != null) {
                        selectedAmount = selectedAmount.add(item.getSubtotal());
                    }
                }
            } catch (JsonProcessingException e) {
                log.warn("解析购物车项失败: userId={}, productId={}", userId, entry.getKey(), e);
            }
        }

        // 刷新TTL
        redisTemplate.expire(cartKey, RedisKeyConstant.TTL.CART, TimeUnit.SECONDS);

        CartVO cartVO = new CartVO();
        cartVO.setItems(items);
        cartVO.setTotalCount(totalCount);
        cartVO.setSelectedCount(selectedCount);
        cartVO.setSelectedAmount(selectedAmount);

        return cartVO;
    }

    @Override
    public void addToCart(Long userId, CartItemDTO cartItemDTO) {
        String cartKey = RedisKeyConstant.buildCartKey(userId);
        HashOperations<String, Object, Object> hashOps = redisTemplate.opsForHash();
        String productIdKey = String.valueOf(cartItemDTO.getProductId());

        // 检查商品是否已在购物车中
        Object existing = hashOps.get(cartKey, productIdKey);
        CartItemVO item;

        if (existing != null) {
            try {
                item = objectMapper.readValue((String) existing, new TypeReference<CartItemVO>() {});
                item.setQuantity(item.getQuantity() + cartItemDTO.getQuantity());
            } catch (JsonProcessingException e) {
                log.warn("解析购物车项失败，重新创建: userId={}, productId={}", userId, productIdKey, e);
                item = createCartItem(cartItemDTO);
            }
        } else {
            item = createCartItem(cartItemDTO);
        }

        try {
            hashOps.put(cartKey, productIdKey, objectMapper.writeValueAsString(item));
            redisTemplate.expire(cartKey, RedisKeyConstant.TTL.CART, TimeUnit.SECONDS);
            log.info("添加购物车成功: userId={}, productId={}, quantity={}",
                    userId, cartItemDTO.getProductId(), item.getQuantity());
        } catch (JsonProcessingException e) {
            log.error("序列化购物车项失败", e);
        }
    }

    @Override
    public void updateQuantity(Long userId, Long productId, Integer quantity) {
        String cartKey = RedisKeyConstant.buildCartKey(userId);
        HashOperations<String, Object, Object> hashOps = redisTemplate.opsForHash();
        String productIdKey = String.valueOf(productId);

        Object existing = hashOps.get(cartKey, productIdKey);
        if (existing == null) {
            log.warn("购物车商品不存在: userId={}, productId={}", userId, productId);
            return;
        }

        try {
            CartItemVO item = objectMapper.readValue((String) existing, new TypeReference<CartItemVO>() {});
            item.setQuantity(quantity);
            hashOps.put(cartKey, productIdKey, objectMapper.writeValueAsString(item));
            redisTemplate.expire(cartKey, RedisKeyConstant.TTL.CART, TimeUnit.SECONDS);
            log.info("更新购物车数量成功: userId={}, productId={}, quantity={}", userId, productId, quantity);
        } catch (JsonProcessingException e) {
            log.error("更新购物车数量失败", e);
        }
    }

    @Override
    public void removeFromCart(Long userId, Long productId) {
        String cartKey = RedisKeyConstant.buildCartKey(userId);
        HashOperations<String, Object, Object> hashOps = redisTemplate.opsForHash();
        hashOps.delete(cartKey, String.valueOf(productId));
        redisTemplate.expire(cartKey, RedisKeyConstant.TTL.CART, TimeUnit.SECONDS);
        log.info("删除购物车商品成功: userId={}, productId={}", userId, productId);
    }

    @Override
    public void selectItem(Long userId, Long productId, Boolean selected) {
        String cartKey = RedisKeyConstant.buildCartKey(userId);
        HashOperations<String, Object, Object> hashOps = redisTemplate.opsForHash();
        String productIdKey = String.valueOf(productId);

        Object existing = hashOps.get(cartKey, productIdKey);
        if (existing == null) {
            log.warn("购物车商品不存在: userId={}, productId={}", userId, productId);
            return;
        }

        try {
            CartItemVO item = objectMapper.readValue((String) existing, new TypeReference<CartItemVO>() {});
            item.setSelected(selected);
            hashOps.put(cartKey, productIdKey, objectMapper.writeValueAsString(item));
            redisTemplate.expire(cartKey, RedisKeyConstant.TTL.CART, TimeUnit.SECONDS);
            log.info("勾选购物车商品成功: userId={}, productId={}, selected={}", userId, productId, selected);
        } catch (JsonProcessingException e) {
            log.error("勾选购物车商品失败", e);
        }
    }

    @Override
    public void selectAll(Long userId, Boolean selected) {
        String cartKey = RedisKeyConstant.buildCartKey(userId);
        HashOperations<String, Object, Object> hashOps = redisTemplate.opsForHash();

        Map<Object, Object> entries = hashOps.entries(cartKey);
        for (Map.Entry<Object, Object> entry : entries.entrySet()) {
            try {
                CartItemVO item = objectMapper.readValue((String) entry.getValue(), new TypeReference<CartItemVO>() {});
                item.setSelected(selected);
                hashOps.put(cartKey, entry.getKey(), objectMapper.writeValueAsString(item));
            } catch (JsonProcessingException e) {
                log.warn("处理购物车项失败: userId={}, productId={}", userId, entry.getKey(), e);
            }
        }

        redisTemplate.expire(cartKey, RedisKeyConstant.TTL.CART, TimeUnit.SECONDS);
        log.info("全选购物车商品成功: userId={}, selected={}", userId, selected);
    }

    @Override
    public void clearCart(Long userId) {
        String cartKey = RedisKeyConstant.buildCartKey(userId);
        redisTemplate.delete(cartKey);
        log.info("清空购物车成功: userId={}", userId);
    }

    @Override
    public void clearSelected(Long userId) {
        String cartKey = RedisKeyConstant.buildCartKey(userId);
        HashOperations<String, Object, Object> hashOps = redisTemplate.opsForHash();

        Map<Object, Object> entries = hashOps.entries(cartKey);
        List<String> toDelete = new ArrayList<>();

        for (Map.Entry<Object, Object> entry : entries.entrySet()) {
            try {
                CartItemVO item = objectMapper.readValue((String) entry.getValue(), new TypeReference<CartItemVO>() {});
                if (Boolean.TRUE.equals(item.getSelected())) {
                    toDelete.add((String) entry.getKey());
                }
            } catch (JsonProcessingException e) {
                log.warn("处理购物车项失败: userId={}, productId={}", userId, entry.getKey(), e);
            }
        }

        if (!toDelete.isEmpty()) {
            hashOps.delete(cartKey, toDelete.toArray());
            redisTemplate.expire(cartKey, RedisKeyConstant.TTL.CART, TimeUnit.SECONDS);
            log.info("清空已选中购物车商品成功: userId={}, count={}", userId, toDelete.size());
        }
    }

    @Override
    public Integer getCartCount(Long userId) {
        String cartKey = RedisKeyConstant.buildCartKey(userId);
        HashOperations<String, Object, Object> hashOps = redisTemplate.opsForHash();

        Map<Object, Object> entries = hashOps.entries(cartKey);
        Integer totalCount = 0;

        for (Object value : entries.values()) {
            try {
                CartItemVO item = objectMapper.readValue((String) value, new TypeReference<CartItemVO>() {});
                totalCount += item.getQuantity();
            } catch (JsonProcessingException e) {
                // 忽略解析失败的项
            }
        }

        return totalCount;
    }

    @Override
    public List<CartItemVO> getSelectedItems(Long userId) {
        String cartKey = RedisKeyConstant.buildCartKey(userId);
        HashOperations<String, Object, Object> hashOps = redisTemplate.opsForHash();

        Map<Object, Object> entries = hashOps.entries(cartKey);
        List<CartItemVO> selectedItems = new ArrayList<>();

        for (Object value : entries.values()) {
            try {
                CartItemVO item = objectMapper.readValue((String) value, new TypeReference<CartItemVO>() {});
                if (Boolean.TRUE.equals(item.getSelected())) {
                    if (item.getPrice() != null && item.getQuantity() != null) {
                        item.setSubtotal(item.getPrice().multiply(new BigDecimal(item.getQuantity())));
                    }
                    selectedItems.add(item);
                }
            } catch (JsonProcessingException e) {
                // 忽略解析失败的项
            }
        }

        return selectedItems;
    }

    /**
     * 创建购物车项
     */
    private CartItemVO createCartItem(CartItemDTO dto) {
        CartItemVO item = new CartItemVO();
        item.setProductId(dto.getProductId());
        item.setProductName(dto.getProductName());
        item.setPrice(dto.getPrice());
        item.setQuantity(dto.getQuantity());
        item.setSelected(dto.getSelected() != null ? dto.getSelected() : true);
        item.setImage(dto.getImage());
        return item;
    }
}
