<template>
  <div class="home">
    <!-- Hero Banner -->
    <section class="hero-section">
      <div class="hero-bg">
        <div class="hero-orb orb-1"></div>
        <div class="hero-orb orb-2"></div>
        <div class="hero-orb orb-3"></div>
      </div>
      <div class="container hero-content">
        <div class="hero-text animate-fade-in">
          <div class="hero-badge">{{ $t('home.badge') }}</div>
          <h1 class="hero-title">
            {{ $t('home.titleMain') }}
            <span class="gradient-text">{{ $t('home.titleHighlight') }}</span>
          </h1>
          <p class="hero-subtitle">{{ $t('home.subtitle') }}</p>
          <div class="hero-search">
            <el-icon class="search-icon"><Search /></el-icon>
            <input
              v-model="keyword"
              class="hero-search-input"
              :placeholder="$t('home.searchPlaceholder')"
              @keyup.enter="fetchProducts"
            />
            <button class="hero-search-btn" @click="fetchProducts">
              <el-icon><Search /></el-icon>
              {{ $t('common.search') }}
            </button>
          </div>
          <div class="hero-stats">
            <div class="stat-item">
              <span class="stat-num">{{ total > 0 ? total + '+' : '—' }}</span>
              <span class="stat-label">{{ $t('home.selectedProducts') }}</span>
            </div>
            <div class="stat-divider"></div>
            <div class="stat-item">
              <span class="stat-num">99.9%</span>
              <span class="stat-label">{{ $t('home.positiveRate') }}</span>
            </div>
            <div class="stat-divider"></div>
            <div class="stat-item">
              <span class="stat-num">24h</span>
              <span class="stat-label">{{ $t('home.fastDelivery') }}</span>
            </div>
          </div>
        </div>
        <div class="hero-visual animate-float">
          <div v-for="(hp, idx) in heroProducts" :key="hp.id" :class="`hero-card card-${idx+1}`" @click="goToDetail(hp.id)" style="cursor:pointer">
            <div class="mini-card-icon">{{ getCategoryEmoji(hp.name) }}</div>
            <div>
              <div class="mini-card-name">{{ hp.name.length > 8 ? hp.name.slice(0,8)+'…' : hp.name }}</div>
              <div class="mini-card-price">¥{{ hp.price.toLocaleString() }}</div>
            </div>
          </div>
          <div class="hero-circle">
            <el-icon :size="60" color="rgba(255,255,255,0.6)"><ShoppingBag /></el-icon>
          </div>
        </div>
      </div>
    </section>

    <!-- 主内容区 -->
    <div class="main-section">
      <div class="container main-wrapper">
        <!-- 左侧分类 -->
        <aside class="sidebar">
          <div class="sidebar-card">
            <div class="sidebar-title">
              <el-icon><Menu /></el-icon>
              {{ $t('home.allCategories') }}
            </div>
            <div class="category-list">
              <div
                class="category-item"
                :class="{ active: selectedCategoryId === null }"
                @click="selectCategory(null)"
              >
                <span class="cat-icon">🏪</span>
                <span>{{ $t('home.allProducts') }}</span>
                <el-icon class="arrow"><ArrowRight /></el-icon>
              </div>
              <div
                v-for="cat in flatCategories"
                :key="cat.id"
                class="category-item"
                :class="{ active: selectedCategoryId === cat.id, 'sub-cat': (cat.level || 0) > 0 }"
                @click="selectCategory(cat.id)"
              >
                <span class="cat-icon">{{ getCategoryEmoji(cat.name) }}</span>
                <span>{{ cat.name }}</span>
                <el-icon class="arrow"><ArrowRight /></el-icon>
              </div>
            </div>
          </div>
        </aside>

        <!-- 右侧商品 -->
        <div class="content-area">
          <!-- 筛选栏 -->
          <div class="filter-bar">
            <div class="filter-left">
              <span class="result-count">
                {{ $t('home.totalProducts1') }} <strong>{{ total }}</strong> {{ $t('home.totalProducts2') }}
                <span v-if="total > 0" class="loaded-count">{{ $t('home.loadedCount', { count: products.length }) }}</span>
              </span>
            </div>
            <div class="filter-right">
              <el-input
                v-model="keyword"
                :placeholder="$t('home.filterPlaceholder')"
                clearable
                @clear="resetAndFetch"
                @keyup.enter="resetAndFetch"
                size="small"
                style="width: 200px;"
              >
                <template #prefix>
                  <el-icon><Search /></el-icon>
                </template>
              </el-input>
            </div>
          </div>

          <!-- 商品网格 -->
          <div class="product-grid">
            <div
              v-for="product in products"
              :key="product.id"
              class="product-card"
              @click="goToDetail(product.id)"
            >
              <div class="product-image">
                <img
                  :src="product.images?.split(',')[0] || ''"
                  :alt="product.name"
                  @error="onImageError"
                />
                <div class="product-overlay">
                  <button class="quick-view-btn" @click.stop="goToDetail(product.id)">
                    <el-icon><View /></el-icon>
                    {{ $t('home.quickView') }}
                  </button>
                </div>
                <div class="stock-badge" v-if="product.stock > 0 && product.stock <= 10">
                  {{ $t('home.onlyLeft', { count: product.stock }) }}
                </div>
                <div class="sold-out-badge" v-if="product.stock <= 0">
                  {{ $t('home.soldOut') }}
                </div>
              </div>
              <div class="product-body">
                <h3 class="product-name">{{ product.name }}</h3>
                <p class="product-desc">{{ product.description || $t('home.noDescription') }}</p>
                <div class="product-footer">
                  <div class="product-price">
                    <span class="price-symbol">¥</span>
                    <span class="price-value">{{ product.price.toFixed(2) }}</span>
                  </div>
                  <button
                    class="add-cart-btn"
                    :disabled="product.stock <= 0"
                    @click.stop="addToCart(product)"
                  >
                    <el-icon><ShoppingCart /></el-icon>
                  </button>
                </div>
              </div>
            </div>

            <!-- 首次加载骨架屏 -->
            <template v-if="loading && products.length === 0">
              <div v-for="i in 8" :key="'sk-'+i" class="product-card-skeleton">
                <div class="skeleton" style="height: 200px; margin-bottom: 12px;"></div>
                <div class="skeleton" style="height: 18px; margin-bottom: 8px;"></div>
                <div class="skeleton" style="height: 14px; width: 60%;"></div>
              </div>
            </template>
          </div>

          <el-empty
            v-if="products.length === 0 && !loading"
            :description="$t('home.emptyTitle')"
            :image-size="120"
          >
            <el-button type="primary" @click="selectCategory(null)">{{ $t('home.allProducts') }}</el-button>
          </el-empty>

          <!-- 加载更多 / 已到底 -->
          <div class="load-more-wrapper" v-if="products.length > 0">
            <button
              v-if="hasMore"
              class="load-more-btn"
              :class="{ loading: loadingMore }"
              @click="loadMore"
              :disabled="loadingMore"
            >
              <span v-if="!loadingMore">
                <el-icon><ArrowDown /></el-icon>
                {{ $t('home.loadMore') }}
              </span>
              <span v-else class="loading-dots">{{ $t('common.loading') }}<i></i><i></i><i></i></span>
            </button>
            <div v-else class="no-more">
              <span>{{ $t('home.noMore', { count: total }) }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search, ShoppingCart, ShoppingBag, Menu, ArrowRight, ArrowDown, View } from '@element-plus/icons-vue'
