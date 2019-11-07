package net.cyweb.service;

import cyweb.utils.ErrorCode;
import net.cyweb.mapper.YangCurrencyMarketMapper;
import net.cyweb.model.Result;
import net.cyweb.model.YangCurrencyMarket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.List;
import java.util.Map;

@Service
public class YangCurrencyMarketService extends BaseService<YangCurrencyMarket>{

    @Autowired
    private YangCurrencyMarketMapper yangCurrencyMarketMapper;

    public Result getCurrencyMarket(){
        Result result=new Result();
        try{
            List<Map> list= yangCurrencyMarketMapper.getCurrencyMarket();
            result.setData(list);
        }catch (Exception e){
            e.printStackTrace();
            result.setCode(Result.Code.ERROR);
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
            result.setMsg(ErrorCode.ERROR_SYSTEM.getMessage());
        }
        return result;
    }

    /**
     * 获取交易市场,查找可以显示的交易区
     * @return
     */
    public Result getTradeCurrencys(){
        Result result=new Result();
        try{
            List<Map> list= yangCurrencyMarketMapper.getTradeCurrencys();
            result.setData(list);
        }catch (Exception e){
            e.printStackTrace();
            result.setCode(Result.Code.ERROR);
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
            result.setMsg(ErrorCode.ERROR_SYSTEM.getMessage());
        }
        return result;
    }
}
