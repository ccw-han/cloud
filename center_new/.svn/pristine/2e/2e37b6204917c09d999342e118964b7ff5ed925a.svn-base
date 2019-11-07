package net.cyweb;

import com.alibaba.fastjson.JSONObject;
import cyweb.utils.CoinConst;
import net.cyweb.service.RedisService;
import net.cyweb.service.YangTradeService;
import net.cyweb.task.KlineHistoryTask;
import net.cyweb.task.KlineTask;
import net.cyweb.task.KlineTask1;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.concurrent.TimeUnit;

@EnableAsync
@EnableTransactionManagement //开启事务
@ServletComponentScan //开启自动注解
@EnableEurekaClient //向Eureka注册
@SpringBootApplication
@EnableSwagger2
@MapperScan(basePackages = "net.cyweb.mapper")
public class klinemongodb {
    private static ConfigurableApplicationContext configurableApplicationContext;

    public static void main( String[] args )
    {
        configurableApplicationContext = SpringApplication.run(klinemongodb.class, args);

    }
}

@Component
class KlineMongodbRunner implements ApplicationRunner {

    @Autowired
    private RedisService redisService;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private YangTradeService yangTradeService;

    @Override
    public void run(ApplicationArguments applicationArguments) throws Exception {

        //k线正向 更新数据
        KlineTask1 klineTask1=new KlineTask1(redisService,mongoTemplate,yangTradeService);
        Thread thread = new Thread(klineTask1);
        thread.start();
//
//        //        k线历史数据获取
//        KlineHistoryTask klineHistoryTask=new KlineHistoryTask(yangTradeService,mongoTemplate,redisService);
//        Thread thread1 = new Thread(klineHistoryTask);
//        thread1.start();
    }
}
