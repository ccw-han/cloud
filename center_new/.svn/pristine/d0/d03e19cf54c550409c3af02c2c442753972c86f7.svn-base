package net.cyweb.task;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import cyweb.utils.CoinConst;
import net.cyweb.controller.ApiBaseController;
import net.cyweb.model.*;
import net.cyweb.service.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.math.BigDecimal;
import java.util.*;

@Configuration
@EnableScheduling
@EnableAsync
@Service
@Scope("prototype")
public class OrderDealTask extends ApiBaseController {

    @Value("${notice.url}")
    private String noticeUrl;

    private static final Integer maxRetryTimes = 5*3;

    @Autowired
    private RedisService redisService;

    @Autowired
    private YangOrderService yangOrderService;

    @Autowired
    private RobotService robotService;

    @Autowired
    private YangPairService yangPairService;

    @Autowired
    private YangMemberService yangMemberService;

    /**
     * 挂单  刷中间单
     */
    @Async
    public void midOrderShua(YangCurrencyPair yangCurrencyPair,long sleep) throws  Exception{
        try {

            Thread.sleep(sleep);

        }catch (Exception e)
        {

        }
//        YangCurrencyPair yangCurrencyPair = yangPairService.getPairInfo(y.getCurrencyId(),y.getCurrencyTradeId(),true);
        //获取  卖单 最低价
        List<String> statusList = Lists.newArrayList();
        statusList.add("0");statusList.add("1");
        Map map=new HashMap();
        map.put("currencyId",yangCurrencyPair.getCurrencyId());
        map.put("currencyTradeId",yangCurrencyPair.getCurrencyTradeId());
        map.put("status",statusList);
        map.put("hasDo",1);
        map.put("orderByClause","price asc");
        map.put("type",CoinConst.ORDER_SELL);
        YangOrders yangOrdersSell=yangOrderService.findMaxPriceSellOrder(map);

        //获取买单最高价格
        map.put("orderByClause","price desc");
        map.put("type",CoinConst.ORDER_BUY);
        YangOrders yangOrdersBuy=yangOrderService.findMaxPriceSellOrder(map);
        //如果买卖单 有一个不存在 则 抛出异常提示
        if(yangOrdersSell==null||yangOrdersBuy==null){
//            yangMemberService.sendEmailCode(CoinConst.ADMIN_EMAIL,CoinConst.EMAIL_WDC_KD_TS,"WDC卡单提示",
//                    "交易对:"+yangCurrencyPair.getCurrencyId()+"/"+yangCurrencyPair.getCurrencyTradeId()+"当前卖单最低价格+:"+yangOrdersSell.getPrice()+"<br/>当前买单最高价格:"+yangOrdersBuy.getPrice(),
//                    "127.0.0.1",0,"0-0");
            //退出 生成订单程序
            return;
        }

        //如果 买单 卖单 差距过小则  邮件提示 并 停止 挂单
        BigDecimal sellPrice=yangOrdersSell.getPrice();
        BigDecimal buyPrice=yangOrdersBuy.getPrice();
        int inputPriceNum=yangCurrencyPair.getInputPriceNum();//显示位数
        sellPrice=sellPrice.setScale(inputPriceNum,BigDecimal.ROUND_DOWN);
        buyPrice=buyPrice.setScale(inputPriceNum,BigDecimal.ROUND_DOWN);
        //生成最小价格差距  字符串
        String noShuaPrice="0.";
        for(int i=0;i<inputPriceNum-1;i++){
            noShuaPrice+="0";
        }
        noShuaPrice+="1";
        //如果卖单价格小于买单价格   或者   两种订单差距  小于等于 0.0000xxxx1
        if(sellPrice.compareTo(buyPrice)<0||(sellPrice.subtract(buyPrice).compareTo(new BigDecimal(noShuaPrice))==0)){
            //发送邮件
//            yangMemberService.sendEmailCode(CoinConst.ADMIN_EMAIL,CoinConst.EMAIL_WDC_KD_TS,"WDC卡单提示",
//                    "交易对:"+yangCurrencyPair.getCurrencyId()+"/"+yangCurrencyPair.getCurrencyTradeId()+" <br/> 当前卖单最低价格+:"+sellPrice+"<br/>当前买单最高价格:"+buyPrice,
//                    "127.0.0.1",0,sellPrice+"-"+buyPrice);
            //退出 生成订单程序
            return;
        }

        //随机 生成20~80% 价格单子
        Random random=new Random();
        int randomNum=random.nextInt(60);
        randomNum+=20;
        //计算倍率 生成要挂单的价格
        BigDecimal rate=new BigDecimal(randomNum).divide(new BigDecimal("100"));
        BigDecimal nowOrderPrice=sellPrice.subtract(buyPrice).multiply(rate).add(buyPrice).setScale(inputPriceNum,BigDecimal.ROUND_DOWN);

        //生成挂单数量
        double nums = getNums(yangCurrencyPair);  //获取这次需要挂单的数量
        nums = BigDecimal.valueOf(nums).setScale(yangCurrencyPair.getShowNum(),BigDecimal.ROUND_DOWN).doubleValue(); //限制挂单小数位数

        //生成卖单
        int typeRandom=random.nextInt(10);
        //如果是偶数 就先生成卖单2 如果是奇数就先生成买单
        if(this.existOrderPrice(nowOrderPrice,yangCurrencyPair,CoinConst.ORDER_SELL)||this.existOrderPrice(nowOrderPrice,yangCurrencyPair,CoinConst.ORDER_BUY)){
            return;
        }
        if(typeRandom%2==0){

            Result resultA = robotService.saleOrbuyRobotNologin(yangCurrencyPair,CoinConst.ORDER_SELL, nowOrderPrice ,nums, "1"); //挂第一个单 后面再挂一个单子 成交这个单子 但是不把所有数量都吃掉 这样保证有单子能挂在那里

            Thread.sleep(100L);
            Result resultB = robotService.saleOrbuyRobotNologin(yangCurrencyPair,CoinConst.ORDER_BUY, nowOrderPrice ,nums, "1"); //挂第一个单 后面再挂一个单子 成交这个单子 但是不把所有数量都吃掉 这样保证有单子能挂在那里
        }else{
            Result resultB = robotService.saleOrbuyRobotNologin(yangCurrencyPair,CoinConst.ORDER_BUY, nowOrderPrice ,nums, "1"); //挂第一个单 后面再挂一个单子 成交这个单子 但是不把所有数量都吃掉 这样保证有单子能挂在那里
            Thread.sleep(100L);
            Result resultA = robotService.saleOrbuyRobotNologin(yangCurrencyPair,CoinConst.ORDER_SELL, nowOrderPrice ,nums, "1"); //挂第一个单 后面再挂一个单子 成交这个单子 但是不把所有数量都吃掉 这样保证有单子能挂在那里
        }


    }


