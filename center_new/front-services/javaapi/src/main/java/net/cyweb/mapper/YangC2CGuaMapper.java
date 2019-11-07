package net.cyweb.mapper;

import net.cyweb.config.mybatis.TkMapper;
import net.cyweb.model.YangC2CGua;
import net.cyweb.model.modelExt.YangC2CGuaEvt;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface YangC2CGuaMapper extends TkMapper<YangC2CGua> {

    List<YangC2CGua> getC2CGuaByCurrencyId(@Param(value = "currencyId") Integer currencyId, @Param(value = "memberId") Integer memberId);

    List<Map<String, Object>> getC2CGuaByMemberId(@Param(value = "memberId") Integer memberId);

    /**
     * 承兑商查询用户订单记录
     *
     * @param memberId
     * @param type
     * @return
     */
    List<Map<String, Object>> findMembersOrdersByAcceptances(@Param(value = "memberId") Integer memberId, @Param(value = "type") Integer type);

    /**
     * 用户法币订单记录
     *
     * @param memberId
     * @param type
     * @return
     */
    List<Map<String, Object>> findMembersOrdersByMemberId(@Param(value = "memberId") Integer memberId, @Param(value = "type") Integer type);

    List<Map<String, String>> buyCoins(YangC2CGuaEvt yangC2CGuaEvt);

    Integer updateStatusById(Map<String, Object> param);
}
