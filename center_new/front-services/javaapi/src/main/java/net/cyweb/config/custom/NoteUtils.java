package net.cyweb.config.custom;

import cyweb.utils.CoinConst;
import cyweb.utils.RandomUtil;
import net.cyweb.config.mes.MesConfig;
import net.cyweb.model.Result;
import net.cyweb.service.RedisService;
import org.apache.http.client.fluent.Request;

import java.net.URLEncoder;
import java.util.concurrent.TimeUnit;

/**
 * 短信工具类
 * 创建人：椰椰
 * Time：2019/9/9
 */
public class NoteUtils {
    /**
     * 发送短信验证码
     *
     * @param phone
     * @param config
     * @return
     */
    public static Result mesSend(String phone, MesConfig config, RedisService redisService) {
        Result result = new Result();
        try {
            String numStr = RandomUtil.generateNumStr(6);
            String mes = config.getRegisterCode() + numStr;
            StringBuilder sb = new StringBuilder();
            sb.append(config.getHttpUrl()).append(config.getUrlLast() + "?")
                    .append("un=").append(config.getUserName())
                    .append("&pw=").append(URLEncoder.encode(config.getPassWord(), config.getUrlEncodeConfig()))
                    .append("&da=").append(phone)
                    .append("&sa=").append(config.getExternalCoding())
                    .append("&dc=").append(config.getDataCoding())
                    .append("&tf=").append(config.getTransferEncoding())
                    .append("&rf=").append(config.getResponseFormat())
                    .append("&sm=").append(URLEncoder.encode(mes, config.getUrlEncodeConfig()));
            String req = sb.toString();
            String res = Request.Get(req).execute().returnContent().asString();
            String key = CoinConst.EMAILFORFINDCODE + phone + "_" + "1";
            if (!redisService.exists(key)) {
                redisService.set(key, numStr, 15L, TimeUnit.MINUTES);
            } else {
                redisService.remove(key);
                redisService.set(key, numStr, 15L, TimeUnit.MINUTES);
            }
            System.out.println("response: " + res);
            System.out.println(numStr);
            result.setCode(1);
            result.setMsg("发送成功");
            result.setData(res);
        } catch (Exception e) {
            result.setCode(0);
            result.setMsg("发送失败");
            e.printStackTrace();
        }

        return result;
    }
}
