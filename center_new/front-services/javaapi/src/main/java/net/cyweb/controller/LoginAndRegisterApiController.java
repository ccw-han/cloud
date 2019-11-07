//package net.cyweb.controller;
//
//import cyweb.utils.CoinConst;
//import cyweb.utils.CoinConstUser;
//import cyweb.utils.CommonTools;
//import cyweb.utils.ErrorCode;
//import io.swagger.annotations.Api;
//import io.swagger.annotations.ApiImplicitParam;
//import io.swagger.annotations.ApiImplicitParams;
//import io.swagger.annotations.ApiOperation;
//import net.cyweb.exception.EmailNotExistException;
//import net.cyweb.exception.EmailRegistRepeatException;
//import net.cyweb.exception.MemberIsLocketException;
//import net.cyweb.exception.PasswordErrorException;
//import net.cyweb.model.Result;
//import net.cyweb.model.YangMember;
//import net.cyweb.service.RedisService;
//import net.cyweb.service.YangMemberService;
//import net.cyweb.validate.LoginValidateGroup;
//import net.cyweb.validate.RegisterValidateGroup;
//import org.apache.commons.lang.time.DateUtils;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.validation.BindingResult;
//import org.springframework.validation.annotation.Validated;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestMethod;
//import org.springframework.web.bind.annotation.RestController;
//import springfox.documentation.annotations.ApiIgnore;
//
//import javax.validation.Valid;
//import java.math.BigInteger;
//import java.security.MessageDigest;
//import java.util.concurrent.TimeUnit;
//
//
//@RestController
//@Api(tags = "注册登录接口",position = 1)
//@RequestMapping(value = "loginAndRegister")
//public class LoginAndRegisterApiController extends ApiBaseController{
//
//    @Autowired
//    private YangMemberService yangMemberService;
//
//    @Autowired
//    private RedisService redisService;
//
//    /*注册接口*/
//    @RequestMapping(value = "register",method = RequestMethod.POST)
//    @ApiOperation(value = "注册", notes = "用户注册")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "email", value = "注册用户名", required = true, dataType = "String", paramType = "query"),
//            @ApiImplicitParam(name = "pwd", value = "登录密码", required = true, dataType = "String", paramType = "query"),
////            @ApiImplicitParam(name = "pwdtrade", value = "交易密码", required = true, dataType = "String", paramType = "query"),
//            @ApiImplicitParam(name = "invitecode", value = "邀请码", required = false, dataType = "String", paramType = "query"),
////            @ApiImplicitParam(name = "cardtype", value = "证件类型", required = true, dataType = "String", paramType = "query"),
////            @ApiImplicitParam(name = "idcard", value = "证件号码", required = true, dataType = "String", paramType = "query"),
////            @ApiImplicitParam(name = "name", value = "姓名", required = true, dataType = "String", paramType = "query"),
//            @ApiImplicitParam(name = "ip", value = "IP", required = true, dataType = "String", paramType = "query")
//    })
//    public Result register(@ApiIgnore @Validated(value = {RegisterValidateGroup.class}) YangMember yangMember, BindingResult bindingResult){
//        Result result = new Result();
//        result.setCode(Result.Code.ERROR);
//        try{
//            /*数据验证*/
//            if(bindingResult.hasErrors()){
//                result.setMsg(getStringErrors(bindingResult));
//            }else{
//                yangMemberService.register(yangMember);
//                result.setCode(Result.Code.SUCCESS);
//            }
//        }catch (EmailRegistRepeatException e){
//            result.setErrorCode(ErrorCode.ERROR_EMAIL_REPEAT.getIndex());
//            result.setMsg(ErrorCode.ERROR_EMAIL_REPEAT.getMessage());
//
//        }catch (Exception e){
//            e.printStackTrace();
//            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
//            result.setMsg(e.getMessage());
//        }
//        return result;
//    }
//
//
//    /*登录接口*/
//    @RequestMapping(value = "login",method = RequestMethod.POST)
//    @ApiOperation(value = "登录", notes = "用户登录")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "email", value = "注册用户名", required = true, dataType = "String", paramType = "query"),
//            @ApiImplicitParam(name = "pwd", value = "登录密码", required = true, dataType = "String", paramType = "query"),
//            @ApiImplicitParam(name = "loginIp", value = "登陆ip", required = true, dataType = "String", paramType = "query"),
//            @ApiImplicitParam(name = "loginType", value = "登陆来源", required = true, dataType = "int", paramType = "query")
//    })
//    public Result login(@ApiIgnore @Validated(value = {LoginValidateGroup.class}) YangMember yangMember, BindingResult bindingResult){
//         Result result = new Result();
//        result.setCode(Result.Code.ERROR);
//        try{
//            /*数据验证*/
//            if(bindingResult.hasErrors()){
//                result.setMsg(getStringErrors(bindingResult));
//            }else{
//
//                yangMember.setLoginTime(BigInteger.valueOf(System.currentTimeMillis()/1000).intValue());
//                YangMember loginMember = yangMemberService.login(yangMember);
//                //设置用户的token
//                String token = CommonTools.Md5(System.currentTimeMillis()+loginMember.getLoginIp());
//
////                redisService.set(CoinConst.TOKENKEYMEMBER+loginMember.getMemberId().intValue(),token, CoinConstUser.LOGIN_TIME_OUT, TimeUnit.SECONDS);
////                redisService.set(CoinConst.TOKENKEYTOKEN+token,loginMember.getMemberId(),CoinConstUser.LOGIN_TIME_OUT, TimeUnit.SECONDS);
//
//                if(CoinConst.LOGIN_TYP_PHONE==yangMember.getLoginType()){
//                    redisService.set(CoinConst.TOKENKEYMEMBER+CoinConst.LOGIN_TYP_PHONE+"_"+loginMember.getMemberId().intValue(),token, CoinConstUser.LOGIN_TIME_OUT_APP, TimeUnit.SECONDS);
//                    redisService.set(CoinConst.TOKENKEYTOKEN+CoinConst.LOGIN_TYP_PHONE+"_"+token,loginMember.getMemberId(),CoinConstUser.LOGIN_TIME_OUT_APP, TimeUnit.SECONDS);
//                }else{
//                    redisService.set(CoinConst.TOKENKEYMEMBER+loginMember.getMemberId().intValue(),token, CoinConstUser.LOGIN_TIME_OUT, TimeUnit.SECONDS);
//                    redisService.set(CoinConst.TOKENKEYTOKEN+token,loginMember.getMemberId(),CoinConstUser.LOGIN_TIME_OUT, TimeUnit.SECONDS);
//                }
//                result.setCode(Result.Code.SUCCESS);
//                result.setData(loginMember);
//                result.setMsg(token);
//                System.out.println("登录成功 : token"+token+" : 用户email"+loginMember.getEmail());
//            }
//        }catch (EmailNotExistException e){
//            result.setErrorCode(ErrorCode.ERROR_EMAIL_NOT_EXIST.getIndex());
//            result.setMsg(ErrorCode.ERROR_EMAIL_NOT_EXIST.getMessage());
//
//        }catch (PasswordErrorException e){
//            result.setErrorCode(ErrorCode.ERROR_PASSWORD_ERR.getIndex());
//            result.setMsg(ErrorCode.ERROR_PASSWORD_ERR.getMessage());
//        }catch (MemberIsLocketException e)
//        {
//            result.setErrorCode(ErrorCode.MEMBER_HAS_LOCKED.getIndex());
//            result.setMsg(ErrorCode.MEMBER_HAS_LOCKED.getMessage());
//        }
//        catch (Exception e){
//            e.printStackTrace();
//            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
//            result.setMsg(e.getMessage());
//        }
//        return result;
//    }
//}
