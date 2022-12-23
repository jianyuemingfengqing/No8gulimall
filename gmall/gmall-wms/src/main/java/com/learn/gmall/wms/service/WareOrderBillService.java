package com.learn.gmall.wms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.learn.gmall.common.bean.PageResultVo;
import com.learn.gmall.common.bean.PageParamVo;
import com.learn.gmall.wms.entity.WareOrderBillEntity;

import java.util.Map;

/**
 * 库存工作单
 *
 * @author jianyueming
 * @email jianyueming99@gmail.com
 * @date 2022-12-23 13:44:22
 */
public interface WareOrderBillService extends IService<WareOrderBillEntity> {

    PageResultVo queryPage(PageParamVo paramVo);
}

