import request from '@/utils/request'
import type { Notification } from '@/types'

// 获取通知列表
export const getNotificationList = (params: {
  current?: number
  size?: number
  isRead?: boolean
}): Promise<{ records: Notification[]; total: number; size: number; current: number }> => {
  return request.get('/notification/list', { params })
}

// 获取未读消息数量
export const getUnreadCount = (): Promise<number> => {
  return request.get('/notification/unread-count')
}

// 标记通知为已读
export const markAsRead = (id: number): Promise<void> => {
  return request.put(`/notification/${id}/read`)
}

// 标记全部为已读
export const markAllAsRead = (): Promise<void> => {
  return request.put('/notification/read-all')
}
