package net.cyweb.mapper;

import net.cyweb.config.mybatis.TkMapper;
import net.cyweb.model.YangFtLockRecord;

import java.util.List;
import java.util.Map;

public interface YangFtLockRecordMapper extends TkMapper<YangFtLockRecord> {
    List<YangFtLockRecord> findLastDayLockInfo(Map map);

    int updateFtLockRecord(List<YangFtLockRecord> list);
}
