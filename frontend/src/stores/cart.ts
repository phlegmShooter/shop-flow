import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { CartInfo } from '@/types'
import { getCart, addToCart as apiAddToCart, updateQuantity, removeFromCart, selectItem, selectAll } from '@/api/cart'

export const useCartStore = defineStore('cart', () => {
  // 购物车信息
  const cartInfo = ref<CartInfo | null>(null)
  // 加载状态
  const loading = ref(false)

  // 获取购物车
  const fetchCart = async () => {
    loading.value = true
    try {
      const data = await getCart()
      cartInfo.value = data
    } catch (error) {
      console.error('获取购物车失败', error)
    } finally {
      loading.value = false
    }
  }

  // 添加到购物车
  const addItem = async (productId: number, productName: string, price: number, quantity = 1, image?: string) => {
    try {
      await apiAddToCart({
        productId,
        productName,
        price,
        quantity,
        selected: true,
        image
      })
      await fetchCart()
      return true
    } catch (error) {
      console.error('添加购物车失败', error)
      return false
    }
  }

  // 更新数量
  const updateItemQuantity = async (productId: number, quantity: number) => {
    try {
      await updateQuantity(productId, quantity)
      await fetchCart()
    } catch (error) {
      console.error('更新数量失败', error)
    }
  }

  // 删除商品
  const removeItem = async (productId: number) => {
    try {
      await removeFromCart(productId)
      await fetchCart()
    } catch (error) {
      console.error('删除商品失败', error)
    }
  }

  // 勾选商品
  const toggleSelect = async (productId: number, selected: boolean) => {
    try {
      await selectItem(productId, selected)
      await fetchCart()
    } catch (error) {
      console.error('勾选商品失败', error)
    }
  }

  // 全选/取消全选
  const toggleSelectAll = async (selected: boolean) => {
    try {
      await selectAll(selected)
      await fetchCart()
    } catch (error) {
      console.error('全选操作失败', error)
    }
  }

  // 清空购物车（本地）
  const clearCart = () => {
    cartInfo.value = null
  }

  return {
    cartInfo,
    loading,
    fetchCart,
    addItem,
    updateItemQuantity,
    removeItem,
    toggleSelect,
    toggleSelectAll,
    clearCart
  }
})
