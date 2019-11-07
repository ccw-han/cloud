package net.cyweb.task;

import cyweb.utils.CoinConst;
import cyweb.utils.DateUtils;
import jdk.nashorn.internal.objects.Global;
import net.cyweb.mapper.YangCurrencyPairMapper;
import net.cyweb.model.*;
import net.cyweb.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.*;

/**
 * 返佣 定时任务
 */
@Configuration
@EnableScheduling
@EnableAsync
@Service
public class PlatFormCurrencyTask {

    private final static Logger logger = LoggerFactory.getLogger(PlatFormCurrencyTask.class);


    @Autowired
    private YangTradeService yangTradeService;

    @Autowired
    private YangFtActivityService yangFtActivityService;

    @Autowired
    private YangMiningInfoService yangMiningInfoService;

    @Autowired
    private YangMemberService yangMemberService;

    @Autowired
    private YangFinanceService yangFinanceService;

    //每月1号凌晨1点执行一次：0 0 1 1 * ?
    @Scheduled(cron = "0 0 1 1 * ?")
    public  void  thaw()
    {
        logger.info("开始解锁");

    }


    /**
     * 解锁用户的资产
     */
    public void unlock()
    {

    }



    /**
     * 锁定用户的资产
     */
    public void lock()
    {

    }
    //返佣处理
    //每天10点执行一次：0 0 10 * * ?
//    @Scheduled(cron = "0 0 10 * * ?")
//    @Scheduled(cron = "0 0/1 *  * * ?")
    @Transactional
    public void dealBackRake()
    {

        logger.info("--------------------------开始计算返佣---------------");
        try{
            String type;
            String memberId;

            Map pama=new HashMap();
            String lastDay = DateUtils.getDateStrPre(-1);
            pama.put("startTime", DateUtils.getTimeStampSeconds(lastDay + " 0:0:0"));
            pama.put("endTime", DateUtils.getTimeStampSeconds(lastDay + " 23:59:59"));
            pama.put("currencyId",CoinConst.FUNT_CURRENCY_ID);
//            pama.put("currencyId",72);

            //获取前一日的 均价 总数量
            Map lastDayInfoMap=yangTradeService.getLastDayInfo(pama);
            //获取 前一日有交易记录且   ft 额度不为空的用户交易信息
            List<Map> list=yangTradeService.findLastDayTradeInfo(pama);
            //中转MAp
            Map<String,Map<String,Map<String,Object>>> userMap=new HashMap<String,Map<String,Map<String,Object>>>();
            //批量更新  ft
            List<YangFtActivity> updateFtList=new ArrayList<YangFtActivity>();
            //批量插入挖矿记录
            List<YangMiningInfo> insertMiningInfoList;
            //机器人挖矿记录
            List<YangMiningInfo> robotInsertMiningInfoList;
            //批量资产更新
            List<YangCurrencyUser> insertCurrencyUserList=new ArrayList<YangCurrencyUser>();;
            //批量日志新增
            List<YangFinance> insertYangFinanceList=new ArrayList<YangFinance>();

            //获取所有有额度的用户ID

            for(int i=0;i<list.size();i++){
                insertMiningInfoList=new ArrayList<YangMiningInfo>();
                pama.put("memberId",String.valueOf(list.get(i).get("memberId")));
                //该用户 昨日所有的交易记录
                List<Map> userList=yangTradeService.findList(pama);
                //昨日应该返佣总额度
                BigDecimal leftNum=(BigDecimal) list.get(i).get("ftnum");
                String userMemberId=String.valueOf((Integer)list.get(i).get("memberId"));
                BigDecimal num;//数量
                BigDecimal price;//单价
                BigDecimal robotNum=new BigDecimal(0);//机器人总数量
                BigDecimal avgPrice=new BigDecimal(((BigDecimal)lastDayInfoMap.get("avgPrice")).toPlainString()).setScale(10,RoundingMode.HALF_UP);
                BigDecimal financePrice=new BigDecimal(0);//资产记录
                for(int j=0;j<userList.size();j++){
                    BigDecimal minNum=new BigDecimal(0);
                    Map<String,Object> userTreadeSingleMap=userList.get(j);
                    if("sell".equals(userTreadeSingleMap.get("type"))){
                        num= new BigDecimal( (Double) userTreadeSingleMap.get("num"));
                        price=new BigDecimal( (Double) userTreadeSingleMap.get("price"));
                        BigDecimal sellNnum=
                                num.multiply(new BigDecimal(price.toPlainString())).multiply(new BigDecimal("0.001")).setScale(18,RoundingMode.HALF_UP).divide(avgPrice,18,RoundingMode.HALF_UP).
                                multiply(new BigDecimal("1.1")).setScale(18,RoundingMode.HALF_UP);
                        minNum= minNum.add(new BigDecimal(sellNnum.toPlainString()).setScale(18,RoundingMode.HALF_UP));
                    }
                    if("buy".equals(userTreadeSingleMap.get("type"))){
                        num=new BigDecimal((Double)userTreadeSingleMap.get("num")) ;
                        BigDecimal buyNum=new BigDecimal(num.multiply(new BigDecimal("0.001")).multiply(new BigDecimal("1.1")).toPlainString()).setScale(18,RoundingMode.HALF_UP);
                        minNum=minNum.add(buyNum);
                    }
                    //如果是机器人
                    if(CoinConst.FUNT_TRADE_ROOT_ID==Integer.valueOf(userMemberId).intValue()){
                        robotNum=robotNum.add(minNum);
                    }else{
                        //可用额度 小于等于0
                        if(leftNum.compareTo(new BigDecimal(0))<=0){
                            break;
                        }
                        if(leftNum.compareTo(minNum)>=0){
                            minNum=minNum.setScale(18,RoundingMode.HALF_UP);
                            leftNum=leftNum.subtract(minNum).setScale(18,RoundingMode.HALF_UP);
                        }else{
                            minNum=leftNum;
                            leftNum=new BigDecimal(0);
                        }
                         //添加挖矿记录
                        YangMiningInfo yangMiningInfo=new YangMiningInfo();
                        yangMiningInfo.setAddTime(DateUtils.getNowTimes());
                        yangMiningInfo.setAddDate(DateUtils.getNoDateDay(new Date(),"yyyy-MM-dd"));
                        yangMiningInfo.setNum(minNum);
                        yangMiningInfo.setMemberId(Integer.valueOf(userMemberId));
//                        yangMiningInfo.setTradeId((int)userTreadeSingleMap.get("tradeId"));
                        insertMiningInfoList.add(yangMiningInfo);
                    }
                    financePrice=financePrice.add(minNum);
                }
                //返佣数量大于0
                if(financePrice.compareTo(new BigDecimal(0))>0){
                    //添加 日志
                    YangFinance yangFinance=new YangFinance();
                    yangFinance.setAddTime(DateUtils.getNowTimesLong());
                    yangFinance.setContent(CoinConst.FINANCE_REMARK+"|"+CoinConst.SYS_ADMIN_ID);
                    yangFinance.setCurrencyId(CoinConst.FUNT_CURRENCY_ID);
                    yangFinance.setIp("127.0.0.1");
                    yangFinance.setMemberId(Integer.valueOf(userMemberId));
                    yangFinance.setMoney(financePrice);
                    yangFinance.setType(CoinConst.FINANCE_RECORD_ADD_BY_RAKE);  //充值
                    yangFinance.setMoneyType(CoinConst.FINANCE_RECORD_MONEY_SR);
                    insertYangFinanceList.add(yangFinance);
                    //添加资产记录
                    YangCurrencyUser yangCurrencyUser=new YangCurrencyUser();
                    yangCurrencyUser.setCurrencyId(CoinConst.FUNT_CURRENCY_ID);
                    yangCurrencyUser.setMemberId(Integer.valueOf(userMemberId));
                    yangCurrencyUser.setNum(financePrice);
                    insertCurrencyUserList.add(yangCurrencyUser);
                    //修改FT记录
                    if(CoinConst.FUNT_TRADE_ROOT_ID!=Integer.valueOf(userMemberId).intValue()){
                        YangFtActivity ft=new YangFtActivity();
                        ft.setMemberId(Integer.valueOf(userMemberId));
                        ft.setNum(leftNum);
                        updateFtList.add(ft);
                    }
                }

                if(CoinConst.FUNT_TRADE_ROOT_ID==Integer.valueOf(userMemberId).intValue()){
                    //机器人返佣
                    if(robotNum.compareTo(new BigDecimal(0))>0){
                        robotInsertMiningInfoList=new ArrayList<YangMiningInfo>();
                        YangMiningInfo yangMiningInfo=new YangMiningInfo();
                        yangMiningInfo.setAddTime(DateUtils.getNowTimes());
                        yangMiningInfo.setAddDate(DateUtils.getNoDateDay(new Date(),"yyyy-MM-dd"));
                        yangMiningInfo.setNum(robotNum);
                        yangMiningInfo.setMemberId(CoinConst.FUNT_TRADE_ROOT_ID);
                        robotInsertMiningInfoList.add(yangMiningInfo);
                        yangMiningInfoService.insertMiningInfoBatch(robotInsertMiningInfoList);
                    }
                }else{
                    //批量更新 挖矿记录
                    if(insertMiningInfoList.size()>0){
                        yangMiningInfoService.insertMiningInfoBatch(insertMiningInfoList);
                    }
                }
            }
            if(updateFtList.size()>0){
                //批量更新FT
                yangFtActivityService.updateFtLeftBatch(updateFtList);
                //批量更新 用户资产
                for(int j=0;j<insertCurrencyUserList.size();j=j+5){
                    YangCurrencyUser yg= insertCurrencyUserList.get(j);
                    YangCurrencyUser yg1=null;
                    YangCurrencyUser yg2= null;
                    YangCurrencyUser yg3= null;
                    YangCurrencyUser yg4= null;
                    if(j+1<insertCurrencyUserList.size()){
                        yg1= insertCurrencyUserList.get(j+1);
                    }
                    if(j+2<insertCurrencyUserList.size()){
                        yg2= insertCurrencyUserList.get(j+2);
                    }
                    if(j+3<insertCurrencyUserList.size()){
                        yg3= insertCurrencyUserList.get(j+3);
                    }
                    if(j+4<insertCurrencyUserList.size()){
                        yg4= insertCurrencyUserList.get(j+4);
                    }

                    yangMemberService.assets(
                            Integer.valueOf(yg.getMemberId()),String.valueOf(yg.getCurrencyId()),yg.getNum(),CoinConst.ASSETSTYPE_INC,new BigDecimal(0),null,
                            Integer.valueOf(yg1==null?0: yg1.getMemberId()), yg1==null?null:String.valueOf(yg1.getCurrencyId()),yg1==null?null: yg1.getNum(),CoinConst.ASSETSTYPE_INC,new BigDecimal(0),null,
                            Integer.valueOf(yg2==null?0: yg2.getMemberId()), yg2==null?null:String.valueOf(yg2.getCurrencyId()),yg2==null?null: yg2.getNum(),CoinConst.ASSETSTYPE_INC,new BigDecimal(0),null,
                            Integer.valueOf(yg3==null?0: yg3.getMemberId()), yg3==null?null:String.valueOf(yg3.getCurrencyId()),yg3==null?null: yg3.getNum(),CoinConst.ASSETSTYPE_INC,new BigDecimal(0),null,
                            Integer.valueOf(yg4==null?0: yg4.getMemberId()), yg4==null?null:String.valueOf(yg4.getCurrencyId()),yg4==null?null: yg4.getNum(),CoinConst.ASSETSTYPE_INC,new BigDecimal(0),null
                    );
                }
                //记录资产变更操作  当前管理员信息
                if(insertYangFinanceList.size()>0){
                    yangFinanceService.currencyUserChangeToSave(insertYangFinanceList);
                    logger.info("--------------------------资产日志记录成功---------------");
                }else{
                    logger.info("--------------------------资产日志记录失败---------------");

                }
                logger.info("--------------------------返佣成功---------------");
            }else{
                logger.info("--------------------------返佣失败  未获取到真实用户挖矿记录---------------");
            }

        }catch(Exception e){
            e.printStackTrace();
            logger.info("--------------------------返佣失败---------------");
        }
        logger.info("--------------------------返佣结算完成---------------");
    }
}
