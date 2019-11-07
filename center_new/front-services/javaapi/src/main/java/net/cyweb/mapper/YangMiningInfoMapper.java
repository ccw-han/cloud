package net.cyweb.mapper;

import net.cyweb.config.mybatis.TkMapper;
import net.cyweb.model.MiningInfos;
import net.cyweb.model.YangMiningInfo;
import org.apache.ibatis.annotations.Mapper;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mapper
public interface YangMiningInfoMapper extends TkMapper<YangMiningInfo> {


    BigDecimal getMiningInfos(Map pama);

    BigDecimal getRakeInfo(Map pama);

    BigDecimal getUserRake(Map pama);

    List<Map> getMiningInfoByMember(Map pama);

}