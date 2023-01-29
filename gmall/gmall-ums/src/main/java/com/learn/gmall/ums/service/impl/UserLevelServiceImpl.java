package com.learn.gmall.ums.service.impl;

import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.learn.gmall.common.bean.PageResultVo;
import com.learn.gmall.common.bean.PageParamVo;

import com.learn.gmall.ums.mapper.UserLevelMapper;
import com.learn.gmall.ums.entity.UserLevelEntity;
import com.learn.gmall.ums.service.UserLevelService;


@Service("userLevelService")
public class UserLevelServiceImpl extends ServiceImpl<UserLevelMapper, UserLevelEntity> implements UserLevelService {

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<UserLevelEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<UserLevelEntity>()
        );

        return new PageResultVo(page);
    }

}