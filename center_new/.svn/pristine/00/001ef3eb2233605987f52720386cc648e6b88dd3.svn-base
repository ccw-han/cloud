package net.cyweb.CoinUtils.CoinEntry;

import net.cyweb.CoinUtils.CoinEntry.Base.BaseCoin;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.admin.Admin;
import org.web3j.protocol.geth.Geth;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Convert;

import java.math.BigInteger;
import java.util.HashMap;

public class EthBase extends BaseCoin {

    protected Web3j web3;
    protected Admin admin;
    protected Geth geth;
    protected BigInteger gasPrice;
    protected String systemAdd = "0xf5d2b7fcd4f97ff52e610683b4a97cd7f7862f78";
    protected String systemEmail  = "493840844@qq.com";

    public EthBase()
    {
        this.config = new HashMap<String,String>();
        this.config.put("coldAddress","0x965a53a52f4bc4fc5386ad8ed5430a3fe276d8ad"); //冷钱包地址 所有钱包里的币 将转入改地址
        this.gasPrice = BigInteger.valueOf(60000000000l); //60 geiwei




    }


}
