package net.cyweb.CoinUtils.CoinEntry;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
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
import net.cyweb.model.YangTibiNew;
import net.cyweb.service.CoinService;
import net.cyweb.service.RedisService;
import org.apache.commons.lang.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.RawTransaction;
import org.web3j.protocol.Service;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.admin.Admin;
import org.web3j.protocol.admin.methods.response.NewAccountIdentifier;
import org.web3j.protocol.admin.methods.response.PersonalUnlockAccount;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.protocol.geth.Geth;
import org.web3j.protocol.http.HttpService;
import org.web3j.protocol.ipc.UnixIpcService;
import org.web3j.utils.Collection;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Future;

@Component
public class EthContract extends EthBase implements BaseCoinI {


    private String blockKey;

    private String contactAddress;

    private BaseCoinI baseCoinIEth;




    @Override
    public void ExtendBaseInfo(YangCurrency yangCurrency, RedisService redisService) throws LogException {


        this.yangCurrency = yangCurrency;



        this.config.put("server","http://"+yangCurrency.getRpcUrl()+":"+yangCurrency.getPortNumber()); //钱包服务器地址

        this.config.put("jd",yangCurrency.getCurrencyAccuracy().toString()); //钱包服务器地址

//        Service service = new HttpService("http://funcoin:wang628625@eth.funcoin.co");  //必须删除

        Service service = new HttpService("http://eth.funcoin.co");  //必须删除
        ((HttpService) service).addHeader("Authorization","Basic ZnVuY29pbjp3YW5nNjI4NjI1");

//        Service service = new UnixIpcService("/data/geth.ipc");

        this.web3 = Web3j.build(service);
        this.admin = Admin.build(service);
        this.geth = Geth.build(service);
        this.redisUtils = redisService;

        memberService = SpringUtils.getBean(MemberService.class);
        coinService = SpringUtils.getBean(CoinService.class);


        this.contactAddress = yangCurrency.getExtenInfo();

        this.blockKey = "blocknums-ethc-"+yangCurrency.getCurrencyId()+"-"+yangCurrency.getExtenInfo(); //这个必须按照币种的名字来  要保证key是唯一的


        try {
            System.out.println("eth基础初始化开始");
            this.baseCoinIEth =  CoinFactory.createCoin(coinService.getYcByCid(Integer.valueOf(37)),redisService);
            System.out.println("eth基础初始化成功");

            this.gasPrice = this.web3.ethGasPrice().send().getGasPrice();
//            this.gasPrice = Convert.toWei("25", Convert.Unit.GWEI).toBigInteger();


        }catch (Exception e)
        {
            e.printStackTrace();
        }



    }

    @Override
    public List<String> getAllAccount() {
        return null;
    }

    @Override
    public BigInteger getBalance(String account) throws LogException {

        try {
            String data = "0x70a08231"+ StringUtils.leftPad(account.substring(2),64,"0"); //查询
            Transaction transaction = new Transaction(account,null,null,null,this.contactAddress, BigInteger.ZERO,data);
            EthCall o = (EthCall) web3.ethCall(transaction, DefaultBlockParameterName.LATEST).send();
            String nums = o.getValue();
            BigInteger bigInteger = Numeric.toBigInt(nums);
            return  bigInteger;
        }catch (Exception e)
        {
            BaseCoin.logger.error(e.getMessage());
            throw new LogException(e.getMessage());
        }


    }

    @Override
    public BigInteger getAllBalance() {
        return null;
    }

