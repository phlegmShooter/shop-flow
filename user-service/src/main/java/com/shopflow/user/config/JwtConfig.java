package com.shopflow.user.config;

import com.shopflow.common.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * JWT配置类
 */
@Configuration
public class JwtConfig {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration:604800}")
    private Long expiration;

    @Bean
    public JwtUtil jwtUtil() {
        JwtUtil jwtUtil = new JwtUtil();
        jwtUtil.setSecret(secret);
        jwtUtil.setExpiration(expiration);
        return jwtUtil;
    }
}
