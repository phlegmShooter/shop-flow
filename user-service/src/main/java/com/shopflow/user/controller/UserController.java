package com.shopflow.user.controller;

import com.shopflow.common.Result;
import com.shopflow.common.dto.LoginDTO;
import com.shopflow.common.dto.PasswordUpdateDTO;
import com.shopflow.common.dto.RegisterDTO;
import com.shopflow.common.dto.UserUpdateDTO;
import com.shopflow.common.utils.JwtUtil;
import com.shopflow.user.entity.User;
import com.shopflow.user.entity.UserAddress;
import com.shopflow.user.service.UserAddressService;
import com.shopflow.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;

/**
 * 用户控制器
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/api/user")
@Tag(name = "用户管理", description = "用户注册、登录、信息管理等相关接口")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserAddressService userAddressService;

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${jwt.token-header:Authorization}")
    private String tokenHeader;

    @Value("${jwt.token-prefix:Bearer }")
    private String tokenPrefix;

    @Operation(summary = "用户注册", description = "新用户注册接口")
    @PostMapping("/register")
    public Result<Long> register(@Valid @RequestBody RegisterDTO registerDTO) {
        try {
            Long userId = userService.register(registerDTO);
            return Result.success("注册成功", userId);
        } catch (Exception e) {
            log.error("用户注册失败: {}", e.getMessage());
            return Result.error(400, e.getMessage());
        }
    }

    @Operation(summary = "用户登录", description = "用户登录接口，返回JWT Token")
    @PostMapping("/login")
    public Result<String> login(@Valid @RequestBody LoginDTO loginDTO) {
        try {
            String token = userService.login(loginDTO);
            return Result.success("登录成功", token);
        } catch (Exception e) {
            log.error("用户登录失败: {}", e.getMessage());
            return Result.error(400, e.getMessage());
        }
    }

    @Operation(summary = "退出登录", description = "用户退出登录，使Token失效")
    @PostMapping("/logout")
    public Result<String> logout(HttpServletRequest request) {
        try {
            String token = extractTokenFromRequest(request);
            if (token != null) {
                userService.logout(token);
            }
            return Result.success("退出登录成功");
        } catch (Exception e) {
            log.error("退出登录失败: {}", e.getMessage());
            return Result.error(500, "退出登录失败");
        }
    }

    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息")
    @GetMapping("/info")
    public Result<User> getCurrentUserInfo(HttpServletRequest request) {
        try {
            String token = extractTokenFromRequest(request);
            if (token == null || !jwtUtil.validateToken(token)) {
                return Result.error(401, "未登录或Token已过期");
            }

            Long userId = jwtUtil.getUserIdFromToken(token);
            User user = userService.getCurrentUserInfo(userId);
            return Result.success(user);
        } catch (Exception e) {
            log.error("获取用户信息失败: {}", e.getMessage());
            return Result.error(500, "获取用户信息失败");
        }
    }

    @Operation(summary = "更新用户信息", description = "更新当前登录用户的信息（邮箱、手机号、头像等）")
    @PutMapping("/info")
    public Result<String> updateUserInfo(
            HttpServletRequest request,
            @Valid @RequestBody UserUpdateDTO userUpdateDTO) {
        try {
            String token = extractTokenFromRequest(request);
            if (token == null || !jwtUtil.validateToken(token)) {
                return Result.error(401, "未登录或Token已过期");
            }

            Long userId = jwtUtil.getUserIdFromToken(token);
            userService.updateUserInfo(userId, userUpdateDTO);
            return Result.success("用户信息更新成功");
        } catch (Exception e) {
            log.error("更新用户信息失败: {}", e.getMessage());
            return Result.error(400, e.getMessage());
        }
    }

    @Operation(summary = "修改密码", description = "修改当前登录用户的密码")
    @PutMapping("/password")
    public Result<String> updatePassword(
            HttpServletRequest request,
            @Valid @RequestBody PasswordUpdateDTO passwordUpdateDTO) {
        try {
            String token = extractTokenFromRequest(request);
            if (token == null || !jwtUtil.validateToken(token)) {
                return Result.error(401, "未登录或Token已过期");
            }

            Long userId = jwtUtil.getUserIdFromToken(token);
            userService.updatePassword(userId, passwordUpdateDTO);
            return Result.success("密码修改成功");
        } catch (Exception e) {
            log.error("修改密码失败: {}", e.getMessage());
            return Result.error(400, e.getMessage());
        }
    }

    @Operation(summary = "验证Token", description = "验证Token是否有效")
    @GetMapping("/validate-token")
    public Result<Boolean> validateToken(HttpServletRequest request) {
        try {
            String token = extractTokenFromRequest(request);
            if (token == null) {
                return Result.success(false);
            }
            boolean isValid = jwtUtil.validateToken(token);
            return Result.success(isValid);
        } catch (Exception e) {
            log.error("验证Token失败: {}", e.getMessage());
            return Result.error(500, "验证Token失败");
        }
    }

    @Operation(summary = "检查用户名是否可用", description = "检查用户名是否已被注册")
    @GetMapping("/check-username")
    public Result<Boolean> checkUsernameAvailable(@RequestParam String username) {
        try {
            // 先校验格式
            if (username == null || username.isBlank()) {
                return Result.error(400, "用户名不能为空");
            }
            if (username.length() < 3 || username.length() > 50) {
                return Result.error(400, "用户名长度必须在3-50个字符之间");
            }
            if (!username.matches("^[\\u4e00-\\u9fa5a-zA-Z0-9_]+$")) {
                return Result.error(400, "用户名只能包含中文、字母、数字和下划线");
            }
            boolean exists = userService.isUsernameExists(username);
            return Result.success(!exists); // 可用返回true
        } catch (Exception e) {
            log.error("检查用户名失败: {}", e.getMessage());
            return Result.error(500, "检查用户名失败");
        }
    }

    @Operation(summary = "检查邮箱是否可用", description = "检查邮箱是否已被注册")
    @GetMapping("/check-email")
    public Result<Boolean> checkEmailAvailable(@RequestParam String email) {
        try {
            boolean exists = userService.isEmailExists(email);
            return Result.success(!exists); // 可用返回true
        } catch (Exception e) {
            log.error("检查邮箱失败: {}", e.getMessage());
            return Result.error(500, "检查邮箱失败");
        }
    }

    /**
     * 从请求中提取Token
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String header = request.getHeader(tokenHeader);
        if (header != null && header.startsWith(tokenPrefix)) {
            return header.substring(tokenPrefix.length());
        }
        return null;
    }

    // ================================================
    // 收货地址管理接口
    // userId 由 Gateway 解析 JWT 后据入请求头
    // ================================================

    @Operation(summary = "获取地址列表", description = "查询当前用户的所有收货地址")
    @GetMapping("/address")
    public Result<List<UserAddress>> listAddress(
            @RequestHeader("X-User-Id") Long userId) {
        return Result.success(userAddressService.listByUser(userId));
    }

    @Operation(summary = "获取默认地址", description = "获取当前用户的默认收货地址")
    @GetMapping("/address/default")
    public Result<UserAddress> getDefaultAddress(
            @RequestHeader("X-User-Id") Long userId) {
        return Result.success(userAddressService.getDefault(userId));
    }

    @Operation(summary = "新增地址", description = "新增收货地址")
    @PostMapping("/address")
    public Result<UserAddress> addAddress(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody UserAddress address) {
        try {
            return Result.success("地址保存成功", userAddressService.addAddress(userId, address));
        } catch (Exception e) {
            log.error("新增地址失败: userId={}", userId, e);
            return Result.error(400, e.getMessage());
        }
    }

    @Operation(summary = "修改地址", description = "修改指定地址信息")
    @PutMapping("/address/{id}")
    public Result<UserAddress> updateAddress(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id,
            @RequestBody UserAddress address) {
        try {
            return Result.success("地址更新成功", userAddressService.updateAddress(userId, id, address));
        } catch (Exception e) {
            log.error("修改地址失败: addressId={}", id, e);
            return Result.error(400, e.getMessage());
        }
    }

    @Operation(summary = "删除地址", description = "删除指定地址")
    @DeleteMapping("/address/{id}")
    public Result<String> deleteAddress(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id) {
        try {
            userAddressService.deleteAddress(userId, id);
            return Result.success("删除成功");
        } catch (Exception e) {
            log.error("删除地址失败: addressId={}", id, e);
            return Result.error(400, e.getMessage());
        }
    }

    @Operation(summary = "设为默认地址", description = "将指定地址设为默认收货地址")
    @PutMapping("/address/{id}/default")
    public Result<String> setDefaultAddress(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id) {
        try {
            userAddressService.setDefault(userId, id);
            return Result.success("默认地址设置成功");
        } catch (Exception e) {
            log.error("设置默认地址失败: addressId={}", id, e);
            return Result.error(400, e.getMessage());
        }
    }
}