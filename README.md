# ShopFlow Microservices Platform

![License](https://img.shields.io/badge/license-MIT-blue.svg)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-green.svg)
![Vue](https://img.shields.io/badge/Vue-3.x-brightgreen.svg)
![Docker](https://img.shields.io/badge/Docker-Ready-blue.svg)

[中文版文档介绍 (Chinese Version)](#shopflow-微服务电商平台)

ShopFlow is a modern, enterprise-grade e-commerce platform built on a microservices architecture. It aims to provide a robust, scalable, and fully containerized shopping experience out of the box, utilizing modern Java and frontend technologies.

## 🌟 Key Features

- **Microservices Architecture**: Beautifully decomposed domains (Gateway, User, Product, Order, Cart, Notification).
- **Secure & Zero-Trust**: All backend services (Nacos, MySQL, Redis, RabbitMQ, Spring Boot apps) communicate strictly within the Docker internal bridge network (`shopflow-net`). No internal operational ports are exposed to the host machine.
- **Unified Proxy**: Nginx serves both as a static file web server and a reverse proxy for the API gateway, eliminating CORS issues and hiding backend topology.
- **Scalable Infrastructure**: Service Discovery and Configuration scaling handled effortlessly by Alibaba Nacos.
- **Modern User Interface**: Built with Vue 3, showcasing a responsive, aesthetic frontend design.

## 🏗️ Architecture & Tech Stack

### Frontend
- **Framework**: Vue 3 (Composition API) + TypeScript
- **Tooling**: Vite
- **Web Server**: Nginx Alpine

### Backend (Spring Cloud Alibaba)
- **API Gateway**: Spring Cloud Gateway (Reactive, Rate Limiting)
- **User Service**: JWT Authentication, User Profile Management
- **Product Service**: Product Catalog, Stock Management
- **Cart Service**: Shopping Cart Operations (backed by Redis)
- **Order Service**: Checkout, Order Lifecycle
- **Notification Service**: Event-driven alerts via RabbitMQ

### Infrastructure & Middleware
- **Service Registry & Config**: Alibaba Nacos 2.2.3
- **Database**: MySQL 8.0 + HikariCP
- **Cache**: Redis 7.0
- **Message Broker**: RabbitMQ 3.12

## 🚀 Quick Start (Docker Deployment)

We provide a production-ready, one-click deployment configuration.

### Prerequisites
- Docker Engine (v20.10+)
- Docker Compose (v2.x)
- *Note for CentOS/Linux Users: Ensure your firewall allows Docker bridge interfaces to communicate (e.g., `firewall-cmd --zone=trusted --add-source=172.16.0.0/12`).*

### 1. Build and Run
Clone this repository and simply execute Docker Compose from the root directory:
```bash
docker compose down
docker compose up -d
```
*Note: The first launch may take 30-60 seconds for the Nacos Server and MySQL databases to initialize perfectly. The microservices are configured to auto-restart until the infrastructure is ready.*

### 2. Access the Platform
Open your browser and navigate to the exposed container port (mapped to `3000` by default):
```text
http://<your-machine-ip>:3000
```
- The frontend will load cleanly.
- API traffic (`/api/**`) is automatically routed to the internal Spring Cloud Gateway.

---

# ShopFlow 微服务电商平台

![License](https://img.shields.io/badge/license-MIT-blue.svg)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-green.svg)
![Vue](https://img.shields.io/badge/Vue-3.x-brightgreen.svg)
![Docker](https://img.shields.io/badge/Docker- Ready-blue.svg)

ShopFlow 是一个基于现代微服务架构构建的企业级电商平台。它采用当前最流行的 Java 后端栈搭配 Vue 3 前端，提供开箱即用、安全、高可扩展的“一键式”容器化电商解决方案。

## 🌟 核心特性

- **微服务架构**：按业务领域严格拆解（网关、用户、商品、订单、购物车、消息通知）。
- **零信任安全网络层**：所有的后端核心基建（Nacos、MySQL、Redis、RabbitMQ）以及微服务容器，均被彻底隔离在 Docker 的自定义桥接网络（`shopflow-net`）内。**宿主机无需暴露任何内网端口**，从根本上杜绝外界恶意扫描扫库。
- **全局统一网关入口**：Nginx 同时作为静态前端服务器与反向代理。自动将带 `/api` 的请求透明转发给后端的 Spring Cloud Gateway，彻底消灭跨域（CORS）问题，隐藏底层拓扑拓扑细节。
- **强劲的注册与配置中心**：深度集成 Alibaba Nacos 2.x，自动感知微服务节点并下发配置。
- **现代化前台设计**：全新 Vue 3 打造丝滑流畅、动态响应式的购物体验界面。

## 🏗️ 架构与技术栈

### 前端生态
- **核心骨架**：Vue 3 (Composition API) + TypeScript
- **构建工具**：Vite
- **部署服务器**：Nginx Alpine

### 后端微服务 (Spring Cloud Alibaba 体系)
- **API 网关**：Spring Cloud Gateway（响应式、支持全局接口限流）
- **用户服务 (User)**：基于 JWT 的身份聚合校验与信息管理
- **商品服务 (Product)**：商品展台检索与库存管理
- **购物车服务 (Cart)**：基于 Redis 的高速购物车操作
- **订单服务 (Order)**：订单履约与生命周期状态机
- **消息通知 (Notification)**：基于 RabbitMQ 消息队列的异步解耦通知架构

### 核心基建及中间件
- **服务发现与配置**：Alibaba Nacos 2.2.3
- **关系型数据库**：MySQL 8.0 
- **缓存引擎**：Redis 7.0 (Alpine)
- **消息队列**：RabbitMQ 3.12 (Management Alpine)

## 🚀 极速部署指南 (Docker)

自带生产级别的终极部署配置，无需繁琐安装各种开发环境，一键直达生产！

### 部署前置要求
- 安装有 Docker Engine (v20.10 及以上)
- 安装有 Docker Compose (v2.x)
- *注意 (针对 CentOS 用户的防坑指南)：CentOS 默认防火墙 `firewall` 会拦截 Docker 容器互相通信。如果遇到 502 报错，请将 docker 网段加入防火墙白名单，或执行 `systemctl stop firewalld` && `systemctl restart docker`。*

### 1. 一键启动
在项目根目录（带有 `docker-compose.yml` 的地方），直接执行清洗与拉起指令：
```bash
docker compose down
docker compose up -d
```
*温馨提示：Nacos 与 MySQL 初次启动建库耗时较长（约 30-60 秒）。微服务自带了 `restart: always` 的保活机制，会在基建沉淀好后自动全部复活对接。请启动后耐心等待半分钟左右即可。*

### 2. 访问你的电商帝国
在浏览器输入安装这套系统的服务器 IP，默认映射宿主机端口为 `3000`：
```text
http://<你的服务器公网或内网IP>:3000
```
- 你将直接看到华丽的商城首页。
- 所有的数据交互会通过 Nginx 安全转发给被隐藏在冰山下的微服务网关引擎。

---
*Happy Coding & Selling!*