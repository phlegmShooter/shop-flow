<template>
  <div class="address-page">
    <div class="container">
      <div class="page-header">
        <button class="back-btn" @click="$router.back()">
          <el-icon><ArrowLeft /></el-icon> {{ $t('common.back') }}
        </button>
        <h1 class="page-title">{{ $t('address.title') }}</h1>
        <button class="add-btn" @click="openForm()">
          <el-icon><Plus /></el-icon> {{ $t('address.add') }}
        </button>
      </div>

      <!-- 地址列表 -->
      <div v-if="loading" class="address-grid">
        <div v-for="i in 3" :key="i" class="address-card skeleton-card">
          <div class="skeleton" style="height:18px;width:60%;margin-bottom:12px"></div>
          <div class="skeleton" style="height:14px;margin-bottom:8px"></div>
          <div class="skeleton" style="height:14px;width:80%"></div>
        </div>
      </div>

      <div v-else-if="addresses.length === 0" class="empty-state">
        <div class="empty-icon">📍</div>
        <p class="empty-text">{{ $t('address.empty') }}</p>
        <button class="add-empty-btn" @click="openForm()">+ {{ $t('address.add') }}</button>
      </div>

      <div v-else class="address-grid">
        <div
          v-for="addr in addresses"
          :key="addr.id"
          class="address-card"
          :class="{ 'is-default': addr.isDefault === 1 }"
        >
          <div class="card-top">
            <div class="receiver-info">
              <span class="receiver-name">{{ addr.receiverName }}</span>
              <span class="receiver-phone">{{ addr.phone }}</span>
            </div>
            <span v-if="addr.isDefault === 1" class="default-tag">{{ $t('address.default') }}</span>
          </div>
          <div class="card-address">
            {{ addr.province }}{{ addr.city }}{{ addr.district || '' }}{{ addr.detail }}
          </div>
          <div class="card-actions">
            <button
              v-if="addr.isDefault !== 1"
              class="action-btn default-btn"
              @click="handleSetDefault(addr.id)"
            >
              {{ $t('address.setDefault') }}
            </button>
            <button class="action-btn edit-btn" @click="openForm(addr)">{{ $t('common.edit') }}</button>
            <button class="action-btn delete-btn" @click="handleDelete(addr)">{{ $t('common.delete') }}</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 新增/编辑弹窗 -->
    <div v-if="showForm" class="modal-overlay" @click.self="closeForm">
      <div class="modal-card">
        <div class="modal-header">
          <h2>{{ editingAddr ? $t('address.editAddress') : $t('address.addAddress') }}</h2>
          <button class="modal-close" @click="closeForm">✕</button>
        </div>

        <div class="modal-body">
          <!-- 收货人 & 手机 -->
          <div class="form-row">
            <div class="form-field">
              <label>{{ $t('address.receiver') }} <span class="req">*</span></label>
              <input v-model="form.receiverName" :placeholder="$t('address.receiverPlaceholder')" class="f-input" />
            </div>
            <div class="form-field">
              <label>{{ $t('address.phone') }} <span class="req">*</span></label>
              <input v-model="form.phone" :placeholder="$t('address.phonePlaceholder')" class="f-input" maxlength="11" />
            </div>
          </div>

          <!-- 省市区三级联动 -->
          <div class="form-row">
            <div class="form-field">
              <label>{{ $t('address.province') }} <span class="req">*</span></label>
              <select v-model="form.province" class="f-select" @change="onProvinceChange">
                <option value="">{{ $t('address.provincePlaceholder') }}</option>
                <option v-for="p in regions" :key="p.name" :value="p.name">{{ p.name }}</option>
              </select>
            </div>
            <div class="form-field">
              <label>{{ $t('address.city') }} <span class="req">*</span></label>
              <select v-model="form.city" class="f-select" @change="onCityChange" :disabled="!form.province">
                <option value="">{{ $t('address.cityPlaceholder') }}</option>
                <option v-for="c in currentCities" :key="c.name" :value="c.name">{{ c.name }}</option>
              </select>
            </div>
            <div class="form-field">
              <label>{{ $t('address.district') }}</label>
              <select v-model="form.district" class="f-select" :disabled="!form.city">
                <option value="">{{ $t('address.districtPlaceholder') }}</option>
                <option v-for="d in currentDistricts" :key="d.name" :value="d.name">{{ d.name }}</option>
              </select>
            </div>
          </div>

          <!-- 详细地址 -->
          <div class="form-field full">
            <label>{{ $t('address.detail') }} <span class="req">*</span></label>
            <input v-model="form.detail" :placeholder="$t('address.detailPlaceholder')" class="f-input" />
          </div>

          <!-- 设为默认 -->
          <div class="form-check">
            <label class="check-label">
              <input type="checkbox" v-model="form.isDefaultBool" class="check-input" />
              <span class="check-text">{{ $t('address.setAsDefault') }}</span>
            </label>
          </div>
        </div>

        <div class="modal-footer">
          <button class="btn-cancel" @click="closeForm">{{ $t('common.cancel') }}</button>
          <button class="btn-save" @click="handleSave" :disabled="saving">
            {{ saving ? $t('common.saving') : $t('address.saveAddress') }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Plus } from '@element-plus/icons-vue'
