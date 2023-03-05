package com.learn.gmall.oms.service.impl;

import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.learn.gmall.common.bean.PageResultVo;
import com.learn.gmall.common.bean.PageParamVo;

import com.learn.gmall.oms.mapper.OrderSettingMapper;
import com.learn.gmall.oms.entity.OrderSettingEntity;
import com.learn.gmall.oms.service.OrderSettingService;


@Service("orderSettingService")
public class OrderSettingServiceImpl extends ServiceImpl<OrderSettingMapper, OrderSettingEntity> implements OrderSettingService {

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<OrderSettingEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<OrderSettingEntity>()
        );

        return new PageResultVo(page);
    }

}