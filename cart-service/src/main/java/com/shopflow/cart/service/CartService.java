package com.shopflow.cart.service;

import com.shopflow.cart.dto.CartItemVO;
import com.shopflow.cart.dto.CartVO;
import com.shopflow.common.dto.CartItemDTO;

import java.util.List;

/**
 * 购物车服务接口
 */
public interface CartService {

    /**
     * 获取购物车
     */
    CartVO getCart(Long userId);

    /**
     * 添加商品到购物车
     */
    void addToCart(Long userId, CartItemDTO cartItemDTO);

    /**
     * 修改商品数量
     */
    void updateQuantity(Long userId, Long productId, Integer quantity);

    /**
     * 删除购物车商品
     */
    void removeFromCart(Long userId, Long productId);

    /**
     * 勾选/取消勾选商品
     */
    void selectItem(Long userId, Long productId, Boolean selected);

    /**
     * 全选/取消全选
     */
    void selectAll(Long userId, Boolean selected);

    /**
     * 清空购物车
     */
    void clearCart(Long userId);

    /**
     * 清空已选中商品（下单后调用）
     */
    void clearSelected(Long userId);

    /**
     * 获取购物车商品总数
     */
    Integer getCartCount(Long userId);

    /**
     * 获取选中的购物车项
     */
    List<CartItemVO> getSelectedItems(Long userId);
}
