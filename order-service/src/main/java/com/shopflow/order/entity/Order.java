package com.shopflow.order.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单实体类
 */
@Data
@TableName("t_order")
@Schema(description = "订单")
public class Order {

    @TableId(type = IdType.AUTO)
    @Schema(description = "订单ID")
    private Long id;

    @Schema(description = "订单号")
    private String orderNo;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "订单总金额")
    private BigDecimal totalAmount;

    @Schema(description = "状态：0待支付 1已支付 2已发货 3已完成 4已取消")
    private Integer status;

    @Schema(description = "收货地址（JSON快照）")
    private String address;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "支付时间")
    private LocalDateTime paidAt;

    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    @Schema(description = "订单项列表")
    private List<OrderItem> items;
}
