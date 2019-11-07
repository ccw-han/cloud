package net.cyweb.service;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import cyweb.utils.CoinConst;
import cyweb.utils.CommonTools;

import net.cyweb.mapper.*;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Set;
import net.cyweb.model.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.web3j.abi.datatypes.Int;
import tk.mybatis.mapper.entity.Example;

import java.math.BigDecimal;
import java.util.*;

import static org.bouncycastle.asn1.x500.style.RFC4519Style.l;

@Service
@EnableAsync
public class YangOrderService extends BaseService<YangOrders> {
    @Autowired
    private YangMemberService yangMemberService;

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
    private RedisService redisService;

    private YangConfig getYangConfig(String key){
        YangConfig yangConfigQuery = new YangConfig();
        yangConfigQuery.setKey(key);
        YangConfig config = yangConfigMapper.selectOne(yangConfigQuery);
        if(null == config || StringUtils.isBlank(config.getValue())){
            throw new RuntimeException(key+" 配置不存在");
        }
        return config;
    }

    /*处理当前订单*/
    @Transactional
    public void deal(YangOrders yangOrdersFind) throws RuntimeException {


        if(null == invitOnerebateConfig){
            invitOnerebateConfig = getYangConfig("invit_onerebate");
        }
        if(null == invitMemberIdConfig){
            invitMemberIdConfig = getYangConfig("invit_member_id");
        }




        //判断当前挂单类型



        /*该订单未找到*/
        if(null == yangOrdersFind){
            throw new RuntimeException("此订单【"+yangOrdersFind.getOrdersId()+"】系统内未找到");
        }

        /*该订单为取消状态*/
        if(yangOrdersFind.getStatus().byteValue() == OrderStatus.ORDER_TYPE_CANCELED.getIndex()){
            System.out.println("此订单【"+yangOrdersFind.getOrdersId()+"】为取消状态");
            return;
        }

        /*该订单为已完成状态*/
        if(yangOrdersFind.getStatus().byteValue() == OrderStatus.ORDER_TYPE_FINISHED.getIndex()){
            System.out.println("此订单【"+yangOrdersFind.getOrdersId()+"】为已完成状态");
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
     *
     * @param yangOrders
     * @throws RuntimeException
     */
    @Transactional
    public void doOrder(YangOrders yangOrders) throws RuntimeException{

//        tradeKeys = CoinConst.REDIS_COIN_TRADE_SALE_+cy.getCurrencyId()+"_"+cy.getCurrencyTradeId();
        String keys = "";
        String key_from ;
        Set<ZSetOperations.TypedTuple<Object>> set;
        if(yangOrders.getType().equals(sellType)) //如果是卖单 则需要寻找价格复核条件的 从高到底的订单
        {
            keys = CoinConst.REDIS_COIN_TRADE_BUY_+yangOrders.getCurrencyId()+"_"+yangOrders.getCurrencyTradeId();

            key_from = CoinConst.REDIS_COIN_TRADE_SALE_ +yangOrders.getCurrencyId()+"_"+yangOrders.getCurrencyTradeId(); //来源单在redis中的数据

            set =  redisService.reverseRangeWithScores(keys,yangOrders.getPrice().longValue(),Long.MAX_VALUE);

        }else{ //如果是买单 则寻找价格从低到高的卖单

            keys = CoinConst.REDIS_COIN_TRADE_SALE_+yangOrders.getCurrencyId()+"_"+yangOrders.getCurrencyTradeId();

            key_from = CoinConst.REDIS_COIN_TRADE_BUY_ +yangOrders.getCurrencyId()+"_"+yangOrders.getCurrencyTradeId(); //来源单在redis中的数据


            set =  redisService.rangeByScoreWithScores(keys,0,yangOrders.getPrice().longValue());

        }

        Iterator iterator = set.iterator();

//        YangOrders yangOrders1;

        List<YangOrders> ordersList = new LinkedList();

        while (iterator.hasNext())
        {
            ZSetOperations.TypedTuple<Object> next = (ZSetOperations.TypedTuple)iterator.next();

            YangOrders yangOrders1 = JSONObject.parseObject(next.getValue().toString(),YangOrders.class);

            if(yangOrders1.getOrdersId() <= yangOrders.getOrdersId())
            {
                //把数据添加到redis中
                ordersList.add(yangOrders1);

            }else{
                    //大于自己的订单 就不要处理了
                continue;

            }



        }


        if(ordersList.size() == 0)  //如果没有数据  则不处理
        {
            return;
        }


        /*************订单排序****************/
        if(yangOrders.getType().equals(sellType)) //如果是卖单 获取的是买单 价格必须按照从高到底 并且按照时间正序
        {
            Collections.sort(ordersList, new Comparator<YangOrders>() {
                @Override
                public int compare(YangOrders o1, YangOrders o2) {

                    if(o1.getPrice().equals(o2.getPrice()))
                    {
                        return  o1.getAddTime() - o2.getAddTime();
                    }else{
                        return o2.getPrice().compareTo(o1.getPrice());
                    }

                }
            });
        }else{ //如果是卖单 获取的是买单 价格必须按照从底到高 并且按照时间正序
            Collections.sort(ordersList, new Comparator<YangOrders>() {
                @Override
                public int compare(YangOrders o1, YangOrders o2) {
                    if(o1.getPrice().equals(o2.getPrice()))
                    {
                        return  o1.getAddTime() - o2.getAddTime();
                    }else{
                        return o1.getPrice().compareTo(o2.getPrice());
                    }
                }
            });
        }
        /*************订单排序****************/

        //价格排序好了 现在开始循环处理

        BigDecimal nums_need = yangOrders.getNum(); //需要购买的数量




        long tradeTime = System.currentTimeMillis()/1000;
        boolean isOk =  false; //是否已经完成

        List<YangOrders> needUpdateList = new LinkedList<>(); //需要更新的订单数据 通过replace into 方式 批量更新
        List<YangTrade> yangTradesList = new LinkedList<>(); //需要加入的交易数据
        List<YangTradeFee> yangTradeFeeList = new LinkedList<>(); //需要插入的交易手续费数据

        //获取交易对基本信息
        YangCurrencyPair yangCurrencyPairFrom = yangPairService.getPairInfo(yangOrders.getCurrencyId(),yangOrders.getCurrencyTradeId());


        //获取用户手续费的默认配置
        YangTradeFee yangTradeFee = new YangTradeFee();
        yangTradeFee.setCyId(yangCurrencyPairFrom.getCyId());
        yangTradeFee.setMemberId(yangOrders.getMemberId());
        YangTradeFee yangTradeFeeFrom = this.getTradeFee(yangTradeFee);

        BigDecimal finishNumsTimes = BigDecimal.ZERO; //这次循环处理的数量


        //开启redis 事务


        for (YangOrders y :ordersList
             ) {

//            redisService.multi();

            BigDecimal numLeft = y.getNum().subtract(y.getTradeNum());

            //首先 把原来的数据 从队列中删掉 暂时没用知道更新的方法  先删除 后 添加
            redisService.zremove(keys,JSONObject.toJSON(y));

            if(!this.cleanBefore(key_from,yangOrders.getPrice().longValue(),yangOrders.getOrdersId()))
            {
                System.out.println("系统错误，没有删掉脏数据");break;
            }
//            redisService.zremove(key_from,JSONObject.toJSON(yangOrders)); //这里删掉的来源订单的数据

            if(nums_need.compareTo(BigDecimal.ZERO) == 0) //如果需要处理的数量都处理了 则结束本次循环
            {
                break;
            }


            if(numLeft.compareTo(nums_need) >= 0 )  //如果这个订单的数量满足并需求订单 则直接完成
            {
                if(numLeft.compareTo(nums_need) == 0)
                {
                    y.setStatus(OrderStatus.ORDER_TYPE_FINISHED.getIndex());
                }else{
                    y.setStatus(OrderStatus.ORDER_TYPE_PART_FINISHED.getIndex());
                }

                yangOrders.setStatus(OrderStatus.ORDER_TYPE_FINISHED.getIndex()); //来源订单数量就够啦 标记为已经完成的状态
                yangOrders.setTradeNum(yangOrders.getTradeNum().add(nums_need));



                y.setTradeNum(y.getTradeNum().add(nums_need));

                if(y.getStatus().byteValue() == OrderStatus.ORDER_TYPE_PART_FINISHED.getIndex()) //如果只是部分成交 则需要把数据继续写到redis中 以供后面使用
                {
                    redisService.zAdd(keys,JSONObject.toJSON(y),y.getPrice().doubleValue());

                }
                isOk = true;
                finishNumsTimes = nums_need;

            }else { //否则就继续循环 到下一个待处理订单处理
                finishNumsTimes = numLeft;
                nums_need = nums_need.subtract(numLeft);  //更新需要购买的数量
                y.setStatus(Byte.valueOf("2"));
                y.setTradeNum(y.getTradeNum().add(numLeft));  // 等于全部卖出去了

                yangOrders.setStatus(OrderStatus.ORDER_TYPE_PART_FINISHED.getIndex()); //来源订单数量就够啦 标记为已经完成的状态
                yangOrders.setTradeNum(yangOrders.getTradeNum().add(numLeft));

                redisService.zAdd(key_from,JSONObject.toJSON(yangOrders),yangOrders.getPrice().doubleValue()); //这里删掉的来源订单的数据


            }

            y.setTradeTime(tradeTime);
            yangOrders.setTradeTime(System.currentTimeMillis()/1000);

            long start_order  = System.currentTimeMillis();





            //把要更新的订单加了list 用来批量更新
//            needUpdateList.add(y);
            yangOrdersMapper.updateByPrimaryKey(y); //更新订单数据
            yangOrdersMapper.updateByPrimaryKey(yangOrders); //更新来源订单数据



            System.out.println("更新订单用时 : "+(System.currentTimeMillis() - start_order));


            //开始计算手续费
            long start_FEE  = System.currentTimeMillis();

            BigDecimal freeBuy; //订单发起者
            BigDecimal freeSell; //订单应答者

            YangTradeFee yangTradeFee1 = new YangTradeFee();
            yangTradeFee1.setCyId(yangCurrencyPairFrom.getCyId());
            yangTradeFee1.setMemberId(y.getMemberId());
            YangTradeFee yangTradeFeeTo = this.getTradeFee(yangTradeFee1);

            if(yangOrders.getType().equals(buyType)) // 如果是买单
            {
                if(null != yangTradeFeeFrom && null != yangTradeFeeFrom.getCurrencyBuyFee())
                {
                    freeBuy = finishNumsTimes.multiply(BigDecimal.valueOf(yangTradeFeeFrom.getCurrencyBuyFee())).divide(BigDecimal.valueOf(100));
                }else{
                    freeBuy = finishNumsTimes.multiply(yangCurrencyPairFrom.getCurrencyBuyFee()).divide(BigDecimal.valueOf(100));
                }

                if(null != yangTradeFeeTo && null != yangTradeFeeTo.getCurrencySellFee())
                {
                    freeSell = finishNumsTimes.multiply(BigDecimal.valueOf(yangTradeFeeTo.getCurrencySellFee()).multiply(y.getPrice()).divide(BigDecimal.valueOf(100)));
                }else{
                    freeSell = finishNumsTimes.multiply(yangCurrencyPairFrom.getCurrencySellFee()).multiply(y.getPrice()).divide(BigDecimal.valueOf(100));
                }

            }else{

                if(null != yangTradeFeeFrom && null != yangTradeFeeFrom. getCurrencySellFee())
                {
                    freeSell = finishNumsTimes.multiply(BigDecimal.valueOf(yangTradeFeeFrom.getCurrencySellFee())).multiply(y.getPrice()).divide(BigDecimal.valueOf(100));
                }else{
                    freeSell = finishNumsTimes.multiply(yangCurrencyPairFrom.getCurrencySellFee()).multiply(y.getPrice()).divide(BigDecimal.valueOf(100));
                }

                if(null != yangTradeFeeTo && null != yangTradeFeeTo.getCurrencyBuyFee())
                {
                    freeBuy = finishNumsTimes.multiply(BigDecimal.valueOf(yangTradeFeeTo.getCurrencyBuyFee()).divide(BigDecimal.valueOf(100)));
                }else{
                    freeBuy = finishNumsTimes.multiply(yangCurrencyPairFrom.getCurrencyBuyFee()).divide(BigDecimal.valueOf(100));
                }

            }



            long tradeInfotime = System.currentTimeMillis();


            if(yangOrders.getType().equals(buyType))
            {
                updateTradeInfo(yangOrders,y,finishNumsTimes,freeBuy,freeSell,y.getType());

            }else{
                updateTradeInfo(y,yangOrders,finishNumsTimes,freeBuy,freeSell,y.getType());

            }



            System.out.println("更新资产用时 : "+(System.currentTimeMillis() - tradeInfotime));




            System.out.println("更新手续费计算信息用时 :"+(System.currentTimeMillis() - start_FEE));


            //更新本次循环，买单卖单的用户资产
            int i = 0;
            if(yangOrders.getType().equals(buyType))
            {
                 i = updateUserMoney(

                         yangOrders.getMemberId(),yangOrders.getCurrencyId(),finishNumsTimes.subtract(freeBuy),"inc","normal" ,

                         y.getMemberId(),yangOrders.getCurrencyId(),finishNumsTimes,"dec","forzen",

                         yangOrders.getMemberId(),yangOrders.getCurrencyTradeId(),finishNumsTimes.multiply(yangOrders.getPrice()),"dec","forzen",

                         y.getMemberId(),yangOrders.getCurrencyTradeId(),finishNumsTimes.multiply(y.getPrice()).subtract(freeSell),"inc","normal",

                         yangOrders.getMemberId(),yangOrders.getCurrencyTradeId(),finishNumsTimes.multiply(yangOrders.getPrice().subtract(y.getPrice())),"inc","normal" //当卖价低于收购价的时候 要给买家把多余的钱加回去


                );
            }else{
                i = updateUserMoney(

                        y.getMemberId(),y.getCurrencyId(),finishNumsTimes.subtract(freeBuy),"inc","normal",

                        yangOrders.getMemberId(),yangOrders.getCurrencyId(),finishNumsTimes,"dec","forzen",

                        y.getMemberId(),y.getCurrencyTradeId(),finishNumsTimes.multiply(y.getPrice()),"dec","forzen",

                        yangOrders.getMemberId(),yangOrders.getCurrencyTradeId(),finishNumsTimes.multiply(yangOrders.getPrice()).subtract(freeSell),"inc","normal",

                        0, Integer.valueOf(0),null,null,null

                );
            }

            if(i == 1)
            {
                throw  new RuntimeException();
            }



            //记录交易记录，以备加入list批量新增 //后期实现 主要是怕数据的不一致性






            //循环处理用户的资金情况 这里耗时是瓶颈 除非全部金额变动都记录内存缓存 对数据一致性的要求就比较高了






            if(isOk)
            {
                break;
            }





        }




//        System.out.println(ordersList);
        //查询满足条件的订单 先按照订单的下单顺序进行排序

//        redisService.exec();



    }


    public boolean cleanBefore(String key ,long score,Integer orderids)
    {
        boolean isOk = false;

        long min = score;

        long max = score+1;


        Set<ZSetOperations.TypedTuple<Object>>  set =  redisService.rangeByScoreWithScores(key,score,max);

        Iterator iterator = set.iterator();


        while (iterator.hasNext() && !isOk)
        {
            ZSetOperations.TypedTuple<Object> next = (ZSetOperations.TypedTuple)iterator.next();

            YangOrders yangOrders1 = JSONObject.parseObject(next.getValue().toString(),YangOrders.class);

            if(yangOrders1.getOrdersId().equals(orderids))
            {
                //删除
                redisService.zremove(key,JSONObject.toJSON(yangOrders1)); //这里删掉的来源订单的数据
                isOk = true;

            }

        }

        return isOk;
    }

    /*处理买单*/
    public void dealBuyOrder(YangOrders yangOrdersSell) throws RuntimeException{
        /*查询所有符合条件的买单 (同类 价格)*/
        Example example = new Example(YangOrders.class);
        List<String> statusList = Lists.newArrayList();
        statusList.add("0");statusList.add("1");
        example.createCriteria().andEqualTo("currencyId",yangOrdersSell.getCurrencyId())
                .andEqualTo("currencyTradeId",yangOrdersSell.getCurrencyTradeId())
                .andEqualTo("type",buyType)
                .andIn("status",statusList)
                .andLessThan("ordersId",yangOrdersSell.getOrdersId())
                .andGreaterThanOrEqualTo("price",yangOrdersSell.getPrice());
        example.setOrderByClause("price desc,add_time asc");
        List<YangOrders> yangOrdersList = selectByExample(example); //查询到所有的符合条件的买单(1.单价大于卖单单价 2.找到最贵的买单单价 3.按时间发布排序)
        if(yangOrdersList.size() > 0){
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
            for(YangOrders yangOrdersBuy : yangOrdersList) { //循环对买单进行卖出操作
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


                if(sellLeftNum.compareTo(buyerLeftNum) == 0){ //卖单的剩余需求交易量 == 当前买单的剩余交易量
                    buyerOrderUpdate.setStatus(OrderStatus.ORDER_TYPE_FINISHED.getIndex());
                    sellerOrderUpdate.setStatus(OrderStatus.ORDER_TYPE_FINISHED.getIndex());
                    loopTradeNum = buyerLeftNum;
                    isNeedLoop = false;
                }else if(sellLeftNum.compareTo(buyerLeftNum) == -1){ //卖单的剩余需求交易量 < 当前买单的剩余交易量
                    //卖单为已完成，买单继续走流程
                    buyerOrderUpdate.setStatus(OrderStatus.ORDER_TYPE_PART_FINISHED.getIndex());
                    sellerOrderUpdate.setStatus(OrderStatus.ORDER_TYPE_FINISHED.getIndex());
                    loopTradeNum = sellLeftNum;
                    isNeedLoop = false;
                }else if(sellLeftNum.compareTo(buyerLeftNum) == 1){ //卖单的剩余需求交易量 > 当前买单的剩余交易量
                    //买单流程结束，卖单继续走流程
                    buyerOrderUpdate.setStatus(OrderStatus.ORDER_TYPE_FINISHED.getIndex());
                    sellerOrderUpdate.setStatus(OrderStatus.ORDER_TYPE_PART_FINISHED.getIndex());
                    loopTradeNum = buyerLeftNum;
                }




                //计算本次买单的手续费(币数量)
                BigDecimal buyerTradeFeeNum = new BigDecimal(0);

                if(yangOrdersBuy.getIsShua().intValue() == 1)
                {
                    buyerTradeFeeNum =  BigDecimal.ZERO;
                }else{
                    if(null != buyerTradeFeeConf && null != buyerTradeFeeConf.getCurrencyBuyFee()){
                        buyerTradeFeeNum = loopTradeNum.multiply(new BigDecimal(buyerTradeFeeConf.getCurrencyBuyFee())).divide(new BigDecimal(100));
                    }else{
                        //按币种的费率走
                        buyerTradeFeeNum = loopTradeNum.multiply(yangCurrencyPairFind.getCurrencyBuyFee()).divide(new BigDecimal(100));
                    }
                }




                BigDecimal sellerTradeFeePrice = new BigDecimal(0);
                if(yangOrdersSell.getIsShua().intValue() == 1)
                {
                    sellerTradeFeePrice = BigDecimal.ZERO;
                }else{
                    //计算本次卖单的手续费(price)
                    if(null != sellerTradeFeeConf && null != sellerTradeFeeConf.getCurrencySellFee()){
                        sellerTradeFeePrice = loopTradeNum.multiply(yangOrdersSell.getPrice()).multiply(new BigDecimal(sellerTradeFeeConf.getCurrencySellFee())).divide(new BigDecimal(100));
                    }else{
                        //按币种的费率走
                        sellerTradeFeePrice = loopTradeNum.multiply(yangOrdersSell.getPrice()).multiply(yangCurrencyPairFind.getCurrencySellFee()).divide(new BigDecimal(100));
                    }
                }


                //更新本次的循环的订单状态、已交易数量 orders
                buyerOrderUpdate.setTradeTime(System.currentTimeMillis()/1000); //买单更新交易时间
                buyerOrderUpdate.setTradeNum(yangOrdersBuy.getTradeNum().add(loopTradeNum)); //买单更新本次交易币数量(增加已交易数量)
                yangOrdersMapper.updateByPrimaryKeySelective(buyerOrderUpdate);
                sellerOrderUpdate.setTradeTime(System.currentTimeMillis()/1000); //卖单更新交易时间
                sellerOrderUpdate.setTradeNum(sellerOrderUpdate.getTradeNum().add(loopTradeNum)); //卖单更新本次交易币数量(增加已交易数量)
                yangOrdersMapper.updateByPrimaryKeySelective(sellerOrderUpdate);


                //新增本次循环交易记录 trade
                updateTradeInfo(yangOrdersBuy,yangOrdersSell,loopTradeNum,buyerTradeFeeNum,sellerTradeFeePrice,"buy");

                //更新本次循环返利交易记录
                rebate(yangOrdersBuy,buyerTradeFeeNum);
                rebate(yangOrdersSell,sellerTradeFeePrice);

                //更新本次循环卖单剩余需求量
                sellLeftNum = sellLeftNum.subtract(loopTradeNum);


                //更新本次循环，买单卖单的用户资产
//                updateUserMoney(yangOrdersBuy.getMemberId(),yangOrdersBuy.getCurrencyId(),loopTradeNum.subtract(buyerTradeFeeNum),"inc","normal" ); //买单加币(扣除交易费用)
//                updateUserMoney(yangOrdersSell.getMemberId(),yangOrdersBuy.getCurrencyId(),loopTradeNum,"dec","forzen" ); //卖单扣币 (扣除冻结金额)

                int i = updateUserMoney(
                        yangOrdersBuy.getMemberId(),yangOrdersBuy.getCurrencyId(),loopTradeNum.subtract(buyerTradeFeeNum),"inc","normal",
                        yangOrdersSell.getMemberId(),yangOrdersBuy.getCurrencyId(),loopTradeNum,"dec","forzen",
                        yangOrdersBuy.getMemberId(),yangOrdersBuy.getCurrencyTradeId(),loopTradeNum.multiply(yangOrdersSell.getPrice()),"dec","forzen",
                        yangOrdersSell.getMemberId(),yangOrdersBuy.getCurrencyTradeId(),loopTradeNum.multiply(yangOrdersSell.getPrice()).subtract(sellerTradeFeePrice),"inc","normal",
                        0, Integer.valueOf(0),null,null,null

                );

                if(i == 1)
                {
                    throw  new RuntimeException();
                }











//                updateUserMoney(yangOrdersBuy.getMemberId(),yangOrdersBuy.getCurrencyTradeId(),loopTradeNum.multiply(yangOrdersSell.getPrice()),"dec","forzen" );
//                updateUserMoney(yangOrdersSell.getMemberId(),yangOrdersBuy.getCurrencyTradeId(),loopTradeNum.multiply(yangOrdersSell.getPrice()).subtract(sellerTradeFeePrice),"inc","normal" ); //卖家加钱(扣除手续费)


//                updateUserMoney(
//                        yangOrdersBuy.getMemberId(),yangOrdersBuy.getCurrencyTradeId(),loopTradeNum.multiply(yangOrdersSell.getPrice()),"dec","forzen",
//                        yangOrdersSell.getMemberId(),yangOrdersBuy.getCurrencyTradeId(),loopTradeNum.multiply(yangOrdersSell.getPrice()).subtract(sellerTradeFeePrice),"inc","normal"
//                );






                if(isNeedLoop == false){ //买单流程走结束了
                    break;
                }

            }

        }
    }

    /*处理卖单*/
    @Transactional
    public void dealSellOrder(YangOrders yangOrdersBuy) throws RuntimeException{

        /*查询所有符合条件的卖单*/
        Example example = new Example(YangOrders.class);
        List<String> statusList = Lists.newArrayList();
        statusList.add("0");statusList.add("1");
        example.createCriteria()
                .andEqualTo("currencyId",yangOrdersBuy.getCurrencyId())
                .andEqualTo("currencyTradeId",yangOrdersBuy.getCurrencyTradeId())
                .andEqualTo("type",sellType)
                .andLessThan("ordersId",yangOrdersBuy.getOrdersId())
                .andIn("status",statusList)
                .andLessThanOrEqualTo("price",yangOrdersBuy.getPrice());
        example.setOrderByClause("price asc,add_time asc");
        List<YangOrders> yangOrdersList = selectByExample(example); //查询到所有的符合条件的卖单(1.单价小于买单单价 2.找到最便宜的卖单单价 3.按时间发布排序)
        if(yangOrdersList.size() > 0){

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
            for(YangOrders yangOrdersSell : yangOrdersList){ //循环对卖单进行买入操作

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
                if(sellLeftNum.compareTo(buyNeedNum) == 0){ //当前卖单的剩余交易量 == 当前买单的需求交易量
                    //买单和卖单均为已完成
                    buyerOrderUpdate.setStatus(OrderStatus.ORDER_TYPE_FINISHED.getIndex());
                    sellerOrderUpdate.setStatus(OrderStatus.ORDER_TYPE_FINISHED.getIndex());
                    loopTradeNum = buyNeedNum;
                    isNeedLoop = false;
                }else if(sellLeftNum.compareTo(buyNeedNum) == -1){ //当前卖单的剩余交易量 < 当前买单的需求交易量
                    //卖单为已完成，买单继续走流程
                    buyerOrderUpdate.setStatus(OrderStatus.ORDER_TYPE_PART_FINISHED.getIndex());
                    sellerOrderUpdate.setStatus(OrderStatus.ORDER_TYPE_FINISHED.getIndex());
                    loopTradeNum = sellLeftNum;

                }else if(sellLeftNum.compareTo(buyNeedNum) == 1){ // 当前卖单的剩余交易量 > 当前买单的需求交易量
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
                if(yangOrdersBuy.getIsShua().intValue() == 1)
                {
                    buyerTradeFeeNum =  BigDecimal.ZERO;

                }else{
                    if(null != buyerTradeFeeConf && null != buyerTradeFeeConf.getCurrencyBuyFee()){
                        buyerTradeFeeNum = loopTradeNum.multiply(new BigDecimal(buyerTradeFeeConf.getCurrencyBuyFee())).divide(new BigDecimal(100));
                    }else{
                        //按币种的费率走
                        buyerTradeFeeNum = loopTradeNum.multiply(yangCurrencyPairFind.getCurrencyBuyFee()).divide(new BigDecimal(100));
                    }

                }

                //机器人刷单  如果是系统平台的 isshua = 1 不收取手续费 如果通过api生成的订单 收取手续费 isshua=2
                if(yangOrdersSell.getIsShua().intValue() == 1)
                {
                    sellerTradeFeePrice = BigDecimal.ZERO;
                }else{
                    //计算本次卖单的手续费(price)

                    if(null != sellerTradeFeeConf && null != sellerTradeFeeConf.getCurrencySellFee()){
                        sellerTradeFeePrice = loopTradeNum.multiply(yangOrdersSell.getPrice()).multiply(new BigDecimal(sellerTradeFeeConf.getCurrencySellFee())).divide(new BigDecimal(100));
                    }else{
                        //按交易对的默认费率走
                        sellerTradeFeePrice = loopTradeNum.multiply(yangOrdersSell.getPrice()).multiply(yangCurrencyPairFind.getCurrencySellFee()).divide(new BigDecimal(100));
                    }
                }




                //更新本次的循环的订单状态、已交易数量 orders
                buyerOrderUpdate.setTradeTime(System.currentTimeMillis()/1000); //买单更新交易时间
                buyerOrderUpdate.setTradeNum(buyerOrderUpdate.getTradeNum().add(loopTradeNum)); //买单更新本次交易币数量(增加已交易数量)
                yangOrdersMapper.updateByPrimaryKeySelective(buyerOrderUpdate);
                sellerOrderUpdate.setTradeTime(System.currentTimeMillis()/1000); //卖单更新交易时间
                sellerOrderUpdate.setTradeNum(yangOrdersSell.getTradeNum().add(loopTradeNum)); //卖单更新本次交易币数量(增加已交易数量)
                yangOrdersMapper.updateByPrimaryKeySelective(sellerOrderUpdate);



                updateTradeInfo(yangOrdersBuy,yangOrdersSell,loopTradeNum,buyerTradeFeeNum,sellerTradeFeePrice,"sell");

                //更新本次循环返利交易记录
                rebate(yangOrdersBuy,buyerTradeFeeNum);
                rebate(yangOrdersSell,sellerTradeFeePrice);


                //更新本次循环，买单卖单的用户资产
                int i = updateUserMoney(
                        yangOrdersBuy.getMemberId(),yangOrdersBuy.getCurrencyId(),loopTradeNum.subtract(buyerTradeFeeNum),"inc","normal" ,
                        yangOrdersSell.getMemberId(),yangOrdersBuy.getCurrencyId(),loopTradeNum,"dec","forzen",
                        yangOrdersBuy.getMemberId(),yangOrdersBuy.getCurrencyTradeId(),loopTradeNum.multiply(yangOrdersSell.getPrice()),"dec","forzen",
                        yangOrdersSell.getMemberId(),yangOrdersBuy.getCurrencyTradeId(),loopTradeNum.multiply(yangOrdersSell.getPrice()).subtract(sellerTradeFeePrice),"inc","normal",

                        yangOrdersBuy.getMemberId(),yangOrdersBuy.getCurrencyTradeId(),loopTradeNum.multiply(yangOrdersBuy.getPrice().subtract(yangOrdersSell.getPrice())),"inc","normal" //当卖价低于收购价的时候 要给买家把多余的钱加回去


                );
                if(i == 1)
                {
                    throw  new RuntimeException();
                }

                //买单加币(扣除交易费用)
//                updateUserMoney(yangOrdersSell.getMemberId(),yangOrdersBuy.getCurrencyId(),loopTradeNum,"dec","forzen" ); //卖单扣币 (扣除冻结金额)



//                updateUserMoney(yangOrdersBuy.getMemberId(),yangOrdersBuy.getCurrencyTradeId(),loopTradeNum.multiply(yangOrdersSell.getPrice()),"dec","forzen" ); //买家扣钱(扣除冻结金额)
//                updateUserMoney(yangOrdersSell.getMemberId(),yangOrdersBuy.getCurrencyTradeId(),loopTradeNum.multiply(yangOrdersSell.getPrice()).subtract(sellerTradeFeePrice),"inc","normal" ); //卖家加钱(扣除手续费)





                //更新本次循环买单剩余需求量
                buyNeedNum = buyNeedNum.subtract(loopTradeNum);

                if(isNeedLoop == false){ //买单流程走结束了
                    break;
                }
            }
        }

    }



    /**
     *
     * @param currency_id
     * @param currency_trade_id
     * @return
     */
    private CoinPairRedisBean initCoinInfos(int currency_id,int currency_trade_id,String key)
    {
        CoinPairRedisBean coinPairRedisBean = new CoinPairRedisBean();

        try {

            if(redisService.hmGet(key,"cy_id") == null)
            {
                coinPairRedisBean.setCy_id(0);
                return coinPairRedisBean;
            }

            coinPairRedisBean.setCy_id(Integer.valueOf(redisService.hmGet(key,"cy_id").toString()).intValue());

            coinPairRedisBean.setCurrency_id(currency_id);

            coinPairRedisBean.setCurrency_trade_id(currency_trade_id);

            coinPairRedisBean.setCurrency_logo((String) redisService.hmGet(key,"currency_logo"));

            coinPairRedisBean.setTrade_logo((String)redisService.hmGet(key,"trade_logo"));

            coinPairRedisBean.setInput_price_num(CommonTools.formatNull2BigDecimal(redisService.hmGet(key,"input_price_num")));

            coinPairRedisBean.setRate_num(CommonTools.formatNull2int(redisService.hmGet(key,"rate_num")));

            coinPairRedisBean.setCurrency_mark((String) redisService.hmGet(key,"currency_mark"));

            coinPairRedisBean.setTrade_mark((String) redisService.hmGet(key,"trade_mark"));

            coinPairRedisBean.setIs_chuang((String)redisService.hmGet(key,"is_chuang"));

            coinPairRedisBean.setIs_old((String)redisService.hmGet(key,"is_old"));

            coinPairRedisBean.setNew_price(CommonTools.formatNull2BigDecimal(redisService.hmGet(key,"new_price")));


            coinPairRedisBean.setPrice_status(CommonTools.formatNull2int(redisService.hmGet(key,"price_status")));


            //                    'input_price_num'=>$cy->input_price_num,//价格显示位数
//                    'rate_num'=>$cy->rate_num,//汇率转换后的位数
//                    'is_chuang'=>$cy->is_chuang,

            coinPairRedisBean.setInput_price_num(CommonTools.formatNull2BigDecimal(redisService.hmGet(key,"input_price_num")));


            coinPairRedisBean.setRate_num(CommonTools.formatNull2int(redisService.hmGet(key,"rate_num")));


            coinPairRedisBean.setIs_chuang((String)redisService.hmGet(key,"is_chuang"));

            coinPairRedisBean.setIs_old((String)redisService.hmGet(key,"is_old"));





            if(currency_trade_id != 0)  //实时获取这个币种的krw价格
            {
                BigDecimal krwprice = BigDecimal.ZERO;
                try {
                    Object krwPriceObject = redisService.hmGet(CoinConst.REDIS_COIN_KRW_PRICE_,String.valueOf(currency_trade_id));
                    if(krwPriceObject != null)
                    {
                        krwprice = BigDecimal.valueOf(Double.valueOf(krwPriceObject.toString()));
                    }
                    coinPairRedisBean.setKrw_price(krwprice);
                }catch (Exception e)
                {
                    coinPairRedisBean.setKrw_price(krwprice);

                    System.out.println("找不到该币种 krw"+currency_trade_id);
                }
            }else{
                coinPairRedisBean.setKrw_price(BigDecimal.valueOf(1));
            }


            //获取24小时的数据
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
            String currentDayKey = dateFormat.format(new Date());
            String key_24h = CoinConst.REDIS_YANG_PARI_DAY_INFO_+currency_id+"-"+currency_trade_id+"_"+currentDayKey;
            String key_24h_base = CoinConst.REDIS_YANG_PARI_DAY_INFO_+currency_id+"-"+currency_trade_id+"_*";

            if(!redisService.keysExist(key_24h))
            {
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
                if(redisService.hmGet(key_24h,"h24_change") == null)
                {
                    coinPairRedisBean.setH24_change(Double.valueOf(0.00));
                }else{
                    java.text.DecimalFormat   df   = new   java.text.DecimalFormat("#.00");
                    String change_24s =  df.format(BigDecimal.valueOf(Double.valueOf(redisService.hmGet(key_24h,"h24_change").toString())).multiply(BigDecimal.valueOf(100)));
                    coinPairRedisBean.setH24_change(Double.valueOf(change_24s));
                }
            }catch (Exception e)
            {
                coinPairRedisBean.setH24_change(Double.valueOf(0.00));
                e.printStackTrace();
            }


            if(redisService.hmGet(key_24h,"h24_num")==null)
            {
                coinPairRedisBean.setH24_num("0");
            }else{
                coinPairRedisBean.setH24_num((String.valueOf(redisService.hmGet(key_24h,"h24_num"))));

            }

//            coinPairRedisBean.setH24_num((String.valueOf(redisService.hmGet(key_24h,"h24_num"))));


            if(redisService.hmGet(key_24h,"h24_money")==null)
            {
                coinPairRedisBean.setH24_money("0");
            }else{
                coinPairRedisBean.setH24_money((String.valueOf(redisService.hmGet(key_24h,"h24_money"))));

            }

            if(redisService.hmGet(key_24h,"h24_max")==null)
            {
                coinPairRedisBean.setH24_max("0");
            }else{
                coinPairRedisBean.setH24_max((String.valueOf(redisService.hmGet(key_24h,"h24_max"))));

            }

            if(redisService.hmGet(key_24h,"h24_min")==null)
            {
                coinPairRedisBean.setH24_min("0");
            }else{
                coinPairRedisBean.setH24_min((String.valueOf(redisService.hmGet(key_24h,"h24_min"))));

            }





            if(redisService.hmGet(key_24h,"price_status")==null)
            {
                coinPairRedisBean.setPrice_status(0);
            }else{
                coinPairRedisBean.setPrice_status(Integer.valueOf(String.valueOf(redisService.hmGet(key_24h,"price_status"))));

            }

            if(redisService.hmGet(key_24h,"new_price") == null)
            {
                coinPairRedisBean.setNew_price(BigDecimal.valueOf(0));
            }else{
                coinPairRedisBean.setNew_price(new BigDecimal(String.valueOf(redisService.hmGet(key_24h,"new_price"))));
            }






        }catch (Exception e)
        {
            e.printStackTrace();
        }


        return  coinPairRedisBean;

    }



    /**
     * 更新交易对信息
     * @return
     */
    private boolean flushCoinInfos(int currency_id,int currency_trade_id,YangTrade yangTrade) {
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

        if(yangTrade.getPrice() == null)
        {
            coinPairRedisBean.setNew_price(BigDecimal.ZERO);

        }else{
            coinPairRedisBean.setNew_price(yangTrade.getPrice());

        }


//        long time_now = System.currentTimeMillis(); //当前时间戳
//        redisService.zAdd(CoinConst.REDIS_TREAD_24_ + coinPairRedisBean.getCurrency_id() + "-" + coinPairRedisBean.getCurrency_trade_id(), JSONObject.toJSON(yangTrade), time_now);

        redisService.hmSet(key,"cy_id", CommonTools.formatNull2String(String.valueOf(coinPairRedisBean.getCy_id())));


        redisService.hmSet(key,"currency_id", CommonTools.formatNull2String(String.valueOf(coinPairRedisBean.getCurrency_id())));
        redisService.hmSet(key,"currency_trade_id", CommonTools.formatNull2String(String.valueOf(coinPairRedisBean.getCurrency_trade_id())));
        redisService.hmSet(key,"currency_logo", CommonTools.formatNull2String(coinPairRedisBean.getCurrency_logo()));
        redisService.hmSet(key,"trade_logo", CommonTools.formatNull2String(coinPairRedisBean.getTrade_logo()));
        redisService.hmSet(key,"currency_mark", CommonTools.formatNull2String(coinPairRedisBean.getCurrency_mark()));
        redisService.hmSet(key,"trade_mark", CommonTools.formatNull2String(coinPairRedisBean.getTrade_mark()));
        redisService.hmSet(key,"new_price", CommonTools.formatNull2String(coinPairRedisBean.getNew_price().toString()));
        redisService.hmSet(key,"price_status", CommonTools.formatNull2String(String.valueOf(coinPairRedisBean.getPrice_status())));
        redisService.hmSet(key,"is_old", CommonTools.formatNull2String(String.valueOf(coinPairRedisBean.getIs_chuang())));
        redisService.hmSet(key,"is_chuang", CommonTools.formatNull2String(String.valueOf(coinPairRedisBean.getIs_old())));

//        redisService.hmSet(key,"is_old", CommonTools.formatNull2String(String.valueOf(getYangCurrencyPair.getIsOld()));





        //这三个后台更新的时候 修改对应的值


//                    'input_price_num'=>$cy->input_price_num,//价格显示位数
//                    'rate_num'=>$cy->rate_num,//汇率转换后的位数
//                    'is_chuang'=>$cy->is_chuang,




        return  true;




    }


    /**
     * 获取交易对信息
     */
    public CoinPairRedisBean getCoinPairRedisBean(int currency_id,int currency_trade_id) {


        String key = CoinConst.REDIS_COIN_PAIR_BASE_+currency_id+"-"+currency_trade_id;

        CoinPairRedisBean coinPairRedisBean = this.initCoinInfos(currency_id,currency_trade_id,key);


        return coinPairRedisBean;


    }







    /*更新交易信息*/
    @Transactional
    public void updateTradeInfo(YangOrders yangOrdersBuy,YangOrders yangOrdersSell,BigDecimal loopTradeNum,BigDecimal buyerTradeFeeNum,BigDecimal sellerTradeFeePrice,String type){
        //新增本次循环交易记录 trade
        Integer isShua = 1; //刷单标记
        Integer isShua1 = 1; //刷单标记

        BigDecimal price_final;

        price_final = yangOrdersSell.getPrice();


        if(type.equals("sell")) //如果处理的是卖单，
        {
            isShua1 = 0;
            isShua = 1;
        }else{
            isShua1 = 1;
            isShua = 0;
        }

        YangTrade buyerTradeLog = new YangTrade();
        buyerTradeLog.setAddTime(System.currentTimeMillis()/1000);
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
        buyerTradeLog.setTradeNo("T"+System.currentTimeMillis()/1000);
        yangTradeMapper.insertSelective(buyerTradeLog);


        YangTrade sellerTradeLog = new YangTrade();
        sellerTradeLog.setAddTime(System.currentTimeMillis()/1000);
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
        sellerTradeLog.setTradeNo("T"+System.currentTimeMillis()/1000);
        yangTradeMapper.insertSelective(sellerTradeLog);


        //更新redis数据
        this.flushCoinInfos(buyerTradeLog.getCurrencyId(),buyerTradeLog.getCurrencyTradeId(),buyerTradeLog);



        //更新本次循环手续费交易记录
        YangFinance buyerFinance = new YangFinance(); //买单手续费
        buyerFinance.setAddTime(System.currentTimeMillis()/1000);
        buyerFinance.setMemberId(yangOrdersBuy.getMemberId());
        buyerFinance.setType((byte) 11);
        buyerFinance.setContent("交易手续费");
        buyerFinance.setMoney(buyerTradeFeeNum);
        buyerFinance.setMoneyType((byte)2);
        buyerFinance.setCurrencyId(yangOrdersBuy.getCurrencyId());
        buyerFinance.setIp("127.0.0.1");
        yangFinanceMapper.insertSelective(buyerFinance);

        YangFinance sellerFinance = new YangFinance(); //卖单手续费
        sellerFinance.setAddTime(System.currentTimeMillis()/1000);
        sellerFinance.setMemberId(yangOrdersSell.getMemberId());
        sellerFinance.setType((byte) 11);
        sellerFinance.setContent("交易手续费");
        sellerFinance.setMoney(sellerTradeFeePrice);
        sellerFinance.setMoneyType((byte)2);
        sellerFinance.setCurrencyId(yangOrdersBuy.getCurrencyTradeId());
        sellerFinance.setIp("127.0.0.1");
        yangFinanceMapper.insertSelective(sellerFinance);




    }

    /*获取买家卖家的自定义手续费配置*/
    public YangTradeFee getTradeFee(YangTradeFee yangTradeFee){
        String key  = CoinConst.REDIS_TRADE_FEE_MEMBER_ + yangTradeFee.getMemberId()+"_"+yangTradeFee.getCyId();
        if(redisService.get(key) != null)
        {
            return JSONObject.parseObject(redisService.get(key).toString(),YangTradeFee.class);

        }else{
            YangTradeFee yangTradeFee1 = yangTradeFeeMapper.selectOne(yangTradeFee);
            if(yangTradeFee1 != null)
            {
                redisService.set(key,JSONObject.toJSON(yangTradeFee1).toString());
            }
            return yangTradeFee1;

        }
    }


    public int updateUserMoney(
            Integer memberId,Integer currencyId,BigDecimal num,String type,String field,
            Integer memberId1,Integer currencyId1,BigDecimal num1,String type1,String field1,
            Integer memberId2,Integer currencyId2,BigDecimal num2,String type2,String field2,
            Integer memberId3,Integer currencyId3,BigDecimal num3,String type3,String field3,

            Integer memberId4,Integer currencyId4,BigDecimal num4,String type4,String field4  //应为有的时候 购买的金额 比自己挂单的价格要底 需要把多余冻结金额返还给账户余额

    )
    {

////                call assets(3211,'0',125.001,'dec',56.89,'dec');
//        Integer i = yangMemberService.assets(3211,"0",BigDecimal.valueOf(125.001),"dec",BigDecimal.valueOf(56.89),"dec");
//        System.out.println(i);



        BigDecimal num_tmp;
        BigDecimal forzen_tmp;


        if(field != null && field.equals("normal"))
        {
            num_tmp = num;
            forzen_tmp = BigDecimal.ZERO;
        }else{
            num_tmp = BigDecimal.ZERO;
            forzen_tmp = num;
        }

        BigDecimal num1_tmp;
        BigDecimal forzen1_tmp;
        if(field1 != null && field1.equals("normal"))
        {
            num1_tmp = num1;
            forzen1_tmp = BigDecimal.ZERO;
        }else{
            num1_tmp = BigDecimal.ZERO;
            forzen1_tmp = num1;
        }


        BigDecimal num2_tmp;
        BigDecimal forzen2_tmp;
        if(field2 != null && field2.equals("normal"))
        {
            num2_tmp = num2;
            forzen2_tmp = BigDecimal.ZERO;
        }else{
            num2_tmp = BigDecimal.ZERO;
            forzen2_tmp = num2;
        }


        BigDecimal num3_tmp;
        BigDecimal forzen3_tmp;
        if(field3 != null && field3.equals("normal"))
        {
            num3_tmp = num3;
            forzen3_tmp = BigDecimal.ZERO;
        }else{
            num3_tmp = BigDecimal.ZERO;
            forzen3_tmp = num3;
        }

        BigDecimal num4_tmp;
        BigDecimal forzen4_tmp;
        if(field4 != null && field4.equals("normal"))
        {
            num4_tmp = num4;
            forzen4_tmp = BigDecimal.ZERO;
        }else{
            num4_tmp = BigDecimal.ZERO;
            forzen4_tmp = num4;
        }


        return yangMemberService.assets(
                memberId,String.valueOf(currencyId.intValue()),num_tmp,type,forzen_tmp,type,
                memberId1,String.valueOf(currencyId1.intValue()),num1_tmp,type1,forzen1_tmp,type1,
                memberId2,String.valueOf(currencyId2.intValue()),num2_tmp,type2,forzen2_tmp,type2,
                memberId3,String.valueOf(currencyId3.intValue()),num3_tmp,type3,forzen3_tmp,type3,
                memberId4,String.valueOf(currencyId4.intValue()),num4_tmp,type4,forzen4_tmp,type4
        );


    }


    //    /*更新用户资产*/
    @Async
    @Transactional
    public void updateUserMoneyBack(Integer memberId,Integer currencyId,BigDecimal num,String type,String field){
        String lock = memberId.toString()+"-"+currencyId.toString();
        synchronized (lock.intern())
        {
            checkCurrency(memberId,currencyId); //保证有资产记录
            if(currencyId == 0){
                YangMember yangMemberQuery = new YangMember();
                yangMemberQuery.setMemberId(memberId);
                YangMember yangMemberFind = yangMemberMapper.selectOne(yangMemberQuery);

                YangMember yangMemberUpdate = new YangMember();
                yangMemberUpdate.setMemberId(memberId);

                if(type.equals("inc")){
                    if(field.equals("normal")){
                        yangMemberUpdate.setRmb(yangMemberFind.getRmb().add(num));
                    }else if(field.equals("forzen")){
                        yangMemberUpdate.setForzenRmb(yangMemberFind.getForzenRmb().add(num));
                    }

                }else if(type.equals("dec")){
                    if(field.equals("normal")){
                        yangMemberUpdate.setRmb(yangMemberFind.getRmb().subtract(num));
                    }else if(field.equals("forzen")){
                        yangMemberUpdate.setForzenRmb(yangMemberFind.getForzenRmb().subtract(num));
                    }
                }

                yangMemberMapper.updateByPrimaryKeySelective(yangMemberUpdate);


            }else{
                YangCurrencyUser yangCurrencyUserQuery = new YangCurrencyUser();
                yangCurrencyUserQuery.setCurrencyId(currencyId);
                yangCurrencyUserQuery.setMemberId(memberId);
                YangCurrencyUser yangCurrencyUserFind = yangCurrencyUserMapper.selectOne(yangCurrencyUserQuery);

                YangCurrencyUser yangCurrencyUserUpdate = new YangCurrencyUser();
                yangCurrencyUserUpdate.setCuId(yangCurrencyUserFind.getCuId());

                if(type.equals("inc")){
                    if(field.equals("normal")){
                        yangCurrencyUserUpdate.setNum(yangCurrencyUserFind.getNum().add(num));
                    }else if(field.equals("forzen")){
                        yangCurrencyUserUpdate.setForzenNum(yangCurrencyUserFind.getForzenNum().add(num));
                    }
                }else if(type.equals("dec")){
                    if(field.equals("normal")){
                        yangCurrencyUserUpdate.setNum(yangCurrencyUserFind.getNum().subtract(num));
                    }else if(field.equals("forzen")){
                        yangCurrencyUserUpdate.setForzenNum(yangCurrencyUserFind.getForzenNum().subtract(num));
                    }
                }

                yangCurrencyUserMapper.updateByPrimaryKeySelective(yangCurrencyUserUpdate);
            }
        }

    }




    /*挂单返利*/
    public void rebate(YangOrders yangOrder,BigDecimal tradeFeeNumOrPrice){
//    public void rebate(YangOrders yangOrdersBuy,YangOrders yangOrdersSell,BigDecimal loopTradeNum,BigDecimal buyerTradeFeeNum,BigDecimal sellerTradeFeePrice){
        //查询一级返利
        BigDecimal rebateMoney =  tradeFeeNumOrPrice.multiply(new BigDecimal(invitOnerebateConfig.getValue())).divide(new BigDecimal("100"));
        //查询买单的推荐用户
        YangMember yangMemberQuery = new YangMember();
        Example example = new Example(YangMember.class);
        example.createCriteria()
                .andEqualTo("memberId",yangOrder.getMemberId())
                .andGreaterThanOrEqualTo("invitTime",System.currentTimeMillis()/100);
        List<YangMember> invitMembers = yangMemberMapper.selectByExample(example);
        Integer invitMemberId = null;

        if(null == invitMembers || invitMembers.size() == 0){
            invitMemberId = Integer.valueOf(invitMemberIdConfig.getValue());
        }else{
            invitMemberId = invitMembers.get(0).getMemberId();
        }

        YangInvit yangInvitInsert = new YangInvit();
        yangInvitInsert.setMemberId(invitMemberId);
        yangInvitInsert.setMemberIdBottom(yangOrder.getMemberId());
        yangInvitInsert.setCtime(System.currentTimeMillis()/1000);
        yangInvitInsert.setOrderId(yangOrder.getOrdersId());
        yangInvitInsert.setCfee(tradeFeeNumOrPrice);
        yangInvitInsert.setRebate(rebateMoney);
        yangInvitInsert.setTradeCurrencyId(yangOrder.getCurrencyTradeId());
        yangInvitInsert.setCurrencyId(yangOrder.getCurrencyId());
        yangInvitInsert.setOrderType(yangOrder.getType());
        yangInvitInsert.setOrderId(yangOrder.getOrdersId());
        yangInvitInsert.setStatus(1);
        if(yangOrder.getType().equals(buyType)){
            yangInvitInsert.setRebatetype(1);
        }else if(yangOrder.getType().equals(sellType)){
            yangInvitInsert.setRebatetype(2);
        }

        //新增返利表记录
        yangInvitMapper.insertSelective(yangInvitInsert);

        /*直接分配返利数据*/
        checkCurrency(yangInvitInsert.getMemberId(),yangOrder.getCurrencyId()); //保证有资产记录
        yangInvitUpdate(yangInvitInsert);

    }

    /*更新一笔返利数据*/
    public void yangInvitUpdate(YangInvit yangInvit){
        if(yangInvit.getOrderType().equals(buyType)){
            if(yangInvit.getCurrencyId() == 0){//韩元市场
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

            }else{ //非韩元
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
        }else{
            if(yangInvit.getTradeCurrencyId() == 0) {//韩元市场
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
            }else{

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
    public void checkCurrency(Integer memberId,Integer currencyId){
        if(currencyId == 0){
            return ;
        }else{
            Example example = new Example(YangCurrencyUser.class);
            example.createCriteria().andEqualTo("memberId",memberId).andEqualTo("currencyId",currencyId);
            int exist = yangCurrencyUserMapper.selectCountByExample(example);
            if(exist == 0){
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
    public YangCurrencyPair getYangCurrencyPair(YangOrders yangOrders){
        YangCurrencyPair yangCurrencyPairQuery = new YangCurrencyPair();
        yangCurrencyPairQuery.setCurrencyId(yangOrders.getCurrencyId());
        yangCurrencyPairQuery.setCurrencyTradeId(yangOrders.getCurrencyTradeId());
        YangCurrencyPair yangCurrencyPairFind = yangCurrencyPairMapper.selectOne(yangCurrencyPairQuery);
        if(null == yangCurrencyPairFind){
            throw new RuntimeException("手续费配置不存在");
        }
        return yangCurrencyPairFind;
    }


    /**
     * 获取订单信息
     * @param currency_id
     * @param currency_trade_id
     * @param orders_id
     * @param type
     * @return
     */
    public List<Map> getOrderByfields(int  currency_id,int currency_trade_id,int  orders_id,String type)
    {

        Map parama = new HashMap();
        parama.put("currency_id",currency_id);
        parama.put("currency_trade_id",currency_trade_id);
        parama.put("orders_id",orders_id);
        parama.put("type",type);

        return yangOrdersMapper.getOrderByfields(parama);

    }

    /**
     * 获取trans信息
     * @param cid
     * @param ordersId
     * @param type
     * @return
     */
    public List<Map> getTradeInfo(String cid,String ordersId,String type)
    {

        return null;



    }



}
