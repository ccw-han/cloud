package net.cyweb.service;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.mongodb.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import cyweb.utils.CoinConst;
import cyweb.utils.CommonTools;
import cyweb.utils.DateUtils;
import cyweb.utils.ErrorCode;
import net.cyweb.mapper.*;
import net.cyweb.model.*;
import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@EnableAsync
public class YangOrderService extends BaseService<YangOrders> {
    @Autowired
    private YangMemberService yangMemberService;

    public enum OrderStatus {
        ORDER_TYPE_INITIAL((byte) 0, "初始状态"),
        ORDER_TYPE_PART_FINISHED((byte) 1, "部分成交"),
        ORDER_TYPE_CANCELED((byte) -1, "撤销"),
        ORDER_TYPE_FINISHED((byte) 2, "成交");
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
    private YangConfigMapper yangConfigMapper;

    @Autowired
    private YangTradeFeeMapper yangTradeFeeMapper;

    @Autowired
    private YangOrdersMapper yangOrdersMapper;

    @Autowired
    private YangTradeMapper yangTradeMapper;

    @Autowired
    private YangFinanceMapper yangFinanceMapper;

    @Autowired
    private YangMemberMapper yangMemberMapper;

    @Autowired
    private YangCurrencyUserMapper yangCurrencyUserMapper;

    @Autowired
    private YangCurrencyPairMapper yangCurrencyPairMapper;

    @Autowired
    private YangMemberTokenService yangMemberTokenService;

    @Autowired
    private YangCurrencyMapper yangCurrencyMapper;

    @Autowired
    private YangPairService yangPairService;

    @Autowired
    private YangInvitMapper yangInvitMapper;

    private YangConfig invitOnerebateConfig;

    private YangConfig invitMemberIdConfig;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private RedisService redisService;

    private YangConfig getYangConfig(String key) {
        YangConfig yangConfigQuery = new YangConfig();
        yangConfigQuery.setKey(key);
        YangConfig config = yangConfigMapper.selectOne(yangConfigQuery);
        if (null == config || StringUtils.isBlank(config.getValue())) {
            throw new RuntimeException(key + " 配置不存在");
        }
        return config;
    }

    /*处理当前订单*/
    @Transactional
    public void deal(YangOrders yangOrdersFind) throws RuntimeException {


        if (null == invitOnerebateConfig) {
            invitOnerebateConfig = getYangConfig("invit_onerebate");
        }
        if (null == invitMemberIdConfig) {
            invitMemberIdConfig = getYangConfig("invit_member_id");
        }


        //判断当前挂单类型



        /*该订单未找到*/
        if (null == yangOrdersFind) {
            throw new RuntimeException("此订单【" + yangOrdersFind.getOrdersId() + "】系统内未找到");
        }

        /*该订单为取消状态*/
        if (yangOrdersFind.getStatus().byteValue() == OrderStatus.ORDER_TYPE_CANCELED.getIndex()) {
            System.out.println("此订单【" + yangOrdersFind.getOrdersId() + "】为取消状态");
            return;
        }

        /*该订单为已完成状态*/
        if (yangOrdersFind.getStatus().byteValue() == OrderStatus.ORDER_TYPE_FINISHED.getIndex()) {
            System.out.println("此订单【" + yangOrdersFind.getOrdersId() + "】为已完成状态");
            return;
        }

        // TODO: 2018/6/12 加上机器人的判断 是否继续正常处理 还是紧急撤单


//
//        if(yangOrdersFind.getType().equals(buyType)){ //当前为买单
//            dealSellOrder(yangOrdersFind);
//        }else if(yangOrdersFind.getType().equals(sellType)){  //当前为卖单
//            dealBuyOrder(yangOrdersFind);
//        }


        doOrder(yangOrdersFind);

    }


    /**
     * @param yangOrders
     * @throws RuntimeException
     */
    @Transactional
    public void doOrder(YangOrders yangOrders) throws RuntimeException {

//        tradeKeys = CoinConst.REDIS_COIN_TRADE_SALE_+cy.getCurrencyId()+"_"+cy.getCurrencyTradeId();
        String keys = "";
        String key_from;
        Set<ZSetOperations.TypedTuple<Object>> set;
        if (yangOrders.getType().equals(sellType)) //如果是卖单 则需要寻找价格复核条件的 从高到底的订单
        {
            keys = CoinConst.REDIS_COIN_TRADE_BUY_ + yangOrders.getCurrencyId() + "_" + yangOrders.getCurrencyTradeId();

            key_from = CoinConst.REDIS_COIN_TRADE_SALE_ + yangOrders.getCurrencyId() + "_" + yangOrders.getCurrencyTradeId(); //来源单在redis中的数据

            set = redisService.reverseRangeWithScores(keys, yangOrders.getPrice().longValue(), Long.MAX_VALUE);

        } else { //如果是买单 则寻找价格从低到高的卖单

            keys = CoinConst.REDIS_COIN_TRADE_SALE_ + yangOrders.getCurrencyId() + "_" + yangOrders.getCurrencyTradeId();

            key_from = CoinConst.REDIS_COIN_TRADE_BUY_ + yangOrders.getCurrencyId() + "_" + yangOrders.getCurrencyTradeId(); //来源单在redis中的数据


            set = redisService.rangeByScoreWithScores(keys, 0, yangOrders.getPrice().longValue());

        }

        Iterator iterator = set.iterator();

//        YangOrders yangOrders1;

        List<YangOrders> ordersList = new LinkedList();

        while (iterator.hasNext()) {
            ZSetOperations.TypedTuple<Object> next = (ZSetOperations.TypedTuple) iterator.next();

            YangOrders yangOrders1 = JSONObject.parseObject(next.getValue().toString(), YangOrders.class);

            if (yangOrders1.getOrdersId() <= yangOrders.getOrdersId()) {
                //把数据添加到redis中
                ordersList.add(yangOrders1);

            } else {
                //大于自己的订单 就不要处理了
                continue;

            }


        }


        if (ordersList.size() == 0)  //如果没有数据  则不处理
        {
            return;
        }


        /*************订单排序****************/
        if (yangOrders.getType().equals(sellType)) //如果是卖单 获取的是买单 价格必须按照从高到底 并且按照时间正序
        {
            Collections.sort(ordersList, new Comparator<YangOrders>() {
                @Override
                public int compare(YangOrders o1, YangOrders o2) {

                    if (o1.getPrice().equals(o2.getPrice())) {
                        return o1.getAddTime() - o2.getAddTime();
                    } else {
                        return o2.getPrice().compareTo(o1.getPrice());
                    }

                }
            });
        } else { //如果是卖单 获取的是买单 价格必须按照从底到高 并且按照时间正序
            Collections.sort(ordersList, new Comparator<YangOrders>() {
                @Override
                public int compare(YangOrders o1, YangOrders o2) {
                    if (o1.getPrice().equals(o2.getPrice())) {
                        return o1.getAddTime() - o2.getAddTime();
                    } else {
                        return o1.getPrice().compareTo(o2.getPrice());
                    }
                }
            });
        }
        /*************订单排序****************/

        //价格排序好了 现在开始循环处理

        BigDecimal nums_need = yangOrders.getNum(); //需要购买的数量


        long tradeTime = System.currentTimeMillis() / 1000;
        boolean isOk = false; //是否已经完成

        List<YangOrders> needUpdateList = new LinkedList<>(); //需要更新的订单数据 通过replace into 方式 批量更新
        List<YangTrade> yangTradesList = new LinkedList<>(); //需要加入的交易数据
        List<YangTradeFee> yangTradeFeeList = new LinkedList<>(); //需要插入的交易手续费数据

        //获取交易对基本信息
        YangCurrencyPair yangCurrencyPairFrom = yangPairService.getPairInfo(yangOrders.getCurrencyId(), yangOrders.getCurrencyTradeId());


        //获取用户手续费的默认配置
        YangTradeFee yangTradeFee = new YangTradeFee();
        yangTradeFee.setCyId(yangCurrencyPairFrom.getCyId());
        yangTradeFee.setMemberId(yangOrders.getMemberId());
        YangTradeFee yangTradeFeeFrom = this.getTradeFee(yangTradeFee);

        BigDecimal finishNumsTimes = BigDecimal.ZERO; //这次循环处理的数量


        //开启redis 事务


        for (YangOrders y : ordersList
                ) {

//            redisService.multi();

            BigDecimal numLeft = y.getNum().subtract(y.getTradeNum());

            //首先 把原来的数据 从队列中删掉 暂时没用知道更新的方法  先删除 后 添加
            redisService.zremove(keys, JSONObject.toJSON(y));

            if (!this.cleanBefore(key_from, yangOrders.getPrice().longValue(), yangOrders.getOrdersId())) {
                System.out.println("系统错误，没有删掉脏数据");
                break;
            }
//            redisService.zremove(key_from,JSONObject.toJSON(yangOrders)); //这里删掉的来源订单的数据

            if (nums_need.compareTo(BigDecimal.ZERO) == 0) //如果需要处理的数量都处理了 则结束本次循环
            {
                break;
            }


            if (numLeft.compareTo(nums_need) >= 0)  //如果这个订单的数量满足并需求订单 则直接完成
            {
                if (numLeft.compareTo(nums_need) == 0) {
                    y.setStatus(OrderStatus.ORDER_TYPE_FINISHED.getIndex());
                } else {
                    y.setStatus(OrderStatus.ORDER_TYPE_PART_FINISHED.getIndex());
                }

                yangOrders.setStatus(OrderStatus.ORDER_TYPE_FINISHED.getIndex()); //来源订单数量就够啦 标记为已经完成的状态
                yangOrders.setTradeNum(yangOrders.getTradeNum().add(nums_need));


                y.setTradeNum(y.getTradeNum().add(nums_need));

                if (y.getStatus().byteValue() == OrderStatus.ORDER_TYPE_PART_FINISHED.getIndex()) //如果只是部分成交 则需要把数据继续写到redis中 以供后面使用
                {
                    redisService.zAdd(keys, JSONObject.toJSON(y), y.getPrice().doubleValue());

                }
                isOk = true;
                finishNumsTimes = nums_need;

            } else { //否则就继续循环 到下一个待处理订单处理
                finishNumsTimes = numLeft;
                nums_need = nums_need.subtract(numLeft);  //更新需要购买的数量
                y.setStatus(Byte.valueOf("2"));
                y.setTradeNum(y.getTradeNum().add(numLeft));  // 等于全部卖出去了

                yangOrders.setStatus(OrderStatus.ORDER_TYPE_PART_FINISHED.getIndex()); //来源订单数量就够啦 标记为已经完成的状态
                yangOrders.setTradeNum(yangOrders.getTradeNum().add(numLeft));

                redisService.zAdd(key_from, JSONObject.toJSON(yangOrders), yangOrders.getPrice().doubleValue()); //这里删掉的来源订单的数据


            }

            y.setTradeTime(tradeTime);
            yangOrders.setTradeTime(System.currentTimeMillis() / 1000);

            long start_order = System.currentTimeMillis();


            //把要更新的订单加了list 用来批量更新
//            needUpdateList.add(y);
            yangOrdersMapper.updateByPrimaryKey(y); //更新订单数据
            yangOrdersMapper.updateByPrimaryKey(yangOrders); //更新来源订单数据


            System.out.println("更新订单用时 : " + (System.currentTimeMillis() - start_order));


            //开始计算手续费
            long start_FEE = System.currentTimeMillis();

            BigDecimal freeBuy; //订单发起者
            BigDecimal freeSell; //订单应答者

            YangTradeFee yangTradeFee1 = new YangTradeFee();
            yangTradeFee1.setCyId(yangCurrencyPairFrom.getCyId());
            yangTradeFee1.setMemberId(y.getMemberId());
            YangTradeFee yangTradeFeeTo = this.getTradeFee(yangTradeFee1);

            if (yangOrders.getType().equals(buyType)) // 如果是买单
            {
                if (null != yangTradeFeeFrom && null != yangTradeFeeFrom.getCurrencyBuyFee()) {
                    freeBuy = finishNumsTimes.multiply(BigDecimal.valueOf(yangTradeFeeFrom.getCurrencyBuyFee())).divide(BigDecimal.valueOf(100));
                } else {
                    freeBuy = finishNumsTimes.multiply(yangCurrencyPairFrom.getCurrencyBuyFee()).divide(BigDecimal.valueOf(100));
                }

                if (null != yangTradeFeeTo && null != yangTradeFeeTo.getCurrencySellFee()) {
                    freeSell = finishNumsTimes.multiply(BigDecimal.valueOf(yangTradeFeeTo.getCurrencySellFee()).multiply(y.getPrice()).divide(BigDecimal.valueOf(100)));
                } else {
                    freeSell = finishNumsTimes.multiply(yangCurrencyPairFrom.getCurrencySellFee()).multiply(y.getPrice()).divide(BigDecimal.valueOf(100));
                }

            } else {

                if (null != yangTradeFeeFrom && null != yangTradeFeeFrom.getCurrencySellFee()) {
                    freeSell = finishNumsTimes.multiply(BigDecimal.valueOf(yangTradeFeeFrom.getCurrencySellFee())).multiply(y.getPrice()).divide(BigDecimal.valueOf(100));
                } else {
                    freeSell = finishNumsTimes.multiply(yangCurrencyPairFrom.getCurrencySellFee()).multiply(y.getPrice()).divide(BigDecimal.valueOf(100));
                }

                if (null != yangTradeFeeTo && null != yangTradeFeeTo.getCurrencyBuyFee()) {
                    freeBuy = finishNumsTimes.multiply(BigDecimal.valueOf(yangTradeFeeTo.getCurrencyBuyFee()).divide(BigDecimal.valueOf(100)));
                } else {
                    freeBuy = finishNumsTimes.multiply(yangCurrencyPairFrom.getCurrencyBuyFee()).divide(BigDecimal.valueOf(100));
                }

            }


            long tradeInfotime = System.currentTimeMillis();


            if (yangOrders.getType().equals(buyType)) {
                updateTradeInfo(yangOrders, y, finishNumsTimes, freeBuy, freeSell, y.getType());

            } else {
                updateTradeInfo(y, yangOrders, finishNumsTimes, freeBuy, freeSell, y.getType());

            }


            System.out.println("更新资产用时 : " + (System.currentTimeMillis() - tradeInfotime));


            System.out.println("更新手续费计算信息用时 :" + (System.currentTimeMillis() - start_FEE));


            //更新本次循环，买单卖单的用户资产
            int i = 0;
            if (yangOrders.getType().equals(buyType)) {
                i = updateUserMoney(

                        yangOrders.getMemberId(), yangOrders.getCurrencyId(), finishNumsTimes.subtract(freeBuy), "inc", "normal",

                        y.getMemberId(), yangOrders.getCurrencyId(), finishNumsTimes, "dec", "forzen",

                        yangOrders.getMemberId(), yangOrders.getCurrencyTradeId(), finishNumsTimes.multiply(yangOrders.getPrice()), "dec", "forzen",

                        y.getMemberId(), yangOrders.getCurrencyTradeId(), finishNumsTimes.multiply(y.getPrice()).subtract(freeSell), "inc", "normal",

                        yangOrders.getMemberId(), yangOrders.getCurrencyTradeId(), finishNumsTimes.multiply(yangOrders.getPrice().subtract(y.getPrice())), "inc", "normal" //当卖价低于收购价的时候 要给买家把多余的钱加回去


                );
            } else {
                i = updateUserMoney(

                        y.getMemberId(), y.getCurrencyId(), finishNumsTimes.subtract(freeBuy), "inc", "normal",

                        yangOrders.getMemberId(), yangOrders.getCurrencyId(), finishNumsTimes, "dec", "forzen",

                        y.getMemberId(), y.getCurrencyTradeId(), finishNumsTimes.multiply(y.getPrice()), "dec", "forzen",

                        yangOrders.getMemberId(), yangOrders.getCurrencyTradeId(), finishNumsTimes.multiply(yangOrders.getPrice()).subtract(freeSell), "inc", "normal",

                        0, Integer.valueOf(0), null, null, null

                );
            }

            if (i == 1) {
                throw new RuntimeException();
            }


            //记录交易记录，以备加入list批量新增 //后期实现 主要是怕数据的不一致性


            //循环处理用户的资金情况 这里耗时是瓶颈 除非全部金额变动都记录内存缓存 对数据一致性的要求就比较高了


            if (isOk) {
                break;
            }


        }


//        System.out.println(ordersList);
        //查询满足条件的订单 先按照订单的下单顺序进行排序

//        redisService.exec();


    }


