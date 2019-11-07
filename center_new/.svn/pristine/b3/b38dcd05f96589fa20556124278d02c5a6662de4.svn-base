package net.cyweb.CoinUtils.CoinEntry;


import com.alibaba.fastjson.JSONObject;
import net.cyweb.CoinUtils.CoinEntry.Base.BaseCoin;
import net.cyweb.CoinUtils.CoinEntry.Base.BaseCoinI;
import net.cyweb.CoinUtils.CoinTools.CommonTools;
import net.cyweb.CoinUtils.CoinTools.SpringUtils;
import net.cyweb.CoinUtils.Exception.LogException;
import net.cyweb.feignClient.MemberService;
import net.cyweb.model.Result;
import net.cyweb.model.YangCurrency;
import net.cyweb.model.YangTibi;
import net.cyweb.model.YangTibiNew;
import net.cyweb.service.RedisService;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.admin.Admin;
import org.web3j.protocol.admin.methods.response.NewAccountIdentifier;
import org.web3j.protocol.admin.methods.response.PersonalUnlockAccount;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.protocol.geth.Geth;
import org.web3j.protocol.http.HttpService;
import rx.Subscription;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Future;

@Service
public class IMT extends BaseCoin implements BaseCoinI {

    protected Web3j web3;
    protected Admin admin;
    protected Geth geth;





    private Subscription subscription;

    private String blockKey;


    @Override
    public void ExtendBaseInfo(YangCurrency yangCurrency,RedisService redisService) throws LogException {
        this.yangCurrency = yangCurrency;
        this.config = new HashMap<>();
//        this.config.put("server","http://47.75.44.35"+":8547"); //钱包服务器地址

        this.config.put("server","http://"+yangCurrency.getRpcUrl()+":"+yangCurrency.getPortNumber()); //钱包服务器地址
        this.config.put("jd",yangCurrency.getCurrencyAccuracy().toString()); //钱包服务器地址

        this.web3 = Web3j.build(new HttpService(this.config.get("server")));
        this.admin = Admin.build(new HttpService(this.config.get("server")));
        this.geth = Geth.build(new HttpService(this.config.get("server")));




        this.blockKey = "blocknums-imt-"+yangCurrency.getCurrencyId();


        this.redisUtils = redisService;

        memberService = SpringUtils.getBean(MemberService.class);

    }

    @Override
    public List<String> getAllAccount() throws Exception {
        EthAccounts ethAccounts =  this.web3.ethAccounts().send();
        return ethAccounts.getAccounts();
    }

    @Override
    public BigInteger getBalance(String account) throws LogException {

        try {
            EthGetBalance ethGetBalance =   this.admin.ethGetBalance(account, DefaultBlockParameterName.LATEST).send();
            return  ethGetBalance.getBalance();
        }catch (Exception e)
        {
            throw  new LogException(e.getMessage());
        }

    }

    @Override
    public BigInteger getAllBalance() {
        return null;
    }

    @Override
    public String sendTransaction(String from, BigInteger num, String to, String passwd) throws LogException {

        try {
            PersonalUnlockAccount personalUnlockAccount =  this.admin.personalUnlockAccount(from,CommonTools.Md5(passwd)).send();
            if(personalUnlockAccount.getError() !=null)
            {
                throw new LogException(personalUnlockAccount.getError().getMessage());
            }
            if(personalUnlockAccount.accountUnlocked())
            {

                org.web3j.protocol.core.methods.request.Transaction transaction = new org.web3j.protocol.core.methods.request.Transaction(from,null,web3.ethGasPrice().send().getGasPrice(),null,to,num,null);
                EthSendTransaction ethSendTransaction = admin.ethSendTransaction(transaction).send();
                if(ethSendTransaction.getError()== null)
                {
                    return ethSendTransaction.getResult(); //返回转账has地址
                }else{
                    throw new Exception("转账失败！转账来源地址: "+from+"转账对象: "+to+" 转账金额 ："+num2real(num)+"账户余额："+num2real(this.getBalance(from))+"出错原因："+ethSendTransaction.getError().getMessage());
                }
            }else{
                throw new Exception("---------警告！！无法解锁用户，转账过程终止-----------");
            }
        }catch (Exception e)
        {
            throw new LogException(e.getMessage());
        }



    }

