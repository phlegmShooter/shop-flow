<template>
  <div class="cart-page">
    <div class="container">
      <div class="page-header">
        <h1 class="page-title">{{ $t('cart.title') }}</h1>
        <span class="item-count" v-if="cartStore.cartInfo?.totalCount">
          {{ $t('cart.totalItems', { count: cartStore.cartInfo.totalCount }) }}
        </span>
      </div>

      <!-- Loading -->
      <div v-if="cartStore.loading" class="loading-state">
        <div v-for="i in 3" :key="i" class="skeleton-row">
          <div class="skeleton" style="width: 80px; height: 80px; border-radius: 8px;"></div>
          <div style="flex: 1; display: flex; flex-direction: column; gap: 8px;">
            <div class="skeleton" style="height: 18px; width: 60%;"></div>
            <div class="skeleton" style="height: 14px; width: 30%;"></div>
          </div>
        </div>
      </div>

      <template v-else-if="cartStore.cartInfo && cartStore.cartInfo.items.length > 0">
        <div class="cart-layout">
          <!-- 购物车列表 -->
          <div class="cart-main">
            <div class="cart-table-header">
              <el-checkbox v-model="allSelected" @change="handleSelectAll" class="select-all-cb">
                {{ $t('cart.selectAll') }}
              </el-checkbox>
              <span class="col-product">{{ $t('cart.productInfo') }}</span>
              <span class="col-price">{{ $t('cart.unitPrice') }}</span>
              <span class="col-qty">{{ $t('cart.quantity') }}</span>
              <span class="col-subtotal">{{ $t('cart.subtotal') }}</span>
              <span class="col-action">{{ $t('cart.action') }}</span>
            </div>

            <div class="cart-items">
              <div
                v-for="item in cartStore.cartInfo.items"
                :key="item.productId"
                class="cart-item"
                :class="{ selected: item.selected }"
              >
                <el-checkbox
                  :model-value="item.selected"
                  @change="(val: boolean) => handleSelectItem(item.productId, val)"
                  class="item-cb"
                />
                <div class="item-image" @click="$router.push(`/product/${item.productId}`)">
                  <img :src="item.image || ''" :alt="item.productName" @error="onImgError" />
                </div>
                <div class="item-info" @click="$router.push(`/product/${item.productId}`)">
                  <h4 class="item-name">{{ item.productName }}</h4>
                </div>
                <div class="item-price">
                  <span class="price-symbol">¥</span>{{ item.price.toFixed(2) }}
                </div>
                <div class="item-qty">
                  <div class="qty-control">
                    <button @click="handleQuantityChange(item.productId, item.quantity - 1)" :disabled="item.quantity <= 1">−</button>
                    <span>{{ item.quantity }}</span>
                    <button @click="handleQuantityChange(item.productId, item.quantity + 1)">+</button>
                  </div>
                </div>
                <div class="item-subtotal">
                  <span class="price-symbol">¥</span>{{ (item.price * item.quantity).toFixed(2) }}
                </div>
                <div class="item-action">
                  <button class="del-btn" @click="handleRemove(item.productId)">
                    <el-icon><Delete /></el-icon>
                  </button>
                </div>
              </div>
            </div>
          </div>

          <!-- 结算侧边栏 -->
          <div class="cart-sidebar">
            <div class="summary-card">
              <h3 class="summary-title">{{ $t('cart.orderSummary') }}</h3>

              <div class="summary-rows">
                <div class="summary-row">
                  <span>{{ $t('cart.selectedItems') }}</span>
                  <span>{{ $t('cart.itemsCount', { count: cartStore.cartInfo.selectedCount }) }}</span>
                </div>
                <div class="summary-row">
                  <span>{{ $t('cart.totalProductAmount') }}</span>
                  <span>¥{{ cartStore.cartInfo.selectedAmount.toFixed(2) }}</span>
                </div>
                <div class="summary-row">
                  <span>{{ $t('cart.shippingFee') }}</span>
                  <span class="free-shipping">{{ $t('cart.freeShipping') }}</span>
                </div>
              </div>

              <div class="summary-total">
                <span>{{ $t('cart.totalAmountpayable') }}</span>
                <div class="total-price">
                  <span class="total-symbol">¥</span>
                  <span class="total-amount">{{ cartStore.cartInfo.selectedAmount.toFixed(2) }}</span>
                </div>
              </div>

              <button
                class="checkout-btn"
                :disabled="cartStore.cartInfo.selectedCount <= 0"
                @click="goCheckout"
              >
                {{ $t('cart.checkout') }} ({{ cartStore.cartInfo.selectedCount }})
              </button>

              <button class="clear-btn" @click="clearCart">
                <el-icon><Delete /></el-icon>
                {{ $t('cart.clearCart') }}
              </button>
            </div>

            <div class="guarantee-card">
              <div class="g-title">{{ $t('cart.shoppingGuarantee') }}</div>
              <div class="g-item" v-for="(g, idx) in guarantees" :key="idx">
                <span class="g-icon">{{ g.icon }}</span>
                <span>{{ $t(`cart.guarantees.${idx}`) }}</span>
              </div>
            </div>
          </div>
        </div>
      </template>

      <!-- 空购物车 -->
      <div v-else class="empty-cart">
        <div class="empty-icon">🛒</div>
        <h3>{{ $t('cart.emptyTitle') }}</h3>
        <p>{{ $t('cart.emptyDesc') }}</p>
        <button class="go-shop-btn" @click="$router.push('/home')">
          {{ $t('cart.goShopping') }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete } from '@element-plus/icons-vue'