    /**
     * 挂单 通过外部网站模式
     */
    @Async
    public void guadanByOutSite(YangCurrencyPair yangCurrencyPair,long sleep)
    {
        //后台 修改基准价格   挂单前必须 重新抓取 配置信息
        yangCurrencyPair = yangPairService.getPairInfo(yangCurrencyPair.getCurrencyId(),yangCurrencyPair.getCurrencyTradeId(),true);

        try {

            Thread.sleep(sleep);

        }catch (Exception e)
        {

        }
        BigDecimal priceThisTimes = BigDecimal.valueOf(0);
        if(yangCurrencyPair.getRobotId() == null || yangCurrencyPair.getRobotId().equals(Integer.valueOf(0))) //设置了机器人id的 就开启刷单 否则不开启
        {
            return;
        }

        int isZhangOrDie = 0 ; //初始状态为平价 1涨价 -1 跌价



        BigDecimal priceNow = yangCurrencyPair.getBasePrice();

        BigDecimal priceLast = null;
        //查询上次 刷单基准价格
        priceLast = redisService.get(CoinConst.GUAPRICE+yangCurrencyPair.getCyId().intValue()) == null ? null :  BigDecimal.valueOf((double)redisService.get(CoinConst.GUAPRICE+yangCurrencyPair.getCyId().intValue()));

        List<String> statusList = Lists.newArrayList();
        statusList.add("0");statusList.add("1");
        Example exampleTo = new Example(YangOrders.class);
        Example.Criteria criteria =  exampleTo.createCriteria().andEqualTo("currencyId",yangCurrencyPair.getCurrencyId())
                                    .andEqualTo("currencyTradeId",yangCurrencyPair.getCurrencyTradeId())
                                    .andIn("status",statusList)
                                    .andEqualTo("hasDo",1)
                                    .andEqualTo("memberId",yangCurrencyPair.getRobotId());



        double nums = getNums(yangCurrencyPair);  //获取这次需要挂单的数量

        if(nums == 0)
        {
            return;
        }

        String type;

        //获取 当前买单和卖单  挂单总数量
        HashMap roderInfos = yangOrderService.getOrderNumsInfo(yangCurrencyPair);

        if(roderInfos != null)
        {
            if(((BigDecimal)roderInfos.get("sellNum")).compareTo((BigDecimal)roderInfos.get("buyNum")) > 0)
            {
                isZhangOrDie = 1;
            }else if(((BigDecimal)roderInfos.get("sellNum")).compareTo(  (BigDecimal)roderInfos.get("buyNum")) == 0){
                isZhangOrDie = 0;
            }else{
                isZhangOrDie = -1;
            }

        }
        if(isZhangOrDie == 1)
        {
            type = "buy";
        }else if(isZhangOrDie == -1){
            type = "sell";
        }else{
            //买卖单数量持平 就从redis获取当前挂单类型 redis没有值则  默认挂卖单
            if(redisService.get(CoinConst.GUATYPE+"_"+yangCurrencyPair.getCyId()) == null)
            {
                type = "sell";
            }else{
                type  = redisService.get(CoinConst.GUATYPE+"_"+yangCurrencyPair.getCyId().intValue()).toString();
            }

        }

        double gl = 1;

        //上次挂单价格和 当前基准价格持平
        if(null != priceLast && priceLast.compareTo(priceNow) == 0  )
        {
            //计算当前这单价格比例
            gl = this.getBl(type,15,yangCurrencyPair);

            //计算 要挂单的价格
            priceThisTimes = priceLast.multiply(BigDecimal.valueOf(gl));


        }else{
            priceThisTimes =    yangCurrencyPair.getBasePrice();
            redisService.set(CoinConst.GUAPRICE+yangCurrencyPair.getCyId().intValue(),priceNow.doubleValue());
        }


        if(priceThisTimes.compareTo(BigDecimal.ZERO) <= 0) // 防止挂单为负数的情况出现
        {
            System.out.println("价格为负数");
            return;
        }


//        System.out.println("当前挂单类别"+type);

        priceThisTimes = priceThisTimes.setScale(yangCurrencyPair.getInputPriceNum(),BigDecimal.ROUND_DOWN);


        //查询一下这个价格是否已经挂单了 如果挂单了  就不要再挂了 给一次机会修改价格增加 如果还是不行 就放弃
        if(this.priceJz(priceThisTimes,yangCurrencyPair) == null)
        {
            System.out.println("------- 价格不合适------放弃-------");
            //再来一次范围大的 不行就放弃
            gl = this.getBl(type,1,yangCurrencyPair);
            priceThisTimes = priceLast.multiply(BigDecimal.valueOf(gl));
            priceThisTimes = priceThisTimes.setScale(yangCurrencyPair.getInputPriceNum(),BigDecimal.ROUND_DOWN);

            if(this.priceJz(priceThisTimes,yangCurrencyPair) == null)
            {
                System.out.println("还是不行 放弃");
                return;
            }
            return;
        }

        if(type == "sell" || type.equals("sell"))
        {

//            System.out.println("设置为买");
            redisService.set(CoinConst.GUATYPE+"_"+yangCurrencyPair.getCyId().intValue(),"buy");
//            System.out.println("设置成功了buy");
        }else{
            redisService.set(CoinConst.GUATYPE+"_"+yangCurrencyPair.getCyId().intValue(),"sell");
//            System.out.println("设置成功了sell");


        }

        nums = BigDecimal.valueOf(nums).setScale(yangCurrencyPair.getShowNum(),BigDecimal.ROUND_DOWN).doubleValue(); //限制挂单小数位数

        Result result = robotService.saleOrbuyRobotNologin(yangCurrencyPair,type, priceThisTimes ,nums, "1"); //挂第一个单 后面再挂一个单子 成交这个单子 但是不把所有数量都吃掉 这样保证有单子能挂在那里

        int ordersId = (int)result.getData();

    }



