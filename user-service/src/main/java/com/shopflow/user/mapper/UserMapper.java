package com.shopflow.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shopflow.user.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 用户Mapper接口
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据用户名查询用户
     */
    @Select("SELECT * FROM t_user WHERE username = #{username} AND status = 1")
    User selectByUsername(String username);

    /**
     * 根据邮箱查询用户
     */
    @Select("SELECT * FROM t_user WHERE email = #{email} AND status = 1")
    User selectByEmail(String email);

    /**
     * 检查用户名是否存在
     */
    @Select("SELECT COUNT(*) FROM t_user WHERE username = #{username}")
    int countByUsername(String username);

    /**
     * 检查邮箱是否存在
     */
    @Select("SELECT COUNT(*) FROM t_user WHERE email = #{email}")
    int countByEmail(String email);
}