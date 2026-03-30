-- MySQL dump 10.13  Distrib 8.0.36, for Linux (x86_64)
--
-- Host: localhost    Database: shopflow
-- ------------------------------------------------------
-- Server version	8.0.36

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `t_category`
--
CREATE DATABASE IF NOT EXISTS shopflow;
USE shopflow;

DROP TABLE IF EXISTS `t_category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_category` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL COMMENT '分类名称',
  `parent_id` bigint DEFAULT '0' COMMENT '父分类ID，0为顶级',
  `sort` int DEFAULT '0' COMMENT '排序',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品分类表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_category`
--

LOCK TABLES `t_category` WRITE;
/*!40000 ALTER TABLE `t_category` DISABLE KEYS */;
INSERT INTO `t_category` VALUES (1,'电子数码',0,1,'2026-03-29 10:11:37'),(2,'服装鞋帽',0,2,'2026-03-29 10:11:37'),(3,'食品饮料',0,3,'2026-03-29 10:11:37'),(4,'家居生活',0,4,'2026-03-29 10:11:37'),(5,'运动户外',0,5,'2026-03-29 10:11:37'),(11,'手机',1,1,'2026-03-29 10:11:37'),(12,'笔记本',1,2,'2026-03-29 10:11:37'),(13,'平板电脑',1,3,'2026-03-29 10:11:37'),(14,'耳机音响',1,4,'2026-03-29 10:11:37'),(21,'男装',2,1,'2026-03-29 10:11:37'),(22,'女装',2,2,'2026-03-29 10:11:37'),(23,'运动鞋',5,1,'2026-03-29 10:11:37');
/*!40000 ALTER TABLE `t_category` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_notification`
--

