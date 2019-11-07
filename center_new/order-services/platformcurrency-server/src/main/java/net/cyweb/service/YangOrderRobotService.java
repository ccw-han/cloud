package net.cyweb.service;

import net.cyweb.mapper.YangOrdersMapper;
import net.cyweb.mapper.YangOrdersRobotMapper;
import net.cyweb.model.YangOrders;
import net.cyweb.model.YangOrdersRobot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@EnableAsync
@Scope("prototype")
public class YangOrderRobotService extends BaseService<YangOrdersRobot> {

    @Autowired
    private YangOrdersRobotMapper yangOrdersRobotMapper;

    public int saveBatch(List<YangOrders> list){
        return yangOrdersRobotMapper.saveBatch(list);
    }

}
