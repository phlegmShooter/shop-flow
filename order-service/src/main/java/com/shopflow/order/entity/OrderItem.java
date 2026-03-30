package com.shopflow.order.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单项实体类
 */
@Data
@TableName("t_order_item")
@Schema(description = "订单项")
public class OrderItem {

    @TableId(type = IdType.AUTO)
    @Schema(description = "订单项ID")
    private Long id;

    @Schema(description = "订单ID")
    private Long orderId;

    @Schema(description = "商品ID")
    private Long productId;

    @Schema(description = "商品名称快照")
    private String productName;

    @Schema(description = "下单时价格快照")
    private BigDecimal price;

    @Schema(description = "数量")
    private Integer quantity;

    @Schema(description = "小计")
    private BigDecimal subtotal;

    @Schema(description = "商品图片URL快照")
    private String image;
}
