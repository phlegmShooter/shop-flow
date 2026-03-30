package com.shopflow.order.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.shopflow.common.dto.OrderCreateDTO;
import com.shopflow.order.entity.Order;

/**
 * 订单服务接口
 */
public interface OrderService extends IService<Order> {

    /**
     * 创建订单
     */
    String createOrder(Long userId, OrderCreateDTO orderCreateDTO);

    /**
     * 获取订单列表
     */
    IPage<Order> getOrderList(Page<Order> page, Long userId, Integer status);

    /**
     * 获取订单详情
     */
    Order getOrderDetail(String orderNo, Long userId);

    /**
     * 模拟支付
     */
    void payOrder(String orderNo, Long userId);

    /**
     * 取消订单
     */
    void cancelOrder(String orderNo, Long userId);

    /**
     * 确认收货
     */
    void confirmOrder(String orderNo, Long userId);

    /**
     * 订单超时取消（内部调用）
     */
    void timeoutCancel(String orderNo);
}