    //修正后的正常挂单模式
//    @Async
    @Transactional
    public void guadanByModifyOutSite(YangCurrencyPair yangCurrencyPair,long sleep)
    {
        //获取 实时 basePrice
        YangCurrencyPair yc=yangPairService.getPairInfo(yangCurrencyPair.getCurrencyId(),yangCurrencyPair.getCurrencyTradeId(),false);
        yangCurrencyPair.setBasePrice(yc.getBasePrice());
        try {
            Thread.sleep(sleep);
        }catch (Exception e)
        {
        }
         if(yangCurrencyPair.getRobotId() == null || yangCurrencyPair.getRobotId().equals(Integer.valueOf(0))) //设置了机器人id的 就开启刷单 否则不开启
        {
            return;
        }

           String type="";//默认生成卖单
        String redisGuaPriceType;
         int isZhangOrDie = 0 ; //初始状态为平价 1涨价 -1 跌价
        //获取当前基准价格
        BigDecimal priceNow = yangCurrencyPair.getBasePrice();
        //从redis   查询上次 刷单基准价格
        BigDecimal priceLast = redisService.get(CoinConst.GUAPRICE+yangCurrencyPair.getCyId().intValue()) == null ? new BigDecimal(0) :new BigDecimal(redisService.get(CoinConst.GUAPRICE+yangCurrencyPair.getCyId().intValue()).toString());

        // 刷单逻辑
        //
        //    价格没有变动
        //          下次挂单 采取   guaprice_sell  或 guaprice_buy
        //   价格变动
        //          下次挂单 采取 basePrice  并根据参数设置差距  并 重新 缓存 guaprice_sell 或guaprice_buy

        //
        // 1  正常 挂单逻辑  查询上次挂单类型 反向挂买卖单  根据设定的买卖单价格 区间 挂单
        //
        //
        // 2  价格 变动的时候     原来撤单的逻辑  改成  成单
//                           2.1 如果是涨价 则只查询  前5条 符合条件的卖单  生成买单  并成交  随机买单 数量 1~3单   然后  用 算法比较 后续的挂单 是否满足差价区间条件 满足 就留着不满足撤掉
//                           2.2 如果是 跌价  则 相反逻辑
//      3  价格涨跌  区间 三种情况    1 在上面 2 在下面 3 卡中间



        //新逻辑开始

        //读取 当前交易对的所有没有对手单的挂单数量
//        HashMap orderInfos = yangOrderService.getOrderNumsInfo(yangCurrencyPair);

        //1 当前基准价格与上次 基准价格一致   采用 旧的生成订单逻辑
        if(null != priceLast && priceLast.compareTo(priceNow) == 0){
//            oldGuaOrderDeal(orderInfos,type,yangCurrencyPair,priceNow);
            return;
        }
        //2   当前基准价格与上次基准价格不一致   新挂单方法
        if(null != priceLast && priceLast.compareTo(priceNow)!=0){
            //先记录 当前挂单基准价格
             redisService.set(CoinConst.GUAPRICE+yangCurrencyPair.getCyId().intValue(),priceNow.toString());
            //核心算法 ！！！！！！！！！！！！！！！！！！！！
            //2.1 涨价
            if(priceLast.compareTo(priceNow)<0){
//                redisService.set(CoinConst.GUAPRICE_SELL+yangCurrencyPair.getCyId().toString(),yangCurrencyPair.getBasePrice());
                 risePrice(CoinConst.ORDER_SELL,CoinConst.ORDER_BUY, CoinConst.GUAPRICE_SELL,yangCurrencyPair,priceLast,priceNow);
            }
            //2.2 跌价
            if(priceLast.compareTo(priceNow)>0){
//                redisService.set(CoinConst.GUAPRICE_BUY+yangCurrencyPair.getCyId().toString(),yangCurrencyPair.getBasePrice());
                risePrice(CoinConst.ORDER_BUY,CoinConst.ORDER_SELL,CoinConst.GUAPRICE_BUY,yangCurrencyPair,priceLast,priceNow);
            }
        }
    }


