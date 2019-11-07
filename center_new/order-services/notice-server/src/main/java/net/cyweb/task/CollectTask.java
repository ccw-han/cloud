package net.cyweb.task;

import cyweb.utils.CoinConst;
import net.cyweb.mapper.YangCurrencyPairMapper;
import net.cyweb.model.YangCollectConfig;
import net.cyweb.model.YangCurrencyPair;
import net.cyweb.service.FeiXiaoHaoCollecterService;
import net.cyweb.service.RedisService;
import net.cyweb.service.YangCollectConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

@Configuration
@EnableScheduling
@EnableAsync
@Service
@Scope("prototype")
public class CollectTask {

    @Autowired
    YangCollectConfigService yangCollectConfigService;

    @Autowired
    YangCurrencyPairMapper yangCurrencyPairMapper;

    @Autowired
    RedisService redisService;

    @Autowired
    FeiXiaoHaoCollecterService feiXiaoHaoCollecterService;




    public void init()
    {
        //获取所有需要采集的交易对的信息


        Example example = new Example(YangCollectConfig.class);
        example.createCriteria().andEqualTo("status",1);
        List<YangCollectConfig> list =  yangCollectConfigService.selectByExample(example);
        for (YangCollectConfig y :list
                ) {
            switch (y.getCollectType().intValue())
            {
                case 2 :
                    try {
                        feiXiaoHaoCollecterService.initBaseInfo(y);
                        feiXiaoHaoCollecterService.doCollect();
                    }catch (Exception e)
                    {
                        e.printStackTrace();
                    }
//                  break;
            }
        }

        Example example1 = new Example(YangCurrencyPair.class);
        example1.createCriteria().andIsNotNull("robotId");
        List<YangCurrencyPair> list1 = yangCurrencyPairMapper.selectByExample(example1);
        for (YangCurrencyPair yc:list1
             ) {


            try {
//                System.out.println("currency"+yc.getCurrencyId()+"  tradid"+yc.getCurrencyTradeId());
//                if(yc.getCurrencyTradeId().equals(Integer.valueOf(37)) && yc.getCurrencyId().equals(Integer.valueOf(39)))
//                {
//                    System.out.println(yc);
//                }
                //根据采集的krw设置价格String key = CURRENCYPRICEBYCOLLECT+this.yangCollectConfig.getCid();redisService.set(key,priceFinalKrw);

                //1
                Object currencyKrw = redisService.get(CoinConst.CURRENCYPRICEBYCOLLECT+yc.getCurrencyId());
                if(currencyKrw == null)
                {
                    continue;
                }
                BigDecimal price_sd;
                if(yc.getCurrencyTradeId().equals(Integer.valueOf(0)))
                {
                    BigDecimal currencyIdPrice = BigDecimal.valueOf(Double.valueOf(currencyKrw.toString()));
                    //格式化金额 限制小数位数
                     price_sd   =   currencyIdPrice.setScale(yc.getInputPriceNum(),   BigDecimal.ROUND_DOWN);
                }else{
                    Object currencyThreadKre = redisService.get(CoinConst.CURRENCYPRICEBYCOLLECT+yc.getCurrencyTradeId());
                    if(currencyKrw == null || currencyThreadKre == null)
                    {
                        continue;
                    }
                    BigDecimal currencyIdPrice = BigDecimal.valueOf(Double.valueOf(currencyKrw.toString()));
                    BigDecimal tradePrice = BigDecimal.valueOf(Double.valueOf(currencyThreadKre.toString()));

                    price_sd = currencyIdPrice.divide(tradePrice, yc.getInputPriceNum(), BigDecimal.ROUND_DOWN);  //获取相对金额
                    //格式化金额 限制小数位数
                    price_sd   =   price_sd.setScale(yc.getInputPriceNum(),   BigDecimal.ROUND_DOWN);
                }



                yc.setBasePrice(price_sd);

                yangCurrencyPairMapper.updateByPrimaryKeySelective(yc);

            }catch (Exception e)
            {
                e.printStackTrace();
            }


        }



    }



    /**
     * 开始采集
     */


    @Async
    public void doCollect()
    {
        while (true)
        {
            try {
                this.init();
                Thread.sleep(20);
            }catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

}
