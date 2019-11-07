package net.cyweb.service;


import net.cyweb.CoinUtils.CoinEntry.Base.BaseCoin;
import net.cyweb.mapper.YangCurrencyMapper;
import net.cyweb.model.YangCurrency;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

@Service
public class CoinService extends BaseService<YangCurrency> {


    @Autowired
    private YangCurrencyMapper yangCurrencyMapper;


    /**
     * 获取币种信息
     * @param currencyId
     * @return
     */
    public YangCurrency getCurrencyInfo(Integer currencyId)
    {
       return this.yangCurrencyMapper.selectByPrimaryKey(currencyId);
    }

    public List<YangCurrency> getNeedListenCoin()
    {

        ArrayList array = new ArrayList<Integer>();
        array.add(40);
        Example example = new Example(YangCurrency.class);
        example.createCriteria().andIn("currencyId",array);
        return this.yangCurrencyMapper.selectByExample(example);

    }



}
