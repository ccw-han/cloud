package net.cyweb;

import net.cyweb.mapper.YangCurrencyMapper;
import net.cyweb.model.YangCurrencyPair;
import net.cyweb.service.RedisService;
import net.cyweb.service.YangPairService;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

import static net.cyweb.controller.ApiBaseController.MQ_ORDER_QUEUE;

/**
 * Hello world!
 *
 */
@EnableAsync
@EnableTransactionManagement //开启事务
@ServletComponentScan //开启自动注解
@EnableEurekaClient //向Eureka注册
@SpringBootApplication
@EnableSwagger2
@MapperScan(basePackages = "net.cyweb.mapper")
public class UserApp
{
    public static void main( String[] args )
    {
       SpringApplication.run(UserApp.class, args);
    }
}