import { getProductList, getCategoryTree } from '@/api/product'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'
import type { Product, Category } from '@/types'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const cartStore = useCartStore()

const products = ref<Product[]>([])
const heroProducts = ref<Product[]>([])
const categories = ref<Category[]>([])
const selectedCategoryId = ref<number | null>(null)
const keyword = ref('')
const current = ref(1)
const size = ref(12)
const total = ref(0)
const loading = ref(false)      // 首次/重置加载
const loadingMore = ref(false)  // 加载更多
const hasMore = computed(() => products.value.length < total.value)

// 扁平化分类树，添加层级信息
const flatCategories = computed(() => {
  const result: Array<Category & { level?: number }> = []
  const flatten = (list: Category[], level = 0) => {
    list.forEach(cat => {
      result.push({ ...cat, level })
      if (cat.children && cat.children.length > 0) {
        flatten(cat.children, level + 1)
      }
    })
  }
  flatten(categories.value)
  return result
})

const getCategoryEmoji = (name: string) => {
  const emojiMap: Record<string, string> = {
    '电子': '📱', '手机': '📱', '电脑': '💻', '数码': '🎮',
    '服装': '👕', '衣服': '👗', '鞋': '👟', '包': '👜',
    '食品': '🍎', '零食': '🍕', '饮料': '🥤',
    '家居': '🏠', '家电': '📺', '厨房': '🍳',
    '美妆': '💄', '护肤': '🧴', '香水': '🌹',
    '运动': '⚽', '健身': '🏋️', '户外': '🏕️',
    '图书': '📚', '文具': '✏️', '办公': '💼',
    '母婴': '👶', '玩具': '🧸'
  }
  for (const [key, emoji] of Object.entries(emojiMap)) {
    if (name.includes(key)) return emoji
  }
  return '🏷️'
}

