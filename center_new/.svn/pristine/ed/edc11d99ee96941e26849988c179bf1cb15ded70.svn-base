package net.cyweb.mapper;

import net.cyweb.config.mybatis.InsertOrderIdListMapper;
import net.cyweb.config.mybatis.TkMapper;
import net.cyweb.model.YangCurrencyPair;
import net.cyweb.model.YangOrders;
import org.apache.ibatis.annotations.Mapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mapper
public interface YangOrdersMapper extends InsertOrderIdListMapper<YangOrders> {



    List<YangOrders> getOrdersGroupByPrice(Map parma);


    Map dealOrder(HashMap pama);


    HashMap getOrderNumsInfo(YangCurrencyPair yangCurrencyPair);

    List<Map> findMaxOrMinPriceSellOrder(Map map);

    List<Map> querySamePrice(YangCurrencyPair yangCurrencyPair);

}