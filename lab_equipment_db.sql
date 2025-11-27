/*
 Navicat Premium Dump SQL

 Source Server         : localDB
 Source Server Type    : MySQL
 Source Server Version : 80044 (8.0.44)
 Source Host           : localhost:3306
 Source Schema         : lab_equipment_db

 Target Server Type    : MySQL
 Target Server Version : 80044 (8.0.44)
 File Encoding         : 65001

 Date: 25/11/2025 21:18:42
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for categories
-- ----------------------------
DROP TABLE IF EXISTS `categories`;
CREATE TABLE `categories`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分类名称',
  `parent_id` bigint NULL DEFAULT NULL COMMENT '父分类ID (用于树状结构)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `fk_categories_parent`(`parent_id` ASC) USING BTREE,
  CONSTRAINT `fk_categories_parent` FOREIGN KEY (`parent_id`) REFERENCES `categories` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '设备分类表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of categories
-- ----------------------------

-- ----------------------------
-- Table structure for devices
-- ----------------------------
DROP TABLE IF EXISTS `devices`;
CREATE TABLE `devices`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '设备ID',
  `asset_number` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '资产编号 (全局唯一, 二维码内容)',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '设备名称',
  `model` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '设备型号',
  `manufacturer` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '生产商家',
  `purchase_date` date NULL DEFAULT NULL COMMENT '购买日期',
  `price` decimal(12, 2) NULL DEFAULT NULL COMMENT '采购价格',
  `category_id` bigint NULL DEFAULT NULL COMMENT '设备分类ID (外键)',
  `lab_id` bigint NOT NULL COMMENT '所属实验室ID (外键)',
  `status` enum('in_stock','in_use','under_repair','scrapped') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'in_stock' COMMENT '状态: 在库, 在用, 维修, 报废',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '录入时间',
  `scrapped_at` datetime NULL DEFAULT NULL COMMENT '报废时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `asset_number`(`asset_number` ASC) USING BTREE,
  INDEX `fk_devices_category`(`category_id` ASC) USING BTREE,
  INDEX `idx_devices_status`(`status` ASC) USING BTREE,
  INDEX `idx_devices_lab_id`(`lab_id` ASC) USING BTREE,
  INDEX `idx_devices_name`(`name` ASC) USING BTREE,
  CONSTRAINT `fk_devices_category` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `fk_devices_lab` FOREIGN KEY (`lab_id`) REFERENCES `labs` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '设备主表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of devices
-- ----------------------------

-- ----------------------------
-- Table structure for labs
-- ----------------------------
DROP TABLE IF EXISTS `labs`;
CREATE TABLE `labs`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '实验室ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '实验室名称',
  `location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '实验室位置',
  `manager_id` bigint NULL DEFAULT NULL COMMENT '负责人ID (关联 users.id)',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创立时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `fk_labs_manager`(`manager_id` ASC) USING BTREE,
  CONSTRAINT `fk_labs_manager` FOREIGN KEY (`manager_id`) REFERENCES `users` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '实验室表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of labs
-- ----------------------------

-- ----------------------------
-- Table structure for repair_records
-- ----------------------------
DROP TABLE IF EXISTS `repair_records`;
CREATE TABLE `repair_records`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '维修表ID',
  `device_id` bigint NOT NULL COMMENT '设备ID',
  `reporter_id` bigint NOT NULL COMMENT '报修人ID',
  `handler_id` bigint NULL DEFAULT NULL COMMENT '维修处理人ID',
  `status` enum('reported','assigned','in_progress','completed','rejected') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'reported' COMMENT '维修状态',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '故障描述',
  `solution` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '解决方案',
  `cost` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '维修费用',
  `reported_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '报修时间',
  `completed_time` datetime NULL DEFAULT NULL COMMENT '完成时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `fk_repair_device`(`device_id` ASC) USING BTREE,
  INDEX `fk_repair_reporter`(`reporter_id` ASC) USING BTREE,
  INDEX `fk_repair_handler`(`handler_id` ASC) USING BTREE,
  CONSTRAINT `fk_repair_device` FOREIGN KEY (`device_id`) REFERENCES `devices` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_repair_handler` FOREIGN KEY (`handler_id`) REFERENCES `users` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `fk_repair_reporter` FOREIGN KEY (`reporter_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '维修记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of repair_records
-- ----------------------------

-- ----------------------------
-- Table structure for transfer_records
-- ----------------------------
DROP TABLE IF EXISTS `transfer_records`;
CREATE TABLE `transfer_records`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '调拨ID',
  `device_id` bigint NOT NULL COMMENT '设备ID',
  `from_lab_id` bigint NOT NULL COMMENT '原实验室ID',
  `to_lab_id` bigint NOT NULL COMMENT '目标实验室ID',
  `operator_id` bigint NOT NULL COMMENT '操作人ID',
  `transfer_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '调拨时间',
  `reason` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '调拨原因',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `fk_transfer_device`(`device_id` ASC) USING BTREE,
  INDEX `fk_transfer_from_lab`(`from_lab_id` ASC) USING BTREE,
  INDEX `fk_transfer_to_lab`(`to_lab_id` ASC) USING BTREE,
  INDEX `fk_transfer_operator`(`operator_id` ASC) USING BTREE,
  CONSTRAINT `fk_transfer_device` FOREIGN KEY (`device_id`) REFERENCES `devices` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_transfer_from_lab` FOREIGN KEY (`from_lab_id`) REFERENCES `labs` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_transfer_operator` FOREIGN KEY (`operator_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_transfer_to_lab` FOREIGN KEY (`to_lab_id`) REFERENCES `labs` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '设备调拨记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of transfer_records
-- ----------------------------

-- ----------------------------
-- Table structure for usage_records
-- ----------------------------
DROP TABLE IF EXISTS `usage_records`;
CREATE TABLE `usage_records`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `device_id` bigint NOT NULL COMMENT '设备ID',
  `user_id` bigint NOT NULL COMMENT '使用人ID',
  `lab_id` bigint NOT NULL COMMENT '使用时的实验室ID (快照)',
  `start_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '开始使用时间',
  `end_time` datetime NULL DEFAULT NULL COMMENT '归还时间 (NULL表示未归还)',
  `purpose` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '使用目的',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '登记时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `fk_usage_user`(`user_id` ASC) USING BTREE,
  INDEX `fk_usage_lab`(`lab_id` ASC) USING BTREE,
  INDEX `idx_usage_no_return`(`device_id` ASC, `end_time` ASC) USING BTREE,
  CONSTRAINT `fk_usage_device` FOREIGN KEY (`device_id`) REFERENCES `devices` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_usage_lab` FOREIGN KEY (`lab_id`) REFERENCES `labs` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_usage_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '设备使用记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of usage_records
-- ----------------------------

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名/学号/工号 (唯一)',
  `password_hash` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '加密后的密码 (BCrypt)',
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '真实姓名',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '手机号 (唯一)',
  `role` enum('admin','manager','user') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'user' COMMENT '角色: 管理员, 负责人, 普通用户',
  `lab_id` bigint NULL DEFAULT NULL COMMENT '所属实验室ID (外键)',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '账号创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username` ASC) USING BTREE,
  UNIQUE INDEX `phone`(`phone` ASC) USING BTREE,
  INDEX `fk_users_lab`(`lab_id` ASC) USING BTREE,
  CONSTRAINT `fk_users_lab` FOREIGN KEY (`lab_id`) REFERENCES `labs` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of users
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
