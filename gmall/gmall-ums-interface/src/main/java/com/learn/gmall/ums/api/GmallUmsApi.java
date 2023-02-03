package com.learn.gmall.ums.api;

import com.learn.gmall.common.bean.ResponseVo;
import com.learn.gmall.ums.entity.UserAddressEntity;
import com.learn.gmall.ums.entity.UserEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface GmallUmsApi {

    @GetMapping("ums/user/query")
    ResponseVo<UserEntity> queryUser(
            @RequestParam("loginName") String loginName,
            @RequestParam("password") String password
    );

    @PostMapping("ums/user/register")
    ResponseVo register(UserEntity userEntity, @RequestParam("code") String code);

    @GetMapping("ums/user/check/{data}/{type}")
    ResponseVo<Boolean> checkData(
            @PathVariable("data") String data,
            @PathVariable("type") Integer type
    );

    @GetMapping("ums/useraddress/user/{userId}")
    ResponseVo<List<UserAddressEntity>> queryAddressesByUserId(
            @PathVariable("userId") Long userId
    );

    @GetMapping("ums/user/{id}")
    ResponseVo<UserEntity> queryUserById(@PathVariable("id") Long id);
}
