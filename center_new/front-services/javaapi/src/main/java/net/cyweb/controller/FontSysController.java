package net.cyweb.controller;

import cyweb.utils.ErrorCode;
import io.swagger.annotations.*;
import net.cyweb.config.custom.Base64Utils;
import net.cyweb.model.Result;
import net.cyweb.model.UserProPs;
import net.cyweb.service.*;
import org.bouncycastle.asn1.ocsp.ResponseData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.apache.commons.codec.binary.Base64;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URLDecoder;

@RestController
@Api(tags = "前端系统（系统）",position = 2)
@RequestMapping(value = "fontSys")
public class FontSysController {

    @Autowired
    private FunFlashService funFlashService;

    @Autowired
    private YangArticleService yangArticleService;

    @Autowired
    private YangConfigService yangConfigService;

    @Autowired
    private YangCurrencyPairService yangCurrencyPairService;

    @Autowired
    private YangTradeFeeService yangTradeFeeService;

    @Autowired
    private YangTimeLineService yangTimeLineService;

    @Autowired
    private YangAreaService yangAreaService;

    @Autowired
    private YangCurrencyFindService yangCurrencyFindService;

    @Autowired
    private YangQianBaoAddressService yangQianBaoAddressService;

    @Autowired
    private YangCurrencyService yangCurrencyService;

    @Autowired
    private YangOrderService yangOrderService;


    @Autowired
    private YangMemberService yangMemberService;

    @Autowired
    private UserProPs userProPs;

