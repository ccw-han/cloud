package net.cyweb.mapper;

import net.cyweb.config.mybatis.TkMapper;
import net.cyweb.model.YangTrade;

import java.util.List;

public interface YangTradeMapper extends TkMapper<YangTrade> {
    List<YangTrade> fontUserTradeFiveRecord(YangTrade yangTrade);
}