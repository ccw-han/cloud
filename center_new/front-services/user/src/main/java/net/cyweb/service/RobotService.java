package net.cyweb.service;

import com.alibaba.druid.support.json.JSONUtils;
import com.alibaba.fastjson.JSONObject;
import cyweb.utils.CoinConst;
import cyweb.utils.ErrorCode;
import net.cyweb.mapper.*;
import net.cyweb.model.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.math.BigDecimal;
import java.math.BigInteger;

import static net.cyweb.controller.ApiBaseController.MQ_ORDER_QUEUE;

@Service
@EnableAsync
public class RobotService {

    @Autowired
    RedisService redisService;

    private  String RobotConfigKeypre  = "ROBOTCONFIG-";

    @Autowired
    private YangCurrencyPairMapper yangCurrencyPairMapper;



    @Autowired
    private YangCurrencyMapper yangCurrencyMapper;

    @Autowired
    private YangTradeMapper yangTradeMapper;


    @Autowired
    private YangMemberTokenService yangMemberTokenService;

    @Autowired
    private YangTimelineMapper yangTimelineMapper;

    @Autowired
    private YangTradeFeeMapper yangTradeFeeMapper;


    @Autowired
    private YangOrderService yangOrderService;

    @Autowired
    private YangOrdersMapper yangOrdersMapper;

    @Autowired
    private YangMemberService yangMemberService;

    /**
     * 卖单机器人
     * @param cId
     */

    @Transactional
    public Result saleOrBuyRobot(String cId , BigDecimal price,double num,String passwd,YangMemberToken yangMemberToken,String type,String isShua) throws RuntimeException
    {


        Result result = new Result();

        YangMemberToken yangMemberToken1 =  yangMemberTokenService.getYangMemberToken(yangMemberToken); //通过token获取用户基本信息

        String lock =  yangMemberToken1.getMemberId()+"-"+cId;


            YangCurrencyPair yangCurrencyPair = new YangCurrencyPair();
            yangCurrencyPair.setCyId(Integer.valueOf(cId));


            yangCurrencyPair =  yangCurrencyPairMapper.selectOne(yangCurrencyPair);

            YangCurrencyUser yangCurrencyUser = new YangCurrencyUser();

            if(type.equals("buy"))
            {
                yangCurrencyUser.setCurrencyId(yangCurrencyPair.getCurrencyTradeId()); //如果是买的话 对应用户
            }else{
                yangCurrencyUser.setCurrencyId(yangCurrencyPair.getCurrencyId());
            }



            result = this.validate(passwd,yangCurrencyPair,price,num,yangMemberToken1,yangCurrencyUser,type);

            if(result.getCode().equals(Result.Code.ERROR))
            {
                return  result;
            }


            //开始挂单 并且把挂单数据提交给redis队列进行处理
            YangMember yangMember = new YangMember();
            yangMember.setRmbType("dec");
            yangMember.setForzenRmbType("inc");



            BigDecimal priceNums;
            if(type.equals("buy"))
            {
                priceNums = price.multiply(BigDecimal.valueOf(num));
            }else{
                priceNums = BigDecimal.valueOf(num);
            }

            yangCurrencyUser.setNumType("dec");
            yangCurrencyUser.setForzenNumType("inc");
            yangCurrencyUser.setNum(priceNums);
            yangCurrencyUser.setForzenNum(priceNums);

            yangMember.setForzenRmb(priceNums);
            yangMember.setRmb(priceNums);

            Result result1 = yangMemberTokenService.updateAssets(yangMemberToken, yangCurrencyUser,yangMember);



            //开始挂单

            int id = this.guadan(num,price,type,yangCurrencyPair,yangMemberToken1, isShua);

            System.out.println("挂单id"+id);

            redisService.rPush(MQ_ORDER_QUEUE,id);

//            redisService.add(MQ_ORDER_QUEUE+"_"+cId,id);

            return result;



    }


