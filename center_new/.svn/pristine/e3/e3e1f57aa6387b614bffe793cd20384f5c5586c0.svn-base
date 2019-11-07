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


    List<Map> getOrderByfields(Map parama);

    List<Map> getOrderBySellOrBuy(Map param);

    List<HashMap> getOrderCollectInfos(YangCurrencyPair yangCurrencyPair);

    List<YangOrders> findRobotList(Map map);

    int deleteBatch(List<YangOrders> list);

    Integer cancelOrder(Map queryMap);

    List<YangOrders> findFinishOrderList();
}