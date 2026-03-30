<template>
  <div class="order-page">
    <div class="container">
      <div class="page-header">
        <h1 class="page-title">{{ $t('order.title') }}</h1>
      </div>

      <!-- 状态标签页 -->
      <div class="order-tabs">
        <button
          v-for="tab in tabs"
          :key="tab.value"
          class="tab-btn"
          :class="{ active: activeTab === tab.value }"
          @click="switchTab(tab.value)"
        >
          {{ $t(`order.status.${tab.i18nKey}`) }}
        </button>
      </div>

      <!-- 订单列表 -->
      <div v-loading="loading" class="order-list">
        <template v-if="orders.length">
          <div v-for="order in orders" :key="order.id" class="order-card">
            <!-- 订单头部 -->
            <div class="order-head">
              <div class="order-meta">
                <span class="order-no">
                  <el-icon><Document /></el-icon>
                  {{ order.orderNo }}
                </span>
                <span class="order-time">{{ formatTime(order.createdAt) }}</span>
              </div>
              <div :class="['order-status-badge', `status-${order.status}`]">
                {{ $t(`order.status.${getStatusKey(order.status)}`) }}
              </div>
            </div>

            <!-- 订单商品 -->
            <div class="order-items">
              <div
                v-for="item in order.items"
                :key="item.id"
                class="order-item"
              >
                <div class="oi-image">
                  <img
                    v-if="item.image"
                    :src="item.image"
                    :alt="item.productName"
                    @error="onImgError"
                  />
                  <!-- 无图片时用商品名首字占位 -->
                  <div v-else class="oi-image-placeholder">
                    {{ item.productName?.charAt(0) || '?' }}
                  </div>
                </div>
                <div class="oi-info">
                  <div class="oi-name">{{ item.productName }}</div>
                  <div class="oi-price">¥{{ item.price.toFixed(2) }} × {{ item.quantity }}</div>
                </div>
                <div class="oi-subtotal">¥{{ item.subtotal.toFixed(2) }}</div>
              </div>
            </div>

            <!-- 订单底部 -->
            <div class="order-foot">
              <div class="order-total">
                {{ $t('order.totalItems', { count: order.items?.length || 0 }) }}
                <span class="total-amount">¥{{ order.totalAmount.toFixed(2) }}</span>
              </div>
              <div class="order-actions">
                <template v-if="order.status === 0">
                  <button class="action-btn btn-primary" @click="handlePay(order)">
                    {{ $t('order.payNow') }}
                  </button>
                  <button class="action-btn btn-ghost" @click="handleCancel(order)">
                    {{ $t('order.cancelOrder') }}
                  </button>
                </template>
                <template v-else-if="order.status === 2">
                  <button class="action-btn btn-primary" @click="handleConfirm(order)">
                    {{ $t('order.confirmReceipt') }}
                  </button>
                </template>
                <template v-else-if="order.status === 3">
                  <button class="action-btn btn-ghost" @click="$router.push('/home')">
                    {{ $t('order.buyAgain') }}
                  </button>
                </template>
              </div>
            </div>
          </div>
        </template>

        <div v-else-if="!loading" class="empty-orders">
          <div class="empty-icon">📋</div>
          <h3>{{ $t('order.emptyTitle') }}</h3>
          <p>{{ $t('order.emptyDesc') }}</p>
          <button class="go-shop-btn" @click="$router.push('/home')">{{ $t('cart.goShopping') }}</button>
        </div>
      </div>

      <!-- 分页 -->
      <div class="pagination-wrapper" v-if="total > size">
        <el-pagination
          v-model:current-page="current"
          v-model:page-size="size"
          :total="total"
          layout="prev, pager, next"
          background
          @current-change="fetchOrders"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Document } from '@element-plus/icons-vue'
import { getOrderList, payOrder, cancelOrder, confirmOrder } from '@/api/order'
import type { Order } from '@/types'

const { t } = useI18n()

const loading = ref(false)
const activeTab = ref('')
const orders = ref<Order[]>([])
const current = ref(1)
const size = ref(10)
const total = ref(0)

const tabs = [
  { label: '全部', value: '', i18nKey: 'all' },
  { label: '待支付', value: '0', i18nKey: 'pendingPay' },
  { label: '已支付', value: '1', i18nKey: 'paid' },
  { label: '已发货', value: '2', i18nKey: 'shipped' },
  { label: '已完成', value: '3', i18nKey: 'completed' },
  { label: '已取消', value: '4', i18nKey: 'canceled' }
]

