<template>
  <div class="profile-page">
    <div class="container">
      <!-- 顶部横幅 -->
      <div class="profile-banner">
        <div class="banner-bg"></div>
        <div class="banner-content">
          <div class="avatar-wrapper">
            <div class="avatar">
              {{ userStore.userInfo?.username?.charAt(0)?.toUpperCase() || 'U' }}
            </div>
          </div>
          <div class="profile-meta">
            <h1 class="profile-name">{{ userStore.userInfo?.username || $t('profile.defaultUsername') }}</h1>
            <p class="profile-email">{{ userStore.userInfo?.email || $t('profile.noEmail') }}</p>
          </div>
        </div>
      </div>

      <div class="profile-layout">
        <!-- 左侧菜单 -->
        <aside class="profile-sidebar">
          <div class="sidebar-card">
            <div
              v-for="item in menus"
              :key="item.key"
              class="sidebar-menu-item"
              :class="{ active: activeMenu === item.key }"
              @click="handleMenuClick(item.key)"
            >
              <span class="menu-icon">{{ item.icon }}</span>
              <span>{{ $t(`profile.menu.${item.key}`) }}</span>
            </div>
          </div>
        </aside>

        <!-- 右侧内容区 -->
        <div class="profile-content">
          <!-- 基本信息 -->
          <div v-if="activeMenu === 'info'" class="content-panel">
            <div class="panel-header">
              <h2>{{ $t('profile.basicInfo') }}</h2>
              <button
                class="edit-toggle"
                :class="{ editing: isEdit }"
                @click="isEdit = !isEdit"
              >
                {{ isEdit ? $t('profile.cancelEdit') : `✏️ ${$t('profile.edit')}` }}
              </button>
            </div>

            <el-form
              ref="formRef"
              :model="form"
              :rules="rules"
              label-width="90px"
              :disabled="!isEdit"
            >
              <el-form-item :label="$t('profile.username')">
                <el-input v-model="form.username" disabled />
              </el-form-item>
              <el-form-item :label="$t('profile.email')" prop="email">
                <el-input v-model="form.email" :placeholder="$t('profile.emailPlaceholder')" />
              </el-form-item>
              <el-form-item :label="$t('profile.phone')" prop="phone">
                <el-input v-model="form.phone" :placeholder="$t('profile.phonePlaceholder')" />
              </el-form-item>

              <el-form-item v-if="isEdit">
                <div class="form-actions">
                  <button class="save-btn" :disabled="loading" @click="handleSave">
                    <span v-if="!loading">{{ $t('profile.saveChanges') }}</span>
                    <span v-else>{{ $t('profile.saving') }}</span>
                  </button>
                  <button class="reset-btn" @click="resetForm">{{ $t('profile.resetBtn') }}</button>
                </div>
              </el-form-item>
            </el-form>
          </div>

          <!-- 修改密码 -->
          <div v-else-if="activeMenu === 'password'" class="content-panel">
            <div class="panel-header">
              <h2>{{ $t('profile.changePassword') }}</h2>
            </div>

            <el-form
              ref="passwordFormRef"
              :model="passwordForm"
              :rules="passwordRules"
              label-width="90px"
            >
              <el-form-item :label="$t('profile.currentPassword')" prop="oldPassword">
                <el-input
                  v-model="passwordForm.oldPassword"
                  type="password"
                  :placeholder="$t('profile.currentPasswordPlaceholder')"
                  show-password
                />
              </el-form-item>
              <el-form-item :label="$t('profile.newPassword')" prop="newPassword">
                <el-input
                  v-model="passwordForm.newPassword"
                  type="password"
                  :placeholder="$t('profile.newPasswordPlaceholder')"
                  show-password
                />
              </el-form-item>
              <el-form-item :label="$t('profile.confirmPassword')" prop="confirmPassword">
                <el-input
                  v-model="passwordForm.confirmPassword"
                  type="password"
                  :placeholder="$t('profile.confirmPasswordPlaceholder')"
                  show-password
                />
              </el-form-item>
              <el-form-item>
                <button class="save-btn" :disabled="passwordLoading" @click="handleChangePassword">
                  <span v-if="!passwordLoading">{{ $t('profile.changePasswordBtn') }}</span>
                  <span v-else>{{ $t('profile.changing') }}</span>
                </button>
              </el-form-item>
            </el-form>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getUserInfo, updateUserInfo, updatePassword } from '@/api/user'

