package com.shopflow.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 购物车项DTO
 */
@Data
@Schema(description = "购物车项DTO")
public class CartItemDTO {

    @Schema(description = "商品ID")
    @NotNull(message = "商品ID不能为空")
    private Long productId;

    @Schema(description = "商品名称")
    private String productName;

    @Schema(description = "商品价格")
    private BigDecimal price;

    @Schema(description = "数量")
    @NotNull(message = "数量不能为空")
    @Min(value = 1, message = "数量至少为1")
    private Integer quantity;

    @Schema(description = "是否选中")
    private Boolean selected = true;

    @Schema(description = "商品图片")
    private String image;
}
