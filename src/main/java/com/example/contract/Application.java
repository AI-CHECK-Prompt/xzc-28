package com.example.contract;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 电子合同存证平台启动类
 * 支持多链架构的电子合同存证系统，实现签约过程实时固证、区块链分布式存储、司法链路直通验真
 */
@SpringBootApplication
@EnableCaching
@EnableAsync
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}