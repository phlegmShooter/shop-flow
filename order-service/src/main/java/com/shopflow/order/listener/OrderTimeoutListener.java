package com.shopflow.order.listener;

import com.shopflow.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

/**
 * Redis过期事件监听器（用于订单超时取消）
 * 注意：需要在Redis配置中开启keyspace notifications
 * config set notify-keyspace-events Ex
 */
@Slf4j
@Component
public class OrderTimeoutListener implements MessageListener {

    @Autowired
    private OrderService orderService;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = new String(message.getBody());
        log.info("监听到Redis Key过期: {}", expiredKey);

        // 判断是否为订单超时Key
        if (expiredKey.startsWith("order:timeout:cancel:")) {
            String orderNo = expiredKey.substring("order:timeout:cancel:".length());
            try {
                orderService.timeoutCancel(orderNo);
            } catch (Exception e) {
                log.error("订单超时取消处理失败: orderNo={}", orderNo, e);
            }
        }
    }
}
