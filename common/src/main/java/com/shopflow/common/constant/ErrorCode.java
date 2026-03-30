package com.shopflow.common.constant;

/**
 * 错误码枚举
 */
public enum ErrorCode {

    SUCCESS(200, "成功"),
    BAD_REQUEST(400, "参数错误"),
    UNAUTHORIZED(401, "未登录/Token过期"),
    FORBIDDEN(403, "无权限"),
    NOT_FOUND(404, "资源不存在"),
    INTERNAL_SERVER_ERROR(500, "服务器内部错误"),

    // 业务错误码
    STOCK_NOT_ENOUGH(1001, "库存不足"),
    ORDER_STATUS_ERROR(1002, "订单状态异常"),
    USERNAME_EXISTS(1003, "用户名已存在"),
    EMAIL_EXISTS(1004, "邮箱已存在"),
    USER_NOT_FOUND(1005, "用户不存在"),
    USER_DISABLED(1006, "用户已被禁用"),
    PASSWORD_ERROR(1007, "密码错误"),
    PRODUCT_NOT_FOUND(1008, "商品不存在"),
    PRODUCT_NOT_ON_SALE(1009, "商品已下架"),
    CART_ITEM_NOT_FOUND(1010, "购物车商品不存在"),
    ORDER_NOT_FOUND(1011, "订单不存在"),
    ORDER_CANNOT_BE_CANCELLED(1012, "订单无法取消"),
    ADDRESS_REQUIRED(1013, "收货地址不能为空"),
    CART_EMPTY(1014, "购物车为空"),
    INSUFFICIENT_BALANCE(1015, "余额不足"),
    PAYMENT_FAILED(1016, "支付失败"),
    NOTIFICATION_SEND_FAILED(1017, "通知发送失败");

    private final Integer code;
    private final String message;

    ErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}