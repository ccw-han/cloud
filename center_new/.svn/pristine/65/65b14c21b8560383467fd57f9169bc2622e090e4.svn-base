package net.cyweb.service;

import com.github.pagehelper.PageInfo;
import cyweb.utils.CoinConst;
import cyweb.utils.DateUtils;
import cyweb.utils.ErrorCode;
import cyweb.utils.UUIDUtils;
import net.cyweb.config.custom.EmailUtils;
import net.cyweb.mapper.*;
import net.cyweb.model.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.math.BigDecimal;
import java.util.*;

@Service
public class YangC2COrdersService extends BaseService<YangC2COrders>{

    @Autowired
    private YangMemberTokenService yangMemberTokenService;

    @Autowired
    private YangC2COrdersMapper yangC2COrdersMapper;

    @Autowired
    private YangC2CGuaMapper yangC2CGuaMapper;

    @Autowired
    private YangBankMapper yangBankMapper;

    @Autowired
    private YangC2CAssetMapper yangC2CAssetMapper;

    @Autowired
    private YangCurrencyUserMapper yangCurrencyUserMapper;

    @Autowired
    private YangFinanceMapper yangFinanceMapper;

    @Autowired
    private RedisService redisService;

    public Result getC2COrderList(Map<String, String> map){
        Result result=new Result();
        try{
            validateAccessToken(map.get("accessToken"),result,yangMemberTokenService);
            if(result.getCode().intValue()==Result.Code.ERROR.intValue()){
                return result;
            }
            YangMember yangMember=(YangMember)result.getData();
            Example example=new Example(YangC2COrders.class);
            Example.Criteria criteria=example.createCriteria();
            criteria.andEqualTo("memberId",yangMember.getMemberId());
            if(StringUtils.isNotEmpty(map.get("type"))){
                criteria.andEqualTo("type",map.get("type"));
            }
            if(StringUtils.isNotEmpty(map.get("buyStatus"))){
                criteria.andEqualTo("buyStatus",map.get("buyStatus"));
            }
            int total= yangC2COrdersMapper.selectCountByExample(example);
            example.setOrderByClause("add_time desc limit "+(Integer.parseInt(map.get("page"))-1)*Integer.parseInt(map.get("pageSize"))+","+Integer.parseInt(map.get("pageSize")));
            List<YangC2COrders> list= yangC2COrdersMapper.selectByExample(example);
            PageInfo<YangC2COrders> pageInfo = new PageInfo<YangC2COrders>(list);
            pageInfo.setTotal(Long.valueOf(total));
            result.setCode(Result.Code.SUCCESS);
            result.setData(pageInfo);
        }catch (Exception e){
            e.printStackTrace();
            result.setCode(Result.Code.ERROR);
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
            result.setMsg(ErrorCode.ERROR_SYSTEM.getMessage());
        }
        return  result;
    }

    public Result getC2COrder(Map<String ,String > map){
        Result result=new Result();
        try{
            validateAccessToken(map.get("accessToken"),result,yangMemberTokenService);
            if(result.getCode().intValue()==Result.Code.ERROR.intValue()){
                return result;
            }
            YangMember yangMember=(YangMember)result.getData();
            Example example=new Example(YangC2COrders.class);
            Example.Criteria criteria=example.createCriteria();
            criteria.andEqualTo("memberId",yangMember.getMemberId());
            criteria.andEqualTo("tradeNo",map.get("id"));
            List<YangC2COrders> list= yangC2COrdersMapper.selectByExample(example);
            result.setCode(Result.Code.SUCCESS);
            result.setData( (list==null||list.size()==0) ?new ArrayList<YangC2COrders>():list.get(0));
        }catch (Exception e){
            e.printStackTrace();
            result.setCode(Result.Code.ERROR);
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
            result.setMsg(ErrorCode.ERROR_SYSTEM.getMessage());
        }
        return  result;
    }

