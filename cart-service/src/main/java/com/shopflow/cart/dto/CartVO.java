package com.shopflow.cart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 购物车VO
 */
@Data
@Schema(description = "购物车VO")
public class CartVO {

    @Schema(description = "购物车商品列表")
    private List<CartItemVO> items;

    @Schema(description = "选中商品总数")
    private Integer selectedCount;

    @Schema(description = "商品总数量")
    private Integer totalCount;

    @Schema(description = "选中商品总金额")
    private BigDecimal selectedAmount;
}
