package net.cyweb.config.custom;

import cyweb.utils.CoinConst;
import cyweb.utils.DecimalUtils;
import net.cyweb.mapper.YangCurrencyMapper;
import net.cyweb.mapper.YangTradeMapper;
import net.cyweb.model.YangCurrency;
import net.cyweb.model.YangTrade;
import net.cyweb.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.List;

/*
 * 给定的币转换为人民币的价格
 * USDT 48 BTC 40  ETH 37
 *
 *
 * */
@Component
public class RevertCNYUtils {

    @Autowired
    private RedisService redisService;

    @Autowired
    private YangCurrencyMapper yangCurrencyMapper;

    @Autowired
    private YangTradeMapper yangTradeMapper;

    public String revertCNYPrice(String mark, Integer currencyId) {
        try {
            if (currencyId == null || currencyId == 0) {
                //使用mark
                List<Integer> ids = yangCurrencyMapper.getCurrencyIdByCurrencyMark(new String[]{mark.toUpperCase()});
                currencyId = ids.get(0);
            }
            List<Integer> ids1 = yangCurrencyMapper.getCurrencyIdByCurrencyMark(new String[]{"USDT"});
            if (ids1.size() > 0) {
                Integer usdtId = ids1.get(0);
                List<YangTrade> yangTrades = yangTradeMapper.getOrdersByCid(currencyId, usdtId);
                if (yangTrades.size() > 0) {
                    //去查最新的交易价格
                    String price = yangTrades.get(0).getPrice().toString();
                    //这个价格 和
                    String key = CoinConst.CNYPRICE + "USDT";
                    String cny = redisService.get(key).toString();
                    String revertPrice = DecimalUtils.multiplication(price, cny);
                    return revertPrice;
                }
            }
            List<Integer> ids2 = yangCurrencyMapper.getCurrencyIdByCurrencyMark(new String[]{"BTC"});
            if (ids2.size() > 0) {
                Integer btcId = ids2.get(0);
                List<YangTrade> yangTrades2 = yangTradeMapper.getOrdersByCid(currencyId, btcId);
                if (yangTrades2.size() > 0) {
                    //去查最新的交易价格
                    String price = yangTrades2.get(0).getPrice().toString();
                    //这个价格 和
                    String key = CoinConst.CNYPRICE + "BTC";
                    String cny = redisService.get(key).toString();
                    String revertPrice = DecimalUtils.multiplication(price, cny);
                    return revertPrice;
                }
            }

            List<Integer> ids3 = yangCurrencyMapper.getCurrencyIdByCurrencyMark(new String[]{"ETH"});
            if (ids3.size() > 0) {
                Integer ethId = ids3.get(0);
                List<YangTrade> yangTrades3 = yangTradeMapper.getOrdersByCid(currencyId, ethId);
                if (yangTrades3.size() > 0) {
                    //去查最新的交易价格
                    String price = yangTrades3.get(0).getPrice().toString();
                    //这个价格 和
                    String key = CoinConst.CNYPRICE + "ETH";
                    String cny = redisService.get(key).toString();
                    String revertPrice = DecimalUtils.multiplication(price, cny);
                    return revertPrice;
                }
            }
            List<YangTrade> yangTrades4 = yangTradeMapper.getOrdersByCurrencyId(currencyId);
            if (yangTrades4.size() > 0) {
                //去查最新的交易价格,这个交易对不是三种的
                String price1 = yangTrades4.get(0).getPrice().toString();
                Integer currencyTradeId = yangTrades4.get(0).getCurrencyTradeId();
                //递归查询
                String price2 = revertCNYPrice("", currencyTradeId);
                String price = DecimalUtils.multiplication(price1, price2);
                //这个价格 和
                return price;
            }

        } catch (Exception e) {
            return "0";
        }
        return "0";
    }
}
