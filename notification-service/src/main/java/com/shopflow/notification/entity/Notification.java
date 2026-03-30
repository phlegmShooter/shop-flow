package com.shopflow.notification.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通知实体类
 */
@Data
@TableName("t_notification")
@Schema(description = "通知")
public class Notification {

    @TableId(type = IdType.AUTO)
    @Schema(description = "通知ID")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "通知类型")
    private String type;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "内容")
    private String content;

    @Schema(description = "是否已读：0未读 1已读")
    private Integer isRead;

    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