    @Override
    public String sendTransactionALl(String from, String to, String passwd) throws LogException {
        return null;
    }

    @Override
    public String sendTransaction2cold(String from, BigInteger num, String passwd) throws LogException {
        return null;
    }

    @Override
    public String sendTransaction2User(String to, BigInteger num, String passwd) throws LogException {
        return null;
    }

    @Override
    public String createUser(String passwd) throws LogException {
        try {
            NewAccountIdentifier newAccountIdentifier = admin.personalNewAccount(passwd).send();
            if(newAccountIdentifier.getError() != null)
            {
                throw new Exception("创建用户失败："+newAccountIdentifier.getError().getMessage());
            }
            return newAccountIdentifier.getResult();

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
    public Future<String> chargingMoneylistener() throws LogException {



        try{
            //第一次进入 这个时候 先把中间断掉的块处理掉

            if(this.redisUtils.get(this.blockKey) != null)
            {
                BigInteger lastpreNums = BigInteger.valueOf(Long.valueOf(this.redisUtils.get(this.blockKey).toString()));


                //直接开启监听，后面的代码对中间缺少的进行弥补

                subscription =  this.web3.blockObservable(false).subscribe(block -> {
                    try {
                        BigInteger nums = block.getBlock().getNumber();
                        this.updateUserCurrency(nums);
                        redisUtils.set(this.blockKey,nums);
                    }catch (Exception e){
                        BaseCoin.logger.error("监听过程出错:"+e.getMessage());
                    }

                });


                if(null!= lastpreNums)
                {
                    BigInteger block_new = this.web3.ethBlockNumber().send().getBlockNumber();

                    while (lastpreNums.compareTo(block_new) <= 0 )
                    {
                        this.updateUserCurrency(lastpreNums);
                        lastpreNums = lastpreNums.add(BigInteger.valueOf(1)); //曾加1
//                        redisUtils.set(this.blockKey,lastpreNums);

                    }
                }

            }else{
                subscription =  this.web3.blockObservable(false).subscribe(block -> {
                    try {
                        BigInteger nums = block.getBlock().getNumber();
                        this.updateUserCurrency(nums);
                        redisUtils.set(this.blockKey,nums);
                    }catch (Exception e){
                        BaseCoin.logger.error("监听过程出错:"+e.getMessage());
                    }

                });
            }

        }catch (Exception e)
        {
            BaseCoin.logger.error("监听过程出错:"+e.getMessage());
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("thread", Thread.currentThread().getName());
        jsonObject.put("time", System.currentTimeMillis());
        return new AsyncResult<String>(jsonObject.toJSONString());

    }









    /**
     * 处理用户信息
     * @param blocknum
     */
    @Async
    public void updateUserCurrency(BigInteger blocknum) throws Exception
    {
        YangTibi yangTibi = null;

        //记录下最新处理的高度 如果服务终端 下次重启的时候 可以把中间未处理的处理掉
        EthGetBlockTransactionCountByNumber ethGetBlockTransactionCountByNumber =  this.web3.ethGetBlockTransactionCountByNumber(DefaultBlockParameter.valueOf(blocknum)).send();
        for(int i = 0 ;i < ethGetBlockTransactionCountByNumber.getTransactionCount().intValue() ;i++)
        {

            EthTransaction ethTransaction = this.web3.ethGetTransactionByBlockNumberAndIndex(DefaultBlockParameter.valueOf(blocknum),BigInteger.valueOf(i)).send();
            Transaction transaction = ethTransaction.getTransaction().get();
            EthGetTransactionReceipt ethGetTransactionReceipt = this.web3.ethGetTransactionReceipt(transaction.getHash()).send();
            TransactionReceipt transactionReceipt = ethGetTransactionReceipt.getTransactionReceipt().get();
            transactionReceipt.getStatus();
            System.out.println("交易金额来源"+transactionReceipt.getFrom()+":对象"+transactionReceipt.getTo()+":状态:"+ transactionReceipt.getStatus());


            // TODO: 2018/4/13 和用户对比 如果有交易记录 则入账


            if(transactionReceipt.getStatus().equals("0x1"))
            {


                yangTibi = new YangTibi();

                yangTibi.setAddTime(Long.valueOf(System.currentTimeMillis()/1000).intValue());
                yangTibi.setCheckTime(Long.valueOf(System.currentTimeMillis()/1000).intValue());
                yangTibi.setFee(BigDecimal.valueOf(0));
                yangTibi.setHeight(String.valueOf(blocknum.intValue()));
                yangTibi.setMyurl(transactionReceipt.getFrom());
                yangTibi.setName("");
                yangTibi.setNum(BigDecimal.valueOf(this.num2real(transaction.getValue())));
                yangTibi.setUrl(transactionReceipt.getTo());
                yangTibi.setTiId(transactionReceipt.getTransactionHash());
                yangTibi.setStatus(Byte.valueOf("3"));
                yangTibi.setActual(BigDecimal.valueOf(this.num2real(transaction.getValue()).doubleValue()));
                yangTibi.setCurrencyId(this.yangCurrency.getCurrencyId());


                memberService.chongzhi(yangTibi);



            }else{
                System.out.println("其他状态"+transactionReceipt);
            }


        }


    }


    @Override
    public Result chongzhiByTx(String tx) throws LogException {
        return null;
    }

    @Override
    public BigInteger tranNeedPrice(BigInteger num, String from, String to) throws LogException {
        return null;
    }


    @Override
    public void chargingMoneylistenerDistory(YangCurrency yangCurrency) throws LogException {
        subscription.unsubscribe(); //取消监听
    }

    public static void main(String[] args)
    {
        IMT imt = new IMT();
        try {
//            String account = imt.createUser("wang628625");
//            System.out.println(account);

//            imt.sendTransaction("0x644930a4242cf969c193d6d4859e8aeb02fa4e33",imt.num2trance(BigInteger.valueOf(2)),"0x832275854ebae65ab4f0462209ce1ef107e5e35d","493840844@qq.com");

            //               baseCoinI.sendTransaction("0x644930a4242cf969c193d6d4859e8aeb02fa4e33",baseCoinI.num2trance(BigInteger.valueOf(2)),"0x832275854ebae65ab4f0462209ce1ef107e5e35d","493840844@qq.com");
//               baseCoinI.sendTransaction("0x644930a4242cf969c193d6d4859e8aeb02fa4e33",baseCoinI.num2trance(BigInteger.valueOf(2)),"0x94fbc99d08ac2970bde708215ee1a8148f942299","493840844@qq.com");
//               baseCoinI.sendTransaction("0x644930a4242cf969c193d6d4859e8aeb02fa4e33",baseCoinI.num2trance(BigInteger.valueOf(2)),"0xbd3f8a0dff2ba476d9e0202694dbaff26b869faa","493840844@qq.com");

//                baseCoinI.sendTransaction("0x644930a4242cf969c193d6d4859e8aeb02fa4e33",baseCoinI.num2trance(BigInteger.valueOf(1)),"0x5081fffa8687495eb8f7f28d8b87169a0ad13c79","493840844@qq.com");

//                baseCoinI.sendTransaction("0x5081fffa8687495eb8f7f28d8b87169a0ad13c79",baseCoinI.num2trance(BigInteger.valueOf(1)),"0x644930a4242cf969c193d6d4859e8aeb02fa4e33",null);


        }catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    @Override
    public void safe() {

    }


    @Override
    public String sendTransaction2UserBySystem(BigInteger num, String to) throws Exception {
        return null;
    }

    @Override
    public boolean validateAddress(String address) {
        return false;
    }

    @Override
    public Result test(HashMap pama) {
        //测试一下

        Result result = new Result();


        return result;

    }
}