    @Override
    public String sendTransaction(String from, BigInteger num, String to, String passwd) throws LogException {

        try {
        EthGetTransactionCount ethGetTransactionCount = this.web3.ethGetTransactionCount(
                    from, DefaultBlockParameterName.LATEST).sendAsync().get();
        BigInteger nonce = ethGetTransactionCount.getTransactionCount();



        Function function = new Function("transfer", Arrays.asList(new Address(to),new Uint256(num)),  Arrays.asList(new TypeReference<Address>(){}, new TypeReference<Uint256>(){}));
        String encodedFunction = FunctionEncoder.encode(function);

        Transaction transaction = Transaction.createFunctionCallTransaction(from,nonce,this.gasPrice, BigInteger.valueOf(this.yangCurrency.getGasLimit()),this.contactAddress,BigInteger.ZERO,encodedFunction);


        PersonalUnlockAccount personalUnlockAccount = this.admin.personalUnlockAccount(from, passwd).send();


        if(personalUnlockAccount.accountUnlocked())
        {
            org.web3j.protocol.core.methods.response.EthSendTransaction o =  web3.ethSendTransaction(transaction).sendAsync().get();

//            EthSendTransaction o = (EthSendTransaction)web3.ethSendTransaction(transaction).send();

            if(((EthSendTransaction) o).getError() == null)
            {
                System.out.println("转账成功，tx地址是"+o.getResult());
                return o.getResult();
            }else{

                System.out.println("错误码"+o.getError().getCode());
                System.out.println("错误原因"+o.getError().getMessage());
                System.out.println("错误数据"+o.getError().getData());

                if(-32000 == o.getError().getCode())
                {

                    System.out.println("气不足，需要从以太坊转币到这个地址");

                    BigInteger needPrice = this.tranNeedPrice(num,from,this.config.get("coldAddress"));
                    String tx_xx =  this.baseCoinIEth.sendTransaction2UserBySystem(needPrice,from);
                    System.out.println("转气成功"+tx_xx);
                    // TODO: 2018/5/1 通知以太坊实例转币

                }

                throw new Exception("转账失败！转账来源地址: "+from+"转账对象: "+to+" 转账金额 ："+num2real(num)+"账户余额："+num2real(this.getBalance(from))+"出错原因："+o.getError().getMessage());
            }

        }else{
            throw new Exception("---------警告！！无法解锁用户，转账过程终止-----------");
        }

        } catch (Exception e) {
            throw new LogException(e.getMessage());
        }

    }

    @Override
    public String sendTransactionALl(String from, String to, String passwd) throws LogException {
        return null;
    }

    @Override
    public String sendTransaction2cold(String from, BigInteger num, String passwd) throws LogException {
        BigInteger bigInteger;
        try {
            if(num == null)
            {
                 bigInteger = this.getBalance(from);

            }else{
                 bigInteger = num;
            }
            if(bigInteger == null)
            {
                System.out.println("转出数量为空");
                return null;
            }
            System.out.println("当前数量"+this.num2real(bigInteger)+"最少转出数量"+this.yangCurrency.getMinNum());
            if(this.num2real(bigInteger) <= this.yangCurrency.getMinNum())
            {
                System.out.println("放弃转出，数量太少，当前数量"+this.num2real(bigInteger)+"最少转出数量"+this.yangCurrency.getMinNum());
                return null;
            }

            System.out.println("------开始转账---------wdj");

            if(null != bigInteger && bigInteger.compareTo(BigInteger.ZERO) > 0  )
            {
                BigInteger needPrice = this.tranNeedPrice(bigInteger,from,this.config.get("coldAddress"));


                if(this.baseCoinIEth.getBalance(from).compareTo(needPrice) < 0) //手续费不足
                {
                    System.out.println("-------地址气不足，从热钱包转气过来-------");

                    String tx_xx =  this.baseCoinIEth.sendTransaction2UserBySystem(needPrice,from);

                    System.out.println("手续费转账成功！等待后续重试，tx = "+tx_xx);

                    return tx_xx;

                }

            }


//            bigInteger = bigInteger.subtract(this.num2trance(BigDecimal.valueOf(1)));

            if(null != bigInteger)
            {
//                System.out.println("开始转账代币----币种"+this.yangCurrency.getCurrencyName()+"--币种id"+this.yangCurrency.getCurrencyId()+"币种余额"+this.num2real(bigInteger));

                if(bigInteger.compareTo(BigInteger.ZERO) > 0)  //全部转到冷钱包
                {

                    this.sendTransaction(from,bigInteger,this.config.get("coldAddress"),passwd);
                }

            }


        }catch (Exception e)
        {

            e.printStackTrace();
            System.out.println(e.getMessage());
//            throw new LogException(e.getMessage());
        }

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
        try {

        }catch (Exception e)
        {

        }
        return null;

    }


