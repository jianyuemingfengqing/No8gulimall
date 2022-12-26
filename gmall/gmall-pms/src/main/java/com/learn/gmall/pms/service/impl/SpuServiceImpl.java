package com.learn.gmall.pms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.learn.gmall.common.bean.PageParamVo;
import com.learn.gmall.common.bean.PageResultVo;
import com.learn.gmall.pms.entity.SpuEntity;
import com.learn.gmall.pms.mapper.SpuMapper;
import com.learn.gmall.pms.service.SpuService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;


@Service("spuService")
public class SpuServiceImpl extends ServiceImpl<SpuMapper, SpuEntity> implements SpuService {

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SpuEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SpuEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public PageResultVo querySpuInfo(PageParamVo paramVo, Long categoryId) {
        QueryWrapper<SpuEntity> wrapper = new QueryWrapper<>();
        if (categoryId != 0) {
            wrapper.eq("category_id", categoryId);
        }
        String key = paramVo.getKey(); //商品id 或者 商品名
        if (StringUtils.isNotBlank(key)) {//判断某字符串是否不为空且长度不为0且不由空白符(whitespace)构成
            wrapper.and(
                    t -> t.like("id", key)
                            .or()
                            .like("name", key)
            );

        }

        return new PageResultVo(this.page(paramVo.getPage(), wrapper));
    }

}