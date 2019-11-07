package net.cyweb.controller;

import net.cyweb.CoinUtils.CoinEntry.Base.BaseCoin;
import net.cyweb.CoinUtils.CoinEntry.Base.BaseCoinI;
import net.cyweb.CoinUtils.CoinTools.CoinFactory;
import net.cyweb.model.YangCurrency;
import net.cyweb.service.CoinService;
import net.cyweb.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/coin")
@EnableScheduling
public class TaskController {

    @Autowired
    private  CoinService coinService;

    @Autowired
    private RedisService redisService;

    @Scheduled(cron = "0 0/5 * * * ?")
    public void safe()
    {
        List<YangCurrency> list = coinService.getNeedListenCoin();
        try {
            for (YangCurrency f:list
                    ) {
                BaseCoinI baseCoinI =  CoinFactory.createCoin(f,redisService);
                System.out.println("开始处理钱包到零钱包任务---当前对象"+f.getCurrencyId()+"-"+f.getBean());
                baseCoinI.safe();
            }
        }catch (Exception e)
        {
            e.printStackTrace();
            BaseCoin.logger.error(e.getMessage());
        }
    }

//    @Scheduled(cron = "0/1 * * * * ?")
    public void info()
    {
        List<YangCurrency> list = coinService.getNeedListenCoin();
        try {
            for (YangCurrency f:list
                    ) {
                BaseCoinI baseCoinI =  CoinFactory.createCoin(f,redisService);
                baseCoinI.chongzhiByTx(null);
            }
        }catch (Exception e)
        {
            e.printStackTrace();
            BaseCoin.logger.error(e.getMessage());
        }
    }



}
