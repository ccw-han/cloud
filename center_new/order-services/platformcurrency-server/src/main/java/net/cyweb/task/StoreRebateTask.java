package net.cyweb.task;

import cyweb.utils.CoinConst;
import cyweb.utils.DateUtils;
import net.cyweb.model.YangCurrencyUser;
import net.cyweb.model.YangFinance;
import net.cyweb.model.YangFtLock;
import net.cyweb.model.YangFtLockRebateRecode;
import net.cyweb.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;


/**
 * 锁仓分红
 */
@Configuration
@EnableScheduling
@EnableAsync
@Service
public class StoreRebateTask {

    @Autowired
    private YangFtLockService yangFtLockService;

    @Autowired
    private YangTradeService yangTradeService;

    @Autowired
    private YangMemberService yangMemberService;

    @Autowired
    private YangFinanceService yangFinanceService;

    @Autowired
    private YangFtLockRebateRecodeService yangFtLockRebateRecodeService;

    //每天6点执行一次
//    @Scheduled(cron = "0 30 18 * * ?")
    @Transactional
    public void unlockTask()
    {
        try{
            System.out.println("-----------开始计算锁仓分红-------------");
            List<YangFtLockRebateRecode> rebateList=new ArrayList<YangFtLockRebateRecode>();
            //批量日志新增
            List<YangFinance> insertYangFinanceList=new ArrayList<YangFinance>();
            //批量资产更新
            List<YangCurrencyUser> insertCurrencyUserList=new ArrayList<YangCurrencyUser>();


            //查询所有的冻结数量
            BigDecimal totalLockNum= yangFtLockService.getTotalFrozenNum();
            //计算昨日交易手续费
            Map pama=new HashMap();
            String lastDay = DateUtils.getDateStrPre(-1);
//            pama.put("startTime", 1535015952);
            pama.put("startTime", DateUtils.getTimeStampSeconds(lastDay + " 0:0:0"));
//            pama.put("endTime", 1535016126);
            pama.put("endTime", DateUtils.getTimeStampSeconds(lastDay + " 23:59:59"));
            pama.put("currencyId",CoinConst.FUNT_CURRENCY_ID);
//            pama.put("currencyId",72);

            Map sellMap=yangTradeService.getLastDaySellFeeTotal(pama);
            Map buyMap=yangTradeService.getLastDayBuyFeeTotal(pama);

            //用户 frozen_num 大于0 的用户
            List<YangFtLock> userList=yangFtLockService.getFrozenNumGtZeroUserList();
            //总手续费
            BigDecimal totalFee=((BigDecimal)sellMap.get("sum")).divide((BigDecimal)sellMap.get("avg"),18,RoundingMode.HALF_UP).add((BigDecimal)buyMap.get("sum"));
            totalFee=totalFee.multiply(new BigDecimal("0.5"));
            BigDecimal robotTotalNum=new BigDecimal(0);
            for(YangFtLock yangFtLock:userList){
                BigDecimal rate=yangFtLock.getForzenNum().divide(totalLockNum,18,RoundingMode.HALF_UP);
                BigDecimal userNum=totalFee.multiply(rate);

                //如果是58  统计个总数
                if(yangFtLock.getMemberId().intValue()==CoinConst.FUNT_TRADE_ROOT_ID){
                    userNum=userNum.multiply(new BigDecimal("0.5"));
                }else{
                    if(userNum.compareTo(new BigDecimal(200))>0){
                        userNum= userNum.multiply(new BigDecimal("0.5"));
                        if(userNum.compareTo(new BigDecimal(200))>0){
                            userNum= userNum.multiply(new BigDecimal("0.4"));
                            if(userNum.compareTo(new BigDecimal(200))>0){
                                userNum= userNum.multiply(new BigDecimal("0.4"));
                                if(userNum.compareTo(new BigDecimal(200))>0){
                                    userNum= userNum.multiply(new BigDecimal("0.2"));
                                }
                            }
                        }
                    }
                }
                //给用户增加
                YangFtLockRebateRecode yangFtLockRebateRecode=new YangFtLockRebateRecode();
                yangFtLockRebateRecode.setAdddate(DateUtils.getNoDateDay(new Date(),"yyyy-MM-dd"));
                yangFtLockRebateRecode.setAddtime(DateUtils.getNowTimes());
                yangFtLockRebateRecode.setMemberid(yangFtLock.getMemberId());
                yangFtLockRebateRecode.setNum(userNum);
                rebateList.add(yangFtLockRebateRecode);

                //添加 日志
                YangFinance yangFinance=new YangFinance();
                yangFinance.setAddTime(DateUtils.getNowTimesLong());
                yangFinance.setContent(CoinConst.FINANCE_REMARK_LOCK_REBAT+"|"+CoinConst.SYS_ADMIN_ID);
                yangFinance.setCurrencyId(CoinConst.FUNT_CURRENCY_ID);
                yangFinance.setIp("127.0.0.1");
                yangFinance.setMemberId(yangFtLock.getMemberId());
                yangFinance.setMoney(userNum);
                yangFinance.setType(CoinConst.FINANCE_RECORD_ADD_BY_LOCK_RAKE);  //锁仓分红
                yangFinance.setMoneyType(CoinConst.FINANCE_RECORD_MONEY_SR);
                insertYangFinanceList.add(yangFinance);

                //同步修改用户资产
                YangCurrencyUser yangCurrencyUser=new YangCurrencyUser();
                yangCurrencyUser.setCurrencyId(CoinConst.FUNT_CURRENCY_ID);
                yangCurrencyUser.setMemberId(yangFtLock.getMemberId());
                yangCurrencyUser.setNum(userNum);
                insertCurrencyUserList.add(yangCurrencyUser);

            }
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
            if(insertYangFinanceList.size()>0) {
                yangFinanceService.currencyUserChangeToSave(insertYangFinanceList);
            }
            if(rebateList.size()>0){
                yangFtLockRebateRecodeService.insertRebatRecords(rebateList);
            }
            System.out.println("-----------锁仓分红计算完成-------------");

        }catch (Exception e){
            e.printStackTrace();
            System.out.println("-----------锁仓分红计算失败-------------");
        }
    }
}
