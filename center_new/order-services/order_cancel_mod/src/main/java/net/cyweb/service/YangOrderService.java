package net.cyweb.service;

import com.alibaba.druid.support.json.JSONUtils;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import cyweb.utils.CoinConst;
import cyweb.utils.CommonTools;
import cyweb.utils.DateUtils;
import net.cyweb.mapper.*;
import net.cyweb.model.*;
import net.cyweb.model.modelExt.DealResultBean;
import net.cyweb.model.modelExt.YangTradeExt;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.ArithmeticOperators;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import tk.mybatis.mapper.entity.Example;

import javax.persistence.RollbackException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

@Service
@EnableAsync
@Scope("prototype")
public class YangOrderService extends BaseService<YangOrders> {

    private Logger logger= LoggerFactory.getLogger(YangOrderService.class);

    @Autowired
    private YangMemberService yangMemberService;

    @Autowired
    private MongoTemplate mongoTemplate;

    public enum OrderStatus{
        ORDER_TYPE_INITIAL((byte) 0,"初始状态"),
        ORDER_TYPE_PART_FINISHED((byte) 1,"部分成交"),
        ORDER_TYPE_CANCELED((byte) -1,"撤销"),
        ORDER_TYPE_FINISHED((byte) 2,"成交");
        private byte index;
        private String msg;

        OrderStatus(byte index, String msg) {
            this.index = index;
            this.msg = msg;
        }

        public byte getIndex() {
            return index;
        }

        public String getMsg() {
            return msg;
        }
    }

    public String buyType = "buy";
    public String sellType = "sell";

    @Autowired
    private YangOrdersMapper yangOrdersMapper;


    public List<YangOrders> getOrdersGroupByPrice(Map pama) {
        return yangOrdersMapper.getOrdersGroupByPrice(pama);
    }
    /**
     * 撤单
     * @param orderId
     * @return
     */
    public int  cancel_order(int orderId)
    {
        //调用订单批量处理方法
        Map map=dealOrder(
                0,null,null,null,null,0,
                0,null,null,null,null,0,
                0,null,null,null,null,0,
                0,null,null,null,null,0,
                0,null,null,null,null,0,

                0,null,0,
                0,null,0,
                null,0,0,0,null,null,null,null,null,0,0,0,0,
                null,0,0,0,null,null,null,null,null,0,0,0,0,
                0,0,0,null,0,0,null,null,
                0,0,0,null,0,0,null,null,
                orderId,
                CoinConst.MYSQL_GC_DEAL_TYPE_CANCEL
        );
        Integer a=(Integer)map.get("res");
        return a.intValue();
//        return yangOrdersMapper.cancel_order(orderId);
//        return 0;
    }

