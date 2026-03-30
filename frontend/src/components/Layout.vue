<template>
  <div class="layout" :class="{ 'scrolled': isScrolled }">
    <!-- 顶部导航栏 -->
    <header class="header">
      <div class="container header-inner">
        <!-- Logo -->
        <div class="logo" @click="$router.push('/home')">
          <div class="logo-icon">
            <el-icon :size="22"><ShoppingBag /></el-icon>
          </div>
          <span class="logo-text">ShopFlow</span>
        </div>

        <div class="header-search">
          <div class="search-input-wrapper">
            <el-icon class="search-icon"><Search /></el-icon>
            <input
              v-model="searchKeyword"
              class="search-input"
              :placeholder="$t('common.search')"
              @keyup.enter="handleSearch"
            />
            <button class="search-btn" @click="handleSearch">{{ $t('common.search') }}</button>
          </div>
        </div>

        <!-- 语言切换 -->
        <div class="lang-switch" @click="toggleLanguage">
          <span class="lang-text">{{ currentLang === 'zh' ? 'EN' : '中' }}</span>
        </div>

        <!-- 右侧操作区 -->
        <div class="nav-actions">
          <template v-if="userStore.isLoggedIn">
            <!-- 购物车 -->
            <div class="nav-item" @click="$router.push('/cart')">
              <div class="nav-icon-wrapper">
                <el-icon :size="20"><ShoppingCart /></el-icon>
                <span v-if="cartStore.cartInfo?.totalCount" class="nav-badge">
                  {{ cartStore.cartInfo?.totalCount > 99 ? '99+' : cartStore.cartInfo?.totalCount }}
                </span>
              </div>
              <span class="nav-label">{{ $t('nav.cart') }}</span>
            </div>

            <!-- 消息 -->
            <div class="nav-item" @click="$router.push('/notification')">
              <div class="nav-icon-wrapper">
                <el-icon :size="20"><Bell /></el-icon>
                <span v-if="unreadCount" class="nav-badge">
                  {{ unreadCount > 99 ? '99+' : unreadCount }}
                </span>
              </div>
              <span class="nav-label">{{ $t('nav.notifications') }}</span>
            </div>

            <!-- 用户下拉菜单（全自定义，替换 el-dropdown） -->
            <div class="user-menu-wrapper" ref="userMenuRef">
              <div
                class="user-avatar-wrapper"
                :class="{ open: dropdownOpen }"
                @click="dropdownOpen = !dropdownOpen"
              >
                <div class="user-avatar">
                  <span>{{ userStore.userInfo?.username?.charAt(0)?.toUpperCase() || 'U' }}</span>
                  <div class="avatar-ring"></div>
                </div>
                <div class="user-info-mini">
                  <span class="user-name">{{ userStore.userInfo?.username || '用户' }}</span>
                  <span class="user-status">已登录</span>
                </div>
                <el-icon class="arrow-icon" :class="{ rotated: dropdownOpen }"><ArrowDown /></el-icon>
              </div>

              <!-- 下拉面板 -->
              <transition name="dropdown-fade">
                <div v-if="dropdownOpen" class="custom-dropdown">
                  <!-- 顶部用户卡片 -->
                  <div class="dd-user-card">
                    <div class="dd-avatar-large">
                      <span>{{ userStore.userInfo?.username?.charAt(0)?.toUpperCase() || 'U' }}</span>
                    </div>
                    <div class="dd-user-detail">
                      <div class="dd-username">{{ userStore.userInfo?.username }}</div>
                      <div class="dd-email">{{ userStore.userInfo?.email }}</div>
                      <div class="dd-badge">普通会员</div>
                    </div>
                  </div>

                  <!-- 菜单项 -->
                  <div class="dd-menu">
                    <div class="dd-item" @click="navigate('profile')">
                      <div class="dd-item-icon" style="background: linear-gradient(135deg,#6366f1,#8b5cf6)">
                        <el-icon><User /></el-icon>
                      </div>
                      <div class="dd-item-text">
                        <span class="dd-item-label">{{ $t('nav.profile') }}</span>
                        <span class="dd-item-sub">{{ $t('common.edit') }}</span>
                      </div>
                      <el-icon class="dd-item-arrow"><ArrowRight /></el-icon>
                    </div>

                    <div class="dd-item" @click="navigate('order')">
                      <div class="dd-item-icon" style="background: linear-gradient(135deg,#f59e0b,#fbbf24)">
                        <el-icon><Document /></el-icon>
                      </div>
                      <div class="dd-item-text">
                        <span class="dd-item-label">{{ $t('nav.orders') }}</span>
                        <span class="dd-item-sub">{{ $t('operate.operate') }}</span>
                      </div>
                      <el-icon class="dd-item-arrow"><ArrowRight /></el-icon>
                    </div>

                    <div class="dd-item" @click="navigate('notification')">
                      <div class="dd-item-icon" style="background: linear-gradient(135deg,#10b981,#34d399)">
                        <el-icon><Bell /></el-icon>
                      </div>
                      <div class="dd-item-text">
                        <span class="dd-item-label">{{ $t('nav.notifications') }}</span>
                        <span class="dd-item-sub">{{ unreadCount ? `${unreadCount}` : '0' }}</span>
                      </div>
                      <span v-if="unreadCount" class="dd-unread-dot">{{ unreadCount }}</span>
                    </div>
                  </div>

                  <!-- 退出按钮 -->
                  <div class="dd-footer">
                    <button class="dd-logout" @click="handleLogout">
                      <el-icon><SwitchButton /></el-icon>
                      {{ $t('nav.logout') }}
                    </button>
                  </div>
                </div>
              </transition>
            </div>
          </template>

          <template v-else>
            <router-link to="/login" class="btn-login">{{ $t('nav.login') }}</router-link>
            <router-link to="/register" class="btn-register">{{ $t('nav.register') }}</router-link>
          </template>
        </div>
      </div>
    </header>

    <!-- 主内容区 -->
    <main class="main-content">
      <slot />
    </main>

    <!-- 页脚 -->
    <footer class="footer">
      <div class="container footer-inner">
        <div class="footer-brand">
          <div class="footer-logo">
            <el-icon :size="20"><ShoppingBag /></el-icon>
            <span>ShopFlow</span>
          </div>
          <p class="footer-desc">基于 Spring Cloud 微服务架构的电商订单管理平台</p>
        </div>
        <div class="footer-links">
          <div class="footer-link-group">
            <h4>服务</h4>
            <a href="#">商品浏览</a>
            <a href="#">订单管理</a>
            <a href="#">购物车</a>
          </div>
          <div class="footer-link-group">
            <h4>技术栈</h4>
            <a href="#">Spring Cloud</a>
            <a href="#">Redis</a>
            <a href="#">RabbitMQ</a>
          </div>
          <div class="footer-link-group">
            <h4>联系</h4>
            <a href="#">GitHub</a>
            <a href="#">API 文档</a>
          </div>
        </div>
      </div>
      <div class="footer-bottom">
        <span>© 2026 ShopFlow - 微服务电商订单平台. All rights reserved.</span>
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ShoppingCart,
  Bell,
  User,
  Document,
  SwitchButton,
  ArrowDown,
  ArrowRight,
  Search,
  ShoppingBag
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'
import { logout } from '@/api/user'
import { getUnreadCount } from '@/api/notification'

