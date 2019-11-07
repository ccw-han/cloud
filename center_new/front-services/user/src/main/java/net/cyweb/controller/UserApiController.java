package net.cyweb.controller;

import cyweb.utils.CoinConst;
import cyweb.utils.ErrorCode;
import io.swagger.annotations.*;
import net.cyweb.exception.ErrorTokenException;
import net.cyweb.exception.OldPwdException;
import net.cyweb.exception.TokenExpirException;
import net.cyweb.model.Result;
import net.cyweb.model.YangMember;
import net.cyweb.model.YangMemberToken;
import net.cyweb.service.YangMemberTokenService;
import net.cyweb.validate.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import java.util.concurrent.TimeUnit;

@RestController
@Api(tags = "用户",position = 2)
@RequestMapping(value = "user")
public class UserApiController extends ApiBaseController{

    @Autowired
    private YangMemberTokenService yangMemberTokenService;

    @RequestMapping(value = "userInfo",method = RequestMethod.POST)
    @ApiOperation(value = "获取用户信息", notes = "获取用户信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "accessToken", value = "用户登录后获取的票据", required = true, dataType = "String", paramType = "query"),
    })
    public Result userInfo(@ApiIgnore @Validated YangMemberToken yangMemberToken, BindingResult bindingResult){
        Result result = new Result();
        result.setCode(Result.Code.ERROR);
        try{
            /*数据验证*/
            if(bindingResult.hasErrors()){
                result.setMsg(getStringErrors(bindingResult));
            }else{

                YangMember member = yangMemberTokenService.findMember(yangMemberToken);

                result.setCode(Result.Code.SUCCESS);
                result.setData(member);
            }
        }catch (ErrorTokenException e) {
            e.printStackTrace();
            result.setErrorCode(ErrorCode.ERROR_ERROR_TOKEN.getIndex());
            result.setMsg(ErrorCode.ERROR_ERROR_TOKEN.getMessage());
        }catch (TokenExpirException e) {
            e.printStackTrace();
            result.setErrorCode(ErrorCode.ERROR_ERROR_TOKEN_EXPIRE.getIndex());
            result.setMsg(ErrorCode.ERROR_ERROR_TOKEN_EXPIRE.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
            result.setMsg(e.getMessage());
        }
        return result;
    }

    /*检测用户交易密码是否正确*/
    @RequestMapping(value = "checkPwdTrade",method = RequestMethod.POST)
    @ApiOperation(value = "检查交易密码", notes = "检查交易密码")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "accessToken", value = "用户登录后获取的票据", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "pwdtrade", value = "交易密码", required = true, dataType = "String", paramType = "query"),
    })
    public Result checkPwdTrade(@ApiIgnore @Validated YangMemberToken yangMemberToken, @ApiIgnore @Validated(value = {CheckPwdTrade.class}) YangMember yangMember, BindingResult bindingResult){
        Result result = new Result();
        result.setCode(Result.Code.ERROR);
        try{
            /*数据验证*/
            if(bindingResult.hasErrors()){
                result.setMsg(getStringErrors(bindingResult));
            }else{

                 if(yangMemberTokenService.checkPwdTrade(yangMemberToken,yangMember)){
                    result.setErrorCode(ErrorCode.ERROR_PWD_TRADE_SUCCESS.getIndex());
                     result.setMsg(ErrorCode.ERROR_PWD_TRADE_SUCCESS.getMessage());
                     result.setCode(Result.Code.SUCCESS);
                 }else{
                     result.setErrorCode(ErrorCode.ERROR_PWD_TRADE_FAILED.getIndex());
                     result.setMsg(ErrorCode.ERROR_PWD_TRADE_FAILED.getMessage());
                 }
            }
        }catch (ErrorTokenException e) {
            e.printStackTrace();
            result.setErrorCode(ErrorCode.ERROR_ERROR_TOKEN.getIndex());
            result.setMsg(ErrorCode.ERROR_ERROR_TOKEN.getMessage());
        }catch (TokenExpirException e) {
            e.printStackTrace();
            result.setErrorCode(ErrorCode.ERROR_ERROR_TOKEN_EXPIRE.getIndex());
            result.setMsg(ErrorCode.ERROR_ERROR_TOKEN_EXPIRE.getMessage());
        }catch (Exception e) {
            e.printStackTrace();
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
        }
        return result;
    }

    /*修改用户密码接口*/
    @RequestMapping(value = "changePwd",method = RequestMethod.POST)
    @ApiOperation(value = "改变用户登录密码", notes = "改变用户登录密码")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "accessToken", value = "用户登录后获取的票据", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "oldPwd", value = "旧密码", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "newPwd", value = "新密码", required = true, dataType = "String", paramType = "query"),
    })
    public Result changePwd(@ApiIgnore @Validated YangMemberToken yangMemberToken, @ApiIgnore @Validated(value = {ChangePwd.class}) YangMember yangMember, BindingResult bindingResult){
        Result result = new Result();
        result.setCode(Result.Code.ERROR);
        try{
            /*数据验证*/
            if(bindingResult.hasErrors()){
                result.setMsg(getStringErrors(bindingResult));
            }else{
                yangMemberTokenService.changePwd(yangMemberToken,yangMember);
                result.setCode(Result.Code.SUCCESS);
            }
        }catch (ErrorTokenException e) {
            e.printStackTrace();
            result.setErrorCode(ErrorCode.ERROR_ERROR_TOKEN.getIndex());
            result.setMsg(ErrorCode.ERROR_ERROR_TOKEN.getMessage());
        }catch (TokenExpirException e) {
            e.printStackTrace();
            result.setErrorCode(ErrorCode.ERROR_ERROR_TOKEN_EXPIRE.getIndex());
            result.setMsg(ErrorCode.ERROR_ERROR_TOKEN_EXPIRE.getMessage());
        }catch (OldPwdException e){
            e.printStackTrace();
            result.setErrorCode(ErrorCode.ERROR_OLD_PWD_ERROR.getIndex());
            result.setMsg(ErrorCode.ERROR_OLD_PWD_ERROR.getMessage());
        }catch (Exception e) {
            e.printStackTrace();
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
        }
        return result;
    }

    /*高级认证，上传身份证*/
    @RequestMapping(value = "certificate",method = RequestMethod.POST)
    @ApiOperation(value = "高级认证", notes = "高级认证")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "accessToken", value = "用户登录后获取的票据", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "pic1", value = "第一张身份证照片地址", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "pic2", value = "第二张身份证照片地址", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "pic3", value = "第三张身份证照片地址", required = true, dataType = "String", paramType = "query"),
    })
    public Result certificate(@ApiIgnore @Validated YangMemberToken yangMemberToken, @ApiIgnore @Validated(value = {Certificate.class}) YangMember yangMember, BindingResult bindingResult){
        Result result = new Result();
        result.setCode(Result.Code.ERROR);
        try{
            /*数据验证*/
            if(bindingResult.hasErrors()){
                result.setMsg(getStringErrors(bindingResult));
            }else{
                yangMemberTokenService.certificate(yangMemberToken,yangMember);
                result.setCode(Result.Code.SUCCESS);

            }
        }catch (ErrorTokenException e) {
            e.printStackTrace();
            result.setErrorCode(ErrorCode.ERROR_ERROR_TOKEN.getIndex());
            result.setMsg(ErrorCode.ERROR_ERROR_TOKEN.getMessage());
        }catch (TokenExpirException e) {
            e.printStackTrace();
            result.setErrorCode(ErrorCode.ERROR_ERROR_TOKEN_EXPIRE.getIndex());
            result.setMsg(ErrorCode.ERROR_ERROR_TOKEN_EXPIRE.getMessage());
        }catch (Exception e) {
            e.printStackTrace();
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
        }
        return result;
    }

    /*登录是否要验证google*/
    @ApiOperation(value = "登录是否要验证google", notes = "登录是否要验证google")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "email", value = "用户填写的用户名", required = true, dataType = "String", paramType = "query"),
    })
    @RequestMapping(value = "loginNeedGoogle",method = RequestMethod.POST)
    public Result loginNeedGoogle(@ApiIgnore @Validated(value = {GoogleAuth.class}) YangMember yangMember, BindingResult bindingResult){
        Result result = new Result();
        result.setCode(Result.Code.ERROR);
        try{
            /*数据验证*/
            if(bindingResult.hasErrors()){
                result.setMsg(getStringErrors(bindingResult));
            }else{
                if(yangMemberTokenService.loginNeedGoogle(yangMember)){
                    result.setErrorCode(ErrorCode.ERROR_NEED_GOOGLE_TRUE.getIndex());
                    result.setMsg(ErrorCode.ERROR_NEED_GOOGLE_TRUE.getMessage());
                }else{
                    result.setErrorCode(ErrorCode.ERROR_NEED_GOOGLE_FALSE.getIndex());
                    result.setMsg(ErrorCode.ERROR_NEED_GOOGLE_FALSE.getMessage());
                }
                result.setCode(Result.Code.SUCCESS);

            }
        }catch (ErrorTokenException e) {
            e.printStackTrace();
            result.setErrorCode(ErrorCode.ERROR_ERROR_TOKEN.getIndex());
            result.setMsg(ErrorCode.ERROR_ERROR_TOKEN.getMessage());
        }catch (TokenExpirException e) {
            e.printStackTrace();
            result.setErrorCode(ErrorCode.ERROR_ERROR_TOKEN_EXPIRE.getIndex());
            result.setMsg(ErrorCode.ERROR_ERROR_TOKEN_EXPIRE.getMessage());
        }catch (Exception e) {
            e.printStackTrace();
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
        }
        return result;
    }

    /*是否有昵称*/
    @ApiOperation(value = "是否有昵称", notes = "是否有昵称")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "accessToken", value = "用户登录后获取的票据", required = true, dataType = "String", paramType = "query"),
    })
    @RequestMapping(value = "checkHasNick",method = RequestMethod.POST)
    public Result checkHasNick(@ApiIgnore @Validated YangMemberToken yangMemberToken, BindingResult bindingResult){
        Result result = new Result();
        result.setCode(Result.Code.ERROR);
        try{
            /*数据验证*/
            if(bindingResult.hasErrors()){
                result.setMsg(getStringErrors(bindingResult));
            }else{
                if(yangMemberTokenService.checkHasNick(yangMemberToken)){
                    result.setErrorCode(ErrorCode.ERROR_CHECK_NICK_TRUE.getIndex());
                    result.setMsg(ErrorCode.ERROR_CHECK_NICK_TRUE.getMessage());
                }else{
                    result.setErrorCode(ErrorCode.ERROR_CHECK_NICKE_FALSE.getIndex());
                    result.setMsg(ErrorCode.ERROR_CHECK_NICKE_FALSE.getMessage());
                }
                result.setCode(Result.Code.SUCCESS);
            }
        }catch (ErrorTokenException e) {
            e.printStackTrace();
            result.setErrorCode(ErrorCode.ERROR_ERROR_TOKEN.getIndex());
            result.setMsg(ErrorCode.ERROR_ERROR_TOKEN.getMessage());
        }catch (TokenExpirException e) {
            e.printStackTrace();
            result.setErrorCode(ErrorCode.ERROR_ERROR_TOKEN_EXPIRE.getIndex());
            result.setMsg(ErrorCode.ERROR_ERROR_TOKEN_EXPIRE.getMessage());
        }catch (Exception e) {
            e.printStackTrace();
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
        }
        return result;
    }

    /*是否有昵称*/
    @ApiOperation(value = "修改昵称", notes = "修改昵称")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "accessToken", value = "用户登录后获取的票据", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "nick", value = "新昵称", required = true, dataType = "String", paramType = "query"),
    })
    @RequestMapping(value = "changeNick",method = RequestMethod.POST)
    public Result changeNick(@ApiIgnore @Validated YangMemberToken yangMemberToken, BindingResult bindingResult, @ApiIgnore @Validated(value = {ChangeNick.class}) YangMember yangMember, BindingResult bindingResult1){
        Result result = new Result();
        result.setCode(Result.Code.ERROR);
        try{
            /*数据验证*/
            if(bindingResult.hasErrors()){
                result.setMsg(getStringErrors(bindingResult));
            }else if(bindingResult1.hasErrors()){
                result.setMsg(getStringErrors(bindingResult1));
            }else{
                yangMemberTokenService.changeNick(yangMemberToken,yangMember);
                result.setCode(Result.Code.SUCCESS);
            }
        }catch (ErrorTokenException e) {
            e.printStackTrace();
            result.setErrorCode(ErrorCode.ERROR_ERROR_TOKEN.getIndex());
            result.setMsg(ErrorCode.ERROR_ERROR_TOKEN.getMessage());
        }catch (TokenExpirException e) {
            e.printStackTrace();
            result.setErrorCode(ErrorCode.ERROR_ERROR_TOKEN_EXPIRE.getIndex());
            result.setMsg(ErrorCode.ERROR_ERROR_TOKEN_EXPIRE.getMessage());
        }catch (Exception e) {
            e.printStackTrace();
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
        }
        return result;
    }


    /**
     * 是否设置交易密码
     * @param yangMemberToken
     * @return
     */
    @ApiOperation(value = "是否已经设置了交易密码", notes = "是否已经设置了交易密码")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "accessToken", value = "用户登录后获取的票据", required = true, dataType = "String", paramType = "query"),
    })
    @RequestMapping(value = "hasTradePasswd",method = RequestMethod.POST)
    public Result hasTradePasswd(@Validated YangMemberToken yangMemberToken)
    {
        Result result = new Result();
        result.setCode(Result.Code.ERROR);
        try {

            result = yangMemberTokenService.hasTradePasswd(yangMemberToken);

        }catch (Exception e)
        {
            result.setCode(Result.Code.ERROR);
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());

        }
        return result;
    }


    /*设置交易密码*/
    @ApiOperation(value = "设置交易密码", notes = "设置交易密码")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "accessToken", value = "用户登录后获取的票据", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "pass", value = "用户登录密码", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "treadPass", value = "交易密码", required = true, dataType = "String", paramType = "query"),
    })
    @RequestMapping(value = "setTreadPasswd",method = RequestMethod.POST)
    public Result setTreadPasswd(@Validated YangMemberToken yangMemberToken,String pass,String treadPass)
    {
        Result result = new Result();
        result.setCode(Result.Code.ERROR);
        try {

            result = yangMemberTokenService.setTreadPasswd(yangMemberToken,pass,treadPass);

        }catch (Exception e)
        {
            result.setCode(Result.Code.ERROR);
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
        }
        return result;
    }

    /*修改交易密码*/
    @ApiOperation(value = "修改交易密码", notes = "修改交易密码")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "accessToken", value = "用户登录后获取的票据", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "pass", value = "用户登录密码", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "treadPassold", value = "原交易密码", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "treadPassnew", value = "新交易密码", required = true, dataType = "String", paramType = "query"),
    })
    @RequestMapping(value = "changeTreadPasswd",method = RequestMethod.POST)
    public Result changeTreadPasswd(@Validated YangMemberToken yangMemberToken,String pass,String treadPassold,String treadPassnew)
    {
        Result result = new Result();
        result.setCode(Result.Code.ERROR);
        try {

            result = yangMemberTokenService.changeTreadPasswd(yangMemberToken,pass,treadPassold,treadPassnew);

        }catch (Exception e)
        {
            result.setCode(Result.Code.ERROR);
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
        }
        return result;
    }



    /**
     * 初级认证
     * @param yangMemberToken
     * @param cardType
     * @param idCard
     * @param name
     * @return
     */
    @ApiOperation(value = "初级认证", notes = "初级认证")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "accessToken", value = "用户登录后获取的票据", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "cardType", value = "身份类别", required = true, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "idCard", value = "idCard号码", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "name", value = "用户的姓名", required = true, dataType = "String", paramType = "query"),
    })
    @RequestMapping(value = "primaryCertification",method = RequestMethod.POST)
    public Result primaryCertification(@Validated YangMemberToken yangMemberToken,int cardType,String idCard,String name)
    {
        Result result = new Result();
        result.setCode(Result.Code.ERROR);
        try {

            result = yangMemberTokenService.primaryCertification(yangMemberToken,cardType,idCard,name);

        }catch (Exception e)
        {
            result.setCode(Result.Code.ERROR);
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
        }
        return result;
    }

    /*修改头像*/
    @ApiOperation(value = "修改头像", notes = "修改头像")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "accessToken", value = "用户登录后获取的票据", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "pic", value = "头像地址", required = true, dataType = "String", paramType = "query"),
    })
    @RequestMapping(value = "updateUserPic",method = RequestMethod.POST)
    public Result updateUserPic(@Validated YangMemberToken yangMemberToken,String pic)
    {
        Result result = new Result();
        result.setCode(Result.Code.ERROR);
        try {

            result = yangMemberTokenService.updateUserPic(yangMemberToken,pic);

        }catch (Exception e)
        {
            result.setCode(Result.Code.ERROR);
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
        }
        return result;
    }

    /*修改头像*/
    @ApiOperation(value = "根据token获取memberId", notes = "根据token获取memberId")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "accessToken", value = "用户登录后获取的票据", required = true, dataType = "String", paramType = "query"),
    })
    @RequestMapping(value = "getMemberIdByToken",method = RequestMethod.POST)
    public Result getMemberIdByToken(@Validated YangMemberToken yangMemberToken)
    {

        Result result = new Result();
        try {
            YangMemberToken yangMemberToken1 = yangMemberTokenService.getYangMemberToken(yangMemberToken);
            result.setData(yangMemberToken1);
            result.setCode(Result.Code.SUCCESS);
        }catch (Exception e)
        {
            result.setCode(Result.Code.ERROR);
            result.setMsg("tokenError");
            result.setErrorCode(ErrorCode.ERROR_ERROR_TOKEN.getIndex());
            result.setMsg(ErrorCode.ERROR_ERROR_TOKEN.getMessage());
        }

        return result;

    }




}
