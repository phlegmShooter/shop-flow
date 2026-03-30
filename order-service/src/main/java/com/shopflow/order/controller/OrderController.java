package com.shopflow.order.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shopflow.common.Result;
import com.shopflow.common.dto.OrderCreateDTO;
import com.shopflow.order.entity.Order;
import com.shopflow.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 订单控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/order")
@Tag(name = "订单管理", description = "订单相关接口")
public class OrderController {

    @Autowired
    private OrderService orderService;

    private Long getUserIdFromHeader(HttpServletRequest request) {
        String userIdStr = request.getHeader("X-User-Id");
        if (userIdStr == null) {
            throw new RuntimeException("未获取到用户信息");
        }
        return Long.parseLong(userIdStr);
    }

    @Operation(summary = "创建订单", description = "创建新订单")
    @PostMapping("/create")
    public Result<String> createOrder(
            HttpServletRequest request,
            @Valid @RequestBody OrderCreateDTO orderCreateDTO) {
        try {
            Long userId = getUserIdFromHeader(request);
            String orderNo = orderService.createOrder(userId, orderCreateDTO);
            return Result.success("订单创建成功", orderNo);
        } catch (Exception e) {
            log.error("创建订单失败", e);
            return Result.error(400, e.getMessage());
        }
    }

    @Operation(summary = "获取订单列表", description = "分页获取订单列表")
    @GetMapping("/list")
    public Result<IPage<Order>> getOrderList(
            HttpServletRequest request,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "订单状态") @RequestParam(required = false) Integer status) {
        try {
            Long userId = getUserIdFromHeader(request);
            Page<Order> page = new Page<>(current, size);
            IPage<Order> result = orderService.getOrderList(page, userId, status);
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取订单列表失败", e);
            return Result.error(500, "获取订单列表失败");
        }
    }

    @Operation(summary = "获取订单详情", description = "根据订单号获取订单详情")
    @GetMapping("/{orderNo}")
    public Result<Order> getOrderDetail(
            HttpServletRequest request,
            @PathVariable String orderNo) {
        try {
            Long userId = getUserIdFromHeader(request);
            Order order = orderService.getOrderDetail(orderNo, userId);
            return Result.success(order);
        } catch (Exception e) {
            log.error("获取订单详情失败: orderNo={}", orderNo, e);
            return Result.error(404, e.getMessage());
        }
    }

    @Operation(summary = "模拟支付", description = "模拟订单支付")
    @PostMapping("/{orderNo}/pay")
    public Result<String> payOrder(
            HttpServletRequest request,
            @PathVariable String orderNo) {
        try {
            Long userId = getUserIdFromHeader(request);
            orderService.payOrder(orderNo, userId);
            return Result.success("支付成功");
        } catch (Exception e) {
            log.error("支付失败: orderNo={}", orderNo, e);
            return Result.error(400, e.getMessage());
        }
    }

    @Operation(summary = "取消订单", description = "取消订单")
    @PostMapping("/{orderNo}/cancel")
    public Result<String> cancelOrder(
            HttpServletRequest request,
            @PathVariable String orderNo) {
        try {
            Long userId = getUserIdFromHeader(request);
            orderService.cancelOrder(orderNo, userId);
            return Result.success("订单已取消");
        } catch (Exception e) {
            log.error("取消订单失败: orderNo={}", orderNo, e);
            return Result.error(400, e.getMessage());
        }
    }

    @Operation(summary = "确认收货", description = "确认订单收货")
    @PostMapping("/{orderNo}/confirm")
    public Result<String> confirmOrder(
            HttpServletRequest request,
            @PathVariable String orderNo) {
        try {
            Long userId = getUserIdFromHeader(request);
            orderService.confirmOrder(orderNo, userId);
            return Result.success("确认收货成功");
        } catch (Exception e) {
            log.error("确认收货失败: orderNo={}", orderNo, e);
            return Result.error(400, e.getMessage());
        }
    }
}