const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()
const { locale } = useI18n()
const unreadCount = ref(0)
const isScrolled = ref(false)
const searchKeyword = ref('')
const dropdownOpen = ref(false)
const userMenuRef = ref<HTMLElement | null>(null)

const currentLang = computed(() => locale.value)
const toggleLanguage = () => {
  const newLang = locale.value === 'zh' ? 'en' : 'zh'
  locale.value = newLang
  localStorage.setItem('language', newLang)
}

// 点击外部关闭下拉
const handleClickOutside = (e: MouseEvent) => {
  if (userMenuRef.value && !userMenuRef.value.contains(e.target as Node)) {
    dropdownOpen.value = false
  }
}

const handleSearch = () => {
  if (searchKeyword.value.trim()) {
    router.push({ path: '/home', query: { keyword: searchKeyword.value.trim() } })
  }
}

// 菜单导航（关闭下拉后跳转）
const navigate = (page: string) => {
  dropdownOpen.value = false
  router.push(`/${page}`)
}

// 退出登录
const handleLogout = async () => {
  dropdownOpen.value = false
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '退出确认', {
      confirmButtonText: '确定退出',
      cancelButtonText: '取消',
      type: 'warning',
      confirmButtonClass: 'el-button--danger'
    })
    await logout()
    userStore.clearUser()
    cartStore.clearCart()
    ElMessage.success('已安全退出')
    router.push('/home')
  } catch {
    // 用户取消
  }
}

