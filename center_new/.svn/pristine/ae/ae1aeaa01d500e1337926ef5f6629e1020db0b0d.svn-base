package net.cyweb.service;

import net.cyweb.mapper.YangTradeRobotMapper;
import net.cyweb.model.YangTrade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@EnableAsync
public class YangTradeRobotService extends BaseService<YangTrade>{

    @Autowired
    private YangTradeRobotMapper yangTradeRobotMapper;

    public int saveBatch(List<YangTrade> list){
        return yangTradeRobotMapper.saveBatch(list);
    }

}
