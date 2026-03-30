package com.shopflow.user.service;

import com.shopflow.common.dto.LoginDTO;
import com.shopflow.common.dto.PasswordUpdateDTO;
import com.shopflow.common.dto.RegisterDTO;
import com.shopflow.common.dto.UserUpdateDTO;
import com.shopflow.user.entity.User;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 用户注册
     */
    Long register(RegisterDTO registerDTO);

    /**
     * 用户登录
     */
    String login(LoginDTO loginDTO);

    /**
     * 用户退出登录
     */
    void logout(String token);

    /**
     * 获取当前用户信息
     */
    User getCurrentUserInfo(Long userId);

    /**
     * 更新用户信息
     */
    void updateUserInfo(Long userId, UserUpdateDTO userUpdateDTO);

    /**
     * 修改密码
     */
    void updatePassword(Long userId, PasswordUpdateDTO passwordUpdateDTO);

    /**
     * 根据用户ID查询用户
     */
    User getUserById(Long userId);

    /**
     * 根据用户名查询用户
     */
    User getUserByUsername(String username);

    /**
     * 检查用户名是否存在
     */
    boolean isUsernameExists(String username);

    /**
     * 检查邮箱是否存在
     */
    boolean isEmailExists(String email);
}