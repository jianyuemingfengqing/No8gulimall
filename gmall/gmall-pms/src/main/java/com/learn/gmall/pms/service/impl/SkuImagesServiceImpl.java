package com.learn.gmall.pms.service.impl;

import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.learn.gmall.common.bean.PageResultVo;
import com.learn.gmall.common.bean.PageParamVo;

import com.learn.gmall.pms.mapper.SkuImagesMapper;
import com.learn.gmall.pms.entity.SkuImagesEntity;
import com.learn.gmall.pms.service.SkuImagesService;


@Service("skuImagesService")
public class SkuImagesServiceImpl extends ServiceImpl<SkuImagesMapper, SkuImagesEntity> implements SkuImagesService {

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SkuImagesEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SkuImagesEntity>()
        );

        return new PageResultVo(page);
    }

}