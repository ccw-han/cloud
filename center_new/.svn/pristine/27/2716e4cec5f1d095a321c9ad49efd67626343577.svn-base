package net.cyweb.CoinUtils.CoinEntry;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import net.cyweb.CoinApp;
import net.cyweb.CoinUtils.CoinEntry.Base.BaseCoin;
import net.cyweb.CoinUtils.CoinEntry.Base.BaseCoinI;
import net.cyweb.CoinUtils.CoinTools.CoinFactory;
import net.cyweb.CoinUtils.CoinTools.CommonTools;
import net.cyweb.CoinUtils.CoinTools.SpringUtils;
import net.cyweb.CoinUtils.Exception.LogException;
import net.cyweb.feignClient.MemberService;
import net.cyweb.model.Result;
import net.cyweb.model.YangCurrency;
import net.cyweb.model.YangTibi;
import net.cyweb.service.CoinService;
import net.cyweb.service.RedisService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.admin.Admin;
import org.web3j.protocol.admin.methods.response.NewAccountIdentifier;
import org.web3j.protocol.admin.methods.response.PersonalUnlockAccount;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.protocol.geth.Geth;
import org.web3j.protocol.http.HttpService;
import org.web3j.protocol.ipc.UnixIpcService;
import org.web3j.utils.Convert;
import rx.Subscription;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.Future;

import org.web3j.protocol.Service;

@Component
public class ETH extends EthBase implements BaseCoinI {


    private String blockKey;

    private String ETHC;

    private Subscription subscription;

    private int startnums = 0;

    private int error_num_re = 10; //允许错误重试次数

    private Map<String,Integer> errorMap;

    private BigInteger lastHeight; //最新高度 避免每次都去查高度

    @Value("${coin.coinServer-addr}")
    private String coinServer_addr;

    @Value("${coin.coinServer-port}")
    private int coinServer_port;

    public HashMap<String, String> getConfig() {
        return (HashMap<String, String>) this.config;
    }

    @Override
    public List<String> getAllAccount() {
        return null;
    }

    public ETH() {
        super();
        this.errorMap = new HashMap();
        BaseCoin.logger.info("---------start with 以太坊-------");

    }

    @Override

    public void ExtendBaseInfo(YangCurrency yangCurrency, RedisService redisService) throws LogException {

        this.yangCurrency = yangCurrency;

        this.config.put("server", "http://" + yangCurrency.getRpcUrl() + ":" + yangCurrency.getPortNumber()); //钱包服务器地址

        this.config.put("jd", yangCurrency.getCurrencyAccuracy().toString()); //钱包服务器地址

//        Service service = new UnixIpcService("/data/geth.ipc");

//        Service service = new HttpService("http://funcoin:wang628625@eth.funcoin.co");  //必须删除
        Service service = new HttpService("http://eth.funcoin.co");  //必须删除
        ((HttpService) service).addHeader("Authorization","Basic ZnVuY29pbjp3YW5nNjI4NjI1");

        this.web3 = Web3j.build(service);
        this.admin = Admin.build(service);
        this.geth = Geth.build(service);


//        System.out.println("gas_price"+this.num2real(this.gasPrice));

        this.redisUtils = redisService;

        memberService = SpringUtils.getBean(MemberService.class);
        coinService = SpringUtils.getBean(CoinService.class);


        this.blockKey = "blocknums-eth-" + yangCurrency.getCurrencyId(); //这个必须按照币种的名字来  要保证key是唯一的


    }

    @Override
    public BigInteger getBalance(String account) throws LogException {
        EthGetBalance ethGetBalance;
        try {
            ethGetBalance = (EthGetBalance) (web3.ethGetBalance(account, DefaultBlockParameterName.LATEST).send());
            if (ethGetBalance.getError() != null) {
                throw new LogException("查询用户: " + account + "余额失败：错误原因" + ethGetBalance.getError().getMessage());
            }
            BaseCoin.logger.info("查询到账号：" + account + "，余额" + this.num2real(ethGetBalance.getBalance()));

            return ethGetBalance.getBalance();

        } catch (Exception e) {
            throw new LogException("查询用户: " + account + "余额失败：错误原因" + e.getMessage());
        }

    }

