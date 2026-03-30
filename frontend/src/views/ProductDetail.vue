<template>
  <div class="product-detail-page" v-loading="loading">
    <div class="container" v-if="product">
      <!-- 面包屑 -->
      <el-breadcrumb class="breadcrumb" separator="/">
        <el-breadcrumb-item :to="{ path: '/home' }">{{ $t('home.title') }}</el-breadcrumb-item>
        <el-breadcrumb-item v-if="categoryName">{{ categoryName }}</el-breadcrumb-item>
        <el-breadcrumb-item>{{ product.name }}</el-breadcrumb-item>
      </el-breadcrumb>

      <div class="detail-grid">
        <!-- 左侧图片区 -->
        <div class="image-section">
          <div class="main-image-area">
            <el-image
              :src="selectedImage || product.images?.split(',')[0] || ''"
              :alt="product.name"
              :preview-src-list="product.images?.split(',') || []"
              fit="contain"
              class="main-image"
            >
              <template #error>
                <div class="image-fallback">
                  <el-icon :size="60" color="#ddd"><Picture /></el-icon>
                  <p>{{ $t('product.noImage') }}</p>
                </div>
              </template>
            </el-image>
            <div class="image-badge-area">
              <span class="hot-badge" v-if="product.stock > 50">🔥 {{ $t('product.hotSale') }}</span>
              <span class="new-badge" v-if="isNew">🆕 {{ $t('product.newRelease') }}</span>
            </div>
          </div>
          <div class="thumb-list" v-if="product.images && product.images.split(',').length > 1">
            <div
              v-for="(img, index) in product.images.split(',')"
              :key="index"
              class="thumb"
              :class="{ active: selectedImage === img || (!selectedImage && index === 0) }"
              @click="selectedImage = img"
            >
              <img :src="img" />
            </div>
          </div>
        </div>

        <!-- 右侧信息区 -->
        <div class="info-section">
          <!-- 标题 -->
          <h1 class="product-title">{{ product.name }}</h1>

          <!-- 价格区 -->
          <div class="price-section">
            <div class="price-row">
              <span class="price-label">{{ $t('product.price') }}</span>
              <div class="price-display">
                <span class="price-symbol">¥</span>
                <span class="price-value">{{ Math.floor(product.price) }}</span>
                <span class="price-decimal">.{{ (product.price % 1).toFixed(2).slice(2) }}</span>
              </div>
            </div>
          </div>

          <!-- 商品属性 -->
          <div class="meta-section">
            <div class="meta-row">
              <span class="meta-key">{{ $t('product.category') }}</span>
              <span class="meta-value">
                <el-tag size="small" type="info">{{ categoryName || $t('product.uncategorized') }}</el-tag>
              </span>
            </div>
            <div class="meta-row">
              <span class="meta-key">{{ $t('product.stock') }}</span>
              <span class="meta-value stock" :class="{ warning: product.stock > 0 && product.stock <= 10, danger: product.stock <= 0 }">
                <span v-if="product.stock > 0">
                  <span class="stock-num">{{ product.stock }}</span> {{ $t('product.availableCount') }}
                </span>
                <span v-else>❌ {{ $t('product.outOfStock') }}</span>
              </span>
            </div>
            <div class="meta-row" v-if="product.stock > 0 && product.stock <= 50">
              <span class="meta-key">{{ $t('product.urgency') }}</span>
              <div class="stock-progress-bar">
                <div
                  class="stock-progress-fill"
                  :style="{ width: Math.max(10, (product.stock / 50) * 100) + '%' }"
                  :class="{ 'low-stock': product.stock <= 10 }"
                ></div>
              </div>
            </div>
          </div>

          <el-divider />

          <!-- 商品描述 -->
          <div class="desc-section" v-if="product.description">
            <div class="desc-label">{{ $t('product.description') }}</div>
            <p class="desc-text">{{ product.description }}</p>
          </div>

          <!-- 数量选择 -->
          <div class="quantity-section">
            <span class="quantity-label">{{ $t('product.purchaseQuantity') }}</span>
            <div class="quantity-controls">
              <button class="qty-btn" @click="decreaseQty" :disabled="quantity <= 1">−</button>
              <span class="qty-value">{{ quantity }}</span>
              <button class="qty-btn" @click="increaseQty" :disabled="quantity >= product.stock">+</button>
            </div>
            <span class="qty-hint">{{ $t('product.maxPurchase', { count: product.stock }) }}</span>
          </div>

          <!-- 操作按钮 -->
          <div class="action-section">
            <button
              class="btn-add-cart"
              :disabled="product.stock <= 0"
              :class="{ loading: addToCartLoading }"
              @click="addToCart"
            >
              <el-icon><ShoppingCart /></el-icon>
              <span>{{ addToCartLoading ? $t('product.adding') : $t('product.addToCart') }}</span>
            </button>
            <button
              class="btn-buy-now"
              :disabled="product.stock <= 0"
              @click="buyNow"
            >
              {{ $t('product.buyNow') }}
            </button>
          </div>

          <!-- 保障信息 -->
          <div class="guarantee-section">
            <div class="guarantee-item" v-for="g in guarantees" :key="g.label">
              <span class="g-icon">{{ g.icon }}</span>
              <span>{{ g.label }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div class="container" v-if="!product && !loading" style="padding: 80px 0; text-align: center;">
      <el-empty :description="$t('product.notFound')">
        <el-button type="primary" @click="$router.push('/home')">{{ $t('common.backToHome') }}</el-button>
      </el-empty>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { ShoppingCart, Picture } from '@element-plus/icons-vue'
import { getProductDetail, getCategoryTree } from '@/api/product'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'
import type { Product, Category } from '@/types'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()
const { t } = useI18n()

const loading = ref(false)
const addToCartLoading = ref(false)
const product = ref<Product | null>(null)
const categories = ref<Category[]>([])
const quantity = ref(1)
const selectedImage = ref<string | null>(null)

const isNew = computed(() => {
  if (!product.value) return false
  const created = new Date(product.value.createdAt)
  const diff = (Date.now() - created.getTime()) / (1000 * 60 * 60 * 24)
  return diff < 30
})

const guarantees = computed(() => [
  { icon: '🛡️', label: t('product.guarantee.authentic') },
  { icon: '🔄', label: t('product.guarantee.return') },
  { icon: '🚀', label: t('product.guarantee.shipping') },
  { icon: '💳', label: t('product.guarantee.secure') }
])

const categoryName = computed(() => {
  if (!product.value) return ''
  const findCategory = (list: Category[], id: number): string => {
    for (const cat of list) {
      if (cat.id === id) return cat.name
      if (cat.children && cat.children.length > 0) {
        const found = findCategory(cat.children, id)
        if (found) return found
      }
    }
    return ''
  }
  return findCategory(categories.value, product.value.categoryId)
})

const decreaseQty = () => {
  if (quantity.value > 1) quantity.value--
}

const increaseQty = () => {
  if (product.value && quantity.value < product.value.stock) quantity.value++
}

const fetchProduct = async () => {
  const id = Number(route.params.id)
  if (!id) {
    router.push('/home')
    return
  }
  loading.value = true
  try {
    product.value = await getProductDetail(id)
    quantity.value = 1
    selectedImage.value = product.value.images?.split(',')[0] || null
  } catch (error) {
    console.error('获取商品详情失败', error)
    router.push('/home')
  } finally {
    loading.value = false
  }
}

const fetchCategories = async () => {
  try {
    categories.value = await getCategoryTree()
  } catch (error) {
    console.error('获取分类失败', error)
  }
}

const addToCart = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning(t('common.loginFirst'))
    router.push('/login')
    return
  }
  if (!product.value) return
  addToCartLoading.value = true
  try {
    const success = await cartStore.addItem(
      product.value.id,
      product.value.name,
      product.value.price,
      quantity.value,
      product.value.images?.split(',')[0]
    )
    if (success) {
      ElMessage.success(t('product.addToCartSuccess'))
    }
  } finally {
    addToCartLoading.value = false
  }
}

