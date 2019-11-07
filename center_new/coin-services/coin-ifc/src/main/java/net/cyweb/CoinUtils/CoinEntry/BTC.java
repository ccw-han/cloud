package net.cyweb.CoinUtils.CoinEntry;



import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.azazar.bitcoin.jsonrpcclient.Bitcoin;
import com.azazar.bitcoin.jsonrpcclient.BitcoinException;
import com.azazar.bitcoin.jsonrpcclient.BitcoinJSONRPCClient;
import net.cyweb.CoinUtils.CoinEntry.Base.BaseCoin;
import net.cyweb.CoinUtils.CoinEntry.Base.BaseCoinI;

import net.cyweb.CoinUtils.CoinTools.SpringUtils;
import net.cyweb.CoinUtils.Exception.LogException;

import net.cyweb.feignClient.MemberService;
import net.cyweb.model.Result;
import net.cyweb.model.YangCurrency;
import net.cyweb.model.YangTibi;
import net.cyweb.service.RedisService;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;


import java.math.BigDecimal;
import java.math.BigInteger;

import java.net.InetAddress;
import java.net.URL;
import java.util.*;
import java.util.concurrent.Future;

@Service
public class BTC extends BaseCoin implements BaseCoinI {

    private BitcoinJSONRPCClient bitcoinJSONRPCClient;

    private String blockKey;

    private int startnums;


    @Override
    public void ExtendBaseInfo(YangCurrency yangCurrency, RedisService redisService) throws LogException {

        try {
//            URL url = new URL("http://wang628625:628625@47.75.87.231:8335");
            InetAddress ip= InetAddress.getByName("ifc-coin-service");
//            InetAddress ip= InetAddress.getByName("btc");
            String address = ip.getHostAddress();

            URL url = new URL("http://infinitecoinrpc:4p9UZojd3t8h9XF4ZWGqDkqhFthzL6xVNnXMUhGVrPZ9@"+address+":9322");

            System.out.println("连接地址为"+"http://infinitecoinrpc:4p9UZojd3t8h9XF4ZWGqDkqhFthzL6xVNnXMUhGVrPZ9@"+address+":9322");

            this.bitcoinJSONRPCClient = new BitcoinJSONRPCClient(url);

            System.out.println("获取到的钱包实力"+this.bitcoinJSONRPCClient);

            System.out.println("当前模块高度为"+bitcoinJSONRPCClient.getBlockCount());

            this.blockKey = "blocknums-btc-"+yangCurrency.getCurrencyId(); //这个必须按照币种的名字来  要保证key是唯一的

            this.redisUtils = redisService;

            this.yangCurrency = yangCurrency;

            memberService = SpringUtils.getBean(MemberService.class);

        }catch (Exception e)
        {

            System.out.println("连接失败了");
            e.printStackTrace();
        }


    }

    @Override
    public BigDecimal getBalance(String account) throws LogException {
        try{
            return new BigDecimal(this.bitcoinJSONRPCClient.getAccount(account));
        }catch (Exception e)
        {
            BTC.logger.error(e.getMessage());
        }
        return BigDecimal.valueOf(0);

    }

    @Override
    public BigDecimal getAllBalance() throws Exception {
        return  new BigDecimal(String.valueOf(this.bitcoinJSONRPCClient.getBalance()));
    }

    @Override
    public List<String> getAllAccount() throws Exception {
        return null;
    }

