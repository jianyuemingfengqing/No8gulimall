package com.learn.gmall.index.service;


import com.alibaba.fastjson.JSON;
import com.learn.gmall.common.bean.ResponseVo;
import com.learn.gmall.index.feign.GmallPmsClient;
import com.learn.gmall.pms.entity.CategoryEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class IndexService {

    @Resource
    private GmallPmsClient pmsClient;

    private static final String key_prefix = "index:cates:"; //前缀
    @Autowired
    private StringRedisTemplate redisTemplate;

    public List<CategoryEntity> queryLvl1Categories() {
        ResponseVo<List<CategoryEntity>> listResponseVo = this.pmsClient.queryCategory(0L);
        return listResponseVo.getData();
    }

    public List<CategoryEntity> queryLevel23CategoriesByPid(Long pid) {
        // 设置缓存
        String json = this.redisTemplate.opsForValue().get(key_prefix + pid);// 参数为key, 结果为value
        if (StringUtils.isNotBlank(json)){
            return JSON.parseArray(json, CategoryEntity.class);
        }
        // 存到缓存
        List<CategoryEntity> data = this.pmsClient.queryLevel23CategoriesByPid(pid).getData();
        this.redisTemplate.opsForValue().set(key_prefix + pid, JSON.toJSONString(data));

        return data;
    }
}