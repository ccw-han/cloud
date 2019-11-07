package net.cyweb.service;

import cyweb.utils.CoinConst;
import cyweb.utils.DateUtils;
import net.cyweb.mapper.YangFtLockRecordMapper;
import net.cyweb.model.YangFtLockRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class YangFtLockRecordService extends BaseService<YangFtLockRecord>{

    @Autowired
    private YangFtLockRecordMapper yangFtLockRecordMapper;

    public List<YangFtLockRecord> findLastDayLockInfo(){
        Map map=new HashMap();
        map.put("status",CoinConst.FT_LOCK_STATUS_TURNOUT );
        map.put("targetTimes",DateUtils.getNowTimes()-3600*24);
        return yangFtLockRecordMapper.findLastDayLockInfo(map);
    }

    public int updateFtLockRecord(List<YangFtLockRecord> list){
       return yangFtLockRecordMapper.updateFtLockRecord(list);
    }

}