    /**
     *
     * @param yangCurrencyPair
     * @param type
     * @param price
     * @param num
     * @param isShua
     * @return
     */
    public Result saleOrbuyRobotNologin(YangCurrencyPair yangCurrencyPair,String type,BigDecimal price,double num,String isShua)
    {
        Result result = new Result();



        YangMemberToken yangMemberToken =  new YangMemberToken(); //通过token获取用户基本信息
        yangMemberToken.setMemberId(yangCurrencyPair.getRobotId());


        YangCurrencyUser yangCurrencyUser = new YangCurrencyUser();

        if(type.equals("buy"))
        {
            yangCurrencyUser.setCurrencyId(yangCurrencyPair.getCurrencyTradeId()); //如果是买的话 对应用户
        }else{
            yangCurrencyUser.setCurrencyId(yangCurrencyPair.getCurrencyId());
        }


        //开始挂单 并且把挂单数据提交给redis队列进行处理
        YangMember yangMember = new YangMember();
        yangMember.setRmbType("dec");
        yangMember.setForzenRmbType("inc");


        BigDecimal priceNums;
        if(type.equals("buy"))
        {
            priceNums = price.multiply(BigDecimal.valueOf(num));
        }else{
            priceNums = BigDecimal.valueOf(num);
        }

        yangCurrencyUser.setNumType("dec");
        yangCurrencyUser.setForzenNumType("inc");
        yangCurrencyUser.setNum(priceNums);
        yangCurrencyUser.setForzenNum(priceNums);

        yangMember.setForzenRmb(priceNums);
        yangMember.setRmb(priceNums);

        Result result1 = yangMemberTokenService.updateAssets(yangMemberToken, yangCurrencyUser,yangMember);


        int id = this.guadan(num,price,type,yangCurrencyPair,yangMemberToken, isShua);

        System.out.println("挂单id"+id);

        redisService.rPush(MQ_ORDER_QUEUE+"_"+yangCurrencyPair.getCyId(),id);

//        redisService.add(MQ_ORDER_QUEUE+"_"+yangCurrencyPair.getCyId().intValue(),id);

        return result;

    }




    //挂单操作
    private int  guadan(double num, BigDecimal price, String type, YangCurrencyPair cy,YangMemberToken yangMemberToken,String isShua) throws RuntimeException
    {


        YangTradeFee yangTradeFee = new YangTradeFee();
        yangTradeFee.setCyId(cy.getCyId());
        yangTradeFee.setMemberId(yangMemberToken.getMemberId());
        yangTradeFee = yangTradeFeeMapper.selectOne(yangTradeFee);

        if(yangTradeFee != null && yangTradeFee.getTradeFeeId() != null && yangTradeFee.getTradeFeeId().compareTo(Integer.valueOf(0)) >  0)
        {
            cy.setCurrencySellFee(BigDecimal.valueOf(yangTradeFee.getCurrencyBuyFee()));
            cy.setCurrencyBuyFee(BigDecimal.valueOf(yangTradeFee.getCurrencySellFee()));

        }

        BigDecimal fee;

        if(type.equals("buy"))
        {
            fee = BigDecimal.valueOf(num).multiply(cy.getCurrencyBuyFee().divide(BigDecimal.valueOf(100)));
        }else{
            fee = BigDecimal.valueOf(num).multiply(cy.getCurrencySellFee()).divide(BigDecimal.valueOf(100));
        }

        YangOrders yangOrders = new YangOrders();
        yangOrders.setMemberId(yangMemberToken.getMemberId());
        yangOrders.setCurrencyId(cy.getCurrencyId());
        yangOrders.setCurrencyTradeId(cy.getCurrencyTradeId());
        yangOrders.setPrice(price);
        yangOrders.setNum(BigDecimal.valueOf(num));
        yangOrders.setTradeNum(BigDecimal.valueOf(0));
        yangOrders.setAddTime(Integer.valueOf(String.valueOf(System.currentTimeMillis()/1000)));
        yangOrders.setTradeTime(System.currentTimeMillis()/1000);
        yangOrders.setStatus(Byte.valueOf("0"));
        yangOrders.setFee(fee);
        yangOrders.setHasDo(0);


        yangOrders.setType(type);
        yangOrders.setIsShua(Byte.valueOf(isShua)); //判读是不是机器人刷单，如果设置为1  就是机器人刷单

        yangOrdersMapper.insertUseGeneratedKeys(yangOrders);


        //挂单数据 存入redis 使得交易可以在内存中进行 start

        String tradeKeys;
        if (type.equals("buy"))
        {
            tradeKeys = CoinConst.REDIS_COIN_TRADE_BUY_+cy.getCurrencyId()+"_"+cy.getCurrencyTradeId();
        }else{
            tradeKeys = CoinConst.REDIS_COIN_TRADE_SALE_+cy.getCurrencyId()+"_"+cy.getCurrencyTradeId();
        }

        redisService.zAdd(tradeKeys, JSONObject.toJSON(yangOrders), yangOrders.getPrice().doubleValue());


        //挂单数据 存入redis 使得交易可以在内存中进行 end


        return yangOrders.getOrdersId();

    }





