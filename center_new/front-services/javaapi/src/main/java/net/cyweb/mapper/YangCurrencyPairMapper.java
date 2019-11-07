package net.cyweb.mapper;

import net.cyweb.config.mybatis.TkMapper;
import net.cyweb.model.YangCurrencyPair;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.security.access.method.P;

import java.util.List;
import java.util.Map;

@Mapper
public interface YangCurrencyPairMapper extends TkMapper<YangCurrencyPair> {
    List<Map> getCurrencyListChange(Map map);

    List<Map> getCurrencyDetailChange(Map map);

    List<Map> getCurrencyPairList(Map map);


    List<Map> getCurrencyListChanges(Map<String, String> map);

    List<Map> getTradeName(Long currencyTradeId);

    List<Map> getYangCurrencyPairList();
    List<Map> getYangCurrencyPairLists();

    YangCurrencyPair getYangCurrencyPairByMark(String mark);

    YangCurrencyPair getYangCurrencyPairByPairId(@Param(value = "currencyId") String currencyId, @Param(value = "currencyTradeId") String currencyTradeId);
}
