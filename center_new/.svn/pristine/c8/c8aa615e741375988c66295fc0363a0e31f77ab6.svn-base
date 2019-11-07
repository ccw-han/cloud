package net.cyweb;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import cyweb.utils.CoinConst;
import cyweb.utils.DateUtils;
import net.cyweb.model.YangCurrencyPair;
import net.cyweb.service.RedisService;
import net.cyweb.service.YangPairService;
import net.cyweb.task.CollectTask;
import net.cyweb.task.OrderDealTask;
import net.cyweb.task.OrderDealTask2;
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
import tk.mybatis.mapper.entity.Example;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;


@EnableAsync
@EnableTransactionManagement //开启事务
@ServletComponentScan //开启自动注解
@EnableEurekaClient //向Eureka注册
@SpringBootApplication
@EnableSwagger2
@MapperScan(basePackages = "net.cyweb.mapper")
public class OrderCreateMod
{


    private static ConfigurableApplicationContext configurableApplicationContext;

    public static void main( String[] args )
    {
         configurableApplicationContext = SpringApplication.run(OrderCreateMod.class, args);

    }
}


@Component
class OrderCreateModRunner implements ApplicationRunner {

    @Autowired
    OrderDealTask2 orderDealTask2;

    @Autowired
    OrderDealTask2 orderDealTask3;

    @Autowired
    private  YangPairService  yangPairService;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private CollectTask collectTask;

    @Autowired
    private RedisService redisService;

    @Override
    public void run(ApplicationArguments applicationArguments) throws Exception {

        List<YangCurrencyPair> list;
        if(redisService.exists(CoinConst.ROBOT_CURRENCY_PAIR_REDIS)){
                list= JSONArray.parseArray(JSONArray.toJSONString(redisService.get(CoinConst.ROBOT_CURRENCY_PAIR_REDIS)), YangCurrencyPair.class);
        }else{
            Example example = new Example(YangCurrencyPair.class);
            example.createCriteria().andIsNotNull("robotId");
            //查询所有机器人Id
            list = yangPairService.selectByExample(example);
            JSONArray ary=JSONArray.parseArray(JSON.toJSONString(list));
            redisService.set(CoinConst.ROBOT_CURRENCY_PAIR_REDIS,ary.toString());
            //未读取到 配置  则 停止刷单
            System.out.println("未读取到配置 退出 成单程序");
            return;
        }
        System.out.println("开始生成订单 "+ DateUtils.getNoDateDay(new Date(),"yyyy-MM-dd HH:mm:ss"));
        for (YangCurrencyPair y :list) {
            //中间单或者正常单
            if (y.getIsMidRobot() == CoinConst.ROBOT_SHUA_TYPE_MID ) {
                orderDealTask2.guadan(y.getCyId(),CoinConst.ROBOT_SHUA_TYPE_MID, y.getCreateRate().longValue() * 1000);
            }else if( y.getIsMidRobot() == CoinConst.ROBOT_SHUA_TYPE_NORMAL) {
                orderDealTask2.guadan(y.getCyId(),CoinConst.ROBOT_SHUA_TYPE_NORMAL, y.getCreateRate().longValue() * 1000*2);
            }else if (y.getIsMidRobot() == CoinConst.ROBOT_SHUA_TYPE_GUAANDMID) {
                //边挂单边刷中间单
                orderDealTask3.guadan(y.getCyId(),CoinConst.ROBOT_SHUA_TYPE_GUAANDMID, y.getCreateRate().longValue() * 1000);
            }

        }
        System.out.println("订单生成 一轮结束 "+ DateUtils.getNoDateDay(new Date(),"yyyy-MM-dd HH:mm:ss"));
        System.out.println("--------------------生成订单系统启动完成");

    }


}