const router = useRouter()
const userStore = useUserStore()
const { t } = useI18n()

const formRef = ref<FormInstance>()
const passwordFormRef = ref<FormInstance>()
const isEdit = ref(false)
const loading = ref(false)
const passwordLoading = ref(false)
const activeMenu = ref('info')

const handleMenuClick = (key: string) => {
  if (key === 'address') {
    router.push('/address')
  } else {
    activeMenu.value = key
  }
}

const menus = [
  { key: 'info', icon: '👤', label: '基本信息' },
  { key: 'password', icon: '🔒', label: '修改密码' },
  { key: 'address', icon: '📍', label: '收货地址' }
]

const form = reactive({
  username: '',
  email: '',
  phone: ''
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const rules = computed<FormRules>(() => ({
  email: [
    { required: true, message: t('profile.validation.emailRequired'), trigger: 'blur' },
    { type: 'email', message: t('profile.validation.emailFormat'), trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: t('profile.validation.phoneFormat'), trigger: 'blur' }
  ]
}))

const validateConfirmPassword = (_rule: any, value: any, callback: any) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error(t('profile.validation.passwordMismatch')))
  } else {
    callback()
  }
}

const passwordRules = computed<FormRules>(() => ({
  oldPassword: [
    { required: true, message: t('profile.validation.currentPasswordRequired'), trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: t('profile.validation.newPasswordRequired'), trigger: 'blur' },
    { min: 6, max: 20, message: t('profile.validation.passwordLength'), trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: t('profile.validation.confirmPasswordRequired'), trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}))

const resetForm = () => {
  if (userStore.userInfo) {
    form.username = userStore.userInfo.username
    form.email = userStore.userInfo.email
    form.phone = userStore.userInfo.phone || ''
  }
}

const handleSave = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await updateUserInfo({ email: form.email, phone: form.phone })
        ElMessage.success(t('profile.saveSuccess'))
        isEdit.value = false
        const userInfo = await getUserInfo()
        userStore.setUserInfo(userInfo)
      } catch (error) {
        console.error('保存失败', error)
      } finally {
        loading.value = false
      }
    }
  })
}

const handleChangePassword = async () => {
  if (!passwordFormRef.value) return
  await passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      passwordLoading.value = true
      try {
        await updatePassword(passwordForm.oldPassword, passwordForm.newPassword)
        ElMessage.success(t('profile.passwordChangeSuccess'))
        userStore.clearUser()
        router.push('/login')
      } catch (error) {
        console.error('修改密码失败', error)
      } finally {
        passwordLoading.value = false
      }
    }
  })
}

onMounted(() => {
  if (userStore.userInfo) {
    form.username = userStore.userInfo.username
    form.email = userStore.userInfo.email
    form.phone = userStore.userInfo.phone || ''
  }
})
</script>

<style scoped lang="scss">
.profile-page {
  padding: 0 0 48px;
  min-height: 70vh;
}

/* ========================
   Banner
   ======================== */
.profile-banner {
  position: relative;
  height: 180px;
  margin-bottom: 60px;
  overflow: visible;

  .banner-bg {
    position: absolute;
    inset: 0;
    background: var(--gradient-hero);
    border-radius: 0 0 var(--border-radius-xl) var(--border-radius-xl);
  }
}

.banner-content {
  position: absolute;
  bottom: -48px;
  left: 50%;
  transform: translateX(-50%);
  width: 1200px;
  max-width: calc(100% - 48px);
  display: flex;
  align-items: flex-end;
  gap: 20px;
}

