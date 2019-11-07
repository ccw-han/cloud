package net.cyweb.mapper;

import net.cyweb.config.mybatis.TkMapper;
import net.cyweb.model.YangCurrency;
import net.cyweb.model.YangCurrencyUser;
import net.cyweb.model.modelExt.YangCurrencyUserExt;
import org.apache.ibatis.annotations.Mapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Mapper
public interface YangCurrencyUserMapper extends TkMapper<YangCurrencyUser> {


    int replaceIntochongzhiUrl(YangCurrencyUser yangCurrencyUser);

    List<YangCurrencyUserExt> getYangCurrencyUserAndEmail(YangCurrency yangCurrency);

    List<YangCurrencyUser> getAllCurrencyList(Map map);

    Map<String, String> isHasEnoughBM(Integer memberId);
}