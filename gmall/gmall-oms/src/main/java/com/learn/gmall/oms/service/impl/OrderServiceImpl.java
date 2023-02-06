package com.learn.gmall.oms.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.learn.gmall.common.bean.PageParamVo;
import com.learn.gmall.common.bean.PageResultVo;
import com.learn.gmall.common.bean.ResponseVo;
import com.learn.gmall.oms.entity.OrderEntity;
import com.learn.gmall.oms.entity.OrderItemEntity;
import com.learn.gmall.oms.feign.GmallPmsClient;
import com.learn.gmall.oms.mapper.OrderItemMapper;
import com.learn.gmall.oms.mapper.OrderMapper;
import com.learn.gmall.oms.service.OrderService;
import com.learn.gmall.oms.vo.OrderItemVo;
import com.learn.gmall.oms.vo.OrderSubmitVo;
import com.learn.gmall.pms.entity.*;
import com.learn.gmall.ums.entity.UserAddressEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderMapper, OrderEntity> implements OrderService {

    @Autowired
    private OrderItemMapper itemMapper;

    @Autowired
    private GmallPmsClient pmsClient;
    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<OrderEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<OrderEntity>()
        );

        return new PageResultVo(page);
    }

    @Transactional
    @Override
    public void saveOrder(OrderSubmitVo submitVo, Long userId) {
        // 1.新增订单
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setUserId(userId);
        orderEntity.setOrderSn(submitVo.getOrderToken());
        orderEntity.setCreateTime(new Date());
        orderEntity.setTotalAmount(submitVo.getTotalPrice());
        orderEntity.setPayAmount(submitVo.getTotalPrice());
        orderEntity.setPayType(submitVo.getPayType());
        orderEntity.setSourceType(0);
        orderEntity.setStatus(0);
        orderEntity.setDeliveryCompany(submitVo.getDeliveryCompany());
        UserAddressEntity address = submitVo.getAddress();
        if (address != null) {
            orderEntity.setReceiverAddress(address.getAddress());
            orderEntity.setReceiverCity(address.getCity());
            orderEntity.setReceiverName(address.getName());
            orderEntity.setReceiverPhone(address.getPhone());
            orderEntity.setReceiverPostCode(address.getPostCode());
            orderEntity.setReceiverProvince(address.getProvince());
            orderEntity.setReceiverRegion(address.getRegion());
        }
        orderEntity.setDeleteStatus(0);
        orderEntity.setUseIntegration(submitVo.getBounds());
        this.save(orderEntity);

        Long orderId = orderEntity.getId();

        // 2.新增订单详情
        List<OrderItemVo> items = submitVo.getItems();
        items.forEach(item -> {
            OrderItemEntity orderItemEntity = new OrderItemEntity();

            orderItemEntity.setOrderId(orderId);
            orderItemEntity.setOrderSn(submitVo.getOrderToken());
            // 获取skuId
            Long skuId = item.getSkuId();
            // 查询sku
            ResponseVo<SkuEntity> skuEntityResponseVo = this.pmsClient.querySkuById(skuId);
            SkuEntity skuEntity = skuEntityResponseVo.getData();
            if (skuEntity != null) {
                orderItemEntity.setSkuId(skuId);
                orderItemEntity.setSkuPrice(skuEntity.getPrice());
                orderItemEntity.setSkuName(skuEntity.getName());
                orderItemEntity.setSkuQuantity(item.getCount().intValue());
                orderItemEntity.setCategoryId(skuEntity.getCategoryId());
                orderItemEntity.setRealAmount(skuEntity.getPrice());
            }

            // 查询sku图片
            ResponseVo<List<SkuImagesEntity>> imagesResponseVo = this.pmsClient.queryImagesBySkuId(skuId);
            List<SkuImagesEntity> skuImagesEntities = imagesResponseVo.getData();
            orderItemEntity.setSkuPic(JSON.toJSONString(skuImagesEntities));

            // 销售属性
            ResponseVo<List<SkuAttrValueEntity>> saleAttrResponseVo = this.pmsClient.querySaleAttrValuesBySkuId(skuId);
            List<SkuAttrValueEntity> skuAttrValueEntities = saleAttrResponseVo.getData();
            orderItemEntity.setSkuAttrsVals(JSON.toJSONString(skuAttrValueEntities));

            Long spuId = skuEntity.getSpuId();
            ResponseVo<SpuEntity> spuEntityResponseVo = this.pmsClient.querySpuById(spuId);
            SpuEntity spuEntity = spuEntityResponseVo.getData();
            if (spuEntity != null) {
                orderItemEntity.setSpuId(spuId);
                orderItemEntity.setSpuName(spuEntity.getName());
            }

            ResponseVo<BrandEntity> brandEntityResponseVo = this.pmsClient.queryBrandById(skuEntity.getBrandId());
            BrandEntity brandEntity = brandEntityResponseVo.getData();
            orderItemEntity.setSpuBrand(JSON.toJSONString(brandEntity));

            ResponseVo<SpuDescEntity> spuDescEntityResponseVo = this.pmsClient.querySpuDescById(spuId);
            SpuDescEntity spuDescEntity = spuDescEntityResponseVo.getData();
            if (spuDescEntity != null) {
                orderItemEntity.setSpuPic(spuDescEntity.getDecript());
            }

            itemMapper.insert(orderItemEntity);
        });
//        int i = 1/0;
//        try {
//            TimeUnit.SECONDS.sleep(4);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }

}