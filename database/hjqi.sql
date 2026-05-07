-- MySQL dump 10.13  Distrib 5.7.40, for Linux (x86_64)
--
-- Host: localhost    Database: hjqi
-- ------------------------------------------------------
-- Server version	5.7.40-log

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `t_activity`
--

DROP TABLE IF EXISTS `t_activity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_activity` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `activity_no` varchar(50) NOT NULL COMMENT '活动编号',
  `name_zh` varchar(200) NOT NULL COMMENT '活动名称（中文）',
  `name_en` varchar(200) DEFAULT NULL COMMENT '活动名称（英文）',
  `description_zh` text COMMENT '活动描述（中文）',
  `description_en` text COMMENT '活动描述（英文）',
  `activity_type` varchar(50) NOT NULL COMMENT '活动类型：DISCOUNT/COUPON/GIFT/FREE_SHIPPING/NEW_USER等',
  `cover_image` varchar(500) DEFAULT NULL COMMENT '活动封面图',
  `h5_url` varchar(500) DEFAULT NULL COMMENT '活动链接（H5）',
  `pc_url` varchar(500) DEFAULT NULL COMMENT '活动链接（PC）',
  `start_time` datetime NOT NULL COMMENT '开始时间',
  `end_time` datetime NOT NULL COMMENT '结束时间',
  `status` tinyint(4) NOT NULL DEFAULT '0' COMMENT '活动状态：0-未开始 1-进行中 2-已结束 3-已取消',
  `enabled` tinyint(4) NOT NULL DEFAULT '1' COMMENT '是否启用：0-禁用 1-启用',
  `sort` int(11) NOT NULL DEFAULT '0' COMMENT '排序',
  `merchant_id` bigint(20) DEFAULT NULL COMMENT '参与商家ID（为空表示全平台活动）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除 0-否 1-是',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_activity_no` (`activity_no`) USING BTREE,
  KEY `idx_activity_type` (`activity_type`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE,
  KEY `idx_enabled` (`enabled`) USING BTREE,
  KEY `idx_merchant_id` (`merchant_id`) USING BTREE,
  KEY `idx_start_time` (`start_time`) USING BTREE,
  KEY `idx_end_time` (`end_time`) USING BTREE,
  KEY `idx_deleted` (`deleted`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='活动管理表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_activity`
--

