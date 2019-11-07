package net.cyweb.service;

import com.alibaba.druid.support.json.JSONUtils;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageInfo;
import cyweb.utils.*;
import io.swagger.annotations.ApiOperation;
import jnr.ffi.annotations.In;
import net.cyweb.config.custom.EmailUtils;
import net.cyweb.config.ftp.FtpUtils;
import net.cyweb.exception.*;
import net.cyweb.mapper.*;
import net.cyweb.model.*;
import net.cyweb.model.modelExt.YangMiningInfoExt;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.core.util.JsonUtils;
import org.bouncycastle.jcajce.provider.digest.MD5;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;
import tk.mybatis.mapper.entity.Example;


import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.servlet.http.HttpServletRequest;
import java.beans.Transient;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Time;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static java.awt.Color.MAGENTA;
import static java.awt.SystemColor.info;

@Service
public class YangMemberService extends BaseService<YangMember> {

    @Autowired
    private YangConfigService yangConfigService;

    @Autowired
    private YangMemberMapper memberMapper;

    @Autowired
    private YangCurrencyUserMapper yangCurrencyUserMapper;

    @Autowired
    private YangFtActivityMapper yangFtActivityMapper;

    @Autowired
    private YangMemberTokenService yangMemberTokenService;

    @Autowired
    private YangMiningInfoMapper yangMiningInfoMapper;

    @Autowired
    private YangTradeMapper yangTradeMapper;

    @Autowired
    private YangCardNumMapper yangCardNumMapper;

    @Autowired
    private YangFtLockMapper yangFtLockMapper;

    @Autowired
    private YangFtLockRebateRecodeMapper yangFtLockRebateRecodeMapper;

    @Autowired
    private RedisService redisService;

    @Autowired
    private YangEmailMapper yangEmailMapper;

    @Autowired
    private YangEmailLogMapper yangEmailLogMapper;

    @Autowired
    public UserProPs userProPs;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private YangInviteCodeService yangInviteCodeService;

    @Autowired
    private YangCurrencyMapper yangCurrencyMapper;

    @Autowired
    private YangQianbaoAddressMapper yangQianbaoAddressMapper;


    /*用户注册逻辑*/
    public void register(YangMember yangMember) {

        if (!checkEmailRepeat(yangMember)) {
            throw new EmailRegistRepeatException("email重复");
        }


        //获取配置
        YangConfig yangConfig = new YangConfig();
        yangConfig.setKey("invit_tay");
        YangConfig yangConfigFind = yangConfigService.selectOne(yangConfig);

        if (null == yangConfigFind || StringUtils.isBlank(yangConfigFind.getValue())) {
            throw new RuntimeException("invit_tay 配置不存在");
        }
        yangMember.setInvitTime(Integer.valueOf(yangConfigFind.getValue()) * 86400 + System.currentTimeMillis() / 1000);


        yangMember.setRegTime(System.currentTimeMillis() / 1000);
        yangMember.setShenhestatus(0);

        yangMember.setPwd(DigestUtils.md5Hex(yangMember.getPwd()));

        saveSelective(yangMember);

//
//        //开启注册送币平台模式
//        int num = 1000; //送1000 可加倍挖矿额度
//        //先查找用户
//        YangFtActivity yangFtActivity = new YangFtActivity();
//        yangFtActivity.setAddTime((int)System.currentTimeMillis()/1000);
//        yangFtActivity.setMemberId(yangMember.getMemberId());
//        yangFtActivity.setNum(BigDecimal.valueOf(num));
//        yangFtActivity.setType(Integer.valueOf(1));
//        yangFtActivityMapper.insert(yangFtActivity);
//
//
//        // 开始给推荐的用户加币
//        if(! StringUtil.isEmpty(yangMember.getPid()))
//        {
//            int num_tj = 200;
//            YangFtActivity yangFtActivity1 = new YangFtActivity();
//            yangFtActivity1.setType(Integer.valueOf(1));
//            yangFtActivity1.setMemberId(Integer.valueOf(yangMember.getPid()));
//            YangFtActivity yangFtActivity2;
//            yangFtActivity2 = yangFtActivityMapper.selectOne(yangFtActivity1);
//            if(yangFtActivity2 != null)
//            {
//                yangFtActivity2.setNum(yangFtActivity2.getNum().add(BigDecimal.valueOf(num_tj)));
//                yangFtActivityMapper.updateByPrimaryKeySelective(yangFtActivity2);
//            }else{
//                //如果没有找到 则加一条记录
//                yangFtActivity2 = yangFtActivity1;
//                yangFtActivity2.setNum(BigDecimal.valueOf(num_tj));
//                yangFtActivity2.setAddTime((int)System.currentTimeMillis()/1000);
//                yangFtActivityMapper.insert(yangFtActivity2);
//
//            }
//        }

    }

    /*检测ecmail是否已经存在*/
    private Boolean checkEmailRepeat(YangMember yangMember) {
        YangMember yangMemberQuery = new YangMember();
        yangMemberQuery.setEmail(yangMember.getEmail());
//        yangMemberQuery.setRegTime(null);
//        yangMemberQuery.setInvitTime(null);
        int selectCount = mapper.selectCount(yangMemberQuery);
        return selectCount == 0;
    }

    /*检测phone是否已经存在*/
    private Boolean checkPhoneRepeat(YangMember yangMember) {
        YangMember yangMemberQuery = new YangMember();
        yangMemberQuery.setPhone(yangMember.getPhone());
//        yangMemberQuery.setRegTime(null);
//        yangMemberQuery.setInvitTime(null);
        int selectCount = mapper.selectCount(yangMemberQuery);
        return selectCount == 0;
    }

    /*检验邀请码是否存在*/
    private Boolean checkInviteCodeRepeat(YangInviteCodeService yangInviteCodeService, String inviteCode) {
        YangInviteCode yangInviteCode = yangInviteCodeService.getInviteCode(inviteCode);
        if (yangInviteCode != null) {
            return true;
        }
        return false;
    }

