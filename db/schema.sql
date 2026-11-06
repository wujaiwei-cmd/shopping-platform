-- ============================================================
-- 网络购物平台 数据库初始化脚本
-- 字符集: utf8mb4  排序: utf8mb4_unicode_ci
-- 适用: MySQL 8.0+
-- ============================================================

CREATE DATABASE IF NOT EXISTS `shopping`
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE `shopping`;

-- ------------------------------------------------------------
-- 用户表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `user` (
  `id`         BIGINT       NOT NULL AUTO_INCREMENT,
  `username`   VARCHAR(50)  NOT NULL,
  `password`   VARCHAR(255) NOT NULL,
  `email`      VARCHAR(100) DEFAULT NULL,
  `phone`      VARCHAR(20)  DEFAULT NULL,
  `enabled`    TINYINT(1)   NOT NULL DEFAULT 1,
  `is_admin`   TINYINT(1)   NOT NULL DEFAULT 0,
  `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME     DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_username` (`username`),
  UNIQUE KEY `uk_user_email`    (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------
-- 商品分类表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `category` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT,
  `name`        VARCHAR(50)  NOT NULL,
  `description` VARCHAR(200) DEFAULT NULL,
  `sort_order`  INT          NOT NULL DEFAULT 0,
  `enabled`     TINYINT(1)   NOT NULL DEFAULT 1,
  `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`  DATETIME     DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_category_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------
-- 商品表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `goods` (
  `id`          BIGINT        NOT NULL AUTO_INCREMENT,
  `name`        VARCHAR(200)  NOT NULL,
  `price`       DECIMAL(10,2) NOT NULL,
  `stock`       INT           NOT NULL DEFAULT 0,
  `category`    VARCHAR(50)   DEFAULT NULL,
  `description` TEXT          DEFAULT NULL,
  `image`       VARCHAR(255)  DEFAULT NULL,
  `status`      VARCHAR(20)   NOT NULL DEFAULT 'active',
  `created_at`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`  DATETIME      DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_goods_category` (`category`),
  KEY `idx_goods_status`   (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------
-- 购物车表（同一用户同一商品唯一）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `cart` (
  `id`         BIGINT   NOT NULL AUTO_INCREMENT,
  `user_id`    BIGINT   NOT NULL,
  `goods_id`   BIGINT   NOT NULL,
  `quantity`   INT      NOT NULL DEFAULT 1,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_goods` (`user_id`, `goods_id`),
  KEY `idx_cart_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------
-- 订单表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `orders` (
  `id`                  BIGINT        NOT NULL AUTO_INCREMENT,
  `user_id`             BIGINT        NOT NULL,
  `order_no`            VARCHAR(50)   NOT NULL,
  `total_amount`        DECIMAL(12,2) NOT NULL,
  `status`              VARCHAR(20)   NOT NULL DEFAULT 'pending',
  `shipping_name`       VARCHAR(50)   DEFAULT NULL,
  `shipping_phone`      VARCHAR(20)   DEFAULT NULL,
  `shipping_address`    VARCHAR(500)  DEFAULT NULL,
  `payment_method`      VARCHAR(20)   DEFAULT NULL,
  `created_at`          DATETIME      NOT NULL,
  `updated_at`          DATETIME      DEFAULT NULL,
  `refund_status`       VARCHAR(20)   DEFAULT NULL,
  `refund_reason`       TEXT          DEFAULT NULL,
  `refund_requested_at` DATETIME      DEFAULT NULL,
  `refund_processed_at` DATETIME      DEFAULT NULL,
  `refund_note`         VARCHAR(500)  DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_orders_no` (`order_no`),
  KEY `idx_orders_user`   (`user_id`),
  KEY `idx_orders_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------
-- 订单商品明细表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `order_item` (
  `id`         BIGINT        NOT NULL AUTO_INCREMENT,
  `order_id`   BIGINT        NOT NULL,
  `goods_id`   BIGINT        NOT NULL,
  `goods_name` VARCHAR(200)  NOT NULL,
  `price`      DECIMAL(10,2) NOT NULL,
  `quantity`   INT           NOT NULL,
  `created_at` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_order_item_order` (`order_id`),
  KEY `idx_order_item_goods` (`goods_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------
-- 收货地址表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `address` (
  `id`         BIGINT       NOT NULL AUTO_INCREMENT,
  `user_id`    BIGINT       NOT NULL,
  `name`       VARCHAR(50)  NOT NULL,
  `phone`      VARCHAR(20)  NOT NULL,
  `province`   VARCHAR(50)  DEFAULT NULL,
  `city`       VARCHAR(50)  DEFAULT NULL,
  `district`   VARCHAR(50)  DEFAULT NULL,
  `address`    VARCHAR(500) NOT NULL,
  `is_default` TINYINT(1)   NOT NULL DEFAULT 0,
  `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME     DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_address_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------
-- 收藏表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `favorite` (
  `id`         BIGINT   NOT NULL AUTO_INCREMENT,
  `user_id`    BIGINT   NOT NULL,
  `goods_id`   BIGINT   NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_favorite_user_goods` (`user_id`, `goods_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------
-- 用户反馈表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `feedback` (
  `id`              BIGINT       NOT NULL AUTO_INCREMENT,
  `user_id`         BIGINT       DEFAULT NULL,
  `username`        VARCHAR(50)  DEFAULT NULL,
  `type`            VARCHAR(20)  NOT NULL,
  `title`           VARCHAR(200) NOT NULL,
  `content`         TEXT         NOT NULL,
  `contact`         VARCHAR(100) DEFAULT NULL,
  `is_processed`    TINYINT(1)   DEFAULT 0,
  `processed_note`  TEXT         DEFAULT NULL,
  `created_at`      DATETIME     NOT NULL,
  `processed_at`    DATETIME     DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_feedback_user`    (`user_id`),
  KEY `idx_feedback_process` (`is_processed`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------
-- 商品评价表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `product_reviews` (
  `review_id`     BIGINT       NOT NULL AUTO_INCREMENT,
  `product_id`    BIGINT       NOT NULL,
  `customer_id`   BIGINT       NOT NULL,
  `customer_name` VARCHAR(60)  DEFAULT NULL,
  `star_count`    INT          DEFAULT 5,
  `review_text`   VARCHAR(1200) DEFAULT NULL,
  `submitted_at`  DATETIME     DEFAULT NULL,
  PRIMARY KEY (`review_id`),
  KEY `idx_review_product` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
