package com.shopflow.notification.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.shopflow.notification.entity.Notification;

/**
 * 通知服务接口
 */
public interface NotificationService extends IService<Notification> {

    /**
     * 创建通知
     */
    void createNotification(Long userId, String type, String title, String content);

    /**
     * 获取用户通知列表
     */
    IPage<Notification> getNotificationList(Page<Notification> page, Long userId, Boolean isRead);

    /**
     * 标记通知为已读
     */
    void markAsRead(Long notificationId, Long userId);

    /**
     * 标记全部为已读
     */
    void markAllAsRead(Long userId);

    /**
     * 获取未读消息数量
     */
    Long getUnreadCount(Long userId);
}
