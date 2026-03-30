package com.shopflow.order.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shopflow.order.entity.OrderItem;
import com.shopflow.order.mapper.OrderItemMapper;
import com.shopflow.order.service.OrderItemService;
import org.springframework.stereotype.Service;

/**
 * 订单项服务实现类
 */
@Service
public class OrderItemServiceImpl extends ServiceImpl<OrderItemMapper, OrderItem> implements OrderItemService {
}
