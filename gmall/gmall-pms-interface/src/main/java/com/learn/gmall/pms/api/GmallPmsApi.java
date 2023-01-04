package com.learn.gmall.pms.api;

import com.learn.gmall.common.bean.PageParamVo;
import com.learn.gmall.common.bean.PageResultVo;
import com.learn.gmall.common.bean.ResponseVo;
import com.learn.gmall.pms.entity.SpuEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface GmallPmsApi {
    @PostMapping("pms/spu/json")
    ResponseVo<List<SpuEntity>> querySpuByPageJson(@RequestBody PageParamVo paramVo);
}
