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

 Date: 31/12/2025 16:23:20
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
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '设备分类表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of categories
-- ----------------------------
INSERT INTO `categories` VALUES (1, '电子信息', NULL);
INSERT INTO `categories` VALUES (2, '化学器材类', NULL);
INSERT INTO `categories` VALUES (3, '电气类', NULL);
INSERT INTO `categories` VALUES (4, '数据科学类', NULL);

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
) ENGINE = InnoDB AUTO_INCREMENT = 23 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '设备主表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of devices
-- ----------------------------
INSERT INTO `devices` VALUES (1, 'LS1010001', 'RTX4090显卡', 'RTX4090 16G', '英伟达', '2025-12-09', 15000.00, 1, 1, 'scrapped', '2025-12-22 17:24:10', '2025-12-29 22:21:11');
INSERT INTO `devices` VALUES (2, 'LS1023041', '显卡RTX4060', 'RTX4060 8G', '英伟达', NULL, 214.00, 1, 1, 'in_stock', '2025-12-29 21:57:51', NULL);
INSERT INTO `devices` VALUES (3, 'PH000001', 'iPad 15 pro', '16g 256G i7th', 'Apple', NULL, 3123.00, 1, 1, 'scrapped', '2025-12-29 22:50:18', '2025-12-30 21:01:58');
INSERT INTO `devices` VALUES (4, 'LAPTOP100001', '拯救者Y700P', '5060+i7-14650th', '联想', NULL, 3124.00, 1, 1, 'in_stock', '2025-12-29 22:51:10', NULL);
INSERT INTO `devices` VALUES (5, 'CH100001', '化学实验卡', '二型卡', '智选生物', NULL, 530.00, 2, 2, 'in_stock', '2025-12-29 23:27:40', NULL);
INSERT INTO `devices` VALUES (6, 'CH100002', '化学试剂瓶', NULL, NULL, '2025-12-30', 20.00, 2, 2, 'in_stock', '2025-12-30 22:25:58', NULL);
INSERT INTO `devices` VALUES (7, 'CH1000001', '化学试剂PH卡', '每组20张', '智选生物科技', '2025-12-30', 10.00, 2, 2, 'in_stock', '2025-12-30 22:30:42', NULL);
INSERT INTO `devices` VALUES (8, 'CG1767113375014', '示波器', '29cm*23cm,高频款', '郑州电子科技有限公司', NULL, 120.00, 3, 2, 'in_stock', NULL, NULL);
INSERT INTO `devices` VALUES (9, 'CG1767113375029', '示波器', '29cm*23cm,高频款', '郑州电子科技有限公司', NULL, 120.00, 3, 2, 'in_stock', NULL, NULL);
INSERT INTO `devices` VALUES (10, 'CG1767114102964', '摇晃仪', '1wrps+19w供电版', '郑州电子仪器有限公司', NULL, 120.00, 2, 2, 'in_stock', '2025-12-31 01:01:43', NULL);
INSERT INTO `devices` VALUES (11, 'CG1767114102967', '摇晃仪', '1wrps+19w供电版', '郑州电子仪器有限公司', NULL, 120.00, 2, 2, 'in_stock', '2025-12-31 01:01:43', NULL);
INSERT INTO `devices` VALUES (12, 'CG1767114102968', '摇晃仪', '1wrps+19w供电版', '郑州电子仪器有限公司', NULL, 120.00, 2, 2, 'in_stock', '2025-12-31 01:01:43', NULL);
INSERT INTO `devices` VALUES (13, 'CG1767114622898', 'CPU', 'i7-14650th', '因特尔', NULL, 2000.00, 1, 2, 'in_stock', NULL, NULL);
INSERT INTO `devices` VALUES (14, 'CG1767114622903', 'CPU', 'i7-14650th', '因特尔', NULL, 2000.00, 1, 2, 'in_stock', NULL, NULL);
INSERT INTO `devices` VALUES (15, 'CG1767114622908', 'CPU', 'i7-14650th', '因特尔', NULL, 2000.00, 1, 2, 'in_stock', NULL, NULL);
INSERT INTO `devices` VALUES (16, 'CG1767114715571', '内存条', 'ddr5 -16gh', '三星', NULL, 1620.00, 1, 2, 'scrapped', '2025-12-31 01:11:56', NULL);
INSERT INTO `devices` VALUES (17, 'CG1767114715575', '内存条', 'ddr5 -16gh', '三星', NULL, 1620.00, 1, 2, 'scrapped', '2025-12-31 01:11:56', NULL);
INSERT INTO `devices` VALUES (18, 'CG1767115290407', '移动硬盘', '1TB SSD', '西部数据', NULL, 560.00, 1, 1, 'in_stock', '2025-12-31 01:21:30', NULL);
INSERT INTO `devices` VALUES (19, 'CG1767115290411', '移动硬盘', '1TB SSD', '西部数据', NULL, 560.00, 1, 1, 'in_stock', '2025-12-31 01:21:30', NULL);
INSERT INTO `devices` VALUES (20, 'CG1767167419766', '采购111', 'model', '公司', NULL, 32.00, 3, 2, 'in_stock', '2025-12-31 15:50:20', NULL);
INSERT INTO `devices` VALUES (21, 'CG1767167419773', '采购111', 'model', '公司', NULL, 32.00, 3, 2, 'in_stock', '2025-12-31 15:50:20', NULL);
INSERT INTO `devices` VALUES (22, 'CG1767167419775', '采购111', 'model', '公司', NULL, 32.00, 3, 2, 'in_stock', '2025-12-31 15:50:20', NULL);

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
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '实验室表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of labs
-- ----------------------------
INSERT INTO `labs` VALUES (1, '仿真实验室', '郑州大学材料馆207', 1, '2025-12-22 17:22:58');
INSERT INTO `labs` VALUES (2, '高级金属材料实验室', '化工楼206', 3, '2025-12-22 17:23:21');

