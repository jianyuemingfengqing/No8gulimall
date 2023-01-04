package com.learn.gmall.pms.api;

import com.learn.gmall.common.bean.PageParamVo;
import com.learn.gmall.common.bean.PageResultVo;
import com.learn.gmall.common.bean.ResponseVo;
import org.springframework.web.bind.annotation.GetMapping;

public interface GmallPmsApi {
    @GetMapping("pms/spu")
    ResponseVo<PageResultVo> querySpuByPage(PageParamVo paramVo) ;
}
