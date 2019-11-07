package net.cyweb.task;

import com.alibaba.druid.support.json.JSONUtils;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import cyweb.utils.CoinConst;
import cyweb.utils.HttpRequest;
import net.cyweb.controller.ApiBaseController;
import net.cyweb.model.*;
import net.cyweb.service.*;
import org.apache.logging.log4j.core.util.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

@Configuration
@EnableScheduling
@EnableAsync
@Service
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

    private boolean isBuy = false; //是否挂的是买单

    private BigDecimal priceNow;

    private boolean upOrdownNow = true; //是上还是下 上 ture  下 false


    @Autowired
    UserProPs userProPs;

    @Async
    public void init()
    {
        this.notice();// 初始化通知
    }


    /**
     *  统计订单数据
     */




//    @Async
    public List<HashMap> docollectOrderInfos(YangCurrencyPair yangCurrencyPair)
    {
        String kes_buy = CoinConst.ORDER2PAIR+yangCurrencyPair.getCyId()+"_buy";

        String kes_sell = CoinConst.ORDER2PAIR+yangCurrencyPair.getCyId()+"_sell";

        List<HashMap> hashMaps =  this.yangOrderService.getOrderCollectInfos(yangCurrencyPair);

        List<String> sell = new LinkedList<>();
        List<String> buy = new LinkedList<>();

        redisService.multi();

        String kF;

        for (HashMap h:hashMaps
             ) {

            if(h.get("type").toString().equals("sell"))
            {
                sell.add(JSONObject.toJSONString(h));
            }else{
                buy.add(JSONObject.toJSONString(h));
            }
        }


        try {
            redisService.zclear(kes_buy);
            redisService.zclear(kes_sell);
        }catch (Exception e)
        {
            e.printStackTrace();
            redisService.discard();
        }

        redisService.lPushAll(kes_buy,buy);
        redisService.lPushAll(kes_sell,sell);

        redisService.exec();

        return hashMaps;

    }






    @Async
    public void notice()
    {

        try {
            while (true)
            {
                try {
                    doNotice();
                }catch (Exception e)
                {
                    e.printStackTrace();
                }

                Thread.sleep(10);

            }

        }catch (Exception e)
        {
            e.printStackTrace();
        }

    }


    @Async
    public int  doNotice() throws Exception
    {

        Object OrdersObject = redisService.lpop(CoinConst.TSORDERS);

        if(OrdersObject == null )
        {
            return 0;
        }

        int orders = (int)OrdersObject;

        try {

            if(orders == 0 )
            {
                return 0;
            }

            String sr = HttpRequest.sendPost(userProPs.getNotice().get("url")+"?orderId="+orders, "");

            return orders;
        }catch (Exception e)
        {
            e.printStackTrace();
        }
        return orders;
    }






}
