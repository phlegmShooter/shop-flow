package com.shopflow.gateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopflow.common.Result;
import com.shopflow.common.constant.RedisKeyConstant;
import com.shopflow.common.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * JWT鉴权过滤器
 */
@Slf4j
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private final PathMatcher pathMatcher = new AntPathMatcher();

    // 白名单路径（无需Token）
    private static final List<String> WHITE_LIST = Arrays.asList(
            "/api/user/register",
            "/api/user/login",
            "/api/user/check-username",
            "/api/user/check-email",
            "/api/product/list",
            "/api/product/category/tree",
            "/api/product/\\d+",
            "/actuator/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/webjars/**"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // 检查是否在白名单中
        if (isWhiteListPath(path)) {
            log.debug("白名单路径，跳过鉴权: {}", path);
            return chain.filter(exchange);
        }

        // 获取Token
        String token = extractToken(request);
        if (token == null) {
            log.warn("请求缺少Token: {}", path);
            return unauthorized(exchange, "未登录或Token已过期");
        }

        try {
            // 验证Token
            if (!jwtUtil.validateToken(token)) {
                log.warn("Token验证失败: {}", path);
                return unauthorized(exchange, "Token无效或已过期");
            }

            // 从Token中获取用户ID
            Claims claims = jwtUtil.getClaimsFromToken(token);
            Long userId = claims.get("userId", Long.class);
            String username = claims.get("username", String.class);

            // 验证Redis中是否存在该Token（支持踢人下线）
            String redisKey = RedisKeyConstant.buildUserTokenKey(userId);
            String redisToken = stringRedisTemplate.opsForValue().get(redisKey);
            if (redisToken == null || !redisToken.equals(token)) {
                log.warn("Token已失效（用户已在其他地方登录）: userId={}", userId);
                return unauthorized(exchange, "Token已失效，请重新登录");
            }

            // 将用户信息添加到请求头，传递给下游服务
            ServerHttpRequest modifiedRequest = request.mutate()
                    .header("X-User-Id", String.valueOf(userId))
                    .header("X-Username", username)
                    .build();

            log.debug("鉴权成功: userId={}, username={}, path={}", userId, username, path);
            return chain.filter(exchange.mutate().request(modifiedRequest).build());

        } catch (Exception e) {
            log.error("鉴权过程发生异常: {}", e.getMessage());
            return unauthorized(exchange, "鉴权失败");
        }
    }

    /**
     * 检查是否为白名单路径
     */
    private boolean isWhiteListPath(String path) {
        for (String pattern : WHITE_LIST) {
            if (pathMatcher.match(pattern, path)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 从请求中提取Token
     */
    private String extractToken(ServerHttpRequest request) {
        String header = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    /**
     * 返回未授权响应
     */
    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        Result<Object> result = Result.error(401, message);
        ObjectMapper objectMapper = new ObjectMapper();
        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(result);
        } catch (JsonProcessingException e) {
            bytes = ("{\"code\":401,\"message\":\"" + message + "\"}".getBytes(StandardCharsets.UTF_8)).getBytes();
        }

        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return 2;
    }
}
