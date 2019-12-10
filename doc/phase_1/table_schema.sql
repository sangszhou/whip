/*
 Navicat MySQL Data Transfer

 Source Server         : remote desktop
 Source Server Type    : MySQL
 Source Server Version : 80018
 Source Host           : 30.1.72.128:3306
 Source Schema         : whip

 Target Server Type    : MySQL
 Target Server Version : 80018
 File Encoding         : 65001

 Date: 10/12/2019 19:59:54
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for execution
-- ----------------------------
DROP TABLE IF EXISTS `execution`;
CREATE TABLE `execution` (
  `id` varchar(256) COLLATE utf8_bin NOT NULL,
  `start_time` datetime NOT NULL,
  `end_time` datetime DEFAULT NULL,
  `status` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `template_id` varchar(256) COLLATE utf8_bin NOT NULL,
  `name` varchar(255) COLLATE utf8_bin NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

-- ----------------------------
-- Table structure for keiko_v1_messages_sqlQueue
-- ----------------------------
DROP TABLE IF EXISTS `keiko_v1_messages_sqlQueue`;
CREATE TABLE `keiko_v1_messages_sqlQueue` (
  `id` char(26) COLLATE utf8_bin NOT NULL,
  `fingerprint` char(36) COLLATE utf8_bin NOT NULL,
  `body` longtext COLLATE utf8_bin NOT NULL,
  `updated_at` bigint(13) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `keiko_messages_fingerprint_idx` (`fingerprint`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

-- ----------------------------
-- Table structure for keiko_v1_queue_sqlQueue
-- ----------------------------
DROP TABLE IF EXISTS `keiko_v1_queue_sqlQueue`;
CREATE TABLE `keiko_v1_queue_sqlQueue` (
  `id` char(26) COLLATE utf8_bin NOT NULL,
  `fingerprint` char(36) COLLATE utf8_bin NOT NULL,
  `delivery` bigint(13) NOT NULL,
  `locked` char(46) COLLATE utf8_bin NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `keiko_queue_fingerprint_idx` (`fingerprint`),
  KEY `keiko_queue_delivery_locked_idx` (`delivery`,`locked`),
  KEY `keiko_queue_locked_idx` (`locked`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

-- ----------------------------
-- Table structure for keiko_v1_unacked_sqlQueue
-- ----------------------------
DROP TABLE IF EXISTS `keiko_v1_unacked_sqlQueue`;
CREATE TABLE `keiko_v1_unacked_sqlQueue` (
  `id` char(26) COLLATE utf8_bin NOT NULL,
  `fingerprint` char(36) COLLATE utf8_bin NOT NULL,
  `expiry` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `keiko_unacked_fingerprint_idx` (`fingerprint`),
  KEY `keiko_unacked_expiry_idx` (`expiry`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

-- ----------------------------
-- Table structure for pipeline_template
-- ----------------------------
DROP TABLE IF EXISTS `pipeline_template`;
CREATE TABLE `pipeline_template` (
  `id` varchar(256) NOT NULL,
  `name` varchar(256) NOT NULL,
  `gmt_create` datetime NOT NULL,
  `gmt_modified` datetime NOT NULL,
  `trigger_interval` int(11) NOT NULL DEFAULT '600',
  `content` varchar(4096) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for stage
-- ----------------------------
DROP TABLE IF EXISTS `stage`;
CREATE TABLE `stage` (
  `id` varchar(256) COLLATE utf8_bin NOT NULL,
  `ref_id` varchar(256) COLLATE utf8_bin NOT NULL,
  `type` varchar(256) COLLATE utf8_bin NOT NULL,
  `name` varchar(256) COLLATE utf8_bin NOT NULL,
  `instance_id` varchar(256) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `execution_id` varchar(256) COLLATE utf8_bin DEFAULT NULL,
  `context` varchar(4096) COLLATE utf8_bin DEFAULT NULL,
  `output` varchar(4096) COLLATE utf8_bin DEFAULT NULL,
  `required` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `start_time` datetime DEFAULT NULL,
  `end_time` datetime DEFAULT NULL,
  `status` varchar(255) COLLATE utf8_bin NOT NULL,
  PRIMARY KEY (`instance_id`) USING BTREE,
  UNIQUE KEY `instance_id_idx` (`instance_id`),
  KEY `exe_id_idx` (`execution_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

SET FOREIGN_KEY_CHECKS = 1;
