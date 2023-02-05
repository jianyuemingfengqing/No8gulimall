package com.learn.gmall.index.feign;

import com.learn.gmall.common.bean.ResponseVo;
import com.learn.gmall.pms.api.GmallPmsApi;
import com.learn.gmall.pms.entity.CategoryEntity;
import org.springframework.cloud.openfeign.FeignClient;

import java.util.List;


@FeignClient("pms-service")
public interface GmallPmsClient extends GmallPmsApi {
    ResponseVo<List<CategoryEntity>> queryCategoriesByPid(long l);
}