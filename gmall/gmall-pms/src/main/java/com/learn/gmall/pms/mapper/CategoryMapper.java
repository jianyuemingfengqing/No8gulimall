package com.learn.gmall.pms.mapper;

import com.learn.gmall.pms.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品三级分类
 * 
 * @author jianyueming
 * @email jianyueming99@gmail.com
 * @date 2022-12-23 14:53:08
 */
@Mapper
public interface CategoryMapper extends BaseMapper<CategoryEntity> {
	
}
