package com.learn.gmall.order.pojo;


import com.baomidou.mybatisplus.annotation.TableField;
import com.learn.gmall.pms.entity.SkuAttrValueEntity;
import com.learn.gmall.sms.vo.ItemSaleVo;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderItemVo {

    private Long skuId;
    private String defaultImage;
    private String title;
    private List<SkuAttrValueEntity> saleAttrs; // 销售属性
    private BigDecimal price;
    private BigDecimal count;
    private Boolean store = false; // 是否有货
    private List<ItemSaleVo> sales; // 营销信息
    private Integer weight;
}