// 首次/重置加载（清空列表，从第1页开始）
const fetchProducts = async () => {
  loading.value = true
  try {
    const data = await getProductList({
      current: current.value,
      size: size.value,
      categoryId: selectedCategoryId.value || undefined,
      keyword: keyword.value || undefined
    })
    products.value = data.records   // 直接替换
    total.value = data.total
  } catch (error) {
    console.error('获取商品列表失败', error)
  } finally {
    loading.value = false
  }
}

// 页码/选项改变时：重置回第1页再加载
const resetAndFetch = () => {
  current.value = 1
  fetchProducts()
}

// 加载更多（携带下一页）
const loadMore = async () => {
  if (loadingMore.value || !hasMore.value) return
  loadingMore.value = true
  try {
    const nextPage = Math.floor(products.value.length / size.value) + 1
    const data = await getProductList({
      current: nextPage,
      size: size.value,
      categoryId: selectedCategoryId.value || undefined,
      keyword: keyword.value || undefined
    })
    products.value = [...products.value, ...data.records]  // 追加
    total.value = data.total
  } catch (error) {
    console.error('加载更多失败', error)
  } finally {
    loadingMore.value = false
  }
}

const fetchCategories = async () => {
  try {
    const data = await getCategoryTree()
    categories.value = data
  } catch (error) {
    console.error('获取分类失败', error)
  }
}

// 加载 Hero 展示卡片（取价格最高的3件商品）
const fetchHeroProducts = async () => {
  try {
    const data = await getProductList({ current: 1, size: 3 })
    heroProducts.value = data.records
  } catch (error) {
    console.error('获取 hero 商品失败', error)
  }
}

const selectCategory = (id: number | null) => {
  selectedCategoryId.value = id
  current.value = 1
  fetchProducts()   // reset
}

const goToDetail = (id: number) => {
  router.push(`/product/${id}`)
}

const addToCart = async (product: Product) => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录后再添加购物车')
    router.push('/login')
    return
  }
  const success = await cartStore.addItem(
    product.id,
    product.name,
    product.price,
    1,
    product.images?.split(',')[0]
  )
  if (success) {
    ElMessage.success('✅ 已加入购物车')
  }
}

const onImageError = (e: Event) => {
  const img = e.target as HTMLImageElement
  img.src = 'http://192.168.1.4:7000/images/placeholder.png'
  img.onerror = null // 防止循环
}

// 从路由 query 中读取关键词
onMounted(() => {
  if (route.query.keyword) {
    keyword.value = route.query.keyword as string
  }
  fetchCategories()
  fetchProducts()
  fetchHeroProducts()
})

