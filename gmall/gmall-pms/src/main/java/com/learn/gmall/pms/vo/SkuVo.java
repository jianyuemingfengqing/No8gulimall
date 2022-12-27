package com.learn.gmall.pms.vo;

import com.learn.gmall.pms.entity.SkuAttrValueEntity;
import com.learn.gmall.pms.entity.SkuEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class SkuVo extends SkuEntity {
    // defultimages
    private List<String> images;

    // 积分活动  sms SkuBoundsEntity
    private BigDecimal growBounds;
    private BigDecimal buyBounds;
    private List<Integer> work;

    // 满减活动  SkuLadderEntity
    private Integer fullCount;
    private BigDecimal discount;
    private Integer addOther;
    // SkuFullReductionEntity
    private BigDecimal fullPrice;
    private BigDecimal reducePrice;
    private Integer fullAddOther;

    private List<SkuAttrValueEntity> saleAttrs;
}
