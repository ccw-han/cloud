package net.cyweb.mapper;
import net.cyweb.config.mybatis.TkMapper;
import net.cyweb.model.YangCurrency;
import net.cyweb.model.YangWithdraw;

import java.util.List;
import java.util.Map;

public interface YangWithdrawMapper extends TkMapper<YangWithdraw> {
    List<Map> getWithdrawRecordByCurrencyId(String currencyId);
}