    public Result c2cOrdersBuy(Map<String, String> map){
        Result result=new Result();

        try{
            validateAccessToken(map.get("accessToken"),result,yangMemberTokenService);
            if(result.getCode().intValue()==Result.Code.ERROR.intValue()){
                return result;
            }
            YangMember yangMember=(YangMember)result.getData();
            //买单  判断用户c2c 订单是否超过5个
            Example example=new Example(YangC2COrders.class);
            example.createCriteria().andEqualTo("type",CoinConst.ORDER_BUY).andEqualTo("memberId",yangMember.getMemberId()).andEqualTo("buyStatus",CoinConst.BANK_STATUS_AVA);
            List<YangC2COrders> list=yangC2COrdersMapper.selectByExample(example);
            if(list!=null&&list.size()>=5){
                result.setCode(Result.Code.ERROR);
                result.setErrorCode(ErrorCode.ERROR_C2C_ORDER_BUY_LIMIT.getIndex());
                result.setMsg(ErrorCode.ERROR_C2C_ORDER_BUY_LIMIT.getMessage());
                return result;
            }
            //查询当前 挂单信息
            Example guaExample=new Example(YangC2CGua.class);
            guaExample.createCriteria().andEqualTo("id",map.get("guaId"));
            List<YangC2CGua> guaList=yangC2CGuaMapper.selectByExample(guaExample);
            //未找到 存在挂单
            if(guaList==null||guaList.size()==0){
                result.setCode(Result.Code.ERROR);
                result.setErrorCode(ErrorCode.ERROR_C2C_GUA_NOT_FOUND.getIndex());
                result.setMsg(ErrorCode.ERROR_C2C_GUA_NOT_FOUND.getMessage());
                return result;
            }

            //查询当前挂单 信息
            YangC2CGua yangC2CGua=guaList.get(0);
            if((yangC2CGua.getNum().subtract(yangC2CGua.getFrozenNum())).compareTo(new BigDecimal(map.get("num")))<0){
                result.setCode(Result.Code.ERROR);
                result.setErrorCode(ErrorCode.ERROR_C2C_ORDER_BUY_NUM_LIMIT.getIndex());
                result.setMsg(ErrorCode.ERROR_C2C_ORDER_BUY_NUM_LIMIT.getMessage());
                return result;
            }
            //新增c2c挂单
            YangC2COrders yangC2COrders=new YangC2COrders();
            yangC2COrders.setTradeNo(UUID.randomUUID().toString().replace("-",""));
            yangC2COrders.setGuaId(Integer.valueOf(map.get("guaId")));
            yangC2COrders.setCurrencyId(guaList.get(0).getCurrencyId());
            yangC2COrders.setType(CoinConst.ORDER_BUY);
            yangC2COrders.setBuyMoney(map.get("buyMoney"));
            yangC2COrders.setBuyStatus(CoinConst.C2C_ORDER_STATUS_WAITPAY);
            yangC2COrders.setAddTime(String.valueOf(DateUtils.getNowTimes()));
            yangC2COrders.setInvalidTime(String.valueOf(DateUtils.getNowTimes()+60*15));
            yangC2COrders.setPrice(guaList.get(0).getPrice());
            yangC2COrders.setNum(new BigDecimal(map.get("num")));
            yangC2COrders.setMemberId(yangMember.getMemberId());
            yangC2COrders.setCardDetail(guaList.get(0).getCardDetail());
            yangC2COrders.setNickName(guaList.get(0).getNickName());
            yangC2COrders.setCankaoNum(EmailUtils.getRandomNumber(6));//6位随机数
            yangC2COrdersMapper.insertSelective(yangC2COrders);
            //冻结商家可交易数量
            yangC2CGua.setFrozenNum(yangC2CGua.getFrozenNum().add(new BigDecimal(map.get("num"))));
            yangC2CGuaMapper.updateByPrimaryKeySelective(yangC2CGua);
            result.setData(yangC2COrders);
        }catch (Exception e){
            e.printStackTrace();
            result.setCode(Result.Code.ERROR);
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
            result.setMsg(ErrorCode.ERROR_SYSTEM.getMessage());
        }
        return result;
    }

