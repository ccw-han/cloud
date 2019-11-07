package net.cyweb.task;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import cyweb.utils.CoinConst;
import cyweb.utils.DateUtils;
import net.cyweb.model.MgKlineData;
import net.cyweb.model.MgYangTrade;
import net.cyweb.model.YangTrade;
import net.cyweb.service.RedisService;
import net.cyweb.service.YangTradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
@Scope("prototype")
public class KlineHistoryTask implements Runnable{

    @Autowired
    private YangTradeService yangTradeService;
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private RedisService redisService;

    public KlineHistoryTask(YangTradeService yangTradeService,MongoTemplate mongoTemplate,RedisService redisService){
        this.yangTradeService=yangTradeService;
        this.mongoTemplate=mongoTemplate;
        this.redisService=redisService;
    }

    private int  oneMinuteStart=0;
    private int  threeMinuteStart=0;
    private int  fiveMinuteStart=0;
    private int  fiftenMinuteStart=0;
    private int  thirtyMinuteStart=0;
    private int  oneHourStart=0;
    @Override
    public void run() {
//        if(1==1){
//        return;
//        }
        try {
            System.out.println("-----------------初始化开始---------------------");
            //开始执行时间戳
            int cTimes = DateUtils.getNowTimes();
//            int cTimes=1536582957;
            //起始时间
            int begin = 0;
            if(redisService.exists(CoinConst.KLINE_HISTORY_NOW_TIME_END)){
                begin=JSONObject.parseObject(redisService.get(CoinConst.KLINE_HISTORY_NOW_TIME_END).toString(),Integer.class).intValue();
            }else{
                 //表中起始时间
                begin = 1530692987;
//                begin=1539327725;
                redisService.set(CoinConst.KLINE_HISTORY_FINAL_END_TIME, cTimes);
            }
            //创建 力度  开始节点  时间戳
             oneMinuteStart=cTimes-172800;
             threeMinuteStart=cTimes-86400*6;
             fiveMinuteStart=cTimes-86400*12;
             fiftenMinuteStart=cTimes-86400*38;
             thirtyMinuteStart=cTimes-86400*74;
             oneHourStart=cTimes-86400*150;

            Map<String, Object> pama = new HashMap<>();
            int step = 106400;
            int size = (cTimes-begin) / step;
            if(size==0){
                if((cTimes-begin) % step>0){
                    pama.put("startTime", begin);
                    pama.put("endTime",cTimes);
                    dealDataCollect(pama);
                    redisService.set(CoinConst.KLINE_HISTORY_NOW_TIME_START, begin);
                    redisService.set(CoinConst.KLINE_HISTORY_NOW_TIME_END,cTimes);
                }
            }else{
                for (int j = 0; j < size; j++) {
                    pama.put("startTime", begin + j * step);
                    pama.put("endTime",begin+(j + 1) * step);
//                dealData(pama);
                    dealDataCollect(pama);
                    redisService.set(CoinConst.KLINE_HISTORY_NOW_TIME_START, begin + j * step);
                    redisService.set(CoinConst.KLINE_HISTORY_NOW_TIME_END,begin+(j + 1) * step);
                    //最后一条数据
                    if(j+1==size){
                        if((cTimes-begin) % step>0){
                            pama.put("startTime", begin + (j+1) * step);
                            pama.put("endTime",cTimes);
                            dealDataCollect(pama);
                            redisService.set(CoinConst.KLINE_HISTORY_NOW_TIME_START, begin + (j+1) * step);
                            redisService.set(CoinConst.KLINE_HISTORY_NOW_TIME_END,cTimes);
                        }
                    }
                }
            }

            //去除 7-15  00:00:00 至 7-15 20:00:00  数据
//          addTime
//            pama.put("notendTime", 1531656000);
//
//            int stepa = 86400;
//            int sizea = (notBegin- begin)/stepa;
//            if ((notBegin- begin)%stepa > 0) {
//                sizea = sizea + 1;
//            }
//            for (int i = 0; i < sizea; i++) {
//                if(i==sizea-1){
//                    if((notBegin- begin)%stepa > 0){
//                        pama.put("startTime", begin + i * stepa);
//                        pama.put("endTime",notBegin);
//                    }else{
//                        pama.put("startTime", begin + i * stepa);
//                        pama.put("endTime", begin + (i + 1) * stepa);
//                    }
//                }else{
//                pama.put("startTime", begin + i * stepa);
//                pama.put("endTime", begin + (i + 1) * stepa);
//
//                }
//                //从MongoDB获取历史k线数据
//                dealData(pama,"0");
//            }
//            System.out.println("15号之前数据 跑完");
//
//            int step = 3600;
//            int size = (cTimes-notEnd) / step;
//            if ((cTimes-notEnd) % step > 0) {
//                size = size + 1;
//            }
//            for (int j = 0; j < size; j++) {
//                if(j==size-1){
//                    if((cTimes-notEnd)%step > 0){
//                        pama.put("startTime", notEnd + j * step);
//                        pama.put("endTime",cTimes);
//                    }else{
//                        pama.put("startTime", notEnd + j * step);
//                        pama.put("endTime", notEnd + (j + 1) * step);
//                    }
//                }else{
//                    pama.put("startTime", notEnd + j * step);
//                    pama.put("endTime", notEnd + (j + 1) * step);
//
//                }
//                //从MongoDB获取历史k线数据
//                dealData(pama,"1");
//            }
            redisService.set(CoinConst.KLINE_HISTORY_END_FLAG,DateUtils.getNowTimes());
                System.out.println("-----------------初始化完成---------------------");
                //往后累计时间戳
                Thread.sleep(40000);
            }catch(Exception e){
                e.printStackTrace();
            }
        }

