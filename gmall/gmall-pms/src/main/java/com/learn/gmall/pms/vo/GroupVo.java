package com.learn.gmall.pms.vo;

import com.learn.gmall.pms.entity.AttrEntity;
import com.learn.gmall.pms.entity.AttrGroupEntity;
import lombok.Data;

import java.util.List;

@Data
public class GroupVo extends AttrGroupEntity {
    private List<AttrEntity> attrEntities;
}
