<template>
  <div class="checkout-page">
    <div class="container">
      <!-- 页面标题 -->
      <div class="page-header">
        <button class="back-btn" @click="$router.back()">
          <el-icon><ArrowLeft /></el-icon>
          {{ $t('common.back') }}
        </button>
        <h1 class="page-title">{{ $t('checkout.title') }}</h1>
      </div>

      <!-- 步骤条 -->
      <div class="steps-bar">
        <div class="step active">
          <div class="step-circle">1</div>
          <span>{{ $t('checkout.fillInfo') }}</span>
        </div>
        <div class="step-line"></div>
        <div class="step">
          <div class="step-circle">2</div>
          <span>{{ $t('checkout.confirmOrder') }}</span>
        </div>
        <div class="step-line"></div>
        <div class="step">
          <div class="step-circle">3</div>
          <span>{{ $t('checkout.pay') }}</span>
        </div>
      </div>

      <div class="checkout-layout">
        <!-- 左侧主要内容 -->
        <div class="checkout-main">
          <!-- 收货地址 -->
          <section class="checkout-section">
            <div class="section-header">
              <div class="section-icon">📍</div>
              <h2 class="section-title">{{ $t('checkout.shippingAddress') }}</h2>
              <button class="manage-addr-btn" @click="$router.push('/address')">
                {{ $t('checkout.manageAddresses') }}
              </button>
            </div>

            <!-- 已有地址：卡片选择 -->
            <div v-if="savedAddresses.length > 0" class="addr-list">
              <div
                v-for="addr in savedAddresses"
                :key="addr.id"
                class="addr-card"
                :class="{ selected: selectedAddrId === addr.id }"
                @click="selectAddress(addr)"
              >
                <div class="addr-radio">
                  <div class="radio-dot" :class="{ active: selectedAddrId === addr.id }"></div>
                </div>
                <div class="addr-body">
                  <div class="addr-top">
                    <span class="addr-name">{{ addr.receiverName }}</span>
                    <span class="addr-phone">{{ addr.phone }}</span>
                    <span v-if="addr.isDefault===1" class="addr-default-tag">{{ $t('address.default') }}</span>
                  </div>
                  <div class="addr-text">
                    {{ addr.province }}{{ addr.city }}{{ addr.district || '' }}{{ addr.detail }}
                  </div>
                </div>
              </div>

              <!-- 手动填写切换 -->
              <div
                class="addr-card manual-card"
                :class="{ selected: selectedAddrId === -1 }"
                @click="selectManual"
              >
                <div class="addr-radio">
                  <div class="radio-dot" :class="{ active: selectedAddrId === -1 }"></div>
                </div>
                <div class="addr-body">
                  <span class="manual-label">{{ $t('checkout.useNewAddress') }}</span>
                </div>
              </div>
            </div>

            <!-- 手动填写表单（无保存地址 或 选择「新地址」时显示） -->
            <div v-if="savedAddresses.length === 0 || selectedAddrId === -1" class="address-form">
              <div class="form-row">
                <div class="form-field">
                  <label class="field-label">{{ $t('address.receiverName') }} <span class="required">*</span></label>
                  <input v-model="addressForm.receiverName" class="field-input" :placeholder="$t('address.receiverPlaceholder')" />
                </div>
                <div class="form-field">
                  <label class="field-label">{{ $t('address.phone') }} <span class="required">*</span></label>
                  <input v-model="addressForm.receiverPhone" class="field-input" :placeholder="$t('address.phonePlaceholder')" maxlength="11" />
                </div>
              </div>
              <div class="form-field">
                <label class="field-label">{{ $t('address.detail') }} <span class="required">*</span></label>
                <textarea v-model="addressForm.detailAddress" class="field-textarea" :placeholder="$t('address.detailPlaceholder')" rows="3"></textarea>
              </div>
              <div class="form-field">
                <label class="field-label">{{ $t('address.remark') }}</label>
                <input v-model="addressForm.remark" class="field-input" :placeholder="$t('address.remarkPlaceholder')" />
              </div>
            </div>

            <!-- 选中已有地址时显示备注 -->
            <div v-else class="remark-row">
              <label class="field-label">{{ $t('address.remark') }}</label>
              <input v-model="addressForm.remark" class="field-input" :placeholder="$t('address.remarkPlaceholder')" />
            </div>
          </section>

          <!-- 商品清单 -->
          <section class="checkout-section">
            <div class="section-header">
              <div class="section-icon">🛍️</div>
              <h2 class="section-title">{{ $t('checkout.productList') }}</h2>
              <span class="section-badge">{{ $t('cart.itemsCount', { count: selectedItems.length }) }}</span>
            </div>
            <div class="item-list">
              <div v-for="item in selectedItems" :key="item.productId" class="checkout-item">
                <div class="ci-image">
                  <img :src="item.image || ''" :alt="item.productName" @error="onImgError" />
                </div>
                <div class="ci-info">
                  <div class="ci-name">{{ item.productName }}</div>
                  <div class="ci-qty">× {{ item.quantity }}</div>
                </div>
                <div class="ci-price">¥{{ (item.price * item.quantity).toFixed(2) }}</div>
              </div>
            </div>
          </section>
        </div>

        <!-- 右侧订单摘要 -->
        <div class="checkout-sidebar">
          <div class="summary-card">
            <h3 class="summary-title">{{ $t('checkout.orderSummary') }}</h3>

            <div class="summary-rows">
              <div class="sum-row">
                <span>{{ $t('checkout.itemsCount') }}</span>
                <span>{{ $t('cart.itemsCount', { count: totalCount }) }}</span>
              </div>
              <div class="sum-row">
                <span>{{ $t('checkout.itemsAmount') }}</span>
                <span>¥{{ totalAmount.toFixed(2) }}</span>
              </div>
              <div class="sum-row">
                <span>{{ $t('checkout.shippingFee') }}</span>
                <span class="shipping-free">{{ $t('checkout.freeShipping') }}</span>
              </div>
              <div class="sum-row" v-if="addressForm.receiverName">
                <span>{{ $t('checkout.receiver') }}</span>
                <span>{{ addressForm.receiverName }}</span>
              </div>
            </div>

            <div class="total-block">
              <span class="total-label">{{ $t('checkout.totalPayable') }}</span>
              <div class="total-value">
                <span class="ts">¥</span>
                <span class="tv">{{ totalAmount.toFixed(2) }}</span>
              </div>
            </div>

            <button
              class="submit-btn"
              :disabled="!canSubmit || loading"
              @click="handleSubmit"
              :class="{ loading: loading }"
            >
              <span v-if="!loading">{{ $t('checkout.submitOrder') }}</span>
              <span v-else>{{ $t('checkout.submitting') }}</span>
            </button>

            <div class="security-note">
              <el-icon><Lock /></el-icon>
              {{ $t('checkout.securityNote') }}
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Lock } from '@element-plus/icons-vue'
import { useCartStore } from '@/stores/cart'
import { getSelectedItems, createOrder } from '@/api/order'
import { getAddressList } from '@/api/address'
import type { CartItem, UserAddress } from '@/types'

