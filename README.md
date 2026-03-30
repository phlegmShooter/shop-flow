# ShopFlow - Microservice E-commerce Platform

A comprehensive e-commerce platform built on Spring Cloud microservice architecture, integrating Redis for inventory management, RabbitMQ for asynchronous notifications, and JWT for full-stack authentication, accompanied by a modern Vue 3 frontend.

[![Java](https://img.shields.io/badge/Java-17-orange)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-6DB33F)](https://spring.io/projects/spring-boot)
[![Vue](https://img.shields.io/badge/Vue-3.x-4FC08D)](https://vuejs.org/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED)](https://docs.docker.com/compose/)

---

## 🏗 System Architecture

```text
Client (Vue 3 + Vite)
       ↓  HTTP
[Gateway :8080]  JWT Auth + Routing + Rate Limiting
       ↓
┌──────────────────────────────────────────┐
│  user-service  :8081  User Auth          │
│  product-service :8082  Products         │
│  cart-service  :8083  Redis Cart         │
│  order-service :8084  Orders             │
│  notification-service :8085  Alerts      │
└──────────────────────────────────────────┘
       ↓            ↓          ↓
   MySQL:3306   Redis:6379  RabbitMQ:5672
       ↑
   Nacos:8848  Service Discovery
```

---

## ✨ Features

- **JWT Full-Stack Authentication:** Centralized token validation at the Gateway. UserID is injected into headers for downstream services, meaning they don't have to authenticate repeatedly.
- **Redis Inventory Control:** Atomic operations using Lua scripts to prevent overselling under high concurrency.
- **High Performance Cart:** Redis Hash storage for the shopping cart ensuring O(1) read/write performance with a 7-day TTL.
- **Order Timeout Cancellation:** Redis Key expiration event listener automatically cancels unpaid orders and restores inventory.
- **Async Notifications:** RabbitMQ Topic Exchange pushes asynchronous notifications for order status changes seamlessly.
- **Product Multi-Level Cache:** Cache-Aside pattern with a 30-minute Redis cache for product details and dynamic tree-categories. Fully modernized UI with internationalization (i18n).

---

## 🚀 Quick Start (Docker)

**Prerequisites:** JDK 17, Maven, Node.js 18+, Docker, and Docker Compose installed.

### 1. Build Backend Services

Navigate to the project root and compile all microservices. This command will build the `.jar` files for each service so the Docker containers can use them.

```bash
mvn clean package -DskipTests
```

### 2. Start Docker Containers

Use Docker Compose to spin up MySQL, Redis, Nacos, RabbitMQ, and all custom microservices simultaneously via one-click deployment:

```bash
docker-compose up -d
```

> **Note:** Wait approximately 60 seconds after running the command for all components (especially Nacos and MySQL) to initialize properly before trying to call APIs.

### 3. Start Frontend Server

In another terminal, start the Vue 3 frontend:

```bash
cd frontend
pnpm install   # Or use 'npm install'
pnpm dev       # Or use 'npm run dev'
```

### 4. Access the Application

- **Frontend App:** http://localhost:3000
- **API Gateway:** http://localhost:8000
- **RabbitMQ Dashboard:** http://localhost:15672 (guest / guest)
- **Nacos Console:** http://localhost:8848/nacos (nacos / nacos)

---

## 📁 Project Structure

```text
shopflow/
├── gateway-service/       # API Gateway (:8000)
├── user-service/          # User Service (:8081)
├── product-service/       # Product Service (:8082)
├── cart-service/          # Cart Service (:8083)
├── order-service/         # Order Service (:8084)
├── notification-service/  # Notification Service (:8085)
├── common/                # Shared module (DTOs, Utils, Exceptions)
├── database/
│   └── shopflow-dump.sql  # Database initialization script
├── frontend/              # Vue 3 Frontend
└── docker-compose.yml     # One-click deployment config
```