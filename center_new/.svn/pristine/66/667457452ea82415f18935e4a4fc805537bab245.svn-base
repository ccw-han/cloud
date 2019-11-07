package net.cyweb.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import net.cyweb.model.Result;
import net.cyweb.model.YangCoinList;
import net.cyweb.model.YangFtActivity;
import net.cyweb.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Api(tags = "前端系统（用户）",position = 2)
@RequestMapping(value = "fontUser")
public class FontUserController {

    @Autowired
    private YangGoogleAuthService yangGoogleAuthService;

    @Autowired
    private YangMemberService yangMemberService;

    @Autowired
    private YangMemberLogService yangMemberLogService;

    @Autowired
    private YangCoinListService yangCoinListService;

    @Autowired
    private YangTibiOutService yangTibiOutService;

    @Autowired
    private YangOrderService yangOrderService;

    @Autowired
    private YangFtActivityService yangFtActivityService;


    @RequestMapping(value = "getGoogle",method = RequestMethod.POST)
    @ApiOperation(value = "查询谷歌验证信息", notes = "查询谷歌验证信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "accessToken", value = "用户Token", required = true, dataType = "String", paramType = "query")
    })
    public Result getGoogle(String accessToken){
        Result result=new Result();
        result=yangGoogleAuthService.getGoogle(accessToken);
        return result;
    }

    @RequestMapping(value = "googleBind",method = RequestMethod.POST)
    @ApiOperation(value = "绑定谷歌验证信息", notes = "绑定谷歌验证信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "accessToken", value = "用户Token", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "secret", value = "谷歌秘钥(必填)", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "loginLock", value = "是否开启登录谷歌验证(必填)", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "orderLock", value = "是否开启挂单谷歌验证(必填)", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "moneyLock", value = "是否开启提币谷歌验证(必填)", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "emailCode", value = "邮箱验证码(必填)", required = true, dataType = "String", paramType = "query")
    })
    public Result googleBind(String accessToken,String secret,String loginLock,String orderLock,String moneyLock,String emailCode){
        Result result=new Result();
        result=yangGoogleAuthService.googleBind(accessToken,secret,loginLock,orderLock,moneyLock,emailCode);
        return result;
    }


    @RequestMapping(value = "googleUpdate",method = RequestMethod.POST)
    @ApiOperation(value = "修改谷歌验证", notes = "修改谷歌验证")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "accessToken", value = "用户Token", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "loginLock", value = "是否开启登录谷歌验证(必填)", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "orderLock", value = "是否开启挂单谷歌验证(必填)", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "moneyLock", value = "是否开启提币谷歌验证(必填)", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "emailCode", value = "邮箱验证码(必填)", required = true, dataType = "String", paramType = "query")
    })
    public Result googleUpdate(String accessToken,String secret,String loginLock,String orderLock,String moneyLock,String emailCode){
        Result result=new Result();
        result=yangGoogleAuthService.googleUpdate(accessToken,loginLock,orderLock,moneyLock,emailCode);
        return result;
    }

    @RequestMapping(value = "googleCancle",method = RequestMethod.POST)
    @ApiOperation(value = "取消谷歌验证", notes = "取消谷歌验证")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "accessToken", value = "用户Token", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "emailCode", value = "邮箱验证码(必填)", required = true, dataType = "String", paramType = "query")
    })
    public Result googleCancle(String accessToken,String secret,String loginLock,String orderLock,String moneyLock,String emailCode){
        Result result=new Result();
        result=yangGoogleAuthService.googleCancle(accessToken,emailCode);
        return result;
    }


   /* @RequestMapping(value = "register",method = RequestMethod.POST)
    @ApiOperation(value = "注册", notes = "注册")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "email", value = "邮箱(用户名)(必填)", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "emailCode", value = "邮箱验证码(必填)", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "pwd", value = "密码", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "ip", value = "ip", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "pid", value = "邀请人用户id", required = true, dataType = "String", paramType = "query")
    })
    public Result register(String email,String emailCode,String pwd,String ip,String pid){
        Result result=new Result();
        result=yangMemberService.userRegister(email,emailCode,pwd,ip,pid);
        return result;
    }*/

    @RequestMapping(value = "addLog",method = RequestMethod.POST)
    @ApiOperation(value = "添加用户日志", notes = "添加用户日志")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "accessToken", value = "用户票据(必填)", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "ip", value = "ip", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "loginType", value = "登录类型", required = true, dataType = "String", paramType = "query")
    })
    public Result addLog(String accessToken,String ip,String loginType){
        Result result=new Result();
        result=yangMemberLogService.addLog(accessToken,ip,loginType);
        return result;
    }

    @RequestMapping(value = "getLog",method = RequestMethod.POST)
    @ApiOperation(value = "查询用户日志", notes = "查询用户日志")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "accessToken", value = "用户票据(必填)", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "ip", value = "ip(选填)", required = false, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "page", value = "页码", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "pageSize", value = "分页条数", required = true, dataType = "String", paramType = "query")
    })
    public Result getLog(String accessToken,String ip,String page,String pageSize){
        Result result=new Result();
        result=yangMemberLogService.getLog(accessToken,ip,Integer.valueOf(page),Integer.valueOf(pageSize));
        return result;
    }


    @RequestMapping(value = "applyCoin",method = RequestMethod.POST)
    @ApiOperation(value = "上币申请提交", notes = "上币申请提交")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "accessToken", value = "用户票据(必填)", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "fullName", value = "全称", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "subName", value = "简称", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "area", value = "地区", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "date", value = "上线日期", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "suan", value = "算法", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "type", value = "币种类型", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "total", value = "发行总量", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "liu", value = "流通量", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "price", value = "价格", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "isSell", value = "是否进行代币售卖", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "mu", value = "募集资金总量", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "url", value = "项目网址", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "wallet", value = "钱包下载地址", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "walletDesc", value = "钱包安装使用说明", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "api", value = "api", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "white", value = "白皮书", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "contact", value = "负责人联系方式", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "proDesc", value = "项目介绍", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "proGu", value = "项目价值", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "platform", value = "已上线平台", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "she", value = "社区地址", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "user", value = "活跃用户", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "send", value = "赠送代币活动方案", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "fei", value = "非小号连接", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "other", value = "其他", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "member_id", value = "用户Id", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "add_time", value = "申请时间", required = true, dataType = "String", paramType = "query")
    })
    public Result applyCoin(@Validated YangCoinList yangCoinList, String accessToken){
        Result result=new Result();
        result=yangCoinListService.applyCoin(yangCoinList,accessToken);
        return result;
    }

    @RequestMapping(value = "getTibiOutList",method = RequestMethod.POST)
    @ApiOperation(value = "查询提币记录", notes = "查询提币记录")
    @ApiImplicitParams({
          /*  @ApiImplicitParam(name = "accessToken", value = "用户票据(必填)", required = true, dataType = "String", paramType = "query"),*/
            @ApiImplicitParam(name = "currencyId", value = "币种id(选填)", required = false, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "page", value = "页码(选填)", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "pageSize", value = "每页条数(必填)", required = true, dataType = "String", paramType = "query")
    })
    public Result getTibiOutList(String accessToken,String currencyId,String page,String pageSize){
        Result result=new Result();
        result=yangTibiOutService.getTibiOutList(accessToken,currencyId,Integer.valueOf(page),Integer.valueOf(pageSize));
        return result;
    }


    @RequestMapping(value = "addTibiOut",method = RequestMethod.POST)
    @ApiOperation(value = "提币", notes = "提币")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "accessToken", value = "用户票据(必填)", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "currencyId", value = "币种id", required = false, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "fee", value = "手续费", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "addressId", value = "提币地址id", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "num", value = "提币数量", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "actualNum", value = "实际到账数量", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "ip", value = "用户Ip", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "emailCode", value = "邮箱验证码", required = true, dataType = "String", paramType = "query")
    })
    public Result addTibiOut(String accessToken,String currencyId,String fee,String addressId,String num,String actualNum,String ip,String emailCode){
        Result result=new Result();
        result=yangTibiOutService.addTibiOut(accessToken,currencyId,fee,addressId,num,actualNum,ip,emailCode);
        return result;
    }


    @RequestMapping(value = "searchOrder",method = RequestMethod.POST)
    @ApiOperation(value = "查询我的挂单", notes = "查询我的挂单")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "accessToken", value = "用户票据(必填)", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "page", value = "页码(必填)", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "pageSize", value = "分页条数(必填)", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "currencyId", value = "币种id(选填)", required = false, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "currencyTradeId", value = "交易市场id(选填)", required = false, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "begin", value = "开始时间(选填)", required = false, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "end", value = "结束时间(选填)", required = false, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "type", value = "类型(选填)", required = false, dataType = "String", paramType = "query")
    })
    public Result searchOrder(String accessToken,String page,String pageSize,String currencyId,String currencyTradeId,String begin,String end,String type){
        Result result=new Result();
        result=yangOrderService.searchOrder(accessToken,Integer.valueOf(page),Integer.valueOf(pageSize),currencyId,currencyTradeId,begin,end,type);
        return result;
    }


    @RequestMapping(value = "getFuntNum",method = RequestMethod.POST)
    @ApiOperation(value = "查询我的FUNT余额以及额度", notes = "查询我的FUNT余额以及额度")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "accessToken", value = "用户票据(必填)", required = true, dataType = "String", paramType = "query")
    })
    public Result getFuntNum(String accessToken){
        Result result=new Result();
        result=yangFtActivityService.getNum(accessToken);
        return result;
    }

    @RequestMapping(value = "getCardOrder",method = RequestMethod.POST)
    @ApiOperation(value = "查询我的FUNT会员卡购买记录", notes = "查询我的FUNT会员卡购买记录")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "accessToken", value = "用户票据(必填)", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "page", value = "用户票据(必填)", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "pageSize", value = "用户票据(必填)", required = true, dataType = "String", paramType = "query")
    })
    public Result getCardOrder(String accessToken,String page,String pageSize){
        Result result=new Result();
        result=yangFtActivityService.getCardOrder(accessToken,Integer.valueOf(page),Integer.valueOf(pageSize));
        return result;
    }

    @RequestMapping(value = "getFtLockRecord",method = RequestMethod.POST)
    @ApiOperation(value = "查询FUNT锁仓记录", notes = "查询FUNT锁仓记录")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "accessToken", value = "用户票据(必填)", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "page", value = "用户票据(必填)", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "pageSize", value = "用户票据(必填)", required = true, dataType = "String", paramType = "query")
    })
    public Result getFtLockRecord(String accessToken,String page,String pageSize){
        Result result=new Result();
        result=yangFtActivityService.getFtLockRecord(accessToken,Integer.valueOf(page),Integer.valueOf(pageSize));
        return result;
    }

    @RequestMapping(value = "getFtLockRecordSum",method = RequestMethod.POST)
    @ApiOperation(value = "查询FUNT锁仓余额及统计", notes = "查询FUNT锁仓余额及统计")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "accessToken", value = "用户票据(必填)", required = true, dataType = "String", paramType = "query")
    })
    public Result getFtLockRecordSum(String accessToken){
        Result result=new Result();
        result=yangFtActivityService.getFtLockRecordSum(accessToken);
        return result;
    }

    @RequestMapping(value = "getCardLock",method = RequestMethod.POST)
    @ApiOperation(value = "查询会员卡解锁日志", notes = "查询会员卡解锁日志")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "accessToken", value = "用户票据(必填)", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "page", value = "页码(必填)", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "pageSize", value = "分页条数(必填)", required = true, dataType = "String", paramType = "query")
    })
    public Result getCardLock(String accessToken,String page,String pageSize){
        Result result=new Result();
        result=yangFtActivityService.getCardLock(accessToken,Integer.valueOf(page),Integer.valueOf(pageSize));
        return result;
    }

    @RequestMapping(value = "loginRecord")
    @ApiOperation(value = "会员登录", notes = "会员登录")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "accessToken", value = "用户票据(必填)", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "ip", value = "ip", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "loginType", value = "登陆来源", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "content", value = "邮件内容", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "title", value = "标题", required = true, dataType = "String", paramType = "query")

    })
    public Result loginRecord(String accessToken,String ip,String loginType,String content,String title){
        Result result=yangMemberLogService.loginRecord(accessToken,ip,loginType,content,title);
        return result;
    }
}
