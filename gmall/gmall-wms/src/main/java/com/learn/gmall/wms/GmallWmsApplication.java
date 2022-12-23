package com.learn.gmall.wms;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableSwagger2
@EnableFeignClients
@MapperScan("com.learn.gmall.wms.mapper")
public class GmallWmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(GmallWmsApplication.class, args);
    }

}
