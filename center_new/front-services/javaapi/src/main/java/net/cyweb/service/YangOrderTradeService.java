package net.cyweb.service;

import com.alibaba.druid.support.json.JSONUtils;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import cyweb.utils.CoinConst;
import cyweb.utils.CommonTools;
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
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import tk.mybatis.mapper.entity.Example;

import java.math.BigDecimal;
import java.util.*;

@Service
@EnableAsync
@Scope("prototype")
public class YangOrderTradeService extends BaseService<YangOrders> {

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
    private YangConfigMapper yangConfigMapper;

    @Autowired
    private YangTradeFeeMapper yangTradeFeeMapper;

    @Autowired
    private YangOrdersMapper yangOrdersMapper;

    @Autowired
    private YangTradeMapper yangTradeMapper;

    @Autowired
    private YangMemberMapper yangMemberMapper;

    @Autowired
    private YangCurrencyUserMapper yangCurrencyUserMapper;

    @Autowired
    private YangCurrencyPairMapper yangCurrencyPairMapper;

//    @Autowired
//    private YangMemberTokenService yangMemberTokenService;

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
            throw new RuntimeException("此订单系统内未找到");
        }

        /*该订单为取消状态*/
        if(yangOrdersFind.getStatus().byteValue() == OrderStatus.ORDER_TYPE_CANCELED.getIndex()){
           logger.debug("此订单【"+yangOrdersFind.getOrdersId()+"】为取消状态");
            return;
        }

        /*该订单为已完成状态*/
        if(yangOrdersFind.getStatus().byteValue() == OrderStatus.ORDER_TYPE_FINISHED.getIndex()){
           logger.debug("此订单【"+yangOrdersFind.getOrdersId()+"】为已完成状态");
            return;
        }

        // TODO: 2018/6/12 加上机器人的判断 是否继续正常处理 还是紧急撤单

        //处理订单
        doOrder(yangOrdersFind);

    }


    /**
     *
     * @param yangOrders
     * @throws RuntimeException
     */
    @Transactional(rollbackFor = Exception.class)
    public void doOrder(YangOrders yangOrders) throws RuntimeException{
        //标记一下处理订单
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
                    //.andLessThan("ordersId",yangOrders.getOrdersId())
                    .andGreaterThanOrEqualTo("price",yangOrders.getPrice().toString());
            example.setOrderByClause("price desc");
            yangOrdersList = selectByExample(example); //查询到所有的符合条件的买单(1.单价大于卖单单价 2.找到最贵的买单单价 3.按时间发布排序)
        }else{ //如果是买单 则寻找价格从低到高的卖单
            List<String> statusList = Lists.newArrayList();
            statusList.add("0");statusList.add("1");
            example.createCriteria()
                    .andEqualTo("currencyId",yangOrders.getCurrencyId())
                    .andEqualTo("currencyTradeId",yangOrders.getCurrencyTradeId())
                    .andEqualTo("type",sellType)
                    //.andLessThan("ordersId",yangOrders.getOrdersId())
                    .andIn("status",statusList)
                    .andLessThanOrEqualTo("price",yangOrders.getPrice().toString());
            example.setOrderByClause("price asc");
            yangOrdersList = selectByExample(example); //查询到所有的符合条件的卖单(1.单价小于买单单价 2.找到最便宜的卖单单价 3.按时间发布排序)
        }
        end_time = System.currentTimeMillis();
        logger.debug("查询订单用时"+(end_time - start_time));
        if (yangOrdersList == null || yangOrdersList.size() == 0 )
        {
            return;
        }
        BigDecimal nums_need = yangOrders.getNum(); //需要购买总的数量
        boolean isOk =  false; //是否已经完成
        //获取交易对基本信息
        start_time = System.currentTimeMillis();
        YangCurrencyPair yangCurrencyPairFrom = yangPairService.getPairInfo(yangOrders.getCurrencyId(),yangOrders.getCurrencyTradeId(),true);
        logger.debug("--------------交易对对象"+yangCurrencyPairFrom);
        end_time = System.currentTimeMillis();
        logger.debug("查询交易对信息用时"+(end_time - start_time));
        start_time = System.currentTimeMillis();
        //获取用户手续费的默认配置
        YangTradeFee yangTradeFee = new YangTradeFee();
        yangTradeFee.setCyId(yangCurrencyPairFrom.getCyId());
        yangTradeFee.setMemberId(yangOrders.getMemberId());
        YangTradeFee yangTradeFeeFrom = this.getTradeFee(yangTradeFee,true);
        logger.debug("--------------用户手续费对象"+yangTradeFeeFrom);
        logger.debug("--------------用户手续费对象"+yangTradeFeeFrom);
        end_time = System.currentTimeMillis();
        logger.debug("获取手续费用时"+(end_time - start_time));
        logger.debug("获取手续费用时"+(end_time - start_time));
        BigDecimal finishNumsTimes = BigDecimal.ZERO; //这次循环处理的数量
        //开启redis 事务
        for (YangOrders y :yangOrdersList){
            DealResultBean dealResultBean =  this.doDeal(y,nums_need,yangCurrencyPairFrom,yangOrders,yangTradeFeeFrom);
            if(dealResultBean.isOk()) {
                break;
            }
            if(dealResultBean.getNums_need() != null) {
                nums_need = dealResultBean.getNums_need();
            }
        }
    }


    /**
     * 具体处理成交模块 可以单元回滚
     */
    @Transactional(rollbackFor = Exception.class,propagation = Propagation.REQUIRES_NEW,readOnly = false)
    public DealResultBean doDeal(YangOrders y, BigDecimal nums_need , YangCurrencyPair yangCurrencyPairFrom , YangOrders yangOrders, YangTradeFee yangTradeFeeFrom)
    {
        DealResultBean dealResultBean = new DealResultBean();
        dealResultBean.setOk(false);
        BigDecimal finishNumsTimes;
        BigDecimal freeFrom; //订单发起者
        BigDecimal freeTo; //订单应答者
        long start_time;
        long end_time;
        long tradeTime = System.currentTimeMillis()/1000;
        Object o =  TransactionAspectSupport.currentTransactionStatus().createSavepoint(); //标记回滚
        //异常订单列表
        List<String> errorList=new LinkedList<String>();
        Map resultMap=new HashMap();
        try {
            BigDecimal numLeft = y.getNum().subtract(y.getTradeNum());  // 本次处理的订单 还能够操作的数量有多少
            if(nums_need.compareTo(BigDecimal.ZERO) == 0) //如果需要处理的数量都处理了 则结束本次循环
            {
                dealResultBean.setOk(true);
                return dealResultBean;
            }
            if(numLeft.compareTo(nums_need) >= 0 )  //如果这个订单的数量满足并需求订单 则直接完成
            {
                dealResultBean.setOk(true);
                finishNumsTimes = nums_need;
            }else { //否则就继续循环 到下一个待处理订单处理
                finishNumsTimes = numLeft;
                nums_need = nums_need.subtract(numLeft);  //更新需要购买的数量
            }
            dealResultBean.setNums_need(nums_need);
            YangTradeFee yangTradeFee1 = new YangTradeFee();
            yangTradeFee1.setCyId(yangCurrencyPairFrom.getCyId());
            yangTradeFee1.setMemberId(y.getMemberId());
            YangTradeFee yangTradeFeeTo = this.getTradeFee(yangTradeFee1,true);
            if(yangOrders.getType().equals(buyType)) // 如果是买单
            {
                //设置买单的买入卖出手续费
                if(null != yangTradeFeeFrom && null != yangTradeFeeFrom.getCurrencyBuyFee())
                {
                    freeFrom = finishNumsTimes.multiply(BigDecimal.valueOf(yangTradeFeeFrom.getCurrencyBuyFee())).divide(BigDecimal.valueOf(100));
                }else{
                    freeFrom = finishNumsTimes.multiply(yangCurrencyPairFrom.getCurrencyBuyFee()).divide(BigDecimal.valueOf(100));
                }

                if(null != yangTradeFeeTo && null != yangTradeFeeTo.getCurrencySellFee())
                {
                    freeTo = finishNumsTimes.multiply(BigDecimal.valueOf(yangTradeFeeTo.getCurrencySellFee()).multiply(y.getPrice()).divide(BigDecimal.valueOf(100)));
                }else{
                    freeTo = finishNumsTimes.multiply(yangCurrencyPairFrom.getCurrencySellFee()).multiply(y.getPrice()).divide(BigDecimal.valueOf(100));
                }

            }else{
                //卖单的买入卖出手续费
                if(null != yangTradeFeeFrom && null != yangTradeFeeFrom.getCurrencySellFee())  //如果是卖单 则发起方获取的是钱
                {
                    freeFrom = finishNumsTimes.multiply(BigDecimal.valueOf(yangTradeFeeFrom.getCurrencySellFee())).multiply(y.getPrice()).divide(BigDecimal.valueOf(100));
                }else{
                    freeFrom = finishNumsTimes.multiply(yangCurrencyPairFrom.getCurrencySellFee()).multiply(y.getPrice()).divide(BigDecimal.valueOf(100));
                }
                if(null != yangTradeFeeTo && null != yangTradeFeeTo.getCurrencyBuyFee()) //如果是卖单 则发起方获取的是币
                {
                    freeTo = finishNumsTimes.multiply(BigDecimal.valueOf(yangTradeFeeTo.getCurrencyBuyFee()).divide(BigDecimal.valueOf(100)));
                }else{
                    freeTo = finishNumsTimes.multiply(yangCurrencyPairFrom.getCurrencyBuyFee()).divide(BigDecimal.valueOf(100));
                }
            }
            if(yangOrders.getMemberId().equals(Integer.valueOf(2029)))
            {
                freeFrom = BigDecimal.valueOf(0);
            }
            if(y.getMemberId().equals(Integer.valueOf(2029)))
            {
                freeTo = BigDecimal.valueOf(0);
            }
            start_time = System.currentTimeMillis();
            //更新订单信息，
            List<YangTrade> list =  updateTradeInfo(yangOrders,y,finishNumsTimes,freeFrom,freeTo,yangOrders.getType(),yangCurrencyPairFrom);
            YangTrade buy  = list.get(0);
            YangTrade sell  = list.get(1);
            //更新本次循环手续费交易记录
            YangFinance buyerFinance = new YangFinance(); //买单手续费
            buyerFinance.setAddTime(System.currentTimeMillis()/1000);
            buyerFinance.setMemberId(yangOrders.getMemberId());
            buyerFinance.setType((byte) 11);
            buyerFinance.setContent("交易手续费");
            buyerFinance.setMoney(freeFrom);
            buyerFinance.setMoneyType((byte)2);
            buyerFinance.setCurrencyId(yangOrders.getCurrencyId());
            buyerFinance.setIp("127.0.0.1");
//            if(!freeBuy.equals(BigDecimal.ZERO))
//            {
//                this.installFinanceRedis(buyerFinance);
//            }
            YangFinance sellerFinance = new YangFinance(); //卖单手续费
            sellerFinance.setAddTime(System.currentTimeMillis()/1000);
            sellerFinance.setMemberId(y.getMemberId());
            sellerFinance.setType((byte) 11);
            sellerFinance.setContent("交易手续费");
            sellerFinance.setMoney(freeTo);
            sellerFinance.setMoneyType((byte)2);
            sellerFinance.setCurrencyId(yangOrders.getCurrencyTradeId());
            sellerFinance.setIp("127.0.0.1");
//            if(!freeSell.equals(BigDecimal.ZERO))
//            {
//                this.installFinanceRedis(sellerFinance);
//
//            }
            end_time = System.currentTimeMillis();
            logger.debug("更新手续费日志用时"+(end_time - start_time));
            //更新本次循环，买单卖单的用户资产
            start_time = System.currentTimeMillis();
            int i = 0;
            if(yangOrders.getType().equals(buyType))
            {
                resultMap=dealOrder(
                        //买家  增加 btc
                        yangOrders.getMemberId(),yangOrders.getCurrencyId(),finishNumsTimes.subtract(freeFrom),"inc","normal" ,yangOrders.getOrdersId(),
                        //卖家 消耗 btc  冻结btc减少
                        y.getMemberId(),yangOrders.getCurrencyId(),finishNumsTimes,"dec","forzen",y.getOrdersId(),
                        //买家 减少krw  冻结krw减少
                        yangOrders.getMemberId(),yangOrders.getCurrencyTradeId(),finishNumsTimes.multiply(yangOrders.getPrice()),"dec","forzen",yangOrders.getOrdersId(),
                        //卖家 新增krw
                        y.getMemberId(),yangOrders.getCurrencyTradeId(),finishNumsTimes.multiply(y.getPrice()).subtract(freeTo),"inc","normal",y.getOrdersId(),

                        yangOrders.getMemberId(),yangOrders.getCurrencyTradeId(),finishNumsTimes.multiply(yangOrders.getPrice().subtract(y.getPrice())),"inc","normal",yangOrders.getOrdersId(), //当卖价低于收购价的时候 要给买家把多余的钱加回去
                        //买家多冻结的krw 减掉

                        y.getOrdersId(),finishNumsTimes,tradeTime,

                        yangOrders.getOrdersId(),finishNumsTimes,tradeTime,

                        //交易记录
                        buy.getTradeNo(),buy.getMemberId(),buy.getCurrencyId(),buy.getCurrencyTradeId(),buy.getPrice(),buy.getNum(),buy.getMoney(),buy.getFee(),buy.getType(),buy.getAddTime(),buy.getStatus(),buy.getShow(), buy.getOrderId(),
                        sell.getTradeNo() ,sell.getMemberId() ,sell.getCurrencyId(),sell.getCurrencyTradeId(),sell.getPrice(),sell.getNum(),sell.getMoney(),sell.getFee(),sell.getType(), sell.getAddTime(),sell.getStatus(),sell.getShow(),sell.getOrderId(),

                        buyerFinance.getMemberId(),buyerFinance.getType() ,buyerFinance.getMoneyType(),buyerFinance.getMoney() ,buyerFinance.getAddTime() ,buyerFinance.getCurrencyId() ,buyerFinance.getIp() ,buyerFinance.getContent() ,
                        sellerFinance.getMemberId(),sellerFinance.getType() ,sellerFinance.getMoneyType(),sellerFinance.getMoney() ,sellerFinance.getAddTime() ,sellerFinance.getCurrencyId() ,sellerFinance.getIp() ,sellerFinance.getContent(),
                        0, CoinConst.MYSQL_GC_DEAL_TYPE_TRADE
                );
                i=(int)resultMap.get("res");
               logger.debug("账户变动信息-----用户id为"+ yangOrders.getMemberId()+" 增加 "+finishNumsTimes.subtract(freeFrom)+" 资产为"+yangOrders.getCurrencyId());
               logger.debug("用户id为"+  y.getMemberId()+" 减少 "+finishNumsTimes+" 资产为"+yangOrders.getCurrencyId()+"(冻结)");
               logger.debug("用户id为"+   yangOrders.getMemberId() +" 减少 "+finishNumsTimes.multiply(yangOrders.getPrice())+" 资产为"+yangOrders.getCurrencyTradeId()+"(冻结)");
               logger.debug("用户id为"+   y.getMemberId() +" 增加 "+finishNumsTimes.multiply(y.getPrice()).subtract(freeTo)+" 资产为"+yangOrders.getCurrencyTradeId());
               logger.debug("用户id为"+  yangOrders.getMemberId() +" 增加 "+finishNumsTimes.multiply(yangOrders.getPrice().subtract(y.getPrice()))+" 资产为"+yangOrders.getCurrencyTradeId());
               logger.debug("交易记录 类别"+  buy.getType() +" 手续费 "+buy.getFee().toPlainString());
               logger.debug("交易记录 类别"+  sell.getType() +" 手续费 "+sell.getFee().toPlainString());
            }else{
                resultMap=dealOrder(
                        //买家    btc增加
                        y.getMemberId(),y.getCurrencyId(),finishNumsTimes.subtract(freeTo),"inc","normal",y.getOrdersId(),
                        //卖家  btc 冻结减少
                        yangOrders.getMemberId(),yangOrders.getCurrencyId(),finishNumsTimes,"dec","forzen",yangOrders.getOrdersId(),
                        //买家 减少冻结的krw
                        y.getMemberId(),y.getCurrencyTradeId(),finishNumsTimes.multiply(y.getPrice()),"dec","forzen",y.getOrdersId(),
                        //卖家krw 增加
                        yangOrders.getMemberId(),yangOrders.getCurrencyTradeId(),finishNumsTimes.multiply(y.getPrice()).subtract(freeFrom),"inc","normal",yangOrders.getOrdersId(),

                        0, Integer.valueOf(0),new BigDecimal(0),"","",0,

                        y.getOrdersId(),finishNumsTimes,tradeTime,

                        yangOrders.getOrdersId(),finishNumsTimes,tradeTime,

                        //交易记录
                        buy.getTradeNo(),buy.getMemberId(),buy.getCurrencyId(),buy.getCurrencyTradeId(),buy.getPrice(),buy.getNum(),buy.getMoney(),buy.getFee(),buy.getType(),buy.getAddTime(),buy.getStatus(),buy.getShow(), buy.getOrderId(),
                        sell.getTradeNo() ,sell.getMemberId() ,sell.getCurrencyId(),sell.getCurrencyTradeId(),sell.getPrice(),sell.getNum(),sell.getMoney(),sell.getFee(),sell.getType(), sell.getAddTime(),sell.getStatus(),sell.getShow(),sell.getOrderId(),

                        buyerFinance.getMemberId(),buyerFinance.getType() ,buyerFinance.getMoneyType(),buyerFinance.getMoney() ,buyerFinance.getAddTime() ,buyerFinance.getCurrencyId() ,buyerFinance.getIp() ,buyerFinance.getContent() ,
                        sellerFinance.getMemberId(),sellerFinance.getType() ,sellerFinance.getMoneyType(),sellerFinance.getMoney() ,sellerFinance.getAddTime() ,sellerFinance.getCurrencyId() ,sellerFinance.getIp() ,sellerFinance.getContent(),
                        0, CoinConst.MYSQL_GC_DEAL_TYPE_TRADE
                );
               i=(int)resultMap.get("res");
               logger.debug("账户变动信息-----用户id为"+ y.getMemberId()+" 增加 "+finishNumsTimes.subtract(freeTo)+" 资产为"+y.getCurrencyId());
               logger.debug("用户id为"+  yangOrders.getMemberId()+" 减少 "+finishNumsTimes+" 资产为"+yangOrders.getCurrencyId()+"(冻结)");
               logger.debug("用户id为"+   y.getMemberId() +" 减少 "+finishNumsTimes.multiply(y.getPrice())+" 资产为"+y.getCurrencyTradeId()+"(冻结)");
               logger.debug("用户id为"+   yangOrders.getMemberId() +" 增加 "+finishNumsTimes.multiply(y.getPrice()).subtract(freeTo)+" 资产为"+yangOrders.getCurrencyTradeId());
               logger.debug("交易记录 类别"+  buy.getType() +" 手续费 "+buy.getFee().toPlainString());
               logger.debug("交易记录 类别"+  sell.getType() +" 手续费 "+sell.getFee().toPlainString());
            }
            if(i == 1)
            {
               logger.debug("错误信息"+yangOrders.getCurrencyId()+"_"+yangOrders.getCurrencyTradeId()+"用户id"+yangOrders.getMemberId());
               logger.debug("错误信息"+y.getMemberId());
               throw  new RuntimeException("金额不对");
            }else{

                //设置前五条委托记录
                setFontUserFiveOrderRecord(yangOrders,y);
                //将k线新订单数据 放入redis中
                setKlineNormalData(list);
                //记录 每个币种最新交易时间
                setUserFontFiveTradeRecord(list);
            }

            end_time = System.currentTimeMillis();

           logger.debug("更新用户资产存储过程用时"+(end_time - start_time));
        }catch (Exception e){
            logger.error(e.getMessage());
            dealResultBean.setOk(false);
            dealResultBean.setNums_need(null);
            dealResultBean.setError(true);
            TransactionAspectSupport.currentTransactionStatus().rollbackToSavepoint(o);
        }
        finally {
            if(dealResultBean.isError()){
                //异常订单处理完毕
                resultMap.remove("res");
                resultMap.remove("resa");
                resultMap.remove("resb");
                resultMap.remove("msg");
                for (Object v : resultMap.values()) {
                    if(!errorList.contains(String.valueOf(v))&&!"0".equals(String.valueOf(v))){
                        errorList.add(String.valueOf(v));
                    }
                }
                for(String ordersId:errorList){
                    //如果是主订单错误,则通知外循环 无需再跑
                    if(yangOrders.getOrdersId().intValue()==Integer.valueOf(ordersId).intValue()){
                        dealResultBean.setOk(true);
                    }
                    signOderError(Integer.valueOf(ordersId));
                }
                return dealResultBean;
            }
        }
        return dealResultBean;

    }
    //设置k线正向数据到redis中
    public void setKlineNormalData(List<YangTrade> list){
        //k线历史方法  redis 不要删除 kline_history_final_end_time
        System.out.println("list长度----------->"+list.size());
        //把交易数据 写到mongdb中
            for(int m=0;m<list.size();m++){
                YangTrade yangTrade=list.get(m);
                String key= CoinConst.REDIS_COIN_PAIR_BASE_ +yangTrade.getCurrencyId()+"-"+yangTrade.getCurrencyTradeId();
                //保存币种,交易区信息
                yangTrade.setCurrencyMark(redisService.hmGet(key,"currency_mark").toString());
                if (yangTrade.getCurrencyTradeId().intValue()== CoinConst.KRW_CURRENCY_TRADE_ID){
                    yangTrade.setCurrencyTitle(CoinConst.KRW_CURRENCY_TRADE_STR);
                }else{
                    yangTrade.setCurrencyTitle(redisService.hmGet(key,"trade_mark").toString());
                }
                //保存用户姓名
                String memberKey= CoinConst.TRADE_RECORD_USER_INFO+yangTrade.getMemberId();
                if(redisService.exists(memberKey)){
                    if(redisService.get(memberKey)!=null){
                        yangTrade.setMemberName(redisService.get(memberKey).toString());
                    }
                }else{
                    Example example=new Example(YangMember.class);
                    example.createCriteria().andEqualTo("memberId",yangTrade.getMemberId());
                    List<YangMember> memberList= yangMemberMapper.selectByExample(example);
                    yangTrade.setMemberName(memberList.get(0).getName());
                    redisService.set(memberKey,memberList.get(0).getName());
                }
                redisService.lPush(CoinConst.KLINE_TRADE_DATA,JSONObject.toJSONString(yangTrade));
                }
    }


    //查询该用户的前5条 委托记录

    public void setFontUserFiveOrderRecord(YangOrders buyOrders, YangOrders sellOrders){
            if(buyOrders!=null){
                if(buyOrders.getIsShua().byteValue()!= CoinConst.ROBOT_SHUA_YES.byteValue()){
                    //更新订单状态
                    String key= CoinConst.FONT_USER_ORDERS_FIVE_RECORDS+buyOrders.getCurrencyId()+"_"+buyOrders.getCurrencyTradeId()+"_"+buyOrders.getMemberId();
                    List<YangOrders> userOrdersList=fontUserTradeFiveRecord(buyOrders);
                    redisService.set(key,JSONObject.toJSONString(userOrdersList));

                }
            }
            if(sellOrders!=null){
                if(sellOrders.getIsShua().byteValue()!= CoinConst.ROBOT_SHUA_YES.byteValue()){
                    //更新订单状态
                    String key= CoinConst.FONT_USER_ORDERS_FIVE_RECORDS+sellOrders.getCurrencyId()+"_"+sellOrders.getCurrencyTradeId()+"_"+sellOrders.getMemberId();
                    List<YangOrders> userOrdersList=fontUserTradeFiveRecord(sellOrders);
                    redisService.set(key,JSONObject.toJSONString(userOrdersList));

                }
            }
    }
    //用户前五条交易记录
    public void setUserFontFiveTradeRecord(List<YangTrade> list){
        if(list.size()>0){
            //每个币种最后一次交易时间
            redisService.set(CoinConst.FONT_TRADE_LAST_TIME+list.get(0).getCurrencyId().intValue()+"_"+list.get(0).getCurrencyTradeId().intValue(),list.get(0).getAddTime());
        }
    }

    /**
     * 标记订单出错 状态为-2
     */
    public void signOderError(Integer oderId)
    {
        YangOrders yangOrders = new YangOrders();
        yangOrders.setOrdersId(oderId);
        yangOrders.setStatus(Byte.valueOf(CoinConst.ORDER_STATUS_ERROR));
        yangOrdersMapper.updateByPrimaryKeySelective(yangOrders);

    }


    class YangTradeAddTime implements Comparator<YangTrade>{
        @Override
        public int compare(YangTrade e1, YangTrade e2) {
            if(e1.getAddTime().intValue() - e2.getAddTime().intValue()<0){
                return 1;
            }else{
                return -1;
            }
        }
    }


    /**
     *
     * @param currency_id
     * @param currency_trade_id
     * @return
     */
    private CoinPairRedisBean initCoinInfos(int currency_id, int currency_trade_id, String key)
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

                   logger.debug("找不到该币种 krw"+currency_trade_id);
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
    public boolean flushCoinInfos(int currency_id, int currency_trade_id, YangTrade yangTrade, YangCurrencyPair yangCurrencyPair1) {

        //刷新交易数据
        try {

            String key = CoinConst.TRADE2PAIR+yangCurrencyPair1.getCyId();
//            redisService.remove(key);
            if(redisService.listSize(key) > 30)
            {
                redisService.lpop(key);
            }

            HashMap hashMap = new HashMap();
            hashMap.put("num",yangTrade.getNum());
            hashMap.put("price",yangTrade.getPrice());
            hashMap.put("time",System.currentTimeMillis()/1000);
            hashMap.put("type",yangTrade.getType());
            redisService.lPush(key, JSONUtils.toJSONString(hashMap));
        }catch (Exception e)
        {
            e.printStackTrace();
        }




        //这里把交易数据 丢到交易Mondb中 后面用于统计24小时内的交易数据
        YangTradeExt yangTradeExt = new YangTradeExt();
        yangTradeExt.setPriceD(yangTrade.getPrice().doubleValue());
        yangTradeExt.setNumD(yangTrade.getNum().doubleValue());
        yangTradeExt.setAddTime(System.currentTimeMillis()/1000);

        String collectName = CoinConst.MONDB_TRADE+yangCurrencyPair1.getCyId().toString();


        mongoTemplate.insert(yangTradeExt,collectName);


        //删除24小时以外的数据
        long timeNow =     System.currentTimeMillis()/1000;
        long timeStart = timeNow - 3600*40;
        Query query = new Query();
        query.addCriteria(Criteria.where("addTime").lt(timeStart));
        mongoTemplate.remove(query, YangTradeExt.class,collectName);





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



        update24(coinPairRedisBean,yangTrade, yangCurrencyPair1);
        return  true;




    }

    /*更新24小时信息（最高价、最低价等）*/
    private void update24(CoinPairRedisBean coinPairRedisBean, YangTrade yangTrade, YangCurrencyPair yangCurrencyPair){

//        YangTradeExt yangTradeExt = new YangTradeExt();
//        yangTradeExt.setPriceD(yangTrade.getPrice().doubleValue());
//        yangTradeExt.setNumD(yangTrade.getNum().doubleValue());
//        String collectName = CoinConst.MONDB_TRADE+yangCurrencyPair1.getCyId().toString();
//        mongoTemplate.insert(yangTradeExt,collectName);
//
//
//        //删除24小时以外的数据
//        long timeNow =     System.currentTimeMillis()/1000;
//        long timeStart = timeNow - 3600*48;
//        Query query = new Query();
//        query.addCriteria(Criteria.where("addTime").lt(timeStart));
//        mongoTemplate.remove(query,yangTradeExt.class,collectName);
//        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
//        String currentDayKey = dateFormat.format(new Date());
        String collectName = CoinConst.MONDB_TRADE+yangCurrencyPair.getCyId().toString();
        String key_24h = CoinConst.REDIS_YANG_PARI_DAY_INFO_+yangTrade.getCurrencyId() + "-" + yangTrade.getCurrencyTradeId();

        long time = System.currentTimeMillis()/1000 - 24*3600;
        Criteria c=Criteria.where("addTime").gte(time);

        Query query = new Query();
        query.limit(1);
        Sort sort = new Sort("tradeId asc");
        query = query.with(sort);
        query.addCriteria(c);

        YangTradeExt yangTradeExtFirst = mongoTemplate.findOne(query, YangTradeExt.class,collectName);
        if(yangTradeExtFirst == null)
        {
            yangTradeExtFirst = new YangTradeExt();
            yangTradeExtFirst.setNumD(yangTrade.getNum().doubleValue());
            yangTradeExtFirst.setPriceD(yangTrade.getPrice().doubleValue());
        }





        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(c),
                Aggregation.group().min("priceD").as("minPrice").
                        sum("numD").as("total").
                        sum(ArithmeticOperators.Multiply.valueOf("numD").
                        multiplyBy(ArithmeticOperators.Multiply.valueOf("priceD"))).as("totalMoney").
                        max("priceD").as("maxPrice")

        );
        AggregationResults<HashMap> aggregate  =  mongoTemplate.aggregate(aggregation, collectName,HashMap.class);
        HashMap map = aggregate.getMappedResults().get(0);

        redisService.hmSet(key_24h,"h24_num",String.valueOf(map.get("total")));

        BigDecimal h24Change = yangTrade.getPrice().subtract(BigDecimal.valueOf(yangTradeExtFirst.getPriceD())).divide(BigDecimal.valueOf(yangTradeExtFirst.getPriceD()),9,BigDecimal.ROUND_HALF_DOWN);
        redisService.hmSet(key_24h,"h24_change",String.valueOf(h24Change.multiply(BigDecimal.valueOf(100))));

        redisService.hmSet(key_24h,"h24_money",String.valueOf(map.get("totalMoney")));

        /*24最高价*/
        redisService.hmSet(key_24h,"h24_max",String.valueOf(map.get("maxPrice")));
        redisService.hmSet(key_24h,"h24_min",String.valueOf(map.get("minPrice")));

        redisService.hmSet(key_24h,"first_price",String.valueOf(yangTradeExtFirst.getPriceD()));

        //最新一笔和上一笔是涨跌

        if(redisService.hmGet(key_24h,"new_price") == null)
        {

        }else{
            BigDecimal lastestPrice = new BigDecimal(String.valueOf(redisService.hmGet(key_24h,"new_price")));
            Integer price_status = yangTrade.getPrice().compareTo(lastestPrice);

            redisService.hmSet(key_24h,"price_status",String.valueOf(price_status));
        }


        /*最新价格*/
        redisService.hmSet(key_24h,"new_price",String.valueOf(yangTrade.getPrice()));



