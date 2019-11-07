package net.cyweb.service;

import com.github.pagehelper.PageInfo;
import cyweb.utils.CoinConst;
import cyweb.utils.DecimalUtils;
import cyweb.utils.ErrorCode;
import net.cyweb.config.custom.RevertCNYUtils;
import net.cyweb.mapper.YangC2CGuaMapper;
import net.cyweb.mapper.YangCurrencyMapper;
import net.cyweb.model.Result;
import net.cyweb.model.YangC2CGua;
import net.cyweb.model.YangMember;
import net.cyweb.model.modelExt.YangC2CGuaEvt;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class YangC2CGuaService extends BaseService<YangC2CGua> {

    @Autowired
    private YangC2CGuaMapper yangC2CGuaMapper;

    @Autowired
    private RedisService redisService;

    @Autowired
    private RevertCNYUtils revertCNYUtils;

    @Autowired
    private YangCurrencyMapper yangCurrencyMapper;

    @Autowired
    private YangMemberTokenService yangMemberTokenService;

    /**
     * yxt
     * 2019/9/18
     * 当前币种的所有挂单信息
     *
     * @param currencyId 币种的id
     * @return 币种的挂单信息
     */
    public Result getC2CGuaByCurrencyId(Integer currencyId, String accessToken) {
        Result result = new Result();
        String token = CoinConst.TOKENKEYTOKEN + accessToken;
        //验证token
        Object memberId = redisService.get(token);
        try {
            List<YangC2CGua> list = yangC2CGuaMapper.getC2CGuaByCurrencyId(currencyId, (Integer) memberId);
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
     * ccw
     * 2019/9/26
     * 当前用户的所有挂单信息
     *
     * @param accessToken
     * @return 币种的挂单信息
     */
    public Result findC2COrdersByAcceptances(String accessToken) {
        Result result = new Result();
        String token = CoinConst.TOKENKEYTOKEN + accessToken;
        //验证token
        Object memberId = redisService.get(token);
        if (memberId == null) {
            result.setCode(Result.Code.ERROR);
            result.setData("");
            result.setMsg(ErrorCode.ERROR_ERROR_TOKEN_EXPIRE.getMessage());
            result.setErrorCode(ErrorCode.ERROR_ERROR_TOKEN_EXPIRE.getIndex());
        }
        try {
            List<Map<String, Object>> list = yangC2CGuaMapper.getC2CGuaByMemberId((Integer) memberId);
            if (list.size() > 0) {
                result.setCode(Result.Code.SUCCESS);
                result.setData(list);
                result.setMsg("查询成功");

            } else {
                result.setCode(Result.Code.ERROR);
                result.setData("");
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
     * ccw
     * 2019/9/26
     * 根据承兑商id获取该承兑商下面的所有相关法币订单
     * 修改人 cq
     * 2019/10/3
     *
     * @param accessToken type
     * @return 币种的挂单信息
     */
    public Result findMembersOrdersByAcceptances(String accessToken, String type) {
        Result result = new Result();
        String token = CoinConst.TOKENKEYTOKEN + accessToken;
        //验证token
        Object memberId = redisService.get(token);
        if (memberId == null) {
            result.setCode(Result.Code.ERROR);
            result.setMsg(ErrorCode.ERROR_ERROR_TOKEN_EXPIRE.getMessage());
            result.setErrorCode(ErrorCode.ERROR_ERROR_TOKEN_EXPIRE.getIndex());
        }
        try {
            List<Map<String, Object>> list = yangC2CGuaMapper.findMembersOrdersByAcceptances((Integer) memberId, Integer.valueOf(type));
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
            result.setCode(Result.Code.ERROR);
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
            result.setMsg(ErrorCode.ERROR_SYSTEM.getMessage());
        }
        return result;
    }

    /**
     * cq
     * 2019/9/26
     * 当前用户的所有下单信息
     *
     * @param accessToken type
     * @return 币种的挂单信息
     */
    public Result findMembersOrdersByMemberId(String accessToken, String type) {
        Result result = new Result();
        String token = CoinConst.TOKENKEYTOKEN + accessToken;
        //验证token
        Object memberId = redisService.get(token);
        if (memberId == null) {
            result.setCode(Result.Code.ERROR);
            result.setMsg(ErrorCode.ERROR_ERROR_TOKEN_EXPIRE.getMessage());
            result.setErrorCode(ErrorCode.ERROR_ERROR_TOKEN_EXPIRE.getIndex());
        }
        try {
            List<Map<String, Object>> list = yangC2CGuaMapper.findMembersOrdersByMemberId((Integer) memberId, Integer.valueOf(type));
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
            result.setCode(Result.Code.ERROR);
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
            result.setMsg(ErrorCode.ERROR_SYSTEM.getMessage());
        }
        return result;
    }

    public Result getGuaList(Map<String, String> map) {
        Result result = new Result();
        try {
            Example example = new Example(YangC2CGua.class);
            List<Integer> statusList = new ArrayList<Integer>();
            statusList.add(CoinConst.C2C_GUA_STATUS_NOTRADE);
            statusList.add(CoinConst.C2C_GUA_STATUS_PARTTRADE);
            example.createCriteria().andEqualTo("type", map.get("type")).andIn("status", statusList);
            int total = yangC2CGuaMapper.selectCountByExample(example);
            example.setOrderByClause("id desc limit " + (Integer.parseInt(map.get("page")) - 1) * Integer.parseInt(map.get("pageSize")) + "," + Integer.parseInt(map.get("pageSize")));
            List<YangC2CGua> list = yangC2CGuaMapper.selectByExample(example);
            PageInfo<YangC2CGua> pageInfo = new PageInfo<YangC2CGua>(list);
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

    public Result getGua(Map<String, String> map) {
        Result result = new Result();
        try {
            Example example = new Example(YangC2CGua.class);
            example.createCriteria().andEqualTo("id", map.get("id"));
            List<YangC2CGua> list = yangC2CGuaMapper.selectByExample(example);
            result.setCode(Result.Code.SUCCESS);
            result.setData((list == null || list.size() == 0) ? new ArrayList<YangC2CGua>() : list.get(0));
        } catch (Exception e) {
            e.printStackTrace();
            result.setCode(Result.Code.ERROR);
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
            result.setMsg(ErrorCode.ERROR_SYSTEM.getMessage());
        }
        return result;
    }

    /**
     * 首页法币交易
     * 椰椰
     * 2019/9/2
     *
     * @param yangC2CGuaEvt
     * @return
     */
    public Result buyCoins(YangC2CGuaEvt yangC2CGuaEvt) {
        Result result = new Result();
        try {
            List<Integer> status = new ArrayList<Integer>();
            status.add(0);
            status.add(1);
            Example example = new Example(YangC2CGua.class);
            Example.Criteria criteria = example.createCriteria();
            criteria.andIn("status", status);
            criteria.andEqualTo("currencyId", yangC2CGuaEvt.getCurrencyId());
            criteria.andGreaterThanOrEqualTo("maxFund", yangC2CGuaEvt.getBuyMoney());
            criteria.andLessThanOrEqualTo("minFund", yangC2CGuaEvt.getBuyMoney());
            criteria.andEqualTo("type", "sell");
            example.setOrderByClause("price asc, id asc");
            List<YangC2CGua> list = yangC2CGuaMapper.selectByExample(example);
            YangC2CGua yangC2CGua = null;
            //遍历集合，找出适合的挂单
            BigDecimal money = new BigDecimal(yangC2CGuaEvt.getBuyMoney());
            for (YangC2CGua data : list) {
                BigDecimal price = money.divide(data.getPrice(), 8, BigDecimal.ROUND_UP);
                BigDecimal num = data.getNum();
                BigDecimal tradeNum = data.getTradeNum();
                BigDecimal frozenNum = data.getFrozenNum();
                BigDecimal num1 = num.subtract(tradeNum).subtract(frozenNum);
                if (price.compareTo(num1) < 1) {
                    yangC2CGua = data;
                    break;
                }
            }
            if (yangC2CGua == null) {
                result.setCode(Result.Code.ERROR);
                result.setMsg("没有查单合适的挂单");
            } else {
                Map<String, String> map = new HashMap<>();
                map.put("maxMoney", yangC2CGua.getMaxMoney());
                map.put("minMoney", yangC2CGua.getMinMoney());
                map.put("nickName", yangC2CGua.getNickName());
                BigDecimal orderNum = yangC2CGua.getOrderNum();
                BigDecimal tradeNum = yangC2CGua.getTradeNum();
                String division = DecimalUtils.subtraction(orderNum.toString(), tradeNum.toString());
                map.put("num", division);
                /*map.put("cardDetail", yangC2CGua.getCardDetail());
                map.put("cardInfo", yangC2CGua.getCardInfo());*/
                map.put("price", yangC2CGua.getPrice() + "");
                map.put("currencyId", yangC2CGua.getCurrencyId() + "");
                Map<String, String> currencyMarkMap = yangCurrencyMapper.getCurrencyMarkByCurrencyId(yangC2CGua.getCurrencyId());
                if (currencyMarkMap != null) {
                    map.put("currencyMark", currencyMarkMap.get("currencyMark"));
                } else {
                    map.put("currencyMark", "");
                }
                //最小购买数量
                String minNum = DecimalUtils.multiplicationForNum(yangC2CGua.getMinMoney(),
                        yangC2CGua.getPrice().toString());
                map.put("minNum", minNum);
                //最大购买数量
                String maxNum = DecimalUtils.multiplicationForNum(yangC2CGua.getMaxMoney(),
                        yangC2CGua.getPrice().toString());
                map.put("maxNum", maxNum);
                map.put("id", yangC2CGua.getId() + "");
                map.put("remark", yangC2CGua.getRemark());
                map.put("CNY", revertCNYUtils.revertCNYPrice("", yangC2CGua.getCurrencyId()));
                result.setCode(Result.Code.SUCCESS);
                result.setData(map);
                result.setMsg("查询成功");
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
     * 承兑商挂单撤单（买）
     *
     * @return
     * @Param map
     */
    public Result cancleAcceptancesBuyOrder(Map<String, String> map, String type) {
        Result result = new Result();
        String accessToken = map.get("accessToken");
        String id = map.get("guaId");
        try {
            //验证token
            if (StringUtils.isNotBlank(accessToken)) {
                result = this.validateAccessToken(accessToken, result, yangMemberTokenService);
                if (result.getData() == null) {
                    result.setCode(Result.Code.ERROR);
                    result.setData("");
                    result.setMsg(ErrorCode.ERROR_ERROR_TOKEN_EXPIRE.getMessage());
                    return result;
                }
                YangMember yangMember = (YangMember) result.getData();
                Map<String, Object> param = new HashMap<String, Object>();
                param.put("id", Integer.valueOf(id));
                param.put("type", type);
                param.put("status", -1);
                yangC2CGuaMapper.updateStatusById(param);
                result.setCode(Result.Code.SUCCESS);
                result.setData("");
                result.setMsg("撤销审核成功");
            }
        } catch (Exception e) {
            result.setData("");
            result.setCode(Result.Code.ERROR);
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
            result.setMsg(ErrorCode.ERROR_SYSTEM.getMessage());
        }
        return result;
    }

}
