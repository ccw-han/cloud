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
import org.springframework.context.annotation.Scope;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.web3j.abi.datatypes.Int;
import tk.mybatis.mapper.entity.Example;

import java.math.BigDecimal;
import java.util.*;

import static net.cyweb.controller.ApiBaseController.MQ_ORDER_QUEUE;

@Service
@EnableAsync
@Scope("prototype")
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


        doOrder(yangOrdersFind);


    }


    /**
     *
     * @param yangOrders
     * @throws RuntimeException
     */
    @Transactional
    public void doOrder(YangOrders yangOrders) throws RuntimeException{


        //标记一下处理订单
        YangOrders yangOrders2 = new YangOrders();
        yangOrders2.setOrdersId(yangOrders.getOrdersId());
        yangOrders2.setHasDo(Integer.valueOf(1));
        yangOrdersMapper.updateByPrimaryKeySelective(yangOrders2);

        long start_time;
        long end_time;

        start_time = System.currentTimeMillis();


        Example example = new Example(YangOrders.class);  //获取需要处理的订单

        List<YangOrders> yangOrdersList;

        if(yangOrders.getType().equals(sellType)) //如果是卖单 则需要寻找价格复核条件的 从高到底的订单
        {
            List<String> statusList = Lists.newArrayList();
            statusList.add("0");statusList.add("1");
            example.createCriteria().andEqualTo("currencyId",yangOrders.getCurrencyId())
                    .andEqualTo("currencyTradeId",yangOrders.getCurrencyTradeId())
                    .andEqualTo("type",buyType)
                    .andIn("status",statusList)
                    .andLessThan("ordersId",yangOrders.getOrdersId())
                    .andGreaterThanOrEqualTo("price",yangOrders.getPrice())
                    ;
            example.setOrderByClause("price desc,add_time asc");

            yangOrdersList = selectByExample(example); //查询到所有的符合条件的买单(1.单价大于卖单单价 2.找到最贵的买单单价 3.按时间发布排序)

        }else{ //如果是买单 则寻找价格从低到高的卖单

            List<String> statusList = Lists.newArrayList();
            statusList.add("0");statusList.add("1");
            example.createCriteria()
                    .andEqualTo("currencyId",yangOrders.getCurrencyId())
                    .andEqualTo("currencyTradeId",yangOrders.getCurrencyTradeId())
                    .andEqualTo("type",sellType)
                    .andLessThan("ordersId",yangOrders.getOrdersId())
                    .andIn("status",statusList)
                    .andLessThanOrEqualTo("price",yangOrders.getPrice());
            example.setOrderByClause("price asc,add_time asc");
            yangOrdersList = selectByExample(example); //查询到所有的符合条件的卖单(1.单价小于买单单价 2.找到最便宜的卖单单价 3.按时间发布排序)

        }


        end_time = System.currentTimeMillis();

        System.out.println("查询订单用时"+(end_time - start_time));

        if (yangOrdersList == null || yangOrdersList.size() == 0 )
        {
            return;
        }



        BigDecimal nums_need = yangOrders.getNum(); //需要购买总的数量

        long tradeTime = System.currentTimeMillis()/1000;

        boolean isOk =  false; //是否已经完成


        //获取交易对基本信息
        start_time = System.currentTimeMillis();


        YangCurrencyPair yangCurrencyPairFrom = yangPairService.getPairInfo(yangOrders.getCurrencyId(),yangOrders.getCurrencyTradeId());

        end_time = System.currentTimeMillis();

        System.out.println("查询交易对信息用时"+(end_time - start_time));


        start_time = System.currentTimeMillis();

        //获取用户手续费的默认配置
        YangTradeFee yangTradeFee = new YangTradeFee();
        yangTradeFee.setCyId(yangCurrencyPairFrom.getCyId());
        yangTradeFee.setMemberId(yangOrders.getMemberId());
        YangTradeFee yangTradeFeeFrom = this.getTradeFee(yangTradeFee);

        end_time = System.currentTimeMillis();
        System.out.println("获取手续费用时"+(end_time - start_time));


        BigDecimal finishNumsTimes = BigDecimal.ZERO; //这次循环处理的数量


        //开启redis 事务


        for (YangOrders y :yangOrdersList
             ) {


            BigDecimal numLeft = y.getNum().subtract(y.getTradeNum());  // 本次处理的订单 还能够操作的数量有多少


            if(nums_need.compareTo(BigDecimal.ZERO) == 0) //如果需要处理的数量都处理了 则结束本次循环
            {
                break;
            }


            if(numLeft.compareTo(nums_need) >= 0 )  //如果这个订单的数量满足并需求订单 则直接完成
            {
                isOk = true;
                finishNumsTimes = nums_need;

            }else { //否则就继续循环 到下一个待处理订单处理
                finishNumsTimes = numLeft;
                nums_need = nums_need.subtract(numLeft);  //更新需要购买的数量
            }


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

            start_time = System.currentTimeMillis();


            List<YangTrade> list =  updateTradeInfo(yangOrders,y,finishNumsTimes,freeBuy,freeSell,yangOrders.getType());

            YangTrade buy  = list.get(0);
            YangTrade sell  = list.get(1);


            //更新本次循环手续费交易记录
            YangFinance buyerFinance = new YangFinance(); //买单手续费
            buyerFinance.setAddTime(System.currentTimeMillis()/1000);
            buyerFinance.setMemberId(yangOrders.getMemberId());
            buyerFinance.setType((byte) 11);
            buyerFinance.setContent("交易手续费");
            buyerFinance.setMoney(freeBuy);
            buyerFinance.setMoneyType((byte)2);
            buyerFinance.setCurrencyId(yangOrders.getCurrencyId());
            buyerFinance.setIp("127.0.0.1");

            this.installFinanceRedis(buyerFinance);

            YangFinance sellerFinance = new YangFinance(); //卖单手续费
            sellerFinance.setAddTime(System.currentTimeMillis()/1000);
            sellerFinance.setMemberId(y.getMemberId());
            sellerFinance.setType((byte) 11);
            sellerFinance.setContent("交易手续费");
            sellerFinance.setMoney(freeSell);
            sellerFinance.setMoneyType((byte)2);
            sellerFinance.setCurrencyId(yangOrders.getCurrencyTradeId());
            sellerFinance.setIp("127.0.0.1");

            this.installFinanceRedis(sellerFinance);


            end_time = System.currentTimeMillis();

            System.out.println("更新手续费日志用时"+(end_time - start_time));




            //更新本次循环，买单卖单的用户资产
            start_time = System.currentTimeMillis();

            int i = 0;



            if(yangOrders.getType().equals(buyType))
            {
                i = updateUserMoney(

                        yangOrders.getMemberId(),yangOrders.getCurrencyId(),finishNumsTimes.subtract(freeBuy),"inc","normal" ,

                        y.getMemberId(),yangOrders.getCurrencyId(),finishNumsTimes,"dec","forzen",

                        yangOrders.getMemberId(),yangOrders.getCurrencyTradeId(),finishNumsTimes.multiply(yangOrders.getPrice()),"dec","forzen",

                        y.getMemberId(),yangOrders.getCurrencyTradeId(),finishNumsTimes.multiply(y.getPrice()).subtract(freeSell),"inc","normal",

                        yangOrders.getMemberId(),yangOrders.getCurrencyTradeId(),finishNumsTimes.multiply(yangOrders.getPrice().subtract(y.getPrice())),"inc","normal", //当卖价低于收购价的时候 要给买家把多余的钱加回去

                        y.getOrdersId(),finishNumsTimes,tradeTime,

                        yangOrders.getOrdersId(),finishNumsTimes,tradeTime,

                        //交易记录
                        buy.getTradeNo(),buy.getMemberId(),buy.getCurrencyId(),buy.getCurrencyTradeId(),buy.getPrice(),buy.getNum(),buy.getMoney(),buy.getFee(),buy.getType(),buy.getAddTime(),buy.getStatus(),buy.getShow(), buy.getOrderId(),
                        sell.getTradeNo() ,sell.getMemberId() ,sell.getCurrencyId(),sell.getCurrencyTradeId(),sell.getPrice(),sell.getNum(),sell.getMoney(),sell.getFee(),sell.getType(), sell.getAddTime(),sell.getStatus(),sell.getShow(),sell.getOrderId(),

                        buyerFinance.getMemberId(),buyerFinance.getType() ,buyerFinance.getMoneyType(),buyerFinance.getMoney() ,buyerFinance.getAddTime() ,buyerFinance.getCurrencyId() ,buyerFinance.getIp() ,buyerFinance.getContent() ,
                        sellerFinance.getMemberId(),sellerFinance.getType() ,sellerFinance.getMoneyType(),sellerFinance.getMoney() ,sellerFinance.getAddTime() ,sellerFinance.getCurrencyId() ,sellerFinance.getIp() ,sellerFinance.getContent()


                );
            }else{
                i = updateUserMoney(

                        y.getMemberId(),y.getCurrencyId(),finishNumsTimes.subtract(freeBuy),"inc","normal",

                        yangOrders.getMemberId(),yangOrders.getCurrencyId(),finishNumsTimes,"dec","forzen",

                        y.getMemberId(),y.getCurrencyTradeId(),finishNumsTimes.multiply(y.getPrice()),"dec","forzen",

                        yangOrders.getMemberId(),yangOrders.getCurrencyTradeId(),finishNumsTimes.multiply(y.getPrice()).subtract(freeSell),"inc","normal",

                        0, Integer.valueOf(0),null,null,null,

                        y.getOrdersId(),finishNumsTimes,tradeTime,

                        yangOrders.getOrdersId(),finishNumsTimes,tradeTime,

                        //交易记录
                        buy.getTradeNo(),buy.getMemberId(),buy.getCurrencyId(),buy.getCurrencyTradeId(),buy.getPrice(),buy.getNum(),buy.getMoney(),buy.getFee(),buy.getType(),buy.getAddTime(),buy.getStatus(),buy.getShow(), buy.getOrderId(),
                        sell.getTradeNo() ,sell.getMemberId() ,sell.getCurrencyId(),sell.getCurrencyTradeId(),sell.getPrice(),sell.getNum(),sell.getMoney(),sell.getFee(),sell.getType(), sell.getAddTime(),sell.getStatus(),sell.getShow(),sell.getOrderId(),

                        buyerFinance.getMemberId(),buyerFinance.getType() ,buyerFinance.getMoneyType(),buyerFinance.getMoney() ,buyerFinance.getAddTime() ,buyerFinance.getCurrencyId() ,buyerFinance.getIp() ,buyerFinance.getContent() ,
                        sellerFinance.getMemberId(),sellerFinance.getType() ,sellerFinance.getMoneyType(),sellerFinance.getMoney() ,sellerFinance.getAddTime() ,sellerFinance.getCurrencyId() ,sellerFinance.getIp() ,sellerFinance.getContent()

                );
            }

            if(i == 1)
            {

                System.out.println("错误信息"+yangOrders.getCurrencyId()+"_"+yangOrders.getCurrencyTradeId()+"用户id"+yangOrders.getMemberId());
                System.out.println("错误信息"+y.getMemberId());
                throw  new RuntimeException();


            }
            end_time = System.currentTimeMillis();

            System.out.println("更新用户资产存储过程用时"+(end_time - start_time));



            if(isOk)
            {
                break;
            }

        }


//        redisService.zremove(MQ_ORDER_QUEUE+"_"+yangCurrencyPairFrom.getCyId().intValue(),yangOrders.getOrdersId().intValue());


    }

    @Async
    public void installFinanceRedis(YangFinance yangFinance)
    {
        redisService.rPush(CoinConst.FINANCE_KEY_INSERT,JSONObject.toJSON(yangFinance));

    }

    @Async
    public void  insertFinance(List<YangFinance> list)
    {
        yangFinanceMapper.insertList(list);
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

            coinPairRedisBean.setNew_price(CommonTools.formatNull2BigDecimal(redisService.hmGet(key,"new_price")));


            coinPairRedisBean.setPrice_status(CommonTools.formatNull2int(redisService.hmGet(key,"price_status")));


            //                    'input_price_num'=>$cy->input_price_num,//价格显示位数
//                    'rate_num'=>$cy->rate_num,//汇率转换后的位数
//                    'is_chuang'=>$cy->is_chuang,

            coinPairRedisBean.setInput_price_num(CommonTools.formatNull2BigDecimal(redisService.hmGet(key,"input_price_num")));


            coinPairRedisBean.setRate_num(CommonTools.formatNull2int(redisService.hmGet(key,"rate_num")));


            coinPairRedisBean.setIs_chuang((String)redisService.hmGet(key,"is_chuang"));

            if(currency_trade_id != 0)  //实时获取这个币种的krw价格
            {
                try {
                    Double l = Double.valueOf(redisService.hmGet(CoinConst.REDIS_COIN_KRW_PRICE_,String.valueOf(currency_trade_id)).toString());
                    coinPairRedisBean.setKrw_price(BigDecimal.valueOf(l));
                }catch (Exception e)
                {
                    coinPairRedisBean.setKrw_price(BigDecimal.valueOf(1));

                    System.out.println("找不到该币种 krw"+currency_trade_id);
                }
            }else{
                coinPairRedisBean.setKrw_price(BigDecimal.valueOf(1));
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
//    @Async
    public boolean flushCoinInfos(int currency_id,int currency_trade_id,YangTrade yangTrade) {
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

        if (currency_trade_id == 0) //设置交易对应的交易krw价格
        {

            redisService.hmSet(CoinConst.REDIS_COIN_KRW_PRICE_, String.valueOf(currency_id), yangTrade.getPrice().toString());

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


        coinPairRedisBean.setNew_price(yangTrade.getPrice());


        long time_now = System.currentTimeMillis(); //当前时间戳
        redisService.zAdd(CoinConst.REDIS_TREAD_24_ + coinPairRedisBean.getCurrency_id() + "-" + coinPairRedisBean.getCurrency_trade_id(), JSONObject.toJSON(yangTrade), time_now);


        redisService.hmSet(key,"cy_id", CommonTools.formatNull2String(String.valueOf(coinPairRedisBean.getCy_id())));
        redisService.hmSet(key,"currency_id", CommonTools.formatNull2String(String.valueOf(coinPairRedisBean.getCurrency_id())));
        redisService.hmSet(key,"currency_trade_id", CommonTools.formatNull2String(String.valueOf(coinPairRedisBean.getCurrency_trade_id())));
        redisService.hmSet(key,"currency_logo", CommonTools.formatNull2String(coinPairRedisBean.getCurrency_logo()));
        redisService.hmSet(key,"trade_logo", CommonTools.formatNull2String(coinPairRedisBean.getTrade_logo()));
        redisService.hmSet(key,"currency_mark", CommonTools.formatNull2String(coinPairRedisBean.getCurrency_mark()));
        redisService.hmSet(key,"trade_mark", CommonTools.formatNull2String(coinPairRedisBean.getTrade_mark()));
        redisService.hmSet(key,"new_price", CommonTools.formatNull2String(coinPairRedisBean.getNew_price().toString()));
        redisService.hmSet(key,"price_status", CommonTools.formatNull2String(String.valueOf(coinPairRedisBean.getPrice_status())));



        update24(coinPairRedisBean,yangTrade);
        return  true;




    }

    /*更新24小时信息（最高价、最低价等）*/
    private void update24(CoinPairRedisBean coinPairRedisBean,YangTrade yangTrade){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        String currentDayKey = dateFormat.format(new Date());
        String key_24h = CoinConst.REDIS_YANG_PARI_DAY_INFO_+yangTrade.getCurrencyId() + "-" + yangTrade.getCurrencyTradeId()+"_"+currentDayKey;

        List<String> h24KeyList = Lists.newArrayList();
        BigDecimal defaultValue = new BigDecimal(0);
        h24KeyList.add("h24_change");
        h24KeyList.add("h24_num");
        h24KeyList.add("h24_money");
        h24KeyList.add("h24_max");
        h24KeyList.add("h24_min");
        h24KeyList.add("new_price");
        h24KeyList.add("price_status");
        h24KeyList.add("first_price"); //当日第一笔价格
        //初始化所有的24小时数据，默认为0
        for(String keySub :h24KeyList){
            Object storedObject = redisService.hmGet(key_24h,keySub);
            if(null == storedObject){
                redisService.hmSet(key_24h,keySub,String.valueOf(defaultValue));
            }
        }

        //更新第一笔的价格
        BigDecimal firstPrice = new BigDecimal(String.valueOf(redisService.hmGet(key_24h,"first_price")));
        if(firstPrice.compareTo(defaultValue) == 0){
            redisService.hmSet(key_24h,"first_price",String.valueOf(yangTrade.getPrice()));
            firstPrice = yangTrade.getPrice();
        }


        //最新一笔和上一笔是涨跌
        BigDecimal lastestPrice = new BigDecimal(String.valueOf(redisService.hmGet(key_24h,"new_price")));
        Integer price_status = yangTrade.getPrice().compareTo(lastestPrice);
        coinPairRedisBean.setPrice_status(price_status);
        redisService.hmSet(key_24h,"price_status",String.valueOf(price_status));

        /*最新价格*/
        redisService.hmSet(key_24h,"new_price",String.valueOf(yangTrade.getPrice()));
        coinPairRedisBean.setNew_price(yangTrade.getPrice());

        //更新24小时涨幅
        BigDecimal h24Change = yangTrade.getPrice().subtract(firstPrice).divide(firstPrice,9,BigDecimal.ROUND_HALF_DOWN);


        redisService.hmSet(key_24h,"h24_change",String.valueOf(h24Change));
        coinPairRedisBean.setH24_change(h24Change.doubleValue());


        /*24数量*/
        BigDecimal h24NumAdded = new BigDecimal(String.valueOf(redisService.hmGet(key_24h,"h24_num"))).add(yangTrade.getNum());
        redisService.hmSet(key_24h,"h24_num",String.valueOf(h24NumAdded));
        coinPairRedisBean.setH24_num(String.valueOf(h24NumAdded));

        /*24成交额*/
        BigDecimal h24MoneyAdded = new BigDecimal(String.valueOf(redisService.hmGet(key_24h,"h24_money"))).add(yangTrade.getNum().multiply(yangTrade.getPrice()));
        redisService.hmSet(key_24h,"h24_money",String.valueOf(h24MoneyAdded));
        coinPairRedisBean.setH24_money(String.valueOf(h24MoneyAdded));

        /*24最高价*/
        BigDecimal h24MaxStored = new BigDecimal(String.valueOf(redisService.hmGet(key_24h,"h24_max")));
        BigDecimal h24Max = h24MaxStored.compareTo(yangTrade.getPrice()) == -1 ? yangTrade.getPrice() : h24MaxStored;
        redisService.hmSet(key_24h,"h24_max",String.valueOf(h24Max));
        coinPairRedisBean.setH24_max(String.valueOf(h24Max));

        /*24最低价*/
        BigDecimal h24MinStored = new BigDecimal(String.valueOf(redisService.hmGet(key_24h,"h24_min")));
        BigDecimal h24Min = h24MinStored.compareTo(yangTrade.getPrice()) == 1 ? yangTrade.getPrice() : h24MinStored;
        if(h24Min.compareTo(defaultValue) == 0){
            h24Min = yangTrade.getPrice();
        }
        redisService.hmSet(key_24h,"h24_min",String.valueOf(h24Min));
        coinPairRedisBean.setH24_min(String.valueOf(h24Min));
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
    public List<YangTrade> updateTradeInfo(YangOrders yangOrdersBuy,YangOrders yangOrdersSell,BigDecimal loopTradeNum,BigDecimal buyerTradeFeeNum,BigDecimal sellerTradeFeePrice,String type){
        //新增本次循环交易记录 trade
        Integer isShua = 1; //刷单标记
        Integer isShua1 = 1; //刷单标记

        BigDecimal price_final;

        price_final = yangOrdersSell.getPrice();


        List<YangTrade>  list = new LinkedList<>();


        YangTrade buyerTradeLog = new YangTrade();
        buyerTradeLog.setAddTime(System.currentTimeMillis()/1000);
        buyerTradeLog.setCurrencyId(yangOrdersBuy.getCurrencyId());
        buyerTradeLog.setCurrencyTradeId(yangOrdersBuy.getCurrencyTradeId());
        buyerTradeLog.setMemberId(yangOrdersBuy.getMemberId());
        buyerTradeLog.setNum(loopTradeNum);
        buyerTradeLog.setType(yangOrdersBuy.getType());

        buyerTradeLog.setOrderId(yangOrdersBuy.getOrdersId());

        buyerTradeLog.setPrice(price_final);
        buyerTradeLog.setMoney(loopTradeNum.multiply(price_final));



        buyerTradeLog.setShow(1);
        buyerTradeLog.setFee(buyerTradeFeeNum);
        buyerTradeLog.setStatus((byte) 0);
        buyerTradeLog.setTradeNo("T"+System.currentTimeMillis()/1000);

        list.add(buyerTradeLog);

//        yangTradeMapper.insertSelective(buyerTradeLog);


        YangTrade sellerTradeLog = new YangTrade();
        sellerTradeLog.setAddTime(System.currentTimeMillis()/1000);
        sellerTradeLog.setCurrencyId(yangOrdersBuy.getCurrencyId());
        sellerTradeLog.setCurrencyTradeId(yangOrdersBuy.getCurrencyTradeId());
        sellerTradeLog.setMemberId(yangOrdersSell.getMemberId());
        sellerTradeLog.setNum(loopTradeNum);
        sellerTradeLog.setType(yangOrdersSell.getType());
        sellerTradeLog.setOrderId(yangOrdersSell.getOrdersId());

        sellerTradeLog.setPrice(price_final);
        sellerTradeLog.setMoney(loopTradeNum.multiply(price_final));


        sellerTradeLog.setShow(0);
        sellerTradeLog.setFee(sellerTradeFeePrice);
        sellerTradeLog.setStatus((byte) 0);
        sellerTradeLog.setTradeNo("T"+System.currentTimeMillis()/1000);
//        yangTradeMapper.insertSelective(sellerTradeLog);
        list.add(sellerTradeLog);

        //更新redis数据
        this.flushCoinInfos(buyerTradeLog.getCurrencyId(),buyerTradeLog.getCurrencyTradeId(),buyerTradeLog);


        return list;

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

            Integer memberId4,Integer currencyId4,BigDecimal num4,String type4,String field4,  //应为有的时候 购买的金额 比自己挂单的价格要底 需要把多余冻结金额返还给账户余额




            int orderId1 ,BigDecimal tradeNum1, long tradeTime1,
            int orderId2 ,BigDecimal tradeNum2, long tradeTime2,
            String tradeNo1,int t_memberId1,int t_currencyId1,int currencyTradeId1,BigDecimal price1,BigDecimal t_num1,BigDecimal t_money1,BigDecimal fee1,String t_type1, long t_addTime1,int t_status1,int show1, int t_orders_id1,
            String tradeNo2 ,int t_memberId2 ,int t_currencyId2,int currencyTradeId2,BigDecimal price2,BigDecimal t_num2,BigDecimal t_money2,BigDecimal fee2,String t_type2, long t_addTime2,int t_status2,int show2,int t_orders_id2,
            int f_memberId1,int f_type1 ,int f_moneyType1,BigDecimal f_money1 ,long f_addTime1 ,int f_currencyId1 ,String  f_ip1 ,String  f_content1 ,
            int f_memberId2,int f_type2 ,int f_moneyType2,BigDecimal f_money2 ,long f_addTime2 ,int f_currencyId2 ,String  f_ip2 ,String  f_content2



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


//        return yangMemberService.assets(
//                memberId,String.valueOf(currencyId.intValue()),num_tmp,type,forzen_tmp,type,
//                memberId1,String.valueOf(currencyId1.intValue()),num1_tmp,type1,forzen1_tmp,type1,
//                memberId2,String.valueOf(currencyId2.intValue()),num2_tmp,type2,forzen2_tmp,type2,
//                memberId3,String.valueOf(currencyId3.intValue()),num3_tmp,type3,forzen3_tmp,type3,
//                memberId4,String.valueOf(currencyId4.intValue()),num4_tmp,type4,forzen4_tmp,type4
//        );
        return yangMemberService.orderConfirm(

                memberId,String.valueOf(currencyId.intValue()),num_tmp,type,forzen_tmp,type,
                memberId1,String.valueOf(currencyId1.intValue()),num1_tmp,type1,forzen1_tmp,type1,
                memberId2,String.valueOf(currencyId2.intValue()),num2_tmp,type2,forzen2_tmp,type2,
                memberId3,String.valueOf(currencyId3.intValue()),num3_tmp,type3,forzen3_tmp,type3,
                memberId4,String.valueOf(currencyId4.intValue()),num4_tmp,type4,forzen4_tmp,type4,

                 orderId1 ,tradeNum1,  tradeTime1,
                 orderId2 , tradeNum2,  tradeTime2,  //订单

                 tradeNo1, t_memberId1, t_currencyId1, currencyTradeId1, price1, t_num1, t_money1, fee1, t_type1,  t_addTime1, t_status1, show1,  t_orders_id1,
                 tradeNo2 , t_memberId2 , t_currencyId2, currencyTradeId2, price2, t_num2, t_money2, fee2, t_type2,  t_addTime2, t_status2, show2, t_orders_id2, //交易记录

                 f_memberId1, f_type1 , f_moneyType1, f_money1 , f_addTime1 , f_currencyId1 ,  f_ip1 ,  f_content1 , //资产变更
                 f_memberId2, f_type2 , f_moneyType2, f_money2 , f_addTime2 , f_currencyId2 ,  f_ip2 ,  f_content2


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


    public List<HashMap> getOrderCollectInfos(YangCurrencyPair yangCurrencyPair)
    {
        return yangOrdersMapper.getOrderCollectInfos(yangCurrencyPair);
    }


}
