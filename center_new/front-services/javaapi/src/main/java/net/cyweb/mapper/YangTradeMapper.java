package net.cyweb.mapper;

import net.cyweb.config.mybatis.TkMapper;
import net.cyweb.model.YangTrade;
import org.apache.ibatis.annotations.Param;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface YangTradeMapper extends TkMapper<YangTrade> {

    public List<HashMap> selectMaxAndMinAndTotal(Map pama);

    public List<HashMap> selectFeeByType(Map pama);

    public List<Map> klineData(Map map);

    public Map getFontTradeLastTime(Map map);

    List<YangTrade> fontUserTradeFiveRecord(Map map);

    List<Map> searchTrade(Map map);

    Long searchTradeCount(Map map);

    YangTrade selectTradeByIds(@Param(value = "memberId") Integer memberId, @Param(value = "currencyId") Integer currencyId);

    List<YangTrade> getOrdersByCid(@Param(value = "currencyId") Integer currencyId, @Param(value = "currencyTradeId") Integer currencyTradeId);

    List<YangTrade> getOrdersByCurrencyId(@Param(value = "currencyId") Integer currencyId);

    Map getNewPrice(@Param(value = "currencyId") Long currencyId, @Param(value = "currencyTradeId") Long currencyTradeId);

    Map getPriceLimit(@Param(value = "currencyId") Long currencyId, @Param(value = "currencyTradeId") Long currencyTradeId, @Param(value = "dayBegin") Long dayBegin);

    Map getMaxPrice(@Param(value = "currencyId") Long currencyId, @Param(value = "currencyTradeId") Long currencyTradeId, @Param(value = "dayBegin") Long dayBegin);

    Map getMinPrice(@Param(value = "currencyId") Long currencyId, @Param(value = "currencyTradeId") Long currencyTradeId, @Param(value = "dayBegin") Long dayBegin);

    Map get24VOL(@Param(value = "currencyId") Long currencyId, @Param(value = "currencyTradeId") Long currencyTradeId, @Param(value = "dayBegin") Long dayBegin);

    Map get24TurnoverL(@Param(value = "currencyId") Long currencyId, @Param(value = "currencyTradeId") Long currencyTradeId, @Param(value = "dayBegin") Long dayBegin);

    Map getPriceLimits(@Param(value = "currencyId") Long currencyId, @Param(value = "currencyTradeId") Long currencyTradeId, @Param(value = "dayBegin") Long dayBegin);

    Map getNewPrices(@Param(value = "currencyId") Long currencyId, @Param(value = "currencyTradeId") Long currencyTradeId);

    Map getMaxPrices(@Param(value = "currencyId") Long currencyId, @Param(value = "currencyTradeId") Long currencyTradeId, @Param(value = "dayBegin") Long dayBegin);

    Map getMinPrices(@Param(value = "currencyId") Long currencyId, @Param(value = "currencyTradeId") Long currencyTradeId, @Param(value = "dayBegin") Long dayBegin);

    Map get24VOLs(@Param(value = "currencyId") Long currencyId, @Param(value = "currencyTradeId") Long currencyTradeId, @Param(value = "dayBegin") Long dayBegin);

    Map get24TurnoverLs(@Param(value = "currencyId") Long currencyId, @Param(value = "currencyTradeId") Long currencyTradeId, @Param(value = "dayBegin") Long dayBegin);

    Map getMaximumOrderPrice(@Param(value = "currencyId") Long currencyId, @Param(value = "currencyTradeId") Long currencyTradeId, @Param(value = "dayBegin") Long dayBegin);
    Map getMinimumGuaPrice(@Param(value = "currencyId") Long currencyId, @Param(value = "currencyTradeId") Long currencyTradeId, @Param(value = "dayBegin") Long dayBegin);
}