const fetchUnreadCount = async () => {
  if (userStore.isLoggedIn) {
    try {
      unreadCount.value = await getUnreadCount()
    } catch (error) {
      console.error('获取未读消息数失败', error)
    }
  }
}

const handleScroll = () => {
  isScrolled.value = window.scrollY > 20
}

onMounted(() => {
  window.addEventListener('scroll', handleScroll)
  document.addEventListener('click', handleClickOutside)
  if (userStore.token) {
    cartStore.fetchCart()
    fetchUnreadCount()
  }
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
  document.removeEventListener('click', handleClickOutside)
})
</script>

<style scoped lang="scss">
.layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--bg-primary);
}

/* ========================
   Header
   ======================== */
.header {
  position: sticky;
  top: 0;
  z-index: 1000;
  height: 68px;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(226, 232, 240, 0.8);
  transition: all var(--transition-normal);

  .layout.scrolled & {
    box-shadow: var(--shadow-md);
    background: rgba(255, 255, 255, 0.95);
  }
}

.header-inner {
  display: flex;
  align-items: center;
  height: 100%;
  gap: 24px;
}

/* Logo */
.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  text-decoration: none;
  flex-shrink: 0;

  .logo-icon {
    width: 38px;
    height: 38px;
    background: var(--gradient-primary);
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    box-shadow: var(--shadow-primary);
    transition: transform var(--transition-normal);

    &:hover {
      transform: rotate(-5deg) scale(1.05);
    }
  }

  .logo-text {
    font-size: 20px;
    font-weight: 800;
    background: var(--gradient-primary);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
    letter-spacing: -0.5px;
  }
}

/* Search */
.header-search {
  flex: 1;
  max-width: 520px;

  .search-input-wrapper {
    display: flex;
    align-items: center;
    background: var(--bg-secondary);
    border: 1.5px solid var(--border-color);
    border-radius: var(--border-radius-full);
    padding: 0 8px 0 16px;
    gap: 8px;
    transition: all var(--transition-fast);

    &:focus-within {
      border-color: var(--color-primary);
      background: white;
      box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.12);
    }

    .search-icon {
      color: var(--text-muted);
      flex-shrink: 0;
    }

    .search-input {
      flex: 1;
      border: none;
      outline: none;
      background: transparent;
      font-size: 14px;
      color: var(--text-primary);
      padding: 9px 0;
      font-family: inherit;

      &::placeholder {
        color: var(--text-muted);
      }
    }

    .search-btn {
      flex-shrink: 0;
      background: var(--gradient-primary);
      color: white;
      border: none;
      border-radius: var(--border-radius-full);
      padding: 6px 16px;
      font-size: 13px;
      font-weight: 600;
      cursor: pointer;
      transition: all var(--transition-fast);
      font-family: inherit;

      &:hover {
        opacity: 0.9;
        transform: scale(1.02);
      }
    }
  }
}

/* Lang Switch */
.lang-switch {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  cursor: pointer;
  margin-right: 8px;
  transition: all var(--transition-fast);
  color: var(--text-secondary);
  font-weight: 700;
  font-size: 12px;

  &:hover {
    color: var(--color-primary);
    border-color: var(--color-primary);
    background: rgba(99, 102, 241, 0.05);
  }
}

/* Nav Actions */
.nav-actions {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-shrink: 0;
}

.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  padding: 6px 12px;
  border-radius: var(--border-radius-sm);
  cursor: pointer;
  transition: all var(--transition-fast);
  position: relative;

  &:hover {
    background: rgba(99, 102, 241, 0.08);
  }

  .nav-icon-wrapper {
    position: relative;
    color: var(--text-secondary);
  }

  .nav-badge {
    position: absolute;
    top: -6px;
    right: -8px;
    background: var(--color-accent);
    color: white;
    font-size: 10px;
    font-weight: 700;
    padding: 1px 5px;
    border-radius: var(--border-radius-full);
    min-width: 18px;
    text-align: center;
    line-height: 16px;
  }

  .nav-label {
    font-size: 11px;
    color: var(--text-secondary);
    font-weight: 500;
  }
}

/* User Avatar */
.user-avatar-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px;
  border-radius: var(--border-radius-md);
  cursor: pointer;
  transition: all var(--transition-fast);

  &:hover {
    background: rgba(99, 102, 241, 0.08);
  }

  .user-avatar {
    width: 34px;
    height: 34px;
    border-radius: 50%;
    background: var(--gradient-primary);
    color: white;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 14px;
    font-weight: 700;
    box-shadow: var(--shadow-primary);
  }

  .user-name {
    font-size: 14px;
    font-weight: 500;
    color: var(--text-primary);
    max-width: 80px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .arrow-icon {
    color: var(--text-muted);
    font-size: 12px;
  }
}