    public Result confirmBuy(Map<String, String> map){
        Result result=new Result();
        try{
            validateAccessToken(map.get("accessToken"),result,yangMemberTokenService);
            if(result.getCode().intValue()==Result.Code.ERROR.intValue()){
                return result;
            }
            YangMember yangMember=(YangMember)result.getData();
            Example example=new Example(YangC2COrders.class);
            example.createCriteria().andEqualTo("tradeNo",map.get("id"));
            List<YangC2COrders> list=yangC2COrdersMapper.selectByExample(example);
            //未找到当前订单
            if(list==null||list.size()==0){
                result.setCode(Result.Code.ERROR);
                result.setErrorCode(ErrorCode.ERROR_C2C_ORDER_NOT_FOUND.getIndex());
                result.setMsg(ErrorCode.ERROR_C2C_ORDER_NOT_FOUND.getMessage());
                return result;
            }
            YangC2COrders yangC2COrders=list.get(0);
            yangC2COrders.setBuyStatus(CoinConst.C2C_ORDER_STATUS_BUYERPAY);
            int i=yangC2COrdersMapper.updateByPrimaryKeySelective(yangC2COrders);
            result.setData(null);
            result.setMsg("购买成功！");
        }catch (Exception e){
            e.printStackTrace();
            result.setCode(Result.Code.ERROR);
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
            result.setMsg(ErrorCode.ERROR_SYSTEM.getMessage());
        }

        return result;
    }

    public Result cancelBuy(Map<String, String> map){
        Result result=new Result();
        try{
            validateAccessToken(map.get("accessToken"),result,yangMemberTokenService);
            if(result.getCode().intValue()==Result.Code.ERROR.intValue()){
                return result;
            }
            YangMember yangMember=(YangMember)result.getData();
            Example example=new Example(YangC2COrders.class);
            example.createCriteria().andEqualTo("tradeNo",map.get("id"));
            List<YangC2COrders> list=yangC2COrdersMapper.selectByExample(example);
            //未找到当前订单
            if(list==null||list.size()==0){
                result.setCode(Result.Code.ERROR);
                result.setErrorCode(ErrorCode.ERROR_C2C_ORDER_NOT_FOUND.getIndex());
                result.setMsg(ErrorCode.ERROR_C2C_ORDER_NOT_FOUND.getMessage());
                return result;
            }
            YangC2COrders yangC2COrders=list.get(0);
            if(CoinConst.ORDER_BUY.equals(yangC2COrders.getType())){
             yangC2COrders.setBuyStatus(CoinConst.C2C_ORDER_STATUS_CANCLE);
            }else{
                yangC2COrders.setBuyStatus(CoinConst.C2C_ORDER_STATUS_CANCLE_SELL);
            }
            int i=yangC2COrdersMapper.updateByPrimaryKeySelective(yangC2COrders);

            //冻结商家可交易数量

            //查询当前 挂单信息 是否存在
            Example guaExample=new Example(YangC2CGua.class);
            guaExample.createCriteria().andEqualTo("id",yangC2COrders.getGuaId());
            List<YangC2CGua> guaList=yangC2CGuaMapper.selectByExample(guaExample);
            if(guaList==null||guaList.size()==0){
                result.setCode(Result.Code.ERROR);
                result.setErrorCode(ErrorCode.ERROR_C2C_GUA_NOT_FOUND.getIndex());
                result.setMsg(ErrorCode.ERROR_C2C_GUA_NOT_FOUND.getMessage());
                return result;
            }
            YangC2CGua yangC2CGua=guaList.get(0);
            yangC2CGua.setFrozenNum(yangC2CGua.getFrozenNum().subtract(yangC2COrders.getNum()));
            yangC2CGua.setOrderNum(new BigDecimal(0));
            yangC2CGuaMapper.updateByPrimaryKeySelective(yangC2CGua);
            result.setData(null);
            result.setMsg("撤销成功！");
        }catch (Exception e){
            e.printStackTrace();
            result.setCode(Result.Code.ERROR);
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
            result.setMsg(ErrorCode.ERROR_SYSTEM.getMessage());
        }
        return result;
    }


