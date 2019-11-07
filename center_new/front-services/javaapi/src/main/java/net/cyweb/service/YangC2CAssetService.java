package net.cyweb.service;

import com.netflix.discovery.converters.Auto;
import cyweb.utils.*;
import net.cyweb.config.custom.RevertCNYUtils;
import net.cyweb.mapper.YangC2CAssetMapper;
import net.cyweb.mapper.YangCurrencyMapper;
import net.cyweb.mapper.YangFinanceMapper;
import net.cyweb.mapper.YangTradeMapper;
import net.cyweb.model.*;
import net.cyweb.model.modelExt.YangCurrencyExt;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import tk.mybatis.mapper.entity.Example;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class YangC2CAssetService extends BaseService<YangC2CAsset> {

    @Autowired
    private YangMemberTokenService yangMemberTokenService;

    @Autowired
    private YangC2CAssetMapper yangC2CAssetMapper;

    @Autowired
    private YangFinanceMapper yangFinanceMapper;

    @Autowired
    private YangCurrencyMapper yangCurrencyMapper;

    @Autowired
    private YangTradeMapper yangTradeMapper;

    @Autowired
    private RevertCNYUtils revertCNYUtils;

    @Autowired
    private  RedisService redisService;

    public Result getAsset(Map<String, String> map) {
        Result result = new Result();
        try {
            //现获取用户信息
            validateAccessToken(map.get("accessToken"), result, yangMemberTokenService);
            if (result.getCode().intValue() == Result.Code.ERROR.intValue()) {
                return result;
            }
            YangMember yangMember = (YangMember) result.getData();
            Example example = new Example(YangCurrencyUser.class);
            example.createCriteria().andEqualTo("memberId", yangMember.getMemberId()).andEqualTo("currencyId", map.get("currencyId"));
            List<YangC2CAsset> list = yangC2CAssetMapper.selectByExample(example);
            result.setData(list);
        } catch (Exception e) {
            e.printStackTrace();
            result.setCode(Result.Code.ERROR);
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
            result.setMsg(ErrorCode.ERROR_SYSTEM.getMessage());
        }
        return result;
    }

    public Result c2cAssetChange(Map<String, String> map,HttpServletRequest request) {
        Result result = new Result();
        try {
            validateAccessToken(map.get("accessToken"), result, yangMemberTokenService);
            if (result.getCode().intValue() == Result.Code.ERROR.intValue()) {
                return result;
            }
            YangMember yangMember = (YangMember) result.getData();
           String ip =GetIpAddr.getIpAddr(request);
            Map map1 = new HashMap();
            map1.put("num", new BigDecimal(map.get("num")));
            map1.put("type", Byte.valueOf(map.get("type")));
            map1.put("memberId", yangMember.getMemberId());
            map1.put("currencyId", Integer.valueOf(map.get("currencyId")));
            int i = yangC2CAssetMapper.assetsChange(map1);
            if (i == 0) {
                //记录资产日志
                YangFinance yangFinance = new YangFinance();
                yangFinance.setAddTime(DateUtils.getNowTimesLong());
                yangFinance.setCurrencyId(Integer.valueOf(map.get("currencyId")));
                yangFinance.setMemberId(yangMember.getMemberId());
                yangFinance.setMoney(new BigDecimal(map.get("num")));
                yangFinance.setIp(ip);
                //c2c 转入 是  支出  c2c 转出是0 收入

                if ("1".equals(map.get("type"))) {
                    yangFinance.setMoneyType(CoinConst.FINANCE_RECORD_MONEY_ZC);
                    yangFinance.setContent(CoinConst.FINANCE_REMARK_IN_C2C);
                    yangFinance.setType(CoinConst.FINANCE_RECORD_ADD_BY_C2C);
                }

                if ("2".equals(map.get("type"))) {
                    yangFinance.setMoneyType(CoinConst.FINANCE_RECORD_MONEY_SR);
                    yangFinance.setContent(CoinConst.FINANCE_REMARK_OUT_C2C);
                    yangFinance.setType(CoinConst.FINANCE_RECORD_DEC_BY_C2C);
                }

                int j = yangFinanceMapper.insertSelective(yangFinance);
            } else {
                result.setCode(Result.Code.ERROR);
                result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
                result.setMsg(ErrorCode.ERROR_SYSTEM.getMessage());
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
     * 获取用户所有资产（币币）
     *
     * @param map
     * @return
     * @author ccw
     */
    public Result bibiAsset(Map<String, String> map) {
        String accessToken = map.get("accessToken");
        //可以没有,若有则为指定币的资产，没有则是全部资产
        String currencyId = map.get("currencyId");
        Map param = new HashMap();
        Result result = new Result();
        if (StringUtils.isBlank(accessToken)) {
            result.setCode(Result.Code.ERROR);
            result.setMsg("参数格式错误");
            return result;
        }
        try {
            //验证token
            result = this.validateAccessToken(accessToken, result, yangMemberTokenService);
            if (result.getData() == null) {
                result.setData("");
                result.setCode(Result.Code.ERROR);
                result.setMsg(ErrorCode.ERROR_ERROR_TOKEN_EXPIRE.getMessage());
                return result;
            }
            YangMember yangMember = (YangMember) result.getData();
            param.put("memberId", yangMember.getMemberId());
            String forzenRmb = "0.0";
            String rmb = "0.0";
            String allRmb = "0.0";
            if (StringUtils.isNotBlank(currencyId)) {
                param.put("currencyId", currencyId);
                //查指定的币的资产,yang_c2c_asset yang_trade 需要俩个参数
                //查出asset中的数量
                List<YangCurrencyExt> yangCurrencyExts = yangC2CAssetMapper.getCurrencyAndBiBiAssets(param);
                if (yangCurrencyExts.size() > 0) {
                    YangCurrencyExt yangCurrencyExt = yangCurrencyExts.get(0);
                    //查出trade中的price 这里的price是换算后的price
                    YangTrade yangTrade = yangTradeMapper.selectTradeByIds(yangMember.getMemberId(), yangCurrencyExt.getCurrencyId());
                    //然后相加
                    String price = revertCNYUtils.revertCNYPrice("", yangCurrencyExt.getCurrencyId());
                    forzenRmb = new DecimalUtils().multiplication(yangCurrencyExt.getForzenNum().toString(), price);
                    rmb = new DecimalUtils().multiplication(yangCurrencyExt.getNum().toString(), price);
                    allRmb = new DecimalUtils().addition(forzenRmb, rmb);
                    //得出yang_currency 和asset得出bean的记录
                    HashMap data = new HashMap();
                    data.put("yangCurrencyExtsList", yangCurrencyExt);
                    data.put("forzenRmb", forzenRmb);
                    data.put("rmb", rmb);
                    data.put("allRmb", allRmb);
                    result.setCode(Result.Code.SUCCESS);
                    result.setData(data);
                    result.setMsg("获取用户所有资产（币币）成功");

                } else {
                    result.setCode(Result.Code.ERROR);
                    result.setData("");
                    result.setMsg("获取用户所有资产（币币）失败");
                }

            } else {
                //根据memberId查所有资产 需要一个参数
                List<YangCurrencyExt> yangCurrencyExts = yangC2CAssetMapper.getCurrencyAndBiBiAssets(param);
                //查出trade中的price
                BigDecimal foren = new BigDecimal("0.00");
                BigDecimal rm = new BigDecimal("0.00");
                BigDecimal all = new BigDecimal("0.00");
                if (yangCurrencyExts.size() > 0) {
                    for (YangCurrencyExt yangCurrencyExt : yangCurrencyExts) {
                        YangTrade yangTrade = yangTradeMapper.selectTradeByIds(yangMember.getMemberId(), yangCurrencyExt.getCurrencyId());
                        //然后相加
                        String price = revertCNYUtils.revertCNYPrice("", yangCurrencyExt.getCurrencyId());
                        BigDecimal foren1 = yangCurrencyExt.getForzenNum().multiply(new BigDecimal(price));
                        foren = foren.add(foren1);
                        BigDecimal rm1 = yangCurrencyExt.getNum().multiply(new BigDecimal(price));
                        rm = rm.add(rm1);
                        BigDecimal all1 = foren1.add(rm1);
                        all = all.add(all1);
                        //得出yang_currency 和asset得出bean的记录
                    }
                    HashMap data = new HashMap();
                    data.put("yangCurrencyExtsList", yangCurrencyExts);
                    data.put("forzenRmb", foren.toString());
                    data.put("rmb", rm.toString());
                    data.put("allRmb", all.toString());
                    result.setCode(Result.Code.SUCCESS);
                    result.setData(data);
                    result.setMsg("获取用户所有资产（币币）成功");
                } else {
                    result.setCode(Result.Code.ERROR);
                    result.setData("");
                    result.setMsg("获取用户所有资产（币币）失败");
                }

            }

        } catch (Exception e) {
            result.setCode(Result.Code.ERROR);
            result.setData("");
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
            result.setMsg(ErrorCode.ERROR_SYSTEM.getMessage());
        }
        return result;
    }

    /**
     * 获取用户指定资产（法币）
     *
     * @param map
     * @return
     * @author ccw
     */
    public Result fabiAsset(Map<String, String> map) {
        String accessToken = map.get("accessToken");
        String currencyId = map.get("currencyId");
        Map param = new HashMap();
        Result result = new Result();
        try {
            //验证token
            result = this.validateAccessToken(accessToken, result, yangMemberTokenService);
            if (result.getData() == null) {
                result.setData("");
                result.setCode(Result.Code.ERROR);
                result.setMsg(ErrorCode.ERROR_ERROR_TOKEN_EXPIRE.getMessage());
                return result;
            }
            YangMember yangMember = (YangMember) result.getData();
            param.put("memberId", yangMember.getMemberId());
            param.put("currencyId", currencyId);
            //查指定的币的资产,yang_currency_user yang_trade 需要俩个参数
            //查出asset中的数量
            List<YangCurrencyExt> yangCurrencyExts = yangC2CAssetMapper.getCurrencyAndFaBiAssets(param);
            if (yangCurrencyExts.size() > 0) {
                YangCurrencyExt yangCurrencyExt = yangCurrencyExts.get(0);
                YangTrade yangTrade = yangTradeMapper.selectTradeByIds(yangMember.getMemberId(), yangCurrencyExt.getCurrencyId());
                //然后相加
                String price = revertCNYUtils.revertCNYPrice("", yangCurrencyExt.getCurrencyId());
                String forzenRmb = new DecimalUtils().multiplication(yangCurrencyExt.getForzenNum().toString(), price);
                String rmb = new DecimalUtils().multiplication(yangCurrencyExt.getNum().toString(), price);
                String allRmb = new DecimalUtils().addition(forzenRmb, rmb);
                //得出yang_currency 和asset得出bean的记录
                HashMap data = new HashMap();
                data.put("yangCurrencyExtsList", yangCurrencyExt);
                data.put("forzenRmb", forzenRmb);
                data.put("rmb", rmb);
                data.put("allRmb", allRmb);
                result.setCode(Result.Code.SUCCESS);
                result.setData(data);
                result.setMsg("获取用户指定资产（法币）成功");
            } else {
                result.setCode(Result.Code.ERROR);
                result.setData("");
                result.setMsg("获取用户指定资产（法币）失败");
            }


        } catch (Exception e) {
            result.setCode(Result.Code.ERROR);
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
            result.setMsg(ErrorCode.ERROR_SYSTEM.getMessage());
        }
        return result;
    }

    /**
     * 获取用户总资产（法币+币币）
     *
     * @param map
     * @return
     * @author ccw
     */
    public Result allAsset(Map<String, String> map) {
        String accessToken = map.get("accessToken");
        String currencyId = map.get("currencyId");
        Map param = new HashMap();
        Result result = new Result();
        try {
            //验证token
            result = this.validateAccessToken(accessToken, result, yangMemberTokenService);
            if (result.getData() == null) {
                result.setData("");
                result.setCode(Result.Code.ERROR);
                result.setMsg(ErrorCode.ERROR_ERROR_TOKEN_EXPIRE.getMessage());
                return result;
            }
            YangMember yangMember = (YangMember) result.getData();
            param.put("memberId", yangMember.getMemberId());
            param.put("currencyId", currencyId);
            //查指定的币的资产,yang_currency_user yang_trade 需要俩个参数
            //法币账户
            //查出asset中的数量
            List<YangCurrencyExt> yangCurrencyExts1 = yangC2CAssetMapper.getCurrencyAndFaBiAssets(param);
            BigDecimal foren1 = new BigDecimal("0");
            BigDecimal rm1 = new BigDecimal("0");
            BigDecimal all1 = new BigDecimal("0");
            if (yangCurrencyExts1.size() > 0) {
                YangCurrencyExt yangCurrencyExt1 = yangCurrencyExts1.get(0);
                //查出trade中的price
                YangTrade yangTrade1 = yangTradeMapper.selectTradeByIds(yangMember.getMemberId(), yangCurrencyExt1.getCurrencyId());
                //然后相加
                String price1 = revertCNYUtils.revertCNYPrice("", yangCurrencyExt1.getCurrencyId());
                foren1 = yangCurrencyExt1.getForzenNum().multiply(new BigDecimal(price1));
                rm1 = yangCurrencyExt1.getNum().multiply(new BigDecimal(price1));
                all1 = foren1.add(rm1);
            }
            BigDecimal foren2 = new BigDecimal("0");
            BigDecimal rm2 = new BigDecimal("0");
            BigDecimal all2 = new BigDecimal("0");
            //获取币币账户
            List<YangCurrencyExt> yangCurrencyExts2 = yangC2CAssetMapper.getCurrencyAndBiBiAssets(param);
            if (yangCurrencyExts2.size() > 0) {
                YangCurrencyExt yangCurrencyExt2 = yangCurrencyExts2.get(0);
                //查出trade中的price
                YangTrade yangTrade2 = yangTradeMapper.selectTradeByIds(yangMember.getMemberId(), yangCurrencyExt2.getCurrencyId());
                //然后相加
                String price2 = revertCNYUtils.revertCNYPrice("", yangCurrencyExt2.getCurrencyId());
                foren2 = yangCurrencyExt2.getForzenNum().multiply(new BigDecimal(price2));
                rm2 = yangCurrencyExt2.getNum().multiply(new BigDecimal(price2));
                all2 = foren2.add(rm2);
            }
            //得出yang_currency 和asset得出bean的记录
            HashMap data = new HashMap();
            String foren = foren1.add(foren2).toString();
            String rm = rm1.add(rm2).toString();
            String all = all1.add(all2).toString();
            data.put("forzenRmb", foren);
            data.put("rmb", rm);
            data.put("allRmb", all);
            result.setCode(Result.Code.SUCCESS);
            result.setData(data);
            result.setMsg("获取用户指定资产（法币+币币）成功");

        } catch (Exception e) {
            e.printStackTrace();
            result.setCode(Result.Code.ERROR);
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
            result.setMsg(ErrorCode.ERROR_SYSTEM.getMessage());
        }
        return result;
    }


}
