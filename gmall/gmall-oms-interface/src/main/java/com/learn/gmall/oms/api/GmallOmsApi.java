package com.learn.gmall.oms.api;


import com.learn.gmall.common.bean.ResponseVo;
import com.learn.gmall.oms.vo.OrderSubmitVo;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public interface GmallOmsApi {

    @PostMapping("oms/order/save/{userId}")
    ResponseVo saveOrder(@RequestBody OrderSubmitVo submitVo, @PathVariable("userId")Long userId);
}
