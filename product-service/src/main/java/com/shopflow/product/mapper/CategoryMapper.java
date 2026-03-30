package com.shopflow.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shopflow.product.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 分类Mapper
 */
@Mapper
public interface CategoryMapper extends BaseMapper<Category> {

    /**
     * MySQL WITH RECURSIVE 递归查询：获取指定分类及所有子孙分类的 ID
     * 不管层级多深都能查到，数据库侧完成，无需 Java 内存递归
     */
    @Select("""
            WITH RECURSIVE category_tree AS (
                SELECT id FROM t_category WHERE id = #{categoryId}
                UNION ALL
                SELECT c.id
                FROM t_category c
                INNER JOIN category_tree ct ON c.parent_id = ct.id
            )
            SELECT id FROM category_tree
            """)
    List<Long> selectSubCategoryIds(@Param("categoryId") Long categoryId);
}
