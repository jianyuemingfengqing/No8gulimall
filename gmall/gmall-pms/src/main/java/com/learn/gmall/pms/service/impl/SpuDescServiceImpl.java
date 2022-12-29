package com.learn.gmall.pms.service.impl;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.learn.gmall.pms.vo.SpuVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.learn.gmall.common.bean.PageResultVo;
import com.learn.gmall.common.bean.PageParamVo;

import com.learn.gmall.pms.mapper.SpuDescMapper;
import com.learn.gmall.pms.entity.SpuDescEntity;
import com.learn.gmall.pms.service.SpuDescService;


@Service("spuDescService")
public class SpuDescServiceImpl extends ServiceImpl<SpuDescMapper, SpuDescEntity> implements SpuDescService {

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SpuDescEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SpuDescEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public void saveSpuDesc(SpuVo spuVo, Long spuId) {
        List<String> spuImages = spuVo.getSpuImages();
        if (!CollectionUtils.isEmpty(spuImages)) {
            SpuDescEntity spuDescEntity = new SpuDescEntity();
            spuDescEntity.setSpuId(spuId);
            spuDescEntity.setDecript(StringUtils.join(spuVo.getSpuImages(), ","));
            this.save(spuDescEntity);
        }
    }

}