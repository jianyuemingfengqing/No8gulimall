package com.learn.gmall.sms.api;

import com.learn.gmall.common.bean.ResponseVo;
import com.learn.gmall.sms.vo.SkuSaleVo;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public interface GmallSmsApi {
    @PostMapping("/sms/skubounds/skusale/save")
    ResponseVo<Object> saveSkuSaleInfo(@RequestBody SkuSaleVo skuSaleVo);
}
