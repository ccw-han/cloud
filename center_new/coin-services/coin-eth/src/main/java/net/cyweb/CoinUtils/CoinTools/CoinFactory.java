package net.cyweb.CoinUtils.CoinTools;

import net.cyweb.CoinUtils.CoinEntry.Base.BaseCoinI;
import net.cyweb.model.YangCurrency;
import net.cyweb.service.RedisService;

import java.util.HashMap;

public  class CoinFactory {

    private String coinName;

    private HashMap ExtenInfo;

    private BaseCoinI coin;


    /**
     * ExtenInfo 必须包含币种的额外信息，例如精度什么的 代币地址等
     * @param yangCurrency
     * @throws Exception
     */
    public static BaseCoinI createCoin(YangCurrency yangCurrency, RedisService redisService) throws Exception {
        Class<BaseCoinI> coinIClass =  CommonTools.getClasses(yangCurrency.getBean());
        BaseCoinI coinI   = coinIClass.newInstance();
        coinI.ExtendBaseInfo(yangCurrency,redisService);
        return coinI;
    }

//
//    /**
//     * 创建用户
//     * @param password
//     * @return
//     * @throws Exception
//     */
//    public String  createUser(String password) throws Exception
//    {
//        return this.coin.createUser(password);
//    }



}
