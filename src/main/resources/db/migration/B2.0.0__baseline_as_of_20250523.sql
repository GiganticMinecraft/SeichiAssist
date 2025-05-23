/*M!999999\- enable the sandbox mode */ 
-- MariaDB dump 10.19  Distrib 10.11.11-MariaDB, for debian-linux-gnu (x86_64)
--
-- Host: localhost    Database: seichiassist
-- ------------------------------------------------------
-- Server version	10.11.11-MariaDB-ubu2204

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `booked_achievement_status_change`
--

USE seichiassist;

DROP TABLE IF EXISTS `booked_achievement_status_change`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `booked_achievement_status_change` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `player_uuid` varchar(36) NOT NULL,
  `achievement_id` int(11) NOT NULL,
  `operation` varchar(10) NOT NULL,
  `completed_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `build_count_rate_limit`
--

DROP TABLE IF EXISTS `build_count_rate_limit`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `build_count_rate_limit` (
  `uuid` char(36) NOT NULL,
  `available_permission` decimal(17,5) unsigned NOT NULL,
  `record_date` datetime NOT NULL,
  PRIMARY KEY (`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `donate_purchase_history`
--

DROP TABLE IF EXISTS `donate_purchase_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `donate_purchase_history` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `uuid` char(36) NOT NULL,
  `get_points` int(11) NOT NULL,
  `timestamp` datetime NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`,`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `donate_usage_history`
--

DROP TABLE IF EXISTS `donate_usage_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `donate_usage_history` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `uuid` char(36) NOT NULL,
  `effect_name` varchar(20) NOT NULL,
  `use_points` int(11) NOT NULL,
  `timestamp` datetime NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`,`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `donatedata`
--

DROP TABLE IF EXISTS `donatedata`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `donatedata` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `playername` varchar(16) DEFAULT NULL,
  `playeruuid` char(36) DEFAULT NULL,
  `effectname` varchar(20) DEFAULT NULL,
  `getpoint` int(11) DEFAULT 0,
  `usepoint` int(11) DEFAULT 0,
  `date` datetime DEFAULT NULL,
  UNIQUE KEY `id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `fly_status_cache`
--

DROP TABLE IF EXISTS `fly_status_cache`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `fly_status_cache` (
  `player_uuid` varchar(36) NOT NULL,
  `remaining_fly_minutes` int(11) NOT NULL CHECK (`remaining_fly_minutes` >= -1),
  PRIMARY KEY (`player_uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `gacha_events`
--

DROP TABLE IF EXISTS `gacha_events`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `gacha_events` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `event_name` varchar(30) NOT NULL,
  `event_start_time` datetime DEFAULT NULL,
  `event_end_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`,`event_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `gachadata`
--

DROP TABLE IF EXISTS `gachadata`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `gachadata` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `probability` double DEFAULT 0,
  `itemstack` blob DEFAULT NULL,
  `event_id` int(11) DEFAULT NULL,
  UNIQUE KEY `id` (`id`),
  KEY `event_id` (`event_id`),
  CONSTRAINT `gachadata_ibfk_1` FOREIGN KEY (`event_id`) REFERENCES `gacha_events` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `grid_template`
--

DROP TABLE IF EXISTS `grid_template`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `grid_template` (
  `id` int(10) unsigned NOT NULL,
  `designer_uuid` char(36) NOT NULL,
  `ahead_length` int(11) NOT NULL DEFAULT 0,
  `behind_length` int(11) NOT NULL DEFAULT 0,
  `right_length` int(11) NOT NULL DEFAULT 0,
  `left_length` int(11) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`,`designer_uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `home`
--

DROP TABLE IF EXISTS `home`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `home` (
  `player_uuid` char(36) NOT NULL,
  `server_id` int(10) unsigned NOT NULL,
  `id` int(10) unsigned NOT NULL,
  `name` text DEFAULT NULL,
  `location_x` double DEFAULT NULL,
  `location_y` double DEFAULT NULL,
  `location_z` double DEFAULT NULL,
  `world_name` varchar(64) NOT NULL,
  `pitch` float DEFAULT 0,
  `yaw` float DEFAULT -90,
  PRIMARY KEY (`player_uuid`,`server_id`,`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `item_migration_in_server_world_levels`
--