-- ----------------------------
-- Table structure for purchase_requests
-- ----------------------------
DROP TABLE IF EXISTS `purchase_requests`;
CREATE TABLE `purchase_requests`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '采购申请ID',
  `lab_id` bigint NOT NULL COMMENT '所属实验室ID (外键，自动填充)',
  `applicant_id` bigint NOT NULL COMMENT '申请人ID (外键)',
  `device_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '设备名称',
  `category_id` bigint NULL DEFAULT NULL COMMENT '设备分类ID (外键)',
  `model` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '设备型号',
  `manufacturer` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '生产厂家',
  `number` int NOT NULL DEFAULT 1 COMMENT '申请数量',
  `one_price` decimal(12, 2) NOT NULL COMMENT '单价',
  `reason` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '采购理由',
  `status` enum('pending','approved','rejected') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'pending' COMMENT '审批状态',
  `approved_by` bigint NULL DEFAULT NULL COMMENT '审批管理员ID (外键)',
  `approved_at` datetime NULL DEFAULT NULL COMMENT '审批通过时间（同步为设备购入日期）',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请提交时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `fk_purchase_lab`(`lab_id` ASC) USING BTREE,
  INDEX `fk_purchase_applicant`(`applicant_id` ASC) USING BTREE,
  INDEX `fk_purchase_category`(`category_id` ASC) USING BTREE,
  INDEX `fk_purchase_approver`(`approved_by` ASC) USING BTREE,
  CONSTRAINT `fk_purchase_applicant` FOREIGN KEY (`applicant_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_purchase_approver` FOREIGN KEY (`approved_by`) REFERENCES `users` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `fk_purchase_category` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `fk_purchase_lab` FOREIGN KEY (`lab_id`) REFERENCES `labs` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '设备采购申请表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of purchase_requests
-- ----------------------------
INSERT INTO `purchase_requests` VALUES (1, 2, 3, '化学试剂卡', 1, '20片盒装', '郑州生物', 2, 20.00, '实验使用', 'rejected', 2, '2025-12-31 00:31:33', '2025-12-30 23:26:13');
INSERT INTO `purchase_requests` VALUES (2, 2, 3, '示波器', 3, '29cm*23cm,高频款', '郑州电子科技有限公司', 2, 120.00, '实验用', 'approved', 2, '2025-12-31 00:49:35', '2025-12-31 00:32:54');
INSERT INTO `purchase_requests` VALUES (3, 2, 3, '摇晃仪', 2, '1wrps+19w供电版', '郑州电子仪器有限公司', 3, 120.00, '实验用，配平摇匀试剂', 'approved', 2, '2025-12-31 01:01:43', '2025-12-31 01:01:31');
INSERT INTO `purchase_requests` VALUES (4, 2, 3, 'CPU', 1, 'i7-14650th', '因特尔', 3, 2000.00, '计算算力采购', 'approved', 2, '2025-12-31 01:10:23', '2025-12-31 01:10:12');
INSERT INTO `purchase_requests` VALUES (5, 2, 3, '内存条', 1, 'ddr5 -16gh', '三星', 2, 1620.00, '内存采购', 'approved', 2, '2025-12-31 01:11:56', '2025-12-31 01:11:48');
INSERT INTO `purchase_requests` VALUES (6, 1, 5, '移动硬盘', 1, '1TB SSD', '西部数据', 2, 560.00, '数据云存储', 'approved', 2, '2025-12-31 01:21:30', '2025-12-31 01:21:18');
INSERT INTO `purchase_requests` VALUES (7, 2, 3, '采购111', 3, 'model', '公司', 3, 32.00, '理由', 'approved', 2, '2025-12-31 15:50:20', '2025-12-31 15:48:52');

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
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '维修记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of repair_records
-- ----------------------------

-- ----------------------------
-- Table structure for scrap_requests
-- ----------------------------
DROP TABLE IF EXISTS `scrap_requests`;
CREATE TABLE `scrap_requests`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '报废申请ID',
  `device_id` bigint NOT NULL COMMENT '申请报废的设备ID (外键)',
  `applicant_id` bigint NOT NULL COMMENT '申请人ID (外键)',
  `reason` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '报废原因',
  `status` enum('pending','approved','rejected') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'pending' COMMENT '审批状态',
  `approved_by` bigint NULL DEFAULT NULL COMMENT '审批管理员ID (外键，可为空)',
  `approved_at` datetime NULL DEFAULT NULL COMMENT '审批通过时间',
  `rejected_reason` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '驳回理由',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `fk_scrap_device`(`device_id` ASC) USING BTREE,
  INDEX `fk_scrap_applicant`(`applicant_id` ASC) USING BTREE,
  INDEX `fk_scrap_approver`(`approved_by` ASC) USING BTREE,
  CONSTRAINT `fk_scrap_applicant` FOREIGN KEY (`applicant_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_scrap_approver` FOREIGN KEY (`approved_by`) REFERENCES `users` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `fk_scrap_device` FOREIGN KEY (`device_id`) REFERENCES `devices` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '设备报废申请表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of scrap_requests
-- ----------------------------
INSERT INTO `scrap_requests` VALUES (1, 18, 5, '中中中中中', 'rejected', 2, '2025-12-31 02:35:51', NULL, '2025-12-31 02:14:24');
INSERT INTO `scrap_requests` VALUES (2, 16, 3, '金手指断了', 'approved', 2, '2025-12-31 02:36:40', NULL, '2025-12-31 02:36:23');
INSERT INTO `scrap_requests` VALUES (3, 17, 3, '用不了了', 'approved', 2, '2025-12-31 16:06:04', NULL, '2025-12-31 15:48:28');

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
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '设备调拨记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of transfer_records
-- ----------------------------
INSERT INTO `transfer_records` VALUES (1, 1, 1, 2, 1, '2025-12-22 17:24:31', '算力消耗');
INSERT INTO `transfer_records` VALUES (2, 1, 2, 1, 1, '2025-12-22 17:24:43', '仿真实验室算力需要');
INSERT INTO `transfer_records` VALUES (5, 2, 2, 1, 1, '2025-12-29 22:05:15', '');
INSERT INTO `transfer_records` VALUES (6, 2, 1, 2, 1, '2025-12-29 22:47:41', '');
INSERT INTO `transfer_records` VALUES (7, 3, 2, 1, 1, '2025-12-29 22:51:18', '');

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
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '设备使用记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of usage_records
-- ----------------------------
INSERT INTO `usage_records` VALUES (1, 1, 1, 1, '2025-12-22 17:24:48', NULL, '', '2025-12-22 17:24:48');

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
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of users
-- ----------------------------
INSERT INTO `users` VALUES (1, 'lihao', '123456', 'lihao', '13674983236', 'manager', 1, '2025-12-10 12:04:06');
INSERT INTO `users` VALUES (2, 'admin', '123123', 'admin', '15892031893', 'admin', NULL, '2025-12-29 21:02:42');
INSERT INTO `users` VALUES (3, 'manager', '123123', 'manager', '13415151255', 'manager', 2, '2025-12-29 21:03:10');
INSERT INTO `users` VALUES (5, 'ssuser', '123123', 'ssuser', '13233124412', 'user', 1, '2025-12-30 14:38:05');
INSERT INTO `users` VALUES (6, 'noalb', '123123', 'sadasf', '14243256523', 'user', 1, '2025-12-30 21:11:23');

SET FOREIGN_KEY_CHECKS = 1;
