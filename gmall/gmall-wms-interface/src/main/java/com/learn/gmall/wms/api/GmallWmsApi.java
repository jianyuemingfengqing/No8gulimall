package com.learn.gmall.wms.api;

import com.learn.gmall.common.bean.ResponseVo;
import com.learn.gmall.wms.entity.WareSkuEntity;
import com.learn.gmall.wms.vo.SkuLockVo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface GmallWmsApi {

    @GetMapping("wms/waresku/sku/{skuId}")
    ResponseVo<List<WareSkuEntity>> queryWareSkusBySkuId(@PathVariable("skuId")Long skuId);

    @PostMapping("wms/waresku/check/lock/{orderToken}")
    ResponseVo<List<SkuLockVo>> checkLock(@RequestBody List<SkuLockVo> lockVos, @PathVariable("orderToken")String orderToken);
}
