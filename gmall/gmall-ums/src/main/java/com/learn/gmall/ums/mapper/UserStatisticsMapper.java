package com.learn.gmall.ums.mapper;

import com.learn.gmall.ums.entity.UserStatisticsEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 统计信息表
 * 
 * @author jianyueming
 * @email jianyueming99@gmail.com
 * @date 2022-12-23 13:42:24
 */
@Mapper
public interface UserStatisticsMapper extends BaseMapper<UserStatisticsEntity> {
	
}
