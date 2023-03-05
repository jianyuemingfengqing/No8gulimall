package com.learn.gmall.oms.service.impl;

import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.learn.gmall.common.bean.PageResultVo;
import com.learn.gmall.common.bean.PageParamVo;

import com.learn.gmall.oms.mapper.OrderOperateHistoryMapper;
import com.learn.gmall.oms.entity.OrderOperateHistoryEntity;
import com.learn.gmall.oms.service.OrderOperateHistoryService;


@Service("orderOperateHistoryService")
public class OrderOperateHistoryServiceImpl extends ServiceImpl<OrderOperateHistoryMapper, OrderOperateHistoryEntity> implements OrderOperateHistoryService {

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<OrderOperateHistoryEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<OrderOperateHistoryEntity>()
        );

        return new PageResultVo(page);
    }

}