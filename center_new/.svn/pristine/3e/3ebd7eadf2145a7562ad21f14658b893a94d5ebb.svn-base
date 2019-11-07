package net.cyweb.mapper;

import net.cyweb.config.mybatis.TkMapper;
import net.cyweb.model.YangMiningInfo;
import org.apache.ibatis.annotations.Mapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Mapper
public interface YangMiningInfoMapper extends TkMapper<YangMiningInfo> {
    int insertMiningInfoBatch(List<YangMiningInfo> list);

    BigDecimal getRakeInfo(Map pama);
}
