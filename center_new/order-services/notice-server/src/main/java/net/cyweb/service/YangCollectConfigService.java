package net.cyweb.service;

import net.cyweb.mapper.YangCollectConfigMapper;
import net.cyweb.model.YangCollectConfig;
import net.cyweb.model.YangMember;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class YangCollectConfigService extends BaseService<YangCollectConfig>{

    @Autowired
    YangCollectConfigMapper yangCollectConfigMapper;




}
