package com.shopflow.product.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.shopflow.product.entity.Product;

/**
 * 商品服务接口
 */
public interface ProductService extends IService<Product> {

    /**
     * 分页查询商品列表
     */
    IPage<Product> getProductList(Page<Product> page, Long categoryId, String keyword);

    /**
     * 获取商品详情
     */
    Product getProductDetail(Long productId);

    /**
     * 扣减库存（使用Redis Lua脚本防超卖）
     */
    boolean deductStock(Long productId, Integer quantity);

    /**
     * 归还库存
     */
    boolean restoreStock(Long productId, Integer quantity);

    /**
     * 预热库存到Redis
     */
    void warmUpStock(Long productId);

    /**
     * 创建商品
     */
    Long createProduct(Product product);

    /**
     * 更新商品
     */
    boolean updateProduct(Product product);

    /**
     * 删除商品
     */
    boolean deleteProduct(Long productId);
}
