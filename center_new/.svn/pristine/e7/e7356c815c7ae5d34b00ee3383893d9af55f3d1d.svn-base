package net.cyweb.mapper;

import net.cyweb.config.mybatis.TkMapper;
import net.cyweb.model.YangCurrencyPair;
import net.cyweb.model.YangTrade;
import net.cyweb.model.YangTradeRobot;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface YangTradeRobotMapper extends TkMapper<YangTradeRobot> {

   int saveBatch(List<YangTrade> list);

}