    public Result c2cOrdersSell(Map<String, String> map){
        Result result=new Result();
        try{
            validateAccessToken(map.get("accessToken"),result,yangMemberTokenService);
            if(result.getCode().intValue()==Result.Code.ERROR.intValue()){
                return result;
            }
            YangMember yangMember=(YangMember)result.getData();
            //查询当前 挂单信息 是否存在
            Example guaExample=new Example(YangC2CGua.class);
            guaExample.createCriteria().andEqualTo("id",map.get("guaId"));
            List<YangC2CGua> guaList=yangC2CGuaMapper.selectByExample(guaExample);
            //未找到 存在挂单
            if(guaList==null||guaList.size()==0){
                result.setCode(Result.Code.ERROR);
                result.setErrorCode(ErrorCode.ERROR_C2C_GUA_NOT_FOUND.getIndex());
                result.setMsg(ErrorCode.ERROR_C2C_GUA_NOT_FOUND.getMessage());
                return result;
            }
            //查询当前 银行卡信息 是否存在
            Example bankExample=new Example(YangBank.class);
            bankExample.createCriteria().andEqualTo("cardnum",map.get("bankId")).andEqualTo("uid",yangMember.getMemberId()).andEqualTo("status",CoinConst.BANK_STATUS_AVA);
            //查询当前用户银行卡信息是否存在  1 存在 修改状态  2 不存在 报错返回
            List<YangBank> list=yangBankMapper.selectByExample(bankExample);
            if(list==null||list.size()==0){
                result.setCode(Result.Code.ERROR);
                result.setErrorCode(ErrorCode.ERROR_BANK_NOT_FOOUD.getIndex());
                result.setMsg(ErrorCode.ERROR_BANK_NOT_FOOUD.getMessage());
                return result;
            }
            //查询今日  卖单数量  不能超过10次
            Example sellExample=new Example(YangC2COrders.class);
            String dateNow = DateUtils.getDateStrPre(0);
            sellExample.createCriteria().andEqualTo("type",CoinConst.ORDER_SELL)
                    .andEqualTo("buyStatus",CoinConst.C2C_ORDER_STATUS_WAITPAY_SELL)
                    .andEqualTo("memberId",yangMember.getMemberId())
                    .andGreaterThanOrEqualTo("addTime",DateUtils.getTimeStamp(dateNow + " 0:0:0") / 1000)
                    .andLessThanOrEqualTo("addTime", DateUtils.getTimeStamp(dateNow + " 23:59:59") / 1000);
            List<YangC2COrders> sellList=yangC2COrdersMapper.selectByExample(sellExample);
            if(sellList!=null&&sellList.size()>=10){
                result.setCode(Result.Code.ERROR);
                result.setErrorCode(ErrorCode.ERROR_C2C_ORDER_SELL_LIMIT.getIndex());
                result.setMsg(ErrorCode.ERROR_C2C_ORDER_SELL_LIMIT.getMessage());
                return result;
            }

            //查询c2C资产余额是否 够
            Example assetExample=new Example(YangC2CAsset.class);
            assetExample.createCriteria().andEqualTo("memberId",yangMember.getMemberId()).andEqualTo("currencyId",guaList.get(0).getCurrencyId());
            List<YangC2CAsset> assetList=yangC2CAssetMapper.selectByExample(assetExample);
            if(assetList==null||assetList.size()==0){
                result.setCode(Result.Code.ERROR);
                result.setErrorCode(ErrorCode.ERROR_TRADE_CURRENCY_USER_PRICE_NOTENOUGH.getIndex());
                result.setMsg(ErrorCode.ERROR_TRADE_CURRENCY_USER_PRICE_NOTENOUGH.getMessage());
                return result;
            }

            //冻结c2c账户余额
            Map tradeMap=new HashMap();
            tradeMap.put("memberId",yangMember.getMemberId());
            tradeMap.put("cid",guaList.get(0).getCurrencyId());
            tradeMap.put("rmb_in",map.get("num"));
            tradeMap.put("rmb_operation","dec");
            tradeMap.put("forzen_rmb_in",map.get("num"));
            tradeMap.put("forzen_rmb_operation","inc");
            int i=yangC2CAssetMapper.assetTrade(tradeMap);
            if(i==1){
                //c2c账户冻结异常
                result.setCode(Result.Code.ERROR);
                result.setErrorCode(ErrorCode.ERROR_C2C_ACCOUNT_FOREZN.getIndex());
                result.setMsg(ErrorCode.ERROR_C2C_ACCOUNT_FOREZN.getMessage());
                return result;
            }
            //新增c2c挂单
            YangC2COrders yangC2COrders=new YangC2COrders();
            yangC2COrders.setTradeNo(UUID.randomUUID().toString().replace("-",""));
            yangC2COrders.setGuaId(Integer.valueOf(map.get("guaId")));
            yangC2COrders.setCurrencyId(guaList.get(0).getCurrencyId());
            yangC2COrders.setType(CoinConst.ORDER_SELL);
            yangC2COrders.setBuyMoney(map.get("buyMoney"));
            yangC2COrders.setBuyStatus(CoinConst.C2C_ORDER_STATUS_WAITPAY_SELL);
            yangC2COrders.setAddTime(String.valueOf(DateUtils.getNowTimes()));
            yangC2COrders.setInvalidTime(String.valueOf(DateUtils.getNowTimes()+60*60));
            yangC2COrders.setPrice(guaList.get(0).getPrice());
            yangC2COrders.setNum(new BigDecimal(map.get("num")));
            yangC2COrders.setMemberId(yangMember.getMemberId());
            yangC2COrders.setCardDetail(guaList.get(0).getCardDetail());
            yangC2COrders.setNickName(guaList.get(0).getNickName());
            yangC2COrders.setCankaoNum(EmailUtils.getRandomNumber(6));//6位随机数
            yangC2COrders.setBankId(Integer.valueOf(map.get("bankId")));
            yangC2COrders.setOrderNum(new BigDecimal(map.get("orderNum")));
            yangC2COrdersMapper.insertSelective(yangC2COrders);

            //新增财务日志
            YangFinance yangFinance=new YangFinance();
            yangFinance.setAddTime(DateUtils.getNowTimesLong());
            yangFinance.setCurrencyId(guaList.get(0).getCurrencyId());
            yangFinance.setMemberId(yangMember.getMemberId());
            yangFinance.setMoney(new BigDecimal(map.get("num")));
            yangFinance.setIp(map.get("ip"));
            yangFinance.setMoneyType(CoinConst.FINANCE_RECORD_MONEY_ZC);//支出
            yangFinance.setContent(CoinConst.FINANCE_REMARK_CHANGE_C2C);
            yangFinance.setType(CoinConst.FINANCE_RECORD_CHANGE_BY_C2C);//c2c资产变更
            int j=yangFinanceMapper.insertSelective(yangFinance);

            //冻结商家可交易数量
            YangC2CGua yangC2CGua=guaList.get(0);
            yangC2CGua.setFrozenNum(yangC2CGua.getFrozenNum().add(new BigDecimal(map.get("num"))));
            yangC2CGuaMapper.updateByPrimaryKeySelective(yangC2CGua);
            result.setData(yangC2COrders);
        }catch (Exception e){
            e.printStackTrace();
            result.setCode(Result.Code.ERROR);
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
            result.setMsg(ErrorCode.ERROR_SYSTEM.getMessage());
        }
        return result;
    }

    /***
     * wf
     * 查询银行卡
     * @param map
     * @return
     */
    public  Result findBanksInfoByUser(Map<String, String> map){
        Result result =new Result();
        try{
            validateAccessToken(map.get("accessToken"),result,yangMemberTokenService);
            if(result.getCode().intValue()==Result.Code.ERROR.intValue()){
                return result;
            }
            Integer memberId = (Integer) redisService.get(CoinConst.TOKENKEYTOKEN + map.get("accessToken"));

            List<Map> list= yangBankMapper.selectYangBankById(memberId.toString(),map.get("type"));
            result.setData(list);
        }catch (Exception e ){
            e.printStackTrace();
            result.setCode(Result.Code.ERROR);
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
            result.setMsg(ErrorCode.ERROR_SYSTEM.getMessage());
        }

        return result;
    }
}
