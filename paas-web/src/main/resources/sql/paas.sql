/*
Navicat MySQL Data Transfer

Source Server         : 216数据库
Source Server Version : 50628
Source Host           : 192.168.1.216:3306
Source Database       : paas2

Target Server Type    : MYSQL
Target Server Version : 50628
File Encoding         : 65001

Date: 2017-07-17 16:01:48
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for access_log
-- ----------------------------
DROP TABLE IF EXISTS `access_log`;
CREATE TABLE `access_log` (
  `id` varchar(64) NOT NULL,
  `object` varchar(128) DEFAULT NULL COMMENT '操作对象',
  `action` varchar(128) DEFAULT NULL COMMENT '行为',
  `result` varchar(255) DEFAULT NULL COMMENT '结果',
  `user_id` bigint(20) DEFAULT NULL COMMENT '操作者id',
  `response_ip` varchar(16) DEFAULT NULL COMMENT '操作者名称',
  `request_ip` varchar(32) DEFAULT NULL COMMENT 'ip',
  `gmt_create` datetime DEFAULT NULL COMMENT '创建时间',
  `detail` text,
  `module` varchar(32) DEFAULT NULL,
  `tenant_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of access_log
-- ----------------------------
INSERT INTO `access_log` VALUES ('131c56a0600a4ec2a4d931ba52366e7f', 'environment', '/environment/list', '请求处理正常！', '1', '127.0.0.1', '192.168.0.139', '2017-07-14 10:07:41', '{\"endpoint\":\"127.0.0.1\",\"apiKey\":\"1214546782\",\"sign\":\"38cd33e447f6ffe0b5aeeef5cfb40e9570cc4d30\",\"simple\":\"false\",\"page\":\"1\",\"rows\":\"10\",\"timestamp\":\"1499998061106\"}', 'BSM.API', '0');
INSERT INTO `access_log` VALUES ('186c9fc4cf44407ea480c84325359dc3', 'host', '/host/list', '请求处理正常！', '1', '127.0.0.1', '192.168.0.139', '2017-07-14 10:24:24', '{\"endpoint\":\"127.0.0.1\",\"apiKey\":\"1214546782\",\"sign\":\"-54c1924616204a5a05fd6654272bc2e4634cc73e\",\"simple\":\"false\",\"page\":\"1\",\"rows\":\"10\",\"params\":\"[]\",\"timestamp\":\"1499999064390\"}', 'BSM.API', '0');
INSERT INTO `access_log` VALUES ('7ab2a999a76d4e6dbdc6acefb040ef08', 'host', '/host/list', '请求处理正常！', '1', '127.0.0.1', '192.168.0.139', '2017-07-14 10:07:42', '{\"endpoint\":\"127.0.0.1\",\"apiKey\":\"1214546782\",\"sign\":\"f85b5dd9b37d30e85d614d1f578aa26b0595d46\",\"simple\":\"false\",\"page\":\"1\",\"rows\":\"10\",\"params\":\"[]\",\"timestamp\":\"1499998062280\"}', 'BSM.API', '0');

-- ----------------------------
-- Table structure for application
-- ----------------------------
DROP TABLE IF EXISTS `application`;
CREATE TABLE `application` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(128) NOT NULL COMMENT '应用名称',
  `status` varchar(16) NOT NULL COMMENT '状态（0 未部署，1 部署中，2 运行，3 异常）',
  `namespace` varchar(64) DEFAULT NULL COMMENT '应用命名空间',
  `env_id` bigint(20) DEFAULT NULL COMMENT '环境id',
  `is_deleted` tinyint(1) NOT NULL COMMENT '是否删除 （0 未删除， 1 删除）',
  `remark` varchar(256) DEFAULT NULL COMMENT '描述',
  `props` varchar(1024) DEFAULT NULL COMMENT '其他属性',
  `gmt_create` datetime NOT NULL COMMENT '创建时间',
  `gmt_modify` datetime DEFAULT NULL COMMENT '修改时间',
  `creater_id` bigint(20) NOT NULL COMMENT '创建者ID',
  `mender_id` bigint(20) DEFAULT NULL COMMENT '修改者ID',
  `owner_id` bigint(20) DEFAULT NULL COMMENT '所有者ID',
  `address` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL COMMENT '应用访问路径',
  `tenant_id` int(11) DEFAULT NULL COMMENT '租户ID',
  `dept_id` bigint(20) DEFAULT NULL COMMENT '所属部门',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of application
-- ----------------------------

-- ----------------------------
-- Table structure for application_image_info
-- ----------------------------
DROP TABLE IF EXISTS `application_image_info`;
CREATE TABLE `application_image_info` (
  `id` bigint(11) NOT NULL AUTO_INCREMENT,
  `application_id` bigint(11) NOT NULL COMMENT '应用id',
  `image_id` bigint(11) NOT NULL COMMENT '软件id',
  `use_count` int(11) NOT NULL COMMENT '依赖总数',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of application_image_info
-- ----------------------------

-- ----------------------------
-- Table structure for application_layout_info
-- ----------------------------
DROP TABLE IF EXISTS `application_layout_info`;
CREATE TABLE `application_layout_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `application_id` bigint(20) DEFAULT NULL,
  `layout_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of application_layout_info
-- ----------------------------

-- ----------------------------
-- Table structure for application_service_loadbalance_info
-- ----------------------------
DROP TABLE IF EXISTS `application_service_loadbalance_info`;
CREATE TABLE `application_service_loadbalance_info` (
  `id` bigint(20) NOT NULL,
  `application_id` bigint(20) NOT NULL,
  `service_id` bigint(20) NOT NULL,
  `loadbalance_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of application_service_loadbalance_info
-- ----------------------------

-- ----------------------------
-- Table structure for application_store
-- ----------------------------
DROP TABLE IF EXISTS `application_store`;
CREATE TABLE `application_store` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id唯一标识',
  `name` varchar(255) NOT NULL COMMENT '组件的名称',
  `version` varchar(255) NOT NULL COMMENT '应用组件的版本',
  `icon` varchar(255) DEFAULT NULL COMMENT '组件图标',
  `template` varchar(255) NOT NULL COMMENT '组件模板名称',
  `type` varchar(255) DEFAULT NULL COMMENT '组件类型',
  `deploy_number` bigint(20) DEFAULT NULL COMMENT '部署次数',
  `remark` varchar(255) DEFAULT NULL COMMENT '组件的描述信息',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of application_store
-- ----------------------------
INSERT INTO `application_store` VALUES ('1', 'gitlab', '8.8', 'gitlab.png', 'gitlab.yaml', '开发工具', null, 'GitLab 是一个用于仓库管理系统的开源项目，使用Git作为代码管理工具，并在此基础上搭建起来的web服务');
INSERT INTO `application_store` VALUES ('2', 'snoarqube', '5.6', 'sonar.png', 'sonar.yaml', '开发工具', null, 'Sonar（代码质量管理平台）是一个开源平台，用于管理Java源代码的质量');
INSERT INTO `application_store` VALUES ('3', 'jenkins', 'latest', 'jenkins.png', 'jenkins.yaml', '开发工具', null, 'Jenkins是一个开源软件项目，旨在提供一个开放易用的软件平台，使软件的持续集成变成可能。');
INSERT INTO `application_store` VALUES ('4', 'tomcat', '8.5', 'tomcat.png', 'tomcat.yaml', '应用服务器', null, 'Tomcat 服务器是一个免费的开放源代码的Web 应用服务器，属于轻量级应用服务器。');
INSERT INTO `application_store` VALUES ('5', 'jboss', 'latest', 'jboss.png', 'jboss.yaml', '应用服务器', null, '是一个基于J2EE的开放源代码的应用服务器。');
INSERT INTO `application_store` VALUES ('7', 'zookeeper', 'latest', 'zookeeper.png', 'zookeeper.yaml', '应用中间件', null, 'ZooKeeper是一个分布式的，开放源码的分布式应用程序协调服务。');
INSERT INTO `application_store` VALUES ('8', 'rabbitmq', 'latest', 'rabbitmq.png', 'rabbitmq.yaml', '应用中间件', null, 'RabbitMQ是一个在AMQP基础上完成的，可复用的企业消息系统。他遵循Mozilla Public License开源协议。');
INSERT INTO `application_store` VALUES ('9', 'redis', 'latest', 'redis.png', 'redis.yaml', '应用中间件', null, 'Redis是一个开源的使用ANSI C语言编写、支持网络、可基于内存亦可持久化的日志型、Key-Value数据库。');
INSERT INTO `application_store` VALUES ('10', 'nginx', 'latest', 'nginx.png', 'nginx.yaml', '负载均衡器', null, 'Nginx是一个高性能的HTTP和反向代理服务器，也是一个IMAP/POP3/SMTP服务器。');
INSERT INTO `application_store` VALUES ('11', 'haproxy', 'latest', 'haproxy.png', 'haproxy.yaml', '负载均衡器', null, 'HAProxy是一个使用C语言编写的自由及开放源代码软件[1]，其提供高可用性、负载均衡，以及基于TCP和HTTP的应用程序代理。');
INSERT INTO `application_store` VALUES ('12', 'mysql', '5.6', 'mysql.png', 'mysql.yaml', '数据库', null, 'MySQL是一个关系型数据库管理系统');
INSERT INTO `application_store` VALUES ('13', 'postgres', 'latest', 'postgres.png', 'postgres.yaml', '数据库', null, 'PostgreSQL 是一个自由的对象-关系数据库服务器(数据库管理系统)');
INSERT INTO `application_store` VALUES ('14', 'influxdb', 'latest', 'influxdb.png', 'influxdb.yaml', '数据库', null, 'InfluxDB 是一个开源分布式时序、事件和指标数据库。');

-- ----------------------------
-- Table structure for authority
-- ----------------------------
DROP TABLE IF EXISTS `authority`;
CREATE TABLE `authority` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `name` varchar(64) NOT NULL COMMENT '权限名称',
  `remark` varchar(256) DEFAULT NULL COMMENT '描述',
  `parent_id` bigint(20) DEFAULT NULL COMMENT '上级节点',
  `gmt_create` datetime NOT NULL COMMENT '创建时间',
  `gmt_modify` datetime DEFAULT NULL COMMENT '修改时间',
  `creater_id` bigint(20) NOT NULL COMMENT '创建者ID',
  `mender_id` bigint(20) DEFAULT NULL COMMENT '修改者ID',
  `status` varchar(16) DEFAULT NULL COMMENT '状态',
  `is_deleted` tinyint(1) NOT NULL COMMENT '是否删除',
  `owner_id` bigint(20) DEFAULT NULL,
  `props` varchar(1024) DEFAULT NULL COMMENT '编码',
  `action_url` varchar(64) DEFAULT NULL COMMENT '权限路径',
  `icon` varchar(64) DEFAULT NULL,
  `category` varchar(20) DEFAULT NULL,
  `priority` int(4) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=155 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of authority
-- ----------------------------
INSERT INTO `authority` VALUES ('8', '数据纵览', '数据纵览', '0', '2016-09-07 15:05:57', '2017-07-08 10:08:37', '1', '6', 'NORMAL', '0', null, '无', 'paas.dashboard.dashboard', 'fa fa-tachometer icon', 'menu', '1');
INSERT INTO `authority` VALUES ('9', '环境资源', '环境资源', '0', '2016-09-07 20:54:08', '2017-06-13 14:07:21', '1', '1', 'NORMAL', '0', null, '无', 'paas.environment', 'fa fa-sitemap icon', 'menu', '2');
INSERT INTO `authority` VALUES ('13', '主机管理', '主机资源', '9', '2016-09-07 20:56:39', '2017-06-13 14:19:21', '1', '1', 'NORMAL', '0', null, 'app.assets.resource', 'paas.environment.host', null, 'menu', '2');
INSERT INTO `authority` VALUES ('40', '用户中心', '用户中心', '0', '2016-09-08 09:59:35', '2017-06-16 10:46:45', '1', '1', 'NORMAL', '0', null, '无', 'paas.userCenter', 'fa fa-user icon', 'menu', '8');
INSERT INTO `authority` VALUES ('42', '用户管理', '用户管理', '40', '2016-09-08 10:00:14', '2016-10-14 17:54:45', '1', '20', 'NORMAL', '0', null, '无', 'paas.userCenter.user', null, 'menu', '2');
INSERT INTO `authority` VALUES ('43', '角色管理', '角色管理', '40', '2016-09-08 10:00:45', '2016-10-14 17:55:00', '1', '20', 'NORMAL', '0', null, '无', 'paas.userCenter.role', null, 'menu', '3');
INSERT INTO `authority` VALUES ('44', '权限管理', '权限管理', '40', '2016-09-08 10:01:03', '2016-10-14 17:55:14', '1', '20', 'NORMAL', '0', null, '无', 'paas.userCenter.permission', null, 'menu', '4');
INSERT INTO `authority` VALUES ('69', '组织机构', '组织机构', '40', '2016-10-14 17:54:28', '2016-10-14 17:54:28', '20', null, 'NORMAL', '0', null, '组织机构', 'paas.userCenter.department', null, 'menu', '1');
INSERT INTO `authority` VALUES ('94', '个人中心', '个人中心', '0', '2016-10-27 17:09:28', '2017-06-16 10:42:00', '22', '1', 'NORMAL', '1', null, '个人中心', 'paas.personal', 'glyphicon glyphicon-dashboard icon', 'menu', '8');
INSERT INTO `authority` VALUES ('104', '个人信息', '个人信息', '94', '2016-11-15 12:01:58', '2017-06-16 10:41:56', '28', '1', 'NORMAL', '1', null, '个人信息', 'paas.personal.info', null, 'menu', '1');
INSERT INTO `authority` VALUES ('115', '系统日志', '系统日志', '40', '2016-11-29 10:38:44', '2016-12-13 10:09:06', '28', '30', 'NORMAL', '0', null, '系统日志', 'paas.userCenter.audit', null, 'menu', '5');
INSERT INTO `authority` VALUES ('122', '镜像仓库', '镜像仓库', '0', '2017-03-01 15:21:07', '2017-07-04 20:05:34', '45', '1', 'NORMAL', '0', null, '', 'paas.repository', 'fa fa-clone icon', 'menu', '4');
INSERT INTO `authority` VALUES ('123', '镜像管理', '镜像', '122', '2017-03-01 16:00:39', '2017-07-04 20:05:45', '45', '1', 'NORMAL', '0', null, '', 'paas.repository.image', null, 'menu', '2');
INSERT INTO `authority` VALUES ('124', '仓库管理', '仓库', '122', '2017-03-01 16:01:27', '2017-03-18 14:00:48', '45', '46', 'NORMAL', '0', null, null, 'paas.repository.repository', null, 'menu', '1');
INSERT INTO `authority` VALUES ('125', '应用服务', '应用服务', '0', '2017-03-08 10:06:57', '2017-03-08 10:06:57', '1', null, 'NORMAL', '0', null, null, 'paas.application', 'fa fa-server icon', 'menu', '5');
INSERT INTO `authority` VALUES ('126', '编排模板', '应用编排', '125', '2017-03-08 10:07:43', '2017-03-16 18:32:19', '1', '49', 'NORMAL', '0', null, null, 'paas.application.template', null, 'menu', '1');
INSERT INTO `authority` VALUES ('127', '应用实例', '应用实例管理', '125', '2017-03-08 10:08:19', '2017-03-08 10:08:19', '1', null, 'NORMAL', '0', null, null, 'paas.application.instance', null, 'menu', '2');
INSERT INTO `authority` VALUES ('137', '环境管理', '环境管理', '9', '2017-06-13 14:16:05', '2017-06-13 14:31:19', '1', '1', 'NORMAL', '0', null, 'app.assets.environment', 'paas.environment.environment', null, 'menu', '1');
INSERT INTO `authority` VALUES ('138', '容器管理', '容器管理', '9', '2017-06-13 14:23:51', '2017-07-05 09:34:12', '1', '6', 'NORMAL', '0', null, 'app.assets.kubernetes', 'paas.environment.container', null, 'menu', '4');
INSERT INTO `authority` VALUES ('139', '存储卷', '存储卷', '9', '2017-06-13 14:27:57', '2017-07-05 09:33:40', '1', '6', 'NORMAL', '0', null, 'app.assets.datastore', 'paas.environment.storage', null, 'menu', '5');
INSERT INTO `authority` VALUES ('140', '应用商店', '应用商店', '125', '2017-06-13 18:54:11', '2017-06-13 18:54:11', '1', null, 'NORMAL', '0', null, 'app.adhibition.module', 'paas.application.store', null, 'menu', '3');
INSERT INTO `authority` VALUES ('142', '系统运维', '系统运维', '0', '2017-06-16 10:35:42', '2017-06-16 11:01:18', '1', '1', 'NORMAL', '0', null, '', 'paas.system.operation', 'fa fa-desktop icon', 'menu', '7');
INSERT INTO `authority` VALUES ('143', '系统参数', '系统参数', '142', '2017-06-16 10:36:43', '2017-06-16 10:36:43', '1', null, 'NORMAL', '0', null, null, 'paas.system.param', null, 'menu', '1');
INSERT INTO `authority` VALUES ('144', '日志中心', '日志中心', '142', '2017-06-16 10:37:41', '2017-06-16 10:37:41', '1', null, 'NORMAL', '0', null, null, 'paas.system.log', null, 'menu', '2');
INSERT INTO `authority` VALUES ('145', '监测平台', '监测平台', '142', '2017-06-16 10:38:20', '2017-06-16 10:38:20', '1', null, 'NORMAL', '0', null, null, 'paas.system.monitor', null, 'menu', '3');
INSERT INTO `authority` VALUES ('146', '告警平台', '告警平台', '142', '2017-06-16 10:38:59', '2017-06-16 10:38:59', '1', null, 'NORMAL', '0', null, null, 'paas.system.warn', null, 'menu', '4');
INSERT INTO `authority` VALUES ('147', '资产目录', '资产目录', '0', '2017-06-16 10:48:35', '2017-06-16 11:01:28', '1', '1', 'NORMAL', '0', null, '', 'paas.assets.statement', 'glyphicon glyphicon-dashboard icon', 'menu', '9');
INSERT INTO `authority` VALUES ('148', '资产管理', '资产管理', '147', '2017-06-16 10:51:43', '2017-06-16 10:51:43', '1', null, 'NORMAL', '0', null, null, 'paas.assets.manager', null, 'menu', '1');
INSERT INTO `authority` VALUES ('149', '服务目录', '服务目录', '147', '2017-06-16 10:52:17', '2017-06-16 10:52:17', '1', null, 'NORMAL', '0', null, null, 'paas.service.statement', null, 'menu', '2');
INSERT INTO `authority` VALUES ('150', '流程管控', '流程管控', '0', '2017-06-16 11:03:04', '2017-06-16 11:03:04', '1', '1', 'NORMAL', '0', null, null, 'paas.process', 'fa fa-sort-amount-asc', 'menu', '6');
INSERT INTO `authority` VALUES ('151', '插件管理', '插件管理', '150', '2017-06-16 11:04:15', '2017-06-16 11:04:15', '1', null, 'NORMAL', '0', null, null, 'paas.process.plug-in', null, 'menu', '1');
INSERT INTO `authority` VALUES ('152', '流程编排', '流程编排', '150', '2017-06-16 11:05:12', '2017-06-16 11:05:12', '1', null, 'NORMAL', '0', null, null, 'paas.process.layout', null, 'menu', '2');
INSERT INTO `authority` VALUES ('153', '定时任务', '定时任务', '150', '2017-06-16 11:05:56', '2017-06-16 11:05:56', '1', null, 'NORMAL', '0', null, null, 'paas.process.task', null, 'menu', '3');
INSERT INTO `authority` VALUES ('154', '负载管理', '负载管理', '9', '2017-07-05 10:30:09', '2017-07-05 10:40:29', '1', '1', 'NORMAL', '0', null, '', 'paas.environment.loadbalance', null, 'menu', '3');

-- ----------------------------
-- Table structure for department
-- ----------------------------
DROP TABLE IF EXISTS `department`;
CREATE TABLE `department` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(64) DEFAULT NULL,
  `status` varchar(16) DEFAULT NULL,
  `is_deleted` tinyint(1) DEFAULT NULL,
  `remark` varchar(256) DEFAULT NULL,
  `props` varchar(1024) DEFAULT NULL,
  `gmt_create` datetime DEFAULT NULL,
  `gmt_modify` datetime DEFAULT NULL,
  `tenant_id` bigint(20) DEFAULT NULL COMMENT '租户id',
  `creater_id` bigint(20) DEFAULT NULL,
  `mender_id` bigint(20) DEFAULT NULL,
  `owner_id` bigint(20) DEFAULT NULL,
  `parent_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of department
-- ----------------------------
INSERT INTO `department` VALUES ('1', 'default', 'NORMAL', '0', '默认', '默认', '2017-04-07 16:17:42', '2017-04-07 16:17:42', null, '1', null, null, '0');
INSERT INTO `department` VALUES ('2', '开发部门', 'NORMAL', '0', '开发', '开发', '2017-06-15 13:57:25', '2017-06-15 13:57:25', null, '1', null, null, '0');
INSERT INTO `department` VALUES ('3', '开发中心', 'NORMAL', '1', '开发中心', 'label=develop1', '2017-06-19 16:39:20', '2017-06-19 16:52:00', null, '2', '2', null, '0');

-- ----------------------------
-- Table structure for deploy_history
-- ----------------------------
DROP TABLE IF EXISTS `deploy_history`;
CREATE TABLE `deploy_history` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `app_id` bigint(20) NOT NULL COMMENT '应用ID',
  `resource_type` varchar(128) NOT NULL COMMENT '资源类型',
  `resource_name` varchar(128) NOT NULL COMMENT '资源名称',
  `current_version` varchar(128) NOT NULL COMMENT '当前版本（镜像名称）',
  `previous_version` varchar(128) DEFAULT NULL COMMENT '上一个版本（镜像名称）',
  `status` varchar(16) NOT NULL COMMENT '状态（0 部署失败，1 部署成功）',
  `is_deleted` tinyint(1) NOT NULL COMMENT '是否删除 （0 未删除， 1 删除）',
  `remark` varchar(256) DEFAULT NULL COMMENT '信息描述',
  `props` varchar(1024) DEFAULT NULL COMMENT '其他属性',
  `gmt_create` datetime NOT NULL COMMENT '创建时间',
  `gmt_modify` datetime DEFAULT NULL COMMENT '修改时间',
  `creater_id` bigint(20) NOT NULL COMMENT '创建者ID',
  `mender_id` bigint(20) DEFAULT NULL COMMENT '修改者ID',
  `owner_id` bigint(20) DEFAULT NULL COMMENT '所有者ID',
  `tenant_id` bigint(20) DEFAULT NULL COMMENT '租户ID',
  `name` varchar(128) NOT NULL COMMENT '部署历史名称',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of deploy_history
-- ----------------------------

-- ----------------------------
-- Table structure for dictionary
-- ----------------------------
DROP TABLE IF EXISTS `dictionary`;
CREATE TABLE `dictionary` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `key` varchar(64) NOT NULL,
  `value` varchar(64) NOT NULL COMMENT '最新版本',
  `pvalue` varchar(32) DEFAULT NULL,
  `name` varchar(64) NOT NULL,
  `gmt_create` datetime DEFAULT NULL,
  `gmt_modify` datetime DEFAULT NULL,
  `creater_id` bigint(11) DEFAULT NULL,
  `owner_id` bigint(11) DEFAULT NULL,
  `mender_id` bigint(11) DEFAULT NULL,
  `status` varchar(1) DEFAULT NULL,
  `is_deleted` tinyint(1) NOT NULL,
  `props` varchar(1024) DEFAULT NULL,
  `remark` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of dictionary
-- ----------------------------

-- ----------------------------
-- Table structure for environment
-- ----------------------------
DROP TABLE IF EXISTS `environment`;
CREATE TABLE `environment` (
  `id` bigint(11) NOT NULL AUTO_INCREMENT COMMENT '环境的id，主键',
  `name` varchar(255) NOT NULL COMMENT '环境名称',
  `status` varchar(1) NOT NULL DEFAULT '1' COMMENT '环境状态信息（1.不可用；2.激活状态；3.冻结状态；4异常状态）',
  `platform` int(1) NOT NULL COMMENT '环境平台（1.kubernetes；2.swarm）',
  `proxy` varchar(100) DEFAULT '' COMMENT '环境代理地址',
  `port` int(6) DEFAULT NULL COMMENT '环境代替访问端口',
  `remark` varchar(255) DEFAULT '' COMMENT '描述信息',
  `tenant_id` int(11) DEFAULT NULL COMMENT '租户id',
  `owner_id` bigint(11) NOT NULL COMMENT '所属者id',
  `mender_id` bigint(11) NOT NULL COMMENT '修改者id',
  `gmt_modify` datetime NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `creater_id` bigint(11) NOT NULL COMMENT '创建者id',
  `gmt_create` datetime NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否被删除的标记（0代表未删除，1代表删除）',
  `props` varchar(1024) DEFAULT NULL COMMENT '其他属性',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of environment
-- ----------------------------

-- ----------------------------
-- Table structure for host
-- ----------------------------
DROP TABLE IF EXISTS `host`;
CREATE TABLE `host` (
  `id` bigint(11) NOT NULL AUTO_INCREMENT COMMENT '环境的id，主键',
  `ip` varchar(17) NOT NULL COMMENT '主机ip',
  `name` varchar(255) DEFAULT NULL COMMENT '主机名称',
  `status` varchar(1) DEFAULT '1' COMMENT '主机状态信息（1--正常；2--不正常；3--可调度；4--不可调度）',
  `remark` varchar(255) DEFAULT '' COMMENT '描述信息',
  `owner_id` bigint(11) DEFAULT NULL COMMENT '所属者id',
  `mender_id` bigint(11) DEFAULT NULL COMMENT '修改者id',
  `gmt_modify` datetime DEFAULT NULL COMMENT '修改时间',
  `creater_id` bigint(11) NOT NULL COMMENT '创建者id',
  `gmt_create` datetime NOT NULL COMMENT '创建时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否被删除的标记（0代表未删除，1代表删除）',
  `props` varchar(1024) DEFAULT NULL COMMENT '其他属性',
  `env_id` bigint(11) DEFAULT NULL COMMENT '环境id',
  `username` varchar(64) DEFAULT NULL COMMENT '主机用户名',
  `password` varchar(64) DEFAULT NULL COMMENT '主机密码',
  `labels` varchar(500) DEFAULT NULL COMMENT '节点标签',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of host
-- ----------------------------

-- ----------------------------
-- Table structure for image
-- ----------------------------
DROP TABLE IF EXISTS `image`;
CREATE TABLE `image` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '镜像id',
  `uuid` varchar(128) NOT NULL COMMENT '镜像的uuid',
  `name` varchar(128) NOT NULL COMMENT '镜像名称',
  `tag` varchar(128) NOT NULL COMMENT '镜像版本号',
  `property` int(1) NOT NULL COMMENT '镜像属性（0 公有，1 私有）',
  `type` int(1) DEFAULT NULL,
  `status` varchar(16) DEFAULT NULL COMMENT '镜像状态信息（0 正常，1异常， 2 锁定）',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否被删除的标记（0代表未删除，1代表删除）',
  `usage_count` int(11) NOT NULL COMMENT '使用次数',
  `remark` varchar(256) DEFAULT NULL COMMENT '描述',
  `props` varchar(1024) DEFAULT NULL COMMENT '其他属性',
  `env_id` bigint(20) DEFAULT NULL,
  `dept_id` bigint(20) DEFAULT NULL,
  `gmt_create` datetime NOT NULL COMMENT '创建时间',
  `gmt_modify` datetime DEFAULT NULL COMMENT '修改时间',
  `creater_id` bigint(20) DEFAULT NULL COMMENT '创建者ID',
  `mender_id` bigint(20) DEFAULT NULL COMMENT '修改者ID',
  `owner_id` bigint(20) DEFAULT NULL COMMENT '所有者ID',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of image
-- ----------------------------

-- ----------------------------
-- Table structure for layout
-- ----------------------------
DROP TABLE IF EXISTS `layout`;
CREATE TABLE `layout` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `file_name` varchar(64) NOT NULL,
  `file_path` varchar(128) NOT NULL,
  `remark` varchar(200) DEFAULT NULL,
  `status` varchar(16) NOT NULL,
  `is_deleted` tinyint(1) DEFAULT NULL,
  `gmt_create` datetime DEFAULT NULL,
  `gmt_modify` datetime DEFAULT NULL,
  `creater_id` bigint(20) DEFAULT NULL,
  `mender_id` bigint(20) DEFAULT NULL,
  `owner_id` bigint(20) DEFAULT NULL,
  `props` varchar(1024) DEFAULT NULL COMMENT '其他属性',
  `type` varchar(16) NOT NULL DEFAULT 'KUBERNETES',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of layout
-- ----------------------------

-- ----------------------------
-- Table structure for loadbalance
-- ----------------------------
DROP TABLE IF EXISTS `loadbalance`;
CREATE TABLE `loadbalance` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '负载ID',
  `name` varchar(64) NOT NULL COMMENT '负载名称',
  `type` int(11) NOT NULL DEFAULT '0' COMMENT '负载类型（0：NGINX,1:F5负载）',
  `manager_ip` varchar(20) NOT NULL COMMENT 'F5 VSIP',
  `port` int(11) DEFAULT NULL COMMENT '负载端口（多为F5端口，NGINX默认为80）',
  `env_id` bigint(20) NOT NULL COMMENT '负载所属环境的ID',
  `tenant_id` int(11) DEFAULT NULL,
  `status` varchar(16) DEFAULT NULL COMMENT '状态（0 正常，1 异常， 2 锁定）',
  `is_deleted` tinyint(1) NOT NULL COMMENT '是否删除（0 未删除, 1 删除）',
  `remark` varchar(256) DEFAULT NULL COMMENT '描述',
  `props` varchar(1024) DEFAULT NULL COMMENT '其他属性',
  `gmt_create` datetime NOT NULL COMMENT '创建时间',
  `gmt_modify` datetime DEFAULT NULL COMMENT '修改时间',
  `creater_id` bigint(20) NOT NULL COMMENT '创建者ID',
  `mender_id` bigint(20) DEFAULT NULL COMMENT '修改者ID',
  `owner_id` bigint(20) DEFAULT NULL COMMENT '所有者ID',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of loadbalance
-- ----------------------------

-- ----------------------------
-- Table structure for openshift_cluster_security
-- ----------------------------
DROP TABLE IF EXISTS `openshift_cluster_security`;
CREATE TABLE `openshift_cluster_security` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `openshift_cluster_id` bigint(20) DEFAULT NULL COMMENT '用户id',
  `salt` varchar(64) DEFAULT NULL COMMENT '随机数值',
  `api_key` varchar(64) DEFAULT NULL,
  `sec_key` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of openshift_cluster_security
-- ----------------------------

-- ----------------------------
-- Table structure for repository
-- ----------------------------
DROP TABLE IF EXISTS `repository`;
CREATE TABLE `repository` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(128) NOT NULL COMMENT '仓库名称',
  `status` varchar(16) NOT NULL COMMENT '状态（0 正常，1 异常， 2 锁定）',
  `is_deleted` tinyint(1) NOT NULL COMMENT '是否删除（0 未删除, 1 删除）',
  `type` int(5) NOT NULL COMMENT '仓库类型（0.dockerregistry;1.harbor;2 dockerhub）',
  `address` varchar(128) NOT NULL COMMENT '仓库地址',
  `port` int(11) DEFAULT NULL COMMENT '仓库端口号',
  `username` varchar(32) DEFAULT NULL COMMENT '用户名',
  `password` varchar(32) DEFAULT NULL COMMENT '密码',
  `property` int(5) DEFAULT NULL COMMENT '属性： 0 共有   1 私有',
  `remark` varchar(256) DEFAULT NULL COMMENT '描述',
  `props` varchar(1024) DEFAULT NULL COMMENT '其他属性',
  `gmt_create` datetime NOT NULL COMMENT '创建时间',
  `gmt_modify` datetime DEFAULT NULL COMMENT '修改时间',
  `creater_id` bigint(20) NOT NULL COMMENT '创建者ID',
  `mender_id` bigint(20) DEFAULT NULL COMMENT '修改者ID',
  `owner_id` bigint(20) DEFAULT NULL COMMENT '所有者ID',
  `dept_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of repository
-- ----------------------------

-- ----------------------------
-- Table structure for repository_image_info
-- ----------------------------
DROP TABLE IF EXISTS `repository_image_info`;
CREATE TABLE `repository_image_info` (
  `id` bigint(11) NOT NULL AUTO_INCREMENT,
  `repository_id` bigint(11) NOT NULL COMMENT '仓库id',
  `namespace` varchar(100) DEFAULT NULL COMMENT '仓库命名空间',
  `image_id` bigint(11) NOT NULL COMMENT '软件id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of repository_image_info
-- ----------------------------

-- ----------------------------
-- Table structure for role
-- ----------------------------
DROP TABLE IF EXISTS `role`;
CREATE TABLE `role` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `name` varchar(64) NOT NULL COMMENT '角色名称',
  `remark` varchar(256) DEFAULT NULL COMMENT '描述',
  `gmt_create` datetime NOT NULL COMMENT '创建时间',
  `gmt_modify` datetime DEFAULT NULL COMMENT '修改时间',
  `tenant_id` bigint(20) DEFAULT NULL COMMENT '租户id',
  `creater_id` bigint(20) DEFAULT NULL COMMENT '创建者ID',
  `mender_id` bigint(20) DEFAULT NULL COMMENT '修改者ID',
  `status` varchar(16) DEFAULT NULL COMMENT '状态',
  `is_deleted` tinyint(1) NOT NULL COMMENT '是否删除',
  `owner_id` bigint(20) DEFAULT NULL,
  `props` varchar(1024) DEFAULT NULL COMMENT '其他属性',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of role
-- ----------------------------
INSERT INTO `role` VALUES ('1', '超级管理员', '超级管理员', '2016-05-31 16:46:20', '2017-06-16 11:07:02', null, '1', '1', 'NORMAL', '0', null, '无');

-- ----------------------------
-- Table structure for role_authority
-- ----------------------------
DROP TABLE IF EXISTS `role_authority`;
CREATE TABLE `role_authority` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `role_id` bigint(20) DEFAULT NULL COMMENT '角色ID',
  `auth_id` bigint(20) DEFAULT NULL COMMENT '权限ID',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=33 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of role_authority
-- ----------------------------
INSERT INTO `role_authority` VALUES ('1', '1', '8');
INSERT INTO `role_authority` VALUES ('2', '1', '9');
INSERT INTO `role_authority` VALUES ('3', '1', '122');
INSERT INTO `role_authority` VALUES ('4', '1', '125');
INSERT INTO `role_authority` VALUES ('5', '1', '150');
INSERT INTO `role_authority` VALUES ('6', '1', '151');
INSERT INTO `role_authority` VALUES ('7', '1', '152');
INSERT INTO `role_authority` VALUES ('8', '1', '153');
INSERT INTO `role_authority` VALUES ('9', '1', '142');
INSERT INTO `role_authority` VALUES ('10', '1', '40');
INSERT INTO `role_authority` VALUES ('11', '1', '147');
INSERT INTO `role_authority` VALUES ('12', '1', '13');
INSERT INTO `role_authority` VALUES ('13', '1', '137');
INSERT INTO `role_authority` VALUES ('14', '1', '138');
INSERT INTO `role_authority` VALUES ('15', '1', '139');
INSERT INTO `role_authority` VALUES ('16', '1', '123');
INSERT INTO `role_authority` VALUES ('17', '1', '124');
INSERT INTO `role_authority` VALUES ('18', '1', '126');
INSERT INTO `role_authority` VALUES ('19', '1', '127');
INSERT INTO `role_authority` VALUES ('20', '1', '140');
INSERT INTO `role_authority` VALUES ('21', '1', '143');
INSERT INTO `role_authority` VALUES ('22', '1', '144');
INSERT INTO `role_authority` VALUES ('23', '1', '145');
INSERT INTO `role_authority` VALUES ('24', '1', '146');
INSERT INTO `role_authority` VALUES ('25', '1', '42');
INSERT INTO `role_authority` VALUES ('26', '1', '43');
INSERT INTO `role_authority` VALUES ('27', '1', '44');
INSERT INTO `role_authority` VALUES ('28', '1', '69');
INSERT INTO `role_authority` VALUES ('29', '1', '115');
INSERT INTO `role_authority` VALUES ('30', '1', '148');
INSERT INTO `role_authority` VALUES ('31', '1', '149');
INSERT INTO `role_authority` VALUES ('32', '1', '154');

-- ----------------------------
-- Table structure for task
-- ----------------------------
DROP TABLE IF EXISTS `task`;
CREATE TABLE `task` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '流程编排id',
  `name` varchar(128) DEFAULT NULL COMMENT '流程编排名称',
  `status` varchar(16) DEFAULT NULL COMMENT '状态（0 正常，1 异常， 2 锁定）',
  `is_deleted` tinyint(1) DEFAULT NULL COMMENT '是否删除（0 未删除, 1 删除）',
  `remark` varchar(256) DEFAULT NULL COMMENT '描述',
  `props` varchar(1024) DEFAULT NULL COMMENT '其他属性',
  `gmt_create` datetime DEFAULT NULL COMMENT '创建时间',
  `gmt_modify` datetime DEFAULT NULL COMMENT '修改时间',
  `creater_id` bigint(20) DEFAULT NULL COMMENT '创建者ID',
  `mender_id` bigint(20) DEFAULT NULL COMMENT '修改者ID',
  `owner_id` bigint(20) DEFAULT NULL COMMENT '所有者ID',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of task
-- ----------------------------
INSERT INTO `task` VALUES ('1', 'git_clone', null, '0', null, null, '2017-06-22 15:32:40', '2017-06-22 15:32:40', null, null, null);
INSERT INTO `task` VALUES ('2', 'code_scan', '', '0', '', '', '2017-06-22 15:32:40', '2017-06-22 15:32:40', null, null, null);
INSERT INTO `task` VALUES ('3', 'jenkins', '', '0', '', '', '2017-06-22 15:32:40', '2017-06-22 15:32:40', null, null, null);
INSERT INTO `task` VALUES ('4', 'build_image', '', '0', '', '', '2017-06-22 15:32:40', '2017-06-22 15:32:40', null, null, null);
INSERT INTO `task` VALUES ('5', 'image_scan', '', '0', '', '', '2017-06-22 15:32:40', '2017-06-22 15:32:40', null, null, null);
INSERT INTO `task` VALUES ('6', 'push_image', '', '0', '', '', '2017-06-22 15:32:40', '2017-06-22 15:32:40', null, null, null);
INSERT INTO `task` VALUES ('7', 'deploy', '', '0', '', '', '2017-06-22 15:32:40', '2017-06-22 15:32:40', null, null, null);

-- ----------------------------
-- Table structure for tenant
-- ----------------------------
DROP TABLE IF EXISTS `tenant`;
CREATE TABLE `tenant` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `name` varchar(64) DEFAULT NULL COMMENT '真实姓名',
  `remark` varchar(256) DEFAULT NULL COMMENT '描述',
  `gmt_create` datetime NOT NULL COMMENT '创建时间',
  `gmt_modify` datetime DEFAULT NULL COMMENT '修改时间',
  `creater_id` bigint(20) NOT NULL COMMENT '创建者ID',
  `mender_id` bigint(20) DEFAULT NULL COMMENT '修改者ID',
  `status` varchar(16) DEFAULT NULL COMMENT '状态',
  `is_deleted` tinyint(1) NOT NULL COMMENT '是否删除',
  `owner_id` bigint(20) DEFAULT NULL,
  `props` varchar(1024) DEFAULT NULL COMMENT '其他属性',
  `company` varchar(64) DEFAULT NULL COMMENT '公司名称',
  `address` varchar(128) DEFAULT NULL COMMENT '公司地址',
  `contacter` varchar(16) DEFAULT NULL COMMENT '联系人',
  `contact_phone` varchar(16) DEFAULT NULL COMMENT '联系邮箱',
  `contact_email` varchar(64) DEFAULT NULL,
  `tenant_phone` varchar(16) DEFAULT NULL COMMENT '租户电话',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of tenant
-- ----------------------------

-- ----------------------------
-- Table structure for tenant_authority
-- ----------------------------
DROP TABLE IF EXISTS `tenant_authority`;
CREATE TABLE `tenant_authority` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `tenant_id` bigint(20) DEFAULT NULL COMMENT '租户ID',
  `auth_id` bigint(20) DEFAULT NULL COMMENT '权限ID',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of tenant_authority
-- ----------------------------

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `name` varchar(64) DEFAULT NULL COMMENT '真实姓名',
  `password` varchar(64) DEFAULT NULL COMMENT '密码',
  `remark` varchar(256) DEFAULT NULL COMMENT '描述',
  `email` varchar(64) DEFAULT NULL COMMENT 'Email',
  `phone` varchar(16) DEFAULT NULL COMMENT '电话',
  `mobile` varchar(16) DEFAULT NULL COMMENT '移动电话',
  `gmt_create` datetime NOT NULL COMMENT '创建时间',
  `gmt_modify` datetime DEFAULT NULL COMMENT '修改时间',
  `creater_id` bigint(20) NOT NULL COMMENT '创建者ID',
  `mender_id` bigint(20) DEFAULT NULL COMMENT '修改者ID',
  `status` varchar(16) DEFAULT NULL COMMENT '状态',
  `is_deleted` tinyint(1) NOT NULL COMMENT '是否删除',
  `owner_id` bigint(20) DEFAULT NULL,
  `props` varchar(1024) DEFAULT NULL COMMENT '其他属性',
  `username` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `company` varchar(256) DEFAULT NULL,
  `session_id` varchar(36) DEFAULT NULL,
  `login_status` tinyint(1) DEFAULT NULL,
  `depart_id` bigint(20) DEFAULT NULL,
  `user_id` varchar(16) DEFAULT NULL COMMENT '工号',
  `sex` tinyint(1) DEFAULT NULL,
  `tenant_id` bigint(20) DEFAULT NULL,
  `is_tenant` tinyint(1) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES ('1', '超级管理员', '-2cc3fe75eb672eeb60a6a80aa0565466506d9d1e', '超级管理员', 'xxx@beyondcent.com', '010-1313131', '18330033981', '2016-03-23 17:48:53', '2017-07-14 10:07:33', '1', '1', 'NORMAL', '0', null, null, 'admin', '', '510D2CE28E1186950E945665A9571843', '1', '1', '001', '1', '0', '0');
INSERT INTO `user` VALUES ('2', '宋松', '-13f40218479171e1bee04629fa9333ec20c5f45c', null, '12345678901@163.com', '010-1234563', '18712345678', '2017-06-15 13:58:30', '2017-07-10 10:25:49', '1', '1', 'NORMAL', '0', null, null, 'ss', 'boyun', '7A73719250852C484424358FC4C0A170', '1', '2', '100', '1', '0', '0');
INSERT INTO `user` VALUES ('3', '张琼', '-136e7519a800f6d0ce157f82f3e7ac85ec9f1d91', null, '12345678901@163.com', '010-1234567', '18712345678', '2017-06-15 13:59:48', '2017-07-10 10:27:44', '1', '1', 'NORMAL', '0', null, null, 'zaney', 'boyun', '5EB2D0EAAA0085EAFDB7C4E1D4081966', '1', '2', '101', '0', '0', '0');
INSERT INTO `user` VALUES ('4', 'ruffy', '-56261fa791cf0127d4a7871b702ad37f5392180f', 'test', '191827469@qq.com', '010-88888888', '15600561414', '2017-06-19 17:34:45', '2017-07-07 18:34:19', '2', '1', 'NORMAL', '0', null, null, 'ruffy', '中科院', '2A94CFCF95749A2A4BF46198CF6ABA45', '1', '1', '11111111', '0', '0', '0');
INSERT INTO `user` VALUES ('5', 'test', '1fc655aa667ad15cdb8485c1c7743dbcac30764c', null, '191827469@qq.com', '010-11111111', '15600561414', '2017-06-19 18:15:07', '2017-06-19 18:54:23', '6', '6', 'ABNORMAL', '1', null, null, 'test', '中科院', null, '0', '1', '333333', '1', null, '0');
INSERT INTO `user` VALUES ('6', 'luogan', '-35c2318f450996283c7be9e26277f7013f123223', null, '123@qq.com', '010-9999999', '13080874355', '2017-07-03 14:14:28', '2017-07-03 18:23:44', '1', '1', 'NORMAL', '0', null, null, 'luogan', 'vx', '052DC1C6F8A7451D999464A6EA74E3C8', '1', '2', '048', '1', null, '0');

-- ----------------------------
-- Table structure for user_role
-- ----------------------------
DROP TABLE IF EXISTS `user_role`;
CREATE TABLE `user_role` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` bigint(20) DEFAULT NULL COMMENT '用户ID',
  `role_id` bigint(20) DEFAULT NULL COMMENT '角色ID',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of user_role
-- ----------------------------
INSERT INTO `user_role` VALUES ('1', '1', '1');
INSERT INTO `user_role` VALUES ('2', '2', '1');
INSERT INTO `user_role` VALUES ('3', '3', '1');
INSERT INTO `user_role` VALUES ('4', '4', '1');
INSERT INTO `user_role` VALUES ('5', '5', '1');
INSERT INTO `user_role` VALUES ('6', '6', '1');

-- ----------------------------
-- Table structure for user_security
-- ----------------------------
DROP TABLE IF EXISTS `user_security`;
CREATE TABLE `user_security` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `user_id` bigint(20) DEFAULT NULL COMMENT '用户id',
  `tenant_id` bigint(20) DEFAULT NULL COMMENT '租户id',
  `salt` varchar(64) DEFAULT NULL COMMENT '随机数值',
  `api_key` varchar(64) DEFAULT NULL,
  `sec_key` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of user_security
-- ----------------------------
INSERT INTO `user_security` VALUES ('1', '1', null, '5560a540-3e29-48c4-b36e-8d85aa8578cb', '1214546782', '1243538932');
INSERT INTO `user_security` VALUES ('2', '2', null, 'b001dd58-2aaf-4c50-828c-287ccb83650b', '-47fd8a0b4ba90b6c3248cc0dac9fa3350560a333', '-d0e02f513b574b6c0fd25f6c619a33232d957d');
INSERT INTO `user_security` VALUES ('3', '3', null, '26989784-e7f1-4972-aaeb-faa4c03da64f', '-57472c8c51ec191a45fb381eaa61421b6c5c3757', '67a3d5efad1ff3b20ca5c3360ba3928567a48cf6');
INSERT INTO `user_security` VALUES ('4', '4', null, '42875a11-9dad-4ae8-9c3b-a01ad211b070', '-5642a819309d8ca935117f0f639bd24e47909ba', '-62b689509342376f96d28eb46fbf956abe552f63');
INSERT INTO `user_security` VALUES ('5', '5', null, '0e3fc80f-d93f-412c-9e8c-3fd3470929b7', '77479a9ce6b4e8b3b5662d3f6b4b046a44a019cd', '5712f60370248fa4db89b92332a237c37dae8cd5');
INSERT INTO `user_security` VALUES ('6', '6', null, '4c354796-f1e7-4fc5-ad31-222b5693669f', '1f9b94ed65317e9eccf471ff990531bdb059c485', '-618cf830c8620449c11a7595d5ed83d7243135bb');

-- ----------------------------
-- Table structure for volume
-- ----------------------------
DROP TABLE IF EXISTS `volume`;
CREATE TABLE `volume` (
  `id` bigint(11) NOT NULL AUTO_INCREMENT COMMENT 'pv主键',
  `name` varchar(255) NOT NULL COMMENT 'pv名称',
  `status` varchar(20) DEFAULT NULL COMMENT 'pv四种状态：Available、Bound、Released、Failed',
  `remark` varchar(255) DEFAULT '' COMMENT '描述信息',
  `owner_id` bigint(11) DEFAULT NULL COMMENT '所属者id',
  `mender_id` bigint(11) DEFAULT NULL COMMENT '修改者id',
  `gmt_modify` datetime DEFAULT NULL COMMENT '修改时间',
  `creater_id` bigint(11) NOT NULL COMMENT '创建者id',
  `gmt_create` datetime NOT NULL COMMENT '创建时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否被删除的标记（0代表未删除，1代表删除）',
  `props` varchar(1024) DEFAULT NULL COMMENT '其他属性',
  `env_id` bigint(11) DEFAULT NULL COMMENT '环境id',
  `labels` varchar(500) DEFAULT NULL COMMENT '节点标签',
  `policy` varchar(10) NOT NULL COMMENT 'pv回收策略：Retain、Recycle、Delete',
  `access` varchar(20) NOT NULL COMMENT 'accessModes：ReadWriteOnce、ReadOnlyMany、ReadWriteMany',
  `capacity` varchar(20) NOT NULL COMMENT 'pv容量',
  `type` varchar(30) NOT NULL COMMENT 'pv类型',
  `annotations` varchar(1024) DEFAULT NULL COMMENT '存储卷注解',
  `ip` varchar(20) DEFAULT NULL COMMENT 'nfs存储服务所在机器ip',
  `path` varchar(100) DEFAULT NULL COMMENT '存储卷存储路径',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of volume
-- ----------------------------

-- ----------------------------
-- Table structure for workflow
-- ----------------------------
DROP TABLE IF EXISTS `workflow`;
CREATE TABLE `workflow` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '流程编排id',
  `name` varchar(128) DEFAULT NULL COMMENT '流程编排名称',
  `status` varchar(16) DEFAULT NULL COMMENT '状态（0 正常，1 异常， 2 锁定）',
  `is_deleted` tinyint(1) DEFAULT NULL COMMENT '是否删除（0 未删除, 1 删除）',
  `remark` varchar(256) DEFAULT NULL COMMENT '描述',
  `props` varchar(1024) DEFAULT NULL COMMENT '其他属性',
  `gmt_create` datetime DEFAULT NULL COMMENT '创建时间',
  `gmt_modify` datetime DEFAULT NULL COMMENT '修改时间',
  `creater_id` bigint(20) DEFAULT NULL COMMENT '创建者ID',
  `mender_id` bigint(20) DEFAULT NULL COMMENT '修改者ID',
  `owner_id` bigint(20) DEFAULT NULL COMMENT '所有者ID',
  `version` int(10) NOT NULL,
  `workflow_json` varchar(8192) NOT NULL,
  `workflow_ids` varchar(2048) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of workflow
-- ----------------------------

-- ----------------------------
-- View structure for view_repository_software
-- ----------------------------
DROP VIEW IF EXISTS `view_repository_software`;
CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`%` SQL SECURITY DEFINER VIEW `view_repository_software` AS select `image`.`dept_id` AS `tenant_id` from `image` ;
SET FOREIGN_KEY_CHECKS=1;
