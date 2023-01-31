package com.learn.gmall.pms.api;

import com.learn.gmall.common.bean.PageParamVo;
import com.learn.gmall.common.bean.ResponseVo;
import com.learn.gmall.pms.entity.*;
import com.learn.gmall.pms.vo.ItemGroupVo;
import com.learn.gmall.pms.vo.SaleAttrValueVo;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface GmallPmsApi {
    // spu
    @PostMapping("pms/spu/json")
    ResponseVo<List<SpuEntity>> querySpuByPageJson(@RequestBody PageParamVo paramVo);

    @GetMapping("pms/spu/{id}")
    ResponseVo<SpuEntity> querySpuById(@PathVariable("id") Long id);

    // sku
    @GetMapping("pms/sku/spu/{spuId}")
    ResponseVo<List<SkuEntity>> list(@PathVariable("spuId") Long spuId);

    @GetMapping("pms/sku/{id}")
    ResponseVo<SkuEntity> querySkuById(@PathVariable("id") Long id);

    // brand
    @GetMapping("pms/brand/{id}")
    ResponseVo<BrandEntity> queryBrandById(@PathVariable("id") Long id);

    // category
    @GetMapping("pms/category/{id}")
    ResponseVo<CategoryEntity> queryCategoryById(@PathVariable("id") Long id);

    @GetMapping("pms/category/parent/{parentID}")
    ResponseVo<List<CategoryEntity>> queryCategory(@PathVariable("parentID") Long parentID);

    @GetMapping("pms/category/level/23/{pid}")
    ResponseVo<List<CategoryEntity>> queryLevel23CategoriesByPid(@PathVariable("pid") Long pid);

    @GetMapping("pms/category/lvl/123/{cid3}")//查询三级分类
    ResponseVo<List<CategoryEntity>> queryLvl123CategoriesByCid3(@PathVariable("cid3") Long cid3);

// 规格参数
    //sku attr
    @GetMapping("pms/skuattrvalue/search/attr/value/{cid}")
    ResponseVo<List<SkuAttrValueEntity>> querySearchAttrValuesByCidAndSkuId(
            @PathVariable("cid") Long cid,
            @RequestParam("skuId") Long skuId
    );
    @GetMapping("pms/skuattrvalue/sku/{skuId}")
    ResponseVo<List<SkuAttrValueEntity>> querySaleAttrValuesBySkuId(@PathVariable("skuId") Long skuId);

    @GetMapping("pms/skuattrvalue/mapping/{spuId}")
    ResponseVo<String> queryMappingBySpuId(@PathVariable("spuId") Long spuId);

    // spu attr
    @GetMapping("pms/skuattrvalue/spu/{spuId}")
    ResponseVo<List<SaleAttrValueVo>> querySaleAttrValuesBySpuId(@PathVariable("spuId") Long spuId);

    @GetMapping("pms/spuattrvalue/search/attr/value/{cid}")
    ResponseVo<List<SpuAttrValueEntity>> querySearchAttrValuesByCidAndSpuId(
            @PathVariable("cid") Long cid,
            @RequestParam("spuId") Long spuId
    );

    // 海报
    @GetMapping("pms/skuimages/sku/{skuId}")
    ResponseVo<List<SkuImagesEntity>> queryImagesBySkuId(@PathVariable("skuId") Long skuId);

    @GetMapping("pms/spudesc/{spuId}")
    ResponseVo<SpuDescEntity> querySpuDescById(@PathVariable("spuId") Long spuId);

    @GetMapping("pms/attrgroup/with/attr/value/{cid}")
    ResponseVo<List<ItemGroupVo>> queryGroupWithAttrValuesByCidAndSpuIdAndSkuId(
            @PathVariable("cid") Long cid, @RequestParam("spuId") Long spuId, @RequestParam("skuId") Long skuId
    );

}