    @Override
    public BigInteger getAllBalance() {

        return null;
    }

    @Override
    public String sendTransaction(String from, BigInteger num, String to, String passwd) throws LogException {
        try {

//            System.out.println("开始解锁--解锁地址是："+from);

            PersonalUnlockAccount personalUnlockAccount = admin.personalUnlockAccount(from, passwd).send(); // 先解锁


            if (personalUnlockAccount.getError() != null) {
                throw new LogException(personalUnlockAccount.getError().getMessage());
            }

            if (personalUnlockAccount.accountUnlocked()) {

//                System.out.println("开始转账--转账地址是："+from);
                Transaction transaction = new Transaction(from, null, this.gasPrice, BigInteger.valueOf(this.yangCurrency.getGasLimit()), to, num, null);
                EthSendTransaction ethSendTransaction = admin.ethSendTransaction(transaction).send();
                if (ethSendTransaction.getError() == null) {

                    String tx = ethSendTransaction.getResult(); //返回转账has地址
//                    System.out.println("转账成功，tx地址是"+tx);
                    return tx;
                } else {
                    throw new Exception("转账失败！转账来源地址: " + from + "转账对象: " + to + " 转账金额 ：" + num2real(num) + "账户余额：" + num2real(this.getBalance(from)) + "出错原因：" + ethSendTransaction.getError().getMessage());
                }
            } else {
                throw new Exception("---------警告！！无法解锁用户，转账过程终止-----------");
            }
        } catch (Exception e) {
            throw new LogException(e.getMessage());
        }

    }


    @Override
    public String sendTransactionALl(String from, String to, String passwd) throws LogException {
        try {
            BigInteger balance = this.getBalance(from); //改地址全部资金

            BigInteger balanceNeedTrad = this.tranNeedPrice(balance, from, to); //需要的手续费

            if (balance.compareTo(balanceNeedTrad) <= 0) //转出所需的资金和需要的手续费相等或者小于
            {
                throw new Exception("账户：from" + "资金太少，无转出意义：其余额：" + num2real(balance));

            }
            return this.sendTransaction(from, balance.subtract(balanceNeedTrad), to, passwd);
        } catch (Exception e) {
            throw new LogException(e.getMessage());
        }
    }

    /**
     * 专门用来 用户币全部转给冷钱包地址 to cold
     *
     * @param from
     * @param num
     * @param passwd
     * @return
     * @throws Exception
     */
    @Override
    public String sendTransaction2cold(String from, BigInteger num, String passwd) throws LogException {
        //先看看当前账户余额

//        System.out.println("当前账号---"+from);

        BigInteger bigInteger = this.getBalance(from);


//        System.out.println("总余额"+num2real(bigInteger) +"转出的冷钱包地址是"+this.config.get("coldAddress"));

        if (from.equals(this.systemAdd)) {
            System.out.println("系统地址，不做任何处理!");
            return "";
        }

        if (this.num2real(bigInteger) < this.yangCurrency.getMinNum()) {

//            System.out.println("太少！！放弃操作,最少数量"+this.yangCurrency.getMinNum());
            return null;
        }


        System.out.println("总余额" + num2real(bigInteger) + "转出的冷钱包地址是" + this.config.get("coldAddress"));

        BigInteger totalgas = this.tranNeedPrice(bigInteger, from, this.config.get("coldAddress"));

        //获取转出需要多少气

        if (bigInteger.compareTo(totalgas) > 0) {

            if (num == null) //如果不指定数量 则尽可能转
            {
                num = bigInteger.subtract(totalgas);
            }

            return this.sendTransaction(from, num, this.config.get("coldAddress"), passwd);
        } else {
            return null;
        }


    }