const buyNow = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning(t('common.loginFirst'))
    router.push('/login')
    return
  }
  if (!product.value) return

  // addItem 内部已经调用 fetchCart()，完成后 cartInfo 是最新的
  const success = await cartStore.addItem(
    product.value.id,
    product.value.name,
    product.value.price,
    quantity.value,
    product.value.images?.split(',')[0]
  )
  if (!success) return

  // 确保该商品是选中状态（addItem 时 selected=true，一般不需要再改，以防万一）
  const cartItem = cartStore.cartInfo?.items.find(item => item.productId === product.value?.id)
  if (cartItem && !cartItem.selected) {
    await cartStore.toggleSelect(cartItem.productId, true)
  }

  router.push('/checkout')
}

onMounted(() => {
  fetchCategories()
  fetchProduct()
})
</script>

<style scoped lang="scss">
.product-detail-page {
  padding: 24px 0 48px;
  min-height: 70vh;
}

.breadcrumb {
  margin-bottom: 24px;
}

.detail-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 48px;
  align-items: flex-start;

  @media (max-width: 768px) {
    grid-template-columns: 1fr;
  }
}

/* ========================
   Image Section
   ======================== */
.image-section {
  position: sticky;
  top: 100px;
}

.main-image-area {
  background: white;
  border-radius: var(--border-radius-lg);
  border: 1px solid var(--border-color);
  overflow: hidden;
  position: relative;
  height: 440px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;
  box-shadow: var(--shadow-md);

  .main-image {
    width: 100%;
    height: 100%;
  }

  .image-fallback {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 12px;

    p {
      color: var(--text-muted);
      font-size: 14px;
    }
  }
}

