<template>
  <el-config-provider :locale="locale">
    <router-view v-if="isGuestPage" />
    <Layout v-else>
      <router-view />
    </Layout>
  </el-config-provider>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import en from 'element-plus/es/locale/lang/en'
import Layout from '@/components/Layout.vue'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const userStore = useUserStore()
const { locale: i18nLocale } = useI18n()

// 映射 Element Plus 的语言包
const locale = computed(() => (i18nLocale.value === 'en' ? en : zhCn))

// 判断是否是访客页面（登录、注册页面不需要 Layout）
const isGuestPage = computed(() => {
  return !!route.meta.guest
})

// 页面加载时，如果 localStorage 有 token，自动恢复用户信息
onMounted(() => {
  userStore.initUser()
})
</script>

<style scoped>
</style>

