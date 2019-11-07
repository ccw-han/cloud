package net.cyweb;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import springfox.documentation.swagger2.annotations.EnableSwagger2;


@EnableAsync
@EnableTransactionManagement //开启事务
@ServletComponentScan //开启自动注解
@EnableEurekaClient //向Eureka注册
@SpringBootApplication
@EnableSwagger2
@MapperScan(basePackages = "net.cyweb.mapper")
public class platFormCurrencyServer
{


    private static ConfigurableApplicationContext configurableApplicationContext;

    public static void main( String[] args )
    {
         configurableApplicationContext = SpringApplication.run(platFormCurrencyServer.class, args);
    }
}

@Component
class platFormCurrencyRunner implements ApplicationRunner {
    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public void run(ApplicationArguments applicationArguments) throws Exception {
        //撤单任务
//        DealCancelOrderTask dealCancelOrderTask=applicationContext.getBean(DealCancelOrderTask.class);
//        Thread thread = new Thread(dealCancelOrderTask);
//        thread.start();
    }

}