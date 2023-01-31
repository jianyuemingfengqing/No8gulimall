package com.learn.gmall.pms.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.learn.gmall.common.bean.PageParamVo;
import com.learn.gmall.common.bean.PageResultVo;
import com.learn.gmall.common.bean.ResponseVo;
import com.learn.gmall.pms.entity.AttrGroupEntity;
import com.learn.gmall.pms.service.AttrGroupService;
import com.learn.gmall.pms.vo.GroupVo;
import com.learn.gmall.pms.vo.ItemGroupVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 属性分组
 *
 * @author jianyueming
 * @email jianyueming99@gmail.com
 * @date 2022-12-23 14:53:08
 */
@Api(tags = "属性分组 管理")
@RestController
@RequestMapping("pms/attrgroup")
public class AttrGroupController {

    @Resource
    private AttrGroupService attrGroupService;

    @GetMapping("with/attr/value/{cid}")
    public ResponseVo<List<ItemGroupVo>> queryGroupWithAttrValuesByCidAndSpuIdAndSkuId(
            @PathVariable("cid")Long cid,
            @RequestParam("spuId")Long spuId,
            @RequestParam("skuId")Long skuId
    ) {
        List<ItemGroupVo> groupVos = this.attrGroupService.queryGroupWithAttrValuesByCidAndSpuIdAndSkuId(cid,spuId,skuId);
        return ResponseVo.ok(groupVos);
    }

    @GetMapping("withattrs/{catId}")
    @ApiOperation("查询分类下的组及规格参数")
    public ResponseVo<List<GroupVo>> queryByCid(@PathVariable("catId") Long cid) {
        List<GroupVo> groupVoList = attrGroupService.queryByCid(cid);
        return ResponseVo.ok(groupVoList);
    }

    /**
     * 列表
     */
    @GetMapping
    @ApiOperation("分页查询")
    public ResponseVo<PageResultVo> queryAttrGroupByPage(PageParamVo paramVo) {
        PageResultVo pageResultVo = attrGroupService.queryPage(paramVo);

        return ResponseVo.ok(pageResultVo);
    }

    @GetMapping("category/{cid}")
    @ApiOperation("查询三级分类的分组")
    public ResponseVo<List<AttrGroupEntity>> queryByCidPage(@PathVariable("cid") Long cid) {
        List<AttrGroupEntity> attrGroupEntityList = attrGroupService.list(
                new QueryWrapper<AttrGroupEntity>().eq("category_id", cid)
        );
        return ResponseVo.ok(attrGroupEntityList);
    }

    /**
     * 信息
     */
    @GetMapping("{id}")
    @ApiOperation("详情查询")
    public ResponseVo<AttrGroupEntity> queryAttrGroupById(@PathVariable("id") Long id) {
        AttrGroupEntity attrGroup = attrGroupService.getById(id);

        return ResponseVo.ok(attrGroup);
    }

    /**
     * 保存
     */
    @PostMapping
    @ApiOperation("保存")
    public ResponseVo<Object> save(@RequestBody AttrGroupEntity attrGroup) {
        attrGroupService.save(attrGroup);

        return ResponseVo.ok();
    }

    /**
     * 修改
     */
    @PostMapping("/update")
    @ApiOperation("修改")
    public ResponseVo update(@RequestBody AttrGroupEntity attrGroup) {
        attrGroupService.updateById(attrGroup);

        return ResponseVo.ok();
    }

    /**
     * 删除
     */
    @PostMapping("/delete")
    @ApiOperation("删除")
    public ResponseVo delete(@RequestBody List<Long> ids) {
        attrGroupService.removeByIds(ids);

        return ResponseVo.ok();
    }

}