DROP TABLE IF EXISTS `item_migration_in_server_world_levels`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `item_migration_in_server_world_levels` (
  `server_id` varchar(20) NOT NULL,
  `version_string` varchar(64) NOT NULL,
  `completed_at` datetime NOT NULL,
  PRIMARY KEY (`server_id`,`version_string`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `item_migration_on_database`
--

DROP TABLE IF EXISTS `item_migration_on_database`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `item_migration_on_database` (
  `version_string` varchar(64) NOT NULL,
  `completed_at` datetime NOT NULL,
  PRIMARY KEY (`version_string`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `mine_stack`
--

DROP TABLE IF EXISTS `mine_stack`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `mine_stack` (
  `player_uuid` char(36) NOT NULL,
  `object_name` varchar(128) NOT NULL,
  `amount` bigint(20) unsigned NOT NULL DEFAULT 0,
  PRIMARY KEY (`player_uuid`,`object_name`),
  KEY `index_mine_stack_on_object_name_amount` (`object_name`,`amount`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `mine_stack_item`
--

DROP TABLE IF EXISTS `mine_stack_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `mine_stack_item` (
  `object_name` varchar(128) NOT NULL,
  `base_item_stack` blob NOT NULL,
  `required_mine_stack_level` int(11) NOT NULL,
  `display_priority` int(11) NOT NULL,
  `category_id` int(11) NOT NULL,
  PRIMARY KEY (`object_name`),
  UNIQUE KEY `category_id` (`category_id`,`display_priority`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `player_break_preference`
--

DROP TABLE IF EXISTS `player_break_preference`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `player_break_preference` (
  `uuid` char(36) NOT NULL,
  `block_category` enum('Chest','MadeFromNetherQuartz') NOT NULL,
  `do_break` tinyint(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (`uuid`,`block_category`),
  KEY `index_player_break_preference_on_uuid` (`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `player_break_suppression_preference`
--

DROP TABLE IF EXISTS `player_break_suppression_preference`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `player_break_suppression_preference` (
  `uuid` char(36) NOT NULL,
  `do_break_suppression_due_to_mana` tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `player_in_server_item_migration`
--

DROP TABLE IF EXISTS `player_in_server_item_migration`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `player_in_server_item_migration` (
  `player_uuid` varchar(36) NOT NULL,
  `server_id` varchar(20) NOT NULL,
  `version_string` varchar(64) NOT NULL,
  `completed_at` datetime NOT NULL,
  PRIMARY KEY (`player_uuid`,`server_id`,`version_string`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `playerdata`
--

DROP TABLE IF EXISTS `playerdata`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `playerdata` (
  `name` varchar(16) DEFAULT NULL,
  `uuid` char(36) NOT NULL,
  `effectflag` tinyint(4) DEFAULT NULL,
  `minestackflag` tinyint(1) DEFAULT 1,
  `messageflag` tinyint(1) DEFAULT 0,
  `gachapoint` bigint(20) DEFAULT 0,
  `numofsorryforbug` int(11) DEFAULT 0,
  `inventory` blob DEFAULT NULL,
  `rgnum` int(11) DEFAULT 0,
  `totalbreaknum` bigint(20) DEFAULT 0,
  `lastquit` datetime DEFAULT NULL,
  `lastcheckdate` varchar(12) DEFAULT NULL,
  `ChainJoin` int(11) DEFAULT 0,
  `TotalJoin` int(11) DEFAULT 0,
  `LimitedLoginCount` int(11) DEFAULT 0,
  `displayTypeLv` tinyint(1) DEFAULT 1,
  `displayTitleNo` int(11) DEFAULT 0,
  `displayTitle1No` int(11) DEFAULT 0,
  `displayTitle2No` int(11) DEFAULT 0,
  `displayTitle3No` int(11) DEFAULT 0,
  `TitleFlags` text DEFAULT NULL,
  `giveachvNo` int(11) DEFAULT 0,
  `achvPointMAX` int(11) DEFAULT 0,
  `achvPointUSE` int(11) DEFAULT 0,
  `achvChangenum` int(11) DEFAULT 0,
  `starlevel` int(11) DEFAULT 0,
  `playtick` bigint(20) DEFAULT 0,
  `killlogflag` tinyint(1) DEFAULT 0,
  `worldguardlogflag` tinyint(1) DEFAULT 1,
  `multipleidbreakflag` tinyint(1) DEFAULT 0,
  `pvpflag` tinyint(1) DEFAULT 0,
  `loginflag` tinyint(1) DEFAULT 0,
  `mana` double DEFAULT 0,
  `expvisible` tinyint(1) DEFAULT 1,
  `totalexp` int(11) DEFAULT 0,
  `shareinv` mediumblob DEFAULT NULL,
  `everysound` tinyint(1) DEFAULT 1,
  `everymessage` tinyint(1) DEFAULT 1,
  `build_count` double NOT NULL DEFAULT 0,
  `anniversary` tinyint(1) DEFAULT 0,
  `hasVotingFairyMana` int(11) DEFAULT 0,
  `GBstage` int(11) DEFAULT 0,
  `GBexp` int(11) DEFAULT 0,
  `GBlevel` int(11) DEFAULT 0,
  `isGBStageUp` tinyint(1) DEFAULT 0,
  `hasNewYearSobaGive` tinyint(1) DEFAULT 0,
  `newYearBagAmount` int(11) DEFAULT 0,
  `hasChocoGave` tinyint(1) DEFAULT 0,
  `serialized_usage_mode` int(11) NOT NULL DEFAULT 0,
  `selected_effect` varchar(64) DEFAULT NULL,
  `selected_active_skill` varchar(64) DEFAULT NULL,
  `selected_assault_skill` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`uuid`),
  UNIQUE KEY `name` (`name`),
  UNIQUE KEY `uuid` (`uuid`),
  KEY `index_playerdata_on_lastquit` (`lastquit`),
  KEY `index_playerdata_playtick` (`playtick`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `present`
--

DROP TABLE IF EXISTS `present`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `present` (
  `present_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `itemstack` blob NOT NULL,
  PRIMARY KEY (`present_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `present_state`
--

DROP TABLE IF EXISTS `present_state`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `present_state` (
  `present_id` bigint(20) NOT NULL,
  `uuid` char(36) NOT NULL,
  `claimed` tinyint(1) NOT NULL,
  PRIMARY KEY (`present_id`,`uuid`),
  CONSTRAINT `present_id_in_present_state_must_exist_in_presents_table` FOREIGN KEY (`present_id`) REFERENCES `present` (`present_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `unknown_player_donation`
--

DROP TABLE IF EXISTS `unknown_player_donation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `unknown_player_donation` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `uuid` char(36) DEFAULT NULL,
  `effect_name` varchar(20) NOT NULL,
  `use_points` int(11) NOT NULL,
  `timestamp` datetime NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `unlocked_active_skill_effect`
--

DROP TABLE IF EXISTS `unlocked_active_skill_effect`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `unlocked_active_skill_effect` (
  `player_uuid` char(36) NOT NULL,
  `effect_name` varchar(64) NOT NULL,
  PRIMARY KEY (`player_uuid`,`effect_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `unlocked_seichi_skill`
--

DROP TABLE IF EXISTS `unlocked_seichi_skill`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `unlocked_seichi_skill` (
  `player_uuid` varchar(128) NOT NULL,
  `skill_name` varchar(64) NOT NULL,
  PRIMARY KEY (`player_uuid`,`skill_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `vote`
--

DROP TABLE IF EXISTS `vote`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `vote` (
  `uuid` char(36) NOT NULL,
  `vote_number` int(11) NOT NULL,
  `chain_vote_number` int(11) NOT NULL,
  `effect_point` int(11) NOT NULL,
  `given_effect_point` int(11) NOT NULL,
  `last_vote` datetime DEFAULT NULL,
  PRIMARY KEY (`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `vote_fairy`
--

DROP TABLE IF EXISTS `vote_fairy`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `vote_fairy` (
  `uuid` char(36) NOT NULL,
  `apple_open_state` int(11) NOT NULL DEFAULT 1,
  `fairy_summon_cost` int(11) NOT NULL DEFAULT 1,
  `is_fairy_using` tinyint(1) NOT NULL DEFAULT 0,
  `fairy_recovery_mana_value` int(11) NOT NULL DEFAULT 0,
  `fairy_end_time` datetime DEFAULT NULL,
  `given_apple_amount` bigint(20) NOT NULL DEFAULT 0,
  `is_play_fairy_speech_sound` tinyint(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-05-23 13:16:02
