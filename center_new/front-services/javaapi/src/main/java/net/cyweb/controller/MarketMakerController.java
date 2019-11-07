//package net.cyweb.controller;
//
//import io.swagger.annotations.Api;
//import io.swagger.annotations.ApiImplicitParam;
//import io.swagger.annotations.ApiImplicitParams;
//import io.swagger.annotations.ApiOperation;
//import net.cyweb.model.Result;
//import net.cyweb.service.YangUserKeysService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestMethod;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.math.BigDecimal;
//
//@RestController
//@Api(tags = "做市商接口",position = 2)
//@RequestMapping(value = "marketMaker")
//public class MarketMakerController {
//
//    @Autowired
//    private YangUserKeysService yangUserKeysService;
//
//
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "cyInfo", value = "交易对id", required = true, dataType = "String", paramType = "query"),
//            @ApiImplicitParam(name = "price", value = "价格", required = true, dataType = "double", paramType = "query"),
//            @ApiImplicitParam(name = "type", value = "交易类型", required = true, dataType = "String", paramType = "query"),
//            @ApiImplicitParam(name = "num", value = "交易数量", required = true, dataType = "String", paramType = "query"),
//            @ApiImplicitParam(name = "key", value = "公钥", required = true, dataType = "String", paramType = "query"),
//            @ApiImplicitParam(name = "nonce", value = "时间戳", required = true, dataType = "String", paramType = "query"),
//            @ApiImplicitParam(name = "signature", value = "签名", required = true, dataType = "String", paramType = "query")
//    })
//    @ApiOperation(value = "挂单", notes = "挂单交易")
//    @RequestMapping(value = "createorder",method = RequestMethod.POST)
//    public Result createorder(String cyInfo,String price,String type,String num,String key,String signature,String nonce){
//        return yangUserKeysService.checkCreateOrder(cyInfo,new BigDecimal(price),type,new BigDecimal(num),key,signature,nonce);
//    }
//
//
//
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "cyInfo", value = "交易对id", required = true, dataType = "String", paramType = "query"),
//            @ApiImplicitParam(name = "begin", value = "开始时间戳", required = false, dataType = "double", paramType = "query"),
//            @ApiImplicitParam(name = "end", value = "结束时间戳", required = false, dataType = "String", paramType = "query"),
//            @ApiImplicitParam(name = "type", value = "类型", required = true, dataType = "String", paramType = "query"),
//            @ApiImplicitParam(name = "key", value = "公钥", required = true, dataType = "String", paramType = "query"),
//            @ApiImplicitParam(name = "nonce", value = "时间戳", required = true, dataType = "String", paramType = "query"),
//            @ApiImplicitParam(name = "signature", value = "签名", required = true, dataType = "String", paramType = "query")
//    })
//    @ApiOperation(value = "我的挂单", notes = "我的挂单")
//    @RequestMapping(value = "queryoOrderList",method = RequestMethod.POST)
//    public Result queryoOrderList(String cyInfo,String begin,String end,String type,String key,String signature,String nonce){
//        return yangUserKeysService.queryOrderList(cyInfo,begin,end,type,key,signature,nonce);
//    }
//
//
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "cyInfo", value = "交易对id", required = true, dataType = "String", paramType = "query"),
//            @ApiImplicitParam(name = "begin", value = "开始时间戳", required = false, dataType = "double", paramType = "query"),
//            @ApiImplicitParam(name = "end", value = "结束时间戳", required = false, dataType = "String", paramType = "query"),
//            @ApiImplicitParam(name = "type", value = "类型", required = true, dataType = "String", paramType = "query"),
//            @ApiImplicitParam(name = "key", value = "公钥", required = true, dataType = "String", paramType = "query"),
//            @ApiImplicitParam(name = "nonce", value = "时间戳", required = true, dataType = "String", paramType = "query"),
//            @ApiImplicitParam(name = "signature", value = "签名", required = true, dataType = "String", paramType = "query")
//    })
//    @ApiOperation(value = "我的成交", notes = "我的成交")
//    @RequestMapping(value = "queryTradeList",method = RequestMethod.POST)
//    public Result queryTradeList(String cyInfo,String begin,String end,String type,String key,String signature,String nonce){
//        return yangUserKeysService.queryTradeList(cyInfo,begin,end,type,key,signature,nonce);
//    }
//
//
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "ordersId", value = "订单Id", required = true, dataType = "String", paramType = "query"),
//            @ApiImplicitParam(name = "key", value = "公钥", required = true, dataType = "String", paramType = "query"),
//            @ApiImplicitParam(name = "nonce", value = "时间戳", required = true, dataType = "String", paramType = "query"),
//            @ApiImplicitParam(name = "signature", value = "签名", required = true, dataType = "String", paramType = "query")
//    })
//    @ApiOperation(value = "撤单", notes = "撤单")
//    @RequestMapping(value = "cheOrder",method = RequestMethod.POST)
//    public Result cheOrder(String ordersId,String key,String signature,String nonce){
//        return yangUserKeysService.cheOrder(ordersId,key,signature,nonce);
//    }
//
//
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "key", value = "公钥", required = true, dataType = "String", paramType = "query"),
//            @ApiImplicitParam(name = "nonce", value = "时间戳", required = true, dataType = "String", paramType = "query"),
//            @ApiImplicitParam(name = "signature", value = "签名", required = true, dataType = "String", paramType = "query")
//    })
//    @ApiOperation(value = "我的资产", notes = "我的资产")
//    @RequestMapping(value = "queryAsset",method = RequestMethod.POST)
//    public Result queryAsset(String key,String signature,String nonce){
//        return yangUserKeysService.queryAsset(key,signature,nonce);
//    }
//
//}
//
