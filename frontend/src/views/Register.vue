<template>
  <div class="auth-page">
    <!-- 左侧品牌区 -->
    <div class="auth-brand">
      <div class="brand-content">
        <div class="brand-logo">
          <el-icon :size="32"><ShoppingBag /></el-icon>
        </div>
        <h1 class="brand-name">ShopFlow</h1>
        <p class="brand-slogan">{{ $t('register.subtitle') }}</p>
        <div class="brand-steps">
          <div class="step-item">
            <div class="step-num">01</div>
            <div>
              <div class="step-title">创建账号</div>
              <div class="step-desc">填写基本信息，快速注册</div>
            </div>
          </div>
          <div class="step-arrow">↓</div>
          <div class="step-item">
            <div class="step-num">02</div>
            <div>
              <div class="step-title">浏览商品</div>
              <div class="step-desc">海量精品，随心挑选</div>
            </div>
          </div>
          <div class="step-arrow">↓</div>
          <div class="step-item">
            <div class="step-num">03</div>
            <div>
              <div class="step-title">享受购物</div>
              <div class="step-desc">安全支付，快速收货</div>
            </div>
          </div>
        </div>
      </div>
      <div class="brand-bg-orb orb-1"></div>
      <div class="brand-bg-orb orb-2"></div>
    </div>

    <!-- 右侧注册表单 -->
    <div class="auth-form-area">
      <div class="auth-form-card">
        <div class="form-header">
          <h2>{{ $t('register.title') }}</h2>
          <p>{{ $t('register.subtitle') }}</p>
        </div>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-width="0"
        >
          <el-form-item prop="username">
            <div class="input-group">
              <label class="input-label">{{ $t('register.username') }}</label>
              <el-input
                v-model="form.username"
                placeholder="请输入用户名（字母、数字、下划线）"
                size="large"
                :prefix-icon="User"
                @blur="checkUsername"
              />
              <div class="field-hint" v-if="usernameChecked">
                <span v-if="usernameAvailable" class="hint-success">
                  <el-icon><CircleCheck /></el-icon>
                  用户名可用
                </span>
                <span v-else class="hint-error">
                  <el-icon><CircleClose /></el-icon>
                  {{ usernameError || '用户名已存在' }}
                </span>
              </div>
            </div>
          </el-form-item>

          <el-form-item prop="email">
            <div class="input-group">
              <label class="input-label">{{ $t('register.email') }}</label>
              <el-input
                v-model="form.email"
                placeholder="请输入邮箱地址"
                size="large"
                :prefix-icon="Message"
                @blur="checkEmail"
              />
              <div class="field-hint" v-if="emailChecked">
                <span v-if="emailAvailable" class="hint-success">
                  <el-icon><CircleCheck /></el-icon>
                  邮箱可用
                </span>
                <span v-else class="hint-error">
                  <el-icon><CircleClose /></el-icon>
                  该邮箱已被注册
                </span>
              </div>
            </div>
          </el-form-item>

          <el-form-item prop="password">
            <div class="input-group">
              <label class="input-label">{{ $t('register.password') }}</label>
              <el-input
                v-model="form.password"
                type="password"
                placeholder="至少6位，包含字母和数字"
                size="large"
                :prefix-icon="Lock"
                show-password
                @input="updatePasswordStrength"
              />
              <div class="password-strength" v-if="form.password">
                <div class="strength-bars">
                  <div
                    v-for="i in 4"
                    :key="i"
                    class="strength-bar"
                    :class="{ active: passwordStrength >= i, [`level-${passwordStrength}`]: true }"
                  ></div>
                </div>
                <span class="strength-text" :class="`level-${passwordStrength}-text`">
                  {{ strengthTexts[passwordStrength - 1] || '弱' }}
                </span>
              </div>
            </div>
          </el-form-item>

          <el-form-item prop="confirmPassword">
            <div class="input-group">
              <label class="input-label">{{ $t('profile.confirmPassword') }}</label>
              <el-input
                v-model="form.confirmPassword"
                type="password"
                placeholder="请再次输入密码"
                size="large"
                :prefix-icon="Lock"
                show-password
                @keyup.enter="handleRegister"
              />
            </div>
          </el-form-item>

          <el-button
            type="primary"
            size="large"
            class="submit-btn"
            :loading="loading"
            @click="handleRegister"
          >
            <span v-if="!loading">{{ $t('register.registerBtn') }}</span>
            <span v-else>{{ $t('register.registering') }}</span>
          </el-button>
        </el-form>

        <div class="divider">
          <span>{{ $t('register.hasAccount') }}</span>
        </div>

        <router-link to="/login" class="login-link">
          {{ $t('register.toLogin') }}
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { ShoppingBag, User, Message, Lock, CircleCheck, CircleClose } from '@element-plus/icons-vue'
import { register, checkUsername as apiCheckUsername, checkEmail as apiCheckEmail } from '@/api/user'

const router = useRouter()
const { t } = useI18n()

const formRef = ref<FormInstance>()
const loading = ref(false)
const usernameChecked = ref(false)
const usernameAvailable = ref(false)
const usernameError = ref('')
const emailChecked = ref(false)
const emailAvailable = ref(false)
const passwordStrength = ref(0)

