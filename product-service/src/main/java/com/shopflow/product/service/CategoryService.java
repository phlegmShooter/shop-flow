package com.shopflow.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shopflow.product.entity.Category;

import java.util.List;

/**
 * 分类服务接口
 */
public interface CategoryService extends IService<Category> {

    /**
     * 获取分类树
     */
    List<Category> getCategoryTree();

    /**
     * 获取指定分类及其所有子孙分类的 ID 列表
     * 点父分类时可查出所有子分类下的商品
     */
    List<Long> getCategoryIds(Long categoryId);
}