.image-badge-area {
  position: absolute;
  top: 16px;
  left: 16px;
  display: flex;
  gap: 8px;

  span {
    padding: 4px 12px;
    border-radius: var(--border-radius-full);
    font-size: 12px;
    font-weight: 700;
  }

  .hot-badge {
    background: rgba(244, 63, 94, 0.9);
    color: white;
    backdrop-filter: blur(10px);
  }

  .new-badge {
    background: rgba(99, 102, 241, 0.9);
    color: white;
    backdrop-filter: blur(10px);
  }
}

.thumb-list {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.thumb {
  width: 72px;
  height: 72px;
  border-radius: var(--border-radius-sm);
  overflow: hidden;
  cursor: pointer;
  border: 2px solid transparent;
  transition: all var(--transition-fast);
  box-shadow: var(--shadow-sm);

  &.active {
    border-color: var(--color-primary);
    box-shadow: var(--shadow-primary);
  }

  &:hover {
    transform: scale(1.05);
  }

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

/* ========================
   Info Section
   ======================== */
.info-section {
  background: white;
  border-radius: var(--border-radius-lg);
  border: 1px solid var(--border-color);
  padding: 32px;
  box-shadow: var(--shadow-md);
}

.product-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1.35;
  margin-bottom: 20px;
}

.price-section {
  background: linear-gradient(135deg, #fff5f7 0%, #ffeef2 100%);
  border-radius: var(--border-radius-md);
  padding: 20px 24px;
  margin-bottom: 24px;
  border: 1px solid rgba(244, 63, 94, 0.1);
}

.price-row {
  display: flex;
  align-items: baseline;
  gap: 16px;

  .price-label {
    font-size: 14px;
    color: var(--text-muted);
  }

  .price-display {
    display: flex;
    align-items: baseline;

    .price-symbol {
      font-size: 18px;
      color: var(--color-accent);
      font-weight: 700;
      margin-right: 2px;
    }

    .price-value {
      font-size: 44px;
      color: var(--color-accent);
      font-weight: 800;
      line-height: 1;
      letter-spacing: -1px;
    }

    .price-decimal {
      font-size: 22px;
      color: var(--color-accent);
      font-weight: 700;
    }
  }
}

.meta-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 24px;
}

.meta-row {
  display: flex;
  align-items: center;
  gap: 20px;
  font-size: 14px;

  .meta-key {
    width: 56px;
    color: var(--text-muted);
    flex-shrink: 0;
  }

  .meta-value {
    color: var(--text-primary);
    font-weight: 500;

    &.warning { color: var(--color-warning); }
    &.danger { color: var(--color-danger); }

    .stock-num {
      font-size: 18px;
      font-weight: 700;
      color: var(--color-success);
    }
  }
}

.stock-progress-bar {
  flex: 1;
  height: 8px;
  background: var(--bg-secondary);
  border-radius: var(--border-radius-full);
  overflow: hidden;
  max-width: 200px;

  .stock-progress-fill {
    height: 100%;
    background: linear-gradient(90deg, var(--color-success), #34d399);
    border-radius: var(--border-radius-full);
    transition: width var(--transition-slow);

    &.low-stock {
      background: linear-gradient(90deg, var(--color-warning), #fbbf24);
    }
  }
}

.desc-section {
  margin-bottom: 24px;

  .desc-label {
    font-size: 14px;
    font-weight: 600;
    color: var(--text-secondary);
    margin-bottom: 10px;
  }

  .desc-text {
    font-size: 14px;
    color: var(--text-secondary);
    line-height: 1.8;
  }
}

.quantity-section {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 28px;

  .quantity-label {
    font-size: 14px;
    color: var(--text-muted);
    flex-shrink: 0;
  }

  .quantity-controls {
    display: flex;
    align-items: center;
    border: 1.5px solid var(--border-color);
    border-radius: var(--border-radius-sm);
    overflow: hidden;

    .qty-btn {
      width: 40px;
      height: 40px;
      background: var(--bg-secondary);
      border: none;
      font-size: 20px;
      cursor: pointer;
      transition: all var(--transition-fast);
      font-weight: 300;
      color: var(--text-secondary);
      font-family: inherit;

      &:hover:not(:disabled) {
        background: rgba(99, 102, 241, 0.1);
        color: var(--color-primary);
      }

      &:disabled {
        opacity: 0.4;
        cursor: not-allowed;
      }
    }

    .qty-value {
      width: 56px;
      text-align: center;
      font-size: 16px;
      font-weight: 700;
      color: var(--text-primary);
    }
  }

  .qty-hint {
    font-size: 12px;
    color: var(--text-muted);
  }
}

.action-section {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;

  button {
    flex: 1;
    height: 52px;
    border: none;
    border-radius: var(--border-radius-md);
    font-size: 16px;
    font-weight: 700;
    cursor: pointer;
    font-family: inherit;
    transition: all var(--transition-fast);
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;

    &:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }
  }

  .btn-add-cart {
    background: white;
    color: var(--color-primary);
    border: 2px solid var(--color-primary);

    &:hover:not(:disabled) {
      background: var(--color-primary);
      color: white;
      transform: translateY(-2px);
      box-shadow: var(--shadow-primary);
    }
  }

  .btn-buy-now {
    background: var(--gradient-accent);
    color: white;
    box-shadow: var(--shadow-accent);

    &:hover:not(:disabled) {
      transform: translateY(-2px);
      box-shadow: 0 12px 30px rgba(244, 63, 94, 0.4);
    }
  }
}

.guarantee-section {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
  padding: 16px;
  background: var(--bg-secondary);
  border-radius: var(--border-radius-md);

  .guarantee-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 4px;
    font-size: 12px;
    color: var(--text-secondary);
    text-align: center;
    font-weight: 500;

    .g-icon {
      font-size: 20px;
    }
  }
}
</style>
