import axios, { AxiosInstance, AxiosResponse, InternalAxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
import { useUserStore } from '@/stores/user'
import type { ApiResponse } from '@/types'

// 创建 axios 实例
const request: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
request.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const userStore = useUserStore()
    // 如果有 token，则添加到请求头
    if (userStore.token && config.headers) {
      config.headers.Authorization = `Bearer ${userStore.token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => {
    const { code, message, data } = response.data

    if (code === 200) {
      return data
    } else if (code === 401) {
      // 未登录，清除用户信息并跳转到登录页
      const userStore = useUserStore()
      userStore.clearUser()
      ElMessage.error(message || '登录已过期，请重新登录')
      router.push('/login')
      return Promise.reject(new Error(message || '未授权'))
    } else {
      ElMessage.error(message || '请求失败')
      return Promise.reject(new Error(message || '请求失败'))
    }
  },
  (error) => {
    if (error.response) {
      const { status, data } = error.response
      if (status === 401) {
        const userStore = useUserStore()
        userStore.clearUser()
        ElMessage.error('登录已过期，请重新登录')
        router.push('/login')
      } else if (status === 429) {
        ElMessage.error('请求过于频繁，请稍后再试')
      } else {
        ElMessage.error(data?.message || '请求失败')
      }
    } else if (error.request) {
      ElMessage.error('网络错误，请稍后再试')
    } else {
      ElMessage.error('请求失败')
    }
    return Promise.reject(error)
  }
)

export default request
