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
        array.add(37);
        Example example = new Example(YangCurrency.class);
        example.createCriteria().andIn("currencyId",array);
        return this.yangCurrencyMapper.selectByExample(example);

    }

    public YangCurrency getYcByCid(Integer currencyId)
    {
        YangCurrency yangCurrency = new YangCurrency();
        yangCurrency.setCurrencyId(currencyId);
        return this.yangCurrencyMapper.selectOne(yangCurrency);
    }

    public List<YangCurrency> getNeedListenCoinAndDb()
    {
        Example example = new Example(YangCurrency.class);

        example.createCriteria().andEqualTo("mainId",37);
        List<YangCurrency> list =  this.yangCurrencyMapper.selectByExample(example);

        example.clear();
        example.createCriteria().andEqualTo("currencyId",37);
        List<YangCurrency> list2 =  this.yangCurrencyMapper.selectByExample(example);


        if(list != null)
        {
            list2.addAll(list);
        }


        return list2;

    }


}
