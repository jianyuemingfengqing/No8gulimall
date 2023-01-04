package com.learn.gmall.pms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.learn.gmall.common.bean.PageParamVo;
import com.learn.gmall.common.bean.PageResultVo;
import com.learn.gmall.pms.entity.AttrEntity;
import com.learn.gmall.pms.entity.SpuAttrValueEntity;
import com.learn.gmall.pms.mapper.AttrMapper;
import com.learn.gmall.pms.mapper.SpuAttrValueMapper;
import com.learn.gmall.pms.service.SpuAttrValueService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;


@Service("spuAttrValueService")
public class SpuAttrValueServiceImpl extends ServiceImpl<SpuAttrValueMapper, SpuAttrValueEntity> implements SpuAttrValueService {
    @Resource
    private AttrMapper attrMapper;

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SpuAttrValueEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SpuAttrValueEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public List<SpuAttrValueEntity> querySearchAttrValuesByCidAndSpuId(Long cid, Long spuId) {
        // 根据分类id查询出销售类型的检索属性
        List<AttrEntity> attrEntities = this.attrMapper.selectList(
                new QueryWrapper<AttrEntity>()
                        .eq("category_id", cid)
                        .eq("search_type", 1)
                        .eq("type", 1)
        );
        if (CollectionUtils.isEmpty(attrEntities)) {
            return null;
        }
        // 获取规格参数id集合
        List<Long> attrIds = attrEntities.stream().map(AttrEntity::getId).collect(Collectors.toList());

        // 根据skuId查询销售类型的检索属性和值
        return this.list(
                new QueryWrapper<SpuAttrValueEntity>()
                        .eq("spu_id", spuId)
                        .in("attr_id", attrIds)
        );
    }
}