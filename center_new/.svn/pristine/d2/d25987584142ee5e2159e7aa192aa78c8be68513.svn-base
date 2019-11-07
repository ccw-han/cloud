package net.cyweb.CoinUtils.CoinEntry.Base;

import cyweb.utils.CoinConst;
import net.cyweb.CoinUtils.CoinEntry.ETH;
import net.cyweb.feignClient.MemberService;
import net.cyweb.model.YangCurrency;
import net.cyweb.service.CoinService;
import net.cyweb.service.RedisService;
import org.apache.log4j.Logger;

import org.springframework.stereotype.Component;


import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class BaseCoin {

    public static final Logger logger = Logger.getLogger(BaseCoin.class);

    protected YangCurrency yangCurrency;

    protected RedisService redisUtils;

    protected MemberService memberService;

    protected CoinService coinService;

    protected Map<String,String> config;

//    protected void  log(String msg)
//    {
//        System.out.println(msg);
//    }

    /**
     * 格式化金额 到真实金额
     * @param blance
     * @return
     */
    public Double num2real(BigInteger blance)
    {
        Double jd = Math.pow(10,Double.valueOf(this.config.get("jd")));
        return blance.doubleValue() / jd;
    }

    /**
     * 格式化金额 到系统金额
     * @param blance
     * @return
     */
    public BigInteger num2trance(BigDecimal blance)
    {
        Double jd = Math.pow(10,Double.valueOf(this.config.get("jd")));
        return BigInteger.valueOf(blance.multiply(BigDecimal.valueOf(jd.longValue())).longValue());

    }


    /**
     * 获取该币种的用户的地址信息
     * @return
     */
    protected HashMap getAddress(String currencyId)
    {
        try {

            String key = CoinConst.redis_currency_user_chizhiurl_key_+currencyId;

            Map<String,YangCurrency> currencyMap = (HashMap<String,YangCurrency>)this.redisUtils.hmGet(CoinConst.redis_currency_user_chizhiurl_keys,key);

            if(currencyMap == null)
            {
                this.memberService.flushCurrencyFlash(currencyId,key);
            }
            return  (HashMap<String,YangCurrency>)this.redisUtils.hmGet(CoinConst.redis_currency_user_chizhiurl_keys,key);

        }catch (Exception e)
        {
            ETH.logger.error(e.getMessage());
        }

        return null;

    }



}
