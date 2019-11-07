package net.cyweb.config.custom;

import com.alibaba.fastjson.JSONObject;
import cyweb.utils.CoinConst;
import net.cyweb.config.mes.MesConfig;
import net.cyweb.model.Result;
import net.cyweb.service.RedisService;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.fluent.Request;
import org.omg.Messaging.SYNC_WITH_TRANSPORT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
@Configuration      //1.主要用于标记配置类，兼备Component的效果。
@EnableScheduling   // 2.开启定时任务
@PropertySource(value = "classpath:conf/config.properties")
public class CNYPriceUtils {
    @Autowired
    private RedisService redisService;

    @Value("${note.cnyBTCUrl}")
    private String cnyBTCUrl;

    @Value("${note.cnyUSDTUrl}")
    private String cnyUSDTUrl;

    @Value("${note.cnyETHUrl}")
    private String cnyETHUrl;


    /**
     * 返回CNY汇率
     *
     * @param
     * @param
     * @return result.getData()
     */
    //3.添加定时任务
    //或直接指定时间间隔，例如：5秒
    @Scheduled(cron = "0/30 * * * * ?")
    public Result cnyBTCUrl() {
        Result result = new Result();
        String price = "";
        try {
            String res = Request.Get(cnyBTCUrl).execute().returnContent().asString();
            Map<String, Object> map = (Map<String, Object>) JSONObject.parse(res);
            if (StringUtils.isNotBlank(map.get("data").toString())) {
                Map<String, Object> data = (Map<String, Object>) JSONObject.parse(map.get("data").toString());
                if (StringUtils.isNotBlank(data.get("quotes").toString())) {
                    Map<String, Object> quotes = (Map<String, Object>) JSONObject.parse(data.get("quotes").toString());
                    if (StringUtils.isNotBlank(quotes.get("CNY").toString())) {
                        Map<String, Object> cny = (Map<String, Object>) JSONObject.parse(quotes.get("CNY").toString());
                        if (StringUtils.isNotBlank(cny.get("price").toString())) {
                            price = cny.get("price").toString();
                        }
                    }
                }
            }
            String key = CoinConst.CNYPRICE + "BTC";
            if (StringUtils.isNotBlank(price)) {
                if (!redisService.exists(key)) {
                    redisService.set(key, price, 15L, TimeUnit.MINUTES);
                } else {
                    redisService.remove(key);
                    redisService.set(key, price, 15L, TimeUnit.MINUTES);
                }
                System.out.println("30 秒 执行了");
                System.out.println("redis BTC CNY:" + redisService.get(key));
                result.setCode(1);
                result.setMsg("发送成功");
                result.setData(price);
            } else {
                throw new Exception("没查到数据");
            }

        } catch (Exception e) {
            result.setCode(0);
            result.setMsg(e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    @Scheduled(cron = "0/30 * * * * ?")
    public Result cnyUSDTUrl() {
        Result result = new Result();
        String price = "";
        try {
            String res = Request.Get(cnyUSDTUrl).execute().returnContent().asString();
            Map<String, Object> map = (Map<String, Object>) JSONObject.parse(res);
            if (StringUtils.isNotBlank(map.get("data").toString())) {
                Map<String, Object> data = (Map<String, Object>) JSONObject.parse(map.get("data").toString());
                if (StringUtils.isNotBlank(data.get("quotes").toString())) {
                    Map<String, Object> quotes = (Map<String, Object>) JSONObject.parse(data.get("quotes").toString());
                    if (StringUtils.isNotBlank(quotes.get("CNY").toString())) {
                        Map<String, Object> cny = (Map<String, Object>) JSONObject.parse(quotes.get("CNY").toString());
                        if (StringUtils.isNotBlank(cny.get("price").toString())) {
                            price = cny.get("price").toString();
                        }
                    }
                }
            }
            String key = CoinConst.CNYPRICE + "USDT";
            if (StringUtils.isNotBlank(price)) {
                if (!redisService.exists(key)) {
                    redisService.set(key, price, 15L, TimeUnit.MINUTES);
                } else {
                    redisService.remove(key);
                    redisService.set(key, price, 15L, TimeUnit.MINUTES);
                }
                System.out.println("30 秒 执行了");
                System.out.println("redis USDT CNY:" + redisService.get(key));
                result.setCode(1);
                result.setMsg("发送成功");
                result.setData(price);
            } else {
                throw new Exception("没查到数据");
            }

        } catch (Exception e) {
            result.setCode(0);
            result.setMsg(e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    @Scheduled(cron = "0/30 * * * * ?")
    public Result cnyETHUrl() {
        Result result = new Result();
        String price = "";
        try {
            String res = Request.Get(cnyETHUrl).execute().returnContent().asString();
            Map<String, Object> map = (Map<String, Object>) JSONObject.parse(res);
            if (StringUtils.isNotBlank(map.get("data").toString())) {
                Map<String, Object> data = (Map<String, Object>) JSONObject.parse(map.get("data").toString());
                if (StringUtils.isNotBlank(data.get("quotes").toString())) {
                    Map<String, Object> quotes = (Map<String, Object>) JSONObject.parse(data.get("quotes").toString());
                    if (StringUtils.isNotBlank(quotes.get("CNY").toString())) {
                        Map<String, Object> cny = (Map<String, Object>) JSONObject.parse(quotes.get("CNY").toString());
                        if (StringUtils.isNotBlank(cny.get("price").toString())) {
                            price = cny.get("price").toString();
                        }
                    }
                }
            }
            String key = CoinConst.CNYPRICE + "ETH";
            if (StringUtils.isNotBlank(price)) {
                if (!redisService.exists(key)) {
                    redisService.set(key, price, 15L, TimeUnit.MINUTES);
                } else {
                    redisService.remove(key);
                    redisService.set(key, price, 15L, TimeUnit.MINUTES);
                }
                System.out.println("30 秒 执行了");
                System.out.println("redis ETH CNY:" + redisService.get(key));
                result.setCode(1);
                result.setMsg("发送成功");
                result.setData(price);
            } else {
                throw new Exception("没查到数据");
            }

        } catch (Exception e) {
            result.setCode(0);
            result.setMsg(e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

}
