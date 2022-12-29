package com.learn.gmall.pms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.learn.gmall.common.bean.PageParamVo;
import com.learn.gmall.common.bean.PageResultVo;
import com.learn.gmall.pms.entity.*;
import com.learn.gmall.pms.feign.GmallSmsClient;
import com.learn.gmall.pms.mapper.SkuMapper;
import com.learn.gmall.pms.mapper.SpuDescMapper;
import com.learn.gmall.pms.mapper.SpuMapper;
import com.learn.gmall.pms.service.*;
import com.learn.gmall.pms.vo.SkuVo;
import com.learn.gmall.pms.vo.SpuAttrValueVo;
import com.learn.gmall.pms.vo.SpuVo;
import com.learn.gmall.sms.vo.SkuSaleVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
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
        //参考上面的分页
        QueryWrapper<SpuEntity> wrapper = new QueryWrapper<>();
        if (categoryId != 0) {
            wrapper.eq("category_id", categoryId);
        }
        String key = paramVo.getKey(); //商品id 或者 商品名
        if (StringUtils.isNotBlank(key)) {//判断某字符串是否不为空且长度不为0且不由空白符(whitespace)构成
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
    public void bigSave(SpuVo spuVo) {
        //1. 保存 spu
        saveSpuInfo(spuVo);
        Long spuId = spuVo.getId(); // 获取新增后的spuId

        // 1.2. 保存pms_spu_desc

        descService.saveSpuDesc(spuVo, spuId);

        // 1.3. 保存pms_spu_attr_value
        saveBaseAttr(spuVo, spuId);


        //2.保存sku
        saveSkuInfo(spuVo, spuId);

    }


    private void saveSkuInfo(SpuVo spuVo, Long spuId) {
        List<SkuVo> skuVos = spuVo.getSkus();
        if (!CollectionUtils.isEmpty(skuVos)) {
            return;
        }
        //通过遍历
        skuVos.forEach(skuVo -> {
            skuVo.setSpuId(spuId);
            skuVo.setBrandId(skuVo.getBrandId());
            skuVo.setCategoryId(spuVo.getCategoryId());
            // sku的图片列表
            List<String> images = skuVo.getImages();
            if (!CollectionUtils.isEmpty(images)) {
                // 设置默认图片, 有默认图片就用默认图片, 没有就用第一个
                skuVo.setDefaultImage(
                        StringUtils.isBlank(skuVo.getDefaultImage()) ? images.get(0) : skuVo.getDefaultImage()
                );
            }
            this.skuMapper.insert(skuVo);
            Long skuId = skuVo.getId();

            // 2.2. 保存pms_sku_images
            if (!CollectionUtils.isEmpty(images)) {
                this.imagesService.saveBatch(
                        images.stream().map(
                                image -> {
                                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                                    skuImagesEntity.setSkuId(skuId);
                                    skuImagesEntity.setUrl(image);
                                    // 网络资源唯一, image存的是地址, 如果地址一直, 说明资源是同一个
                                    skuImagesEntity.setDefaultStatus(StringUtils.equals(skuVo.getDefaultImage(), image) ? 1 : 0);
                                    return skuImagesEntity;
                                }
                        ).collect(Collectors.toList())
                );
            }
            // 2.3. 保存pms_sku_attr_value
            List<SkuAttrValueEntity> saleAttrs = skuVo.getSaleAttrs();
            if (!CollectionUtils.isEmpty(saleAttrs)) {
                saleAttrs.forEach(
                        skuAttrValueEntity -> {
                            skuAttrValueEntity.setSkuId(skuId);
                        }
                );
                this.saleAttrService.saveBatch(saleAttrs);
            }
            // 3.保存营销
            // 使用远程接口  feign
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