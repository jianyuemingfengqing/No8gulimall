package com.learn.gmall.pms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.learn.gmall.common.bean.PageParamVo;
import com.learn.gmall.common.bean.PageResultVo;
import com.learn.gmall.pms.entity.AttrEntity;
import com.learn.gmall.pms.entity.AttrGroupEntity;
import com.learn.gmall.pms.mapper.AttrGroupMapper;
import com.learn.gmall.pms.mapper.AttrMapper;
import com.learn.gmall.pms.service.AttrGroupService;
import com.learn.gmall.pms.vo.GroupVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupMapper, AttrGroupEntity> implements AttrGroupService {

    @Resource
    AttrMapper attrMapper;

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

}