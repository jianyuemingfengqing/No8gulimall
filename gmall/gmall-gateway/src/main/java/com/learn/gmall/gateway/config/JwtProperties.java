package com.learn.gmall.gateway.config;


import com.learn.gmall.common.utils.RsaUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.io.File;
import java.security.PrivateKey;
import java.security.PublicKey;

@ConfigurationProperties(prefix = "jwt")
@Data
@Slf4j
public class JwtProperties {

    private String pubKeyPath;
    private String cookieName;
    private String token;

    private PublicKey publicKey;

    @PostConstruct
    public void init(){
        try {
            // 读取公私钥对象
            this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("读取公钥失败！");
        }
    }
}
