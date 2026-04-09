package com.arthaschan.metagptmall;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.arthaschan.metagptmall.mapper")
public class MetaGptMallApplication {
    public static void main(String[] args) {
        SpringApplication.run(MetaGptMallApplication.class, args);
    }
}
