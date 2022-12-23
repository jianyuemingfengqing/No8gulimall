package com.learn.gmall.ums.mapper;

import com.learn.gmall.ums.entity.UserLevelEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员等级表
 * 
 * @author jianyueming
 * @email jianyueming99@gmail.com
 * @date 2022-12-23 13:42:24
 */
@Mapper
public interface UserLevelMapper extends BaseMapper<UserLevelEntity> {
	
}