    //旧的挂单逻辑
    public void oldGuaOrderDeal(HashMap orderInfos,String type,YangCurrencyPair yangCurrencyPair,BigDecimal priceNow){
        //获取生成订单数量
        //获取需要挂单的数量
        double nums = getModifyNum(yangCurrencyPair);
        //挂单数量为0   停止挂单
        if(nums == 0)
        {
            return;
        }
        // 1.1 判断这单 订单类型  获取 当前买单和卖单  挂单总数量
        if(orderInfos != null)
        {
            BigDecimal sellOrderNums=(BigDecimal)orderInfos.get("sellNum");
            BigDecimal buyOrderNums=(BigDecimal)orderInfos.get("buyNum");
            //卖单数量大于30单
            if(sellOrderNums.compareTo(new BigDecimal(CoinConst.MAXORDERNUMS))>0||sellOrderNums.compareTo(buyOrderNums) > 0&&buyOrderNums.compareTo(new BigDecimal(CoinConst.MAXORDERNUMS))<0){
                type = CoinConst.ORDER_BUY;
            }
            //买单数量大于30单
            if(buyOrderNums.compareTo(new BigDecimal(CoinConst.MAXORDERNUMS))>0||sellOrderNums.compareTo(buyOrderNums) < 0&&sellOrderNums.compareTo(new BigDecimal(CoinConst.MAXORDERNUMS))<0){
                type = CoinConst.ORDER_SELL;
            }
        }
        if(!StringUtils.isNotEmpty(type)){
            return;
        }
        //1.2 生成订单价格  根据  guaprice_sell  或 guaprice_buy
        BigDecimal priceThisTimes=this.getModifyPrice(yangCurrencyPair,type,0).setScale(yangCurrencyPair.getInputPriceNum(),BigDecimal.ROUND_DOWN);
        if(priceThisTimes.compareTo(BigDecimal.ZERO) <= 0) // 防止挂单价格为负数的情况出现
        {
            System.out.println("价格为负数");
            return;
        }
        //缓存当前生成订单的  买卖单价格
        if(CoinConst.ORDER_BUY.equals(type)){
            redisService.set(CoinConst.GUAPRICE_BUY+yangCurrencyPair.getCyId().intValue(),priceThisTimes.toString());
        }
        if(CoinConst.ORDER_SELL.equals(type)){
            redisService.set(CoinConst.GUAPRICE_SELL+yangCurrencyPair.getCyId().intValue(),priceThisTimes.toString());
        }

        if(this.existOrderPrice(priceThisTimes,yangCurrencyPair,type)){
            //当前订单已存在  退出程序
            System.out.println("当前价格订单 已存在");
            return;
        }
        //缓存当前生成订单的基准价格
        redisService.set(CoinConst.GUAPRICE+yangCurrencyPair.getCyId().intValue(),priceNow.toString());

        nums = BigDecimal.valueOf(nums).setScale(yangCurrencyPair.getShowNum(),BigDecimal.ROUND_DOWN).doubleValue(); //限制挂单小数位数

        Result result = robotService.saleOrbuyRobotNologin(yangCurrencyPair,type, priceThisTimes ,nums, "1"); //挂第一个单 后面再挂一个单子 成交这个单子 但是不把所有数量都吃掉 这样保证有单子能挂在那里

        int ordersId = (int)result.getData();
    }

    //新算法 涨跌单
//    @Async
    public void risePrice(String type,String eatType,String redisGuaPriceType,YangCurrencyPair yangCurrencyPair,BigDecimal priceLast,BigDecimal priceNow){
        //1  在涨价区间里的卖单
        //检索  买卖单价格 在本次基准价格 下的订单  (priceLst,priceNow]
        List<YangOrders> yangOrdersList  = yangOrderService.getQueryCondition(yangCurrencyPair,"0",type,priceLast,priceNow);
        //检索  卖单价格 大于 本次基准价格的卖单
        List<YangOrders> yangOrdersOutList  = yangOrderService.getQueryCondition(yangCurrencyPair,"1",type,priceLast,priceNow);
        //实际可以 生成对手单的 区间内 挂单
//        int realSum=0;
        //如果涨价区间内  有订单 则 吃单 没有则 只考虑挂单
        this.eatOrderDeal(yangOrdersList,yangCurrencyPair,eatType);
        //2如果 涨价范围内的卖或买单 是当前全部在挂的卖单 或者  买买卖单当前挂单数量为0   开启紧急 挂单模式
        if(yangOrdersList.size()==CoinConst.FASTMAKERORDERNUMS&&yangOrdersOutList.size()==0||(yangOrdersList.size()==0&&yangOrdersOutList.size()==0)){
            //快速生成 23单 卖单或买单
            makeOrderFast(priceNow,yangCurrencyPair,type,redisGuaPriceType,CoinConst.FASTMAKERORDERNUMS);
        }
        //3如果 涨价范围内的卖单  不是全部在挂的卖单 则   (priceNow,∞]
        if(yangOrdersOutList.size()>0){
        // 在涨价区间外的卖单  判断每一单是否 符合挂单规则
            judgeCancelOrKeepOrder(yangOrdersOutList,priceNow,yangCurrencyPair,type,redisGuaPriceType);
        }
        //4涨或跌 反向挂单
        this.zhangOrDieReverseOrder(type,yangCurrencyPair,priceLast,priceNow);
    }

