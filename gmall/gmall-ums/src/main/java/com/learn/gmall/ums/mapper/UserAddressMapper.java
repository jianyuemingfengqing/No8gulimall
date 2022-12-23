package com.learn.gmall.ums.mapper;

import com.learn.gmall.ums.entity.UserAddressEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 收货地址表
 * 
 * @author jianyueming
 * @email jianyueming99@gmail.com
 * @date 2022-12-23 13:42:24
 */
@Mapper
public interface UserAddressMapper extends BaseMapper<UserAddressEntity> {
	
}