const router = useRouter()
const cartStore = useCartStore()
const { t } = useI18n()

const loading = ref(false)
const selectedItems = ref<CartItem[]>([])
const savedAddresses = ref<UserAddress[]>([])
const selectedAddrId = ref<number>(0)   // 0=未选, -1=手动输入, >0=选中的地址ID

const addressForm = reactive({
  receiverName: '',
  receiverPhone: '',
  detailAddress: '',
  remark: ''
})

// 选中某个已保存地址
const selectAddress = (addr: UserAddress) => {
  selectedAddrId.value = addr.id
}

// 切换到手动输入模式
const selectManual = () => {
  selectedAddrId.value = -1
  addressForm.receiverName = ''
  addressForm.receiverPhone = ''
  addressForm.detailAddress = ''
}

// 加载已保存地址，自动选中默认地址
const loadAddresses = async () => {
  try {
    savedAddresses.value = await getAddressList()
    if (savedAddresses.value.length > 0) {
      const def = savedAddresses.value.find(a => a.isDefault === 1) || savedAddresses.value[0]
      selectedAddrId.value = def.id
    } else {
      selectedAddrId.value = -1  // 无地址则直接手动填写
    }
  } catch {
    selectedAddrId.value = -1
  }
}

const totalCount = computed(() => {
  return selectedItems.value.reduce((sum, item) => sum + item.quantity, 0)
})