    public void eatOrderDeal(List<YangOrders> yangOrdersList,YangCurrencyPair yangCurrencyPair,String eatType){
        //如果涨价区间内  有订单 则 吃单 没有则 只考虑挂单
        if(yangOrdersList!=null&&yangOrdersList.size()>0){
            //吃掉 涨价范围内的订单 每笔单子分 1~3单吃完    实际上就是生成同等价位的买单  让买单机器人去处理成交
            for(YangOrders yangOrders:yangOrdersList){
                //如果当前价格的  吃单 已经存在 且 数量一致 则进行下一单的循环操作

                if(this.existOrderPrice(yangOrders.getPrice(),yangCurrencyPair,eatType)){
                    continue;
                }
                //吃单  分单算法
                List<BigDecimal> eatNumList=this.getAvgNumList(yangOrders.getNum(),yangCurrencyPair.getShowNum());//生成单数
                //生成订单
                for(BigDecimal bd:eatNumList){
                    robotService.saleOrbuyRobotNologin(yangCurrencyPair,eatType, yangOrders.getPrice() ,bd.doubleValue(), "1");
                }
            }
        }
    }

    //涨跌单  相反方向 订单 全部重新 判断撤单还是挂单
    public void  zhangOrDieReverseOrder(String type,YangCurrencyPair yangCurrencyPair,BigDecimal priceLast,BigDecimal priceNow){
        String redisGuaPriceType;
        if(CoinConst.ORDER_BUY.equals(type)){
            type=CoinConst.ORDER_SELL;
            redisGuaPriceType=CoinConst.GUAPRICE_SELL;
        }else{
            type=CoinConst.ORDER_BUY;
            redisGuaPriceType=CoinConst.GUAPRICE_BUY;
        }
        List<YangOrders> yangOrdersOutList  = yangOrderService.getQueryCondition(yangCurrencyPair,"2",type,priceLast,priceNow);
        judgeCancelOrKeepOrder(yangOrdersOutList,priceNow,yangCurrencyPair,type,redisGuaPriceType);

        //反向单 多考虑 一个 在当前基准价 外的 本不该在此的订单   多线程造成的 基准价不一致
//        List<YangOrders> list  = yangOrderService.getQueryCondition(yangCurrencyPair,"3",type,priceLast,priceNow);
//        //撤单
//        if(list!=null&&list.size()>0){
//            for(int i=0;i<list.size();i++){
//                redisService.rPush(CoinConst.MQ_CANCEL_ORDER_QUEUE,list.get(i).getOrdersId());
//            }
//        }
    }




    //涨价范围外的挂单  同步判断保留还是撤销 撤销单塞入redis缓存
    public void  judgeCancelOrKeepOrder(List<YangOrders> yangOrdersOutList,BigDecimal basePrice,YangCurrencyPair yangCurrencyPair,String type,String redisGuaPriceType){
        if(yangOrdersOutList==null||yangOrdersOutList.size()==0){
            this.makeOrderFast(basePrice,yangCurrencyPair,type,redisGuaPriceType,CoinConst.FASTMAKERORDERNUMS);
        }else{
            //判断 新基准价格 之外的订单的逻辑
            everOurSiteOrderDeal(yangOrdersOutList,yangCurrencyPair,basePrice,type,redisGuaPriceType);
            if(yangOrdersOutList.size()<CoinConst.FASTMAKERORDERNUMS){
                this.makeOrderFast(new BigDecimal(redisService.get(redisGuaPriceType+yangCurrencyPair.getCyId()).toString()),yangCurrencyPair,type,redisGuaPriceType,CoinConst.FASTMAKERORDERNUMS-yangOrdersOutList.size());
            }

        }
    }

    //紧急挂单
    public void makeOrderFast(BigDecimal basePrice,YangCurrencyPair yangCurrencyPair,String type,String redisGuaPriceType,int step){
        BigDecimal quickGuaDanLast=basePrice;
        for(int i=0;i<step;i++){
            //获取一个最大挂单价格和最小挂单价格
            quickGuaDanLast =this.getModifyPrice(yangCurrencyPair,type,i+1).setScale(yangCurrencyPair.getInputPriceNum(),BigDecimal.ROUND_DOWN);
            if(quickGuaDanLast.compareTo(BigDecimal.ZERO) <= 0) // 防止挂单价格为负数的情况出现
            {
                System.out.println("价格为负数");
                break;
            }
            redisService.set(redisGuaPriceType+yangCurrencyPair.getCyId(),quickGuaDanLast.toString());
            double everNums = getModifyNum(yangCurrencyPair);
            Result result= robotService.saleOrbuyRobotNologin(yangCurrencyPair,type, quickGuaDanLast ,everNums, "1"); //挂第一个单 后面再挂一个单子 成交这个单子 但是不把所有数量都吃掉 这样保证有单子能挂在那里
            System.out.println("1");
        }
    }

