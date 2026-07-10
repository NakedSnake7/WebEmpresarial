
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
DROP TABLE IF EXISTS `admin_users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `admin_users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `email` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `enabled` bit(1) NOT NULL,
  `full_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `password` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `role` enum('STORE_ADMIN','STORE_STAFF','SUPER_ADMIN') COLLATE utf8mb4_unicode_ci NOT NULL,
  `store_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_admin_user_email` (`email`),
  KEY `FK1640cj1m4tkdq5gy8109mykcx` (`store_id`),
  CONSTRAINT `FK1640cj1m4tkdq5gy8109mykcx` FOREIGN KEY (`store_id`) REFERENCES `stores` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `auth_users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `auth_users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `email` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `enabled` bit(1) NOT NULL,
  `password` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `profile_completed` bit(1) NOT NULL,
  `role` enum('CLIENTE') COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `cliente_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK6jqfsuvys3lan090p4mk16a5t` (`email`),
  UNIQUE KEY `UKd8u04gcilo0bpwlknps9epjxy` (`cliente_id`),
  CONSTRAINT `FKs9ptsbu60d0c7ahtvh6oow6kb` FOREIGN KEY (`cliente_id`) REFERENCES `clientes` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `automation_execution_actions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `automation_execution_actions` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `action_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `duration_ms` bigint NOT NULL,
  `message` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `success` bit(1) NOT NULL,
  `execution_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK5mqnaoko2w82dypfi2tsfob19` (`execution_id`),
  CONSTRAINT `FK5mqnaoko2w82dypfi2tsfob19` FOREIGN KEY (`execution_id`) REFERENCES `automation_executions` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=48 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `automation_executions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `automation_executions` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `finished_at` datetime(6) DEFAULT NULL,
  `started_at` datetime(6) DEFAULT NULL,
  `success` bit(1) NOT NULL,
  `total_actions` int NOT NULL,
  `total_duration_ms` bigint NOT NULL,
  `trigger_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `correlation_id` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `execution_id` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `parent_execution_id` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `span_id` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `categorias`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `categorias` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `nombre` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `store_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKla20cc5wg4sn8asbjbhtyearx` (`store_id`),
  CONSTRAINT `FKla20cc5wg4sn8asbjbhtyearx` FOREIGN KEY (`store_id`) REFERENCES `stores` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `clientes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `clientes` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `default_address` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `email` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `full_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `phone` varchar(15) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `store_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_clientes_email_store` (`email`,`store_id`),
  KEY `FKdd9vk72krakikcq8dplwo28hx` (`store_id`),
  CONSTRAINT `FKdd9vk72krakikcq8dplwo28hx` FOREIGN KEY (`store_id`) REFERENCES `stores` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `coupons`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `coupons` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `active` bit(1) NOT NULL,
  `code` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `discount_percentage` double NOT NULL,
  `store_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK78ugyrs2qk27452e15n0b9eqn` (`code`,`store_id`),
  KEY `FK4nbkwp07tjuxyq0arduy2ax2i` (`store_id`),
  CONSTRAINT `FK4nbkwp07tjuxyq0arduy2ax2i` FOREIGN KEY (`store_id`) REFERENCES `stores` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `execution_spans`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `execution_spans` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `correlation_id` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `duration_ms` bigint NOT NULL,
  `execution_id` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `finished_at` datetime(6) DEFAULT NULL,
  `message` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `parent_execution_id` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `source` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `span_id` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `started_at` datetime(6) DEFAULT NULL,
  `success` bit(1) NOT NULL,
  `type` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `exception_message` text COLLATE utf8mb4_unicode_ci,
  `exception_type` text COLLATE utf8mb4_unicode_ci,
  `input` text COLLATE utf8mb4_unicode_ci,
  `metadata` text COLLATE utf8mb4_unicode_ci,
  `output` text COLLATE utf8mb4_unicode_ci,
  `payload` text COLLATE utf8mb4_unicode_ci,
  `stacktrace` text COLLATE utf8mb4_unicode_ci,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=111 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `feature_usage`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `feature_usage` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `context` varchar(120) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `feature` enum('ANALYTICS','API_ACCESS','AUTOMATIONS','CATEGORIES','CHECKOUT','COUPONS','CRM','CUSTOM_DOMAIN','EMAIL_MARKETING','INVENTORY','LEADS','MULTI_USER','ORDERS','PIPELINE','PRODUCTS','PROPOSALS','REVIEWS','STRIPE_CONNECT','TASKS','WHATSAPP_AUTOMATION','WHITE_LABEL_FULL') COLLATE utf8mb4_unicode_ci NOT NULL,
  `used_at` datetime(6) NOT NULL,
  `store_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKij78mnipjf3taia47nob16qsu` (`store_id`),
  CONSTRAINT `FKij78mnipjf3taia47nob16qsu` FOREIGN KEY (`store_id`) REFERENCES `stores` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=42 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `imagenes_productos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `imagenes_productos` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `image_url` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `orden` int DEFAULT NULL,
  `principal` bit(1) NOT NULL,
  `public_id` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `producto_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_imagen_producto` (`producto_id`),
  CONSTRAINT `FKit49va58jtu1j3j977paiayhu` FOREIGN KEY (`producto_id`) REFERENCES `productos` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `lead_activities`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `lead_activities` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `title` varchar(160) COLLATE utf8mb4_unicode_ci NOT NULL,
  `type` enum('CALL_LOGGED','COMMENT_ADDED','EMAIL_SENT','FOLLOW_UP_CREATED','LEAD_CREATED','NOTE_ADDED','PROPOSAL_ACCEPTED','PROPOSAL_CREATED','PROPOSAL_REJECTED','PROPOSAL_SENT','STATUS_CHANGED','TASK_COMPLETED','TASK_CREATED','WHATSAPP_SENT') COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_by_id` bigint DEFAULT NULL,
  `lead_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKoo8ej5478dxj6eotgqb4w7mkx` (`created_by_id`),
  KEY `FKle7c8q5nrqmbyt6ewdijgoqqw` (`lead_id`),
  CONSTRAINT `FKle7c8q5nrqmbyt6ewdijgoqqw` FOREIGN KEY (`lead_id`) REFERENCES `leads` (`id`),
  CONSTRAINT `FKoo8ej5478dxj6eotgqb4w7mkx` FOREIGN KEY (`created_by_id`) REFERENCES `admin_users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `lead_audit_logs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `lead_audit_logs` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `action` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `actor` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `field_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `lead_id` bigint DEFAULT NULL,
  `new_value` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `old_value` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `store_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `lead_budget_ranges`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `lead_budget_ranges` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `active` bit(1) NOT NULL,
  `code` varchar(60) COLLATE utf8mb4_unicode_ci NOT NULL,
  `estimated_amount` decimal(12,2) NOT NULL,
  `label` varchar(120) COLLATE utf8mb4_unicode_ci NOT NULL,
  `max_amount` decimal(12,2) DEFAULT NULL,
  `min_amount` decimal(12,2) DEFAULT NULL,
  `score_weight` int NOT NULL,
  `sort_order` int NOT NULL,
  `store_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_budget_range_store_code` (`store_id`,`code`),
  CONSTRAINT `FKmh9t1puyretn8f4knhmqyj4u4` FOREIGN KEY (`store_id`) REFERENCES `stores` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `leads`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `leads` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `close_probability` int DEFAULT NULL,
  `closed_at` datetime(6) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `empresa` varchar(120) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `exact_source` varchar(150) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `instagram` varchar(150) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `last_contact_at` datetime(6) DEFAULT NULL,
  `lost_at` datetime(6) DEFAULT NULL,
  `next_follow_up_at` datetime(6) DEFAULT NULL,
  `nombre` varchar(120) COLLATE utf8mb4_unicode_ci NOT NULL,
  `objetivo` text COLLATE utf8mb4_unicode_ci,
  `presupuesto` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `priority` enum('HIGH','LOW','MEDIUM','URGENT') COLLATE utf8mb4_unicode_ci NOT NULL,
  `projected_value` decimal(38,2) DEFAULT NULL,
  `proposal_amount` decimal(38,2) DEFAULT NULL,
  `score` int NOT NULL,
  `servicio` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `source` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` enum('CALL_BOOKED','CLOSED','CONTACTED','FOLLOW_UP','LOST','NEGOTIATION','NEW','PROPOSAL_SENT','QUALIFIED') COLLATE utf8mb4_unicode_ci NOT NULL,
  `temperature` enum('COLD','HOT','WARM') COLLATE utf8mb4_unicode_ci NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `whatsapp` varchar(40) COLLATE utf8mb4_unicode_ci NOT NULL,
  `store_id` bigint NOT NULL,
  `merged` bit(1) NOT NULL,
  `merged_at` datetime(6) DEFAULT NULL,
  `merged_into_lead_id` bigint DEFAULT NULL,
  `score_breakdown` text COLLATE utf8mb4_unicode_ci,
  `budget_label` varchar(80) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `estimated_budget` decimal(12,2) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKhn06tbmtxua0k1vqccslhjx3j` (`store_id`),
  CONSTRAINT `FKhn06tbmtxua0k1vqccslhjx3j` FOREIGN KEY (`store_id`) REFERENCES `stores` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `marcas`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `marcas` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `nombre` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `store_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKpdjiwjli3hx69i884dtxbyfic` (`nombre`,`store_id`),
  KEY `FK4p2c7inwn70r7kkt794dtfy9j` (`store_id`),
  CONSTRAINT `FK4p2c7inwn70r7kkt794dtfy9j` FOREIGN KEY (`store_id`) REFERENCES `stores` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `mensajes_pendientes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `mensajes_pendientes` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `creado_en` datetime(6) NOT NULL,
  `enviado` bit(1) NOT NULL,
  `mensaje` varchar(1000) COLLATE utf8mb4_unicode_ci NOT NULL,
  `telefono` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `store_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_mensaje_store_enviado` (`store_id`,`enviado`),
  CONSTRAINT `FKouvuu4wsou5assv9g6l2tb88y` FOREIGN KEY (`store_id`) REFERENCES `stores` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `order_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_items` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `price` decimal(12,2) NOT NULL,
  `product_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `quantity` int NOT NULL,
  `order_id` bigint NOT NULL,
  `producto_id` bigint NOT NULL,
  `variante_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_order_item_order` (`order_id`),
  KEY `idx_order_item_producto` (`producto_id`),
  KEY `idx_order_item_variante` (`variante_id`),
  CONSTRAINT `FK690rv4we9vsegsnebjvakwa19` FOREIGN KEY (`producto_id`) REFERENCES `productos` (`id`),
  CONSTRAINT `FKbioxgbv59vetrxe0ejfubep1w` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`),
  CONSTRAINT `FKih7tveqas0t7fg95mxk68ht1l` FOREIGN KEY (`variante_id`) REFERENCES `producto_variantes` (`id`),
  CONSTRAINT `order_items_chk_1` CHECK ((`price` >= 0)),
  CONSTRAINT `order_items_chk_2` CHECK ((`quantity` >= 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `orders`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `orders` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `address` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `carrier` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `claimed` bit(1) NOT NULL,
  `customer_email` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `customer_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `guest_token` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `is_guest` bit(1) NOT NULL,
  `order_date` datetime(6) NOT NULL,
  `order_expired_sent` bit(1) NOT NULL,
  `order_status` enum('CANCELLED','CREATED','DELIVERED','PAID_PENDING_STOCK','PROCESSED','SHIPPED') COLLATE utf8mb4_unicode_ci NOT NULL,
  `paid_at` datetime(6) DEFAULT NULL,
  `payment_confirmed_sent` bit(1) NOT NULL,
  `payment_intent_id` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `payment_method` enum('STRIPE','TRANSFER') COLLATE utf8mb4_unicode_ci NOT NULL,
  `payment_status` enum('EXPIRED','FAILED','PAID','PENDING') COLLATE utf8mb4_unicode_ci NOT NULL,
  `shipping_confirmation_sent` bit(1) NOT NULL,
  `stock_reduced` bit(1) NOT NULL,
  `stripe_session_id` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `total` double NOT NULL,
  `tracking_number` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `transfer_instructions_sent` bit(1) NOT NULL,
  `version` bigint NOT NULL,
  `user_id` bigint DEFAULT NULL,
  `store_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKqhochqo7y66p216aes9cpi4rl` (`guest_token`),
  UNIQUE KEY `UK659si9nhbvim0i6yu783rasii` (`payment_intent_id`),
  UNIQUE KEY `UKgkeltls6kwwpk41f577et02dj` (`stripe_session_id`),
  KEY `idx_orders_store` (`store_id`),
  KEY `idx_orders_store_date` (`store_id`,`order_date`),
  KEY `idx_orders_email` (`customer_email`),
  KEY `idx_orders_guest_token` (`guest_token`),
  KEY `idx_orders_email_status` (`customer_email`,`order_status`),
  KEY `idx_orders_email_user` (`customer_email`,`user_id`),
  KEY `FKjfx4md4ip6vfoag6pg39q7uqp` (`user_id`),
  CONSTRAINT `FKjfx4md4ip6vfoag6pg39q7uqp` FOREIGN KEY (`user_id`) REFERENCES `clientes` (`id`),
  CONSTRAINT `FKnqkwhwveegs6ne9ra90y1gq0e` FOREIGN KEY (`store_id`) REFERENCES `stores` (`id`),
  CONSTRAINT `orders_chk_1` CHECK ((`total` >= 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `platform_event_executions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `platform_event_executions` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `event_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `finished_at` datetime(6) DEFAULT NULL,
  `occurred_at` datetime(6) DEFAULT NULL,
  `source_module` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `success` bit(1) NOT NULL,
  `total_duration_ms` bigint NOT NULL,
  `total_listeners` int NOT NULL,
  `correlation_id` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `execution_id` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `parent_execution_id` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `span_id` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `platform_event_listener_executions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `platform_event_listener_executions` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `duration_ms` bigint NOT NULL,
  `listener_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `message` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `success` bit(1) NOT NULL,
  `execution_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKl85pewb1nn90oxse5o1uuhek2` (`execution_id`),
  CONSTRAINT `FKl85pewb1nn90oxse5o1uuhek2` FOREIGN KEY (`execution_id`) REFERENCES `platform_event_executions` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `producto_variantes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `producto_variantes` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `precio` decimal(38,2) DEFAULT NULL,
  `principal` bit(1) NOT NULL,
  `stock` int NOT NULL,
  `producto_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKa9mst006awyv85oy2n4wx2ix0` (`producto_id`),
  CONSTRAINT `FKa9mst006awyv85oy2n4wx2ix0` FOREIGN KEY (`producto_id`) REFERENCES `productos` (`id`),
  CONSTRAINT `producto_variantes_chk_1` CHECK ((`stock` >= 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `productos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `productos` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `description` text COLLATE utf8mb4_unicode_ci,
  `porcentaje_descuento` double NOT NULL,
  `price` decimal(10,2) NOT NULL,
  `product_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `sku` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `stock_simple` int DEFAULT NULL,
  `tiene_promocion` bit(1) NOT NULL,
  `version` bigint DEFAULT NULL,
  `visible_en_menu` bit(1) NOT NULL,
  `categoria_id` bigint NOT NULL,
  `marca_id` bigint DEFAULT NULL,
  `store_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK2fwq10nwymfv7fumctxt9vpgb` (`categoria_id`),
  KEY `FK2k6lj04qqala7kgd526xduxgn` (`marca_id`),
  KEY `FKcpyrbiijlbahaxq81efqnbqpo` (`store_id`),
  CONSTRAINT `FK2fwq10nwymfv7fumctxt9vpgb` FOREIGN KEY (`categoria_id`) REFERENCES `categorias` (`id`),
  CONSTRAINT `FK2k6lj04qqala7kgd526xduxgn` FOREIGN KEY (`marca_id`) REFERENCES `marcas` (`id`),
  CONSTRAINT `FKcpyrbiijlbahaxq81efqnbqpo` FOREIGN KEY (`store_id`) REFERENCES `stores` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `proposals`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `proposals` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `accepted_at` datetime(6) DEFAULT NULL,
  `amount` decimal(12,2) NOT NULL,
  `close_probability` int NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `rejected_at` datetime(6) DEFAULT NULL,
  `sent_at` datetime(6) DEFAULT NULL,
  `status` enum('ACCEPTED','DRAFT','EXPIRED','REJECTED','SENT','VIEWED') COLLATE utf8mb4_unicode_ci NOT NULL,
  `title` varchar(160) COLLATE utf8mb4_unicode_ci NOT NULL,
  `viewed_at` datetime(6) DEFAULT NULL,
  `lead_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKl14u4b0n58rlipd8ns9at8drs` (`lead_id`),
  CONSTRAINT `FKl14u4b0n58rlipd8ns9at8drs` FOREIGN KEY (`lead_id`) REFERENCES `leads` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `resenas`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `resenas` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `comentario` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `estrellas` int NOT NULL,
  `imagen_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `nombre` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `public_id` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `verificado` bit(1) NOT NULL,
  `store_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKf3bj6e1sn42mhuoibxsbuq8xp` (`store_id`),
  CONSTRAINT `FKf3bj6e1sn42mhuoibxsbuq8xp` FOREIGN KEY (`store_id`) REFERENCES `stores` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `saas_metric_snapshots`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `saas_metric_snapshots` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `active_stores` bigint DEFAULT NULL,
  `active_subscriptions` bigint DEFAULT NULL,
  `arr` decimal(12,2) DEFAULT NULL,
  `mrr` decimal(12,2) DEFAULT NULL,
  `snapshot_date` date DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `sales_tasks`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sales_tasks` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `completed_at` datetime(6) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `due_at` datetime(6) DEFAULT NULL,
  `priority` enum('HIGH','LOW','MEDIUM','URGENT') COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` enum('CANCELLED','COMPLETED','IN_PROGRESS','OVERDUE','PENDING') COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `title` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `assigned_to_id` bigint DEFAULT NULL,
  `lead_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK5cse8217tfo0002lubkvqhuhq` (`assigned_to_id`),
  KEY `FKdmjjex5gsi2loo44mnb5gj92k` (`lead_id`),
  CONSTRAINT `FK5cse8217tfo0002lubkvqhuhq` FOREIGN KEY (`assigned_to_id`) REFERENCES `admin_users` (`id`),
  CONSTRAINT `FKdmjjex5gsi2loo44mnb5gj92k` FOREIGN KEY (`lead_id`) REFERENCES `leads` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `store_settings`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `store_settings` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `accent_color` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `company_address` varchar(250) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `company_email` varchar(150) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `company_phone` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `company_website` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `contact_name` varchar(120) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `currency` varchar(3) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `favicon_url` varchar(300) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `font_family` varchar(80) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `hero_image_url` varchar(300) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `logo_url` varchar(300) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `primary_color` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `proposal_footer` text COLLATE utf8mb4_unicode_ci,
  `secondary_color` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `slogan` varchar(120) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `store_id` bigint NOT NULL,
  `about_text` text COLLATE utf8mb4_unicode_ci,
  `about_title` varchar(160) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `cta_text` text COLLATE utf8mb4_unicode_ci,
  `cta_title` varchar(160) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `facebook_url` varchar(300) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `footer_text` text COLLATE utf8mb4_unicode_ci,
  `hero_button_text` varchar(80) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `hero_button_url` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `hero_eyebrow` varchar(80) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `hero_subtitle` text COLLATE utf8mb4_unicode_ci,
  `hero_title` varchar(160) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `instagram_url` varchar(300) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `tiktok_url` varchar(300) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `whatsapp_message` varchar(300) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `custom_css` text COLLATE utf8mb4_unicode_ci,
  `custom_js` text COLLATE utf8mb4_unicode_ci,
  `google_analytics_id` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `hotjar_id` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `meta_pixel_id` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `tiktok_pixel_id` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKta56b9j5481fg49wlt7pheh0d` (`store_id`),
  CONSTRAINT `FKlo278pc1kfkqec5kj907ysyca` FOREIGN KEY (`store_id`) REFERENCES `stores` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `stores`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `stores` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `accent_color` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `activa` bit(1) NOT NULL,
  `company_address` varchar(250) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `company_email` varchar(150) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `company_phone` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `company_website` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `contact_name` varchar(120) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `currency` varchar(3) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `dominio` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `favicon_url` varchar(300) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `font_family` varchar(80) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `hero_image_url` varchar(300) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `logo_url` varchar(300) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `nombre` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `plan` enum('BASIC','PREMIUM','PRO') COLLATE utf8mb4_unicode_ci NOT NULL,
  `primary_color` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `proposal_footer` text COLLATE utf8mb4_unicode_ci,
  `secondary_color` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `slogan` varchar(120) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `stripe_connected` bit(1) NOT NULL,
  `stripe_connected_account_id` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `stripe_connected_at` datetime(6) DEFAULT NULL,
  `theme` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `theme_type` enum('BASIC','CUSTOM','PREMIUM','PRO') COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_store_dominio` (`dominio`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `subscriptions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `subscriptions` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `currency` varchar(3) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `current_period_end` datetime(6) DEFAULT NULL,
  `current_period_start` datetime(6) DEFAULT NULL,
  `ends_at` datetime(6) DEFAULT NULL,
  `monthly_amount` decimal(12,2) DEFAULT NULL,
  `next_billing_date` datetime(6) DEFAULT NULL,
  `plan` enum('BASIC','PREMIUM','PRO') COLLATE utf8mb4_unicode_ci NOT NULL,
  `starts_at` datetime(6) DEFAULT NULL,
  `status` enum('ACTIVE','CANCELLED','EXPIRED','PAST_DUE','TRIAL') COLLATE utf8mb4_unicode_ci NOT NULL,
  `stripe_customer_id` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `stripe_price_id` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `stripe_subscription_id` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `store_id` bigint NOT NULL,
  `billing_exempt` bit(1) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK1k28xf2n8kqg5el6js4b7mxe3` (`store_id`),
  CONSTRAINT `FKlqbc5fxora6h97c7l9xbuprnb` FOREIGN KEY (`store_id`) REFERENCES `stores` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `variante_atributos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `variante_atributos` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `nombre` varchar(80) COLLATE utf8mb4_unicode_ci NOT NULL,
  `valor` varchar(120) COLLATE utf8mb4_unicode_ci NOT NULL,
  `variante_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_atributo_variante` (`variante_id`),
  CONSTRAINT `FKnh0yeqtjbscg8rbu57uw96a35` FOREIGN KEY (`variante_id`) REFERENCES `producto_variantes` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `verification_tokens`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `verification_tokens` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `email` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `expiration` datetime(6) NOT NULL,
  `token` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `used` bit(1) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK6q9nsb665s9f8qajm3j07kd1e` (`token`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

