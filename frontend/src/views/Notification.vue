<template>
  <div class="notification-page">
    <div class="container">
      <!-- 页面标题 -->
      <div class="page-header">
        <div class="header-left">
          <h1 class="page-title">{{ $t('notification.title') }}</h1>
          <span class="unread-badge" v-if="unreadCount > 0">
            {{ $t('notification.unreadCountBadge', { count: unreadCount }) }}
          </span>
        </div>
        <button
          v-if="unreadCount > 0"
          class="mark-all-btn"
          @click="handleMarkAllRead"
        >
          ✓ {{ $t('notification.markAllRead') }}
        </button>
      </div>

      <!-- 通知卡片 -->
      <div class="notification-wrapper">
        <!-- 标签切换 -->
        <div class="tab-bar">
          <button
            class="tab-btn"
            :class="{ active: activeTab === '' }"
            @click="switchTab('')"
          >
            {{ $t('notification.all') }}
          </button>
          <button
            class="tab-btn"
            :class="{ active: activeTab === 'unread' }"
            @click="switchTab('unread')"
          >
            {{ $t('notification.unread') }}
            <span v-if="unreadCount" class="tab-count">{{ unreadCount }}</span>
          </button>
        </div>

        <!-- 通知列表 -->
        <div v-loading="loading" class="notification-list-wrapper">
          <template v-if="notifications.length">
            <div
              v-for="item in notifications"
              :key="item.id"
              class="notification-item"
              :class="{ unread: item.isRead === 0 }"
              @click="handleRead(item)"
            >
              <div class="notification-icon" :style="{ background: getTypeGradient(item.type) }">
                <span class="ni-emoji">{{ getTypeEmoji(item.type) }}</span>
              </div>
              <div class="notification-body">
                <div class="nb-header">
                  <h4 class="nb-title">{{ item.title }}</h4>
                  <span class="nb-time">{{ formatTime(item.createdAt) }}</span>
                </div>
                <p class="nb-content">{{ item.content }}</p>
              </div>
              <div v-if="item.isRead === 0" class="unread-indicator"></div>
            </div>
          </template>

          <div v-else-if="!loading" class="empty-notifications">
            <div class="empty-icon">🔔</div>
            <h3>{{ $t('notification.emptyTitle') }}</h3>
            <p>{{ $t('notification.emptyDesc') }}</p>
          </div>
        </div>

        <!-- 分页 -->
        <div class="pagination-wrapper" v-if="total > size">
          <el-pagination
            v-model:current-page="current"
            v-model:page-size="size"
            :total="total"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next"
            background
            @size-change="handleSizeChange"
            @current-change="fetchNotifications"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getNotificationList, getUnreadCount as apiGetUnreadCount, markAsRead, markAllAsRead } from '@/api/notification'
import type { Notification } from '@/types'

const { t } = useI18n()
const loading = ref(false)
const activeTab = ref('')
const notifications = ref<Notification[]>([])
const unreadCount = ref(0)
const current = ref(1)
const size = ref(10)
const total = ref(0)

const formatTime = (time: string) => {
  const date = new Date(time)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const locale = t('common.locale') || 'zh-CN'

  if (diff < 60000) return t('notification.time.justNow')
  if (diff < 3600000) return t('notification.time.minutesAgo', { count: Math.floor(diff / 60000) })
  if (diff < 86400000) return t('notification.time.hoursAgo', { count: Math.floor(diff / 3600000) })
  if (diff < 604800000) return t('notification.time.daysAgo', { count: Math.floor(diff / 86400000) })
  return date.toLocaleDateString(locale)
}

const getTypeEmoji = (type: string) => {
  const map: Record<string, string> = {
    'order': '📦',
    'payment': '💰',
    'shipment': '🚚',
    'success': '✅',
    'warning': '⚠️',
    'info': 'ℹ️'
  }
  return map[type] || 'ℹ️'
}

const getTypeGradient = (type: string) => {
  const map: Record<string, string> = {
    'order': 'linear-gradient(135deg, #6366f1, #8b5cf6)',
    'payment': 'linear-gradient(135deg, #10b981, #34d399)',
    'shipment': 'linear-gradient(135deg, #f59e0b, #fbbf24)',
    'success': 'linear-gradient(135deg, #10b981, #6ee7b7)',
    'warning': 'linear-gradient(135deg, #f59e0b, #fcd34d)',
    'info': 'linear-gradient(135deg, #3b82f6, #93c5fd)'
  }
  return map[type] || 'linear-gradient(135deg, #64748b, #94a3b8)'
}

const switchTab = (val: string) => {
  activeTab.value = val
  current.value = 1
  fetchNotifications()
}

const fetchNotifications = async () => {
  loading.value = true
  try {
    const data = await getNotificationList({
      current: current.value,
      size: size.value,
      isRead: activeTab.value === 'unread' ? false : undefined
    })
    notifications.value = data.records
    total.value = data.total
  } catch (error) {
    console.error('获取消息列表失败', error)
  } finally {
    loading.value = false
  }
}

const fetchUnreadCount = async () => {
  try {
    unreadCount.value = await apiGetUnreadCount()
  } catch (error) {
    console.error('获取未读消息数失败', error)
  }
}