watch(() => route.query.keyword, (val) => {
  if (val) {
    keyword.value = val as string
    fetchProducts()
  }
})
</script>

<style scoped lang="scss">
/* ========================
   Hero Section
   ======================== */
.hero-section {
  position: relative;
  background: var(--gradient-hero);
  overflow: hidden;
  padding: 72px 0 60px;
}

.hero-bg {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.hero-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(60px);
  opacity: 0.3;

  &.orb-1 {
    width: 400px;
    height: 400px;
    background: radial-gradient(circle, #6366f1, transparent);
    top: -100px;
    left: -100px;
  }
  &.orb-2 {
    width: 300px;
    height: 300px;
    background: radial-gradient(circle, #8b5cf6, transparent);
    top: 50px;
    right: 200px;
  }
  &.orb-3 {
    width: 250px;
    height: 250px;
    background: radial-gradient(circle, #f43f5e, transparent);
    bottom: -50px;
    right: 100px;
  }
}

.hero-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 60px;
  position: relative;
  z-index: 1;
}

.hero-text {
  flex: 1;
  max-width: 540px;

  .hero-badge {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    background: rgba(255, 255, 255, 0.15);
    backdrop-filter: blur(10px);
    border: 1px solid rgba(255, 255, 255, 0.2);
    color: rgba(255, 255, 255, 0.9);
    padding: 6px 16px;
    border-radius: var(--border-radius-full);
    font-size: 13px;
    font-weight: 600;
    margin-bottom: 20px;
  }

  .hero-title {
    font-size: 48px;
    font-weight: 800;
    line-height: 1.2;
    color: white;
    margin-bottom: 16px;
    letter-spacing: -1px;

    .gradient-text {
      background: linear-gradient(135deg, #a78bfa, #f472b6);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
    }
  }

  .hero-subtitle {
    font-size: 16px;
    color: rgba(255, 255, 255, 0.7);
    margin-bottom: 32px;
    line-height: 1.6;
  }
}

.hero-search {
  display: flex;
  align-items: center;
  background: white;
  border-radius: var(--border-radius-full);
  padding: 6px 6px 6px 20px;
  gap: 12px;
  box-shadow: var(--shadow-xl);
  margin-bottom: 40px;

  .search-icon {
    color: var(--text-muted);
    flex-shrink: 0;
  }

  .hero-search-input {
    flex: 1;
    border: none;
    outline: none;
    font-size: 15px;
    color: var(--text-primary);
    font-family: inherit;
    padding: 8px 0;

    &::placeholder {
      color: var(--text-muted);
    }
  }

  .hero-search-btn {
    display: flex;
    align-items: center;
    gap: 6px;
    background: var(--gradient-primary);
    color: white;
    border: none;
    border-radius: var(--border-radius-full);
    padding: 10px 24px;
    font-size: 14px;
    font-weight: 600;
    cursor: pointer;
    font-family: inherit;
    transition: all var(--transition-fast);
    box-shadow: var(--shadow-primary);

    &:hover {
      transform: scale(1.03);
      box-shadow: 0 8px 24px rgba(99, 102, 241, 0.5);
    }
  }
}

.hero-stats {
  display: flex;
  align-items: center;
  gap: 24px;

  .stat-item {
    display: flex;
    flex-direction: column;
    align-items: flex-start;

    .stat-num {
      font-size: 22px;
      font-weight: 800;
      color: white;
      line-height: 1;
    }

    .stat-label {
      font-size: 12px;
      color: rgba(255, 255, 255, 0.6);
      margin-top: 4px;
    }
  }

  .stat-divider {
    width: 1px;
    height: 36px;
    background: rgba(255, 255, 255, 0.2);
  }
}

/* Hero Visual */
.hero-visual {
  position: relative;
  width: 300px;
  height: 360px;
  flex-shrink: 0;
}

.hero-circle {
  width: 210px;
  height: 210px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}

.hero-card {
  position: absolute;
  display: flex;
  align-items: center;
  gap: 10px;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.25);
  border-radius: var(--border-radius-md);
  padding: 12px 16px;
  white-space: nowrap;
  animation: float 3s ease-in-out infinite;

  .mini-card-icon {
    font-size: 28px;
  }

  .mini-card-name {
    font-size: 13px;
    color: rgba(255,255,255,0.9);
    font-weight: 500;
  }

  .mini-card-price {
    font-size: 15px;
    color: #fbbf24;
    font-weight: 700;
  }

  &.card-1 {
    top: 20px;
    left: -20px;
    animation-delay: 0s;
  }

  &.card-2 {
    top: 50%;
    right: -30px;
    transform: translateY(-50%);
    animation-delay: 1s;
  }

  &.card-3 {
    bottom: 20px;
    left: -10px;
    animation-delay: 0.5s;
  }
}

/* ========================
   Main Section
   ======================== */
.main-section {
  padding: 36px 0 48px;
}

.main-wrapper {
  display: flex;
  gap: 24px;
  align-items: flex-start;
}

/* ========================
   Sidebar
   ======================== */
.sidebar {
  width: 220px;
  flex-shrink: 0;
  position: sticky;
  top: 88px;
}

.sidebar-card {
  background: white;
  border-radius: var(--border-radius-md);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-color);
  overflow: hidden;
}

.sidebar-title {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 16px 20px;
  font-size: 15px;
  font-weight: 700;
  color: var(--text-primary);
  border-bottom: 1px solid var(--border-color);
  background: linear-gradient(135deg, #f8f9ff 0%, #f0f1ff 100%);
}

.category-list {
  padding: 8px;
}

.category-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  border-radius: var(--border-radius-sm);
  cursor: pointer;
  font-size: 13px;
  color: var(--text-secondary);
  transition: all var(--transition-fast);
  font-weight: 500;

  &:hover {
    background: rgba(99, 102, 241, 0.06);
    color: var(--color-primary);
  }

  &.active {
    background: rgba(99, 102, 241, 0.1);
    color: var(--color-primary);
    font-weight: 600;
  }

  &.sub-cat {
    padding-left: 24px;
    font-size: 12px;
  }

  .cat-icon {
    font-size: 18px;
    flex-shrink: 0;
  }

  .arrow {
    margin-left: auto;
    font-size: 12px;
    opacity: 0;
    transition: opacity var(--transition-fast);
  }

  &:hover .arrow,
  &.active .arrow {
    opacity: 1;
  }
}

/* ========================
   Content Area
   ======================== */
.content-area {
  flex: 1;
  min-width: 0;
}

.filter-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  background: white;
  padding: 14px 20px;
  border-radius: var(--border-radius-md);
  border: 1px solid var(--border-color);
  box-shadow: var(--shadow-sm);

  .result-count {
    font-size: 14px;
    color: var(--text-secondary);

    strong {
      color: var(--color-primary);
      font-weight: 700;
    }
  }
}

