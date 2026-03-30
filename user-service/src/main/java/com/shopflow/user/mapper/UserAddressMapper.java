package com.shopflow.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shopflow.user.entity.UserAddress;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 用户地址 Mapper
 */
@Mapper
public interface UserAddressMapper extends BaseMapper<UserAddress> {

    /**
     * 清除指定用户的所有默认地址标记
     * 设置新默认地址前调用，保证只有一个默认
     */
    @Update("UPDATE t_user_address SET is_default = 0 WHERE user_id = #{userId}")
    int clearDefault(@Param("userId") Long userId);
}
