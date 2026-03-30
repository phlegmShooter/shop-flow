package com.shopflow.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 创建订单DTO
 */
@Data
@Schema(description = "创建订单DTO")
public class OrderCreateDTO {

    @Schema(description = "收货地址（JSON格式）")
    @NotBlank(message = "收货地址不能为空")
    private String address;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "商品ID列表（从购物车下单时使用）")
    @NotEmpty(message = "请选择要购买的商品")
    private List<Long> productIds;
}
