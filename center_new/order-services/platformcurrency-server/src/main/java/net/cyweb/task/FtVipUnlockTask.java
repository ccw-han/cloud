package net.cyweb.task;

import cyweb.utils.CoinConst;
import cyweb.utils.DateUtils;
import net.cyweb.model.YangCardNum;
import net.cyweb.model.YangCurrencyUser;
import net.cyweb.model.YangFinance;
import net.cyweb.service.YangCardNumService;
import net.cyweb.service.YangFinanceService;
import net.cyweb.service.YangMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 会员卡解锁 定时任务
 */
@Configuration
@EnableScheduling
@EnableAsync
@Service
public class FtVipUnlockTask {

    @Autowired
    private YangCardNumService yangCardNumService;

    @Autowired
    private YangMemberService yangMemberService;

    @Autowired
    private YangFinanceService yangFinanceService;
    //会员卡解锁
    //每天早上10点解锁
//    @Scheduled(cron = "0 0 10 * * ?")
    @Transactional
    public void vipUnlock()
    {
        try{
            System.out.println("-----------会员卡解锁-------------"+DateUtils.getNoDateDay(new Date(),"yyyy-MM-dd HH:mm:ss"));
            //批量资产更新
            List<YangCurrencyUser> insertCurrencyUserList=new ArrayList<YangCurrencyUser>();
            //批量日志新增
            List<YangFinance> insertYangFinanceList=new ArrayList<YangFinance>();
            List<YangCardNum> list=yangCardNumService.getAllCardNumInfos();
            for(YangCardNum yangCardNum:list){
                //解锁次数 超过90次  不处理
                if(yangCardNum.getDealTimes()>=90){
                    continue;
                }
                BigDecimal jieNum=yangCardNum.getNum().multiply(new BigDecimal("0.01"));
                yangCardNum.setDealNum(yangCardNum.getDealNum().add(jieNum));
                yangCardNum.setDealTimes(yangCardNum.getDealTimes()+1);
                yangCardNumService.updateYangCardNum(yangCardNum);
                //同步修改用户资产
                YangCurrencyUser yangCurrencyUser=new YangCurrencyUser();
                yangCurrencyUser.setCurrencyId(CoinConst.FUNT_CURRENCY_ID);
                yangCurrencyUser.setMemberId(yangCardNum.getMemberId());
                yangCurrencyUser.setNum(jieNum);
                insertCurrencyUserList.add(yangCurrencyUser);

                //添加 日志
                YangFinance yangFinance=new YangFinance();
                yangFinance.setAddTime(DateUtils.getNowTimesLong());
                yangFinance.setContent(CoinConst.FINANCE_REMARK_VIP_UNLOCK+"|"+CoinConst.SYS_ADMIN_ID);
                yangFinance.setCurrencyId(CoinConst.FUNT_CURRENCY_ID);
                yangFinance.setIp("127.0.0.1");
                yangFinance.setMemberId(yangCardNum.getMemberId());
                yangFinance.setMoney(jieNum);
                yangFinance.setType(CoinConst.FINANCE_RECORD_ADD_BY_VIP_UNLOCK);  //会员卡解锁
                yangFinance.setMoneyType(CoinConst.FINANCE_RECORD_MONEY_SR);
                insertYangFinanceList.add(yangFinance);

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
            System.out.println("-----------会员卡解锁完成-------------"+DateUtils.getNoDateDay(new Date(),"yyyy-MM-dd HH:mm:ss"));
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("-----------会员卡解锁失败-------------"+DateUtils.getNoDateDay(new Date(),"yyyy-MM-dd HH:mm:ss"));
        }
    }
}