LOCK TABLES `t_activity` WRITE;
/*!40000 ALTER TABLE `t_activity` DISABLE KEYS */;
INSERT INTO `t_activity` VALUES (1,'ACT609607FA','121','','','','COUPON','','','','2026-01-12 16:00:00','2026-01-20 16:00:00',0,1,0,NULL,'2026-01-02 05:54:42','2026-01-02 05:54:42',0);
/*!40000 ALTER TABLE `t_activity` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_admin`
--

DROP TABLE IF EXISTS `t_admin`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_admin` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(255) NOT NULL COMMENT '密码',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `nickname` varchar(50) DEFAULT NULL COMMENT '昵称/姓名',
  `avatar` varchar(500) DEFAULT NULL COMMENT '头像',
  `role` varchar(20) NOT NULL DEFAULT 'ADMIN' COMMENT '角色 SUPER_ADMIN/ADMIN/OPERATOR',
  `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '状态 0-禁用 1-正常',
  `last_login_at` datetime DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` varchar(50) DEFAULT NULL COMMENT '最后登录IP',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除 0-否 1-是',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_username` (`username`) USING BTREE,
  KEY `idx_email` (`email`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='平台管理员表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_admin`
--

LOCK TABLES `t_admin` WRITE;
/*!40000 ALTER TABLE `t_admin` DISABLE KEYS */;
INSERT INTO `t_admin` VALUES (1,'admin','$2a$10$s6G2bdHKumrlx4dzS/2nj.fIZ2CRcEW33DWd2/jdZe30vcBgc94.C','admin@vote.com',NULL,'超级管理员',NULL,'SUPER_ADMIN',1,NULL,NULL,'2025-12-29 15:54:24','2026-01-03 19:11:25',0);
/*!40000 ALTER TABLE `t_admin` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_after_sale_rule`
--

DROP TABLE IF EXISTS `t_after_sale_rule`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_after_sale_rule` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `merchant_id` bigint(20) NOT NULL COMMENT '商家ID',
  `product_id` bigint(20) DEFAULT NULL COMMENT '商品ID（NULL表示通用规则）',
  `rule_type` varchar(20) NOT NULL COMMENT '规则类型 RETURN/EXCHANGE/REPAIR',
  `title_zh` varchar(200) NOT NULL COMMENT '规则标题（中文）',
  `title_en` varchar(200) DEFAULT NULL COMMENT '规则标题（英文）',
  `content_zh` text NOT NULL COMMENT '规则内容（中文）',
  `content_en` text COMMENT '规则内容（英文）',
  `valid_days` int(11) NOT NULL DEFAULT '7' COMMENT '有效期（天）',
  `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '状态 0-禁用 1-启用',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除 0-否 1-是',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_merchant_id` (`merchant_id`) USING BTREE,
  KEY `idx_product_id` (`product_id`) USING BTREE,
  KEY `idx_rule_type` (`rule_type`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='售后规则表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_after_sale_rule`
--

LOCK TABLES `t_after_sale_rule` WRITE;
/*!40000 ALTER TABLE `t_after_sale_rule` DISABLE KEYS */;
INSERT INTO `t_after_sale_rule` VALUES (1,2,NULL,'REFUND','退款 运费险','wwwwww','退款 运费险','aftersale',7,1,'2025-12-31 01:29:55','2026-01-03 22:00:11',0);
/*!40000 ALTER TABLE `t_after_sale_rule` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_app_download`
--

DROP TABLE IF EXISTS `t_app_download`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_app_download` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `platform` varchar(20) NOT NULL COMMENT '平台类型：IOS/ANDROID',
  `version` varchar(50) NOT NULL COMMENT '版本号',
  `version_name` varchar(100) DEFAULT NULL COMMENT '版本名称',
  `download_url` varchar(500) NOT NULL COMMENT '下载链接',
  `update_notes_zh` text COMMENT '更新说明（中文）',
  `update_notes_en` text COMMENT '更新说明（英文）',
  `force_update` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否强制更新：0-否 1-是',
  `file_size` bigint(20) DEFAULT NULL COMMENT '文件大小（字节）',
  `file_md5` varchar(64) DEFAULT NULL COMMENT '文件MD5',
  `enabled` tinyint(4) NOT NULL DEFAULT '1' COMMENT '是否启用：0-禁用 1-启用',
  `publish_time` datetime DEFAULT NULL COMMENT '发布时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除 0-否 1-是',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_platform` (`platform`) USING BTREE,
  KEY `idx_version` (`version`) USING BTREE,
  KEY `idx_enabled` (`enabled`) USING BTREE,
  KEY `idx_publish_time` (`publish_time`) USING BTREE,
  KEY `idx_deleted` (`deleted`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='App下载管理表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_app_download`
--

LOCK TABLES `t_app_download` WRITE;
/*!40000 ALTER TABLE `t_app_download` DISABLE KEYS */;
INSERT INTO `t_app_download` VALUES (1,'IOS','1','3213','2312','1212','',0,NULL,'',1,'2026-01-02 05:55:04','2026-01-02 05:55:04','2026-01-02 05:55:04',0);
/*!40000 ALTER TABLE `t_app_download` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_app_download_config`
--

DROP TABLE IF EXISTS `t_app_download_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_app_download_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `merchant_id` bigint(20) DEFAULT NULL COMMENT '商家ID（NULL表示平台配置）',
  `platform` varchar(20) NOT NULL COMMENT '平台类型 IOS/ANDROID',
  `version` varchar(50) NOT NULL COMMENT '版本号',
  `download_url` varchar(500) NOT NULL COMMENT '下载链接',
  `update_log_zh` text COMMENT '更新日志（中文）',
  `update_log_en` text COMMENT '更新日志（英文）',
  `force_update` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否强制更新 0-否 1-是',
  `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '状态 0-禁用 1-启用',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_merchant_id` (`merchant_id`) USING BTREE,
  KEY `idx_platform` (`platform`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='App下载页配置表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_app_download_config`
--

LOCK TABLES `t_app_download_config` WRITE;
/*!40000 ALTER TABLE `t_app_download_config` DISABLE KEYS */;
/*!40000 ALTER TABLE `t_app_download_config` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_brand`
--

DROP TABLE IF EXISTS `t_brand`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_brand` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `merchant_id` bigint(20) NOT NULL COMMENT '商家ID',
  `name_zh` varchar(100) NOT NULL COMMENT '品牌名称（中文）',
  `name_en` varchar(100) DEFAULT NULL COMMENT '品牌名称（英文）',
  `logo` varchar(500) DEFAULT NULL COMMENT '品牌Logo',
  `description_zh` text COMMENT '品牌描述（中文）',
  `description_en` text COMMENT '品牌描述（英文）',
  `sort` int(11) NOT NULL DEFAULT '0' COMMENT '排序',
  `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '状态 0-禁用 1-启用',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除 0-否 1-是',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_merchant_id` (`merchant_id`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE,
  KEY `idx_sort` (`sort`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='品牌表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_brand`
--

LOCK TABLES `t_brand` WRITE;
/*!40000 ALTER TABLE `t_brand` DISABLE KEYS */;
INSERT INTO `t_brand` VALUES (1,2,'哇哇哇哇','12312','/upload/20251231_023410_09666dc6.jpg','2123','wsda',0,1,'2025-12-31 02:34:18','2025-12-31 02:34:18',0);
/*!40000 ALTER TABLE `t_brand` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_cart_item`
--

DROP TABLE IF EXISTS `t_cart_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_cart_item` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `merchant_id` bigint(20) NOT NULL COMMENT '商家ID',
  `product_id` bigint(20) NOT NULL COMMENT '商品ID',
  `sku_id` bigint(20) DEFAULT NULL COMMENT 'SKU ID（如果有SKU）',
  `quantity` int(11) NOT NULL DEFAULT '1' COMMENT '数量',
  `selected` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否选中',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除 0-否 1-是',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_user_product_sku` (`user_id`,`product_id`,`sku_id`) USING BTREE,
  KEY `idx_user_id` (`user_id`) USING BTREE,
  KEY `idx_merchant_id` (`merchant_id`) USING BTREE,
  KEY `idx_product_id` (`product_id`) USING BTREE,
  KEY `idx_sku_id` (`sku_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='购物车表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_cart_item`
--

LOCK TABLES `t_cart_item` WRITE;
/*!40000 ALTER TABLE `t_cart_item` DISABLE KEYS */;
INSERT INTO `t_cart_item` VALUES (1,1,2,5,4,1,1,'2025-12-31 23:56:19','2026-01-02 05:25:08',1),(2,1,2,6,NULL,1,1,'2026-01-01 01:17:34','2026-01-01 02:09:01',1),(3,1,2,5,3,1,1,'2026-01-01 01:40:03','2026-01-03 21:47:15',1),(4,1,2,5,NULL,1,1,'2026-01-01 02:09:03','2026-01-01 02:09:07',1),(5,1,2,5,NULL,1,1,'2026-01-01 02:09:55','2026-01-01 02:15:46',1),(6,1,2,6,NULL,1,1,'2026-01-01 02:15:48','2026-01-01 02:27:58',1),(11,1,2,6,0,1,1,'2026-01-01 11:21:58','2026-01-01 13:29:05',1);
/*!40000 ALTER TABLE `t_cart_item` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_category`
--

DROP TABLE IF EXISTS `t_category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_category` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `merchant_id` bigint(20) NOT NULL COMMENT '商家ID',
  `parent_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '父分类ID（0表示一级分类）',
  `name_zh` varchar(50) NOT NULL COMMENT '分类名称（中文）',
  `name_en` varchar(50) DEFAULT NULL COMMENT '分���名称（英文）',
  `icon` varchar(500) DEFAULT NULL COMMENT '分类图标',
  `sort` int(11) NOT NULL DEFAULT '0' COMMENT '排序（越小越靠前）',
  `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '状态 0-禁用 1-启用',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除 0-否 1-是',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_merchant_id` (`merchant_id`) USING BTREE,
  KEY `idx_parent_id` (`parent_id`) USING BTREE,
  KEY `idx_sort` (`sort`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='商品分类表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_category`
--

LOCK TABLES `t_category` WRITE;
/*!40000 ALTER TABLE `t_category` DISABLE KEYS */;
INSERT INTO `t_category` VALUES (1,2,1,'电子','dianzi',NULL,1,1,'2025-12-31 02:16:53','2025-12-31 02:16:53',0);
/*!40000 ALTER TABLE `t_category` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_chat_message`
--

DROP TABLE IF EXISTS `t_chat_message`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_chat_message` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `merchant_id` bigint(20) NOT NULL COMMENT '商家ID',
  `user_id` bigint(20) DEFAULT NULL COMMENT '用户ID（管理员与商家聊天时为NULL）',
  `sender_type` varchar(20) NOT NULL COMMENT '发送者类型 USER/MERCHANT/ADMIN',
  `sender_id` bigint(20) NOT NULL COMMENT '发送者ID',
  `content` text NOT NULL COMMENT '消息内容',
  `message_type` varchar(20) NOT NULL DEFAULT 'TEXT' COMMENT '消息类型 TEXT/IMAGE/FILE',
  `order_id` bigint(20) DEFAULT NULL COMMENT '关联的订单ID（用于发送订单卡片）',
  `is_read` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否已读 0-未读 1-已读',
  `read_at` datetime DEFAULT NULL COMMENT '已读时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除 0-否 1-是',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_merchant_id` (`merchant_id`) USING BTREE,
  KEY `idx_user_id` (`user_id`) USING BTREE,
  KEY `idx_sender_id` (`sender_id`) USING BTREE,
  KEY `idx_is_read` (`is_read`) USING BTREE,
  KEY `idx_created_at` (`created_at`) USING BTREE,
  KEY `idx_order_id` (`order_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='客服聊天消息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_chat_message`
--

LOCK TABLES `t_chat_message` WRITE;
/*!40000 ALTER TABLE `t_chat_message` DISABLE KEYS */;
INSERT INTO `t_chat_message` VALUES (1,2,NULL,'ADMIN',1,'11','TEXT',NULL,0,NULL,'2025-12-31 14:25:20',0),(2,2,NULL,'ADMIN',1,'22','TEXT',NULL,0,NULL,'2025-12-31 14:32:56',0),(3,2,NULL,'MERCHANT',2,'222','TEXT',NULL,1,'2025-12-31 14:39:58','2025-12-31 14:39:55',0),(4,2,1,'USER',1,'dsadasdsad','TEXT',NULL,1,'2026-01-01 07:06:55','2026-01-01 06:55:29',0),(5,2,1,'MERCHANT',2,'的撒大','TEXT',NULL,1,'2026-01-01 07:13:43','2026-01-01 07:06:57',0),(6,2,1,'MERCHANT',2,'的撒大','TEXT',NULL,1,'2026-01-01 07:13:43','2026-01-01 07:13:36',0),(7,2,1,'USER',1,'ds','TEXT',NULL,1,'2026-01-01 07:48:17','2026-01-01 07:13:51',0),(8,2,1,'USER',1,'a','TEXT',NULL,1,'2026-01-01 07:48:17','2026-01-01 07:17:07',0),(9,2,1,'USER',1,'sda','TEXT',NULL,1,'2026-01-01 07:48:17','2026-01-01 07:34:28',0),(10,2,1,'USER',1,'s','TEXT',NULL,1,'2026-01-01 07:48:17','2026-01-01 07:37:12',0),(11,2,1,'MERCHANT',2,'搜索','TEXT',NULL,1,'2026-01-01 07:48:27','2026-01-01 07:48:20',0),(12,2,1,'USER',1,'ss','TEXT',NULL,1,'2026-01-01 07:54:36','2026-01-01 07:48:37',0),(13,2,1,'MERCHANT',2,'但是','TEXT',NULL,1,'2026-01-01 07:54:44','2026-01-01 07:54:38',0),(14,2,1,'MERCHANT',2,'十大','TEXT',NULL,1,'2026-01-01 07:54:57','2026-01-01 07:54:50',0),(15,2,1,'MERCHANT',2,'是','TEXT',NULL,1,'2026-01-01 08:03:45','2026-01-01 07:59:11',0),(16,2,1,'MERCHANT',2,'的','TEXT',NULL,1,'2026-01-01 08:03:45','2026-01-01 08:03:43',0),(17,3,1,'USER',1,'可以修改收货地址吗？','TEXT',NULL,1,'2026-01-01 22:55:59','2026-01-01 20:34:53',0),(18,3,1,'USER',1,'可以','TEXT',NULL,1,'2026-01-01 22:55:59','2026-01-01 20:41:30',0),(19,2,1,'USER',1,'1111','TEXT',NULL,1,'2026-01-02 06:40:27','2026-01-02 05:32:07',0),(20,2,1,'USER',1,'dsad','TEXT',NULL,1,'2026-01-02 06:40:27','2026-01-02 05:39:47',0),(21,2,1,'USER',1,'sda','TEXT',NULL,1,'2026-01-02 06:40:27','2026-01-02 05:40:17',0),(22,2,1,'USER',1,'sdasd','TEXT',NULL,1,'2026-01-02 06:40:27','2026-01-02 05:44:11',0),(23,2,1,'USER',1,'sadsad','TEXT',NULL,1,'2026-01-02 06:40:27','2026-01-02 05:46:31',0),(24,2,1,'USER',1,'sda','TEXT',NULL,1,'2026-01-02 06:40:27','2026-01-02 05:49:43',0),(25,2,1,'USER',1,'sda','TEXT',NULL,1,'2026-01-02 06:40:27','2026-01-02 05:53:13',0);
/*!40000 ALTER TABLE `t_chat_message` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_content`
--

DROP TABLE IF EXISTS `t_content`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_content` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `content_type` varchar(20) NOT NULL COMMENT '内容类型 SEO/ACTIVITY/NEWS/ANNOUNCEMENT',
  `title_zh` varchar(200) DEFAULT NULL COMMENT '标题（中文）',
  `title_en` varchar(200) DEFAULT NULL COMMENT '标题（英文）',
  `content_zh` text COMMENT '内容（中文）',
  `content_en` text COMMENT '内容（英文）',
  `cover_image` varchar(500) DEFAULT NULL COMMENT '封面图',
  `link_url` varchar(500) DEFAULT NULL COMMENT '链接URL',
  `sort` int(11) NOT NULL DEFAULT '0' COMMENT '排序',
  `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '状态 0-禁用 1-启用',
  `publish_at` datetime DEFAULT NULL COMMENT '发布时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除 0-否 1-是',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_content_type` (`content_type`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE,
  KEY `idx_sort` (`sort`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='内容管理表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_content`
--

LOCK TABLES `t_content` WRITE;
/*!40000 ALTER TABLE `t_content` DISABLE KEYS */;
INSERT INTO `t_content` VALUES (1,'ANNOUNCEMENT','欢迎使用投票商城系统','Welcome to Vote Mall System','欢迎使用投票商城SaaS系统！本系统为商家提供完整的电商解决方案。','Welcome to Vote Mall SaaS System! We provide complete e-commerce solutions for merchants.',NULL,NULL,1,1,'2025-12-29 21:03:06','2025-12-29 21:03:06','2025-12-29 21:03:06',0),(2,'NEWS','关于投票商城','About Vote Mall','投票商城是一个专业的多商户SaaS商城系统，���商家提供完整的电商功能。','Vote Mall is a professional multi-merchant SaaS mall system that provides complete e-commerce functions for merchants.',NULL,NULL,2,1,'2025-12-29 21:03:06','2025-12-29 21:03:06','2025-12-29 21:03:06',0),(3,'SEO','投票商城 - 首页','Vote Mall - Home','投票商城是一个专业的SaaS商城系统，支持多商户入驻，提供完整的电商功能。','Vote Mall is a professional SaaS mall system that supports multi-merchant settlement and provides complete e-commerce functions.',NULL,NULL,0,1,'2025-12-29 21:03:06','2025-12-29 21:03:06','2025-12-29 21:03:06',0);
/*!40000 ALTER TABLE `t_content` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_email_template`
--

DROP TABLE IF EXISTS `t_email_template`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_email_template` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `template_key` varchar(100) NOT NULL COMMENT '模板键 WELCOME/ORDER_NOTIFICATION/INVOICE等',
  `name_zh` varchar(100) NOT NULL COMMENT '模板名称（中文）',
  `name_en` varchar(100) DEFAULT NULL COMMENT '模板名称（英文）',
  `subject_zh` varchar(200) NOT NULL COMMENT '邮件主题（中文）',
  `subject_en` varchar(200) DEFAULT NULL COMMENT '邮件主题（英文）',
  `content_zh` text NOT NULL COMMENT '邮件内容（中文，Thymeleaf模板）',
  `content_en` text COMMENT '邮件内容（英文，Thymeleaf模板）',
  `variables` varchar(500) DEFAULT NULL COMMENT '变量说明（JSON格式）',
  `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '状态 0-禁用 1-启用',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_template_key` (`template_key`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='邮件模板表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_email_template`
--

LOCK TABLES `t_email_template` WRITE;
/*!40000 ALTER TABLE `t_email_template` DISABLE KEYS */;
INSERT INTO `t_email_template` VALUES (1,'WELCOME','欢迎邮件','Welcome Email','欢迎加入{{shopName}}','Welcome to {{shopName}}','<html><body><h1>欢迎加入{{shopName}}！</h1><p>亲爱的{{username}}，</p><p>感谢您注册成为我们的用户！</p></body></html>','<html><body><h1>Welcome to {{shopName}}!</h1><p>Dear {{username}},</p><p>Thank you for registering as our user!</p></body></html>','{\"shopName\":\"店铺名称\",\"username\":\"用户名\"}',1,'2025-12-29 22:48:18','2025-12-29 22:48:18'),(2,'ORDER_NOTIFICATION','订单通知','Order Notification','您有新订单：{{orderNo}}','New Order: {{orderNo}}','<html><body><h1>新订单通知</h1><p>订单号：{{orderNo}}</p><p>金额：{{amount}}</p></body></html>','<html><body><h1>New Order Notification</h1><p>Order No: {{orderNo}}</p><p>Amount: {{amount}}</p></body></html>','{\"orderNo\":\"订单号\",\"amount\":\"金额\"}',1,'2025-12-29 22:48:18','2025-12-29 22:48:18'),(3,'INVOICE','发票邮件','Invoice Email','您的发票：{{invoiceNo}}','Your Invoice: {{invoiceNo}}','<html><body><h1>发票</h1><p>发票号：{{invoiceNo}}</p><p>下载链接：<a href=\"{{downloadUrl}}\">点击下载</a></p></body></html>','<html><body><h1>Invoice</h1><p>Invoice No: {{invoiceNo}}</p><p>Download: <a href=\"{{downloadUrl}}\">Click to Download</a></p></body></html>','{\"invoiceNo\":\"发票号\",\"downloadUrl\":\"下载链接\"}',1,'2025-12-29 22:48:18','2025-12-29 22:48:18'),(4,'MERCHANT_WELCOME','商家欢迎邮件','Merchant Welcome Email','欢迎加入我们的平台！','Welcome to Our Platform!','<html><body><h2>尊敬的 [[${shopName}]]，</h2><p>欢迎您成功入驻我们的平台！</p><p><strong>您的账号信息：</strong></p><ul><li>用户名：[[${username}]]</li><li>店铺邀请码：[[${inviteCode}]]</li><li>推广链接：[[${promotionLink}]]</li></ul><p><strong>快速开始：</strong></p><ol><li>完善店铺信息</li><li>上传店铺Logo和Banner</li><li>创建您的第一个投票活动</li><li>分享邀请码给用户</li></ol><p><strong>推广奖励：</strong></p><p>邀请新商家入驻可获得 <strong>[[${merchantReward}]]元</strong> 奖励！</p><p>邀请用户注册并消费可获得 <strong>[[${userRewardRate}]]%</strong> 的消费奖励！</p><p>如有任何问题，请联系我们的客服团队。</p><p>祝您使用愉快！</p></body></html>','<html><body><h2>Dear [[${shopName}]],</h2><p>Welcome to our platform!</p><p><strong>Your Account Information:</strong></p><ul><li>Username: [[${username}]]</li><li>Invite Code: [[${inviteCode}]]</li><li>Promotion Link: [[${promotionLink}]]</li></ul><p><strong>Quick Start:</strong></p><ol><li>Complete your shop information</li><li>Upload shop logo and banner</li><li>Create your first voting activity</li><li>Share invite code with users</li></ol><p><strong>Promotion Rewards:</strong></p><p>Invite new merchants to get <strong>$[[${merchantReward}]]</strong> reward!</p><p>Invite users to register and consume to get <strong>[[${userRewardRate}]]%</strong> consumption reward!</p><p>If you have any questions, please contact our customer service team.</p><p>Best regards!</p></body></html>','username,shopName,inviteCode,promotionLink,merchantReward,userRewardRate',1,'2025-12-30 09:50:00','2025-12-30 09:50:00'),(5,'MERCHANT_AUDIT_APPROVED','商家审核通过','Merchant Audit Approved','您的商家入驻申请已通过审核','Your Merchant Application Has Been Approved','<html><body><h2>尊敬的 [[${shopName}]]，</h2><p>恭喜您！您的商家入驻申请已通过审核。</p><p>请尽快完成缴费以激活您的账号。</p><p><strong>缴费信息：</strong></p><ul><li>入驻费用：[[${fee}]]元</li><li>缴费链接：[[${paymentLink}]]</li></ul><p>缴费完成后，您将收到发票和欢迎邮件。</p><p>感谢您的支持！</p></body></html>','<html><body><h2>Dear [[${shopName}]],</h2><p>Congratulations! Your merchant application has been approved.</p><p>Please complete the payment to activate your account.</p><p><strong>Payment Information:</strong></p><ul><li>Registration Fee: $[[${fee}]]</li><li>Payment Link: [[${paymentLink}]]</li></ul><p>After payment, you will receive an invoice and welcome email.</p><p>Thank you for your support!</p></body></html>','shopName,fee,paymentLink',1,'2025-12-30 09:50:00','2025-12-30 09:50:00'),(6,'MERCHANT_AUDIT_REJECTED','商家审核未通过','Merchant Audit Rejected','您的商家入驻申请未通过审核','Your Merchant Application Has Been Rejected','<html><body><h2>尊敬的 [[${shopName}]]，</h2><p>很抱歉，您的商家入驻申请未通过审核。</p><p><strong>拒绝原因：</strong></p><p>[[${reason}]]</p><p>您可以修改信息后重新提交申请。</p><p>如有疑问，请联系我们的客服团队。</p></body></html>','<html><body><h2>Dear [[${shopName}]],</h2><p>We are sorry to inform you that your merchant application has been rejected.</p><p><strong>Rejection Reason:</strong></p><p>[[${reason}]]</p><p>You can modify the information and resubmit the application.</p><p>If you have any questions, please contact our customer service team.</p></body></html>','shopName,reason',1,'2025-12-30 09:50:00','2025-12-30 09:50:00'),(7,'MERCHANT_INVOICE','商家发票','Merchant Invoice','您的入驻费用发票','Your Registration Fee Invoice','<html><body><h2>尊敬的 [[${shopName}]]，</h2><p>感谢您的缴费！</p><p>您的入驻费用发票已生成，请查收附件。</p><p><strong>发票信息：</strong></p><ul><li>发票编号：[[${invoiceNo}]]</li><li>开票日期：[[${invoiceDate}]]</li><li>金额：[[${amount}]]元</li></ul><p>如需重新下载，请访问：[[${downloadUrl}]]</p></body></html>','<html><body><h2>Dear [[${shopName}]],</h2><p>Thank you for your payment!</p><p>Your registration fee invoice has been generated, please check the attachment.</p><p><strong>Invoice Information:</strong></p><ul><li>Invoice No: [[${invoiceNo}]]</li><li>Invoice Date: [[${invoiceDate}]]</li><li>Amount: $[[${amount}]]</li></ul><p>To download again, please visit: [[${downloadUrl}]]</p></body></html>','shopName,invoiceNo,invoiceDate,amount,downloadUrl',1,'2025-12-30 09:50:00','2025-12-30 09:50:00');
/*!40000 ALTER TABLE `t_email_template` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_merchant`
--

DROP TABLE IF EXISTS `t_merchant`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_merchant` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) NOT NULL COMMENT '商家账号（登录用）',
  `password` varchar(255) NOT NULL COMMENT '密码',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `shop_name_zh` varchar(100) DEFAULT NULL COMMENT '店铺名称（中文）',
  `shop_name_en` varchar(100) DEFAULT NULL COMMENT '店铺名称（英文）',
  `logo` varchar(500) DEFAULT NULL COMMENT '店铺Logo',
  `banner` varchar(500) DEFAULT NULL COMMENT '店铺Banner图',
  `description_zh` text COMMENT '店铺简介（中文）',
  `description_en` text COMMENT '店铺简介（英文）',
  `contact_name` varchar(50) DEFAULT NULL COMMENT '联系人姓名',
  `contact_phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `business_hours` varchar(100) DEFAULT NULL COMMENT '营业时间',
  `invite_code` varchar(20) DEFAULT NULL COMMENT '邀请码（用户绑定商家用）',
  `qr_code_url` varchar(500) DEFAULT NULL COMMENT '邀请二维码URL',
  `status` tinyint(4) NOT NULL DEFAULT '0' COMMENT '状态 0-待审核 1-已通过 2-已拒绝 3-已禁用',
  `payment_status` tinyint(4) NOT NULL DEFAULT '0' COMMENT '缴费状态 0-未缴费 1-已缴费 2-已退款',
  `payment_amount` decimal(10,2) DEFAULT NULL COMMENT '缴费金额',
  `payment_time` datetime DEFAULT NULL COMMENT '缴费时间',
  `audit_remark` varchar(500) DEFAULT NULL COMMENT '审核备注',
  `audit_time` datetime DEFAULT NULL COMMENT '审核时间',
  `auditor_id` bigint(20) DEFAULT NULL COMMENT '审核人ID',
  `service_wechat` varchar(100) DEFAULT NULL COMMENT '客服微信',
  `service_phone` varchar(20) DEFAULT NULL COMMENT '客服电话',
  `promotion_code` varchar(20) DEFAULT NULL COMMENT '推广码（用于邀请其他商家）',
  `promotion_link` varchar(500) DEFAULT NULL COMMENT '推广链接',
  `referrer_code` varchar(20) DEFAULT NULL COMMENT '推荐人推广码（注册时填写）',
  `total_reward` decimal(10,2) DEFAULT '0.00' COMMENT '累计推广奖励金额',
  `available_reward` decimal(10,2) DEFAULT '0.00' COMMENT '可提现奖励金额',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除 0-否 1-是',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_username` (`username`) USING BTREE,
  UNIQUE KEY `uk_invite_code` (`invite_code`) USING BTREE,
  KEY `idx_email` (`email`) USING BTREE,
  KEY `idx_phone` (`phone`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE,
  KEY `idx_promotion_code` (`promotion_code`) USING BTREE,
  KEY `idx_payment_status` (`payment_status`) USING BTREE,
  KEY `idx_referrer_code` (`referrer_code`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='商家表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_merchant`
--

LOCK TABLES `t_merchant` WRITE;
/*!40000 ALTER TABLE `t_merchant` DISABLE KEYS */;
INSERT INTO `t_merchant` VALUES (1,'merchant','$2a$10$7DooW.SodYeG5u2NahMUmeqW6F.SOognU4i.uH3i5a4P7PW7gnrCC','merchant@test.com','13900139000','测试商家店铺','Test Merchant Shop',NULL,NULL,NULL,NULL,'张三','13900139000',NULL,'TEST001',NULL,1,0,NULL,NULL,'测试商家，自动审核通过',NULL,NULL,NULL,NULL,'M2F81897D',NULL,NULL,0.00,0.00,'2025-12-29 21:03:06','2025-12-29 21:57:39',0),(2,'huawei','$2a$10$zftOJubCAtkmcbeiJQS4puY.D90NTh3qlu/0XxDQp0OMSD3fy/bnC','huawei@qq.com','13355655445','华为','HUAWEI','/upload/20251231_013013_af77a162.jpg','/upload/20251231_013014_609b0f75.jpg',NULL,NULL,'we','13366544556','周一至周五 9:00-18:00','7261ABA4','/upload/qrcode/merchant/invite_2_1767084523315.png',1,1,1000.00,'2025-12-30 14:57:42','审核通过','2025-12-30 11:03:40',1,NULL,'13323123123','MC07AB24','http://localhost:3000/merchant/register?referrer=MC07AB24',' ',100.00,50.00,'2025-12-30 10:52:48','2026-01-02 06:55:58',0),(3,'meituan','$2a$10$iCbyspMrfZOprkI/wSTRfeQ.XbL2CnEWTJGqg1h9NPlj/yJLN5ozG','meituan@qq.com','13355466554','美团',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'7D894A68','/upload/qrcode/merchant/invite_3_1767079100101.png',1,1,1000.00,'2025-12-30 15:18:20','审核通过','2025-12-30 15:10:11',1,NULL,NULL,'MD2B316B','http://localhost:3000/merchant/register?referrer=MD2B316B',NULL,0.00,0.00,'2025-12-30 15:09:49','2026-01-01 20:35:37',0),(4,'pop','$2a$10$YL1/.bUVMWOzd.PN/oqFl.fbg2QPZo2dolHs3r7tDKwb6usZAos32','pop@qq.com',NULL,'泡泡','paopao',NULL,NULL,NULL,NULL,'wq','14455433445',NULL,'194C2648','/upload/qrcode/merchant/invite_4_1767084801080.png',1,1,1000.00,'2025-12-30 16:53:21','审核通过','2025-12-30 16:48:12',1,NULL,NULL,'M4AAADD7','http://localhost:8080/merchant/register?referrer=M4AAADD7','MC07AB24',0.00,0.00,'2025-12-30 16:47:34','2025-12-31 10:18:37',0),(5,'ali','$2a$10$k/W/vHgs8SQbr9qGbzvdXOg657BXBck0mRi9cmnccvZs5YJ2fu8Jq','1111@qq.com',NULL,'阿里','Wu',NULL,NULL,NULL,NULL,'lingfeng','13355433441',NULL,'C06DB105','/upload/qrcode/merchant/invite_5_1767308310048.png',1,1,1000.00,'2026-01-02 06:58:30','审核通过','2026-01-02 06:56:55',1,NULL,NULL,'M42857FA','http://localhost:3000/merchant/register?referrer=M42857FA',NULL,0.00,0.00,'2026-01-02 06:56:32','2026-01-02 07:01:44',0),(6,'didi','$2a$10$lA0ghBY2cM7UVRzj67mOCeQIrPQL4JQaC/Cxi5fwa0dilffs79Vdi','1121@qq.com',NULL,'滴滴','Wu',NULL,NULL,NULL,NULL,'lingfeng','16566676555',NULL,'F6FDEE34','/upload/qrcode/merchant/invite_6_1767308530161.png',1,0,1000.00,NULL,'审核通过','2026-01-02 07:02:15',1,NULL,NULL,'MA9E7F6C','http://localhost:3000/merchant/register?referrer=MA9E7F6C',NULL,0.00,0.00,'2026-01-02 07:02:10','2026-01-02 07:09:50',0),(7,'dou','$2a$10$M46DzwJd/eJFc/v1viv63eIvQv868CU9spb/MdpHBZNTNcfL0vUhO','1221@qq.com',NULL,'抖音','JUNKAI',NULL,NULL,NULL,NULL,'LUO','13344544334',NULL,'5303A767','/upload/qrcode/merchant/invite_7_1767309016030.png',1,1,1000.00,'2026-01-02 07:10:16','审核通过','2026-01-02 07:09:59',1,NULL,NULL,'M3D30BC2','http://localhost:3000/merchant/register?referrer=M3D30BC2',NULL,0.00,0.00,'2026-01-02 07:09:54','2026-01-02 07:19:43',0),(8,'ww2','$2a$10$jsz8rT7ffgdq0o/5vTmgUuD5oN/Kkyy4YRIHXgXQqRChWCZsJFnuC','2200215967@qq.com',NULL,'ww我','JUNKAI',NULL,NULL,NULL,NULL,'LUO','15577877665',NULL,'AC04508F','/upload/qrcode/merchant/invite_8_1767309647230.png',1,1,1000.00,'2026-01-02 07:20:47','审核通过','2026-01-02 07:20:25',1,NULL,NULL,'M027223B','http://localhost:3000/merchant/register?referrer=M027223B',NULL,0.00,0.00,'2026-01-02 07:20:20','2026-01-02 07:20:47',0);
/*!40000 ALTER TABLE `t_merchant` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_merchant_bill`
--

DROP TABLE IF EXISTS `t_merchant_bill`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_merchant_bill` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `merchant_id` bigint(20) NOT NULL COMMENT '商家ID',
  `bill_no` varchar(50) NOT NULL COMMENT '账单号',
  `bill_type` varchar(20) NOT NULL COMMENT '账单类型 SERVICE_FEE/PLATFORM_FEE',
  `amount` decimal(10,2) NOT NULL COMMENT '金额',
  `status` tinyint(4) NOT NULL DEFAULT '0' COMMENT '状态 0-待支付 1-已支付 2-已取消',
  `start_date` date DEFAULT NULL COMMENT '开始日期',
  `end_date` date DEFAULT NULL COMMENT '结束日期',
  `invoice_url` varchar(500) DEFAULT NULL COMMENT '发票URL',
  `paid_at` datetime DEFAULT NULL COMMENT '支付时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除 0-否 1-是',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_bill_no` (`bill_no`) USING BTREE,
  KEY `idx_merchant_id` (`merchant_id`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='商家账单表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_merchant_bill`
--

LOCK TABLES `t_merchant_bill` WRITE;
/*!40000 ALTER TABLE `t_merchant_bill` DISABLE KEYS */;
INSERT INTO `t_merchant_bill` VALUES (1,2,'BILL2026010203153911B178','SERVICE_FEE',1000.00,1,'2026-01-02','2026-01-03',NULL,'2026-01-02 03:21:52','2026-01-02 03:15:40','2026-01-02 03:21:52',0),(2,2,'BILL20260102032904690D4B','PLATFORM_FEE',188.00,1,'2026-01-02','2026-01-03','/upload/invoices/invoice_INV20260102383792.pdf','2026-01-02 03:29:08','2026-01-02 03:29:05','2026-01-02 03:29:09',0),(3,2,'BILL20260102033450AD7CA3','SERVICE_FEE',12.00,1,'2026-01-02','2026-01-09','/upload/invoices/invoice_INV20260102180003.pdf','2026-01-02 03:34:55','2026-01-02 03:34:51','2026-01-02 03:34:56',0),(4,2,'BILL202601020338492EF04F','PLATFORM_FEE',100.00,1,'2026-01-02','2026-01-07','/upload/invoices/invoice_INV20260102567774.pdf','2026-01-02 03:39:07','2026-01-02 03:38:50','2026-01-02 03:39:07',0),(5,2,'BILL20260102034016479327','SERVICE_FEE',100.00,1,'2026-01-02','2026-01-03','/upload/invoices/invoice_INV20260102530144.pdf','2026-01-02 03:40:23','2026-01-02 03:40:16','2026-01-02 03:40:23',0),(6,2,'BILL202601020345277A8584','SERVICE_FEE',1000.00,1,'2026-01-08','2026-01-03','/upload/invoices/invoice_INV20260102579205.pdf','2026-01-02 03:45:39','2026-01-02 03:45:27','2026-01-02 03:45:40',0),(7,2,'BILL202601020400281E0B92','SERVICE_FEE',100.00,1,'2026-01-02','2026-01-03','/upload/invoices/invoice_INV20260102121190.pdf','2026-01-02 04:00:33','2026-01-02 04:00:29','2026-01-02 04:00:33',0);
/*!40000 ALTER TABLE `t_merchant_bill` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_merchant_config`
--

DROP TABLE IF EXISTS `t_merchant_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_merchant_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `merchant_id` bigint(20) NOT NULL COMMENT '商家ID',
  `pickup_address_zh` varchar(500) DEFAULT NULL COMMENT '自提地址（中文）',
  `pickup_address_en` varchar(500) DEFAULT NULL COMMENT '自提地址（英文）',
  `delivery_area_zh` varchar(500) DEFAULT NULL COMMENT '配送范围说明（中文）',
  `delivery_area_en` varchar(500) DEFAULT NULL COMMENT '配送范围说明（英文）',
  `min_order_amount` decimal(10,2) DEFAULT '0.00' COMMENT '起送金额',
  `delivery_fee` decimal(10,2) DEFAULT '0.00' COMMENT '配送费',
  `free_delivery_amount` decimal(10,2) DEFAULT NULL COMMENT '免配送费金额',
  `support_pickup` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否支持自提',
  `support_delivery` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否支持配送',
  `delivery_time_slots` json DEFAULT NULL COMMENT '配送时间段配置（JSON格式）',
  `pickup_time_slots` json DEFAULT NULL COMMENT '自提时间段配置（JSON格式）',
  `advance_booking_days` int(11) DEFAULT '0' COMMENT '提前预订天数',
  `max_delivery_distance` decimal(10,2) DEFAULT NULL COMMENT '最大配送距离（公里）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除 0-否 1-是',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_merchant_id` (`merchant_id`) USING BTREE,
  KEY `idx_merchant_id` (`merchant_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='商家配置表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_merchant_config`
--

LOCK TABLES `t_merchant_config` WRITE;
/*!40000 ALTER TABLE `t_merchant_config` DISABLE KEYS */;
INSERT INTO `t_merchant_config` VALUES (1,1,NULL,NULL,NULL,NULL,0.00,0.00,NULL,1,1,NULL,NULL,0,NULL,'2025-12-29 21:57:44','2025-12-29 21:57:44',0),(2,2,NULL,NULL,NULL,NULL,0.00,0.00,NULL,1,1,NULL,NULL,0,NULL,'2025-12-30 11:04:04','2025-12-30 22:16:00',0);
/*!40000 ALTER TABLE `t_merchant_config` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_merchant_email`
--

DROP TABLE IF EXISTS `t_merchant_email`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_merchant_email` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `merchant_id` bigint(20) NOT NULL COMMENT '商家ID',
  `email` varchar(200) NOT NULL COMMENT '邮箱地址',
  `name` varchar(100) DEFAULT NULL COMMENT '邮箱名称（如：订单通知邮箱）',
  `purpose` varchar(200) DEFAULT NULL COMMENT '用途说明',
  `is_default` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否默认邮箱：0-否 1-是',
  `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '状态：0-禁用 1-启用',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除 0-否 1-是',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_merchant_id` (`merchant_id`) USING BTREE,
  KEY `idx_is_default` (`is_default`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE,
  KEY `idx_deleted` (`deleted`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='商家邮箱配置表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_merchant_email`
--

LOCK TABLES `t_merchant_email` WRITE;
/*!40000 ALTER TABLE `t_merchant_email` DISABLE KEYS */;
INSERT INTO `t_merchant_email` VALUES (1,2,'2200215967@qq.com','通知','111',1,1,'2026-01-01 23:14:32','2026-01-01 23:14:32',0);
/*!40000 ALTER TABLE `t_merchant_email` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_merchant_invoice`
--

DROP TABLE IF EXISTS `t_merchant_invoice`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_merchant_invoice` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `merchant_id` bigint(20) NOT NULL COMMENT '商家ID',
  `invoice_no` varchar(50) NOT NULL COMMENT '发票编号',
  `invoice_type` varchar(20) NOT NULL DEFAULT 'REGISTRATION' COMMENT '发票类型 REGISTRATION-入驻费 SUBSCRIPTION-订阅费',
  `amount` decimal(10,2) NOT NULL COMMENT '发票金额',
  `tax_amount` decimal(10,2) DEFAULT '0.00' COMMENT '税额',
  `total_amount` decimal(10,2) NOT NULL COMMENT '总金额（含税）',
  `invoice_title` varchar(200) DEFAULT NULL COMMENT '发票抬头',
  `tax_number` varchar(50) DEFAULT NULL COMMENT '税号',
  `invoice_date` date NOT NULL COMMENT '开票日期',
  `pdf_url` varchar(500) DEFAULT NULL COMMENT 'PDF���件URL',
  `status` tinyint(4) NOT NULL DEFAULT '0' COMMENT '状态 0-待生成 1-已生成 2-已发送 3-已作废',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_invoice_no` (`invoice_no`) USING BTREE,
  KEY `idx_merchant_id` (`merchant_id`) USING BTREE,
  KEY `idx_invoice_type` (`invoice_type`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='商家发票表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_merchant_invoice`
--

LOCK TABLES `t_merchant_invoice` WRITE;
/*!40000 ALTER TABLE `t_merchant_invoice` DISABLE KEYS */;
INSERT INTO `t_merchant_invoice` VALUES (1,2,'INV20251230636243','REGISTRATION',1000.00,60.00,1060.00,NULL,NULL,'2025-12-30','/upload/invoices/invoice_INV20251230636243.pdf',1,'商家入驻费用发票','2025-12-30 14:57:42','2025-12-30 14:57:43'),(2,3,'INV20251230293677','REGISTRATION',1000.00,60.00,1060.00,NULL,NULL,'2025-12-30','/upload/invoices/invoice_INV20251230293677.pdf',1,'商家入驻费用发票','2025-12-30 15:18:20','2025-12-30 15:18:21'),(3,4,'INV20251230129673','REGISTRATION',1000.00,60.00,1060.00,'泡泡',NULL,'2025-12-30','/upload/invoices/invoice_INV20251230129673.pdf',1,'商家入驻费用发票','2025-12-30 16:53:21','2025-12-30 16:53:22'),(4,2,'INV20260102383792','PLATFORM_FEE',188.00,11.28,199.28,'华为',NULL,'2026-01-02','/upload/invoices/invoice_INV20260102383792.pdf',1,'账单号: BILL20260102032904690D4B','2026-01-02 03:29:08','2026-01-02 03:29:09'),(5,2,'INV20260102180003','SERVICE_FEE',12.00,0.72,12.72,'华为',NULL,'2026-01-02','/upload/invoices/invoice_INV20260102180003.pdf',1,'账单号: BILL20260102033450AD7CA3','2026-01-02 03:34:55','2026-01-02 03:34:56'),(6,2,'INV20260102567774','PLATFORM_FEE',100.00,6.00,106.00,'华为',NULL,'2026-01-02','/upload/invoices/invoice_INV20260102567774.pdf',1,'账单号: BILL202601020338492EF04F','2026-01-02 03:39:07','2026-01-02 03:39:07'),(7,2,'INV20260102530144','SERVICE_FEE',100.00,6.00,106.00,'华为',NULL,'2026-01-02','/upload/invoices/invoice_INV20260102530144.pdf',1,'账单号: BILL20260102034016479327','2026-01-02 03:40:23','2026-01-02 03:40:23'),(8,2,'INV20260102579205','SERVICE_FEE',1000.00,60.00,1060.00,'华为',NULL,'2026-01-02','/upload/invoices/invoice_INV20260102579205.pdf',1,'账单号: BILL202601020345277A8584','2026-01-02 03:45:39','2026-01-02 03:45:40'),(9,2,'INV20260102121190','SERVICE_FEE',100.00,6.00,106.00,'华为',NULL,'2026-01-02','/upload/invoices/invoice_INV20260102121190.pdf',2,'账单号: BILL202601020400281E0B92','2026-01-02 04:00:33','2026-01-02 04:40:10'),(10,5,'INV20260102384770','REGISTRATION',1000.00,60.00,1060.00,'阿里',NULL,'2026-01-02','/upload/invoices/invoice_INV20260102384770.pdf',2,'商家入驻费用发票','2026-01-02 06:58:09','2026-01-02 06:58:20'),(11,5,'INV20260102450327','REGISTRATION',1000.00,60.00,1060.00,'阿里',NULL,'2026-01-02','/upload/invoices/invoice_INV20260102450327.pdf',2,'商家入驻费用发票','2026-01-02 06:58:51','2026-01-02 06:59:01'),(12,7,'INV20260102490821','REGISTRATION',1000.00,60.00,1060.00,'抖音',NULL,'2026-01-02','/upload/invoices/invoice_INV20260102490821.pdf',2,'商家入驻费用发票','2026-01-02 07:10:16','2026-01-02 07:10:17'),(13,8,'INV20260102149154','REGISTRATION',1000.00,60.00,1060.00,'ww我',NULL,'2026-01-02','/upload/invoices/invoice_INV20260102149154.pdf',2,'商家入驻费用发票','2026-01-02 07:20:47','2026-01-02 07:20:48');
/*!40000 ALTER TABLE `t_merchant_invoice` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_merchant_notification_config`
--

DROP TABLE IF EXISTS `t_merchant_notification_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_merchant_notification_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `merchant_id` bigint(20) NOT NULL COMMENT '商家ID',
  `notification_type` varchar(50) NOT NULL COMMENT '通知类型：ORDER_CREATED/ORDER_CONFIRMED/ORDER_SHIPPED/ORDER_COMPLETED/ORDER_CANCELLED/PAYMENT_SUCCESS/PAYMENT_FAILED',
  `notification_channel` varchar(20) NOT NULL COMMENT '通知渠道：EMAIL/SMS/PUSH/WECHAT',
  `enabled` tinyint(4) NOT NULL DEFAULT '1' COMMENT '是否启用：0-禁用 1-启用',
  `config_json` text COMMENT '配置信息（JSON格式）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除 0-否 1-是',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_merchant_id` (`merchant_id`) USING BTREE,
  KEY `idx_notification_type` (`notification_type`) USING BTREE,
  KEY `idx_notification_channel` (`notification_channel`) USING BTREE,
  KEY `idx_enabled` (`enabled`) USING BTREE,
  KEY `idx_deleted` (`deleted`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=57 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='商家通知配置表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_merchant_notification_config`
--

LOCK TABLES `t_merchant_notification_config` WRITE;
/*!40000 ALTER TABLE `t_merchant_notification_config` DISABLE KEYS */;
INSERT INTO `t_merchant_notification_config` VALUES (1,2,'ORDER_CREATED','EMAIL',1,NULL,'2025-12-31 16:37:23','2025-12-31 16:37:23',0),(2,2,'ORDER_CREATED','SMS',0,NULL,'2025-12-31 16:37:23','2025-12-31 16:37:23',0),(3,2,'ORDER_CREATED','PUSH',1,NULL,'2025-12-31 16:37:23','2026-01-01 07:41:11',0),(4,2,'ORDER_CREATED','WECHAT',0,NULL,'2025-12-31 16:37:23','2025-12-31 16:37:23',0),(5,2,'ORDER_CONFIRMED','EMAIL',1,NULL,'2025-12-31 16:37:23','2025-12-31 16:37:23',0),(6,2,'ORDER_CONFIRMED','SMS',0,NULL,'2025-12-31 16:37:23','2025-12-31 16:37:23',0),(7,2,'ORDER_CONFIRMED','PUSH',1,NULL,'2025-12-31 16:37:23','2026-01-01 07:41:10',0),(8,2,'ORDER_CONFIRMED','WECHAT',0,NULL,'2025-12-31 16:37:23','2025-12-31 16:37:23',0),(9,2,'ORDER_SHIPPED','EMAIL',1,NULL,'2025-12-31 16:37:23','2025-12-31 16:37:23',0),(10,2,'ORDER_SHIPPED','SMS',0,NULL,'2025-12-31 16:37:23','2025-12-31 16:37:23',0),(11,2,'ORDER_SHIPPED','PUSH',1,NULL,'2025-12-31 16:37:23','2026-01-01 07:41:06',0),(12,2,'ORDER_SHIPPED','WECHAT',0,NULL,'2025-12-31 16:37:23','2025-12-31 16:37:23',0),(13,2,'ORDER_COMPLETED','EMAIL',1,NULL,'2025-12-31 16:37:23','2025-12-31 16:37:23',0),(14,2,'ORDER_COMPLETED','SMS',0,NULL,'2025-12-31 16:37:23','2025-12-31 16:37:23',0),(15,2,'ORDER_COMPLETED','PUSH',1,NULL,'2025-12-31 16:37:23','2026-01-01 07:41:13',0),(16,2,'ORDER_COMPLETED','WECHAT',0,NULL,'2025-12-31 16:37:23','2025-12-31 16:37:23',0),(17,2,'ORDER_CANCELLED','EMAIL',1,NULL,'2025-12-31 16:37:23','2025-12-31 16:37:23',0),(18,2,'ORDER_CANCELLED','SMS',0,NULL,'2025-12-31 16:37:23','2025-12-31 16:37:23',0),(19,2,'ORDER_CANCELLED','PUSH',1,NULL,'2025-12-31 16:37:23','2026-01-01 07:41:08',0),(20,2,'ORDER_CANCELLED','WECHAT',0,NULL,'2025-12-31 16:37:23','2025-12-31 16:37:23',0),(21,2,'PAYMENT_SUCCESS','EMAIL',1,NULL,'2025-12-31 16:37:23','2025-12-31 16:37:23',0),(22,2,'PAYMENT_SUCCESS','SMS',0,NULL,'2025-12-31 16:37:23','2025-12-31 16:37:23',0),(23,2,'PAYMENT_SUCCESS','PUSH',1,NULL,'2025-12-31 16:37:23','2026-01-01 07:41:03',0),(24,2,'PAYMENT_SUCCESS','WECHAT',0,NULL,'2025-12-31 16:37:23','2025-12-31 16:37:23',0),(25,2,'PAYMENT_FAILED','EMAIL',1,NULL,'2025-12-31 16:37:23','2025-12-31 16:37:23',0),(26,2,'PAYMENT_FAILED','SMS',0,NULL,'2025-12-31 16:37:23','2025-12-31 16:37:23',0),(27,2,'PAYMENT_FAILED','PUSH',1,NULL,'2025-12-31 16:37:23','2026-01-01 07:41:07',0),(28,2,'PAYMENT_FAILED','WECHAT',0,NULL,'2025-12-31 16:37:23','2025-12-31 16:37:23',0),(29,3,'ORDER_CREATED','EMAIL',1,NULL,'2026-01-01 23:04:00','2026-01-01 23:04:00',0),(30,3,'ORDER_CREATED','SMS',0,NULL,'2026-01-01 23:04:00','2026-01-01 23:04:00',0),(31,3,'ORDER_CREATED','PUSH',0,NULL,'2026-01-01 23:04:00','2026-01-01 23:04:00',0),(32,3,'ORDER_CREATED','WECHAT',0,NULL,'2026-01-01 23:04:00','2026-01-01 23:04:00',0),(33,3,'ORDER_CONFIRMED','EMAIL',1,NULL,'2026-01-01 23:04:00','2026-01-01 23:04:00',0),(34,3,'ORDER_CONFIRMED','SMS',0,NULL,'2026-01-01 23:04:00','2026-01-01 23:04:00',0),(35,3,'ORDER_CONFIRMED','PUSH',0,NULL,'2026-01-01 23:04:00','2026-01-01 23:04:00',0),(36,3,'ORDER_CONFIRMED','WECHAT',0,NULL,'2026-01-01 23:04:00','2026-01-01 23:04:00',0),(37,3,'ORDER_SHIPPED','EMAIL',1,NULL,'2026-01-01 23:04:00','2026-01-01 23:04:00',0),(38,3,'ORDER_SHIPPED','SMS',0,NULL,'2026-01-01 23:04:00','2026-01-01 23:04:00',0),(39,3,'ORDER_SHIPPED','PUSH',0,NULL,'2026-01-01 23:04:00','2026-01-01 23:04:00',0),(40,3,'ORDER_SHIPPED','WECHAT',0,NULL,'2026-01-01 23:04:00','2026-01-01 23:04:00',0),(41,3,'ORDER_COMPLETED','EMAIL',1,NULL,'2026-01-01 23:04:00','2026-01-01 23:04:00',0),(42,3,'ORDER_COMPLETED','SMS',0,NULL,'2026-01-01 23:04:00','2026-01-01 23:04:00',0),(43,3,'ORDER_COMPLETED','PUSH',0,NULL,'2026-01-01 23:04:00','2026-01-01 23:04:00',0),(44,3,'ORDER_COMPLETED','WECHAT',0,NULL,'2026-01-01 23:04:00','2026-01-01 23:04:00',0),(45,3,'ORDER_CANCELLED','EMAIL',1,NULL,'2026-01-01 23:04:00','2026-01-01 23:04:00',0),(46,3,'ORDER_CANCELLED','SMS',0,NULL,'2026-01-01 23:04:00','2026-01-01 23:04:00',0),(47,3,'ORDER_CANCELLED','PUSH',0,NULL,'2026-01-01 23:04:00','2026-01-01 23:04:00',0),(48,3,'ORDER_CANCELLED','WECHAT',0,NULL,'2026-01-01 23:04:00','2026-01-01 23:04:00',0),(49,3,'PAYMENT_SUCCESS','EMAIL',1,NULL,'2026-01-01 23:04:00','2026-01-01 23:04:00',0),(50,3,'PAYMENT_SUCCESS','SMS',0,NULL,'2026-01-01 23:04:00','2026-01-01 23:04:00',0),(51,3,'PAYMENT_SUCCESS','PUSH',0,NULL,'2026-01-01 23:04:00','2026-01-01 23:04:00',0),(52,3,'PAYMENT_SUCCESS','WECHAT',0,NULL,'2026-01-01 23:04:00','2026-01-01 23:04:00',0),(53,3,'PAYMENT_FAILED','EMAIL',1,NULL,'2026-01-01 23:04:00','2026-01-01 23:04:00',0),(54,3,'PAYMENT_FAILED','SMS',0,NULL,'2026-01-01 23:04:00','2026-01-01 23:04:00',0),(55,3,'PAYMENT_FAILED','PUSH',0,NULL,'2026-01-01 23:04:00','2026-01-01 23:04:00',0),(56,3,'PAYMENT_FAILED','WECHAT',0,NULL,'2026-01-01 23:04:00','2026-01-01 23:04:00',0);
/*!40000 ALTER TABLE `t_merchant_notification_config` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_merchant_subscription`
--

DROP TABLE IF EXISTS `t_merchant_subscription`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_merchant_subscription` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `merchant_id` bigint(20) NOT NULL COMMENT '商家ID',
  `plan_id` bigint(20) NOT NULL COMMENT '套餐ID',
  `start_date` date NOT NULL COMMENT '开始日期',
  `end_date` date NOT NULL COMMENT '结束日期',
  `status` varchar(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态 ACTIVE/EXPIRED/CANCELLED',
  `auto_renew` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否自动续费 0-否 1-是',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_merchant_id` (`merchant_id`) USING BTREE,
  KEY `idx_plan_id` (`plan_id`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE,
  KEY `idx_end_date` (`end_date`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='商家订阅表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_merchant_subscription`
--

LOCK TABLES `t_merchant_subscription` WRITE;
/*!40000 ALTER TABLE `t_merchant_subscription` DISABLE KEYS */;
/*!40000 ALTER TABLE `t_merchant_subscription` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_merchant_ticket`
--

DROP TABLE IF EXISTS `t_merchant_ticket`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_merchant_ticket` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `ticket_no` varchar(50) NOT NULL COMMENT '工单号',
  `merchant_id` bigint(20) NOT NULL COMMENT '商家ID',
  `user_id` bigint(20) DEFAULT NULL COMMENT '用户ID（如果是用户提交的工单）',
  `category` varchar(50) NOT NULL COMMENT '工单分类 ORDER/PRODUCT/PAYMENT/ACCOUNT/OTHER',
  `priority` varchar(20) NOT NULL DEFAULT 'NORMAL' COMMENT '优先级 LOW/NORMAL/HIGH/URGENT',
  `status` varchar(20) NOT NULL DEFAULT 'OPEN' COMMENT '状态 OPEN/IN_PROGRESS/RESOLVED/CLOSED',
  `title` varchar(200) NOT NULL COMMENT '工单标题',
  `content` text NOT NULL COMMENT '工单内容',
  `admin_id` bigint(20) DEFAULT NULL COMMENT '处理管理员ID',
  `admin_reply` text COMMENT '管理员回复',
  `resolved_at` datetime DEFAULT NULL COMMENT '解决时间',
  `closed_at` datetime DEFAULT NULL COMMENT '关闭时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除 0-否 1-是',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_ticket_no` (`ticket_no`) USING BTREE,
  KEY `idx_merchant_id` (`merchant_id`) USING BTREE,
  KEY `idx_user_id` (`user_id`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE,
  KEY `idx_category` (`category`) USING BTREE,
  KEY `idx_priority` (`priority`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='商家工单表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_merchant_ticket`
--

LOCK TABLES `t_merchant_ticket` WRITE;
/*!40000 ALTER TABLE `t_merchant_ticket` DISABLE KEYS */;
INSERT INTO `t_merchant_ticket` VALUES (1,'TK1767115383725BF9B76BA',2,NULL,'PAYMENT','HIGH','RESOLVED','111','1111',1,'wwwww','2025-12-31 01:29:06',NULL,'2025-12-31 01:23:04','2025-12-31 01:29:06',0);
/*!40000 ALTER TABLE `t_merchant_ticket` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_merchant_violation`
--

DROP TABLE IF EXISTS `t_merchant_violation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_merchant_violation` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `merchant_id` bigint(20) NOT NULL COMMENT '商家ID',
  `violation_type` varchar(50) NOT NULL COMMENT '违规类型 FAKE_PRODUCT/ILLEGAL_CONTENT/UNPAID_BILL/OTHER',
  `title` varchar(200) NOT NULL COMMENT '违规标题',
  `description` text NOT NULL COMMENT '违规描述',
  `penalty_type` varchar(50) DEFAULT NULL COMMENT '处罚类型 WARNING/FINE/SUSPEND/BAN',
  `penalty_amount` decimal(10,2) DEFAULT NULL COMMENT '处罚金额',
  `penalty_duration_days` int(11) DEFAULT NULL COMMENT '处罚时长（天）',
  `status` varchar(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态 PENDING/APPROVED/REJECTED',
  `admin_id` bigint(20) DEFAULT NULL COMMENT '处理管理员ID',
  `admin_remark` varchar(500) DEFAULT NULL COMMENT '管理员备注',
  `resolved_at` datetime DEFAULT NULL COMMENT '处理时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除 0-否 1-是',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_merchant_id` (`merchant_id`) USING BTREE,
  KEY `idx_violation_type` (`violation_type`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='商家违规记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_merchant_violation`
--

LOCK TABLES `t_merchant_violation` WRITE;
/*!40000 ALTER TABLE `t_merchant_violation` DISABLE KEYS */;
/*!40000 ALTER TABLE `t_merchant_violation` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_notification_record`
--

DROP TABLE IF EXISTS `t_notification_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_notification_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `merchant_id` bigint(20) DEFAULT NULL COMMENT '商家ID',
  `order_id` bigint(20) DEFAULT NULL COMMENT '订单ID',
  `type` varchar(50) NOT NULL COMMENT '通知类型 ORDER_NEW/ORDER_CONFIRMED/ORDER_SHIPPED/ORDER_COMPLETED',
  `title` varchar(200) DEFAULT NULL COMMENT '通知标题',
  `content` text COMMENT '通知内容',
  `recipient_email` varchar(100) DEFAULT NULL COMMENT '收件人邮箱',
  `status` tinyint(4) NOT NULL DEFAULT '0' COMMENT '状态 0-待发送 1-已发送 2-发送失败',
  `sent_at` datetime DEFAULT NULL COMMENT '发送时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_merchant_id` (`merchant_id`) USING BTREE,
  KEY `idx_order_id` (`order_id`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=35 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='通知记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_notification_record`
--

LOCK TABLES `t_notification_record` WRITE;
/*!40000 ALTER TABLE `t_notification_record` DISABLE KEYS */;
INSERT INTO `t_notification_record` VALUES (1,2,1,'ORDER_NEW','新订单通知','您有一个新订单！\n\n订单号：O20260101024047510E62\n订单金额：500000.00元\n下单时间：2026-01-01T02:40:47.677259500\n\n请及时处理。','huawei@qq.com',2,NULL,'2026-01-01 02:40:48'),(2,2,1,'ORDER_CANCELLED','订单已取消','订单 O20260101024047510E62 已取消。','huawei@qq.com',2,NULL,'2026-01-01 08:04:03'),(3,2,1,'ORDER_CANCELLED','订单已取消','订单 O20260101024047510E62 已取消。','huawei@qq.com',2,NULL,'2026-01-01 08:04:13'),(4,2,2,'ORDER_NEW','新订单通知','您有一个新订单！订单号：O202601010804526AE52F，金额：500000.00元','huawei@qq.com',2,NULL,'2026-01-01 08:04:52'),(5,2,2,'ORDER_CONFIRMED','订单已确认','订单 O202601010804526AE52F 已确认。','huawei@qq.com',2,NULL,'2026-01-01 08:05:49'),(6,2,2,'ORDER_SHIPPED','订单已发货','订单 O202601010804526AE52F 已发货。','huawei@qq.com',2,NULL,'2026-01-01 08:06:04'),(7,2,2,'ORDER_COMPLETED','订单已完成','订单 O202601010804526AE52F 已完成。','huawei@qq.com',2,NULL,'2026-01-01 10:46:31'),(8,2,2,'ORDER_CANCELLED','订单已取消','订单 O202601010804526AE52F 已取消。','huawei@qq.com',2,NULL,'2026-01-01 10:46:42'),(9,2,3,'ORDER_NEW','新订单通知','您有一个新订单！订单号：O202601011125414409CD，金额：700000.00元','huawei@qq.com',2,NULL,'2026-01-01 11:25:42'),(10,2,3,'ORDER_CONFIRMED','订单已确认','订单 O202601011125414409CD 已确认。','huawei@qq.com',2,NULL,'2026-01-01 11:26:44'),(11,2,3,'ORDER_SHIPPED','订单已发货','订单 O202601011125414409CD 已发货。','huawei@qq.com',2,NULL,'2026-01-01 11:27:26'),(12,2,3,'ORDER_COMPLETED','订单已完成','订单 O202601011125414409CD 已完成。','huawei@qq.com',2,NULL,'2026-01-01 12:03:19'),(13,2,4,'ORDER_NEW','新订单通知','您有一个新订单！订单号：O20260101120822EDB884，金额：500000.00元','huawei@qq.com',2,NULL,'2026-01-01 12:08:22'),(14,2,4,'ORDER_CANCELLED','订单已取消','订单 O20260101120822EDB884 已取消。','huawei@qq.com',2,NULL,'2026-01-01 12:13:06'),(15,2,5,'ORDER_NEW','新订单通知','您有一个新订单！订单号：O20260101132904AE9B31，金额：500000.00元','huawei@qq.com',2,NULL,'2026-01-01 13:29:05'),(16,2,5,'ORDER_CANCELLED','订单已取消','订单 O20260101132904AE9B31 已取消。','huawei@qq.com',2,NULL,'2026-01-01 13:35:37'),(17,2,6,'ORDER_NEW','新订单通知','您有一个新订单！订单号：O202601011556533B2758，金额：500000.00元','huawei@qq.com',2,NULL,'2026-01-01 15:56:54'),(18,2,7,'ORDER_NEW','新订单通知','您有一个新订单！订单号：O2026010116345011616F，金额：500000.00元','huawei@qq.com',2,NULL,'2026-01-01 16:34:51'),(19,2,8,'ORDER_NEW','新订单通知','您有一个新订单！订单号：O202601011704139F04B5，金额：500000.00元','huawei@qq.com',2,NULL,'2026-01-01 17:04:14'),(20,2,9,'ORDER_NEW','新订单通知','您有一个新订单！订单号：O202601011708463DA4D6，金额：700000.00元','huawei@qq.com',2,NULL,'2026-01-01 17:08:47'),(21,2,10,'ORDER_NEW','新订单通知','您有一个新订单！订单号：O20260101170927585233，金额：700000.00元','huawei@qq.com',2,NULL,'2026-01-01 17:09:28'),(22,2,11,'ORDER_NEW','新订单通知','您有一个新订单！订单号：O20260101172121BBA869，金额：500000.00元','huawei@qq.com',2,NULL,'2026-01-01 17:21:21'),(23,2,12,'ORDER_NEW','新订单通知','您有一个新订单！订单号：O20260101172711BEB99F，金额：500000.00元','huawei@qq.com',2,NULL,'2026-01-01 17:27:12'),(24,2,12,'ORDER_CANCELLED','订单已取消','订单 O20260101172711BEB99F 已取消。','huawei@qq.com',2,NULL,'2026-01-01 17:46:28'),(25,2,11,'ORDER_CANCELLED','订单已取消','订单 O20260101172121BBA869 已取消。','huawei@qq.com',2,NULL,'2026-01-01 17:46:30'),(26,2,10,'ORDER_CANCELLED','订单已取消','订单 O20260101170927585233 已取消。','huawei@qq.com',2,NULL,'2026-01-01 17:46:35'),(27,2,9,'ORDER_CANCELLED','订单已取消','订单 O202601011708463DA4D6 已取消。','huawei@qq.com',2,NULL,'2026-01-01 17:46:39'),(28,2,8,'ORDER_CANCELLED','订单已取消','订单 O202601011704139F04B5 已取消。','huawei@qq.com',2,NULL,'2026-01-01 17:46:41'),(29,2,7,'ORDER_CANCELLED','订单已取消','订单 O2026010116345011616F 已取消。','huawei@qq.com',2,NULL,'2026-01-01 17:46:43'),(30,2,6,'ORDER_CANCELLED','订单已取消','订单 O202601011556533B2758 已取消。','huawei@qq.com',2,NULL,'2026-01-01 17:46:48'),(31,2,13,'ORDER_NEW','新订单通知','您有一个新订单！订单号：O2026010118483122385E，金额：900000.00元','huawei@qq.com',2,NULL,'2026-01-01 18:48:32'),(32,2,13,'ORDER_CONFIRMED','订单已确认','订单 O2026010118483122385E 已确认。','huawei@qq.com',2,NULL,'2026-01-01 18:48:54'),(33,2,13,'ORDER_SHIPPED','订单已发货','订单 O2026010118483122385E 已发货。','huawei@qq.com',2,NULL,'2026-01-01 18:49:22'),(34,2,13,'ORDER_COMPLETED','订单已完成','订单 O2026010118483122385E 已完成。','huawei@qq.com',2,NULL,'2026-01-01 18:53:39');
/*!40000 ALTER TABLE `t_notification_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_operation_log`
--

DROP TABLE IF EXISTS `t_operation_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_operation_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(20) DEFAULT NULL COMMENT '用户ID',
  `user_type` varchar(20) NOT NULL COMMENT '用户类型 ADMIN/MERCHANT/USER',
  `username` varchar(50) DEFAULT NULL COMMENT '用户名',
  `operation` varchar(50) NOT NULL COMMENT '操作类型 CREATE/UPDATE/DELETE/AUDIT/LOGIN',
  `module` varchar(50) DEFAULT NULL COMMENT '模块 MERCHANT/ORDER/PRODUCT/CONFIG',
  `description` varchar(500) DEFAULT NULL COMMENT '操作描述',
  `request_url` varchar(500) DEFAULT NULL COMMENT '请求URL',
  `request_method` varchar(10) DEFAULT NULL COMMENT '请求方法',
  `ip_address` varchar(50) DEFAULT NULL COMMENT 'IP地址',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_user_type` (`user_type`) USING BTREE,
  KEY `idx_module` (`module`) USING BTREE,
  KEY `idx_operation` (`operation`) USING BTREE,
  KEY `idx_created_at` (`created_at`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='操作日志表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_operation_log`
--

LOCK TABLES `t_operation_log` WRITE;
/*!40000 ALTER TABLE `t_operation_log` DISABLE KEYS */;
INSERT INTO `t_operation_log` VALUES (1,1,'ADMIN','admin','LOGIN','SYSTEM','管理员登录','/api/auth/admin/login','POST','127.0.0.1','2026-01-02 06:56:44'),(2,1,'ADMIN','admin','AUDIT','MERCHANT','审核商家','/api/admin/merchants/5/audit','POST','127.0.0.1','2026-01-02 06:57:02'),(3,1,'ADMIN','admin','AUDIT','MERCHANT','审核商家','/api/admin/merchants/5/audit','POST','127.0.0.1','2026-01-02 06:57:07'),(4,1,'ADMIN','admin','AUDIT','MERCHANT','审核商家','/api/admin/merchants/5/audit','POST','127.0.0.1','2026-01-02 06:57:18'),(5,1,'ADMIN','admin','AUDIT','MERCHANT','审核商家','/api/admin/merchants/5/audit','POST','127.0.0.1','2026-01-02 06:57:28'),(6,1,'ADMIN','admin','AUDIT','MERCHANT','审核商家','/api/admin/merchants/5/audit','POST','127.0.0.1','2026-01-02 06:57:38'),(7,1,'ADMIN','admin','AUDIT','MERCHANT','审核商家','/api/admin/merchants/5/audit','POST','127.0.0.1','2026-01-02 06:57:49'),(8,1,'ADMIN','admin','AUDIT','MERCHANT','审核商家','/api/admin/merchants/6/audit','POST','127.0.0.1','2026-01-02 07:02:26'),(9,1,'ADMIN','admin','AUDIT','MERCHANT','审核商家','/api/admin/merchants/7/audit','POST','127.0.0.1','2026-01-02 07:09:59'),(10,1,'ADMIN','admin','AUDIT','MERCHANT','审核商家','/api/admin/merchants/8/audit','POST','127.0.0.1','2026-01-02 07:20:25'),(11,1,'ADMIN','admin','LOGIN','SYSTEM','管理员登录','/api/auth/admin/login','POST','127.0.0.1','2026-01-03 13:28:12'),(12,1,'ADMIN','admin','LOGIN','SYSTEM','管理员登录','/api/auth/admin/login','POST','127.0.0.1','2026-01-03 19:12:33'),(13,1,'ADMIN','admin','LOGIN','SYSTEM','管理员登录','/api/auth/admin/login','POST','127.0.0.1','2026-01-03 19:59:14'),(14,1,'ADMIN','admin','LOGIN','SYSTEM','管理员登录','/api/auth/admin/login','POST','127.0.0.1','2026-01-03 20:03:55'),(15,1,'ADMIN','admin','LOGIN','SYSTEM','管理员登录','/api/auth/admin/login','POST','127.0.0.1','2026-01-03 20:05:35'),(16,1,'ADMIN','admin','LOGIN','SYSTEM','管理员登录','/api/auth/admin/login','POST','127.0.0.1','2026-01-03 20:05:41'),(17,1,'ADMIN','admin','LOGIN','SYSTEM','管理员登录','/api/auth/admin/login','POST','127.0.0.1','2026-01-03 20:08:28'),(18,1,'ADMIN','admin','LOGIN','SYSTEM','管理员登录','/api/auth/admin/login','POST','127.0.0.1','2026-01-03 20:09:20'),(19,1,'ADMIN','admin','LOGIN','SYSTEM','管理员登录','/api/auth/admin/login','POST','127.0.0.1','2026-01-03 21:45:57'),(20,1,'ADMIN','admin','LOGIN','SYSTEM','管理员登录','/api/auth/admin/login','POST','127.0.0.1','2026-01-03 23:32:39');
/*!40000 ALTER TABLE `t_operation_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_order`
--

DROP TABLE IF EXISTS `t_order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_order` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_no` varchar(32) NOT NULL COMMENT '订单号',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `merchant_id` bigint(20) NOT NULL COMMENT '商家ID',
  `status` varchar(20) NOT NULL DEFAULT 'PENDING' COMMENT '订单状态 PENDING/CONFIRMED/SHIPPING/COMPLETED/CANCELLED',
  `product_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '商品总金额',
  `delivery_fee` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '配送费',
  `discount_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '优惠金额',
  `total_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '订单总金额',
  `product_count` int(11) NOT NULL DEFAULT '0' COMMENT '商品数量',
  `delivery_type` tinyint(4) NOT NULL DEFAULT '1' COMMENT '配送方式 1-自提 2-配送',
  `receiver_name` varchar(50) DEFAULT NULL COMMENT '收货人姓名',
  `receiver_phone` varchar(20) DEFAULT NULL COMMENT '收货人电话',
  `receiver_address` varchar(500) DEFAULT NULL COMMENT '收货地址',
  `expected_date` date DEFAULT NULL COMMENT '期望提货/配送日期',
  `expected_time` varchar(50) DEFAULT NULL COMMENT '期望时间段',
  `user_remark` varchar(500) DEFAULT NULL COMMENT '用户备注',
  `merchant_remark` varchar(500) DEFAULT NULL COMMENT '商家备注',
  `confirmed_at` datetime DEFAULT NULL COMMENT '确认时间',
  `shipped_at` datetime DEFAULT NULL COMMENT '发货时间',
  `completed_at` datetime DEFAULT NULL COMMENT '完成时间',
  `cancelled_at` datetime DEFAULT NULL COMMENT '取消时间',
  `cancel_reason` varchar(500) DEFAULT NULL COMMENT '取消原因',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除 0-否 1-是',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_order_no` (`order_no`) USING BTREE,
  KEY `idx_user_id` (`user_id`) USING BTREE,
  KEY `idx_merchant_id` (`merchant_id`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE,
  KEY `idx_created_at` (`created_at`) USING BTREE,
  KEY `idx_expected_date` (`expected_date`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='订单表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_order`
--

LOCK TABLES `t_order` WRITE;
/*!40000 ALTER TABLE `t_order` DISABLE KEYS */;
INSERT INTO `t_order` VALUES (1,'O20260101024047510E62',1,2,'CANCELLED',500000.00,0.00,0.00,500000.00,1,1,'wu','13355466554','重庆市','2026-01-01',NULL,'111',NULL,NULL,NULL,NULL,'2026-01-01 08:04:07','','2026-01-01 02:40:48','2026-01-01 08:04:07',0),(2,'O202601010804526AE52F',1,2,'CANCELLED',500000.00,0.00,0.00,500000.00,1,2,'wu','13355466554','重庆市','2026-01-02',NULL,'sss',NULL,'2026-01-01 08:05:49','2026-01-01 08:06:04','2026-01-01 10:46:31','2026-01-01 10:46:40','','2026-01-01 08:04:52','2026-01-01 10:46:40',0),(3,'O202601011125414409CD',1,2,'COMPLETED',700000.00,0.00,0.00,700000.00,1,2,'wu','13355466554','重庆市','2026-01-02',NULL,'sss',NULL,'2026-01-01 11:26:44','2026-01-01 11:27:26','2026-01-01 12:03:19',NULL,NULL,'2026-01-01 11:25:42','2026-01-01 12:03:19',0),(4,'O20260101120822EDB884',1,2,'CANCELLED',500000.00,0.00,0.00,500000.00,1,2,'wu','13355466554','重庆市','2026-01-02',NULL,'',NULL,NULL,NULL,NULL,'2026-01-01 12:13:06','','2026-01-01 12:08:22','2026-01-01 12:13:06',0),(5,'O20260101132904AE9B31',1,2,'CANCELLED',500000.00,0.00,0.00,500000.00,1,2,'wu','13355466554','重庆市','2026-01-02',NULL,'',NULL,NULL,NULL,NULL,'2026-01-01 13:35:37','','2026-01-01 13:29:05','2026-01-01 13:35:37',0),(6,'O202601011556533B2758',1,2,'CANCELLED',500000.00,0.00,0.00,500000.00,1,2,'wu','13355466554','重庆市','2026-01-02',NULL,'',NULL,NULL,NULL,NULL,'2026-01-01 17:46:48','','2026-01-01 15:56:54','2026-01-01 17:46:48',0),(7,'O2026010116345011616F',1,2,'CANCELLED',500000.00,0.00,0.00,500000.00,1,2,'wu','13355466554','重庆市','2026-01-02',NULL,'',NULL,NULL,NULL,NULL,'2026-01-01 17:46:43','','2026-01-01 16:34:51','2026-01-01 17:46:43',0),(8,'O202601011704139F04B5',1,2,'CANCELLED',500000.00,0.00,0.00,500000.00,1,2,'wu','13355466554','重庆市','2026-01-02',NULL,'',NULL,NULL,NULL,NULL,'2026-01-01 17:46:41','','2026-01-01 17:04:14','2026-01-01 17:46:41',0),(9,'O202601011708463DA4D6',1,2,'CANCELLED',700000.00,0.00,0.00,700000.00,1,2,'wu','13355466554','重庆市','2026-01-02',NULL,'',NULL,NULL,NULL,NULL,'2026-01-01 17:46:39','','2026-01-01 17:08:47','2026-01-01 17:46:39',0),(10,'O20260101170927585233',1,2,'CANCELLED',700000.00,0.00,0.00,700000.00,1,2,'wu','13355466554','重庆市','2026-01-02',NULL,'',NULL,NULL,NULL,NULL,'2026-01-01 17:46:35','','2026-01-01 17:09:28','2026-01-01 17:46:35',0),(11,'O20260101172121BBA869',1,2,'CANCELLED',500000.00,0.00,0.00,500000.00,1,2,'wu','13355466554','重庆市','2026-01-02',NULL,'',NULL,NULL,NULL,NULL,'2026-01-01 17:46:30','','2026-01-01 17:21:21','2026-01-01 17:46:30',0),(12,'O20260101172711BEB99F',1,2,'CANCELLED',500000.00,0.00,0.00,500000.00,1,2,'wu','13355466554','重庆市','2026-01-02',NULL,'',NULL,NULL,NULL,NULL,'2026-01-01 17:46:28','','2026-01-01 17:27:12','2026-01-01 17:46:28',0),(13,'O2026010118483122385E',1,2,'COMPLETED',700000.00,200000.00,0.00,900000.00,1,2,'wu','13355466554','重庆市','2026-01-02',NULL,'',NULL,'2026-01-01 18:48:54','2026-01-01 18:49:22','2026-01-01 18:53:39',NULL,NULL,'2026-01-01 18:48:32','2026-01-01 18:53:39',0),(14,'O20260102045818717ECC',1,2,'CANCELLED',700000.00,200000.00,0.00,900000.00,1,2,'wu','13355466554','重庆市','2026-01-02',NULL,'',NULL,NULL,NULL,NULL,'2026-01-02 04:58:26','','2026-01-02 04:58:19','2026-01-02 04:58:26',0),(15,'O2026010204584164006E',1,2,'CANCELLED',500000.00,200000.00,0.00,700000.00,1,2,'wu','13355466554','重庆市','2026-01-02',NULL,'',NULL,NULL,NULL,NULL,'2026-01-02 05:04:22','','2026-01-02 04:58:42','2026-01-02 05:04:22',0),(16,'O2026010205053970DF67',1,2,'CANCELLED',500000.00,0.00,0.00,500000.00,1,1,'wu','13355466554','重庆市','2026-01-02',NULL,'',NULL,NULL,NULL,NULL,'2026-01-02 05:09:23','','2026-01-02 05:05:40','2026-01-02 05:09:23',0),(17,'O202601020509371CECBC',1,2,'CANCELLED',500000.00,200000.00,0.00,700000.00,1,2,'wu','13355466554','重庆市','2026-01-02',NULL,'',NULL,NULL,NULL,NULL,'2026-01-02 05:09:53','','2026-01-02 05:09:37','2026-01-02 05:09:53',0),(18,'O2026010205145198483F',1,2,'PENDING',500000.00,200000.00,0.00,700000.00,1,2,'wu','13355466554','重庆市','2026-01-02',NULL,'',NULL,NULL,NULL,NULL,NULL,NULL,'2026-01-02 05:14:51','2026-01-02 05:14:51',0),(19,'O2026010205245088C2C2',1,2,'PENDING',500000.00,200000.00,0.00,700000.00,1,2,'wu','13355466554','重庆市','2026-01-02',NULL,'',NULL,NULL,NULL,NULL,NULL,NULL,'2026-01-02 05:24:50','2026-01-02 05:24:50',0),(20,'O2026010205250860958C',1,2,'CANCELLED',700000.00,0.00,0.00,700000.00,1,1,'wu','13355466554','重庆市','2026-01-02',NULL,'',NULL,NULL,NULL,NULL,'2026-01-02 06:52:14','','2026-01-02 05:25:08','2026-01-02 06:52:14',0),(21,'O20260103214714C9F8BE',1,2,'PENDING',500000.00,200000.00,0.00,700000.00,1,2,'wu','13355466554','重庆市','2026-01-04',NULL,'',NULL,NULL,NULL,NULL,NULL,NULL,'2026-01-03 21:47:15','2026-01-03 21:47:15',0);
/*!40000 ALTER TABLE `t_order` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_order_item`
--

DROP TABLE IF EXISTS `t_order_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_order_item` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_id` bigint(20) NOT NULL COMMENT '订单ID',
  `product_id` bigint(20) NOT NULL COMMENT '商品ID',
  `sku_id` bigint(20) DEFAULT NULL COMMENT 'SKU ID',
  `product_name_zh` varchar(200) DEFAULT NULL COMMENT '商品名称（中文）- 快照',
  `product_name_en` varchar(200) DEFAULT NULL COMMENT '商品名称（英文）- 快照',
  `product_image` varchar(500) DEFAULT NULL COMMENT '商品图片 - 快照',
  `spec_name_zh` varchar(200) DEFAULT NULL COMMENT '规格名称（中文）- 快照',
  `spec_name_en` varchar(200) DEFAULT NULL COMMENT '规格名称（英文）- 快照',
  `price` decimal(10,2) NOT NULL COMMENT '单价 - 快照',
  `quantity` int(11) NOT NULL DEFAULT '1' COMMENT '数量',
  `subtotal` decimal(10,2) NOT NULL COMMENT '小计金额',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除 0-否 1-是',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_order_id` (`order_id`) USING BTREE,
  KEY `idx_product_id` (`product_id`) USING BTREE,
  KEY `idx_sku_id` (`sku_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='订单项表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_order_item`
--

LOCK TABLES `t_order_item` WRITE;
/*!40000 ALTER TABLE `t_order_item` DISABLE KEYS */;
INSERT INTO `t_order_item` VALUES (1,1,5,3,'1111',NULL,'/upload/20251231_234352_1a8fa36a.jpg','128G',NULL,500000.00,1,500000.00,'2026-01-01 02:40:48','2026-01-01 02:40:48',0),(2,2,5,3,'1111',NULL,'/upload/20251231_234352_1a8fa36a.jpg','128G',NULL,500000.00,1,500000.00,'2026-01-01 08:04:52','2026-01-01 08:04:52',0),(3,3,5,4,'1111',NULL,'/upload/20251231_234352_1a8fa36a.jpg','256G',NULL,700000.00,1,700000.00,'2026-01-01 11:25:42','2026-01-01 11:25:42',0),(4,4,6,0,'21',NULL,'/upload/20260101_012218_cdb33a4e.png',NULL,NULL,500000.00,1,500000.00,'2026-01-01 12:08:22','2026-01-01 12:08:22',0),(5,5,6,0,'21',NULL,'/upload/20260101_012218_cdb33a4e.png',NULL,NULL,500000.00,1,500000.00,'2026-01-01 13:29:05','2026-01-01 13:29:05',0),(6,6,5,3,'1111',NULL,'/upload/20251231_234352_1a8fa36a.jpg','128G',NULL,500000.00,1,500000.00,'2026-01-01 15:56:54','2026-01-01 15:56:54',0),(7,7,5,3,'1111',NULL,'/upload/20251231_234352_1a8fa36a.jpg','128G',NULL,500000.00,1,500000.00,'2026-01-01 16:34:51','2026-01-01 16:34:51',0),(8,8,5,3,'1111',NULL,'/upload/20251231_234352_1a8fa36a.jpg','128G',NULL,500000.00,1,500000.00,'2026-01-01 17:04:14','2026-01-01 17:04:14',0),(9,9,5,4,'1111',NULL,'/upload/20251231_234352_1a8fa36a.jpg','256G',NULL,700000.00,1,700000.00,'2026-01-01 17:08:47','2026-01-01 17:08:47',0),(10,10,5,4,'1111',NULL,'/upload/20251231_234352_1a8fa36a.jpg','256G',NULL,700000.00,1,700000.00,'2026-01-01 17:09:28','2026-01-01 17:09:28',0),(11,11,5,3,'1111',NULL,'/upload/20251231_234352_1a8fa36a.jpg','128G',NULL,500000.00,1,500000.00,'2026-01-01 17:21:21','2026-01-01 17:21:21',0),(12,12,5,3,'1111',NULL,'/upload/20251231_234352_1a8fa36a.jpg','128G',NULL,500000.00,1,500000.00,'2026-01-01 17:27:12','2026-01-01 17:27:12',0),(13,13,5,4,'1111',NULL,'/upload/20251231_234352_1a8fa36a.jpg','256G',NULL,700000.00,1,700000.00,'2026-01-01 18:48:32','2026-01-01 18:48:32',0),(14,14,5,4,'1111',NULL,'/upload/20251231_234352_1a8fa36a.jpg','256G',NULL,700000.00,1,700000.00,'2026-01-02 04:58:19','2026-01-02 04:58:19',0),(15,15,5,3,'1111',NULL,'/upload/20251231_234352_1a8fa36a.jpg','128G',NULL,500000.00,1,500000.00,'2026-01-02 04:58:42','2026-01-02 04:58:42',0),(16,16,5,3,'1111',NULL,'/upload/20251231_234352_1a8fa36a.jpg','128G',NULL,500000.00,1,500000.00,'2026-01-02 05:05:40','2026-01-02 05:05:40',0),(17,17,5,3,'1111',NULL,'/upload/20251231_234352_1a8fa36a.jpg','128G',NULL,500000.00,1,500000.00,'2026-01-02 05:09:37','2026-01-02 05:09:37',0),(18,18,5,3,'1111',NULL,'/upload/20251231_234352_1a8fa36a.jpg','128G',NULL,500000.00,1,500000.00,'2026-01-02 05:14:51','2026-01-02 05:14:51',0),(19,19,5,3,'1111',NULL,'/upload/20251231_234352_1a8fa36a.jpg','128G',NULL,500000.00,1,500000.00,'2026-01-02 05:24:50','2026-01-02 05:24:50',0),(20,20,5,4,'1111',NULL,'/upload/20251231_234352_1a8fa36a.jpg','256G',NULL,700000.00,1,700000.00,'2026-01-02 05:25:08','2026-01-02 05:25:08',0),(21,21,5,3,'1111',NULL,'/upload/20251231_234352_1a8fa36a.jpg','128G',NULL,500000.00,1,500000.00,'2026-01-03 21:47:15','2026-01-03 21:47:15',0);
/*!40000 ALTER TABLE `t_order_item` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_password_reset_token`
--

DROP TABLE IF EXISTS `t_password_reset_token`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_password_reset_token` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `token` varchar(100) NOT NULL COMMENT '重置令牌',
  `code` varchar(10) DEFAULT NULL COMMENT '验证码',
  `user_type` varchar(20) NOT NULL COMMENT '用户类型 USER/MERCHANT/ADMIN',
  `used` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否已使用 0-否 1-是',
  `expire_at` datetime NOT NULL COMMENT '过期时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_token` (`token`) USING BTREE,
  KEY `idx_email` (`email`) USING BTREE,
  KEY `idx_expire_at` (`expire_at`) USING BTREE,
  KEY `idx_code` (`code`) USING BTREE,
  KEY `idx_email_user_type` (`email`,`user_type`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='密码重置令牌表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_password_reset_token`
--

LOCK TABLES `t_password_reset_token` WRITE;
/*!40000 ALTER TABLE `t_password_reset_token` DISABLE KEYS */;
INSERT INTO `t_password_reset_token` VALUES (1,'user@test.com',NULL,'fdc81e6e7c344632b562a2820871faff','9999','USER',1,'2025-12-30 00:33:29','2025-12-30 00:23:29'),(3,'2200215967@qq.com',NULL,'b382a7804043495eac10f39a0c29e222','996517','MERCHANT',1,'2025-12-31 10:27:49','2025-12-31 10:17:49');
/*!40000 ALTER TABLE `t_password_reset_token` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_payment_config`
--

DROP TABLE IF EXISTS `t_payment_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_payment_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `merchant_id` bigint(20) DEFAULT NULL COMMENT '商家ID（NULL表示平台配置）',
  `payment_type` varchar(50) NOT NULL COMMENT '支付类型 EASY_PAY/ALIPAY/WECHAT等',
  `config_key` varchar(100) NOT NULL COMMENT '配置键',
  `config_value` text COMMENT '配置值（加密存储）',
  `description` varchar(500) DEFAULT NULL COMMENT '配置描述',
  `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '状态 0-禁用 1-启用',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_merchant_payment_key` (`merchant_id`,`payment_type`,`config_key`) USING BTREE,
  KEY `idx_merchant_id` (`merchant_id`) USING BTREE,
  KEY `idx_payment_type` (`payment_type`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='支付配置表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_payment_config`
--

LOCK TABLES `t_payment_config` WRITE;
/*!40000 ALTER TABLE `t_payment_config` DISABLE KEYS */;
/*!40000 ALTER TABLE `t_payment_config` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_payment_info_config`
--

DROP TABLE IF EXISTS `t_payment_info_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_payment_info_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `merchant_id` bigint(20) DEFAULT NULL COMMENT '商家ID（NULL表示平台配置）',
  `language` varchar(10) NOT NULL DEFAULT 'zh' COMMENT '语言 zh/en',
  `title` varchar(200) NOT NULL COMMENT '标题',
  `content` text NOT NULL COMMENT '内容（富文本）',
  `sort` int(11) NOT NULL DEFAULT '0' COMMENT '排序',
  `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '状态 0-禁用 1-启用',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除 0-否 1-是',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_merchant_id` (`merchant_id`) USING BTREE,
  KEY `idx_language` (`language`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='支付说明页配置表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_payment_info_config`
--

LOCK TABLES `t_payment_info_config` WRITE;
/*!40000 ALTER TABLE `t_payment_info_config` DISABLE KEYS */;
/*!40000 ALTER TABLE `t_payment_info_config` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_payment_order`
--

DROP TABLE IF EXISTS `t_payment_order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_payment_order` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_id` bigint(20) NOT NULL COMMENT '订单ID',
  `merchant_id` bigint(20) DEFAULT NULL COMMENT '商家ID（商家缴费时使用）',
  `order_type` varchar(20) NOT NULL DEFAULT 'VOTE' COMMENT '订单类型 VOTE-投票订单 MERCHANT_REG-商家入驻 SUBSCRIPTION-订阅',
  `description` varchar(500) DEFAULT NULL COMMENT '订单描述',
  `payment_no` varchar(50) NOT NULL COMMENT '支付单号',
  `payment_type` varchar(50) NOT NULL COMMENT '支付类型 EASY_PAY/ALIPAY/WECHAT',
  `amount` decimal(10,2) NOT NULL COMMENT '支付金额',
  `status` varchar(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态 PENDING/PAID/FAILED/CANCELLED',
  `third_party_order_no` varchar(100) DEFAULT NULL COMMENT '第三方订单号',
  `callback_data` text COMMENT '回调数据（JSON）',
  `paid_at` datetime DEFAULT NULL COMMENT '支付时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_payment_no` (`payment_no`) USING BTREE,
  KEY `idx_order_id` (`order_id`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE,
  KEY `idx_third_party_order_no` (`third_party_order_no`) USING BTREE,
  KEY `idx_merchant_id` (`merchant_id`) USING BTREE,
  KEY `idx_order_type` (`order_type`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='支付订单表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_payment_order`
--

LOCK TABLES `t_payment_order` WRITE;
/*!40000 ALTER TABLE `t_payment_order` DISABLE KEYS */;
INSERT INTO `t_payment_order` VALUES (1,0,2,'MERCHANT_REG','商家入驻费用 - null','PAY20251230144749BAFE1E','ALIPAY',1000.00,'SUCCESS','MOCK_1767077862199','{\"mock\": true, \"paymentNo\": \"PAY20251230144749BAFE1E\"}','2025-12-30 14:57:42','2025-12-30 14:47:50','2025-12-30 14:57:42'),(2,0,3,'MERCHANT_REG','商家入驻费用 - null','PAY2025123015180519066C','ALIPAY',1000.00,'SUCCESS','MOCK_1767079099997','{\"mock\": true, \"paymentNo\": \"PAY2025123015180519066C\"}','2025-12-30 15:18:20','2025-12-30 15:18:05','2025-12-30 15:18:20'),(3,0,4,'MERCHANT_REG','商家入驻费用 - 泡泡','PAY20251230165313D9793E','ALIPAY',1000.00,'SUCCESS','MOCK_1767084800934','{\"mock\": true, \"paymentNo\": \"PAY20251230165313D9793E\"}','2025-12-30 16:53:21','2025-12-30 16:53:13','2025-12-30 16:53:21'),(4,0,5,'MERCHANT_REG','商家入驻费用 - 阿里','PAY202601020657246326E4','ALIPAY',1000.00,'SUCCESS','MOCK_1767308281741','{\"mock\": true, \"paymentNo\": \"PAY202601020657246326E4\"}','2026-01-02 06:58:02','2026-01-02 06:57:25','2026-01-02 06:58:02'),(5,0,7,'MERCHANT_REG','商家入驻费用 - 抖音','PAY20260102071012D32AFC','ALIPAY',1000.00,'SUCCESS','MOCK_1767309015998','{\"mock\": true, \"paymentNo\": \"PAY20260102071012D32AFC\"}','2026-01-02 07:10:16','2026-01-02 07:10:13','2026-01-02 07:10:16'),(6,0,8,'MERCHANT_REG','商家入驻费用 - ww我','PAY20260102072044617521','ALIPAY',1000.00,'SUCCESS','MOCK_1767309647191','{\"mock\": true, \"paymentNo\": \"PAY20260102072044617521\"}','2026-01-02 07:20:47','2026-01-02 07:20:45','2026-01-02 07:20:47');
/*!40000 ALTER TABLE `t_payment_order` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_plan`
--

DROP TABLE IF EXISTS `t_plan`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_plan` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name_zh` varchar(100) NOT NULL COMMENT '套餐名称（中文）',
  `name_en` varchar(100) DEFAULT NULL COMMENT '套餐名称（英文）',
  `description_zh` text COMMENT '套餐描述（中文）',
  `description_en` text COMMENT '套餐描述（英文）',
  `price` decimal(10,2) NOT NULL COMMENT '价格',
  `duration_days` int(11) NOT NULL COMMENT '时长（天）',
  `features` json DEFAULT NULL COMMENT '功能列表（JSON数组）',
  `max_products` int(11) DEFAULT NULL COMMENT '最大商品数（NULL表示无限制）',
  `max_orders_per_month` int(11) DEFAULT NULL COMMENT '每月最大订单数（NULL表示无限制）',
  `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '状态 0-禁用 1-启用',
  `sort` int(11) NOT NULL DEFAULT '0' COMMENT '排序',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除 0-否 1-是',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE,
  KEY `idx_sort` (`sort`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='套餐表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_plan`
--

LOCK TABLES `t_plan` WRITE;
/*!40000 ALTER TABLE `t_plan` DISABLE KEYS */;
INSERT INTO `t_plan` VALUES (1,'基础版','Basic Plan','适合小型商家','Suitable for small merchants',99.00,30,'[\"基础功能\", \"商品管理\", \"订单管理\"]',100,500,1,1,'2025-12-29 22:48:18','2025-12-29 22:48:18',0),(2,'标准版','Standard Plan','适合中型商家','Suitable for medium merchants',299.00,30,'[\"所有基础功能\", \"高级功能\", \"数据分析\"]',500,2000,1,2,'2025-12-29 22:48:18','2025-12-29 22:48:18',0),(3,'专业版','Professional Plan','适合大型商家','Suitable for large merchants',599.00,30,'[\"所有功能\", \"专属客服\", \"定制开发\"]',NULL,NULL,1,3,'2025-12-29 22:48:18','2025-12-29 22:48:18',0);
/*!40000 ALTER TABLE `t_plan` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_platform_config`
--

DROP TABLE IF EXISTS `t_platform_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_platform_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `config_key` varchar(100) NOT NULL COMMENT '配置键（唯一）',
  `config_value` text COMMENT '配置值（JSON格式，支持复杂数据结构）',
  `name_zh` varchar(200) DEFAULT NULL COMMENT '配置名称（中文）',
  `name_en` varchar(200) DEFAULT NULL COMMENT '配置名称（英文）',
  `description` varchar(500) DEFAULT NULL COMMENT '配置描述',
  `config_type` varchar(50) NOT NULL COMMENT '配置类型：SEO/PAYMENT/RISK_CONTROL/APP_DOWNLOAD/BASIC_INFO',
  `config_group` varchar(50) DEFAULT NULL COMMENT '配置分组：SEO_META/SEO_KEYWORDS/PAYMENT_WECHAT/PAYMENT_ALIPAY/PAYMENT_BANK/PAYMENT_PAYPAL等',
  `sort` int(11) NOT NULL DEFAULT '0' COMMENT '排序',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除 0-否 1-是',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_config_key` (`config_key`) USING BTREE,
  KEY `idx_config_type` (`config_type`) USING BTREE,
  KEY `idx_config_group` (`config_group`) USING BTREE,
  KEY `idx_deleted` (`deleted`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='平台全局配置表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_platform_config`
--

LOCK TABLES `t_platform_config` WRITE;
/*!40000 ALTER TABLE `t_platform_config` DISABLE KEYS */;
INSERT INTO `t_platform_config` VALUES (1,'seo.title','{\"zh\": \"Vote SaaS电商平台\", \"en\": \"Vote SaaS E-commerce Platform\"}','SEO标题','SEO Title','网站SEO标题（支持中英文）','SEO','SEO_META',1,'2025-12-31 01:52:41','2025-12-31 01:52:41',0),(2,'seo.keywords','{\"zh\": \"电商,购物,在线商城\", \"en\": \"ecommerce,shopping,online store\"}','SEO关键词','SEO Keywords','网站SEO关键词','SEO','SEO_KEYWORDS',2,'2025-12-31 01:52:41','2025-12-31 01:52:41',0),(3,'seo.description','{\"zh\": \"专业的SaaS电商平台解决方案\", \"en\": \"Professional SaaS E-commerce Platform Solution\"}','SEO描述','SEO Description','网站SEO描述','SEO','SEO_META',3,'2025-12-31 01:52:41','2025-12-31 01:52:41',0),(4,'payment.easypay.api_url','https://api.cpayb.net','易支付API地址','EasyPay API URL','易支付接口地址','PAYMENT','PAYMENT_EASYPAY',1,'2025-12-31 01:52:41','2025-12-31 01:52:41',0),(5,'payment.easypay.pid','','易支付商户ID','EasyPay Merchant ID','易支付商户ID（需要配置）','PAYMENT','PAYMENT_EASYPAY',2,'2025-12-31 01:52:41','2025-12-31 01:52:41',0),(6,'payment.easypay.key','','易支付密钥','EasyPay Key','易支付密钥（需要配置）','PAYMENT','PAYMENT_EASYPAY',3,'2025-12-31 01:52:41','2025-12-31 01:52:41',0),(7,'payment.wechat.enabled','1','微信支付启用','WeChat Pay Enabled','是否启用微信支付','PAYMENT','PAYMENT_WECHAT',4,'2025-12-31 01:52:41','2025-12-31 01:52:41',0),(8,'payment.alipay.enabled','1','支付宝启用','Alipay Enabled','是否启用支付宝','PAYMENT','PAYMENT_ALIPAY',5,'2025-12-31 01:52:41','2025-12-31 01:52:41',0),(9,'payment.bank.enabled','1','银行卡支付启用','Bank Card Pay Enabled','是否启用银行卡支付','PAYMENT','PAYMENT_BANK',6,'2025-12-31 01:52:41','2025-12-31 01:52:41',0),(10,'payment.paypal.enabled','1','PayPal启用','PayPal Enabled','是否启用PayPal','PAYMENT','PAYMENT_PAYPAL',7,'2025-12-31 01:52:41','2025-12-31 01:52:41',0),(11,'platform.locale','zh-CN','平台语言设置','Platform Language Setting','控制整个平台的显示语言','BASIC_INFO','LANGUAGE',0,'2026-01-03 16:10:49','2026-01-04 10:42:08',0);
/*!40000 ALTER TABLE `t_platform_config` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_product`
--

DROP TABLE IF EXISTS `t_product`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_product` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `merchant_id` bigint(20) NOT NULL COMMENT '商家ID',
  `category_id` bigint(20) DEFAULT NULL COMMENT '分类ID',
  `brand_id` bigint(20) DEFAULT NULL COMMENT '品牌ID',
  `name_zh` varchar(200) NOT NULL COMMENT '商品名称（中文）',
  `name_en` varchar(200) DEFAULT NULL COMMENT '商品名称（英文）',
  `subtitle_zh` varchar(500) DEFAULT NULL COMMENT '商品副标题（中文）',
  `subtitle_en` varchar(500) DEFAULT NULL COMMENT '商品副标题（英文）',
  `main_image` varchar(500) DEFAULT NULL COMMENT '商品主图',
  `images` json DEFAULT NULL COMMENT '商品图片（多张，JSON数组）',
  `video` varchar(500) DEFAULT NULL COMMENT '商品视频',
  `description_zh` text COMMENT '商品详情（中文）',
  `description_en` text COMMENT '商品详情（英文）',
  `original_price` decimal(10,2) DEFAULT NULL COMMENT '原价',
  `price` decimal(10,2) NOT NULL COMMENT '售价',
  `stock` int(11) NOT NULL DEFAULT '0' COMMENT '库存',
  `sales` int(11) NOT NULL DEFAULT '0' COMMENT '销量',
  `unit_zh` varchar(20) DEFAULT NULL COMMENT '单位（中文）',
  `unit_en` varchar(20) DEFAULT NULL COMMENT '单位（英文）',
  `weight` int(11) DEFAULT NULL COMMENT '重量（克）',
  `shipping_template_id` bigint(20) DEFAULT NULL COMMENT '运费模板ID',
  `after_sale_rule_id` bigint(20) DEFAULT NULL COMMENT '售后规则ID',
  `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '状态 0-下架 1-上架',
  `sort` int(11) NOT NULL DEFAULT '0' COMMENT '排序（越小越靠前）',
  `is_recommend` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否推荐',
  `is_new` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否新��',
  `is_hot` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否热销',
  `limit_count` int(11) NOT NULL DEFAULT '0' COMMENT '限购数量（0表示不限购）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除 0-否 1-是',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_merchant_id` (`merchant_id`) USING BTREE,
  KEY `idx_category_id` (`category_id`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE,
  KEY `idx_sort` (`sort`) USING BTREE,
  KEY `idx_is_recommend` (`is_recommend`) USING BTREE,
  KEY `idx_is_new` (`is_new`) USING BTREE,
  KEY `idx_is_hot` (`is_hot`) USING BTREE,
  KEY `idx_sales` (`sales`) USING BTREE,
  KEY `idx_brand_id` (`brand_id`) USING BTREE,
  KEY `idx_shipping_template_id` (`shipping_template_id`) USING BTREE,
  KEY `idx_after_sale_rule_id` (`after_sale_rule_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='商品表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_product`
--

LOCK TABLES `t_product` WRITE;
/*!40000 ALTER TABLE `t_product` DISABLE KEYS */;
INSERT INTO `t_product` VALUES (1,2,1,1,'平板','ipad','苹果','apple','','[\"/upload/20251231_023541_73b33f03.jpg\"]','','','',50.00,4600.00,50,0,'件','',NULL,NULL,NULL,1,0,1,1,1,0,'2025-12-31 02:35:20','2025-12-31 02:39:51',1),(2,2,1,1,'111','222','2223','33',NULL,NULL,NULL,NULL,NULL,60000.00,50000.00,5,0,NULL,NULL,NULL,NULL,NULL,1,0,0,0,0,0,'2025-12-31 02:40:22','2025-12-31 02:51:45',1),(3,2,1,1,'dasd','sadasd','sad','asda','/upload/20251231_025301_2cafb9aa.jpg','[\"/upload/20251231_025255_2203b0e4.jpg\"]',NULL,NULL,NULL,6000.00,5000.00,60,0,'1','1',1,NULL,NULL,1,1,1,1,1,1,'2025-12-31 02:52:16','2025-12-31 03:02:10',1),(4,2,1,1,'苹果14','apple14',NULL,NULL,'/upload/20251231_030425_236ce51b.jpg','[\"/upload/20251231_030423_989a585b.png\"]',NULL,NULL,NULL,999900.00,999900.00,85,0,NULL,NULL,NULL,1,1,1,0,1,0,0,0,'2025-12-31 03:04:31','2025-12-31 17:53:30',1),(5,2,1,1,'飞机',NULL,'飞机111',NULL,'/upload/20251231_234352_1a8fa36a.jpg','[\"/upload/20251231_234353_c144a2c6.jpg\"]',NULL,'飞机',NULL,NULL,0.00,0,0,NULL,NULL,NULL,2,1,1,1,1,0,0,0,'2025-12-31 23:44:06','2026-01-03 22:04:59',0),(6,2,1,1,'21',NULL,'21',NULL,'/upload/20260101_012218_cdb33a4e.png','[\"/upload/20260101_011713_35bfdc92.png\"]',NULL,'21231',NULL,1100.00,500000.00,1090,0,NULL,NULL,NULL,2,1,1,0,0,0,0,0,'2026-01-01 01:17:16','2026-01-01 17:47:56',0);
/*!40000 ALTER TABLE `t_product` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_product_sku`
--

DROP TABLE IF EXISTS `t_product_sku`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_product_sku` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `merchant_id` bigint(20) NOT NULL COMMENT '商家ID',
  `product_id` bigint(20) NOT NULL COMMENT '商品ID',
  `sku_code` varchar(50) DEFAULT NULL COMMENT 'SKU编码',
  `spec_name_zh` varchar(200) DEFAULT NULL COMMENT '规格名称（中文）如：红色/大号',
  `spec_name_en` varchar(200) DEFAULT NULL COMMENT '规格名称（英文）',
  `spec_values` json DEFAULT NULL COMMENT '规格值（JSON格式）',
  `image` varchar(500) DEFAULT NULL COMMENT 'SKU图片',
  `original_price` decimal(10,2) DEFAULT NULL COMMENT '原价',
  `price` decimal(10,2) NOT NULL COMMENT '售价',
  `stock` int(11) NOT NULL DEFAULT '0' COMMENT '库存',
  `sales` int(11) NOT NULL DEFAULT '0' COMMENT '销量',
  `weight` int(11) DEFAULT NULL COMMENT '重量（克）',
  `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '状态 0-禁用 1-启用',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除 0-否 1-是',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_merchant_id` (`merchant_id`) USING BTREE,
  KEY `idx_product_id` (`product_id`) USING BTREE,
  KEY `idx_sku_code` (`sku_code`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='商品SKU表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_product_sku`
--

LOCK TABLES `t_product_sku` WRITE;
/*!40000 ALTER TABLE `t_product_sku` DISABLE KEYS */;
INSERT INTO `t_product_sku` VALUES (1,2,4,NULL,'256G','256G',NULL,NULL,NULL,400000.00,6,0,NULL,1,'2025-12-31 17:19:21','2025-12-31 17:43:23',0),(2,2,4,NULL,'512G','512G',NULL,NULL,NULL,800000.00,79,0,NULL,1,'2025-12-31 17:19:40','2025-12-31 17:43:32',0),(3,2,5,'PRODUCT_5_SKU_1767195845870','128G',NULL,NULL,NULL,NULL,500000.00,97,3,NULL,1,'2025-12-31 23:44:06','2026-01-03 22:05:00',0),(4,2,5,'PRODUCT_5_SKU_1767195846012','256G',NULL,NULL,NULL,NULL,700000.00,177,0,NULL,1,'2025-12-31 23:44:06','2026-01-03 22:05:00',0);
/*!40000 ALTER TABLE `t_product_sku` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_promotion_record`
--

DROP TABLE IF EXISTS `t_promotion_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_promotion_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `merchant_id` bigint(20) NOT NULL COMMENT '商家ID（推广人）',
  `inviter_id` bigint(20) DEFAULT NULL COMMENT '邀请人ID（商家或用户）',
  `invitee_id` bigint(20) DEFAULT NULL COMMENT '被邀请人ID（商家或用户）',
  `invitee_type` varchar(20) NOT NULL COMMENT '被邀请人类型 MERCHANT/USER',
  `promotion_code` varchar(20) DEFAULT NULL COMMENT '推广码',
  `reward_amount` int(11) DEFAULT '0' COMMENT '奖励金额（分）',
  `reward_type` varchar(20) DEFAULT 'FIXED' COMMENT '奖励类型 FIXED-固定金额 PERCENTAGE-百分比',
  `reward_rate` decimal(5,2) DEFAULT NULL COMMENT '奖励比例（百分比）',
  `order_amount` decimal(10,2) DEFAULT NULL COMMENT '关联订单金额（用于计算百分比奖励）',
  `status` tinyint(4) NOT NULL DEFAULT '0' COMMENT '状态 0-待确认 1-已确认 2-已取消',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `confirmed_at` datetime DEFAULT NULL COMMENT '确认时间',
  `settlement_time` datetime DEFAULT NULL COMMENT '结算时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_merchant_id` (`merchant_id`) USING BTREE,
  KEY `idx_invitee_id` (`invitee_id`) USING BTREE,
  KEY `idx_promotion_code` (`promotion_code`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='推广记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_promotion_record`
--

LOCK TABLES `t_promotion_record` WRITE;
/*!40000 ALTER TABLE `t_promotion_record` DISABLE KEYS */;
INSERT INTO `t_promotion_record` VALUES (1,2,2,4,'MERCHANT','MC07AB24',10000,'FIXED',NULL,NULL,1,'2025-12-30 16:53:21','2025-12-30 16:53:21','2025-12-30 16:53:21','商家推广奖励');
/*!40000 ALTER TABLE `t_promotion_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_promotion_withdrawal`
--

DROP TABLE IF EXISTS `t_promotion_withdrawal`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_promotion_withdrawal` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `merchant_id` bigint(20) NOT NULL COMMENT '商家ID',
  `withdrawal_no` varchar(50) NOT NULL COMMENT '提现单号',
  `amount` decimal(10,2) NOT NULL COMMENT '提现金额',
  `fee` decimal(10,2) DEFAULT '0.00' COMMENT '手续费',
  `actual_amount` decimal(10,2) NOT NULL COMMENT '实际到账金额',
  `account_type` varchar(20) NOT NULL COMMENT '账户类型 ALIPAY-支付宝 WECHAT-微信 BANK-银行卡',
  `account_name` varchar(100) NOT NULL COMMENT '账户名称',
  `account_number` varchar(100) NOT NULL COMMENT '账号',
  `status` tinyint(4) NOT NULL DEFAULT '0' COMMENT '状态 0-待审核 1-审核通过 2-已打款 3-已拒绝 4-已取消',
  `audit_remark` varchar(500) DEFAULT NULL COMMENT '审核备注',
  `auditor_id` bigint(20) DEFAULT NULL COMMENT '审核人ID',
  `audit_time` datetime DEFAULT NULL COMMENT '审核时间',
  `transfer_time` datetime DEFAULT NULL COMMENT '打款时间',
  `transfer_voucher` varchar(500) DEFAULT NULL COMMENT '打款凭证',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_withdrawal_no` (`withdrawal_no`) USING BTREE,
  KEY `idx_merchant_id` (`merchant_id`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='推广奖励提现表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_promotion_withdrawal`
--

LOCK TABLES `t_promotion_withdrawal` WRITE;
/*!40000 ALTER TABLE `t_promotion_withdrawal` DISABLE KEYS */;
INSERT INTO `t_promotion_withdrawal` VALUES (1,2,'WD202512301655156506',50.00,0.50,49.50,'ALIPAY','wu','1333232323',2,'111',1,'2025-12-30 21:38:42','2025-12-30 21:38:49','','2025-12-30 16:55:16','2025-12-30 21:38:49');
/*!40000 ALTER TABLE `t_promotion_withdrawal` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_risk_control_rule`
--

DROP TABLE IF EXISTS `t_risk_control_rule`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_risk_control_rule` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `rule_name` varchar(200) NOT NULL COMMENT '规则名称',
  `rule_type` varchar(50) NOT NULL COMMENT '规则类型：ORDER_AMOUNT/ORDER_FREQUENCY/IP_BLACKLIST/DEVICE_BLACKLIST/USER_BLACKLIST等',
  `rule_config` text NOT NULL COMMENT '规则配置（JSON格式），例如：{"maxAmount": 10000, "maxCount": 10, "timeWindow": 3600}',
  `action` varchar(20) NOT NULL COMMENT '触发动作：BLOCK/ALERT/REVIEW',
  `enabled` tinyint(4) NOT NULL DEFAULT '1' COMMENT '是否启用：0-禁用 1-启用',
  `priority` int(11) NOT NULL DEFAULT '1' COMMENT '优先级（数字越大优先级越高）',
  `description` varchar(500) DEFAULT NULL COMMENT '规则描述',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除 0-否 1-是',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_rule_type` (`rule_type`) USING BTREE,
  KEY `idx_action` (`action`) USING BTREE,
  KEY `idx_enabled` (`enabled`) USING BTREE,
  KEY `idx_priority` (`priority`) USING BTREE,
  KEY `idx_deleted` (`deleted`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='风控规则表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_risk_control_rule`
--

LOCK TABLES `t_risk_control_rule` WRITE;
/*!40000 ALTER TABLE `t_risk_control_rule` DISABLE KEYS */;
INSERT INTO `t_risk_control_rule` VALUES (1,'单笔订单金额限制','ORDER_AMOUNT','{\"maxAmount\": 50000, \"minAmount\": 0.01}','BLOCK',1,10,'限制单笔订单最大金额为50000元','2025-12-31 01:52:41','2025-12-31 01:52:41',0),(2,'订单频率限制','ORDER_FREQUENCY','{\"maxCount\": 10, \"timeWindow\": 3600}','ALERT',1,5,'1小时内最多允许10笔订单，超过则告警','2025-12-31 01:52:41','2025-12-31 01:52:41',0),(3,'IP黑名单','IP_BLACKLIST','{\"ips\": []}','BLOCK',1,20,'IP黑名单规则，拦截指定IP的订单','2025-12-31 01:52:41','2025-12-31 01:52:41',0);
/*!40000 ALTER TABLE `t_risk_control_rule` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_shipping_template`
--

DROP TABLE IF EXISTS `t_shipping_template`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_shipping_template` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `merchant_id` bigint(20) NOT NULL COMMENT '商家ID',
  `name` varchar(100) NOT NULL COMMENT '模板名称',
  `type` varchar(20) NOT NULL DEFAULT 'REGION' COMMENT '计费类型 REGION/WEIGHT/COUNT',
  `free_shipping_amount` decimal(10,2) DEFAULT NULL COMMENT '包邮金额（满此金额包邮）',
  `free_shipping_condition` varchar(50) DEFAULT NULL COMMENT '包邮条件 AMOUNT/WEIGHT/COUNT',
  `default_fee` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '默认运费',
  `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '状态 0-禁用 1-启用',
  `is_default` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否默认模板',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除 0-否 1-是',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_merchant_id` (`merchant_id`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='运费模板表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_shipping_template`
--

LOCK TABLES `t_shipping_template` WRITE;
/*!40000 ALTER TABLE `t_shipping_template` DISABLE KEYS */;
INSERT INTO `t_shipping_template` VALUES (1,2,'asdasd','FIXED',10000.00,NULL,200.00,1,1,'2025-12-31 01:49:52','2026-01-01 17:47:09',1),(2,2,'测试模板','FIXED',1000000.00,NULL,200000.00,1,1,'2026-01-01 17:47:38','2026-01-01 17:47:38',0);
/*!40000 ALTER TABLE `t_shipping_template` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_shipping_template_rule`
--

DROP TABLE IF EXISTS `t_shipping_template_rule`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_shipping_template_rule` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `template_id` bigint(20) NOT NULL COMMENT '模板ID',
  `region_codes` varchar(500) DEFAULT NULL COMMENT '地区编码（JSON数组）',
  `region_names` varchar(500) DEFAULT NULL COMMENT '地区名称（JSON数组）',
  `first_weight` int(11) DEFAULT NULL COMMENT '首重（克）',
  `first_fee` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '首重运费',
  `continue_weight` int(11) DEFAULT NULL COMMENT '续重（克）',
  `continue_fee` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '续重运费',
  `first_count` int(11) DEFAULT NULL COMMENT '首件数',
  `first_count_fee` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '首件运费',
  `continue_count` int(11) DEFAULT NULL COMMENT '续件数',
  `continue_count_fee` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '续件运费',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_template_id` (`template_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='运费模板规则表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_shipping_template_rule`
--

LOCK TABLES `t_shipping_template_rule` WRITE;
/*!40000 ALTER TABLE `t_shipping_template_rule` DISABLE KEYS */;
INSERT INTO `t_shipping_template_rule` VALUES (1,1,NULL,'北京',1000,0.00,500,0.00,2,1.00,1,1.00,'2026-01-01 01:36:16','2026-01-01 01:36:16');
/*!40000 ALTER TABLE `t_shipping_template_rule` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_system_config`
--

DROP TABLE IF EXISTS `t_system_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_system_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `config_key` varchar(100) NOT NULL COMMENT '配置键',
  `config_value` text COMMENT '配置值',
  `name_zh` varchar(100) DEFAULT NULL COMMENT '配置名称（中文）',
  `name_en` varchar(100) DEFAULT NULL COMMENT '配置名称（英文）',
  `description` varchar(500) DEFAULT NULL COMMENT '配置描述',
  `config_type` varchar(20) NOT NULL DEFAULT 'STRING' COMMENT '配置类型 STRING/NUMBER/BOOLEAN/JSON',
  `config_group` varchar(50) DEFAULT NULL COMMENT '配置分组',
  `sort` int(11) NOT NULL DEFAULT '0' COMMENT '排序',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除 0-否 1-是',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_config_key` (`config_key`) USING BTREE,
  KEY `idx_config_group` (`config_group`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=27 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='系统配置表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_system_config`
--

LOCK TABLES `t_system_config` WRITE;
/*!40000 ALTER TABLE `t_system_config` DISABLE KEYS */;
INSERT INTO `t_system_config` VALUES (9,'site_name_zh','私域商城','网站名称（中文）','Site Name (Chinese)','网站中文名称','STRING','basic',1,'2025-12-29 23:22:53','2025-12-29 23:22:53',0),(10,'site_name_en','Private Domain Mall','网站名称（英文）','Site Name (English)','网站英文名称','STRING','basic',2,'2025-12-29 23:22:53','2025-12-29 23:22:53',0),(11,'site_logo','','网站Logo','Site Logo','网站Logo图片地址','STRING','basic',3,'2025-12-29 23:22:53','2025-12-29 23:22:53',0),(12,'site_description_zh','专业的SaaS电商平台','网站描述（中文）','Site Description (Chinese)','网站中文描述','STRING','basic',4,'2025-12-29 23:22:53','2025-12-29 23:22:53',0),(13,'site_description_en','Professional SaaS E-commerce Platform','网站描述（英文）','Site Description (English)','网站英文描述','STRING','basic',5,'2025-12-29 23:22:53','2025-12-29 23:22:53',0),(14,'default_language','zh','默认语言','Default Language','系统默认语言 zh/en','STRING','basic',6,'2025-12-29 23:22:53','2025-12-29 23:22:53',0),(15,'enable_registration','true','开放注册','Enable Registration','是否开放用户注册','BOOLEAN','user',10,'2025-12-29 23:22:53','2025-12-29 23:22:53',0),(16,'enable_merchant_registration','true','开放商家入驻','Enable Merchant Registration','是否开放商家入驻申请','BOOLEAN','merchant',20,'2025-12-29 23:22:53','2025-12-29 23:22:53',0),(17,'merchant.audit.enabled','true','商家审核开关','Merchant Audit Switch','是否开启商家入驻审核，关闭后商家注册自动通过','BOOLEAN','merchant',1,'2025-12-30 09:50:00','2025-12-30 09:50:00',0),(18,'merchant.registration.fee','1000.00','商家入驻费用','Merchant Registration Fee','商家入驻需缴纳的费用（元）','NUMBER','merchant',2,'2025-12-30 09:50:00','2025-12-30 09:50:00',0),(19,'merchant.registration.fee.enabled','true','是否收取入驻费','Registration Fee Enabled','是否收取商家入驻费用','BOOLEAN','merchant',3,'2025-12-30 09:50:00','2025-12-30 09:50:00',0),(20,'merchant.promotion.reward.merchant','100.00','邀请商家奖励','Merchant Referral Reward','成功邀请一个商家的奖励金额（元）','NUMBER','promotion',10,'2025-12-30 09:50:00','2025-12-30 09:50:00',0),(21,'merchant.promotion.reward.user','5.00','邀请用户奖励比例','User Referral Reward Rate','邀请用户消费的奖励比例（%）','NUMBER','promotion',11,'2025-12-30 09:50:00','2025-12-30 09:50:00',0),(22,'merchant.promotion.enabled','true','推广功能开关','Promotion Feature Switch','是否开启商家推广功能','BOOLEAN','promotion',12,'2025-12-30 09:50:00','2025-12-30 09:50:00',0),(23,'merchant.invoice.auto.generate','true','自动生成发票','Auto Generate Invoice','缴费成功后是否自动生成发票','BOOLEAN','invoice',20,'2025-12-30 09:50:00','2025-12-30 09:50:00',0),(24,'merchant.invoice.tax.rate','6.00','发票税率','Invoice Tax Rate','发票税率（%）','NUMBER','invoice',21,'2025-12-30 09:50:00','2025-12-30 09:50:00',0),(25,'merchant.welcome.email.enabled','true','发送欢迎邮件','Send Welcome Email','商家注册成功后是否发送欢迎邮件','BOOLEAN','email',30,'2025-12-30 09:50:00','2025-12-30 09:50:00',0),(26,'merchant.qrcode.auto.generate','true','自动生成二维码','Auto Generate QR Code','是否自动生成商家邀请二维码','BOOLEAN','merchant',4,'2025-12-30 09:50:00','2025-12-30 09:50:00',0);
/*!40000 ALTER TABLE `t_system_config` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_user`
--

DROP TABLE IF EXISTS `t_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(255) NOT NULL COMMENT '密码',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `nickname` varchar(50) DEFAULT NULL COMMENT '昵称',
  `real_name` varchar(50) DEFAULT NULL COMMENT '真实姓名',
  `avatar` varchar(500) DEFAULT NULL COMMENT '头像URL',
  `merchant_id` bigint(20) DEFAULT NULL COMMENT '绑定的商家ID（核心字段）',
  `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '状态 0-禁用 1-正常',
  `invite_code` varchar(20) DEFAULT NULL COMMENT '推广码（用于邀请其他用户）',
  `inviter_id` bigint(20) DEFAULT NULL COMMENT '邀请人ID',
  `last_login_at` datetime DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` varchar(50) DEFAULT NULL COMMENT '最后登录IP',
  `language` varchar(10) DEFAULT 'zh' COMMENT '语言偏好 zh/en',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除 0-否 1-是',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_username` (`username`) USING BTREE,
  UNIQUE KEY `uk_invite_code` (`invite_code`) USING BTREE,
  KEY `idx_email` (`email`) USING BTREE,
  KEY `idx_phone` (`phone`) USING BTREE,
  KEY `idx_merchant_id` (`merchant_id`) USING BTREE,
  KEY `idx_inviter_id` (`inviter_id`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='用户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_user`
--

LOCK TABLES `t_user` WRITE;
/*!40000 ALTER TABLE `t_user` DISABLE KEYS */;
INSERT INTO `t_user` VALUES (1,'user','$2a$10$bJLdeJ85Xe9Y.ibLHqKdE.AFmxKPLaC5WiRXaZ7Q9d4b8qhsBpQSO','user@test.com','13800138001','测试用户','wu','/upload/20251230_010353_b1ccb5f5.jpg',2,1,NULL,NULL,NULL,NULL,'zh','2025-12-29 21:03:06','2026-01-01 23:05:31',0);
/*!40000 ALTER TABLE `t_user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_user_address`
--

DROP TABLE IF EXISTS `t_user_address`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_user_address` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `receiver_name` varchar(50) NOT NULL COMMENT '收货人姓名',
  `receiver_phone` varchar(20) NOT NULL COMMENT '收货人电话',
  `province` varchar(50) DEFAULT NULL COMMENT '省份',
  `city` varchar(50) DEFAULT NULL COMMENT '城市',
  `district` varchar(50) DEFAULT NULL COMMENT '区/县',
  `detail_address` varchar(500) DEFAULT NULL COMMENT '详细地址',
  `full_address` varchar(500) DEFAULT NULL COMMENT '完整地址',
  `postal_code` varchar(20) DEFAULT NULL COMMENT '邮政编码',
  `is_default` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否默认地址',
  `tag` varchar(20) DEFAULT NULL COMMENT '标签（家/公司/学校等）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除 0-否 1-是',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_user_id` (`user_id`) USING BTREE,
  KEY `idx_is_default` (`is_default`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='用户收货地址表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_user_address`
--

LOCK TABLES `t_user_address` WRITE;
/*!40000 ALTER TABLE `t_user_address` DISABLE KEYS */;
INSERT INTO `t_user_address` VALUES (1,1,'wu','13355466554',NULL,NULL,NULL,NULL,'重庆市',NULL,1,NULL,'2025-12-30 00:49:38','2025-12-30 01:05:10',0);
/*!40000 ALTER TABLE `t_user_address` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-05-07 12:22:32
