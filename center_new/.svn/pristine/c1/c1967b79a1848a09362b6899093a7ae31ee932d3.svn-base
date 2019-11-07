package net.cyweb.service;


import net.cyweb.mapper.YangCurrencyPairMapper;
import net.cyweb.model.YangCurrencyPair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class YangCurrencyPairService {

    @Autowired
    private YangCurrencyPairMapper yangCurrencyPairMapper;

    public List<YangCurrencyPair> getRoboList(){
        return yangCurrencyPairMapper.getRobotList();
    }

}
