import request from '@/utils/request'
import type { UserAddress } from '@/types'

// 获取地址列表
export const getAddressList = (): Promise<UserAddress[]> =>
  request.get('/user/address')

// 获取默认地址
export const getDefaultAddress = (): Promise<UserAddress | null> =>
  request.get('/user/address/default')

// 新增地址
export const addAddress = (data: Partial<UserAddress>): Promise<UserAddress> =>
  request.post('/user/address', data)

// 修改地址
export const updateAddress = (id: number, data: Partial<UserAddress>): Promise<UserAddress> =>
  request.put(`/user/address/${id}`, data)

// 删除地址
export const deleteAddress = (id: number): Promise<void> =>
  request.delete(`/user/address/${id}`)

// 设为默认地址
export const setDefaultAddress = (id: number): Promise<void> =>
  request.put(`/user/address/${id}/default`)
