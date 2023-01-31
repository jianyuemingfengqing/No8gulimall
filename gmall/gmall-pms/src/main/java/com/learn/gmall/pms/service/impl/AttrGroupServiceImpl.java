package com.learn.gmall.pms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.learn.gmall.common.bean.PageParamVo;
import com.learn.gmall.common.bean.PageResultVo;
import com.learn.gmall.pms.entity.AttrEntity;
import com.learn.gmall.pms.entity.AttrGroupEntity;
import com.learn.gmall.pms.entity.SkuAttrValueEntity;
import com.learn.gmall.pms.entity.SpuAttrValueEntity;
import com.learn.gmall.pms.mapper.AttrGroupMapper;
import com.learn.gmall.pms.mapper.AttrMapper;
import com.learn.gmall.pms.mapper.SkuAttrValueMapper;
import com.learn.gmall.pms.mapper.SpuAttrValueMapper;
import com.learn.gmall.pms.service.AttrGroupService;
import com.learn.gmall.pms.vo.AttrValueVo;
import com.learn.gmall.pms.vo.GroupVo;
import com.learn.gmall.pms.vo.ItemGroupVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupMapper, AttrGroupEntity> implements AttrGroupService {

    @Resource
    private AttrMapper attrMapper;
    @Resource
    private SpuAttrValueMapper baseAttrValueMapper;//基础属性
    @Resource
    private SkuAttrValueMapper saleAttrValueMapper;//销售属性

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<AttrGroupEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public List<GroupVo> queryByCid(Long cid) {
        // 查分组
        List<AttrGroupEntity> attrGroupEntities = this.list(
                new QueryWrapper<AttrGroupEntity>().eq("category_id", cid)
        );

        List<GroupVo> groupVos = attrGroupEntities.stream().map(
                attrGroupEntity -> {
                    GroupVo groupVo = new GroupVo();
                    BeanUtils.copyProperties(attrGroupEntity, groupVo);

                    List<AttrEntity> attrEntities = attrMapper.selectList(
                            new QueryWrapper<AttrEntity>()
                                    .eq("group_id", attrGroupEntity.getId())
                                    .eq("type", 1)
                    );
                    groupVo.setAttrEntities(attrEntities);
                    return groupVo;
                }
        ).collect(Collectors.toList());

        return groupVos;
    }

    @Override
    public List<ItemGroupVo> queryGroupWithAttrValuesByCidAndSpuIdAndSkuId(Long cid, Long spuId, Long skuId) {
        //根据分类找分组
        List<AttrGroupEntity> attrGroupEntities = list(
                new QueryWrapper<AttrGroupEntity>().eq("category_id", cid)
        );
        if (CollectionUtils.isEmpty(attrGroupEntities)) {
            return null;
        }

        return attrGroupEntities.stream().map(attrGroupEntity -> {
            ItemGroupVo itemGroupVo = new ItemGroupVo();
            // 设置基本属性
            itemGroupVo.setId(attrGroupEntity.getId());
            itemGroupVo.setName(attrGroupEntity.getName());

            // 遍历数据
            // 用分组id查询参数
            List<AttrEntity> attrEntities = attrMapper.selectList(
                    new QueryWrapper<AttrEntity>().eq("group_id", attrGroupEntity.getId())
            );
            if (CollectionUtils.isEmpty(attrEntities)) {
                return itemGroupVo; // 没有, 就返回基础数据
            }

            // 获取参数id集合
            List<Long> attrIds = attrEntities.stream().map(AttrEntity::getId).collect(Collectors.toList());

            // 基本属性
            List<SpuAttrValueEntity> spuAttrValueEntities = this.baseAttrValueMapper.selectList(
                    new QueryWrapper<SpuAttrValueEntity>()
                            .in("attr_id", attrIds)
                            .eq("spu_id", spuId)
            );
            // 销售属性
            List<SkuAttrValueEntity> skuAttrValueEntities = this.saleAttrValueMapper.selectList(
                    new QueryWrapper<SkuAttrValueEntity>()
                            .in("attr_id", attrIds)
                            .eq("sku_id", skuId)
            );
            // 转为所需集合
            ArrayList<AttrValueVo> attrValueVos = new ArrayList<>();// 接收参数值

            // spu
            if (!CollectionUtils.isEmpty(spuAttrValueEntities)){
                attrValueVos.addAll(spuAttrValueEntities.stream().map(
                        spuAttrValueEntity -> {
                            AttrValueVo attrValueVo = new AttrValueVo();
                            BeanUtils.copyProperties(spuAttrValueEntity,attrValueVo);
                            return attrValueVo;
                        }
                ).collect(Collectors.toList()));
            }

            //sku
            if (!CollectionUtils.isEmpty(skuAttrValueEntities)){
                attrValueVos.addAll(skuAttrValueEntities.stream().map(
                        skuAttrValueEntity -> {
                            AttrValueVo attrValueVo = new AttrValueVo();
                            BeanUtils.copyProperties(skuAttrValueEntity,attrValueVo);
                            return attrValueVo;
                        }
                ).collect(Collectors.toList()));
            }

            itemGroupVo.setAttrValues(attrValueVos);//存储参数值
            return itemGroupVo;
        }).collect(Collectors.toList());
    }

}