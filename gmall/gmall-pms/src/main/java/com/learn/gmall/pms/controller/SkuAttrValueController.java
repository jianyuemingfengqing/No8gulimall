package com.learn.gmall.pms.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.learn.gmall.common.bean.PageParamVo;
import com.learn.gmall.common.bean.PageResultVo;
import com.learn.gmall.common.bean.ResponseVo;
import com.learn.gmall.pms.entity.SkuAttrValueEntity;
import com.learn.gmall.pms.service.SkuAttrValueService;
import com.learn.gmall.pms.vo.SaleAttrValueVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * sku销售属性&值
 *
 * @author jianyueming
 * @email jianyueming99@gmail.com
 * @date 2022-12-23 14:53:08
 */
@Api(tags = "sku销售属性&值 管理")
@RestController
@RequestMapping("pms/skuattrvalue")
public class SkuAttrValueController {

    @Resource
    private SkuAttrValueService skuAttrValueService;

    @GetMapping("mapping/{spuId}")
    public ResponseVo<String> queryMappingBySpuId(@PathVariable("spuId")Long spuId){
        String json = this.skuAttrValueService.queryMappingBySpuId(spuId);
        return ResponseVo.ok(json);
    }

    @GetMapping("spu/{spuId}")
    public ResponseVo<List<SaleAttrValueVo>> querySaleAttrValuesBySpuId(@PathVariable("spuId")Long spuId){
        List<SaleAttrValueVo> saleAttrValueVos = this.skuAttrValueService.querySaleAttrValuesBySpuId(spuId);
        return ResponseVo.ok(saleAttrValueVos);
    }

    @GetMapping("sku/{skuId}")
    public ResponseVo<List<SkuAttrValueEntity>> querySaleAttrValuesBySkuId(@PathVariable("skuId")Long skuId){
        List<SkuAttrValueEntity> skuAttrValueEntities = this.skuAttrValueService.list(
                new QueryWrapper<SkuAttrValueEntity>().eq("sku_id", skuId)
        );
        return ResponseVo.ok(skuAttrValueEntities);
    }
    @GetMapping("search/attr/value/{cid}")
    public ResponseVo<List<SkuAttrValueEntity>> querySearchAttrValuesByCidAndSkuId(
            @PathVariable("cid") Long cid,
            @RequestParam("skuId") Long skuId
    ) {
        List<SkuAttrValueEntity> skuAttrValueEntities = this.skuAttrValueService.querySearchAttrValuesByCidAndSkuId(cid, skuId);
        return ResponseVo.ok(skuAttrValueEntities);
    }

    /**
     * 列表
     */
    @GetMapping
    @ApiOperation("分页查询")
    public ResponseVo<PageResultVo> querySkuAttrValueByPage(PageParamVo paramVo) {
        PageResultVo pageResultVo = skuAttrValueService.queryPage(paramVo);

        return ResponseVo.ok(pageResultVo);
    }


    /**
     * 信息
     */
    @GetMapping("{id}")
    @ApiOperation("详情查询")
    public ResponseVo<SkuAttrValueEntity> querySkuAttrValueById(@PathVariable("id") Long id) {
        SkuAttrValueEntity skuAttrValue = skuAttrValueService.getById(id);

        return ResponseVo.ok(skuAttrValue);
    }

    /**
     * 保存
     */
    @PostMapping
    @ApiOperation("保存")
    public ResponseVo<Object> save(@RequestBody SkuAttrValueEntity skuAttrValue) {
        skuAttrValueService.save(skuAttrValue);

        return ResponseVo.ok();
    }

    /**
     * 修改
     */
    @PostMapping("/update")
    @ApiOperation("修改")
    public ResponseVo update(@RequestBody SkuAttrValueEntity skuAttrValue) {
        skuAttrValueService.updateById(skuAttrValue);

        return ResponseVo.ok();
    }

    /**
     * 删除
     */
    @PostMapping("/delete")
    @ApiOperation("删除")
    public ResponseVo delete(@RequestBody List<Long> ids) {
        skuAttrValueService.removeByIds(ids);

        return ResponseVo.ok();
    }

}
