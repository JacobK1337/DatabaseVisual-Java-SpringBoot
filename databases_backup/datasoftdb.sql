-- MySQL dump 10.13  Distrib 8.0.26, for Win64 (x86_64)
--
-- Host: localhost    Database: datasoftdb
-- ------------------------------------------------------
-- Server version	8.0.26

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
-- Table structure for table `data_api`
--

DROP TABLE IF EXISTS `data_api`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `data_api` (
  `data_api_id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `table_id` int NOT NULL,
  `primary_key_name` varchar(30) NOT NULL,
  `data_api_json` json NOT NULL,
  PRIMARY KEY (`data_api_id`),
  KEY `user_id_fk` (`user_id`),
  KEY `table_id_fk` (`table_id`),
  CONSTRAINT `table_id_fk` FOREIGN KEY (`table_id`) REFERENCES `database_tables` (`table_id`) ON DELETE CASCADE,
  CONSTRAINT `user_id_fk` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `data_api`
--

LOCK TABLES `data_api` WRITE;
/*!40000 ALTER TABLE `data_api` DISABLE KEYS */;
/*!40000 ALTER TABLE `data_api` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `database_tables`
--

DROP TABLE IF EXISTS `database_tables`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `database_tables` (
  `table_id` int NOT NULL AUTO_INCREMENT,
  `database_id` int NOT NULL,
  PRIMARY KEY (`table_id`),
  KEY `database_tables_ibfk_1` (`database_id`),
  CONSTRAINT `database_tables_ibfk_1` FOREIGN KEY (`database_id`) REFERENCES `user_databases` (`database_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=52 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `database_tables`
--

LOCK TABLES `database_tables` WRITE;
/*!40000 ALTER TABLE `database_tables` DISABLE KEYS */;
/*!40000 ALTER TABLE `database_tables` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `field_constraints`
--

DROP TABLE IF EXISTS `field_constraints`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `field_constraints` (
  `constraint_id` int NOT NULL AUTO_INCREMENT,
  `field_id` int NOT NULL,
  `database_id` int NOT NULL,
  `constraint_info_json` json NOT NULL,
  PRIMARY KEY (`constraint_id`),
  KEY `field_id` (`field_id`),
  KEY `database_id` (`database_id`),
  CONSTRAINT `field_constraints_ibfk_1` FOREIGN KEY (`field_id`) REFERENCES `table_fields` (`field_id`) ON DELETE CASCADE,
  CONSTRAINT `field_constraints_ibfk_2` FOREIGN KEY (`database_id`) REFERENCES `user_databases` (`database_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=216 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `field_constraints`
--

LOCK TABLES `field_constraints` WRITE;
/*!40000 ALTER TABLE `field_constraints` DISABLE KEYS */;
/*!40000 ALTER TABLE `field_constraints` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `table_details`
--

DROP TABLE IF EXISTS `table_details`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `table_details` (
  `table_id` int NOT NULL,
  `table_name` varchar(30) NOT NULL DEFAULT 'noname',
  `page_x` int NOT NULL,
  `page_y` int NOT NULL,
  `color` varchar(30) NOT NULL DEFAULT 'darkgrey',
  PRIMARY KEY (`table_id`),
  CONSTRAINT `table_details_ibfk_1` FOREIGN KEY (`table_id`) REFERENCES `database_tables` (`table_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `table_details`
--

LOCK TABLES `table_details` WRITE;
/*!40000 ALTER TABLE `table_details` DISABLE KEYS */;
/*!40000 ALTER TABLE `table_details` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `table_fields`
--

DROP TABLE IF EXISTS `table_fields`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `table_fields` (
  `field_id` int NOT NULL AUTO_INCREMENT,
  `table_id` int NOT NULL,
  `field_name` varchar(30) NOT NULL,
  `field_type` enum('int','varchar','date') NOT NULL,
  `not_null` tinyint(1) NOT NULL DEFAULT '0',
  `unique_value` tinyint(1) DEFAULT '0',
  `default_value` varchar(50) DEFAULT 'null',
  `is_primary_key` tinyint(1) DEFAULT '0',
  `is_foreign_key` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`field_id`),
  KEY `table_fields_ibfk_1` (`table_id`),
  CONSTRAINT `table_fields_ibfk_1` FOREIGN KEY (`table_id`) REFERENCES `database_tables` (`table_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=134 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `table_fields`
--

LOCK TABLES `table_fields` WRITE;
/*!40000 ALTER TABLE `table_fields` DISABLE KEYS */;
/*!40000 ALTER TABLE `table_fields` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tables_data`
--

DROP TABLE IF EXISTS `tables_data`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tables_data` (
  `data_id` int NOT NULL AUTO_INCREMENT,
  `table_id` int NOT NULL,
  `field_json_value` json DEFAULT NULL,
  PRIMARY KEY (`data_id`),
  KEY `table_id` (`table_id`),
  CONSTRAINT `tables_data_ibfk_1` FOREIGN KEY (`table_id`) REFERENCES `database_tables` (`table_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=338 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tables_data`
--

LOCK TABLES `tables_data` WRITE;
/*!40000 ALTER TABLE `tables_data` DISABLE KEYS */;
/*!40000 ALTER TABLE `tables_data` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_databases`
--

DROP TABLE IF EXISTS `user_databases`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_databases` (
  `database_id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `database_name` varchar(30) NOT NULL,
  PRIMARY KEY (`database_id`),
  KEY `user_databases_ibfk_1` (`user_id`),
  CONSTRAINT `user_databases_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_databases`
--

LOCK TABLES `user_databases` WRITE;
/*!40000 ALTER TABLE `user_databases` DISABLE KEYS */;
INSERT INTO `user_databases` VALUES (1,1,'TestDB'),(3,1,'NewDatabaseCreated'),(4,1,'abc');
/*!40000 ALTER TABLE `user_databases` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `user_id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(20) NOT NULL,
  `password` varchar(60) NOT NULL,
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'tomasz','$2a$10$iqJfC4/VXlFn1x74Nj6uMOJqYlepSi.kaaVsNjcy4BZ0clprJOFGK');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2022-04-03 11:28:46
