package com.learn.gmall.pms.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.learn.gmall.common.bean.PageParamVo;
import com.learn.gmall.common.bean.PageResultVo;
import com.learn.gmall.common.bean.ResponseVo;
import com.learn.gmall.pms.entity.AttrEntity;
import com.learn.gmall.pms.service.AttrService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品属性
 *
 * @author jianyueming
 * @email jianyueming99@gmail.com
 * @date 2022-12-23 14:53:08
 */
@Api(tags = "商品属性 管理")
@RestController
@RequestMapping("pms/attr")
public class AttrController {

    @Autowired
    private AttrService attrService;

    /**
     * 列表
     */
    @GetMapping
    @ApiOperation("分页查询")
    public ResponseVo<PageResultVo> queryAttrByPage(PageParamVo paramVo) {
        PageResultVo pageResultVo = attrService.queryPage(paramVo);

        return ResponseVo.ok(pageResultVo);
    }

    @GetMapping("group/{gid}")
    @ApiOperation("查询组下的规格参数")
    public ResponseVo<List<AttrEntity>> queryAttrsByGid(@PathVariable("gid") Long gid) {
        List<AttrEntity> attrEntityList = attrService.list(
                new QueryWrapper<AttrEntity>().eq("group_id", gid)
        );
        return ResponseVo.ok(attrEntityList);
    }

    @GetMapping("category/{cid}")
    @ApiOperation("查询组下的规格参数")
    public ResponseVo<List<AttrEntity>> queryAttrsByCid(
            @PathVariable("cid") Long cid,
            @RequestParam(value = "type", required = false) Integer type,
            @RequestParam(value = "searchType", required = false) Integer searchType
    ) {
        List<AttrEntity> attrEntities = attrService.queryAttrsByCid(cid, type, searchType);
        return ResponseVo.ok(attrEntities);
    }

    /**
     * 信息
     */
    @GetMapping("{id}")
    @ApiOperation("详情查询")
    public ResponseVo<AttrEntity> queryAttrById(@PathVariable("id") Long id) {
        AttrEntity attr = attrService.getById(id);

        return ResponseVo.ok(attr);
    }

    /**
     * 保存
     */
    @PostMapping
    @ApiOperation("保存")
    public ResponseVo<Object> save(@RequestBody AttrEntity attr) {
        attrService.save(attr);

        return ResponseVo.ok();
    }

    /**
     * 修改
     */
    @PostMapping("/update")
    @ApiOperation("修改")
    public ResponseVo update(@RequestBody AttrEntity attr) {
        attrService.updateById(attr);

        return ResponseVo.ok();
    }

    /**
     * 删除
     */
    @PostMapping("/delete")
    @ApiOperation("删除")
    public ResponseVo delete(@RequestBody List<Long> ids) {
        attrService.removeByIds(ids);

        return ResponseVo.ok();
    }

}
