package net.cyweb.task;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import cyweb.utils.CoinConst;
import cyweb.utils.HttpRequest;
import net.cyweb.controller.ApiBaseController;
import net.cyweb.mapper.YangCurrencyPairMapper;
import net.cyweb.model.*;
import net.cyweb.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

@Configuration
@EnableScheduling
@EnableAsync
@Service
@Scope("prototype")
public class OrderDealTask extends ApiBaseController {

    @Value("${notice.url}")
    private String noticeUrl;

    private static final Integer maxRetryTimes = 5*3;

    @Autowired
    private RedisService redisService;

    @Autowired
    private YangOrderService yangOrderService;

    @Autowired
    private RobotService robotService;

    @Autowired
    private YangMemberService yangMemberService;

    @Autowired
    private YangPairService yangPairService;

    private volatile boolean isBuy = false; //是否挂的是买单

    private volatile BigDecimal priceNow;

    private volatile boolean upOrdownNow = true; //是上还是下 上 ture  下 false


    @Autowired
    UserProPs userProPs;

    /**
     *  处理买单卖单
     * cron是用来指定执行的 秒，分钟，日期等
     */
   //@Scheduled(cron="0/30 * * * * *")

    /**
     *  初始化全部数据
     */
    @Async
    public void init()
    {
        //查询订单表里  hasdo 不为1  且  为部分成交和 未成交状态的 订单值
        long size = redisService.listSize(MQ_ORDER_QUEUE);
        System.out.println("开始清理key");
        redisService.remove(MQ_ORDER_QUEUE);

        List<String> statusList = Lists.newArrayList();
        statusList.add("0");statusList.add("1");
        Example example = new Example(YangOrders.class);
        example.createCriteria()
//                .andEqualTo("currencyId",yangCurrencyPair.getCurrencyId())
//                .andEqualTo("currencyTradeId",yangCurrencyPair.getCurrencyTradeId())
//                .andNotEqualTo("hasDo",1)
                .andIn("status",statusList);

        example.orderBy("orders_id asc");

        List<YangOrders> list = yangOrderService.selectByExample(example);

        for (YangOrders y:list){
            //redisService.rPush(MQ_ORDER_QUEUE,y.getOrdersId());
            redisService.lPush(MQ_ORDER_QUEUE,y.getOrdersId());
        }
        System.out.println("初始化成功"+list.size()+"条数据");

//        this.notice();// 初始化通知

//        this.doInsertFinance(); //初始化财务入账

    }

    @Async
    public void deal(YangCurrencyPair yangCurrencyPair){

        //首先清楚队列中的数据 重新初始花
        Object ordersId = null;
        while (true){
            try{
                if(yangCurrencyPair.getYangCurrencyPairExtendMidOrder()!=null&&yangCurrencyPair.getYangCurrencyPairExtendMidOrder().getTradeRate()!=null){
                    Thread.sleep(yangCurrencyPair.getYangCurrencyPairExtendMidOrder().getTradeRate().longValue()*1000);
                }
                if(redisService.listSize(MQ_ORDER_QUEUE+"_"+yangCurrencyPair.getCurrencyId()+"_"+yangCurrencyPair.getCurrencyTradeId()) > 0)
                {
                    System.out.println(yangCurrencyPair.getCurrencyId()+"_"+yangCurrencyPair.getCurrencyTradeId()+"当前队列下有【"+redisService.listSize(MQ_ORDER_QUEUE+"_"+yangCurrencyPair.getCurrencyId()+"_"+yangCurrencyPair.getCurrencyTradeId())+"】条订单需要处理");
                }
                ordersId =  redisService.lpop(MQ_ORDER_QUEUE+"_"+yangCurrencyPair.getCurrencyId()+"_"+yangCurrencyPair.getCurrencyTradeId());

                /*当前队列中，没有挂单*/
                if(null == ordersId){
                    Thread.sleep(10);
                }else{
                     this.doDeal((int)ordersId);
                }

            }catch (Exception e){
                e.printStackTrace();
            }finally {
                if(ordersId!=null)
                {
                    this.signOrderFininshed(Integer.valueOf((int)ordersId));

                }

            }

        }
    }

    public void oldDeal(YangCurrencyPair yangCurrencyPair){

        //首先清楚队列中的数据 重新初始花
        Object ordersId = null;
        while (true){
            try{
                if(yangCurrencyPair.getYangCurrencyPairExtendMidOrder()!=null&&yangCurrencyPair.getYangCurrencyPairExtendMidOrder().getTradeRate()!=null){
                    Thread.sleep(yangCurrencyPair.getYangCurrencyPairExtendMidOrder().getTradeRate().longValue()*1000);
                }
                /*if(redisService.listSize(MQ_ORDER_QUEUE) > 0)
                {
                    System.out.println(yangCurrencyPair.getCurrencyId()+"_"+yangCurrencyPair.getCurrencyTradeId()+"当前队列下有【"+redisService.listSize(MQ_ORDER_QUEUE)+"】条订单需要处理");
                }*/
                //ordersId =  redisService.lpop(MQ_ORDER_QUEUE);
                ordersId = redisService.rPop(MQ_ORDER_QUEUE);
                /*当前队列中，没有挂单*/
                if(null == ordersId){
                    //Thread.sleep(10);
                    return;
                }else{
                    this.doDeal((int)ordersId);
                }

            }catch (Exception e){
                e.printStackTrace();
            }finally {
                if(ordersId!=null)
                {
                    this.signOrderFininshed(Integer.valueOf((int)ordersId));

                }

            }

        }
    }

    @Async
    public void doDeal(int ordersId) throws Exception
    {

//        Thread.sleep(10000);
        long startTime = System.currentTimeMillis();
        YangOrders yangOrdersQuery = new YangOrders();
        yangOrdersQuery.setOrdersId((int) ordersId);

        YangOrders yangOrdersFind = yangOrderService.selectOne(yangOrdersQuery);

        yangOrderService.deal(yangOrdersFind);

        System.out.println("当前处理订单："+((int) ordersId));

        //标记订单完成
        this.signOrderFininshed(yangOrdersFind.getOrdersId());

        long endTime = System.currentTimeMillis();
        System.out.println("处理用时："+(endTime-startTime)+"ms");


    }


    /**
     * 标记订单已经完成
     */
    private  void signOrderFininshed(Integer orderId)
    {
        YangOrders yangOrders2 = new YangOrders();
        yangOrders2.setOrdersId(orderId);
        yangOrders2.setHasDo(Integer.valueOf(1));
        yangOrderService.updateByPrimaryKeySelective(yangOrders2);

    }

    class YangOrdersAddTime implements Comparator<YangOrders>{
        @Override
        public int compare(YangOrders e1, YangOrders e2) {
            if(e1.getAddTime().intValue() - e2.getAddTime().intValue()<0){
                return 1;
            } else if(e1.getAddTime().intValue()==e2.getAddTime().intValue()){
                if(e1.getOrdersId().intValue()-e2.getOrdersId().intValue()<0){
                    return 1;
                }else{
                    return -1;
                }

            }else{
                return -1;
            }
        }
    }


    public static  void  main(String[] args)
    {
        Random random = new Random();
        int r = random.nextInt(5);

        int upOrDown = random.nextInt(2); //如果是0 则是涨 如果是1 则是跌
        System.out.println(upOrDown);
    }
}
