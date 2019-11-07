package net.cyweb;

import com.netflix.discovery.converters.Auto;
import jnr.ffi.annotations.In;
import net.cyweb.mapper.YangCurrencyUserMapper;
import net.cyweb.mapper.YangFtActivityMapper;
import net.cyweb.mapper.YangMemberMapper;
import net.cyweb.mapper.YangOrdersMapper;
import net.cyweb.model.*;
import net.cyweb.service.RedisService;
import net.cyweb.service.YangCurrencyUserService;
import net.cyweb.service.YangOrderService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import tk.mybatis.mapper.entity.Example;

import java.math.BigDecimal;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TradeTest {
    @Autowired
    private RedisService redisService;



    @Autowired
    private YangOrdersMapper yangOrdersMapper;

    @Autowired
    private YangMemberMapper yangMemberMapper;

    @Autowired
    private YangCurrencyUserMapper yangCurrencyUserMapper;

    @Autowired
    private YangOrderService yangOrderService;

    @Autowired
    private YangFtActivityMapper yangFtActivityMapper;



    /*测试卖单*/
    private void preCheckSell(){
        /*1个卖单，多个买单*/
        genOrderSingle(6,37,0,new BigDecimal(15),new BigDecimal(100),"sell");
        genOrderSingle(6,37,0,new BigDecimal(20),new BigDecimal(20),"buy");
        genOrderSingle(6,37,0,new BigDecimal(20),new BigDecimal(20),"buy");

    }

    private void preCheckBuy(){
        /*1个买单 ， 多个卖单*/

        Integer currencyId = 37;
        Integer currencyTradeId = 0;

        Integer pMemberId = 190;
        Integer buyMemberId = 6;
        Integer sellMemberId = 8;

        Integer buyPrice = 20;
        Integer sellPrice = 10;

        Integer buyNum = 100;
        Integer sellNum = 30;



        genOrderSingle(buyMemberId,currencyId,currencyTradeId,new BigDecimal(buyPrice),new BigDecimal(buyNum),"buy");
        genOrderSingle(sellMemberId,currencyId,currencyTradeId,new BigDecimal(sellPrice),new BigDecimal(sellNum),"sell");
        genOrderSingle(sellMemberId,currencyId,currencyTradeId,new BigDecimal(sellPrice),new BigDecimal(sellNum+10),"sell");
        genOrderSingle(sellMemberId,currencyId,currencyTradeId,new BigDecimal(sellPrice),new BigDecimal(sellNum+20),"sell");


    }

    /*创建单笔订单*/
    private void genOrderSingle(Integer memberId,Integer currencyId,Integer currencyTradeId,BigDecimal price,BigDecimal num,String type){
        YangOrders yangOrders = new YangOrders();
        yangOrders.setStatus((byte) 0);
        yangOrders.setMemberId(memberId);
        yangOrders.setCurrencyId(currencyId);
        yangOrders.setCurrencyTradeId(currencyTradeId);
        yangOrders.setPrice(price);
        yangOrders.setNum(num);
        yangOrders.setTradeNum(new BigDecimal(0)); //已交易数量为0
        yangOrders.setType(type);
        yangOrders.setAddTime((int) (System.currentTimeMillis()/1000));
        yangOrders.setTradeTime((System.currentTimeMillis()/1000));
        yangOrders.setStatus((byte) 0);
        yangOrdersMapper.insertSelective(yangOrders);

        if(type.equals("buy")){
            //买单,冻结price
            if(currencyTradeId == 0){
                yangOrderService.updateUserMoney(
                        memberId,0,num.multiply(price),"inc","forzen",
                        0,null,null,null,null,
                        0,null,null,null,null,
                        0,null,null,null,null,
                        0,null,null,null,null
                );
            }else{
                yangOrderService.updateUserMoney(
                        memberId,currencyTradeId,num.multiply(price),"inc","forzen",
                        0,null,null,null,null,
                        0,null,null,null,null,
                        0,null,null,null,null,
                        0,null,null,null,null

                );
            }

        }else{
            //卖单,冻结币
            yangOrderService.updateUserMoney(
                    memberId,currencyId,num,"inc","forzen",
                    0,null,null,null,null,
                    0,null,null,null,null,
                    0,null,null,null,null,
                    0,null,null,null,null

            );
        }
    }

    @Test
    public void dealOrderTest(){

        for(int i = 0;i< 20;i++){
            redisService.lpop("mountOrder");
        }

//        preCheckSell();

        preCheckBuy();

        /*查询所有的卖单*/
        YangOrders yangOrdersSellQuery = new YangOrders();
        yangOrdersSellQuery.setType("sell");
        List<YangOrders> yangOrdersList = yangOrdersMapper.select(yangOrdersSellQuery);

        for(YangOrders yangOrders:yangOrdersList){
            redisService.rPush("mountOrder",yangOrders.getOrdersId());
        }

//        redisService.rPush("mountOrder",1);

//        orderDealTask.deal();
    }

    @Test
    public void deal(){
        BigDecimal nums = BigDecimal.valueOf(200);
        System.out.println("开始增加额度"+nums);

        YangTibi y = new YangTibi();
        y.setUserId(2522);

        YangFtActivity yangFtActivity = new YangFtActivity();
        yangFtActivity.setMemberId(y.getUserId());

        List<YangFtActivity> list1 = yangFtActivityMapper.select(yangFtActivity);

        if(list1 != null && list1.size() > 0)
        {
            System.out.println("增加额度用户");

            yangFtActivity.setNum(nums.add(list1.get(0).getNum()));

            Example example = new Example(YangFtActivity.class);
            example.createCriteria().andEqualTo("memberId",y.getUserId());

            yangFtActivityMapper.updateByExampleSelective(yangFtActivity,example);


//            yangFtActivityMapper.updateByPrimaryKeySelective(yangFtActivity);


        }else{
            System.out.println("新增额度用户");

            yangFtActivity.setAddTime(Integer.valueOf(String.valueOf(System.currentTimeMillis()/1000)));
            yangFtActivity.setMemberId(y.getUserId());
            yangFtActivity.setNum(nums);
            yangFtActivity.setType(Integer.valueOf(0));
            yangFtActivityMapper.insert(yangFtActivity);
        }
        System.out.println("增加额度成功"+nums);
//        orderDealTask.deal();
    }


}