    //通用订单 处理service
    public Map dealOrder( Integer memberId,Integer currencyId,BigDecimal num,String type,String field,int tradeOrderId,
                          Integer memberId1,Integer currencyId1,BigDecimal num1,String type1,String field1,int tradeOrderId1,
                          Integer memberId2,Integer currencyId2,BigDecimal num2,String type2,String field2,int tradeOrderId2,
                          Integer memberId3,Integer currencyId3,BigDecimal num3,String type3,String field3,int tradeOrderId3,

                          Integer memberId4,Integer currencyId4,BigDecimal num4,String type4,String field4,int tradeOrderId4,  //应为有的时候 购买的金额 比自己挂单的价格要底 需要把多余冻结金额返还给账户余额




                          int orderId1 ,BigDecimal tradeNum1, long tradeTime1,
                          int orderId2 ,BigDecimal tradeNum2, long tradeTime2,
                          String tradeNo1,int t_memberId1,int t_currencyId1,int currencyTradeId1,BigDecimal price1,BigDecimal t_num1,BigDecimal t_money1,BigDecimal fee1,String t_type1, long t_addTime1,int t_status1,int show1, int t_orders_id1,
                          String tradeNo2 ,int t_memberId2 ,int t_currencyId2,int currencyTradeId2,BigDecimal price2,BigDecimal t_num2,BigDecimal t_money2,BigDecimal fee2,String t_type2, long t_addTime2,int t_status2,int show2,int t_orders_id2,
                          int f_memberId1,int f_type1 ,int f_moneyType1,BigDecimal f_money1 ,long f_addTime1 ,int f_currencyId1 ,String  f_ip1 ,String  f_content1 ,
                          int f_memberId2,int f_type2 ,int f_moneyType2,BigDecimal f_money2 ,long f_addTime2 ,int f_currencyId2 ,String  f_ip2 ,String  f_content2,
                          int order_id,
                          int dealType)
    {
            HashMap pama = new HashMap();
        if(CoinConst.MYSQL_GC_DEAL_TYPE_TRADE==dealType){
            BigDecimal num_tmp;
            BigDecimal forzen_tmp;
            BigDecimal num1_tmp;
            BigDecimal forzen1_tmp;
            BigDecimal num2_tmp;
            BigDecimal forzen2_tmp;
            BigDecimal num3_tmp;
            BigDecimal forzen3_tmp;
            BigDecimal num4_tmp;
            BigDecimal forzen4_tmp;
            if(field != null && field.equals("normal"))
            {
                num_tmp = num;
                forzen_tmp = BigDecimal.ZERO;
            }else{
                num_tmp = BigDecimal.ZERO;
                forzen_tmp = num;
            }
            pama.put("memberId",memberId);
            pama.put("cyId",currencyId);
            pama.put("num",num_tmp);
            pama.put("numOptions",type);
            pama.put("forzen",forzen_tmp);
            pama.put("forzenOptions",type);
            pama.put("trade_orders_id",tradeOrderId);

            if(field1 != null && field1.equals("normal"))
            {
                num1_tmp = num1;
                forzen1_tmp = BigDecimal.ZERO;
            }else{
                num1_tmp = BigDecimal.ZERO;
                forzen1_tmp = num1;
            }
            pama.put("memberId1",memberId1);
            pama.put("cyId1",currencyId1);
            pama.put("num1",num1_tmp);
            pama.put("numOptions1",type1);
            pama.put("forzen1",forzen1_tmp);
            pama.put("forzenOptions1",type1);
            pama.put("trade_orders_id1",tradeOrderId1);
            if(field2 != null && field2.equals("normal"))
            {
                num2_tmp = num2;
                forzen2_tmp = BigDecimal.ZERO;
            }else{
                num2_tmp = BigDecimal.ZERO;
                forzen2_tmp = num2;
            }
            pama.put("memberId2",memberId2);
            pama.put("cyId2",currencyId2);
            pama.put("num2",num2_tmp);
            pama.put("numOptions2",type2);
            pama.put("forzen2",forzen2_tmp);
            pama.put("forzenOptions2",type2);
            pama.put("trade_orders_id2",tradeOrderId2);
            if(field3 != null && field3.equals("normal"))
            {
                num3_tmp = num3;
                forzen3_tmp = BigDecimal.ZERO;
            }else{
                num3_tmp = BigDecimal.ZERO;
                forzen3_tmp = num3;
            }
            pama.put("memberId3",memberId3);
            pama.put("cyId3",currencyId3);
            pama.put("num3",num3_tmp);
            pama.put("numOptions3",type3);
            pama.put("forzen3",forzen3_tmp);
            pama.put("forzenOptions3",type3);
            pama.put("trade_orders_id3",tradeOrderId3);
            if(field4 != null && field4.equals("normal"))
            {
                num4_tmp = num4;
                forzen4_tmp = BigDecimal.ZERO;
            }else{
                num4_tmp = BigDecimal.ZERO;
                forzen4_tmp = num4;
            }
            pama.put("memberId4",memberId4);
            pama.put("cyId4",currencyId4);
            pama.put("num4",num4_tmp);
            pama.put("numOptions4",type4);
            pama.put("forzen4",forzen4_tmp);
            pama.put("forzenOptions4",type4);
            pama.put("trade_orders_id4",tradeOrderId4);


            pama.put("orderId1",orderId1);
            pama.put("tradeNum1",tradeNum1);
            pama.put("tradeTime1",tradeTime1);
            pama.put("orderId2",orderId2);
            pama.put("tradeNum2",tradeNum2);
            pama.put("tradeTime2",tradeTime2);

            pama.put("tradeNo1",tradeNo1);
            pama.put("t_memberId1",t_memberId1);
            pama.put("t_currencyId1",t_currencyId1);
            pama.put("currencyTradeId1",currencyTradeId1);
            pama.put("price1",price1);
            pama.put("t_num1",t_num1);
            pama.put("t_money1",t_money1);
            pama.put("fee1",fee1);
            pama.put("t_type1",t_type1);
            pama.put("t_addTime1",t_addTime1);
            pama.put("t_status1",t_status1);
            pama.put("show1",show1);
            pama.put("t_orders_id1",t_orders_id1);
            pama.put("tradeNo2",tradeNo2);
            pama.put("t_memberId2",t_memberId2);
            pama.put("t_currencyId2",t_currencyId2);
            pama.put("currencyTradeId2",currencyTradeId2);
            pama.put("price2",price2);
            pama.put("t_num2",t_num2);
            pama.put("t_money2",t_money2);
            pama.put("fee2",fee2);
            pama.put("t_type2",t_type2);
            pama.put("t_addTime2",t_addTime2);
            pama.put("t_status2",t_status2);
            pama.put("show2",show2);
            pama.put("t_orders_id2",t_orders_id2);


            pama.put("f_memberId1",f_memberId1);
            pama.put("f_type1",f_type1);
            pama.put("f_moneyType1",f_moneyType1);
            pama.put("f_money1",f_money1);
            pama.put("f_addTime1",f_addTime1);
            pama.put("f_currencyId1",f_currencyId1);
            pama.put("f_ip1",f_ip1);
            pama.put("f_content1",f_content1);
            pama.put("f_memberId2",f_memberId2);
            pama.put("f_type2",f_type2);
            pama.put("f_moneyType2",f_moneyType2);
            pama.put("f_money2",f_money2);
            pama.put("f_addTime2",f_addTime2);
            pama.put("f_currencyId2",f_currencyId2);
            pama.put("f_ip2",f_ip2);
            pama.put("f_content2",f_content2);
            pama.put("order_id_in",0);
        }
        if(CoinConst.MYSQL_GC_DEAL_TYPE_CANCEL==dealType){
            pama.put("memberId",0);
            pama.put("cyId",null);
            pama.put("num",null);
            pama.put("numOptions",null);
            pama.put("forzen",null);
            pama.put("forzenOptions",null);
            pama.put("trade_orders_id",tradeOrderId);
            pama.put("memberId1",0);
            pama.put("cyId1",null);
            pama.put("num1",null);
            pama.put("numOptions1",null);
            pama.put("forzen1",null);
            pama.put("forzenOptions1",null);
            pama.put("trade_orders_id1",tradeOrderId1);
            pama.put("memberId2",0);
            pama.put("cyId2",null);
            pama.put("num2",null);
            pama.put("numOptions2",null);
            pama.put("forzen2",null);
            pama.put("forzenOptions2",null);
            pama.put("trade_orders_id2",tradeOrderId2);
            pama.put("memberId3",0);
            pama.put("cyId3",null);
            pama.put("num3",null);
            pama.put("numOptions3",null);
            pama.put("forzen3",null);
            pama.put("forzenOptions3",null);
            pama.put("trade_orders_id3",tradeOrderId3);
            pama.put("memberId4",0);
            pama.put("cyId4",null);
            pama.put("num4",null);
            pama.put("numOptions4",null);
            pama.put("forzen4",null);
            pama.put("forzenOptions4",null);
            pama.put("trade_orders_id4",tradeOrderId4);


            pama.put("orderId1",null);
            pama.put("tradeNum1",null);
            pama.put("tradeTime1",null);
            pama.put("orderId2",null);
            pama.put("tradeNum2",null);
            pama.put("tradeTime2",null);

            pama.put("tradeNo1",null);
            pama.put("t_memberId1",null);
            pama.put("t_currencyId1",null);
            pama.put("currencyTradeId1",null);
            pama.put("price1",null);
            pama.put("t_num1",null);
            pama.put("t_money1",null);
            pama.put("fee1",null);
            pama.put("t_type1",null);
            pama.put("t_addTime1",null);
            pama.put("t_status1",null);
            pama.put("show1",null);
            pama.put("t_orders_id1",null);
            pama.put("tradeNo2",null);
            pama.put("t_memberId2",null);
            pama.put("t_currencyId2",null);
            pama.put("currencyTradeId2",null);
            pama.put("price2",null);
            pama.put("t_num2",null);
            pama.put("t_money2",null);
            pama.put("fee2",null);
            pama.put("t_type2",null);
            pama.put("t_addTime2",null);
            pama.put("t_status2",null);
            pama.put("show2",null);
            pama.put("t_orders_id2",null);
            pama.put("f_memberId1",null);
            pama.put("f_type1",null);
            pama.put("f_moneyType1",null);
            pama.put("f_money1",null);
            pama.put("f_addTime1",null);
            pama.put("f_currencyId1",null);
            pama.put("f_ip1",null);
            pama.put("f_content1",null);
            pama.put("f_memberId2",null);
            pama.put("f_type2",null);
            pama.put("f_moneyType2",null);
            pama.put("f_money2",null);
            pama.put("f_addTime2",null);
            pama.put("f_currencyId2",null);
            pama.put("f_ip2",null);
            pama.put("f_content2",null);
            pama.put("order_id_in",order_id);
        }
        pama.put("dealType",dealType);

        return yangOrdersMapper.dealOrder(pama);
    }

    /**
     * 获取订单数量状态信息
     * @return
     */
    public HashMap getOrderNumsInfo(YangCurrencyPair yangCurrencyPair)
    {
        return yangOrdersMapper.getOrderNumsInfo(yangCurrencyPair);
    }


    /**
     * 查询买卖单价格 极值
     * @param map
     * @return
     */
    public List<Map> findMaxPriceSellOrder(Map map){
        return yangOrdersMapper.findMaxOrMinPriceSellOrder(map);
    }

    /**
     * 查询价格一样的  未处理的  买卖单
     */
    public List<Map> querySamePrice(YangCurrencyPair yangCurrencyPair){
        return yangOrdersMapper.querySamePrice(yangCurrencyPair);
    }
}