    @Override
    public String sendTransaction(String from, BigInteger num, String to, String passwd) throws LogException {
        try {
            if(this.unlock())
            {
                String hash =  this.bitcoinJSONRPCClient.sendToAddress(to,num.doubleValue());
                this.lock();
                return hash;
            }else{
                throw new Exception("无法解锁钱包");
            }


        }catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String sendTransactionALl(String from, String to, String passwd) throws LogException {
        return null;
    }

    @Override
    public String sendTransaction2cold(String from,  double num, String passwd) throws LogException {
        String coldAddress = "iQN1cn87htQr3qxW9Djnu595oZKRg1hi14";
        try {
            if(this.unlock())
            {
                System.out.println("开始转账，冷钱包地址为:"+coldAddress+" 金额："+num);
                String tx = this.bitcoinJSONRPCClient.sendToAddress(coldAddress,num);
                this.lock();
                System.out.println("转账成功;tid="+tx);
                return tx;
            }
        }catch (Exception e)
        {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public String sendTransaction2User(String to, BigInteger num, String passwd) throws LogException {
        return null;
    }

    @Override
    public Result chongzhiByTx(String tx) throws LogException {
        return null;
    }

    @Override
    public String createUser(String passwd) throws LogException {
        try {
           return  this.bitcoinJSONRPCClient.getNewAddress(passwd);
        }catch (Exception e)
        {
            throw new LogException(e.getMessage());
        }
    }

    @Override
    public boolean changUser(String account, String passwd) throws LogException {
        return false;
    }

    @Override
    @Async
    public Future<String> chargingMoneylistener() throws InterruptedException {
        System.out.println("redis实例是" + this.redisUtils);

        try {
            while (true) {

                doChargingMoneylistener();
                Thread.sleep(3000);
            }
        } catch (Exception e) {
            e.printStackTrace();
            this.logger.error(e.getMessage());
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("thread", Thread.currentThread().getName());
        jsonObject.put("time", System.currentTimeMillis());
        return new AsyncResult<String>(jsonObject.toJSONString());

    }

    public void  doChargingMoneylistener() throws Exception
    {
        try{
            this.startnums ++ ;
            BigInteger lastpreNums;
            //第一次进入 这个时候 先把中间断掉的块处理掉
            System.out.println("-----开始第"+startnums+"次处理-----");

            int blockNow = this.bitcoinJSONRPCClient.getBlockCount();

            if(this.redisUtils.get(this.blockKey) != null) {

                lastpreNums = BigInteger.valueOf(Long.valueOf(this.redisUtils.get(this.blockKey).toString()));

                if(lastpreNums.compareTo(BigInteger.valueOf(blockNow)) == 0 )
                {
                    System.out.println("已经到达最新高度！要不停下来休息下 等等他 ！先休息个30s");
                    Thread.sleep(30000);

                }else{
                    while (true)
                    {
                        if(lastpreNums.compareTo(BigInteger.valueOf(blockNow)) >= 0 )
                        {
                            System.out.println("校准当前高度为"+blockNow);
                            redisUtils.set(this.blockKey,blockNow);
                            break;
                        }

                        this.confirm(BigInteger.valueOf(blockNow));


                        Bitcoin.Block block =  this.bitcoinJSONRPCClient.getBlock(this.bitcoinJSONRPCClient.getBlockHash(lastpreNums.intValue()));


                        this.updateUserCurrency(block);

                        lastpreNums = lastpreNums.add(BigInteger.valueOf(1));
                    }
                }

            }else{
                this.redisUtils.set(this.blockKey,blockNow);
            }
        }catch (Exception e)
        {
            e.printStackTrace();
            System.out.println("出现异常重新处理");
        }
    }


    @Async
    @Override
    public void chargingMoneylistenerDistory(YangCurrency yangCurrency) throws LogException, BitcoinException {
        try {
//            this.lock();
        }catch (Exception e)
        {

        }

    }

    @Async
    public void updateUserCurrency( Bitcoin.Block block) {
        System.out.println("当前处理高度----" + block.height());

        YangTibi yangTibi = null;

        List<String> txList = block.tx();

        for (String tx : txList
                ) {

            try {
                BitcoinJSONRPCClient.RawTransactionImpl rawTransaction = (BitcoinJSONRPCClient.RawTransactionImpl) this.bitcoinJSONRPCClient.getTransaction(tx);
                Map m = rawTransaction.m;
                String address;
                Double amount = (Double) m.get("amount");
                String confirmations = m.get("confirmations").toString();

                ArrayList<LinkedHashMap> detailsArray = (ArrayList) m.get("details");
                if (null != detailsArray && detailsArray.size() != 0) {
                    LinkedHashMap detailsMap = detailsArray.get(0);
                    address = detailsMap.get("address").toString();
                    System.out.println(address);
                } else {
                    throw new LogException("数据不全");
                }

                if (rawTransaction.confirmations() > 0) {
                    System.out.println(rawTransaction);
                    yangTibi = new YangTibi();
                    yangTibi.setAddTime(Long.valueOf(System.currentTimeMillis() / 1000).intValue());
                    yangTibi.setCheckTime(Long.valueOf(System.currentTimeMillis() / 1000).intValue());
                    yangTibi.setFee(BigDecimal.valueOf(0));
                    yangTibi.setHeight(String.valueOf(block.height()));
                    yangTibi.setMyurl("");
                    yangTibi.setName("");
                    yangTibi.setConfirmations(Integer.valueOf(confirmations));
                    yangTibi.setNum(BigDecimal.valueOf(amount));
                    yangTibi.setUrl(address);
                    yangTibi.setTiId(tx);
                    yangTibi.setStatus(Byte.valueOf("2"));
                    yangTibi.setActual(BigDecimal.valueOf(amount));
                    yangTibi.setCurrencyId(yangCurrency.getCurrencyId());
                    System.out.println("监测是否能够充值：------当前币种id" + yangTibi.getCurrencyId() + "---当前充值 url " + yangTibi.getUrl());
                    memberService.chongzhi(yangTibi);
                }

            } catch (Exception e) {
                BTC.logger.error(e.getMessage());
            }
        }

    }

    /**
     *
     * @param blockNumber
     */
    @Async
    public void confirm(BigInteger blockNumber)
    {
        try {

            System.out.println("开始确认之前充币是否到账---");

            String CurrencyIds = "";

            CurrencyIds += this.yangCurrency.getCurrencyId().toString();

            Result result = memberService.confirm(blockNumber, CurrencyIds,100);
            if(result.getCode().equals(Result.Code.SUCCESS) && result.getData() != null) {
                LinkedList<YangTibi> successList = new LinkedList<>();
                LinkedList<YangTibi> falseList = new LinkedList<>();
                List<YangTibi> list = new LinkedList<YangTibi>();
                list = cyweb.utils.CommonTools.json2Object((JSONArray) result.getData(), list, YangTibi.class);
                for (YangTibi t : list
                        ) {
                    try {
                        Bitcoin.RawTransaction rawTransaction = this.bitcoinJSONRPCClient.getTransaction(t.getTiId());
                        System.out.println(rawTransaction);
                        if(rawTransaction.confirmations() >= 0)
                        {
                            successList.add(t);
                        }
                    }catch (Exception e)
                    {
                        System.out.println("监测到错误信息：获取到的交易信息为失败的" + t.getTiId());
                        falseList.add(t);
                    }

                }
                Object o1 = JSONObject.toJSON(successList);
                Object o2 = JSONObject.toJSON(falseList);
                memberService.finishConfirm(blockNumber, o1.toString(), o2.toString(), 15);
                //然后和服务端再次教研 改变状态
            }

        }catch (Exception e)
        {
            e.printStackTrace();
            BaseCoin.logger.error(e.getMessage());
        }


    }


    @Override
    public BigInteger tranNeedPrice(BigInteger num, String from, String to) throws LogException {
        return null;
    }

    @Override
    public void safe() {

        try{
            BigDecimal balance = this.getAllBalance();
            BigDecimal needLeft = BigDecimal.valueOf(100000);
            if(balance.compareTo(needLeft) > 0) //如果大于 0.001 则全部把剩下的转出去
            {
                BaseCoin.logger.info("开始转账");
                this.sendTransaction2cold(null,balance.subtract(needLeft).doubleValue(),null);
            }
        }catch (Exception e)
        {
            e.printStackTrace();

        }
    }

    private  boolean lock() throws Exception
    {
        Object o =  this.bitcoinJSONRPCClient.query("walletlock",null);
        if(null == o)
        {
            return true;
        }else{
            return false;
        }
    }


    private  boolean unlock() throws Exception
    {
        Object o =  this.bitcoinJSONRPCClient.query("walletpassphrase","wang628625",30);
        if(o == null)
        {
            return true;
        }else{
            return false;
        }
    }

    @Override
    public boolean validateAddress(String address) {
        try{
            Bitcoin.AddressValidationResult addressValidationResult = this.bitcoinJSONRPCClient.validateAddress(address);
            return addressValidationResult.isValid();
        }catch (Exception e)
        {
            BTC.logger.error(e.getMessage());
        }
        return false;

    }



}