    @Async
    public void updateUserCurrency(String tx) throws Exception
    {
        YangTibi yangTibi = null;

        EthGetTransactionReceipt ethGetTransactionReceipt;

        TransactionReceipt transactionReceipt;

        org.web3j.protocol.core.methods.response.Transaction transaction;

        transaction = this.web3.ethGetTransactionByHash(tx).send().getTransaction().get();
        ethGetTransactionReceipt = this.web3.ethGetTransactionReceipt(tx).send();
        transactionReceipt = ethGetTransactionReceipt.getTransactionReceipt().get();

        String input = transaction.getInput();

        String to =  input.substring(10,68).substring(24);

        String contractAddr = transaction.getTo();

        Double nums =  num2real( new BigInteger(input.substring(74,input.length()),16));


        if(transactionReceipt.getStatus().equals("0x1"))
        {
            System.out.println("交易金额来源"+transactionReceipt.getFrom()+":对象"+transactionReceipt.getTo()+":状态:"+ transactionReceipt.getStatus());

            yangTibi = new YangTibi();
            yangTibi.setAddTime(Long.valueOf(System.currentTimeMillis()/1000).intValue());
            yangTibi.setCheckTime(Long.valueOf(System.currentTimeMillis()/1000).intValue());
            yangTibi.setFee(BigDecimal.valueOf(0));
            yangTibi.setHeight(String.valueOf(transactionReceipt.getBlockNumber()));
            yangTibi.setMyurl(transactionReceipt.getFrom());
            yangTibi.setName("");
            yangTibi.setConfirmations(0);
            yangTibi.setNum(BigDecimal.valueOf((nums)));
            yangTibi.setUrl(to);
            yangTibi.setTiId(transactionReceipt.getTransactionHash());
            yangTibi.setStatus(Byte.valueOf("2"));
            yangTibi.setActual(BigDecimal.valueOf((nums)));
            yangTibi.setCurrencyId(this.yangCurrency.getCurrencyId());
            memberService.chongzhi(yangTibi);

        }


    }

    @Override
    public Result chongzhiByTx(String tx) throws LogException {
        Result result = new Result();
        result.setCode(Result.Code.ERROR);
        try {
            YangTibi yangTibi = null;

            EthGetTransactionReceipt ethGetTransactionReceipt;

            TransactionReceipt transactionReceipt;

            org.web3j.protocol.core.methods.response.Transaction transaction;

            transaction = this.web3.ethGetTransactionByHash(tx).send().getTransaction().get();
            ethGetTransactionReceipt = this.web3.ethGetTransactionReceipt(tx).send();
            transactionReceipt = ethGetTransactionReceipt.getTransactionReceipt().get();

            String input = transaction.getInput();

            String to =  "0x"+input.substring(10,74).substring(24);

            String contractAddr = transaction.getTo();

            Double nums =  num2real( new BigInteger(input.substring(74,input.length()),16));

            System.out.println("检测到代币充币信息 开始充币");
            if(transactionReceipt.getStatus().equals("0x1"))
            {
                System.out.println("交易金额来源"+transactionReceipt.getFrom()+":对象"+transactionReceipt.getTo()+":状态:"+ transactionReceipt.getStatus());
                yangTibi = new YangTibi();
                yangTibi.setAddTime(Long.valueOf(System.currentTimeMillis()/1000).intValue());
                yangTibi.setCheckTime(Long.valueOf(System.currentTimeMillis()/1000).intValue());
                yangTibi.setFee(BigDecimal.valueOf(0));
                yangTibi.setHeight(String.valueOf(transactionReceipt.getBlockNumber()));
                yangTibi.setMyurl(transactionReceipt.getFrom());
                yangTibi.setName("");
                yangTibi.setConfirmations(0);
                yangTibi.setNum(BigDecimal.valueOf((nums)));
                yangTibi.setUrl(to);
                yangTibi.setTiId(transactionReceipt.getTransactionHash());
                yangTibi.setStatus(Byte.valueOf("2"));
                yangTibi.setActual(BigDecimal.valueOf((nums)));
                yangTibi.setCurrencyId(this.yangCurrency.getCurrencyId());
                System.out.println("充币开始");
                memberService.chongzhi(yangTibi);
                System.out.println("充币完成");
                result.setCode(Result.Code.SUCCESS);

            }
        }catch (Exception e)
        {
            result.setMsg(e.getMessage());
            throw new LogException(e.getMessage());

        }
        return result;
    }

