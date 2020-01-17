/*
SQLyog Ultimate v10.00 Beta1
MySQL - 5.6.45 : Database - fuzhou
*********************************************************************
*/

/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
CREATE DATABASE /*!32312 IF NOT EXISTS*/`fuzhou` /*!40100 DEFAULT CHARACTER SET utf8 */;

USE `fuzhou`;

/*Table structure for table `a3` */

DROP TABLE IF EXISTS `a3`;

CREATE TABLE `a3` (
  `qxx` varchar(500) DEFAULT NULL COMMENT '客户全信息+门店',
  `sgdh` varchar(128) DEFAULT NULL COMMENT '手工单号',
  `cpbm` varchar(128) DEFAULT NULL COMMENT '产品编码',
  `cpqm` varchar(128) DEFAULT NULL COMMENT '产品全名',
  `xsbz` varchar(500) DEFAULT NULL COMMENT '销售备注',
  `xsmc` varchar(128) DEFAULT NULL COMMENT '销售名称',
  `zj` varchar(500) DEFAULT NULL COMMENT '（主键）产品+全信息+门店+手工单号+销售备注',
  `htsl` int(11) DEFAULT NULL COMMENT 'A3合同数量',
  `ftje` double DEFAULT NULL COMMENT 'A3分摊金额',
  `ddzpbj` double DEFAULT NULL COMMENT 'A3单定制品标价',
  `bdid` varchar(128) DEFAULT NULL COMMENT '表单id'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `a3_1` */

DROP TABLE IF EXISTS `a3_1`;

CREATE TABLE `a3_1` (
  `qxx` varchar(500) DEFAULT NULL COMMENT '客户全信息+门店',
  `sgdh` varchar(128) DEFAULT NULL COMMENT '手工单号',
  `cpbm` varchar(128) DEFAULT NULL COMMENT '产品编码',
  `cpqm` varchar(500) DEFAULT NULL COMMENT '产品全名',
  `xsbz` varchar(500) DEFAULT NULL COMMENT '销售备注',
  `zj` varchar(500) DEFAULT NULL COMMENT '（主键）产品+全信息+门店+手工单号+销售备注',
  `dhsl` int(11) DEFAULT NULL COMMENT 'A3.1订货数量',
  `thsl` int(11) DEFAULT NULL COMMENT 'A3.1退货数量（填负数）',
  `dhftje` double DEFAULT NULL COMMENT 'A3.1订货分摊金额',
  `thftje` double DEFAULT NULL COMMENT 'A3.1退货分摊金额',
  `ddzpbj` double DEFAULT NULL COMMENT 'A3.1单定制品标价',
  `bdid` varchar(128) DEFAULT NULL COMMENT '表单id',
  `xsmc` varchar(128) DEFAULT NULL COMMENT '销售名称'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `a4` */

DROP TABLE IF EXISTS `a4`;

CREATE TABLE `a4` (
  `zj` varchar(500) DEFAULT NULL COMMENT '（主键）产品+全信息+门店+手工单号+销售备注',
  `cgsl` int(11) DEFAULT NULL COMMENT '采购数量',
  `bdid` varchar(128) DEFAULT NULL COMMENT '表单id'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `a5` */

DROP TABLE IF EXISTS `a5`;

CREATE TABLE `a5` (
  `zj` varchar(500) DEFAULT NULL COMMENT '（主键）产品+全信息+门店+手工单号+销售备注',
  `sfsl` int(11) DEFAULT NULL COMMENT '实发数量',
  `sjxsje` double DEFAULT NULL COMMENT 'A5实际销售金额',
  `bdid` varchar(128) DEFAULT NULL COMMENT '表单id'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
