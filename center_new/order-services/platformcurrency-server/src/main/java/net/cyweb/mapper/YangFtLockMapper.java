package net.cyweb.mapper;

import net.cyweb.config.mybatis.TkMapper;
import net.cyweb.model.YangFtLock;

import java.math.BigDecimal;
import java.util.Map;

public interface YangFtLockMapper extends TkMapper<YangFtLock> {

    BigDecimal getLockInfo(Map pama);

    Map getTotalLockNums();

    Map getTotalFrozenNum();
}