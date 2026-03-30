package com.shopflow.product.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shopflow.common.Result;
import com.shopflow.product.entity.Category;
import com.shopflow.product.entity.Product;
import com.shopflow.product.service.CategoryService;
import com.shopflow.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/product")
@Tag(name = "商品管理", description = "商品和分类管理接口")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @Operation(summary = "获取商品列表", description = "分页查询商品列表，支持按分类和关键词筛选")
    @GetMapping("/list")
    public Result<IPage<Product>> getProductList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "分类ID") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword) {
        Page<Product> page = new Page<>(current, size);
        IPage<Product> result = productService.getProductList(page, categoryId, keyword);
        return Result.success(result);
    }

    @Operation(summary = "获取商品详情", description = "根据商品ID获取商品详情")
    @GetMapping("/{id}")
    public Result<Product> getProductDetail(@PathVariable Long id) {
        try {
            Product product = productService.getProductDetail(id);
            return Result.success(product);
        } catch (Exception e) {
            log.error("获取商品详情失败: id={}", id, e);
            return Result.error(404, "商品不存在");
        }
    }

    @Operation(summary = "获取分类树", description = "获取商品分类树形结构")
    @GetMapping("/category/tree")
    public Result<List<Category>> getCategoryTree() {
        List<Category> tree = categoryService.getCategoryTree();
        return Result.success(tree);
    }

    @Operation(summary = "创建商品", description = "管理员创建新商品")
    @PostMapping
    public Result<Long> createProduct(@Valid @RequestBody Product product) {
        try {
            Long productId = productService.createProduct(product);
            return Result.success("商品创建成功", productId);
        } catch (Exception e) {
            log.error("创建商品失败", e);
            return Result.error(400, e.getMessage());
        }
    }

    @Operation(summary = "更新商品", description = "管理员更新商品信息")
    @PutMapping("/{id}")
    public Result<String> updateProduct(@PathVariable Long id, @Valid @RequestBody Product product) {
        try {
            product.setId(id);
            boolean success = productService.updateProduct(product);
            if (success) {
                return Result.success("商品更新成功");
            } else {
                return Result.error(404, "商品不存在");
            }
        } catch (Exception e) {
            log.error("更新商品失败: id={}", id, e);
            return Result.error(400, e.getMessage());
        }
    }

    @Operation(summary = "删除商品", description = "管理员删除商品")
    @DeleteMapping("/{id}")
    public Result<String> deleteProduct(@PathVariable Long id) {
        try {
            boolean success = productService.deleteProduct(id);
            if (success) {
                return Result.success("商品删除成功");
            } else {
                return Result.error(404, "商品不存在");
            }
        } catch (Exception e) {
            log.error("删除商品失败: id={}", id, e);
            return Result.error(400, e.getMessage());
        }
    }

    @Operation(summary = "批量获取商品信息", description = "内部服务调用，根据商品ID列表获取商品信息")
    @GetMapping("/batch")
    public Result<List<Product>> getProductBatch(@RequestParam List<Long> ids) {
        List<Product> products = productService.listByIds(ids);
        return Result.success(products);
    }

    @Operation(summary = "扣减库存", description = "内部服务调用，扣减商品库存")
    @PostMapping("/{id}/deduct-stock")
    public Result<Boolean> deductStock(
            @PathVariable Long id,
            @RequestParam Integer quantity) {
        try {
            boolean success = productService.deductStock(id, quantity);
            return Result.success(success);
        } catch (Exception e) {
            log.error("扣减库存失败: id={}, quantity={}", id, quantity, e);
            return Result.error(400, e.getMessage());
        }
    }

    @Operation(summary = "归还库存", description = "内部服务调用，归还商品库存")
    @PostMapping("/{id}/restore-stock")
    public Result<Boolean> restoreStock(
            @PathVariable Long id,
            @RequestParam Integer quantity) {
        try {
            boolean success = productService.restoreStock(id, quantity);
            return Result.success(success);
        } catch (Exception e) {
            log.error("归还库存失败: id={}, quantity={}", id, quantity, e);
            return Result.error(400, e.getMessage());
        }
    }
}
