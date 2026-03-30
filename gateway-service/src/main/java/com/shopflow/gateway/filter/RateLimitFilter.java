package com.shopflow.gateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopflow.common.Result;
import com.shopflow.common.constant.RedisKeyConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

/**
 * 限流过滤器（基于Redis令牌桶算法）
 */
@Slf4j
@Component
public class RateLimitFilter implements GlobalFilter, Ordered {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Value("${rate-limit.enabled:true}")
    private boolean rateLimitEnabled;

    @Value("${rate-limit.requests-per-second:10}")
    private int requestsPerSecond;

    @Value("${rate-limit.bucket-capacity:100}")
    private int bucketCapacity;

    // Lua脚本：令牌桶算法
    private static final String RATE_LIMIT_SCRIPT =
            "local key = KEYS[1] " +
            "local capacity = tonumber(ARGV[1]) " +
            "local rate = tonumber(ARGV[2]) " +
            "local now = tonumber(ARGV[3]) " +
            "local requested = tonumber(ARGV[4]) " +
            "local bucket_info = redis.call('get', key) " +
            "if bucket_info == false then " +
            "    redis.call('hset', key, 'tokens', capacity - requested) " +
            "    redis.call('hset', key, 'last_time', now) " +
            "    redis.call('expire', key, 60) " +
            "    return 1 " +
            "end " +
            "local tokens = tonumber(redis.call('hget', key, 'tokens')) " +
            "local last_time = tonumber(redis.call('hget', key, 'last_time')) " +
            "local delta = math.max(0, (now - last_time) / 1000) " +
            "local new_tokens = math.min(capacity, tokens + delta * rate) " +
            "if new_tokens >= requested then " +
            "    redis.call('hset', key, 'tokens', new_tokens - requested) " +
            "    redis.call('hset', key, 'last_time', now) " +
            "    redis.call('expire', key, 60) " +
            "    return 1 " +
            "else " +
            "    redis.call('hset', key, 'tokens', new_tokens) " +
            "    redis.call('hset', key, 'last_time', now) " +
            "    redis.call('expire', key, 60) " +
            "    return 0 " +
            "end";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 如果限流未启用，直接放行
        if (!rateLimitEnabled) {
            return chain.filter(exchange);
        }

        ServerHttpRequest request = exchange.getRequest();
        String clientIp = getClientIp(request);
        String path = request.getURI().getPath();

        // 构建限流Key
        String rateLimitKey = RedisKeyConstant.buildRateLimitKey(clientIp);

        try {
            // 执行Lua脚本进行限流判断
            DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
            redisScript.setScriptText(RATE_LIMIT_SCRIPT);
            redisScript.setResultType(Long.class);

            long now = System.currentTimeMillis();
            List<String> keys = Collections.singletonList(rateLimitKey);

            Long result = stringRedisTemplate.execute(
                    redisScript,
                    keys,
                    String.valueOf(bucketCapacity),
                    String.valueOf(requestsPerSecond),
                    String.valueOf(now),
                    "1"
            );

            if (result != null && result == 1) {
                // 限流通过
                log.debug("限流通过: ip={}, path={}", clientIp, path);
                return chain.filter(exchange);
            } else {
                // 触发限流
                log.warn("触发限流: ip={}, path={}", clientIp, path);
                return rateLimitExceeded(exchange);
            }

        } catch (Exception e) {
            log.error("限流检查异常，放行请求: {}", e.getMessage());
            // Redis异常时放行，避免影响业务
            return chain.filter(exchange);
        }
    }

    /**
     * 获取客户端IP
     */
    private String getClientIp(ServerHttpRequest request) {
        String ip = request.getHeaders().getFirst("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeaders().getFirst("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddress() != null ? request.getRemoteAddress().getAddress().getHostAddress() : "unknown";
        }
        // 多个代理的情况，取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * 返回限流响应
     */
    private Mono<Void> rateLimitExceeded(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        Result<Object> result = Result.error(429, "请求过于频繁，请稍后再试");
        ObjectMapper objectMapper = new ObjectMapper();
        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(result);
        } catch (JsonProcessingException e) {
            bytes = "{\"code\":429,\"message\":\"请求过于频繁，请稍后再试\"}".getBytes(StandardCharsets.UTF_8);
        }

        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return 1;
    }
}
