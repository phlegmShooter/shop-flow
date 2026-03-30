package com.shopflow.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

/**
 * 商品服务启动类
 */
@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.shopflow.product.mapper")
@ComponentScan(basePackages = {"com.shopflow.product", "com.shopflow.common"})
public class ProductServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductServiceApplication.class, args);
    }
}