    public boolean cleanBefore(String key, long score, Integer orderids) {
        boolean isOk = false;

        long min = score;

        long max = score + 1;


        Set<ZSetOperations.TypedTuple<Object>> set = redisService.rangeByScoreWithScores(key, score, max);

        Iterator iterator = set.iterator();


        while (iterator.hasNext() && !isOk) {
            ZSetOperations.TypedTuple<Object> next = (ZSetOperations.TypedTuple) iterator.next();

            YangOrders yangOrders1 = JSONObject.parseObject(next.getValue().toString(), YangOrders.class);

            if (yangOrders1.getOrdersId().equals(orderids)) {
                //删除
                redisService.zremove(key, JSONObject.toJSON(yangOrders1)); //这里删掉的来源订单的数据
                isOk = true;

            }

        }

        return isOk;
    }

    /*处理买单*/
    public void dealBuyOrder(YangOrders yangOrdersSell) throws RuntimeException {
        /*查询所有符合条件的买单 (同类 价格)*/
        Example example = new Example(YangOrders.class);
        List<String> statusList = Lists.newArrayList();
        statusList.add("0");
        statusList.add("1");
        example.createCriteria().andEqualTo("currencyId", yangOrdersSell.getCurrencyId())
                .andEqualTo("currencyTradeId", yangOrdersSell.getCurrencyTradeId())
                .andEqualTo("type", buyType)
                .andIn("status", statusList)
                .andLessThan("ordersId", yangOrdersSell.getOrdersId())
                .andGreaterThanOrEqualTo("price", yangOrdersSell.getPrice());
        example.setOrderByClause("price desc,add_time asc");
        List<YangOrders> yangOrdersList = selectByExample(example); //查询到所有的符合条件的买单(1.单价大于卖单单价 2.找到最贵的买单单价 3.按时间发布排序)
        if (yangOrdersList.size() > 0) {
            //查询当前交易的买入卖出手续费默认配置
            YangCurrencyPair yangCurrencyPairFind = getYangCurrencyPair(yangOrdersSell);

            /*当前卖单的更新值*/
            YangOrders sellerOrderUpdate = new YangOrders();
            sellerOrderUpdate.setOrdersId(yangOrdersSell.getOrdersId());
            sellerOrderUpdate.setTradeNum(yangOrdersSell.getTradeNum());

            /*当前卖单的自定义手续费配置*/
            YangTradeFee yangTradeFeeQuery = new YangTradeFee();
            yangTradeFeeQuery.setMemberId(yangOrdersSell.getMemberId());
            yangTradeFeeQuery.setCyId(yangCurrencyPairFind.getCyId());
            YangTradeFee sellerTradeFeeConf = getTradeFee(yangTradeFeeQuery);

            Boolean isNeedLoop = true;

            //当前卖单剩余交易数量
            BigDecimal sellLeftNum = yangOrdersSell.getNum().subtract(yangOrdersSell.getTradeNum());
            for (YangOrders yangOrdersBuy : yangOrdersList) { //循环对买单进行卖出操作
                /*本次循环的交易数量*/
                BigDecimal loopTradeNum = new BigDecimal(0);
                /*当前买单的更新值*/
                YangOrders buyerOrderUpdate = new YangOrders();
                buyerOrderUpdate.setOrdersId(yangOrdersBuy.getOrdersId());

                /*当前买单的自定义手续费配置*/
                YangTradeFee yangTradeFeeBuyQuery = new YangTradeFee();
                yangTradeFeeBuyQuery.setMemberId(yangOrdersBuy.getMemberId());
                yangTradeFeeBuyQuery.setCyId(yangCurrencyPairFind.getCyId());
                YangTradeFee buyerTradeFeeConf = getTradeFee(yangTradeFeeBuyQuery);

                /*当前买单剩余交易数量*/
                BigDecimal buyerLeftNum = yangOrdersBuy.getNum().subtract(yangOrdersBuy.getTradeNum());


                if (sellLeftNum.compareTo(buyerLeftNum) == 0) { //卖单的剩余需求交易量 == 当前买单的剩余交易量
                    buyerOrderUpdate.setStatus(OrderStatus.ORDER_TYPE_FINISHED.getIndex());
                    sellerOrderUpdate.setStatus(OrderStatus.ORDER_TYPE_FINISHED.getIndex());
                    loopTradeNum = buyerLeftNum;
                    isNeedLoop = false;
                } else if (sellLeftNum.compareTo(buyerLeftNum) == -1) { //卖单的剩余需求交易量 < 当前买单的剩余交易量
                    //卖单为已完成，买单继续走流程
                    buyerOrderUpdate.setStatus(OrderStatus.ORDER_TYPE_PART_FINISHED.getIndex());
                    sellerOrderUpdate.setStatus(OrderStatus.ORDER_TYPE_FINISHED.getIndex());
                    loopTradeNum = sellLeftNum;
                    isNeedLoop = false;
                } else if (sellLeftNum.compareTo(buyerLeftNum) == 1) { //卖单的剩余需求交易量 > 当前买单的剩余交易量
                    //买单流程结束，卖单继续走流程
                    buyerOrderUpdate.setStatus(OrderStatus.ORDER_TYPE_FINISHED.getIndex());
                    sellerOrderUpdate.setStatus(OrderStatus.ORDER_TYPE_PART_FINISHED.getIndex());
                    loopTradeNum = buyerLeftNum;
                }


                //计算本次买单的手续费(币数量)
                BigDecimal buyerTradeFeeNum = new BigDecimal(0);

                if (yangOrdersBuy.getIsShua().intValue() == 1) {
                    buyerTradeFeeNum = BigDecimal.ZERO;
                } else {
                    if (null != buyerTradeFeeConf && null != buyerTradeFeeConf.getCurrencyBuyFee()) {
                        buyerTradeFeeNum = loopTradeNum.multiply(new BigDecimal(buyerTradeFeeConf.getCurrencyBuyFee())).divide(new BigDecimal(100));
                    } else {
                        //按币种的费率走
                        buyerTradeFeeNum = loopTradeNum.multiply(yangCurrencyPairFind.getCurrencyBuyFee()).divide(new BigDecimal(100));
                    }
                }


                BigDecimal sellerTradeFeePrice = new BigDecimal(0);
                if (yangOrdersSell.getIsShua().intValue() == 1) {
                    sellerTradeFeePrice = BigDecimal.ZERO;
                } else {
                    //计算本次卖单的手续费(price)
                    if (null != sellerTradeFeeConf && null != sellerTradeFeeConf.getCurrencySellFee()) {
                        sellerTradeFeePrice = loopTradeNum.multiply(yangOrdersSell.getPrice()).multiply(new BigDecimal(sellerTradeFeeConf.getCurrencySellFee())).divide(new BigDecimal(100));
                    } else {
                        //按币种的费率走
                        sellerTradeFeePrice = loopTradeNum.multiply(yangOrdersSell.getPrice()).multiply(yangCurrencyPairFind.getCurrencySellFee()).divide(new BigDecimal(100));
                    }
                }


                //更新本次的循环的订单状态、已交易数量 orders
                buyerOrderUpdate.setTradeTime(System.currentTimeMillis() / 1000); //买单更新交易时间
                buyerOrderUpdate.setTradeNum(yangOrdersBuy.getTradeNum().add(loopTradeNum)); //买单更新本次交易币数量(增加已交易数量)
                yangOrdersMapper.updateByPrimaryKeySelective(buyerOrderUpdate);
                sellerOrderUpdate.setTradeTime(System.currentTimeMillis() / 1000); //卖单更新交易时间
                sellerOrderUpdate.setTradeNum(sellerOrderUpdate.getTradeNum().add(loopTradeNum)); //卖单更新本次交易币数量(增加已交易数量)
                yangOrdersMapper.updateByPrimaryKeySelective(sellerOrderUpdate);


                //新增本次循环交易记录 trade
                updateTradeInfo(yangOrdersBuy, yangOrdersSell, loopTradeNum, buyerTradeFeeNum, sellerTradeFeePrice, "buy");

                //更新本次循环返利交易记录
                rebate(yangOrdersBuy, buyerTradeFeeNum);
                rebate(yangOrdersSell, sellerTradeFeePrice);

                //更新本次循环卖单剩余需求量
                sellLeftNum = sellLeftNum.subtract(loopTradeNum);


                //更新本次循环，买单卖单的用户资产
//                updateUserMoney(yangOrdersBuy.getMemberId(),yangOrdersBuy.getCurrencyId(),loopTradeNum.subtract(buyerTradeFeeNum),"inc","normal" ); //买单加币(扣除交易费用)
//                updateUserMoney(yangOrdersSell.getMemberId(),yangOrdersBuy.getCurrencyId(),loopTradeNum,"dec","forzen" ); //卖单扣币 (扣除冻结金额)

                int i = updateUserMoney(
                        yangOrdersBuy.getMemberId(), yangOrdersBuy.getCurrencyId(), loopTradeNum.subtract(buyerTradeFeeNum), "inc", "normal",
                        yangOrdersSell.getMemberId(), yangOrdersBuy.getCurrencyId(), loopTradeNum, "dec", "forzen",
                        yangOrdersBuy.getMemberId(), yangOrdersBuy.getCurrencyTradeId(), loopTradeNum.multiply(yangOrdersSell.getPrice()), "dec", "forzen",
                        yangOrdersSell.getMemberId(), yangOrdersBuy.getCurrencyTradeId(), loopTradeNum.multiply(yangOrdersSell.getPrice()).subtract(sellerTradeFeePrice), "inc", "normal",
                        0, Integer.valueOf(0), null, null, null

                );

                if (i == 1) {
                    throw new RuntimeException();
                }


//                updateUserMoney(yangOrdersBuy.getMemberId(),yangOrdersBuy.getCurrencyTradeId(),loopTradeNum.multiply(yangOrdersSell.getPrice()),"dec","forzen" );
//                updateUserMoney(yangOrdersSell.getMemberId(),yangOrdersBuy.getCurrencyTradeId(),loopTradeNum.multiply(yangOrdersSell.getPrice()).subtract(sellerTradeFeePrice),"inc","normal" ); //卖家加钱(扣除手续费)


//                updateUserMoney(
//                        yangOrdersBuy.getMemberId(),yangOrdersBuy.getCurrencyTradeId(),loopTradeNum.multiply(yangOrdersSell.getPrice()),"dec","forzen",
//                        yangOrdersSell.getMemberId(),yangOrdersBuy.getCurrencyTradeId(),loopTradeNum.multiply(yangOrdersSell.getPrice()).subtract(sellerTradeFeePrice),"inc","normal"
//                );


                if (isNeedLoop == false) { //买单流程走结束了
                    break;
                }

            }

        }
    }

