package com.learn.gmall.pms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.learn.gmall.common.bean.PageResultVo;
import com.learn.gmall.common.bean.PageParamVo;
import com.learn.gmall.pms.entity.SpuAttrValueEntity;

import java.util.List;

/**
 * spu属性值
 *
 * @author jianyueming
 * @email jianyueming99@gmail.com
 * @date 2022-12-23 14:53:08
 */
public interface SpuAttrValueService extends IService<SpuAttrValueEntity> {

    PageResultVo queryPage(PageParamVo paramVo);

    List<SpuAttrValueEntity> querySearchAttrValuesByCidAndSpuId(Long cid, Long spuId);
}

