package net.cyweb.config.bigcustom;

import com.alibaba.fastjson.JSONObject;
import cyweb.utils.CoinConst;
import net.cyweb.config.custom.RemoteAccessUtils;
import net.cyweb.config.mes.MesConfig;
import net.cyweb.mapper.ProgramBackMapper;
import net.cyweb.model.Result;
import net.cyweb.service.ProgramBackService;
import net.cyweb.service.RedisService;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.fluent.Request;
import org.omg.Messaging.SYNC_WITH_TRANSPORT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

@Component
@Configuration      //1.主要用于标记配置类，兼备Component的效果。
@EnableScheduling   // 2.开启定时任务
@PropertySource(value = "classpath:conf/config.properties")
public class ScheduledTask {
    @Autowired
    private RedisService redisService;

    @Value("${note.cnyBTCUrl}")
    private String cnyBTCUrl;

    @Value("${note.cnyUSDTUrl}")
    private String cnyUSDTUrl;

    @Value("${note.cnyETHUrl}")
    private String cnyETHUrl;

    @Autowired
    private ProgramBackService programBackService;

    @Autowired
    private ProgramBackMapper programBackMapper;

    private static final Logger log = LoggerFactory.getLogger(ScheduledTask.class);


    /**
     * 查询均价，往数据库中插入数据给datav使用的，数据库为funcoin，表为big_ave_price
     *
     * @param
     * @param
     * @return result.getData()
     */
    //3.添加定时任务
    //或直接指定时间间隔，例如：5分钟 异步执行
    @Scheduled(cron = "0 0/5 * * * ?")
    @Async
    public void getAvePrice() {
        TreeMap<String, String> param = new TreeMap<String, String>();
        List<Map<String, String>> datas = programBackService.getAvePrice(param);
        Map<String, List<Map<String, String>>> data = programBackService.aveToDataBase(datas);
        try {
            programBackMapper.insertBigAvePriceList(data.get("buy"));
            programBackMapper.insertBigAvePriceList(data.get("sell"));
        } catch (Exception e) {
            e.printStackTrace();
            log.error("异常{}", e);
        }
    }

    /**
     * 插用户表 资产表 流水表 充币地址表
     *
     * @param
     * @param
     * @return result.getData()
     */
    //3.添加定时任务
    //或直接指定时间间隔，例如：5分钟 异步执行
    @Scheduled(cron = "0 0/5 * * * ?")
    @Async
    public void getUserInfo() {
        TreeMap<String, String> param = new TreeMap<>();
        //1先去本地数据库看有没有id，没有就去，有拿最后一条
        Integer userLastId = programBackMapper.getYangMemberLast();
        if (userLastId != null) {
            param.put("from_id", userLastId.toString());
        }
        //查用户
        String response = RemoteAccessUtils.postOrg(param, "/user/query_user_list");
        Map<String, Object> userDatas = (Map<String, Object>) JSONObject.parse(response);
        //查询实名用户列表
        TreeMap<String, String> name = new TreeMap<>();
        //1先去本地数据库看有没有id，没有就去，有拿最后一条
        Integer nameLastId = programBackMapper.getYangMemberLast();
        if (userLastId != null) {
            name.put("from_id", nameLastId.toString());
        }
        String responseName = RemoteAccessUtils.postOrg(param, "/query_user_kyc_list");
        Map<String, Object> userNameDatas = (Map<String, Object>) JSONObject.parse(responseName);
        if (userNameDatas.get("data") != null) {
            //用户列表
            List<Map<String, Object>> userNameList = (List<Map<String, Object>>) userNameDatas.get("data");

            try {
                //插入用户列表
                programBackMapper.insertYangMemberList(userList);
                //插入实名用户列表
            } catch (Exception e) {
                e.printStackTrace();
                log.error("异常{}", e);
            }
        } else {
            //没有用户信息
        }

        //邀请信息
        for () {
            String response = RemoteAccessUtils.postOrg(param, "/user/get_invite_info");
        }

        //3插用户，更新用户
        //4拿出上面的user_id 去查询账户信息 循环查 map 很多去插
        ////拿出user_id token_id 同时去查信息，然后插入 map 很多去插
        //5拿出user_id 查询流水信息 循环查 map去插
        //user_id 查历史成交


        try {
//            programBackMapper.insertList();
//            programBackMapper.insertList();
        } catch (Exception e) {
            e.printStackTrace();
            log.error("异常{}", e);
        }
    }


}