    /*处理卖单*/
    @Transactional
    public void dealSellOrder(YangOrders yangOrdersBuy) throws RuntimeException {

        /*查询所有符合条件的卖单*/
        Example example = new Example(YangOrders.class);
        List<String> statusList = Lists.newArrayList();
        statusList.add("0");
        statusList.add("1");
        example.createCriteria()
                .andEqualTo("currencyId", yangOrdersBuy.getCurrencyId())
                .andEqualTo("currencyTradeId", yangOrdersBuy.getCurrencyTradeId())
                .andEqualTo("type", sellType)
                .andLessThan("ordersId", yangOrdersBuy.getOrdersId())
                .andIn("status", statusList)
                .andLessThanOrEqualTo("price", yangOrdersBuy.getPrice());
        example.setOrderByClause("price asc,add_time asc");
        List<YangOrders> yangOrdersList = selectByExample(example); //查询到所有的符合条件的卖单(1.单价小于买单单价 2.找到最便宜的卖单单价 3.按时间发布排序)
        if (yangOrdersList.size() > 0) {

            //查询当前交易的买入卖出手续费默认配置

            YangCurrencyPair yangCurrencyPairFind = getYangCurrencyPair(yangOrdersBuy);


            //当前买单剩余交易数量
            BigDecimal buyNeedNum = yangOrdersBuy.getNum().subtract(yangOrdersBuy.getTradeNum());

            /*当前买单的更新值*/
            YangOrders buyerOrderUpdate = new YangOrders();
            buyerOrderUpdate.setOrdersId(yangOrdersBuy.getOrdersId());
            buyerOrderUpdate.setTradeNum(yangOrdersBuy.getTradeNum());

            /*当前买单的手续费配置*/
            YangTradeFee yangTradeFeeQuery = new YangTradeFee();
            yangTradeFeeQuery.setMemberId(yangOrdersBuy.getMemberId());
            yangTradeFeeQuery.setCyId(yangOrdersBuy.getCurrencyId());
            YangTradeFee buyerTradeFeeConf = getTradeFee(yangTradeFeeQuery);

            Boolean isNeedLoop = true;
            for (YangOrders yangOrdersSell : yangOrdersList) { //循环对卖单进行买入操作

                /*本次循环的交易数量*/
                BigDecimal loopTradeNum = new BigDecimal(0);

                /*当前卖单的更新值*/
                YangOrders sellerOrderUpdate = new YangOrders();
                sellerOrderUpdate.setOrdersId(yangOrdersSell.getOrdersId());

                /*当前卖单的手续费配置*/
                YangTradeFee yangTradeFeeSellQuery = new YangTradeFee();
                yangTradeFeeSellQuery.setMemberId(yangOrdersSell.getMemberId());
                yangTradeFeeSellQuery.setCyId(yangOrdersSell.getCurrencyId());
                YangTradeFee sellerTradeFeeConf = getTradeFee(yangTradeFeeSellQuery);

                BigDecimal sellLeftNum = yangOrdersSell.getNum().subtract(yangOrdersSell.getTradeNum());
                if (sellLeftNum.compareTo(buyNeedNum) == 0) { //当前卖单的剩余交易量 == 当前买单的需求交易量
                    //买单和卖单均为已完成
                    buyerOrderUpdate.setStatus(OrderStatus.ORDER_TYPE_FINISHED.getIndex());
                    sellerOrderUpdate.setStatus(OrderStatus.ORDER_TYPE_FINISHED.getIndex());
                    loopTradeNum = buyNeedNum;
                    isNeedLoop = false;
                } else if (sellLeftNum.compareTo(buyNeedNum) == -1) { //当前卖单的剩余交易量 < 当前买单的需求交易量
                    //卖单为已完成，买单继续走流程
                    buyerOrderUpdate.setStatus(OrderStatus.ORDER_TYPE_PART_FINISHED.getIndex());
                    sellerOrderUpdate.setStatus(OrderStatus.ORDER_TYPE_FINISHED.getIndex());
                    loopTradeNum = sellLeftNum;

                } else if (sellLeftNum.compareTo(buyNeedNum) == 1) { // 当前卖单的剩余交易量 > 当前买单的需求交易量
                    //买单为已完成，卖单扣除此次交易数量
                    buyerOrderUpdate.setStatus(OrderStatus.ORDER_TYPE_FINISHED.getIndex());
                    sellerOrderUpdate.setStatus(OrderStatus.ORDER_TYPE_PART_FINISHED.getIndex());
                    loopTradeNum = buyNeedNum;
                    isNeedLoop = false;
                }
                //计算本次买单的手续费(币数量)
                BigDecimal buyerTradeFeeNum = new BigDecimal(0);
                BigDecimal sellerTradeFeePrice = new BigDecimal(0);


                //机器人刷单  如果是系统平台的 isshua = 1 不收取手续费 如果通过api生成的订单 收取手续费 isshua=2
                if (yangOrdersBuy.getIsShua().intValue() == 1) {
                    buyerTradeFeeNum = BigDecimal.ZERO;

                } else {
                    if (null != buyerTradeFeeConf && null != buyerTradeFeeConf.getCurrencyBuyFee()) {
                        buyerTradeFeeNum = loopTradeNum.multiply(new BigDecimal(buyerTradeFeeConf.getCurrencyBuyFee())).divide(new BigDecimal(100));
                    } else {
                        //按币种的费率走
                        buyerTradeFeeNum = loopTradeNum.multiply(yangCurrencyPairFind.getCurrencyBuyFee()).divide(new BigDecimal(100));
                    }

                }

                //机器人刷单  如果是系统平台的 isshua = 1 不收取手续费 如果通过api生成的订单 收取手续费 isshua=2
                if (yangOrdersSell.getIsShua().intValue() == 1) {
                    sellerTradeFeePrice = BigDecimal.ZERO;
                } else {
                    //计算本次卖单的手续费(price)

                    if (null != sellerTradeFeeConf && null != sellerTradeFeeConf.getCurrencySellFee()) {
                        sellerTradeFeePrice = loopTradeNum.multiply(yangOrdersSell.getPrice()).multiply(new BigDecimal(sellerTradeFeeConf.getCurrencySellFee())).divide(new BigDecimal(100));
                    } else {
                        //按交易对的默认费率走
                        sellerTradeFeePrice = loopTradeNum.multiply(yangOrdersSell.getPrice()).multiply(yangCurrencyPairFind.getCurrencySellFee()).divide(new BigDecimal(100));
                    }
                }


                //更新本次的循环的订单状态、已交易数量 orders
                buyerOrderUpdate.setTradeTime(System.currentTimeMillis() / 1000); //买单更新交易时间
                buyerOrderUpdate.setTradeNum(buyerOrderUpdate.getTradeNum().add(loopTradeNum)); //买单更新本次交易币数量(增加已交易数量)
                yangOrdersMapper.updateByPrimaryKeySelective(buyerOrderUpdate);
                sellerOrderUpdate.setTradeTime(System.currentTimeMillis() / 1000); //卖单更新交易时间
                sellerOrderUpdate.setTradeNum(yangOrdersSell.getTradeNum().add(loopTradeNum)); //卖单更新本次交易币数量(增加已交易数量)
                yangOrdersMapper.updateByPrimaryKeySelective(sellerOrderUpdate);


                updateTradeInfo(yangOrdersBuy, yangOrdersSell, loopTradeNum, buyerTradeFeeNum, sellerTradeFeePrice, "sell");

                //更新本次循环返利交易记录
                rebate(yangOrdersBuy, buyerTradeFeeNum);
                rebate(yangOrdersSell, sellerTradeFeePrice);


                //更新本次循环，买单卖单的用户资产
                int i = updateUserMoney(
                        yangOrdersBuy.getMemberId(), yangOrdersBuy.getCurrencyId(), loopTradeNum.subtract(buyerTradeFeeNum), "inc", "normal",
                        yangOrdersSell.getMemberId(), yangOrdersBuy.getCurrencyId(), loopTradeNum, "dec", "forzen",
                        yangOrdersBuy.getMemberId(), yangOrdersBuy.getCurrencyTradeId(), loopTradeNum.multiply(yangOrdersSell.getPrice()), "dec", "forzen",
                        yangOrdersSell.getMemberId(), yangOrdersBuy.getCurrencyTradeId(), loopTradeNum.multiply(yangOrdersSell.getPrice()).subtract(sellerTradeFeePrice), "inc", "normal",

                        yangOrdersBuy.getMemberId(), yangOrdersBuy.getCurrencyTradeId(), loopTradeNum.multiply(yangOrdersBuy.getPrice().subtract(yangOrdersSell.getPrice())), "inc", "normal" //当卖价低于收购价的时候 要给买家把多余的钱加回去


                );
                if (i == 1) {
                    throw new RuntimeException();
                }

                //买单加币(扣除交易费用)
//                updateUserMoney(yangOrdersSell.getMemberId(),yangOrdersBuy.getCurrencyId(),loopTradeNum,"dec","forzen" ); //卖单扣币 (扣除冻结金额)


//                updateUserMoney(yangOrdersBuy.getMemberId(),yangOrdersBuy.getCurrencyTradeId(),loopTradeNum.multiply(yangOrdersSell.getPrice()),"dec","forzen" ); //买家扣钱(扣除冻结金额)
//                updateUserMoney(yangOrdersSell.getMemberId(),yangOrdersBuy.getCurrencyTradeId(),loopTradeNum.multiply(yangOrdersSell.getPrice()).subtract(sellerTradeFeePrice),"inc","normal" ); //卖家加钱(扣除手续费)


                //更新本次循环买单剩余需求量
                buyNeedNum = buyNeedNum.subtract(loopTradeNum);

                if (isNeedLoop == false) { //买单流程走结束了
                    break;
                }
            }
        }

    }


    /**
     * @param currency_id
     * @param currency_trade_id
     * @return
     */
    private CoinPairRedisBean initCoinInfos(int currency_id, int currency_trade_id, String key) {
        CoinPairRedisBean coinPairRedisBean = new CoinPairRedisBean();

        try {

            if (redisService.hmGet(key, "cy_id") == null) {
                coinPairRedisBean.setCy_id(0);
                return coinPairRedisBean;
            }

            coinPairRedisBean.setCy_id(Integer.valueOf(redisService.hmGet(key, "cy_id").toString()).intValue());

            coinPairRedisBean.setCurrency_id(currency_id);

            coinPairRedisBean.setCurrency_trade_id(currency_trade_id);

            coinPairRedisBean.setCurrency_logo((String) redisService.hmGet(key, "currency_logo"));

            coinPairRedisBean.setTrade_logo((String) redisService.hmGet(key, "trade_logo"));

            coinPairRedisBean.setInput_price_num(CommonTools.formatNull2BigDecimal(redisService.hmGet(key, "input_price_num")));

            coinPairRedisBean.setRate_num(CommonTools.formatNull2int(redisService.hmGet(key, "rate_num")));

            coinPairRedisBean.setCurrency_mark((String) redisService.hmGet(key, "currency_mark"));

            coinPairRedisBean.setTrade_mark((String) redisService.hmGet(key, "trade_mark"));

            coinPairRedisBean.setIs_chuang((String) redisService.hmGet(key, "is_chuang"));

            coinPairRedisBean.setIs_old((String) redisService.hmGet(key, "is_old"));

            coinPairRedisBean.setNew_price(CommonTools.formatNull2BigDecimal(redisService.hmGet(key, "new_price")));


            coinPairRedisBean.setPrice_status(CommonTools.formatNull2int(redisService.hmGet(key, "price_status")));


            //                    'input_price_num'=>$cy->input_price_num,//价格显示位数
//                    'rate_num'=>$cy->rate_num,//汇率转换后的位数
//                    'is_chuang'=>$cy->is_chuang,

            coinPairRedisBean.setInput_price_num(CommonTools.formatNull2BigDecimal(redisService.hmGet(key, "input_price_num")));


            coinPairRedisBean.setRate_num(CommonTools.formatNull2int(redisService.hmGet(key, "rate_num")));


            coinPairRedisBean.setIs_chuang((String) redisService.hmGet(key, "is_chuang"));

            coinPairRedisBean.setIs_old((String) redisService.hmGet(key, "is_old"));


            if (currency_trade_id != 0)  //实时获取这个币种的krw价格
            {
                BigDecimal krwprice = BigDecimal.ZERO;
                try {
                    Object krwPriceObject = redisService.hmGet(CoinConst.REDIS_COIN_KRW_PRICE_, String.valueOf(currency_trade_id));
                    if (krwPriceObject != null) {
                        krwprice = BigDecimal.valueOf(Double.valueOf(krwPriceObject.toString()));
                    }
                    coinPairRedisBean.setKrw_price(krwprice);
                } catch (Exception e) {
                    coinPairRedisBean.setKrw_price(krwprice);

                    System.out.println("找不到该币种 krw" + currency_trade_id);
                }
            } else {
                coinPairRedisBean.setKrw_price(BigDecimal.valueOf(1));
            }


            //获取24小时的数据
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
            String currentDayKey = dateFormat.format(new Date());
            String key_24h = CoinConst.REDIS_YANG_PARI_DAY_INFO_ + currency_id + "-" + currency_trade_id + "_" + currentDayKey;
            String key_24h_base = CoinConst.REDIS_YANG_PARI_DAY_INFO_ + currency_id + "-" + currency_trade_id + "_*";

            if (!redisService.keysExist(key_24h)) {
                Set set = redisService.keys(key_24h_base);
                Iterator<String> it = set.iterator();
                while (it.hasNext()) {
                    key_24h = it.next();
                    break;
                }
            }


//            System.out.println(set);
//            h24KeyList.add("h24_change");
//            h24KeyList.add("h24_num");
//            h24KeyList.add("h24_money");
//            h24KeyList.add("h24_max");
//            h24KeyList.add("h24_min");
//            h24KeyList.add("new_price");
//            h24KeyList.add("price_status");
//            h24KeyList.add("first_price"); //当日第一笔价格
            try {
                if (redisService.hmGet(key_24h, "h24_change") == null) {
                    coinPairRedisBean.setH24_change(Double.valueOf(0.00));
                } else {
                    java.text.DecimalFormat df = new java.text.DecimalFormat("#.00");
                    String change_24s = df.format(BigDecimal.valueOf(Double.valueOf(redisService.hmGet(key_24h, "h24_change").toString())).multiply(BigDecimal.valueOf(100)));
                    coinPairRedisBean.setH24_change(Double.valueOf(change_24s));
                }
            } catch (Exception e) {
                coinPairRedisBean.setH24_change(Double.valueOf(0.00));
                e.printStackTrace();
            }


            if (redisService.hmGet(key_24h, "h24_num") == null) {
                coinPairRedisBean.setH24_num("0");
            } else {
                coinPairRedisBean.setH24_num((String.valueOf(redisService.hmGet(key_24h, "h24_num"))));

            }

//            coinPairRedisBean.setH24_num((String.valueOf(redisService.hmGet(key_24h,"h24_num"))));


            if (redisService.hmGet(key_24h, "h24_money") == null) {
                coinPairRedisBean.setH24_money("0");
            } else {
                coinPairRedisBean.setH24_money((String.valueOf(redisService.hmGet(key_24h, "h24_money"))));

            }

            if (redisService.hmGet(key_24h, "h24_max") == null) {
                coinPairRedisBean.setH24_max("0");
            } else {
                coinPairRedisBean.setH24_max((String.valueOf(redisService.hmGet(key_24h, "h24_max"))));

            }

            if (redisService.hmGet(key_24h, "h24_min") == null) {
                coinPairRedisBean.setH24_min("0");
            } else {
                coinPairRedisBean.setH24_min((String.valueOf(redisService.hmGet(key_24h, "h24_min"))));

            }


            if (redisService.hmGet(key_24h, "price_status") == null) {
                coinPairRedisBean.setPrice_status(0);
            } else {
                coinPairRedisBean.setPrice_status(Integer.valueOf(String.valueOf(redisService.hmGet(key_24h, "price_status"))));

            }

            if (redisService.hmGet(key_24h, "new_price") == null) {
                coinPairRedisBean.setNew_price(BigDecimal.valueOf(0));
            } else {
                coinPairRedisBean.setNew_price(new BigDecimal(String.valueOf(redisService.hmGet(key_24h, "new_price"))));
            }


        } catch (Exception e) {
            e.printStackTrace();
        }


        return coinPairRedisBean;

    }


