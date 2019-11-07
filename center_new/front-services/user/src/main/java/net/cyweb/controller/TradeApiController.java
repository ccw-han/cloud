package net.cyweb.controller;

import com.netflix.discovery.converters.Auto;
import cyweb.utils.CoinConst;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import net.cyweb.mapper.YangCurrencyPairMapper;
import net.cyweb.mapper.YangTradeFeeMapper;
import net.cyweb.mapper.YangTradeMapper;
import net.cyweb.model.*;
import net.cyweb.service.RedisService;
import net.cyweb.service.RobotService;
import net.cyweb.service.YangOrderService;
import net.cyweb.service.YangPairService;
import net.cyweb.validate.OrderQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

@RestController
@Api(tags = "交易",position = 2)
@RequestMapping(value = "trade")
public class TradeApiController extends ApiBaseController{

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

    @RequestMapping(value = "mountOrder",method = RequestMethod.POST)
    @ApiOperation(value = "挂单", notes = "挂单，买单卖单进队列")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "ordersId", value = "订单ID", required = true, dataType = "String", paramType = "query"),
    })
    public Result mountOrder(@ApiIgnore @Validated({OrderQueue.class}) YangOrders yangOrders, BindingResult bindingResult){
        Result result = new Result();
        result.setCode(Result.Code.ERROR);
        try{
            /*数据验证*/
            if(bindingResult.hasErrors()){
                result.setMsg(getStringErrors(bindingResult));
            }else{
                redisService.rPush(MQ_ORDER_QUEUE,yangOrders.getOrdersId());
                result.setCode(Result.Code.SUCCESS);
            }

        }catch (Exception e){
            result.setMsg(e.getMessage());
        }

        return result;
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "cId", value = "交易对id", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "price", value = "价格", required = true, dataType = "double", paramType = "query"),
            @ApiImplicitParam(name = "passwd", value = "交易密码", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "accessToken", value = "token", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "type", value = "交易类型", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "num", value = "交易数量", required = true, dataType = "String", paramType = "query"),
    })
    @ApiOperation(value = "生成订单", notes = "生成订单并自动进队列")
    @RequestMapping(value = "createorder",method = RequestMethod.POST)
    public Result createorder(String cId , BigDecimal price, double num, String passwd, YangMemberToken yangMemberToken, String type)
    {
        Result result = new Result();
        try {
            result = robotService.saleOrBuyRobot( cId ,  price, num, passwd, yangMemberToken, type,"0");
        }catch (Exception e)
        {
            result.setCode(Result.Code.ERROR);
            result.setMsg(e.getMessage());
            e.printStackTrace();
        }

        return result;

    }


    /**
     * 获取coin信息
     * @param pamas
     * @return
     */
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pamas", value = "凭借参数 currency_id-thread_id,currency_id-thread_id,currency_id-thread_id", required = true, dataType = "int", paramType = "query"),
    })
    @RequestMapping(value = "getCurrencyInfo",method = RequestMethod.POST)
    public Result  getCurrencyInfo(String pamas)
    {
        Result result = new Result();

        //24小时涨跌数据
        long endtimed  = System.currentTimeMillis() /1000;

        long starttime  = endtimed - 24*3600;


        try {
            result.setCode(Result.Code.SUCCESS);
            String[] pamas_arr = pamas.split(",");
            List list = new LinkedList();

            HashMap pama = new HashMap();
            pama.put("addTime",starttime);
            List<HashMap> maps = yangTradeMapper.selectMaxAndMinAndTotal(pama);
            java.text.DecimalFormat   df   = new   java.text.DecimalFormat("#.00");

            for(String s : pamas_arr)
            {
                String[] cct = s.split("-");
                CoinPairRedisBean coinPairRedisBean = yangOrderService.getCoinPairRedisBean(Integer.valueOf(cct[0]),Integer.valueOf(cct[1]));
                list.add(coinPairRedisBean);
                
            }


            result.setData(list);
        }catch (Exception e)
        {
            result.setCode(Result.Code.ERROR);
        }

        return result;

    }


    /**
     * 修改redis中currency pair的数据
     * @param cid
     * @return
     */
    @ApiImplicitParams({
            @ApiImplicitParam(name = "cid", value = "交易对id", required = true, dataType = "int", paramType = "query"),
    })
    @RequestMapping(value = "flushCurrencyPair",method = RequestMethod.POST)
    public boolean flushCurrencyPair(int cid)
    {
        try {
            YangCurrencyPair yangCurrencyPair = new YangCurrencyPair();
            yangCurrencyPair.setCyId(cid);
            YangCurrencyPair yangCurrencyPair1 = yangCurrencyPairMapper.selectOne(yangCurrencyPair);

            if(yangCurrencyPair1 != null)
            {
                yangPairService.getPairInfo(yangCurrencyPair1.getCurrencyId(),yangCurrencyPair1.getCurrencyTradeId(),true);
            }
        }catch (Exception e)
        {
            return false;
        }
        return  true;

    }




}
