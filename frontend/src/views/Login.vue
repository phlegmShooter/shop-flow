<template>
  <div class="auth-page">
    <!-- 左侧品牌区 -->
    <div class="auth-brand">
      <div class="brand-content">
        <div class="brand-logo">
          <el-icon :size="32"><ShoppingBag /></el-icon>
        </div>
        <h1 class="brand-name">ShopFlow</h1>
        <p class="brand-slogan">微服务电商订单平台</p>
        <div class="brand-features">
          <div class="feature-item" v-for="f in features" :key="f.title">
            <div class="feature-icon">{{ f.icon }}</div>
            <div>
              <div class="feature-title">{{ f.title }}</div>
              <div class="feature-desc">{{ f.desc }}</div>
            </div>
          </div>
        </div>
      </div>
      <div class="brand-bg-orb orb-1"></div>
      <div class="brand-bg-orb orb-2"></div>
    </div>

    <!-- 右侧登录表单 -->
    <div class="auth-form-area">
      <div class="auth-form-card">
        <div class="form-header">
          <h2>{{ $t('login.title') }}</h2>
          <p>{{ $t('login.subtitle') }}</p>
        </div>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-width="0"
          @submit.prevent
        >
          <el-form-item prop="username">
            <div class="input-group">
              <label class="input-label">{{ $t('login.username') }}</label>
              <el-input
                v-model="form.username"
                placeholder="请输入用户名"
                size="large"
                :prefix-icon="User"
              />
            </div>
          </el-form-item>

          <el-form-item prop="password">
            <div class="input-group">
              <div style="display:flex; justify-content:space-between; align-items:center;">
                <label class="input-label">{{ $t('login.password') }}</label>
                <a href="#" class="forgot-link">{{ $t('login.forgot') }}</a>
              </div>
              <el-input
                v-model="form.password"
                type="password"
                placeholder="请输入密码"
                size="large"
                :prefix-icon="Lock"
                show-password
                @keyup.enter="handleLogin"
              />
            </div>
          </el-form-item>

          <el-button
            type="primary"
            size="large"
            class="submit-btn"
            :loading="loading"
            @click="handleLogin"
          >
            <span v-if="!loading">{{ $t('login.loginBtn') }}</span>
            <span v-else>{{ $t('login.logining') }}</span>
          </el-button>
        </el-form>

        <div class="divider">
          <span>{{ $t('login.noAccount') }}</span>
        </div>

        <router-link to="/register" class="register-link">
          {{ $t('login.toRegister') }}
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { ShoppingBag, User, Lock } from '@element-plus/icons-vue'
import { login, getUserInfo } from '@/api/user'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const { t } = useI18n()

const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const features = [
  { icon: '🛡️', title: '安全保障', desc: 'JWT 双重认证，数据加密传输' },
  { icon: '⚡', title: '极速体验', desc: 'Redis 缓存，毫秒级响应' },
  { icon: '📦', title: '订单管理', desc: '全流程订单跟踪，实时通知' },
  { icon: '🌐', title: '微服务架构', desc: 'Spring Cloud 高可用部署' }
]

const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const token = await login(form)
        userStore.setToken(token)
        const userInfo = await getUserInfo()
        userStore.setUserInfo(userInfo)
        ElMessage.success(t('common.success') + ' 👋')
        const redirect = (route.query.redirect as string) || '/home'
        router.push(redirect)
      } catch (error) {
        console.error('登录失败', error)
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped lang="scss">
.auth-page {
  min-height: 100vh;
  display: flex;
}

/* ========================
   Brand Side
   ======================== */
.auth-brand {
  flex: 1;
  background: var(--gradient-hero);
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px;

  @media (max-width: 768px) {
    display: none;
  }
}

.brand-bg-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.4;

  &.orb-1 {
    width: 400px;
    height: 400px;
    background: radial-gradient(circle, #6366f1, transparent);
    top: -100px;
    right: -100px;
  }

  &.orb-2 {
    width: 300px;
    height: 300px;
    background: radial-gradient(circle, #f43f5e, transparent);
    bottom: -80px;
    left: -80px;
  }
}

.brand-content {
  position: relative;
  z-index: 1;
  max-width: 440px;
}

.brand-logo {
  width: 72px;
  height: 72px;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.25);
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  margin-bottom: 24px;
}

.brand-name {
  font-size: 48px;
  font-weight: 800;
  color: white;
  margin-bottom: 10px;
  letter-spacing: -1px;
}

.brand-slogan {
  font-size: 18px;
  color: rgba(255, 255, 255, 0.7);
  margin-bottom: 48px;
}

.brand-features {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.feature-item {
  display: flex;
  align-items: flex-start;
  gap: 16px;

  .feature-icon {
    font-size: 28px;
    width: 50px;
    height: 50px;
    background: rgba(255, 255, 255, 0.1);
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
  }

  .feature-title {
    font-size: 16px;
    font-weight: 600;
    color: white;
    margin-bottom: 4px;
  }

  .feature-desc {
    font-size: 13px;
    color: rgba(255, 255, 255, 0.6);
  }
}

/* ========================
   Form Side
   ======================== */
.auth-form-area {
  width: 480px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px 48px;
  background: white;

  @media (max-width: 768px) {
    width: 100%;
    padding: 40px 24px;
  }
}

.auth-form-card {
  width: 100%;
  max-width: 380px;
}

.form-header {
  margin-bottom: 36px;

  h2 {
    font-size: 32px;
    font-weight: 800;
    color: var(--text-primary);
    margin-bottom: 8px;
    letter-spacing: -0.5px;
  }

  p {
    font-size: 15px;
    color: var(--text-muted);
  }
}

.input-group {
  width: 100%;
  margin-bottom: 4px;

  .input-label {
    display: block;
    font-size: 13px;
    font-weight: 600;
    color: var(--text-secondary);
    margin-bottom: 8px;
    letter-spacing: 0.3px;
  }
}

.forgot-link {
  font-size: 13px;
  color: var(--color-primary);
  text-decoration: none;
  font-weight: 500;

  &:hover {
    text-decoration: underline;
  }
}

.submit-btn {
  width: 100%;
  height: 50px;
  font-size: 16px;
  font-weight: 700;
  border-radius: var(--border-radius-md) !important;
  margin-top: 8px;
  letter-spacing: 0.3px;
}

.divider {
  text-align: center;
  margin: 24px 0 16px;
  font-size: 14px;
  color: var(--text-muted);
}

.register-link {
  display: block;
  text-align: center;
  padding: 12px;
  border: 2px solid var(--border-color);
  border-radius: var(--border-radius-md);
  font-size: 15px;
  font-weight: 600;
  color: var(--color-primary);
  text-decoration: none;
  transition: all var(--transition-fast);

  &:hover {
    border-color: var(--color-primary);
    background: rgba(99, 102, 241, 0.04);
    transform: translateY(-1px);
  }
}

:deep(.el-form-item) {
  margin-bottom: 20px;
}

:deep(.el-input__wrapper) {
  height: 48px;
  border-radius: var(--border-radius-sm) !important;
}
</style>
