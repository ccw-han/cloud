package net.cyweb.service;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;
import cyweb.utils.CoinConst;
import cyweb.utils.DateUtils;
import cyweb.utils.DecimalUtils;
import cyweb.utils.ErrorCode;
import io.swagger.annotations.ApiOperation;
import net.cyweb.mapper.*;
import net.cyweb.model.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import tk.mybatis.mapper.entity.Example;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class YangTibiOutService extends BaseService<YangTibiOut> {

    @Autowired
    private YangTibiOutMapper yangTibiOutMapper;
    @Autowired
    private YangQianbaoAddressMapper yangQianbaoAddressMapper;

    @Autowired
    private YangMemberTokenService yangMemberTokenService;

    @Autowired
    private YangFinanceMapper yangFinanceMapper;

    @Autowired
    private RedisService redisService;

    @Autowired
    private YangCurrencyMapper yangCurrencyMapper;

    @Autowired
    private YangTibiMapper yangTibiMapper;

    @Autowired
    YangC2CAssetMapper yangC2CAssetMapper;

    @Autowired
    YangConfigMapper yangConfigMapper;


    /**
     * 获取用户的历史提币记录
     * yxt
     * 2019/9/18
     *
     * @param currencyId 用户的id
     * @return
     */
    public Result getWithdrawRecordByCurrencyId(String currencyId, String accessToken) {
        Result result = new Result();
        String token = CoinConst.TOKENKEYTOKEN + accessToken;
        //验证token
        Object memberId = redisService.get(token);
        try {
            List<YangTibiOut> list = yangTibiOutMapper.getWithdrawRecordByCurrencyId(currencyId, (Integer) memberId);
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


    public Result getTibiOutList(String accessToken, String currencyId, int page, int pageSize) {
        Result result = new Result();
        try {
            validateAccessToken(accessToken, result, yangMemberTokenService);
            if (result.getCode().intValue() == Result.Code.ERROR.intValue()) {
                return result;
            }

            YangMember yangMember = (YangMember) result.getData();
            Map map = new HashMap();
            if (StringUtils.isNotEmpty(currencyId)) {
                map.put("currencyId", currencyId);
            }

            map.put("memberId", yangMember.getMemberId());
            map.put("start", (page - 1) * pageSize);
            map.put("end", pageSize);
            List<Map> list = yangTibiOutMapper.findTiBiList(map);
            int total = yangTibiOutMapper.selectTotalRecord(map);
            PageInfo<Map> pageInfo = new PageInfo<Map>(list);
            pageInfo.setTotal(Long.valueOf(total));
            result.setCode(Result.Code.SUCCESS);
            result.setData(pageInfo);

        } catch (Exception e) {
            e.printStackTrace();
            result.setCode(Result.Code.ERROR);
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
            result.setMsg(ErrorCode.ERROR_SYSTEM.getMessage());
        }
        return result;
    }


    public Result addTibiOut(String accessToken, String currencyId, String fee, String addressId, String num, String actualNum, String ip, String emailCode) {
        Result result = new Result();
        try {
            validateAccessToken(accessToken, result, yangMemberTokenService);
            if (result.getCode().intValue() == Result.Code.ERROR.intValue()) {
                return result;
            }

            YangMember yangMember = (YangMember) result.getData();
            String key = CoinConst.EMAILFORFINDCODE + yangMember.getEmail() + "_" + CoinConst.EMAIL_CODE_TYPE_TB;
            if (redisService.exists(key)) {
                EmailLog emailLog = JSONObject.parseObject(redisService.get(key).toString(), EmailLog.class);

                if (emailCode.equals(emailLog.getContent())) {
                    //检查提币地址 id 和 token是否匹配

                    Example example = new Example(YangQianBaoAddress.class);
                    example.createCriteria().andEqualTo("userId", yangMember.getMemberId())
                            .andEqualTo("id", addressId).andEqualTo("currencyId", currencyId);
                    List<YangQianBaoAddress> list = yangQianbaoAddressMapper.selectByExample(example);
                    if (list == null || list.size() == 0) {
                        result.setCode(Result.Code.ERROR);
                        result.setErrorCode(ErrorCode.ERROR_QIANBAO_ADDRESS_NO_FOUND.getIndex());
                        result.setMsg(ErrorCode.ERROR_QIANBAO_ADDRESS_NO_FOUND.getMessage());
                        return result;
                    }
                    //冻结用户资产

                    YangMemberToken yangMemberToken = new YangMemberToken();
                    yangMemberToken.setAccessToken(accessToken);

                    YangCurrencyUser yangCurrencyUser = new YangCurrencyUser();
                    yangCurrencyUser.setCurrencyId(Integer.valueOf(currencyId));
                    yangCurrencyUser.setNum(new BigDecimal(num));
                    yangCurrencyUser.setNumType("dec");
                    yangCurrencyUser.setForzenNumType("inc");
                    yangCurrencyUser.setForzenNum(new BigDecimal(num));

                    result = yangMemberTokenService.updateAssets(yangMemberToken, yangCurrencyUser, new YangMember());

                    //添加提币记录

                    YangTibiOut yangTibiOut = new YangTibiOut();
                    yangTibiOut.setUserId(yangMember.getMemberId());
                    yangTibiOut.setUrl(list.get(0).getQianbaoUrl());
                    yangTibiOut.setName(list.get(0).getName());
                    yangTibiOut.setAddTime(DateUtils.getNowTimes());
                    yangTibiOut.setNum(new BigDecimal(num));
                    yangTibiOut.setStatus(CoinConst.TIBI_OUT_STATUS_ING);
                    yangTibiOut.setCurrencyId(Integer.valueOf(currencyId));
                    yangTibiOut.setFee(new BigDecimal(fee));
                    yangTibiOut.setActual(new BigDecimal(actualNum));
                    yangTibiOut.setConfirmations(0);
                    yangTibiOut.setMyurl(" ");
                    yangTibiOut.setTiId(" ");
                    yangTibiOut.setCheckTime(0);
                    yangTibiOutMapper.insertSelective(yangTibiOut);
                    //增加资产日志

                    YangFinance yangFinance = new YangFinance();
                    yangFinance.setAddTime(DateUtils.getNowTimesLong());
                    yangFinance.setCurrencyId(Integer.valueOf(currencyId));
                    yangFinance.setMemberId(yangMember.getMemberId());
                    yangFinance.setMoney(new BigDecimal(num));
                    yangFinance.setIp(ip);
                    yangFinance.setMoneyType(CoinConst.FINANCE_RECORD_MONEY_ZC);//支出
                    yangFinance.setContent(CoinConst.FINANCE_REMARK_TIBI_OUT);
                    yangFinance.setType(CoinConst.FINANCE_RECORD_TIBI_OUT);//提币
                    yangFinanceMapper.insertSelective(yangFinance);
                    result.setData(yangTibiOut);


                } else {
                    result.setCode(Result.Code.ERROR);
                    result.setMsg("验证码错误,请重新请求");
                    result.setErrorCode(ErrorCode.ERROR_ERROR_CODE.getIndex());
                }

            } else {
                result.setCode(Result.Code.ERROR);
                result.setMsg("验证码不存在或已过期");
                result.setErrorCode(ErrorCode.ERROR_ERROR_CODE.getIndex());
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
     * 用户发出提币请求
     *
     * @return
     * @author ccw
     */
    public Result withdrawRequest(String accessToken, String currencyId, String fee, String url, String num) {
        Result result = new Result();
        try {
            result = this.validateAccessToken(accessToken, result, yangMemberTokenService);
            if (result.getData() == null) {
                result.setCode(Result.Code.ERROR);
                result.setErrorCode(ErrorCode.ERROR_ERROR_TOKEN_EXPIRE.getIndex());
                result.setData(null);
                result.setMsg(ErrorCode.ERROR_ERROR_TOKEN_EXPIRE.getMessage());
                return result;
            }
            YangMember yangMember = (YangMember) result.getData();
            //从yang_c2c_asset中 冻结数量添加ammout 冻结资产，当提币成功，从冻结资产到num中
            //冻结增加num
            YangC2CAsset yangC2CAsset = yangC2CAssetMapper.getAssetOne(yangMember.getMemberId(), Integer.valueOf(currencyId));
            if (yangC2CAsset != null) {
                BigDecimal firstForzenNum = yangC2CAsset.getForzenNum();
                BigDecimal lastForzenNum = firstForzenNum.add(new BigDecimal(num));
                yangC2CAsset.setForzenNum(lastForzenNum);
                //num 减少
                BigDecimal firstNum = yangC2CAsset.getNum();
                BigDecimal lastNum = firstNum.subtract(new BigDecimal(num));
                yangC2CAsset.setNum(lastNum);
                //更新
                yangC2CAssetMapper.updateById(yangC2CAsset);
                //添加提币记录
                YangTibiOut yangTibiOut = new YangTibiOut();
                yangTibiOut.setUserId(yangMember.getMemberId());
                yangTibiOut.setUrl(url);
                yangTibiOut.setName(yangMember.getName());
                yangTibiOut.setAddTime(DateUtils.getNowTimes());
                yangTibiOut.setNum(new BigDecimal(num));
                yangTibiOut.setStatus(CoinConst.TIBI_OUT_STATUS_ING);
                yangTibiOut.setCurrencyId(Integer.valueOf(currencyId));
                yangTibiOut.setFee(new BigDecimal(fee));
                yangTibiOut.setConfirmations(0);
                yangTibiOut.setMyurl(" ");
                yangTibiOut.setTiId(" ");
                yangTibiOut.setCheckTime(0);
                yangTibiOutMapper.insertSelective(yangTibiOut);
                result.setCode(Result.Code.SUCCESS);
                result.setData(null);
                result.setMsg("添加提币请求成功");
            } else {
                result.setCode(Result.Code.ERROR);
                result.setData(null);
                result.setMsg("没找到用户该资产");
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
     * 获取后台通过审核的提币记录(钱包调取)
     *
     * @return
     * @author ccw
     */
    public Result getWithdrawRecordByStatus(String[] currencyMarks, Integer limit) {
        Result result = new Result();
        try {
            //获取currencyIds
            List<Integer> currencyIds = yangCurrencyMapper.getCurrencyIdByCurrencyMark(currencyMarks);
            List<Map<String, Object>> datas = yangTibiOutMapper.getWithdrawRecordByStatus(currencyIds);
            if (datas.size() > 0) {
                if (limit > 0) {
                    if (limit < datas.size()) {
                        datas = datas.subList(0, limit - 1);
                    }
                }
                result.setCode(Result.Code.SUCCESS);
                result.setData(datas);
                result.setMsg("查询成功");
            } else {
                result.setCode(Result.Code.ERROR);
                result.setMsg("查询失败");
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
     * 添加提币地址
     *
     * @return
     * @author ccw
     */
    public Result addDepositAddress(String accessToken, Map<String, String> map) {
        Result result = new Result();
        try {
            result = this.validateAccessToken(accessToken, result, yangMemberTokenService);
            if (result.getData() == null) {
                result.setCode(Result.Code.ERROR);
                result.setData("");
                result.setErrorCode(ErrorCode.ERROR_ERROR_TOKEN_EXPIRE.getIndex());
                result.setMsg(ErrorCode.ERROR_ERROR_TOKEN_EXPIRE.getMessage());
                return result;
            }
            YangMember yangMember = (YangMember) result.getData();
            YangQianBaoAddress yangQianBaoAddress = new YangQianBaoAddress();
            yangQianBaoAddress.setUserId(yangMember.getMemberId());
            yangQianBaoAddress.setQianbaoUrl(map.get("address"));
            yangQianBaoAddress.setCurrencyId(Integer.parseInt(map.get("currencyId")));
            yangQianBaoAddress.setRemark(map.get("remark").toUpperCase());
            yangQianBaoAddress.setStatus(1);
            yangQianBaoAddress.setName(yangMember.getName());
            yangQianBaoAddress.setAddTime(DateUtils.getNowTimes());
            yangQianbaoAddressMapper.addDepositAddress(yangQianBaoAddress);
            result.setCode(Result.Code.SUCCESS);
            result.setData(null);
            result.setMsg("添加地址成功");
        } catch (Exception e) {
            e.printStackTrace();
            result.setCode(Result.Code.ERROR);
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
            result.setMsg(ErrorCode.ERROR_SYSTEM.getMessage());
        }

        return result;
    }

    /**
     * 强哥返回的提币记录，记录表中
     *
     * @param map
     * @return
     * @author ccw
     */
    public Result withdrawNotifyReq(Map<String, String> map) {
        Result result = new Result();
        YangQianBaoAddress yangQianBaoAddress;
        String id = map.get("trans_id");
        String currencyMark = map.get("symbol").toUpperCase();
        String addressTo = map.get("address_to");
        String txid = map.get("txid");
        String amount = map.get("amount");
        Integer status = Integer.valueOf(map.get("status"));
        String realFee = map.get("real_fee");
        //验证签名（验证用）
        String sign = map.get("sign");
        Integer timestamp = Integer.valueOf(map.get("timestamp"));
        String secret = "jinzhiqiang";
        Integer curTime = DateUtils.getNowTimes();
        //时间戳（验证用）
        if ((curTime - timestamp) > 60) {
            result.setCode(Result.Code.ERROR);
            result.setMsg("提交时间过期");
            return result;
        }
        String mes = "address_to" + addressTo + "amount" + amount + "real_fee" + realFee + "status" + status + "symbol" + currencyMark + "timestamp" + timestamp + "trans_id" + id + "txid" + txid + secret;
        String md5Hex = DigestUtils.md5Hex(mes);
        //先验证sign
        if (!sign.equals(md5Hex)) {
            result.setCode(Result.Code.ERROR);
            result.setMsg("签名失败");
            return result;
        }
        try {
            //更新我那个状态，因为已经提币成功了
            if (status == CoinConst.TIBI_OUT_STATUS_PAY) {
                //修改状态
                YangTibiOut yangTibiOut = yangTibiOutMapper.selectYangTibiOutById(Integer.valueOf(id));
                yangTibiOut.setStatus(status);
                yangTibiOutMapper.updateYangTiBiOut(yangTibiOut);
            }
            if (status == CoinConst.TIBI_OUT_STATUS_SUC) {
                //成功，那么冻结数量减去 ammout 其他资产增加ammout
                YangTibiOut yangTibiOut = yangTibiOutMapper.selectYangTibiOutById(Integer.valueOf(id));
                yangTibiOut.setStatus(status);
                yangTibiOutMapper.updateYangTiBiOut(yangTibiOut);
                //更新资产表
                YangC2CAsset yangC2CAsset = yangC2CAssetMapper.getAssetOne(yangTibiOut.getUserId(), yangTibiOut.getCurrencyId());
                if (yangC2CAsset != null) {
                    BigDecimal firstForzenNum = yangC2CAsset.getForzenNum();
                    BigDecimal lastForzenNum = firstForzenNum.subtract(new BigDecimal(amount));
                    yangC2CAsset.setForzenNum(lastForzenNum);
                    //更新
                    yangC2CAssetMapper.updateById(yangC2CAsset);
                } else {
                    result.setCode(Result.Code.ERROR);
                    result.setMsg("用户没有该资产");
                    return result;
                }

            }
            if (status == CoinConst.TIBI_OUT_STATUS_REJ) {
                //拒绝 那么冻结数量减去 ammout num增加ammont
                YangTibiOut yangTibiOut = yangTibiOutMapper.selectYangTibiOutById(Integer.valueOf(id));
                yangTibiOut.setStatus(status);
                yangTibiOutMapper.updateYangTiBiOut(yangTibiOut);
                //更新资产表
                YangC2CAsset yangC2CAsset = yangC2CAssetMapper.getAssetOne(yangTibiOut.getUserId(), yangTibiOut.getCurrencyId());
                if (yangC2CAsset != null) {
                    BigDecimal firstForzenNum = yangC2CAsset.getForzenNum();
                    BigDecimal lastForzenNum = firstForzenNum.subtract(new BigDecimal(amount));
                    yangC2CAsset.setForzenNum(lastForzenNum);
                    //num 增加
                    BigDecimal firstNum = yangC2CAsset.getNum();
                    BigDecimal lastNum = firstNum.add(new BigDecimal(amount));
                    yangC2CAsset.setNum(lastNum);
                    //更新
                    yangC2CAssetMapper.updateById(yangC2CAsset);
                } else {
                    result.setCode(Result.Code.ERROR);
                    result.setMsg("用户没有该资产");
                    return result;
                }

            }

            result.setCode(Result.Code.SUCCESS);
            result.setMsg("更新提币记录成功");
        } catch (Exception e) {
            e.printStackTrace();
            result.setCode(Result.Code.ERROR);
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
            result.setMsg(ErrorCode.ERROR_SYSTEM.getMessage());
        }
        return result;
    }

    /**
     * 强哥返回的充币记录，记录表中
     *
     * @param map
     * @return
     * @author ccw
     */
    public Result depositNotifyReq(Map<String, String> map) {
        Integer confirmCount = 0;
        Result result = new Result();
        List<YangConfig> configs = yangConfigMapper.selectAll();
        if (configs.size() > 0) {
            for (YangConfig config : configs) {
                if ("confirm".equals(config.getKey())) {
                    confirmCount = Integer.valueOf(config.getValue());
                }
            }
        } else {
            result.setCode(Result.Code.ERROR);
            result.setMsg("配置表没数据");
            return result;
        }

        YangQianBaoAddress yangQianBaoAddress;
        YangTibi yangTibi = new YangTibi();
        String currencyMark = map.get("symbol").toUpperCase();
        String addressTo = map.get("address_to");
        String txid = map.get("txid");
        String amount = map.get("amount");
        Integer confirm = Integer.valueOf(map.get("confirm"));
        //验证签名（验证用）
        String sign = map.get("sign");
        //时间戳（验证用）
        Integer timestamp = Integer.valueOf(map.get("timestamp"));
        String secret = "jinzhiqiang";
        Integer curTime = DateUtils.getNowTimes();
        //时间戳（验证用）
        if ((curTime - timestamp) > 60) {
            result.setCode(Result.Code.ERROR);
            result.setMsg("充币提交时间过期");
            return result;
        }
        String mes = "address_to" + addressTo + "amount" + amount + "confirm" + confirm + "symbol" + currencyMark + "timestamp" + timestamp + "txid" + txid + secret;
        String md5Hex = DigestUtils.md5Hex(mes);
        //先验证sign
        if (!sign.equals(md5Hex)) {
            result.setCode(Result.Code.ERROR);
            result.setMsg("签名失败");
            return result;
        }
        //插一条数据
        try {
            yangQianBaoAddress = yangQianbaoAddressMapper.getYangQianBaoAddressByQianBaoUrl(addressTo);
            //根据地址，查询address中的user_id 以及name
            YangTibi yangTibi1 = yangTibiMapper.selectByTiId(txid);
            if (yangTibi1 != null) {
                yangTibi = yangTibi1;
            } else {
                yangTibi.setUserId(yangQianBaoAddress.getUserId());
                yangTibi.setMyurl("");
                yangTibi.setUrl(addressTo);
                yangTibi.setName(yangQianBaoAddress.getName());
                yangTibi.setAddTime(DateUtils.getNowTimes());
                yangTibi.setNum(new BigDecimal(amount));
                //设置未确认
                yangTibi.setStatus((byte) 0);
                yangTibi.setTiId(txid);
                yangTibi.setHeight("");
                yangTibi.setCheckTime(DateUtils.getNowTimes());
                //根据mark查出来的
                List<Integer> ids = yangCurrencyMapper.getCurrencyIdByCurrencyMark(new String[]{currencyMark});
                if (ids.size() > 0) {
                    yangTibi.setCurrencyId(ids.get(0));
                    yangTibi.setFee(new BigDecimal(0.000000000000000000));
                    yangTibi.setActual(new BigDecimal(0.000000000000000000));
                    yangTibi.setConfirmations(confirm);
                    yangTibiMapper.insertYangTiBi(yangTibi);
                } else {
                    result.setCode(Result.Code.ERROR);
                    result.setMsg("没有该币种");
                    return result;
                }

            }

            if (confirm >= confirmCount) {
                //状态为确认的，然后资产中加数量
                yangTibi.setStatus((byte) 1);
                yangTibiMapper.updateByPrimaryKey(yangTibi);
                Integer memberId = yangTibi.getUserId();
                Integer currencyId = yangTibi.getCurrencyId();
                YangC2CAsset yangC2CAsset = yangC2CAssetMapper.getAssetOne(memberId, currencyId);
                //num 增加
                if (yangC2CAsset != null) {
                    BigDecimal firstNum = yangC2CAsset.getNum();
                    BigDecimal lastNum = firstNum.add(new BigDecimal(amount));
                    yangC2CAsset.setNum(lastNum);
                    //更新
                    yangC2CAssetMapper.updateById(yangC2CAsset);
                } else {
                    result.setCode(Result.Code.ERROR);
                    result.setMsg("用户没该资产");
                    return result;
                }

            } else {
                result.setCode(Result.Code.ERROR);
                result.setMsg("次数没达到");
                return result;
            }
            result.setCode(Result.Code.SUCCESS);
            result.setMsg("添加提币地址成功");
        } catch (Exception e) {
            e.printStackTrace();
            result.setCode(Result.Code.ERROR);
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
            result.setMsg(ErrorCode.ERROR_SYSTEM.getMessage());
        }

        return result;
    }

}
