package net.cyweb.task;

import cyweb.utils.CoinConst;
import cyweb.utils.DateUtils;
import net.cyweb.model.YangCurrencyUser;
import net.cyweb.model.YangFinance;
import net.cyweb.model.YangFtLock;
import net.cyweb.model.YangFtLockRecord;
import net.cyweb.service.YangFinanceService;
import net.cyweb.service.YangFtLockRecordService;
import net.cyweb.service.YangFtLockService;
import net.cyweb.service.YangMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

/**
 * 用户解锁 定时任务
 */
@Configuration
@EnableScheduling
@EnableAsync
@Service
public class StoreUnlockTask {

    @Autowired
    private YangFtLockRecordService yangFtLockRecordService;

    @Autowired
    private YangFtLockService yangFtLockService;

    @Autowired
    private YangMemberService yangMemberService;

    @Autowired
    private YangFinanceService yangFinanceService;
    //解锁
    //每10分钟执行一次
//    @Scheduled(cron = "0 0/10 * * * ?")
    @Transactional
    public void unlockTask()
    {
        try{
            System.out.println("---------------锁仓解锁开始--------------------"+DateUtils.getNoDateDay(new Date(),"yyyy-MM-dd hh:mm:ss"));
            //批量日志新增
            List<YangFinance> insertYangFinanceList=new ArrayList<YangFinance>();
            //批量资产更新
            List<YangCurrencyUser> insertCurrencyUserList=new ArrayList<YangCurrencyUser>();
            List<YangFtLockRecord> list= yangFtLockRecordService.findLastDayLockInfo();
            List<YangFtLockRecord> updateFtLockRecordList=new ArrayList<YangFtLockRecord>();
            //
            for(YangFtLockRecord yangFtLockRecords:list){
                Map map=new HashMap();
                map.put("memberId",yangFtLockRecords.getMemberId());
                YangFtLock yangFtLock=yangFtLockService.getUserLockInfo(map);
                if(yangFtLock==null){
                    System.out.println("用户Id"+yangFtLockRecords.getMemberId()+"<><>未找到 余额数据");
                    continue;
                }
                //如果用户余额大于 解锁余额
                if(yangFtLock.getNum().compareTo(yangFtLockRecords.getNum())>=0){
                    yangFtLock.setNum(yangFtLock.getNum().subtract(yangFtLockRecords.getNum()));
                    yangFtLockService.updateYangFtLock(yangFtLock);

                    //同步修改用户资产
                    YangCurrencyUser yangCurrencyUser=new YangCurrencyUser();
                    yangCurrencyUser.setCurrencyId(CoinConst.FUNT_CURRENCY_ID);
                    yangCurrencyUser.setMemberId(yangFtLock.getMemberId());
                    yangCurrencyUser.setNum(yangFtLockRecords.getNum());
                    insertCurrencyUserList.add(yangCurrencyUser);

                    //添加 日志
                    YangFinance yangFinance=new YangFinance();
                    yangFinance.setAddTime(DateUtils.getNowTimesLong());
                    yangFinance.setContent(CoinConst.FINANCE_REMARK_LOCK_FT+"|"+CoinConst.SYS_ADMIN_ID);
                    yangFinance.setCurrencyId(CoinConst.FUNT_CURRENCY_ID);
                    yangFinance.setIp("127.0.0.1");
                    yangFinance.setMemberId(yangFtLock.getMemberId());
                    yangFinance.setMoney(yangFtLockRecords.getNum());
                    yangFinance.setType(CoinConst.FINANCE_RECORD_ADD_BY_FT_LOCK);  //锁仓解锁
                    yangFinance.setMoneyType(CoinConst.FINANCE_RECORD_MONEY_SR);
                    insertYangFinanceList.add(yangFinance);

                    yangFtLockRecords.setStatus(CoinConst.FT_LOCK_STATUS_FT_LOCK_UNLOCK_SUCEESS);
                    updateFtLockRecordList.add(yangFtLockRecords);
                }
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
            if(updateFtLockRecordList.size()>0){
                yangFtLockRecordService.updateFtLockRecord(updateFtLockRecordList);
            }
            System.out.println("---------------锁仓解锁完成--------------------"+DateUtils.getNoDateDay(new Date(),"yyyy-MM-dd HH:mm:ss"));
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("---------------锁仓解锁失败--------------------"+DateUtils.getNoDateDay(new Date(),"yyyy-MM-dd HH:mm:ss"));
        }
    }

}
