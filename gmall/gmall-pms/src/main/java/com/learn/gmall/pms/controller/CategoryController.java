package com.learn.gmall.pms.controller;

import java.util.List;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.learn.gmall.pms.entity.CategoryEntity;
import com.learn.gmall.pms.service.CategoryService;
import com.learn.gmall.common.bean.PageResultVo;
import com.learn.gmall.common.bean.ResponseVo;
import com.learn.gmall.common.bean.PageParamVo;

/**
 * 商品三级分类
 *
 * @author jianyueming
 * @email jianyueming99@gmail.com
 * @date 2022-12-23 14:53:08
 */
@Api(tags = "商品三级分类 管理")
@RestController
@RequestMapping("pms/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 列表
     */
    @GetMapping
    @ApiOperation("分页查询")
    public ResponseVo<PageResultVo> queryCategoryByPage(PageParamVo paramVo){
        PageResultVo pageResultVo = categoryService.queryPage(paramVo);

        return ResponseVo.ok(pageResultVo);
    }
    @GetMapping("parent/{parentID}")
    @ApiOperation("根据父id查询分类")
    public ResponseVo<List<CategoryEntity>> queryCategory(@PathVariable("parentID") Long parentID){
        List<CategoryEntity> categoryEntityList =  categoryService.queryCategory(parentID);

        return ResponseVo.ok(categoryEntityList);
    }

    @GetMapping("level/23/{pid}")
    public ResponseVo<List<CategoryEntity>> queryLevel23CategoriesByPid(@PathVariable("pid") Long pid){
        List<CategoryEntity> categoryEntities = this.categoryService.queryLevel23CategoriesByPid(pid);
        return ResponseVo.ok(categoryEntities);
    }

    @GetMapping("lvl/123/{cid3}")//查询三级分类
    public ResponseVo<List<CategoryEntity>> queryLvl123CategoriesByCid3(@PathVariable("cid3")Long cid3){
        List<CategoryEntity> categoryEntities = this.categoryService.queryLvl123CategoriesByCid3(cid3);
        return ResponseVo.ok(categoryEntities);
    }

    /**
     * 信息
     */
    @GetMapping("{id}")
    @ApiOperation("详情查询")
    public ResponseVo<CategoryEntity> queryCategoryById(@PathVariable("id") Long id){
		CategoryEntity category = categoryService.getById(id);

        return ResponseVo.ok(category);
    }

    /**
     * 保存
     */
    @PostMapping
    @ApiOperation("保存")
    public ResponseVo<Object> save(@RequestBody CategoryEntity category){
		categoryService.save(category);

        return ResponseVo.ok();
    }

    /**
     * 修改
     */
    @PostMapping("/update")
    @ApiOperation("修改")
    public ResponseVo update(@RequestBody CategoryEntity category){
		categoryService.updateById(category);

        return ResponseVo.ok();
    }

    /**
     * 删除
     */
    @PostMapping("/delete")
    @ApiOperation("删除")
    public ResponseVo delete(@RequestBody List<Long> ids){
		categoryService.removeByIds(ids);

        return ResponseVo.ok();
    }

}
