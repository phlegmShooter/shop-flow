package com.shopflow.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopflow.common.constant.ErrorCode;
import com.shopflow.common.constant.RedisKeyConstant;
import com.shopflow.common.exception.BaseException;
import com.shopflow.product.entity.Product;
import com.shopflow.product.mapper.ProductMapper;
import com.shopflow.product.service.CategoryService;
import com.shopflow.product.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 商品服务实现类
 */
@Slf4j
@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    // 防超卖 Lua脚本
    private static final String DEDUCT_STOCK_SCRIPT =
            "local stock = tonumber(redis.call('GET', KEYS[1])) " +
            "if stock == nil then " +
            "    return -1 " +  // 缓存不存在
            "end " +
            "if stock < tonumber(ARGV[1]) then " +
            "    return 0 " +   // 库存不足
            "end " +
            "redis.call('DECRBY', KEYS[1], ARGV[1]) " +
            "return 1";       // 扣减成功

    @Override
    public IPage<Product> getProductList(Page<Product> page, Long categoryId, String keyword) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, 1); // 只查询上架商品

        if (categoryId != null) {
            // 获取该分类及所有子孙分类 ID，实现点父分类能查到子分类下的商品
            List<Long> categoryIds = categoryService.getCategoryIds(categoryId);
            wrapper.in(Product::getCategoryId, categoryIds);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Product::getName, keyword);
        }
        wrapper.orderByDesc(Product::getCreatedAt);

        return productMapper.selectPage(page, wrapper);
    }

    @Override
    public Product getProductDetail(Long productId) {
        // 先从缓存获取
        String cacheKey = RedisKeyConstant.buildProductDetailKey(productId);
        String cached = stringRedisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            try {
                return objectMapper.readValue(cached, Product.class);
            } catch (JsonProcessingException e) {
                log.warn("解析商品缓存失败: productId={}", productId, e);
            }
        }

        // 从数据库查询
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BaseException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        // 写入缓存
        try {
            stringRedisTemplate.opsForValue().set(
                    cacheKey,
                    objectMapper.writeValueAsString(product),
                    RedisKeyConstant.TTL.PRODUCT_DETAIL,
                    TimeUnit.SECONDS
            );
        } catch (JsonProcessingException e) {
            log.warn("写入商品缓存失败: productId={}", productId, e);
        }

        // 预热库存到Redis
        warmUpStock(productId);

        return product;
    }

    @Override
    public boolean deductStock(Long productId, Integer quantity) {
        String stockKey = RedisKeyConstant.buildProductStockKey(productId);

        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(DEDUCT_STOCK_SCRIPT);
        redisScript.setResultType(Long.class);

        Long result = stringRedisTemplate.execute(
                redisScript,
                Collections.singletonList(stockKey),
                String.valueOf(quantity)
        );

        if (result == null || result == -1) {
            // 缓存不存在，先预热库存
            warmUpStock(productId);
            // 再次尝试扣减
            result = stringRedisTemplate.execute(
                    redisScript,
                    Collections.singletonList(stockKey),
                    String.valueOf(quantity)
            );
        }

        if (result == null || result == 0) {
            log.warn("库存不足: productId={}, quantity={}", productId, quantity);
            throw new BaseException(ErrorCode.STOCK_NOT_ENOUGH);
        }

        if (result == 1) {
            // 同时更新数据库
            int rows = productMapper.deductStock(productId, quantity);
            if (rows == 0) {
                // 数据库扣减失败，回滚Redis
                stringRedisTemplate.opsForValue().increment(stockKey, quantity);
                throw new BaseException(ErrorCode.STOCK_NOT_ENOUGH);
            }
            log.info("库存扣减成功: productId={}, quantity={}", productId, quantity);
            return true;
        }

        return false;
    }

    @Override
    public boolean restoreStock(Long productId, Integer quantity) {
        String stockKey = RedisKeyConstant.buildProductStockKey(productId);

        // Redis增加库存
        stringRedisTemplate.opsForValue().increment(stockKey, quantity);

        // 数据库增加库存
        productMapper.restoreStock(productId, quantity);

        log.info("库存归还成功: productId={}, quantity={}", productId, quantity);
        return true;
    }

    @Override
    public void warmUpStock(Long productId) {
        Product product = productMapper.selectById(productId);
        if (product != null) {
            String stockKey = RedisKeyConstant.buildProductStockKey(productId);
            stringRedisTemplate.opsForValue().set(stockKey, String.valueOf(product.getStock()));
            log.debug("库存预热成功: productId={}, stock={}", productId, product.getStock());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createProduct(Product product) {
        product.setStatus(1); // 默认上架
        productMapper.insert(product);
        log.info("商品创建成功: productId={}, name={}", product.getId(), product.getName());
        return product.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateProduct(Product product) {
        // 删除缓存
        String cacheKey = RedisKeyConstant.buildProductDetailKey(product.getId());
        stringRedisTemplate.delete(cacheKey);

        int rows = productMapper.updateById(product);
        log.info("商品更新成功: productId={}", product.getId());

        // 更新库存缓存
        if (product.getStock() != null) {
            warmUpStock(product.getId());
        }

        return rows > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteProduct(Long productId) {
        // 删除缓存
        String cacheKey = RedisKeyConstant.buildProductDetailKey(productId);
        stringRedisTemplate.delete(cacheKey);

        // 删除库存缓存
        String stockKey = RedisKeyConstant.buildProductStockKey(productId);
        stringRedisTemplate.delete(stockKey);

        int rows = productMapper.deleteById(productId);
        log.info("商品删除成功: productId={}", productId);
        return rows > 0;
    }
}
