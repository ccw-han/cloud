package net.cyweb;

import net.cyweb.mapper.YangCurrencyMapper;
import net.cyweb.mapper.YangCurrencyUserMapper;
import net.cyweb.mapper.YangMemberMapper;
import net.cyweb.mapper.YangOrdersMapper;
import net.cyweb.model.YangCurrency;
import net.cyweb.model.YangOrders;
import net.cyweb.service.RedisService;
import net.cyweb.service.YangOrderService;
import net.cyweb.task.OrderDealTask;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

@RunWith(SpringRunner.class)
@Profile(value = "dev")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TradeTest {
    @Autowired
    private RedisService redisService;

    @Autowired
    private OrderDealTask orderDealTask;

    @Autowired
    private YangOrdersMapper yangOrdersMapper;

    @Autowired
    private YangMemberMapper yangMemberMapper;

    @Autowired
    private YangCurrencyUserMapper yangCurrencyUserMapper;

    @Autowired
    private YangOrderService yangOrderService;

    @Autowired
    private YangCurrencyMapper yangCurrencyMapper;






    @Test
    public void redisTest(){
        Integer maxTimes = 50000000;
        Integer maxTimes2 = 50000000;

        System.out.println("塞入数据");
        while (maxTimes-- > 0){
            redisService.zAdd("test",new Random().nextInt(1000)+1,new Random().nextInt(1000)+1);
            if(maxTimes%10000 == 0){
                System.out.println(maxTimes);
            }

        }
        System.out.println("开始时间");
        long startTime = System.currentTimeMillis();
        Set<ZSetOperations.TypedTuple<Object>> set = redisService.rangeByScoreWithScores("test", 0, Double.MAX_VALUE);
        Iterator iterator = set.iterator();
        while (iterator.hasNext()) {
            ZSetOperations.TypedTuple<Object> next = (ZSetOperations.TypedTuple) iterator.next();
        }
        long endTime = System.currentTimeMillis();

        System.out.println( (endTime - startTime) );
        System.out.println("结束时间");


    }


    @Test
    public void deal(){

        double i = 100000000000000000000.0000123;
        double i1 = 100000000000000000000.00100123;

        BigDecimal.valueOf(i+i1);
        System.out.println(i+i1);


     }

     @Test
     @Transactional
    public void testTrans() throws RuntimeException
     {
         try {

             YangOrders yangOrders = new YangOrders();
             yangOrders.setOrdersId(25384222);
             yangOrders.setHasDo(0);
             int i = yangOrderService.updateByPrimaryKeySelective(yangOrders);
             System.out.println(i);

         }catch (Exception e)
         {

             e.printStackTrace();
         }
     }


}
