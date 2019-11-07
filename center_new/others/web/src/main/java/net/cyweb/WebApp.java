package net.cyweb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.cloud.netflix.hystrix.dashboard.EnableHystrixDashboard;

/**
 * config server
 *
 */
@SpringBootApplication
@EnableEurekaClient //向Eureka注册
@EnableFeignClients
@EnableHystrix
@EnableHystrixDashboard
public class WebApp
{
    public static void main( String[] args )
    {
        SpringApplication.run(WebApp.class, args);
    }
}