import { useCartStore } from '@/stores/cart'
import { clearCart as apiClearCart } from '@/api/cart'

const router = useRouter()
const cartStore = useCartStore()
const { t } = useI18n()

const guarantees = [
  { icon: '🛡️', text: '正品保障' },
  { icon: '🔄', text: '7天退换' },
  { icon: '🔒', text: '安全支付' },
  { icon: '🚀', text: '急速发货' }
]

const allSelected = computed({
  get: () => {
    if (!cartStore.cartInfo?.items.length) return false
    return cartStore.cartInfo.items.every(item => item.selected)
  },
  set: (val: boolean) => {
    cartStore.toggleSelectAll(val)
  }
})

const handleSelectAll = (val: boolean) => {
  cartStore.toggleSelectAll(val)
}

const handleSelectItem = (productId: number, selected: boolean) => {
  cartStore.toggleSelect(productId, selected)
}

const handleQuantityChange = (productId: number, quantity: number) => {
  if (quantity < 1) return
  cartStore.updateItemQuantity(productId, quantity)
}

const handleRemove = (productId: number) => {
  cartStore.removeItem(productId)
}

const clearCart = async () => {
  try {
    await ElMessageBox.confirm(
      t('cart.clearConfirmDesc'),
      t('cart.clearConfirmTitle'),
      {
        confirmButtonText: t('common.confirm'),
        cancelButtonText: t('common.cancel'),
        type: 'warning'
      }
    )
    await apiClearCart()
    await cartStore.fetchCart()
    ElMessage.success(t('cart.clearSuccess'))
  } catch {
    // 用户取消
  }
}

const goCheckout = () => {
  if (!cartStore.cartInfo?.selectedCount || cartStore.cartInfo.selectedCount <= 0) {
    ElMessage.warning(t('cart.selectItemsFirst'))
    return
  }
  router.push('/checkout')
}

const onImgError = (e: Event) => {
  const img = e.target as HTMLImageElement
  img.src = `https://picsum.photos/seed/${Math.random()}/160/160`
}

onMounted(() => {
  cartStore.fetchCart()
})
</script>

<style scoped lang="scss">
.cart-page {
  padding: 32px 0 48px;
  min-height: 70vh;
}

.page-header {
  display: flex;
  align-items: baseline;
  gap: 16px;
  margin-bottom: 28px;

  .page-title {
    font-size: 28px;
    font-weight: 800;
    color: var(--text-primary);
  }

  .item-count {
    font-size: 14px;
    color: var(--text-muted);
  }
}