    /**
     * 更新交易对信息
     *
     * @return
     */
    private boolean flushCoinInfos(int currency_id, int currency_trade_id, YangTrade yangTrade) {
        String key = CoinConst.REDIS_COIN_PAIR_BASE_ + currency_id + "-" + currency_trade_id;
        CoinPairRedisBean coinPairRedisBean = this.initCoinInfos(currency_id, currency_trade_id, key);
        if (coinPairRedisBean.getCy_id() == 0) //如果找不到这个对象 则要设置log等基本信息
        {
            YangCurrencyPair yangCurrencyPair = new YangCurrencyPair();
            yangCurrencyPair.setCurrencyId(currency_id);
            yangCurrencyPair.setCurrencyTradeId(currency_trade_id);
            yangCurrencyPair = yangCurrencyPairMapper.selectOne(yangCurrencyPair);
            YangCurrency yangCurrencyCoin = null;
            YangCurrency yangCurrencyCoinTrade = null;
            if (yangCurrencyPair.getCyId() == null) {
                return false;
            } else {
                //查询币种信息
                //查询对应的交易币种信息
                Example example = new Example(YangCurrency.class);
                example.createCriteria();

                if (yangCurrencyPair.getCyId().equals(0)) {
                    yangCurrencyCoinTrade.setCurrencyLogo("http://static.bimin.vip/app/1.0/krw_logo.png");

                    yangCurrencyCoinTrade.setCurrencyMark("KRW");

                    List currencyList = new LinkedList();
                    currencyList.add(currency_id);
                    currencyList.add(currency_trade_id);
                    example.createCriteria().andIn("currencyId", currencyList);
                } else {
                    example.createCriteria().andEqualTo("currencyId", currency_id);

                }

                List<YangCurrency> list = yangCurrencyMapper.selectByExample(example);
                for (YangCurrency yangCurrency : list) {
                    if (yangCurrency.getCurrencyId().equals(yangCurrencyPair.getCurrencyId())) {
                        yangCurrencyCoin = yangCurrency;
                    }
                    if (yangCurrency.getCurrencyId().equals(yangCurrencyPair.getCurrencyTradeId())) {
                        yangCurrencyCoinTrade = yangCurrency;
                    }
                }

                if (yangCurrencyCoin != null) {
                    coinPairRedisBean.setCurrency_logo(yangCurrencyCoin.getCurrencyLogo());
                    coinPairRedisBean.setCurrency_mark(yangCurrencyCoin.getCurrencyMark());
                }

                if (yangCurrencyCoinTrade != null) {
                    coinPairRedisBean.setTrade_logo(yangCurrencyCoinTrade.getCurrencyLogo());
                    coinPairRedisBean.setTrade_mark(yangCurrencyCoinTrade.getCurrencyMark());
                }


                coinPairRedisBean.setCy_id(yangCurrencyPair.getCyId());


            }
        }

//        todo
        if (currency_trade_id == 0) //设置交易对应的交易krw价格  初一165是为了换算成rmb 后期这个走数据库
        {

            redisService.hmSet(CoinConst.REDIS_COIN_KRW_PRICE_, String.valueOf(currency_id), yangTrade.getPrice());

        }

        if (coinPairRedisBean.getNew_price() != null) {
            switch (yangTrade.getPrice().compareTo(coinPairRedisBean.getNew_price())) {
                case -1:
                    coinPairRedisBean.setPrice_status(-1);
                    break;
                case 0:
                    coinPairRedisBean.setPrice_status(0);
                    break;
                case 1:
                    coinPairRedisBean.setPrice_status(1);
                    break;
            }

        }

        if (yangTrade.getPrice() == null) {
            coinPairRedisBean.setNew_price(BigDecimal.ZERO);

        } else {
            coinPairRedisBean.setNew_price(yangTrade.getPrice());

        }


//        long time_now = System.currentTimeMillis(); //当前时间戳
//        redisService.zAdd(CoinConst.REDIS_TREAD_24_ + coinPairRedisBean.getCurrency_id() + "-" + coinPairRedisBean.getCurrency_trade_id(), JSONObject.toJSON(yangTrade), time_now);

        redisService.hmSet(key, "cy_id", CommonTools.formatNull2String(String.valueOf(coinPairRedisBean.getCy_id())));


        redisService.hmSet(key, "currency_id", CommonTools.formatNull2String(String.valueOf(coinPairRedisBean.getCurrency_id())));
        redisService.hmSet(key, "currency_trade_id", CommonTools.formatNull2String(String.valueOf(coinPairRedisBean.getCurrency_trade_id())));
        redisService.hmSet(key, "currency_logo", CommonTools.formatNull2String(coinPairRedisBean.getCurrency_logo()));
        redisService.hmSet(key, "trade_logo", CommonTools.formatNull2String(coinPairRedisBean.getTrade_logo()));
        redisService.hmSet(key, "currency_mark", CommonTools.formatNull2String(coinPairRedisBean.getCurrency_mark()));
        redisService.hmSet(key, "trade_mark", CommonTools.formatNull2String(coinPairRedisBean.getTrade_mark()));
        redisService.hmSet(key, "new_price", CommonTools.formatNull2String(coinPairRedisBean.getNew_price().toString()));
        redisService.hmSet(key, "price_status", CommonTools.formatNull2String(String.valueOf(coinPairRedisBean.getPrice_status())));
        redisService.hmSet(key, "is_old", CommonTools.formatNull2String(String.valueOf(coinPairRedisBean.getIs_chuang())));
        redisService.hmSet(key, "is_chuang", CommonTools.formatNull2String(String.valueOf(coinPairRedisBean.getIs_old())));

//        redisService.hmSet(key,"is_old", CommonTools.formatNull2String(String.valueOf(getYangCurrencyPair.getIsOld()));


        //这三个后台更新的时候 修改对应的值


//                    'input_price_num'=>$cy->input_price_num,//价格显示位数
//                    'rate_num'=>$cy->rate_num,//汇率转换后的位数
//                    'is_chuang'=>$cy->is_chuang,


        return true;


    }


    /**
     * 获取交易对信息
     */
    public CoinPairRedisBean getCoinPairRedisBean(int currency_id, int currency_trade_id) {


        String key = CoinConst.REDIS_COIN_PAIR_BASE_ + currency_id + "-" + currency_trade_id;

        CoinPairRedisBean coinPairRedisBean = this.initCoinInfos(currency_id, currency_trade_id, key);


        return coinPairRedisBean;


    }


    /*更新交易信息*/
    @Transactional
    public void updateTradeInfo(YangOrders yangOrdersBuy, YangOrders yangOrdersSell, BigDecimal loopTradeNum, BigDecimal buyerTradeFeeNum, BigDecimal sellerTradeFeePrice, String type) {
        //新增本次循环交易记录 trade
        Integer isShua = 1; //刷单标记
        Integer isShua1 = 1; //刷单标记

        BigDecimal price_final;

        price_final = yangOrdersSell.getPrice();


        if (type.equals("sell")) //如果处理的是卖单，
        {
            isShua1 = 0;
            isShua = 1;
        } else {
            isShua1 = 1;
            isShua = 0;
        }

        YangTrade buyerTradeLog = new YangTrade();
        buyerTradeLog.setAddTime(System.currentTimeMillis() / 1000);
        buyerTradeLog.setCurrencyId(yangOrdersBuy.getCurrencyId());
        buyerTradeLog.setCurrencyTradeId(yangOrdersBuy.getCurrencyTradeId());
        buyerTradeLog.setMemberId(yangOrdersBuy.getMemberId());
        buyerTradeLog.setNum(loopTradeNum);
        buyerTradeLog.setType(buyType);

        buyerTradeLog.setPrice(price_final);
        buyerTradeLog.setMoney(loopTradeNum.multiply(price_final));


        buyerTradeLog.setShow(isShua);
        buyerTradeLog.setFee(buyerTradeFeeNum);
        buyerTradeLog.setStatus((byte) 0);
        buyerTradeLog.setTradeNo("T" + System.currentTimeMillis() / 1000);
        yangTradeMapper.insertSelective(buyerTradeLog);


        YangTrade sellerTradeLog = new YangTrade();
        sellerTradeLog.setAddTime(System.currentTimeMillis() / 1000);
        sellerTradeLog.setCurrencyId(yangOrdersBuy.getCurrencyId());
        sellerTradeLog.setCurrencyTradeId(yangOrdersBuy.getCurrencyTradeId());
        sellerTradeLog.setMemberId(yangOrdersSell.getMemberId());
        sellerTradeLog.setNum(loopTradeNum);
        sellerTradeLog.setType(sellType);

        sellerTradeLog.setPrice(price_final);
        sellerTradeLog.setMoney(loopTradeNum.multiply(price_final));


        sellerTradeLog.setShow(isShua1);
        sellerTradeLog.setFee(sellerTradeFeePrice);
        sellerTradeLog.setStatus((byte) 0);
        sellerTradeLog.setTradeNo("T" + System.currentTimeMillis() / 1000);
        yangTradeMapper.insertSelective(sellerTradeLog);


        //更新redis数据
        this.flushCoinInfos(buyerTradeLog.getCurrencyId(), buyerTradeLog.getCurrencyTradeId(), buyerTradeLog);


        //更新本次循环手续费交易记录
        YangFinance buyerFinance = new YangFinance(); //买单手续费
        buyerFinance.setAddTime(System.currentTimeMillis() / 1000);
        buyerFinance.setMemberId(yangOrdersBuy.getMemberId());
        buyerFinance.setType((byte) 11);
        buyerFinance.setContent("交易手续费");
        buyerFinance.setMoney(buyerTradeFeeNum);
        buyerFinance.setMoneyType((byte) 2);
        buyerFinance.setCurrencyId(yangOrdersBuy.getCurrencyId());
        buyerFinance.setIp("127.0.0.1");
        yangFinanceMapper.insertSelective(buyerFinance);

        YangFinance sellerFinance = new YangFinance(); //卖单手续费
        sellerFinance.setAddTime(System.currentTimeMillis() / 1000);
        sellerFinance.setMemberId(yangOrdersSell.getMemberId());
        sellerFinance.setType((byte) 11);
        sellerFinance.setContent("交易手续费");
        sellerFinance.setMoney(sellerTradeFeePrice);
        sellerFinance.setMoneyType((byte) 2);
        sellerFinance.setCurrencyId(yangOrdersBuy.getCurrencyTradeId());
        sellerFinance.setIp("127.0.0.1");
        yangFinanceMapper.insertSelective(sellerFinance);


    }

    /*获取买家卖家的自定义手续费配置*/
    public YangTradeFee getTradeFee(YangTradeFee yangTradeFee) {
        String key = CoinConst.REDIS_TRADE_FEE_MEMBER_ + yangTradeFee.getMemberId() + "_" + yangTradeFee.getCyId();
        if (redisService.get(key) != null) {
            return JSONObject.parseObject(redisService.get(key).toString(), YangTradeFee.class);

        } else {
            YangTradeFee yangTradeFee1 = yangTradeFeeMapper.selectOne(yangTradeFee);
            if (yangTradeFee1 != null) {
                redisService.set(key, JSONObject.toJSON(yangTradeFee1).toString());
            }
            return yangTradeFee1;

        }
    }


    public int updateUserMoney(
            Integer memberId, Integer currencyId, BigDecimal num, String type, String field,
            Integer memberId1, Integer currencyId1, BigDecimal num1, String type1, String field1,
            Integer memberId2, Integer currencyId2, BigDecimal num2, String type2, String field2,
            Integer memberId3, Integer currencyId3, BigDecimal num3, String type3, String field3,

            Integer memberId4, Integer currencyId4, BigDecimal num4, String type4, String field4  //应为有的时候 购买的金额 比自己挂单的价格要底 需要把多余冻结金额返还给账户余额

    ) {

////                call assets(3211,'0',125.001,'dec',56.89,'dec');
//        Integer i = yangMemberService.assets(3211,"0",BigDecimal.valueOf(125.001),"dec",BigDecimal.valueOf(56.89),"dec");
//        System.out.println(i);


        BigDecimal num_tmp;
        BigDecimal forzen_tmp;


        if (field != null && field.equals("normal")) {
            num_tmp = num;
            forzen_tmp = BigDecimal.ZERO;
        } else {
            num_tmp = BigDecimal.ZERO;
            forzen_tmp = num;
        }

        BigDecimal num1_tmp;
        BigDecimal forzen1_tmp;
        if (field1 != null && field1.equals("normal")) {
            num1_tmp = num1;
            forzen1_tmp = BigDecimal.ZERO;
        } else {
            num1_tmp = BigDecimal.ZERO;
            forzen1_tmp = num1;
        }


        BigDecimal num2_tmp;
        BigDecimal forzen2_tmp;
        if (field2 != null && field2.equals("normal")) {
            num2_tmp = num2;
            forzen2_tmp = BigDecimal.ZERO;
        } else {
            num2_tmp = BigDecimal.ZERO;
            forzen2_tmp = num2;
        }


        BigDecimal num3_tmp;
        BigDecimal forzen3_tmp;
        if (field3 != null && field3.equals("normal")) {
            num3_tmp = num3;
            forzen3_tmp = BigDecimal.ZERO;
        } else {
            num3_tmp = BigDecimal.ZERO;
            forzen3_tmp = num3;
        }

        BigDecimal num4_tmp;
        BigDecimal forzen4_tmp;
        if (field4 != null && field4.equals("normal")) {
            num4_tmp = num4;
            forzen4_tmp = BigDecimal.ZERO;
        } else {
            num4_tmp = BigDecimal.ZERO;
            forzen4_tmp = num4;
        }


        return yangMemberService.assets(
                memberId, String.valueOf(currencyId.intValue()), num_tmp, type, forzen_tmp, type,
                memberId1, String.valueOf(currencyId1.intValue()), num1_tmp, type1, forzen1_tmp, type1,
                memberId2, String.valueOf(currencyId2.intValue()), num2_tmp, type2, forzen2_tmp, type2,
                memberId3, String.valueOf(currencyId3.intValue()), num3_tmp, type3, forzen3_tmp, type3,
                memberId4, String.valueOf(currencyId4.intValue()), num4_tmp, type4, forzen4_tmp, type4
        );


    }


    //    /*更新用户资产*/
    @Async
    @Transactional
    public void updateUserMoneyBack(Integer memberId, Integer currencyId, BigDecimal num, String type, String field) {
        String lock = memberId.toString() + "-" + currencyId.toString();
        synchronized (lock.intern()) {
            checkCurrency(memberId, currencyId); //保证有资产记录
            if (currencyId == 0) {
                YangMember yangMemberQuery = new YangMember();
                yangMemberQuery.setMemberId(memberId);
                YangMember yangMemberFind = yangMemberMapper.selectOne(yangMemberQuery);

                YangMember yangMemberUpdate = new YangMember();
                yangMemberUpdate.setMemberId(memberId);

                if (type.equals("inc")) {
                    if (field.equals("normal")) {
                        yangMemberUpdate.setRmb(yangMemberFind.getRmb().add(num));
                    } else if (field.equals("forzen")) {
                        yangMemberUpdate.setForzenRmb(yangMemberFind.getForzenRmb().add(num));
                    }

                } else if (type.equals("dec")) {
                    if (field.equals("normal")) {
                        yangMemberUpdate.setRmb(yangMemberFind.getRmb().subtract(num));
                    } else if (field.equals("forzen")) {
                        yangMemberUpdate.setForzenRmb(yangMemberFind.getForzenRmb().subtract(num));
                    }
                }

                yangMemberMapper.updateByPrimaryKeySelective(yangMemberUpdate);


            } else {
                YangCurrencyUser yangCurrencyUserQuery = new YangCurrencyUser();
                yangCurrencyUserQuery.setCurrencyId(currencyId);
                yangCurrencyUserQuery.setMemberId(memberId);
                YangCurrencyUser yangCurrencyUserFind = yangCurrencyUserMapper.selectOne(yangCurrencyUserQuery);

                YangCurrencyUser yangCurrencyUserUpdate = new YangCurrencyUser();
                yangCurrencyUserUpdate.setCuId(yangCurrencyUserFind.getCuId());

                if (type.equals("inc")) {
                    if (field.equals("normal")) {
                        yangCurrencyUserUpdate.setNum(yangCurrencyUserFind.getNum().add(num));
                    } else if (field.equals("forzen")) {
                        yangCurrencyUserUpdate.setForzenNum(yangCurrencyUserFind.getForzenNum().add(num));
                    }
                } else if (type.equals("dec")) {
                    if (field.equals("normal")) {
                        yangCurrencyUserUpdate.setNum(yangCurrencyUserFind.getNum().subtract(num));
                    } else if (field.equals("forzen")) {
                        yangCurrencyUserUpdate.setForzenNum(yangCurrencyUserFind.getForzenNum().subtract(num));
                    }
                }

                yangCurrencyUserMapper.updateByPrimaryKeySelective(yangCurrencyUserUpdate);
            }
        }

    }