const strengthTexts = ['弱', '一般', '较强', '强']

const form = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: ''
})

const updatePasswordStrength = () => {
  const pwd = form.password
  let strength = 0
  if (pwd.length >= 6) strength++
  if (pwd.length >= 10) strength++
  if (/[A-Z]/.test(pwd) || /[0-9]/.test(pwd)) strength++
  if (/[!@#$%^&*(),.?":{}|<>]/.test(pwd)) strength++
  passwordStrength.value = Math.min(4, strength)
}
const validateUsername = (_rule: any, value: any, callback: any) => {
  if (!value) {
    callback()
    return
  }
  if (!/^[\u4e00-\u9fa5a-zA-Z0-9_]+$/.test(value)) {
    callback(new Error('用户名只能包含中文、字母、数字和下划线'))
  } else {
    callback()
  }
}

const validateConfirmPassword = (_rule: any, value: any, callback: any) => {
  if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度为 3-50 个字符', trigger: 'blur' },
    { validator: validateUsername, trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为 6-20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const checkUsername = async () => {
  if (!form.username) return
  try {
    usernameAvailable.value = await apiCheckUsername(form.username)
    usernameChecked.value = true
  } catch (error) {
    console.error('检查用户名失败', error)
  }
}

const checkEmail = async () => {
  if (!form.email) return
  try {
    emailAvailable.value = await apiCheckEmail(form.email)
    emailChecked.value = true
  } catch (error) {
    console.error('检查邮箱失败', error)
  }
}

const handleRegister = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await register({
          username: form.username,
          email: form.email,
          password: form.password
        })
        ElMessage.success(t('register.success'))
        router.push('/login')
      } catch (error) {
        console.error('注册失败', error)
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

/* Brand Side */
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
    width: 350px;
    height: 350px;
    background: radial-gradient(circle, #8b5cf6, transparent);
    top: -80px;
    left: -80px;
  }

  &.orb-2 {
    width: 280px;
    height: 280px;
    background: radial-gradient(circle, #10b981, transparent);
    bottom: -60px;
    right: -60px;
  }
}

.brand-content {
  position: relative;
  z-index: 1;
  max-width: 400px;
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
  font-size: 44px;
  font-weight: 800;
  color: white;
  margin-bottom: 10px;
  letter-spacing: -1px;
}

.brand-slogan {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.7);
  margin-bottom: 48px;
}

.brand-steps {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.step-item {
  display: flex;
  align-items: center;
  gap: 16px;

  .step-num {
    width: 48px;
    height: 48px;
    background: rgba(255, 255, 255, 0.15);
    border: 1px solid rgba(255, 255, 255, 0.25);
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 16px;
    font-weight: 800;
    color: white;
    flex-shrink: 0;
  }

  .step-title {
    font-size: 16px;
    font-weight: 600;
    color: white;
    margin-bottom: 3px;
  }

  .step-desc {
    font-size: 13px;
    color: rgba(255, 255, 255, 0.6);
  }
}

.step-arrow {
  padding-left: 18px;
  color: rgba(255, 255, 255, 0.4);
  font-size: 18px;
}

/* Form Side */
.auth-form-area {
  width: 520px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px 48px;
  background: white;
  overflow-y: auto;

  @media (max-width: 768px) {
    width: 100%;
    padding: 40px 24px;
  }
}

.auth-form-card {
  width: 100%;
  max-width: 400px;
}

.form-header {
  margin-bottom: 32px;

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

  .input-label {
    display: block;
    font-size: 13px;
    font-weight: 600;
    color: var(--text-secondary);
    margin-bottom: 8px;
  }
}

.field-hint {
  margin-top: 6px;
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 4px;

  .hint-success {
    color: var(--color-success);
    display: flex;
    align-items: center;
    gap: 4px;
  }

  .hint-error {
    color: var(--color-danger);
    display: flex;
    align-items: center;
    gap: 4px;
  }
}

.password-strength {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 8px;

  .strength-bars {
    display: flex;
    gap: 4px;
  }

  .strength-bar {
    width: 40px;
    height: 4px;
    background: var(--border-color);
    border-radius: 2px;
    transition: background var(--transition-fast);

    &.active.level-1 { background: var(--color-danger); }
    &.active.level-2 { background: var(--color-warning); }
    &.active.level-3 { background: var(--color-info); }
    &.active.level-4 { background: var(--color-success); }
  }

  .strength-text {
    font-size: 12px;
    font-weight: 600;

    &.level-1-text { color: var(--color-danger); }
    &.level-2-text { color: var(--color-warning); }
    &.level-3-text { color: var(--color-info); }
    &.level-4-text { color: var(--color-success); }
  }
}

.submit-btn {
  width: 100%;
  height: 50px;
  font-size: 16px;
  font-weight: 700;
  border-radius: var(--border-radius-md) !important;
  margin-top: 8px;
}

.divider {
  text-align: center;
  margin: 24px 0 16px;
  font-size: 14px;
  color: var(--text-muted);
}

.login-link {
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
