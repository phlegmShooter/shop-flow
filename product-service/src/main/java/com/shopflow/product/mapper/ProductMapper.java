package com.shopflow.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shopflow.product.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 商品Mapper
 */
@Mapper
public interface ProductMapper extends BaseMapper<Product> {

    /**
     * 扣减库存（乐观锁）
     */
    @Update("UPDATE t_product SET stock = stock - #{quantity}, updated_at = NOW() WHERE id = #{productId} AND stock >= #{quantity}")
    int deductStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    /**
     * 归还库存
     */
    @Update("UPDATE t_product SET stock = stock + #{quantity}, updated_at = NOW() WHERE id = #{productId}")
    int restoreStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);
}