    /*挂单返利*/
    public void rebate(YangOrders yangOrder, BigDecimal tradeFeeNumOrPrice) {
//    public void rebate(YangOrders yangOrdersBuy,YangOrders yangOrdersSell,BigDecimal loopTradeNum,BigDecimal buyerTradeFeeNum,BigDecimal sellerTradeFeePrice){
        //查询一级返利
        BigDecimal rebateMoney = tradeFeeNumOrPrice.multiply(new BigDecimal(invitOnerebateConfig.getValue())).divide(new BigDecimal("100"));
        //查询买单的推荐用户
        YangMember yangMemberQuery = new YangMember();
        Example example = new Example(YangMember.class);
        example.createCriteria()
                .andEqualTo("memberId", yangOrder.getMemberId())
                .andGreaterThanOrEqualTo("invitTime", System.currentTimeMillis() / 100);
        List<YangMember> invitMembers = yangMemberMapper.selectByExample(example);
        Integer invitMemberId = null;

        if (null == invitMembers || invitMembers.size() == 0) {
            invitMemberId = Integer.valueOf(invitMemberIdConfig.getValue());
        } else {
            invitMemberId = invitMembers.get(0).getMemberId();
        }

        YangInvit yangInvitInsert = new YangInvit();
        yangInvitInsert.setMemberId(invitMemberId);
        yangInvitInsert.setMemberIdBottom(yangOrder.getMemberId());
        yangInvitInsert.setCtime(System.currentTimeMillis() / 1000);
        yangInvitInsert.setOrderId(yangOrder.getOrdersId());
        yangInvitInsert.setCfee(tradeFeeNumOrPrice);
        yangInvitInsert.setRebate(rebateMoney);
        yangInvitInsert.setTradeCurrencyId(yangOrder.getCurrencyTradeId());
        yangInvitInsert.setCurrencyId(yangOrder.getCurrencyId());
        yangInvitInsert.setOrderType(yangOrder.getType());
        yangInvitInsert.setOrderId(yangOrder.getOrdersId());
        yangInvitInsert.setStatus(1);
        if (yangOrder.getType().equals(buyType)) {
            yangInvitInsert.setRebatetype(1);
        } else if (yangOrder.getType().equals(sellType)) {
            yangInvitInsert.setRebatetype(2);
        }

        //新增返利表记录
        yangInvitMapper.insertSelective(yangInvitInsert);

        /*直接分配返利数据*/
        checkCurrency(yangInvitInsert.getMemberId(), yangOrder.getCurrencyId()); //保证有资产记录
        yangInvitUpdate(yangInvitInsert);

    }

    /*更新一笔返利数据*/
    public void yangInvitUpdate(YangInvit yangInvit) {
        if (yangInvit.getOrderType().equals(buyType)) {
            if (yangInvit.getCurrencyId() == 0) {//韩元市场
                /*上级返利*/
                YangMember yangMemberQuery = new YangMember();
                yangMemberQuery.setMemberId(yangInvit.getMemberId());
                YangMember yangMemberPreFind = yangMemberMapper.selectByPrimaryKey(yangMemberQuery);

                YangMember yangMemberUpdate = new YangMember();
                yangMemberUpdate.setMemberId(yangInvit.getMemberId());
                yangMemberUpdate.setRmb(yangMemberPreFind.getRmb().add(yangInvit.getRebate())); //上级给返利
                yangMemberMapper.updateByPrimaryKeySelective(yangMemberUpdate);
                /*指定账户返利*/
                YangMember yangMemberQuery2 = new YangMember();
                yangMemberQuery2.setMemberId(Integer.valueOf(invitMemberIdConfig.getValue())); //指定账户给剩余的返利
                YangMember yangMemberDefaultFind = yangMemberMapper.selectByPrimaryKey(yangMemberQuery);
                YangMember yangMemberDefaultUpdate = new YangMember();
                yangMemberDefaultUpdate.setMemberId(Integer.valueOf(invitMemberIdConfig.getValue()));
                yangMemberDefaultUpdate.setRmb(yangMemberDefaultFind.getRmb().add(yangInvit.getCfee()).subtract(yangInvit.getRebate())); //上级给返利 (手续费 - 返利)
                yangMemberMapper.updateByPrimaryKeySelective(yangMemberDefaultUpdate);

            } else { //非韩元
                YangCurrencyUser yangCurrencyUserQuery = new YangCurrencyUser();
                yangCurrencyUserQuery.setMemberId(yangInvit.getMemberId());
                yangCurrencyUserQuery.setCurrencyId(yangInvit.getCurrencyId());
                YangCurrencyUser yangCurrencyUserPreFind = yangCurrencyUserMapper.selectOne(yangCurrencyUserQuery);

                YangCurrencyUser yangCurrencyUserUpdate = new YangCurrencyUser();
                yangCurrencyUserUpdate.setCuId(yangCurrencyUserPreFind.getCuId());
                yangCurrencyUserUpdate.setNum(yangCurrencyUserPreFind.getNum().add(yangInvit.getRebate()));
                yangCurrencyUserMapper.updateByPrimaryKeySelective(yangCurrencyUserUpdate); //上级给返利

                /*指定账户返利*/
                YangCurrencyUser yangCurrencyUserQuery2 = new YangCurrencyUser();
                yangCurrencyUserQuery2.setMemberId(Integer.valueOf(invitMemberIdConfig.getValue()));
                yangCurrencyUserQuery2.setCurrencyId(yangInvit.getCurrencyId());
                YangCurrencyUser yangCurrencyUserDefaultFind = yangCurrencyUserMapper.selectOne(yangCurrencyUserQuery2);

                YangCurrencyUser yangCurrencyUserDefaultUpdate = new YangCurrencyUser();
                yangCurrencyUserDefaultUpdate.setCuId(yangCurrencyUserDefaultFind.getCuId());
                yangCurrencyUserDefaultUpdate.setNum(yangCurrencyUserDefaultFind.getNum().add(yangInvit.getCfee()).subtract(yangInvit.getRebate()));//上级给返利 (手续费 - 返利)
                yangCurrencyUserMapper.updateByPrimaryKeySelective(yangCurrencyUserDefaultUpdate);

            }
        } else {
            if (yangInvit.getTradeCurrencyId() == 0) {//韩元市场
                /*上级返利*/
                YangMember yangMemberQuery = new YangMember();
                yangMemberQuery.setMemberId(yangInvit.getMemberId());
                YangMember yangMemberPreFind = yangMemberMapper.selectByPrimaryKey(yangMemberQuery);

                YangMember yangMemberUpdate = new YangMember();
                yangMemberUpdate.setMemberId(yangInvit.getMemberId());
                yangMemberUpdate.setRmb(yangMemberPreFind.getRmb().add(yangInvit.getRebate())); //上级给返利
                yangMemberMapper.updateByPrimaryKeySelective(yangMemberUpdate);
                /*指定账户返利*/
                YangMember yangMemberQuery2 = new YangMember();
                yangMemberQuery2.setMemberId(Integer.valueOf(invitMemberIdConfig.getValue())); //指定账户给剩余的返利
                YangMember yangMemberDefaultFind = yangMemberMapper.selectByPrimaryKey(yangMemberQuery);
                YangMember yangMemberDefaultUpdate = new YangMember();
                yangMemberDefaultUpdate.setMemberId(Integer.valueOf(invitMemberIdConfig.getValue()));
                yangMemberDefaultUpdate.setRmb(yangMemberDefaultFind.getRmb().add(yangInvit.getCfee()).subtract(yangInvit.getRebate())); //上级给返利 (手续费 - 返利)
                yangMemberMapper.updateByPrimaryKeySelective(yangMemberDefaultUpdate);
            } else {

                YangCurrencyUser yangCurrencyUserQuery = new YangCurrencyUser();
                yangCurrencyUserQuery.setMemberId(yangInvit.getMemberId());
                yangCurrencyUserQuery.setCurrencyId(yangInvit.getTradeCurrencyId());
                YangCurrencyUser yangCurrencyUserPreFind = yangCurrencyUserMapper.selectOne(yangCurrencyUserQuery);

                YangCurrencyUser yangCurrencyUserUpdate = new YangCurrencyUser();
                yangCurrencyUserUpdate.setCuId(yangCurrencyUserPreFind.getCuId());
                yangCurrencyUserUpdate.setNum(yangCurrencyUserPreFind.getNum().add(yangInvit.getRebate()));
                yangCurrencyUserMapper.updateByPrimaryKeySelective(yangCurrencyUserUpdate); //上级给返利

                /*指定账户返利*/
                YangCurrencyUser yangCurrencyUserQuery2 = new YangCurrencyUser();
                yangCurrencyUserQuery2.setMemberId(Integer.valueOf(invitMemberIdConfig.getValue()));
                yangCurrencyUserQuery2.setCurrencyId(yangInvit.getTradeCurrencyId());
                YangCurrencyUser yangCurrencyUserDefaultFind = yangCurrencyUserMapper.selectOne(yangCurrencyUserQuery2);

                YangCurrencyUser yangCurrencyUserDefaultUpdate = new YangCurrencyUser();
                yangCurrencyUserDefaultUpdate.setCuId(yangCurrencyUserDefaultFind.getCuId());
                yangCurrencyUserDefaultUpdate.setNum(yangCurrencyUserDefaultFind.getNum().add(yangInvit.getCfee()).subtract(yangInvit.getRebate()));//上级给返利 (手续费 - 返利)
                yangCurrencyUserMapper.updateByPrimaryKeySelective(yangCurrencyUserDefaultUpdate);

            }
        }


    }

    //验证用户是否有资产记录，没有则创建初始化记录
    public void checkCurrency(Integer memberId, Integer currencyId) {
        if (currencyId == 0) {
            return;
        } else {
            Example example = new Example(YangCurrencyUser.class);
            example.createCriteria().andEqualTo("memberId", memberId).andEqualTo("currencyId", currencyId);
            int exist = yangCurrencyUserMapper.selectCountByExample(example);
            if (exist == 0) {
                YangCurrencyUser yangCurrencyUserInsert = new YangCurrencyUser();
                yangCurrencyUserInsert.setMemberId(memberId);
                yangCurrencyUserInsert.setCurrencyId(currencyId);
                yangCurrencyUserInsert.setNum(new BigDecimal("0"));
                yangCurrencyUserInsert.setForzenNum(new BigDecimal("0"));
                yangCurrencyUserInsert.setStatus((byte) 0);
                yangCurrencyUserMapper.insertSelective(yangCurrencyUserInsert);
            }
        }
    }

    /*获取手续费配置*/
    public YangCurrencyPair getYangCurrencyPair(YangOrders yangOrders) {
        YangCurrencyPair yangCurrencyPairQuery = new YangCurrencyPair();
        yangCurrencyPairQuery.setCurrencyId(yangOrders.getCurrencyId());
        yangCurrencyPairQuery.setCurrencyTradeId(yangOrders.getCurrencyTradeId());
        YangCurrencyPair yangCurrencyPairFind = yangCurrencyPairMapper.selectOne(yangCurrencyPairQuery);
        if (null == yangCurrencyPairFind) {
            throw new RuntimeException("手续费配置不存在");
        }
        return yangCurrencyPairFind;
    }


    /**
     * 获取订单信息
     *
     * @param currency_id
     * @param currency_trade_id
     * @param orders_id
     * @param type
     * @return
     */
    public List<Map> getOrderByfields(int currency_id, int currency_trade_id, int orders_id, String type) {

        Map parama = new HashMap();
        parama.put("currency_id", currency_id);
        parama.put("currency_trade_id", currency_trade_id);
        parama.put("orders_id", orders_id);
        parama.put("type", type);

        return yangOrdersMapper.getOrderByfields(parama);

    }

    /**
     * 获取trans信息
     *
     * @param cid
     * @param ordersId
     * @param type
     * @return
     */
    public List<Map> getTradeInfo(String cid, String ordersId, String type) {

        return null;


    }

    /**
     * K线数据
     *
     * @param map
     * @return
     */
    public List<Map> getKlineData(Map map) {
        return yangTradeMapper.klineData(map);
    }

    /**
     * K线数据
     *
     * @param map
     * @return
     */
    public List<Map> getKlineDataTest(Map map) throws Exception {
//        Query query=new Query();
//        query.with(new Sort(Sort.Direction.DESC,"scaleKey"));
        getMongoDataId((int) map.get("addTime"), (long) map.get("start"), (long) map.get("end"), map);
        DBObject query1 = new BasicDBObject(); //setup the query criteria 设置查询条件
        query1.put("scaleKey", (new BasicDBObject("$gte", (int) map.get("scaleKeyStart"))).append("$lte", (int) map.get("scaleKeyEnd")));
        DBCursor dbCursor = mongoTemplate.getCollection(CoinConst.KLINE_MONGODB_PREX + map.get("table_name") + "_" + (String) map.get("currencyId") + "_" + (String) map.get("currencyTradeId")).find(query1).sort(new BasicDBObject("scaleKey", 1));
        List<Map> list = new ArrayList<Map>();
        while (dbCursor.hasNext()) {
            DBObject object = dbCursor.next();
            Map changeMap = new HashMap();
            changeMap.put("trade_id", object.get("tradeId") == null ? "" : object.get("tradeId").toString());
            changeMap.put("group_time", object.get("groupTime").toString());
            changeMap.put("trade_time", object.get("tradeTime").toString());
            changeMap.put("max_price", object.get("maxPrice").toString());
            changeMap.put("min_price", object.get("minPrice").toString());
            changeMap.put("open_price", object.get("openPrice").toString());
            changeMap.put("close_price", object.get("closePrice").toString());
            changeMap.put("num", object.get("num").toString());
            list.add(changeMap);
        }
        return list;
    }

