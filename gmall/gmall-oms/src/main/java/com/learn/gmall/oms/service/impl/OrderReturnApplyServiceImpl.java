package com.learn.gmall.oms.service.impl;

import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.learn.gmall.common.bean.PageResultVo;
import com.learn.gmall.common.bean.PageParamVo;

import com.learn.gmall.oms.mapper.OrderReturnApplyMapper;
import com.learn.gmall.oms.entity.OrderReturnApplyEntity;
import com.learn.gmall.oms.service.OrderReturnApplyService;


@Service("orderReturnApplyService")
public class OrderReturnApplyServiceImpl extends ServiceImpl<OrderReturnApplyMapper, OrderReturnApplyEntity> implements OrderReturnApplyService {

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<OrderReturnApplyEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<OrderReturnApplyEntity>()
        );

        return new PageResultVo(page);
    }

}