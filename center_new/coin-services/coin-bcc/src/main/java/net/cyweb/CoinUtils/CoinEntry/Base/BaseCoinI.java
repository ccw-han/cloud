package net.cyweb.CoinUtils.CoinEntry.Base;

import com.azazar.bitcoin.jsonrpcclient.BitcoinException;
import net.cyweb.CoinUtils.Exception.LogException;
import net.cyweb.model.Result;
import net.cyweb.model.YangCurrency;
import net.cyweb.service.RedisService;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Future;

public interface BaseCoinI {


    /**
     *初始化基本信息
     */
    public void ExtendBaseInfo(YangCurrency yangCurrency, RedisService redisService) throws LogException;

    /**
     * 获取莫个地址金额
     * @return
     */
    public BigDecimal getBalance(String account)  throws LogException;

    /**
     * 获取全部钱包金额
     * @return
     */
    public BigDecimal getAllBalance() throws Exception;


    /**
     * 获取全部用户地址
     * @return
     */
    public List<String> getAllAccount() throws Exception;


    /**
     * 转账 如果成功 返回交易hash 失败 返回"00"
     * @param from
     * @param num
     * @param to
     * @return
     */
    public String  sendTransaction(String from,long num,String to,String passwd) throws LogException;


    /**
     * 转出改账户下全部资金
     * @param from
     * @param to
     * @param passwd
     * @return
     * @throws Exception
     */
    public String  sendTransactionALl(String from,String to,String passwd) throws LogException;


    /**
     * 转账 如果成功 返回交易hash 失败 返回"00" 这个方法用户给平台用户充值的时候，把币转给冷钱包
     * @param from
     * @param num
     * @return
     */
    public String  sendTransaction2cold(String from,double num,String passwd) throws LogException;



    /**
     * 用户平台给用户转账
     * @param to
     * @param num
     * @param passwd
     * @return
     * @throws Exception
     */
    public String sendTransaction2User(String to,BigInteger num,String passwd) throws LogException;



     public Result chongzhiByTx(String tx) throws LogException;





    /**
     * 创建用户
     * @return
     */
    public String createUser(String passwd) throws  LogException;


    /**
     * 修改用户密码
     * @return
     */
    public boolean changUser(String account,String passwd) throws LogException;

    /**
     * 监听用户充币用来
     */
    public Future<String> chargingMoneylistener() throws LogException, InterruptedException;


    public void chargingMoneylistenerDistory(YangCurrency yangCurrency) throws LogException, BitcoinException;


    public BigInteger tranNeedPrice(BigInteger num,String from ,String to) throws  LogException;



    public Double num2real(BigDecimal blance);


    public void safe();


    public boolean validateAddress(String address);


    /**
     * 格式化金额 到系统金额
     * @param blance
     * @return
     */
    public BigInteger num2trance(BigDecimal blance);


}