    @RequestMapping(value = "getArticleDetail",method = RequestMethod.POST)
    @ApiOperation(value = "获取文章详情", notes = "获取文章详情")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "language", value = "语言", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "articleId", value = "文章Id", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "accessToken", value = "用户Token", required = false, dataType = "String", paramType = "query")
    })
    public Result getArticleDetail(String language,String articleId,String accessToken){
        Result result=new Result();
        result=yangArticleService.getArticleDetail(language,articleId,accessToken);
        return result;
    }

    @RequestMapping(value = "getConfig",method = RequestMethod.POST)
    @ApiOperation(value = "获取网站配置", notes = "获取网站配置")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "configName", value = "配置名称", required = true, dataType = "String", paramType = "query")
    })
    public Result getConfig(String configName){
        Result result=new Result();
        result=yangConfigService.getConfig(configName);
        return result;
    }



    @RequestMapping(value = "getTradeFee",method = RequestMethod.POST)
    @ApiOperation(value = "getTradeFee", notes = "获取自定义交易手续费")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "accessToken", value = "用户Token", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "cyId", value = "交易对Id", required = true, dataType = "String", paramType = "query")
    })
    public Result getTradeFee(String accessToken,String cyId){
        Result result=new Result();
        result=yangTradeFeeService.getTradeFee(accessToken,cyId);
        return result;
    }

    @RequestMapping(value = "getCurrencyPairOrder",method = RequestMethod.POST)
    @ApiOperation(value = "getCurrencyPairOrder", notes = "获取交易对挂单")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "type", value = "买卖类型", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "cyId", value = "交易对Id", required = true, dataType = "String", paramType = "query")
    })
    public Result getCurrencyPairOrder(String type,String cyId){
        Result result=new Result();
        result= yangCurrencyPairService.getCurrencyPairOrder(type,cyId);
        return result;
    }

    @RequestMapping(value = "getCurrencyPairTrade",method = RequestMethod.POST)
    @ApiOperation(value = "获取交易对成交", notes = "获取交易对成交")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "cyId", value = "交易对Id", required = true, dataType = "String", paramType = "query")
    })
    public Result getCurrencyPairTrade(String cyId){
        Result result=new Result();
        result= yangCurrencyPairService.getCurrencyPairTrade(cyId);
        return result;
    }

    @RequestMapping(value = "searchTrade",method = RequestMethod.POST)
    @ApiOperation(value = "查询我的成交", notes = "查询我的成交")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "accessToken", value = "用户票据(必填)", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "page", value = "页码", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "pageSize", value = "每页条数(必填)", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "currencyId", value = "币种id(选填)", required = false, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "currencyTradeId", value = "交易市场(选填)", required = false, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "begin", value = "开始时间(选填)", required = false, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "end", value = "结束时间(选填)", required = false, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "type", value = "类型(选填)", required = false, dataType = "String", paramType = "query")
    })
    public Result searchTrade(String accessToken,String page,String pageSize,String currencyId,String currencyTradeId,String begin,String end,String type){
        Result result=new Result();
        result= yangOrderService.searchTrade(accessToken,Integer.valueOf(page),Integer.valueOf(pageSize),currencyId,currencyTradeId,begin,end,type);
        return result;
    }


    @RequestMapping(value = "getCurrencyPair",method = RequestMethod.POST)
    @ApiOperation(value = "getCurrencyPair", notes = "查询交易对信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "cyId", value = "交易对Id", required = false, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "currencyId", value = "币种Id", required = false, dataType = "String", paramType = "query")
    })
    public Result getCurrencyPair(String cyId,String currencyId){
        Result result=new Result();
        result= yangCurrencyPairService.getCurrencyPair(cyId,currencyId);
        return result;
    }

    @RequestMapping(value = "addTimeLine",method = RequestMethod.POST)
    @ApiOperation(value = "addTimeLine", notes = "添加时间轴信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "accessToken", value = "用户票据(必填)", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "type", value = "类型(必填)", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "content", value = "说明(必填)", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "json", value = "补充信息(选填)", required = false, dataType = "String", paramType = "query")
    })
    public Result addTimeLine(String accessToken,String type,String content,String json){
        Result result=new Result();
        result=yangTimeLineService.addTimeLine(accessToken,type,content,json);
        return result;
    }

    @RequestMapping(value = "getTimeLine",method = RequestMethod.POST)
    @ApiOperation(value = "getTimeLine", notes = "查询时间轴信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "accessToken", value = "用户票据(必填)", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "type", value = "类型(必填)", required = false, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "start", value = "开始时间戳(选填)", required = false, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "end", value = "结束时间(选填)", required = false, dataType = "String", paramType = "query")
    })
    public Result getTimeLine(String accessToken,String type,String start,String end){
        Result result=new Result();
        result=yangTimeLineService.getTimeLine(accessToken,type,start,end);
        return result;
    }


    @RequestMapping(value = "getArea",method = RequestMethod.POST)
    @ApiOperation(value = "获取地区", notes = "获取地区")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "parentId", value = "父亲级", required = true, dataType = "String", paramType = "query")
    })
    public Result getArea(String parentId){
        Result result=new Result();
        result=yangAreaService.getArea(parentId);
        return result;
    }


    @RequestMapping(value = "currencyFindAdd",method = RequestMethod.POST)
    @ApiOperation(value = "提交找回币种工单", notes = "提交找回币种工单")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "accessToken", value = "用户票据(必填)", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "num", value = "数量", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "address", value = "充币地址", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "currencyId", value = "币种id", required = true, dataType = "String", paramType = "query")
    })
    public Result currencyFindAdd(String accessToken,String num,String address,String currencyId){
        Result result=new Result();
        result=yangCurrencyFindService.currencyFindAdd(accessToken,num,address,currencyId);
        return result;
    }

    @RequestMapping(value = "currencyFindList",method = RequestMethod.POST)
    @ApiOperation(value = "提交找回币种工单", notes = "提交找回币种工单")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "accessToken", value = "用户票据(必填)", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "page", value = "页码", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "pageSize", value = "分页条数(必填)", required = true, dataType = "String", paramType = "query")
    })
    public Result currencyFindList(String accessToken,String page,String pageSize){
        Result result=new Result();
        result=yangCurrencyFindService.currencyFindList(accessToken,Integer.valueOf(page),Integer.valueOf(pageSize));
        return result;
    }

    @RequestMapping(value = "getQianbaoAddress",method = RequestMethod.POST)
    @ApiOperation(value = "查询提币地址", notes = "查询提币地址")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "accessToken", value = "用户票据(必填)", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "currencyId", value = "币种id(选填)", required = false, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "id", value = "地址Id", required = false, dataType = "String", paramType = "query"),
    })
    public Result getQianbaoAddress(String accessToken,String currencyId,String id){
        Result result=new Result();
        result=yangQianBaoAddressService.getQianbaoAddress(accessToken,currencyId,id);
        return result;
    }


    @RequestMapping(value = "addQianbaoAddress",method = RequestMethod.POST)
    @ApiOperation(value = "添加提币地址", notes = "添加提币地址")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "accessToken", value = "用户票据(必填)", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "currencyId", value = "币种id(选填)", required = false, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "name", value = "币种id(选填)", required = false, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "qianbaoUrl", value = "币种id(选填)", required = false, dataType = "String", paramType = "query")
    })
    public Result addQianbaoAddress(String accessToken,String currencyId,String name,String qianbaoUrl){
        Result result=new Result();
        result=yangQianBaoAddressService.addQianbaoAddress(accessToken,currencyId,name,qianbaoUrl);
        return result;
    }

    @RequestMapping(value = "delQianbaoAddress",method = RequestMethod.POST)
    @ApiOperation(value = "删除提币地址", notes = "删除提币地址")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "accessToken", value = "用户票据(必填)", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "id", value = "币种id(选填)", required = false, dataType = "String", paramType = "query")
    })
    public Result delQianbaoAddress(String accessToken,String id){
        Result result=new Result();
        result=yangQianBaoAddressService.delQianbaoAddress(accessToken,id);
        return result;
    }


    @RequestMapping(value = "getCurrency",method = RequestMethod.POST)
    @ApiOperation(value = "查询币种", notes = "查询币种")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "currencyId", value = "币种Id", required = false, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "searchText", value = "搜索币种名称(选填)", required = false, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "currencyMark", value = "搜索币种名称(选填)", required = false, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "market", value = "所属市场(选填)", required = false, dataType = "String", paramType = "query")
    })
    public Result getCurrency(String currencyId,String searchText,String currencyMark,String market){
        Result result=new Result();
        result=yangCurrencyService.getCurrency(currencyId,searchText,currencyMark,market);
        return result;
    }

    @RequestMapping(value = "getCategory",method = RequestMethod.POST)
    @ApiOperation(value = "查询栏目", notes = "查询栏目")
    @ApiImplicitParams({
    })
    public Result getCategory(String language){
        Result result=new Result();
        result=yangArticleService.getCategory();
        return result;
    }

    @RequestMapping(value = "readImage")
    @ApiOperation(value = "读取图片", notes = "读取图片")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "path", value = "路径", required = false, dataType = "String", paramType = "query")
    })
    public Result readImage(String path){
        Result result=new Result();
        try{
            path=URLDecoder.decode(path,"UTF-8");
            result.setCode(Result.Code.SUCCESS);
            result.setData(Base64Utils.readImage(path,userProPs.getFtp().get("newReadHost"),userProPs.getFtp().get("oldReadHost"),userProPs.getFtp().get("newReadHost2"),userProPs.getFtp().get("rootPath")));
        }catch (Exception e){
            e.printStackTrace();
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
            result.setMsg(ErrorCode.ERROR_SYSTEM.getMessage());
            result.setCode(Result.Code.ERROR);
        }
        return result;
    }


    @RequestMapping(value = "getFlash")
    @ApiOperation(value = "获取幻灯片", notes = "获取幻灯片")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "language", value = "语言", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "type", value = "类型", required = true, dataType = "String", paramType = "query")
    })
    public Result getFlash(String language,String type){
        Result result=new Result();
        //繁体->英文   韩语->英文
        result=funFlashService.getFlash(language,type);
        return result;
    }


    @RequestMapping(value = "getArticleList",method = RequestMethod.POST)
    @ApiOperation(value = "获取文章列表", notes = "获取文章列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "language", value = "语言", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "type", value = "类型", required = false, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "searchText", value = "搜索内容", required = false, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "page", value = "页码", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "pageSize", value = "分页条数", required = true, dataType = "String", paramType = "query")
    })
    public Result getArticleList(String language,String type,String searchText,int page,int pageSize){
        Result result=new Result();
        result=yangArticleService.getArticleList(language,type,searchText,page,pageSize);
        return result;
    }

    @RequestMapping(value = "getCurrencyListChange" ,method = RequestMethod.POST)
    @ApiOperation(value = "获取交易对行情列表", notes = "获取交易对行情列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "ids", value = "交易对id,多个id以逗号拼接(选填)", required = false, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "searchText", value = "模糊搜索交易对名称(选填)", required = false, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "isMine", value = "是否挖矿区", required = false, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "currencyTradeId", value = "交易市场Id", required = false, dataType = "String", paramType = "query")

    })
    public Result getCurrencyListChange(String ids,String searchText,String isMine,String currencyTradeId){
        Result result=new Result();
        result=yangCurrencyPairService.getCurrencyListChange(ids,searchText,isMine,currencyTradeId);
        return result;
    }

    @RequestMapping(value = "getCurrencyDetailChange",method = RequestMethod.POST)
    @ApiOperation(value = "获取单个交易对行情", notes = "获取交易对行情列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "currencyId", value = "币种Id", required = false, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "currencyTradeId", value = "交易市场Id", required = false, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "cyId", value = "交易对Id", required = false, dataType = "String", paramType = "query")

    })
    public Result getCurrencyDetailChange(String currencyId,String currencyTradeId,String cyId){
        Result result=new Result();
        result=yangCurrencyPairService.getCurrencyDetailChange(currencyId,currencyTradeId,cyId);
        return result;
    }



    @RequestMapping(value = "initWeb")
    @ApiOperation(value = "网站初始化", notes = "网站初始化")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "language", value = "语言", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "type", value = "类型", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "ids", value = "交易对id,多个id以逗号拼接(选填)", required = false, dataType = "String", paramType = "query")
    })
    public Result initWeb(String language,String type,String ids){
        Result result=new Result();
        result=funFlashService.initWeb(language,type,ids);
        return result;
    }


    /*上传图片*/
    @ApiOperation(value = "上传图片", notes = "上传图片")
    @ApiImplicitParams({
    })
    @PostMapping(value="upload",headers="content-type=multipart/form-data")
    public Result upload(@ApiParam(value="pic", required = true) MultipartFile pic){
        Result result = new Result();
        result=yangMemberService.saveImage(pic,"0");
        return result;
    }

    /*上传图片*/
    @ApiOperation(value = "上传图片", notes = "上传图片")
    @ApiImplicitParams({
    })
    @PostMapping(value="uploadByUser",headers="content-type=multipart/form-data")
    public Result uploadByUser(@ApiParam(value="pic", required = true) MultipartFile pic){
        Result result = new Result();
        result=yangMemberService.saveImage(pic,"1");
        return result;
    }

    /**
     * 交易对初始化信息
     * @param language
     * @param type
     * @param ids
     * @return
     */
    @RequestMapping(value = "getTradeData")
    @ApiOperation(value = "交易对区初始化", notes = "交易区初始化")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "language", value = "语言", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "cyId", value = "交易对Id", required = true, dataType = "String", paramType = "query")
    })
    public Result getTradeData(String language,String cyId){
        Result result=new Result();
        result=funFlashService.getTradeData(language,cyId);
        return result;
    }



    /**
     * 交易对初始化信息
     * @param language
     * @param type
     * @param ids
     * @return
     */
    @RequestMapping(value = "getUserTradeData")
    @ApiOperation(value = "用户交易区初始化", notes = "用户交易区初始化")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "accessToken", value = "用户token", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "cyId", value = "交易对Id", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "currencyId", value = "币种Id", required = true, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "currencyTradeId", value = "交易区Id", required = true, dataType = "int", paramType = "query")
    })
    public Result getUserTradeData(String accessToken,String cyId,int currencyId,int currencyTradeId){
        Result result=new Result();
        result=funFlashService.getUserTradeData(accessToken,cyId,currencyId,currencyTradeId);
        return result;
    }
}