/* ========================
   Product Grid
   ======================== */
.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 20px;
}

.product-card-skeleton {
  background: white;
  border-radius: var(--border-radius-md);
  padding: 16px;
  border: 1px solid var(--border-color);
}

.product-card {
  background: white;
  border-radius: var(--border-radius-md);
  border: 1px solid var(--border-color);
  overflow: hidden;
  cursor: pointer;
  transition: all var(--transition-normal);
  box-shadow: var(--shadow-sm);

  &:hover {
    transform: translateY(-6px);
    box-shadow: var(--shadow-lg);
    border-color: transparent;

    .product-overlay {
      opacity: 1;
    }

    .product-image img {
      transform: scale(1.06);
    }
  }

  .product-image {
    height: 200px;
    position: relative;
    overflow: hidden;
    background: var(--bg-secondary);

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      transition: transform var(--transition-slow);
    }
  }

  .product-overlay {
    position: absolute;
    inset: 0;
    background: rgba(0,0,0,0.4);
    display: flex;
    align-items: center;
    justify-content: center;
    opacity: 0;
    transition: opacity var(--transition-normal);
    backdrop-filter: blur(2px);

    .quick-view-btn {
      display: flex;
      align-items: center;
      gap: 6px;
      background: white;
      color: var(--color-primary);
      border: none;
      border-radius: var(--border-radius-full);
      padding: 10px 20px;
      font-size: 13px;
      font-weight: 600;
      cursor: pointer;
      font-family: inherit;
      transition: all var(--transition-fast);

      &:hover {
        transform: scale(1.05);
      }
    }
  }

  .stock-badge {
    position: absolute;
    top: 10px;
    right: 10px;
    background: var(--color-warning);
    color: white;
    font-size: 11px;
    font-weight: 700;
    padding: 3px 10px;
    border-radius: var(--border-radius-full);
  }

  .sold-out-badge {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0,0,0,0.5);
    color: white;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: 700;
    font-size: 18px;
  }

  .product-body {
    padding: 14px 16px;
  }

  .product-name {
    font-size: 14px;
    font-weight: 600;
    color: var(--text-primary);
    margin-bottom: 6px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    line-height: 1.4;
  }

  .product-desc {
    font-size: 12px;
    color: var(--text-muted);
    margin-bottom: 12px;
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    line-height: 1.5;
    height: 36px;
  }

  .product-footer {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }

  .product-price {
    .price-symbol {
      font-size: 13px;
      color: var(--color-accent);
      font-weight: 700;
      vertical-align: super;
    }
    .price-value {
      font-size: 22px;
      color: var(--color-accent);
      font-weight: 800;
      letter-spacing: -0.5px;
    }
  }

  .add-cart-btn {
    width: 36px;
    height: 36px;
    background: var(--gradient-primary);
    color: white;
    border: none;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    transition: all var(--transition-fast);
    box-shadow: var(--shadow-primary);
    font-size: 16px;

    &:hover {
      transform: scale(1.1) rotate(5deg);
      box-shadow: 0 8px 20px rgba(99, 102, 241, 0.5);
    }

    &:disabled {
      background: var(--bg-secondary);
      color: var(--text-muted);
      box-shadow: none;
      cursor: not-allowed;
      transform: none;
    }
  }
}

