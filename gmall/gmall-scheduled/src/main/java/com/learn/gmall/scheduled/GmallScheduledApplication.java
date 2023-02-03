package com.learn.gmall.scheduled;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.learn.gmall.scheduled.mapper")
public class GmallScheduledApplication {

    public static void main(String[] args) {
        SpringApplication.run(GmallScheduledApplication.class, args);
    }

}
