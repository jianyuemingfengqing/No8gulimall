package com.learn.gmall.index.controller;

import com.learn.gmall.common.bean.ResponseVo;
import com.learn.gmall.index.service.IndexService;
import com.learn.gmall.pms.entity.CategoryEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class IndexController {

    @Autowired
    private IndexService indexService;

    @GetMapping("/**")
    public String toIndex(Model model, @RequestHeader(value = "userId", required = false) Long userId) {
        System.out.println("========================" + userId);
        //加载一级分类
        List<CategoryEntity> categoryEntities = this.indexService.queryLvl1Categories();
        model.addAttribute("categories", categoryEntities);

        // TODO: 加载其他数据

        return "index";
    }

    @GetMapping("index/cates/{pid}")
    @ResponseBody
    ResponseVo<List<CategoryEntity>> queryLevel23CategoriesByPid(@PathVariable("pid") Long pid) {
        //加载二,三级级分类
        List<CategoryEntity> categoryEntities = this.indexService.queryLevel23CategoriesByPid(pid);
        return ResponseVo.ok(categoryEntities);
    }
}