package net.cyweb;

import com.alibaba.fastjson.JSONArray;
import cyweb.utils.CoinConst;
import net.cyweb.mapper.YangCurrencyPairMapper;
import net.cyweb.model.YangCurrencyPair;
import net.cyweb.service.RedisService;
import net.cyweb.task.*;
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
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.List;


@EnableAsync
@EnableTransactionManagement //开启事务
@ServletComponentScan //开启自动注解
@EnableEurekaClient //向Eureka注册
@SpringBootApplication
@EnableSwagger2
@MapperScan(basePackages = "net.cyweb.mapper")
public class OrderTradeMod
{


    private static ConfigurableApplicationContext configurableApplicationContext;

    public static void main( String[] args )
    {
         configurableApplicationContext = SpringApplication.run(OrderTradeMod.class, args);

    }
}


//@Component
//class OrderTradeModRunner implements ApplicationRunner {
//
//    @Autowired
//    OrderDealTask orderDealTask;
//
//    @Autowired
//    private ApplicationContext applicationContext;
//
//    @Autowired
//    private RedisService redisService;
//
//    @Autowired
//    YangCurrencyPairMapper yangCurrencyPairMapper;
//
//    @Override
//    @Transactional(rollbackFor = Exception.class)
//    public void run(ApplicationArguments applicationArguments) throws Exception {
//
//        List<YangCurrencyPair> list;
//        /*if(redisService.exists(CoinConst.ROBOT_CURRENCY_PAIR_REDIS)){
//            String s = JSONArray.toJSONString(redisService.get(CoinConst.ROBOT_CURRENCY_PAIR_REDIS));
//            System.out.println(s);
//            s=s.substring(1,s.length()-1);
//            list = JSONArray.parseArray(s,YangCurrencyPair.class);
//        }else{
//            //未读取到 配置  则 停止刷单
//            System.out.println("未读取到配置 退出 成单程序");
//            return;
//        }*/
//        YangCurrencyPair yangCurrencyPair= new YangCurrencyPair();
//        list = yangCurrencyPairMapper.select(yangCurrencyPair);
//        for (YangCurrencyPair y :list) {
//            OrderDealTask orderDealTask = applicationContext.getBean(OrderDealTask.class);
//            orderDealTask.init();
//            if (y.getIsMidRobot() == CoinConst.ROBOT_SHUA_TYPE_GUAANDMID) {
//                orderDealTask.deal(y);
//            }else{
//                orderDealTask.oldDeal(y);
//            }
//
//
////            OrderDealTask orderDealTask2 = applicationContext.getBean(OrderDealTask.class);
////            orderDealTask2.deal(y);
////            OrderDealTask orderDealTask3 = applicationContext.getBean(OrderDealTask.class);
////            orderDealTask3.deal(y);
////            OrderDealTask orderDealTask4 = applicationContext.getBean(OrderDealTask.class);
////            orderDealTask4.deal(y);
////            OrderDealTask orderDealTask5 = applicationContext.getBean(OrderDealTask.class);
////            orderDealTask5.deal(y);
//        }
////
////        /*****处理fince数据********/
////        OrderDealTask orderDealTask = applicationContext.getBean(OrderDealTask.class);
////
////
////        orderDealTask.init();
////        orderDealTask.deal();
//////
////        OrderDealTask orderDealTask1 = applicationContext.getBean(OrderDealTask.class);
////        OrderDealTask orderDealTask2 = applicationContext.getBean(OrderDealTask.class);
////        OrderDealTask orderDealTask3 = applicationContext.getBean(OrderDealTask.class);
////
////
////        orderDealTask1.deal();
////        orderDealTask2.deal();
////        orderDealTask3.deal();
//////
////        System.out.println("三个对象"+orderDealTask1+":"+orderDealTask2+":"+orderDealTask);
//
//
//        System.out.println("------------------------------------订单交易系统启动完成");
//    }
//
//
//}
