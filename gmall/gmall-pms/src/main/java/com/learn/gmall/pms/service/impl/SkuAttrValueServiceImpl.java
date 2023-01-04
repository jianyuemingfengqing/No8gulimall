package com.learn.gmall.pms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.learn.gmall.common.bean.PageParamVo;
import com.learn.gmall.common.bean.PageResultVo;
import com.learn.gmall.pms.entity.AttrEntity;
import com.learn.gmall.pms.entity.SkuAttrValueEntity;
import com.learn.gmall.pms.mapper.AttrMapper;
import com.learn.gmall.pms.mapper.SkuAttrValueMapper;
import com.learn.gmall.pms.service.SkuAttrValueService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;


@Service("skuAttrValueService")
public class SkuAttrValueServiceImpl extends ServiceImpl<SkuAttrValueMapper, SkuAttrValueEntity> implements SkuAttrValueService {

    @Resource
    private AttrMapper attrMapper;

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SkuAttrValueEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SkuAttrValueEntity>()
        );

        return new PageResultVo(page);
    }


    @Override
    public List<SkuAttrValueEntity> querySearchAttrValuesByCidAndSkuId(Long cid, Long skuId) {
        // 根据分类id查询出销售类型的检索属性
        List<AttrEntity> attrEntities = this.attrMapper.selectList(
                new QueryWrapper<AttrEntity>()
                        .eq("category_id", cid)
                        .eq("search_type", 1).eq("type", 0)
        );
        if (CollectionUtils.isEmpty(attrEntities)) {
            return null;
        }
        // 获取规格参数id集合
        List<Long> attrIds = attrEntities.stream().map(AttrEntity::getId).collect(Collectors.toList());

        // 根据skuId查询销售类型的检索属性和值
        return this.list(
                new QueryWrapper<SkuAttrValueEntity>()
                        .eq("sku_id", skuId)
                        .in("attr_id", attrIds)
        );
    }

}