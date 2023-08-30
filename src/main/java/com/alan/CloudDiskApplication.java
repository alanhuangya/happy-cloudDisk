package com.alan;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * SpringBoot启动类
 * @Author Alan
 * @Date 2023/8/28
 * @Version 1.0
 */
// 开启异步调用
@EnableAsync
// 启动类
@SpringBootApplication(scanBasePackages = "com.alan")
// 开启事务管理
@EnableTransactionManagement
// 开启定时任务
@EnableScheduling
public class CloudDiskApplication {
    public static void main(String[] args) {
        SpringApplication.run(CloudDiskApplication.class, args);
    }
}
