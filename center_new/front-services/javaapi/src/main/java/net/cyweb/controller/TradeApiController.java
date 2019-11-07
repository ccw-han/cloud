package net.cyweb.controller;

import cyweb.utils.CoinConst;
import cyweb.utils.CoinConstUser;
import cyweb.utils.DateUtils;
import cyweb.utils.ErrorCode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import jnr.ffi.annotations.In;
import net.cyweb.config.custom.ParticipationIsBlankUtils;
import net.cyweb.config.custom.ParticipationRemoveBlankSpaceUtils;
import net.cyweb.mapper.YangCurrencyPairMapper;
import net.cyweb.mapper.YangTradeMapper;
import net.cyweb.model.*;
import net.cyweb.service.*;
import net.cyweb.validate.OrderQueue;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import springfox.documentation.annotations.ApiIgnore;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@Api(tags = "交易", position = 2)
@RequestMapping(value = "trade")
public class TradeApiController extends ApiBaseController {

    @Autowired
    private RedisService redisService;

    @Autowired
    private YangOrderService yangOrderService;
    @Autowired
    RobotService robotService;
    @Autowired
    YangTradeMapper yangTradeMapper;

    @Autowired
    private YangPairService yangPairService;

    @Autowired
    YangCurrencyPairMapper yangCurrencyPairMapper;

    @Autowired
    private YangConfigService yangConfigService;

