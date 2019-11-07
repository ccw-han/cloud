package net.cyweb.service;

import cyweb.utils.CoinConst;
import cyweb.utils.DateUtils;
import cyweb.utils.ErrorCode;
import net.cyweb.config.custom.ApiSignatureUtils;
import net.cyweb.mapper.*;
import net.cyweb.model.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class YangUserKeysService extends BaseService<YangUserKeys>{

    @Autowired
    private YangUserKeysMapper yangUserKeysMapper;

    @Autowired
    private YangMemberMapper yangMemberMapper;

    @Autowired
    private YangCurrencyPairMapper yangCurrencyPairMapper;

    @Autowired
    private YangCurrencyMapper yangCurrencyMapper;

    @Autowired
    private YangMemberTokenService yangMemberTokenService;

    @Autowired
    private RobotService robotService;

    @Autowired
    private YangOrdersMapper yangOrdersMapper;

    @Autowired
    private YangTradeMapper yangTradeMapper;

    @Autowired
    private YangOrderService yangOrderService;

    @Autowired
    private YangCurrencyUserMapper yangCurrencyUserMapper;

    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private RedisService redisService;

    @Autowired
    private YangCurrencyMarketMapper yangCurrencyMarketMapper;

    /**
     * 挂单交易
     * @return
     */
    public Result checkCreateOrder(String cId,BigDecimal price,String type,BigDecimal num,String key,String signature,String nonce){
        Result result=new Result();

        //1 密钥不存在
        result=checkKey(key);
        if(Result.Code.ERROR==result.getCode().intValue()){
            return result;
        }
        Integer memberId=((YangUserKeys)result.getData()).getMemberId();
        String privateKey=((YangUserKeys)result.getData()).getPrivateKey();
        //2 用户权限不足
        YangMember yangMember=new YangMember();
        yangMember.setMemberId(memberId);
        yangMember=yangMemberMapper.selectByPrimaryKey(yangMember);
        if(yangMember.getIsBlackName()==null|| CoinConst.IS_BLACK_MAN_YES==yangMember.getIsBlackName().intValue()){
            result.setCode(Result.Code.ERROR);
            result.setErrorCode(ErrorCode.ERROR_MAKE_MARKET_NO_AUTH.getIndex());
            result.setMsg(ErrorCode.ERROR_MAKE_MARKET_NO_AUTH.getMessage());
            result.setData(null);
            return result;
        }
        //3 签名不一致
        Map sb=new HashMap();
        sb.put("key",key);
        sb.put("cyInfo",cId.toLowerCase());
        sb.put("price",price.toString());
        sb.put("type",type);
        sb.put("num",num.toString());
        sb.put("nonce",nonce);
        result=checkSignature(ApiSignatureUtils.createLinkString(sb),signature,privateKey);
        if(Result.Code.ERROR==result.getCode().intValue()){
            result.setData(null);
            return result;
        }
        // 下单 时间限制
        String redisKey="api_user_last_create_order_time_"+memberId;
        if(redisService.exists(redisKey)){
            Long lastTime=Long.valueOf(redisService.get(redisKey).toString());
            long nowTime=System.currentTimeMillis();
            if(nowTime-lastTime<=1000){
                result.setCode(Result.Code.ERROR);
                result.setErrorCode(ErrorCode.ERROR_MAKE_MARKET_LIMIT_ORDER.getIndex());
                result.setMsg(ErrorCode.ERROR_MAKE_MARKET_LIMIT_ORDER.getMessage());
                return result;
            }
        }
        redisService.set(redisKey, String.valueOf(System.currentTimeMillis()));

        //4 交易对是否存在
        result= checkCurrencyPair(cId);
        if(Result.Code.ERROR==result.getCode().intValue()){
            result.setData(null);
            return result;
        }
        YangCurrencyPair yangCurrencyPair=(YangCurrencyPair)result.getData();


        //5 交易对 是否允许交易
        YangCurrency yangCurrency = new YangCurrency();
        yangCurrency.setCurrencyId(yangCurrencyPair.getCurrencyId());
        yangCurrency = yangCurrencyMapper.selectOne(yangCurrency);
        if(yangCurrency.getIsLock().intValue() == 1)  //交易不许可
        {
            result.setErrorCode(ErrorCode.ERROR_MAKE_MARKET_NO_TRADE_CURRENCY.getIndex());
            result.setMsg(ErrorCode.ERROR_MAKE_MARKET_NO_TRADE_CURRENCY.getMessage());
            result.setCode(Result.Code.ERROR);
            result.setData(null);
            return result;
        }


        //6 价格是否 合适

        if(price==null || price.compareTo(BigDecimal.ZERO) <=0 )  //验证交易基本信息是否合法
        {
            result.setErrorCode(ErrorCode.ERROR_MAKE_MARKET_PRICE_ERROR.getIndex());
            result.setMsg(ErrorCode.ERROR_MAKE_MARKET_PRICE_ERROR.getMessage());
            result.setCode(Result.Code.ERROR);
            result.setData(null);
            return result;
        }
        //7 挂单数量 不合适
        if(num.compareTo(BigDecimal.ZERO) <=0 ||(yangCurrencyPair.getMinMoney().compareTo(num.doubleValue()) >0 ))  //验证交易基本信息是否合法
        {
            result.setErrorCode(ErrorCode.ERROR_MAKE_MARKET_NUM_ERROR.getIndex());
            result.setMsg(ErrorCode.ERROR_MAKE_MARKET_NUM_ERROR.getMessage());
            result.setCode(Result.Code.ERROR);
            result.setData(null);
            return result;
        }


        price = price.setScale(yangCurrencyPair.getInputPriceNum(), BigDecimal.ROUND_DOWN);

        num = num.setScale(yangCurrencyPair.getShowNum(),BigDecimal.ROUND_DOWN);

        //8 用户资产是否充足
        YangCurrencyUser yangCurrencyUser=new YangCurrencyUser();
        if(type.equals(CoinConst.ORDER_BUY))
        {
            yangCurrencyUser.setCurrencyId(yangCurrencyPair.getCurrencyTradeId()); //如果是买的话 对应用户
        }else{
            yangCurrencyUser.setCurrencyId(yangCurrencyPair.getCurrencyId());
        }
        yangMember = yangMemberTokenService.getSpecAssetsForMarektMake(yangMember,yangCurrencyUser);
        BigDecimal lefts;
        if(type.equals(CoinConst.ORDER_SELL))
        {
            lefts = yangMember.getYangCurrencyUserList().get(0).getNum();
            if(lefts.compareTo(num) < 0)
            {
                result.setErrorCode(ErrorCode.ERROR_MAKE_MARKET_NO_LEFTNUM.getIndex());
                result.setMsg(ErrorCode.ERROR_MAKE_MARKET_NO_LEFTNUM.getMessage());
                result.setCode(Result.Code.ERROR);
                result.setData(null);
                return result;
            }
        }else{
            if(yangCurrencyPair.getCurrencyTradeId().equals(CoinConst.KRW_CURRENCY_TRADE_ID))
            {
                lefts =   yangMember.getRmb();
            }else{
                lefts = yangMember.getYangCurrencyUserList().get(0).getNum();
            }
            if(lefts.compareTo(num.multiply(price)) < 0)
            {
                result.setErrorCode(ErrorCode.ERROR_MAKE_MARKET_NO_LEFTNUM.getIndex());
                result.setMsg(ErrorCode.ERROR_MAKE_MARKET_NO_LEFTNUM.getMessage());
                result.setCode(Result.Code.ERROR);
                result.setData(null);
                return result;
            }
        }

        // 开始挂单
        YangMember ym = new YangMember();
        ym.setRmbType("dec");
        ym.setForzenRmbType("inc");

        BigDecimal priceNums;
        if(type.equals(CoinConst.ORDER_BUY))
        {
            priceNums = price.multiply(num);
        }else{
            priceNums = num;
        }

        yangCurrencyUser.setNumType("dec");
        yangCurrencyUser.setForzenNumType("inc");
        yangCurrencyUser.setNum(priceNums);
        yangCurrencyUser.setForzenNum(priceNums);

        ym.setForzenRmb(priceNums);
        ym.setRmb(priceNums);

        ym.setMemberId(memberId);

        Result result1 = yangMemberTokenService.updateAssetsForMarkerMake(yangCurrencyUser,ym);

        //开始挂单
        int id=robotService.guadanForMarketMake(num,price,type,yangCurrencyPair,ym,"0");

        System.out.println("挂单id"+id);
        result.setData(null);
        result.setCode(Result.Code.SUCCESS);
        return result;
    }

    /**
     * 查询挂单列表
     * @param cId
     * @param begin
     * @param end
     * @param type
     * @param key
     * @param signature
     * @param nonce
     * @return
     */
    public Result queryOrderList(String cId,String begin,String end,String type,String key,String signature,String nonce){
        Result result=new Result();
        //1 密钥不存在
        result=checkKey(key);
        if(Result.Code.ERROR==result.getCode().intValue()){
            return result;
        }
        Integer memberId=((YangUserKeys)result.getData()).getMemberId();
        String privateKey=((YangUserKeys)result.getData()).getPrivateKey();
        //2 验证签名
        Map sb=new HashMap();
        sb.put("key",key);
        sb.put("cyInfo",cId.toLowerCase());
        sb.put("begin", StringUtils.isNotEmpty(begin)?begin:"");
        sb.put("end",StringUtils.isNotEmpty(end)?end:"");
        sb.put("type",type);
        sb.put("nonce",nonce);
        result=checkSignature(ApiSignatureUtils.createLinkString(sb),signature,privateKey);
        if(Result.Code.ERROR==result.getCode().intValue()){
            return result;
        }
        //3 验证交易对是否存在
        result= checkCurrencyPair(cId);
        if(Result.Code.ERROR==result.getCode().intValue()){
            return result;
        }
        YangCurrencyPair yangCurrencyPair=(YangCurrencyPair)result.getData();

        //4 查询订单数据
        Example example=new Example(YangOrders.class);
        example.setOrderByClause("add_time desc limit 500");
        Example.Criteria criteria=  example.createCriteria();
        criteria .andEqualTo("currencyId",yangCurrencyPair.getCurrencyId())
                .andEqualTo("currencyTradeId",yangCurrencyPair.getCurrencyTradeId())
                .andEqualTo("memberId",memberId).andEqualTo("type",type);

        if(StringUtils.isNotEmpty(end)&&StringUtils.isNotEmpty(begin)){
            criteria.andGreaterThanOrEqualTo("addTime",begin).andLessThanOrEqualTo("addTime",end);
        }else{
            if(StringUtils.isNotEmpty(begin)){
                criteria.andGreaterThanOrEqualTo("addTime",begin);
            }
            if(StringUtils.isNotEmpty(end)){
                criteria.andLessThanOrEqualTo("addTime",end);
            }
        }
        List<YangOrders> list=yangOrdersMapper.selectByExample(example);
        result.setCode(Result.Code.SUCCESS);
        result.setData(list==null?new ArrayList<YangOrders>():list);
        return  result;
    }


    /**
     * 查询交易列表
     * @param cId
     * @param begin
     * @param end
     * @param type
     * @param key
     * @param signature
     * @param nonce
     * @return
     */
    public Result queryTradeList(String cId,String begin,String end,String type,String key,String signature,String nonce){
        Result result=new Result();
        //1 密钥不存在
        result=checkKey(key);
        if(Result.Code.ERROR==result.getCode().intValue()){
            return result;
        }
        Integer memberId=((YangUserKeys)result.getData()).getMemberId();
        String privateKey=((YangUserKeys)result.getData()).getPrivateKey();
        //2 验证签名
        Map sb=new HashMap();
        sb.put("key",key);
        sb.put("cyInfo",cId.toLowerCase());
        sb.put("begin",StringUtils.isNotEmpty(begin)?begin:"");
        sb.put("end",StringUtils.isNotEmpty(end)?end:"");
        sb.put("type",type);
        sb.put("nonce",nonce);
        result=checkSignature(ApiSignatureUtils.createLinkString(sb),signature,privateKey);
        if(Result.Code.ERROR==result.getCode().intValue()){
            return result;
        }
        //3 验证交易对是否存在
        result= checkCurrencyPair(cId);
        if(Result.Code.ERROR==result.getCode().intValue()){
            return result;
        }
        YangCurrencyPair yangCurrencyPair=(YangCurrencyPair)result.getData();

        //4 查询订单数据
        Query query=new Query();
        query.addCriteria(Criteria.where("currencyId").is(yangCurrencyPair.getCurrencyId())).
                addCriteria(Criteria.where("currencyTradeId").is(yangCurrencyPair.getCurrencyTradeId())).
                addCriteria(Criteria.where("memberId").is(memberId));
        if(StringUtils.isNotEmpty(begin)&&StringUtils.isNotEmpty(end)){
            query.addCriteria(Criteria.where("addTime").gte(Integer.valueOf(begin)).lte(Integer.valueOf(end)));
        }else{
            if(StringUtils.isNotEmpty(begin)){
                query.addCriteria(Criteria.where("addTime").gte(Integer.valueOf(begin)));
            }
            if(StringUtils.isNotEmpty(end)){
                query.addCriteria(Criteria.where("addTime").lte(Integer.valueOf(end)));
            }
        }
        query.with(new Sort(new Sort.Order(Sort.Direction.DESC,"addTime"))).limit(500);
        List<MgYangTrade> sourceList= this.mongoTemplate.find(query,MgYangTrade.class,CoinConst.MONGODB_TRADE_COLLECTION);

        result.setCode(Result.Code.SUCCESS);
        result.setData(sourceList==null?new ArrayList<YangTrade>():sourceList);
        return  result;
    }


    /**
     * 撤单
     * @param ordersId
     * @param key
     * @param signature
     * @param nonce
     * @return
     */
    public Result cheOrder(String ordersId,String key,String signature,String nonce){
        Result result=new Result();
        //1 密钥不存在
        result=checkKey(key);
        if(Result.Code.ERROR==result.getCode().intValue()){
            return result;
        }
        Integer memberId=((YangUserKeys)result.getData()).getMemberId();
        String privateKey=((YangUserKeys)result.getData()).getPrivateKey();
        //2 验证签名
        Map sb=new HashMap();
        sb.put("key",key);
        sb.put("ordersId",ordersId);
        sb.put("nonce",nonce);
        result=checkSignature(ApiSignatureUtils.createLinkString(sb),signature,privateKey);
        if(Result.Code.ERROR==result.getCode().intValue()){
            return result;
        }
        //3 撤单
        YangOrders yangOrders=new YangOrders();
        yangOrders.setOrdersId(Integer.valueOf(ordersId));
        //判断订单hasDo状态是否为1
        YangOrders nowYangorder=yangOrdersMapper.selectByPrimaryKey(yangOrders);
        if(1!=nowYangorder.getHasDo().intValue()){
            result.setCode(Result.Code.ERROR);
            result.setErrorCode(ErrorCode.SEND_EMAILCODE_ERROR.getIndex());
            result.setMsg(ErrorCode.SEND_EMAILCODE_ERROR.getMessage());
            return result;
        }
        yangOrders.setStatus(CoinConst.ORDER_STATUS_CANCEL_ING);
        yangOrdersMapper.updateByPrimaryKeySelective(yangOrders);
        //调用 撤单存储过程
        Map map=yangOrderService.dealOrder(
                0,null,null,null,null,0,
                0,null,null,null,null,0,
                0,null,null,null,null,0,
                0,null,null,null,null,0,
                0,null,null,null,null,0,

                0,null,0,
                0,null,0,
                null,0,0,0,null,null,null,null,null,0,0,0,0,
                null,0,0,0,null,null,null,null,null,0,0,0,0,
                0,0,0,null,0,0,null,null,
                0,0,0,null,0,0,null,null,
                Integer.valueOf(ordersId).intValue(),
                CoinConst.MYSQL_GC_DEAL_TYPE_CANCEL

        );
        Integer a=(Integer)map.get("res");
        if(a == 1){
            //设置订单状态为异常
            YangOrders yangOrdersa=new YangOrders();
            yangOrdersa.setOrdersId(Integer.valueOf(ordersId));
            yangOrdersa.setStatus(CoinConst.ORDER_STATUS_ERROR);
            yangOrdersMapper.updateByPrimaryKeySelective(yangOrdersa);
            result.setCode(Result.Code.ERROR);
            result.setErrorCode(ErrorCode.ERROR_ORDER_UNNORMAL.getIndex());
            result.setMsg(ErrorCode.ERROR_ORDER_UNNORMAL.getMessage());
        }else{
            result.setCode(Result.Code.SUCCESS);
        }
        return  result;
    }

    /**
     * 查询资产
     * @param key
     * @param signature
     * @param nonce
     * @return
     */
    public Result queryAsset(String key,String signature,String nonce){
        Result result=new Result();
        //1 密钥不存在
        result=checkKey(key);
        if(Result.Code.ERROR==result.getCode().intValue()){
            return result;
        }
        Integer memberId=((YangUserKeys) result.getData()).getMemberId();
        String privateKey=((YangUserKeys)result.getData()).getPrivateKey();
        //2 验证签名
        Map sb=new HashMap();
        sb.put("key",key);
        sb.put("nonce",nonce);
        result=checkSignature(ApiSignatureUtils.createLinkString(sb),signature,privateKey);
        if(Result.Code.ERROR==result.getCode().intValue()){
            return result;
        }
        //获取所有资产
            //韩元
            Example example=new Example(YangMember.class);
            example.createCriteria().andEqualTo("memberId",memberId);
            List<YangMember> list= yangMemberMapper.selectByExample(example);

            //查询 资产 列表
           Map param=new HashMap();
           param.put("memberId",memberId);
            List<YangCurrencyUser> lista=yangCurrencyUserMapper.getAllCurrencyList(param);
            lista= lista==null?new ArrayList<YangCurrencyUser>():lista;


            YangCurrencyUser krw=new YangCurrencyUser();
            krw.setCurrencyId(CoinConst.KRW_CURRENCY_TRADE_ID);
            krw.setNum(list.get(0).getRmb());
            krw.setCurrencyMark(CoinConst.KRW_CURRENCY_TRADE_STR);
            krw.setForzenNum(list.get(0).getForzenRmb());
            lista.add(krw);
            result.setCode(Result.Code.SUCCESS);
            result.setData(lista);
        return  result;
    }

    //验证公钥
    public Result checkKey(String key){
        Result result=new Result();
        result.setCode(Result.Code.SUCCESS);
        Example example=new Example(YangUserKeys.class);
        example.createCriteria().andEqualTo("publicKey",key);
        List<YangUserKeys> list= yangUserKeysMapper.selectByExample(example);
        //1 密钥不存在
        if(list==null||list.size()==0){
            result.setCode(Result.Code.ERROR);
            result.setErrorCode(ErrorCode.ERROR_MAKE_MARKET_NO_KEY.getIndex());
            result.setMsg(ErrorCode.ERROR_MAKE_MARKET_NO_KEY.getMessage());
            return result;
        }
        result.setData(list.get(0));
        return result;
    }
    //验证交易对是否存在
    public Result checkCurrencyPair(String cId){
        Result result=new Result();
        String[] ary=cId.split("_");
        if(ary.length!=2){
            result.setCode(Result.Code.ERROR);
            result.setErrorCode(ErrorCode.ERROR_MAKE_MARKET_NO_CURRENCY.getIndex());
            result.setMsg(ErrorCode.ERROR_MAKE_MARKET_NO_CURRENCY.getMessage());
            return  result;
        }
        //查询 市场和币种Id
        YangCurrencyMarket yangCurrencyMarket=new YangCurrencyMarket();
        yangCurrencyMarket.setTitle(ary[1]);
        Example example=new Example(YangCurrencyMarket.class);
        example.createCriteria().andEqualTo("title",ary[1].toUpperCase());
        List<YangCurrencyMarket> marketList= yangCurrencyMarketMapper.selectByExample(example);

        Example example1=new Example(YangCurrency.class);
        example1.createCriteria().andEqualTo("currencyMark",ary[0].toUpperCase());
         List<YangCurrency> currencyList= yangCurrencyMapper.selectByExample(example1);
        if(marketList==null||marketList.size()==0||currencyList==null||currencyList.size()==0){
            result.setCode(Result.Code.ERROR);
            result.setErrorCode(ErrorCode.ERROR_MAKE_MARKET_NO_CURRENCY.getIndex());
            result.setMsg(ErrorCode.ERROR_MAKE_MARKET_NO_CURRENCY.getMessage());
            return  result;
        }
        //查询交易对
        Example example2=new Example(YangCurrencyPair.class);
        example2.createCriteria().andEqualTo("currencyId",currencyList.get(0).getCurrencyId())
                .andEqualTo("currencyTradeId",marketList.get(0).getCurrencyId());
        List<YangCurrencyPair> pairList=yangCurrencyPairMapper.selectByExample(example2);

        if(pairList==null||pairList.size()==0){
            result.setCode(Result.Code.ERROR);
            result.setErrorCode(ErrorCode.ERROR_MAKE_MARKET_NO_CURRENCY.getIndex());
            result.setMsg(ErrorCode.ERROR_MAKE_MARKET_NO_CURRENCY.getMessage());
            return  result;
        }
        result.setCode(Result.Code.SUCCESS);
        result.setData(pairList.get(0));
        return result;
    }

    //验证签名是否一致
    public Result checkSignature(String signaturBuff,String signature,String key){
        Result result=new Result();
        try {
            String signature_local=ApiSignatureUtils.sha256_make(key,signaturBuff.toString());
            if(!signature.equals(signature_local)){
                result.setCode(Result.Code.ERROR);
                result.setErrorCode(ErrorCode.ERROR_MAKE_MARKET_ERROR_SIGN.getIndex());
                result.setMsg(ErrorCode.ERROR_MAKE_MARKET_ERROR_SIGN.getMessage());
                return result;
            }
        }catch (Exception e){
            result.setCode(Result.Code.ERROR);
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
            result.setMsg(ErrorCode.ERROR_SYSTEM.getMessage());
            e.printStackTrace();
        }
        result.setCode(Result.Code.SUCCESS);
        return result;
    }
}