/* ========================
   Loading Skeleton
   ======================== */
.loading-state {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.skeleton-row {
  background: white;
  border-radius: var(--border-radius-md);
  padding: 20px;
  display: flex;
  gap: 20px;
  align-items: center;
  border: 1px solid var(--border-color);
}

/* ========================
   Cart Layout
   ======================== */
.cart-layout {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 24px;
  align-items: flex-start;

  @media (max-width: 1024px) {
    grid-template-columns: 1fr;
  }
}

/* ========================
   Cart Main
   ======================== */
.cart-main {
  background: white;
  border-radius: var(--border-radius-md);
  border: 1px solid var(--border-color);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
}

.cart-table-header {
  display: flex;
  align-items: center;
  padding: 16px 24px;
  background: var(--bg-secondary);
  border-bottom: 1px solid var(--border-color);
  font-size: 13px;
  font-weight: 600;
  color: var(--text-muted);

  .select-all-cb {
    margin-right: 12px;
  }

  .col-product { flex: 1; padding-left: 8px; }
  .col-price { width: 100px; text-align: center; }
  .col-qty { width: 130px; text-align: center; }
  .col-subtotal { width: 100px; text-align: center; }
  .col-action { width: 64px; text-align: center; }
}

.cart-item {
  display: flex;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid var(--border-color);
  transition: background var(--transition-fast);

  &:last-child {
    border-bottom: none;
  }

  &.selected {
    background: rgba(99, 102, 241, 0.02);
  }

  &:hover {
    background: var(--bg-secondary);
  }

  .item-cb {
    margin-right: 12px;
    flex-shrink: 0;
  }

  .item-image {
    width: 88px;
    height: 88px;
    border-radius: var(--border-radius-sm);
    overflow: hidden;
    cursor: pointer;
    flex-shrink: 0;
    margin-right: 16px;
    background: var(--bg-secondary);
    border: 1px solid var(--border-color);

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      transition: transform var(--transition-normal);
    }

    &:hover img {
      transform: scale(1.05);
    }
  }

  .item-info {
    flex: 1;
    cursor: pointer;
    min-width: 0;

    .item-name {
      font-size: 14px;
      font-weight: 600;
      color: var(--text-primary);
      overflow: hidden;
      text-overflow: ellipsis;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      line-height: 1.5;

      &:hover {
        color: var(--color-primary);
      }
    }
  }

  .item-price {
    width: 100px;
    text-align: center;
    font-size: 15px;
    font-weight: 600;
    color: var(--text-secondary);

    .price-symbol {
      font-size: 12px;
      vertical-align: super;
    }
  }

  .item-qty {
    width: 130px;
    display: flex;
    justify-content: center;
  }

  .item-subtotal {
    width: 100px;
    text-align: center;
    font-size: 16px;
    font-weight: 700;
    color: var(--color-accent);

    .price-symbol {
      font-size: 12px;
      vertical-align: super;
    }
  }

  .item-action {
    width: 64px;
    display: flex;
    justify-content: center;
  }

  .del-btn {
    width: 34px;
    height: 34px;
    border: none;
    background: transparent;
    border-radius: 50%;
    cursor: pointer;
    color: var(--text-muted);
    transition: all var(--transition-fast);
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 16px;

    &:hover {
      background: rgba(244, 63, 94, 0.1);
      color: var(--color-accent);
    }
  }
}

.qty-control {
  display: flex;
  align-items: center;
  border: 1.5px solid var(--border-color);
  border-radius: var(--border-radius-sm);
  overflow: hidden;

  button {
    width: 32px;
    height: 34px;
    border: none;
    background: var(--bg-secondary);
    cursor: pointer;
    font-size: 16px;
    color: var(--text-secondary);
    transition: all var(--transition-fast);
    font-family: inherit;
    font-weight: 400;

    &:hover:not(:disabled) {
      background: rgba(99, 102, 241, 0.1);
      color: var(--color-primary);
    }

    &:disabled {
      opacity: 0.4;
      cursor: not-allowed;
    }
  }

  span {
    width: 40px;
    text-align: center;
    font-size: 14px;
    font-weight: 700;
    color: var(--text-primary);
  }
}

