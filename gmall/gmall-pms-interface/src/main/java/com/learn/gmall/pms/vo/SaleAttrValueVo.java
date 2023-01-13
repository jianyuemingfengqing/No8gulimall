package com.learn.gmall.pms.vo;

import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class SaleAttrValueVo {

    private Long attrId;
    private String attrName;
//    private List<String> attrValues;
    private Set<String> attrValues;
}
