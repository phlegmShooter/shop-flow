package com.shopflow.notification;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

/**
 * 通知服务启动类
 */
@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.shopflow.notification.mapper")
@ComponentScan(basePackages = {"com.shopflow.notification", "com.shopflow.common"})
public class NotificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}