/* ========================
   Sidebar
   ======================== */
.cart-sidebar {
  position: sticky;
  top: 90px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.summary-card {
  background: white;
  border-radius: var(--border-radius-md);
  border: 1px solid var(--border-color);
  padding: 24px;
  box-shadow: var(--shadow-md);

  .summary-title {
    font-size: 18px;
    font-weight: 700;
    color: var(--text-primary);
    margin-bottom: 20px;
  }
}

.summary-rows {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid var(--border-color);

  .summary-row {
    display: flex;
    justify-content: space-between;
    font-size: 14px;
    color: var(--text-secondary);

    .free-shipping {
      color: var(--color-success);
      font-weight: 600;
    }
  }
}

.summary-total {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 20px;
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);

  .total-price {
    display: flex;
    align-items: baseline;

    .total-symbol {
      font-size: 14px;
      color: var(--color-accent);
      font-weight: 700;
      margin-right: 2px;
    }

    .total-amount {
      font-size: 28px;
      color: var(--color-accent);
      font-weight: 800;
    }
  }
}

.checkout-btn {
  width: 100%;
  height: 52px;
  background: var(--gradient-primary);
  color: white;
  border: none;
  border-radius: var(--border-radius-md);
  font-size: 16px;
  font-weight: 700;
  cursor: pointer;
  font-family: inherit;
  box-shadow: var(--shadow-primary);
  transition: all var(--transition-fast);
  margin-bottom: 12px;

  &:hover:not(:disabled) {
    transform: translateY(-2px);
    box-shadow: 0 12px 30px rgba(99, 102, 241, 0.4);
  }

  &:disabled {
    background: var(--bg-secondary);
    color: var(--text-muted);
    box-shadow: none;
    cursor: not-allowed;
  }
}

.clear-btn {
  width: 100%;
  height: 40px;
  background: transparent;
  color: var(--text-muted);
  border: 1px solid var(--border-color);
  border-radius: var(--border-radius-sm);
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  font-family: inherit;
  transition: all var(--transition-fast);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;

  &:hover {
    color: var(--color-accent);
    border-color: var(--color-accent);
    background: rgba(244, 63, 94, 0.04);
  }
}

.guarantee-card {
  background: white;
  border-radius: var(--border-radius-md);
  border: 1px solid var(--border-color);
  padding: 16px 20px;

  .g-title {
    font-size: 13px;
    font-weight: 700;
    color: var(--text-secondary);
    margin-bottom: 12px;
  }

  .g-item {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 13px;
    color: var(--text-secondary);
    padding: 6px 0;

    .g-icon {
      font-size: 16px;
    }
  }
}

/* ========================
   Empty Cart
   ======================== */
.empty-cart {
  text-align: center;
  padding: 100px 0;
  background: white;
  border-radius: var(--border-radius-lg);
  border: 1px solid var(--border-color);
  box-shadow: var(--shadow-sm);

  .empty-icon {
    font-size: 80px;
    margin-bottom: 20px;
    opacity: 0.5;
  }

  h3 {
    font-size: 22px;
    font-weight: 700;
    color: var(--text-primary);
    margin-bottom: 8px;
  }

  p {
    font-size: 15px;
    color: var(--text-muted);
    margin-bottom: 32px;
  }

  .go-shop-btn {
    background: var(--gradient-primary);
    color: white;
    border: none;
    border-radius: var(--border-radius-full);
    padding: 14px 40px;
    font-size: 16px;
    font-weight: 700;
    cursor: pointer;
    font-family: inherit;
    box-shadow: var(--shadow-primary);
    transition: all var(--transition-fast);

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 12px 30px rgba(99, 102, 241, 0.4);
    }
  }
}
</style>
