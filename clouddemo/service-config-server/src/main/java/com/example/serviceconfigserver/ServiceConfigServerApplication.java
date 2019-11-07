package com.example.serviceconfigserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

/**
 * 开启配置服务
 */
@EnableConfigServer
@SpringBootApplication
@EnableEurekaClient
public class ServiceConfigServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceConfigServerApplication.class, args);
    }
    /*
    *
    * /{application}/{profile}/[label]
    /{application}-{profile}.yml
    /{label}/{application}-{profile}.yml
    /{application}-{profile}.properties
    /{label}/{application}-{profile}.properties

    * */
    /*
    * 使用github webhook作主动推送，然后config server从推送的地址获取
    *
    * */
}