    /**
     * K线数据
     *
     * @param map
     * @return
     */
    public List<Map> getKlineDataApi(Map map) {
        getMongoDataId(Integer.parseInt(map.get("addTime").toString()), Long.parseLong(map.get("start").toString()), Long.parseLong(map.get("end").toString()), map);
        DBObject query1 = new BasicDBObject(); //setup the query criteria 设置查询条件
        BasicDBObject basicDBObject = new BasicDBObject();
        if (Long.parseLong(map.get("start").toString()) != 0) {
            basicDBObject.append("$gte", (int) map.get("scaleKeyStart"));
        }
        if (Long.parseLong(map.get("end").toString()) != 0) {
            basicDBObject.append("$lte", (int) map.get("scaleKeyEnd"));
        }
        if (Long.parseLong(map.get("start").toString()) != 0 || Long.parseLong(map.get("end").toString()) != 0) {
            query1.put("scaleKey", basicDBObject);
        }
        DBCursor dbCursor = mongoTemplate.getCollection(CoinConst.KLINE_MONGODB_PREX + map.get("table_name") + "_" + map.get("currencyId").toString() + "_" + map.get("currencyTradeId").toString()).find(query1).limit(1000).sort(new BasicDBObject("scaleKey", 1));
        ;
        List<Map> list = new ArrayList<Map>();
        while (dbCursor.hasNext()) {
            DBObject object = dbCursor.next();
            Map changeMap = new HashMap();
            changeMap.put("trade_id", object.get("tradeId") == null ? "" : object.get("tradeId").toString());
            changeMap.put("group_time", object.get("groupTime").toString());
            changeMap.put("trade_time", object.get("tradeTime").toString());
            changeMap.put("max_price", object.get("maxPrice").toString());
            changeMap.put("min_price", object.get("minPrice").toString());
            changeMap.put("open_price", object.get("openPrice").toString());
            changeMap.put("close_price", object.get("closePrice").toString());
            changeMap.put("num", object.get("num").toString());
            changeMap.put("count", object.get("count").toString());
            Double a = (Double) object.get("maxPrice");
            Double b = (Double) object.get("minPrice");
            Double c = (Double) object.get("num");
            changeMap.put("money", (a + b) / 2 * c);
            list.add(changeMap);
        }
        return list;
    }

    /**
     * 根据addTIme判断 判断增量
     *
     * @return
     */
    public static Map getMongoDataId(int timeUnit, Long start, Long end, Map map) {
        switch (timeUnit) {
            case 60:
                map.put("table_name", "oneMinute");
                break;
            case 180:
                map.put("table_name", "threeMinute");
                break;
            case 300:
                map.put("table_name", "fiveMinute");
                break;
            case 900:
                map.put("table_name", "fifteenMinute");
                break;
            case 1800:
                map.put("table_name", "thirtyMinute");
                break;
            case 3600:
                map.put("table_name", "oneHour");
                break;
            case 21600:
                map.put("table_name", "sixHour");
                break;
            case 86400:
                map.put("table_name", "oneDay");
                break;
            case 604800:
                map.put("table_name", "oneWeek");
                break;
        }
        map.put("times", timeUnit);

        //开始时间段key
        long startScaleKey = start / timeUnit;//取商
        long startLeft = start % timeUnit;//取余
        if (startLeft >= (timeUnit / 2)) {
            map.put("scaleKeyStart", new Long(startScaleKey + 1L).intValue());
        } else {
            map.put("scaleKeyStart", new Long(startScaleKey).intValue());
        }
        long endScaleKey = end / timeUnit;//取商
        long endLeft = end % timeUnit;//取余
        if (endLeft >= (timeUnit / 2)) {
            map.put("scaleKeyEnd", new Long(endScaleKey + 1L).intValue());
        } else {
            map.put("scaleKeyEnd", new Long(endScaleKey).intValue());
        }
        return map;

    }

    public Result delMongoDB() {
        Result result = new Result();
        Example example = new Example(YangCurrencyPair.class);
//        List<YangCurrencyPair> list=yangCurrencyPairMapper.selectByExample(example);
//        List<String> typelist=getDataType();
//
//        for(int i=0;i<list.size();i++){
//            for(int j=0;j<typelist.size();j++){
//            this.mongoTemplate.dropCollection(  CoinConst.KLINE_MONGODB_PREX+typelist.get(j)+"_"+list.get(i).getCurrencyId()+"_"+list.get(i).getCurrencyTradeId());
//            }
//        }

        ServerAddress serverAddress = new ServerAddress("192.168.31.126", 27017);
        List<ServerAddress> addrs = new ArrayList<ServerAddress>();
        addrs.add(serverAddress);

        MongoCredential credential = MongoCredential.createScramSha1Credential("funcoin", "funcoin", "123456".toCharArray());
        List<MongoCredential> credentials = new ArrayList<MongoCredential>();
        credentials.add(credential);
        MongoClient mongoClient = new MongoClient(addrs, credentials);
        MongoDatabase database = mongoClient.getDatabase("funcoin");

//        Query query=new Query();
//        query.addCriteria(Criteria.where("lastTradeTime").gte(1537344074));
        Set<String> namelist = this.mongoTemplate.getCollectionNames();
        for (String collectionName : namelist) {
            if (collectionName.indexOf(CoinConst.KLINE_MONGODB_PREX) == 0) {
                MongoCollection<Document> collection = database.getCollection(collectionName);
                collection.createIndex(new BasicDBObject("scaleKey", 1));
            }
        }


//        for(int i=0;i<list.size();i++){
//            for(int j=0;j<typelist.size();j++){
//                Set<String> namelist=this.mongoTemplate.getCollectionNames();
//                MongoCollection<Document> collection = database.getCollection(CoinConst.KLINE_MONGODB_PREX+typelist.get(j)+"_"+list.get(i).getCurrencyId()+"_"+list.get(i).getCurrencyTradeId());
//                collection.createIndex(new BasicDBObject("scaleKey", 1));
        //                this.mongoTemplate.remove(query,CoinConst.KLINE_MONGODB_PREX+typelist.get(j)+"_"+list.get(i).getCurrencyId()+"_"+list.get(i).getCurrencyTradeId());
//            this.mongoTemplate.dropCollection(  CoinConst.KLINE_MONGODB_PREX+typelist.get(j)+"_"+list.get(i).getCurrencyId()+"_"+list.get(i).getCurrencyTradeId());
//            }
//        }

        result.setCode(Result.Code.SUCCESS);
        result.setData(null);
        return result;
    }

