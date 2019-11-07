package net.cyweb.task;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import cyweb.utils.CoinConst;
import net.cyweb.controller.ApiBaseController;
import net.cyweb.model.YangCurrencyPair;
import net.cyweb.service.RedisService;
import net.cyweb.service.YangPairService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;

@Configuration
@EnableScheduling
@EnableAsync
@Service
@Scope("prototype")
public class OrderDealTask2 extends ApiBaseController {

    @Autowired
    private RedisService redisService;

    @Autowired
    private YangPairService yangPairService;

    @Autowired
    OrderDealTask orderDealTask;

    @Async
    @Transactional(rollbackFor = Exception.class)
    public void guadan(Integer cyId,Integer type, Long sleep) throws Exception
    {
        List<YangCurrencyPair> list;

        while (true)
        {
            Thread.sleep(sleep);
            list= JSONArray.parseArray(JSONArray.toJSONString(redisService.get(CoinConst.ROBOT_CURRENCY_PAIR_REDIS)), YangCurrencyPair.class);

            Random random = new Random();
//                long timeSleep = random.nextInt(2000)+500;
            //遍历机器人Id
            YangCurrencyPair yangCurrencyPair=new YangCurrencyPair();
            for (YangCurrencyPair y :list) {
                if(y.getCyId().intValue()==cyId.intValue()){
                    yangCurrencyPair=y;
                    break;
                }
            }
            try {
                //如果是刷中间单
                if(type.intValue()==CoinConst.ROBOT_SHUA_TYPE_MID){
                    orderDealTask.midOrderShua(yangCurrencyPair,yangCurrencyPair.getCreateRate().multiply(new BigDecimal("1000")).longValue());
                }else if(type.intValue()==CoinConst.ROBOT_SHUA_TYPE_NORMAL){
                    //开始挂单
                    orderDealTask.guadanByOutSite(yangCurrencyPair,yangCurrencyPair.getCreateRate().multiply(new BigDecimal("1000")).longValue());
                }else if(type.intValue()==CoinConst.ROBOT_SHUA_TYPE_GUAANDMID){
                    //修正后的正常挂单模式
                    orderDealTask.guadanByModifyOutSite(yangCurrencyPair,yangCurrencyPair.getCreateRate().multiply(new BigDecimal("1000")).longValue());

                }
            }catch (Exception e)
            {
                e.printStackTrace();
            }

        }

    }

}