/* Auth Buttons */
.btn-login {
  padding: 8px 18px;
  border-radius: var(--border-radius-full);
  font-size: 14px;
  font-weight: 600;
  color: var(--color-primary);
  text-decoration: none;
  transition: all var(--transition-fast);
  border: 1.5px solid var(--color-primary);

  &:hover {
    background: rgba(99, 102, 241, 0.08);
  }
}

.btn-register {
  padding: 8px 18px;
  border-radius: var(--border-radius-full);
  font-size: 14px;
  font-weight: 600;
  color: white;
  text-decoration: none;
  background: var(--gradient-primary);
  box-shadow: var(--shadow-primary);
  transition: all var(--transition-fast);

  &:hover {
    transform: translateY(-1px);
    box-shadow: 0 8px 24px rgba(99, 102, 241, 0.4);
  }
}

/* ========================
   User Avatar Wrapper (Trigger)
   ======================== */
.user-menu-wrapper {
  position: relative;
}

.user-avatar-wrapper {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 5px 12px 5px 5px;
  border-radius: var(--border-radius-full);
  cursor: pointer;
  transition: all var(--transition-fast);
  border: 1.5px solid transparent;

  &:hover,
  &.open {
    background: rgba(99, 102, 241, 0.06);
    border-color: rgba(99, 102, 241, 0.2);
  }

  .user-avatar {
    position: relative;
    width: 36px;
    height: 36px;
    border-radius: 50%;
    background: var(--gradient-primary);
    color: white;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 15px;
    font-weight: 700;
    box-shadow: 0 2px 8px rgba(99, 102, 241, 0.4);
    flex-shrink: 0;

    .avatar-ring {
      position: absolute;
      inset: -2px;
      border-radius: 50%;
      border: 2px solid transparent;
      background: var(--gradient-primary) border-box;
      -webkit-mask: linear-gradient(#fff 0 0) padding-box, linear-gradient(#fff 0 0);
      -webkit-mask-composite: destination-out;
      mask-composite: exclude;
      opacity: 0;
      transition: opacity var(--transition-fast);
    }
  }

  &.open .avatar-ring {
    opacity: 1;
  }

  .user-info-mini {
    display: flex;
    flex-direction: column;
    gap: 1px;

    .user-name {
      font-size: 13px;
      font-weight: 600;
      color: var(--text-primary);
      max-width: 72px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      line-height: 1.3;
    }

    .user-status {
      font-size: 10px;
      color: #10b981;
      font-weight: 500;
      display: flex;
      align-items: center;
      gap: 3px;

      &::before {
        content: '';
        width: 5px;
        height: 5px;
        border-radius: 50%;
        background: #10b981;
        display: inline-block;
      }
    }
  }

  .arrow-icon {
    color: var(--text-muted);
    font-size: 11px;
    transition: transform var(--transition-fast);

    &.rotated {
      transform: rotate(180deg);
    }
  }
}

/* ========================
   Custom Dropdown Panel
   ======================== */
.custom-dropdown {
  position: absolute;
  top: calc(100% + 10px);
  right: 0;
  width: 260px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(24px);
  -webkit-backdrop-filter: blur(24px);
  border: 1px solid rgba(226, 232, 240, 0.8);
  border-radius: 16px;
  box-shadow:
    0 8px 32px rgba(0, 0, 0, 0.12),
    0 2px 8px rgba(0, 0, 0, 0.06),
    inset 0 1px 0 rgba(255, 255, 255, 0.8);
  overflow: hidden;
  z-index: 2000;
}

/* User Card */
.dd-user-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px 18px 16px;
  background: linear-gradient(135deg, #f8f9ff 0%, rgba(99, 102, 241, 0.05) 100%);
  border-bottom: 1px solid rgba(226, 232, 240, 0.6);

  .dd-avatar-large {
    width: 50px;
    height: 50px;
    border-radius: 14px;
    background: var(--gradient-primary);
    color: white;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 20px;
    font-weight: 800;
    flex-shrink: 0;
    box-shadow: 0 4px 12px rgba(99, 102, 241, 0.35);
  }

  .dd-user-detail {
    min-width: 0;
    flex: 1;

    .dd-username {
      font-size: 15px;
      font-weight: 700;
      color: var(--text-primary);
      margin-bottom: 3px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .dd-email {
      font-size: 11px;
      color: var(--text-muted);
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      margin-bottom: 6px;
    }

    .dd-badge {
      display: inline-flex;
      align-items: center;
      background: linear-gradient(135deg, rgba(99, 102, 241, 0.1), rgba(139, 92, 246, 0.1));
      color: var(--color-primary);
      font-size: 10px;
      font-weight: 700;
      padding: 2px 8px;
      border-radius: 20px;
      border: 1px solid rgba(99, 102, 241, 0.2);
    }
  }
}

/* Menu Items */
.dd-menu {
  padding: 8px 0;
}

.dd-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 16px;
  cursor: pointer;
  transition: all var(--transition-fast);
  position: relative;

  &:hover {
    background: rgba(99, 102, 241, 0.05);

    .dd-item-label {
      color: var(--color-primary);
    }

    .dd-item-arrow {
      opacity: 1;
      transform: translateX(2px);
    }
  }

  .dd-item-icon {
    width: 36px;
    height: 36px;
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    flex-shrink: 0;
    font-size: 15px;
    box-shadow: 0 2px 6px rgba(0, 0, 0, 0.15);
  }

  .dd-item-text {
    flex: 1;
    min-width: 0;
    display: flex;
    flex-direction: column;
    gap: 1px;

    .dd-item-label {
      font-size: 14px;
      font-weight: 600;
      color: var(--text-primary);
      transition: color var(--transition-fast);
    }

    .dd-item-sub {
      font-size: 11px;
      color: var(--text-muted);
    }
  }

  .dd-item-arrow {
    color: var(--text-muted);
    font-size: 13px;
    opacity: 0;
    transition: all var(--transition-fast);
    flex-shrink: 0;
  }

  .dd-unread-dot {
    background: var(--color-accent);
    color: white;
    font-size: 10px;
    font-weight: 700;
    padding: 1px 6px;
    border-radius: 20px;
    min-width: 18px;
    text-align: center;
    flex-shrink: 0;
  }
}

/* Footer / Logout */
.dd-footer {
  padding: 8px 12px 12px;
  border-top: 1px solid rgba(226, 232, 240, 0.6);
  margin-top: 4px;
}

.dd-logout {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 10px;
  background: rgba(244, 63, 94, 0.05);
  color: var(--color-accent);
  border: 1px solid rgba(244, 63, 94, 0.15);
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all var(--transition-fast);
  font-family: inherit;

  &:hover {
    background: rgba(244, 63, 94, 0.1);
    border-color: rgba(244, 63, 94, 0.3);
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(244, 63, 94, 0.15);
  }
}

/* Dropdown Animation */
.dropdown-fade-enter-active,
.dropdown-fade-leave-active {
  transition: all 0.2s cubic-bezier(0.16, 1, 0.3, 1);
  transform-origin: top right;
}

.dropdown-fade-enter-from,
.dropdown-fade-leave-to {
  opacity: 0;
  transform: scale(0.92) translateY(-8px);
}


/* ========================
   Main Content
   ======================== */
.main-content {
  flex: 1;
  min-height: calc(100vh - 68px - 240px);
}

/* ========================
   Footer
   ======================== */
.footer {
  background: #1e293b;
  color: #94a3b8;
  margin-top: auto;
}

.footer-inner {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 48px 24px;
  gap: 40px;
}

.footer-brand {
  max-width: 280px;

  .footer-logo {
    display: flex;
    align-items: center;
    gap: 8px;
    color: white;
    font-size: 18px;
    font-weight: 700;
    margin-bottom: 12px;
  }

  .footer-desc {
    font-size: 13px;
    line-height: 1.7;
    color: #64748b;
  }
}

.footer-links {
  display: flex;
  gap: 48px;
}

.footer-link-group {
  h4 {
    color: white;
    font-size: 14px;
    font-weight: 600;
    margin-bottom: 16px;
  }

  a {
    display: block;
    color: #64748b;
    font-size: 13px;
    text-decoration: none;
    padding: 4px 0;
    transition: color var(--transition-fast);

    &:hover {
      color: var(--color-primary-light);
    }
  }
}

.footer-bottom {
  text-align: center;
  padding: 16px 24px;
  border-top: 1px solid #2d3748;
  font-size: 12px;
  color: #475569;
}
</style>
