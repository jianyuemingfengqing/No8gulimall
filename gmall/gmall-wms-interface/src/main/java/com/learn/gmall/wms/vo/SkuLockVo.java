package com.learn.gmall.wms.vo;

import lombok.Data;

@Data
public class SkuLockVo {

    private Long skuId;
    private Integer count;
    private Boolean lock; // 是否锁定成功 true-锁定成功 false锁定失败
    private Long wareSkuId; // 锁定成功的情况下，记录锁定库存的id
}