    @RequestMapping(value = "mountOrder", method = RequestMethod.POST)
    @ApiOperation(value = "挂单", notes = "挂单，买单卖单进队列")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "ordersId", value = "订单ID", required = true, dataType = "String", paramType = "query"),
    })
    public Result mountOrder(@ApiIgnore @Validated({OrderQueue.class}) YangOrders yangOrders, BindingResult bindingResult) {
        Result result = new Result();
        result.setCode(Result.Code.ERROR);
        try {
            /*数据验证*/
            if (bindingResult.hasErrors()) {
                result.setMsg(getStringErrors(bindingResult));
            } else {
                redisService.rPush(MQ_ORDER_QUEUE, yangOrders.getOrdersId());
                result.setCode(Result.Code.SUCCESS);
            }
        } catch (Exception e) {
            result.setMsg(e.getMessage());
        }
        return result;
    }

    /**
     * 获取机器人的token
     * @param map
     * @return
     */
    @RequestMapping(value = "getToken", method = RequestMethod.POST)
    @ApiOperation(value = "获取token", notes = "获取token")
    public Result getToken(@RequestBody Map<String,String> map ) {
        Result result = new Result();
        String memberId = map.get("memberId");
        int memId = Integer.parseInt(memberId);
        try{
            Result robot_token = yangConfigService.getConfig("robot_token");
            YangConfig data = (YangConfig) robot_token.getData();
            if (data != null){
                String token = data.getValue();
                redisService.set(CoinConst.TOKENKEYMEMBER + memberId,token);
                redisService.set(CoinConst.TOKENKEYTOKEN + token,memId);
                result.setCode(1);
                result.setMsg(token);
            }else{
                result.setCode(0);
                result.setMsg("数据库没有该token");
            }
        }catch (Exception e){
            result.setCode(0);
            result.setMsg("系统错误");
        }
        return result;
    }

    @ApiOperation(value = "生成订单", notes = "生成订单并自动进队列")
    @RequestMapping(value = "createorder", method = RequestMethod.POST)
    public Result createorder(@RequestBody Map<String,String> map) {
        Result result = new Result();
        Boolean blank = ParticipationIsBlankUtils.isBlank(map);
        if (blank){
            result.setCode(0);
            result.setMsg("参数格式错误");
        }
        Map<String,String> map1 = ParticipationRemoveBlankSpaceUtils.removeBlankSpace(map);
        Result resulta = new Result();
        try {
            String cId = map1.get("cId");
            //将字符串转换为BigDecimal
            String price = map1.get("price");
            String num = map1.get("num");
            /*if (StringUtil.isEmpty(price)) {
                price = "0";
            }
            if (StringUtil.isEmpty(num)) {
                num = "0";
            }*/
            BigDecimal price1 = new BigDecimal(price);
            BigDecimal num1 = new BigDecimal(num);
            String passwd = map1.get("passwd");
            String accessToken = map1.get("accessToken");
            String type = map1.get("type");

            //resulta=yangOrderService.checkDayOrderLimt(yangMemberToken.getAccessToken());
            Integer memberId = (Integer) redisService.get(CoinConst.TOKENKEYTOKEN + accessToken);
            YangMemberToken yangMemberToken = new YangMemberToken();
            yangMemberToken.setAccessToken(accessToken);
            yangMemberToken.setMemberId(memberId);
            if (memberId == null) {
                resulta.setCode(0);
                resulta.setMsg("token过期");
                return resulta;
            }
            result = robotService.saleOrBuyRobot(cId, price1, num1, passwd, yangMemberToken, type, "0");
            if (result.getCode().equals(Result.Code.SUCCESS)) {
                int orderId = (int) result.getData();
                redisService.lPush(MQ_ORDER_QUEUE, orderId);
            }

        } catch (Exception e) {
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
            result.setCode(Result.Code.ERROR);
            result.setMsg(e.getMessage());
            e.printStackTrace();
        }
        return result;

    }


    /**
     * 获取coin信息
     *
     * @param pamas
     * @return
     */
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pamas", value = "凭借参数 currency_id-thread_id,currency_id-thread_id,currency_id-thread_id", required = true, dataType = "int", paramType = "query"),
    })
    @RequestMapping(value = "getCurrencyInfo", method = RequestMethod.POST)
    public Result getCurrencyInfo(String pamas) {
        Result result = new Result();

        //24小时涨跌数据
        long endtimed = System.currentTimeMillis() / 1000;

        long starttime = endtimed - 24 * 3600;


        try {
            result.setCode(Result.Code.SUCCESS);
            String[] pamas_arr = pamas.split(",");
            List list = new LinkedList();

            HashMap pama = new HashMap();
            pama.put("addTime", starttime);
            List<HashMap> maps = yangTradeMapper.selectMaxAndMinAndTotal(pama);
            java.text.DecimalFormat df = new java.text.DecimalFormat("#.00");

            for (String s : pamas_arr) {
                String[] cct = s.split("-");
                CoinPairRedisBean coinPairRedisBean = yangOrderService.getCoinPairRedisBean(Integer.valueOf(cct[0]), Integer.valueOf(cct[1]));
                list.add(coinPairRedisBean);
            }
            result.setData(list);
        } catch (Exception e) {
            result.setCode(Result.Code.ERROR);
        }
        return result;
    }


    /**
     * 修改redis中currency pair的数据
     *
     * @param cid
     * @return
     */
    @ApiImplicitParams({
            @ApiImplicitParam(name = "cid", value = "交易对id", required = true, dataType = "int", paramType = "query"),
    })
    @RequestMapping(value = "flushCurrencyPair", method = RequestMethod.POST)
    public boolean flushCurrencyPair(int cid) {
        try {
            YangCurrencyPair yangCurrencyPair = new YangCurrencyPair();
            yangCurrencyPair.setCyId(cid);
            YangCurrencyPair yangCurrencyPair1 = yangCurrencyPairMapper.selectOne(yangCurrencyPair);

            if (yangCurrencyPair1 != null) {
                yangPairService.getPairInfo(yangCurrencyPair1.getCurrencyId(), yangCurrencyPair1.getCurrencyTradeId(), true);
            }
        } catch (Exception e) {
            return false;
        }
        return true;

    }

    /**
     * K线数据
     *
     * @return
     */

    @RequestMapping(value = "klineData", method = RequestMethod.POST)
    @ApiOperation(value = "K线数据", notes = "K线数据")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "seconds", value = "时间戳", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "currencyId", value = "币种ID", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "currencyTradeId", value = "交易市场ID", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "start", value = "开始时间", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "end", value = "结束时间", required = true, dataType = "String", paramType = "query"),
    })
    public Result klineData(String seconds, String currencyId, String currencyTradeId, String start, String end) {
        Result result = new Result();
        result.setCode(Result.Code.ERROR);
        try {
            Map map = new HashMap<String, Object>();
            map.put("addTime", seconds);
            map.put("currencyId", currencyId);
            map.put("currencyTradeId", currencyTradeId);
            map.put("start", start);
            map.put("end", end);

            List<Map> list = yangOrderService.getKlineData(map);

//
//        map.put("addTime",Integer.valueOf(seconds));
//        map.put("currencyId",currencyId);
//        map.put("currencyTradeId",currencyTradeId);
//        map.put("start",Long.valueOf(start));
//        map.put("end",Long.valueOf(end));
//
//        List<Map> list=yangOrderService.getKlineDataTest(map);
            result.setCode(Result.Code.SUCCESS);
            result.setData(list);
        } catch (Exception e) {
            result.setMsg(e.getMessage());
            result.setCode(Result.Code.ERROR);
            e.printStackTrace();
        }
        return result;
    }


    /**
     * K线数据
     *
     * @return
     */

    @RequestMapping(value = "klineDataTest", method = RequestMethod.POST)
    @ApiOperation(value = "K线数据", notes = "K线数据")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "seconds", value = "时间戳", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "currencyId", value = "币种ID", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "currencyTradeId", value = "交易市场ID", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "start", value = "开始时间", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "end", value = "结束时间", required = true, dataType = "String", paramType = "query"),
    })
    public Result klineDataTest(String seconds, String currencyId, String currencyTradeId, String start, String end) {
        Result result = new Result();
        result.setCode(Result.Code.ERROR);
        try {
            Map map = new HashMap<String, Object>();
            map.put("addTime", Integer.valueOf(seconds).intValue());
            map.put("currencyId", currencyId);
            map.put("currencyTradeId", currencyTradeId);
            map.put("start", Long.valueOf(start));
            map.put("end", Long.valueOf(end));
            List<Map> list = yangOrderService.getKlineDataTest(map);
            result.setCode(Result.Code.SUCCESS);
            result.setData(list);
        } catch (Exception e) {
            result.setMsg(e.getMessage());
            result.setCode(Result.Code.ERROR);
        }
        return result;
    }


    /**
     * K线数据
     *
     * @return 主要的
     */

    @RequestMapping(value = "klineDataApi", method = RequestMethod.POST)
    @ApiOperation(value = "K线数据", notes = "K线数据")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "seconds", value = "时间戳", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "currencyId", value = "币种ID", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "currencyTradeId", value = "交易市场ID", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "start", value = "开始时间", required = false, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "end", value = "结束时间", required = false, dataType = "String", paramType = "query"),
    })
    public Result klineDataApi(String seconds, String currencyId, String currencyTradeId, String start, String end) {
        Result result = new Result();
        result.setCode(Result.Code.ERROR);
        try {
            Map map = new HashMap<String, Object>();
            map.put("addTime", Integer.valueOf(seconds));
            map.put("currencyId", currencyId);
            map.put("currencyTradeId", currencyTradeId);
            map.put("start", Long.valueOf((StringUtils.isNotEmpty(start) ? start : "0")));
            map.put("end", Long.valueOf((StringUtils.isNotEmpty(end) ? end : "0")));
            List<Map> list = yangOrderService.getKlineDataApi(map);
            result.setCode(Result.Code.SUCCESS);
            result.setData(list);
        } catch (Exception e) {
            result.setMsg(e.getMessage());
            result.setCode(Result.Code.ERROR);
        }
        return result;
    }


    /**
     * K线数据
     *
     * @return
     */

    @RequestMapping(value = "delMongodb", method = RequestMethod.GET)
    @ApiOperation(value = "删除Mongodb", notes = "K线数据")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "delFlag", value = "删除标记", required = true, dataType = "String", paramType = "query"),
    })
    public Result delMongodb(String delFlag) {
        Result result = new Result();
        result.setCode(Result.Code.ERROR);
//        if("1".equals(delFlag)){
//        yangOrderService.delMongoDB();
//        }
        return result;
    }

    /**
     * 查询用户委托记录
     *
     * @return
     */

    @RequestMapping(value = "getUserOrderRecord", method = RequestMethod.POST)
    @ApiOperation(value = "查询用户委托记录", notes = "查询用户委托记录")
    public Result getUserOrderFiveRecord(@RequestBody Map<String,String> map) {
        Result result = new Result();
        Boolean blank = ParticipationIsBlankUtils.isBlank(map);
        if (blank){
            result.setCode(0);
            result.setMsg("参数格式错误");
        }
        Map<String,String> map1 = ParticipationRemoveBlankSpaceUtils.removeBlankSpace(map);
        String accessToken = map1.get("accessToken");
        YangMemberToken yangMemberToken = new YangMemberToken();
        yangMemberToken.setAccessToken(accessToken);
        result = yangOrderService.getFontUserOrderFiveRecord(map1, yangMemberToken);
        return result;
    }

    /**
     * 用户前交易记录
     *
     * @return
     */

    @RequestMapping(value = "getUserTradeRecord", method = RequestMethod.POST)
    @ApiOperation(value = "用户交易记录", notes = "用户交易记录")
    public Result getUserTradeFiveRecord(@RequestBody Map<String,String> map) {
        Result result = new Result();
        Boolean blank = ParticipationIsBlankUtils.isBlank(map);
        if (blank){
            result.setCode(0);
            result.setMsg("参数格式错误");
        }
        Map<String,String> map1 = ParticipationRemoveBlankSpaceUtils.removeBlankSpace(map);
        String accessToken = map1.get("accessToken");
        YangMemberToken yangMemberToken = new YangMemberToken();
        yangMemberToken.setAccessToken(accessToken);
        result = yangOrderService.getFontUserTradeFiveRecord(map1, yangMemberToken);
        return result;
    }

    /**
     * 每个币种最新交易时间
     *
     * @return
     */

    @RequestMapping(value = "getFontTradeLastTime", method = RequestMethod.POST)
    @ApiOperation(value = "每个币种最新交易时间", notes = "每个币种最新交易时间")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "currencyId", value = "币种Id", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "currencyTradeId", value = "交易区Id", required = true, dataType = "String", paramType = "query")
    })
    public Result getFontTradeLstTime(String currencyId, String currencyTradeId) {
        Result result = new Result();
        Map map = new HashMap();
        map.put("currencyId", currencyId);
        map.put("currencyTradeId", currencyTradeId);
        result = yangOrderService.getFontTradeLastTime(map);
        return result;
    }


    @RequestMapping(value = "cancelOrder", method = RequestMethod.POST)
    @ApiOperation(value = "撤单", notes = "撤单")
    public Result cancelOrder(@RequestBody Map<String,String> map) {
        Result result = new Result();
        Boolean blank = ParticipationIsBlankUtils.isBlank(map);
        if (blank){
            result.setCode(0);
            result.setMsg("参数格式错误");
        }
        Map<String,String> map1 = ParticipationRemoveBlankSpaceUtils.removeBlankSpace(map);
        String ordersId = map1.get("orderId");
        String accessToken = map.get("accessToken");
        result = yangOrderService.cancelOrder(ordersId, accessToken);
        return result;
    }

}
