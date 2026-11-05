-- MySQL dump 10.13  Distrib 9.7.2, for macos26.6 (arm64)
--
-- Host: 127.0.0.1    Database: lrcore_cloud
-- ------------------------------------------------------
-- Server version	9.7.2

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
SET @MYSQLDUMP_TEMP_LOG_BIN = @@SESSION.SQL_LOG_BIN;
SET @@SESSION.SQL_LOG_BIN= 0;

--
-- GTID state at the beginning of the backup 
--

SET @@GLOBAL.GTID_PURGED=/*!80000 '+'*/ '691c38a4-aa78-11f1-abb9-52e99b4eb49d:1-7468';

--
-- Table structure for table `sys_user_role`
--

DROP TABLE IF EXISTS `sys_user_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_user_role` (
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `relation_status` tinyint NOT NULL DEFAULT '0' COMMENT '关联状态（0有效 1无效）',
  `data_scope` tinyint NOT NULL DEFAULT '0' COMMENT '数据权限范围',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
  `create_user_id` bigint DEFAULT NULL COMMENT '创建者ID',
  `update_user_id` bigint DEFAULT NULL COMMENT '更新者ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `build_in` tinyint DEFAULT '0' COMMENT '是否内置(0-否 1-是)',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '删除标识（0未删除 1已删除）',
  `data_scope_type` tinyint NOT NULL DEFAULT '0' COMMENT '数据权限类型',
  `custom_data_scope` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '自定义数据范围',
  `field_permissions` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '字段级权限',
  `row_permissions` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '行级权限',
  `column_permissions` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '用户角色字段级权限配置（JSON格式）',
  PRIMARY KEY (`user_id`,`role_id`),
  KEY `idx_ur_data_scope` (`data_scope`) USING BTREE,
  KEY `idx_ur_deleted` (`deleted`) USING BTREE,
  KEY `idx_ur_relation_status` (`relation_status`) USING BTREE,
  KEY `idx_ur_role_id` (`role_id`) USING BTREE,
  KEY `idx_ur_role_user_deleted` (`role_id`,`user_id`,`deleted`) USING BTREE,
  KEY `idx_ur_user_role_deleted` (`user_id`,`role_id`,`deleted`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='用户-角色关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_user_role`
--

LOCK TABLES `sys_user_role` WRITE;
/*!40000 ALTER TABLE `sys_user_role` DISABLE KEYS */;
INSERT INTO `sys_user_role` VALUES (7264590000000000071,7264590000000000061,0,0,NULL,NULL,NULL,'2026-11-03 23:07:45',NULL,NULL,0,0,0,NULL,NULL,NULL,NULL),(7264590000000000072,7264590000000000063,0,0,NULL,NULL,NULL,'2026-11-03 16:57:05',NULL,NULL,0,0,0,NULL,NULL,NULL,NULL),(7264590000000000073,7264590000000000064,0,4,7264590000000000001,7264590000000000072,NULL,'2026-06-06 00:00:00',NULL,'普通用户角色',0,0,0,NULL,NULL,NULL,NULL),(7264590000000000074,7264590000000000064,0,4,7264590000000000001,7264590000000000072,NULL,'2026-06-06 00:00:00',NULL,'普通用户角色',0,0,0,NULL,NULL,NULL,NULL),(7264590000000000075,7264590000000000064,0,4,7264590000000000001,7264590000000000071,NULL,'2026-06-06 00:00:00',NULL,'普通用户角色',0,0,0,NULL,NULL,NULL,NULL),(7264590000000000076,7264590000000000065,0,4,7264590000000000001,7264590000000000071,NULL,'2026-06-06 00:00:00',NULL,'只读用户角色',0,0,0,NULL,NULL,NULL,NULL);
/*!40000 ALTER TABLE `sys_user_role` ENABLE KEYS */;
UNLOCK TABLES;
SET @@SESSION.SQL_LOG_BIN = @MYSQLDUMP_TEMP_LOG_BIN;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-11-05 21:58:50
