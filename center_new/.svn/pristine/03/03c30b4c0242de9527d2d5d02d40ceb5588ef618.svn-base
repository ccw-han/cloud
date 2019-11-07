package net.cyweb.mapper;

import net.cyweb.config.mybatis.TkMapper;
import net.cyweb.model.YangCurrency;
import net.cyweb.model.modelExt.YangCurrencyExt;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.security.access.method.P;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mapper
public interface YangCurrencyMapper extends TkMapper<YangCurrency> {

    List<YangCurrencyExt> getCurrencyandUserAssets(YangCurrencyExt yangCurrencyExt);

    List<Map> getCurrencyETH(Map map);

    List<Map> getCurrency(Map map);

    List<Map> getCoins();

    List<Map> getAllCoins();

    YangCurrency getCurrencyInfoById(@Param(value = "currencyId") Integer currencyId);

    YangCurrency getCoinById(Integer currencyId);

    List<Integer> getCurrencyIdByCurrencyMark(String[] currencyMarks);

    YangCurrency getYangCurrencyById(@Param(value = "id") Integer id);

    Integer isExistTable(String tableName);

    Map<String, Object> selectYangAddressByMemberId(@Param(value = "tableName") String tableName, @Param(value = "memberId") Integer memberId);

    Integer updateMemberIdInYangAddress(@Param(value = "tableName") String tableName, @Param(value = "memberId") Integer memberId);

    String selectAddressByMemberId(@Param(value = "tableName") String tableName, @Param(value = "memberId") Integer memberId);

    Map<String, String> getCurrencyMarkByCurrencyId(Integer currencyId);
}