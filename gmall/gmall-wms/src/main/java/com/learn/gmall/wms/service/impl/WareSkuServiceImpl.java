package com.learn.gmall.wms.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.learn.gmall.common.bean.PageParamVo;
import com.learn.gmall.common.bean.PageResultVo;
import com.learn.gmall.common.exception.OrderException;
import com.learn.gmall.wms.entity.WareSkuEntity;
import com.learn.gmall.wms.mapper.WareSkuMapper;
import com.learn.gmall.wms.service.WareSkuService;
import com.learn.gmall.wms.vo.SkuLockVo;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuMapper, WareSkuEntity> implements WareSkuService {
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private WareSkuMapper wareSkuMapper;
    @Autowired
    private StringRedisTemplate redisTemplate;
    private static final String LOCK_PREFIX = "stock:lock:";
    private static final String KEY_PREFIX = "stock:info:";

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<WareSkuEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<WareSkuEntity>()
        );

        return new PageResultVo(page);
    }

    @Transactional
    @Override
    public List<SkuLockVo> checkLock(List<SkuLockVo> lockVos, String orderToken) {

        // 判断是否为空
        if (CollectionUtils.isEmpty(lockVos)) {
            throw new OrderException("请选择要购买的商品");
        }

        // 遍历所有商品，验库存并锁库存
        lockVos.forEach(lockVo -> {
            this.checkAndLock(lockVo);
        });

        // 如果存在锁定失败的库存，则所有锁定成功了的库存要解锁
        if (lockVos.stream().anyMatch(lockVo -> !lockVo.getLock())) {// 只要有一个锁定, 就解锁
            // 获取锁定成功的库存，并遍历解锁库存,
            lockVos.stream().filter(SkuLockVo::getLock)
                    .collect(Collectors.toList())
                    .forEach(
                            lockVo -> {
                                this.wareSkuMapper.unlock(lockVo.getWareSkuId(), lockVo.getCount());//mysql中所有写操作都有事务, 所以不用加锁
                            }
                    );
            return lockVos;
        }

        // 为了方便将来减库存 或者 解锁库存，把锁定信息缓存到redis
        this.redisTemplate.opsForValue()
                .set(KEY_PREFIX + orderToken, JSON.toJSONString(lockVos), 30, TimeUnit.HOURS);// 缓存时间, 到时间后关闭订单

        // 如果都锁定成功返回null
        return null;
    }

    private void checkAndLock(SkuLockVo lockVo) {

        // 通过分布式锁保证原子性
        RLock fairLock = this.redissonClient.getFairLock(LOCK_PREFIX + lockVo.getSkuId());
        fairLock.lock();

        try {
            // 1.验库存：查询库存
            List<WareSkuEntity> wareSkuEntities = this.wareSkuMapper.check(lockVo.getSkuId(), lockVo.getCount());
            if (CollectionUtils.isEmpty(wareSkuEntities)) {
                lockVo.setLock(false); // 锁定库存失败
                return;
            }

            // 2.锁库存：更新库存
            WareSkuEntity wareSkuEntity = wareSkuEntities.get(0); // 正常情况下，通过大数据分析接口找到成本最低，这里取第一个
            if (this.wareSkuMapper.lock(wareSkuEntity.getId(), lockVo.getCount()) == 1) { // =1 锁成功
                lockVo.setLock(true); // 锁定库存成功
                lockVo.setWareSkuId(wareSkuEntity.getId()); // 记录锁定库存的id
            } else {
                lockVo.setLock(false);
            }

        } finally {
            // 释放分布式锁
            fairLock.unlock();
        }
    }

}