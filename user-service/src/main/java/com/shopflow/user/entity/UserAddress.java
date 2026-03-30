package com.shopflow.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户收货地址实体
 * 对应数据库表 t_user_address
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_user_address")
public class UserAddress implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    @TableField("user_id")
    private Long userId;

    /** 收货人姓名 */
    @TableField("receiver_name")
    private String receiverName;

    /** 手机号 */
    @TableField("phone")
    private String phone;

    /** 省 */
    @TableField("province")
    private String province;

    /** 市 */
    @TableField("city")
    private String city;

    /** 区/县（可为空） */
    @TableField("district")
    private String district;

    /** 详细地址（街道/楼栋/门牌等） */
    @TableField("detail")
    private String detail;

    /** 是否默认地址：1是 0否 */
    @TableField("is_default")
    private Integer isDefault;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
