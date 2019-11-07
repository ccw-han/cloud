package net.cyweb.mapper;

import net.cyweb.config.mybatis.TkMapper;
import net.cyweb.model.YangCurrencyMarket;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface YangCurrencyMarketMapper extends TkMapper<YangCurrencyMarket> {
    List<Map> getCurrencyMarket();

    List<Map> getTradeCurrencys();
}
