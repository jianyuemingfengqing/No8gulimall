package com.learn.gmall.ums.controller;

import com.learn.gmall.common.bean.PageParamVo;
import com.learn.gmall.common.bean.PageResultVo;
import com.learn.gmall.common.bean.ResponseVo;
import com.learn.gmall.ums.entity.UserEntity;
import com.learn.gmall.ums.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 用户表
 *
 * @author jianyueming
 * @email jianyueming99@gmail.com
 * @date 2022-12-23 13:42:24
 */
@Api(tags = "用户表 管理")
@RestController
@RequestMapping("ums/user")
public class UserController {

    @Resource
    private UserService userService;


    // 数据校验  GET /ums/user/check/{data}/{type}
    @GetMapping("check/{data}/{type}")
    public ResponseVo<Boolean> checkData(
            @PathVariable("data") String data,
            @PathVariable("type") Integer type) {
        Boolean flag = userService.checkData(data, type);
        return ResponseVo.ok(flag);
    }

    //todo 发送短信 使用mq 发送异步请求

    // 注册  POST /ums/user/register
    @PostMapping("register")
    public ResponseVo register(UserEntity user,@RequestParam("code") String code){
        userService.register(user,code);
        return ResponseVo.ok();
    }

    // 登录
    @GetMapping("query")
    public ResponseVo<UserEntity> queryUser(
            @RequestParam("loginName") String loginName,
            @RequestParam("password") String password
    ) {
        UserEntity userEntity = this.userService.queryUser(loginName, password);
        return ResponseVo.ok(userEntity);
    }

    /**
     * 列表
     */
    @GetMapping
    @ApiOperation("分页查询")
    public ResponseVo<PageResultVo> queryUserByPage(PageParamVo paramVo) {
        PageResultVo pageResultVo = userService.queryPage(paramVo);

        return ResponseVo.ok(pageResultVo);
    }


    /**
     * 信息
     */
    @GetMapping("{id}")
    @ApiOperation("详情查询")
    public ResponseVo<UserEntity> queryUserById(@PathVariable("id") Long id) {
        UserEntity user = userService.getById(id);

        return ResponseVo.ok(user);
    }

    /**
     * 保存
     */
    @PostMapping
    @ApiOperation("保存")
    public ResponseVo<Object> save(@RequestBody UserEntity user) {
        userService.save(user);

        return ResponseVo.ok();
    }

    /**
     * 修改
     */
    @PostMapping("/update")
    @ApiOperation("修改")
    public ResponseVo update(@RequestBody UserEntity user) {
        userService.updateById(user);

        return ResponseVo.ok();
    }

    /**
     * 删除
     */
    @PostMapping("/delete")
    @ApiOperation("删除")
    public ResponseVo delete(@RequestBody List<Long> ids) {
        userService.removeByIds(ids);

        return ResponseVo.ok();
    }

}