DROP TABLE IF EXISTS `t_notification`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_notification` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `type` varchar(50) NOT NULL COMMENT '通知类型',
  `title` varchar(200) NOT NULL,
  `content` text NOT NULL,
  `is_read` tinyint NOT NULL DEFAULT '0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id_is_read` (`user_id`,`is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='通知表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_notification`
--

LOCK TABLES `t_notification` WRITE;
/*!40000 ALTER TABLE `t_notification` DISABLE KEYS */;
/*!40000 ALTER TABLE `t_notification` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_order`
--

DROP TABLE IF EXISTS `t_order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_order` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_no` varchar(32) NOT NULL COMMENT '订单号（全局唯一）',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `total_amount` decimal(10,2) NOT NULL COMMENT '订单总金额',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '0待支付 1已支付 2已发货 3已完成 4已取消',
  `address` varchar(500) NOT NULL COMMENT '收货地址（JSON快照）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `paid_at` datetime DEFAULT NULL COMMENT '支付时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_order`
--

--
-- Table structure for table `t_order_item`
--

DROP TABLE IF EXISTS `t_order_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_order_item` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `product_name` varchar(200) NOT NULL COMMENT '商品名称快照',
  `price` decimal(10,2) NOT NULL COMMENT '下单时价格快照',
  `quantity` int NOT NULL COMMENT '数量',
  `subtotal` decimal(10,2) NOT NULL COMMENT '小计',
  `image` varchar(500) DEFAULT NULL COMMENT '商品图片URL快照',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单项表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_order_item`
--

--
-- Table structure for table `t_product`
--

DROP TABLE IF EXISTS `t_product`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_product` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '商品ID',
  `category_id` bigint NOT NULL COMMENT '分类ID',
  `name` varchar(200) NOT NULL COMMENT '商品名称',
  `description` text COMMENT '商品描述',
  `price` decimal(10,2) NOT NULL COMMENT '价格',
  `stock` int NOT NULL DEFAULT '0' COMMENT '库存',
  `images` varchar(1000) DEFAULT NULL COMMENT '图片URL，逗号分隔',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '1上架 0下架',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_product`
--

LOCK TABLES `t_product` WRITE;
/*!40000 ALTER TABLE `t_product` DISABLE KEYS */;
INSERT INTO `t_product` VALUES (1,11,'iPhone 15 Pro','A17 Pro 芯片，钛金属机身，4800万像素主摄，支持 USB-C 快充。全新钛金属外框，更轻更强。全系标配 ProMotion 自适应刷新率。',8999.00,200,'http://192.168.1.4:7000/images/ecommerce/iphone15.png',1,'2026-03-28 15:19:08','2026-03-28 15:19:08'),(2,11,'华为 Mate 60 Pro','麒麟 9000S 处理器，6.82 英寸 LTPO 曲面屏，支持卫星通话，1 英寸大底徕卡镜头。',6999.00,150,'http://192.168.1.4:7000/images/ecommerce/mate60.png',1,'2026-03-28 15:19:08','2026-03-28 15:19:08'),(3,11,'小米 14 Pro','骁龙 8 Gen3，徕卡光学镜头，120W 有线 + 50W 无线快充，IP68 防水防尘。',4999.00,300,'http://192.168.1.4:7000/images/ecommerce/xiaomi14.png',1,'2026-03-28 15:19:08','2026-03-28 15:19:08'),(4,11,'OPPO Find X7 Ultra','双潜望长焦，哈苏调色，1 英寸 IMX989 超主摄，100W 有线 + 50W 无线闪充。',5999.00,120,'http://192.168.1.4:7000/images/ecommerce/oppox7.png',1,'2026-03-28 15:19:08','2026-03-28 15:19:08'),(5,12,'MacBook Pro 16英寸 M3 Max','M3 Max 芯片，48GB 统一内存，120Hz Liquid Retina XDR 显示屏，22 小时续航。',24999.00,60,'http://192.168.1.4:7000/images/ecommerce/macbook16.png',1,'2026-03-28 15:19:08','2026-03-28 15:19:08'),(6,12,'联想 ThinkPad X1 Carbon','英特尔第 13 代酷睿 i7，16GB LPDDR5，1T SSD，14 英寸 2.8K OLED 屏，仅重 1.12kg。',12999.00,80,'http://192.168.1.4:7000/images/ecommerce/thinkpad.png',1,'2026-03-28 15:19:08','2026-03-28 15:19:08'),(7,12,'华为 MateBook X Pro 2024','酷睿 Ultra 9，14.2 英寸 3.1K 触控屏，三麦超线性扬声器，轻至 1.26 公斤。',9999.00,100,'http://192.168.1.4:7000/images/ecommerce/matebook.png',1,'2026-03-28 15:19:08','2026-03-28 15:19:08'),(8,13,'iPad Pro 13英寸 M4','超薄 M4 芯片平板，OLED Tandem 显示屏，Apple Pencil Pro 支持，兼容 Magic Keyboard。',10999.00,80,'http://192.168.1.4:7000/images/ecommerce/ipadpro.png',1,'2026-03-28 15:19:08','2026-03-28 15:19:08'),(9,13,'华为 MatePad Pro 13.2英寸','2.8K OLED 144Hz 屏，麒麟 9000s，支持二代 M 笔，一碰协同功能。',4999.00,100,'http://192.168.1.4:7000/images/ecommerce/matepad.png',1,'2026-03-28 15:19:08','2026-03-28 15:19:08'),(10,14,'AirPods Pro 第二代','自适应主动降噪，个性化空间音频，MagSafe 充电盒，单次续航 6 小时，IPX4 防水。',1899.00,500,'http://192.168.1.4:7000/images/ecommerce/airpods.png',1,'2026-03-28 15:19:08','2026-03-28 15:19:08'),(11,14,'索尼 WH-1000XM5','行业顶级主动降噪，30 小时续航，8 个麦克风精准降噪，支持 LDAC 高解析音质。',2499.00,200,'http://192.168.1.4:7000/images/ecommerce/sony.png',1,'2026-03-28 15:19:08','2026-03-28 15:19:08'),(12,14,'华为 FreeBuds Pro 3','三麦智慧降噪，双扬声器设计，11mm 动圈 + 骨声纹传感器，iDMC 解码。',1099.00,350,'http://192.168.1.4:7000/images/ecommerce/freebuds.png',1,'2026-03-28 15:19:08','2026-03-28 15:19:08'),(13,21,'优衣库 男款羽绒服','90% 白鸭绒填充，轻盈保暖，防泼水处理，立领设计，多色可选。',599.00,1000,'http://192.168.1.4:7000/images/ecommerce/jacket.png',1,'2026-03-28 15:19:08','2026-03-28 15:19:08'),(14,21,'POLO衫 纯棉商务休闲','100% 精梳棉，透气舒适，经典翻领设计，适合日常商务穿搭。',199.00,2000,'http://192.168.1.4:7000/images/ecommerce/polo.png',1,'2026-03-28 15:19:08','2026-03-28 15:19:08'),(15,22,'韩版连衣裙 春夏新款','雪纺面料，印花设计，修身版型，V 领收腰，适合约会通勤。',299.00,1500,'http://192.168.1.4:7000/images/ecommerce/dress.png',1,'2026-03-28 15:19:08','2026-03-28 15:19:08'),(16,3,'新疆灰枣 2斤装','新疆若羌特产，颗粒饱满，自然晒干，无添加，富含维生素。',59.90,5000,'http://192.168.1.4:7000/images/ecommerce/dates.png',1,'2026-03-28 15:19:08','2026-03-28 15:19:08'),(17,3,'云南小粒咖啡豆 500g','云南普洱产区，中深烘焙，果香浓郁，适合手冲、摩卡壶、意式机。',89.00,3000,'http://192.168.1.4:7000/images/ecommerce/coffee.png',1,'2026-03-28 15:19:08','2026-03-28 15:19:08'),(18,4,'小米 智能台灯 Pro','支持 2700K-6500K 色温调节，亮度调节 1%-100%，防蓝光护眼，支持 App 控制。',299.00,800,'http://192.168.1.4:7000/images/ecommerce/lamp.png',1,'2026-03-28 15:19:08','2026-03-28 15:19:08'),(19,4,'戴森 V12 Detect Slim 无线吸尘器','激光纤柔除尘，比马达，自动感应地板类型，毛发缠结清除技术。',3990.00,150,'http://192.168.1.4:7000/images/ecommerce/dyson.png',1,'2026-03-28 15:19:08','2026-03-28 15:19:08'),(20,23,'Nike Air Max 270 跑鞋','Air Max 气垫缓震，透气网眼鞋面，橡胶外底耐磨，适合日常跑步训练。',899.00,400,'http://192.168.1.4:7000/images/ecommerce/nike.png',1,'2026-03-28 15:19:08','2026-03-28 15:19:08'),(21,23,'Adidas Ultra Boost 23','Boost 回弹中底，Primeknit 针织鞋面，Continental 橡胶外底，轻量舒适。',1099.00,300,'http://192.168.1.4:7000/images/ecommerce/adidas.png',1,'2026-03-28 15:19:08','2026-03-28 15:19:08');
/*!40000 ALTER TABLE `t_product` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_user`
--

DROP TABLE IF EXISTS `t_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(255) NOT NULL COMMENT '密码（BCrypt加密）',
  `email` varchar(100) NOT NULL COMMENT '邮箱',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像URL',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：1正常 0禁用',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_user`
--

--
-- Table structure for table `t_user_address`
--

DROP TABLE IF EXISTS `t_user_address`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_user_address` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '地址ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `receiver_name` varchar(50) NOT NULL COMMENT '收货人姓名',
  `phone` varchar(20) NOT NULL COMMENT '手机号',
  `province` varchar(50) NOT NULL COMMENT '省',
  `city` varchar(50) NOT NULL COMMENT '市',
  `district` varchar(50) DEFAULT NULL COMMENT '区/县',
  `detail` varchar(200) NOT NULL COMMENT '详细地址（街道/楼栋/门牌）',
  `is_default` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否默认地址：1是 0否',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户收货地址表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_user_address`
--

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-03-29 12:10:13
