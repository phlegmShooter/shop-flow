import request from '@/utils/request'
import type { CartItem, CartInfo } from '@/types'

// 获取购物车
export const getCart = (): Promise<CartInfo> => {
  return request.get('/cart')
}

// 添加到购物车
export const addToCart = (data: {
  productId: number
  productName?: string
  price?: number
  quantity: number
  selected?: boolean
  image?: string
}): Promise<void> => {
  return request.post('/cart/add', data)
}

// 修改商品数量
export const updateQuantity = (productId: number, quantity: number): Promise<void> => {
  return request.put('/cart/quantity', null, { params: { productId, quantity } })
}

// 删除购物车商品
export const removeFromCart = (productId: number): Promise<void> => {
  return request.delete(`/cart/${productId}`)
}

// 勾选/取消勾选商品
export const selectItem = (productId: number, selected: boolean): Promise<void> => {
  return request.put('/cart/select', null, { params: { productId, selected } })
}

// 全选/取消全选
export const selectAll = (selected: boolean): Promise<void> => {
  return request.put('/cart/select-all', null, { params: { selected } })
}

// 清空购物车
export const clearCart = (): Promise<void> => {
  return request.delete('/cart/clear')
}

// 获取购物车商品总数
export const getCartCount = (): Promise<number> => {
  return request.get('/cart/count')
}

// 获取选中的购物车项
export const getSelectedItems = (): Promise<CartItem[]> => {
  return request.get('/cart/selected')
}

// 清空已选中的购物车商品（下单后调用）
export const clearSelected = (): Promise<void> => {
  return request.delete('/cart/clear-selected')
}
