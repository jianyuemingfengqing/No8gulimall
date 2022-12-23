package com.learn.gmall.oms.mapper;

import com.learn.gmall.oms.entity.OrderItemEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单项信息
 * 
 * @author jianyueming
 * @email jianyueming99@gmail.com
 * @date 2022-12-23 13:15:41
 */
@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItemEntity> {
	
}
