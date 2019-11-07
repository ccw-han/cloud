package com.example.commodityservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.ComponentScan;

/*
* 如果你的其他包都在使用了@SpringBootApplication注解的main app所在的包及其下级包，则你什么都不用做，SpringBoot会自动帮你把其他包都扫描了
如果你有一些bean所在的包，不在main app的包及其下级包，那么你需要手动加上@ComponentScan注解并指定那个bean所在的包
* */
@SpringBootApplication
@ComponentScan(basePackages = {"com.example.commodityservice.controller", "com.example.commodityservice.service","com.example.commodityservice.entity"})
//手动指定bean组件扫描范围
@EnableEurekaClient
public class CommodityserviceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CommodityserviceApplication.class, args);
    }

}
