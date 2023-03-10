package com.learn.gmall.wms.service.impl;

import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.learn.gmall.common.bean.PageResultVo;
import com.learn.gmall.common.bean.PageParamVo;

import com.learn.gmall.wms.mapper.WareMapper;
import com.learn.gmall.wms.entity.WareEntity;
import com.learn.gmall.wms.service.WareService;


@Service("wareService")
public class WareServiceImpl extends ServiceImpl<WareMapper, WareEntity> implements WareService {

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<WareEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<WareEntity>()
        );

        return new PageResultVo(page);
    }

}