const totalAmount = computed(() => {
  return selectedItems.value.reduce((sum, item) => sum + item.price * item.quantity, 0)
})

const canSubmit = computed(() => {
  if (selectedItems.value.length === 0) return false
  if (selectedAddrId.value > 0) return true  // 已选保存地址
  // 手动输入模式
  return addressForm.receiverName.trim() &&
    addressForm.receiverPhone.trim() &&
    addressForm.detailAddress.trim()
})

const fetchSelectedItems = async () => {
  try {
    selectedItems.value = await getSelectedItems()
    if (selectedItems.value.length === 0) {
      ElMessage.warning(t('checkout.pleaseSelectItems'))
      router.push('/cart')
    }
  } catch (error) {
    console.error('获取选中商品失败', error)
    router.push('/cart')
  }
}

const handleSubmit = async () => {
  if (!canSubmit.value) {
    ElMessage.warning(t('checkout.fillCompleteInfo'))
    return
  }
  loading.value = true
  try {
    let address: string
    if (selectedAddrId.value > 0) {
      // 使用已保存的地址
      const addr = savedAddresses.value.find(a => a.id === selectedAddrId.value)!
      address = JSON.stringify({
        receiverName: addr.receiverName,
        receiverPhone: addr.phone,
        detailAddress: `${addr.province}${addr.city}${addr.district || ''}${addr.detail}`
      })
    } else {
      // 使用手动填写的地址
      address = JSON.stringify({
        receiverName: addressForm.receiverName,
        receiverPhone: addressForm.receiverPhone,
        detailAddress: addressForm.detailAddress
      })
    }
    const productIds = selectedItems.value.map(item => item.productId)
    await createOrder({
      address,
      remark: addressForm.remark || undefined,
      productIds
    })
    ElMessage.success(t('checkout.createSuccess'))
    await cartStore.fetchCart()
    router.push('/order')
  } catch (error: any) {
    console.error('创建订单失败', error)
  } finally {
    loading.value = false
  }
}

const onImgError = (e: Event) => {
  const img = e.target as HTMLImageElement
  img.src = `https://picsum.photos/seed/${Math.random()}/100/100`
}

onMounted(() => {
  fetchSelectedItems()
  loadAddresses()
})
</script>

<style scoped lang="scss">
.checkout-page {
  padding: 32px 0 48px;
  min-height: 70vh;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 28px;

  .back-btn {
    display: flex;
    align-items: center;
    gap: 4px;
    background: none;
    border: 1px solid var(--border-color);
    border-radius: var(--border-radius-full);
    padding: 8px 16px;
    font-size: 14px;
    color: var(--text-secondary);
    cursor: pointer;
    font-family: inherit;
    transition: all var(--transition-fast);

    &:hover {
      border-color: var(--color-primary);
      color: var(--color-primary);
    }
  }

  .page-title {
    font-size: 28px;
    font-weight: 800;
    color: var(--text-primary);
  }
}

/* ========================
   Steps Bar
   ======================== */
.steps-bar {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0;
  margin-bottom: 36px;
  background: white;
  border-radius: var(--border-radius-md);
  border: 1px solid var(--border-color);
  padding: 20px 40px;
  box-shadow: var(--shadow-sm);
}

