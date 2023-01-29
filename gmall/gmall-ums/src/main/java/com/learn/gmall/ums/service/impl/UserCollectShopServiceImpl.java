package com.learn.gmall.ums.service.impl;

import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.learn.gmall.common.bean.PageResultVo;
import com.learn.gmall.common.bean.PageParamVo;

import com.learn.gmall.ums.mapper.UserCollectShopMapper;
import com.learn.gmall.ums.entity.UserCollectShopEntity;
import com.learn.gmall.ums.service.UserCollectShopService;


@Service("userCollectShopService")
public class UserCollectShopServiceImpl extends ServiceImpl<UserCollectShopMapper, UserCollectShopEntity> implements UserCollectShopService {

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<UserCollectShopEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<UserCollectShopEntity>()
        );

        return new PageResultVo(page);
    }

}