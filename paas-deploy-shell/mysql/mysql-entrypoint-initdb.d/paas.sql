/*
Navicat MySQL Data Transfer

Source Server         : Zq
Source Server Version : 50717
Source Host           : localhost:3306
Source Database       : paas

Target Server Type    : MYSQL
Target Server Version : 50717
File Encoding         : 65001

Date: 2018-03-16 15:01:16
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
  `quota_status` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=27 DEFAULT CHARSET=utf8;


-- ----------------------------
-- Table structure for application_image_info
-- ----------------------------
DROP TABLE IF EXISTS `application_image_info`;
CREATE TABLE `application_image_info` (
  `id` bigint(11) NOT NULL AUTO_INCREMENT,
  `application_id` bigint(11) NOT NULL COMMENT '应用id',
  `image_id` bigint(11) NOT NULL COMMENT '软件id',
  `use_count` int(11) DEFAULT NULL COMMENT '依赖总数',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8;


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
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
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
  `file_path` varchar(128) DEFAULT NULL COMMENT '用户上传的yaml文件路径',
  `is_deleted` tinyint(1) NOT NULL,
  `picture_path` varchar(128) DEFAULT NULL COMMENT '用户上传的图片路径',
  `deploy_type` varchar(32) NOT NULL COMMENT '部署方式',
  `remark` varchar(255) NOT NULL COMMENT '组件的描述信息',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of application_store
-- ----------------------------
INSERT INTO `application_store` VALUES ('1', 'gitlab', '9.3.2-ce.0', 'gitlab.png', 'gitlab-deployment.yaml', '开发工具', '0', '', '0', null, '0', 'GitLab 是一个用于仓库管理系统的开源项目，使用Git作为代码管理工具，并在此基础上搭建起来的web服务');
INSERT INTO `application_store` VALUES ('2', 'snoarqube', '5.6', 'sonar.png', 'sonar.yaml', '开发工具', '0', null, '1', null, '1', 'Sonar（代码质量管理平台）是一个开源平台，用于管理Java源代码的质量');
INSERT INTO `application_store` VALUES ('3', 'jenkins', '2.60.2-1', 'jenkins.png', 'jenkins-deployment.yaml', '开发工具', '0', null, '0', null, '0', 'Jenkins是一个开源软件项目，旨在提供一个开放易用的软件平台，使软件的持续集成变成可能。');
INSERT INTO `application_store` VALUES ('4', 'tomcat', 'com', 'tomcat.png', 'tomcat-deployment.yaml', '应用服务器', '31', null, '0', null, '0', 'Tomcat 服务器是一个免费的开放源代码的Web 应用服务器，属于轻量级应用服务器。');
INSERT INTO `application_store` VALUES ('5', 'jboss', 'latest', 'jboss.png', 'jboss.yaml', '应用服务器', '0', null, '1', null, '1', '是一个基于J2EE的开放源代码的应用服务器。');
INSERT INTO `application_store` VALUES ('7', 'zookeeper', 'v1', 'zookeeper.png', 'zk-deployment.yaml,zookeeper-cluster.yaml', '应用中间件', '23', null, '0', null, '2', 'ZooKeeper是一个分布式的，开放源码的分布式应用程序协调服务。');
INSERT INTO `application_store` VALUES ('8', 'rabbitmq', '3-management', 'rabbitmq.png', 'rabbitmq-deployment.yaml,rabbitmq-cluster.yaml', '应用中间件', '4', null, '0', null, '2', 'RabbitMQ是一个在AMQP基础上完成的，可复用的企业消息系统。他遵循Mozilla Public License开源协议。');
INSERT INTO `application_store` VALUES ('9', 'redis', '3.2.1', 'redis.png', 'redis-deployment.yaml,redis-cluster.yaml', '应用中间件', '2', null, '0', null, '2', 'Redis是一个开源的使用ANSI C语言编写、支持网络、可基于内存亦可持久化的日志型、Key-Value数据库。');
INSERT INTO `application_store` VALUES ('10', 'nginx', '1.11.5-patched', 'nginx.png', 'nginx-deployment.yaml', '负载均衡器', '0', null, '0', null, '0', 'Nginx是一个高性能的HTTP和反向代理服务器，也是一个IMAP/POP3/SMTP服务器。');
INSERT INTO `application_store` VALUES ('11', 'haproxy', '1.7', 'haproxy.png', 'haproxy-deployment.yaml', '负载均衡器', '0', null, '0', null, '0', 'HAProxy是一个使用C语言编写的自由及开放源代码软件[1]，其提供高可用性、负载均衡，以及基于TCP和HTTP的应用程序代理。');
INSERT INTO `application_store` VALUES ('12', 'mysql', '5.6', 'mysql.png', 'mysql-deployment.yaml,mysql-cluster.yaml', '数据库', '27', null, '0', null, '2', 'MySQL是一个关系型数据库管理系统');
INSERT INTO `application_store` VALUES ('13', 'postgres', 'latest', 'postgres.png', 'postgres.yaml', '数据库', '0', null, '1', null, '1', 'PostgreSQL 是一个自由的对象-关系数据库服务器(数据库管理系统)');
INSERT INTO `application_store` VALUES ('14', 'influxdb', 'latest', 'influxdb.png', 'influxdb.yaml', '数据库', '0', null, '1', null, '1', 'InfluxDB 是一个开源分布式时序、事件和指标数据库。');
INSERT INTO `application_store` VALUES ('15', 'kafka', 'v1', 'kafka.png', 'kafka-deployment.yaml,kafka-cluster.yaml', '应用中间件', '7', '', '0', '', '2', 'Kafka是一种高吞吐量的分布式发布订阅消息系统，可以处理消费者规模的网站中的所有动作流数据。');
INSERT INTO `application_store` VALUES ('18', 'ty', '8.5.15', 'default.png', 'ty.yaml', '应用服务器', '1', '/tmp/application_store/file\\20171116152900887-ty', '0', '', '0', 'regt');

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
) ENGINE=InnoDB AUTO_INCREMENT=158 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of authority
-- ----------------------------
INSERT INTO `authority` VALUES ('8', '数据纵览', '数据纵览', '0', '2016-09-07 15:05:57', '2017-07-08 10:08:37', '1', '6', 'NORMAL', '0', null, '无', 'paas.dashboard.dashboard', 'fa fa-television', 'menu', '1');
INSERT INTO `authority` VALUES ('9', '环境资源', '环境资源', '0', '2016-09-07 20:54:08', '2017-06-13 14:07:21', '1', '1', 'NORMAL', '0', null, '无', 'paas.environment', '	fa fa-hdd-o', 'menu', '2');
INSERT INTO `authority` VALUES ('13', '主机管理', '主机资源', '9', '2016-09-07 20:56:39', '2017-06-13 14:19:21', '1', '1', 'NORMAL', '0', null, 'app.assets.resource', 'paas.environment.host', null, 'menu', '2');
INSERT INTO `authority` VALUES ('40', '用户中心', '用户中心', '0', '2016-09-08 09:59:35', '2017-06-16 10:46:45', '1', '1', 'NORMAL', '0', null, '无', 'paas.userCenter', '	fa fa-user-o', 'menu', '8');
INSERT INTO `authority` VALUES ('42', '用户管理', '用户管理', '40', '2016-09-08 10:00:14', '2016-10-14 17:54:45', '1', '20', 'NORMAL', '0', null, '无', 'paas.userCenter.user', null, 'menu', '2');
INSERT INTO `authority` VALUES ('43', '角色管理', '角色管理', '40', '2016-09-08 10:00:45', '2016-10-14 17:55:00', '1', '20', 'NORMAL', '0', null, '无', 'paas.userCenter.role', null, 'menu', '3');
INSERT INTO `authority` VALUES ('44', '权限管理', '权限管理', '40', '2016-09-08 10:01:03', '2016-10-14 17:55:14', '1', '20', 'NORMAL', '0', null, '无', 'paas.userCenter.permission', null, 'menu', '4');
INSERT INTO `authority` VALUES ('69', '组织机构', '组织机构', '40', '2016-10-14 17:54:28', '2016-10-14 17:54:28', '20', null, 'NORMAL', '0', null, '组织机构', 'paas.userCenter.department', null, 'menu', '1');
INSERT INTO `authority` VALUES ('94', '个人中心', '个人中心', '0', '2016-10-27 17:09:28', '2017-06-16 10:42:00', '22', '1', 'NORMAL', '1', null, '个人中心', 'paas.personal', 'glyphicon glyphicon-dashboard icon', 'menu', '8');
INSERT INTO `authority` VALUES ('104', '个人信息', '个人信息', '94', '2016-11-15 12:01:58', '2017-06-16 10:41:56', '28', '1', 'NORMAL', '1', null, '个人信息', 'paas.personal.info', null, 'menu', '1');
INSERT INTO `authority` VALUES ('115', '系统日志', '系统日志', '40', '2016-11-29 10:38:44', '2017-10-25 18:22:51', '28', '1', 'NORMAL', '1', null, '系统日志', 'paas.userCenter.audit', null, 'menu', '5');
INSERT INTO `authority` VALUES ('122', '镜像仓库', '镜像仓库', '0', '2017-03-01 15:21:07', '2017-07-04 20:05:34', '45', '1', 'NORMAL', '0', null, '', 'paas.repository', '	fa fa-clone', 'menu', '4');
INSERT INTO `authority` VALUES ('123', '镜像管理', '镜像', '122', '2017-03-01 16:00:39', '2017-09-20 09:51:38', '45', '10', 'NORMAL', '0', null, '', 'paas.repository.dockerimage', null, 'menu', '2');
INSERT INTO `authority` VALUES ('124', '仓库管理', '仓库', '122', '2017-03-01 16:01:27', '2017-03-18 14:00:48', '45', '46', 'NORMAL', '0', null, null, 'paas.repository.repository', null, 'menu', '1');
INSERT INTO `authority` VALUES ('125', '应用服务', '应用服务', '0', '2017-03-08 10:06:57', '2017-03-08 10:06:57', '1', null, 'NORMAL', '0', null, null, 'paas.application', '	fa fa-server', 'menu', '5');
INSERT INTO `authority` VALUES ('126', '应用编排', '应用编排', '125', '2017-03-08 10:07:43', '2017-03-16 18:32:19', '1', '49', 'NORMAL', '0', null, null, 'paas.application.template', null, 'menu', '3');
INSERT INTO `authority` VALUES ('127', '应用实例', '应用实例管理', '125', '2017-03-08 10:08:19', '2017-03-08 10:08:19', '1', null, 'NORMAL', '0', null, null, 'paas.application.instance', null, 'menu', '1');
INSERT INTO `authority` VALUES ('137', '环境管理', '环境管理', '9', '2017-06-13 14:16:05', '2017-06-13 14:31:19', '1', '1', 'NORMAL', '0', null, 'app.assets.environment', 'paas.environment.environment', null, 'menu', '1');
INSERT INTO `authority` VALUES ('138', '容器管理', '容器管理', '9', '2017-06-13 14:23:51', '2017-07-05 09:34:12', '1', '6', 'NORMAL', '0', null, 'app.assets.kubernetes', 'paas.environment.container', null, 'menu', '4');
INSERT INTO `authority` VALUES ('139', '存储卷', '存储卷', '9', '2017-06-13 14:27:57', '2017-07-05 09:33:40', '1', '6', 'NORMAL', '0', null, 'app.assets.datastore', 'paas.environment.storage', null, 'menu', '5');
INSERT INTO `authority` VALUES ('140', '应用商店', '应用商店', '125', '2017-06-13 18:54:11', '2018-03-06 16:44:12', '1', '1', 'NORMAL', '1', null, 'app.adhibition.module', 'paas.application.store', null, 'menu', '2');
INSERT INTO `authority` VALUES ('142', '系统运维', '系统运维', '0', '2017-06-16 10:35:42', '2017-06-16 11:01:18', '1', '1', 'NORMAL', '0', null, '', 'paas.system.operation', '	fa fa-codepen', 'menu', '7');
INSERT INTO `authority` VALUES ('143', '系统参数', '系统参数', '142', '2017-06-16 10:36:43', '2018-03-06 16:45:40', '1', '1', 'NORMAL', '1', null, '', 'paas.system.parameter', null, 'menu', '1');
INSERT INTO `authority` VALUES ('144', '日志中心', '日志中心', '142', '2017-06-16 10:37:41', '2017-06-16 10:37:41', '1', null, 'NORMAL', '0', null, null, 'paas.system.log', null, 'menu', '2');
INSERT INTO `authority` VALUES ('145', '监测平台', '监测平台', '142', '2017-06-16 10:38:20', '2017-06-16 10:38:20', '1', null, 'NORMAL', '0', null, null, 'paas.system.monitor', null, 'menu', '3');
INSERT INTO `authority` VALUES ('146', '告警平台', '告警平台', '142', '2017-06-16 10:38:59', '2018-03-06 16:45:45', '1', '1', 'NORMAL', '1', null, null, 'paas.system.warn', null, 'menu', '4');
INSERT INTO `authority` VALUES ('147', '资产目录', '资产目录', '0', '2017-06-16 10:48:35', '2018-03-06 16:46:10', '1', '1', 'NORMAL', '1', null, '', 'paas.assets.statement', '	fa fa-file-text-o', 'menu', '9');
INSERT INTO `authority` VALUES ('148', '资产管理', '资产管理', '147', '2017-06-16 10:51:43', '2018-03-06 16:46:01', '1', '1', 'NORMAL', '1', null, null, 'paas.assets.manager', null, 'menu', '1');
INSERT INTO `authority` VALUES ('149', '服务目录', '服务目录', '147', '2017-06-16 10:52:17', '2018-03-06 16:46:07', '1', '1', 'NORMAL', '1', null, '', 'paas.service_catalog.list', '', 'menu', '2');
INSERT INTO `authority` VALUES ('150', '流程管控', '流程管控', '0', '2017-06-16 11:03:04', '2018-03-06 16:46:31', '1', '1', 'NORMAL', '1', null, null, 'paas.process', 'fa fa-object-group', 'menu', '6');
INSERT INTO `authority` VALUES ('151', '插件管理', '插件管理', '150', '2017-06-16 11:04:15', '2017-10-25 18:23:08', '1', '1', 'NORMAL', '1', null, null, 'paas.process.plugin', null, 'menu', '1');
INSERT INTO `authority` VALUES ('152', '流程编排', '流程编排', '150', '2017-06-16 11:05:12', '2018-03-06 16:46:28', '1', '1', 'NORMAL', '1', null, null, 'paas.process.layout', null, 'menu', '2');
INSERT INTO `authority` VALUES ('153', '定时任务', '定时任务', '150', '2017-06-16 11:05:56', '2017-06-16 11:05:56', '1', null, 'NORMAL', '1', null, null, 'paas.process.task', null, 'menu', '3');
INSERT INTO `authority` VALUES ('154', '负载管理', '负载管理', '9', '2017-07-05 10:30:09', '2017-09-05 10:02:48', '1', '2', 'NORMAL', '1', null, '', 'paas.environment.loadbalance', null, 'menu', '3');
INSERT INTO `authority` VALUES ('156', '配置管理', '配置管理', '125', '2017-10-18 15:12:14', '2017-10-18 15:13:22', '3', '3', 'NORMAL', '0', null, '', 'paas.application.configManage', '', 'menu', '4');
INSERT INTO `authority` VALUES ('157', '代码仓库', '代码仓库', '150', '2017-10-27 19:08:22', '2018-03-06 16:46:22', '2', '1', 'NORMAL', '1', null, '', 'paas.process.codeRepository', '', 'menu', '1');

-- ----------------------------
-- Table structure for code_repository
-- ----------------------------
DROP TABLE IF EXISTS `code_repository`;
CREATE TABLE `code_repository` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '流程编排id',
  `name` varchar(128) DEFAULT NULL COMMENT '流程编排名称',
  `status` varchar(16) DEFAULT NULL COMMENT '状态（0 激活，1 锁定）',
  `is_deleted` tinyint(1) DEFAULT NULL COMMENT '是否删除（0 未删除, 1 删除）',
  `remark` varchar(256) DEFAULT NULL COMMENT '描述',
  `props` varchar(1024) DEFAULT NULL COMMENT '其他属性',
  `gmt_create` datetime DEFAULT NULL COMMENT '创建时间',
  `gmt_modify` datetime DEFAULT NULL COMMENT '修改时间',
  `creater_id` bigint(20) DEFAULT NULL COMMENT '创建者ID',
  `mender_id` bigint(20) DEFAULT NULL COMMENT '修改者ID',
  `owner_id` bigint(20) DEFAULT NULL COMMENT '所有者ID',
  `dept_id` bigint(20) DEFAULT NULL COMMENT '所属部门',
  `user_name` varchar(32) DEFAULT NULL COMMENT '私有代码仓库的用户名',
  `password` varchar(64) DEFAULT NULL COMMENT '私有代码仓库的密码',
  `type` varchar(16) DEFAULT NULL COMMENT '代码仓库类型：公有 私有 ',
  `code_source` varchar(255) DEFAULT NULL COMMENT '代码源',
  `software_type` varchar(16) DEFAULT NULL COMMENT '软件类型： 0、git ；1、svn',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of code_repository
-- ----------------------------

-- ----------------------------
-- Table structure for config_manage
-- ----------------------------
DROP TABLE IF EXISTS `config_manage`;
CREATE TABLE `config_manage` (
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
  `file_dir` varchar(128) DEFAULT NULL COMMENT '目录创建时，多文件目录路径',
  `dept_id` bigint(20) DEFAULT NULL COMMENT '所属部门',
  `type` varchar(5) DEFAULT NULL COMMENT '类型：0：手动  1：自动',
  `app_id` bigint(20) DEFAULT NULL COMMENT '应用ID',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;


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
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of department
-- ----------------------------
INSERT INTO `department` VALUES ('1', 'default', 'NORMAL', '0', '默认', '默认', '2017-04-07 16:17:42', '2017-04-07 16:17:42', null, '1', null, null, '0');

-- ----------------------------
-- Table structure for deploy_history
-- ----------------------------
DROP TABLE IF EXISTS `deploy_history`;
CREATE TABLE `deploy_history` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `service_name` varchar(32) DEFAULT NULL COMMENT '服务名',
  `app_id` bigint(20) DEFAULT NULL COMMENT '应用ID',
  `version` datetime DEFAULT NULL COMMENT '版本号',
  `object` varchar(32) DEFAULT NULL COMMENT '操作主体对象(弹性伸缩,资源限制，滚动升级，版本回滚)',
  `data_info` varchar(512) DEFAULT NULL COMMENT '存储更新前服务资源信息,用于页面展示',
  `remark` varchar(512) DEFAULT NULL COMMENT '结果',
  `gmt_create` datetime NOT NULL COMMENT '创建时间',
  `param_info` varchar(512) DEFAULT NULL COMMENT '存储更新前服务资源信息',
  `result` varchar(16) DEFAULT NULL,
  `creater_id` bigint(20) NOT NULL COMMENT '创建者ID',
  `dept_id` bigint(20) DEFAULT NULL COMMENT '所属部门',
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
  `dict_key` varchar(64) NOT NULL COMMENT 'key 是属性名称',
  `dict_value` varchar(64) NOT NULL COMMENT 'key属性名对应的值',
  `pvalue` varchar(32) DEFAULT NULL COMMENT '某一个key 属于哪个模块里',
  `software_type` varchar(32) DEFAULT NULL COMMENT '属于哪个模块的哪个软件类型',
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
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for environment
-- ----------------------------
DROP TABLE IF EXISTS `environment`;
CREATE TABLE `environment` (
  `id` bigint(11) NOT NULL AUTO_INCREMENT COMMENT '环境的id，主键',
  `name` varchar(255) NOT NULL COMMENT '环境名称',
  `status` varchar(1) NOT NULL DEFAULT '1' COMMENT '环境状态信息（1.不可用；2.激活状态；3.冻结状态；4异常状态;5创建中；6死亡）',
  `platform` int(1) NOT NULL COMMENT '环境平台（1.kubernetes；2.swarm）',
  `proxy` varchar(100) DEFAULT '' COMMENT '环境代理地址',
  `port` int(6) DEFAULT NULL COMMENT '环境代替访问端口',
  `remark` varchar(255) DEFAULT '' COMMENT '描述信息',
  `tenant_id` int(11) DEFAULT NULL COMMENT '租户id',
  `owner_id` bigint(11) NOT NULL COMMENT '所属者id',
  `mender_id` bigint(11) NOT NULL COMMENT '修改者id',
  `gmt_modify` datetime NOT NULL COMMENT '修改时间',
  `creater_id` bigint(11) NOT NULL COMMENT '创建者id',
  `gmt_create` datetime NOT NULL COMMENT '创建时间',
  `dept_id` bigint(20) DEFAULT NULL,
  `is_deleted` tinyint(20) NOT NULL DEFAULT '0' COMMENT '是否被删除的标记（0代表未删除，1代表删除）',
  `props` varchar(1024) DEFAULT NULL COMMENT '其他属性',
  `source` varchar(20) DEFAULT NULL COMMENT '标明环境进群来源:1.receive(接管)2create(创建)',
  `master` varchar(17) DEFAULT NULL COMMENT '该环境下的主节点IP',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8;


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
  `creater_id` bigint(11) DEFAULT NULL COMMENT '创建者id',
  `gmt_create` datetime NOT NULL COMMENT '创建时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否被删除的标记（0代表未删除，1代表删除）',
  `dept_id` bigint(20) DEFAULT NULL COMMENT '所属部门',
  `props` varchar(1024) DEFAULT NULL COMMENT '其他属性',
  `env_id` bigint(11) DEFAULT NULL COMMENT '环境id',
  `username` varchar(64) DEFAULT NULL COMMENT '主机用户名',
  `password` varchar(64) DEFAULT NULL COMMENT '主机密码',
  `labels` varchar(500) DEFAULT NULL COMMENT '节点标签',
  `host_name` varchar(128) DEFAULT NULL COMMENT '主机在集群中显示的名称',
  `source` varchar(20) NOT NULL COMMENT '主机来源：create、receive',
  `etcd` varchar(1) DEFAULT NULL COMMENT '0代表该主机是etcd主机',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2286 DEFAULT CHARSET=utf8;


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
) ENGINE=InnoDB AUTO_INCREMENT=249 DEFAULT CHARSET=utf8;


-- ----------------------------
-- Table structure for jenkins_credential
-- ----------------------------
DROP TABLE IF EXISTS `jenkins_credential`;
CREATE TABLE `jenkins_credential` (
  `id` bigint(11) NOT NULL AUTO_INCREMENT COMMENT 'id，主键',
  `credential_id` varchar(64) NOT NULL COMMENT '凭证的id',
  `credential_username` varchar(25) NOT NULL COMMENT '凭证的用户名',
  `credential_password` varchar(25) NOT NULL COMMENT '凭证的密码',
  `credential_scope` varchar(25) NOT NULL COMMENT '凭证的范围，目前仅支持Global和System',
  `credential_description` varchar(255) DEFAULT NULL COMMENT '凭证的描述',
  `remark` varchar(255) DEFAULT '' COMMENT '描述信息',
  `tenant_id` int(11) DEFAULT NULL COMMENT '租户id',
  `owner_id` bigint(11) NOT NULL COMMENT '所属者id',
  `mender_id` bigint(11) NOT NULL COMMENT '修改者id',
  `gmt_modify` datetime NOT NULL COMMENT '修改时间',
  `creater_id` bigint(11) NOT NULL COMMENT '创建者id',
  `gmt_create` datetime NOT NULL COMMENT '创建时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否被删除的标记（0代表未删除，1代表删除）',
  `dept_id` bigint(20) DEFAULT NULL COMMENT '所属部门',
  `props` varchar(1024) DEFAULT NULL COMMENT '其他属性',
  `name` varchar(64) DEFAULT NULL,
  `status` varchar(1) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of jenkins_credential
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
  `dept_id` bigint(20) DEFAULT NULL COMMENT '所属部门',
  `props` varchar(1024) DEFAULT NULL COMMENT '其他属性',
  `type` varchar(16) NOT NULL DEFAULT 'KUBERNETES',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of layout
-- ----------------------------

-- ----------------------------
-- Table structure for layout_template
-- ----------------------------
DROP TABLE IF EXISTS `layout_template`;
CREATE TABLE `layout_template` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `remark` varchar(200) DEFAULT NULL,
  `status` varchar(16) DEFAULT NULL,
  `is_deleted` tinyint(1) DEFAULT NULL,
  `gmt_create` datetime DEFAULT NULL,
  `gmt_modify` datetime DEFAULT NULL,
  `creater_id` bigint(20) DEFAULT NULL,
  `mender_id` bigint(20) DEFAULT NULL,
  `owner_id` bigint(20) DEFAULT NULL,
  `dept_id` bigint(20) DEFAULT NULL COMMENT '所属部门',
  `props` varchar(1024) DEFAULT NULL COMMENT '其他属性',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of layout_template
-- ----------------------------

-- ----------------------------
-- Table structure for layout_template_version
-- ----------------------------
DROP TABLE IF EXISTS `layout_template_version`;
CREATE TABLE `layout_template_version` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `version` varchar(255) NOT NULL,
  `template_file_path` varchar(256) NOT NULL,
  `remark` varchar(200) DEFAULT NULL,
  `status` varchar(16) DEFAULT NULL,
  `is_deleted` tinyint(1) DEFAULT NULL,
  `gmt_create` datetime DEFAULT NULL,
  `gmt_modify` datetime DEFAULT NULL,
  `creater_id` bigint(20) DEFAULT NULL,
  `mender_id` bigint(20) DEFAULT NULL,
  `owner_id` bigint(20) DEFAULT NULL,
  `props` varchar(1024) DEFAULT NULL COMMENT '其他属性',
  `layout_template_id` bigint(20) NOT NULL,
  `name` varchar(32) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of layout_template_version
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
  `dept_id` bigint(20) DEFAULT NULL COMMENT '所属部门',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of loadbalance
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
  `protocol_type` int(1) DEFAULT '0' COMMENT '0:http协议；1:https协议',
  `auth_mode` int(1) DEFAULT '0' COMMENT '0:公共认证；1:私有认证',
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
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;


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
) ENGINE=InnoDB AUTO_INCREMENT=249 DEFAULT CHARSET=utf8;


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
  `dept_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of role
-- ----------------------------
INSERT INTO `role` VALUES ('1', '超级管理员', '超级管理员', '2016-05-31 16:46:20', '2017-11-09 16:15:29', null, '1', '4', 'NORMAL', '0', null, '无', null);

-- ----------------------------
-- Table structure for role_authority
-- ----------------------------
DROP TABLE IF EXISTS `role_authority`;
CREATE TABLE `role_authority`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `role_id` bigint(20) DEFAULT NULL COMMENT '角色ID',
  `auth_id` bigint(20) DEFAULT NULL COMMENT '权限ID',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5101 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of role_authority
-- ----------------------------
INSERT INTO `role_authority` VALUES (5082, 1, 8);
INSERT INTO `role_authority` VALUES (5083, 1, 137);
INSERT INTO `role_authority` VALUES (5084, 1, 13);
INSERT INTO `role_authority` VALUES (5085, 1, 139);
INSERT INTO `role_authority` VALUES (5086, 1, 122);
INSERT INTO `role_authority` VALUES (5087, 1, 124);
INSERT INTO `role_authority` VALUES (5088, 1, 123);
INSERT INTO `role_authority` VALUES (5089, 1, 125);
INSERT INTO `role_authority` VALUES (5090, 1, 127);
INSERT INTO `role_authority` VALUES (5091, 1, 126);
INSERT INTO `role_authority` VALUES (5092, 1, 156);
INSERT INTO `role_authority` VALUES (5093, 1, 142);
INSERT INTO `role_authority` VALUES (5094, 1, 144);
INSERT INTO `role_authority` VALUES (5095, 1, 145);
INSERT INTO `role_authority` VALUES (5096, 1, 40);
INSERT INTO `role_authority` VALUES (5097, 1, 69);
INSERT INTO `role_authority` VALUES (5098, 1, 42);
INSERT INTO `role_authority` VALUES (5099, 1, 43);
INSERT INTO `role_authority` VALUES (5100, 1, 44);

-- ----------------------------
-- Table structure for service_alarm
-- ----------------------------
DROP TABLE IF EXISTS `service_alarm`;
CREATE TABLE `service_alarm` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '流程编排id',
  `name` varchar(64) DEFAULT NULL COMMENT '服务名称',
  `number` int(11) DEFAULT NULL COMMENT '告警策略值',
  `email` varchar(255) DEFAULT NULL COMMENT '用户邮箱地址',
  `status` varchar(16) DEFAULT NULL COMMENT '告警状态：0、正常  1、告警',
  `application_id` bigint(20) DEFAULT NULL COMMENT '应用ID',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of service_alarm
-- ----------------------------

-- ----------------------------
-- Table structure for service_rely_info
-- ----------------------------
DROP TABLE IF EXISTS `service_rely_info`;
CREATE TABLE `service_rely_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `current_name` varchar(32) DEFAULT NULL COMMENT '当前服务名',
  `current_namespace` varchar(32) DEFAULT NULL COMMENT '当前服务工作空间',
  `rely_name` varchar(32) DEFAULT NULL COMMENT '依赖服务名',
  `rely_namespace` varchar(32) DEFAULT NULL COMMENT '依赖服务工作空间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of service_rely_info
-- ----------------------------

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
  `file_name` varchar(32) DEFAULT NULL,
  `ch_name` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of task
-- ----------------------------
INSERT INTO `task` VALUES ('1', 'git_clone', null, '0', null, null, '2017-06-22 15:32:40', '2017-06-22 15:32:40', null, null, null, 'pull_template.xml', '从git上拉取代码');
INSERT INTO `task` VALUES ('2', 'code_scan', '', '0', '', '', '2017-06-22 15:32:40', '2017-06-22 15:32:40', null, null, null, 'sonar_template.xml', '代码扫描');
INSERT INTO `task` VALUES ('3', 'package', '', '0', '', '', '2017-06-22 15:32:40', '2017-06-22 15:32:40', null, null, null, 'package_template.xml', '代码打包');
INSERT INTO `task` VALUES ('4', 'build_image', '', '0', '', '', '2017-06-22 15:32:40', '2017-06-22 15:32:40', null, null, null, 'build_template.xml', '构建镜像');
INSERT INTO `task` VALUES ('5', 'image_scan', '', '0', '', '', '2017-06-22 15:32:40', '2017-06-22 15:32:40', null, null, null, 'clair_template.xml', '镜像扫描');
INSERT INTO `task` VALUES ('6', 'push_image', '', '0', '', '', '2017-06-22 15:32:40', '2017-06-22 15:32:40', null, null, null, 'push_template.xml', '推送镜像');
INSERT INTO `task` VALUES ('7', 'check_out', '', '0', '', '', '2017-08-28 13:47:59', '2017-08-28 13:48:03', null, null, null, 'checkout_template.xml', '从svn检出代码');

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
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES ('1', '超级管理员', '-2cc3fe75eb672eeb60a6a80aa0565466506d9d1e', '超级管理员', 'xxx@beyondcent.com', '010-1313131', '18330033981', '2016-03-23 17:48:53', '2018-03-15 10:21:21', '1', '1', 'NORMAL', '0', null, null, 'admin', '', 'A8D8894027A8E99B8F4F31F397A2F519', '1', '1', '001', '1', '0', '0');

-- ----------------------------
-- Table structure for user_role
-- ----------------------------
DROP TABLE IF EXISTS `user_role`;
CREATE TABLE `user_role` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` bigint(20) DEFAULT NULL COMMENT '用户ID',
  `role_id` bigint(20) DEFAULT NULL COMMENT '角色ID',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of user_role
-- ----------------------------
INSERT INTO `user_role` VALUES ('1', '1', '1');


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
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of user_security
-- ----------------------------
INSERT INTO `user_security` VALUES ('1', '1', null, '5560a540-3e29-48c4-b36e-8d85aa8578cb', '1214546782', '1243538932');

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
  `creater_id` bigint(11) DEFAULT NULL COMMENT '创建者id',
  `gmt_create` datetime NOT NULL COMMENT '创建时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否被删除的标记（0代表未删除，1代表删除）',
  `dept_id` bigint(20) DEFAULT NULL COMMENT '所属部门',
  `props` varchar(1024) DEFAULT NULL COMMENT '其他属性',
  `env_id` bigint(11) DEFAULT NULL COMMENT '环境id',
  `labels` varchar(500) DEFAULT NULL COMMENT '节点标签',
  `policy` varchar(10) NOT NULL COMMENT 'pv回收策略：Retain、Recycle、Delete',
  `access` varchar(20) NOT NULL COMMENT 'accessModes：ReadWriteOnce、ReadOnlyMany、ReadWriteMany',
  `capacity` varchar(30) NOT NULL COMMENT 'pv容量',
  `type` varchar(30) NOT NULL COMMENT 'pv类型',
  `annotations` varchar(1024) DEFAULT NULL COMMENT '存储卷注解',
  `ip` varchar(20) DEFAULT NULL COMMENT 'nfs存储服务所在机器ip',
  `path` varchar(100) DEFAULT NULL COMMENT '存储卷存储路径',
  `monitors` varchar(255) DEFAULT NULL COMMENT 'ceph监控节点',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=48 DEFAULT CHARSET=utf8;


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
  `dept_id` bigint(20) DEFAULT NULL COMMENT '所属部门',
  `version` int(10) NOT NULL,
  `workflow_json` varchar(8192) NOT NULL COMMENT '存放页面流程图插件信息',
  `workflow_def` varchar(3072) NOT NULL COMMENT '存放workflowDef对象数据信息',
  `workflow_id` varchar(64) DEFAULT NULL COMMENT '工作流ID',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of workflow
-- ----------------------------
