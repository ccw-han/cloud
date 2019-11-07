package net.cyweb.mapper;

import net.cyweb.config.mybatis.InsertOrderIdListMapper;
import net.cyweb.model.YangOrders;
import net.cyweb.model.YangOrdersRobot;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface YangOrdersRobotMapper extends InsertOrderIdListMapper<YangOrdersRobot> {

    int saveBatch(List<YangOrders> list);
}