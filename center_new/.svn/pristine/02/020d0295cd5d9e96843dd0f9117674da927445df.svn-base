package net.cyweb.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import net.cyweb.model.Result;
import net.cyweb.model.YangBank;
import net.cyweb.service.*;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@Api(tags = "前端系统(交易)", position = 2)
@RequestMapping(value = "fontTrade")
public class FontTradeController {

    @Autowired
    private YangCurrencyUserService yangCurrencyUserService;
    @Autowired
    private YangC2CAssetService yangC2CAssetService;

    @Autowired
    private YangC2CGuaService yangC2CGuaService;

    @Autowired
    private YangC2COrdersService yangC2COrdersService;

    @Autowired
    private YangBankService yangBankService;

    @Autowired
    private YangCurrencyMarketService yangCurrencyMarketService;

    @Autowired
    private YangCurrencyService yangCurrencyService;

    /***
     * WF
     * 查询C2C资产余额
     * @param map
     * @return
     */
    @RequestMapping(value = "selectC2CAssets", method = RequestMethod.POST)
    @ApiOperation(value = "查询C2C资产余额", notes = "查询C2C资产余额")
    public Result cancleBuy(@RequestBody Map<String, String> map) {
        Result result = new Result();
        result = yangC2CAssetService.getAsset(map);
        return result;
    }

    /***
     * WF
     * C2C资产划转
     * @param map
     * @return
     */
    @RequestMapping(value = "c2cAssetChange", method = RequestMethod.POST)
    @ApiOperation(value = "C2C资产划转", notes = "C2C资产划转")
    public Result c2cAssetChange(@RequestBody Map<String, String> map ,HttpServletRequest request) {
        Result result = new Result();
        result = yangC2CAssetService.c2cAssetChange(map,request);
        return result;
    }

    /***
     * WF
     * 查询C2C挂单列表
     * @param map
     * @return
     */
    @RequestMapping(value = "getGuaList", method = RequestMethod.POST)
    @ApiOperation(value = "查询C2C挂单列表", notes = "查询C2C挂单列表")
    public Result getGuaList(@RequestBody Map<String, String> map) {
        Result result = new Result();
        result = yangC2CGuaService.getGuaList(map);
        return result;
    }

    /***
     * WF
     * 查询一条C2C挂单
     * @param map
     * @return
     */
    @RequestMapping(value = "getGua", method = RequestMethod.POST)
    @ApiOperation(value = "查询一条C2C挂单", notes = "查询一条C2C挂单")
    public Result getGua(@RequestBody Map<String, String> map) {
        Result result = new Result();
        result = yangC2CGuaService.getGua(map);
        return result;
    }

    /***
     * WF
     * 查询用户c2c订单列表
     * @param map
     * @return
     */
    @RequestMapping(value = "getC2COrderList", method = RequestMethod.POST)
    @ApiOperation(value = "查询用户c2c订单列表", notes = "查询用户c2c订单列表")
    public Result getC2COrderList(@RequestBody Map<String, String> map) {
        Result result = new Result();
        result = yangC2COrdersService.getC2COrderList(map);
        return result;
    }

    /***
     * WF
     * 查询一条C2C订单
     * @param map
     * @return
     */
    @RequestMapping(value = "getC2COrder", method = RequestMethod.POST)
    @ApiOperation(value = "查询一条C2C订单", notes = "查询一条C2C订单")
    public Result getC2COrder(@RequestBody Map<String, String> map) {
        Result result = new Result();
        result = yangC2COrdersService.getC2COrder(map);
        return result;
    }


    /***
     * WF
     * C2C购买
     * @param map
     * @return
     */

    @RequestMapping(value = "c2cOrdersBuy", method = RequestMethod.POST)
    @ApiOperation(value = "C2C购买", notes = "C2C购买")
    public Result c2cOrdersBuy(@RequestBody Map<String, String> map) {
        Result result = new Result();
        result = yangC2COrdersService.c2cOrdersBuy(map);
        return result;
    }

    /***
     * WF
     * C2C确认汇款
     * @param map
     * @return
     */
    @RequestMapping(value = "confirmBuy", method = RequestMethod.POST)
    @ApiOperation(value = "C2C确认汇款", notes = "C2C确认汇款")
    public Result confirmBuy(@RequestBody Map<String, String> map) {
        Result result = new Result();
        result = yangC2COrdersService.confirmBuy(map);
        result.setData(null);
        return result;
    }

    /***
     * WF
     * C2C卖出
     * @param map
     * @return
     */

    @RequestMapping(value = "c2cOrdersSell", method = RequestMethod.POST)
    @ApiOperation(value = "C2C卖出", notes = "C2C卖出")
    public Result c2cOrdersSell(@RequestBody Map<String, String> map) {
        Result result = new Result();
        result = yangC2COrdersService.c2cOrdersSell(map);
        return result;
    }

    /***
     * WF
     * C2C撤销购买
     * @param map
     * @return
     */

    @RequestMapping(value = "cancelBuy", method = RequestMethod.POST)
    @ApiOperation(value = "C2C撤销购买", notes = "C2C撤销购买")
    public Result cancelBuy(@RequestBody Map<String, String> map) {
        Result result = new Result();
        result = yangC2COrdersService.cancelBuy(map);
        return result;
    }


     /*@RequestMapping(value = "getBankList",method = RequestMethod.POST)
    @ApiOperation(value = "获取银行卡列表", notes = "获取银行卡列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "accessToken", value = "用户Token", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "id", value = "用户Token", required = false, dataType = "String", paramType = "query")
    })
    public Result getBankList(String accessToken,String id){
        Result result=new Result();
        result=yangBankService.getBankList(accessToken,id);
        return result;
    }

   @RequestMapping(value = "addBank",method = RequestMethod.POST)
    @ApiOperation(value = "新增银行卡", notes = "新增银行卡")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "accessToken", value = "用户Token", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "cardnum", value = "银行卡号", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "bankname", value = "银行名称", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "cardname", value = "银行卡姓名", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "address", value = "银行地址", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "bankBranch", value = "所属支行", required = true, dataType = "String", paramType = "query")
    })
    public Result addBank(@Validated YangBank yangBank, String accessToken){
        Result result=new Result();
        result=yangBankService.addBank(yangBank,accessToken);
        return result;
    }

    @RequestMapping(value = "delBank",method = RequestMethod.POST)
    @ApiOperation(value = "删除银行卡", notes = "删除银行卡")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "accessToken", value = "用户Token", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "id", value = "银行卡id", required = true, dataType = "String", paramType = "query")
    })
    public Result delBank(String accessToken,String id){
        Result result=new Result();
        result=yangBankService.delBank(accessToken,id);
        return result;
    }

    @RequestMapping(value = "getCurrencyMarket",method = RequestMethod.POST)
    @ApiOperation(value = "查询币种交易市场", notes = "查询币种交易市场")
    @ApiImplicitParams({
    })
    public Result getCurrencyMarket(){
        Result result=new Result();
        result=yangCurrencyMarketService.getCurrencyMarket();
        return result;
    }

    @RequestMapping(value = "getCurrencyETH",method = RequestMethod.POST)
    @ApiOperation(value = "查询以太坊系列币种", notes = "查询以太坊系列币种")
    @ApiImplicitParams({
    })
    public Result getCurrencyETH(){
        Result result=new Result();
        result=yangCurrencyService.getCurrencyETH();
        return result;
    }*/
}