    @Override
    public String sendTransaction2User(String to, BigInteger num, String passwd) throws LogException {
        return this.sendTransaction(this.config.get("activityAddress"), num, to, passwd);
    }

    /**
     * 转代币气不足的时候 转币
     *
     * @param num
     * @param to
     * @return
     * @throws Exception
     */

    public String sendTransaction2UserBySystem(BigInteger num, String to) throws Exception {

        return this.sendTransaction(this.systemAdd, num, to, CommonTools.Md5(this.systemEmail));

    }


    @Override
    public String createUser(String passwd) throws LogException {
        try {
            System.out.println("钱包开始创建用户--start");
            NewAccountIdentifier newAccountIdentifier = admin.personalNewAccount(passwd).send();
            System.out.println("钱包开始创建用户--end");
            if (newAccountIdentifier.getError() != null) {
                throw new Exception("创建用户失败：" + newAccountIdentifier.getError().getMessage());
            }
            return newAccountIdentifier.getResult();
        } catch (Exception e) {
            throw new LogException(e.getMessage());
        }

    }

    @Override
    public boolean changUser(String account, String passwd) throws LogException {
        // TODO: 2018/3/31 暂时没用找到对应的方法
        return false;
    }

    @Override
    @Async
    public Future<String> chargingMoneylistener() throws LogException {

        System.out.println("redis实例是" + this.redisUtils);


            while (true) {
                try {
                    doChargingMoneylistener();
                    Thread.sleep(10000);

            }catch (Exception e) {
                    ETH.logger.info(e.getMessage());
                }
        }
//
//        JSONObject jsonObject = new JSONObject();
//        jsonObject.put("thread", Thread.currentThread().getName());
//        jsonObject.put("time", System.currentTimeMillis());
//        return new AsyncResult<String>(jsonObject.toJSONString());


    }

    public void doChargingMoneylistener() throws Exception {
        EthBlock block = null;
        BigInteger lastpreNums = BigInteger.ZERO;
        BigInteger blockNow;
        try {
            this.startnums++;

            //第一次进入 这个时候 先把中间断掉的块处理掉
            BaseCoin.logger.info("-----开始第" + startnums + "次处理-----");


//            //测试用 必须删除
//             blockNow = this.web3.ethGetBlockByNumber(DefaultBlockParameter.valueOf(BigInteger.valueOf(6190510l)),false).send().getBlock().getNumber();
//             block =  this.web3.ethGetBlockByNumber(DefaultBlockParameter.valueOf(blockNow),false).send();
//             this.updateUserCurrency(block.getBlock());
//
//             Thread.sleep(3000);

//
//            this.confirm(this.web3.ethGetBlockByNumber(DefaultBlockParameter.valueOf(BigInteger.valueOf(5621349l)),false).send().getBlock());

            blockNow = this.web3.ethBlockNumber().send().getBlockNumber();


            if (this.redisUtils.get(this.blockKey) != null) {
                lastpreNums = BigInteger.valueOf(Long.valueOf(this.redisUtils.get(this.blockKey).toString()));

                if(lastHeight == null)
                {
                    lastHeight = blockNow;
                }





                if (lastpreNums.compareTo(lastHeight) == 0) {
                    System.out.println("已经到达最新高度！要不停下来休息下 等等他 ！先休息个30s");
                    Thread.sleep(30000);
                    blockNow = this.web3.ethBlockNumber().send().getBlockNumber();
                    lastHeight = blockNow;


                } else {
                    while (true) {

                        if (lastpreNums.compareTo(lastHeight) >= 0) {
                            System.out.println("校准当前高度为" + lastHeight);
                            redisUtils.set(this.blockKey, lastHeight);
                            break;
                        }
                        System.out.println("当前处理高度"+lastpreNums);

                        block = this.web3.ethGetBlockByNumber(DefaultBlockParameter.valueOf(lastpreNums), false).send();

                        this.confirm(block.getBlock());

                        this.updateUserCurrency(block.getBlock());

                        lastpreNums = lastpreNums.add(BigInteger.valueOf(1));
                    }
                }

            }else{

                redisUtils.set(this.blockKey, blockNow);

            }


        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("出现异常重新处理");

            if(this.errorMap.containsKey(lastpreNums.toString()))
            {
                Integer err = this.errorMap.get(lastpreNums);
                this.errorMap.put(lastpreNums.toString(),++err);
                System.out.println("高度"+lastpreNums+":错误次数"+err);
            }else{
                System.out.println("高度"+lastpreNums+":错误次数"+1);
                this.errorMap.put(lastpreNums.toString(),1);
            }




        }
    }