const getStatusKey = (status: number) => {
  const map: Record<number, string> = {
    0: 'pendingPay',
    1: 'paid',
    2: 'shipped',
    3: 'completed',
    4: 'canceled'
  }
  return map[status] || 'all'
}

const formatTime = (time: string) => {
  return new Date(time).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const switchTab = (val: string) => {
  activeTab.value = val
  current.value = 1
  fetchOrders()
}

const fetchOrders = async () => {
  loading.value = true
  try {
    const data = await getOrderList({
      current: current.value,
      size: size.value,
      status: activeTab.value ? Number(activeTab.value) : undefined
    })
    orders.value = data.records
    total.value = data.total
  } catch (error) {
    console.error('获取订单列表失败', error)
  } finally {
    loading.value = false
  }
}

const handlePay = async (order: Order) => {
  try {
    await ElMessageBox.confirm(
      t('order.payConfirmDesc', { no: order.orderNo, amount: order.totalAmount.toFixed(2) }),
      t('order.payConfirmTitle'),
      {
        confirmButtonText: t('order.payNow'),
        cancelButtonText: t('common.cancel'),
        type: 'info'
      }
    )
    await payOrder(order.orderNo)
    ElMessage.success(t('order.paySuccess'))
    fetchOrders()
  } catch (error) {
    if (error !== 'cancel') console.error('支付失败', error)
  }
}

const handleCancel = async (order: Order) => {
  try {
    await ElMessageBox.confirm(t('order.cancelConfirmDesc'), t('order.cancelOrder'), {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      type: 'warning'
    })
    await cancelOrder(order.orderNo)
    ElMessage.success(t('order.cancelSuccess'))
    fetchOrders()
  } catch (error) {
    if (error !== 'cancel') console.error('取消订单失败', error)
  }
}

const handleConfirm = async (order: Order) => {
  try {
    await ElMessageBox.confirm(t('order.receiptConfirmDesc'), t('order.confirmReceipt'), {
      confirmButtonText: t('order.confirmReceipt'),
      cancelButtonText: t('common.cancel'),
      type: 'success'
    })
    await confirmOrder(order.orderNo)
    ElMessage.success(t('order.receiptSuccess'))
    fetchOrders()
  } catch (error) {
    if (error !== 'cancel') console.error('确认收货失败', error)
  }
}

const onImgError = (e: Event) => {
  const img = e.target as HTMLImageElement
  img.src = 'http://192.168.1.4:7000/images/placeholder.png'
  img.onerror = null
}

onMounted(() => {
  fetchOrders()
})
</script>

<style scoped lang="scss">
.order-page {
  padding: 32px 0 48px;
  min-height: 70vh;
}

.page-header {
  margin-bottom: 24px;

  .page-title {
    font-size: 28px;
    font-weight: 800;
    color: var(--text-primary);
  }
}

/* ========================
   Tabs
   ======================== */
.order-tabs {
  display: flex;
  gap: 4px;
  background: white;
  border-radius: var(--border-radius-md);
  border: 1px solid var(--border-color);
  padding: 6px;
  margin-bottom: 24px;
  box-shadow: var(--shadow-sm);
  overflow-x: auto;
}

.tab-btn {
  flex-shrink: 0;
  padding: 8px 20px;
  border: none;
  background: transparent;
  border-radius: var(--border-radius-sm);
  font-size: 14px;
  font-weight: 500;
  color: var(--text-secondary);
  cursor: pointer;
  font-family: inherit;
  transition: all var(--transition-fast);

  &:hover {
    background: var(--bg-secondary);
    color: var(--text-primary);
  }

  &.active {
    background: var(--gradient-primary);
    color: white;
    font-weight: 700;
    box-shadow: var(--shadow-primary);
  }
}

/* ========================
   Order List
   ======================== */
.order-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-height: 200px;
}

/* ========================
   Order Card
   ======================== */
.order-card {
  background: white;
  border-radius: var(--border-radius-md);
  border: 1px solid var(--border-color);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
  transition: all var(--transition-normal);

  &:hover {
    box-shadow: var(--shadow-md);
  }
}

