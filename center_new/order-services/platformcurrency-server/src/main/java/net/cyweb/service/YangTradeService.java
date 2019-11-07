package net.cyweb.service;

import com.alibaba.fastjson.JSON;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import cyweb.utils.CoinConst;
import net.cyweb.mapper.YangTradeMapper;
import net.cyweb.model.MgYangTrade;
import net.cyweb.model.YangCurrencyPair;
import net.cyweb.model.YangTrade;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
@EnableAsync
public class YangTradeService extends BaseService<YangTrade>{

    @Autowired
    private YangTradeMapper yangTradeMapper;

    @Autowired
    private MongoTemplate mongoTemplate;

    public Map getLastDayInfo(Map params){
        Map sumMap=new HashMap();
        Aggregation aggregation= Aggregation.newAggregation(
                Aggregation.match(getConditionGroup(params)),
                Aggregation.group().avg("price").as("avgPrice").count().as("total"));
        AggregationResults<Map> outputTypeCount5 = this.mongoTemplate.aggregate(aggregation, CoinConst.MONGODB_TRADE_COLLECTION, Map.class);
        if(outputTypeCount5.iterator().hasNext()){
            for (Iterator<Map> iterator = outputTypeCount5.iterator(); iterator.hasNext(); ) {
                Map<String, Object> obj = iterator.next();
                System.out.println(JSON.toJSONString(obj));
                sumMap.put("avgPrice", new BigDecimal((Double) obj.get("avgPrice")));
                sumMap.put("total", (int) obj.get("total"));
            }
        }

//        yangTradeMapper.queryLastDayInfo(params);
        return sumMap;
    }

    @Transactional
    public List<Map> findLastDayTradeInfo(Map params){
        //获取前一日所有用户信息
        Map sumMap=new HashMap();
        Aggregation aggregation= Aggregation.newAggregation(
                Aggregation.match(getConditionGroup(params)),
                Aggregation.group("memberId"),
                Aggregation.project("memberId")
                );
        AggregationResults<Map> outputTypeCount5 = this.mongoTemplate.aggregate(aggregation, CoinConst.MONGODB_TRADE_COLLECTION, Map.class);
        List<Map> list=outputTypeCount5.getMappedResults();


        params.put("list",list);
        List<Map>  resutList=yangTradeMapper.findLastDayTradeInfoForMongodb(params);
//        yangTradeMapper.findLastDayTradeInfo(params);
        return resutList;
    }
    //拼凑查询参数
    public Criteria getConditionGroup(Map params){
        int start=((Long)params.get("startTime")).intValue();
        int end=((Long)params.get("endTime")).intValue();
        int currencyId=(Integer)params.get("currencyId");

        Criteria a=new Criteria();
        Criteria b=new Criteria();
        Criteria c=new Criteria();
        Criteria d=new Criteria();
        Criteria e=new Criteria();
        b=b.andOperator(Criteria.where("addTime").gte(start).lte(end));
        c= c.andOperator(Criteria.where("currencyId").is(currencyId));
        if (null != params.get("memberId") && StringUtils.isNotBlank(String.valueOf(params.get("memberId")))) {
            d=d.andOperator(Criteria.where("memberId").is(Integer.valueOf((String)params.get("memberId"))));
        }
        if (null != params.get("type") && StringUtils.isNotBlank(String.valueOf(params.get("type")))) {
            e=e.andOperator(Criteria.where("type").is(Integer.valueOf((String)params.get("type"))));
        }
        a.andOperator(b,c,d,e);
        return a;
    }

    @Transactional
    public List<Map> findList(Map params){
        Query query=new Query();
        query.addCriteria(getConditionGroup(params));
        List<Map> list=this.mongoTemplate.find(query,Map.class,CoinConst.MONGODB_TRADE_COLLECTION);
        yangTradeMapper.findList(params);
        return list;
    }

    //查询所有的机器人订单
    public List<YangTrade> findRobotList(Map map){
        return yangTradeMapper.findRobotList(map);
    }

    //批量删除机器人订单
    public int deleteBatch(List<YangTrade> list){
        return yangTradeMapper.deleteBatch(list);
    }

    public Map  getLastDaySellFeeTotal(Map params){
        Map sumMap=new HashMap();
        Aggregation aggregation= Aggregation.newAggregation(
                Aggregation.match(getConditionGroup(params)),
                    Aggregation.group().avg("price").as("avg").sum(ArithmeticOperators.Multiply.valueOf("num").
                            multiplyBy(ArithmeticOperators.Multiply.valueOf("price")).multiplyBy(0.001)).as("sum")
        );
        AggregationResults<Map> outputTypeCount5 = this.mongoTemplate.aggregate(aggregation, CoinConst.MONGODB_TRADE_COLLECTION, Map.class);
        if(outputTypeCount5.iterator().hasNext()){
            for (Iterator<Map> iterator = outputTypeCount5.iterator(); iterator.hasNext(); ) {
                Map<String, Object> obj = iterator.next();
                sumMap.put("avg", new BigDecimal((Double) obj.get("avg")));
                sumMap.put("sum", new BigDecimal((Double) obj.get("sum")));
            }
        }
        return sumMap;
    }

    public Map getLastDayBuyFeeTotal(Map params){
        Map sumMap=new HashMap();
        Aggregation aggregation= Aggregation.newAggregation(
                Aggregation.match(getConditionGroup(params)),
                Aggregation.group().sum(ArithmeticOperators.Multiply.valueOf("num").multiplyBy(0.001)).as("sum")
        );
        AggregationResults<Map> outputTypeCount5 = this.mongoTemplate.aggregate(aggregation, CoinConst.MONGODB_TRADE_COLLECTION, Map.class);
        if(outputTypeCount5.iterator().hasNext()){
            for (Iterator<Map> iterator = outputTypeCount5.iterator(); iterator.hasNext(); ) {
                Map<String, Object> obj = iterator.next();
                sumMap.put("sum", new BigDecimal((Double) obj.get("sum")));
            }
        }
        return sumMap;
    }


    /**
     * 从MongoDB获取 最后一条  交易记录ID
     * @return
     */
    public int findLastRecordData() {
        Integer maxTradeId = new Integer(0);
        Aggregation aggregation = Aggregation.newAggregation(Aggregation.group().max("tradeId").as("tradeId"));

        AggregationResults<BasicDBObject> outputTypeCount5 = mongoTemplate.aggregate(aggregation, CoinConst.MONGODB_TRADE_COLLECTION, BasicDBObject.class);

        for (Iterator<BasicDBObject> iterator = outputTypeCount5.iterator(); iterator.hasNext(); ) {
            DBObject obj = iterator.next();
            maxTradeId= (Integer)obj.get("tradeId");
        }
        System.out.println(JSON.toJSONString(maxTradeId));
        return maxTradeId;
    }

    /**
     * 从MongoDB获取 最后一条  交易记录ID
     * @return
     */
    public int findLastOrderRecordData() {
        Integer maxOrderId = new Integer(0);
        Aggregation aggregation = Aggregation.newAggregation(Aggregation.group().max("ordersId").as("ordersId"));

        AggregationResults<BasicDBObject> outputTypeCount5 = mongoTemplate.aggregate(aggregation, CoinConst.MONGODB_ORDERS_COLLECTION, BasicDBObject.class);

        for (Iterator<BasicDBObject> iterator = outputTypeCount5.iterator(); iterator.hasNext(); ) {
            DBObject obj = iterator.next();
            maxOrderId= (Integer)obj.get("ordersId");
        }
        System.out.println(JSON.toJSONString(maxOrderId));
        return maxOrderId;
    }
}
