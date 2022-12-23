package com.learn.gmall.sms.mapper;

import com.learn.gmall.sms.entity.CouponEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 * 
 * @author jianyueming
 * @email jianyueming99@gmail.com
 * @date 2022-12-23 14:51:55
 */
@Mapper
public interface CouponMapper extends BaseMapper<CouponEntity> {
	
}
