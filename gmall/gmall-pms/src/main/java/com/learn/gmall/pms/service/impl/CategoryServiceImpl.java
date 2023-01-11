package com.learn.gmall.pms.service.impl;

import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.learn.gmall.common.bean.PageResultVo;
import com.learn.gmall.common.bean.PageParamVo;

import com.learn.gmall.pms.mapper.CategoryMapper;
import com.learn.gmall.pms.entity.CategoryEntity;
import com.learn.gmall.pms.service.CategoryService;

import javax.annotation.Resource;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, CategoryEntity> implements CategoryService {

    @Resource
    private CategoryMapper categoryMapper;
    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<CategoryEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public List<CategoryEntity> queryCategory(Long parentID) {
        QueryWrapper<CategoryEntity> wrapper = new QueryWrapper<>();
        if (parentID != -1){
            wrapper.eq("parent_id",parentID);
        }
        return categoryMapper.selectList(wrapper);
    }

    @Override
    public List<CategoryEntity> queryLevel23CategoriesByPid(Long pid) {
        return this.categoryMapper.queryCategoriesByPid(pid);
    }

    @Override
    public List<CategoryEntity> queryLvl123CategoriesByCid3(Long cid3) {
        CategoryEntity categoryEntity3 = this.getById(cid3);//获取三级分类

        if (categoryEntity3 == null){
            return null;
        }

        CategoryEntity categoryEntity2 = this.getById(categoryEntity3.getParentId());//获取二级分类

        CategoryEntity categoryEntity1 = this.getById(categoryEntity2.getParentId());//获取一级分类

        return Arrays.asList(categoryEntity1, categoryEntity2, categoryEntity3);
    }
}