.step {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;

  .step-circle {
    width: 36px;
    height: 36px;
    border-radius: 50%;
    border: 2px solid var(--border-color);
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 14px;
    font-weight: 700;
    color: var(--text-muted);
    background: white;
    transition: all var(--transition-normal);
  }

  span:last-child {
    font-size: 13px;
    color: var(--text-muted);
    font-weight: 500;
  }

  &.active {
    .step-circle {
      background: var(--gradient-primary);
      border-color: transparent;
      color: white;
      box-shadow: var(--shadow-primary);
    }

    span:last-child {
      color: var(--color-primary);
      font-weight: 700;
    }
  }
}

.step-line {
  flex: 1;
  height: 2px;
  background: var(--border-color);
  margin: 0 20px;
  min-width: 60px;
  position: relative;
  top: -12px;
}

/* ========================
   Layout
   ======================== */
.checkout-layout {
  display: grid;
  grid-template-columns: 1fr 340px;
  gap: 24px;
  align-items: flex-start;

  @media (max-width: 1024px) {
    grid-template-columns: 1fr;
  }
}

.checkout-main {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* ========================
   Section
   ======================== */
.checkout-section {
  background: white;
  border-radius: var(--border-radius-md);
  border: 1px solid var(--border-color);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
}

.section-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 20px 24px;
  border-bottom: 1px solid var(--border-color);
  background: linear-gradient(135deg, #f8f9ff 0%, #f0f1ff 100%);

  .section-icon {
    font-size: 22px;
  }

  .section-title {
    font-size: 17px;
    font-weight: 700;
    color: var(--text-primary);
    flex: 1;
  }

  .section-badge {
    background: rgba(99, 102, 241, 0.1);
    color: var(--color-primary);
    font-size: 13px;
    font-weight: 700;
    padding: 3px 12px;
    border-radius: var(--border-radius-full);
  }
}

/* ========================
   Address Form
   ======================== */
.address-form {
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;

  @media (max-width: 600px) {
    grid-template-columns: 1fr;
  }
}

.form-field {
  display: flex;
  flex-direction: column;
  gap: 8px;

  .field-label {
    font-size: 13px;
    font-weight: 600;
    color: var(--text-secondary);

    .required {
      color: var(--color-accent);
    }
  }

  .field-input,
  .field-textarea {
    border: 1.5px solid var(--border-color);
    border-radius: var(--border-radius-sm);
    padding: 12px 16px;
    font-size: 14px;
    color: var(--text-primary);
    font-family: inherit;
    outline: none;
    transition: all var(--transition-fast);
    background: white;

    &::placeholder {
      color: var(--text-muted);
    }

    &:focus {
      border-color: var(--color-primary);
      box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.1);
    }
  }

  .field-textarea {
    resize: vertical;
    min-height: 80px;
  }
}

/* ========================
   Manage Address Button
   ======================== */
.manage-addr-btn {
  margin-left: auto;
  padding: 5px 14px;
  font-size: 13px;
  font-weight: 500;
  color: var(--color-primary);
  background: rgba(99, 102, 241, 0.08);
  border: 1px solid rgba(99, 102, 241, 0.25);
  border-radius: 20px;
  cursor: pointer;
  font-family: inherit;
  transition: all 0.2s;

  &:hover {
    background: var(--color-primary);
    color: white;
  }
}

/* ========================
   Saved Address Card List
   ======================== */
.addr-list {
  padding: 16px 24px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.addr-card {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  padding: 14px 16px;
  border: 2px solid var(--border-color);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
  background: #f8fafc;

  &:hover {
    border-color: rgba(99, 102, 241, 0.4);
    background: rgba(99, 102, 241, 0.03);
  }

  &.selected {
    border-color: var(--color-primary);
    background: rgba(99, 102, 241, 0.05);
    box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.1);
  }

  &.manual-card {
    border-style: dashed;
    .manual-label {
      font-size: 14px;
      color: var(--color-primary);
      font-weight: 500;
      line-height: 22px;
    }
  }
}

.addr-radio {
  flex-shrink: 0;
  padding-top: 2px;
}

