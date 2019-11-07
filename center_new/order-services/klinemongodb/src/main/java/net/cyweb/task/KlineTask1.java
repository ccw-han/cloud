package net.cyweb.task;

import com.alibaba.fastjson.JSONObject;
import com.mongodb.DBCollection;
import cyweb.utils.CoinConst;
import cyweb.utils.DateUtils;
import net.cyweb.model.MgKlineData;
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
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@Scope("prototype")
public class KlineTask1 implements Runnable {

    @Autowired
    private RedisService redisService;

    @Autowired
    private MongoTemplate mongoTemplate;


    @Autowired
    private YangTradeService yangTradeService;

    public KlineTask1(RedisService redisService, MongoTemplate mongoTemplate, YangTradeService yangTradeService) {
        this.redisService = redisService;
        this.mongoTemplate = mongoTemplate;
        this.yangTradeService = yangTradeService;
    }

    @Override
    public void run() {
        while (true) {
            try {
                //批量插入的map集合    当前 查询力度   下的批量新增
                Map<String, List<MgKlineData>> insertBatchListMap = new HashMap<String, List<MgKlineData>>();
                Map<String, Map<String, Object>> allCollectionMap = new HashMap<String, Map<String, Object>>();
                int size = redisService.listSize(CoinConst.KLINE_TRADE_DATA).intValue();
                //判断 交易记录缓存是否存在
                if (size > 1) {
//                    LinkedList<YangTrade> tradeList=new LinkedList<YangTrade>();
                    if (size > 50000) {
                        size = 50000;
                    }
                    //从redis中 批量取出数据 并删除 数据
                    List<YangTrade> alist = JSONObject.parseArray(redisService.lRange(CoinConst.KLINE_TRADE_DATA, 0, size - 1).toString(), YangTrade.class);
                    redisService.lTrim(CoinConst.KLINE_TRADE_DATA, size, -1);

                    //将 实时trade 记录 存入mongodb
                    tradeData(alist);
//                    for(YangTrade yangTrade:alist){
//                        tradeList.add(yangTrade);
//                    }
//                    Collections.sort(tradeList,new YangTradeCompareByAddTime());
                    //
                    if (alist.size() > 0) {
                        System.out.println("<><><><><><><redis有值  K线正向数据  开始 录入 mongodb><><><><><><");
                        //判断是否存在库
                        DBCollection db = this.mongoTemplate.getCollection("funcoin");
                        //0000000000
                        for (int i = 0; i < alist.size(); i++) {
                            YangTrade trade = alist.get(i);
                            System.out.println("<><><><><>tradeAddTime<><><><><><" + trade.getAddTime());
                            Long times = trade.getAddTime();
                            //计算当前交易记录Trade 在9个 力度集合中的刻度信息scaleKey
                            Map<String, Object> mongoMap = KlineTask1.getMongoDataId(times);
                            for (Map.Entry<String, Object> mongoMapEntry : mongoMap.entrySet()) {
                                Map<String, Object> tiemsMap = (Map) mongoMapEntry.getValue();
                                long dataKey = (long) tiemsMap.get("datakey");//刻度
                                int timesKey = (int) tiemsMap.get("times");//力度

                                Map<String, Object> scaleMap;
                                //存在 力度集合
                                if (allCollectionMap.containsKey(mongoMapEntry.getKey())) {
                                    scaleMap = allCollectionMap.get(mongoMapEntry.getKey());
                                } else {
                                    scaleMap = new HashMap<String, Object>();
                                }
                                //  40_0_232323  currencyId_currencyTradeId_scaleKey
                                String scaleKey = trade.getCurrencyId().toString() + "_" + trade.getCurrencyTradeId().toString() + "|" + dataKey;
                                BigDecimal price = trade.getPrice().setScale(18, BigDecimal.ROUND_HALF_DOWN);
                                BigDecimal num = trade.getNum().setScale(18, BigDecimal.ROUND_HALF_DOWN);
                                //如果当前力度已经做过新增
                                if (scaleMap.containsKey(scaleKey)) {
                                    MgKlineData updateMgKlineDate = (MgKlineData) scaleMap.get(scaleKey);
                                    //最大价格
                                    if (price.doubleValue() > updateMgKlineDate.getMaxPrice()) {
                                        updateMgKlineDate.setMaxPrice(price.doubleValue());
                                    }
                                    //最小价格
                                    if (price.doubleValue() < updateMgKlineDate.getMinPrice()) {
                                        updateMgKlineDate.setMinPrice(price.doubleValue());
                                    }
                                    updateMgKlineDate.setCount(updateMgKlineDate.getCount() + 1L);
                                    updateMgKlineDate.setNum(updateMgKlineDate.getNum() + num.doubleValue());
                                    updateMgKlineDate.setNowTradeTime(trade.getAddTime());
                                    updateMgKlineDate.setClosePrice(price.doubleValue());
                                } else {
                                    MgKlineData insertMgKlineDate = new MgKlineData();
                                    insertMgKlineDate.setCount(1L);
                                    insertMgKlineDate.setOpenPrice(price.doubleValue());//开盘价
                                    insertMgKlineDate.setClosePrice(price.doubleValue());//闭盘价
                                    insertMgKlineDate.setGroupTime(dataKey);//顺序
                                    insertMgKlineDate.setTradeTime(dataKey * timesKey);//集合段交易时间
                                    insertMgKlineDate.setMaxPrice(price.doubleValue());//最大价格
                                    insertMgKlineDate.setMinPrice(price.doubleValue());//最小价格
                                    insertMgKlineDate.setNum(num.doubleValue());//数量
                                    insertMgKlineDate.setScaleKey(dataKey);//datakey
                                    insertMgKlineDate.setNowTradeTime(trade.getAddTime());
                                    insertMgKlineDate.setCurrencyId(trade.getCurrencyId());
                                    insertMgKlineDate.setCurrencyTradeId(trade.getCurrencyTradeId());
                                    insertMgKlineDate.setLastTradeTime(trade.getAddTime());
                                    scaleMap.put(scaleKey, insertMgKlineDate);
                                }
                                allCollectionMap.put(mongoMapEntry.getKey(), scaleMap);
                            }
                        }
                        //-----redis当前数据整合完毕  相当于在数据库中group了一遍数据 开始正向入库
                        for (Map.Entry<String, Map<String, Object>> everCollectionMap : allCollectionMap.entrySet()) {
                            Map<String, Object> everMap = everCollectionMap.getValue();
                            String timesKey = everCollectionMap.getKey();//力度
                            for (Map.Entry<String, Object> sacleMap : everMap.entrySet()) {
                                MgKlineData everMgK = (MgKlineData) sacleMap.getValue();
                                String scaleKey = sacleMap.getKey().substring(sacleMap.getKey().indexOf("|") + 1, sacleMap.getKey().length());
                                Query query = new Query();
                                query.addCriteria(Criteria.where("scaleKey").is(Integer.valueOf(scaleKey)));
                                String collectionName = CoinConst.KLINE_MONGODB_PREX + timesKey + "_" + sacleMap.getKey().substring(0, sacleMap.getKey().indexOf("|"));
                                List<MgKlineData> list = mongoTemplate.find(query, MgKlineData.class, collectionName);
                                //当前订单 价格和数量

                                if (list.size() == 0) {
                                    List<MgKlineData> mgList = insertBatchListMap.get(collectionName);
                                    if (mgList == null) {
                                        mgList = new ArrayList<MgKlineData>();
                                    }
                                    mgList.add(everMgK);
                                    insertBatchListMap.put(collectionName, mgList);
                                } else {
                                    Update update = new Update();
                                    MgKlineData nowUpdateMgKlineData = list.get(0);
                                    if (everMgK.getMaxPrice() > nowUpdateMgKlineData.getMaxPrice()) {
                                        update.set("maxPrice", everMgK.getMaxPrice());
                                    }
                                    if (everMgK.getMinPrice() < nowUpdateMgKlineData.getMinPrice()) {
                                        update.set("minPrice", everMgK.getMinPrice());
                                    }
                                    //总计条数
                                    update.set("count", nowUpdateMgKlineData.getCount() + everMgK.getCount());
                                    //总计数量
                                    update.set("num", nowUpdateMgKlineData.getNum() + everMgK.getNum());
                                    update.set("closePrice", everMgK.getClosePrice());
                                    mongoTemplate.getCollection(collectionName);
                                    this.mongoTemplate.updateFirst(query, update, collectionName);
                                }

                            }

                        }
                        //开始批量插入  当前时间力度的数据
                        for (Map.Entry<String, List<MgKlineData>> entry : insertBatchListMap.entrySet()) {
                            this.mongoTemplate.getCollection(entry.getKey());
                            this.mongoTemplate.insert(entry.getValue(), entry.getKey());
                        }

                        //                                List<MgKlineData> list= mongoTemplate.find(query,MgKlineData.class,collectionName);
//                                //没有当前刻度的  数据 新增操作
//                                if(list.size()==0){
//                                    System.out.println("insert<><><><><><"+(String)mongoMapEntry.getKey()+"<><><><><><><><>"+trade.getAddTime());
//                                    MgKlineData mgKlineData=new MgKlineData();
//                                    BigDecimal price=trade.getPrice().setScale(18,BigDecimal.ROUND_HALF_DOWN);
//                                    BigDecimal num=trade.getNum().setScale(18,BigDecimal.ROUND_HALF_DOWN);
//                                    mgKlineData.setCount(1L);
//                                    mgKlineData.setOpenPrice(price.doubleValue());//开盘价
//                                    mgKlineData.setClosePrice(price.doubleValue());//闭盘价
//                                    mgKlineData.setGroupTime((long)tiemsMap.get("datakey"));//顺序
//                                    mgKlineData.setTradeTime((long)tiemsMap.get("datakey")*(int)tiemsMap.get("times"));//集合段交易时间
//                                    mgKlineData.setMaxPrice(price.doubleValue());//最大价格
//                                    mgKlineData.setMinPrice(price.doubleValue());//最小价格
//                                    mgKlineData.setNum(num.doubleValue());//数量
//                                    mgKlineData.setScaleKey((long)tiemsMap.get("datakey"));//datakey
//                                    mgKlineData.setNowTradeTime(trade.getAddTime());
//                                    mgKlineData.setCurrencyId(trade.getCurrencyId());
//                                    mgKlineData.setCurrencyTradeId(trade.getCurrencyTradeId());
//                                    mgKlineData.setLastTradeTime(trade.getAddTime());
//                                    mongoTemplate.getCollection(collectionName);
//                                    this.mongoTemplate.insert(mgKlineData,collectionName);
//                                }else{
//                                    System.out.println("update<><><><>"+(String)mongoMapEntry.getKey()+"<><><><><><><><>"+trade.getAddTime());
//                                    MgKlineData mgKlineData=list.get(0);
//                                    Update update=new Update();
//                                    BigDecimal price=trade.getPrice().setScale(18,BigDecimal.ROUND_HALF_DOWN);
//                                    BigDecimal num=trade.getNum().setScale(18,BigDecimal.ROUND_HALF_DOWN);
//                                    //最大价格
//                                    if(price.doubleValue()>mgKlineData.getMaxPrice()){
//                                        update.set("maxPrice",price.doubleValue());
//                                    }
//                                    //最小价格
//                                    if(price.doubleValue()<mgKlineData.getMinPrice()){
//                                        update.set("minPrice",price.doubleValue());
//                                    }
//                                    //总计条数
//                                    update.set("count",mgKlineData.getCount()+1);
//                                    //总计数量
//                                    update.set("num",mgKlineData.getNum()+num.doubleValue());
//                                    //交易时间
////                                    if(trade.getAddTime()>mgKlineData.getNowTradeTime()){
//                                    update.set("nowTradeTime",trade.getAddTime());
//                                    update.set("closePrice",price.doubleValue());
////                                    }
//                                    mongoTemplate.getCollection(collectionName);
//                                    this.mongoTemplate.updateFirst(query,update,collectionName);
//                                }

                        Thread.sleep(200);
                    } else {
                        //交易记录 redis缓存 当前时段为空
                        Thread.sleep(200);
                    }
                } else {
                    //redis缓存不存在 继续休眠 往里插一些数据 ccw
                    List<YangTrade> yangTrades = yangTradeService.getTradeDataFromSqlC();
                    if (yangTrades.size() > 0) {
                        for (YangTrade yangTrade : yangTrades) {
                            redisService.lPush(CoinConst.KLINE_TRADE_DATA, JSONObject.toJSONString(yangTrade));
                        }
                    }

                    Thread.sleep(200);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    class YangTradeCompareByAddTime implements Comparator<YangTrade> {
        @Override
        public int compare(YangTrade e1, YangTrade e2) {
            if (e1.getAddTime().intValue() - e2.getAddTime().intValue() < 0) {
                return -1;
            } else {
                return 1;
            }
        }
    }

    /**
     * 根据addTIme判断 判断增量
     *
     * @return
     */
    public static Map getMongoDataId(Long addTime) {
        Map<String, Object> map = new HashMap<String, Object>();
        Map<String, Object> mapa = new HashMap<String, Object>();
        Map<String, Object> mapb = new HashMap<String, Object>();
        Map<String, Object> mapc = new HashMap<String, Object>();
        Map<String, Object> mapd = new HashMap<String, Object>();
        Map<String, Object> mape = new HashMap<String, Object>();
        Map<String, Object> mapf = new HashMap<String, Object>();
        Map<String, Object> mapg = new HashMap<String, Object>();
        Map<String, Object> maph = new HashMap<String, Object>();
        Map<String, Object> mapi = new HashMap<String, Object>();
        mapa.put("times", 60);
        mapb.put("times", 180);
        mapc.put("times", 300);
        mapd.put("times", 900);
        mape.put("times", 1800);
        mapf.put("times", 3600);
        mapg.put("times", 21600);
        maph.put("times", 86400);
        mapi.put("times", 604800);
        //1分钟线
        Long oneMinute = addTime / 60;//取商
        Long oneMinuteLeft = addTime % 60;//取余
        if (oneMinuteLeft >= 30) {
            mapa.put("datakey", oneMinute + 1);
        } else {
            mapa.put("datakey", oneMinute);
        }
        //3分钟线
        Long threeMinute = addTime / 180;//取商
        Long threeMinuteLeft = addTime % 180;//取余
        if (threeMinuteLeft >= 90) {
            mapb.put("datakey", threeMinute + 1);
        } else {
            mapb.put("datakey", threeMinute);
        }
        //5分钟线
        Long fiveMinute = addTime / 300;//取商
        Long fivemMinuteLeft = addTime % 300;//取余
        if (fivemMinuteLeft >= 150) {
            mapc.put("datakey", fiveMinute + 1);
        } else {
            mapc.put("datakey", fiveMinute);
        }
        //15分钟线
        Long fifteenMinute = addTime / 900;//取商
        Long fifteenMinuteLeft = addTime % 900;//取余
        if (fifteenMinuteLeft >= 450) {
            mapd.put("datakey", fifteenMinute + 1);
        } else {
            mapd.put("datakey", fifteenMinute);
        }
        //30分钟线
        Long thirtyMinute = addTime / 1800;//取商
        Long thirtyMinuteLeft = addTime % 1800;//取余
        if (thirtyMinuteLeft >= 900) {
            mape.put("datakey", thirtyMinute + 1);
        } else {
            mape.put("datakey", thirtyMinute);
        }

        //1小时
        Long oneHour = addTime / 3600;//取商
        Long oneHourLeft = addTime % 3600;//取余
        if (oneHourLeft >= 1800) {
            mapf.put("datakey", oneHour + 1);
        } else {
            mapf.put("datakey", oneHour);
        }
        //6小时线
        Long sixHour = addTime / 21600;//取商
        Long sixHourLeft = addTime % 21600;//取余
        if (sixHourLeft >= 10800) {
            mapg.put("datakey", sixHour + 1);
        } else {
            mapg.put("datakey", sixHour);
        }
        //1天线
        Long oneDay = addTime / 86400;//取商
        Long oneDayLeft = addTime % 86400;//取余
        if (oneDayLeft >= 43200) {
            maph.put("datakey", oneDay + 1);
        } else {
            maph.put("datakey", oneDay);
        }
        //一周线
        Long onWeek = addTime / 604800;//取商
        Long onWeekLeft = addTime % 604800;//取余
        if (onWeekLeft >= 302400) {
            mapi.put("datakey", onWeek + 1);
        } else {
            mapi.put("datakey", onWeek);
        }
        map.put("oneMinute", mapa);
        map.put("threeMinute", mapb);
        map.put("fiveMinute", mapc);
        map.put("fifteenMinute", mapd);
        map.put("thirtyMinute", mape);
        map.put("oneHour", mapf);
        map.put("sixHour", mapg);
        map.put("oneDay", maph);
        map.put("oneWeek", mapi);
        return map;

    }

    /**
     * 判断传入日期是星期几
     *
     * @param pTime
     * @return
     * @throws Exception
     */
    public static int dayForWeek(String pTime) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar c = Calendar.getInstance();
        c.setTime(sdf.parse(pTime));
        int dayForWeek = 0;
        if (c.get(Calendar.DAY_OF_WEEK) == 1) {
            dayForWeek = 7;
        } else {
            dayForWeek = c.get(Calendar.DAY_OF_WEEK) - 1;
        }
        return dayForWeek;
    }

    public void tradeData(List<YangTrade> list) {
//        List<MgYangTrade> mgList = new ArrayList<MgYangTrade>();
        if (list.size() > 0) {
//            for (int i = 0; i < list.size(); i++) {
//                YangTrade ft = list.get(i);
//                MgYangTrade mg = new MgYangTrade();
////                mg.setTradeId(Integer.valueOf(ft.getTradeId()));
//                mg.setTradeNo(ft.getTradeNo());
//                mg.setMemberId(Integer.valueOf(ft.getMemberId()));
//                mg.setCurrencyId(Integer.valueOf(ft.getCurrencyId()));
//                mg.setCurrencyTradeId(Integer.valueOf(ft.getCurrencyTradeId()));
//                mg.setPrice(ft.getPrice().doubleValue());
//                mg.setNum(ft.getNum().doubleValue());
//                mg.setMoney(ft.getMoney().doubleValue());
//                mg.setFee(ft.getFee().doubleValue());
//                mg.setType(ft.getType());
//                mg.setAddTime(ft.getAddTime().intValue());
//                mg.setStatus(Integer.valueOf(ft.getStatus()));
//                mg.setShow(Integer.valueOf(ft.getShow()));
//                mg.setName(ft.getMemberName());
//                mg.setTitle(ft.getCurrencyTitle());
//                mg.setCurrencyMark(ft.getCurrencyMark());
//                mg.setIsShua(Integer.valueOf(ft.getIsShua()));
//                mgList.add(mg);
//            }
            yangTradeService.saveBatch1(list, CoinConst.MONGODB_TRADE_COLLECTION);
        }


    }


    public static void main(String[] args) throws Exception {

        System.out.println(KlineTask1.dayForWeek(DateUtils.getDataStampStr(1535971293L)));
        System.out.println(KlineTask1.getMongoDataId(1535971293L));
        long a = System.currentTimeMillis() / 1000;
        int b = (int) a;
        System.out.println(b);
    }
}
