package net.cyweb;

import net.cyweb.CoinUtils.CoinEntry.Base.BaseCoin;
import net.cyweb.CoinUtils.CoinEntry.Base.BaseCoinI;
import net.cyweb.CoinUtils.CoinEntry.EthBase;
import net.cyweb.CoinUtils.CoinTools.CoinFactory;
import net.cyweb.model.YangCurrency;
import net.cyweb.service.CoinService;
import net.cyweb.service.RedisService;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.web3j.protocol.Service;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.admin.Admin;
import org.web3j.protocol.geth.Geth;
import org.web3j.protocol.http.HttpService;

import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.Future;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("local")
public class Test {

    @Autowired
    private  RedisService redisService;

    @Autowired
    private CoinService coinService;

    protected Web3j web3;
    protected Admin admin;
    protected Geth geth;

    @Value("${coin.coinServer-port}")
    private int coinServer_port;

    /**
     * 开启监听
     */
    @org.junit.Test
    public  void listenAllCoin()
    {
        System.out.println(coinServer_port);

        List<YangCurrency> list = coinService.getNeedListenCoin();
        System.out.println("待执行币种："+list);
        try {
            for (YangCurrency f:list
            ) {
                BaseCoinI baseCoinI =  CoinFactory.createCoin(f,redisService);
                System.out.println("初始化成功："+baseCoinI);
                Future<String> future = baseCoinI.chargingMoneylistener();
                EthBase.logger.info("begin to deal other Task!");

                while (true) {
                    if(future.isCancelled()){
                        System.out.println("任务异常！已经取消");
                        future = baseCoinI.chargingMoneylistener();

                    }else if (future.isDone() ) {
                        System.out.println("任务已经完成"+future.get());
                        future = baseCoinI.chargingMoneylistener();

                    }else{
                        System.out.println("充币监听任务正常执行中.....");
                        Thread.sleep(1000);
                    }
                }
            }
        }catch (Exception e)
        {
            e.printStackTrace();
            BaseCoin.logger.error(e.getMessage());
        }

    }

    @org.junit.Test
    public void test()
    {
        System.out.println("23213");
//        Service service = new HttpService("http://funcoin:wang628625@eth.funcoin.co");  //必须删除
        Service service = new HttpService("http://eth.funcoin.co");  //必须删除
        ((HttpService) service).addHeader("Authorization","Basic ZnVuY29pbjp3YW5nNjI4NjI1");

        this.web3 = Web3j.build(service);
        this.admin = Admin.build(service);
        this.geth = Geth.build(service);
        try {
            BigInteger blockNow = this.web3.ethBlockNumber().send().getBlockNumber();
//            this.web3.
            System.out.println(blockNow.intValue());
        }catch (Exception e)
        {
            e.printStackTrace();

        }



    }

}
