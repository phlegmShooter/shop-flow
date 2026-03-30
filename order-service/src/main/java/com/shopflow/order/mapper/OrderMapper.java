package com.shopflow.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shopflow.order.entity.Order;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单Mapper
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {
}