    @Override
    public BigInteger tranNeedPrice(BigInteger num, String from, String to) throws LogException {
        try {

//            EthGetTransactionCount ethGetTransactionCount = this.web3.ethGetTransactionCount(from, DefaultBlockParameterName.LATEST).send();
//
//            BigInteger nonce = ethGetTransactionCount.getTransactionCount();
//
//
//            Function function = new Function("transfer", Arrays.asList(new Address(to),new Uint256(num)),  Arrays.asList(new TypeReference<Address>(){}, new TypeReference<Uint256>(){}));
//
//            String encodedFunction = FunctionEncoder.encode(function);
//
//            Transaction transaction = Transaction.createFunctionCallTransaction(from,nonce,this.gasPrice, BigInteger.valueOf(200000),this.contactAddress,BigInteger.ZERO,encodedFunction);
//
//
//            EthEstimateGas ethEstimateGas = web3.ethEstimateGas(transaction).send();
//
//            BigInteger gasNums = ethEstimateGas.getAmountUsed();
//
//            System.out.println("AmountUsed"+gasNums);


//            BigInteger amount = gasNums.add(BigInteger.valueOf(1));

            BigInteger amount = BigInteger.valueOf(this.yangCurrency.getGasLimit());



            System.out.println("需要消耗的气"+amount+"="+amount.doubleValue());
            System.out.println("手续费"+amount.multiply(this.gasPrice)+"等于"+this.num2real(amount.multiply(this.gasPrice)));

            return this.gasPrice.multiply(amount).multiply(BigInteger.valueOf(2));

        }catch (Exception e)
        {
            e.printStackTrace();
            throw new LogException(e.getMessage());
        }

    }

    @Override
    public void chargingMoneylistenerDistory(YangCurrency funCurrency) throws LogException {

    }

    @Override
    @Async
    public void safe() {

        Result result = memberService.getMemberCurrencyList(this.yangCurrency.getCurrencyId());

        System.out.println(result);

        try{
            if(result.getCode().equals(Result.Code.SUCCESS))
            {
                JSONArray jsonArray = (JSONArray) ((Result) result).getData();

                if(jsonArray != null)
                {
                    for (Object o :jsonArray
                            ) {
                        try {
                            String email = ((JSONObject) o).get("email").toString();
                            String chongzhiUrl = ((JSONObject) o).get("chongzhiUrl").toString();

                            if(null != this.yangCurrency.getBean())
                            {
                                this.sendTransaction2cold(chongzhiUrl,null, CommonTools.Md5(email));

                            }
                        }catch (Exception e)
                        {
                            EthContract.logger.error("找不到email 或者 chongzhi url");
                        }
                    }
                }
            }
        }catch (Exception e)
        {
            e.printStackTrace();
            BaseCoin.logger.error(e.getMessage());

        }

    }

    @Override
    public String sendTransaction2UserBySystem(BigInteger num, String to) throws Exception {
        return null;
    }

    @Override
    public boolean validateAddress(String address) {
        return true;
    }

    @Override
    public Result test(HashMap pama) {
        //测试一下

        Result result = new Result();


        return result;

    }
}