    //涨价范围外的挂单 每笔订单的  具体挂单撤单逻辑
    public void everOurSiteOrderDeal(List<YangOrders> list,YangCurrencyPair yangCurrencyPair,BigDecimal basePrice,String type,String redisGuaPriceType){
        for(int i=0;i<list.size();i++){
            if(i>CoinConst.MAXORDERNUMS){
                redisService.rPush(CoinConst.MQ_CANCEL_ORDER_QUEUE,list.get(i).getOrdersId());
                continue;
            }
            YangOrders yangOrders=list.get(i);
            Map map=getPriceDifDeail(Long.valueOf(i+1),type,yangCurrencyPair);
            String priceMin=(String)map.get("priceMin");
            String priceMax=(String)map.get("priceMax");
            if(CoinConst.ORDER_SELL.equals(type)){
                if(yangOrders.getPrice().subtract(basePrice).compareTo( new BigDecimal(priceMin))>=0&&
                        yangOrders.getPrice().subtract(basePrice).compareTo( new BigDecimal(priceMax))<=0
                ){

                    basePrice=yangOrders.getPrice();
                    if(this.existDSOrderPrice(basePrice,yangCurrencyPair,type)){
                        basePrice=this.everOutSiteOrderDealDetail(yangCurrencyPair,type,yangOrders,redisGuaPriceType,priceMin,priceMax,basePrice);
                    }else{
                        redisService.set(redisGuaPriceType+yangCurrencyPair.getCyId(),yangOrders.getPrice().toString());
                    }
                }else{
                    //非正常价格挂单 处理
                    basePrice=this.everOutSiteOrderDealDetail(yangCurrencyPair,type,yangOrders,redisGuaPriceType,priceMin,priceMax,basePrice);
                }
            }
            if(CoinConst.ORDER_BUY.equals(type)){
                //第一单
                if(basePrice.subtract(yangOrders.getPrice()).compareTo( new BigDecimal(priceMin))>=0&&
                        basePrice.subtract(yangOrders.getPrice()).compareTo( new BigDecimal(priceMax))<=0
                ){
                    basePrice=yangOrders.getPrice();
                    if(this.existDSOrderPrice(basePrice,yangCurrencyPair,type)){
                        basePrice=this.everOutSiteOrderDealDetail(yangCurrencyPair,type,yangOrders,redisGuaPriceType,priceMin,priceMax,basePrice);
                    }else{
                        redisService.set(redisGuaPriceType+yangCurrencyPair.getCyId(),yangOrders.getPrice().toString());
                    }
                }else{
                    //非正常价格挂单 处理
                    basePrice=this.everOutSiteOrderDealDetail(yangCurrencyPair,type,yangOrders,redisGuaPriceType,priceMin,priceMax,basePrice);
                }

            }
        }
    }


    //涨价范围外的每笔订单判定后的操作
    public BigDecimal everOutSiteOrderDealDetail(YangCurrencyPair yangCurrencyPair,String type,YangOrders yangOrders,String redisGuaPriceType,String minPrice,String maxPirce,BigDecimal basePrice){
        //生成新的第一单 同时撤掉旧的第一单
        double nums = getModifyNum(yangCurrencyPair);
        BigDecimal  firstOrderPrice=this.getRandomPrice(minPrice,maxPirce,basePrice,type,yangCurrencyPair.getInputPriceNum());
        if(!this.existOrderPrice(firstOrderPrice,yangCurrencyPair,type)&&!this.existDSOrderPrice(firstOrderPrice,yangCurrencyPair,type)){
            robotService.saleOrbuyRobotNologin(yangCurrencyPair,type, firstOrderPrice ,nums, "1"); //挂第一个单 后面再挂一个单子 成交这个单子 但是不把所有数量都吃掉 这样保证有单子能挂在那里
            redisService.rPush(CoinConst.MQ_CANCEL_ORDER_QUEUE,yangOrders.getOrdersId());
        }
        basePrice=firstOrderPrice;
        //撤单 放入 redis
        redisService.set(redisGuaPriceType+yangCurrencyPair.getCyId(),firstOrderPrice.toString());
        return basePrice;
    }



    //获取 涨跌 单的 吃单 数量
    public List<BigDecimal> getAvgNumList(BigDecimal eatNum,int numLength){
        List<BigDecimal> list=new ArrayList<BigDecimal>();
        //生成两次随机数量
        BigDecimal a=this.getAvgNum(eatNum,numLength);
        BigDecimal b=this.getAvgNum(eatNum,numLength);
        //如果生成的数量 大于等于 总价格 则取 总价格
        if(a.compareTo(eatNum)>=0){
            list.add(a);
        }
        //生成的数量小于总数量 看第二个随机数量值
        if(a.compareTo(eatNum)<0){
            if(a.add(b).compareTo(eatNum)>=0){
                list.add(a);
                list.add(eatNum.subtract(a));
            }
            if(a.add(b).compareTo(eatNum)<0){
                list.add(a);
                list.add(b);
                list.add(eatNum.subtract(a).subtract(b));
            }

        }
        return list;
    }


    //随机  获取订单数量
    public BigDecimal getAvgNum(BigDecimal total,int numLength){
        String totalStr=total.toPlainString();
        return this.getRandomPrice("0",totalStr,BigDecimal.valueOf(0),CoinConst.ORDER_SELL,numLength);
    }

    //修正后的 挂单价格
    public BigDecimal getModifyPrice(YangCurrencyPair yangCurrencyPair,String type,int number){
        BigDecimal priceThisTime=new BigDecimal(0);
        //如果是  卖单 则 增加 价格
        //获取一个最大挂单价格和最小挂单价格
        Map map=getPriceDifVal(yangCurrencyPair,type,number);
        BigDecimal basePrice=BigDecimal.valueOf(0);
        if(CoinConst.ORDER_SELL.equals(type)){
            if(redisService.exists(CoinConst.GUAPRICE_SELL+yangCurrencyPair.getCyId().toString())){
                basePrice = new BigDecimal(redisService.get(CoinConst.GUAPRICE_SELL+yangCurrencyPair.getCyId().toString()).toString());
            }else{
                basePrice=yangCurrencyPair.getBasePrice();
            }
            priceThisTime=getRandomPrice((String)map.get("priceMin"),(String)map.get("priceMax"),basePrice,type,yangCurrencyPair.getInputPriceNum());
        }
        //如果是买单 则 减少价格
        if(CoinConst.ORDER_BUY.equals(type)){
            if(redisService.exists(CoinConst.GUAPRICE_BUY+yangCurrencyPair.getCyId().toString())){
                basePrice = new BigDecimal(redisService.get(CoinConst.GUAPRICE_BUY+yangCurrencyPair.getCyId().toString()).toString());
            }else{
                basePrice=yangCurrencyPair.getBasePrice();
            }
            priceThisTime=getRandomPrice((String)map.get("priceMin"),(String)map.get("priceMax"),basePrice,type,yangCurrencyPair.getInputPriceNum());
        }
        return priceThisTime;

    }
    //计算  实际生成的订单价格  和 生成订单数量  通用 方法
    public BigDecimal getRandomPrice(String minStr,String maxStr,BigDecimal basePrice,String type,int numLength){
        BigDecimal min= new BigDecimal(minStr);//去除小数点后的  价格 整数
        BigDecimal max=new BigDecimal(maxStr);//去除小数点后的 价格 整数
        //获取随机数
        BigDecimal price=new BigDecimal(Math.random()).multiply(max.subtract(min)).add(min);
        //卖单 则 +
        if(CoinConst.ORDER_SELL.equals(type)){
            //转换成double
            return (basePrice.add(price).setScale(numLength, BigDecimal.ROUND_DOWN));
        }
        //买单 则 -
        if(CoinConst.ORDER_BUY.equals(type)){
            //转换成double
            return (basePrice.subtract(price).setScale(numLength, BigDecimal.ROUND_DOWN));
        }
        return BigDecimal.valueOf(0);
    }

