package net.cyweb.mapper;

import javafx.scene.chart.ValueAxis;
import net.cyweb.config.mybatis.InsertOrderIdListMapper;
import net.cyweb.config.mybatis.TkMapper;
import net.cyweb.model.YangOrders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Mapper

public interface YangOrdersMapper extends InsertOrderIdListMapper<YangOrders> {


    List<Map> getOrderByfields(Map parama);

    List<Map> getOrderBySellOrBuy(Map param);

    List<YangOrders> fontUserTradeFiveRecord(Map map);

    List<Map> myFontOrders(Map map);

    int myFontOrdersCount(Map map);

    Integer cancelOrder(Map queryMap);

    Map dealOrder(HashMap pama);

    List<Map> getHistoryOrdersByMemberId(Integer id);

    List<Map> getOrdersByMemberId(Integer id);

    List<Map> getUserOrderFiveRecordBuy();
    List<Map> getUserOrderFiveRecordSell();

    List<YangOrders> getUserOrderFiveRecordByTwoParam(@Param(value = "currencyId") Integer currencyId,@Param(value = "currencyTradeId") Integer currencyTradeId);

    List<YangOrders> fontUserTradeFiveRecords(YangOrders yangOrders);
}