import {
  getAddressList, addAddress, updateAddress,
  deleteAddress, setDefaultAddress
} from '@/api/address'
import type { UserAddress } from '@/types'
import regionsData from '@/data/regions.json'

const { t } = useI18n()
const regions = regionsData as any[]

// ── 状态 ──────────────────────────────────────────
const addresses = ref<UserAddress[]>([])
const loading = ref(false)
const showForm = ref(false)
const saving = ref(false)
const editingAddr = ref<UserAddress | null>(null)

const emptyForm = () => ({
  receiverName: '',
  phone: '',
  province: '',
  city: '',
  district: '',
  detail: '',
  isDefaultBool: false,
})
const form = ref(emptyForm())

// ── 省市区联动 ────────────────────────────────────
const currentCities = computed(() => {
  const prov = regions.find(p => p.name === form.value.province)
  return prov?.children ?? []
})

const currentDistricts = computed(() => {
  const city = currentCities.value.find((c: any) => c.name === form.value.city)
  return city?.children ?? []
})

const onProvinceChange = () => {
  form.value.city = ''
  form.value.district = ''
}
const onCityChange = () => {
  form.value.district = ''
}

// ── 加载地址 ──────────────────────────────────────
const loadAddresses = async () => {
  loading.value = true
  try {
    addresses.value = await getAddressList()
  } catch {
    // 拦截器已处理
  } finally {
    loading.value = false
  }
}

// ── 打开表单 ──────────────────────────────────────
const openForm = (addr?: UserAddress) => {
  if (addr) {
    editingAddr.value = addr
    form.value = {
      receiverName: addr.receiverName,
      phone: addr.phone,
      province: addr.province,
      city: addr.city,
      district: addr.district ?? '',
      detail: addr.detail,
      isDefaultBool: addr.isDefault === 1,
    }
  } else {
    editingAddr.value = null
    form.value = emptyForm()
  }
  showForm.value = true
}

const closeForm = () => {
  showForm.value = false
}

// ── 校验 ──────────────────────────────────────────
const validate = () => {
  const { receiverName, phone, province, city, detail } = form.value
  if (!receiverName.trim()) { ElMessage.warning(t('address.validation.receiverRequired')); return false }
  if (!/^1[3-9]\d{9}$/.test(phone)) { ElMessage.warning(t('address.validation.phoneInvalid')); return false }
  if (!province) { ElMessage.warning(t('address.validation.provinceRequired')); return false }
  if (!city) { ElMessage.warning(t('address.validation.cityRequired')); return false }
  if (!detail.trim()) { ElMessage.warning(t('address.validation.detailRequired')); return false }
  return true
}

// ── 保存 ──────────────────────────────────────────
const handleSave = async () => {
  if (!validate()) return
  saving.value = true
  const payload: Partial<UserAddress> = {
    receiverName: form.value.receiverName,
    phone: form.value.phone,
    province: form.value.province,
    city: form.value.city,
    district: form.value.district || undefined,
    detail: form.value.detail,
    isDefault: form.value.isDefaultBool ? 1 : 0,
  }
  try {
    if (editingAddr.value) {
      await updateAddress(editingAddr.value.id, payload)
      ElMessage.success(t('address.updateSuccess'))
    } else {
      await addAddress(payload)
      ElMessage.success(t('address.addSuccess'))
    }
    closeForm()
    await loadAddresses()
  } catch {
    // 拦截器处理
  } finally {
    saving.value = false
  }
}

// ── 设为默认 ──────────────────────────────────────
const handleSetDefault = async (id: number) => {
  try {
    await setDefaultAddress(id)
    ElMessage.success(t('address.setDefaultSuccess'))
    await loadAddresses()
  } catch {}
}

// ── 删除 ──────────────────────────────────────────
const handleDelete = async (addr: UserAddress) => {
  try {
    await ElMessageBox.confirm(
      t('address.deleteConfirmDesc', { name: addr.receiverName }),
      t('address.deleteConfirmTitle'),
      { type: 'warning', confirmButtonText: t('common.delete'), cancelButtonText: t('common.cancel') }
    )
    await deleteAddress(addr.id)
    ElMessage.success(t('address.deleteSuccess'))
    await loadAddresses()
  } catch {}
}

onMounted(loadAddresses)
</script>

<style scoped lang="scss">
.address-page {
  min-height: 100vh;
  background: var(--bg-primary);
  padding: 32px 0 64px;
}

.container {
  max-width: 900px;
  margin: 0 auto;
  padding: 0 24px;
}

/* ── Header ── */
.page-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 32px;

  .page-title {
    flex: 1;
    font-size: 24px;
    font-weight: 700;
    color: var(--text-primary);
    margin: 0;
  }
}

.back-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  color: var(--text-secondary);
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
  &:hover { background: var(--bg-secondary); }
}

.add-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 20px;
  background: var(--gradient-primary);
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: var(--shadow-primary);
  &:hover { transform: translateY(-2px); box-shadow: 0 8px 20px rgba(99,102,241,.4); }
}