    private Result validate(String passwd,YangCurrencyPair yangCurrencyPair,BigDecimal price,double num,YangMemberToken yangMemberToken,YangCurrencyUser yangCurrencyUser,String type) throws RuntimeException
    {
        //先判断交易对是否有效
        Result result = new Result();
        result.setCode(Result.Code.SUCCESS);

        //获取交易信息
        YangCurrency yangCurrency = new YangCurrency();
        yangCurrency.setCurrencyId(yangCurrencyPair.getCurrencyId());
        yangCurrency = yangCurrencyMapper.selectOne(yangCurrency);

        //获取用户基本信息
        YangMember yangMemberbase = new  YangMember();
        yangMemberbase.setMemberId(yangMemberToken.getMemberId());
        yangMemberbase = yangMemberService.selectOne(yangMemberbase);


         if(StringUtils.isEmpty(yangMemberbase.getPwdtrade()) )//验证交易密码
         {
             result.setErrorCode(ErrorCode.ERROR_PWD_TRADE_ISNETSED.getIndex());
             result.setMsg(ErrorCode.ERROR_PWD_TRADE_ISNETSED.getMessage());
             result.setCode(Result.Code.ERROR);
         }else if(StringUtils.isEmpty(passwd))//验证交易密码
         {
             result.setErrorCode(ErrorCode.ERROR_PWD_TRADE_ISEMPTY.getIndex());
             result.setMsg(ErrorCode.ERROR_PWD_TRADE_ISEMPTY.getMessage());

             result.setCode(Result.Code.ERROR);
         }
         else if(  !DigestUtils.md5Hex(passwd) .equals( yangMemberbase.getPwdtrade()))//验证交易密码
         {
             result.setErrorCode(ErrorCode.ERROR_PWD_TRADE_FAILED.getIndex());
             result.setMsg(ErrorCode.ERROR_PWD_TRADE_FAILED.getMessage());
             result.setCode(Result.Code.ERROR);
         }else if(yangCurrency.getIsLock().intValue() == 1)  //交易不许可
        {
            result.setErrorCode(ErrorCode.ERROR_TRADE_CURRENCY_DENIED.getIndex());
            result.setMsg(ErrorCode.ERROR_TRADE_CURRENCY_DENIED.getMessage());
            result.setCode(Result.Code.ERROR);
        }else if(price==null || price.compareTo(BigDecimal.ZERO) <=0 || num <=0 )  //验证交易基本信息是否合法
        {
            result.setErrorCode(ErrorCode.ERROR_TRADE_CURRENCY_PARAMA_ERROR.getIndex());
            result.setMsg(ErrorCode.ERROR_TRADE_CURRENCY_PARAMA_ERROR.getMessage());
            result.setCode(Result.Code.ERROR);
        }else if(!this.checkMinMoney(yangCurrencyPair,price,num)) //检查最低交易量
        {
            result.setErrorCode(ErrorCode.ERROR_TRADE_CURRENCY_PRICE_LIMIT.getIndex());
            result.setMsg(ErrorCode.ERROR_TRADE_CURRENCY_PRICE_LIMIT.getMessage());
            result.setCode(Result.Code.ERROR);
        }else{
            YangMember yangMember = yangMemberTokenService.getSpecAssets(yangMemberToken,yangCurrencyUser);
            BigDecimal lefts;

            if(type.equals("sell"))
            {
                lefts = yangMember.getYangCurrencyUserList().get(0).getNum();
                if(lefts.compareTo(BigDecimal.valueOf(num)) < 0)
                {
                    result.setErrorCode(ErrorCode.ERROR_TRADE_CURRENCY_USER_PRICE_NOTENOUGH.getIndex());
                    result.setMsg(ErrorCode.ERROR_TRADE_CURRENCY_USER_PRICE_NOTENOUGH.getMessage());
                    result.setCode(Result.Code.ERROR);
                }
            }else{
                if(yangCurrencyPair.getCurrencyTradeId().equals(0))
                {
                    lefts =   yangMember.getRmb();
                }else{
                    lefts = yangMember.getYangCurrencyUserList().get(0).getNum();
                }
                if(lefts.compareTo(BigDecimal.valueOf(num).multiply(price)) < 0)
                {
                    result.setErrorCode(ErrorCode.ERROR_TRADE_CURRENCY_USER_PRICE_NOTENOUGH.getIndex());
                    result.setMsg(ErrorCode.ERROR_TRADE_CURRENCY_USER_PRICE_NOTENOUGH.getMessage());
                    result.setCode(Result.Code.ERROR);
                }
            }


        }


        return result;

    }
    /*最小交易金额限制 后台设置的单位为韩元*/
    private boolean checkMinMoney(YangCurrencyPair yangCurrencyPair, BigDecimal price , double num)
    {

        return  yangCurrencyPair.getMinMoney().compareTo(Double.valueOf(num)) >=0 ? false : true;

//        BigDecimal money;
//        try {
//            if(yangCurrencyPair.getCurrencyTradeId().equals(Integer.valueOf(0)))
//            {
//                money = price.multiply(BigDecimal.valueOf(num));
//
//            }else{
//                //非韩元区
//
//                Example example = new Example(YangTrade.class);
//                Example.Criteria  criteria = example.createCriteria();
//                criteria.andEqualTo("currencyId",yangCurrencyPair.getCurrencyTradeId());
//                criteria.andEqualTo("currencyTradeId",0);
//                example.orderBy("tradeId desc");
//                YangTrade trade = yangTradeMapper.selectByExample(example).get(0);
//                money = trade.getPrice().multiply(BigDecimal.valueOf(num)).multiply(price);
//            }
//            if(money.compareTo(BigDecimal.valueOf(yangCurrencyPair.getMinMoney())) < 0)
//            {
//                return false;
//            }
//            return true;
//        }catch (Exception e)
//        {
//            e.printStackTrace();
//            return  false;
//        }

    }




//    /**
//     * 是否成交 控制机器人成交策略
//     * @return
//     */
//    public boolean Deal(String cId)
//    {
//        RobotConfigBean robotConfigBean = initConfigByRedis(cId);
//        if( robotConfigBean != null || robotConfigBean.isPersonMod())
//        {
//            return false;
//        }else{
//            //判断是否这笔可以成交
//
//        }
//    }



    /**
     * 策略
     * @return
     */
    public boolean strategy(String cId)
    {
        //先判断是人工模式还是机器人干预模式  如果当前交易对已经设置为人工模式，则机器人只成交机器人的订单

        return false;
    }

    /**
     * 交易对想初始化
     * @param cId
     * @return
     */
    private RobotConfigBean initConfigByRedis(String cId)
    {
        try {
            String robotCoinfig =  this.redisService.get(this.RobotConfigKeypre+cId).toString();
            if(robotCoinfig != null)
            {
                RobotConfigBean robotConfigBean =  JSONObject.parseObject(robotCoinfig, RobotConfigBean.class);
                return  robotConfigBean;
            }else{
                return  null;
            }
        }catch (Exception e)
        {
            return null;
        }

    }


}