const handleRead = async (item: Notification) => {
  if (item.isRead === 0) {
    try {
      await markAsRead(item.id)
      item.isRead = 1
      unreadCount.value = Math.max(0, unreadCount.value - 1)
    } catch (error) {
      console.error('标记已读失败', error)
    }
  }
}

const handleMarkAllRead = async () => {
  try {
    await ElMessageBox.confirm(t('notification.markAllConfirmDesc'), t('common.confirm'), {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      type: 'info'
    })
    await markAllAsRead()
    ElMessage.success(t('notification.markAllSuccess'))
    notifications.value.forEach(item => { item.isRead = 1 })
    unreadCount.value = 0
  } catch (error) {
    if (error !== 'cancel') console.error('标记全部已读失败', error)
  }
}

const handleSizeChange = () => {
  current.value = 1
  fetchNotifications()
}

onMounted(() => {
  fetchNotifications()
  fetchUnreadCount()
})
</script>

<style scoped lang="scss">
.notification-page {
  padding: 32px 0 48px;
  min-height: 70vh;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;

  .header-left {
    display: flex;
    align-items: center;
    gap: 16px;
  }

  .page-title {
    font-size: 28px;
    font-weight: 800;
    color: var(--text-primary);
  }

  .unread-badge {
    background: var(--gradient-accent);
    color: white;
    font-size: 12px;
    font-weight: 700;
    padding: 4px 14px;
    border-radius: var(--border-radius-full);
  }

  .mark-all-btn {
    padding: 10px 20px;
    background: white;
    border: 1.5px solid var(--color-primary);
    border-radius: var(--border-radius-full);
    font-size: 13px;
    font-weight: 700;
    color: var(--color-primary);
    cursor: pointer;
    font-family: inherit;
    transition: all var(--transition-fast);

    &:hover {
      background: var(--gradient-primary);
      color: white;
      border-color: transparent;
      box-shadow: var(--shadow-primary);
    }
  }
}

/* ========================
   Notification Wrapper
   ======================== */
.notification-wrapper {
  background: white;
  border-radius: var(--border-radius-md);
  border: 1px solid var(--border-color);
  box-shadow: var(--shadow-sm);
  overflow: hidden;
}

/* ========================
   Tab Bar
   ======================== */
.tab-bar {
  display: flex;
  gap: 4px;
  padding: 12px 16px;
  border-bottom: 1px solid var(--border-color);
  background: var(--bg-secondary);
}

.tab-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 20px;
  border: none;
  background: transparent;
  border-radius: var(--border-radius-full);
  font-size: 14px;
  font-weight: 500;
  color: var(--text-secondary);
  cursor: pointer;
  font-family: inherit;
  transition: all var(--transition-fast);

  &:hover {
    background: white;
    color: var(--text-primary);
  }

  &.active {
    background: var(--gradient-primary);
    color: white;
    font-weight: 700;
    box-shadow: var(--shadow-primary);
  }

  .tab-count {
    background: rgba(255,255,255,0.3);
    color: white;
    font-size: 11px;
    font-weight: 800;
    padding: 1px 7px;
    border-radius: var(--border-radius-full);
    min-width: 20px;
    text-align: center;
    line-height: 16px;
  }
}

/* ========================
   Notification Items
   ======================== */
.notification-list-wrapper {
  min-height: 200px;
}

.notification-item {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 20px 24px;
  border-bottom: 1px solid var(--border-color);
  cursor: pointer;
  transition: all var(--transition-fast);
  position: relative;

  &:last-child {
    border-bottom: none;
  }

  &:hover {
    background: var(--bg-secondary);
  }

  &.unread {
    background: linear-gradient(135deg, #f5f7ff 0%, #f0f2ff 100%);

    &:hover {
      background: linear-gradient(135deg, #eef0ff 0%, #e8eaff 100%);
    }
  }
}

.notification-icon {
  width: 52px;
  height: 52px;
  border-radius: var(--border-radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: var(--shadow-sm);

  .ni-emoji {
    font-size: 24px;
  }
}

.notification-body {
  flex: 1;
  min-width: 0;

  .nb-header {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    gap: 16px;
    margin-bottom: 8px;

    .nb-title {
      font-size: 15px;
      font-weight: 700;
      color: var(--text-primary);
      line-height: 1.4;
    }

    .nb-time {
      font-size: 12px;
      color: var(--text-muted);
      flex-shrink: 0;
      margin-top: 2px;
    }
  }

  .nb-content {
    font-size: 14px;
    color: var(--text-secondary);
    line-height: 1.6;
  }
}

.unread-indicator {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: var(--gradient-accent);
  flex-shrink: 0;
  margin-top: 6px;
  box-shadow: 0 0 0 3px rgba(244, 63, 94, 0.2);
}

/* ========================
   Empty State
   ======================== */
.empty-notifications {
  text-align: center;
  padding: 80px 0;

  .empty-icon {
    font-size: 72px;
    margin-bottom: 16px;
    opacity: 0.4;
  }

  h3 {
    font-size: 20px;
    font-weight: 700;
    color: var(--text-primary);
    margin-bottom: 8px;
  }

  p {
    font-size: 14px;
    color: var(--text-muted);
  }
}

/* ========================
   Pagination
   ======================== */
.pagination-wrapper {
  padding: 20px 24px;
  border-top: 1px solid var(--border-color);
  display: flex;
  justify-content: center;
}
</style>
