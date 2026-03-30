import request from '@/utils/request'
import type { Product, Category } from '@/types'

// 获取商品列表
export const getProductList = (params: {
  current?: number
  size?: number
  categoryId?: number
  keyword?: string
}): Promise<{ records: Product[]; total: number; size: number; current: number }> => {
  return request.get('/product/list', { params })
}

// 获取商品详情
export const getProductDetail = (id: number): Promise<Product> => {
  return request.get(`/product/${id}`)
}

// 获取分类树
export const getCategoryTree = (): Promise<Category[]> => {
  return request.get('/product/category/tree')
}

// 批量获取商品信息
export const getProductBatch = (ids: number[]): Promise<Product[]> => {
  return request.get('/product/batch', { params: { ids: ids.join(',') } })
}
