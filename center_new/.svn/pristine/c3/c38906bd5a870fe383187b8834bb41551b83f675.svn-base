package net.cyweb.mapper;

import net.cyweb.config.mybatis.TkMapper;
import net.cyweb.model.YangAcceptance;
import net.cyweb.model.YangArea;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface YangAcceptanceMapper extends TkMapper<YangAcceptance> {
    Integer insertYangAcceptance(YangAcceptance yangAcceptance);

    int updateAcceptancesPicInfo(YangAcceptance yangAcceptance);

    Integer updateStatesByMemberId(@Param(value = "memberId") Integer memberId, @Param(value = "states") String states);
}
