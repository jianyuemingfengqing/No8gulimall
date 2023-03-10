package com.learn.gmall.pms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.learn.gmall.common.bean.PageParamVo;
import com.learn.gmall.common.bean.PageResultVo;
import com.learn.gmall.pms.entity.SkuAttrValueEntity;
import com.learn.gmall.pms.entity.SkuImagesEntity;
import com.learn.gmall.pms.entity.SpuAttrValueEntity;
import com.learn.gmall.pms.entity.SpuEntity;
import com.learn.gmall.pms.feign.GmallSmsClient;
import com.learn.gmall.pms.mapper.SkuMapper;
import com.learn.gmall.pms.mapper.SpuMapper;
import com.learn.gmall.pms.service.*;
import com.learn.gmall.pms.vo.SkuVo;
import com.learn.gmall.pms.vo.SpuAttrValueVo;
import com.learn.gmall.pms.vo.SpuVo;
import com.learn.gmall.sms.vo.SkuSaleVo;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.FileNotFoundException;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


@Service("spuService")
public class SpuServiceImpl extends ServiceImpl<SpuMapper, SpuEntity> implements SpuService {

    @Resource
    private SpuAttrValueService baseService;
    @Resource
    private SkuMapper skuMapper;
    @Resource
    private SkuAttrValueService saleAttrService;
    @Resource
    private SkuImagesService imagesService;
    @Resource
    private GmallSmsClient smsClient;
    @Resource
    private SpuDescService descService;

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SpuEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SpuEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public PageResultVo querySpuInfo(PageParamVo paramVo, Long categoryId) {
        //?????????????????????
        QueryWrapper<SpuEntity> wrapper = new QueryWrapper<>();
        if (categoryId != 0) {
            wrapper.eq("category_id", categoryId);
        }
        String key = paramVo.getKey(); //??????id ?????? ?????????
        if (StringUtils.isNotBlank(key)) {//????????????????????????????????????????????????0??????????????????(whitespace)??????
            wrapper.and(
                    t -> t.like("id", key)
                            .or()
                            .like("name", key)
            );

        }
        IPage<SpuEntity> page = this.page(
                paramVo.getPage(),
                wrapper
        );

        return new PageResultVo(page);
    }

    @Override
    @GlobalTransactional
    public void bigSave(SpuVo spuVo)  {
        //1. ?????? spu
        Long spuId = saveSpuInfo(spuVo);
//        Long spuId = spuVo.getId(); // ??????????????????spuId


        // 1.2. ??????pms_spu_desc

        descService.saveSpuDesc(spuVo, spuId);
//        int i = 1 / 0; // ????????????, ?????????????????????

        // 1.3. ??????pms_spu_attr_value
        saveBaseAttr(spuVo, spuId);


        //2.??????sku
        saveSkuInfo(spuVo, spuId);

        // ?????????  ????????????es
        this.rabbitTemplate.convertAndSend("PMS.SPU.EXCHANGE","item.insert",spuId);// ??????, ??????

    }


    private void saveSkuInfo(SpuVo spuVo, Long spuId) {
        List<SkuVo> skuVos = spuVo.getSkus();
        if (!CollectionUtils.isEmpty(skuVos)) {
            return;
        }
        //????????????
        skuVos.forEach(skuVo -> {
            skuVo.setSpuId(spuId);
            skuVo.setBrandId(skuVo.getBrandId());
            skuVo.setCategoryId(spuVo.getCategoryId());
            // sku???????????????
            List<String> images = skuVo.getImages();
            if (!CollectionUtils.isEmpty(images)) {
                // ??????????????????, ?????????????????????????????????, ?????????????????????
                skuVo.setDefaultImage(
                        StringUtils.isBlank(skuVo.getDefaultImage()) ? images.get(0) : skuVo.getDefaultImage()
                );
            }
            this.skuMapper.insert(skuVo);
            Long skuId = skuVo.getId();

            // 2.2. ??????pms_sku_images
            if (!CollectionUtils.isEmpty(images)) {
                this.imagesService.saveBatch(
                        images.stream().map(
                                image -> {
                                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                                    skuImagesEntity.setSkuId(skuId);
                                    skuImagesEntity.setUrl(image);
                                    // ??????????????????, image???????????????, ??????????????????, ????????????????????????
                                    skuImagesEntity.setDefaultStatus(StringUtils.equals(skuVo.getDefaultImage(), image) ? 1 : 0);
                                    return skuImagesEntity;
                                }
                        ).collect(Collectors.toList())
                );
            }
            // 2.3. ??????pms_sku_attr_value
            List<SkuAttrValueEntity> saleAttrs = skuVo.getSaleAttrs();
            if (!CollectionUtils.isEmpty(saleAttrs)) {
                saleAttrs.forEach(
                        skuAttrValueEntity -> {
                            skuAttrValueEntity.setSkuId(skuId);
                        }
                );
                this.saleAttrService.saveBatch(saleAttrs);
            }
            // 3.????????????
            // ??????????????????  feign
            SkuSaleVo skuSaleVo = new SkuSaleVo();
            BeanUtils.copyProperties(skuVo, skuSaleVo);
            skuSaleVo.setSkuId(skuId);
            this.smsClient.saveSkuSaleInfo(skuSaleVo);
        });
    }

    private void saveBaseAttr(SpuVo spuVo, Long spuId) {
        List<SpuAttrValueVo> baseAttrs = spuVo.getBaseAttrs();
        if (!CollectionUtils.isEmpty(baseAttrs)) {
            this.baseService.saveBatch(
                    baseAttrs.stream().map(
                            spuAttrValueVo -> {
                                SpuAttrValueEntity spuAttrValueEntity = new SpuAttrValueEntity();
                                BeanUtils.copyProperties(spuAttrValueVo, spuAttrValueEntity);
                                spuAttrValueEntity.setSpuId(spuId);
                                return spuAttrValueEntity;
                            }
                    ).collect(Collectors.toList())
            );
        }
    }

    private Long saveSpuInfo(SpuVo spuVo) {
        spuVo.setCreateTime(new Date());
        spuVo.setUpdateTime(spuVo.getCreateTime());
        this.save(spuVo);
        return spuVo.getId();
    }

}