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
     * 刷单
     * @param yangCurrencyPair
     * @param type
     * @param price
     * @param num
     * @param isShua
     * @return
     */
    @Transactional
    public Result   saleOrbuyRobotNologin(YangCurrencyPair yangCurrencyPair,String type,BigDecimal price,double num,String isShua)
    {
            Result result = new Result();

            if(num==0)
            {
                return result;
            }

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

            if(result1.getCode().equals(Result.Code.ERROR))
            {
                throw new RuntimeException();
            }

            int id = this.guadan(num,price,type,yangCurrencyPair,yangMemberToken, isShua);

            System.out.println("挂单id"+id);

            redisService.rPush(MQ_ORDER_QUEUE+"_"+yangCurrencyPair.getCurrencyId()+"_"+yangCurrencyPair.getCurrencyTradeId(),id);

//        redisService.add(MQ_ORDER_QUEUE+"_"+yangCurrencyPair.getCyId().intValue(),id);

            result.setData(id);

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



        yangOrders.setType(type);
        yangOrders.setIsShua(Byte.valueOf(isShua)); //判读是不是机器人刷单，如果设置为1  就是机器人刷单
        yangOrders.setHasDo(0);
        yangOrdersMapper.insertUseGeneratedKeys(yangOrders);


        //挂单数据 存入redis 使得交易可以在内存中进行 start

//        String tradeKeys;
//        if (type.equals("buy"))
//        {
//            tradeKeys = CoinConst.REDIS_COIN_TRADE_BUY_+cy.getCurrencyId()+"_"+cy.getCurrencyTradeId();
//        }else{
//            tradeKeys = CoinConst.REDIS_COIN_TRADE_SALE_+cy.getCurrencyId()+"_"+cy.getCurrencyTradeId();
//        }
//
//        redisService.zAdd(tradeKeys, JSONObject.toJSON(yangOrders), yangOrders.getPrice().doubleValue());


        //挂单数据 存入redis 使得交易可以在内存中进行 end


        return yangOrders.getOrdersId();

    }


}
