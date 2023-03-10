package com.learn.gmall.sms.api;

import com.learn.gmall.common.bean.ResponseVo;
import com.learn.gmall.sms.vo.ItemSaleVo;
import com.learn.gmall.sms.vo.SkuSaleVo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface GmallSmsApi {
    @PostMapping("/sms/skubounds/skusale/save")
    ResponseVo<Object> saveSkuSaleInfo(@RequestBody SkuSaleVo skuSaleVo);

    @GetMapping("/sms/skubounds/sku/{skuId}")
    ResponseVo<List<ItemSaleVo>> querySalesBySkuId(@PathVariable("skuId") Long skuId);
}
