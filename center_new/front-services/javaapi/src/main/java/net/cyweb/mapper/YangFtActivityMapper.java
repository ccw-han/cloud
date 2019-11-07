package net.cyweb.mapper;

import net.cyweb.config.mybatis.TkMapper;
import net.cyweb.model.YangFtActivity;
import org.apache.ibatis.annotations.Mapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Mapper
public interface YangFtActivityMapper extends TkMapper<YangFtActivity> {

    Map getFuntNum(Map map);

    List<Map> getCardOrder(Map map);

    Long getCardOrderCount(Map map);

    List<Map> getFtLockRecord(Map map);

    Long getFtLockRecordCount(Map map);

    Map getFtLockRecordSum(Map map);

    Map getFtLockInfo(Map map);
}