package net.cyweb.service;


import com.alibaba.fastjson.JSONObject;
import cyweb.utils.CoinConst;
import net.cyweb.mapper.YangCurrencyPairMapper;
import net.cyweb.model.YangCurrencyPair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class YangPairService extends BaseService<YangCurrencyPair>{


    @Autowired
    private RedisService redisService;

    @Autowired
    private YangCurrencyPairMapper yangCurrencyPairMapper;


    /**
     * 获取交易对的基本信息，加上redis的缓存
     * @return
     */
    public YangCurrencyPair getPairInfo(Integer currency_id,Integer thread_id,boolean isFlash)
    {
        String key = CoinConst.REDIS_YANG_PARI_INFO_ + currency_id +"_"+ thread_id;
        if(  !isFlash  && redisService.get(key) != null)
        {

            return JSONObject.parseObject(redisService.get(key).toString(),YangCurrencyPair.class);
        }else{
            System.out.println("找不到交易对信息,走数据库"+currency_id+"-"+thread_id);
            YangCurrencyPair yangCurrencyPair = new YangCurrencyPair();
            yangCurrencyPair.setCurrencyId(currency_id);
            yangCurrencyPair.setCurrencyTradeId(thread_id);
            YangCurrencyPair yangCurrencyPair1 = yangCurrencyPairMapper.selectOne(yangCurrencyPair);

            if(yangCurrencyPair1 != null)
            {
                redisService.set(key,JSONObject.toJSON(yangCurrencyPair1).toString());
            }

            return yangCurrencyPair1;
        }
    }


}
