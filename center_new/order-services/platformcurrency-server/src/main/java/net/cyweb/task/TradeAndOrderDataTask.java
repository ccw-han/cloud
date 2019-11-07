package net.cyweb.task;

import net.cyweb.model.YangCurrencyPair;
import net.cyweb.model.YangOrders;
import net.cyweb.model.YangTrade;
import net.cyweb.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 处理 trade 和order数据到 robot 表
 */
@Configuration
@EnableScheduling
@EnableAsync
@Service
public class TradeAndOrderDataTask {

    @Autowired
    private YangCurrencyPairService yangCurrencyPairService;

    @Autowired
    private YangTradeService yangTradeService;

    @Autowired
    private YangTradeRobotService yangTradeRobotService;

    @Autowired
    private YangOrderService yangOrderService;

    @Autowired
    private YangOrderRobotService yangOrderRobotService;

    //每10秒跑一次  处理 机器人交易记录
//    @Scheduled(cron = "0/5 * * * * ?")
//    @Transactional
//    public void dealTradeData() {
//        //查询所有的机器人ID
//        List<YangCurrencyPair> robotList= yangCurrencyPairService.getRoboList();
//        if(robotList.size()>0){
//            int maxTradeId=yangTradeService.findLastRecordData();
//            //暂定逻辑  1 后台 tradeId 为空时 不做处理 2 后台 mongodb tradeId 数据未同步时 不做 处理
//            if(maxTradeId==0){
//                return;
//            }
//            Map<String,Object> pamas=new HashMap<String,Object>();
//            pamas.put("list",robotList);
//            pamas.put("tradeId",maxTradeId);
//            //从trade表里查询机器人数据
//            List<YangTrade> tradeList=yangTradeService.findRobotList(pamas);
//            if(tradeList.size()>0){
//            //插入 yang_trade_robot
//            int i=yangTradeRobotService.saveBatch(tradeList);
//            //批量删除trade表数据
//            yangTradeService.deleteBatch(tradeList);
//            }
//        }
//    }
//
//    //每天凌晨4点 清理一次 订单表 数据
    @Scheduled(cron = "0 0 4 * * ?")
    @Transactional
    public void dealOrderData() {

        //查询所有的机器人ID
//        List<YangCurrencyPair> robotList= yangCurrencyPairService.getRoboList();


//        if(robotList.size()>0){
//            int maxOrderId=yangTradeService.findLastOrderRecordData();
//            //暂定逻辑  1 后台 orderId 为空时 不做处理 2 后台 mongodb  orderId 数据未同步时 不做 处理
//            if(maxOrderId==0){
//                return;
//            }
//            Map<String,Object> pamas=new HashMap<String,Object>();
//            pamas.put("list",robotList);
//            pamas.put("ordersId",maxOrderId);
//            //从order表里查询机器人数据
//            List<YangOrders> ordersList=yangOrderService.findRobotList(pamas);
//            if(ordersList.size()>0){
//                //插入 yang_trade_robot
//                int i=yangOrderRobotService.saveBatch(ordersList);
//                //批量删除order表数据
//                yangOrderService.deleteBatch(ordersList);
//
//            }
//        }
        try{
            for(int j=1;j<2000;j++){
                List<YangOrders> list=yangOrderService.findFinishOrderList();
                if(list!=null&&list.size()>0){
                    int i=yangOrderRobotService.saveBatch(list);
                    yangOrderService.deleteBatch(list);
                    System.out.println("订单数据转移开始Id"+list.get(0).getOrdersId()+"-------------------结束Id "+list.get(list.size()-1).getOrdersId());
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

}