    public void dealDataCollect(Map pama){
        int nowTimes=(int)pama.get("endTime");
         for(int i=0;i<getDataType().size();i++){
            if("oneMinute".equals(getDataType().get(i))){
                if(nowTimes<=oneMinuteStart){
                    continue;
                }
            }
            if("threeMinute".equals(getDataType().get(i))){
                if(nowTimes<=threeMinuteStart){
                    continue;
                }
            }

            if("fiveMinute".equals(getDataType().get(i))){
                if(nowTimes<=fiveMinuteStart){
                    continue;
                }
            }

            if("fifteenMinute".equals(getDataType().get(i))){
                if(nowTimes<=fiftenMinuteStart){
                    continue;
                }
            }

            if("thirtyMinute".equals(getDataType().get(i))){
                if(nowTimes<=thirtyMinuteStart){
                    continue;
                }
            }
            if("oneHour".equals(getDataType().get(i))){
                if(nowTimes<=oneHourStart){
                    continue;
                }
            }
            //批量插入的map集合    当前 查询力度   下的批量新增
            //两张表 分别查询录入 mongoddb
            pama.put("times",getDataTypeTimes(getDataType().get(i)));
            List<HashMap> tradeList= yangTradeService.queryKlineHistoryData(pama);
            List<HashMap> tradeListA= yangTradeService.queryKlineHistoryDataA(pama);
            updateAndInsertIntoMongodb(tradeList,i);
            updateAndInsertIntoMongodb(tradeListA,i);

        }
    }

