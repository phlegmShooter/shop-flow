package com.shopflow.common.constant;

/**
 * Redis Key常量
 */
public class RedisKeyConstant {

    // 用户Token前缀
    public static final String USER_TOKEN_PREFIX = "token:user:";

    // 商品详情缓存前缀
    public static final String PRODUCT_DETAIL_PREFIX = "product:detail:";

    // 商品分类树缓存
    public static final String PRODUCT_CATEGORY_TREE = "product:category:tree";

    // 商品库存前缀（用于防超卖）
    public static final String PRODUCT_STOCK_PREFIX = "product:stock:";

    // 购物车前缀
    public static final String CART_PREFIX = "cart:";

    // 订单超时取消延迟队列
    public static final String ORDER_TIMEOUT_CANCEL_PREFIX = "order:timeout:cancel:";

    // 限流令牌桶前缀
    public static final String RATE_LIMIT_PREFIX = "rate:limit:";

    // 验证码前缀
    public static final String CAPTCHA_PREFIX = "captcha:";

    // 分布式锁前缀
    public static final String LOCK_PREFIX = "lock:";

    // 缓存默认TTL（秒）
    public static class TTL {
        // 用户Token TTL（7天）
        public static final long USER_TOKEN = 7 * 24 * 60 * 60;

        // 商品详情缓存TTL（30分钟）
        public static final long PRODUCT_DETAIL = 30 * 60;

        // 商品分类树缓存TTL（1小时）
        public static final long PRODUCT_CATEGORY_TREE = 60 * 60;

        // 购物车TTL（7天）
        public static final long CART = 7 * 24 * 60 * 60;

        // 订单超时取消（30分钟）
        public static final long ORDER_TIMEOUT_CANCEL = 30 * 60;

        // 验证码TTL（5分钟）
        public static final long CAPTCHA = 5 * 60;

        // 分布式锁TTL（10秒）
        public static final long LOCK = 10;

        // 限流令牌桶重置时间（1秒）
        public static final long RATE_LIMIT_RESET = 1;
    }

    /**
     * 构建完整的Redis Key
     */
    public static String buildKey(String prefix, Object... parts) {
        StringBuilder key = new StringBuilder(prefix);
        for (Object part : parts) {
            key.append(part).append(":");
        }
        // 移除最后一个冒号
        if (key.charAt(key.length() - 1) == ':') {
            key.deleteCharAt(key.length() - 1);
        }
        return key.toString();
    }

    /**
     * 构建用户Token Key
     */
    public static String buildUserTokenKey(Long userId) {
        return buildKey(USER_TOKEN_PREFIX, userId);
    }

    /**
     * 构建商品详情Key
     */
    public static String buildProductDetailKey(Long productId) {
        return buildKey(PRODUCT_DETAIL_PREFIX, productId);
    }

    /**
     * 构建商品库存Key
     */
    public static String buildProductStockKey(Long productId) {
        return buildKey(PRODUCT_STOCK_PREFIX, productId);
    }

    /**
     * 构建购物车Key
     */
    public static String buildCartKey(Long userId) {
        return buildKey(CART_PREFIX, userId);
    }

    /**
     * 构建订单超时取消Key
     */
    public static String buildOrderTimeoutCancelKey(String orderNo) {
        return buildKey(ORDER_TIMEOUT_CANCEL_PREFIX, orderNo);
    }

    /**
     * 构建限流Key
     */
    public static String buildRateLimitKey(String ip) {
        return buildKey(RATE_LIMIT_PREFIX, ip);
    }

    /**
     * 构建验证码Key
     */
    public static String buildCaptchaKey(String sessionId) {
        return buildKey(CAPTCHA_PREFIX, sessionId);
    }

    /**
     * 构建分布式锁Key
     */
    public static String buildLockKey(String resource) {
        return buildKey(LOCK_PREFIX, resource);
    }
}