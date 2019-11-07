package net.cyweb.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import cyweb.utils.CoinConst;
import cyweb.utils.DateUtils;
import cyweb.utils.ErrorCode;
import net.cyweb.exception.EmailNotExistException;
import net.cyweb.exception.EmailRegistRepeatException;
import net.cyweb.exception.PasswordErrorException;
import net.cyweb.mapper.*;
import net.cyweb.model.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class YangMemberService extends BaseService<YangMember>{

    @Autowired
    private YangConfigService yangConfigService;

    @Autowired
    private YangMemberMapper memberMapper;

    @Autowired
    private YangCurrencyUserMapper yangCurrencyUserMapper;

    @Autowired
    private YangTradeMapper yangTradeMapper;

    @Autowired
    private RedisService redisService;

    @Autowired
    private YangMiningInfoMapper yangMiningInfoMapper;

    @Autowired
    private YangFtLockRebateRecodeMapper yangFtLockRebateRecodeMapper;

    @Autowired
    private YangFtLockMapper yangFtLockMapper;


    @Autowired
    private YangCardNumMapper yangCardNumMapper;

    @Autowired
    private MongoTemplate mongoTemplate;


    /*用户注册逻辑*/
    public void register(YangMember yangMember) {

        if(!checkEmailRepeat(yangMember)){
            throw new EmailRegistRepeatException("email重复");
        }


        //获取配置
        YangConfig yangConfig = new YangConfig();
        yangConfig.setKey("invit_tay");
        YangConfig yangConfigFind = yangConfigService.selectOne(yangConfig);

        if(null == yangConfigFind || StringUtils.isBlank(yangConfigFind.getValue())){
            throw new RuntimeException("invit_tay 配置不存在");
        }
        yangMember.setInvitTime(Integer.valueOf(yangConfigFind.getValue()) * 86400 + System.currentTimeMillis() / 1000 );


        yangMember.setRegTime(System.currentTimeMillis()/1000);
        yangMember.setShenhestatus(0);

        yangMember.setPwd(DigestUtils.md5Hex(yangMember.getPwd()));
//        yangMember.setPwdtrade(DigestUtils.md5Hex(yangMember.getPwdtrade()));

        saveSelective(yangMember);
    }

    /*检测ecmail是否已经存在*/
    private Boolean checkEmailRepeat(YangMember yangMember){
        YangMember yangMemberQuery = new YangMember();
        yangMemberQuery.setEmail(yangMember.getEmail());
//        yangMemberQuery.setRegTime(null);
//        yangMemberQuery.setInvitTime(null);
        int selectCount = mapper.selectCount(yangMemberQuery);
        return selectCount == 0;
    }


    /*用户登录*/
    public YangMember login(YangMember yangMember) {
        YangMember yangMemberQuery = new YangMember();
        yangMemberQuery.setEmail(yangMember.getEmail());
        YangMember yangMemberFind = mapper.selectOne(yangMemberQuery);
        if(null == yangMemberFind){
            throw new EmailNotExistException(ErrorCode.ERROR_EMAIL_NOT_EXIST.getMessage());
        }

        if(!yangMemberFind.getPwd().equals(DigestUtils.md5Hex(yangMember.getPwd()))){
            throw new PasswordErrorException(ErrorCode.ERROR_PASSWORD_ERR.getMessage());
        }

        /*去掉密码*/
        yangMemberFind.setPwdtrade(null);
        yangMemberFind.setPwd(null);
        return yangMemberFind;

    }

//    @Transactional
//    public void update(YangMember yangMember, YangCurrencyUser yangCurrencyUser) {
//        String lock = yangMember.getMemberId().toString();
//        synchronized (lock.intern())
//        {
//
//             this.memberMapper.updateByPrimaryKeySelective(yangMember);
//             this.yangCurrencyUserMapper.updateByPrimaryKeySelective(yangCurrencyUser);
//        }
//
//    }


    /**
     *
     * @param memberId
     * @param cyId
     * @param num
     * @param numOptions inc dec
     * @param forzen
     * @param forzenOptions inc dec
     * @return
     */
    public  Integer assets(
            int memberId, String cyId, BigDecimal num, String numOptions, BigDecimal forzen, String forzenOptions,
            int memberId1, String cyId1, BigDecimal num1, String numOptions1, BigDecimal forzen1, String forzenOptions1,
            int memberId2, String cyId2, BigDecimal num2, String numOptions2, BigDecimal forzen2, String forzenOptions2,
            int memberId3, String cyId3, BigDecimal num3, String numOptions3, BigDecimal forzen3, String forzenOptions3,
            int memberId4, String cyId4, BigDecimal num4, String numOptions4, BigDecimal forzen4, String forzenOptions4
            ){
        HashMap pama = new HashMap();
        pama.put("memberId",memberId);
        pama.put("cyId",cyId);
        pama.put("num",num);
        pama.put("numOptions",numOptions);
        pama.put("forzen",forzen);
        pama.put("forzenOptions",forzenOptions);


        pama.put("memberId1",memberId1);
        pama.put("cyId1",cyId1);
        pama.put("num1",num1);
        pama.put("numOptions1",numOptions1);
        pama.put("forzen1",forzen1);
        pama.put("forzenOptions1",forzenOptions1);

        pama.put("memberId2",memberId2);
        pama.put("cyId2",cyId2);
        pama.put("num2",num2);
        pama.put("numOptions2",numOptions2);
        pama.put("forzen2",forzen2);
        pama.put("forzenOptions2",forzenOptions2);

        pama.put("memberId3",memberId3);
        pama.put("cyId3",cyId3);
        pama.put("num3",num3);
        pama.put("numOptions3",numOptions3);
        pama.put("forzen3",forzen3);
        pama.put("forzenOptions3",forzenOptions3);

        pama.put("memberId4",memberId4);
        pama.put("cyId4",cyId4);
        pama.put("num4",num4);
        pama.put("numOptions4",numOptions4);
        pama.put("forzen4",forzen4);
        pama.put("forzenOptions4",forzenOptions4);

        return  memberMapper.assets(pama);
    }

    public int orderConfirm(
            int memberId, String cyId, BigDecimal num, String numOptions, BigDecimal forzen, String forzenOptions,
            int memberId1, String cyId1, BigDecimal num1, String numOptions1, BigDecimal forzen1, String forzenOptions1,
            int memberId2, String cyId2, BigDecimal num2, String numOptions2, BigDecimal forzen2, String forzenOptions2,
            int memberId3, String cyId3, BigDecimal num3, String numOptions3, BigDecimal forzen3, String forzenOptions3,
            int memberId4, String cyId4, BigDecimal num4, String numOptions4, BigDecimal forzen4, String forzenOptions4,

            int orderId1 ,BigDecimal tradeNum1, long tradeTime1,
            int orderId2 ,BigDecimal tradeNum2, long tradeTime2,

            String tradeNo1,int t_memberId1,int t_currencyId1,int currencyTradeId1,BigDecimal price1,BigDecimal t_num1,BigDecimal t_money1,BigDecimal fee1,String t_type1, long t_addTime1,int t_status1,int show1, int t_orders_id1,

            String tradeNo2 ,int t_memberId2 ,int t_currencyId2,int currencyTradeId2,BigDecimal price2,BigDecimal t_num2,BigDecimal t_money2,BigDecimal fee2,String t_type2, long t_addTime2,int t_status2,int show2,int t_orders_id2,

            int f_memberId1,int f_type1 ,int f_moneyType1,BigDecimal f_money1 ,long f_addTime1 ,int f_currencyId1 ,String  f_ip1 ,String  f_content1 ,
            int f_memberId2,int f_type2 ,int f_moneyType2,BigDecimal f_money2 ,long f_addTime2 ,int f_currencyId2 ,String  f_ip2 ,String  f_content2
            )
    {

        HashMap pama = new HashMap();
        pama.put("memberId",memberId);
        pama.put("cyId",cyId);
        pama.put("num",num);
        pama.put("numOptions",numOptions);
        pama.put("forzen",forzen);
        pama.put("forzenOptions",forzenOptions);


        pama.put("memberId1",memberId1);
        pama.put("cyId1",cyId1);
        pama.put("num1",num1);
        pama.put("numOptions1",numOptions1);
        pama.put("forzen1",forzen1);
        pama.put("forzenOptions1",forzenOptions1);

        pama.put("memberId2",memberId2);
        pama.put("cyId2",cyId2);
        pama.put("num2",num2);
        pama.put("numOptions2",numOptions2);
        pama.put("forzen2",forzen2);
        pama.put("forzenOptions2",forzenOptions2);

        pama.put("memberId3",memberId3);
        pama.put("cyId3",cyId3);
        pama.put("num3",num3);
        pama.put("numOptions3",numOptions3);
        pama.put("forzen3",forzen3);
        pama.put("forzenOptions3",forzenOptions3);

        pama.put("memberId4",memberId4);
        pama.put("cyId4",cyId4);
        pama.put("num4",num4);
        pama.put("numOptions4",numOptions4);
        pama.put("forzen4",forzen4);
        pama.put("forzenOptions4",forzenOptions4);


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



        return  memberMapper.orderConfirm(pama);

    }

    public void dealMiningInfo(){
        MiningInfos miningInfos;
        String enddate = DateUtils.getDateStrPre(-1);
        String startdate = DateUtils.getDateStrPre(-2);
        String dateNow = DateUtils.getDateStrPre(0);
        miningInfos = new MiningInfos();
        BigDecimal mineTotal = BigDecimal.valueOf(2400000000l);
        YangTrade yangTrade = null;
        BigDecimal hasMineETH = BigDecimal.ZERO;
        BigDecimal lastMineETH = BigDecimal.ZERO;
        BigDecimal hasMineTotal = BigDecimal.ZERO;
        BigDecimal lastMine = BigDecimal.ZERO;
        BigDecimal hasBackETH = BigDecimal.ZERO;
        BigDecimal hourAssign = BigDecimal.ZERO;
        BigDecimal dayAssign = BigDecimal.ZERO;

        HashMap pama = new HashMap();
        pama.put("currencyId", 80);
        pama.put("currencyTrade", 37);

        yangTrade = this.getTrade(80, 37);
        if(yangTrade==null){
            System.out.println("昨日无交易记录------------------------------>");
            return;
        }

//        Example example = new Example(YangTrade.class);
//        example.createCriteria().andEqualTo("currencyId", 80).andEqualTo("currencyTradeId", 37);
//        example.setOrderByClause("trade_id desc");
//        List<YangTrade> list = yangTradeMapper.selectByExample(example);

            //前一日
            pama.put("startTime", String.valueOf(DateUtils.getTimeStamp(enddate + " 0:0:0") / 1000));
            pama.put("endTime", String.valueOf(DateUtils.getTimeStamp(enddate + " 23:59:59") / 1000));
            lastMine = this.getMiningNums(pama, yangTrade).multiply(BigDecimal.valueOf(1.1));
            miningInfos.setLastMine(lastMine);

            lastMineETH = lastMine.multiply(yangTrade.getPrice());
            miningInfos.setLastMineETH(lastMineETH);

            //前一日总返佣值
            miningInfos.setLastRakeTotal(yangMiningInfoMapper.getRakeInfo(pama));
            //前一日累计分红
            miningInfos.setLastLockTotal(yangFtLockRebateRecodeMapper.getLockInfo(pama));

            //--------------------------------------------------------------------------------------------------
            //所有的
            pama.put("startTime", "");
            pama.put("endTime", "");
            hasMineTotal = this.getMiningNums(pama, yangTrade).multiply(BigDecimal.valueOf(1.1));
            miningInfos.setHasMineTotal(hasMineTotal);//已经挖矿总量ETH
            BigDecimal totalCardNums=(BigDecimal)yangCardNumMapper.getDealNums().get("totalNum");
            miningInfos.setCirculateNum(hasMineTotal.add(totalCardNums));//总流通量
            miningInfos.setUnsetNum(new BigDecimal(58303808));//FT回购销毁量
            BigDecimal totalLockNums=(BigDecimal) yangFtLockMapper.getTotalLockNums().get("totalLockNum");
            miningInfos.setSecondNum(miningInfos.getCirculateNum().subtract(totalLockNums));//二级市场流通量

            hasMineETH = hasMineTotal.multiply(yangTrade.getPrice());
            miningInfos.setHasMineETH(hasMineTotal.multiply(yangTrade.getPrice()));//已经挖矿总量ETH
            //累计返佣
            miningInfos.setHasRakeTotal(yangMiningInfoMapper.getRakeInfo(pama));
            //累计分红
            miningInfos.setHasLockTotal(yangFtLockRebateRecodeMapper.getLockInfo(pama));

            //累计锁仓
            miningInfos.setHasLockNum(yangFtLockMapper.getLockInfo(pama));
            //--------------------------------------------------------------------------------------------------
            //hourAssign 本小时待分配
            System.out.println("当前时间戳 " + new Date());
            pama.put("startTime", String.valueOf(System.currentTimeMillis() / 1000 - 3600));
            pama.put("endTime", String.valueOf( System.currentTimeMillis() / 1000));
            hourAssign = this.getMiningNums(pama, yangTrade).multiply(BigDecimal.valueOf(1.1));
            miningInfos.setHourAssign(hourAssign);
            //hourAssign 本小时待分配

            //dayAssign 今日待分配
            pama.put("startTime",String.valueOf( DateUtils.getTimeStamp(dateNow + " 0:0:0") / 1000));
            pama.put("endTime", String.valueOf( DateUtils.getTimeStamp(dateNow + " 23:59:59") / 1000));
//            System.out.println(" 开始时间" + pama.get("startTime"));
//            System.out.println(" 结束时间" + pama.get("endTime"));
            dayAssign = this.getMiningNums(pama, yangTrade).multiply(BigDecimal.valueOf(1.1));

            miningInfos.setDayAssign(dayAssign);
            //dayAssign 今日待分配

            //累计回购基金总量兑换ETH 算手续费的%40

            hasBackETH = hasMineETH.multiply(BigDecimal.valueOf(0.4));
            miningInfos.setHasBackETH(hasBackETH);
            //lastBackETH 昨天
            BigDecimal lastBackETH = BigDecimal.ZERO;
            lastBackETH = lastMineETH.multiply(BigDecimal.valueOf(0.4));
            miningInfos.setLastBackETH(lastBackETH);
            //lastBackETH 昨天


            //累计回购基金总量兑换ETH 算手续费的%40


            miningInfos.setLeftMineTotal(mineTotal.subtract(hasMineTotal));

            miningInfos.setMineTotal(mineTotal);
            redisService.set(CoinConst.MININGINFOS,JSONObject.toJSON(miningInfos),30L, TimeUnit.MINUTES);
            System.out.println("funt全局缓存 数据 查询完毕<><><><>");

    }

    private YangTrade getTrade(int currencyId,int currencyTrade)
    {
        Query query=new Query();
        query.addCriteria(Criteria.where("currencyId").is(currencyId)).addCriteria(Criteria.where("currencyTradeId").is(currencyTrade));
        query.with(new Sort(new Sort.Order(Sort.Direction.DESC,"addTime")));
        query.skip(0).limit(1);
        List<MgYangTrade> list= this.mongoTemplate.find(query,MgYangTrade.class,CoinConst.MONGODB_TRADE_COLLECTION);



//        Example example = new Example(YangTrade.class);
//        example.createCriteria().andEqualTo("currencyId", currencyId).andEqualTo("currencyTradeId", currencyTrade);
//        example.setOrderByClause("trade_id desc");
//        List<YangTrade> list = yangTradeMapper.selectByExample(example);
        if(null != list && list.size() > 0 )
        {
            MgYangTrade mgYangTrade=list.get(0);
            YangTrade yangTrade=new YangTrade();
            yangTrade.setPrice(new BigDecimal(mgYangTrade.getPrice()));
            return yangTrade;
        }else{
            return null;
        }
    }

    public Criteria getConditionGroup(Map params){

        int currencyId=(Integer)params.get("currencyId");
        int currencyTrade=(Integer)params.get("currencyTrade");
        Integer memberId=(Integer) params.get("memberId");

        Criteria a=new Criteria();
        Criteria b=new Criteria();
        Criteria c=new Criteria();
        Criteria d=new Criteria();
        Criteria e=new Criteria();

        c= c.andOperator(Criteria.where("currencyId").is(currencyId));
        e= e.andOperator(Criteria.where("currencyTradeId").is(currencyTrade));
        if (null != params.get("startTime") && StringUtils.isNotBlank(String.valueOf(params.get("startTime")))&&(null != params.get("endTime") && StringUtils.isNotBlank(String.valueOf(params.get("endTime")))) ) {
            int start= Integer.valueOf((String) params.get("startTime"));
            int end= Integer.valueOf((String) params.get("endTime"));
            b=b.andOperator(Criteria.where("addTime").gte(start).lte(end));
        }
        if (null != params.get("memberId") && StringUtils.isNotBlank(String.valueOf(params.get("memberId")))) {
            d=d.andOperator(Criteria.where("memberId").is(memberId));
        }

        a.andOperator(b,c,d,e);
        return a;
    }

    /**
     * 获取制定时间挖矿数量
     *
     * @return 获取的是funt
     */
    private BigDecimal getMiningNums(HashMap pama, YangTrade yangTrade) {
//        List<HashMap> list = yangTradeMapper.selectFeeByType(pama);
        Aggregation aggregation= Aggregation.newAggregation(
                Aggregation.match(getConditionGroup(pama)),
                Aggregation.group("type").sum("fee").as("total").first("type").as("type"));
        AggregationResults<HashMap> outputTypeCount5 = this.mongoTemplate.aggregate(aggregation, CoinConst.MONGODB_TRADE_COLLECTION, HashMap.class);
        List<HashMap> list=outputTypeCount5.getMappedResults();

        BigDecimal nums = BigDecimal.ZERO;
        for (HashMap h : list) {
            if (h.get("type").equals("sell"))  //卖单
            {
                //这里获取的是eth 需要转化成funt
                nums = nums.add(BigDecimal.valueOf(Double.valueOf(h.get("total").toString())).divide(yangTrade.getPrice(), 6, RoundingMode.DOWN));
            } else {
                nums = nums.add(BigDecimal.valueOf(Double.valueOf(h.get("total").toString())));
            }
        }
        return nums;
    }
}
