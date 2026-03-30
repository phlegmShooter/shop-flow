package com.shopflow.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shopflow.common.constant.ErrorCode;
import com.shopflow.common.constant.RedisKeyConstant;
import com.shopflow.common.dto.LoginDTO;
import com.shopflow.common.dto.PasswordUpdateDTO;
import com.shopflow.common.dto.RegisterDTO;
import com.shopflow.common.dto.UserUpdateDTO;
import com.shopflow.common.exception.BaseException;
import com.shopflow.common.utils.JwtUtil;
import com.shopflow.user.entity.User;
import com.shopflow.user.mapper.UserMapper;
import com.shopflow.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

/**
 * 用户服务实现类
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long register(RegisterDTO registerDTO) {
        // 1. 校验用户名是否已存在
        if (isUsernameExists(registerDTO.getUsername())) {
            throw new BaseException(ErrorCode.USERNAME_EXISTS);
        }

        // 2. 校验邮箱是否已存在
        if (isEmailExists(registerDTO.getEmail())) {
            throw new BaseException(ErrorCode.EMAIL_EXISTS);
        }

        // 3. 创建用户实体
        User user = new User();
        BeanUtils.copyProperties(registerDTO, user);

        // 4. 加密密码
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        user.setStatus(1); // 正常状态

        // 5. 保存到数据库
        userMapper.insert(user);
        log.info("用户注册成功: username={}, userId={}", user.getUsername(), user.getId());

        return user.getId();
    }

    @Override
    public String login(LoginDTO loginDTO) {
        // 1. 根据用户名查询用户
        User user = getUserByUsername(loginDTO.getUsername());
        if (user == null) {
            throw new BaseException(ErrorCode.USER_NOT_FOUND, "用户名或密码错误");
        }

        // 2. 检查用户状态
        if (user.getStatus() == 0) {
            throw new BaseException(ErrorCode.USER_DISABLED);
        }

        // 3. 验证密码
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new BaseException(ErrorCode.PASSWORD_ERROR);
        }

        // 4. 生成JWT Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

        // 5. 将Token存入Redis（支持踢人下线）
        String redisKey = RedisKeyConstant.buildUserTokenKey(user.getId());
        redisTemplate.opsForValue().set(redisKey, token, RedisKeyConstant.TTL.USER_TOKEN, TimeUnit.SECONDS);

        log.info("用户登录成功: username={}, userId={}", user.getUsername(), user.getId());
        return token;
    }

    @Override
    public void logout(String token) {
        try {
            // 1. 从Token中获取用户ID
            Long userId = jwtUtil.getUserIdFromToken(token);

            // 2. 从Redis中删除Token
            String redisKey = RedisKeyConstant.buildUserTokenKey(userId);
            redisTemplate.delete(redisKey);

            log.info("用户退出登录: userId={}", userId);
        } catch (Exception e) {
            log.warn("退出登录时发生异常: {}", e.getMessage());
        }
    }

    @Override
    public User getCurrentUserInfo(Long userId) {
        User user = getUserById(userId);
        if (user == null) {
            throw new BaseException(ErrorCode.USER_NOT_FOUND);
        }

        // 返回用户信息（敏感信息如密码需要清除）
        user.setPassword(null);
        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserInfo(Long userId, UserUpdateDTO userUpdateDTO) {
        User user = getUserById(userId);
        if (user == null) {
            throw new BaseException(ErrorCode.USER_NOT_FOUND);
        }

        // 检查邮箱是否被其他用户使用
        if (StringUtils.hasText(userUpdateDTO.getEmail()) &&
            !userUpdateDTO.getEmail().equals(user.getEmail()) &&
            isEmailExists(userUpdateDTO.getEmail())) {
            throw new BaseException(ErrorCode.EMAIL_EXISTS);
        }

        // 更新用户信息
        if (StringUtils.hasText(userUpdateDTO.getEmail())) {
            user.setEmail(userUpdateDTO.getEmail());
        }
        if (StringUtils.hasText(userUpdateDTO.getPhone())) {
            user.setPhone(userUpdateDTO.getPhone());
        }
        if (StringUtils.hasText(userUpdateDTO.getAvatar())) {
            user.setAvatar(userUpdateDTO.getAvatar());
        }

        userMapper.updateById(user);
        log.info("用户信息更新成功: userId={}", userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePassword(Long userId, PasswordUpdateDTO passwordUpdateDTO) {
        User user = getUserById(userId);
        if (user == null) {
            throw new BaseException(ErrorCode.USER_NOT_FOUND);
        }

        // 验证原密码
        if (!passwordEncoder.matches(passwordUpdateDTO.getOldPassword(), user.getPassword())) {
            throw new BaseException(ErrorCode.PASSWORD_ERROR, "原密码错误");
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(passwordUpdateDTO.getNewPassword()));
        userMapper.updateById(user);

        // 使当前用户的Token失效（强制重新登录）
        String redisKey = RedisKeyConstant.buildUserTokenKey(userId);
        redisTemplate.delete(redisKey);

        log.info("用户密码修改成功: userId={}", userId);
    }

    @Override
    public User getUserById(Long userId) {
        return userMapper.selectById(userId);
    }

    @Override
    public User getUserByUsername(String username) {
        return userMapper.selectByUsername(username);
    }

    @Override
    public boolean isUsernameExists(String username) {
        return userMapper.countByUsername(username) > 0;
    }

    @Override
    public boolean isEmailExists(String email) {
        return userMapper.countByEmail(email) > 0;
    }
}