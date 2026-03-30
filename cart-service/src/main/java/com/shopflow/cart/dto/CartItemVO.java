package com.shopflow.cart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 购物车项VO
 */
@Data
@Schema(description = "购物车项VO")
public class CartItemVO {

    @Schema(description = "商品ID")
    private Long productId;

    @Schema(description = "商品名称")
    private String productName;

    @Schema(description = "商品价格")
    private BigDecimal price;

    @Schema(description = "数量")
    private Integer quantity;

    @Schema(description = "是否选中")
    private Boolean selected;

    @Schema(description = "商品图片")
    private String image;

    @Schema(description = "小计")
    private BigDecimal subtotal;
}