    /*用户登录*/
    public YangMember login(YangMember yangMember) {
        YangMember yangMemberQuery = new YangMember();
        yangMemberQuery.setEmail(yangMember.getEmail());
        YangMember yangMemberFind = mapper.selectOne(yangMemberQuery);
        if (null == yangMemberFind) {
            throw new EmailNotExistException(ErrorCode.ERROR_EMAIL_NOT_EXIST.getMessage());
        }

        if (!yangMemberFind.getPwd().equals(DigestUtils.md5Hex(yangMember.getPwd()))) {
            throw new PasswordErrorException(ErrorCode.ERROR_PASSWORD_ERR.getMessage());
        }

        if (yangMemberFind.getIsLock().intValue() == 1) {
            throw new MemberIsLocketException(ErrorCode.MEMBER_HAS_LOCKED.getMessage());
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
     * @param memberId
     * @param cyId
     * @param num
     * @param numOptions    inc dec
     * @param forzen
     * @param forzenOptions inc dec
     * @return
     */
    public Integer assets(
            int memberId, String cyId, BigDecimal num, String numOptions, BigDecimal forzen, String forzenOptions,
            int memberId1, String cyId1, BigDecimal num1, String numOptions1, BigDecimal forzen1, String forzenOptions1,
            int memberId2, String cyId2, BigDecimal num2, String numOptions2, BigDecimal forzen2, String forzenOptions2,
            int memberId3, String cyId3, BigDecimal num3, String numOptions3, BigDecimal forzen3, String forzenOptions3,
            int memberId4, String cyId4, BigDecimal num4, String numOptions4, BigDecimal forzen4, String forzenOptions4
    ) {
        HashMap pama = new HashMap();
        pama.put("memberId", memberId);
        pama.put("cyId", cyId);
        pama.put("num", num);
        pama.put("numOptions", numOptions);
        pama.put("forzen", forzen);
        pama.put("forzenOptions", forzenOptions);


        pama.put("memberId1", memberId1);
        pama.put("cyId1", cyId1);
        pama.put("num1", num1);
        pama.put("numOptions1", numOptions1);
        pama.put("forzen1", forzen1);
        pama.put("forzenOptions1", forzenOptions1);

        pama.put("memberId2", memberId2);
        pama.put("cyId2", cyId2);
        pama.put("num2", num2);
        pama.put("numOptions2", numOptions2);
        pama.put("forzen2", forzen2);
        pama.put("forzenOptions2", forzenOptions2);

        pama.put("memberId3", memberId3);
        pama.put("cyId3", cyId3);
        pama.put("num3", num3);
        pama.put("numOptions3", numOptions3);
        pama.put("forzen3", forzen3);
        pama.put("forzenOptions3", forzenOptions3);

        pama.put("memberId4", memberId4);
        pama.put("cyId4", cyId4);
        pama.put("num4", num4);
        pama.put("numOptions4", numOptions4);
        pama.put("forzen4", forzen4);
        pama.put("forzenOptions4", forzenOptions4);

        return memberMapper.assets(pama);
    }


    public Result platFormCurrencyLock(YangMemberToken yangMemberToken, BigDecimal num) {
        Result result = new Result();
        result.setCode(Result.Code.ERROR);
        try {
            //现获取用户信息
            YangMember yangMemberSelf = yangMemberTokenService.findMember(yangMemberToken);
            if (yangMemberSelf == null) {
                throw new TokenExpirException("token 错误");
            }

            YangFtLock yangFtLock = new YangFtLock();
            yangFtLock.setMemberId(yangMemberSelf.getMemberId());
            yangFtLock.setNum(num);


            int r = memberMapper.platFormCurrencyLock(yangFtLock);
            if (r == 1) {
                result.setCode(Result.Code.ERROR);
                result.setErrorCode(ErrorCode.ERROR_TRADE_CURRENCY_USER_PRICE_NOTENOUGH.getIndex());
                result.setMsg(ErrorCode.ERROR_TRADE_CURRENCY_USER_PRICE_NOTENOUGH.getMessage());
            } else {
                result.setCode(Result.Code.SUCCESS);
            }
        } catch (TokenExpirException e) {

            result.setErrorCode(ErrorCode.ERROR_ERROR_TOKEN.getIndex());
            result.setMsg(ErrorCode.ERROR_ERROR_TOKEN.getMessage());
            result.setCode(Result.Code.ERROR);

        } catch (Exception e) {
            e.printStackTrace();
            result.setCode(Result.Code.ERROR);
        }

        return result;
    }


    public Result platFormCurrencyUnLock(YangMemberToken yangMemberToken, BigDecimal num) {
        Result result = new Result();
        result.setCode(Result.Code.ERROR);
        try {
            //现获取用户信息
            YangMember yangMemberSelf = yangMemberTokenService.findMember(yangMemberToken);
            if (yangMemberSelf == null) {
                throw new TokenExpirException("token 错误");
            }

            YangFtLock yangFtLock = new YangFtLock();
            yangFtLock.setMemberId(yangMemberSelf.getMemberId());
            yangFtLock.setNum(num);
            int r = memberMapper.platFormCurrencyUnLock(yangFtLock);
            if (r == 1) {
                result.setCode(Result.Code.ERROR);
                result.setErrorCode(ErrorCode.ERROR_TRADE_CURRENCY_USER_PRICE_NOTENOUGH.getIndex());
                result.setMsg(ErrorCode.ERROR_TRADE_CURRENCY_USER_PRICE_NOTENOUGH.getMessage());
            } else {
                result.setCode(Result.Code.SUCCESS);
            }
        } catch (TokenExpirException e) {

            result.setErrorCode(ErrorCode.ERROR_ERROR_TOKEN.getIndex());
            result.setMsg(ErrorCode.ERROR_ERROR_TOKEN.getMessage());
            result.setCode(Result.Code.ERROR);

        } catch (Exception e) {
            e.printStackTrace();
            result.setCode(Result.Code.ERROR);
        }

        return result;
    }

    public Criteria getConditionGroup(Map params) {

        int currencyId = (Integer) params.get("currencyId");
        int currencyTrade = (Integer) params.get("currencyTrade");

        Criteria a = new Criteria();
        Criteria b = new Criteria();
        Criteria c = new Criteria();
        Criteria d = new Criteria();
        Criteria e = new Criteria();

        c = c.andOperator(Criteria.where("currencyId").is(currencyId));
        e = e.andOperator(Criteria.where("currencyTradeId").is(currencyTrade));
        if (null != params.get("startTime") && StringUtils.isNotBlank(String.valueOf(params.get("startTime"))) && (null != params.get("endTime") && StringUtils.isNotBlank(String.valueOf(params.get("endTime"))))) {
            int start = Integer.valueOf((String) params.get("startTime"));
            int end = Integer.valueOf((String) params.get("endTime"));
            b = b.andOperator(Criteria.where("addTime").gte(start).lte(end));
        }
        if (null != params.get("memberId") && StringUtils.isNotBlank(String.valueOf(params.get("memberId")))) {
            d = d.andOperator(Criteria.where("memberId").is(Integer.valueOf((String) params.get("memberId"))));
        }

        a.andOperator(b, c, d, e);
        return a;
    }

    /**
     * 获取制定时间挖矿数量
     *
     * @return 获取的是funt
     */
    private BigDecimal getMiningNums(HashMap pama, YangTrade yangTrade) {
//        List<HashMap> list = yangTradeMapper.selectFeeByType(pama);
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(getConditionGroup(pama)),
                Aggregation.group("type").sum("fee").as("total").first("type").as("type"));
        AggregationResults<HashMap> outputTypeCount5 = this.mongoTemplate.aggregate(aggregation, CoinConst.MONGODB_TRADE_COLLECTION, HashMap.class);
        List<HashMap> list = outputTypeCount5.getMappedResults();


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

    private YangTrade getTrade(int currencyId, int currencyTrade) {
//        Example example = new Example(YangTrade.class);
//        example.createCriteria().andEqualTo("currencyId", currencyId).andEqualTo("currencyTradeId", currencyTrade);
//        example.setOrderByClause("trade_id desc");
//        List<YangTrade> list = yangTradeMapper.selectByExample(example);

        Query query = new Query();
        query.addCriteria(Criteria.where("currencyId").is(currencyId)).addCriteria(Criteria.where("currencyTradeId").is(currencyTrade));
        query.with(new Sort(new Sort.Order(Sort.Direction.DESC, "addTime")));
        query.skip(0).limit(1);
        List<MgYangTrade> list = this.mongoTemplate.find(query, MgYangTrade.class, CoinConst.MONGODB_TRADE_COLLECTION);


        if (null != list && list.size() > 0) {
            MgYangTrade mgYangTrade = list.get(0);
            YangTrade yangTrade = new YangTrade();
            yangTrade.setPrice(new BigDecimal(mgYangTrade.getPrice()));
            return yangTrade;
        } else {
            return null;
        }
    }

    /**
     * 获取挖矿信息
     */
    public Result getMiningInfos(String accessToken) {
        Result result = new Result();
        result.setCode(Result.Code.ERROR);
        MiningInfos miningInfos;
        YangMember yangMemberSelf = null;
        try {

            if (StringUtils.isNotEmpty(accessToken)) {
                YangMemberToken yt = new YangMemberToken();
                yt.setAccessToken(accessToken);
                yangMemberSelf = yangMemberTokenService.findMember(yt);
            }

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
            pama.put("currencyId", CoinConst.FUNT_CURRENCY_ID);
            pama.put("currencyTrade", 37);
//            Example example = new Example(YangTrade.class);
//            example.createCriteria().andEqualTo("currencyId", 80).andEqualTo("currencyTradeId", 37);
//            example.setOrderByClause("trade_id desc");
//            List<YangTrade> list = yangTradeMapper.selectByExample(example);
            //token不为空 查询个人数据  空则查询全局数据
            if (StringUtils.isNotEmpty(accessToken)) {
                if (redisService.keysExist(CoinConst.MININGINFOS + accessToken) && redisService.get(CoinConst.MININGINFOS + accessToken) != null) {
                    miningInfos = JSONObject.parseObject(redisService.get(CoinConst.MININGINFOS + accessToken).toString(), MiningInfos.class);
                } else {
                    yangTrade = this.getTrade(80, 37);
                    if (yangMemberSelf != null) {
                        pama.put("startTime", String.valueOf(DateUtils.getTimeStamp(enddate + " 0:0:0") / 1000));
                        pama.put("endTime", String.valueOf(DateUtils.getTimeStamp(enddate + " 23:59:59") / 1000));
                        pama.put("memberId", String.valueOf(yangMemberSelf.getMemberId()));
                        miningInfos.setLastRake(yangMiningInfoMapper.getRakeInfo(pama));//我的前一日挖矿返佣
                        miningInfos.setLastLockMoney(yangFtLockRebateRecodeMapper.getLockInfo(pama));//我的昨日分红

                        pama.put("startTime", "");
                        pama.put("endTime", "");

                        miningInfos.setHasMine(this.getMiningNums(pama, yangTrade).multiply(BigDecimal.valueOf(1.1))); //我的累计挖矿
                        miningInfos.setHasRake(yangMiningInfoMapper.getRakeInfo(pama)); //我的累计返佣
                        miningInfos.setLockMoney(yangFtLockRebateRecodeMapper.getLockInfo(pama));//我的锁仓分红收益

                        miningInfos.setLockNum(yangFtLockMapper.getLockInfo(pama));//我的锁仓数量
                        redisService.set(CoinConst.MININGINFOS + accessToken, JSONObject.toJSON(miningInfos), 5L, TimeUnit.MINUTES);
                    }
                }
            } else {
                if (redisService.keysExist(CoinConst.MININGINFOS) && redisService.get(CoinConst.MININGINFOS) != null) {
                    miningInfos = JSONObject.parseObject(redisService.get(CoinConst.MININGINFOS).toString(), MiningInfos.class);
                } else {
//                    yangTrade = this.getTrade(80, 37);
//                    //前一日
//                    pama.put("startTime", DateUtils.getTimeStamp(enddate + " 0:0:0") / 1000);
//                    pama.put("endTime", DateUtils.getTimeStamp(enddate + " 23:59:59") / 1000);
//                    lastMine = this.getMiningNums(pama, yangTrade).multiply(BigDecimal.valueOf(1.1));
//                    miningInfos.setLastMine(lastMine);
//
//                    lastMineETH = lastMine.multiply(yangTrade.getPrice());
//                    miningInfos.setLastMineETH(lastMineETH);
//
//                    //前一日总返佣值
//                    miningInfos.setLastRakeTotal(yangMiningInfoMapper.getRakeInfo(pama));
//                    //前一日累计分红
//                    miningInfos.setLastLockTotal(yangFtLockRebateRecodeMapper.getLockInfo(pama));
//
//                    //--------------------------------------------------------------------------------------------------
//                    //所有的
//                    pama.put("startTime", "");
//                    pama.put("endTime", "");
//                    hasMineTotal = this.getMiningNums(pama, yangTrade).multiply(BigDecimal.valueOf(1.1));
//                    miningInfos.setHasMineTotal(hasMineTotal);//已经挖矿总量ETH
//                    hasMineETH = hasMineTotal.multiply(yangTrade.getPrice());
//                    miningInfos.setHasMineETH(hasMineTotal.multiply(yangTrade.getPrice()));//已经挖矿总量ETH
//                    //累计返佣
//                    miningInfos.setHasRakeTotal(yangMiningInfoMapper.getRakeInfo(pama));
//                    //累计分红
//                    miningInfos.setHasLockTotal(yangFtLockRebateRecodeMapper.getLockInfo(pama));
//
//                    //累计锁仓
//                    miningInfos.setHasLockNum(yangFtLockMapper.getLockInfo(pama));
//                    //--------------------------------------------------------------------------------------------------
//                    //hourAssign 本小时待分配
//                    System.out.println("当前时间戳 " + new Date());
//                    pama.put("startTime", System.currentTimeMillis() / 1000 - 3600);
//                    pama.put("endTime", System.currentTimeMillis() / 1000);
//                    hourAssign = this.getMiningNums(pama, yangTrade).multiply(BigDecimal.valueOf(1.1));
//                    miningInfos.setHourAssign(hourAssign);
//                    //hourAssign 本小时待分配
//
//                    //dayAssign 今日待分配
//                    pama.put("startTime", DateUtils.getTimeStamp(dateNow + " 0:0:0") / 1000);
//                    pama.put("endTime", DateUtils.getTimeStamp(dateNow + " 23:59:59") / 1000);
//                    System.out.println(" 开始时间" + pama.get("startTime"));
//                    System.out.println(" 结束时间" + pama.get("endTime"));
//                    dayAssign = this.getMiningNums(pama, yangTrade).multiply(BigDecimal.valueOf(1.1));
//
//                    miningInfos.setDayAssign(dayAssign);
//                    //dayAssign 今日待分配
//
//                    //累计回购基金总量兑换ETH 算手续费的%40
//
//                    hasBackETH = hasMineETH.multiply(BigDecimal.valueOf(0.4));
//                    miningInfos.setHasBackETH(hasBackETH);
//                    //lastBackETH 昨天
//                    BigDecimal lastBackETH = BigDecimal.ZERO;
//                    lastBackETH = lastMineETH.multiply(BigDecimal.valueOf(0.4));
//                    miningInfos.setLastBackETH(lastBackETH);
//                    //lastBackETH 昨天
//
//
//                    //累计回购基金总量兑换ETH 算手续费的%40
//
//
//                    miningInfos.setLeftMineTotal(mineTotal.subtract(hasMineTotal));
//
//                    miningInfos.setMineTotal(mineTotal);
//                    redisService.set(CoinConst.MININGINFOS,JSONObject.toJSON(miningInfos),5L, TimeUnit.MINUTES);
                }

            }

            System.out.println(miningInfos);

            result.setData(miningInfos);
            System.out.println(result);
            result.setCode(Result.Code.SUCCESS);

        } catch (TokenExpirException e) {

            result.setErrorCode(ErrorCode.ERROR_ERROR_TOKEN.getIndex());
            result.setMsg(ErrorCode.ERROR_ERROR_TOKEN.getMessage());
            result.setCode(Result.Code.ERROR);

        } catch (Exception e) {
            e.printStackTrace();
            result.setCode(Result.Code.ERROR);
        }

        return result;

    }


    /**
     * 获取挖矿信息
     */
    public Result getMemberMiningInfos(YangMemberToken yangMemberToken) {
        Result result = new Result();
        result.setCode(Result.Code.ERROR);
        MemberMinginfos memberMinginfos;
        try {


            //现获取用户信息
            YangMember yangMemberSelf = yangMemberTokenService.findMember(yangMemberToken);
            if (yangMemberSelf == null) {
                throw new TokenExpirException("token 错误");
            }

            memberMinginfos = new MemberMinginfos();


            //昨日个人挖矿返佣 lastMine
            String enddate = DateUtils.getDateStrPre(-1);
            String startdate = DateUtils.getDateStrPre(-2);
            String dateNow = DateUtils.getDateStrPre(0);

            BigDecimal lastMine = BigDecimal.ZERO;

            HashMap pama = new HashMap();
            pama.put("startdate", enddate);
            pama.put("enddate", enddate);
            pama.put("memberId", yangMemberSelf.getMemberId());

            lastMine = yangMiningInfoMapper.getMiningInfos(pama);

            memberMinginfos.setLastMine(lastMine);


            BigDecimal totalMine = BigDecimal.ZERO;
            pama.put("startdate", "");
            pama.put("enddate", "");
            pama.put("memberId", yangMemberSelf.getMemberId());
            totalMine = yangMiningInfoMapper.getMiningInfos(pama);
            memberMinginfos.setTotalMine(totalMine);


//            cardMine  会员卡锁仓量
            YangCardNum yangCardNum = new YangCardNum();
            yangCardNum.setMemberid(yangMemberSelf.getMemberId());
            YangCardNum yangCardNum1 = yangCardNumMapper.selectOne(yangCardNum);
            if (yangCardNum1 != null) {
                memberMinginfos.setCardMine(yangCardNum1.getNum().subtract(yangCardNum1.getDealnum()));
            }


            YangFtLock yangFtLock = new YangFtLock();
            yangFtLock.setMemberId(yangMemberSelf.getMemberId());
            YangFtLock yangFtLock1 = yangFtLockMapper.selectOne(yangFtLock);
            if (yangFtLock1 != null) {
                memberMinginfos.setPool(yangFtLock1.getForzenNum());
            }

            //totalLock累计锁仓分红
            HashMap pama2 = new HashMap();
            pama2.put("memberId", yangMemberSelf.getMemberId());
            BigDecimal totalLock = BigDecimal.ZERO;
            totalLock = yangFtLockRebateRecodeMapper.getTotal(pama);
            memberMinginfos.setTotalLock(totalLock);


            result.setCode(Result.Code.SUCCESS);
            result.setData(memberMinginfos);

        } catch (TokenExpirException e) {

            result.setErrorCode(ErrorCode.ERROR_ERROR_TOKEN.getIndex());
            result.setMsg(ErrorCode.ERROR_ERROR_TOKEN.getMessage());
            result.setCode(Result.Code.ERROR);

        } catch (Exception e) {
            e.printStackTrace();
            result.setCode(Result.Code.ERROR);
        }


        return result;

    }


    /**
     * 获取挖矿信息
     */
    public Result getMiningInfosByMember(YangMemberToken yangMemberToken, Page page, String begin, String end) {
        Result result = new Result();
        result.setCode(Result.Code.SUCCESS);
        try {
            Map param = new HashMap<String, Object>();
            //现获取用户信息
            YangMember yangMemberSelf = yangMemberTokenService.findMember(yangMemberToken);
//            if (yangMemberSelf == null) {
//                throw new TokenExpirException("token 错误");
//            }
//            Example example = new Example(YangMiningInfo.class);
//            example.setOrderByClause("group by  tradeid");
//            example.orderBy("addtime desc");
//            Example.Criteria criteria = example.createCriteria();
            if (yangMemberSelf != null) {
//                criteria.andEqualTo("memberid",yangMemberSelf.getMemberId());
                param.put("memberId", yangMemberSelf.getMemberId());
            }
            if (StringUtils.isNotEmpty(end)) {
//                criteria.andLessThanOrEqualTo("addtime",Integer.valueOf(end));
                param.put("endTime", end);
            }
            if (StringUtils.isNotEmpty(begin)) {
//                criteria.andGreaterThanOrEqualTo("addtime",Integer.valueOf(begin));
                param.put("startTime", begin);
            }
            param.put("start", (page.getPageNum() - 1) * page.getPageSize());
            param.put("end", page.getPageSize());
//            PageHelper.startPage(page.getPageNum(),page.getPageSize());
//            List<YangMiningInfo> list = yangMiningInfoMapper.selectByExample(example);
            List<Map> list = yangMiningInfoMapper.getMiningInfoByMember(param);
            LinkedList linkedList = new LinkedList();
//            YangTrade yangTrade = this.getTrade(80,37);
            BigDecimal price = new BigDecimal(0);
            if (redisService.keysExist(CoinConst.REDIS_COIN_PAIR_BASE_ + "80-37")) {
                price = CommonTools.formatNull2BigDecimal(redisService.hmGet(CoinConst.REDIS_COIN_PAIR_BASE_ + "80-37", "new_price"));
            }
            for (Map y : list
                    ) {
                YangMiningInfoExt yangMiningInfoExt = new YangMiningInfoExt();
//                BeanUtils.copyProperties(y,yangMiningInfoExt);
                yangMiningInfoExt.setEthNum(((BigDecimal) y.get("total")).multiply(price));
                yangMiningInfoExt.setAdddate((String) y.get("addDate"));
                yangMiningInfoExt.setNum((BigDecimal) y.get("total"));
//                if(yangTrade != null)
//                {
//                }
                linkedList.add(yangMiningInfoExt);
            }
            BigDecimal a = yangMiningInfoMapper.getUserRake(param);
            PageInfo<YangMiningInfoExt> pageInfo = new PageInfo<YangMiningInfoExt>(linkedList);
            pageInfo.setTotal(Long.valueOf(a.toString()));
            result.setData(pageInfo);
            result.setCode(Result.Code.SUCCESS);
        } catch (TokenExpirException e) {
            result.setErrorCode(ErrorCode.ERROR_ERROR_TOKEN.getIndex());
            result.setMsg(ErrorCode.ERROR_ERROR_TOKEN.getMessage());
            result.setCode(Result.Code.ERROR);
        } catch (Exception e) {
            e.printStackTrace();
            result.setCode(Result.Code.ERROR);
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
            result.setMsg(ErrorCode.ERROR_SYSTEM.getMessage());
        }
        return result;
    }

    /**
     * 找回交易密码
     *
     * @return
     */
    public Result findBackTradePass(YangMemberToken yangMemberToken, String pass, String emailCode) {
        Result result = new Result();

        //现获取用户信息
        YangMember yangMemberSelf = yangMemberTokenService.findMember(yangMemberToken);
        if (yangMemberSelf != null) {
            String key = CoinConst.EMAILFORFINDCODE + yangMemberSelf.getEmail() + "_" + CoinConst.EMAIL_CODE_TYPE_TRADEPWD;
            if (redisService.exists(key)) {
                EmailLog emailLog = JSONObject.parseObject(redisService.get(key).toString(), EmailLog.class);
                if (emailCode.equals(emailLog.getContent())) {
                    //设置交易密码
                    yangMemberSelf.setPwdtrade(DigestUtils.md5Hex(pass));
                    memberMapper.updateByPrimaryKeySelective(yangMemberSelf);

                    result.setCode(Result.Code.SUCCESS);
                } else {
                    result.setCode(Result.Code.ERROR);
                    result.setMsg("验证码错误,请重新请求");
                    result.setErrorCode(ErrorCode.ERROR_ERROR_CODE.getIndex());
                }
            } else {
                result.setCode(Result.Code.ERROR);
                result.setMsg("验证码不存在或已过期");
                result.setErrorCode(ErrorCode.ERROR_ERROR_CODE.getIndex());
            }
        } else {
            result.setErrorCode(ErrorCode.ERROR_ERROR_TOKEN.getIndex());
            result.setMsg(ErrorCode.ERROR_ERROR_TOKEN.getMessage());
            result.setCode(Result.Code.ERROR);
        }
        return result;
    }


    /**
     * 发送邮箱验证码
     *
     * @param email
     * @param type
     * @param title
     * @param content
     * @return
     */
    public Result sendEmailCode(String email, String type, String title, String content, int sendTimes, HttpServletRequest request) {
        Result result = new Result();
        result.setCode(Result.Code.SUCCESS);
        //key
        String key = CoinConst.EMAILFORFINDCODE + email + "_" + type;
        String sendkey = CoinConst.EMAILFORFINDCODE + email + "_" + type + "_60sec";
        if (redisService.exists(sendkey)) {
            result.setErrorCode(ErrorCode.HAS_SEND_EMAILCODE.getIndex());
            result.setCode(Result.Code.ERROR);
            result.setMsg(ErrorCode.HAS_SEND_EMAILCODE.getMessage());
            return result;
        }
        //String emailCode = EmailUtils.getRandomNumber(6);
        Example example = new Example(YangEmail.class);
        example.setOrderByClause("send_time asc");

        example.createCriteria().andEqualTo("status", CoinConst.EMAIL_STATUS_NORMAL);
        List<YangEmail> emailList = yangEmailMapper.selectByExample(example);
        System.out.println(emailList.get(0).getUsername() + "------------" + sendTimes);
        if (sendTimes > 3) {
            result.setErrorCode(ErrorCode.SEND_EMAILCODE_ERROR.getIndex());
            result.setCode(Result.Code.ERROR);
            result.setMsg(ErrorCode.SEND_EMAILCODE_ERROR.getMessage());
            return result;
        }
        YangEmail yangEmail = emailList.get(0);
        //发件人账户名
        String senderAccount = yangEmail.getUsername();
        //发件人账户密码
        String senderPassword = yangEmail.getPassword();
        try {
            //1、连接邮件服务器的参数配置
            Properties props = new Properties();
            //设置用户的认证方式
            props.setProperty("mail.smtp.auth", "true");
            //设置传输协议s
            props.setProperty("mail.transport.protocol", "smtp");
            //设置发件人的SMTP服务器地址
            props.setProperty("mail.smtp.host", yangEmail.getEmailHost());

            // 如果使用ssl，则去掉使用25端口的配置，进行如下配置
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

            props.put("mail.smtp.socketFactory.port", yangEmail.getPort().toString());

            props.put("mail.smtp.port", yangEmail.getPort().toString());

            //2、创建定义整个应用程序所需的环境信息的 Session 对象
            Session session = Session.getInstance(props);
            //设置调试信息在控制台打印出来
            session.setDebug(false);
            //3、创建邮件的实例对象
            Message msg = EmailUtils.getMimeMessage(session, email, yangEmail.getSendUser(), title, "", content);
            //4、根据session对象获取邮件传输对象Transport
            Transport transport = session.getTransport();
            //设置发件人的账户名和密码
            transport.connect(senderAccount, senderPassword);
            //发送邮件，并发送到所有收件人地址，message.getAllRecipients() 获取到的是在创建邮件对象时添加的所有收件人, 抄送人, 密送人
            transport.sendMessage(msg, msg.getAllRecipients());

            //如果只想发送给指定的人，可以如下写法
            //transport.sendMessage(msg, new Address[]{new InternetAddress("xxx@qq.com")});
            //5、关闭邮件连接
            transport.close();
            //存入redis
            EmailLog emailLog = new EmailLog();
            emailLog.setContent(content);
            emailLog.setWrong_times("0");
            redisService.set(key, JSONObject.toJSONString(emailLog), 15L, TimeUnit.MINUTES);
            redisService.set(sendkey, "1", 60L, TimeUnit.SECONDS);
            //更新邮箱信息
            yangEmail.setSendNum(yangEmail.getSendNum() + 1);
            yangEmail.setSendTime(DateUtils.getNowTimes());
            yangEmailMapper.updateByPrimaryKeySelective(yangEmail);
            //更新邮箱日志
            YangEmailLog yangEmailLog = new YangEmailLog();
            yangEmailLog.setAcceptMail(email);
            yangEmailLog.setSendMail(yangEmail.getSendUser());
            yangEmailLog.setAddTime(DateUtils.getNowTimes());
            yangEmailLog.setTitle(title);
            yangEmailLog.setContent(content);
            //获取访问者IP地址
            String ipAddr = GetIpAddr.getIpAddr(request);
            yangEmailLog.setIp(ipAddr);
            yangEmailLog.setType(Integer.valueOf(type));
            yangEmailLog.setWrongTimes(0);
            yangEmailLogMapper.insert(yangEmailLog);
            result.setMsg("发送成功");
            result.setData(null);
            result.setCode(Result.Code.SUCCESS);
        } catch (Exception e) {
            sendTimes++;
            e.printStackTrace();
            yangEmail.setError(String.valueOf(Integer.valueOf(yangEmail.getError()).intValue() + 1));
            yangEmail.setSendNum(yangEmail.getSendNum() + 1);
            yangEmail.setSendTime(DateUtils.getNowTimes());
            yangEmailMapper.updateByPrimaryKeySelective(yangEmail);
            sendEmailCode(email, type, title, content, sendTimes, request);
            result.setCode(Result.Code.ERROR);
            result.setMsg("发送失败");
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
        }
        return result;
    }

    public Result sendNormalEmailCode(String email, String title, String content, int sendTimes) {
        Result result = new Result();
        result.setCode(Result.Code.SUCCESS);

        Example example = new Example(YangEmail.class);
        example.setOrderByClause("send_time asc");
        example.createCriteria().andEqualTo("status", CoinConst.EMAIL_STATUS_NORMAL);
        List<YangEmail> emailList = yangEmailMapper.selectByExample(example);
        System.out.println(emailList.get(0).getUsername() + "------------" + sendTimes);
        if (sendTimes > 3) {
            result.setErrorCode(ErrorCode.SEND_EMAILCODE_ERROR.getIndex());
            result.setCode(Result.Code.ERROR);
            result.setMsg(ErrorCode.SEND_EMAILCODE_ERROR.getMessage());
            return result;
        }
        YangEmail yangEmail = emailList.get(0);
        //发件人账户名
        String senderAccount = yangEmail.getUsername();
        //发件人账户密码
        String senderPassword = yangEmail.getPassword();

        try {
            //1、连接邮件服务器的参数配置
            Properties props = new Properties();
            //设置用户的认证方式
            props.setProperty("mail.smtp.auth", "true");
            //设置传输协议s
            props.setProperty("mail.transport.protocol", "smtp");
            //设置发件人的SMTP服务器地址
            props.setProperty("mail.smtp.host", yangEmail.getEmailHost());
            // 如果使用ssl，则去掉使用25端口的配置，进行如下配置
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

            props.put("mail.smtp.socketFactory.port", yangEmail.getPort().toString());

            props.put("mail.smtp.port", yangEmail.getPort().toString());
            //2、创建定义整个应用程序所需的环境信息的 Session 对象
            Session session = Session.getInstance(props);
            //设置调试信息在控制台打印出来
            session.setDebug(false);
            //3、创建邮件的实例对象
            Message msg = EmailUtils.getNormalMimeMessage(session, email, yangEmail.getSendUser(), title, content);
            //4、根据session对象获取邮件传输对象Transport
            Transport transport = session.getTransport();
            //设置发件人的账户名和密码
            transport.connect(senderAccount, senderPassword);
            //发送邮件，并发送到所有收件人地址，message.getAllRecipients() 获取到的是在创建邮件对象时添加的所有收件人, 抄送人, 密送人
            transport.sendMessage(msg, msg.getAllRecipients());
            //如果只想发送给指定的人，可以如下写法
            //transport.sendMessage(msg, new Address[]{new InternetAddress("xxx@qq.com")});
            //5、关闭邮件连接
            transport.close();
            //更新邮箱信息
            yangEmail.setSendNum(yangEmail.getSendNum() + 1);
            yangEmail.setSendTime(DateUtils.getNowTimes());
            yangEmailMapper.updateByPrimaryKeySelective(yangEmail);
        } catch (Exception e) {
            sendTimes++;
            e.printStackTrace();
            result.setErrorCode(ErrorCode.SEND_EMAILCODE_ERROR.getIndex());
            result.setCode(Result.Code.ERROR);
            result.setMsg(ErrorCode.SEND_EMAILCODE_ERROR.getMessage());
            yangEmail.setError(String.valueOf(Integer.valueOf(yangEmail.getError()).intValue() + 1));
            yangEmail.setSendNum(yangEmail.getSendNum() + 1);
            yangEmail.setSendTime(DateUtils.getNowTimes());
            yangEmailMapper.updateByPrimaryKeySelective(yangEmail);
            sendNormalEmailCode(email, title, content, sendTimes);
        }
        return result;
    }

    /**
     * 根据邮箱注册
     *
     * @param email
     * @param emailCode
     * @param pwd
     * @param nationality
     * @param inviteCode
     * @param request
     * @return
     */
    public Result userRegisterByEmail(String email, String emailCode, String pwd, String nationality, String inviteCode, HttpServletRequest request, String nationalityCode) {
        Result result = new Result();
        try {
            YangMember yangMember = new YangMember();
            yangMember.setEmail(email);
            if (!checkEmailRepeat(yangMember)) {
                result.setErrorCode(ErrorCode.ERROR_EMAIL_REPEAT.getIndex());
                result.setCode(Result.Code.ERROR);
                result.setMsg(ErrorCode.ERROR_EMAIL_REPEAT.getMessage());
                return result;
            }
            String key = CoinConst.EMAILFORFINDCODE + email + "_" + CoinConst.EMAIL_CODE_TYPE_ZC;
            if (redisService.exists(key)) {
                EmailLog emailLog = JSONObject.parseObject(redisService.get(key).toString(), EmailLog.class);
                if (emailCode.equals(emailLog.getContent())) {
                    //获取配置
                    YangConfig yangConfig = new YangConfig();
                    yangConfig.setKey("invit_tay");
                    YangConfig yangConfigFind = yangConfigService.selectOne(yangConfig);

                    if (null == yangConfigFind || StringUtils.isBlank(yangConfigFind.getValue())) {
                        throw new RuntimeException("invit_tay 配置不存在");
                    }
                    yangMember.setInvitTime(Integer.valueOf(yangConfigFind.getValue()) * 86400 + System.currentTimeMillis() / 1000);
                    yangMember.setRegTime(System.currentTimeMillis() / 1000);
                    yangMember.setShenhestatus(0);
                    yangMember.setPwd(DigestUtils.md5Hex(pwd));
                    //获取访问者ip
                    String ipAddr = GetIpAddr.getIpAddr(request);
                    yangMember.setIp(ipAddr);
                    //生成邀请码,做验证重复校验
                    String invCode;
                    invCode = RandomUtil.generateStr(6);
                    Boolean inviteCodeRepeat;
                    inviteCodeRepeat = checkInviteCodeRepeat(yangInviteCodeService, invCode);
                    //如果存在，再去随机生成一个邀请码，并判断新邀请码是否存在
                    while (inviteCodeRepeat) {
                        invCode = RandomUtil.generateStr(6);
                        inviteCodeRepeat = checkInviteCodeRepeat(yangInviteCodeService, invCode);
                        //如果该邀请码不存在，结束循环
                        if (inviteCodeRepeat == false) {
                            break;
                        }
                    }
                    yangMember.setInviteCode(invCode);
                    yangInviteCodeService.addInviteCode(invCode);
                    //邀请码不为空
                    if (StringUtils.isNotEmpty(inviteCode)) {
                        //该邀请码的用户线下人数加1
                        memberMapper.updateByInviteCode(inviteCode);
                    }
                    yangMember.setNationality(nationality);
                    yangMember.setNationalityCode(nationalityCode);
                    saveSelective(yangMember);
                    result.setCode(Result.Code.SUCCESS);
                    result.setMsg("注册成功");
                } else {
                    result.setCode(Result.Code.ERROR);
                    result.setMsg("验证码错误,请重新请求");
                    result.setErrorCode(ErrorCode.ERROR_ERROR_CODE.getIndex());
                }
            } else {
                result.setCode(Result.Code.ERROR);
                result.setMsg("验证码不存在或已过期");
                result.setErrorCode(ErrorCode.ERROR_ERROR_CODE.getIndex());
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.setErrorCode(ErrorCode.SEND_EMAILCODE_ERROR.getIndex());
            result.setCode(Result.Code.ERROR);
            result.setMsg(ErrorCode.SEND_EMAILCODE_ERROR.getMessage());
        }
        return result;
    }

    /**
     * 根据手机号注册
     *
     * @param phone
     * @param noteCode
     * @param pwd
     * @param nationality
     * @param inviteCode
     * @param request
     * @return
     */
    public Result userRegisterByPhone(String phone, String noteCode, String pwd, String nationality, String inviteCode, HttpServletRequest request, String nationalityCode) {
        Result result = new Result();
        try {
            YangMember yangMember = new YangMember();
            yangMember.setPhone(phone);
            if (!checkPhoneRepeat(yangMember)) {
                result.setErrorCode(ErrorCode.ERROR_EMAIL_REPEAT.getIndex());
                result.setCode(Result.Code.ERROR);
                result.setMsg("手机号已注册");
                return result;
            }
            String key = CoinConst.EMAILFORFINDCODE + phone + "_" + "1";
            if (redisService.exists(key)) {
                String numStr = (String) redisService.get(key);
                if (noteCode != null && noteCode.equals(numStr)) {
                    yangMember.setInvitTime(365 * 86400 + System.currentTimeMillis() / 1000);
                    yangMember.setRegTime(System.currentTimeMillis() / 1000);
                    yangMember.setShenhestatus(0);
                    yangMember.setPwd(DigestUtils.md5Hex(pwd));
                    //获取访问者ip
                    String ipAddr = GetIpAddr.getIpAddr(request);
                    yangMember.setIp(ipAddr);
                    //生成邀请码,做验证重复校验
                    String invCode;
                    invCode = RandomUtil.generateStr(6);
                    Boolean inviteCodeRepeat;
                    inviteCodeRepeat = checkInviteCodeRepeat(yangInviteCodeService, invCode);
                    //如果存在，再去随机生成一个邀请码，并判断新邀请码是否存在
                    while (inviteCodeRepeat) {
                        invCode = RandomUtil.generateStr(6);
                        inviteCodeRepeat = checkInviteCodeRepeat(yangInviteCodeService, invCode);
                        //如果该邀请码不存在，结束循环
                        if (inviteCodeRepeat == false) {
                            break;
                        }
                    }
                    yangMember.setInviteCode(invCode);
                    yangInviteCodeService.addInviteCode(invCode);
                    //邀请码不为空
                    if (StringUtils.isNotEmpty(inviteCode)) {
                        //该邀请码的用户线下人数加1
                        memberMapper.updateByInviteCode(inviteCode);
                    }
                    yangMember.setNationality(nationality);
                    yangMember.setNationalityCode(nationalityCode);
                    saveSelective(yangMember);
                    result.setCode(Result.Code.SUCCESS);
                    result.setMsg("注册成功");
                } else {
                    result.setCode(Result.Code.ERROR);
                    result.setMsg("验证码错误，请重新请求");
                }

            } else {
                result.setCode(Result.Code.ERROR);
                result.setMsg("验证码不存在或已过期");
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.setErrorCode(ErrorCode.SEND_EMAILCODE_ERROR.getIndex());
            result.setCode(Result.Code.ERROR);
            result.setMsg(ErrorCode.SEND_EMAILCODE_ERROR.getMessage());
        }
        return result;
    }

    @Transient
    public Result certificate(String accessToken, MultipartFile pic1, MultipartFile pic2, MultipartFile pic3) {
        Result result = new Result();
        try {
            validateAccessToken(accessToken, result, yangMemberTokenService);
            if (result.getCode().intValue() == Result.Code.ERROR.intValue()) {
                return result;
            }
            YangMember yangMember = (YangMember) result.getData();
            if (pic1 != null && pic1.getSize() != 0 && pic1 != null && pic1.getSize() != 0 && pic1 != null && pic1.getSize() != 0) {

                String readeUrl = userProPs.getFtp().get("oldReadHost");
                String savePath = userProPs.getFtp().get("savePath");
                String rootPath = userProPs.getFtp().get("rootPath");
                Result a = FtpUtils.uploadFileLocalFile(pic1, result, rootPath + savePath);
                Result b = FtpUtils.uploadFileLocalFile(pic2, result, rootPath + savePath);
                Result c = FtpUtils.uploadFileLocalFile(pic3, result, rootPath + savePath);
                if (a.getCode().intValue() == Result.Code.ERROR) {
                    return a;
                }
                if (b.getCode().intValue() == Result.Code.ERROR) {
                    return b;
                }
                if (c.getCode().intValue() == Result.Code.ERROR) {
                    return c;
                }
                //进行保存操作
                YangMember yangMemberUpdate = new YangMember();
                yangMemberUpdate.setMemberId(yangMember.getMemberId());
                yangMemberUpdate.setPic1(readeUrl + savePath + (String) a.getData());
                yangMemberUpdate.setPic2(readeUrl + savePath + (String) b.getData());
                yangMemberUpdate.setPic3(readeUrl + savePath + (String) c.getData());
                yangMemberUpdate.setShenhestatus(Integer.valueOf(3));
                yangMemberUpdate.setSafeTime(String.valueOf(System.currentTimeMillis() / 1000));
                int res = memberMapper.updateByPrimaryKeySelective(yangMemberUpdate);
            } else {
                //确认上传三个图片
                result.setCode(Result.Code.ERROR);
                result.setErrorCode(ErrorCode.ERROR_PIC_D.getIndex());
                result.setMsg(ErrorCode.ERROR_PIC_D.getMessage());
                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.setErrorCode(ErrorCode.SEND_EMAILCODE_ERROR.getIndex());
            result.setCode(Result.Code.ERROR);
            result.setMsg(ErrorCode.SEND_EMAILCODE_ERROR.getMessage());
        }
        return result;
    }

    //保存图片
    public Result saveImage(MultipartFile pic, String type) {
        String adminPath;
        if ("0".equals(type)) {
            adminPath = userProPs.getFtp().get("adminNewPath");
        } else {
            adminPath = userProPs.getFtp().get("savePath");
        }
        String date = DateUtils.getNoDateDay(new Date(), "yyyy-MM-dd");
        adminPath = adminPath + date;
        Result result = FtpUtils.uploadFileFromBackSystem(pic, userProPs.getFtp().get("rootPath"), adminPath);
        return result;
    }

    public YangMember loginByPhone(YangMember yangMember) {
        YangMember yangMemberQuery = new YangMember();
        yangMemberQuery.setPhone(yangMember.getPhone());
        YangMember yangMemberFind = mapper.selectOne(yangMemberQuery);
        if (null == yangMemberFind) {
            throw new EmailNotExistException("该用户不存在");
        }
        //被禁止登录
        if (checkRecord(yangMember.getPwd())) {
            throw new PwdErrorException("密码错误次数过多，请十五分钟后重试");
        }
        if (!yangMemberFind.getPwd().equals(DigestUtils.md5Hex(yangMember.getPwd()))) {
            recordTimes(yangMember.getPhone());
            throw new PasswordErrorException(ErrorCode.ERROR_PASSWORD_ERR.getMessage());
        }
        if (yangMemberFind.getIsLock().intValue() == 1) {
            throw new MemberIsLocketException(ErrorCode.MEMBER_HAS_LOCKED.getMessage());
        }
        return yangMemberFind;
    }

    public YangMember loginByEmail(YangMember yangMember) {
        YangMember yangMemberQuery = new YangMember();
        yangMemberQuery.setEmail(yangMember.getEmail());
        YangMember yangMemberFind = mapper.selectOne(yangMemberQuery);
        if (null == yangMemberFind) {
            throw new EmailNotExistException("该用户不存在");
        }
        //被禁止登录
        if (checkRecord(yangMember.getPwd())) {
            throw new PwdErrorException("密码错误次数过多，请十五分钟后重试");
        }
        if (!yangMemberFind.getPwd().equals(DigestUtils.md5Hex(yangMember.getPwd()))) {
            recordTimes(yangMember.getEmail());
            throw new PasswordErrorException(ErrorCode.ERROR_PASSWORD_ERR.getMessage());

        }
        if (yangMemberFind.getIsLock().intValue() == 1) {
            throw new MemberIsLocketException(ErrorCode.MEMBER_HAS_LOCKED.getMessage());
        }
        return yangMemberFind;
    }

//    public static void main(String[] args) throws Exception {
//        String a="'a'";
//        String b="\"a\"";
//        String c=new String("a");
//        System.out.println(a.getBytes("UTF-8"));
//        System.out.println(b.getBytes("iso8859-1"));
//    }

    /**
     * 记录错误密码次数
     *
     * @param loginUserName
     */
    public void recordTimes(String loginUserName) {
        //获取redis中的值
        Integer count = (Integer) redisService.get(loginUserName);
        //如果不存在
        if (count == null) {
            count = 1;
        } else {
            //存在
            count++;
        }
        redisService.set(loginUserName, count, 900L, TimeUnit.SECONDS);
    }

    /**
     * 验证密码错误次数
     *
     * @param loginUserName
     * @return
     * @auther song
     */
    public boolean checkRecord(String loginUserName) {
        //获取redis中的值
        Integer count = (Integer) redisService.get(loginUserName);
        if (count != null && count >= 5) {
            //达到次数
            redisService.set(loginUserName + "jin", 0, 900L, TimeUnit.SECONDS);
            return true;
        }
        //不存在
        return false;
    }

    /**
     * 登录成功更新用户的登录ip和登录时间
     *
     * @param loginyangMember
     */
    public void updateYangmember(YangMember loginyangMember) {
        memberMapper.updateYangmember(loginyangMember);
    }

    /**
     * 根据email获取用户
     *
     * @param loginUserName
     * @return
     */
    public Map<String, String> getMemberByEmail(String loginUserName) {
        Map<String, String> map = memberMapper.getMemberByEmail(loginUserName);
        return map;
    }

    /**
     * 根据手机号获取用户
     *
     * @param loginUserName
     * @return
     */
    public Map<String, String> getMemberByPhone(String loginUserName) {
        Map<String, String> map = memberMapper.getMemberByPhone(loginUserName);
        return map;
    }

    /**
     * 根据邮箱重置密码
     * 椰椰
     * 2019/9/12
     *
     * @param yangMember
     * @return
     */
    public Result resetPwdByEmail(YangMember yangMember) {
        Result result = new Result();
        int i = memberMapper.resetPwdByEmail(yangMember);
        if (i == 1) {
            result.setCode(1);
            result.setMsg("重置成功");
        } else {
            result.setCode(0);
            result.setMsg("重置失败");
        }
        return result;
    }

    /**
     * 根据手机重置密码
     * 椰椰
     * 2019/9/12
     *
     * @param yangMember
     * @return
     */
    public Result resetPwdByPhone(YangMember yangMember) {
        Result result = new Result();
        int i = memberMapper.resetPwdByPhone(yangMember);
        if (i == 1) {
            result.setCode(1);
            result.setMsg("重置成功");
        } else {
            result.setCode(0);
            result.setMsg("重置失败");
        }
        return result;
    }

    /**
     * 验证账号是否正确
     * 椰椰
     * 2019/9/12
     *
     * @return
     */
    public Result checkAccount(YangMember yangMember) {
        Result result = new Result();
        Map<String,String> yangMember1 = memberMapper.checkAccount(yangMember);
        if (yangMember1 != null) {
            result.setCode(1);
            result.setMsg("正确");
        } else {
            result.setCode(0);
            result.setMsg("请输入正确的账号");
        }
        return result;
    }

    public Result changePwd(Integer memberId, String pwd) {
        Result result = new Result();
        Example example = new Example(YangMember.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("memberId", memberId);
        List<YangMember> yangMemberList = memberMapper.selectByExample(example);
        YangMember yangMember = yangMemberList.get(0);
        yangMember.setPwd(DigestUtils.md5Hex(pwd));
        int count = memberMapper.changePwd(yangMember);
        if (count == 1) {
            result.setCode(1);
            result.setMsg("修改成功");
        } else {
            result.setCode(0);
            result.setMsg("修改失败");
        }
        return result;
    }

    /**
     * 初级认证
     *
     * @param map
     * @return
     * @author ccw
     */
    public Result primaryCertification(String accessToken, Map<String, String> map) {
        YangMember yangMember = new YangMember();
        //赋值
        yangMember.setCardtype(map.get("cardType"));
        yangMember.setIdcard(map.get("idCard"));
        yangMember.setName(map.get("name"));
        yangMember.setPic1(map.get("pic1"));
        yangMember.setPic2(map.get("pic2"));
        yangMember.setPic3(map.get("pic3"));
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
        yangMember.setMemberId((Integer) memberId);
        try {
            memberMapper.updateMemberCertification(yangMember);
            result.setCode(Result.Code.SUCCESS);
            result.setMsg("初级认证成功");

        } catch (Exception e) {
            e.printStackTrace();
            result.setCode(Result.Code.ERROR);
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
            result.setMsg(ErrorCode.ERROR_SYSTEM.getMessage());
        }
        return result;

    }

    public Result getUserInfo(String accessToken) {
        YangMember yangMember;
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
            yangMember = memberMapper.getUserInfo((Integer) memberId);
            if (yangMember != null) {
                result.setCode(Result.Code.SUCCESS);
                result.setData(yangMember);
                result.setMsg("获取用户成功");
            } else {
                result.setCode(Result.Code.ERROR);
                result.setMsg("获取用户失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.setCode(Result.Code.ERROR);
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
            result.setMsg(ErrorCode.ERROR_SYSTEM.getMessage());
        }
        return result;
    }

    public Result setFiatMoneyPwd(String accessToken, String fiatMoneyPwd) {
        //验证token
        Result result = new Result();
        String token = CoinConst.TOKENKEYTOKEN + accessToken;
        //验证token
        Object memberId = redisService.get(token);
        String pwd = DigestUtils.md5Hex(fiatMoneyPwd);
        if (memberId == null) {
            result.setCode(Result.Code.SUCCESS);
            result.setErrorCode(ErrorCode.ERROR_ERROR_TOKEN_EXPIRE.getIndex());
            result.setMsg(ErrorCode.ERROR_ERROR_TOKEN_EXPIRE.getMessage());
            return result;
        }
        try {
            memberMapper.setFiatMoneyPwd((Integer) memberId, pwd);
            result.setCode(Result.Code.SUCCESS);
            result.setMsg("设置资金密码成功");
        } catch (Exception e) {
            e.printStackTrace();
            result.setCode(Result.Code.ERROR);
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
            result.setMsg(ErrorCode.ERROR_SYSTEM.getMessage());
        }
        return result;
    }

    public Result findFiatMoneyPwd(String accessToken, String pwd, String verificationCode) {
        //验证token
        Result result = new Result();
        String token = CoinConst.TOKENKEYTOKEN + accessToken;
        //验证token
        Object memberId = redisService.get(token);
        String password = DigestUtils.md5Hex(pwd);
        if (memberId == null) {
            result.setCode(Result.Code.SUCCESS);
            result.setErrorCode(ErrorCode.ERROR_ERROR_TOKEN_EXPIRE.getIndex());
            result.setMsg(ErrorCode.ERROR_ERROR_TOKEN_EXPIRE.getMessage());
            return result;
        }
        try {
            //取到用户信息
            Map yangMember = (Map) memberMapper.getUserInfo((Integer) memberId);
            //判断是邮箱验证码还是手机验证码
            String email = (String) yangMember.get("email");
            String phone = (String) yangMember.get("phone");
            if (StringUtils.isNotBlank(email)) {
                String key = CoinConst.EMAILFORFINDCODE + email + "_" + CoinConst.EMAIL_CODE_TYPE_ZC;
                if (redisService.exists(key)) {
                    EmailLog emailLog = JSONObject.parseObject(redisService.get(key).toString(), EmailLog.class);
                    //邮箱验证通过
                    if (verificationCode.equals(emailLog.getContent())) {
                        memberMapper.findFiatMoneyPwd((Integer) memberId, password);
                        result.setCode(Result.Code.SUCCESS);
                        result.setMsg("修改资金密码成功");
                        return result;
                    }
                }

            }
            if (StringUtils.isNotBlank(phone)) {
                String key = CoinConst.EMAILFORFINDCODE + phone + "_" + "1";
                if (redisService.exists(key)) {
                    String numStr = (String) redisService.get(key);
                    //手机验证码验证通过
                    if (verificationCode != null && verificationCode.equals(numStr)) {
                        memberMapper.findFiatMoneyPwd((Integer) memberId, password);
                        result.setCode(Result.Code.SUCCESS);
                        result.setMsg("修改资金密码成功");
                        return result;
                    }
                }

            }
            result.setCode(Result.Code.SUCCESS);
            result.setMsg("验证码错误");

        } catch (Exception e) {
            e.printStackTrace();
            result.setCode(Result.Code.ERROR);
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
            result.setMsg(ErrorCode.ERROR_SYSTEM.getMessage());
        }
        return result;
    }

    public Result getAddress(String accessToken, String currencyId) {
        YangQianBaoAddress yangQianBaoAddress;
        //验证token
        Result result = new Result();
        try {
            result = this.validateAccessToken(accessToken, result, yangMemberTokenService);
            if (result.getData() == null) {
                result.setCode(Result.Code.ERROR);
                result.setData("");
                result.setMsg(ErrorCode.ERROR_ERROR_TOKEN_EXPIRE.getMessage());
                return result;
            }
            YangMember yangMember = (YangMember) result.getData();
            //根据id查currency
            YangCurrency yangCurrency = yangCurrencyMapper.getYangCurrencyById(Integer.parseInt(currencyId));
            if (yangCurrency != null) {
                String fatherCyMark = yangCurrency.getFatherCyMark();
                //拼接表名
                String tableName = "yang_address_" + fatherCyMark;
                //判断表名是否存在
                Integer count = yangCurrencyMapper.isExistTable(tableName);
                if (count > 0) {
                    //存在，根据memberId去查地址，如果查到则返回，没查到插一条，并且插yang_qiaobao 直接返回address
                    Map<String, Object> map = yangCurrencyMapper.selectYangAddressByMemberId(tableName, yangMember.getMemberId());
                    if (map != null) {
                        if (StringUtils.isNotBlank(map.get("address").toString())) {
                            result.setCode(Result.Code.SUCCESS);
                            Map data = new HashMap();
                            data.put("address", map.get("address"));
                            result.setData(data);
                            result.setMsg("获取用户充币地址成功");
                            return result;
                        }
                    }
                    //为空，随机让一个member_id 更新一下，并插入qiaobao表
                    yangCurrencyMapper.updateMemberIdInYangAddress(tableName, yangMember.getMemberId());
                    //反查回来地址
                    String address = yangCurrencyMapper.selectAddressByMemberId(tableName, yangMember.getMemberId());
                    yangQianBaoAddress = new YangQianBaoAddress();
                    yangQianBaoAddress.setUserId(yangMember.getMemberId());
                    yangQianBaoAddress.setName(yangMember.getName());
                    yangQianBaoAddress.setQianbaoUrl(address);
                    yangQianBaoAddress.setStatus(1);
                    yangQianBaoAddress.setAddTime(DateUtils.getNowTimes());
                    yangQianBaoAddress.setCurrencyId(Integer.parseInt(currencyId));
                    yangQianBaoAddress.setRemark("");
                    yangQianbaoAddressMapper.insertYangQiaoBaoAddress(yangQianBaoAddress);
                    Map data = new HashMap();
                    data.put("address", address);
                    result.setCode(Result.Code.SUCCESS);
                    result.setData(data);
                    result.setMsg("获取用户充币地址成功");
                } else {
                    result.setCode(Result.Code.ERROR);
                    result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
                    result.setMsg("不存在地址表");
                    return result;
                }
            } else {
                //没有币名
                result.setCode(Result.Code.ERROR);
                result.setMsg("没有该币种");
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
     * 设置用户的资金密码
     *
     * @param memberId     用户的id
     * @param fiatMoneyPwd 用户的资金密码
     * @return 返回成功信息
     */
    public Result setsFiatMoneyPwd(Integer memberId, String fiatMoneyPwd) {
        Result result = new Result();
        Example example = new Example(YangMember.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("memberId", memberId);
        List<YangMember> yangMemberList = memberMapper.selectByExample(example);
        try {
            YangMember yangMember = yangMemberList.get(0);
            if (yangMember == null) {
                result.setCode(0);
                result.setMsg("获取失败");
            }else {
                yangMember.setFiatMoneyPwd(DigestUtils.md5Hex(fiatMoneyPwd));
                int count = memberMapper.setsFiatMoneyPwd(yangMember);
                if (count == 1) {
                    result.setCode(1);
                    result.setMsg("设置成功");
                } else {
                    result.setCode(0);
                    result.setMsg("设置失败");
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
            result.setCode(Result.Code.ERROR);
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
            result.setMsg(ErrorCode.ERROR_SYSTEM.getMessage());
        }
        return result;
    }

    /**
     * 修改资金密码
     *
     * @param memberId     用户的id
     * @param fiatMoneyPwd 资金密码‘
     * @return
     */
    public Result updateFiatMoneyPwd(Integer memberId, String fiatMoneyPwd) {
        Result result = new Result();
        try {
            Example example = new Example(YangMember.class);
            Example.Criteria criteria = example.createCriteria();
            criteria.andEqualTo("memberId", memberId);
            List<YangMember> yangMemberList = memberMapper.selectByExample(example);
            YangMember yangMember = yangMemberList.get(0);
            if (yangMember == null) {
                result.setCode(0);
                result.setMsg("获取失败");
            }else {
                yangMember.setFiatMoneyPwd(DigestUtils.md5Hex(fiatMoneyPwd));
                int count = memberMapper.setsFiatMoneyPwd(yangMember);
                if (count == 1) {
                    result.setCode(1);
                    result.setMsg("设置成功");
                } else {
                    result.setCode(0);
                    result.setMsg("设置失败");
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
            result.setCode(Result.Code.ERROR);
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
            result.setMsg(ErrorCode.ERROR_SYSTEM.getMessage());
        }
        return  result;
    }

    /**
     * 根据email获取用户资金密码
     *
     * @param loginUserName
     * @return
     */
    public Map<String, String> getFiatMoneyPwdByEmail(String loginUserName) {
        Map<String, String> map = memberMapper.getFiatMoneyPwdByEmail(loginUserName);
        return map;
    }

    /**
     * 根据phone获取用户资金密码
     *
     * @param loginUserName
     * @return
     */
    public Map<String, String> getFiatMoneyPwdByPhone(String loginUserName) {
        Map<String, String> map = memberMapper.getFiatMoneyPwdByPhone(loginUserName);
        return map;
    }

    /**
     * 根据邮箱重置资金密码
     * yxt
     * 2019/9/16
     *
     * @param yangMember
     * @return
     */
    public Result resetFiatMoneyPwdByEmail(YangMember yangMember) {
        Result result = new Result();
        int i = memberMapper.resetFiatMoneyPwdByEmail(yangMember);
        if (i == 1) {
            result.setCode(1);
            result.setMsg("重置成功");
        } else {
            result.setCode(0);
            result.setMsg("重置失败");
        }
        return result;
    }

    /**
     * 根据手机重置资金密码
     * yxt
     * 2019/9/16
     *
     * @param yangMember
     * @return
     */
    public Result resetFiatMoneyPwdByPhone(YangMember yangMember) {
        Result result = new Result();
        int i = memberMapper.resetFiatMoneyPwdByPhone(yangMember);
        if (i == 1) {
            result.setCode(1);
            result.setMsg("重置成功");
        } else {
            result.setCode(0);
            result.setMsg("重置失败");
        }
        return result;
    }

    public int orderConfirm(
            int memberId, String cyId, BigDecimal num, String numOptions, BigDecimal forzen, String forzenOptions,
            int memberId1, String cyId1, BigDecimal num1, String numOptions1, BigDecimal forzen1, String forzenOptions1,
            int memberId2, String cyId2, BigDecimal num2, String numOptions2, BigDecimal forzen2, String forzenOptions2,
            int memberId3, String cyId3, BigDecimal num3, String numOptions3, BigDecimal forzen3, String forzenOptions3,
            int memberId4, String cyId4, BigDecimal num4, String numOptions4, BigDecimal forzen4, String forzenOptions4,

            int orderId1, BigDecimal tradeNum1, long tradeTime1,
            int orderId2, BigDecimal tradeNum2, long tradeTime2,

            String tradeNo1, int t_memberId1, int t_currencyId1, int currencyTradeId1, BigDecimal price1, BigDecimal t_num1, BigDecimal t_money1, BigDecimal fee1, String t_type1, long t_addTime1, int t_status1, int show1, int t_orders_id1,

            String tradeNo2, int t_memberId2, int t_currencyId2, int currencyTradeId2, BigDecimal price2, BigDecimal t_num2, BigDecimal t_money2, BigDecimal fee2, String t_type2, long t_addTime2, int t_status2, int show2, int t_orders_id2,

            int f_memberId1, int f_type1, int f_moneyType1, BigDecimal f_money1, long f_addTime1, int f_currencyId1, String f_ip1, String f_content1,
            int f_memberId2, int f_type2, int f_moneyType2, BigDecimal f_money2, long f_addTime2, int f_currencyId2, String f_ip2, String f_content2
    ) {

        HashMap pama = new HashMap();
        pama.put("memberId", memberId);
        pama.put("cyId", cyId);
        pama.put("num", num);
        pama.put("numOptions", numOptions);
        pama.put("forzen", forzen);
        pama.put("forzenOptions", forzenOptions);


        pama.put("memberId1", memberId1);
        pama.put("cyId1", cyId1);
        pama.put("num1", num1);
        pama.put("numOptions1", numOptions1);
        pama.put("forzen1", forzen1);
        pama.put("forzenOptions1", forzenOptions1);

        pama.put("memberId2", memberId2);
        pama.put("cyId2", cyId2);
        pama.put("num2", num2);
        pama.put("numOptions2", numOptions2);
        pama.put("forzen2", forzen2);
        pama.put("forzenOptions2", forzenOptions2);

        pama.put("memberId3", memberId3);
        pama.put("cyId3", cyId3);
        pama.put("num3", num3);
        pama.put("numOptions3", numOptions3);
        pama.put("forzen3", forzen3);
        pama.put("forzenOptions3", forzenOptions3);

        pama.put("memberId4", memberId4);
        pama.put("cyId4", cyId4);
        pama.put("num4", num4);
        pama.put("numOptions4", numOptions4);
        pama.put("forzen4", forzen4);
        pama.put("forzenOptions4", forzenOptions4);


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


        return memberMapper.orderConfirm(pama);
    }

    /**
     * 根据手机或者是邮箱找回密码
     *
     * @param accessToken      用户的id
     * @param account          手机号或者是邮箱
     * @param pwd              新密码
     * @param verificationCode 手机或者是邮箱的验证码
     * @return
     */
    public Result findFiatMoneyPwds(String accessToken, String account, String pwd, String verificationCode) {
        //验证token
        Result result = new Result();
        String token = CoinConst.TOKENKEYTOKEN + accessToken;
        //验证token
        Object memberId = redisService.get(token);
        String password = DigestUtils.md5Hex(pwd);
        try {
            //取到用户信息
            //判断是邮箱验证码还是手机验证码
            validateAccessToken(accessToken, result, yangMemberTokenService);
            if (result.getCode().intValue() == Result.Code.ERROR.intValue()) {
                return result;
            }
            YangMember yangMemberSelf = (YangMember) result.getData();
            //取到用户信息
            if (account.contains("@")) {
                String key = CoinConst.EMAILFORFINDCODE + account + "_" + CoinConst.EMAIL_CODE_TYPE_ZC;
                if (redisService.exists(key)) {
                    EmailLog emailLog = JSONObject.parseObject(redisService.get(key).toString(), EmailLog.class);
                    //邮箱验证通过
                    if (verificationCode.equals(emailLog.getContent())) {
                        memberMapper.findFiatMoneyPwd((Integer) memberId, password);
                        result.setCode(Result.Code.SUCCESS);
                        result.setMsg("修改资金密码成功");
                        return result;
                    } else {
                        result.setCode(Result.Code.ERROR);
                        result.setMsg("验证码错误,请重新请求");
                        result.setErrorCode(ErrorCode.ERROR_ERROR_CODE.getIndex());
                    }
                } else {
                    result.setCode(Result.Code.ERROR);
                    result.setMsg("验证码不存在或已过期");
                    result.setErrorCode(ErrorCode.ERROR_ERROR_CODE.getIndex());
                }
            } else {
                String key = CoinConst.EMAILFORFINDCODE + account + "_" + "1";
                if (redisService.exists(key)) {
                    String numStr = (String) redisService.get(key);
                    //手机验证码验证通过
                    if (verificationCode != null && verificationCode.equals(numStr)) {
                        memberMapper.findFiatMoneyPwd((Integer) memberId, password);
                        result.setCode(Result.Code.SUCCESS);
                        result.setMsg("修改资金密码成功");
                        return result;
                    } else {
                        result.setCode(Result.Code.ERROR);
                        result.setMsg("验证码错误,请重新请求");
                        result.setErrorCode(ErrorCode.ERROR_ERROR_CODE.getIndex());
                    }
                } else {
                    result.setCode(Result.Code.ERROR);
                    result.setMsg("验证码不存在或已过期");
                    result.setErrorCode(ErrorCode.ERROR_ERROR_CODE.getIndex());
                }
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
     * 重置密码
     *
     * @param account          手机号或者是邮箱
     * @param pwd              新密码
     * @param verificationCode 手机号或者是邮箱得验证码
     * @return
     */
    public Result findMemberPwd(String account, String pwd, String verificationCode) {
        Result result = new Result();
        String password = DigestUtils.md5Hex(pwd);
        try {
            if (account.contains("@")) {
                String key = CoinConst.EMAILFORFINDCODE + account + "_" + CoinConst.EMAIL_CODE_TYPE_ZC;
                if (redisService.exists(key)) {
                    EmailLog emailLog = JSONObject.parseObject(redisService.get(key).toString(), EmailLog.class);
                    //邮箱验证通过
                    if (verificationCode.equals(emailLog.getContent())) {
                        memberMapper.findMemberPwdByEmail(account, password);
                        result.setCode(Result.Code.SUCCESS);
                        result.setMsg("重置密码成功");
                        return result;
                    } else {
                        result.setCode(Result.Code.ERROR);
                        result.setMsg("验证码错误,请重新请求");
                        result.setErrorCode(ErrorCode.ERROR_ERROR_CODE.getIndex());
                    }
                } else {
                    result.setCode(Result.Code.ERROR);
                    result.setMsg("验证码不存在或已过期");
                    result.setErrorCode(ErrorCode.ERROR_ERROR_CODE.getIndex());
                }
            } else {
                String key = CoinConst.EMAILFORFINDCODE + account + "_" + "1";
                if (redisService.exists(key)) {
                    String numStr = (String) redisService.get(key);
                    //手机验证码验证通过
                    if (verificationCode != null && verificationCode.equals(numStr)) {
                        memberMapper.findMemberPwdByPhone(account, password);
                        result.setCode(Result.Code.SUCCESS);
                        result.setMsg("重置密码成功");
                        return result;
                    } else {
                        result.setCode(Result.Code.ERROR);
                        result.setMsg("验证码错误,请重新请求");
                        result.setErrorCode(ErrorCode.ERROR_ERROR_CODE.getIndex());
                    }
                } else {
                    result.setCode(Result.Code.ERROR);
                    result.setMsg("验证码不存在或已过期");
                    result.setErrorCode(ErrorCode.ERROR_ERROR_CODE.getIndex());
                }
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
     * 重置密码
     *
     * @param account          手机号或者是邮箱
     * @param pwd              新密码
     * @param verificationCode 手机号或者是邮箱得验证码
     * @return
     */
    public Result resetPwds(String account, String pwd, String verificationCode) {
        Result result = new Result();
        String password = DigestUtils.md5Hex(pwd);
        try {
            if (account.contains("@")) {
                String key = CoinConst.EMAILFORFINDCODE + account + "_" + CoinConst.EMAIL_CODE_TYPE_ZC;
                if (redisService.exists(key)) {
                    EmailLog emailLog = JSONObject.parseObject(redisService.get(key).toString(), EmailLog.class);
                    //邮箱验证通过
                    if (verificationCode.equals(emailLog.getContent())) {
                        memberMapper.resetPasswordByEmail(account, verificationCode);
                        result.setCode(Result.Code.SUCCESS);
                        result.setMsg("修改密码成功");
                        return result;
                    } else {
                        result.setCode(Result.Code.ERROR);
                        result.setMsg("验证码错误,请重新请求");
                        result.setErrorCode(ErrorCode.ERROR_ERROR_CODE.getIndex());
                    }
                } else {
                    result.setCode(Result.Code.ERROR);
                    result.setMsg("验证码不存在或已过期");
                    result.setErrorCode(ErrorCode.ERROR_ERROR_CODE.getIndex());
                }
            } else {
                String key = CoinConst.EMAILFORFINDCODE + account + "_" + "1";
                if (redisService.exists(key)) {
                    String numStr = (String) redisService.get(key);
                    //手机验证码验证通过
                    if (verificationCode != null && verificationCode.equals(numStr)) {
                        memberMapper.resetPasswordByPhone(account, verificationCode);
                        result.setCode(Result.Code.SUCCESS);
                        result.setMsg("修改密码成功");
                        return result;
                    } else {
                        result.setCode(Result.Code.ERROR);
                        result.setMsg("验证码错误,请重新请求");
                        result.setErrorCode(ErrorCode.ERROR_ERROR_CODE.getIndex());
                    }
                } else {
                    result.setCode(Result.Code.ERROR);
                    result.setMsg("验证码不存在或已过期");
                    result.setErrorCode(ErrorCode.ERROR_ERROR_CODE.getIndex());
                }
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
     * 提供过邮箱获取memberId
     *
     * @param account
     * @return
     */
    public YangMember getMemberIdByEmail(String account) {
        Result result = new Result();
        YangMember yangMember = new YangMember();
        try{
            yangMember = memberMapper.getMemberIdByEmail(account);
            return yangMember;
        }catch (Exception e) {
            e.printStackTrace();
            result.setCode(Result.Code.ERROR);
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
            result.setMsg(ErrorCode.ERROR_SYSTEM.getMessage());
        }
        return yangMember;
    }

    /**
     * 通过account获取memberId
     *
     * @param account
     * @return
     */
    public YangMember getMemberIdByPhone(String account) {
        Result result = new Result();
        YangMember yangMember = new YangMember();
        try{
            yangMember = memberMapper.getMemberIdByPhone(account);
            return yangMember;
        }catch (Exception e) {
            e.printStackTrace();
            result.setCode(Result.Code.ERROR);
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
            result.setMsg(ErrorCode.ERROR_SYSTEM.getMessage());
        }
        return yangMember;
    }

    /**
     * 检测用户是否注册过或者被绑定过
     * @param map
     * @return
     */
    public Result checkAccountIsOnly(Map<String, String> map) {
        Result result = new Result();
        String account = map.get("account");
        try{
            Map<String, String> memberByEmail = memberMapper.getMemberByEmail(account);
            Map<String, String> memberByPhone = memberMapper.getMemberByPhone(account);
            Map<String,Boolean> dataMap = new HashMap<>();
            if (memberByEmail == null || memberByPhone == null){
                dataMap.put("isOnly",false);
                result.setData(dataMap);
                result.setMsg("不存在");
                result.setCode(1);
            }else{
                dataMap.put("isOnly",true);
                result.setData(dataMap);
                result.setMsg("存在");
                result.setCode(1);
            }
        }catch (Exception e){
            result.setCode(Result.Code.ERROR);
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
            result.setMsg(ErrorCode.ERROR_SYSTEM.getMessage());
        }
        return result;
    }
}
