package com.shopflow.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopflow.common.constant.RedisKeyConstant;
import com.shopflow.product.entity.Category;
import com.shopflow.product.mapper.CategoryMapper;
import com.shopflow.product.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 分类服务实现类
 */
@Slf4j
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public List<Category> getCategoryTree() {
        // 先从缓存获取
        String cacheKey = RedisKeyConstant.PRODUCT_CATEGORY_TREE;
        String cached = stringRedisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            try {
                return objectMapper.readValue(cached, new TypeReference<List<Category>>() {});
            } catch (JsonProcessingException e) {
                log.warn("解析分类树缓存失败", e);
            }
        }

        // 从数据库查询所有分类
        List<Category> allCategories = list(new LambdaQueryWrapper<Category>()
                .orderByAsc(Category::getSort)
                .orderByAsc(Category::getId));

        // 构建树形结构
        List<Category> tree = buildTree(allCategories);

        // 写入缓存
        try {
            stringRedisTemplate.opsForValue().set(
                    cacheKey,
                    objectMapper.writeValueAsString(tree),
                    RedisKeyConstant.TTL.PRODUCT_CATEGORY_TREE,
                    TimeUnit.SECONDS
            );
        } catch (JsonProcessingException e) {
            log.warn("写入分类树缓存失败", e);
        }

        return tree;
    }

    /**
     * 使用 MySQL WITH RECURSIVE CTE，数据库侧递归查询分类及其所有子孙分类的 ID
     * 不管分类层级多深，一条 SQL 搞定。
     */
    @Override
    public List<Long> getCategoryIds(Long categoryId) {
        return getBaseMapper().selectSubCategoryIds(categoryId);
    }

    /**
     * 构建树形结构
     */
    private List<Category> buildTree(List<Category> allCategories) {
        // 找出顶级分类
        List<Category> rootCategories = allCategories.stream()
                .filter(c -> c.getParentId() == null || c.getParentId() == 0)
                .collect(Collectors.toList());

        // 递归设置子分类
        for (Category root : rootCategories) {
            root.setChildren(getChildren(root, allCategories));
        }

        return rootCategories;
    }

    /**
     * 递归获取子分类
     */
    private List<Category> getChildren(Category parent, List<Category> allCategories) {
        List<Category> children = new ArrayList<>();
        for (Category category : allCategories) {
            if (parent.getId().equals(category.getParentId())) {
                category.setChildren(getChildren(category, allCategories));
                children.add(category);
            }
        }
        return children;
    }
}
