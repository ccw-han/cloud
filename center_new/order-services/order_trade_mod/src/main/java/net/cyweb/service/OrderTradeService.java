package net.cyweb.service;

import net.cyweb.mapper.YangCurrencyPairMapper;
import net.cyweb.model.YangCurrencyPair;
import net.cyweb.task.OrderDealTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderTradeService {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private YangCurrencyPairMapper yangCurrencyPairMapper;

    /**
     * 每三十秒执行一次撮单系统
     */
    @Scheduled(cron="0/30 * * * * *")
    public void orderTraderMod(){
        YangCurrencyPair yangCurrencyPair = new YangCurrencyPair();
        OrderDealTask orderDealTask = applicationContext.getBean(OrderDealTask.class);

        orderDealTask.init();
        List<YangCurrencyPair> yangCurrencyPairs = yangCurrencyPairMapper.select(yangCurrencyPair);
        for (YangCurrencyPair currencyPair : yangCurrencyPairs) {
            orderDealTask.oldDeal(currencyPair);
        }
    }
}