    /**
     * 对于平台中的地址 一直循环处理  把币转到冷钱包
     */

    public void safe() {

        List<YangCurrency> list = new LinkedList<>();
        list.add(this.yangCurrency);

        for (YangCurrency y : list //获取全部用户的账户
        ) {
            Result result = memberService.getMemberCurrencyList(y.getCurrencyId());

            try {
                if (result.getCode().equals(Result.Code.SUCCESS)) {
                    JSONArray jsonArray = (JSONArray) ((Result) result).getData();

                    if (jsonArray != null) {
                        for (Object o : jsonArray
                        ) {
                            try {
                                String email = ((JSONObject) o).get("email").toString();
                                String chongzhiUrl = ((JSONObject) o).get("chongzhiUrl").toString();
                                if (null != y.getBean()) {
                                    this.sendTransaction2cold(chongzhiUrl, null, CommonTools.Md5(email));
                                }
                            } catch (Exception e) {
                                ETH.logger.error("找不到充值url 或者 email");
                            }


                        }
                    }

                }
            } catch (Exception e) {
                BaseCoin.logger.error(e.getMessage());
//                    e.printStackTrace();

            }


        }
    }


    /**
     * 处理用户信息
     *
     * @param block
     */
//    @Async
    public void updateUserCurrency(EthBlock.Block block) throws Exception {

        System.out.println("当前处理高度----" + block.getNumber());

        YangTibi yangTibi = null;

        EthGetTransactionReceipt ethGetTransactionReceipt = null;

        TransactionReceipt transactionReceipt = null;

        org.web3j.protocol.core.methods.response.Transaction transaction = null;

        List<EthBlock.TransactionResult> list = block.getTransactions();

//        System.out.println("交易记录 工"+list.size());

//        HashMap<String,YangCurrency> hashMap =  this.getAddress(this.yangCurrency.getCurrencyId().toString());  //获取这个币种主币和代币的地址


        for (EthBlock.TransactionResult transactionResult : list) {
            try {

                ethGetTransactionReceipt = this.web3.ethGetTransactionReceipt(transactionResult.get().toString()).send();

                transactionReceipt = ethGetTransactionReceipt.getTransactionReceipt().get();



                transaction = this.web3.ethGetTransactionByHash(transactionResult.get().toString()).send().getTransaction().get();

                String url = transactionReceipt.getTo();

                if (transactionReceipt.getFrom().equals(this.systemAdd)) {
                    System.out.println("系统入账 不做记录!!");
                    continue;
                }


                String input = transaction.getInput();
                if (!input.equals("0x") && input.length() == 138) {

                    String method = input.substring(2, 10);

                    if (method.equals("a9059cbb")) //说明是代币
                    {




                        //先获取到当前是哪个币的对象
                        String contractAddr = transaction.getTo();

                        YangCurrency yangCurrency = new YangCurrency();
                        yangCurrency.setExtenInfo(contractAddr);
                        yangCurrency.setMainId(this.yangCurrency.getCurrencyId().toString());


                        yangCurrency = coinService.selectOne(yangCurrency);

                        if (null == yangCurrency) {
                            continue;
                        }

                        if (yangCurrency.getChongbiOpen().intValue() == 0) {
                            continue; //如果没有开启充币 则充币不到账
                        }

                        BaseCoinI baseCoinI = CoinFactory.createCoin(yangCurrency, this.redisUtils);


                        baseCoinI.chongzhiByTx(transactionResult.get().toString());

                        System.out.println("监测是否能够充值-是个代币：------当前币种id" + yangTibi.getCurrencyId() + "---当前充值 url " + yangTibi.getUrl());

                        continue;

                    }


                }

            } catch (Exception e) {
                e.printStackTrace();
            }


            if (null != transactionReceipt) {
//                System.out.println("状态为"+transactionReceipt.getStatus());

                if (transactionReceipt.getStatus().equals("0x1")) {
//                    System.out.println("交易金额来源"+transactionReceipt.getFrom()+":对象"+transactionReceipt.getTo()+":状态:"+ transactionReceipt.getStatus());
                    try {
                        yangTibi = new YangTibi();
                        yangTibi.setAddTime(Long.valueOf(System.currentTimeMillis() / 1000).intValue());
                        yangTibi.setCheckTime(Long.valueOf(System.currentTimeMillis() / 1000).intValue());
                        yangTibi.setFee(BigDecimal.valueOf(0));
                        yangTibi.setHeight(String.valueOf(transactionReceipt.getBlockNumber()));
                        yangTibi.setMyurl(transactionReceipt.getFrom());
                        yangTibi.setName("");
                        yangTibi.setConfirmations(0);

                        yangTibi.setNum(BigDecimal.valueOf(this.num2real(transaction.getValue())));
                        yangTibi.setUrl(transactionReceipt.getTo());
                        yangTibi.setTiId(transactionReceipt.getTransactionHash());
                        yangTibi.setStatus(Byte.valueOf("2"));
                        yangTibi.setActual(BigDecimal.valueOf(this.num2real(transaction.getValue()).doubleValue()));
                        yangTibi.setCurrencyId(this.yangCurrency.getCurrencyId());

//                    System.out.println("监测是否能够充值：------当前币种id"+yangTibi.getCurrencyId()+"---当前充值 url "+yangTibi.getUrl());


                        memberService.chongzhi(yangTibi);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }

            } else {
                System.out.println("交易凭据为null");
            }

        }


        redisUtils.set(this.blockKey, block.getNumber());  //记录高度

    }

    /**
     * 确认是否满足30个到账
     *
     * @param block
     */
//    @Async
    public void confirm(EthBlock.Block block) {
        try {

            System.out.println("开始确认之前充币是否到账---");
            BigInteger blockNumber = block.getNumber();

            String CurrencyIds = "";

            org.web3j.protocol.core.methods.response.Transaction transaction;

            CurrencyIds += this.yangCurrency.getCurrencyId().toString();

            //找到自己的currencyId和自己的代币currencyId

            YangCurrency yangCurrency = new YangCurrency();
            yangCurrency.setMainId(String.valueOf(this.yangCurrency.getCurrencyId()));

            PageHelper.startPage(1, 20000);
            List<YangCurrency> yangCurrencyList2 = coinService.select(yangCurrency);



            if (yangCurrencyList2 != null) {
                for (YangCurrency y : yangCurrencyList2
                ) {
                    CurrencyIds += "," + y.getCurrencyId().toString();
                }
            }
            System.out.println("需要确认的币种数据"+CurrencyIds);
            Result result = memberService.confirm(blockNumber, CurrencyIds, 10);
            System.out.println("确认充值数据"+result);


            if (result.getCode().equals(Result.Code.SUCCESS) && result.getData() != null) {
                LinkedList<YangTibi> successList = new LinkedList<>();
                LinkedList<YangTibi> falseList = new LinkedList<>();
                List<YangTibi> list = new LinkedList<YangTibi>();
                list = cyweb.utils.CommonTools.json2Object((JSONArray) result.getData(), list, YangTibi.class);
                System.out.println("具体确认数据"+list);
                for (YangTibi t : list
                ) {
                    TransactionReceipt transactionReceipt = web3.ethGetTransactionReceipt(t.getTiId()).send().getTransactionReceipt().get();

                    System.out.println("开始确认数据"+t.getTiId());

                    if (transactionReceipt.getStatus().equals("0x1")) {

                        transaction = this.web3.ethGetTransactionByHash(t.getTiId()).send().getTransaction().get();

                        String input = transaction.getInput();

                        if (!input.equals("0x") && input.length() == 138) {

                            String method = input.substring(2, 10);

                            if (method.equals("a9059cbb")) //说明是代币
                            {

                                //判断当前的代币是否是有问题的
                                List<Log> list1 = transactionReceipt.getLogs();
                                if(list1.size()==0)
                                {
                                    System.out.println("异常:"+transactionReceipt.getBlockHash()+":tx="+t.getTiId().toString());
                                    falseList.add(t);
                                }else{
                                    successList.add(t);
                                }
                            }
                        }else{

                            successList.add(t);
                        }

                    } else {



                        System.out.println("监测到错误信息：获取到的交易信息为失败的" + t.getTiId());
//                        falseList.add(t);

                    }


                }
                Object o1 = JSONObject.toJSON(successList);
                Object o2 = JSONObject.toJSON(falseList);
                memberService.finishConfirm(block.getNumber(), o1.toString(), o2.toString(), 10);
                //然后和服务端再次教研 改变状态


            }


        } catch (Exception e) {
            e.printStackTrace();
            BaseCoin.logger.error(e.getMessage());
        }


    }


    @Override
    public Result chongzhiByTx(String tx) throws LogException {

        Result result = new Result();

        result.setCode(Result.Code.ERROR);

        try {

            YangTibi yangTibi = null;

            EthGetTransactionReceipt ethGetTransactionReceipt;

            TransactionReceipt transactionReceipt = null;

            org.web3j.protocol.core.methods.response.Transaction transaction = null;

            ethGetTransactionReceipt = this.web3.ethGetTransactionReceipt(tx).send();

            transactionReceipt = ethGetTransactionReceipt.getTransactionReceipt().get();

            transaction = this.web3.ethGetTransactionByHash(tx).send().getTransaction().get();

            String url = transactionReceipt.getTo();

            if (transactionReceipt.getFrom().equals(this.systemAdd)) {
                System.out.println("系统入账 不做记录!!");

            }

            String input = transaction.getInput();
            if (!input.equals("0x") && input.length() == 138) {

                String method = input.substring(2, 10);

                if (method.equals("a9059cbb")) //说明是代币
                {
                    //先获取到当前是哪个币的对象
                    String contractAddr = transaction.getTo();

                    YangCurrency yangCurrency = new YangCurrency();
                    yangCurrency.setExtenInfo(contractAddr);
                    yangCurrency.setMainId(this.yangCurrency.getCurrencyId().toString());


                    yangCurrency = coinService.selectOne(yangCurrency);

                    if (null == yangCurrency) {
                        result.setMsg("找不到交易对信息");
                        return result;
                    }

                    if (yangCurrency.getChongbiOpen().intValue() == 0) {
                        result.setMsg("该交易对关闭充值");
                        return result; //如果没有开启充币 则充币不到账
                    }

                    BaseCoinI baseCoinI = CoinFactory.createCoin(yangCurrency, this.redisUtils);


                    result = baseCoinI.chongzhiByTx(tx);

                    System.out.println("监测是否能够充值-是个代币：------当前币种id" + yangTibi.getCurrencyId() + "---当前充值 url " + yangTibi.getUrl());

                    result.setCode(Result.Code.SUCCESS);
                    return result;

                }


            }


            if (null != transactionReceipt) {
//                System.out.println("状态为"+transactionReceipt.getStatus());

                if (transactionReceipt.getStatus().equals("0x1")) {
//                    System.out.println("交易金额来源"+transactionReceipt.getFrom()+":对象"+transactionReceipt.getTo()+":状态:"+ transactionReceipt.getStatus());

                    yangTibi = new YangTibi();
                    yangTibi.setAddTime(Long.valueOf(System.currentTimeMillis() / 1000).intValue());
                    yangTibi.setCheckTime(Long.valueOf(System.currentTimeMillis() / 1000).intValue());
                    yangTibi.setFee(BigDecimal.valueOf(0));
                    yangTibi.setHeight(String.valueOf(transactionReceipt.getBlockNumber()));
                    yangTibi.setMyurl(transactionReceipt.getFrom());
                    yangTibi.setName("");
                    yangTibi.setConfirmations(0);

                    yangTibi.setNum(BigDecimal.valueOf(this.num2real(transaction.getValue())));
                    yangTibi.setUrl(transactionReceipt.getTo());
                    yangTibi.setTiId(transactionReceipt.getTransactionHash());
                    yangTibi.setStatus(Byte.valueOf("2"));
                    yangTibi.setActual(BigDecimal.valueOf(this.num2real(transaction.getValue()).doubleValue()));
                    yangTibi.setCurrencyId(this.yangCurrency.getCurrencyId());
//                    System.out.println("监测是否能够充值：------当前币种id"+yangTibi.getCurrencyId()+"---当前充值 url "+yangTibi.getUrl());
                    memberService.chongzhi(yangTibi);
                }


            } else {
                result.setMsg("交易凭据为null");
                System.out.println("交易凭据为null");
            }


        } catch (Exception e) {
            e.printStackTrace();
            result.setMsg(e.getMessage());
        }
        return null;
    }

    /**
     * 测试需要多少手续费
     *
     * @param num
     * @param from
     * @param to
     * @return
     * @throws Exception
     */
    public BigInteger tranNeedPrice(BigInteger num, String from, String to) throws LogException {
        try {

            Transaction transaction = new Transaction(from, null, this.gasPrice, BigInteger.valueOf(this.yangCurrency.getGasLimit()), to, num, null);
            EthEstimateGas ethEstimateGas = admin.ethEstimateGas(transaction).send();
//            BigInteger amount = ethEstimateGas.getAmountUsed().add(BigInteger.valueOf(1));
            BigInteger amount = BigInteger.valueOf(this.yangCurrency.getGasLimit());

            System.out.println("ETHgasNums" + amount);
            System.out.println("手续费" + this.num2real(amount.multiply(this.gasPrice)));


            return this.gasPrice.multiply(amount);
        } catch (Exception e) {
            throw new LogException(e.getMessage());
        }

    }


    @Override
    public void chargingMoneylistenerDistory(YangCurrency yangCurrency) throws LogException {

    }


    @Override
    public boolean validateAddress(String address) {
        return true;
    }

    public static void main(String[] args) {

    }

    @Override
    public Result test(HashMap pama) {
        //测试一下

        Result result = new Result();

        try {
            String tx = pama.get("tx").toString();

//            TransactionReceipt transactionReceipt =  web3.ethGetTransactionReceipt(tx).send().getTransactionReceipt().get();

            org.web3j.protocol.core.methods.response.Transaction transaction = web3.ethGetTransactionByHash(tx).send().getTransaction().get();

            List<Log> logList = web3.ethGetTransactionReceipt(tx).send().getTransactionReceipt().get().getLogs();

            System.out.println("logLisg:"+logList);

            for (Log l : logList
            ) {
                System.out.println(l);
            }

            result.setData(logList);

        } catch (Exception e) {
            e.printStackTrace();
        }


        return result;

    }
}
