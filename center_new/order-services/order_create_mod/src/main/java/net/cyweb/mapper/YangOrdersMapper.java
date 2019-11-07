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


    HashMap getOrderNumsInfo(YangCurrencyPair yangCurrencyPair);

    List<YangOrders> fontUserTradeFiveRecord(YangOrders yangOrders);

    YangOrders findMaxOrMinPriceSellOrder(Map map);

    Long countOrderNumByCondition(YangOrders yangOrders);

    List<YangOrders> queryNoDsOrderListForBuy(Map map);
    List<YangOrders> queryNoDsOrderListForSell(Map map);
}