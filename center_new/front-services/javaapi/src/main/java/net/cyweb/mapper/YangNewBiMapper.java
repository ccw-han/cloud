package net.cyweb.mapper;

import net.cyweb.config.mybatis.TkMapper;
import net.cyweb.model.YangNewbi;
import net.cyweb.model.modelExt.YangCurrencyExt;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface YangNewBiMapper extends TkMapper<YangNewbi> {


    /**
     * 获取币种介绍信息
     * @param currencyId    货币的id
     * @return  币种介绍信息
     */
    YangNewbi getCurrencyProfile(@Param(value = "currencyId") Integer currencyId);


}