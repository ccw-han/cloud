package net.cyweb.mapper;

import net.cyweb.config.mybatis.TkMapper;
import net.cyweb.model.YangCurrencyPair;
import net.cyweb.model.YangTrade;
import org.apache.ibatis.annotations.Mapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mapper
public interface YangTradeMapper extends TkMapper<YangTrade> {
    Map<String, Object> queryLastDayInfo(Map map);

    List<Map> findLastDayTradeInfo(Map map);

    List<Map> findLastDayTradeInfoForMongodb(Map map);

    List<Map> findList(Map map);

    List<YangTrade> findRobotList(Map list);

    int deleteBatch(List<YangTrade> list);

    public  List<HashMap> selectFeeByType(Map pama);

    public Map getLastDaySellFeeTotal(Map map);

    public Map getLastDayBuyFeeTotal(Map map);

}