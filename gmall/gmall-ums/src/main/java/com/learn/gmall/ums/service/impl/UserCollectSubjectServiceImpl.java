package com.learn.gmall.ums.service.impl;

import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.learn.gmall.common.bean.PageResultVo;
import com.learn.gmall.common.bean.PageParamVo;

import com.learn.gmall.ums.mapper.UserCollectSubjectMapper;
import com.learn.gmall.ums.entity.UserCollectSubjectEntity;
import com.learn.gmall.ums.service.UserCollectSubjectService;


@Service("userCollectSubjectService")
public class UserCollectSubjectServiceImpl extends ServiceImpl<UserCollectSubjectMapper, UserCollectSubjectEntity> implements UserCollectSubjectService {

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<UserCollectSubjectEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<UserCollectSubjectEntity>()
        );

        return new PageResultVo(page);
    }

}