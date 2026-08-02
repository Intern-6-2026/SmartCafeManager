-- MySQL dump 10.13  Distrib 8.0.46, for Win64 (x86_64)
--
-- Host: localhost    Database: smart_cafe_management
-- ------------------------------------------------------
-- Server version	8.0.46

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `account`
--

DROP TABLE IF EXISTS `account`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `account` (
  `account_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `created_by` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `email` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `password` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `password_changed_at` datetime(6) DEFAULT NULL,
  `reset_token` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `reset_token_expiry` datetime(6) DEFAULT NULL,
  `status` enum('ACTIVE','INACTIVE') COLLATE utf8mb4_unicode_ci NOT NULL,
  `username` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `role_id` bigint DEFAULT NULL,
  PRIMARY KEY (`account_id`),
  UNIQUE KEY `UKq0uja26qgu1atulenwup9rxyr` (`email`),
  UNIQUE KEY `UKgex1lmaqpg0ir5g1f5eftyaa1` (`username`),
  KEY `FKd4vb66o896tay3yy52oqxr9w0` (`role_id`),
  CONSTRAINT `FKd4vb66o896tay3yy52oqxr9w0` FOREIGN KEY (`role_id`) REFERENCES `role` (`role_id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `account`
--

LOCK TABLES `account` WRITE;
/*!40000 ALTER TABLE `account` DISABLE KEYS */;
INSERT INTO `account` VALUES (1,'2026-07-12 19:19:00.000000','system',NULL,'2026-07-30 00:15:23.155000','admin','codegymintern@gmail.com','$2a$10$gVt/t3VoFQ20aMJW6n/wnOPjRzwa50Hviu6FjFQLV47i5DBQB3SUe','2026-07-30 00:15:23.056000','7734a6e7-1122-472f-91e7-6ed0be6fc164','2026-07-22 22:49:15.290000','ACTIVE','admin',1),(2,'2026-07-12 19:19:00.000000','admin',NULL,NULL,NULL,'staff01@smartcafe.com','$2a$10$tDjHK77akloaamnCYJHKw.vA6l6zl9NhVEZyLff/UCeEbRu0pUmQu','2026-07-12 19:19:00.000000',NULL,NULL,'ACTIVE','staff01',2),(3,'2026-07-12 19:19:00.000000','admin',NULL,NULL,NULL,'staff02@smartcafe.com','$2a$10$tDjHK77akloaamnCYJHKw.vA6l6zl9NhVEZyLff/UCeEbRu0pUmQu','2026-07-12 19:19:00.000000',NULL,NULL,'ACTIVE','staff02',2),(4,'2026-07-12 19:19:00.000000','system',NULL,'2026-07-16 19:08:38.761000',NULL,'thanhbinhk645@gmail.com','$2a$10$ag3gq.PYrDN0lshEY8vj/.poKGLa3kkUMAJkkt4a1T8T96O0JrB9e','2026-07-16 19:08:38.760000',NULL,NULL,'ACTIVE','customer01',3),(5,'2026-07-12 19:19:00.000000','system',NULL,NULL,NULL,'customer02@gmail.com','$2a$10$tDjHK77akloaamnCYJHKw.vA6l6zl9NhVEZyLff/UCeEbRu0pUmQu','2026-07-12 19:19:00.000000',NULL,NULL,'ACTIVE','customer02',3);
/*!40000 ALTER TABLE `account` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cafe_table`
--

DROP TABLE IF EXISTS `cafe_table`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cafe_table` (
  `table_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `created_by` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `is_occupied` bit(1) NOT NULL,
  `physical_state` enum('BROKEN','GOOD','MAINTENANCE') COLLATE utf8mb4_unicode_ci NOT NULL,
  `service_status` enum('CALLING_WAITER','NORMAL','REQUESTING_BILL','WAITING_FOOD') COLLATE utf8mb4_unicode_ci NOT NULL,
  `table_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`table_id`),
  UNIQUE KEY `UK2ch8ea3u95v0mwkiaxufanrrr` (`table_name`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cafe_table`
--

LOCK TABLES `cafe_table` WRITE;
/*!40000 ALTER TABLE `cafe_table` DISABLE KEYS */;
INSERT INTO `cafe_table` VALUES (1,'2026-07-12 19:19:00.000000',NULL,NULL,NULL,NULL,_binary '\0','GOOD','NORMAL','Bàn 01'),(2,'2026-07-12 19:19:00.000000',NULL,NULL,NULL,NULL,_binary '\0','GOOD','NORMAL','Bàn 02'),(3,'2026-07-12 19:19:00.000000',NULL,NULL,NULL,NULL,_binary '','GOOD','WAITING_FOOD','Bàn 03'),(4,'2026-07-12 19:19:00.000000',NULL,NULL,NULL,NULL,_binary '\0','MAINTENANCE','NORMAL','Bàn 04');
/*!40000 ALTER TABLE `cafe_table` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `customer`
--

DROP TABLE IF EXISTS `customer`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `customer` (
  `customer_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `created_by` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `address` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `date_of_birth` datetime(6) DEFAULT NULL,
  `full_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `gender` enum('FEMALE','MALE') COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `image_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `loyalty_points` int DEFAULT NULL,
  `phone_number` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `account_id` bigint NOT NULL,
  PRIMARY KEY (`customer_id`),
  UNIQUE KEY `UKjwt2qo9oj3wd7ribjkymryp8s` (`account_id`),
  CONSTRAINT `FKn9x2k8svpxj3r328iy1rpur83` FOREIGN KEY (`account_id`) REFERENCES `account` (`account_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `customer`
--

LOCK TABLES `customer` WRITE;
/*!40000 ALTER TABLE `customer` DISABLE KEYS */;
INSERT INTO `customer` VALUES (1,'2026-07-12 19:19:00.000000','customer01',NULL,'2026-07-17 08:24:14.370000',NULL,'123 Nguyễn Văn Linh, Đà Nẵng','1995-05-14 07:00:00.000000','Phạm Khách Hàng 1 (sửa)','MALE','https://res.cloudinary.com/xqkvkmdf/image/upload/v1784251454/lbvflfgrwhwowrafhpy9.png',100,'0905123456',4),(2,'2026-07-12 19:19:00.000000','customer02',NULL,NULL,NULL,'456 Lê Duẩn, Đà Nẵng','1998-10-20 00:00:00.000000','Đặng Khách Hàng 2','FEMALE','https://res.cloudinary.com/xqkvkmdf/image/upload/v1/avatar_cus2.png',50,'0935987654',5);
/*!40000 ALTER TABLE `customer` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `databasechangelog`
--

DROP TABLE IF EXISTS `databasechangelog`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `databasechangelog` (
  `ID` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `AUTHOR` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `FILENAME` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `DATEEXECUTED` datetime NOT NULL,
  `ORDEREXECUTED` int NOT NULL,
  `EXECTYPE` varchar(10) COLLATE utf8mb4_unicode_ci NOT NULL,
  `MD5SUM` varchar(35) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `DESCRIPTION` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `COMMENTS` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `TAG` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `LIQUIBASE` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `CONTEXTS` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `LABELS` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `DEPLOYMENT_ID` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `databasechangelog`
--

LOCK TABLES `databasechangelog` WRITE;
/*!40000 ALTER TABLE `databasechangelog` DISABLE KEYS */;
/*!40000 ALTER TABLE `databasechangelog` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `databasechangeloglock`
--

DROP TABLE IF EXISTS `databasechangeloglock`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `databasechangeloglock` (
  `ID` int NOT NULL,
  `LOCKED` tinyint NOT NULL,
  `LOCKGRANTED` datetime DEFAULT NULL,
  `LOCKEDBY` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `databasechangeloglock`
--

LOCK TABLES `databasechangeloglock` WRITE;
/*!40000 ALTER TABLE `databasechangeloglock` DISABLE KEYS */;
INSERT INTO `databasechangeloglock` VALUES (1,0,NULL,NULL);
/*!40000 ALTER TABLE `databasechangeloglock` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `employee`
--

DROP TABLE IF EXISTS `employee`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `employee` (
  `employee_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `created_by` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `address` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `date_of_birth` datetime(6) DEFAULT NULL,
  `full_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `gender` enum('FEMALE','MALE') COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `image_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `phone_number` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `salary` decimal(38,2) NOT NULL,
  `account_id` bigint NOT NULL,
  PRIMARY KEY (`employee_id`),
  UNIQUE KEY `UKlsnx7na4u8ohrhoeag7un4wh3` (`account_id`),
  CONSTRAINT `FKcfg6ajo8oske94exynxpf7tf9` FOREIGN KEY (`account_id`) REFERENCES `account` (`account_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `employee`
--

LOCK TABLES `employee` WRITE;
/*!40000 ALTER TABLE `employee` DISABLE KEYS */;
INSERT INTO `employee` VALUES (1,'2026-07-12 19:19:00.000000','system',NULL,'2026-07-22 22:42:42.967000','admin','Đà Nẵng','1989-12-31 07:00:00.000000','Admin Codegym','MALE','https://res.cloudinary.com/xqkvkmdf/image/upload/v1/avatar_admin.png','0377584918',15000000.00,1),(2,'2026-07-12 19:19:00.000000','admin',NULL,NULL,NULL,'789 Trần Phú, Đà Nẵng','2000-02-14 00:00:00.000000','Trần Thị Nhân Viên 1','FEMALE','https://res.cloudinary.com/xqkvkmdf/image/upload/v1/avatar_staff1.png','0912111222',8000000.00,2),(3,'2026-07-12 19:19:00.000000','admin',NULL,NULL,NULL,'321 Bạch Đằng, Đà Nẵng','1999-12-12 00:00:00.000000','Lê Văn Nhân Viên 2','MALE','https://res.cloudinary.com/xqkvkmdf/image/upload/v1/avatar_staff2.png','0988333444',8000000.00,3);
/*!40000 ALTER TABLE `employee` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `feedback`
--

DROP TABLE IF EXISTS `feedback`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `feedback` (
  `feedback_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `created_by` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `content` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `email` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `image_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `rating` int NOT NULL,
  `sender_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `sent_at` datetime(6) NOT NULL,
  `customer_id` bigint DEFAULT NULL,
  `item_id` bigint DEFAULT NULL,
  PRIMARY KEY (`feedback_id`),
  KEY `FKpi2y2j7n01ypo49fone3knjry` (`customer_id`),
  KEY `FKmb01nh42pdh08swkfwgn9lfvi` (`item_id`),
  CONSTRAINT `FKmb01nh42pdh08swkfwgn9lfvi` FOREIGN KEY (`item_id`) REFERENCES `item` (`item_id`),
  CONSTRAINT `FKpi2y2j7n01ypo49fone3knjry` FOREIGN KEY (`customer_id`) REFERENCES `customer` (`customer_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `feedback`
--

LOCK TABLES `feedback` WRITE;
/*!40000 ALTER TABLE `feedback` DISABLE KEYS */;
INSERT INTO `feedback` VALUES (1,'2026-07-12 19:19:00.000000','customer01',NULL,NULL,NULL,'Trà đào rất ngon, phục vụ chu đáo!','customer01@gmail.com','https://res.cloudinary.com/xqkvkmdf/image/upload/v1/feedback_photo.png',5,'Phạm Khách Hàng 1','2026-07-12 19:19:00.000000',1,3);
/*!40000 ALTER TABLE `feedback` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `item`
--

DROP TABLE IF EXISTS `item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `item` (
  `item_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `created_by` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `description` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `image_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `is_available` bit(1) NOT NULL,
  `item_code` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `item_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `price` decimal(15,0) NOT NULL,
  `total_order_count` int NOT NULL,
  `category_id` bigint DEFAULT NULL,
  PRIMARY KEY (`item_id`),
  UNIQUE KEY `UKmm7bf7y05rx808c7mw73iiu8q` (`item_code`),
  UNIQUE KEY `UKku7fv295hhuscbb04easg4184` (`item_name`),
  KEY `FK7bhw51h2808m4nbmq62pn9tco` (`category_id`),
  CONSTRAINT `FK7bhw51h2808m4nbmq62pn9tco` FOREIGN KEY (`category_id`) REFERENCES `menu_category` (`category_id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `item`
--

LOCK TABLES `item` WRITE;
/*!40000 ALTER TABLE `item` DISABLE KEYS */;
INSERT INTO `item` VALUES (1,'2026-07-12 19:19:00.000000','admin',NULL,NULL,NULL,'Cà phê đen đá pha phin truyền thống, đậm vị nguyên bản.','https://res.cloudinary.com/xqkvkmdf/image/upload/v1/cf_den_da.png',_binary '','CF01','Cà phê Đen Đá',25000,150,1),(2,'2026-07-12 19:19:00.000000','admin',NULL,NULL,NULL,'Cà phê sữa đá pha phin thơm béo, đậm đà khó quên.','https://res.cloudinary.com/xqkvkmdf/image/upload/v1/cf_sua_da.png',_binary '','CF02','Cà phê Sữa Đá',29000,200,1),(3,'2026-07-12 19:19:00.000000','admin',NULL,NULL,NULL,'Trà đào cam sả thanh mát, giải nhiệt tức thì cho mùa hè.','https://res.cloudinary.com/xqkvkmdf/image/upload/v1/tra_dao.png',_binary '','TR01','Trà Đào Cam Sả',45000,120,2),(4,'2026-07-12 19:19:00.000000','admin',NULL,NULL,NULL,'Trà vải nhiệt đới ngọt thanh, kết hợp cùng trái vải ngâm giòn.','https://res.cloudinary.com/xqkvkmdf/image/upload/v1/tra_vai.png',_binary '','TR02','Trà Vải Nhiệt Đới',45000,90,2),(5,'2026-07-12 19:19:00.000000','admin',NULL,NULL,NULL,'Bánh Tiramisu Ý mềm mịn, lớp kem béo ngậy vị cà phê cacao.','https://res.cloudinary.com/xqkvkmdf/image/upload/v1/tiramisu.png',_binary '','BN01','Tiramisu',35000,40,3);
/*!40000 ALTER TABLE `item` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `menu_category`
--

DROP TABLE IF EXISTS `menu_category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `menu_category` (
  `category_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `created_by` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `category_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`category_id`),
  UNIQUE KEY `UKiut00xemb7licbf1aglkkwpj4` (`category_name`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `menu_category`
--

LOCK TABLES `menu_category` WRITE;
/*!40000 ALTER TABLE `menu_category` DISABLE KEYS */;
INSERT INTO `menu_category` VALUES (1,'2026-07-12 19:19:00.000000',NULL,NULL,NULL,NULL,'Cà phê truyền thống'),(2,'2026-07-12 19:19:00.000000',NULL,NULL,NULL,NULL,'Trà trái cây'),(3,'2026-07-12 19:19:00.000000',NULL,NULL,NULL,NULL,'Bánh ngọt');
/*!40000 ALTER TABLE `menu_category` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `news`
--

DROP TABLE IF EXISTS `news`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `news` (
  `news_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `created_by` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `content` text COLLATE utf8mb4_unicode_ci,
  `image_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `summary` text COLLATE utf8mb4_unicode_ci,
  `title` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `account_id` bigint DEFAULT NULL,
  `status` enum('PENDING','PUBLISHED','REJECTED') COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`news_id`),
  KEY `FK9jgaemfexdg06ffxt30n6acwq` (`account_id`),
  CONSTRAINT `FK9jgaemfexdg06ffxt30n6acwq` FOREIGN KEY (`account_id`) REFERENCES `account` (`account_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `news`
--

LOCK TABLES `news` WRITE;
/*!40000 ALTER TABLE `news` DISABLE KEYS */;
INSERT INTO `news` VALUES (1,'2026-07-12 19:19:00.000000','admin',NULL,NULL,NULL,'Nội dung chi tiết chương trình khai trương dành cho khách hàng...','https://res.cloudinary.com/xqkvkmdf/image/upload/v1/news_grand_opening.png','Tuần lễ khai trương giảm giá 20%','Khai trương hồng phát',1,'PUBLISHED'),(2,'2026-07-12 19:19:00.000000','staff01',NULL,NULL,NULL,'Cùng thưởng thức menu đồ uống giải nhiệt mùa hè của Smart Cafe...','https://res.cloudinary.com/xqkvkmdf/image/upload/v1/news_summer.png','Thử ngay Trà Vải Nhiệt Đới','Ra mắt thức uống mới',2,'PENDING'),(3,'2026-07-12 12:32:29.972000','admin','2026-07-12 12:39:21.502000','2026-07-12 12:39:21.502000','admin','Không có gì hết ở nội dung','https://res.cloudinary.com/xqkvkmdf/image/upload/v1783859551/htyhtuz9cz7bc9vrqesx.png','Không có gì hết ở tóm tắt','Mẫu thử',1,'PENDING'),(4,'2026-07-15 13:23:05.974000','admin',NULL,'2026-07-28 09:46:16.140000','admin','aaaa','https://res.cloudinary.com/xqkvkmdf/image/upload/v1784121790/zn7zqisdad984ha8qs0i.png','aaaa','aaaa',1,'PUBLISHED');
/*!40000 ALTER TABLE `news` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order_detail`
--

DROP TABLE IF EXISTS `order_detail`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_detail` (
  `order_detail_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `created_by` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `note` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `quantity` int NOT NULL,
  `status` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `unit_price` decimal(15,0) NOT NULL,
  `item_id` bigint DEFAULT NULL,
  `order_id` bigint DEFAULT NULL,
  PRIMARY KEY (`order_detail_id`),
  KEY `FK4dtqbi7ilse9x730y087wagm2` (`item_id`),
  KEY `FKd7u0gc7kgc44yf8juueqhu8ey` (`order_id`),
  CONSTRAINT `FK4dtqbi7ilse9x730y087wagm2` FOREIGN KEY (`item_id`) REFERENCES `item` (`item_id`),
  CONSTRAINT `FKd7u0gc7kgc44yf8juueqhu8ey` FOREIGN KEY (`order_id`) REFERENCES `table_order` (`order_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_detail`
--

LOCK TABLES `order_detail` WRITE;
/*!40000 ALTER TABLE `order_detail` DISABLE KEYS */;
INSERT INTO `order_detail` VALUES (1,'2026-07-12 19:19:00.000000','customer01',NULL,NULL,NULL,'Ít đá, không đường',1,'SERVED',25000,1,1),(2,'2026-07-12 19:19:00.000000','customer01',NULL,NULL,NULL,NULL,1,'SERVED',45000,3,1),(3,'2026-07-12 19:19:00.000000','customer02',NULL,NULL,NULL,'Nhiều trân châu',2,'PENDING',45000,4,2);
/*!40000 ALTER TABLE `order_detail` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `role`
--

DROP TABLE IF EXISTS `role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `role` (
  `role_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `created_by` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `role_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`role_id`),
  UNIQUE KEY `UKiubw515ff0ugtm28p8g3myt0h` (`role_name`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `role`
--

LOCK TABLES `role` WRITE;
/*!40000 ALTER TABLE `role` DISABLE KEYS */;
INSERT INTO `role` VALUES (1,NULL,NULL,NULL,NULL,NULL,'ADMIN'),(2,NULL,NULL,NULL,NULL,NULL,'STAFF'),(3,NULL,NULL,NULL,NULL,NULL,'USER');
/*!40000 ALTER TABLE `role` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `table_order`
--

DROP TABLE IF EXISTS `table_order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `table_order` (
  `order_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `created_by` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `close_at` datetime(6) DEFAULT NULL,
  `open_at` datetime(6) NOT NULL,
  `paid_at` datetime(6) DEFAULT NULL,
  `payment_method` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` enum('CANCELLED','OPEN','PAID') COLLATE utf8mb4_unicode_ci NOT NULL,
  `total_amount` decimal(15,0) NOT NULL,
  `customer_id` bigint DEFAULT NULL,
  `employee_id` bigint DEFAULT NULL,
  `table_id` bigint DEFAULT NULL,
  PRIMARY KEY (`order_id`),
  KEY `FK5tx3lr2wfe6vggtq35atnviiv` (`customer_id`),
  KEY `FKkxxygsd18l03f7lqtjge09clo` (`employee_id`),
  KEY `FKotmcbqjiyur4250cdlcn325f3` (`table_id`),
  CONSTRAINT `FK5tx3lr2wfe6vggtq35atnviiv` FOREIGN KEY (`customer_id`) REFERENCES `customer` (`customer_id`),
  CONSTRAINT `FKkxxygsd18l03f7lqtjge09clo` FOREIGN KEY (`employee_id`) REFERENCES `employee` (`employee_id`),
  CONSTRAINT `FKotmcbqjiyur4250cdlcn325f3` FOREIGN KEY (`table_id`) REFERENCES `cafe_table` (`table_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `table_order`
--

LOCK TABLES `table_order` WRITE;
/*!40000 ALTER TABLE `table_order` DISABLE KEYS */;
INSERT INTO `table_order` VALUES (1,'2026-07-12 17:19:00.000000','customer01',NULL,'2026-07-12 18:30:00.000000','staff01','2026-07-12 18:30:00.000000','2026-07-12 17:19:00.000000','2026-07-12 18:30:00.000000','CASH','PAID',70000,1,2,1),(2,'2026-07-12 19:19:00.000000','customer02',NULL,NULL,NULL,NULL,'2026-07-12 19:19:00.000000',NULL,NULL,'OPEN',90000,2,3,3);
/*!40000 ALTER TABLE `table_order` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-07-31  9:55:31
