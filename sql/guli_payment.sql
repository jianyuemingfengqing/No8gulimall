CREATE DATABASE /*!32312 IF NOT EXISTS */`guli_payment` /*!40100 DEFAULT CHARACTER SET utf8mb4 */;

USE `guli_payment`;
CREATE TABLE `payment_info`
(
    `id`               BIGINT(20) NOT NULL AUTO_INCREMENT,
    `out_trade_no`     VARCHAR(64)    DEFAULT NULL COMMENT '商户订单号',
    `payment_type`     TINYINT(4)     DEFAULT NULL COMMENT '支付类型（微信与支付宝）',
    `trade_no`         VARCHAR(64)    DEFAULT NULL COMMENT '支付宝交易凭证号',
    `total_amount`     DECIMAL(18, 4) DEFAULT NULL COMMENT '订单金额。订单中获取',
    `subject`          VARCHAR(100)   DEFAULT NULL COMMENT '交易内容。利用商品名称拼接。',
    `payment_status`   TINYINT(4)     DEFAULT NULL COMMENT '支付状态，默认值0-未支付，1-已支付。',
    `create_time`      DATETIME       DEFAULT NULL COMMENT '创建时间',
    `callback_time`    DATETIME       DEFAULT NULL COMMENT '回调时间，初始为空，支付宝异步回调时记录',
    `callback_content` TEXT COMMENT '回调信息，初始为空，支付宝异步回调时记录',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8 COMMENT ='支付对账表';