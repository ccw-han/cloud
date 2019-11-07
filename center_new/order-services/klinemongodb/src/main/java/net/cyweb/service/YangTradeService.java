package net.cyweb.service;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import cyweb.utils.CoinConst;
import net.cyweb.mapper.YangTradeMapper;
import net.cyweb.model.MgYangTrade;
import net.cyweb.model.YangTrade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@EnableAsync
public class YangTradeService extends BaseService<YangTrade> {


    @Autowired
    private YangTradeMapper yangTradeMapper;

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * K线数据
     *
     * @param map
     * @return
     */
    public List<HashMap> getKlineData(Map map) {
        return yangTradeMapper.klineData(map);
    }


    /**
     * K线数据
     *
     * @param map
     * @return
     */
    public List<HashMap> getKlineDataA(Map map) {
        return yangTradeMapper.klineDataA(map);
    }


    /**
     * 初始化方法K线数据
     *
     * @param map
     * @return
     */
    public List<HashMap> queryKlineHistoryData(Map map) {
        return yangTradeMapper.queryKlineHistoryData(map);
    }

    /**
     * 初始化方法K线数据
     *
     * @param map
     * @return
     */
    public List<HashMap> queryKlineHistoryDataA(Map map) {
        return yangTradeMapper.queryKlineHistoryDataA(map);
    }

    /**
     * 从mongodb获取数据
     */
    public List<MgYangTrade> getTradeDataFromMongoDB(Map map) {
        DBObject query1 = new BasicDBObject(); //setup the query criteria 设置查询条件
        query1.put("addTime", (new BasicDBObject("$gte", (int) map.get("startTime"))).append("$lt", (int) map.get("endTime")));
        DBCursor dbCursor = mongoTemplate.getCollection(CoinConst.MONGODB_TRADE_COLLECTION).find(query1);
        List<MgYangTrade> list = new ArrayList<>();
        while (dbCursor.hasNext()) {
            DBObject object = dbCursor.next();
            MgYangTrade mg = new MgYangTrade();
            mg.setTradeId((int) object.get("tradeId"));
            mg.setCurrencyId((int) object.get("currencyId"));
            mg.setCurrencyTradeId((int) object.get("currencyTradeId"));
            mg.setPrice((double) object.get("price"));
            mg.setNum((double) object.get("num"));
            mg.setAddTime((int) object.get("addTime"));
            list.add(mg);
        }
        return list;
    }

    /**
     * 从mongodb获取数据
     */
    public List<MgYangTrade> getTradeDataFromMongoDBB(Map map) {
        DBObject query1 = new BasicDBObject(); //setup the query criteria 设置查询条件
        query1.put("addTime", (new BasicDBObject("$gt", (int) map.get("startTime"))).append("$lte", (int) map.get("endTime")));
        DBCursor dbCursor = mongoTemplate.getCollection(CoinConst.MONGODB_TRADE_COLLECTION).find(query1);
        List<MgYangTrade> list = new ArrayList<>();
        while (dbCursor.hasNext()) {
            DBObject object = dbCursor.next();
            MgYangTrade mg = new MgYangTrade();
            mg.setTradeId((int) object.get("tradeId"));
            mg.setCurrencyId((int) object.get("currencyId"));
            mg.setCurrencyTradeId((int) object.get("currencyTradeId"));
            mg.setPrice((double) object.get("price"));
            mg.setNum((double) object.get("num"));
            mg.setAddTime((int) object.get("addTime"));
            list.add(mg);
        }
        return list;
    }

    /**
     * 从数据库获取数据S
     */
    public List<YangTrade> getTradeDataFromSql(Map map) {
        Example example = new Example(YangTrade.class);
        example.createCriteria().andGreaterThanOrEqualTo("addTime", map.get("startTime")).
                andLessThan("addTime", map.get("endTime"));
        example.setOrderByClause("addTime  asc");
        List<YangTrade> yangTradeList = selectByExample(example);
        return yangTradeList;
    }

    /**
     * 从数据库获取数据S
     */
    public List<YangTrade> getTradeDataFromSqlB(Map map) {
        Example example = new Example(YangTrade.class);
        example.createCriteria().andGreaterThan("addTime", map.get("startTime")).
                andLessThanOrEqualTo("addTime", map.get("endTime"));
        example.setOrderByClause("addTime  asc");
        List<YangTrade> yangTradeList = selectByExample(example);
        return yangTradeList;
    }

    /**
     * 从数据库获取数据S ccw
     */
    public List<YangTrade> getTradeDataFromSqlC() {
//        Example example = new Example(YangTrade.class);
//        example.setOrderByClause("addTime  asc");
//        List<YangTrade> yangTradeList = selectByExample(example);
        List<YangTrade> yangTradeList = yangTradeMapper.getAllYangTrades();
        return yangTradeList;
    }

    public List<YangTrade> tradeData(Map map) {
        return yangTradeMapper.tradeData(map);
    }

    public void saveBatch1(List<YangTrade> list, String collectionName) {
        mongoTemplate.getCollection(collectionName);
        this.mongoTemplate.insert(list, collectionName);
    }

    public void saveBatch(List<MgYangTrade> list, String collectionName) {
        mongoTemplate.getCollection(collectionName);
        this.mongoTemplate.insert(list, collectionName);
    }

}
