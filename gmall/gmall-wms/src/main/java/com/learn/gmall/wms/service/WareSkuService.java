package com.learn.gmall.wms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.learn.gmall.common.bean.PageResultVo;
import com.learn.gmall.common.bean.PageParamVo;
import com.learn.gmall.wms.entity.WareSkuEntity;
import com.learn.gmall.wms.vo.SkuLockVo;

import java.util.List;

/**
 * 商品库存
 *
 * @author jianyueming
 * @email jianyueming99@gmail.com
 * @date 2022-12-23 13:44:22
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageResultVo queryPage(PageParamVo paramVo);

    List<SkuLockVo> checkLock(List<SkuLockVo> lockVos, String orderToken);
}

