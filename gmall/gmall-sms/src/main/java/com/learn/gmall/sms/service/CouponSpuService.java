package com.learn.gmall.sms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.learn.gmall.common.bean.PageResultVo;
import com.learn.gmall.common.bean.PageParamVo;
import com.learn.gmall.sms.entity.CouponSpuEntity;

import java.util.Map;

/**
 * 优惠券与产品关联
 *
 * @author jianyueming
 * @email jianyueming99@gmail.com
 * @date 2022-12-23 14:51:55
 */
public interface CouponSpuService extends IService<CouponSpuEntity> {

    PageResultVo queryPage(PageParamVo paramVo);
}

