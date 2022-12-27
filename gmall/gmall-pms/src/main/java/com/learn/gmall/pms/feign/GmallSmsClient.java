package com.learn.gmall.pms.feign;

import com.learn.gmall.common.bean.ResponseVo;
import com.learn.gmall.sms.api.GmallSmsApi;
import com.learn.gmall.sms.vo.SkuSaleVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("sms-service")
public interface GmallSmsClient extends GmallSmsApi {

}