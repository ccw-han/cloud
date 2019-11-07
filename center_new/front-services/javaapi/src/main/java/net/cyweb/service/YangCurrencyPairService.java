package net.cyweb.service;

import cyweb.utils.CoinConst;
import cyweb.utils.DateUtils;
import cyweb.utils.DecimalUtils;
import cyweb.utils.ErrorCode;
import net.cyweb.config.custom.Base64Utils;
import net.cyweb.config.custom.EmailUtils;
import net.cyweb.config.custom.GetDataUtils;
import net.cyweb.config.custom.RevertCNYUtils;
import net.cyweb.mapper.YangCurrencyPairMapper;
import net.cyweb.mapper.YangOrdersMapper;
import net.cyweb.mapper.YangTradeMapper;
import net.cyweb.model.Result;
import net.cyweb.model.UserProPs;
import net.cyweb.model.YangCurrencyPair;
import net.cyweb.model.YangTrade;
import net.cyweb.mapper.*;
import net.cyweb.model.*;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.math.BigDecimal;
import java.util.*;

@Service
public class YangCurrencyPairService extends BaseService<YangCurrencyPair> {

    @Autowired
    private YangCurrencyPairMapper yangCurrencyPairMapper;

    @Autowired
    private RedisService redisService;

    @Autowired
    private UserProPs userProPs;

    @Autowired
    private YangTradeMapper yangTradeMapper;

    @Autowired
    private RevertCNYUtils revertCNYUtils;
    @Autowired
    private YangMemberTokenService yangMemberTokenService;
    @Autowired
    private YangC2CGuaMapper yangC2CGuaMapper;
    @Autowired
    private YangC2CAssetMapper yangC2CAssetMapper;
    @Autowired
    private YangC2COrdersMapper yangC2COrdersMapper;

    /**
     * 获取行情列表
     *
     * @param ids
     * @param searchText
     * @param isMine
     * @param currencyTradeId
     * @return
     */
    public Result getCurrencyListChange(String ids, String searchText, String isMine, String currencyTradeId) {
        Result result = new Result();
        result.setCode(Result.Code.SUCCESS);
        try {
            Map map = new HashMap();
            if (StringUtils.isNotEmpty(ids)) {
                String[] arys = ids.split(",");
                map.put("ids", arys);
            }
            map.put("searchText", searchText);
            map.put("isMine", isMine);
            map.put("currencyId", CoinConst.FUNT_CURRENCY_ID);
            map.put("isLine", CoinConst.CURRENCY_IS_LINE_YES);
            map.put("currencyTradeId", currencyTradeId);
            List<Map> list = yangCurrencyPairMapper.getCurrencyListChange(map);
            List<Map> resultList = new ArrayList<Map>();
            for (Map a : list) {
                String redisKey = CoinConst.REDIS_COIN_PAIR_BASE_ + a.get("currencyId") + "-" + a.get("currencyTradeId");
                Map mapa;
                if (!redisService.exists(redisKey)) {
                    mapa = new HashMap<String, Object>();
                    modifyMapInfo(mapa, a);
                    resultList.add(mapa);
                    continue;
                } else {
                    mapa = initCoinRedisBean(redisKey);
                    if (mapa == null || Integer.valueOf((String) mapa.get("cy_id")).intValue() == 0) {
                        modifyMapInfo(mapa, a);
                    }
                }
                String redisKyeB = CoinConst.REDIS_YANG_PARI_DAY_INFO_ + a.get("currencyId") + "-" + a.get("currencyTradeId");
                Map mapb = null;
                if (redisService.exists(redisKyeB)) {
                    mapb = initCoinRedisBeanB(redisKyeB);
                    mapa.putAll(mapb);
                }
                //非韩元区 币种  转换价格
                if (((Long) a.get("currencyTradeId")).intValue() != CoinConst.KRW_CURRENCY_TRADE_ID) {
                    if (mapb == null) {
                        mapa.put("krw_price", 0);
                    } else {
                        String redisKeyKrwPrice = CoinConst.REDIS_COIN_PAIR_BASE_ + a.get("currencyTradeId") + "-" + CoinConst.KRW_CURRENCY_TRADE_ID;
                        if (redisService.exists(redisKeyKrwPrice)) {
                            mapa.put("krw_price", initCoinRedisBean(redisKeyKrwPrice).get("new_price"));
                        } else {
                            mapa.put("krw_price", 0);
                        }

                    }
                } else {
                    mapa.put("krw_price", 1);
                }
                shareMapContent(mapa, a);
                mapa.put("new_price", mapa.containsKey("new_price") ? mapa.get("new_price") : 0);
                mapa.put("krw_price", mapa.containsKey("krw_price") ? mapa.get("krw_price") : 0);
                mapa.put("price_status", mapa.containsKey("price_status") ? mapa.get("price_status") : 0);
                mapa.put("h24_change", mapa.containsKey("h24_change") ? getFormtNums(2, (String) mapa.get("h24_change")) : "0.00");
                mapa.put("h24_num", mapa.containsKey("h24_num") ? mapa.get("h24_num") : 0);
                mapa.put("h24_money", mapa.containsKey("h24_money") ? mapa.get("h24_money") : 0);
                mapa.put("h24_max", mapa.containsKey("h24_max") ? mapa.get("h24_max") : 0);
                mapa.put("h24_min", mapa.containsKey("h24_min") ? mapa.get("h24_min") : 0);
                resultList.add(mapa);
            }
            String rootPath = userProPs.getFtp().get("rootPath");
            String oldReadHost = userProPs.getFtp().get("oldReadHost");
            for (Map resultMap : resultList) {
                String path = (String) resultMap.get("currency_logo");
                String tradePath = (String) resultMap.get("trade_logo");
                Base64Utils.getRealImageUrl(path, oldReadHost, rootPath, resultMap, "currency_logo");
                Base64Utils.getRealImageUrl(tradePath, oldReadHost, rootPath, resultMap, "trade_logo");
            }
            result.setData(resultList);
        } catch (Exception e) {
            e.printStackTrace();
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
            result.setCode(Result.Code.ERROR);
            result.setMsg(ErrorCode.ERROR_SYSTEM.getMessage());
        }
        return result;
    }

