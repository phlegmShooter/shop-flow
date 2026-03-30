import request from '@/utils/request'
import type { Order, CreateOrderParams, CartItem } from '@/types'

// 创建订单
export const createOrder = (data: CreateOrderParams): Promise<string> => {
  return request.post('/order/create', data)
}

// 获取订单列表
export const getOrderList = (params: {
  current?: number
  size?: number
  status?: number
}): Promise<{ records: Order[]; total: number; size: number; current: number }> => {
  return request.get('/order/list', { params })
}

// 获取订单详情
export const getOrderDetail = (orderNo: string): Promise<Order> => {
  return request.get(`/order/${orderNo}`)
}

// 模拟支付
export const payOrder = (orderNo: string): Promise<void> => {
  return request.post(`/order/${orderNo}/pay`)
}

// 取消订单
export const cancelOrder = (orderNo: string): Promise<void> => {
  return request.post(`/order/${orderNo}/cancel`)
}

// 确认收货
export const confirmOrder = (orderNo: string): Promise<void> => {
  return request.post(`/order/${orderNo}/confirm`)
}

// 获取选中的购物车商品（用于结算）
export const getSelectedItems = (): Promise<CartItem[]> => {
  return request.get('/cart/selected')
}
