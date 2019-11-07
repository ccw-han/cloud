package net.cyweb.service;

import net.cyweb.mapper.YangWithdrawMapper;
import net.cyweb.model.Result;
import net.cyweb.model.YangWithdraw;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class YangWithdrawService extends BaseService<YangWithdraw>{

    @Autowired
    private YangWithdrawMapper yangWithdrawMapper;

    public Result getWithdrawRecordByCurrencyId(String currencyId) {
        Result result = new Result();
        List<Map> withdrawRecordByCurrencyId = yangWithdrawMapper.getWithdrawRecordByCurrencyId(currencyId);
        result.setCode(1);
        result.setMsg("查询成功");
        result.setData(withdrawRecordByCurrencyId);
        return result;
    }
}