.avatar-wrapper {
  .avatar {
    width: 96px;
    height: 96px;
    border-radius: 50%;
    background: var(--gradient-primary);
    color: white;
    font-size: 36px;
    font-weight: 800;
    display: flex;
    align-items: center;
    justify-content: center;
    border: 4px solid white;
    box-shadow: var(--shadow-lg);
  }
}

.profile-meta {
  padding-bottom: 8px;

  .profile-name {
    font-size: 24px;
    font-weight: 800;
    color: white;
    text-shadow: 0 2px 8px rgba(0,0,0,0.3);
  }

  .profile-email {
    font-size: 14px;
    color: rgba(255,255,255,0.8);
    text-shadow: 0 1px 4px rgba(0,0,0,0.2);
  }
}

/* ========================
   Layout
   ======================== */
.profile-layout {
  display: grid;
  grid-template-columns: 220px 1fr;
  gap: 24px;
  align-items: flex-start;

  @media (max-width: 768px) {
    grid-template-columns: 1fr;
  }
}

/* ========================
   Sidebar
   ======================== */
.profile-sidebar {
  position: sticky;
  top: 90px;
}

.sidebar-card {
  background: white;
  border-radius: var(--border-radius-md);
  border: 1px solid var(--border-color);
  box-shadow: var(--shadow-sm);
  overflow: hidden;
  padding: 8px;
}

.sidebar-menu-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  border-radius: var(--border-radius-sm);
  font-size: 14px;
  font-weight: 500;
  color: var(--text-secondary);
  cursor: pointer;
  transition: all var(--transition-fast);

  .menu-icon {
    font-size: 18px;
  }

  &:hover {
    background: rgba(99, 102, 241, 0.06);
    color: var(--color-primary);
  }

  &.active {
    background: rgba(99, 102, 241, 0.1);
    color: var(--color-primary);
    font-weight: 700;
  }
}

/* ========================
   Content Panel
   ======================== */
.profile-content {
  min-width: 0;
}

.content-panel {
  background: white;
  border-radius: var(--border-radius-md);
  border: 1px solid var(--border-color);
  box-shadow: var(--shadow-sm);
  overflow: hidden;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 28px;
  border-bottom: 1px solid var(--border-color);
  background: linear-gradient(135deg, #f8f9ff 0%, #f0f1ff 100%);

  h2 {
    font-size: 18px;
    font-weight: 700;
    color: var(--text-primary);
  }

  .edit-toggle {
    padding: 8px 18px;
    border: 1.5px solid var(--border-color);
    border-radius: var(--border-radius-full);
    background: white;
    font-size: 13px;
    font-weight: 600;
    color: var(--text-secondary);
    cursor: pointer;
    font-family: inherit;
    transition: all var(--transition-fast);

    &:hover,
    &.editing {
      border-color: var(--color-primary);
      color: var(--color-primary);
      background: rgba(99, 102, 241, 0.04);
    }
  }
}

:deep(.el-form) {
  padding: 28px;

  .el-form-item {
    margin-bottom: 20px;
  }

  .el-input__wrapper {
    height: 44px;
  }
}

.form-actions {
  display: flex;
  gap: 12px;
}

.save-btn {
  padding: 10px 28px;
  background: var(--gradient-primary);
  color: white;
  border: none;
  border-radius: var(--border-radius-full);
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
  font-family: inherit;
  box-shadow: var(--shadow-primary);
  transition: all var(--transition-fast);

  &:hover:not(:disabled) {
    transform: translateY(-1px);
    box-shadow: 0 8px 20px rgba(99, 102, 241, 0.4);
  }

  &:disabled {
    opacity: 0.6;
    cursor: not-allowed;
  }
}

.reset-btn {
  padding: 10px 24px;
  background: white;
  color: var(--text-secondary);
  border: 1.5px solid var(--border-color);
  border-radius: var(--border-radius-full);
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  font-family: inherit;
  transition: all var(--transition-fast);

  &:hover {
    border-color: var(--text-muted);
    color: var(--text-primary);
  }
}
</style>
