package com.shopflow.notification.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.shopflow.notification.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;

/**
 * 订单消息监听器
 */
@Slf4j
@Component
public class OrderMessageListener {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private ObjectMapper objectMapper;

    @RabbitListener(queues = "order.created.queue")
    public void handleOrderCreated(Message message, Channel channel) throws IOException {
        String body = new String(message.getBody());
        long deliveryTag = message.getMessageProperties().getDeliveryTag();

        try {
            Map<String, Object> orderMessage = objectMapper.readValue(body, new TypeReference<Map<String, Object>>() {});
            Long userId = Long.valueOf(orderMessage.get("userId").toString());
            String orderNo = orderMessage.get("orderNo").toString();

            // 创建订单创建通知
            String title = "订单创建成功";
            String content = String.format("您的订单 %s 已创建，请在30分钟内完成支付。", orderNo);
            notificationService.createNotification(userId, "ORDER_CREATED", title, content);

            // 确认消息
            channel.basicAck(deliveryTag, false);
            log.info("处理订单创建消息成功: orderNo={}", orderNo);
        } catch (Exception e) {
            log.error("处理订单创建消息失败", e);
            // 消息重新入队
            channel.basicNack(deliveryTag, false, true);
        }
    }

    @RabbitListener(queues = "order.paid.queue")
    public void handleOrderPaid(Message message, Channel channel) throws IOException {
        String body = new String(message.getBody());
        long deliveryTag = message.getMessageProperties().getDeliveryTag();

        try {
            Map<String, Object> orderMessage = objectMapper.readValue(body, new TypeReference<Map<String, Object>>() {});
            Long userId = Long.valueOf(orderMessage.get("userId").toString());
            String orderNo = orderMessage.get("orderNo").toString();
            BigDecimal totalAmount = new BigDecimal(orderMessage.get("totalAmount").toString());

            // 创建订单支付通知
            String title = "订单支付成功";
            String content = String.format("您的订单 %s 已支付成功，金额：%.2f元，我们将尽快为您发货。", orderNo, totalAmount);
            notificationService.createNotification(userId, "ORDER_PAID", title, content);

            // 确认消息
            channel.basicAck(deliveryTag, false);
            log.info("处理订单支付消息成功: orderNo={}", orderNo);
        } catch (Exception e) {
            log.error("处理订单支付消息失败", e);
            // 消息重新入队
            channel.basicNack(deliveryTag, false, true);
        }
    }

    @RabbitListener(queues = "order.cancelled.queue")
    public void handleOrderCancelled(Message message, Channel channel) throws IOException {
        String body = new String(message.getBody());
        long deliveryTag = message.getMessageProperties().getDeliveryTag();

        try {
            Map<String, Object> orderMessage = objectMapper.readValue(body, new TypeReference<Map<String, Object>>() {});
            Long userId = Long.valueOf(orderMessage.get("userId").toString());
            String orderNo = orderMessage.get("orderNo").toString();

            // 创建订单取消通知
            String title = "订单已取消";
            String content = String.format("您的订单 %s 已取消。", orderNo);
            notificationService.createNotification(userId, "ORDER_CANCELLED", title, content);

            // 确认消息
            channel.basicAck(deliveryTag, false);
            log.info("处理订单取消消息成功: orderNo={}", orderNo);
        } catch (Exception e) {
            log.error("处理订单取消消息失败", e);
            // 消息重新入队
            channel.basicNack(deliveryTag, false, true);
        }
    }
}
