/*
SQLyog Community v13.2.1 (64 bit)
MySQL - 8.0.26 : Database - vue
*********************************************************************
*/

/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
CREATE DATABASE /*!32312 IF NOT EXISTS*/`vue` /*!40100 DEFAULT CHARACTER SET utf8 */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `vue`;

/*Table structure for table `area_daily_stats` */

DROP TABLE IF EXISTS `area_daily_stats`;

CREATE TABLE `area_daily_stats` (
  `id` int NOT NULL AUTO_INCREMENT,
  `area_id` int NOT NULL COMMENT '关联区域',
  `stat_date` date NOT NULL COMMENT '统计日期',
  `report_count` int NOT NULL DEFAULT '0' COMMENT '上报总数',
  `abnormal_count` int NOT NULL DEFAULT '0' COMMENT '异常数量',
  `area_type` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_area_date` (`area_id`,`stat_date`),
  CONSTRAINT `area_daily_stats_ibfk_1` FOREIGN KEY (`area_id`) REFERENCES `areas` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb3;

/*Data for the table `area_daily_stats` */

insert  into `area_daily_stats`(`id`,`area_id`,`stat_date`,`report_count`,`abnormal_count`,`area_type`) values 
(1,3,'2025-08-13',1,5,'RESIDENTIAL'),
(2,4,'2025-08-13',2,50,'RESIDENTIAL'),
(3,2,'2025-08-13',1,4,'RESIDENTIAL'),
(4,5,'2025-08-13',1,4,'RESIDENTIAL'),
(6,6,'2025-08-13',1,33,'RESIDENTIAL'),
(7,6,'2025-09-21',1,0,'RESIDENTIAL');

/*Table structure for table `areas` */

DROP TABLE IF EXISTS `areas`;

CREATE TABLE `areas` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL COMMENT '区域名称',
  `base_value` decimal(12,2) NOT NULL COMMENT '基础数值',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `TYPE` enum('BRIDGE','TUNNEL','ROAD','COMMERCIAL','RESIDENTIAL','INDUSTRIAL','NATURAL','SPECIAL') NOT NULL COMMENT '区域类型',
  `unit` enum('SQUARE_KM','SEAT','PIECE','ROAD','HECTARE') NOT NULL COMMENT '计量单位',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=36 DEFAULT CHARSET=utf8mb3;

/*Data for the table `areas` */

insert  into `areas`(`id`,`name`,`base_value`,`created_at`,`TYPE`,`unit`) values 
(1,'华南大桥',1.00,'2025-08-09 20:15:02','BRIDGE','ROAD'),
(2,'洛溪大桥',1.00,'2025-08-10 08:21:20','BRIDGE','ROAD'),
(3,'宏威大厦',0.50,'2025-08-13 09:10:36','COMMERCIAL','SEAT'),
(4,'城际大厦',10.00,'2025-08-13 09:11:00','COMMERCIAL','SEAT'),
(5,'高村',50.00,'2025-08-13 09:20:05','RESIDENTIAL','HECTARE'),
(6,'西一村',50.00,'2025-08-13 09:26:11','RESIDENTIAL','HECTARE'),
(7,'八一大桥',1.00,'2025-12-21 11:11:28','BRIDGE','SEAT'),
(8,'南昌大桥',1.00,'2025-12-21 11:11:28','BRIDGE','SEAT'),
(9,'朝阳大桥',1.00,'2025-12-21 11:11:28','BRIDGE','SEAT'),
(10,'红谷隧道',1.00,'2025-12-21 11:11:31','TUNNEL','PIECE'),
(11,'阳明路隧道',1.00,'2025-12-21 11:11:31','TUNNEL','PIECE'),
(12,'阳明路',1.00,'2025-12-21 11:11:34','ROAD','ROAD'),
(13,'八一大道',1.00,'2025-12-21 11:11:34','ROAD','ROAD'),
(14,'红谷中大道',1.00,'2025-12-21 11:11:34','ROAD','ROAD'),
(15,'八一广场商圈',2.50,'2025-12-21 11:11:36','COMMERCIAL','SQUARE_KM'),
(16,'红谷滩CBD',5.80,'2025-12-21 11:11:36','COMMERCIAL','SQUARE_KM'),
(17,'中山路商业街',1.00,'2025-12-21 11:11:36','COMMERCIAL','ROAD'),
(18,'红谷滩新区',28.60,'2025-12-21 11:11:39','RESIDENTIAL','SQUARE_KM'),
(19,'朝阳新城',18.20,'2025-12-21 11:11:39','RESIDENTIAL','SQUARE_KM'),
(20,'青山湖区',65.50,'2025-12-21 11:11:39','RESIDENTIAL','SQUARE_KM'),
(21,'南昌经济技术开发区',158.00,'2025-12-21 11:11:42','INDUSTRIAL','SQUARE_KM'),
(22,'小蓝经济技术开发区',40.00,'2025-12-21 11:11:42','INDUSTRIAL','SQUARE_KM'),
(23,'艾溪湖湿地公园',260.00,'2025-12-21 11:11:45','NATURAL','HECTARE'),
(24,'瑶湖郊野森林公园',350.00,'2025-12-21 11:11:45','NATURAL','HECTARE'),
(25,'梅岭国家森林公园',150.00,'2025-12-21 11:11:45','NATURAL','SQUARE_KM'),
(26,'滕王阁景区',1.00,'2025-12-21 11:11:48','SPECIAL','PIECE'),
(27,'南昌大学前湖校区',3.60,'2025-12-21 11:11:48','SPECIAL','SQUARE_KM'),
(28,'南昌西站',1.00,'2025-12-21 11:11:48','SPECIAL','PIECE'),
(29,'南昌市建成区面积',366.00,'2025-12-21 11:11:52','SPECIAL','SQUARE_KM'),
(30,'东湖区',18.35,'2025-12-21 11:11:52','RESIDENTIAL','SQUARE_KM'),
(31,'西湖区',34.50,'2025-12-21 11:11:52','RESIDENTIAL','SQUARE_KM'),
(32,'青云谱区',40.40,'2025-12-21 11:11:52','RESIDENTIAL','SQUARE_KM'),
(33,'青山湖区',127.60,'2025-12-21 11:11:52','RESIDENTIAL','SQUARE_KM'),
(34,'新建区',2193.30,'2025-12-21 11:11:52','RESIDENTIAL','SQUARE_KM'),
(35,'南昌县县城',45.20,'2025-12-21 11:11:52','RESIDENTIAL','SQUARE_KM');

/*Table structure for table `areas_backup` */

DROP TABLE IF EXISTS `areas_backup`;

CREATE TABLE `areas_backup` (
  `id` int NOT NULL DEFAULT '0',
  `name` varchar(50) NOT NULL COMMENT '区域名称',
  `type` enum('桥梁','涵洞隧道','交通干道','商业区','住宅区','工业区','自然区域','特殊区域') NOT NULL COMMENT '区域类型',
  `unit` enum('平方公里','座','个','条','公顷') NOT NULL COMMENT '计量单位',
  `base_value` decimal(12,2) NOT NULL COMMENT '基础数值',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;

/*Data for the table `areas_backup` */

insert  into `areas_backup`(`id`,`name`,`type`,`unit`,`base_value`,`created_at`) values 
(1,'华南大桥','桥梁','条',1.00,'2025-08-09 20:15:02');

/*Table structure for table `cruise_mission` */

DROP TABLE IF EXISTS `cruise_mission`;

CREATE TABLE `cruise_mission` (
  `id` int NOT NULL AUTO_INCREMENT,
  `mission_name` varchar(100) NOT NULL COMMENT '任务名称',
  `mission_type` varchar(20) DEFAULT NULL,
  `status` varchar(20) DEFAULT NULL,
  `route_points` json NOT NULL COMMENT '巡航路线点集合',
  `assigned_uav_id` int DEFAULT NULL COMMENT '分配的无人机ID',
  `priority` int DEFAULT '1' COMMENT '任务优先级',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`),
  KEY `idx_assigned_uav` (`assigned_uav_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='巡航任务表';

/*Data for the table `cruise_mission` */

insert  into `cruise_mission`(`id`,`mission_name`,`mission_type`,`status`,`route_points`,`assigned_uav_id`,`priority`,`start_time`,`end_time`,`created_time`,`updated_time`,`deleted`) values 
(1,'南昌交通巡查','CRUISE','ACTIVE','[{\"lat\": 28.662, \"lng\": 115.907, \"name\": \"南昌站\"}, {\"lat\": 28.62, \"lng\": 115.768, \"name\": \"南昌西站\"}, {\"lat\": 28.865, \"lng\": 115.9, \"name\": \"昌北机场\"}]',1,2,'2025-11-10 16:08:43',NULL,'2025-11-10 15:57:23','2025-11-10 15:57:23',0),
(2,'机场快线','CRUISE','ACTIVE','[{\"lat\": 28.865, \"lng\": 115.9, \"name\": \"昌北机场\", \"stopTime\": 60}, {\"lat\": 28.679, \"lng\": 115.899, \"name\": \"八一广场\", \"stopTime\": 30}, {\"lat\": 28.662, \"lng\": 115.907, \"name\": \"南昌站\", \"stopTime\": 60}, {\"lat\": 28.865, \"lng\": 115.9, \"name\": \"昌北机场\"}, {\"lat\": 28.662, \"lng\": 115.907, \"name\": \"南昌站\"}]',2,2,'2025-11-10 21:15:41',NULL,'2025-11-10 18:14:09','2025-11-10 18:14:09',0),
(3,'机场快线','CRUISE','PENDING','[{\"lat\": 28.865, \"lng\": 115.9, \"name\": \"昌北机场\", \"stopTime\": 60}, {\"lat\": 28.679, \"lng\": 115.899, \"name\": \"八一广场\", \"stopTime\": 30}, {\"lat\": 28.662, \"lng\": 115.907, \"name\": \"南昌站\", \"stopTime\": 60}, {\"lat\": 28.6634, \"lng\": 115.8893, \"name\": \"南昌市洪都中医院\"}]',NULL,2,NULL,NULL,'2026-01-28 19:16:37','2026-01-28 19:16:37',0);

/*Table structure for table `dispatch_tasks` */

DROP TABLE IF EXISTS `dispatch_tasks`;

CREATE TABLE `dispatch_tasks` (
  `id` int NOT NULL AUTO_INCREMENT,
  `task_name` varchar(100) NOT NULL COMMENT '任务名称',
  `task_type_id` int NOT NULL COMMENT '任务类型',
  `task_time` datetime NOT NULL COMMENT '任务时间',
  `quantity` int NOT NULL DEFAULT '1' COMMENT '任务数量',
  `status` enum('COMPLETED','EXECUTING','PENDING') NOT NULL DEFAULT 'PENDING',
  PRIMARY KEY (`id`),
  KEY `task_type_id` (`task_type_id`),
  KEY `idx_task_time` (`task_time`),
  KEY `idx_status` (`status`),
  CONSTRAINT `dispatch_tasks_ibfk_1` FOREIGN KEY (`task_type_id`) REFERENCES `task_types` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb3;

/*Data for the table `dispatch_tasks` */

insert  into `dispatch_tasks`(`id`,`task_name`,`task_type_id`,`task_time`,`quantity`,`status`) values 
(1,'桥面危害-桥梁加固',8,'2025-08-11 09:39:48',1,'PENDING'),
(2,'路面危害-路面油污清理',1,'2025-08-05 09:41:11',1,'COMPLETED'),
(3,'交安设施-交通标志牌放置',1,'2025-08-11 09:41:39',1,'COMPLETED'),
(4,'桥面危害-桥梁加固',8,'2025-08-12 22:29:53',1,'PENDING'),
(5,'桥面危害-桥梁加固',8,'2025-08-13 22:30:14',1,'EXECUTING'),
(6,'桥面危害-桥梁加固',4,'2025-08-05 22:30:26',1,'COMPLETED'),
(7,'桥面危害-桥梁加固',3,'2025-08-06 22:30:40',1,'EXECUTING'),
(8,'桥面危害-桥梁加固',2,'2025-08-06 22:31:04',1,'EXECUTING');

/*Table structure for table `dispatch_tasks_backup` */

DROP TABLE IF EXISTS `dispatch_tasks_backup`;

CREATE TABLE `dispatch_tasks_backup` (
  `id` int NOT NULL DEFAULT '0',
  `task_name` varchar(100) NOT NULL COMMENT '任务名称',
  `task_type_id` int NOT NULL COMMENT '任务类型',
  `task_time` datetime NOT NULL COMMENT '任务时间',
  `quantity` int NOT NULL DEFAULT '1' COMMENT '任务数量',
  `status` enum('已完成','执行中','待处理') NOT NULL DEFAULT '待处理'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;

/*Data for the table `dispatch_tasks_backup` */

insert  into `dispatch_tasks_backup`(`id`,`task_name`,`task_type_id`,`task_time`,`quantity`,`status`) values 
(1,'桥面危害-桥梁加固',8,'2025-08-11 09:39:48',1,'待处理'),
(2,'路面危害-路面油污清理',1,'2025-08-05 09:41:11',1,'已完成'),
(3,'交安设施-交通标志牌放置',1,'2025-08-11 09:41:39',1,'执行中');

/*Table structure for table `flight_track` */

DROP TABLE IF EXISTS `flight_track`;

CREATE TABLE `flight_track` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `uav_id` int NOT NULL COMMENT '无人机ID',
  `mission_id` int DEFAULT NULL COMMENT '任务ID',
  `lng` decimal(10,7) NOT NULL COMMENT '经度',
  `lat` decimal(10,7) NOT NULL COMMENT '纬度',
  `altitude` decimal(6,2) DEFAULT NULL COMMENT '高度',
  `speed` decimal(6,2) DEFAULT NULL COMMENT '速度',
  `battery` int DEFAULT NULL COMMENT '电量',
  `recorded_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '记录时间',
  PRIMARY KEY (`id`),
  KEY `idx_uav_time` (`uav_id`,`recorded_time`),
  KEY `idx_mission` (`mission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='飞行轨迹表';

/*Data for the table `flight_track` */

/*Table structure for table `inspection_data` */

DROP TABLE IF EXISTS `inspection_data`;

CREATE TABLE `inspection_data` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `task_name` varchar(200) DEFAULT NULL COMMENT '任务/项目名称',
  `area_id` int DEFAULT NULL COMMENT '所属区域id（关联 areas.id，可为空）',
  `drone_id` varchar(100) DEFAULT NULL COMMENT '无人机编号/机号',
  `uploader_id` int DEFAULT NULL COMMENT '上传人 user_id',
  `capture_time` datetime DEFAULT NULL COMMENT '拍摄/采集时间（EXIF或填写）',
  `latitude` double DEFAULT NULL COMMENT '经度（EXIF）',
  `longitude` double DEFAULT NULL COMMENT '纬度（EXIF）',
  `altitude` double DEFAULT NULL COMMENT '高度（EXIF或设备）',
  `file_count` int DEFAULT '0' COMMENT '关联媒体数量',
  `data_type` enum('PHOTO','VIDEO','ORTHO','MODEL','POINTCLOUD','OTHER') DEFAULT 'PHOTO' COMMENT '主要数据类型',
  `description` text COMMENT '说明/事故描述/备注',
  `status` varchar(20) NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_task_name` (`task_name`),
  KEY `idx_area_id` (`area_id`),
  KEY `idx_drone_id` (`drone_id`),
  KEY `idx_capture_time` (`capture_time`),
  KEY `idx_lat_long` (`latitude`,`longitude`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='巡查数据主表（元数据）';

/*Data for the table `inspection_data` */

insert  into `inspection_data`(`id`,`task_name`,`area_id`,`drone_id`,`uploader_id`,`capture_time`,`latitude`,`longitude`,`altitude`,`file_count`,`data_type`,`description`,`status`,`created_at`,`updated_at`) values 
(1,'桥梁定期巡查_2024Q1',1,'DJI-AIR001',1,'2024-03-15 09:30:00',39.9042,116.4074,150.5,24,'PHOTO','季度桥梁结构安全检查，重点关注桥墩和桥面状况','已审核','2025-10-09 17:25:05','2025-10-09 17:25:05'),
(2,'高速公路边坡监测',2,'DJI-MAVIC002',2,'2024-03-18 14:20:00',31.2304,121.4737,80.2,15,'VIDEO','K25+300至K26+100路段边坡稳定性巡查','待审核','2025-10-09 17:25:05','2025-10-09 17:25:05'),
(3,'电力线路巡检_220kV',3,'DJI-PHANTOM003',1,'2024-03-20 10:15:00',23.1291,113.2644,200.8,8,'PHOTO','220kV输电线路走廊树障排查','已审核','2025-10-09 17:25:05','2025-10-09 17:25:05'),
(4,'建筑工地进度记录',1,'DJI-AIR001',3,'2024-03-22 16:45:00',39.9042,116.4074,120,12,'ORTHO','A地块主体结构施工进度正射影像采集','已归档','2025-10-09 17:25:05','2025-10-09 17:25:05'),
(5,'河道水质巡查',2,'DJI-MAVIC002',2,'2024-03-25 11:00:00',31.2304,121.4737,60.5,6,'VIDEO','河道排污口巡查及水质异常情况记录','待审核','2025-10-09 17:25:05','2025-10-09 17:25:05'),
(6,'矿山三维建模',4,'DJI-MATRICE004',1,'2024-03-28 13:30:00',25.0375,102.7221,350.7,156,'MODEL','采矿区高精度三维模型数据采集','已审核','2025-10-09 17:25:05','2025-10-09 17:25:05'),
(7,'地形测绘项目',3,'DJI-PHANTOM003',3,'2024-04-01 09:00:00',23.1291,113.2644,180.3,89,'POINTCLOUD','开发区地形测绘点云数据采集','已审核','2025-10-09 17:25:05','2025-10-09 17:25:05'),
(8,'应急事故勘查',1,'DJI-AIR001',1,'2024-04-03 15:20:00',39.9042,116.4074,95.8,18,'PHOTO','交通事故现场勘查及证据固定','已归档','2025-10-09 17:25:05','2025-10-09 17:25:05'),
(9,'农业植保监测',5,'DJI-AGRAS005',2,'2024-04-05 10:45:00',30.2741,120.1551,25.6,32,'OTHER','小麦病虫害无人机监测作业','待审核','2025-10-09 17:25:05','2025-10-09 17:25:05'),
(10,'城市绿化调查',2,'DJI-MAVIC002',3,'2024-04-08 14:10:00',31.2304,121.4737,75.2,27,'PHOTO','中心城区绿化覆盖率调查统计','已审核','2025-10-09 17:25:05','2025-10-09 17:25:05'),
(11,'城市绿化调查',1,'DJI-MAVIC002',1,NULL,NULL,NULL,NULL,NULL,NULL,'中心城区绿化覆盖率调查统计','PENDING','2025-10-12 15:36:47','2025-12-21 15:23:32'),
(12,'城市绿化调查',2,'DJI-MAVIC002',1,NULL,NULL,NULL,NULL,1,NULL,'中心城区绿化覆盖率调查统计','PENDING','2025-10-12 15:43:40','2025-12-21 15:23:34'),
(13,'矿山三维建模',1,'DJI-MATRICE004',1,NULL,NULL,NULL,NULL,1,NULL,'采矿区高精度三维模型数据采集','PENDING','2025-10-12 15:47:00','2025-12-21 15:23:35'),
(14,'矿山三维建模',1,'DJI-MATRICE004',1,NULL,NULL,NULL,NULL,1,NULL,'采矿区高精度三维模型数据采集','PENDING','2025-10-12 15:51:39','2025-12-21 15:23:38'),
(15,'巡航',4,'dIJ202501',1,NULL,NULL,NULL,NULL,1,NULL,'','PENDING','2025-11-20 16:06:09','2025-12-21 15:23:45'),
(16,'消防安全检查',5,'DIJ_256',1,NULL,NULL,NULL,NULL,1,NULL,'消防安全检查','PENDING','2025-12-21 11:38:13','2025-12-21 15:23:47');

/*Table structure for table `inspection_media` */

DROP TABLE IF EXISTS `inspection_media`;

CREATE TABLE `inspection_media` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `data_id` bigint NOT NULL COMMENT 'inspection_data.id',
  `file_name` varchar(512) NOT NULL COMMENT '原始文件名',
  `storage_path` varchar(1024) NOT NULL COMMENT '文件在服务器/对象存储的路径/URL',
  `mime_type` varchar(100) DEFAULT NULL,
  `file_size` bigint DEFAULT '0',
  `thumb_path` varchar(1024) DEFAULT NULL COMMENT '缩略图/预览图路径',
  `capture_time` datetime DEFAULT NULL,
  `latitude` double DEFAULT NULL,
  `longitude` double DEFAULT NULL,
  `altitude` double DEFAULT NULL,
  `exif` json DEFAULT NULL COMMENT '原始exif元数据（json）',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_data_id` (`data_id`),
  CONSTRAINT `inspection_media_ibfk_1` FOREIGN KEY (`data_id`) REFERENCES `inspection_data` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=69 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='巡查媒体文件（照片/视频/点云/模型）';

/*Data for the table `inspection_media` */

insert  into `inspection_media`(`id`,`data_id`,`file_name`,`storage_path`,`mime_type`,`file_size`,`thumb_path`,`capture_time`,`latitude`,`longitude`,`altitude`,`exif`,`created_at`) values 
(35,1,'bridge_inspection_001.jpg','https://upload.wikimedia.org/wikipedia/commons/0/0c/GoldenGateBridge-001.jpg','image/jpeg',4521789,'https://upload.wikimedia.org/wikipedia/commons/0/0c/GoldenGateBridge-001.jpg','2024-03-15 09:31:23',39.9042,116.4074,148.2,'{\"ISO\": 100, \"Make\": \"DJI\", \"Model\": \"Air 2S\", \"FNumber\": 2.8, \"ExposureTime\": \"1/800\"}','2025-10-09 20:24:44'),
(36,2,'highway_slope_001.mp4','https://www.example.com/highway_slope_001.mp4','video/mp4',23456789,'https://www.example.com/highway_slope_001_thumb.jpg','2024-03-18 14:21:30',31.2304,121.4737,78.5,'{\"Make\": \"DJI\", \"Model\": \"Mavic 3\", \"Duration\": \"00:03:45\"}','2025-10-09 20:24:44'),
(39,1,'bridge_inspection_002.jpg','https://q2.itc.cn/q_70/images01/20250731/e7690341b4df42d4b999d723f307b166.jpeg','image/jpeg',3567890,'https://q2.itc.cn/q_70/images01/20250731/e7690341b4df42d4b999d723f307b166.jpeg','2024-03-15 09:32:45',31.2304,121.4737,152.1,'{\"ISO\": 100, \"Make\": \"DJI\", \"Model\": \"Air 2S\", \"FNumber\": 2.8, \"FocalLength\": \"24mm\", \"GPSAltitude\": 152.1, \"GPSLatitude\": 31.2304, \"ExposureTime\": \"1/1000\", \"GPSLongitude\": 121.4737}','2025-10-10 10:35:58'),
(40,2,'highway_slope_inspection_001.jpg','https://q8.itc.cn/images01/20250428/f84f7ab53bfd480fb68d6581ae6e508c.jpeg','image/jpeg',3245678,'https://q8.itc.cn/images01/20250428/f84f7ab53bfd480fb68d6581ae6e508c.jpeg','2024-03-18 14:25:00',31.2305,121.4738,78.5,'{\"ISO\": 200, \"Make\": \"DJI\", \"Model\": \"Mavic 3\", \"FNumber\": 4.0, \"FocalLength\": \"28mm\", \"GPSAltitude\": 78.5, \"GPSLatitude\": 31.2305, \"ExposureTime\": \"1/800\", \"GPSLongitude\": 121.4738}','2025-10-10 10:44:35'),
(41,3,'power_line_inspection_001.jpg','https://q6.itc.cn/q_70/images03/20240423/aed6ecf8b04443cdac07e4baf19df14f.jpeg','image/jpeg',3245678,'https://q6.itc.cn/q_70/images03/20240423/aed6ecf8b04443cdac07e4baf19df14f.jpeg','2024-03-20 10:16:30',23.1292,113.2645,198.5,'{\"ISO\": 100, \"Make\": \"DJI\", \"Model\": \"Phantom 4 Pro\", \"FNumber\": 5.6, \"FocalLength\": \"24mm\", \"GPSAltitude\": 198.5, \"GPSLatitude\": 23.1292, \"ExposureTime\": \"1/800\", \"GPSLongitude\": 113.2645}','2025-10-10 10:48:54'),
(42,3,'power_line_inspection_002.jpg','https://pic1.nmgnews.com.cn/003/008/746/00300874617_43d73470.jpg','image/jpeg',2890123,'https://pic1.nmgnews.com.cn/003/008/746/00300874617_43d73470.jpg','2024-03-20 10:18:15',23.1289,113.2642,203.2,'{\"ISO\": 200, \"Make\": \"DJI\", \"Model\": \"Phantom 4 Pro\", \"FNumber\": 4.0, \"FocalLength\": \"35mm\", \"GPSAltitude\": 203.2, \"GPSLatitude\": 23.1289, \"ExposureTime\": \"1/1000\", \"GPSLongitude\": 113.2642}','2025-10-10 10:48:54'),
(44,4,'construction_site_progress_001.jpg','https://copyright.bdstatic.com/vcg/edit/e7489beb2b923b9e454a1f8b32554e82.jpg','image/jpeg',4234567,'https://copyright.bdstatic.com/vcg/edit/e7489beb2b923b9e454a1f8b32554e82.jpg','2024-03-22 16:45:00',39.9042,116.4074,120,'{\"ISO\": 200, \"Make\": \"DJI\", \"Model\": \"Air 2S\", \"FNumber\": 4.0, \"FocalLength\": \"35mm\", \"GPSAltitude\": 120.0, \"GPSLatitude\": 39.9042, \"ExposureTime\": \"1/800\", \"GPSLongitude\": 116.4074}','2025-10-10 10:54:43'),
(45,4,'construction_site_progress_002.jpg','https://omo-oss-image.thefastimg.com/portal-saas/pg2024101617050216508/cms/image/73cde420-168a-49bc-bdbb-62e23a4e35f4.jpg','image/jpeg',3876543,'https://omo-oss-image.thefastimg.com/portal-saas/pg2024101617050216508/cms/image/73cde420-168a-49bc-bdbb-62e23a4e35f4.jpg','2024-03-22 16:48:30',39.9043,116.4075,118.5,'{\"ISO\": 100, \"Make\": \"DJI\", \"Model\": \"Air 2S\", \"FNumber\": 2.8, \"FocalLength\": \"24mm\", \"GPSAltitude\": 118.5, \"GPSLatitude\": 39.9043, \"ExposureTime\": \"1/1000\", \"GPSLongitude\": 116.4075}','2025-10-10 10:54:43'),
(46,5,'river_water_quality_001.jpg','https://wx2.sinaimg.cn/mw690/b99d2b3dly1hyhw58c9nhj20p30il0yi.jpg','image/jpeg',2456789,'https://wx2.sinaimg.cn/mw690/b99d2b3dly1hyhw58c9nhj20p30il0yi.jpg','2024-03-25 11:00:00',31.2304,121.4737,60.5,'{\"ISO\": 200, \"Make\": \"DJI\", \"Model\": \"Mavic 2\", \"FNumber\": 4.0, \"FocalLength\": \"28mm\", \"GPSAltitude\": 60.5, \"GPSLatitude\": 31.2304, \"ExposureTime\": \"1/800\", \"GPSLongitude\": 121.4737}','2025-10-10 11:53:47'),
(47,5,'river_water_quality_002.jpg','https://q9.itc.cn/images01/20250822/2e09b9ceeef14e2296a05c567920ba82.jpeg','image/jpeg',3567890,'https://q9.itc.cn/images01/20250822/2e09b9ceeef14e2296a05c567920ba82.jpeg','2024-03-25 11:02:30',31.2305,121.4738,62.1,'{\"ISO\": 100, \"Make\": \"DJI\", \"Model\": \"Mavic 2\", \"FNumber\": 2.8, \"FocalLength\": \"24mm\", \"GPSAltitude\": 62.1, \"GPSLatitude\": 31.2305, \"ExposureTime\": \"1/1000\", \"GPSLongitude\": 121.4738}','2025-10-10 11:53:47'),
(48,5,'river_water_quality_003.jpg','https://wx1.sinaimg.cn/mw1024/9d28f3b5ly4huped1h0arj20fa0r6qml.jpg','image/jpeg',4123456,'https://wx1.sinaimg.cn/mw1024/9d28f3b5ly4huped1h0arj20fa0r6qml.jpg','2024-03-25 11:05:15',31.2303,121.4736,58.8,'{\"ISO\": 400, \"Make\": \"DJI\", \"Model\": \"Mavic 2\", \"FNumber\": 5.6, \"FocalLength\": \"35mm\", \"GPSAltitude\": 58.8, \"GPSLatitude\": 31.2303, \"ExposureTime\": \"1/640\", \"GPSLongitude\": 121.4736}','2025-10-10 11:53:47'),
(49,6,'mine_modeling_001.jpg','https://p5.itc.cn/q_70/images03/20230805/f5745fae2f08436b99a862fe39f2174f.jpeg','image/jpeg',4234567,'https://p5.itc.cn/q_70/images03/20230805/f5745fae2f08436b99a862fe39f2174f.jpeg','2024-03-28 13:35:00',25.0375,102.7221,348.2,'{\"ISO\": 200, \"Make\": \"DJI\", \"Model\": \"Matrice 350\", \"FNumber\": 5.6, \"FocalLength\": \"35mm\", \"ExposureTime\": \"1/800\"}','2025-10-10 12:24:53'),
(50,6,'mine_modeling_002.jpg','https://p1.itc.cn/q_70/images03/20230805/cb194f6b48ab4dd0adc9452a0e3af983.jpeg','image/jpeg',3876543,'https://p1.itc.cn/q_70/images03/20230805/cb194f6b48ab4dd0adc9452a0e3af983.jpeg','2024-03-28 13:40:00',25.0376,102.7222,352.8,'{\"ISO\": 100, \"Make\": \"DJI\", \"Model\": \"Matrice 350\", \"FNumber\": 4.0, \"FocalLength\": \"24mm\", \"ExposureTime\": \"1/1000\"}','2025-10-10 12:24:53'),
(51,7,'topographic_survey_001.jpg','https://q4.itc.cn/q_70/images03/20240925/5d77c0002496430a9b40a4f747ef95d2.jpeg','image/jpeg',4567890,'https://q4.itc.cn/q_70/images03/20240925/5d77c0002496430a9b40a4f747ef95d2.jpeg','2024-04-01 09:05:00',23.1291,113.2644,178.9,'{\"ISO\": 100, \"Make\": \"DJI\", \"Model\": \"Phantom 4 RTK\", \"FNumber\": 5.6, \"FocalLength\": \"20mm\", \"ExposureTime\": \"1/640\"}','2025-10-10 12:24:53'),
(52,7,'topographic_survey_002.jpg','https://enterprise-insights.dji.com/cn/wp-content/uploads/2023/11/8-1024x617.jpg','image/jpeg',3123456,'https://enterprise-insights.dji.com/cn/wp-content/uploads/2023/11/8-1024x617.jpg','2024-04-01 09:15:00',23.1292,113.2645,181.5,'{\"ISO\": 200, \"Make\": \"DJI\", \"Model\": \"Phantom 4 RTK\", \"FNumber\": 4.5, \"FocalLength\": \"20mm\", \"ExposureTime\": \"1/500\"}','2025-10-10 12:24:53'),
(53,8,'accident_site_001.jpg','https://khimg.tezhongzhuangbei.com/image/20191202/20191202105316_75136.jpg','image/jpeg',2987654,'https://khimg.tezhongzhuangbei.com/image/20191202/20191202105316_75136.jpg','2024-04-03 15:22:00',39.9042,116.4074,94.2,'{\"ISO\": 250, \"Make\": \"DJI\", \"Model\": \"Air 2S\", \"FNumber\": 4.0, \"FocalLength\": \"24mm\", \"ExposureTime\": \"1/400\"}','2025-10-10 12:24:53'),
(54,8,'accident_site_002.jpg','https://img.cjyun.org/a/10122/202501/69d8ca5b2ec1ce0dff2d1e19f1aed087.jpeg','image/jpeg',3456789,'https://img.cjyun.org/a/10122/202501/69d8ca5b2ec1ce0dff2d1e19f1aed087.jpeg','2024-04-03 15:25:00',39.9043,116.4075,96.8,'{\"ISO\": 200, \"Make\": \"DJI\", \"Model\": \"Air 2S\", \"FNumber\": 4.0, \"FocalLength\": \"24mm\", \"ExposureTime\": \"1/500\"}','2025-10-10 12:24:53'),
(55,8,'accident_site_003.jpg','https://q8.itc.cn/images01/20240331/efe49245530a406dab683a290fd42a2f.jpeg','image/jpeg',4123456,'https://q8.itc.cn/images01/20240331/efe49245530a406dab683a290fd42a2f.jpeg','2024-04-03 15:28:00',39.9041,116.4073,97.5,'{\"ISO\": 320, \"Make\": \"DJI\", \"Model\": \"Air 2S\", \"FNumber\": 3.5, \"FocalLength\": \"24mm\", \"ExposureTime\": \"1/320\"}','2025-10-10 12:24:53'),
(56,8,'accident_site_004.jpg','https://pic.rmb.bdstatic.com/bjh/bc130e8fe37/250115/00b8caf57f93b323f8203c14ad40bef5.jpeg','image/jpeg',3678901,'https://pic.rmb.bdstatic.com/bjh/bc130e8fe37/250115/00b8caf57f93b323f8203c14ad40bef5.jpeg','2024-04-03 15:30:00',39.9042,116.4074,95.8,'{\"ISO\": 400, \"Make\": \"DJI\", \"Model\": \"Air 2S\", \"FNumber\": 3.2, \"FocalLength\": \"24mm\", \"ExposureTime\": \"1/250\"}','2025-10-10 12:24:53'),
(58,9,'agriculture_monitor_002.jpg','https://qcloud.dpfile.com/pc/3q4Us-zAFkIr1TivUicD7qxAO6uaEjHHR4ho5TXF4USietqLHudAnaOHSya7N4YhY0q73sB2DyQcgmKUxZFQtw.jpg','image/jpeg',2890123,'https://qcloud.dpfile.com/pc/3q4Us-zAFkIr1TivUicD7qxAO6uaEjHHR4ho5TXF4USietqLHudAnaOHSya7N4YhY0q73sB2DyQcgmKUxZFQtw.jpg','2024-04-05 10:52:00',30.2742,120.1552,26.3,'{\"ISO\": 125, \"Make\": \"DJI\", \"Model\": \"Agras T40\", \"FNumber\": 3.2, \"FocalLength\": \"16mm\", \"ExposureTime\": \"1/800\"}','2025-10-10 12:24:53'),
(59,9,'agriculture_monitor_003.jpg','https://q5.itc.cn/images01/20240918/442d83d43e3c4e5080b12bc31d74b89e.jpeg','image/jpeg',4012345,'https://q5.itc.cn/images01/20240918/442d83d43e3c4e5080b12bc31d74b89e.jpeg','2024-04-05 10:55:00',30.274,120.155,25.6,'{\"ISO\": 100, \"Make\": \"DJI\", \"Model\": \"Agras T40\", \"FNumber\": 2.8, \"FocalLength\": \"16mm\", \"ExposureTime\": \"1/1250\"}','2025-10-10 12:24:53'),
(60,10,'urban_greening_001.jpg','https://q9.itc.cn/q_70/images01/20240523/c8a847e6a8af4f7081559358202d29d6.jpeg','image/jpeg',3789012,'https://q9.itc.cn/q_70/images01/20240523/c8a847e6a8af4f7081559358202d29d6.jpeg','2024-04-08 14:12:00',31.2304,121.4737,73.8,'{\"ISO\": 100, \"Make\": \"DJI\", \"Model\": \"Mavic 3\", \"FNumber\": 2.8, \"FocalLength\": \"24mm\", \"ExposureTime\": \"1/800\"}','2025-10-10 12:24:53'),
(61,10,'urban_greening_002.jpg','https://bpic.588ku.com/video_listen/588ku_cover/00/48/17/0d9baad081f570a2481e4135d096038e.jpg','image/jpeg',3456789,'https://bpic.588ku.com/video_listen/588ku_cover/00/48/17/0d9baad081f570a2481e4135d096038e.jpg','2024-04-08 14:15:00',31.2305,121.4738,76.2,'{\"ISO\": 100, \"Make\": \"DJI\", \"Model\": \"Mavic 3\", \"FNumber\": 2.8, \"FocalLength\": \"24mm\", \"ExposureTime\": \"1/1000\"}','2025-10-10 12:24:53'),
(62,10,'urban_greening_003.jpg','https://bpic.588ku.com/video_listen/588ku_cover/00/48/17/9393171ec4e863e41b0d78f7073aee40.jpg','image/jpeg',3123456,'https://bpic.588ku.com/video_listen/588ku_cover/00/48/17/9393171ec4e863e41b0d78f7073aee40.jpg','2024-04-08 14:18:00',31.2303,121.4736,75.5,'{\"ISO\": 200, \"Make\": \"DJI\", \"Model\": \"Mavic 3\", \"FNumber\": 4.0, \"FocalLength\": \"24mm\", \"ExposureTime\": \"1/640\"}','2025-10-10 12:24:53'),
(63,10,'urban_greening_004.jpg','https://hellorfimg.zcool.cn/provider_image/large/hi2243223780.jpg','image/jpeg',4234567,'https://hellorfimg.zcool.cn/provider_image/large/hi2243223780.jpg','2024-04-08 14:20:00',31.2304,121.4737,74.9,'{\"ISO\": 160, \"Make\": \"DJI\", \"Model\": \"Mavic 3\", \"FNumber\": 3.5, \"FocalLength\": \"24mm\", \"ExposureTime\": \"1/500\"}','2025-10-10 12:24:53'),
(66,14,'btd89IV6i2_small.jpg','/uploads/2025-10-12/ec6643e5-7a50-48e1-9640-48ca2b47153f.jpg','image/jpeg',99912,NULL,NULL,NULL,NULL,NULL,NULL,'2025-10-12 15:51:39'),
(67,15,'2.mp4','/uploads/2025-11-20/de98ab4f-ee6c-4364-b44e-9f4962ff8ff4.mp4','video/mp4',12285150,NULL,NULL,NULL,NULL,NULL,NULL,'2025-11-20 16:06:09'),
(68,16,'新建 文本文档 (3).txt','/uploads/2025-12-21/4618230e-8676-4c53-be15-fc008b36ecb6.txt','text/plain',44987,NULL,NULL,NULL,NULL,NULL,NULL,'2025-12-21 11:38:13');

/*Table structure for table `inspection_reports` */

DROP TABLE IF EXISTS `inspection_reports`;

CREATE TABLE `inspection_reports` (
  `id` int NOT NULL AUTO_INCREMENT,
  `area_id` int NOT NULL COMMENT '关联区域',
  `report_type_id` int NOT NULL COMMENT '上报类型',
  `quantity` int unsigned NOT NULL DEFAULT '1' COMMENT '上报数量',
  `report_time` datetime NOT NULL COMMENT '上报时间',
  `details` text COMMENT '详情描述',
  PRIMARY KEY (`id`),
  KEY `area_id` (`area_id`),
  KEY `report_type_id` (`report_type_id`),
  KEY `idx_report_time` (`report_time`),
  CONSTRAINT `inspection_reports_ibfk_1` FOREIGN KEY (`area_id`) REFERENCES `areas` (`id`),
  CONSTRAINT `inspection_reports_ibfk_2` FOREIGN KEY (`report_type_id`) REFERENCES `report_types` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb3;

/*Data for the table `inspection_reports` */

insert  into `inspection_reports`(`id`,`area_id`,`report_type_id`,`quantity`,`report_time`,`details`) values 
(1,1,4,1,'2025-08-10 10:07:03','高峰时段流量超载'),
(2,3,2,1,'2025-08-13 09:27:26','楼面破损'),
(3,4,7,1,'2025-08-13 20:45:03','监控设备老旧'),
(4,5,9,1,'2025-08-13 21:22:13','绿化维护'),
(5,4,1,1,'2025-08-13 21:55:26','xxxxxx'),
(6,6,4,1,'2025-08-13 21:58:15','xxxxxxxx'),
(7,6,6,1,'2025-09-21 21:34:59','xxxxx');

/*Table structure for table `menu` */

DROP TABLE IF EXISTS `menu`;

CREATE TABLE `menu` (
  `menu_id` int NOT NULL AUTO_INCREMENT COMMENT '菜单编号,主键自增',
  `name` varchar(100) DEFAULT NULL COMMENT '组件名称',
  `redirect` varchar(100) DEFAULT NULL COMMENT '重定向地址',
  `path` varchar(255) DEFAULT NULL COMMENT '组件地址',
  `component` varchar(255) DEFAULT NULL COMMENT '组件对象',
  `meta` json DEFAULT NULL COMMENT '路由的元配置',
  `parent_id` int DEFAULT '0' COMMENT '父ID,关联菜单主键,默认一级残带',
  PRIMARY KEY (`menu_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb3 COMMENT='菜单表';

/*Data for the table `menu` */

insert  into `menu`(`menu_id`,`name`,`redirect`,`path`,`component`,`meta`,`parent_id`) values 
(1,'Nested','/nested/menu1','/nested','Layout','{\"icon\": \"nested\", \"title\": \"Nested\"}',0),
(2,'Menu1',NULL,'menu1','nested/menu1/index','{\"title\": \"Menu1\"}',1),
(3,'Menu1-1',NULL,'menu1-1','nested/menu1/menu1-1','{\"title\": \"Menu1-1\"}',2);

/*Table structure for table `orders` */

DROP TABLE IF EXISTS `orders`;

CREATE TABLE `orders` (
  `id` int NOT NULL AUTO_INCREMENT,
  `order_sn` varchar(32) NOT NULL COMMENT '订单号',
  `status` varchar(20) DEFAULT NULL,
  `pickup_lng` decimal(10,7) NOT NULL COMMENT '取件点经度',
  `pickup_lat` decimal(10,7) NOT NULL COMMENT '取件点纬度',
  `delivery_lng` decimal(10,7) NOT NULL COMMENT '配送点经度',
  `delivery_lat` decimal(10,7) NOT NULL COMMENT '配送点纬度',
  `weight` decimal(5,2) DEFAULT '1.00' COMMENT '重量(kg)',
  `assigned_uav_id` int DEFAULT NULL COMMENT '分配的无人机ID',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_order_sn` (`order_sn`),
  KEY `idx_status` (`status`),
  KEY `idx_assigned_uav` (`assigned_uav_id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单表';

/*Data for the table `orders` */

insert  into `orders`(`id`,`order_sn`,`status`,`pickup_lng`,`pickup_lat`,`delivery_lng`,`delivery_lat`,`weight`,`assigned_uav_id`,`created_time`,`updated_time`,`deleted`) values 
(1,'EXP_1762762010995','COMPLETED',115.8990000,28.6790000,115.9070000,28.6620000,1.50,5,'2025-11-10 16:06:51','2025-11-10 16:06:51',0),
(2,'EXP_1762780558759','ASSIGNED',115.8890000,28.6630000,115.9070000,28.6620000,1.20,4,'2025-11-10 21:15:59','2025-11-10 21:15:59',0),
(3,'EXP_1762780762444','ASSIGNED',115.9000000,28.8650000,115.9830000,28.6250000,1.00,3,'2025-11-10 21:19:22','2025-11-10 21:19:22',0),
(4,'EXP_1769598978919','ASSIGNED',115.8925780,28.6819450,115.8893000,28.6634000,1.00,10,'2026-01-28 19:16:19','2026-01-28 19:16:19',0),
(5,'EXP_1769598980017','ASSIGNED',115.8925780,28.6819450,115.8893000,28.6634000,1.00,11,'2026-01-28 19:16:20','2026-01-28 19:16:20',0);

/*Table structure for table `preset_route` */

DROP TABLE IF EXISTS `preset_route`;

CREATE TABLE `preset_route` (
  `id` int NOT NULL AUTO_INCREMENT,
  `route_name` varchar(100) NOT NULL COMMENT '路线名称',
  `description` varchar(500) DEFAULT NULL COMMENT '描述',
  `route_points` json NOT NULL COMMENT '路线点',
  `total_distance` decimal(8,2) DEFAULT NULL COMMENT '总距离(km)',
  `estimated_duration` int DEFAULT NULL COMMENT '预计耗时(分钟)',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='预设路线表';

/*Data for the table `preset_route` */

insert  into `preset_route`(`id`,`route_name`,`description`,`route_points`,`total_distance`,`estimated_duration`,`created_time`,`updated_time`,`deleted`) values 
(1,'交通枢纽环线','覆盖南昌主要交通枢纽的巡航路线','[{\"lat\": 28.865, \"lng\": 115.9, \"name\": \"昌北机场\", \"stopTime\": 120}, {\"lat\": 28.662, \"lng\": 115.907, \"name\": \"南昌站\", \"stopTime\": 90}, {\"lat\": 28.62, \"lng\": 115.768, \"name\": \"南昌西站\", \"stopTime\": 120}, {\"lat\": 28.625, \"lng\": 115.983, \"name\": \"南昌东站\", \"stopTime\": 90}]',NULL,NULL,'2025-11-05 19:48:48','2025-11-05 19:48:48',0),
(2,'机场快线','昌北机场到市区的快速物流通道','[{\"lat\": 28.865, \"lng\": 115.9, \"name\": \"昌北机场\", \"stopTime\": 60}, {\"lat\": 28.679, \"lng\": 115.899, \"name\": \"八一广场\", \"stopTime\": 30}, {\"lat\": 28.662, \"lng\": 115.907, \"name\": \"南昌站\", \"stopTime\": 60}]',NULL,NULL,'2025-11-05 19:48:48','2025-11-05 19:48:48',0);

/*Table structure for table `report_types` */

DROP TABLE IF EXISTS `report_types`;

CREATE TABLE `report_types` (
  `id` int NOT NULL AUTO_INCREMENT,
  `type_name` varchar(20) NOT NULL COMMENT '上报类型名称',
  PRIMARY KEY (`id`),
  UNIQUE KEY `type_name` (`type_name`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb3;

/*Data for the table `report_types` */

insert  into `report_types`(`id`,`type_name`) values 
(7,'交安设施'),
(2,'交通事故'),
(5,'农林病害'),
(1,'日常养护'),
(4,'桥通问题'),
(9,'绿化养护'),
(6,'路基问题'),
(3,'路面问题'),
(8,'除雪作业');

/*Table structure for table `role` */

DROP TABLE IF EXISTS `role`;

CREATE TABLE `role` (
  `role_id` int NOT NULL AUTO_INCREMENT COMMENT '角色编号, 主键自增',
  `role_name` varchar(100) NOT NULL COMMENT '角色名',
  PRIMARY KEY (`role_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb3 COMMENT='角色表';

/*Data for the table `role` */

insert  into `role`(`role_id`,`role_name`) values 
(1,'admin'),
(2,'user'),
(3,'CTO'),
(4,'CEO');

/*Table structure for table `task_types` */

DROP TABLE IF EXISTS `task_types`;

CREATE TABLE `task_types` (
  `id` int NOT NULL AUTO_INCREMENT,
  `type_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `type_name` (`type_name`)
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb3;

/*Data for the table `task_types` */

insert  into `task_types`(`id`,`type_name`) values 
(1,'交通事故处理'),
(9,'农林监测'),
(5,'安防巡逻'),
(6,'活动拍摄'),
(3,'测绘勘测'),
(2,'消防救援'),
(4,'物流运输'),
(7,'环境调查'),
(8,'设备维护'),
(10,'运输任务');

/*Table structure for table `uav` */

DROP TABLE IF EXISTS `uav`;

CREATE TABLE `uav` (
  `id` int NOT NULL AUTO_INCREMENT,
  `sn` varchar(32) NOT NULL COMMENT '无人机序列号',
  `model` varchar(64) DEFAULT 'Default_Model' COMMENT '型号',
  `status` varchar(20) DEFAULT NULL,
  `battery_level` int DEFAULT '100' COMMENT '电量百分比',
  `current_lng` decimal(10,7) DEFAULT '116.3974280' COMMENT '当前经度',
  `current_lat` decimal(10,7) DEFAULT '39.9092300' COMMENT '当前纬度',
  `load_capacity` decimal(5,2) DEFAULT '5.00' COMMENT '载重(kg)',
  `current_mission` varchar(255) DEFAULT NULL COMMENT '当前任务',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_sn` (`sn`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='无人机表';

/*Data for the table `uav` */

insert  into `uav`(`id`,`sn`,`model`,`status`,`battery_level`,`current_lng`,`current_lat`,`load_capacity`,`current_mission`,`created_time`,`updated_time`,`deleted`) values 
(1,'NCUAV_001','DJI_Mavic_3','ON_MISSION',95,115.9070000,28.6620000,5.00,'MISSION_1','2025-11-05 19:48:38','2025-11-05 19:48:38',0),
(2,'NCUAV_002','DJI_Mavic_3','ON_MISSION',88,115.7680000,28.6200000,5.00,'MISSION_2','2025-11-05 19:48:38','2025-11-05 19:48:38',0),
(3,'NCUAV_003','DJI_Mavic_3','ON_MISSION',92,115.9000000,28.8650000,5.00,'ORDER_3','2025-11-05 19:48:38','2025-11-05 19:48:38',0),
(4,'NCUAV_004','DJI_Matrice_350','ON_MISSION',100,115.9830000,28.6250000,5.00,'ORDER_2','2025-11-05 19:48:38','2025-11-05 19:48:38',0),
(5,'NCUAV_005','DJI_Mavic_3','ON_MISSION',78,115.8890000,28.6630000,5.00,'ORDER_1','2025-11-05 19:48:38','2025-11-05 19:48:38',0),
(6,'NCUAV_006','DJI_Mavic_3','ON_MISSION',89,116.3974280,39.9092300,5.00,NULL,'2025-11-21 16:30:31','2025-11-21 16:31:00',0),
(7,'DJI-001','DJI_Mavic_3','IDLE',100,115.8129000,28.6602000,5.00,NULL,'2025-12-07 20:51:35','2025-12-07 21:02:46',1),
(10,'DJI-970','DJI_M300','ON_MISSION',100,115.8976000,28.6812000,5.00,'ORDER_4','2025-12-08 21:47:25','2025-12-08 21:47:25',0),
(11,'DJI-681','DJI_M300','ON_MISSION',100,115.8976000,28.6812000,5.00,'ORDER_5','2025-12-21 09:44:03','2025-12-21 09:44:03',0),
(12,'DJI-224','DJI_M300','IDLE',100,115.9043000,28.6718000,5.00,NULL,'2025-12-21 10:25:45','2025-12-21 10:25:45',0),
(13,'DJI-551','DJI_Mavic_3','IDLE',100,115.8572000,28.6845000,5.00,NULL,'2025-12-22 15:35:19','2025-12-22 15:35:19',0),
(14,'DJI-915','DJI_Mavic_3','IDLE',100,115.8570000,28.6830000,5.00,NULL,'2025-12-22 15:51:29','2025-12-22 15:51:29',0),
(15,'DJI-197','DJI_Mavic_3','CHARGING',100,115.8572000,28.6845000,5.00,NULL,'2025-12-25 11:06:48','2025-12-25 11:06:48',0),
(16,'DJI-142','DJI_Mavic_3','CHARGING',100,115.8572000,28.6845000,5.00,NULL,'2026-01-26 19:25:58','2026-01-26 19:25:58',0),
(17,'DJI-841','DJI_Mavic_3','CHARGING',100,115.8129000,28.6602000,5.00,NULL,'2026-01-28 19:10:54','2026-01-28 19:10:54',0);

/*Table structure for table `upload_chunks` */

DROP TABLE IF EXISTS `upload_chunks`;

CREATE TABLE `upload_chunks` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `upload_id` varchar(128) NOT NULL COMMENT '客户端生成的上传会话id',
  `chunk_index` int NOT NULL,
  `chunk_size` int NOT NULL,
  `total_size` bigint NOT NULL,
  `file_name` varchar(512) NOT NULL,
  `uploaded_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `total_chunks` int DEFAULT NULL COMMENT '总分块数',
  `chunk_path` varchar(512) DEFAULT NULL COMMENT '分块文件存储路径',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_upload_chunk` (`upload_id`,`chunk_index`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='断点续传分块信息';

/*Data for the table `upload_chunks` */

/*Table structure for table `user` */

DROP TABLE IF EXISTS `user`;

CREATE TABLE `user` (
  `user_id` int NOT NULL AUTO_INCREMENT COMMENT '用户ID,主键自增',
  `username` varchar(100) NOT NULL COMMENT '用户名',
  `password` varchar(255) NOT NULL COMMENT '密码',
  `token` varchar(255) NOT NULL COMMENT '令牌',
  `avatar` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '头像',
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `token` (`token`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb3 COMMENT='用户表';

/*Data for the table `user` */

insert  into `user`(`user_id`,`username`,`password`,`token`,`avatar`) values 
(1,'admin','111111','1752888115815','https://img1.baidu.com/it/u=1637142835,3949135594&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=500'),
(3,'xixixi','111111','1754266442940','https://img1.baidu.com/it/u=1637142835,3949135594&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=500');

/*Table structure for table `user_role` */

DROP TABLE IF EXISTS `user_role`;

CREATE TABLE `user_role` (
  `role_id` int NOT NULL COMMENT '角色编号',
  `user_id` int NOT NULL COMMENT '用户编号',
  KEY `role_id` (`role_id`,`user_id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `user_role_ibfk_1` FOREIGN KEY (`role_id`) REFERENCES `role` (`role_id`),
  CONSTRAINT `user_role_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COMMENT='角色表';

/*Data for the table `user_role` */

insert  into `user_role`(`role_id`,`user_id`) values 
(1,1),
(2,3);

/* Trigger structure for table `inspection_reports` */

DELIMITER $$

/*!50003 DROP TRIGGER*//*!50032 IF EXISTS */ /*!50003 `after_insert_report` */$$

/*!50003 CREATE */ /*!50017 DEFINER = 'root'@'localhost' */ /*!50003 TRIGGER `after_insert_report` AFTER INSERT ON `inspection_reports` FOR EACH ROW BEGIN
    DECLARE area_type_val VARCHAR(50);
    DECLARE is_abnormal BOOLEAN;

    -- 获取区域类型
    SELECT TYPE INTO area_type_val FROM areas WHERE id = NEW.area_id;

    -- 获取报告类型是否异常
    SET is_abnormal = (SELECT is_abnormal FROM report_types WHERE id = NEW.report_type_id);

    -- 更新区域统计
    INSERT INTO area_daily_stats (area_id, stat_date, area_type, report_count, abnormal_count)
    VALUES (
        NEW.area_id,
        CURDATE(),
        area_type_val,
        1,
        IF(is_abnormal, 1, 0)
    )
    ON DUPLICATE KEY UPDATE
        report_count = report_count + 1,
        abnormal_count = abnormal_count + IF(is_abnormal, 1, 0);
END */$$


DELIMITER ;

/* Trigger structure for table `inspection_reports` */

DELIMITER $$

/*!50003 DROP TRIGGER*//*!50032 IF EXISTS */ /*!50003 `after_update_report` */$$

/*!50003 CREATE */ /*!50017 DEFINER = 'root'@'localhost' */ /*!50003 TRIGGER `after_update_report` AFTER UPDATE ON `inspection_reports` FOR EACH ROW BEGIN
    DECLARE old_area_type_val VARCHAR(50);
    DECLARE new_area_type_val VARCHAR(50);
    DECLARE old_is_abnormal BOOLEAN;
    DECLARE new_is_abnormal BOOLEAN;

    -- 获取旧区域类型
    SELECT TYPE INTO old_area_type_val FROM areas WHERE id = OLD.area_id;

    -- 获取新区域类型
    SELECT TYPE INTO new_area_type_val FROM areas WHERE id = NEW.area_id;

    -- 获取旧的异常状态
    SET old_is_abnormal = (SELECT is_abnormal FROM report_types WHERE id = OLD.report_type_id);

    -- 获取新的异常状态
    SET new_is_abnormal = (SELECT is_abnormal FROM report_types WHERE id = NEW.report_type_id);

    -- 更新旧区域统计
    INSERT INTO area_daily_stats (area_id, stat_date, area_type, report_count, abnormal_count)
    VALUES (
        OLD.area_id,
        CURDATE(),
        old_area_type_val,
        -1,
        IF(old_is_abnormal, -1, 0)
    )
    ON DUPLICATE KEY UPDATE
        report_count = report_count - 1,
        abnormal_count = abnormal_count - IF(old_is_abnormal, 1, 0);

    -- 更新新区域统计
    INSERT INTO area_daily_stats (area_id, stat_date, area_type, report_count, abnormal_count)
    VALUES (
        NEW.area_id,
        CURDATE(),
        new_area_type_val,
        1,
        IF(new_is_abnormal, 1, 0)
    )
    ON DUPLICATE KEY UPDATE
        report_count = report_count + 1,
        abnormal_count = abnormal_count + IF(new_is_abnormal, 1, 0);
END */$$


DELIMITER ;

/* Trigger structure for table `inspection_reports` */

DELIMITER $$

/*!50003 DROP TRIGGER*//*!50032 IF EXISTS */ /*!50003 `after_delete_report` */$$

/*!50003 CREATE */ /*!50017 DEFINER = 'root'@'localhost' */ /*!50003 TRIGGER `after_delete_report` AFTER DELETE ON `inspection_reports` FOR EACH ROW BEGIN
    DECLARE area_type_val VARCHAR(50);
    DECLARE is_abnormal BOOLEAN;

    -- 获取区域类型
    SELECT TYPE INTO area_type_val FROM areas WHERE id = OLD.area_id;

    -- 获取报告类型是否异常
    SET is_abnormal = (SELECT is_abnormal FROM report_types WHERE id = OLD.report_type_id);

    -- 更新区域统计
    INSERT INTO area_daily_stats (area_id, stat_date, area_type, report_count, abnormal_count)
    VALUES (
        OLD.area_id,
        CURDATE(),
        area_type_val,
        -1,
        IF(is_abnormal, -1, 0)
    )
    ON DUPLICATE KEY UPDATE
        report_count = report_count - 1,
        abnormal_count = abnormal_count - IF(is_abnormal, 1, 0);
END */$$


DELIMITER ;

/* Procedure structure for procedure `UpdateAreaStats` */

/*!50003 DROP PROCEDURE IF EXISTS  `UpdateAreaStats` */;

DELIMITER $$

/*!50003 CREATE DEFINER=`root`@`localhost` PROCEDURE `UpdateAreaStats`(IN area_id INT, IN is_abnormal BOOLEAN, IN increment INT)
BEGIN
    DECLARE area_type_val VARCHAR(20);
    
    -- 获取区域类型
    SELECT type INTO area_type_val FROM areas WHERE id = area_id;
    
    -- 更新区域统计
    INSERT INTO area_daily_stats (area_id, stat_date, area_type, report_count, abnormal_count)
    VALUES (
        area_id,
        CURDATE(),
        area_type_val,
        increment,
        IF(is_abnormal, increment, 0)
    )
    ON DUPLICATE KEY UPDATE
        report_count = report_count + increment,
        abnormal_count = abnormal_count + IF(is_abnormal, increment, 0);
END */$$
DELIMITER ;

/*Table structure for table `area_stats` */

DROP TABLE IF EXISTS `area_stats`;

/*!50001 DROP VIEW IF EXISTS `area_stats` */;
/*!50001 DROP TABLE IF EXISTS `area_stats` */;

/*!50001 CREATE TABLE  `area_stats`(
 `area_type` enum('BRIDGE','TUNNEL','ROAD','COMMERCIAL','RESIDENTIAL','INDUSTRIAL','NATURAL','SPECIAL') ,
 `total_area` decimal(37,2) ,
 `report_count` bigint 
)*/;

/*View structure for view area_stats */

/*!50001 DROP TABLE IF EXISTS `area_stats` */;
/*!50001 DROP VIEW IF EXISTS `area_stats` */;

/*!50001 CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `area_stats` AS select `a`.`TYPE` AS `area_type`,sum((case `a`.`unit` when 'SQUARE_KM' then (`a`.`base_value` * 100) when 'HECTARE' then `a`.`base_value` else 0 end)) AS `total_area`,count(`r`.`id`) AS `report_count` from (`areas` `a` left join `inspection_reports` `r` on((`a`.`id` = `r`.`area_id`))) where (`a`.`TYPE` in ('COMMERCIAL','RESIDENTIAL','INDUSTRIAL','NATURAL','SPECIAL')) group by `a`.`TYPE` */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
