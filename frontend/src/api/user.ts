import request from '@/utils/request'
import type { LoginParams, RegisterParams, UserInfo } from '@/types'

// 用户登录
export const login = (data: LoginParams): Promise<string> => {
  return request.post('/user/login', data)
}

// 用户注册
export const register = (data: RegisterParams): Promise<number> => {
  return request.post('/user/register', data)
}

// 用户退出
export const logout = (): Promise<void> => {
  return request.post('/user/logout')
}

// 获取当前用户信息
export const getUserInfo = (): Promise<UserInfo> => {
  return request.get('/user/info')
}

// 更新用户信息
export const updateUserInfo = (data: Partial<UserInfo>): Promise<void> => {
  return request.put('/user/info', data)
}

// 修改密码
export const updatePassword = (oldPassword: string, newPassword: string): Promise<void> => {
  return request.put('/user/password', { oldPassword, newPassword })
}

// 检查用户名是否可用
export const checkUsername = (username: string): Promise<boolean> => {
  return request.get('/user/check-username', { params: { username } })
}

// 检查邮箱是否可用
export const checkEmail = (email: string): Promise<boolean> => {
  return request.get('/user/check-email', { params: { email } })
}
