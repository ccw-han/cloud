package net.cyweb.service;

import cyweb.utils.CoinConst;
import cyweb.utils.ErrorCode;
import io.swagger.annotations.ApiResponse;
import net.cyweb.config.custom.Base64Utils;
import net.cyweb.mapper.FunFlashMapper;
import net.cyweb.model.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FunFlashService extends BaseService<FunFlash> {

    @Autowired
    private FunFlashMapper funFlashMapper;

    @Autowired
    private YangArticleService yangArticleService;

    @Autowired
    public UserProPs userProPs;

    @Autowired
    private YangCurrencyPairService yangCurrencyPairService;

    @Autowired
    private YangTradeFeeService yangTradeFeeService;

    @Autowired
    private YangMemberTokenService yangMemberTokenService;

    @Autowired
    private YangGoogleAuthService yangGoogleAuthService;

    @Autowired
    private YangOrderService yangOrderService;

    /**
     * 获取幻灯片
     *
     * @return
     * @author ccw
     */
    public Result getFlash(String language, String type) {
        Result result = new Result();
        try {
            Example example = new Example(FunFlash.class);
            example.setOrderByClause("sort desc");
            Example.Criteria criteria = example.createCriteria();
            criteria.andEqualTo("delFlag", CoinConst.DEL_FLAG_NO);
            criteria.andEqualTo("type", type);
            List<FunFlash> list = funFlashMapper.selectByExample(example);

            List<Map<String, String>> dataList = new ArrayList<Map<String, String>>();
            if (list.size() > 0) {
                for (FunFlash funFlash : list) {
                    Map<String, String> data = new HashMap<String, String>();
                    data.put("flashId", funFlash.getFlashId());
                    data.put("pic", funFlash.getPic());
                    data.put("jumpUrl", funFlash.getJumpUrl());
                    dataList.add(data);
                }
                result.setCode(Result.Code.SUCCESS);
                result.setData(dataList);
                result.setMsg("获取成功");
            } else {
                result.setCode(Result.Code.ERROR);
                result.setMsg("获取失败");
            }

        } catch (Exception e) {
            e.printStackTrace();
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
            result.setCode(Result.Code.ERROR);
            result.setMsg(ErrorCode.ERROR_SYSTEM.getMessage());
        }
        return result;
    }


    public Result initWeb(String language, String type, String ids) {
        Result result = new Result();

        Map map = new HashMap();
        Result a = getFlash(language, type);
        Result b = yangArticleService.getArticleList(language, type, "", 1, 5);

        Result c = yangCurrencyPairService.getCurrencyListChange("", "", "1", "");
        if (StringUtils.isNotEmpty(ids)) {
            Result e = yangCurrencyPairService.getCurrencyListChange(ids, "", "", "");
            map.put("cm", e.getData());
        }
        Result d = yangCurrencyPairService.getCurrencyListChange("", "", "0", "");
        map.put("flash", a.getData());
        map.put("article", b.getData());
        map.put("pairWithMine", c.getData());
        map.put("pairList", d.getData());
        result.setData(map);
        return result;
    }

    public Result getTradeData(String language, String cyId) {
        Result result = new Result();
        Map map = new HashMap();
        Result a = yangArticleService.getArticleList(language, "", "", 1, 5);
        Result b = yangCurrencyPairService.getCurrencyListChange("", "", "0", "");
        Result c = yangCurrencyPairService.getCurrencyListChange("", "", "1", "");
        Result d = yangCurrencyPairService.getCurrencyDetailChange("", "", cyId);
        Result e = yangCurrencyPairService.getCurrencyPairOrder(CoinConst.ORDER_BUY, cyId);
        Result f = yangCurrencyPairService.getCurrencyPairOrder(CoinConst.ORDER_SELL, cyId);
        Result g = yangCurrencyPairService.getCurrencyPairTrade(cyId);

        map.put("article", a.getData());
        map.put("pairList", b.getData());
        map.put("pairListWithMine", c.getData());
        map.put("pairDetail", d.getData());
        map.put("pairOrderByBuy", e.getData());
        map.put("pairOrderBySell", f.getData());
        map.put("pairTrade", g.getData());

        result.setData(map);
        return result;
    }

    public Result getUserTradeData(String accessToken, String cyId, int currencyId, int currencyTradeId) {
        Result result = new Result();
        Map map = new HashMap();
        Result a = yangTradeFeeService.getTradeFee(accessToken, cyId);
        YangMemberToken yangMemberToken = new YangMemberToken();
        yangMemberToken.setAccessToken(accessToken);
        YangCurrencyUser yangCurrencyUser = new YangCurrencyUser();
        yangCurrencyUser.setCurrencyId(currencyId);
        YangMember b = yangMemberTokenService.getSpecAssets(yangMemberToken, yangCurrencyUser);
        YangCurrencyUser yangCurrencyUsera = new YangCurrencyUser();
        yangCurrencyUsera.setCurrencyId(currencyTradeId);
        YangMember c = yangMemberTokenService.getSpecAssets(yangMemberToken, yangCurrencyUsera);

        Result d = yangGoogleAuthService.getGoogle(accessToken);


        Map pama = new HashMap();
        pama.put("currencyId", String.valueOf(currencyId));
        pama.put("currencyTradeId", String.valueOf(currencyTradeId));
        Result e = yangOrderService.getFontUserOrderFiveRecord(pama, yangMemberToken);
        Result f = yangOrderService.getFontUserTradeFiveRecord(pama, yangMemberToken);

        map.put("tradeFee", a.getData());
        map.put("assetsByCurrencyId", b);
        map.put("assetsByCurrencyTradeId", c);
        map.put("googleInfo", d.getData());
        map.put("fiveOrderRecord", e.getData());
        map.put("fiveTradeRecord", f.getData());

        result.setData(map);
        return result;
    }
}
