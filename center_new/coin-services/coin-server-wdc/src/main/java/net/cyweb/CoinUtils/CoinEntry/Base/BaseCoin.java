package net.cyweb.CoinUtils.CoinEntry.Base;

import com.sun.org.apache.xpath.internal.functions.FuncCurrent;
import net.cyweb.feignClient.MemberService;
import net.cyweb.model.YangCurrency;
import net.cyweb.service.CoinService;
import net.cyweb.service.RedisService;
import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;
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
    public Double num2real(BigDecimal blance)
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



}
