package net.cyweb;

import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import net.cyweb.CoinUtils.CoinEntry.Base.BaseCoin;
import net.cyweb.CoinUtils.CoinEntry.Base.BaseCoinI;
import net.cyweb.CoinUtils.CoinTools.CoinFactory;
import net.cyweb.model.YangCurrency;
import net.cyweb.service.CoinService;
import net.cyweb.service.RedisService;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.HttpMessageConverters;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.cloud.netflix.hystrix.dashboard.EnableHystrixDashboard;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Hello world!
 *
 */
@EnableTransactionManagement //开启事务
@ServletComponentScan //开启自动注解
@EnableEurekaClient //向Eureka注册
@EnableFeignClients
@EnableHystrix
@EnableHystrixDashboard
@SpringBootApplication
@EnableAsync
@EnableScheduling
@MapperScan(basePackages = "net.cyweb.mapper")
public class CoinApp
{

    public static void main( String[] args )
    {
        ConfigurableApplicationContext configurableApplicationContext = SpringApplication.run(CoinApp.class, args);
        listenAllCoin(configurableApplicationContext);
    }

    /**
     * 开启监听
     */
    public static void listenAllCoin(ConfigurableApplicationContext configurableApplicationContext)
    {
        CoinService coinService =  configurableApplicationContext.getBean(CoinService.class);
        RedisService redisService  =  configurableApplicationContext.getBean(RedisService.class);

        System.out.println("coinService:"+coinService);
        System.out.println("redisService:"+redisService);

        List<YangCurrency> list = coinService.getNeedListenCoin();
        System.out.println("待执行币种："+list);
        try {
            for (YangCurrency f:list
                    ) {
               BaseCoinI baseCoinI =  CoinFactory.createCoin(f,redisService);
                baseCoinI.chargingMoneylistener();

            }
        }catch (Exception e)
        {
            e.printStackTrace();
            BaseCoin.logger.error(e.getMessage());



        }

    }






}
