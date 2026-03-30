package com.shopflow.order.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 购物车商品快照（Order Service 内部用于读取 Redis 购物车数据）
 */
@Data
public class CartItemSnapshot {

    /** 商品ID */
    private Long productId;

    /** 商品名称 */
    private String productName;

    /** 下单时价格 */
    private BigDecimal price;

    /** 数量 */
    private Integer quantity;

    /** 是否选中 */
    private Boolean selected;

    /** 商品图片 */
    private String image;

    /** 小计（可为 null） */
    private BigDecimal subtotal;
}