.radio-dot {
  width: 18px;
  height: 18px;
  border-radius: 50%;
  border: 2px solid var(--border-color);
  background: white;
  transition: all 0.2s;
  position: relative;

  &.active {
    border-color: var(--color-primary);

    &::after {
      content: '';
      position: absolute;
      inset: 3px;
      border-radius: 50%;
      background: var(--color-primary);
    }
  }
}

.addr-body {
  flex: 1;
  min-width: 0;
}

.addr-top {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 6px;
  flex-wrap: wrap;
}

.addr-name {
  font-size: 15px;
  font-weight: 700;
  color: var(--text-primary);
}

.addr-phone {
  font-size: 13px;
  color: var(--text-secondary);
}

.addr-default-tag {
  padding: 2px 8px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: white;
  border-radius: 20px;
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.3px;
}

.addr-text {
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.6;
}

/* 选中已有地址时的备注区 */
.remark-row {
  padding: 0 24px 20px;
  display: flex;
  flex-direction: column;
  gap: 8px;

  .field-label {
    font-size: 13px;
    font-weight: 600;
    color: var(--text-secondary);
  }

  .field-input {
    border: 1.5px solid var(--border-color);
    border-radius: 8px;
    padding: 12px 16px;
    font-size: 14px;
    color: var(--text-primary);
    font-family: inherit;
    outline: none;
    transition: all 0.15s;
    background: white;

    &::placeholder { color: var(--text-muted); }
    &:focus {
      border-color: var(--color-primary);
      box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.1);
    }
  }
}

/* ========================
   Item List
   ======================== */
.item-list {
  padding: 8px 0;
}

.checkout-item {
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
    background: var(--bg-secondary);
  }

  .ci-image {
    width: 72px;
    height: 72px;
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
  }

  .ci-info {
    flex: 1;
    min-width: 0;

    .ci-name {
      font-size: 14px;
      font-weight: 600;
      color: var(--text-primary);
      margin-bottom: 6px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .ci-qty {
      font-size: 13px;
      color: var(--text-muted);
    }
  }

  .ci-price {
    font-size: 16px;
    font-weight: 700;
    color: var(--color-accent);
    flex-shrink: 0;
  }
}

/* ========================
   Sidebar
   ======================== */
.checkout-sidebar {
  position: sticky;
  top: 90px;
}

.summary-card {
  background: white;
  border-radius: var(--border-radius-md);
  border: 1px solid var(--border-color);
  box-shadow: var(--shadow-md);
  overflow: hidden;

  .summary-title {
    font-size: 18px;
    font-weight: 700;
    color: var(--text-primary);
    padding: 20px 24px;
    border-bottom: 1px solid var(--border-color);
    background: linear-gradient(135deg, #f8f9ff 0%, #f0f1ff 100%);
  }
}

.summary-rows {
  padding: 16px 24px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  border-bottom: 1px solid var(--border-color);

  .sum-row {
    display: flex;
    justify-content: space-between;
    font-size: 14px;
    color: var(--text-secondary);

    .shipping-free {
      color: var(--color-success);
      font-weight: 600;
    }
  }
}

.total-block {
  padding: 20px 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid var(--border-color);

  .total-label {
    font-size: 15px;
    font-weight: 600;
    color: var(--text-primary);
  }

  .total-value {
    display: flex;
    align-items: baseline;

    .ts {
      font-size: 14px;
      color: var(--color-accent);
      font-weight: 700;
      margin-right: 2px;
    }

    .tv {
      font-size: 30px;
      color: var(--color-accent);
      font-weight: 800;
    }
  }
}

.submit-btn {
  display: block;
  width: calc(100% - 48px);
  margin: 20px 24px 12px;
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

  &:hover:not(:disabled) {
    transform: translateY(-2px);
    box-shadow: 0 12px 30px rgba(99, 102, 241, 0.4);
  }

  &:disabled {
    background: var(--bg-secondary);
    color: var(--text-muted);
    box-shadow: none;
    cursor: not-allowed;
    transform: none;
  }
}

.security-note {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  font-size: 12px;
  color: var(--text-muted);
  padding: 0 24px 20px;
}
</style>