.order-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 24px;
  background: var(--bg-secondary);
  border-bottom: 1px solid var(--border-color);

  .order-meta {
    display: flex;
    align-items: center;
    gap: 20px;

    .order-no {
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: 13px;
      font-weight: 600;
      color: var(--text-secondary);
      font-family: monospace;
    }

    .order-time {
      font-size: 13px;
      color: var(--text-muted);
    }
  }

  .order-status-badge {
    font-size: 12px;
    font-weight: 700;
    padding: 4px 14px;
    border-radius: var(--border-radius-full);
  }
}

.order-items {
  padding: 4px 0;
}

.order-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 24px;
  border-bottom: 1px solid var(--border-color);
  transition: background var(--transition-fast);

  &:last-child {
    border-bottom: none;
  }

  &:hover {
    background: rgba(99, 102, 241, 0.02);
  }

  .oi-image {
    width: 64px;
    height: 64px;
    border-radius: var(--border-radius-sm);
    overflow: hidden;
    flex-shrink: 0;
    background: var(--bg-secondary);
    border: 1px solid var(--border-color);

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    .oi-image-placeholder {
      width: 100%;
      height: 100%;
      display: flex;
      align-items: center;
      justify-content: center;
      background: linear-gradient(135deg, #6366f1, #8b5cf6);
      color: white;
      font-size: 22px;
      font-weight: 700;
    }
  }

  .oi-info {
    flex: 1;
    min-width: 0;

    .oi-name {
      font-size: 14px;
      font-weight: 600;
      color: var(--text-primary);
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      margin-bottom: 6px;
    }

    .oi-price {
      font-size: 13px;
      color: var(--text-muted);
    }
  }

  .oi-subtotal {
    font-size: 15px;
    font-weight: 700;
    color: var(--text-primary);
    flex-shrink: 0;
  }
}

.order-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 24px;
  border-top: 1px solid var(--border-color);
  background: linear-gradient(135deg, #fafbff 0%, #f5f6ff 100%);

  .order-total {
    font-size: 14px;
    color: var(--text-secondary);

    .total-amount {
      font-size: 18px;
      font-weight: 800;
      color: var(--color-accent);
    }
  }

  .order-actions {
    display: flex;
    gap: 10px;
  }
}

/* ========================
   Action Buttons
   ======================== */
.action-btn {
  padding: 8px 20px;
  border-radius: var(--border-radius-full);
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
  font-family: inherit;
  transition: all var(--transition-fast);

  &.btn-primary {
    background: var(--gradient-primary);
    color: white;
    border: none;
    box-shadow: var(--shadow-primary);

    &:hover {
      transform: translateY(-1px);
      box-shadow: 0 8px 20px rgba(99, 102, 241, 0.4);
    }
  }

  &.btn-ghost {
    background: white;
    color: var(--text-secondary);
    border: 1.5px solid var(--border-color);

    &:hover {
      border-color: var(--color-primary);
      color: var(--color-primary);
    }
  }
}

/* ========================
   Empty State
   ======================== */
.empty-orders {
  text-align: center;
  padding: 80px 0;
  background: white;
  border-radius: var(--border-radius-lg);
  border: 1px solid var(--border-color);

  .empty-icon {
    font-size: 72px;
    margin-bottom: 16px;
    opacity: 0.5;
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
    margin-bottom: 28px;
  }

  .go-shop-btn {
    background: var(--gradient-primary);
    color: white;
    border: none;
    border-radius: var(--border-radius-full);
    padding: 12px 36px;
    font-size: 15px;
    font-weight: 700;
    cursor: pointer;
    font-family: inherit;
    box-shadow: var(--shadow-primary);
    transition: all var(--transition-fast);

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 10px 24px rgba(99, 102, 241, 0.4);
    }
  }
}

/* ========================
   Pagination
   ======================== */
.pagination-wrapper {
  margin-top: 32px;
  display: flex;
  justify-content: center;
}

/* Status Overrides */
.status-0 { background: #fef3c7; color: #92400e; }
.status-1 { background: #dbeafe; color: #1e40af; }
.status-2 { background: #e0f2fe; color: #0369a1; }
.status-3 { background: #d1fae5; color: #065f46; }
.status-4 { background: #f3f4f6; color: #6b7280; }
</style>
