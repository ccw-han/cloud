package net.cyweb;

import net.cyweb.mapper.YangCurrencyMapper;
import net.cyweb.model.YangCurrencyPair;
import net.cyweb.service.RedisService;
import net.cyweb.service.YangPairService;
import net.cyweb.task.CollectTask;
import net.cyweb.task.OrderDealTask;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
import tk.mybatis.mapper.entity.Example;

import java.util.HashMap;
import java.util.List;


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
public class Rebot
{


    private static ConfigurableApplicationContext configurableApplicationContext;

    public static void main( String[] args )
    {
         configurableApplicationContext = SpringApplication.run(Rebot.class, args);

    }
}


@Component
class OrderDealRunner implements ApplicationRunner {

    @Autowired
    OrderDealTask orderDealTask;

    @Autowired
    private  YangPairService  yangPairService;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private CollectTask collectTask;



    @Override
    public void run(ApplicationArguments applicationArguments) throws Exception {


        /*****处理fince数据********/
        OrderDealTask orderDealTask = applicationContext.getBean(OrderDealTask.class);
//        orderDealTask.notice();// 初始化通知
//        orderDealTask.init();

        Example example = new Example(YangCurrencyPair.class);
        List<YangCurrencyPair> list =  yangPairService.selectByExample(example);

        while (true)
        {
            long startTime;
            long endTime;
            long startTimeTotal;
            long endTimeTotal;
            startTimeTotal =  System.currentTimeMillis();
            for (YangCurrencyPair y:list) {

                startTime = System.currentTimeMillis();
                List<HashMap> list1 =  orderDealTask.docollectOrderInfos(y);
                endTime = System.currentTimeMillis();
                System.out.println("交易对"+y.getCurrencyMark()+" cid"+y.getCyId()+": 耗时"+(endTime-startTime)/1000);
            }
            endTimeTotal  = System.currentTimeMillis();
            System.out.println("处理完成一次,总计耗时"+ (endTimeTotal - startTimeTotal)/1000 );
            Thread.sleep(10000);
        }

    }
}