    private static List<String> getDataType() {
        List<String> list = new ArrayList<String>();
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

    /**
     * 查询用户前五条委托记录
     *
     * @param map
     * @return
     */
    public Result getFontUserOrderFiveRecord(Map map, YangMemberToken yangMemberToken) {
        Result result = new Result();
        try {
            YangMember yangMemberSelf = yangMemberTokenService.findMember(yangMemberToken);
            if (yangMemberSelf == null) {
                result.setErrorCode(ErrorCode.ERROR_ERROR_TOKEN.getIndex());
                result.setMsg(ErrorCode.ERROR_ERROR_TOKEN.getMessage());
                result.setCode(Result.Code.ERROR);
                return result;
            }
            String redisKey = CoinConst.FONT_USER_ORDERS_FIVE_RECORDS + (String) map.get("currencyId") + "_" + (String) map.get("currencyTradeId") + "_" + yangMemberSelf.getMemberId();
            if (!redisService.exists(redisKey)) {
                map.put("memberId", yangMemberSelf.getMemberId());
                List<YangOrders> userOrdersList = yangOrdersMapper.fontUserTradeFiveRecord(map);
                redisService.set(redisKey, JSONObject.toJSONString(userOrdersList));
                result.setCode(Result.Code.SUCCESS);
                result.setData(userOrdersList);
            } else {
                List<YangOrders> list = JSONObject.parseArray(redisService.get(redisKey).toString(), YangOrders.class);
                result.setCode(Result.Code.SUCCESS);
                result.setData(list);
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
            result.setMsg(ErrorCode.ERROR_SYSTEM.getMessage());
            result.setCode(Result.Code.ERROR);
        }
        return result;
    }


    /**
     * 查询用户交易记录
     *
     * @param map
     * @return
     */
    public Result getFontUserTradeFiveRecord(Map map, YangMemberToken yangMemberToken) {
        Result result = new Result();
        try {
            validateAccessToken(yangMemberToken.getAccessToken(), result, yangMemberTokenService);
            if (result.getCode().intValue() == Result.Code.ERROR.intValue()) {
                return result;
            }
            YangMember yangMember = (YangMember) result.getData();
            int currencyId = Integer.valueOf((String) map.get("currencyId"));
            int currencyTradeId = Integer.valueOf((String) map.get("currencyTradeId"));
            Query query = new Query();
            query.addCriteria(Criteria.where("currencyId").is(currencyId)).
                    addCriteria(Criteria.where("currencyTradeId").is(currencyTradeId)).
                    addCriteria(Criteria.where("memberId").is(yangMember.getMemberId()));
            query.with(new Sort(new Sort.Order(Sort.Direction.DESC, "addTime")));
            List<MgYangTrade> sourceList = this.mongoTemplate.find(query, MgYangTrade.class, CoinConst.MONGODB_TRADE_COLLECTION);
            result.setCode(Result.Code.SUCCESS);
            result.setData(sourceList);
        } catch (Exception e) {
            e.printStackTrace();
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
            result.setMsg(ErrorCode.ERROR_SYSTEM.getMessage());
            result.setCode(Result.Code.ERROR);
        }
        return result;
    }

    /**
     * 每个币种最新交易时间
     *
     * @param map
     * @return
     */
    public Result getFontTradeLastTime(Map map) {
        Result result = new Result();
        try {
            String key = CoinConst.FONT_TRADE_LAST_TIME + map.get("currencyId") + "_" + map.get("currencyTradeId");
            if (redisService.exists(key)) {
                result.setData(JSONObject.parseObject(redisService.get(key).toString(), Integer.class));
            } else {
                map = yangTradeMapper.getFontTradeLastTime(map);
                if (map != null) {
                    int time = (int) map.get("lastTradeTime");
                    result.setData(time);
                    redisService.set(key, time);
                } else {
                    result.setData(0);
                    redisService.set(key, 0);
                }
            }
            result.setCode(Result.Code.SUCCESS);

        } catch (Exception e) {
            e.printStackTrace();
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
            result.setMsg(ErrorCode.ERROR_SYSTEM.getMessage());
            result.setCode(Result.Code.ERROR);
        }
        return result;
    }

    class YangOrdersAddTime implements Comparator<YangOrders> {
        @Override
        public int compare(YangOrders e1, YangOrders e2) {
            if (e1.getAddTime().intValue() - e2.getAddTime().intValue() < 0) {
                return 1;
            } else if (e1.getAddTime().intValue() == e2.getAddTime().intValue()) {
                if (e1.getOrdersId().intValue() - e2.getOrdersId().intValue() < 0) {
                    return 1;
                } else {
                    return -1;
                }

            } else {
                return -1;
            }
        }
    }

    class YangTradeAddTime implements Comparator<YangTrade> {
        @Override
        public int compare(YangTrade e1, YangTrade e2) {
            if (e1.getAddTime().intValue() - e2.getAddTime().intValue() < 0) {
                return 1;
            } else if (e1.getAddTime().intValue() == e2.getAddTime().intValue()) {
                if (e1.getTradeId().intValue() - e2.getTradeId().intValue() < 0) {
                    return 1;
                } else {
                    return -1;
                }

            } else {
                return -1;
            }
        }
    }

    //撤销订单
    public Result cancelOrder(String ordersId, String accessToken) {
        Result result = new Result();
        try {
            validateAccessToken(accessToken, result, yangMemberTokenService);
            if (result.getCode().intValue() == Result.Code.ERROR.intValue()) {
                return result;
            }


            //设置订单 状态为   处理中 ORDER_STATUS_CANCEL_ING
            YangOrders yangOrders = new YangOrders();
            yangOrders.setOrdersId(Integer.valueOf(ordersId));

            //判断订单是否为未成交的和部分成交的
            YangOrders nowYangorder = yangOrdersMapper.selectByPrimaryKey(yangOrders);
            if (nowYangorder.getStatus() != 1 && nowYangorder.getStatus() != 0) {
                result.setCode(Result.Code.ERROR);
                result.setErrorCode(ErrorCode.SEND_EMAILCODE_ERROR.getIndex());
                result.setMsg(ErrorCode.SEND_EMAILCODE_ERROR.getMessage());
                return result;
            }

            yangOrders.setStatus(CoinConst.ORDER_STATUS_CANCEL_ING);
            yangOrdersMapper.updateByPrimaryKeySelective(yangOrders);

            //调用 撤单存储过程
            Map map = dealOrder(
                    0, null, null, null, null, 0,
                    0, null, null, null, null, 0,
                    0, null, null, null, null, 0,
                    0, null, null, null, null, 0,
                    0, null, null, null, null, 0,

                    0, null, 0,
                    0, null, 0,
                    null, 0, 0, 0, null, null, null, null, null, 0, 0, 0, 0,
                    null, 0, 0, 0, null, null, null, null, null, 0, 0, 0, 0,
                    0, 0, 0, null, 0, 0, null, null,
                    0, 0, 0, null, 0, 0, null, null,
                    Integer.valueOf(ordersId).intValue(),
                    CoinConst.MYSQL_GC_DEAL_TYPE_CANCEL

            );
            Integer a = (Integer) map.get("res");
            if (a == 1) {
                //设置订单状态为异常
                YangOrders yangOrdersa = new YangOrders();
                yangOrdersa.setOrdersId(Integer.valueOf(ordersId));
                yangOrdersa.setStatus(CoinConst.ORDER_STATUS_ERROR);
                yangOrdersMapper.updateByPrimaryKeySelective(yangOrdersa);
                result.setCode(Result.Code.ERROR);
                result.setErrorCode(ErrorCode.ERROR_ORDER_UNNORMAL.getIndex());
                result.setMsg(ErrorCode.ERROR_ORDER_UNNORMAL.getMessage());
            } else {
                result.setCode(Result.Code.SUCCESS);
                result.setMsg("撤单成功");
            }
            //不管撤单是否完成  查询  用户前5条记录   订单状态 -1 在存储过程里改过了
            recordFontUserFiveRecor(yangOrders);
        } catch (Exception e) {
            e.printStackTrace();
            result.setCode(Result.Code.ERROR);
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
            result.setMsg(ErrorCode.ERROR_SYSTEM.getMessage());
        }
        return result;
    }

    //查询用户前5条委托记录
    public void recordFontUserFiveRecor(YangOrders yangOrders) {
        YangOrders yangOrdersFind = yangOrdersMapper.selectByPrimaryKey(yangOrders);
        String key = CoinConst.FONT_USER_ORDERS_FIVE_RECORDS + yangOrdersFind.getCurrencyId() + "_" + yangOrdersFind.getCurrencyTradeId() + "_" + yangOrdersFind.getMemberId();
        Map pamaMap = new HashMap();
        pamaMap.put("currencyId", yangOrdersFind.getCurrencyId());
        pamaMap.put("currencyTradeId", yangOrdersFind.getCurrencyTradeId());
        pamaMap.put("memberId", yangOrdersFind.getMemberId());
        List<YangOrders> userOrdersList = yangOrdersMapper.fontUserTradeFiveRecord(pamaMap);
        redisService.set(key, JSONObject.toJSONString(userOrdersList));
    }


    public void cancleOrderRobot(String accessToken) {
        YangMemberToken yangMemberToken = new YangMemberToken();
        yangMemberToken.setAccessToken(accessToken);
        YangMember yangMemberSelf = yangMemberTokenService.findMember(yangMemberToken);
        Example example = new Example(YangOrders.class);
        List<String> a = new ArrayList<String>();
        a.add("0");
        a.add("1");
        example.createCriteria().andIn("status", a).andEqualTo("hasDo", "1").andEqualTo("memberId", yangMemberSelf.getMemberId());
        ;
        List<YangOrders> list = yangOrdersMapper.selectByExample(example);
        for (YangOrders yangOrders : list) {

//            Map map=new HashMap();
//            map.put("ordersId",String.valueOf(yangOrders.getOrdersId()));
//            map.put("memberId",String.valueOf(yangMemberSelf.getMemberId()));
//            if(redisService.exists(CoinConst.ORDER_CANCLE_LIST)){
//                if(!redisService.zExist(CoinConst.ORDER_CANCLE_LIST,JSONObject.toJSONString(map))){
//                redisService.lPush(CoinConst.ORDER_CANCLE_LIST,JSONObject.toJSONString(map));
//                }
//            }else{
//                redisService.lPush(CoinConst.ORDER_CANCLE_LIST,JSONObject.toJSONString(map));
//            }
//            redisService.lPush(CoinConst.ORDER_CANCLE_LIST,JSONObject.toJSONString(map));
        }
    }

    public Result searchOrder(String accessToken, int page, int pageSize, String currencyId, String currencyTradeId, String begin, String end, String type) {
        Result result = new Result();
        try {
            validateAccessToken(accessToken, result, yangMemberTokenService);
            if (result.getCode().intValue() == Result.Code.ERROR.intValue()) {
                return result;
            }
            YangMember yangMember = (YangMember) result.getData();

            Map map = new HashMap();
            if (StringUtils.isNotEmpty(currencyId)) {
                map.put("currencyId", currencyId);
            }
            if (StringUtils.isNotEmpty(currencyTradeId)) {
                map.put("currencyTradeId", currencyTradeId);
            }
            if (StringUtils.isNotEmpty(begin)) {
                map.put("beginTime", begin);
            }
            if (StringUtils.isNotEmpty(end)) {
                map.put("endTime", end);
            }
            if (StringUtils.isNotEmpty(type)) {
                map.put("type", type);
            }
            map.put("memberId", yangMember.getMemberId());
            map.put("start", (page - 1) * pageSize);
            map.put("end", pageSize);
            List<Map> list = yangOrdersMapper.myFontOrders(map);
            int total = yangOrdersMapper.myFontOrdersCount(map);
            PageInfo<Map> pageInfo = new PageInfo<Map>(list);
            pageInfo.setTotal(Long.valueOf(total));
            result.setCode(Result.Code.SUCCESS);
            result.setData(pageInfo);
        } catch (Exception e) {
            e.printStackTrace();
            result.setCode(Result.Code.ERROR);
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
            result.setMsg(ErrorCode.ERROR_SYSTEM.getMessage());
        }
        return result;
    }

    public Result searchTrade(String accessToken, int page, int pageSize, String currencyId, String currencyTradeId, String begin, String end, String type) {
        Result result = new Result();
        try {
            validateAccessToken(accessToken, result, yangMemberTokenService);
            if (result.getCode().intValue() == Result.Code.ERROR.intValue()) {
                return result;
            }
            YangMember yangMember = (YangMember) result.getData();
            Map map = new HashMap();
            map.put("memberId", yangMember.getMemberId());
            if (StringUtils.isNotEmpty(currencyId)) {
                map.put("currencyId", currencyId);
            }
            if (StringUtils.isNotEmpty(currencyTradeId)) {
                map.put("currencyTradeId", currencyTradeId);
            }
            if (StringUtils.isNotEmpty(begin)) {
                map.put("beginDate", begin);
            }
            if (StringUtils.isNotEmpty(end)) {
                map.put("endDate", end);
            }
            if (StringUtils.isNotEmpty(type)) {
                map.put("type", type);
            }
            PageInfo<MgYangTrade> pageInfo = getSearchTradeList(map, page, pageSize);
            result.setData(pageInfo);
        } catch (Exception e) {
            e.printStackTrace();
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
            result.setCode(Result.Code.ERROR);
            result.setMsg(ErrorCode.ERROR_SYSTEM.getMessage());
        }
        return result;
    }


    public PageInfo<MgYangTrade> getSearchTradeList(Map paramMap, int page, int pageSize) {
        PageInfo<MgYangTrade> pageInfo = new PageInfo<MgYangTrade>();
        Query query = new Query();
        query.addCriteria(Criteria.where("memberId").is(paramMap.get("memberId")));
        if (null != paramMap.get("type") && StringUtils.isNotBlank(String.valueOf(paramMap.get("type")))) {
            query.addCriteria(Criteria.where("type").is(Integer.valueOf((String) paramMap.get("type"))));
        }
        if (null != paramMap.get("currencyId") && StringUtils.isNotBlank(String.valueOf(paramMap.get("currencyId")))) {
            query.addCriteria(Criteria.where("currencyId").is(Integer.valueOf((String) paramMap.get("currencyId"))));
        }
        if (null != paramMap.get("currencyTradeId") && StringUtils.isNotBlank(String.valueOf(paramMap.get("currencyTradeId")))) {
            query.addCriteria(Criteria.where("currencyTradeId").is(Integer.valueOf((String) paramMap.get("currencyTradeId"))));
        }
        if (null != paramMap.get("beginDate") && StringUtils.isNotBlank(String.valueOf(paramMap.get("beginDate"))) && null != paramMap.get("endDate") && StringUtils.isNotBlank(String.valueOf(paramMap.get("endDate")))) {
            int data = Integer.valueOf((String) paramMap.get("beginDate"));
            int data2 = Integer.valueOf((String) paramMap.get("endDate"));
            query.addCriteria(Criteria.where("addTime").gte(data).lte(data2));

        }
        if (null != paramMap.get("beginDate") && StringUtils.isNotBlank(String.valueOf(paramMap.get("beginDate"))) && !(null != paramMap.get("endDate") && StringUtils.isNotBlank(String.valueOf(paramMap.get("endDate"))))) {
            int data = Integer.valueOf((String) paramMap.get("beginDate"));
            query.addCriteria(Criteria.where("addTime").gte(data));
        }
        if (null != paramMap.get("endDate") && StringUtils.isNotBlank(String.valueOf(paramMap.get("endDate"))) && !(null != paramMap.get("beginDate") && StringUtils.isNotBlank(String.valueOf(paramMap.get("beginDate"))))) {
            int data = Integer.valueOf((String) paramMap.get("endData"));
            query.addCriteria(Criteria.where("addTime").lte(data));
        }
        //总页数
        long pageNum = this.mongoTemplate.count(query, MgYangTrade.class, CoinConst.MONGODB_TRADE_COLLECTION);
        //list
        query.with(new Sort(new Sort.Order(Sort.Direction.DESC, "addTime")));
        query.skip((page - 1) * pageSize).limit(pageSize);
        List<MgYangTrade> resultList = this.mongoTemplate.find(query, MgYangTrade.class, CoinConst.MONGODB_TRADE_COLLECTION);

        //为每条记录增加  位数处理
        for (MgYangTrade trade : resultList) {
            String key = CoinConst.REDIS_YANG_PARI_INFO_ + trade.getCurrencyId() + "_" + trade.getCurrencyTradeId();
            YangCurrencyPair yangCurrencyPair = JSONObject.parseObject(redisService.get(key).toString(), YangCurrencyPair.class);
            trade.setInputPriceNum(yangCurrencyPair.getInputPriceNum());
            trade.setShowNum(yangCurrencyPair.getShowNum());
            trade.setAllMoneyNum(yangCurrencyPair.getAllMoneyNum());
        }
        pageInfo.setList(resultList);
        pageInfo.setTotal(pageNum);
        return pageInfo;
    }

    //订单 交易或者撤单
    public Map dealOrder(Integer memberId, Integer currencyId, BigDecimal num, String type, String field, int tradeOrderId,
                         Integer memberId1, Integer currencyId1, BigDecimal num1, String type1, String field1, int tradeOrderId1,
                         Integer memberId2, Integer currencyId2, BigDecimal num2, String type2, String field2, int tradeOrderId2,
                         Integer memberId3, Integer currencyId3, BigDecimal num3, String type3, String field3, int tradeOrderId3,

                         Integer memberId4, Integer currencyId4, BigDecimal num4, String type4, String field4, int tradeOrderId4,  //应为有的时候 购买的金额 比自己挂单的价格要底 需要把多余冻结金额返还给账户余额


                         int orderId1, BigDecimal tradeNum1, long tradeTime1,
                         int orderId2, BigDecimal tradeNum2, long tradeTime2,
                         String tradeNo1, int t_memberId1, int t_currencyId1, int currencyTradeId1, BigDecimal price1, BigDecimal t_num1, BigDecimal t_money1, BigDecimal fee1, String t_type1, long t_addTime1, int t_status1, int show1, int t_orders_id1,
                         String tradeNo2, int t_memberId2, int t_currencyId2, int currencyTradeId2, BigDecimal price2, BigDecimal t_num2, BigDecimal t_money2, BigDecimal fee2, String t_type2, long t_addTime2, int t_status2, int show2, int t_orders_id2,
                         int f_memberId1, int f_type1, int f_moneyType1, BigDecimal f_money1, long f_addTime1, int f_currencyId1, String f_ip1, String f_content1,
                         int f_memberId2, int f_type2, int f_moneyType2, BigDecimal f_money2, long f_addTime2, int f_currencyId2, String f_ip2, String f_content2,
                         int order_id,
                         int dealType) {
        HashMap pama = new HashMap();
        if (CoinConst.MYSQL_GC_DEAL_TYPE_TRADE == dealType) {
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
            if (field != null && field.equals("normal")) {
                num_tmp = num;
                forzen_tmp = BigDecimal.ZERO;
            } else {
                num_tmp = BigDecimal.ZERO;
                forzen_tmp = num;
            }
            pama.put("memberId", memberId);
            pama.put("cyId", currencyId);
            pama.put("num", num_tmp);
            pama.put("numOptions", type);
            pama.put("forzen", forzen_tmp);
            pama.put("forzenOptions", type);
            pama.put("trade_orders_id", tradeOrderId);

            if (field1 != null && field1.equals("normal")) {
                num1_tmp = num1;
                forzen1_tmp = BigDecimal.ZERO;
            } else {
                num1_tmp = BigDecimal.ZERO;
                forzen1_tmp = num1;
            }
            pama.put("memberId1", memberId1);
            pama.put("cyId1", currencyId1);
            pama.put("num1", num1_tmp);
            pama.put("numOptions1", type1);
            pama.put("forzen1", forzen1_tmp);
            pama.put("forzenOptions1", type1);
            pama.put("trade_orders_id1", tradeOrderId1);
            if (field2 != null && field2.equals("normal")) {
                num2_tmp = num2;
                forzen2_tmp = BigDecimal.ZERO;
            } else {
                num2_tmp = BigDecimal.ZERO;
                forzen2_tmp = num2;
            }
            pama.put("memberId2", memberId2);
            pama.put("cyId2", currencyId2);
            pama.put("num2", num2_tmp);
            pama.put("numOptions2", type2);
            pama.put("forzen2", forzen2_tmp);
            pama.put("forzenOptions2", type2);
            pama.put("trade_orders_id2", tradeOrderId2);
            if (field3 != null && field3.equals("normal")) {
                num3_tmp = num3;
                forzen3_tmp = BigDecimal.ZERO;
            } else {
                num3_tmp = BigDecimal.ZERO;
                forzen3_tmp = num3;
            }
            pama.put("memberId3", memberId3);
            pama.put("cyId3", currencyId3);
            pama.put("num3", num3_tmp);
            pama.put("numOptions3", type3);
            pama.put("forzen3", forzen3_tmp);
            pama.put("forzenOptions3", type3);
            pama.put("trade_orders_id3", tradeOrderId3);
            if (field4 != null && field4.equals("normal")) {
                num4_tmp = num4;
                forzen4_tmp = BigDecimal.ZERO;
            } else {
                num4_tmp = BigDecimal.ZERO;
                forzen4_tmp = num4;
            }
            pama.put("memberId4", memberId4);
            pama.put("cyId4", currencyId4);
            pama.put("num4", num4_tmp);
            pama.put("numOptions4", type4);
            pama.put("forzen4", forzen4_tmp);
            pama.put("forzenOptions4", type4);
            pama.put("trade_orders_id4", tradeOrderId4);


            pama.put("orderId1", orderId1);
            pama.put("tradeNum1", tradeNum1);
            pama.put("tradeTime1", tradeTime1);
            pama.put("orderId2", orderId2);
            pama.put("tradeNum2", tradeNum2);
            pama.put("tradeTime2", tradeTime2);

            pama.put("tradeNo1", tradeNo1);
            pama.put("t_memberId1", t_memberId1);
            pama.put("t_currencyId1", t_currencyId1);
            pama.put("currencyTradeId1", currencyTradeId1);
            pama.put("price1", price1);
            pama.put("t_num1", t_num1);
            pama.put("t_money1", t_money1);
            pama.put("fee1", fee1);
            pama.put("t_type1", t_type1);
            pama.put("t_addTime1", t_addTime1);
            pama.put("t_status1", t_status1);
            pama.put("show1", show1);
            pama.put("t_orders_id1", t_orders_id1);
            pama.put("tradeNo2", tradeNo2);
            pama.put("t_memberId2", t_memberId2);
            pama.put("t_currencyId2", t_currencyId2);
            pama.put("currencyTradeId2", currencyTradeId2);
            pama.put("price2", price2);
            pama.put("t_num2", t_num2);
            pama.put("t_money2", t_money2);
            pama.put("fee2", fee2);
            pama.put("t_type2", t_type2);
            pama.put("t_addTime2", t_addTime2);
            pama.put("t_status2", t_status2);
            pama.put("show2", show2);
            pama.put("t_orders_id2", t_orders_id2);


            pama.put("f_memberId1", f_memberId1);
            pama.put("f_type1", f_type1);
            pama.put("f_moneyType1", f_moneyType1);
            pama.put("f_money1", f_money1);
            pama.put("f_addTime1", f_addTime1);
            pama.put("f_currencyId1", f_currencyId1);
            pama.put("f_ip1", f_ip1);
            pama.put("f_content1", f_content1);
            pama.put("f_memberId2", f_memberId2);
            pama.put("f_type2", f_type2);
            pama.put("f_moneyType2", f_moneyType2);
            pama.put("f_money2", f_money2);
            pama.put("f_addTime2", f_addTime2);
            pama.put("f_currencyId2", f_currencyId2);
            pama.put("f_ip2", f_ip2);
            pama.put("f_content2", f_content2);
            pama.put("order_id_in", 0);
        }
        if (CoinConst.MYSQL_GC_DEAL_TYPE_CANCEL == dealType) {
            pama.put("memberId", 0);
            pama.put("cyId", null);
            pama.put("num", null);
            pama.put("numOptions", null);
            pama.put("forzen", null);
            pama.put("forzenOptions", null);
            pama.put("trade_orders_id", tradeOrderId);
            pama.put("memberId1", 0);
            pama.put("cyId1", null);
            pama.put("num1", null);
            pama.put("numOptions1", null);
            pama.put("forzen1", null);
            pama.put("forzenOptions1", null);
            pama.put("trade_orders_id1", tradeOrderId1);
            pama.put("memberId2", 0);
            pama.put("cyId2", null);
            pama.put("num2", null);
            pama.put("numOptions2", null);
            pama.put("forzen2", null);
            pama.put("forzenOptions2", null);
            pama.put("trade_orders_id2", tradeOrderId2);
            pama.put("memberId3", 0);
            pama.put("cyId3", null);
            pama.put("num3", null);
            pama.put("numOptions3", null);
            pama.put("forzen3", null);
            pama.put("forzenOptions3", null);
            pama.put("trade_orders_id3", tradeOrderId3);
            pama.put("memberId4", 0);
            pama.put("cyId4", null);
            pama.put("num4", null);
            pama.put("numOptions4", null);
            pama.put("forzen4", null);
            pama.put("forzenOptions4", null);
            pama.put("trade_orders_id4", tradeOrderId4);


            pama.put("orderId1", null);
            pama.put("tradeNum1", null);
            pama.put("tradeTime1", null);
            pama.put("orderId2", null);
            pama.put("tradeNum2", null);
            pama.put("tradeTime2", null);

            pama.put("tradeNo1", null);
            pama.put("t_memberId1", null);
            pama.put("t_currencyId1", null);
            pama.put("currencyTradeId1", null);
            pama.put("price1", null);
            pama.put("t_num1", null);
            pama.put("t_money1", null);
            pama.put("fee1", null);
            pama.put("t_type1", null);
            pama.put("t_addTime1", null);
            pama.put("t_status1", null);
            pama.put("show1", null);
            pama.put("t_orders_id1", null);
            pama.put("tradeNo2", null);
            pama.put("t_memberId2", null);
            pama.put("t_currencyId2", null);
            pama.put("currencyTradeId2", null);
            pama.put("price2", null);
            pama.put("t_num2", null);
            pama.put("t_money2", null);
            pama.put("fee2", null);
            pama.put("t_type2", null);
            pama.put("t_addTime2", null);
            pama.put("t_status2", null);
            pama.put("show2", null);
            pama.put("t_orders_id2", null);
            pama.put("f_memberId1", null);
            pama.put("f_type1", null);
            pama.put("f_moneyType1", null);
            pama.put("f_money1", null);
            pama.put("f_addTime1", null);
            pama.put("f_currencyId1", null);
            pama.put("f_ip1", null);
            pama.put("f_content1", null);
            pama.put("f_memberId2", null);
            pama.put("f_type2", null);
            pama.put("f_moneyType2", null);
            pama.put("f_money2", null);
            pama.put("f_addTime2", null);
            pama.put("f_currencyId2", null);
            pama.put("f_ip2", null);
            pama.put("f_content2", null);
            pama.put("order_id_in", order_id);
        }
        pama.put("dealType", dealType);

        return yangOrdersMapper.dealOrder(pama);
    }

    public Result checkDayOrderLimt(String accessToken) {
        Result result = new Result();
        try {
            validateAccessToken(accessToken, result, yangMemberTokenService);
            if (result.getCode().intValue() == Result.Code.ERROR.intValue()) {
                return result;
            }
            YangMember yangMember = (YangMember) result.getData();

            if (yangMember.getIsBlackName() != null && CoinConst.IS_BLACK_MAN_NO == yangMember.getIsBlackName().intValue()) {
                result.setCode(Result.Code.SUCCESS);
                result.setData(null);
                return result;
            }

            String key = CoinConst.CREATE_ORDER_DAY_LIMIT_ + yangMember.getMemberId();
            //将rediskey  预先生成放入 result中
            result.setData(key);
            String dateNow = DateUtils.getDateStrPre(0);
            Long startTime = DateUtils.getTimeStamp(dateNow + " 0:0:0") / 1000;
            Long endTime = DateUtils.getTimeStamp(dateNow + "  23:59:59") / 1000;
            Long nowTime = DateUtils.getNowTimesLong();

            //不存在key值
            if (!redisService.exists(key)) {
                Example example = new Example(YangOrders.class);
                example.createCriteria().andEqualTo("memberId", yangMember.getMemberId())
                        .andLessThanOrEqualTo("addTime", endTime)
                        .andGreaterThanOrEqualTo("addTime", startTime);
                List<YangOrders> list = yangOrdersMapper.selectByExample(example);
                redisService.set(key, list == null ? 0 : list.size(), endTime - nowTime, TimeUnit.SECONDS);
            }
            int nums = (int) redisService.get(key);
            if (nums >= CoinConst.CREATE_ORDER_DAY_LIMIT_NUMS) {
                result.setCode(Result.Code.ERROR);
                result.setMsg(ErrorCode.ERROR_ORDER_DAY_LIMIT.getMessage());
                result.setErrorCode(ErrorCode.ERROR_ORDER_DAY_LIMIT.getIndex());
                return result;
            }

            result.setCode(Result.Code.SUCCESS);
            return result;

        } catch (Exception e) {
            e.printStackTrace();
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
            result.setCode(Result.Code.ERROR);
            result.setMsg(ErrorCode.ERROR_SYSTEM.getMessage());
        }
        return result;
    }

    //更新用户前五条委托记录
    public void updateRedis(int orderId) {
        YangOrders yangOrdersFind = new YangOrders();
        yangOrdersFind.setOrdersId(orderId);
        YangOrders yangOrders = yangOrdersMapper.selectByPrimaryKey(yangOrdersFind);

        String key = CoinConst.FONT_USER_ORDERS_FIVE_RECORDS + yangOrders.getCurrencyId() + "_" + yangOrders.getCurrencyTradeId() + "_" + yangOrders.getMemberId();
        Map pamaMap = new HashMap();
        pamaMap.put("currencyId", yangOrders.getCurrencyId());
        pamaMap.put("currencyTradeId", yangOrders.getCurrencyTradeId());
        pamaMap.put("memberId", yangOrders.getMemberId());
        List<YangOrders> userOrdersList = yangOrdersMapper.fontUserTradeFiveRecord(pamaMap);
        redisService.set(key, JSONObject.toJSONString(userOrdersList));

    }

    /**
     * 获取历史委托订单
     *
     * @return
     * @author ccw
     */
    public Result getHistoryOrdersByMemberId(String accessToken) {
        List<Map> yangOrders;
        //验证token
        Result result = new Result();
        String token = CoinConst.TOKENKEYTOKEN + accessToken;
        //验证token
        Object memberId = redisService.get(token);
        if (memberId == null) {
            result.setCode(Result.Code.ERROR);
            result.setErrorCode(ErrorCode.ERROR_ERROR_TOKEN_EXPIRE.getIndex());
            result.setMsg(ErrorCode.ERROR_ERROR_TOKEN_EXPIRE.getMessage());
            return result;
        }
        try {
            yangOrders = yangOrdersMapper.getHistoryOrdersByMemberId((Integer) memberId);
            result.setCode(Result.Code.SUCCESS);
            if (yangOrders.size() > 0) {
                result.setData(yangOrders);
                result.setMsg("获取历史订单成功");
            } else {
                result.setMsg("获取历史订单失败");
            }


        } catch (Exception e) {
            e.printStackTrace();
            result.setCode(Result.Code.ERROR);
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
            result.setMsg(ErrorCode.ERROR_SYSTEM.getMessage());
        }
        return result;
    }

    /**
     * 获取委托订单
     *
     * @return
     * @author ccw
     */
    public Result getOrdersByMemberId(String accessToken) {
        List<Map> yangOrders;
        //验证token
        Result result = new Result();
        String token = CoinConst.TOKENKEYTOKEN + accessToken;
        //验证token
        Object memberId = redisService.get(token);
        if (memberId == null) {
            result.setCode(Result.Code.ERROR);
            result.setErrorCode(ErrorCode.ERROR_ERROR_TOKEN_EXPIRE.getIndex());
            result.setMsg(ErrorCode.ERROR_ERROR_TOKEN_EXPIRE.getMessage());
            return result;
        }
        try {
            yangOrders = yangOrdersMapper.getOrdersByMemberId((Integer) memberId);
            result.setCode(Result.Code.SUCCESS);
            if (yangOrders.size() > 0) {
                result.setData(yangOrders);
                result.setMsg("获取订单成功");
            } else {
                result.setMsg("获取订单失败");
            }


        } catch (Exception e) {
            e.printStackTrace();
            result.setCode(Result.Code.ERROR);
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
            result.setMsg(ErrorCode.ERROR_SYSTEM.getMessage());
        }
        return result;
    }

    /**
     * 获取前5条委托记录总和（币币）BUY
     *
     * @param map
     * @return
     * @author ccw
     */
    public Result getUserOrderFiveRecordBuy(Map<String, String> map) {
        //验证token,可空
//        String accessToken = map.get("accessToken");
        String type = map.get("type");
//        String currencyTradeId = map.get("currencyTradeId");
        Result result = new Result();
        List<Map> datas = new ArrayList<Map>();
        try {
//            if (StringUtils.isNotBlank(accessToken)) {
//                //验证token
//                result = this.validateAccessToken(accessToken, result, yangMemberTokenService);
//                if (result.getData() == null) {
//                    result.setCode(Result.Code.ERROR);
//                    result.setData("");
//                    result.setErrorCode(ErrorCode.ERROR_ERROR_TOKEN_EXPIRE.getIndex());
//                    result.setMsg(ErrorCode.ERROR_ERROR_TOKEN_EXPIRE.getMessage());
//                    return result;
//                }
////                YangMember yangMember = (YangMember) result.getData();
//                //根据三个去查，然后price排序，返回bean
//                datas = yangOrdersMapper.getUserOrderFiveRecord(String type);
//
//            } else {
//                //不适用token
//                //根据二个去查，然后price排序，返回bean
//
//            }
            datas = yangOrdersMapper.getUserOrderFiveRecordBuy();
            if (datas.size() > 0) {
                for(Map<String,Object> data:datas){
                    data.put("price",data.get("price").toString());
                }
                result.setCode(Result.Code.SUCCESS);
                result.setData(datas);
                result.setMsg("获取前5条委托记录总和成功买");
            } else {
                result.setCode(Result.Code.ERROR);
                result.setData("");
                result.setMsg("获取前5条委托记录总和失败");
            }

        } catch (Exception e) {
            e.printStackTrace();
            result.setData("");
            result.setCode(Result.Code.ERROR);
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
            result.setMsg(ErrorCode.ERROR_SYSTEM.getMessage());
        }
        return result;
    }

    /**
     * 获取前5条委托记录总和（币币）SELL
     *
     * @param map
     * @return
     * @author ccw
     */
    public Result getUserOrderFiveRecordSell(Map<String, String> map) {
        //验证token,可空
//        String accessToken = map.get("accessToken");
//        String currencyTradeId = map.get("currencyTradeId");
        Result result = new Result();
        List<Map> datas = new ArrayList<Map>();
        try {
//            if (StringUtils.isNotBlank(accessToken)) {
//                //验证token
//                result = this.validateAccessToken(accessToken, result, yangMemberTokenService);
//                if (result.getData() == null) {
//                    result.setCode(Result.Code.ERROR);
//                    result.setData("");
//                    result.setErrorCode(ErrorCode.ERROR_ERROR_TOKEN_EXPIRE.getIndex());
//                    result.setMsg(ErrorCode.ERROR_ERROR_TOKEN_EXPIRE.getMessage());
//                    return result;
//                }
////                YangMember yangMember = (YangMember) result.getData();
//                //根据三个去查，然后price排序，返回bean
//                datas = yangOrdersMapper.getUserOrderFiveRecord(String type);
//
//            } else {
//                //不适用token
//                //根据二个去查，然后price排序，返回bean
//
//            }
            datas = yangOrdersMapper.getUserOrderFiveRecordSell();
            if (datas.size() > 0) {
                for(Map<String,Object> data:datas){
                    data.put("price",data.get("price").toString());
                }
                result.setCode(Result.Code.SUCCESS);
                result.setData(datas);
                result.setMsg("获取前5条委托记录总和成功卖");
            } else {
                result.setCode(Result.Code.ERROR);
                result.setData("");
                result.setMsg("获取前5条委托记录总和失败");
            }

        } catch (Exception e) {
            e.printStackTrace();
            result.setData("");
            result.setCode(Result.Code.ERROR);
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
            result.setMsg(ErrorCode.ERROR_SYSTEM.getMessage());
        }
        return result;
    }

    /**
     * k线处理中间空挡时间的数据
     *
     * @param datas addTime
     * @return
     * @author ccw
     */
    //k线处理中间空挡时间的数据
    public List<Map> dealEmpty(List<Map> datas, Integer addTime) {
        //拿出数组第一个和最后一个交易时间
        Integer first = Integer.valueOf(datas.get(0).get("trade_time").toString());
        Integer last = Integer.valueOf(datas.get(datas.size() - 1).get("trade_time").toString());
        //将数组集合中的所有交易时间拿出来，放入新的集合
        List<Integer> tradeTimes = new ArrayList<Integer>();
        //交易时间
        for (Map data : datas) {
            tradeTimes.add(Integer.valueOf(data.get("trade_time").toString()));
        }
        //生成完整的时间组
        Double inte = Double.valueOf(last) - Double.valueOf(first);
        Double step = Double.valueOf(addTime);
        Double fen = Math.floor(inte / step);
        Integer stepInit = 0;
        //间隔时间 开始 加上步进时间
        List<String> fullTimes = new ArrayList<>();
        for (int i = 0; i < fen; i++) {
            Integer ele = first + stepInit;
            fullTimes.add(ele.toString());
            stepInit += addTime;
        }
        List<Map> newDatas = new ArrayList<>();
        //遍历fullTIme
        int index = 0;
        for (int i = 0; i < fullTimes.size(); i++) {
            if (Integer.valueOf(fullTimes.get(i)).equals(tradeTimes.get(index))) {
                newDatas.add(i, datas.get(index));
                index++;
            } else {
                //无交易数据,查询前一笔数据
                Map prev = newDatas.get(newDatas.size() - 1);
                HashMap clone = new HashMap();
                clone.put("trade_id", prev.get("trade_id"));
                clone.put("trade_time", fullTimes.get(i));
                clone.put("num", 0);
                clone.put("open_price", prev.get("close_price"));
                clone.put("max_price", prev.get("close_price"));
                clone.put("min_price", prev.get("close_price"));
                clone.put("close_price", prev.get("close_price"));
                clone.put("count", prev.get("count"));
                clone.put("group_time", prev.get("group_time"));
                newDatas.add(i, clone);
            }

        }
        return newDatas;
    }


}
