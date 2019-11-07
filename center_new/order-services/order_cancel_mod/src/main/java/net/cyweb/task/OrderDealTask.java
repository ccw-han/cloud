package net.cyweb.task;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import cyweb.utils.CoinConst;
import cyweb.utils.HttpRequest;
import net.cyweb.controller.ApiBaseController;
import net.cyweb.mapper.YangOrdersMapper;
import net.cyweb.model.*;
import net.cyweb.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.math.BigDecimal;
import java.math.BigInteger;
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
    private YangPairService yangPairService;

    @Autowired
    private YangOrdersMapper yangOrdersMapper;


    @Autowired
    UserProPs userProPs;


    @Async
    public void guadan() throws Exception
    {
//        Example example = new Example(YangCurrencyPair.class);
//        example.createCriteria().andIsNotNull("robotId");
        List<YangCurrencyPair> list;

        while (true)
        {
            if(redisService.exists(CoinConst.ROBOT_CURRENCY_PAIR_REDIS)){
                list= JSONArray.parseArray(JSONArray.toJSONString(redisService.get(CoinConst.ROBOT_CURRENCY_PAIR_REDIS)), YangCurrencyPair.class);
            }else{
                System.out.println("未读取到配置 退出 撤单程序");
                return ;
            }

            //无限撤单

//            Thread.sleep(2000);
//            Example example1=new Example(YangOrders.class);
//            List<String> statusList=new ArrayList<String>();
//            statusList.add("0");
//            statusList.add("1");
//            example1.createCriteria().andEqualTo("memberId",2029).andIn("status",statusList);
//            List<YangOrders> alist=yangOrdersMapper.selectByExample(example1);
//            for(YangOrders yangOrders:alist){
//
//            int i=yangOrderService.cancel_order(yangOrders.getOrdersId());
//                if(i==1){
//                    System.out.println("撤单失败"+yangOrders.getOrdersId());
//                }else{
//                    System.out.println("撤单成功"+yangOrders.getOrdersId());
//                }
//            }
//
                Random random = new Random();
//                long timeSleep = random.nextInt(2000)+500;
                //遍历配置信息  撤单
                for (YangCurrencyPair y :list) {
                    try {
                        //如果是刷中间单则 不 走  正常撤单逻辑
                        if(y.getIsMidRobot()==CoinConst.ROBOT_SHUA_TYPE_MID){
                            this.midShuaCheOrder(y,1);
                        }else if(y.getIsMidRobot()==CoinConst.ROBOT_SHUA_TYPE_GUAANDMID){
                            //边挂单边刷中间单
                            this.midShuaCheOrder(y,1);
//                            this.guadanByOutSite(y,1);
                            this.cancelOrderByMideAndShua(y,1);
                            this.cancelOrderByNums(y);
                        }else if(y.getIsMidRobot()==CoinConst.ROBOT_SHUA_TYPE_NORMAL){
                        //正常撤单
                            this.guadanByOutSite(y,1);
                        }


                    }catch (Exception e)
                    {
                        e.printStackTrace();
                    }

                }


        }

    }

    /**
     * 刷中间单 机器人卡单情况下 自动撤单
     */

    @Async
    public void midShuaCheOrder(YangCurrencyPair yangCurrencyPair,long sleep){
        try {
            Thread.sleep(sleep);

        }catch (Exception e)
        {

        }
        //查询该交易对当前  卖单最低价和 卖单最高价是否 相等
        List<String> statusList = Lists.newArrayList();
        statusList.add("0");statusList.add("1");
        Map map=new HashMap();
        map.put("currencyId",yangCurrencyPair.getCurrencyId());
        map.put("currencyTradeId",yangCurrencyPair.getCurrencyTradeId());
        map.put("status",statusList);
         map.put("hasDo",1);
         map.put("orderByClause","price asc");
        map.put("type",CoinConst.ORDER_SELL);
        map.put("pageSize",1);
         List<Map> yangOrdersSellList=yangOrderService.findMaxPriceSellOrder(map);
         if(yangOrdersSellList==null||yangOrdersSellList.size()==0){
             return ;
         }
        Map yangOrdersSell=yangOrdersSellList.get(0);
        //获取买单最高价格
        map.put("orderByClause","price desc");
        map.put("type",CoinConst.ORDER_BUY);

        List<Map>  yangOrdersBuyList=yangOrderService.findMaxPriceSellOrder(map);
        Map yangOrdersBuy=yangOrdersBuyList.get(0);

        BigDecimal sellPrice=(BigDecimal)yangOrdersSell.get("price");
        BigDecimal buyPrice=(BigDecimal)yangOrdersBuy.get("price");
        int sellShua=(int)yangOrdersSell.get("is_shua");
        int buyShua=(int)yangOrdersBuy.get("is_shua");
        //买卖单价格 相等
        if(sellPrice.compareTo(buyPrice)==0){
            //买卖单都是 机器人
            if(sellShua==CoinConst.ROBOT_SHUA_YES.intValue()&&buyShua==CoinConst.ROBOT_SHUA_YES.intValue()){
                yangOrderService.cancel_order((Integer)yangOrdersBuy.get("orders_id"));
                yangOrderService.cancel_order((Integer)yangOrdersSell.get("orders_id"));
            }
        }
        //如果买卖单差距只有  1个价格单位则   买单和卖单各撤3单
        int inputPriceNum=yangCurrencyPair.getInputPriceNum();//显示位数
        sellPrice=sellPrice.setScale(inputPriceNum,BigDecimal.ROUND_DOWN);
        buyPrice=buyPrice.setScale(inputPriceNum,BigDecimal.ROUND_DOWN);
        //生成最小价格差距  字符串
        String noShuaPrice="0.";
        for(int i=0;i<inputPriceNum-1;i++){
            noShuaPrice+="0";
        }
        noShuaPrice+="1";

        if(sellPrice.subtract(buyPrice).compareTo(new BigDecimal(noShuaPrice))==0){
            map.put("orderByClause","price asc");
            map.put("type",CoinConst.ORDER_SELL);
            map.put("pageSize",3);
            List<Map> yangOrdersCheSellList=yangOrderService.findMaxPriceSellOrder(map);

            map.put("orderByClause","price desc");
            map.put("type",CoinConst.ORDER_BUY);
            map.put("pageSize",3);
            List<Map>  yangOrdersCheBuyList=yangOrderService.findMaxPriceSellOrder(map);

            for(Map m:yangOrdersCheSellList){
                yangOrderService.cancel_order((Integer)m.get("orders_id"));
            }

            for(Map m:yangOrdersCheBuyList){
                yangOrderService.cancel_order((Integer)m.get("orders_id"));
            }
        }





    }

    /**
     * 挂单 通过外部网站模式
     */
    @Async
    public void guadanByOutSite(YangCurrencyPair yangCurrencyPair,long sleep)
    {
        //后台修改基准价格后  撤单前 要重新读取配置信息
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


        HashMap roderInfos = yangOrderService.getOrderNumsInfo(yangCurrencyPair);

        if(roderInfos != null)
        {
            if(((BigDecimal)roderInfos.get("buyNum")).compareTo(BigDecimal.valueOf(30))>=0)
            {

                this.cheOrder("buy",yangCurrencyPair.getCurrencyId(),yangCurrencyPair.getCurrencyTradeId(),isZhangOrDie,yangCurrencyPair.getRobotId(),priceNow,yangCurrencyPair);
            }
            if(((BigDecimal)roderInfos.get("sellNum")).compareTo(BigDecimal.valueOf(30))>=0){
                this.cheOrder("sell",yangCurrencyPair.getCurrencyId(),yangCurrencyPair.getCurrencyTradeId(),isZhangOrDie,yangCurrencyPair.getRobotId(),priceNow,yangCurrencyPair);
            }
        }

        //如果非小号价格变动了 开启紧急价格持平
            if(priceNow.compareTo(priceLast) > 0)  //涨价
        {
            //如果涨价 需要把卖单低于当前价格的全部订单都吃掉
            System.out.println("开启卖单紧急平稳价格");
            criteria.andLessThan("price",priceLast).andEqualTo("type","sell");
            List<YangOrders> yangOrdersList  = yangOrderService.selectByExample(exampleTo);
            for (YangOrders y:yangOrdersList
                    ) {
                yangOrderService.cancel_order(y.getOrdersId());
            }

        }else if(priceNow.compareTo(priceLast) < 0){  //跌价
            System.out.println("开启买单紧急平稳价格");
            criteria.andGreaterThan("price",priceLast).andEqualTo("type","buy");
            List<YangOrders> yangOrdersList  = yangOrderService.selectByExample(exampleTo);
            for (YangOrders y:yangOrdersList
                    ) {
                yangOrderService.cancel_order(y.getOrdersId());
            }


        }
    }

    //新挂单模式下  的撤单逻辑    1 从redis读取 创建订单服务 抛出的撤单id  2 循环遍历 订单长度超过30 就撤单
    @Async
    public void cancelOrderByMideAndShua(YangCurrencyPair yangCurrencyPair,long sleep){
        try {
            Thread.sleep(sleep);
        }catch (Exception e)
        {

        }
        if(yangCurrencyPair.getRobotId() == null || yangCurrencyPair.getRobotId().equals(Integer.valueOf(0))) //设置了机器人id的 就开启撤单
        {
            return;
        }


        Integer ordersId = null;
        if(redisService.listSize(CoinConst.MQ_CANCEL_ORDER_QUEUE) > 0)
        {
            System.out.println("当前队列下有【"+redisService.listSize(CoinConst.MQ_CANCEL_ORDER_QUEUE)+"】条订单需要撤销");
            for(int i=0;i<redisService.listSize(CoinConst.MQ_CANCEL_ORDER_QUEUE);i++){
                ordersId =  Integer.valueOf(redisService.lpop(CoinConst.MQ_CANCEL_ORDER_QUEUE).toString());
                yangOrderService.cancel_order(ordersId);
            }
        }
    }

    //获取 已处理的  价格合适未成交的订单
    @Async
    public void cancelOrderByNums(YangCurrencyPair yangCurrencyPair){
        List<Map> list=yangOrderService.querySamePrice(yangCurrencyPair);
        for(int i=0;i<list.size();i++){
            yangOrderService.cancel_order((Integer) list.get(i).get("a"));
            yangOrderService.cancel_order((Integer)list.get(i).get("b"));
        }
    }

    /**
     * 判断当前价格的订单 是否存在
     * @return
     */
    public Boolean priceJz(BigDecimal priceNow,YangCurrencyPair yangCurrencyPair,String type)
    {

        List statusList  = new LinkedList();
        statusList.add(0);statusList.add(1);
        Example example = new Example(YangOrders.class);
        example.createCriteria()
                .andEqualTo("memberId",yangCurrencyPair.getRobotId())
                .andEqualTo("currencyId",yangCurrencyPair.getCurrencyId())
                .andEqualTo("currencyTradeId",yangCurrencyPair.getCurrencyTradeId())
                .andIn("status",statusList).andEqualTo("type",type)
                .andEqualTo("price",priceNow);
        List<YangOrders> list = yangOrderService.selectByExample(example);

        if(list==null ||  list.size() == 0)
        {
            return false;
        }

        return true;  //价格不对 返回null

    }


    /**
     * 撤单流程
     */
    public void cheOrder(String type,Integer currencyId,Integer currencyTradeId,int isZhangOrDie,int memberId,BigDecimal basePrice ,YangCurrencyPair yangCurrencyPair)
    {

        List members = new LinkedList();
        members.add(memberId);
        List statusList  = new LinkedList();
        statusList.add(0);statusList.add(1);
        Example example = new Example(YangOrders.class);
        Example.Criteria criteria = example.createCriteria().andEqualTo("hasDo",1)
                .andEqualTo("currencyId",currencyId)
                .andEqualTo("currencyTradeId",currencyTradeId)
                .andEqualTo("type",type)
                .andIn("memberId",members)
                .andIn("status",statusList)
                ;

        //基准价

        if(type.equals("sell"))
        {
            criteria.andLessThan("price",basePrice.multiply(BigDecimal.valueOf(1-yangCurrencyPair.getShuaBl())));

        }else{
            criteria.andGreaterThan("price",basePrice.multiply(BigDecimal.valueOf(1+yangCurrencyPair.getShuaBl())));

        }

        List<YangOrders> list1  = yangOrderService.selectByExample(example);
        for (YangOrders y :list1
                ) {

            if(yangOrderService.cancel_order(y.getOrdersId()) == 0 )
            {
                System.out.println("订单"+y.getOrdersId()+" 撤单成功");
            }else{
                System.out.println("订单"+y.getOrdersId()+" 撤单失败");

            }
        }

        //撤单逻辑 如果先查看分组数据 看一下这个列别的订单分组是否超过23 是的话23以外的数据 全部撤单

        Map parama = new HashMap();
        parama.put("members",members);
        parama.put("type",type);
        parama.put("currencyId",currencyId.intValue());
        parama.put("currencyTradeId",currencyTradeId.intValue());
        parama.put("status",statusList);

        if(type.equals("sell"))
        {
            parama.put("sortType","asc");
        }else{
            parama.put("sortType","desc");
        }

        BigDecimal price_lj;

        int blnum = 50;

        List<YangOrders> list = yangOrderService.getOrdersGroupByPrice(parama);

        if(list == null || list.size() <= blnum)
        {
            return;
        }




        price_lj = list.get(blnum).getPrice(); //23处的价格为灵界价格 其他的都要删掉 全部撤单


        example.clear();
        criteria = example.createCriteria().andEqualTo("hasDo",1)
                .andEqualTo("currencyId",currencyId)
                .andEqualTo("currencyTradeId",currencyTradeId)
                .andEqualTo("type",type)
                .andIn("memberId",members)
                .andIn("status",statusList)
                ;

        if(type.equals("sell"))
        {
            criteria.andGreaterThan("price",price_lj);
        }else{
            criteria.andLessThan("price",price_lj);
        }

        list1  = yangOrderService.selectByExample(example);

        for (YangOrders y :list1
                ) {

            if(yangOrderService.cancel_order(y.getOrdersId()) == 0 )
            {
                System.out.println("订单"+y.getOrdersId()+" 撤单成功");
            }else{
                System.out.println("订单"+y.getOrdersId()+" 撤单失败");

            }
        }


    }


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

    private double getRandomNum(double min,double max)
    {

        double boundedDouble = min + new Random().nextDouble() * (max - min);
//        double d = min + Math.random() * max % (max - min + 1);
        return boundedDouble;

    }
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