    //修正后的挂单数量
    public double getModifyNum(YangCurrencyPair yangCurrencyPair){
        YangCurrencyPairExtendMidOrderJSON yj=yangCurrencyPair.getYangCurrencyPairExtendMidOrder();
        //获取 随机数
        Random random=new Random();
        int result=random.nextInt(10);
        double r=Double.valueOf(result*10);
        Integer inputPriceNum = yangCurrencyPair.getShowNum();
        double max;
        double min;

        if(r<= yj.getOneProbability())  //落入概率为1
        {
            min  = yj.getOneParaNumMin();
            max  = yj.getOneParaNumMax();
        }else if( r > yj.getOneProbability() && r <=(yj.getOneProbability()+yj.getTwoProbability())){
            min  = yj.getTwoParaNumMin();
            max  = yj.getTwoParaNumMax();
        }else{
            min  = yj.getThreeParaNumMin();
            max  = yj.getThreeParaNumMax();
        }

        int wei = this.getNumberDecimalDigits(min);  //小数的数量

        //获取随机数
        double nums = getRandomNum(min,max);
        BigDecimal   b   =   new   BigDecimal(nums);
        nums   =   b.setScale(inputPriceNum,   BigDecimal.ROUND_DOWN).doubleValue();
        if(nums > max || nums < min)
        {
            System.out.println("随机数量超出限制"+nums+" max:"+max+"  min:"+min);
            return 0;
        }
        return nums;
    }


    //获取当前摆单价格区间
    public Map getPriceDifVal(YangCurrencyPair yangCurrencyPair,String type,int number){
        Long a=0L;
        //number为0 代表 旧逻辑生成订单 1 为紧急挂单逻辑
         if(number==0){

             YangOrders yangOrders=new YangOrders();
             yangOrders.setCurrencyId(yangCurrencyPair.getCurrencyId());
             yangOrders.setCurrencyTradeId(yangCurrencyPair.getCurrencyTradeId());
             yangOrders.setType(type);
             a=yangOrderService.countOrderNumByCondition(yangOrders);
            a=a+1;
        }else{
            a=Long.valueOf(number);
         }
        return getPriceDifDeail(a,type,yangCurrencyPair);
    }
    //获取当前摆单价格区间  详细
    public Map getPriceDifDeail(Long a,String type,YangCurrencyPair yangCurrencyPair) {
        Map map = new HashMap();
        YangCurrencyPairExtendMidOrderJSON yangCurrencyPairExtendMidOrderJSON = yangCurrencyPair.getYangCurrencyPairExtendMidOrder();
        //卖单价格区间
        if (CoinConst.ORDER_SELL.equals(type)) {
            String sellCondition = yangCurrencyPairExtendMidOrderJSON.getSellCondition();
            List<YangCurrencyPairCondition> sellList = JSONArray.parseArray(sellCondition, YangCurrencyPairCondition.class);
            for (YangCurrencyPairCondition yangCurrencyPairCondition : sellList) {
                if (a>=yangCurrencyPairCondition.getNumberMin()  && yangCurrencyPairCondition.getNumberMax() >= a) {
                    map.put("priceMin", yangCurrencyPairCondition.getPriceMin().toString());
                    map.put("priceMax", yangCurrencyPairCondition.getPriceMax().toString());
                }
            }
        }
        //买单价格区间
        if (CoinConst.ORDER_BUY.equals(type)) {
            String buyCondition = yangCurrencyPairExtendMidOrderJSON.getBuyCondition();
            List<YangCurrencyPairCondition> buyList = JSONArray.parseArray(buyCondition, YangCurrencyPairCondition.class);
            for (YangCurrencyPairCondition yangCurrencyPairCondition : buyList) {
                if (a>=yangCurrencyPairCondition.getNumberMin() && yangCurrencyPairCondition.getNumberMax() >= a) {
                    map.put("priceMin", yangCurrencyPairCondition.getPriceMin().toString());
                    map.put("priceMax", yangCurrencyPairCondition.getPriceMax().toString());
                    break;
                }
            }
        }
        return map;
    }


