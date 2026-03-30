package com.shopflow.notification.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shopflow.common.Result;
import com.shopflow.notification.entity.Notification;
import com.shopflow.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 通知控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/notification")
@Tag(name = "通知管理", description = "通知相关接口")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    private Long getUserIdFromHeader(HttpServletRequest request) {
        String userIdStr = request.getHeader("X-User-Id");
        if (userIdStr == null) {
            throw new RuntimeException("未获取到用户信息");
        }
        return Long.parseLong(userIdStr);
    }

    @Operation(summary = "获取通知列表", description = "分页获取通知列表")
    @GetMapping("/list")
    public Result<IPage<Notification>> getNotificationList(
            HttpServletRequest request,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "是否已读") @RequestParam(required = false) Boolean isRead) {
        try {
            Long userId = getUserIdFromHeader(request);
            Page<Notification> page = new Page<>(current, size);
            IPage<Notification> result = notificationService.getNotificationList(page, userId, isRead);
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取通知列表失败", e);
            return Result.error(500, "获取通知列表失败");
        }
    }

    @Operation(summary = "获取未读消息数量", description = "获取当前用户未读消息数量")
    @GetMapping("/unread-count")
    public Result<Long> getUnreadCount(HttpServletRequest request) {
        try {
            Long userId = getUserIdFromHeader(request);
            Long count = notificationService.getUnreadCount(userId);
            return Result.success(count);
        } catch (Exception e) {
            log.error("获取未读消息数量失败", e);
            return Result.error(500, "获取未读消息数量失败");
        }
    }

    @Operation(summary = "标记通知为已读", description = "标记单个通知为已读")
    @PutMapping("/{id}/read")
    public Result<String> markAsRead(
            HttpServletRequest request,
            @PathVariable Long id) {
        try {
            Long userId = getUserIdFromHeader(request);
            notificationService.markAsRead(id, userId);
            return Result.success("操作成功");
        } catch (Exception e) {
            log.error("标记已读失败: id={}", id, e);
            return Result.error(400, e.getMessage());
        }
    }

    @Operation(summary = "标记全部为已读", description = "标记所有通知为已读")
    @PutMapping("/read-all")
    public Result<String> markAllAsRead(HttpServletRequest request) {
        try {
            Long userId = getUserIdFromHeader(request);
            notificationService.markAllAsRead(userId);
            return Result.success("操作成功");
        } catch (Exception e) {
            log.error("标记全部已读失败", e);
            return Result.error(400, e.getMessage());
        }
    }
}
