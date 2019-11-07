package net.cyweb.service;


import com.alibaba.fastjson.JSONObject;
import cyweb.utils.CoinConst;
import net.cyweb.mapper.YangCurrencyPairMapper;
import net.cyweb.model.YangCurrencyPair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class YangPairService extends BaseService<YangCurrencyPair>{


    @Autowired
    private RedisService redisService;

    @Autowired
    private YangCurrencyPairMapper yangCurrencyPairMapper;




    /**
     * 获取交易对的基本信息，加上redis的缓存
     * @return
     */
    public YangCurrencyPair getPairInfo(Integer currency_id,Integer thread_id,boolean isFlash)
    {
        String key = CoinConst.REDIS_YANG_PARI_INFO_ + currency_id +"_"+ thread_id;
        if(  !isFlash  && redisService.get(key) != null)
        {

            return JSONObject.parseObject(redisService.get(key).toString(),YangCurrencyPair.class);
        }else{
            System.out.println("找不到交易对信息,走数据库"+currency_id+"-"+thread_id);
            YangCurrencyPair yangCurrencyPair = new YangCurrencyPair();
            yangCurrencyPair.setCurrencyId(currency_id);
            yangCurrencyPair.setCurrencyTradeId(thread_id);
            YangCurrencyPair yangCurrencyPair1 = yangCurrencyPairMapper.selectOne(yangCurrencyPair);

            if(yangCurrencyPair1 != null)
            {
                redisService.set(key,JSONObject.toJSON(yangCurrencyPair1).toString());
            }

            return yangCurrencyPair1;
        }
    }

    /**
     * 获取交易对的基本信息，加上redis的缓存
     * @return
     */
    public YangCurrencyPair getPairInfo(Integer currency_id,Integer thread_id)
    {
        String key = CoinConst.REDIS_YANG_PARI_INFO_ + currency_id +"_"+ thread_id;
        if(redisService.get(key) != null)
        {

            return JSONObject.parseObject(redisService.get(key).toString(),YangCurrencyPair.class);
        }else{
            System.out.println("找不到交易对信息,走数据库"+currency_id+"-"+thread_id);
            YangCurrencyPair yangCurrencyPair = new YangCurrencyPair();
            yangCurrencyPair.setCurrencyId(currency_id);
            yangCurrencyPair.setCurrencyTradeId(thread_id);
            YangCurrencyPair yangCurrencyPair1 = yangCurrencyPairMapper.selectOne(yangCurrencyPair);

            if(yangCurrencyPair1 != null)
            {
                redisService.set(key,JSONObject.toJSON(yangCurrencyPair1).toString());
            }

            return yangCurrencyPair1;
        }
    }

    /*初始化交易对 买单 卖单*/
    public synchronized void initPairMongo() {
        /*获取所有的交易对信息*/
//        List<YangCurrencyPair> yangCurrencyPairList = yangCurrencyPairMapper.selectAll();
//
//        /*获取所有的mongo 集合名称*/
//        Set<String> mongoTemplateCollectionNames = mongoTemplate.getCollectionNames();
//
////        CollectionOptions collectionOptions = new CollectionOptions(10000, 60, true); //闭环集合，最多设置60个文档
//        CollectionOptions collectionOptions = new CollectionOptions(50000, null, false); //闭环集合，最多设置60个文档
//
//        for (YangCurrencyPair yangCurrencyPair : yangCurrencyPairList) {
//            Boolean pairBuyInited = false;
//            Boolean pairSellInited = false;
//            Boolean pairTradeInited = false;
//
//            String collectionNameForBuy = CoinConst.PAIR_MONGO_BUY + yangCurrencyPair.getCurrencyId() + "_" + yangCurrencyPair.getCurrencyTradeId();
//            String collectionNameForSell = CoinConst.PAIR_MONGO_SELL + yangCurrencyPair.getCurrencyId() + "_" + yangCurrencyPair.getCurrencyTradeId();
//            String collectionNameForTrade = CoinConst.PAIR_MONGO_TRADE + yangCurrencyPair.getCurrencyId() + "_" + yangCurrencyPair.getCurrencyTradeId();
//            for (String collectionName : mongoTemplateCollectionNames) {
//                if (collectionName.equals(collectionNameForBuy)) {
//                    pairBuyInited = true;
//                }
//                if (collectionName.equals(collectionNameForSell)) {
//                    pairSellInited = true;
//                }
//                if (collectionName.equals(collectionNameForTrade)) {
//                    pairTradeInited = true;
//                }
//            }
//            /*初始化当前交易对的买单集合，并创建索引*/
//            if (false == pairBuyInited) {
//                mongoTemplate.createCollection(collectionNameForBuy, collectionOptions);
//                IndexOperations indexOperations = mongoTemplate.indexOps(collectionNameForBuy);
//                Index index = new Index();
//                index.on("price",Sort.Direction.DESC); //按价格
//
//                Index indexUnique = new Index();
//                indexUnique.unique();
//                index.on("orderId",Sort.Direction.DESC); //按订单做唯一索引
//                indexOperations.ensureIndex(index);
//            }
//
//            /*初始化当前交易对的卖单集合，并创建索引*/
//            if(false == pairSellInited){
//                mongoTemplate.createCollection(collectionNameForSell, collectionOptions);
//                IndexOperations indexOperations = mongoTemplate.indexOps(collectionNameForSell);
//                Index index = new Index();
//                index.on("price",Sort.Direction.ASC); //按价格
//                indexOperations.ensureIndex(index);
//
//                Index indexUnique = new Index();
//                indexUnique.unique();
//                index.on("orderId",Sort.Direction.DESC); //按订单做唯一索引
//                indexOperations.ensureIndex(index);
//            }
//
//            /*初始化当前交易对的交易集合，并创建索引*/
//            if(false == pairTradeInited){
//                mongoTemplate.createCollection(collectionNameForTrade, collectionOptions);
//                IndexOperations indexOperations = mongoTemplate.indexOps(collectionNameForTrade);
//                Index index = new Index();
//                index.on("trade_id",Sort.Direction.ASC); //按交易ID
//                indexOperations.ensureIndex(index);
//            }
//
//        }


    }




}
