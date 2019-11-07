package net.cyweb.service;

import cyweb.utils.CoinConst;
import cyweb.utils.ErrorCode;
import net.cyweb.config.custom.Base64Utils;
import net.cyweb.mapper.YangCurrencyMapper;
import net.cyweb.model.Result;
import net.cyweb.model.UserProPs;
import net.cyweb.model.YangCurrency;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.util.StringUtil;

import javax.xml.ws.Action;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class YangCurrencyService extends BaseService<YangCurrency> {
    @Autowired
    private YangCurrencyMapper yangCurrencyMapper;

    @Autowired
    private UserProPs userProPs;

    /**
     * yxt
     * 2019/9/18
     * <p>
     * 获取币种的基本信息
     *
     * @param currencyId 币种的id
     * @return 币种的信息
     */
    public Result getCurrencyInfoById(Integer currencyId) {
        Result result = new Result();
        try {
            YangCurrency yangCurrency = yangCurrencyMapper.getCurrencyInfoById(currencyId);
            if (yangCurrency != null) {
                result.setCode(Result.Code.SUCCESS);
                result.setData(yangCurrency);
                result.setMsg("查询成功");

            } else {
                result.setCode(Result.Code.ERROR);
                result.setMsg("查询失败");
                result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.setCode(Result.Code.ERROR);
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
            result.setMsg(ErrorCode.ERROR_SYSTEM.getMessage());
        }
        return result;
    }

    public Result getCurrency(String currencyId, String searchText, String currencyMark, String market) {
        Result result = new Result();
        try {
            Map pama = new HashMap();
            if (StringUtils.isNotEmpty(currencyId)) {
                pama.put("currencyId", currencyId);
            }
            if (StringUtils.isNotEmpty(searchText)) {
                pama.put("searchText", searchText);
            }
            if (StringUtils.isNotEmpty(currencyMark)) {
                pama.put("currencyMark", currencyMark);
            }
            if (StringUtils.isNotEmpty(market)) {
                pama.put("market", market);
            }
            pama.put("isLine", CoinConst.CURRENCY_IS_LINE_YES);
            List<Map> list = yangCurrencyMapper.getCurrency(pama);
            String rootPath = userProPs.getFtp().get("rootPath");
            String oldReadHost = userProPs.getFtp().get("oldReadHost");
            for (Map resultMap : list) {
                String path = (String) resultMap.get("currency_logo");
                Base64Utils.getRealImageUrl(path, oldReadHost, rootPath, resultMap, "currency_logo");
            }
            result.setData((list == null || list.size() == 0) ? new ArrayList<Map>() : list);
        } catch (Exception e) {
            e.printStackTrace();
            result.setCode(Result.Code.ERROR);
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
            result.setMsg(ErrorCode.ERROR_SYSTEM.getMessage());
        }
        return result;
    }

    public Result getCurrencyETH() {
        Result result = new Result();
        try {
            Map map = new HashMap();
            map.put("a", CoinConst.ETH_CURRENCY_A);
            map.put("b", CoinConst.ETH_CURRENCY_B);
            map.put("lineFlag", CoinConst.CURRENCY_IS_LINE_YES);
            List<Map> list = yangCurrencyMapper.getCurrencyETH(map);
            result.setData((list == null || list.size() == 0) ? new ArrayList<YangCurrency>() : list);
        } catch (Exception e) {
            e.printStackTrace();
            result.setCode(Result.Code.ERROR);
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
            result.setMsg(ErrorCode.ERROR_SYSTEM.getMessage());
        }
        return result;
    }

    /**
     * 获取币种信息
     * 创建人：椰椰
     * Time：2019/9/3
     *
     * @return
     */
    public Result getCoins() {
        Result result = new Result();
        try {
            List<Map> list = yangCurrencyMapper.getCoins();
            if (list != null) {
                result.setCode(Result.Code.SUCCESS);
                result.setData(list);
                result.setMsg("查询成功");

            } else {
                result.setCode(Result.Code.ERROR);
                result.setMsg("查询失败");
                result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
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
     * 获取所有币种
     * 创建人：ccw
     * Time：2019/9/12
     *
     * @return
     */
    public Result getAllCoins() {
        Result result = new Result();
        try {
            List<Map> list = yangCurrencyMapper.getAllCoins();
            if (list.size() > 0) {
                result.setCode(Result.Code.SUCCESS);
                result.setData(list);
                result.setMsg("查询成功");

            } else {
                result.setCode(Result.Code.ERROR);
                result.setMsg("查询失败");
                result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.setCode(Result.Code.ERROR);
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
            result.setMsg(ErrorCode.ERROR_SYSTEM.getMessage());
        }
        return result;
    }
}
