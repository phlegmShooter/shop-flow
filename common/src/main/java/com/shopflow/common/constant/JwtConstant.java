package com.shopflow.common.constant;

/**
 * JWT常量
 */
public class JwtConstant {

    // Token请求头
    public static final String TOKEN_HEADER = "Authorization";

    // Token前缀
    public static final String TOKEN_PREFIX = "Bearer ";

    // Token过期时间（7天，单位秒）
    public static final Long EXPIRATION = 7 * 24 * 60 * 60L;

    // Redis中Token的key前缀
    public static final String REDIS_TOKEN_KEY_PREFIX = "token:user:";

    // JWT密钥（实际使用时应从配置文件中读取）
    public static final String SECRET = "shopflow-secret-key-2026-03-25-electronic-commerce";
}