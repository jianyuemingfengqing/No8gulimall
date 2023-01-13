package com.learn.gmall.pms.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.learn.gmall.common.bean.PageParamVo;
import com.learn.gmall.common.bean.PageResultVo;
import com.learn.gmall.pms.entity.AttrEntity;
import com.learn.gmall.pms.entity.SkuAttrValueEntity;
import com.learn.gmall.pms.entity.SkuEntity;
import com.learn.gmall.pms.mapper.AttrMapper;
import com.learn.gmall.pms.mapper.SkuAttrValueMapper;
import com.learn.gmall.pms.mapper.SkuMapper;
import com.learn.gmall.pms.service.SkuAttrValueService;
import com.learn.gmall.pms.vo.SaleAttrValueVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("skuAttrValueService")
public class SkuAttrValueServiceImpl extends ServiceImpl<SkuAttrValueMapper, SkuAttrValueEntity> implements SkuAttrValueService {

    @Resource
    private AttrMapper attrMapper;
    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private SkuAttrValueMapper attrValueMapper;

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

    @Override
    public List<SaleAttrValueVo> querySaleAttrValuesBySpuId(Long spuId) {
        // 1.根据spuId查询sku
        List<SkuEntity> skuEntities = this.skuMapper.selectList(
                new QueryWrapper<SkuEntity>().eq("spu_id", spuId)
        ); // 在sku表中用spu查

        if (CollectionUtils.isEmpty(skuEntities)) {
            return null;
        }
        // 获取skuId集合
        List<Long> skuIds = skuEntities.stream().map(SkuEntity::getId).collect(Collectors.toList());

        // 2.根据skuIds查询销售属性及值
        List<SkuAttrValueEntity> skuAttrValueEntities = this.list(
                new QueryWrapper<SkuAttrValueEntity>().in("sku_id", skuIds)
        );
        if (CollectionUtils.isEmpty(skuAttrValueEntities)) {
            return null;
        }

        // 3.把销售属性及值列表 转化成 List<SaleAttrValueVo>
        // [{attrId: 3, attrName: 机身颜色, attrValues: ['暗夜黑', '白天白']},
        //  {attrId: 4, attrName: 运行内存, attrValues: ['8G', '12G']},
        //  {attrId: 5, attrName: 机身存储, attrValues: ['128G', '256G']}]
        // 分组完成之后，以attrId作为key，以当前这组数据作为value
        Map<Long, List<SkuAttrValueEntity>> map = skuAttrValueEntities.stream()
                                                     .collect(Collectors.groupingBy(SkuAttrValueEntity::getAttrId));
        List<SaleAttrValueVo> saleAttrValueVos = new ArrayList<>();
        // 把每一个kv转化成SaleAttrValueVo对象
        map.forEach((attrId, attrValueEntities) -> {
            SaleAttrValueVo saleAttrValueVo = new SaleAttrValueVo();
            saleAttrValueVo.setAttrId(attrId);
            // 有该分组的情况下，该组下至少会有一个元素
            saleAttrValueVo.setAttrName(attrValueEntities.get(0).getAttrName());
            // 把该组数据集合 转化成 规格参数值的set集合  set 可以去重
            saleAttrValueVo.setAttrValues(
                    attrValueEntities.stream().map(SkuAttrValueEntity::getAttrValue).collect(Collectors.toSet())
            );
            saleAttrValueVos.add(saleAttrValueVo);
        });
        return saleAttrValueVos;
    }

    @Override
    public String queryMappingBySpuId(Long spuId) {
        // 1.根据spuId查询sku
        List<SkuEntity> skuEntities = this.skuMapper.selectList(new QueryWrapper<SkuEntity>().eq("spu_id", spuId));
        if (CollectionUtils.isEmpty(skuEntities)) {
            return null;
        }

        // 获取skuId集合
        List<Long> skuIds = skuEntities.stream().map(SkuEntity::getId).collect(Collectors.toList());

        // 2.根据skuids查询映射关系：{'暗夜黑,8G,128G': 100, '暗夜黑,8G,256G': 101}
        List<Map<String, Object>> maps = this.attrValueMapper.queryMappingBySkuIds(skuIds);
        if (CollectionUtils.isEmpty(maps)) {
            return null;
        }
        // 把map的list集合 转化成一个map集合
        Map<String, Long> mappingMap = maps.stream().collect(Collectors.toMap(map -> map.get("attr_values").toString(), map -> (Long) map.get("sku_id")));

        return JSON.toJSONString(mappingMap);
    }

}