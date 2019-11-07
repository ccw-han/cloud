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
    private YangOrdersMapper yangOrdersMapper;

    /**
     * 获取订单数量状态信息
     * @return
     */
    public HashMap getOrderNumsInfo(YangCurrencyPair yangCurrencyPair)
    {
        return yangOrdersMapper.getOrderNumsInfo(yangCurrencyPair);
    }

    public List<YangOrders> fontUserTradeFiveRecord(YangOrders yangOrders){
        return yangOrdersMapper.fontUserTradeFiveRecord(yangOrders);
    }

    public YangOrders findMaxPriceSellOrder(Map map){
       return yangOrdersMapper.findMaxOrMinPriceSellOrder(map);
    }

    public Long countOrderNumByCondition(YangOrders yangOrders){return yangOrdersMapper.countOrderNumByCondition(yangOrders);}

    public List<YangOrders> getQueryCondition(YangCurrencyPair yangCurrencyPair,String range,String type,BigDecimal priceLast,BigDecimal priceNow){
        Map map=new HashMap();

        map.put("currencyId",yangCurrencyPair.getCurrencyId());
        map.put("currencyTradeId",yangCurrencyPair.getCurrencyTradeId());
        map.put("memberId",yangCurrencyPair.getRobotId());
        //买卖单
        if("0".equals(range)){
            map.put("priceType",1);
            if(priceNow.compareTo(priceLast)>0){
                map.put("priceMin",priceLast);
                map.put("priceMax",priceNow);
            }
            if(priceNow.compareTo(priceLast)<0){
                map.put("priceMin",priceNow);
                map.put("priceMax",priceLast);
            }
        }

        if(CoinConst.ORDER_SELL.equals(type)){
            map.put("type",CoinConst.ORDER_SELL);
            //涨幅外的挂单集合
            if("1".equals(range)){
                map.put("priceType",2);
                map.put("priceMin",priceNow);
            }
            //反向 挂单集合
            if("2".equals(range)){
                map.put("priceType",3);
                map.put("priceMin",priceLast);
            }
            //第三种 情况
            if("3".equals(range)){
                map.put("priceType",4);
                map.put("priceMax",priceNow);
            }
            return  yangOrdersMapper.queryNoDsOrderListForSell(map);
        }
        //跌价内的挂单集合
        if(CoinConst.ORDER_BUY.equals(type)){
            map.put("type",CoinConst.ORDER_BUY);
            //跌价外的挂单集合
            if("1".equals(range)){
                map.put("priceType",2);
                map.put("priceMax",priceNow);
            }
            //反向 挂单集合
            if("2".equals(range)){
                map.put("priceType",3);
                map.put("priceMax",priceLast);
             }
            //第三种 情况
            if("3".equals(range)){
                map.put("priceType",4);
                map.put("priceMin",priceNow);
            }
            return  yangOrdersMapper.queryNoDsOrderListForBuy(map);
        }

        return  null;

    }
}
