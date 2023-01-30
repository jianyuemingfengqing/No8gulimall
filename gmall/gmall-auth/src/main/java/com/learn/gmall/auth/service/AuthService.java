package com.learn.gmall.auth.service;

import com.learn.gmall.auth.config.JwtProperties;
import com.learn.gmall.auth.feign.GmallUmsClient;
import com.learn.gmall.common.bean.ResponseVo;
import com.learn.gmall.common.exception.AuthException;
import com.learn.gmall.common.utils.CookieUtils;
import com.learn.gmall.common.utils.IpUtils;
import com.learn.gmall.common.utils.JwtUtils;
import com.learn.gmall.ums.entity.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;

@EnableConfigurationProperties(JwtProperties.class)
@Service
public class AuthService {

    @Resource
    private GmallUmsClient gmallUmsClient;
    @Resource
    private JwtProperties properties;
    public void accredit(String loginName, String password, HttpServletRequest request, HttpServletResponse response) {
        try {
            // 查询用户
            ResponseVo<UserEntity> userEntityResponseVo = gmallUmsClient.queryUser(loginName, password);
            UserEntity userEntity = userEntityResponseVo.getData();
            //  若为空  抛出异常
            if (userEntity == null) {
                throw new AuthException("用户名或密码错误");
            }
            // 不为空继续执行
            //封装数据
            HashMap<String, Object> map = new HashMap<>();
            map.put("userId", userEntity.getId());
            map.put("username", userEntity.getUsername());

            // 为了防止盗用加入登录用户的ip地址
            String ip = IpUtils.getIpAddressAtService(request);
            map.put("ip", ip);

            //生成jwt
            String token = JwtUtils.generateToken(map, this.properties.getPrivateKey(), this.properties.getExpire());
            // 存储
            CookieUtils.setCookie(request, response, this.properties.getCookieName(), token, this.properties.getExpire() * 60);
            // 为了方便展示登录效果，把昵称放入cookie中
            CookieUtils.setCookie(request, response, this.properties.getUnick(), userEntity.getNickname(), this.properties.getExpire() * 60);
        } catch (Exception e) {
            e.printStackTrace();
            // 可能是网络延迟,等问题
            throw new AuthException("用户名或密码错误");

        }
    }
}
