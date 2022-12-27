package com.learn.gmall.pms.vo;

import com.learn.gmall.pms.entity.SpuAttrValueEntity;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Data
public class SpuAttrValueVo extends SpuAttrValueEntity {
    private List<String> valueSelected;
    // 接收数据, 直接给attr value


    public void setValueSelected(List<Object> valueSelected) {
        if (CollectionUtils.isEmpty(valueSelected)){
            return;
        }
       this.setAttrValue(StringUtils.join(valueSelected, ","));
    }
}
