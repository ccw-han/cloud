package net.cyweb;

import net.cyweb.service.YangOrderService;
import net.cyweb.service.YangPairService;
import net.cyweb.task.OrderDealTask;
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
import org.springframework.transaction.annotation.Transactional;
import springfox.documentation.swagger2.annotations.EnableSwagger2;



@EnableAsync
@EnableTransactionManagement //开启事务
@ServletComponentScan //开启自动注解
@EnableEurekaClient //向Eureka注册
@SpringBootApplication
@EnableSwagger2
@MapperScan(basePackages = "net.cyweb.mapper")
public class OrderCancelMod
{


    private static ConfigurableApplicationContext configurableApplicationContext;

    public static void main( String[] args )
    {
         configurableApplicationContext = SpringApplication.run(OrderCancelMod.class, args);

    }
}


@Component
class OrderCancelModRunner implements ApplicationRunner {

    @Autowired
    OrderDealTask orderDealTask;

    @Autowired
    private ApplicationContext applicationContext;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void run(ApplicationArguments applicationArguments) throws Exception {


        //撤单方法 名称未做修改而已

        System.out.println("--------------------------撤单系统启动完成");


        OrderDealTask orderDealTask = applicationContext.getBean(OrderDealTask.class);
        orderDealTask.guadan();

        OrderDealTask orderDealTask1 = applicationContext.getBean(OrderDealTask.class);
        orderDealTask1.guadan();

        /***************test*****/
        //        int i = 0;
//
//
//            while (++i <=3)
//            {
//
//                this.test(i);
//                System.out.println("当前处理"+i);
//
//                Thread.sleep(5000);
//
//            }


    }


}