//
//
//
//        List<String> h24KeyList = Lists.newArrayList();
//        BigDecimal defaultValue = new BigDecimal(0);
//        h24KeyList.add("h24_change");
//        h24KeyList.add("h24_num");
//        h24KeyList.add("h24_money");
//
//        h24KeyList.add("h24_max");
//        h24KeyList.add("h24_min");
//
//        h24KeyList.add("new_price");
//        h24KeyList.add("price_status");
//        h24KeyList.add("first_price"); //当日第一笔价格
//        //初始化所有的24小时数据，默认为0
//        for(String keySub :h24KeyList){
//            Object storedObject = redisService.hmGet(key_24h,keySub);
//            if(null == storedObject){
//                redisService.hmSet(key_24h,keySub,String.valueOf(defaultValue));
//            }
//        }
//
//        //更新第一笔的价格
//        BigDecimal firstPrice = new BigDecimal(String.valueOf(redisService.hmGet(key_24h,"first_price")));
//        if(firstPrice.compareTo(defaultValue) == 0){
//            redisService.hmSet(key_24h,"first_price",String.valueOf(yangTrade.getPrice()));
//            firstPrice = yangTrade.getPrice();
//        }
//
//
//        //最新一笔和上一笔是涨跌
//        BigDecimal lastestPrice = new BigDecimal(String.valueOf(redisService.hmGet(key_24h,"new_price")));
//        Integer price_status = yangTrade.getPrice().compareTo(lastestPrice);
//        coinPairRedisBean.setPrice_status(price_status);
//        redisService.hmSet(key_24h,"price_status",String.valueOf(price_status));
//
//        /*最新价格*/
//        redisService.hmSet(key_24h,"new_price",String.valueOf(yangTrade.getPrice()));
//        coinPairRedisBean.setNew_price(yangTrade.getPrice());
//
//        //更新24小时涨幅
//        BigDecimal h24Change = yangTrade.getPrice().subtract(firstPrice).divide(firstPrice,9,BigDecimal.ROUND_HALF_DOWN);
//
//
//        redisService.hmSet(key_24h,"h24_change",String.valueOf(h24Change.multiply(BigDecimal.valueOf(100))));
//        coinPairRedisBean.setH24_change(h24Change.doubleValue()*100);
//
//
//        /*24数量*/
//        BigDecimal h24NumAdded = new BigDecimal(String.valueOf(redisService.hmGet(key_24h,"h24_num"))).add(yangTrade.getNum());
//        redisService.hmSet(key_24h,"h24_num",String.valueOf(h24NumAdded));
//        coinPairRedisBean.setH24_num(String.valueOf(h24NumAdded));
//
//        /*24成交额*/
//        BigDecimal h24MoneyAdded = new BigDecimal(String.valueOf(redisService.hmGet(key_24h,"h24_money"))).add(yangTrade.getNum().multiply(yangTrade.getPrice()));
//        redisService.hmSet(key_24h,"h24_money",String.valueOf(h24MoneyAdded));
//        coinPairRedisBean.setH24_money(String.valueOf(h24MoneyAdded));
//
//        /*24最高价*/
//        BigDecimal h24MaxStored = new BigDecimal(String.valueOf(redisService.hmGet(key_24h,"h24_max")));
//        BigDecimal h24Max = h24MaxStored.compareTo(yangTrade.getPrice()) == -1 ? yangTrade.getPrice() : h24MaxStored;
//        redisService.hmSet(key_24h,"h24_max",String.valueOf(h24Max));
//        coinPairRedisBean.setH24_max(String.valueOf(h24Max));
//
//        /*24最低价*/
//        BigDecimal h24MinStored = new BigDecimal(String.valueOf(redisService.hmGet(key_24h,"h24_min")));
//        BigDecimal h24Min = h24MinStored.compareTo(yangTrade.getPrice()) == 1 ? yangTrade.getPrice() : h24MinStored;
//        if(h24Min.compareTo(defaultValue) == 0){
//            h24Min = yangTrade.getPrice();
//        }
//        redisService.hmSet(key_24h,"h24_min",String.valueOf(h24Min));
//        coinPairRedisBean.setH24_min(String.valueOf(h24Min));
    }



    /*更新交易信息*/
    @Transactional
    public List<YangTrade> updateTradeInfo(YangOrders yangOrdersBuy, YangOrders yangOrdersSell, BigDecimal loopTradeNum, BigDecimal buyerTradeFeeNum, BigDecimal sellerTradeFeePrice, String type, YangCurrencyPair yangCurrencyPair){
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
        buyerTradeLog.setIsShua(yangOrdersBuy.getIsShua());
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
        sellerTradeLog.setIsShua(yangOrdersSell.getIsShua());
        list.add(sellerTradeLog);
        //更新redis数据
        this.flushCoinInfos(buyerTradeLog.getCurrencyId(),buyerTradeLog.getCurrencyTradeId(),buyerTradeLog, yangCurrencyPair);

        //更新redis中行情的数据

        return list;
    }



    /*获取买家卖家的自定义手续费配置*/
    public YangTradeFee getTradeFee(YangTradeFee yangTradeFee, boolean flash){
        String key  = CoinConst.REDIS_TRADE_FEE_MEMBER_ + yangTradeFee.getMemberId()+"_"+yangTradeFee.getCyId();

        if(redisService.get(key) != null &&  JSONObject.parseObject(redisService.get(key).toString(), YangTradeFee.class).getMemberId().equals(yangTradeFee.getMemberId()) && !flash)
        {
            return JSONObject.parseObject(redisService.get(key).toString(), YangTradeFee.class);

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
//       logger.debug(i);



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
        Map resultMap= yangOrdersMapper.dealOrder(pama);
        return resultMap;
    }

    public List<YangOrders> fontUserTradeFiveRecord(YangOrders yangOrders){
        return yangOrdersMapper.fontUserTradeFiveRecords(yangOrders);
    }

}
