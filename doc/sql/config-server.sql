DROP DATABASE IF EXISTS `config-server`;
CREATE DATABASE IF NOT EXISTS `config-server`  DEFAULT CHARACTER SET utf8;
USE `config-server`;

DROP TABLE IF EXISTS `config_category`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `config_category`(
`id` INT UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT,
`cell` VARCHAR(16) NOT NULL,
`resource` VARCHAR(256) DEFAULT NULL,
`type` VARCHAR(16) DEFAULT NULL) ENGINE=InnoDB DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

DROP TABLE IF EXISTS `config_details`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `config_details`(
`config_id` INT UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT,
`category_id` INT UNSIGNED NOT NULL,
`content` BLOB  NULL,
`create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
`client_id` VARCHAR(32) NOT NULL,
CONSTRAINT cd_category_id_fk FOREIGN key (category_id) references config_category (id)) ENGINE=InnoDB DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

DROP TABLE IF EXISTS `config_change_log`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `config_change_log`(
`category_id` INT UNSIGNED NOT NULL PRIMARY KEY,
`md5` BLOB   VARCHAR(64) NOT NULL,
CONSTRAINT ccr_category_id_fk FOREIGN key (category_id) references config_category (id)) ENGINE=InnoDB DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

DROP TABLE IF EXISTS `config_heart_beat`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `config_heart_beat`(
`client_id` VARCHAR(64) NOT NULL PRIMARY KEY,
`last_modified_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP) ENGINE=InnoDB DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

