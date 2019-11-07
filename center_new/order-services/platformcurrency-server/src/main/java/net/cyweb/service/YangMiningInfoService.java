package net.cyweb.service;

import net.cyweb.mapper.YangMiningInfoMapper;
import net.cyweb.model.YangMiningInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@EnableAsync
public class YangMiningInfoService extends BaseService<YangMiningInfo>{

        @Autowired
        private YangMiningInfoMapper yangMiningInfoMapper;


        @Transactional
        public int insertMiningInfoBatch(List<YangMiningInfo> list){
                return yangMiningInfoMapper.insertMiningInfoBatch(list);
        }
}
