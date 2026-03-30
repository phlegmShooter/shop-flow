// 通用响应类型
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
  timestamp: number
}

// 用户信息类型
export interface UserInfo {
  id: number
  username: string
  email: string
  phone?: string
  avatar?: string
  status: number
}

// 登录请求参数
export interface LoginParams {
  username: string
  password: string
}

// 注册请求参数
export interface RegisterParams {
  username: string
  password: string
  email: string
}

// 商品类型
export interface Product {
  id: number
  categoryId: number
  name: string
  description?: string
  price: number
  stock: number
  images?: string
  status: number
  createdAt: string
  updatedAt?: string
}

// 商品分类类型
export interface Category {
  id: number
  name: string
  parentId: number
  sort: number
  createdAt: string
  children?: Category[]
}

// 购物车项类型
export interface CartItem {
  productId: number
  productName: string
  price: number
  quantity: number
  selected: boolean
  image?: string
  subtotal?: number
}

// 购物车类型
export interface CartInfo {
  items: CartItem[]
  selectedCount: number
  totalCount: number
  selectedAmount: number
}

// 订单项类型
export interface OrderItem {
  id: number
  orderId: number
  productId: number
  productName: string
  price: number
  quantity: number
  subtotal: number
  image?: string   // 商品图片URL快照
}

// 订单类型
export interface Order {
  id: number
  orderNo: string
  userId: number
  totalAmount: number
  status: number
  address: string
  remark?: string
  paidAt?: string
  createdAt: string
  updatedAt: string
  items?: OrderItem[]
}

// 通知类型
export interface Notification {
  id: number
  userId: number
  type: string
  title: string
  content: string
  isRead: number
  createdAt: string
}

// 创建订单参数
export interface CreateOrderParams {
  address: string
  remark?: string
  productIds: number[]
}

// 用户收货地址
export interface UserAddress {
  id: number
  userId?: number
  receiverName: string
  phone: string
  province: string
  city: string
  district?: string
  detail: string
  isDefault: number    // 1=默认 0=非默认
  createdAt?: string
  updatedAt?: string
}

// 省市区数据节点
export interface RegionNode {
  name: string
  children?: RegionNode[]
}
