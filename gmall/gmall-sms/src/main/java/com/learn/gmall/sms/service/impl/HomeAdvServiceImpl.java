package com.learn.gmall.sms.service.impl;

import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.learn.gmall.common.bean.PageResultVo;
import com.learn.gmall.common.bean.PageParamVo;

import com.learn.gmall.sms.mapper.HomeAdvMapper;
import com.learn.gmall.sms.entity.HomeAdvEntity;
import com.learn.gmall.sms.service.HomeAdvService;


@Service("homeAdvService")
public class HomeAdvServiceImpl extends ServiceImpl<HomeAdvMapper, HomeAdvEntity> implements HomeAdvService {

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<HomeAdvEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<HomeAdvEntity>()
        );

        return new PageResultVo(page);
    }

}