package com.learn.gmall.ums.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.learn.gmall.common.bean.PageResultVo;
import com.learn.gmall.common.bean.PageParamVo;
import com.learn.gmall.ums.entity.UserCollectShopEntity;

import java.util.Map;

/**
 * 关注店铺表
 *
 * @author jianyueming
 * @email jianyueming99@gmail.com
 * @date 2022-12-23 13:42:24
 */
public interface UserCollectShopService extends IService<UserCollectShopEntity> {

    PageResultVo queryPage(PageParamVo paramVo);
}

