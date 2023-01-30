
CREATE DATABASE guli_cart CHARSET utf8mb4;
USE guli_cart;

CREATE TABLE `cart_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` varchar(50) NOT NULL COMMENT '用户id或者userKey',#userkey是uuid也是字符串
  `sku_id` bigint(20) NOT NULL COMMENT 'skuId',
  `check` tinyint(4) NOT NULL COMMENT '选中状态',
  `title` varchar(255) NOT NULL COMMENT '标题',
  `default_image` varchar(255) DEFAULT NULL COMMENT '默认图片',
  `price` decimal(18,2) NOT NULL COMMENT '加入购物车时价格',
  `count` int(11) NOT NULL COMMENT '数量',
  `store` tinyint(4) NOT NULL COMMENT '是否有货',
  `sale_attrs` varchar(255) DEFAULT NULL COMMENT '销售属性（json格式）',
  `sales` varchar(255) DEFAULT NULL COMMENT '营销信息（json格式）',
  PRIMARY KEY (`id`),
  KEY `idx_uid_sid` (`user_id`,`sku_id`) #组合索引 顺序不能颠倒,需要用用户id查商品, 点到后就不能走索引, 速度慢
) ENGINE=InnoDB DEFAULT CHARSET=utf8;