/* ========================
   Load More / End of List
   ======================== */
.load-more-wrapper {
  margin-top: 36px;
  display: flex;
  justify-content: center;
  align-items: center;
  padding-bottom: 24px;
}

.load-more-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 12px 32px;
  background: white;
  color: var(--color-primary);
  border: 2px solid var(--color-primary);
  border-radius: var(--border-radius-full);
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all var(--transition-fast);
  font-family: inherit;
  box-shadow: 0 2px 12px rgba(99, 102, 241, 0.12);

  &:hover:not(:disabled) {
    background: var(--color-primary);
    color: white;
    transform: translateY(-2px);
    box-shadow: 0 8px 24px rgba(99, 102, 241, 0.3);
  }

  &:disabled,
  &.loading {
    opacity: 0.7;
    cursor: not-allowed;
    transform: none;
  }
}

.loaded-count {
  font-size: 12px;
  color: var(--text-muted);
  font-weight: 400;
  margin-left: 4px;
}

/* 加载中三点动画 */
.loading-dots {
  display: inline-flex;
  align-items: center;
  gap: 2px;

  i {
    width: 5px;
    height: 5px;
    border-radius: 50%;
    background: currentColor;
    display: inline-block;
    animation: dot-bounce 1.2s infinite ease-in-out;
    font-style: normal;

    &:nth-child(1) { animation-delay: 0s; }
    &:nth-child(2) { animation-delay: 0.2s; }
    &:nth-child(3) { animation-delay: 0.4s; }
  }
}

@keyframes dot-bounce {
  0%, 80%, 100% { transform: scale(0.6); opacity: 0.4; }
  40% { transform: scale(1); opacity: 1; }
}

.no-more {
  text-align: center;
  color: var(--text-muted);
  font-size: 13px;
  font-weight: 500;
  letter-spacing: 0.5px;
  padding: 8px 16px;
}

</style>
