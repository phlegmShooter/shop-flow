package com.shopflow.cart.controller;

import com.shopflow.cart.dto.CartItemVO;
import com.shopflow.cart.dto.CartVO;
import com.shopflow.cart.service.CartService;
import com.shopflow.common.Result;
import com.shopflow.common.dto.CartItemDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 购物车控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/cart")
@Tag(name = "购物车管理", description = "购物车相关接口")
public class CartController {

    @Autowired
    private CartService cartService;

    private Long getUserIdFromHeader(HttpServletRequest request) {
        String userIdStr = request.getHeader("X-User-Id");
        if (userIdStr == null) {
            throw new RuntimeException("未获取到用户信息");
        }
        return Long.parseLong(userIdStr);
    }

    @Operation(summary = "获取购物车", description = "获取当前用户的购物车")
    @GetMapping
    public Result<CartVO> getCart(HttpServletRequest request) {
        try {
            Long userId = getUserIdFromHeader(request);
            CartVO cart = cartService.getCart(userId);
            return Result.success(cart);
        } catch (Exception e) {
            log.error("获取购物车失败", e);
            return Result.error(500, "获取购物车失败");
        }
    }

    @Operation(summary = "添加商品到购物车", description = "添加商品到购物车")
    @PostMapping("/add")
    public Result<String> addToCart(
            HttpServletRequest request,
            @Valid @RequestBody CartItemDTO cartItemDTO) {
        try {
            Long userId = getUserIdFromHeader(request);
            cartService.addToCart(userId, cartItemDTO);
            return Result.success("添加成功");
        } catch (Exception e) {
            log.error("添加购物车失败", e);
            return Result.error(400, e.getMessage());
        }
    }

    @Operation(summary = "修改商品数量", description = "修改购物车中商品的数量")
    @PutMapping("/quantity")
    public Result<String> updateQuantity(
            HttpServletRequest request,
            @Parameter(description = "商品ID") @RequestParam Long productId,
            @Parameter(description = "数量") @RequestParam @Min(1) Integer quantity) {
        try {
            Long userId = getUserIdFromHeader(request);
            cartService.updateQuantity(userId, productId, quantity);
            return Result.success("更新成功");
        } catch (Exception e) {
            log.error("更新购物车数量失败", e);
            return Result.error(400, e.getMessage());
        }
    }

    @Operation(summary = "删除购物车商品", description = "从购物车中删除商品")
    @DeleteMapping("/{productId}")
    public Result<String> removeFromCart(
            HttpServletRequest request,
            @PathVariable Long productId) {
        try {
            Long userId = getUserIdFromHeader(request);
            cartService.removeFromCart(userId, productId);
            return Result.success("删除成功");
        } catch (Exception e) {
            log.error("删除购物车商品失败", e);
            return Result.error(400, e.getMessage());
        }
    }

    @Operation(summary = "勾选/取消勾选商品", description = "勾选或取消勾选购物车中的商品")
    @PutMapping("/select")
    public Result<String> selectItem(
            HttpServletRequest request,
            @Parameter(description = "商品ID") @RequestParam Long productId,
            @Parameter(description = "是否选中") @RequestParam Boolean selected) {
        try {
            Long userId = getUserIdFromHeader(request);
            cartService.selectItem(userId, productId, selected);
            return Result.success("操作成功");
        } catch (Exception e) {
            log.error("勾选商品失败", e);
            return Result.error(400, e.getMessage());
        }
    }

    @Operation(summary = "全选/取消全选", description = "全选或取消全选购物车中的商品")
    @PutMapping("/select-all")
    public Result<String> selectAll(
            HttpServletRequest request,
            @Parameter(description = "是否全选") @RequestParam Boolean selected) {
        try {
            Long userId = getUserIdFromHeader(request);
            cartService.selectAll(userId, selected);
            return Result.success("操作成功");
        } catch (Exception e) {
            log.error("全选操作失败", e);
            return Result.error(400, e.getMessage());
        }
    }

    @Operation(summary = "清空购物车", description = "清空当前用户的购物车")
    @DeleteMapping("/clear")
    public Result<String> clearCart(HttpServletRequest request) {
        try {
            Long userId = getUserIdFromHeader(request);
            cartService.clearCart(userId);
            return Result.success("清空成功");
        } catch (Exception e) {
            log.error("清空购物车失败", e);
            return Result.error(400, e.getMessage());
        }
    }

    @Operation(summary = "获取购物车商品总数", description = "获取购物车商品总数（用于角标显示）")
    @GetMapping("/count")
    public Result<Integer> getCartCount(HttpServletRequest request) {
        try {
            Long userId = getUserIdFromHeader(request);
            Integer count = cartService.getCartCount(userId);
            return Result.success(count);
        } catch (Exception e) {
            log.error("获取购物车数量失败", e);
            return Result.error(500, "获取购物车数量失败");
        }
    }

    @Operation(summary = "获取选中的购物车项", description = "内部服务调用，获取选中的购物车项")
    @GetMapping("/selected")
    public Result<List<CartItemVO>> getSelectedItems(HttpServletRequest request) {
        try {
            Long userId = getUserIdFromHeader(request);
            List<CartItemVO> items = cartService.getSelectedItems(userId);
            return Result.success(items);
        } catch (Exception e) {
            log.error("获取选中购物车项失败", e);
            return Result.error(500, "获取选中购物车项失败");
        }
    }

    @Operation(summary = "清空已选中商品", description = "内部服务调用，下单后清空已选中商品")
    @DeleteMapping("/clear-selected")
    public Result<String> clearSelected(HttpServletRequest request) {
        try {
            Long userId = getUserIdFromHeader(request);
            cartService.clearSelected(userId);
            return Result.success("清空成功");
        } catch (Exception e) {
            log.error("清空已选中商品失败", e);
            return Result.error(400, e.getMessage());
        }
    }
}
