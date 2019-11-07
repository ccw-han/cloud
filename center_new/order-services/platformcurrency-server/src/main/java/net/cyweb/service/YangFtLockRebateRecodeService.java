package net.cyweb.service;

import net.cyweb.mapper.YangFtLockRebateRecodeMapper;
import net.cyweb.model.YangFtLockRebateRecode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class YangFtLockRebateRecodeService extends BaseService<YangFtLockRebateRecode>{
    @Autowired
    private YangFtLockRebateRecodeMapper yangFtLockRebateRecodeMapper;

    public int insertRebatRecords(List<YangFtLockRebateRecode> list){
        return yangFtLockRebateRecodeMapper.insertRebatRecords(list);
    }
}
