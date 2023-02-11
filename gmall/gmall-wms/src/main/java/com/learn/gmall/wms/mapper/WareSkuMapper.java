package com.learn.gmall.wms.mapper;

import com.learn.gmall.wms.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品库存
 * 
 * @author jianyueming
 * @email jianyueming99@gmail.com
 * @date 2022-12-23 13:44:22
 */
@Mapper
public interface WareSkuMapper extends BaseMapper<WareSkuEntity> {


    List<WareSkuEntity> check(@Param("skuId") Long skuId, @Param("count") Integer count);

    Integer lock(@Param("id") Long id, @Param("count") Integer count);

    Integer unlock(@Param("id") Long id, @Param("count") Integer count);

    Integer minus(@Param("id") Long id, @Param("count") Integer count);
}