/* ── Address Grid ── */
.address-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(360px, 1fr));
  gap: 20px;
}

.address-card {
  background: var(--bg-card);
  border: 2px solid var(--border-color);
  border-radius: 16px;
  padding: 20px;
  transition: all 0.25s;

  &:hover { border-color: var(--color-primary); box-shadow: 0 4px 20px rgba(99,102,241,.12); }

  &.is-default {
    border-color: var(--color-primary);
    background: linear-gradient(135deg, rgba(99,102,241,.04), var(--bg-card));
  }
}

.card-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}

.receiver-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.receiver-name {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-primary);
}

.receiver-phone {
  font-size: 14px;
  color: var(--text-secondary);
}

.default-tag {
  padding: 3px 10px;
  background: var(--gradient-primary);
  color: white;
  border-radius: 20px;
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.5px;
}

.card-address {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.6;
  margin-bottom: 16px;
  padding: 10px 12px;
  background: var(--bg-secondary);
  border-radius: 8px;
}

.card-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}

.action-btn {
  padding: 6px 14px;
  border-radius: 6px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.15s;
  border: 1px solid transparent;
  font-family: inherit;

  &.default-btn {
    background: transparent;
    border-color: var(--color-primary);
    color: var(--color-primary);
    &:hover { background: var(--color-primary); color: white; }
  }
  &.edit-btn {
    background: var(--bg-secondary);
    color: var(--text-secondary);
    &:hover { background: var(--bg-primary); }
  }
  &.delete-btn {
    background: transparent;
    border-color: #ef4444;
    color: #ef4444;
    &:hover { background: #ef4444; color: white; }
  }
}

/* ── Empty ── */
.empty-state {
  text-align: center;
  padding: 80px 0;

  .empty-icon { font-size: 64px; margin-bottom: 16px; }
  .empty-text { font-size: 16px; color: var(--text-muted); margin-bottom: 24px; }
}

.add-empty-btn {
  padding: 12px 32px;
  background: var(--gradient-primary);
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  box-shadow: var(--shadow-primary);
  &:hover { transform: translateY(-2px); }
}

/* ── Skeleton ── */
.skeleton-card { border: 2px solid transparent; }
.skeleton {
  background: linear-gradient(90deg, var(--bg-secondary) 25%, var(--bg-primary) 50%, var(--bg-secondary) 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: 6px;
}
@keyframes shimmer {
  0% { background-position: -200% 0; }
  100% { background-position: 200% 0; }
}

/* ── Modal ── */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,.5);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
}

.modal-card {
  background: var(--bg-card);
  border-radius: 20px;
  width: 100%;
  max-width: 580px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 24px 64px rgba(0,0,0,.25);
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24px 28px 0;

  h2 {
    font-size: 20px;
    font-weight: 700;
    color: var(--text-primary);
    margin: 0;
  }
}

.modal-close {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: var(--bg-secondary);
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  color: var(--text-muted);
  transition: all 0.15s;
  &:hover { background: var(--bg-primary); color: var(--text-primary); }
}

.modal-body {
  padding: 24px 28px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;

  &:has(.form-field:nth-child(3)) {
    grid-template-columns: 1fr 1fr 1fr;
  }
}

.form-field {
  display: flex;
  flex-direction: column;
  gap: 6px;

  &.full { grid-column: 1 / -1; }

  label {
    font-size: 13px;
    font-weight: 600;
    color: var(--text-secondary);

    .req { color: #ef4444; }
  }
}

.f-input, .f-select {
  padding: 10px 14px;
  background: var(--bg-secondary);
  border: 1.5px solid var(--border-color);
  border-radius: 10px;
  font-size: 14px;
  color: var(--text-primary);
  font-family: inherit;
  transition: all 0.15s;
  outline: none;

  &:focus { border-color: var(--color-primary); box-shadow: 0 0 0 3px rgba(99,102,241,.12); }
  &:disabled { opacity: 0.5; cursor: not-allowed; }
}

.f-select { cursor: pointer; }

.form-check {
  padding: 12px 16px;
  background: var(--bg-secondary);
  border-radius: 10px;
}

.check-label {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
}

.check-input {
  width: 18px;
  height: 18px;
  accent-color: var(--color-primary);
}

.check-text {
  font-size: 14px;
  color: var(--text-secondary);
}

.modal-footer {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  padding: 0 28px 24px;
}

.btn-cancel {
  padding: 10px 24px;
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: 10px;
  color: var(--text-secondary);
  font-size: 14px;
  cursor: pointer;
  font-family: inherit;
  transition: all 0.15s;
  &:hover { background: var(--bg-primary); }
}

.btn-save {
  padding: 10px 28px;
  background: var(--gradient-primary);
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  font-family: inherit;
  box-shadow: var(--shadow-primary);
  transition: all 0.2s;

  &:hover:not(:disabled) { transform: translateY(-1px); box-shadow: 0 8px 20px rgba(99,102,241,.4); }
  &:disabled { opacity: 0.6; cursor: not-allowed; transform: none; }
}
</style>
