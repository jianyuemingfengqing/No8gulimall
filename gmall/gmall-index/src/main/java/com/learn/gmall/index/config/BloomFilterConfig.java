package com.learn.gmall.index.config;


import com.learn.gmall.common.bean.ResponseVo;
import com.learn.gmall.index.feign.GmallPmsClient;
import com.learn.gmall.pms.entity.CategoryEntity;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Configuration
public class BloomFilterConfig {

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private GmallPmsClient pmsClient;

    private static final String KEY_PREFIX = "index:cates:";

    @Bean
    public RBloomFilter<String> bloomFilter(){
        // 初始化一个布隆过滤器
        RBloomFilter<String> bloomFilter = this.redissonClient.getBloomFilter("index:bf");
        bloomFilter.tryInit(2000, 0.03);
        // 向布隆过滤器中初始化数据
        // 三级分类:根据pid=0查询一级分类，并把一级分类的id放入布隆过滤器
        ResponseVo<List<CategoryEntity>> categoryResponseVo = this.pmsClient.queryCategoriesByPid(0l);
        List<CategoryEntity> categoryEntities = categoryResponseVo.getData();
        if (!CollectionUtils.isEmpty(categoryEntities)){
            categoryEntities.forEach(categoryEntity -> {
                bloomFilter.add(KEY_PREFIX + categoryEntity.getId());
            });
        }

        // TODO：广告

        return bloomFilter;
    }
}
