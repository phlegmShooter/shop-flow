import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { UserInfo } from '@/types'
import { getUserInfo } from '@/api/user'

export const useUserStore = defineStore('user', () => {
  // 用户信息
  const userInfo = ref<UserInfo | null>(null)
  // Token
  const token = ref<string>(localStorage.getItem('token') || '')

  // 是否已登录
  const isLoggedIn = computed(() => !!token.value && !!userInfo.value)

  // 设置用户信息
  const setUserInfo = (info: UserInfo) => {
    userInfo.value = info
  }

  // 设置 Token
  const setToken = (t: string) => {
    token.value = t
    localStorage.setItem('token', t)
  }

  // 清除用户信息
  const clearUser = () => {
    userInfo.value = null
    token.value = ''
    localStorage.removeItem('token')
  }

  // 初始化用户信息：页面刷新时，若 localStorage 有 token，自动拉取用户信息
  const initUser = async () => {
    if (token.value && !userInfo.value) {
      try {
        const info = await getUserInfo()
        userInfo.value = info
      } catch (e) {
        // Token 已过期或无效，清除登录态
        clearUser()
      }
    }
  }

  return {
    userInfo,
    token,
    isLoggedIn,
    setUserInfo,
    setToken,
    clearUser,
    initUser
  }
})