    public double getBl(String type,int multiple,YangCurrencyPair yangCurrencyPair)
    {
        //如果价格没有更新 就按照当前价格 随机千分之1-5涨跌来挂单


        double bl = yangCurrencyPair.getShuaBl();

        Double bl_min = yangCurrencyPair.getShuaBlMin();
        if(null == bl_min || bl_min.compareTo(Double.valueOf(0)) <=0 )
        {
            int ibl = getNumberDecimalDigits(bl);
            bl_min = 1/Math.pow(10,ibl);
        }else{
//            System.out.println(yangCurrencyPair);
        }

        bl = this.getRandomNum(bl_min,bl);

        Random random = new Random();




        int upOrDown = random.nextInt(2); //如果是0 则是涨 如果是1 则是跌
//        double r = random.nextDouble()/100;
        double gl = 1;

        if(type.equals("buy")) //如果是买
        {
            if(upOrDown==0){
                gl = gl + bl;
            }else{
                gl = gl - bl*multiple;
            }
        }else{
            if(upOrDown==0){
                gl = gl + bl*multiple;
            }else{
                gl = gl - bl;
            }
        }
        return gl;

    }

    /**
     * 修正挂单价格 两次机会 --  查找当前价格的挂单
     * @return
     */
    public BigDecimal priceJz(BigDecimal priceNow,YangCurrencyPair yangCurrencyPair)
    {

        List statusList  = new LinkedList();
        statusList.add(0);statusList.add(1);
        Example example = new Example(YangOrders.class);
        example.createCriteria()
                .andEqualTo("memberId",yangCurrencyPair.getRobotId())
                .andEqualTo("currencyId",yangCurrencyPair.getCurrencyId())
                .andEqualTo("currencyTradeId",yangCurrencyPair.getCurrencyTradeId())
                .andIn("status",statusList)
                .andEqualTo("price",priceNow);
        List<YangOrders> list = yangOrderService.selectByExample(example);

        if(list==null ||  list.size() == 0)
        {
            return priceNow;
        }

        return null;  //价格不对 返回null

    }

    public boolean existDSOrderPrice(BigDecimal priceNow,YangCurrencyPair yangCurrencyPair,String type){
        String eatType="";
        if(CoinConst.ORDER_BUY.equals(type)){
            eatType=CoinConst.ORDER_SELL;
        }
        if(CoinConst.ORDER_SELL.equals(type)){
            eatType=CoinConst.ORDER_BUY;
        }

        List statusList  = new LinkedList();
        statusList.add(0);statusList.add(1);
        Example example = new Example(YangOrders.class);
        example.createCriteria()
                .andEqualTo("memberId",yangCurrencyPair.getRobotId())
                .andEqualTo("currencyId",yangCurrencyPair.getCurrencyId())
                .andEqualTo("currencyTradeId",yangCurrencyPair.getCurrencyTradeId())
                .andEqualTo("hasDo","0")
                .andIn("status",statusList)
                .andEqualTo("price",priceNow).andEqualTo("type",eatType);
        List<YangOrders> list = yangOrderService.selectByExample(example);

        if(list==null ||  list.size() == 0)
        {
            return false;
        }

        return true;  //价格不对 返回null
    }

    public boolean existOrderPrice(BigDecimal priceNow,YangCurrencyPair yangCurrencyPair,String eatType)
    {

        List statusList  = new LinkedList();
        statusList.add(0);statusList.add(1);
        Example example = new Example(YangOrders.class);
        example.createCriteria()
                .andEqualTo("memberId",yangCurrencyPair.getRobotId())
                .andEqualTo("currencyId",yangCurrencyPair.getCurrencyId())
                .andEqualTo("currencyTradeId",yangCurrencyPair.getCurrencyTradeId())
                .andIn("status",statusList)
                .andEqualTo("price",priceNow).andEqualTo("type",eatType);
        List<YangOrders> list = yangOrderService.selectByExample(example);

        if(list==null ||  list.size() == 0)
        {
            return false;
        }

        return true;  //价格不对 返回null

    }

    //获取 配置中的  三段 数量
    private double  getNums(YangCurrencyPair yangCurrencyPair)
    {
        Random random=new Random();

        int result=random.nextInt(10);

        Integer inputPriceNum = yangCurrencyPair.getShowNum();

        double max;

        double min;



        if(result <= yangCurrencyPair.getProbability1())  //落入概率为1
        {
            min  = yangCurrencyPair.getMin1();
            max  = yangCurrencyPair.getMax1();
        }else if( result > yangCurrencyPair.getProbability1() && result <= yangCurrencyPair.getProbability2()){
            min  = yangCurrencyPair.getMin2();
            max  = yangCurrencyPair.getMax2();
        }else{
            min  = yangCurrencyPair.getMin3();
            max  = yangCurrencyPair.getMax3();
        }

        int wei = this.getNumberDecimalDigits(min);  //小数的数量

        //获取随机数
        double nums = getRandomNum(min,max);
        BigDecimal   b   =   new   BigDecimal(nums);
        nums   =   b.setScale(inputPriceNum,   BigDecimal.ROUND_DOWN).doubleValue();


        if(nums > max || nums < min)
        {
            System.out.println("随机数量超出限制"+nums+" max:"+max+"  min:"+min);
            return 0;
        }
        return nums;

    }


    //获取 最大价格和最小价格之间的随机价格
    private double getRandomNum(double min,double max)
    {

        double boundedDouble = min + new Random().nextDouble() * (max - min);
//        double d = min + Math.random() * max % (max - min + 1);
        return boundedDouble;
    }


    //获取小数 位数
    private int getNumberDecimalDigits(Double balance) {
        int dcimalDigits = 0;
        String balanceStr = String.valueOf(balance);
        int indexOf = balanceStr.indexOf(".");
        if(indexOf > 0){
            dcimalDigits = balanceStr.length() - 1 - indexOf;
        }
        return dcimalDigits;
    }

    public static  void  main(String[] args)
    {
        Random random = new Random();
        int r = random.nextInt(5);

        int upOrDown = random.nextInt(2); //如果是0 则是涨 如果是1 则是跌
        System.out.println(upOrDown);

    }




}
