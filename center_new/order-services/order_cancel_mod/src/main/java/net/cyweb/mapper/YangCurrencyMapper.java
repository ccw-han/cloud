package net.cyweb.mapper;

import net.cyweb.config.mybatis.TkMapper;
import net.cyweb.model.YangCurrency;
import net.cyweb.model.modelExt.YangCurrencyExt;
import org.apache.ibatis.annotations.Mapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mapper
public interface YangCurrencyMapper extends TkMapper<YangCurrency> {


}