package com.learn.gmall.pms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.learn.gmall.common.bean.PageParamVo;
import com.learn.gmall.common.bean.PageResultVo;
import com.learn.gmall.pms.entity.AttrEntity;
import com.learn.gmall.pms.mapper.AttrMapper;
import com.learn.gmall.pms.service.AttrService;
import org.springframework.stereotype.Service;

import java.util.List;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrMapper, AttrEntity> implements AttrService {

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<AttrEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<AttrEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public List<AttrEntity> queryAttrsByCid(Long cid, Integer type, Integer searchType) {
        QueryWrapper<AttrEntity> wrapper = new QueryWrapper();
        if (cid != 0) {
            wrapper.eq("category_id", cid);
        }
        if (type != null) {
            wrapper.eq("type", type);
        }
        if (searchType != null) {
            wrapper.eq("search_type", searchType);
        }

        return list(wrapper);
    }

}