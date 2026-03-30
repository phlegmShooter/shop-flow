package com.shopflow.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 商品DTO
 */
@Data
@Schema(description = "商品DTO")
public class ProductDTO {

    @Schema(description = "商品ID")
    private Long id;

    @Schema(description = "分类ID")
    @NotNull(message = "分类ID不能为空")
    private Long categoryId;

    @Schema(description = "商品名称")
    @NotBlank(message = "商品名称不能为空")
    private String name;

    @Schema(description = "商品描述")
    private String description;

    @Schema(description = "价格")
    @NotNull(message = "价格不能为空")
    @DecimalMin(value = "0.01", message = "价格必须大于0")
    private BigDecimal price;

    @Schema(description = "库存")
    @NotNull(message = "库存不能为空")
    private Integer stock;

    @Schema(description = "图片URL，逗号分隔")
    private String images;

    @Schema(description = "状态：1上架 0下架")
    private Integer status = 1;
}