    public void updateAndInsertIntoMongodb(List<HashMap> tradeList,int i){
        Map<String,List<MgKlineData>> insertBatchListMap=new HashMap<String,List<MgKlineData>>();
        for (int j = 0; j < tradeList.size(); j++) {
            Map trade = tradeList.get(j);
            System.out.println("<><><><><><><><><><><><><>开始存入mongodb_______"+getDataType().get(i)+"<><><>"+trade.get("trade_id")+"<><><"+trade.get("trade_time"));
            long dataKey=((BigDecimal)trade.get("group_time")).longValue();
            Query query = new Query();
            query.addCriteria(Criteria.where("scaleKey").is(dataKey));
            //动态读取  不同刻度集合数据
            String collectionName = CoinConst.KLINE_MONGODB_PREX + getDataType().get(i) + "_" + trade.get("currencyId") + "_" + trade.get("currencyTradeId");
            mongoTemplate.getCollection(collectionName);
            List<MgKlineData> list = mongoTemplate.find(query, MgKlineData.class, collectionName);
            //当前订单 价格和数量
            BigDecimal maxPrice = (BigDecimal) trade.get("maxPrice");
            BigDecimal minPrice = (BigDecimal) trade.get("minPrice");
            BigDecimal num =(BigDecimal)trade.get("num");
            long count =(long)trade.get("count");
            BigDecimal openPrice =(BigDecimal)trade.get("open_price");
            BigDecimal closePrice =new BigDecimal((String)trade.get("close_price"));
            //没有当前刻度的  数据 新增操作
            if(list.size()==0){
                int currencyId=(int)trade.get("currencyId");
                int currencyTradeId=(int)trade.get("currencyTradeId");
                MgKlineData mgKlineData=new MgKlineData();
                mgKlineData.setCount(num.longValue());
                mgKlineData.setOpenPrice(openPrice.doubleValue());//开盘价
                mgKlineData.setClosePrice(closePrice.doubleValue());//闭盘价
                mgKlineData.setGroupTime(dataKey);//顺序
                mgKlineData.setTradeTime(((BigDecimal)trade.get("trade_time")).longValue());//集合段交易时间
                mgKlineData.setLastTradeTime(Long.valueOf((int)trade.get("lastTradeTime")));//最早下单时间
                mgKlineData.setMaxPrice(maxPrice.doubleValue());//最大价格
                mgKlineData.setMinPrice(minPrice.doubleValue());//最小价格
                mgKlineData.setNum(num.doubleValue());//数量
                mgKlineData.setScaleKey(dataKey);//datakey
                mgKlineData.setNowTradeTime(Long.valueOf((int)trade.get("maxTradeTime")));//最晚下单时间
                mgKlineData.setCurrencyId(currencyId);
                mgKlineData.setCurrencyTradeId(currencyTradeId);
                List<MgKlineData> mgList=insertBatchListMap.get(collectionName);
                if(mgList==null){
                    mgList=new ArrayList<MgKlineData>();
                }
                mgList.add(mgKlineData);
                insertBatchListMap.put(collectionName,mgList);
//                        mongoTemplate.getCollection(collectionName);
//                        this.mongoTemplate.insert(mgKlineData,collectionName);
//                System.out.println("      -----"+collectionName+" 新增成功 ");
            }else{
                MgKlineData mgKlineData=list.get(0);
                Update update=new Update();
                //最大价格
                if(maxPrice.doubleValue()>mgKlineData.getMaxPrice()){
                    update.set("maxPrice",maxPrice.doubleValue());
                }
                //最小价格
                if(minPrice.doubleValue()<mgKlineData.getMinPrice()){
                    update.set("minPrice",minPrice.doubleValue());
                }
                //总计条数
                update.set("count",mgKlineData.getCount()+count);
                //总计数量
                update.set("num",mgKlineData.getNum()+num.doubleValue());
                //交易时间
                if((int)trade.get("maxTradeTime")>mgKlineData.getNowTradeTime().intValue()){
                    update.set("nowTradeTime",(int)trade.get("maxTradeTime"));
                    update.set("closePrice",trade.get("close_price"));
                }
                if((int)trade.get("lastTradeTime")<mgKlineData.getLastTradeTime().intValue()){
                        update.set("openPrice",trade.get("open_price"));
                        update.set("lastTradeTime",trade.get("lastTradeTime"));
                }
                //开始遇见 初始化时同时开始的增量数据
//                if(mgKlineData.getLastTradeTime()!=null&&mgKlineData.getLastTradeTime().intValue()>0){
//                    if((int)trade.get("lastTradeTime")<mgKlineData.getLastTradeTime().intValue()){
//                        update.set("openPrice",openPrice.doubleValue());
//                        update.set("lastTradeTime",(int)trade.get("lastTradeTime"));
//                    }
//                }
                mongoTemplate.getCollection(collectionName);
                this.mongoTemplate.updateFirst(query,update,collectionName);
//                    System.out.println("      -----"+collectionName+" 修改成功 ");
            }
        }
        //开始批量插入  当前时间力度的数据
        for (Map.Entry<String, List<MgKlineData>> entry : insertBatchListMap.entrySet()) {
            this.mongoTemplate.getCollection(entry.getKey());
            this.mongoTemplate.insert(entry.getValue(),entry.getKey());
        }
    }

    //获取 集合类型列表
    private static List<String> getDataType(){
        List<String> list=new ArrayList<String>();
        list.add("oneMinute");
        list.add("threeMinute");
        list.add("fiveMinute");
        list.add("fifteenMinute");
        list.add("thirtyMinute");
        list.add("oneHour");
        list.add("sixHour");
        list.add("oneDay");
        list.add("oneWeek");
        return list;
    }
        //根据 类型获取 时间单位
    private static int getDataTypeTimes(String dataType){
        int timesUnit=0;
        switch (dataType){
            case "oneMinute":
                timesUnit= 60;break;
            case "threeMinute":
                timesUnit= 180;break;
            case "fiveMinute":
                timesUnit= 300;break;
            case "fifteenMinute":
                timesUnit= 900;break;
            case "thirtyMinute":
                timesUnit= 1800;break;
            case "oneHour":
                timesUnit= 3600;break;
            case "sixHour":
                timesUnit= 21600;break;
            case "oneDay":
                timesUnit= 86400;break;
            case "oneWeek":
                timesUnit =604800;break;
        }
        return timesUnit;
    }

}
