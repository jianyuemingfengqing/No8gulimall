package com.learn.gmall.auth;


import com.learn.gmall.common.utils.JwtUtils;
import com.learn.gmall.common.utils.RsaUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

public class JwtTest {

    // 别忘了创建D:\\project\rsa目录
	private static final String pubKeyPath = "F:\\useless\\rsa\\rsa.pub";
    private static final String priKeyPath = "F:\\useless\\rsa\\rsa.pri";

    private PublicKey publicKey;

    private PrivateKey privateKey;

    @Test
    public void testRsa() throws Exception {
        RsaUtils.generateKey(pubKeyPath, priKeyPath, "234");//234就是秘钥, 越复杂越好
    }

    @BeforeEach //没有秘钥就会报错
    public void testGetRsa() throws Exception {
        this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        this.privateKey = RsaUtils.getPrivateKey(priKeyPath);
    }

    @Test
    public void testGenerateToken() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("id", "11");
        map.put("username", "liuyan");
        // 生成token
        String token = JwtUtils.generateToken(map, privateKey, 3);
        System.out.println("token = " + token);
    }

    @Test
    public void testParseToken() throws Exception {
        String token = "eyJhbGciOiJSUzI1NiJ9.eyJpZCI6IjExIiwidXNlcm5hbWUiOiJsaXV5YW4iLCJleHAiOjE2NzQ5MTIxNTR9.LKfWle5kCM4KCwn7wr5zNpytjnAFZG5CvYvSQv7Ccuc3d9ntkQyGExkv1zeMfj4wYmlNYYdWccfH2auE_3qpFU6oGBG-tDT5qNNt6DhiNrrCLK8m-2o-4iMnnKH4TpDRxpaOqMDvG8Ml12yPA3jBt4b5ucmLClE9BBajNWUFPnb9pUvE1gacifSBUhjXmb9GHf1j5p2E4WpAObm4-Ac92aHyGiAGLGIloeIQiZCKn5Q2ISjzi4YkyTX6aHtW2OBQUkWJB6WxWBXEP-KWVTuaDqMKkVdMOERt01qPxqg6PE909DkNcF-kZfxsvyCH4QBy0rY2eE0cLcCKcTJ4Qhu-Xw";

        // 解析token
        Map<String, Object> map = JwtUtils.getInfoFromToken(token, publicKey);
        System.out.println("id: " + map.get("id"));
        System.out.println("userName: " + map.get("username"));
    }
}