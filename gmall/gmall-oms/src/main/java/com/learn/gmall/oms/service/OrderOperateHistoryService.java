package com.learn.gmall.oms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.learn.gmall.common.bean.PageResultVo;
import com.learn.gmall.common.bean.PageParamVo;
import com.learn.gmall.oms.entity.OrderOperateHistoryEntity;

import java.util.Map;

/**
 * 订单操作历史记录
 *
 * @author jianyueming
 * @email jianyueming99@gmail.com
 * @date 2022-12-23 13:15:41
 */
public interface OrderOperateHistoryService extends IService<OrderOperateHistoryEntity> {

    PageResultVo queryPage(PageParamVo paramVo);
}