    public Result getCurrencyDetailChange(String currencyId, String currencyTradeId, String cyId) {
        Result result = new Result();
        result.setCode(Result.Code.SUCCESS);
        try {
            Map map = new HashMap();
            map.put("currencyId", currencyId);
            map.put("currencyTradeId", currencyTradeId);
            map.put("cyId", cyId);
            List<Map> list = yangCurrencyPairMapper.getCurrencyDetailChange(map);
            List<Map> resultList = new ArrayList<Map>();
            for (Map a : list) {
                String redisKey = CoinConst.REDIS_COIN_PAIR_BASE_ + a.get("currencyId") + "-" + a.get("currencyTradeId");
                Map mapa;
                if (!redisService.exists(redisKey)) {
                    mapa = new HashMap<String, Object>();
                    modifyMapInfo(mapa, a);
                    resultList.add(mapa);
                    continue;
                } else {
                    mapa = initCoinRedisBean(redisKey);
                    if (mapa == null || Integer.valueOf((String) mapa.get("cy_id")).intValue() == 0) {
                        modifyMapInfo(mapa, a);
                    }
                }
                String redisKyeB = CoinConst.REDIS_YANG_PARI_DAY_INFO_ + a.get("currencyId") + "-" + a.get("currencyTradeId");
                Map mapb = null;
                if (redisService.exists(redisKyeB)) {
                    mapb = initCoinRedisBeanB(redisKyeB);
                    mapa.putAll(mapb);
                }
                //非韩元区 币种  转换价格
                if (((Long) a.get("currencyTradeId")).intValue() != CoinConst.KRW_CURRENCY_TRADE_ID) {

                    String redisKeyKrwPrice = CoinConst.REDIS_COIN_PAIR_BASE_ + a.get("currencyTradeId") + "-" + CoinConst.KRW_CURRENCY_TRADE_ID;
                    if (redisService.exists(redisKeyKrwPrice)) {
                        mapa.put("krw_price", initCoinRedisBean(redisKeyKrwPrice).get("new_price"));
                    } else {
                        mapa.put("krw_price", 0);
                    }

                } else {
                    mapa.put("krw_price", 1);
                }
                shareMapContent(mapa, a);
                mapa.put("new_price", mapa.containsKey("new_price") ? mapa.get("new_price") : 0);
                mapa.put("krw_price", mapa.containsKey("krw_price") ? mapa.get("krw_price") : 0);
                mapa.put("price_status", mapa.containsKey("price_status") ? mapa.get("price_status") : 0);
                mapa.put("h24_change", mapa.containsKey("h24_change") ? getFormtNums(2, (String) mapa.get("h24_change")) : "0.00");
                mapa.put("h24_num", mapa.containsKey("h24_num") ? mapa.get("h24_num") : 0);
                mapa.put("h24_money", mapa.containsKey("h24_money") ? mapa.get("h24_money") : 0);
                mapa.put("h24_max", mapa.containsKey("h24_max") ? mapa.get("h24_max") : 0);
                mapa.put("h24_min", mapa.containsKey("h24_min") ? mapa.get("h24_min") : 0);
                resultList.add(mapa);
            }
            Map resultMap = new HashMap();
            if (resultList != null && resultList.size() > 0) {
                resultMap = resultList.get(0);
                if (resultMap != null) {
                    String path = (String) resultMap.get("currency_logo");
                    String tradePath = (String) resultMap.get("trade_logo");
                    String rootPath = userProPs.getFtp().get("rootPath");
                    String oldReadHost = userProPs.getFtp().get("oldReadHost");
                    Base64Utils.getRealImageUrl(path, oldReadHost, rootPath, resultMap, "currency_logo");
                    Base64Utils.getRealImageUrl(tradePath, oldReadHost, rootPath, resultMap, "trade_logo");
                }
            }
            result.setData(resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
            result.setCode(Result.Code.ERROR);
            result.setMsg(ErrorCode.ERROR_SYSTEM.getMessage());
        }
        return result;
    }


    public void modifyMapInfo(Map mapa, Map a) {
        shareMapContent(mapa, a);
        mapa.put("new_price", 0);
        mapa.put("krw_price", 0);
        mapa.put("price_status", 0);
        mapa.put("h24_change", "0.00");
        mapa.put("h24_num", 0);
        mapa.put("h24_money", 0);
        mapa.put("h24_max", 0);
        mapa.put("h24_min", 0);
    }

    public void shareMapContent(Map mapa, Map a) {
        mapa.put("cy_id", a.get("cyId") == null ? 0 : a.get("cyId"));
        mapa.put("is_old", a.get("isOld") == null ? 0 : a.get("isOld"));
        mapa.put("currency_id", a.get("currencyId") == null ? 0 : a.get("currencyId"));
        mapa.put("currency_trade_id", a.get("currencyTradeId") == null ? 0 : a.get("currencyTradeId"));
        mapa.put("currency_logo", a.get("currencyLogo") == null ? 0 : a.get("currencyLogo"));
        mapa.put("trade_logo", a.get("tradeLogo") == null ? 0 : a.get("tradeLogo"));
        mapa.put("input_price_num", a.get("inputPriceNum") == null ? 0 : a.get("inputPriceNum"));
        mapa.put("rate_num", a.get("rateNum") == null ? 0 : a.get("rateNum"));
        mapa.put("currency_mark", a.get("currencyMark") == null ? 0 : a.get("currencyMark"));
        mapa.put("trade_mark", a.get("tradeMark") == null ? 0 : a.get("tradeMark"));
        mapa.put("is_chuang", a.get("isChuang") == null ? 0 : a.get("isChuang"));
        mapa.put("order_num", a.get("orderNum") == null ? 0 : a.get("orderNum"));
        mapa.put("show_num", a.get("showNum") == null ? 0 : a.get("showNum"));
        mapa.put("add_time", a.get("addTime") == null ? 0 : a.get("addTime"));
        mapa.put("detail_id", a.get("detailId") == null ? 0 : a.get("detailId"));
        mapa.put("currency_buy_fee", a.get("currencyBuyFee") == null ? 0 : a.get("currencyBuyFee"));
        mapa.put("currency_sell_fee", a.get("currencySellFee") == null ? 0 : a.get("currencySellFee"));
        mapa.put("all_money_num", a.get("allMoneyNum") == null ? 0 : a.get("allMoneyNum"));
    }


    public Map initCoinRedisBean(String key) {
        Map map = new HashMap();
        Map a = redisService.hashGetAll(key);
        map.put("cy_id", a.get("cy_id") == null ? 0 : a.get("cy_id").toString());
        map.put("currency_mark", a.get("currency_mark") == null ? 0 : a.get("currency_mark").toString());
        map.put("trade_logo", a.get("trade_logo") == null ? 0 : a.get("trade_logo").toString());
        map.put("currency_id", a.get("currency_id") == null ? 0 : a.get("currency_id").toString());
        map.put("trade_mark", a.get("trade_mark") == null ? 0 : a.get("trade_mark").toString());
        map.put("currency_trade_id", a.get("currency_trade_id") == null ? 0 : a.get("currency_trade_id").toString());
        map.put("price_status", a.get("price_status") == null ? 0 : a.get("price_status").toString());
        map.put("currency_logo", a.get("currency_logo") == null ? 0 : a.get("currency_logo").toString());
        map.put("new_price", a.get("new_price") == null ? 0 : a.get("new_price").toString());
        return map;
    }

    public Map initCoinRedisBeanB(String key) {
        Map map = new HashMap();
        Map a = redisService.hashGetAll(key);
        map.put("h24_num", a.get("h24_num") == null ? 0 : a.get("h24_num").toString());
        map.put("h24_change", a.get("h24_change") == null ? "0.00" : getFormtNums(2, a.get("h24_change").toString()));
        map.put("h24_money", a.get("h24_money") == null ? 0 : a.get("h24_money").toString());
        map.put("h24_max", a.get("h24_max") == null ? 0 : a.get("h24_max").toString());
        map.put("h24_min", a.get("h24_min") == null ? 0 : a.get("h24_min").toString());
        map.put("first_price", a.get("first_price") == null ? 0 : a.get("first_price").toString());
        map.put("new_price", a.get("new_price") == null ? 0 : a.get("new_price").toString());
        map.put("price_status", a.get("price_status") == null ? 0 : a.get("price_status").toString());
        return map;
    }

    public Result getCurrencyPairOrder(String type, String cyId) {
        Result result = new Result();
        try {
            if (redisService.exists(CoinConst.ORDER2PAIR + cyId + "_" + type)) {
                List<Object> list = redisService.lRange(CoinConst.ORDER2PAIR + cyId + "_" + type, 0, 23L);
                result.setCode(Result.Code.SUCCESS);
                result.setData(list);
            } else {
                result.setCode(Result.Code.SUCCESS);
                result.setData(new ArrayList<Map>());
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
            result.setCode(Result.Code.ERROR);
            result.setMsg(ErrorCode.ERROR_SYSTEM.getMessage());
        }
        return result;
    }

    public Result getCurrencyPairTrade(String cyId) {
        Result result = new Result();
        try {
            if (redisService.exists(CoinConst.TRADE2PAIR + cyId)) {
                Long length = redisService.listSize(CoinConst.TRADE2PAIR + cyId);
                List<Object> list = redisService.lRange(CoinConst.TRADE2PAIR + cyId, length - 23, length);
                result.setCode(Result.Code.SUCCESS);
                result.setData(list);
            } else {
                result.setCode(Result.Code.SUCCESS);
                result.setData(new ArrayList<Map>());
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
            result.setCode(Result.Code.ERROR);
            result.setMsg(ErrorCode.ERROR_SYSTEM.getMessage());
        }
        return result;
    }

    public Result getCurrencyPair(String cyId, String currencyId) {
        Result result = new Result();
        try {
            Map map = new HashMap();
            if (StringUtils.isNotEmpty(cyId)) {
                map.put("cyId", cyId);
            }
            if (StringUtils.isNotEmpty(currencyId)) {
                map.put("currencyId", currencyId);
            }
            List<Map> list = yangCurrencyPairMapper.getCurrencyPairList(map);
            result.setCode(Result.Code.SUCCESS);
            result.setData(list);
        } catch (Exception e) {
            e.printStackTrace();
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
            result.setCode(Result.Code.ERROR);
            result.setMsg(ErrorCode.ERROR_SYSTEM.getMessage());
        }
        return result;
    }

    public String getFormtNums(int format, String o) {
        BigDecimal decimal = new BigDecimal(o);
        decimal = decimal.setScale(format, BigDecimal.ROUND_HALF_UP);
        return decimal.toPlainString();
    }

    /**
     * 获取所有行情
     * 耶耶
     *
     * @param map
     */
    public Result getCurrencyListChanges(Map<String, String> map) {
        Result result = new Result();
        String aa = map.get("currencyTradeId");
        if (StringUtils.isBlank(aa)){
            result.setMsg("参数格式错误");
            result.setCode(0);
            return result;
        }
        aa.replace(" ","");
        Long currencyTradeId = Long.valueOf(aa);
        //总数据
        //Map<String,String> dataMap11 = new HashMap<>();
        List<Map<String, String>> list = new ArrayList<>();
        //获取当前大区下所有的交易对
        List<Map> currencyList = yangCurrencyPairMapper.getCurrencyListChanges(map);
        if (currencyList != null && currencyList.size() != 0) {
            for (Map map1 : currencyList) {
                try {
                    //单条数据
                    Map<String, String> dataMap = new HashMap<>();
                    Long currencyId = (Long) map1.get("currency_id");
                    String cnyMaxPrice = revertCNYUtils.revertCNYPrice("", Integer.parseInt(currencyId + ""));
                    //获取当前交易对的所有订单进行数据封装
                    //最新价
                    Map newPrice = null;
                    newPrice = yangTradeMapper.getNewPrice(currencyId, currencyTradeId);
                    if (newPrice != null) {
                        Object object = newPrice.get("price");
                        //String price = (String) newPrice.get("price");
                        BigDecimal bigDecimal = new BigDecimal(object.toString());
                        dataMap.put("price", bigDecimal.toPlainString());
                        //最新价约等于多少人民币
                        if (!"0".equals(cnyMaxPrice)) {
                            String newPrice1 = DecimalUtils.multiplicationForNum(newPrice.get("price").toString(), cnyMaxPrice);
                            dataMap.put("newPriceForCNY", newPrice1);
                        } else {
                            dataMap.put("newPriceForCNY", "0");
                        }
                    } else {
                        dataMap.put("price", "0");
                        dataMap.put("newPriceForCNY", "0");
                    }


                    //涨跌幅
                    Map priceLimit = yangTradeMapper.getPriceLimit(currencyId, currencyTradeId,
                            GetDataUtils.getDayBegin());
                    if (priceLimit != null) {
                        Object price = newPrice.get("price");
                        Object price1 = priceLimit.get("price");
                        String priceLimit1 = DecimalUtils.subtraction(price.toString(), price1.toString());
                        String division = DecimalUtils.division(priceLimit1, price1.toString(), 6);
                        String multiplication = DecimalUtils.multiplication(division, "100");
                        dataMap.put("priceLimit", multiplication + "%");
                    } else {
                        dataMap.put("priceLimit", "00.00%");
                    }
                    //最高价
                    Map maxprice = yangTradeMapper.getMaxPrice(currencyId, currencyTradeId, GetDataUtils.getDayBegin());
                    if (maxprice != null) {
                        Object maxPrice = maxprice.get("maxPrice");
                        dataMap.put("maxPrice", maxPrice.toString());
                        //最高价约等于多少人民币
                        if (!"0".equals(cnyMaxPrice)) {
                            String maxPrice1 = DecimalUtils.multiplicationForNum(maxprice.get("maxPrice").toString(), cnyMaxPrice);
                            dataMap.put("maxPriceForCNY", maxPrice1);
                        } else {
                            dataMap.put("maxPriceForCNY", "0");
                        }
                    } else {
                        dataMap.put("maxPrice", "0");
                        dataMap.put("maxPriceForCNY", "0");
                    }

                    //最低价
                    Map minprice = yangTradeMapper.getMinPrice(currencyId, currencyTradeId, GetDataUtils.getDayBegin());
                    if (minprice != null) {
                        Object minPrice = minprice.get("minPrice");
                        dataMap.put("minPrice", minPrice.toString());
                        //最低价约等于多少人民币
                        if (!"0".equals(cnyMaxPrice)) {
                            String newPrice1 = DecimalUtils.multiplicationForNum(minprice.get("minPrice").toString(), cnyMaxPrice);
                            dataMap.put("minPriceForCNY", newPrice1);
                        } else {
                            dataMap.put("minPriceForCNY", "0");
                        }
                    } else {
                        dataMap.put("minPrice", "0");
                        dataMap.put("minPriceForCNY", "0");
                    }


                    //获取24小时成交额(钱)
                    Map vol = yangTradeMapper.get24VOL(currencyId, currencyTradeId, GetDataUtils.getDayBegin());
                    if (vol != null) {
                        dataMap.put("money", vol.get("money").toString());
                        //24小时成交额约等于多少人民币
                        if (!"0".equals(cnyMaxPrice)) {
                            String newPrice1 = DecimalUtils.multiplicationForNum((String) vol.get("money").toString(), cnyMaxPrice);
                            dataMap.put("moneyForCNY", newPrice1);
                        } else {
                            dataMap.put("moneyForCNY", "0");
                        }
                    } else {
                        dataMap.put("money", "0");
                        dataMap.put("moneyForCNY", "0");
                    }

                    //获取24小时成交量
                    Map TurnoverL = yangTradeMapper.get24TurnoverL(currencyId, currencyTradeId, GetDataUtils.getDayBegin());
                    if (TurnoverL != null) {
                        dataMap.put("num", TurnoverL.get("num").toString());
                    } else {
                        dataMap.put("num", "0");
                    }
                    dataMap.put("currencyMark", map1.get("currency_mark").toString());
                    list.add(dataMap);
                } catch (Exception e) {
                    e.printStackTrace();
                    result.setCode(0);
                    result.setMsg("查询失败");
                    return result;
                }
            }
        }

        /*List<Map.Entry<String,String>> list = new ArrayList<Map.Entry<String,String>>(dataMap11.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<String, String>>() {
            @Override
            public int compare(Map.Entry<String, String> o1, Map.Entry<String, String> o2) {

                if ("BTC".equals(o1.getKey())){
                    return 1;
                }
                if ("ETH".equals(o1.getKey())){
                    return 2;
                }
                if ("ETC".equals(o1.getKey())){
                    return 3;
                }
                return o1.getKey().compareTo(o2.getKey());
            }
        });*/
        result.setCode(1);
        result.setMsg("查询成功");
        //System.out.println(list.toString());
        result.setData(list);
        return result;
    }

    /**
     * 获取首页涨幅榜
     * 耶耶
     *
     * @return
     */
    public Result getUpCurrencyInfos() {
        Result result = new Result();
        result.setCode(0);
        result.setMsg("查询失败");
        //获取当前大区下所有的交易对
        Map<String, String> map = new HashMap<>();
        List<Map> currencyList = yangCurrencyPairMapper.getCurrencyListChanges(map);
        //总数据
        //Map<String,String> dataMap11 = new HashMap<>();
        List<Map<String, String>> list = new ArrayList<>();
        //遍历交易对
        if (currencyList != null && currencyList.size() > 0) {
            for (Map map1 : currencyList) {
                try {
                    //单条数据
                    Map<String, String> dataMap = new HashMap<>();
                    Long currencyId = (Long) map1.get("currency_id");
                    Long currencyTradeId = (Long) map1.get("currency_trade_id");
                    String cnyMaxPrice = revertCNYUtils.revertCNYPrice("", Integer.parseInt(currencyId + ""));
                    //获取当前交易对的所有订单进行数据封装
                    //查询计价币的name进行进行交易对名拼接
                    List<Map> tradeName = yangCurrencyPairMapper.getTradeName(currencyTradeId);
                    if (tradeName != null && tradeName.size() > 0) {
                        dataMap.put("pair", map1.get("currency_mark").toString() + "/" + tradeName.get(0).get("currencyTradeMark"));
                    }
                    //最新价
                    Map newPrice = null;
                    newPrice = yangTradeMapper.getNewPrice(currencyId, currencyTradeId);
                    if (newPrice != null) {
                        Object object = newPrice.get("price");
                        //String price = (String) newPrice.get("price");
                        BigDecimal bigDecimal = new BigDecimal(object.toString());
                        dataMap.put("price", bigDecimal.toPlainString());
                        if (!"0".equals(cnyMaxPrice)) {
                            String newPrice1 = DecimalUtils.multiplicationForNum(newPrice.get("price").toString(), cnyMaxPrice);
                            dataMap.put("newPriceForCNY", newPrice1);
                        } else {
                            dataMap.put("newPriceForCNY", "0");
                        }
                    } else {
                        dataMap.put("price", "0");
                        dataMap.put("newPriceForCNY", "0");
                    }
                    //最新价约等于多少人民币


                    //涨跌幅
                    Map priceLimit = yangTradeMapper.getPriceLimit(currencyId, currencyTradeId,
                            GetDataUtils.getDayBegin());
                    if (priceLimit != null) {
                        Object price = newPrice.get("price");
                        Object price1 = priceLimit.get("price");
                        String priceLimit1 = DecimalUtils.subtraction(price.toString(), price1.toString());
                        String division = DecimalUtils.division(priceLimit1, price1.toString(), 6);
                        String multiplication = DecimalUtils.multiplication(division, "100");
                        dataMap.put("priceLimit", multiplication);
                    } else {
                        dataMap.put("priceLimit", "00.00");
                    }
                    //最高价
                    Map maxprice = yangTradeMapper.getMaxPrice(currencyId, currencyTradeId, GetDataUtils.getDayBegin());
                    if (maxprice != null) {
                        Object maxPrice = maxprice.get("maxPrice");
                        dataMap.put("maxPrice", maxPrice.toString());
                        //最高价约等于多少人民币
                        if (!"0".equals(cnyMaxPrice)) {
                            String newPrice1 = DecimalUtils.multiplicationForNum(maxprice.get("maxPrice").toString(), cnyMaxPrice);
                            dataMap.put("maxPriceForCNY", newPrice1);
                        } else {
                            dataMap.put("maxPriceForCNY", "0");
                        }
                    } else {
                        dataMap.put("maxPrice", "0");
                        dataMap.put("maxPriceForCNY", "0");
                    }

                    //最低价
                    Map minprice = yangTradeMapper.getMinPrice(currencyId, currencyTradeId, GetDataUtils.getDayBegin());
                    if (minprice != null) {
                        Object minPrice = minprice.get("minPrice");
                        dataMap.put("minPrice", minPrice.toString());
                        //最低价约等于多少人民币
                        if (!"0".equals(cnyMaxPrice)) {
                            String newPrice1 = DecimalUtils.multiplicationForNum(minprice.get("minPrice").toString(), cnyMaxPrice);
                            dataMap.put("minPriceForCNY", newPrice1);
                        } else {
                            dataMap.put("minPriceForCNY", "0");
                        }
                    } else {
                        dataMap.put("minPrice", "0");
                        dataMap.put("minPriceForCNY", "0");
                    }

                    //获取24小时成交额(钱)
                    Map vol = yangTradeMapper.get24VOL(currencyId, currencyTradeId, GetDataUtils.getDayBegin());
                    if (vol != null) {
                        dataMap.put("money", vol.get("money").toString());
                        //最高价约等于多少人民币
                        if (!"0".equals(cnyMaxPrice)) {
                            String newPrice1 = DecimalUtils.multiplicationForNum(vol.get("money").toString(), cnyMaxPrice);
                            dataMap.put("moneyForCNY", newPrice1);
                        } else {
                            dataMap.put("moneyForCNY", "0");
                        }
                    } else {
                        dataMap.put("money", "0");
                        dataMap.put("moneyForCNY", "0");
                    }
                    //获取24小时成交量
                    Map TurnoverL = yangTradeMapper.get24TurnoverL(currencyId, currencyTradeId, GetDataUtils.getDayBegin());
                    if (TurnoverL != null) {
                        dataMap.put("num", TurnoverL.get("num").toString());
                    } else {
                        dataMap.put("num", "0");
                    }
                    list.add(dataMap);

                    //dataMap.put("currencyMark",map1.get("currency_mark").toString());
                    //dataMap11.put(map1.get("currency_mark").toString()+"",dataMap.toString());
                } catch (Exception e) {
                    result.setMsg("系统错误");
                    result.setCode(0);
                    e.printStackTrace();
                }
            }

            Collections.sort(list, new Comparator<Map<String, String>>() {
                @Override
                public int compare(Map<String, String> o1, Map<String, String> o2) {
                    //String priceLimit1 = o1.get("priceLimit");
                    //String priceLimit2 = o2.get("priceLimit");
                    String priceLimit1 = o1.get("priceLimit");
                    String priceLimit2 = o2.get("priceLimit");
                    BigDecimal limit1 = null;
                    BigDecimal limit2 = null;
                    if (StringUtils.isNotBlank(priceLimit1)) {
                        limit1 = new BigDecimal(priceLimit1);
                    }
                    if (StringUtils.isNotBlank(priceLimit2)) {
                        limit2 = new BigDecimal(priceLimit2);
                    }

                    return limit2.compareTo(limit1);
                }
            });
            List<Map<String,String>> dataList = new ArrayList<>();
            for (int i = 0; i <= 9; i++) {
                dataList.add(list.get(i));
            }
            result.setCode(1);
            result.setMsg("查询成功");
            result.setData(dataList);
        }
        return result;
    }

    /**
     * 获取首页成交榜
     * 耶耶
     *
     * @return
     */
    public Result getTradeCurrencyInfos() {
        Result result = new Result();
        result.setCode(0);
        result.setMsg("查询失败");
        //获取当前大区下所有的交易对
        Map<String, String> map = new HashMap<>();
        List<Map> currencyList = yangCurrencyPairMapper.getCurrencyListChanges(map);
        //总数据
        List<Map<String, String>> list = new ArrayList<>();
        if (currencyList != null && currencyList.size() > 0) {
            for (Map map1 : currencyList) {
                try {
                    //单条数据
                    Map<String, String> dataMap = new HashMap<>();
                    Long currencyId = (Long) map1.get("currency_id");
                    Long currencyTradeId = (Long) map1.get("currency_trade_id");
                    String cnyMaxPrice = revertCNYUtils.revertCNYPrice("", Integer.parseInt(currencyId + ""));
                    //获取当前交易对的所有订单进行数据封装
                    //最新价
                    List<Map> tradeName = yangCurrencyPairMapper.getTradeName(currencyTradeId);
                    if (tradeName != null && tradeName.size() > 0) {
                        dataMap.put("pair", map1.get("currency_mark").toString() + "/" + tradeName.get(0).get("currencyTradeMark"));
                    }
                    Map newPrice = null;
                    newPrice = yangTradeMapper.getNewPrice(currencyId, currencyTradeId);
                    if (newPrice != null) {
                        Object object = newPrice.get("price");
                        BigDecimal bigDecimal = new BigDecimal(object.toString());
                        dataMap.put("price", bigDecimal.toPlainString());
                        //最新价约等于多少人民币
                        if (!"0".equals(cnyMaxPrice)) {
                            String newPrice1 = DecimalUtils.multiplicationForNum(newPrice.get("price").toString(), cnyMaxPrice);
                            dataMap.put("newPriceForCNY", newPrice1);
                        } else {
                            dataMap.put("newPriceForCNY", "0");
                        }
                    } else {
                        dataMap.put("price", "0");
                        dataMap.put("newPriceForCNY", "0");
                    }

                    //涨跌幅
                    Map priceLimit = yangTradeMapper.getPriceLimit(currencyId, currencyTradeId,
                            GetDataUtils.getDayBegin());
                    if (priceLimit != null) {
                        Object price = newPrice.get("price");
                        Object price1 = priceLimit.get("price");
                        String priceLimit1 = DecimalUtils.subtraction(price.toString(), price1.toString());
                        String division = DecimalUtils.division(priceLimit1, price1.toString(), 6);
                        String multiplication = DecimalUtils.multiplication(division, "100");
                        dataMap.put("priceLimit", multiplication + "%");
                    } else {
                        dataMap.put("priceLimit", "00.00%");
                    }
                    //最高价
                    Map maxprice = yangTradeMapper.getMaxPrice(currencyId, currencyTradeId, GetDataUtils.getDayBegin());
                    if (maxprice != null) {
                        Object maxPrice = maxprice.get("maxPrice");
                        dataMap.put("maxPrice", maxPrice.toString());
                        if (!"0".equals(cnyMaxPrice)) {
                            String newPrice1 = DecimalUtils.multiplicationForNum(maxprice.get("maxPrice").toString(), cnyMaxPrice);
                            dataMap.put("maxPriceForCNY", newPrice1);
                        } else {
                            dataMap.put("maxPriceForCNY", "0");
                        }
                    } else {
                        dataMap.put("maxPrice", "0");
                        dataMap.put("maxPriceForCNY", "0");
                    }


                    //最低价
                    Map minprice = yangTradeMapper.getMinPrice(currencyId, currencyTradeId, GetDataUtils.getDayBegin());
                    if (minprice != null) {
                        Object minPrice = minprice.get("minPrice");
                        dataMap.put("minPrice", minPrice.toString());
                        if (!"0".equals(cnyMaxPrice)) {
                            String newPrice1 = DecimalUtils.multiplicationForNum(minprice.get("minPrice").toString(), cnyMaxPrice);
                            dataMap.put("minPriceForCNY", newPrice1);
                        } else {
                            dataMap.put("minPriceForCNY", "0");
                        }
                    } else {
                        dataMap.put("minPrice", "0");
                        dataMap.put("minPriceForCNY", "0");
                    }

                    //获取24小时成交额(钱)
                    Map vol = yangTradeMapper.get24VOL(currencyId, currencyTradeId, GetDataUtils.getDayBegin());
                    if (vol != null) {
                        dataMap.put("money", vol.get("money").toString());
                        if (!"0".equals(cnyMaxPrice)) {
                            String newPrice1 = DecimalUtils.multiplicationForNum(vol.get("money").toString(), cnyMaxPrice);
                            dataMap.put("moneyForCNY", newPrice1);
                        } else {
                            dataMap.put("moneyForCNY", "0");
                        }
                    } else {
                        dataMap.put("money", "0");
                        dataMap.put("moneyForCNY", "0");
                    }

                    //获取24小时成交量
                    Map TurnoverL = yangTradeMapper.get24TurnoverL(currencyId, currencyTradeId, GetDataUtils.getDayBegin());
                    if (TurnoverL != null) {
                        dataMap.put("num", TurnoverL.get("num").toString());
                    } else {
                        dataMap.put("num", "0");
                    }
                    list.add(dataMap);

                } catch (Exception e) {
                    result.setMsg("系统错误");
                    result.setCode(0);
                    e.printStackTrace();
                }
            }
            Collections.sort(list, new Comparator<Map<String, String>>() {
                @Override
                public int compare(Map<String, String> o1, Map<String, String> o2) {
                    String priceLimit1 = o1.get("money");
                    String priceLimit2 = o2.get("money");
                    BigDecimal limit1 = null;
                    BigDecimal limit2 = null;
                    if (StringUtils.isNotBlank(priceLimit1)) {
                        limit1 = new BigDecimal(priceLimit1);
                    }
                    if (StringUtils.isNotBlank(priceLimit2)) {
                        limit2 = new BigDecimal(priceLimit2);
                    }

                    return limit2.compareTo(limit1);
                }
            });
            List<Map<String,String>> dataList = new ArrayList<>();
            for (int i = 0; i <= 9; i++) {
                dataList.add(list.get(i));
            }
            result.setCode(1);
            result.setMsg("查询成功");
            result.setData(dataList);
        }
        return result;
    }

    /**
     * 前台轮播数据
     */
    public Result getIncreaseList() {
        Result result = new Result();
        List<Map<String, String>> list = new ArrayList<>();
        //获取当前大区下所有的交易对
        List<Map> currencyList = yangCurrencyPairMapper.getYangCurrencyPairList();
        if (currencyList != null && currencyList.size() != 0) {
            for (Map map1 : currencyList) {
                try {
                    //单条数据
                    Map<String, String> dataMap = new HashMap<>();
                    Long currencyId = (Long) map1.get("currency_id");
                    Long currencyTradeId = (Long) map1.get("currency_trade_id");
                    String cnyMaxPrice = revertCNYUtils.revertCNYPrice("", Integer.parseInt(currencyId + ""));
                    //获取当前交易对的所有订单进行数据封装
                    //最新价
                    List<Map> tradeName = yangCurrencyPairMapper.getTradeName(currencyTradeId);
                    if (tradeName != null && tradeName.size() > 0) {
                        dataMap.put("pair", map1.get("currency_mark").toString() + "/" + tradeName.get(0).get("currencyTradeMark"));
                        Map newPrice = null;
                        newPrice = yangTradeMapper.getNewPrice(currencyId, currencyTradeId);
                        if (newPrice != null) {
                            Object object = newPrice.get("price");
                            dataMap.put("price", object.toString());
                            //最新价约等于多少人民币
                            if (!"0".equals(cnyMaxPrice)) {
                                String newPrice1 = DecimalUtils.multiplicationForNum(newPrice.get("price").toString(), cnyMaxPrice);
                                dataMap.put("newPriceForCNY", newPrice1);
                            } else {
                                dataMap.put("newPriceForCNY", "0");
                            }
                        } else {
                            dataMap.put("price", "0");
                            dataMap.put("newPriceForCNY", "0");
                        }

                        //涨跌幅
                        Map priceLimit = yangTradeMapper.getPriceLimit(currencyId, currencyTradeId,
                                GetDataUtils.getDayBegin());
                        if (priceLimit != null) {
                            Object price = newPrice.get("price");
                            Object price1 = priceLimit.get("price");
                            String priceLimit1 = DecimalUtils.subtraction(price.toString(), price1.toString());
                            String division = DecimalUtils.division(priceLimit1, price1.toString(), 6);
                            String multiplication = DecimalUtils.multiplication(division, "100");
                            dataMap.put("priceLimit", multiplication + "%");
                        } else {
                            dataMap.put("priceLimit", "0");
                        }
                        //最高价
                        Map maxprice = yangTradeMapper.getMaxPrice(currencyId, currencyTradeId, GetDataUtils.getDayBegin());
                        if (maxprice != null) {
                            Object maxPrice = maxprice.get("maxPrice");
                            dataMap.put("maxPrice", maxPrice.toString());
                            if (!"0".equals(cnyMaxPrice)) {
                                String newPrice1 = DecimalUtils.multiplicationForNum(maxprice.get("maxPrice").toString(), cnyMaxPrice);
                                dataMap.put("maxPriceForCNY", newPrice1);
                            } else {
                                dataMap.put("maxPriceForCNY", "0");
                            }
                        } else {
                            dataMap.put("maxPrice", "0");
                            dataMap.put("maxPriceForCNY", "0");
                        }
                        //最低价
                        Map minprice = yangTradeMapper.getMinPrice(currencyId, currencyTradeId, GetDataUtils.getDayBegin());
                        if (minprice != null) {
                            Object minPrice = minprice.get("minPrice");
                            dataMap.put("minPrice", minPrice.toString());
                            if (!"0".equals(cnyMaxPrice)) {
                                String newPrice1 = DecimalUtils.multiplicationForNum(minprice.get("minPrice").toString(), cnyMaxPrice);
                                dataMap.put("minPriceForCNY", newPrice1);
                            } else {
                                dataMap.put("minPriceForCNY", "0");
                            }
                        } else {
                            dataMap.put("minPrice", "0");
                            dataMap.put("minPriceForCNY", "0");
                        }
                        //获取24小时成交额(钱)
                        Map vol = yangTradeMapper.get24VOL(currencyId, currencyTradeId, GetDataUtils.getDayBegin());
                        if (vol != null) {
                            dataMap.put("money", vol.get("money").toString());
                            if (!"0".equals(cnyMaxPrice)) {
                                String newPrice1 = DecimalUtils.multiplicationForNum(vol.get("money").toString(), cnyMaxPrice);
                                dataMap.put("moneyForCNY", newPrice1);
                            } else {
                                dataMap.put("moneyForCNY", "0");
                            }
                        } else {
                            dataMap.put("money", "0");
                            dataMap.put("moneyForCNY", "0");
                        }
                        //获取24小时成交量
                        Map TurnoverL = yangTradeMapper.get24TurnoverL(currencyId, currencyTradeId, GetDataUtils.getDayBegin());
                        if (TurnoverL != null) {
                            dataMap.put("num", TurnoverL.get("num").toString());
                        } else {
                            dataMap.put("num", "0");
                        }
                        dataMap.put("currencyMark", map1.get("currency_mark").toString());
                        list.add(dataMap);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
        result.setData(list);
        return result;
    }

    /****
     * 新币榜
     * @return
     */
    public Result getNewCurrenciesList() {
        Result result = new Result();
        Map<String, String> map = new HashMap<>();
        //获取当前大区下所有的交易对
        List<Map> currencyList = yangCurrencyPairMapper.getYangCurrencyPairLists();
        List<Map<String, String>> list = new ArrayList<>();
        if (currencyList != null && currencyList.size() != 0) {
            for (Map map1 : currencyList) {
                try {
                    //单条数据
                    Map<String, String> dataMap = new HashMap<>();
                    Long currencyId = (Long) map1.get("currency_id");
                    Long currencyTradeId = (Long) map1.get("currency_trade_id");
                    String cnyMaxPrice = revertCNYUtils.revertCNYPrice("", Integer.parseInt(currencyId + ""));
                    //获取当前交易对的所有订单进行数据封装
                    //最新价
                    List<Map> tradeName = yangCurrencyPairMapper.getTradeName(currencyTradeId);
                    if (tradeName != null && tradeName.size() > 0) {
                        dataMap.put("pair", map1.get("currency_mark").toString() + "/" + tradeName.get(0).get("currencyTradeMark"));
                        Map newPrice = null;
                        newPrice = yangTradeMapper.getNewPrices(currencyId, currencyTradeId);
                        if (newPrice != null) {
                            Object object = newPrice.get("price");
                            dataMap.put("price", object.toString());
                            //最新价约等于多少人民币
                            if (!"0".equals(cnyMaxPrice)) {
                                String newPrice1 = DecimalUtils.multiplicationForNum(newPrice.get("price").toString(), cnyMaxPrice);
                                dataMap.put("newPriceForCNY", newPrice1);
                            } else {
                                dataMap.put("newPriceForCNY", "0");
                            }
                        } else {
                            dataMap.put("price", "0");
                            dataMap.put("newPriceForCNY", "0");
                        }
                        //涨跌幅
                        Map priceLimit = yangTradeMapper.getPriceLimits(currencyId, currencyTradeId,
                                GetDataUtils.getDayBegin());
                        if (priceLimit != null) {
                            Object price = newPrice.get("price");
                            Object price1 = priceLimit.get("price");
                            String priceLimit1 = DecimalUtils.subtraction(price.toString(), price1.toString());
                            String division = DecimalUtils.division(priceLimit1, price1.toString(), 6);
                            String multiplication = DecimalUtils.multiplication(division, "100");
                            dataMap.put("priceLimit", multiplication + "%");
                        } else {
                            dataMap.put("priceLimit", "0.00%");
                        }
                        //最高价
                        Map maxprice = yangTradeMapper.getMaxPrices(currencyId, currencyTradeId, GetDataUtils.getDayBegin());
                        if (maxprice != null) {
                            Object maxPrice = maxprice.get("maxPrice");
                            dataMap.put("maxPrice", maxPrice.toString());
                            if (!"0".equals(cnyMaxPrice)) {
                                String newPrice1 = DecimalUtils.multiplicationForNum(maxprice.get("maxPrice").toString(), cnyMaxPrice);
                                dataMap.put("maxPriceForCNY", newPrice1);
                            } else {
                                dataMap.put("maxPriceForCNY", "0");
                            }
                        } else {
                            dataMap.put("maxPrice", "0");
                            dataMap.put("maxPriceForCNY", "0");
                        }
                        //最低价
                        Map minprice = yangTradeMapper.getMinPrices(currencyId, currencyTradeId, GetDataUtils.getDayBegin());
                        if (minprice != null) {
                            Object minPrice = minprice.get("minPrice");
                            dataMap.put("minPrice", minPrice.toString());
                            if (!"0".equals(cnyMaxPrice)) {
                                String newPrice1 = DecimalUtils.multiplicationForNum(minprice.get("minPrice").toString(), cnyMaxPrice);
                                dataMap.put("minPriceForCNY", newPrice1);
                            } else {
                                dataMap.put("minPriceForCNY", "0");
                            }
                        } else {
                            dataMap.put("minPrice", "0");
                            dataMap.put("minPriceForCNY", "0");
                        }
                        //获取24小时成交额(钱)
                        Map vol = yangTradeMapper.get24VOLs(currencyId, currencyTradeId, GetDataUtils.getDayBegin());
                        if (vol != null) {
                            dataMap.put("money", vol.get("money").toString());
                            if (!"0".equals(cnyMaxPrice)) {
                                String newPrice1 = DecimalUtils.multiplicationForNum(vol.get("money").toString(), cnyMaxPrice);
                                dataMap.put("moneyForCNY", newPrice1);
                            } else {
                                dataMap.put("moneyForCNY", "0");
                            }
                        } else {
                            dataMap.put("money", "0");
                            dataMap.put("moneyForCNY", "0");
                        }
                        //获取24小时成交量
                        Map TurnoverL = yangTradeMapper.get24TurnoverLs(currencyId, currencyTradeId, GetDataUtils.getDayBegin());
                        if (TurnoverL != null) {
                            dataMap.put("num", TurnoverL.get("num").toString());
                        } else {
                            dataMap.put("num", "0");
                        }
                        list.add(dataMap);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
        result.setData(list);
        return result;

    }

    /****
     * wf
     * 承兑商挂卖/买
     * @return
     */
    public Result creatAcceptancesOrder(Map<String, String> map) {
        Result result = new Result();
        try {
            validateAccessToken(map.get("accessToken"), result, yangMemberTokenService);
            if (result.getCode().intValue() == Result.Code.ERROR.intValue()) {
                return result;
            }
            YangMember yangMember = (YangMember) result.getData();
            if (map.get("type").equals("sell") && map.get("type") != null) {
                //查询当前 挂单信息 是否存在
                Example guaExample = new Example(YangC2CGua.class);
                guaExample.createCriteria().andEqualTo("id", map.get("guaId"));
                List<YangC2CGua> guaList = yangC2CGuaMapper.selectByExample(guaExample);
                if (guaList == null || guaList.size() == 0) {
                    result.setCode(Result.Code.ERROR);
                    result.setErrorCode(ErrorCode.ERROR_C2C_GUA_NOT_FOUND.getIndex());
                    result.setMsg(ErrorCode.ERROR_C2C_GUA_NOT_FOUND.getMessage());
                    return result;
                }
                //查询c2C资产余额是否足够
                Example assetExample = new Example(YangC2CAsset.class);
                assetExample.createCriteria().andEqualTo("memberId", yangMember.getMemberId()).andEqualTo("currencyId", guaList.get(0).getCurrencyId());
                List<YangC2CAsset> assetList = yangC2CAssetMapper.selectByExample(assetExample);
                if (assetList == null || assetList.size() == 0) {
                    result.setCode(Result.Code.ERROR);
                    result.setErrorCode(ErrorCode.ERROR_TRADE_CURRENCY_USER_PRICE_NOTENOUGH.getIndex());
                    result.setMsg(ErrorCode.ERROR_TRADE_CURRENCY_USER_PRICE_NOTENOUGH.getMessage());
                    return result;
                }
                //冻结c2c账户余额
                Map tradeMap = new HashMap();
                tradeMap.put("memberId", yangMember.getMemberId());
                tradeMap.put("cid", guaList.get(0).getCurrencyId());
                tradeMap.put("rmb_in", map.get("num"));
                tradeMap.put("rmb_operation", "dec");
                tradeMap.put("forzen_rmb_in", map.get("num"));
                tradeMap.put("forzen_rmb_operation", "inc");
                int i = yangC2CAssetMapper.assetTrade(tradeMap);
                if (i == 0) {
                    //c2c账户冻结异常
                    result.setCode(Result.Code.ERROR);
                    result.setErrorCode(ErrorCode.ERROR_C2C_ACCOUNT_FOREZN.getIndex());
                    result.setMsg(ErrorCode.ERROR_C2C_ACCOUNT_FOREZN.getMessage());
                    return result;
                }
                //新增c2c挂单
                YangC2CGua yangC2CGua = new YangC2CGua();
                yangC2CGua.setPrice(new BigDecimal(map.get("price")));
                yangC2CGua.setNum(new BigDecimal(map.get("num")));
                yangC2CGua.setMaxMoney(map.get("max_money"));
                yangC2CGua.setMinMoney(map.get("minMoney"));
                yangC2CGua.setNickName(map.get("nick_name"));
                yangC2CGua.setCurrencyId(Integer.parseInt(map.get("currencyId")));
                yangC2CGua.setRemark(map.get("remark"));
                yangC2CGua.setCardInfo(map.get("cardInfo"));
                yangC2CGua.setCardDetail(map.get("cardDetail"));
                yangC2CGua.setMinFund(map.get("minFund"));
                yangC2CGua.setMaxFund(map.get("maxFund"));
                yangC2CGua.setOrderNum(new BigDecimal(map.get("orderNum")));
                yangC2CGuaMapper.insert(yangC2CGua);
                //冻结商家可交易数量
                YangC2CGua yangC2CGua1 = guaList.get(0);
                yangC2CGua1.setFrozenNum(yangC2CGua.getFrozenNum().add(new BigDecimal(map.get("num"))));
                yangC2CGuaMapper.updateByPrimaryKeySelective(yangC2CGua1);
                result.setData(yangC2CGua);
                result.setMsg("挂卖成功！");
            } else if (map.get("type").equals("buy") && map.get("type") != null) {
                //买单  判断用户c2c 订单是否超过5个
                Example example = new Example(YangC2COrders.class);
                example.createCriteria().andEqualTo("type", CoinConst.ORDER_BUY).andEqualTo("memberId", yangMember.getMemberId()).andEqualTo("buyStatus", CoinConst.BANK_STATUS_AVA);
                List<YangC2COrders> list = yangC2COrdersMapper.selectByExample(example);
                if (list != null && list.size() >= 5) {
                    result.setCode(Result.Code.ERROR);
                    result.setErrorCode(ErrorCode.ERROR_C2C_ORDER_BUY_LIMIT.getIndex());
                    result.setMsg(ErrorCode.ERROR_C2C_ORDER_BUY_LIMIT.getMessage());
                    return result;
                }
                //查询当前 挂单信息
                Example guaExample = new Example(YangC2CGua.class);
                guaExample.createCriteria().andEqualTo("id", map.get("guaId"));
                List<YangC2CGua> guaList = yangC2CGuaMapper.selectByExample(guaExample);
                //未找到 存在挂单
                if (guaList == null || guaList.size() == 0) {
                    result.setCode(Result.Code.ERROR);
                    result.setErrorCode(ErrorCode.ERROR_C2C_GUA_NOT_FOUND.getIndex());
                    result.setMsg(ErrorCode.ERROR_C2C_GUA_NOT_FOUND.getMessage());
                    return result;
                }

                //查询当前挂单 信息
                YangC2CGua yangC2CGua = guaList.get(0);
                if ((yangC2CGua.getNum().subtract(yangC2CGua.getFrozenNum())).compareTo(new BigDecimal(map.get("num"))) < 0) {
                    result.setCode(Result.Code.ERROR);
                    result.setErrorCode(ErrorCode.ERROR_C2C_ORDER_BUY_NUM_LIMIT.getIndex());
                    result.setMsg(ErrorCode.ERROR_C2C_ORDER_BUY_NUM_LIMIT.getMessage());
                    return result;
                }
                //新增c2c挂单
                YangC2COrders yangC2COrders = new YangC2COrders();
                yangC2COrders.setTradeNo(UUID.randomUUID().toString().replace("-", ""));
                yangC2COrders.setGuaId(Integer.valueOf(map.get("guaId")));
                yangC2COrders.setCurrencyId(guaList.get(0).getCurrencyId());
                yangC2COrders.setType(CoinConst.ORDER_BUY);
                yangC2COrders.setBuyMoney(map.get("buyMoney"));
                yangC2COrders.setBuyStatus(CoinConst.C2C_ORDER_STATUS_WAITPAY);
                yangC2COrders.setAddTime(String.valueOf(DateUtils.getNowTimes()));
                yangC2COrders.setInvalidTime(String.valueOf(DateUtils.getNowTimes() + 60 * 15));
                yangC2COrders.setPrice(guaList.get(0).getPrice());
                yangC2COrders.setNum(new BigDecimal(map.get("num")));
                yangC2COrders.setMemberId(yangMember.getMemberId());
                yangC2COrders.setCardDetail(guaList.get(0).getCardDetail());
                yangC2COrders.setNickName(guaList.get(0).getNickName());
                yangC2COrders.setCankaoNum(EmailUtils.getRandomNumber(6));//6位随机数
                yangC2COrders.setOrderNum(new BigDecimal(map.get("orderNum")));
                yangC2COrdersMapper.insertSelective(yangC2COrders);
                //冻结商家可交易数量
                yangC2CGua.setFrozenNum(yangC2CGua.getFrozenNum().add(new BigDecimal(map.get("num"))));
                yangC2CGuaMapper.updateByPrimaryKeySelective(yangC2CGua);
                result.setData(yangC2COrders);
                result.setMsg("挂买成功！");
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.setCode(Result.Code.ERROR);
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
            result.setMsg(ErrorCode.ERROR_SYSTEM.getMessage());
        }
        return result;
    }

    /***
     * wf
     * 承兑商确认收款后，放行币
     * @param map
     * @return
     */
    public Result acceptancesOrderRelease(Map<String, String> map) {
        Result result = new Result();
        try {
            validateAccessToken(map.get("accessToken"), result, yangMemberTokenService);
            if (result.getCode().intValue() == Result.Code.ERROR.intValue()) {
                return result;
            }
            YangMember yangMember = (YangMember) result.getData();
            Example example = new Example(YangC2COrders.class);
            example.createCriteria().andEqualTo("tradeNo", map.get("id"));
            List<YangC2COrders> list = yangC2COrdersMapper.selectByExample(example);
            //未找到当前订单
            if (list == null || list.size() == 0) {
                result.setCode(Result.Code.ERROR);
                result.setErrorCode(ErrorCode.ERROR_C2C_ORDER_NOT_FOUND.getIndex());
                result.setMsg(ErrorCode.ERROR_C2C_ORDER_NOT_FOUND.getMessage());
                return result;
            }
            YangC2COrders yangC2COrders = list.get(0);
            yangC2COrders.setBuyStatus(CoinConst.C2C_ORDER_STATUS_BUYERPAY);
            int i = yangC2COrdersMapper.updateByPrimaryKeySelective(yangC2COrders);
            result.setData(null);
            result.setMsg("购买成功！");
        } catch (Exception e) {
            e.printStackTrace();
            result.setCode(Result.Code.ERROR);
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
            result.setMsg(ErrorCode.ERROR_SYSTEM.getMessage());
        }
        return result;
    }

    /***
     *wf
     * 获取交易所交易对行情数据
     * @return
     */
    public Result getFontApi() {
        Result result = new Result();
        result.setCode(0);
        result.setMsg("查询失败");
        //获取当前大区下所有的交易对
        Map<String, String> map = new HashMap<>();
        List<Map> currencyList = yangCurrencyPairMapper.getCurrencyListChanges(map);
        //总数据
        List<Map<String, String>> list = new ArrayList<>();
        if (currencyList != null && currencyList.size() > 0) {
            for (Map map1 : currencyList) {
                try {
                    //单条数据
                    Map<String, String> dataMap = new HashMap<>();
                    Long currencyId = (Long) map1.get("currency_id");
                    Long currencyTradeId = (Long) map1.get("currency_trade_id");
                    String cnyMaxPrice = revertCNYUtils.revertCNYPrice("", Integer.parseInt(currencyId + ""));
                    //获取当前交易对的所有订单进行数据封装
                    //最新价
                    List<Map> tradeName = yangCurrencyPairMapper.getTradeName(currencyTradeId);
                    if (tradeName != null && tradeName.size() > 0) {
                        dataMap.put("symbol", map1.get("currency_mark").toString() + "/" + tradeName.get(0).get("currencyTradeMark"));
                    }
                    Map newPrice = null;
                    newPrice = yangTradeMapper.getNewPrice(currencyId, currencyTradeId);
                    if (newPrice != null) {
                        Object object = newPrice.get("price");
                        BigDecimal bigDecimal = new BigDecimal(object.toString());
                        dataMap.put("last", bigDecimal.toPlainString());
                        //最新价约等于多少人民币
                        if (!"0".equals(cnyMaxPrice)) {
                            String newPrice1 = DecimalUtils.multiplicationForNum(newPrice.get("price").toString(), cnyMaxPrice);
                            dataMap.put("lastForCNY", newPrice1);
                        } else {
                            dataMap.put("lastForCNY", "0");
                        }
                    } else {
                        dataMap.put("last", "0");
                        dataMap.put("lastForCNY", "0");
                    }

                    //涨跌幅
                    Map priceLimit = yangTradeMapper.getPriceLimit(currencyId, currencyTradeId,
                            GetDataUtils.getDayBegin());
                    if (priceLimit != null) {
                        Object price = newPrice.get("price");
                        Object price1 = priceLimit.get("price");
                        String priceLimit1 = DecimalUtils.subtraction(price.toString(), price1.toString());
                        String division = DecimalUtils.division(priceLimit1, price1.toString(), 6);
                        String multiplication = DecimalUtils.multiplication(division, "100");
                        dataMap.put("change", multiplication + "%");
                    } else {
                        dataMap.put("change", "00.00%");
                    }
                    //最高价
                    Map maxprice = yangTradeMapper.getMaxPrice(currencyId, currencyTradeId, GetDataUtils.getDayBegin());
                    if (maxprice != null) {
                        Object maxPrice = maxprice.get("maxPrice");
                        dataMap.put("high", maxPrice.toString());
                        if (!"0".equals(cnyMaxPrice)) {
                            String newPrice1 = DecimalUtils.multiplicationForNum(maxprice.get("maxPrice").toString(), cnyMaxPrice);
                            dataMap.put("highForCNY", newPrice1);
                        } else {
                            dataMap.put("highForCNY", "0");
                        }
                    } else {
                        dataMap.put("high", "0");
                        dataMap.put("highForCNY", "0");
                    }


                    //最低价
                    Map minprice = yangTradeMapper.getMinPrice(currencyId, currencyTradeId, GetDataUtils.getDayBegin());
                    if (minprice != null) {
                        Object minPrice = minprice.get("minPrice");
                        dataMap.put("low", minPrice.toString());
                        if (!"0".equals(cnyMaxPrice)) {
                            String newPrice1 = DecimalUtils.multiplicationForNum(minprice.get("minPrice").toString(), cnyMaxPrice);
                            dataMap.put("lowForCNY", newPrice1);
                        } else {
                            dataMap.put("lowForCNY", "0");
                        }
                    } else {
                        dataMap.put("low", "0");
                        dataMap.put("lowForCNY", "0");
                    }

                    //获取24小时成交额(钱)
                    Map vol = yangTradeMapper.get24VOL(currencyId, currencyTradeId, GetDataUtils.getDayBegin());
                    if (vol != null) {
                        dataMap.put("money", vol.get("money").toString());
                        if (!"0".equals(cnyMaxPrice)) {
                            String newPrice1 = DecimalUtils.multiplicationForNum(vol.get("money").toString(), cnyMaxPrice);
                            dataMap.put("moneyForCNY", newPrice1);
                        } else {
                            dataMap.put("moneyForCNY", "0");
                        }
                    } else {
                        dataMap.put("money", "0");
                        dataMap.put("moneyForCNY", "0");
                    }

                    //获取24小时成交量
                    Map TurnoverL = yangTradeMapper.get24TurnoverL(currencyId, currencyTradeId, GetDataUtils.getDayBegin());
                    if (TurnoverL != null) {
                        dataMap.put("vol", TurnoverL.get("num").toString());
                    } else {
                        dataMap.put("vol", "0");
                    }
                    //获取买一价
                    Map MaximumOrderPrice = yangTradeMapper.getMaximumOrderPrice(currencyId, currencyTradeId, GetDataUtils.getDayBegin());
                    if (MaximumOrderPrice!=null){
                        dataMap.put("buy",MaximumOrderPrice.get("price").toString());
                    }else {
                        dataMap.put("buy","0");
                    }
                    //获取卖一价
                    Map MinimumGuaPrice =yangTradeMapper.getMinimumGuaPrice(currencyId, currencyTradeId, GetDataUtils.getDayBegin());
                    if(MinimumGuaPrice!=null){
                        dataMap.put("sell",MinimumGuaPrice.get("price").toString());
                    }else {
                        dataMap.put("sell","0");
                    }
                    list.add(dataMap);

                } catch (Exception e) {
                    result.setMsg("系统错误");
                    result.setCode(0);
                    e.printStackTrace();
                }
            }
            result.setCode(1);
            result.setMsg("查询成功");
            result.setData(list);
        }
        return result;
    }

    /**
     * 获取首页涨幅榜
     * ccw
     *
     * @return
     */
    public Result getUpCurrencyInfo(String symbol) {
        Result result = new Result();
        result.setCode(0);
        result.setMsg("查询失败");
        //获取当前大区下所有的交易对
        Map<String, String> map = new HashMap<>();
        map.put("cyId", symbol);
        List<Map> currencyList = yangCurrencyPairMapper.getCurrencyPairList(map);
        //总数据
        //Map<String,String> dataMap11 = new HashMap<>();
        List<Map<String, String>> list = new ArrayList<>();
        //遍历交易对
        if (currencyList != null && currencyList.size() > 0) {
            Map map1 = currencyList.get(0);
            try {
                //单条数据
                Map<String, String> dataMap = new HashMap<>();
                Long currencyId = (Long) map1.get("currency_id");
                Long currencyTradeId = (Long) map1.get("currency_trade_id");
                String cnyMaxPrice = revertCNYUtils.revertCNYPrice("", Integer.parseInt(currencyId + ""));
                //获取当前交易对的所有订单进行数据封装
                //查询计价币的name进行进行交易对名拼接
//                List<Map> tradeName = yangCurrencyPairMapper.getTradeName(currencyTradeId);
//                if (tradeName != null && tradeName.size() > 0) {

//                }
                dataMap.put("symbol", map1.get("currency_mark").toString() + "/" + map1.get("tradeMark"));
                //最新价
                Map newPrice = null;
                newPrice = yangTradeMapper.getNewPrice(currencyId, currencyTradeId);
                if (newPrice != null) {
                    Object object = newPrice.get("price");
                    //String price = (String) newPrice.get("price");
                    BigDecimal bigDecimal = new BigDecimal(object.toString());
                    dataMap.put("newPrice", bigDecimal.toPlainString());
                    if (!"0".equals(cnyMaxPrice)) {
                        String newPrice1 = DecimalUtils.multiplicationForNum(newPrice.get("price").toString(), cnyMaxPrice);
                        dataMap.put("newPriceForCNY", newPrice1);
                    } else {
                        dataMap.put("newPriceForCNY", "0");
                    }
                } else {
                    dataMap.put("newPrice", "0");
                    dataMap.put("newPriceForCNY", "0");
                }
                //最新价约等于多少人民币

                //涨跌幅
                Map priceLimit = yangTradeMapper.getPriceLimit(currencyId, currencyTradeId,
                        GetDataUtils.getDayBegin());
                if (priceLimit != null) {
                    Object price = newPrice.get("price");
                    Object price1 = priceLimit.get("price");
                    String priceLimit1 = DecimalUtils.subtraction(price.toString(), price1.toString());
                    String division = DecimalUtils.division(priceLimit1, price1.toString(), 6);
                    String multiplication = DecimalUtils.multiplication(division, "100");
                    dataMap.put("priceLimit", multiplication);
                } else {
                    dataMap.put("priceLimit", "00.00");
                }
                //最高价
                Map maxprice = yangTradeMapper.getMaxPrice(currencyId, currencyTradeId, GetDataUtils.getDayBegin());
                if (maxprice != null) {
                    Object maxPrice = maxprice.get("maxPrice");
                    dataMap.put("maxPrice", maxPrice.toString());
                    //最高价约等于多少人民币
                    if (!"0".equals(cnyMaxPrice)) {
                        String newPrice1 = DecimalUtils.multiplicationForNum(maxprice.get("maxPrice").toString(), cnyMaxPrice);
                        dataMap.put("maxPriceForCNY", newPrice1);
                    } else {
                        dataMap.put("maxPriceForCNY", "0");
                    }
                } else {
                    dataMap.put("maxPrice", "0");
                    dataMap.put("maxPriceForCNY", "0");
                }

                //最低价
                Map minprice = yangTradeMapper.getMinPrice(currencyId, currencyTradeId, GetDataUtils.getDayBegin());
                if (minprice != null) {
                    Object minPrice = minprice.get("minPrice");
                    dataMap.put("minPrice", minPrice.toString());
                    //最低价约等于多少人民币
                    if (!"0".equals(cnyMaxPrice)) {
                        String newPrice1 = DecimalUtils.multiplicationForNum(minprice.get("minPrice").toString(), cnyMaxPrice);
                        dataMap.put("minPriceForCNY", newPrice1);
                    } else {
                        dataMap.put("minPriceForCNY", "0");
                    }
                } else {
                    dataMap.put("minPrice", "0");
                    dataMap.put("minPriceForCNY", "0");
                }

                //获取24小时成交额(钱)
                Map vol = yangTradeMapper.get24VOL(currencyId, currencyTradeId, GetDataUtils.getDayBegin());
                if (vol != null) {
                    dataMap.put("money", vol.get("money").toString());
                    //最高价约等于多少人民币
                    if (!"0".equals(cnyMaxPrice)) {
                        String newPrice1 = DecimalUtils.multiplicationForNum(vol.get("money").toString(), cnyMaxPrice);
                        dataMap.put("moneyForCNY", newPrice1);
                    } else {
                        dataMap.put("moneyForCNY", "0");
                    }
                } else {
                    dataMap.put("money", "0");
                    dataMap.put("moneyForCNY", "0");
                }
                //获取24小时成交量
                Map TurnoverL = yangTradeMapper.get24TurnoverL(currencyId, currencyTradeId, GetDataUtils.getDayBegin());
                if (TurnoverL != null) {
                    dataMap.put("num", TurnoverL.get("num").toString());
                } else {
                    dataMap.put("num", "0");
                }
                list.add(dataMap);

                //dataMap.put("currencyMark",map1.get("currency_mark").toString());
                //dataMap11.put(map1.get("currency_mark").toString()+"",dataMap.toString());
            } catch (Exception e) {
                result.setMsg("系统错误");
                result.setCode(0);
                e.printStackTrace();
            }
            result.setCode(1);
            result.setMsg("查询成功");
            result.setData(list);
        }
        return result;
    }

}
