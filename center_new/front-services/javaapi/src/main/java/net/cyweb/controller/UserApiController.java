//package net.cyweb.controller;
//
//import com.alibaba.druid.support.json.JSONUtils;
//import com.alibaba.fastjson.JSONObject;
//import com.github.pagehelper.Page;
//import com.github.pagehelper.PageHelper;
//import com.github.pagehelper.PageInfo;
//import cyweb.utils.CoinConst;
//import cyweb.utils.CoinConstUser;
//import cyweb.utils.ErrorCode;
//import io.swagger.annotations.*;
//import net.cyweb.exception.ErrorTokenException;
//import net.cyweb.exception.OldPwdException;
//import net.cyweb.exception.TokenExpirException;
//import net.cyweb.mapper.YangMemberMapper;
//import net.cyweb.model.*;
//import net.cyweb.service.RedisService;
//import net.cyweb.service.YangMemberService;
//import net.cyweb.service.YangMemberTokenService;
//import net.cyweb.service.YangTimeLineService;
//import net.cyweb.validate.*;
//import org.apache.commons.codec.digest.DigestUtils;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.validation.BindingResult;
//import org.springframework.validation.annotation.Validated;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestMethod;
//import org.springframework.web.bind.annotation.RestController;
//import org.springframework.web.multipart.MultipartFile;
//import springfox.documentation.annotations.ApiIgnore;
//import tk.mybatis.mapper.entity.Example;
//
//import javax.mail.Multipart;
//import java.math.BigDecimal;
//import java.util.List;
//import java.util.concurrent.TimeUnit;
//
//@RestController
//@Api(tags = "用户",position = 2)
//@RequestMapping(value = "user")
//public class UserApiController extends ApiBaseController{
//
//    @Autowired
//    private YangMemberTokenService yangMemberTokenService;
//
//    @Autowired
//    private YangMemberMapper yangMemberMapper;
//
//    @Autowired
//    private YangMemberService yangMemberService;
//
//    @Autowired
//    private RedisService redisService;
//
//    @Autowired
//    private YangTimeLineService yangTimeLineService;
//
//    @RequestMapping(value = "userInfo",method = RequestMethod.POST)
//    @ApiOperation(value = "获取用户信息", notes = "获取用户信息")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "accessToken", value = "用户登录后获取的票据", required = true, dataType = "String", paramType = "query"),
//    })
//    public Result userInfo(@ApiIgnore @Validated YangMemberToken yangMemberToken, BindingResult bindingResult){
//        Result result = new Result();
//        result.setCode(Result.Code.ERROR);
//        try{
//            /*数据验证*/
//            if(bindingResult.hasErrors()){
//                result.setMsg(getStringErrors(bindingResult));
//            }else{
//
//                YangMember member = yangMemberTokenService.findMember(yangMemberToken);
//                if (member == null)
//                {
//
//                    throw new ErrorTokenException("token 无效");
//                }
//
//                //修改token的过期时间
//
//                result.setCode(Result.Code.SUCCESS);
//                result.setData(member);
//            }
//        }catch (ErrorTokenException e) {
////            e.printStackTrace();
//            result.setErrorCode(ErrorCode.ERROR_ERROR_TOKEN.getIndex());
//            result.setMsg(ErrorCode.ERROR_ERROR_TOKEN.getMessage());
//        }catch (TokenExpirException e) {
////            e.printStackTrace();
//            result.setErrorCode(ErrorCode.ERROR_ERROR_TOKEN_EXPIRE.getIndex());
//            result.setMsg(ErrorCode.ERROR_ERROR_TOKEN_EXPIRE.getMessage());
//        }catch (Exception e){
////            e.printStackTrace();
//            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
//            result.setMsg(e.getMessage());
//        }
//        return result;
//    }
//
//    /*检测用户交易密码是否正确*/
//    @RequestMapping(value = "checkPwdTrade",method = RequestMethod.POST)
//    @ApiOperation(value = "检查交易密码", notes = "检查交易密码")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "accessToken", value = "用户登录后获取的票据", required = true, dataType = "String", paramType = "query"),
//            @ApiImplicitParam(name = "pwdtrade", value = "交易密码", required = true, dataType = "String", paramType = "query"),
//    })
//    public Result checkPwdTrade(@ApiIgnore @Validated YangMemberToken yangMemberToken, @ApiIgnore @Validated(value = {CheckPwdTrade.class}) YangMember yangMember, BindingResult bindingResult){
//        Result result = new Result();
//        result.setCode(Result.Code.ERROR);
//        try{
//            /*数据验证*/
//            if(bindingResult.hasErrors()){
//                result.setMsg(getStringErrors(bindingResult));
//            }else{
//
//                 if(yangMemberTokenService.checkPwdTrade(yangMemberToken,yangMember)){
//                    result.setErrorCode(ErrorCode.ERROR_PWD_TRADE_SUCCESS.getIndex());
//                     result.setMsg(ErrorCode.ERROR_PWD_TRADE_SUCCESS.getMessage());
//                     result.setCode(Result.Code.SUCCESS);
//                 }else{
//                     result.setErrorCode(ErrorCode.ERROR_PWD_TRADE_FAILED.getIndex());
//                     result.setMsg(ErrorCode.ERROR_PWD_TRADE_FAILED.getMessage());
//                 }
//            }
//        }catch (ErrorTokenException e) {
//            e.printStackTrace();
//            result.setErrorCode(ErrorCode.ERROR_ERROR_TOKEN.getIndex());
//            result.setMsg(ErrorCode.ERROR_ERROR_TOKEN.getMessage());
//        }catch (TokenExpirException e) {
//            e.printStackTrace();
//            result.setErrorCode(ErrorCode.ERROR_ERROR_TOKEN_EXPIRE.getIndex());
//            result.setMsg(ErrorCode.ERROR_ERROR_TOKEN_EXPIRE.getMessage());
//        }catch (Exception e) {
//            e.printStackTrace();
//            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
//        }
//        return result;
//    }
//
//    /*修改用户密码接口*/
//    @RequestMapping(value = "changePwd",method = RequestMethod.POST)
//    @ApiOperation(value = "改变用户登录密码", notes = "改变用户登录密码")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "accessToken", value = "用户登录后获取的票据", required = true, dataType = "String", paramType = "query"),
//            @ApiImplicitParam(name = "oldPwd", value = "旧密码", required = true, dataType = "String", paramType = "query"),
//            @ApiImplicitParam(name = "newPwd", value = "新密码", required = true, dataType = "String", paramType = "query"),
//    })
//    public Result changePwd(@ApiIgnore @Validated YangMemberToken yangMemberToken, @ApiIgnore @Validated(value = {ChangePwd.class}) YangMember yangMember, BindingResult bindingResult){
//        Result result = new Result();
//        result.setCode(Result.Code.ERROR);
//        try{
//            /*数据验证*/
//            if(bindingResult.hasErrors()){
//                result.setMsg(getStringErrors(bindingResult));
//            }else{
//                yangMemberTokenService.changePwd(yangMemberToken,yangMember);
//                result.setCode(Result.Code.SUCCESS);
//            }
//        }catch (ErrorTokenException e) {
//            e.printStackTrace();
//            result.setErrorCode(ErrorCode.ERROR_ERROR_TOKEN.getIndex());
//            result.setMsg(ErrorCode.ERROR_ERROR_TOKEN.getMessage());
//        }catch (TokenExpirException e) {
//            e.printStackTrace();
//            result.setErrorCode(ErrorCode.ERROR_ERROR_TOKEN_EXPIRE.getIndex());
//            result.setMsg(ErrorCode.ERROR_ERROR_TOKEN_EXPIRE.getMessage());
//        }catch (OldPwdException e){
//            e.printStackTrace();
//            result.setErrorCode(ErrorCode.ERROR_OLD_PWD_ERROR.getIndex());
//            result.setMsg(ErrorCode.ERROR_OLD_PWD_ERROR.getMessage());
//        }catch (Exception e) {
//            e.printStackTrace();
//            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
//        }
//        return result;
//    }
//
//    /*高级认证，上传身份证*/
//    @RequestMapping(value = "certificate",method = RequestMethod.POST)
//    @ApiOperation(value = "高级认证", notes = "高级认证")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "accessToken", value = "用户登录后获取的票据", required = true, dataType = "String", paramType = "query")
//    })
//    @PostMapping(value="/upload",headers="content-type=multipart/form-data")
//    public Result certificate(@ApiParam(value="pic1",required = true) MultipartFile pic1,@ApiParam(value="pic2",required = true) MultipartFile pic2,@ApiParam(value="pic3",required = true) MultipartFile pic3,
//           String accessToken){
//        Result result = new Result();
//        result.setCode(Result.Code.ERROR);
//        try{
////                yangMemberTokenService.certificate(yangMemberToken,yangMember);
//                yangMemberService.certificate(accessToken,pic1,pic2,pic3);
//                result.setCode(Result.Code.SUCCESS);
//        }catch (ErrorTokenException e) {
//            e.printStackTrace();
//            result.setErrorCode(ErrorCode.ERROR_ERROR_TOKEN.getIndex());
//            result.setMsg(ErrorCode.ERROR_ERROR_TOKEN.getMessage());
//        }catch (TokenExpirException e) {
//            e.printStackTrace();
//            result.setErrorCode(ErrorCode.ERROR_ERROR_TOKEN_EXPIRE.getIndex());
//            result.setMsg(ErrorCode.ERROR_ERROR_TOKEN_EXPIRE.getMessage());
//        }catch (Exception e) {
//            e.printStackTrace();
//            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
//        }
//        return result;
//    }
//
//    /*登录是否要验证google*/
//    @ApiOperation(value = "登录是否要验证google", notes = "登录是否要验证google")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "email", value = "用户填写的用户名", required = true, dataType = "String", paramType = "query"),
//    })
//    @RequestMapping(value = "loginNeedGoogle",method = RequestMethod.POST)
//    public Result loginNeedGoogle(@ApiIgnore @Validated(value = {GoogleAuth.class}) YangMember yangMember, BindingResult bindingResult){
//        Result result = new Result();
//        result.setCode(Result.Code.ERROR);
//        try{
//            /*数据验证*/
//            if(bindingResult.hasErrors()){
//                result.setMsg(getStringErrors(bindingResult));
//            }else{
//                if(yangMemberTokenService.loginNeedGoogle(yangMember)){
//                    result.setErrorCode(ErrorCode.ERROR_NEED_GOOGLE_TRUE.getIndex());
//                    result.setMsg(ErrorCode.ERROR_NEED_GOOGLE_TRUE.getMessage());
//                }else{
//                    result.setErrorCode(ErrorCode.ERROR_NEED_GOOGLE_FALSE.getIndex());
//                    result.setMsg(ErrorCode.ERROR_NEED_GOOGLE_FALSE.getMessage());
//                }
//                result.setCode(Result.Code.SUCCESS);
//
//            }
//        }catch (ErrorTokenException e) {
//            e.printStackTrace();
//            result.setErrorCode(ErrorCode.ERROR_ERROR_TOKEN.getIndex());
//            result.setMsg(ErrorCode.ERROR_ERROR_TOKEN.getMessage());
//        }catch (TokenExpirException e) {
//            e.printStackTrace();
//            result.setErrorCode(ErrorCode.ERROR_ERROR_TOKEN_EXPIRE.getIndex());
//            result.setMsg(ErrorCode.ERROR_ERROR_TOKEN_EXPIRE.getMessage());
//        }catch (Exception e) {
//            e.printStackTrace();
//            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
//        }
//        return result;
//    }
//
//    /*是否有昵称*/
//    @ApiOperation(value = "是否有昵称", notes = "是否有昵称")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "accessToken", value = "用户登录后获取的票据", required = true, dataType = "String", paramType = "query"),
//    })
//    @RequestMapping(value = "checkHasNick",method = RequestMethod.POST)
//    public Result checkHasNick(@ApiIgnore @Validated YangMemberToken yangMemberToken, BindingResult bindingResult){
//        Result result = new Result();
//        result.setCode(Result.Code.ERROR);
//        try{
//            /*数据验证*/
//            if(bindingResult.hasErrors()){
//                result.setMsg(getStringErrors(bindingResult));
//            }else{
//                if(yangMemberTokenService.checkHasNick(yangMemberToken)){
//                    result.setErrorCode(ErrorCode.ERROR_CHECK_NICK_TRUE.getIndex());
//                    result.setMsg(ErrorCode.ERROR_CHECK_NICK_TRUE.getMessage());
//                }else{
//                    result.setErrorCode(ErrorCode.ERROR_CHECK_NICKE_FALSE.getIndex());
//                    result.setMsg(ErrorCode.ERROR_CHECK_NICKE_FALSE.getMessage());
//                }
//                result.setCode(Result.Code.SUCCESS);
//            }
//        }catch (ErrorTokenException e) {
//            e.printStackTrace();
//            result.setErrorCode(ErrorCode.ERROR_ERROR_TOKEN.getIndex());
//            result.setMsg(ErrorCode.ERROR_ERROR_TOKEN.getMessage());
//        }catch (TokenExpirException e) {
//            e.printStackTrace();
//            result.setErrorCode(ErrorCode.ERROR_ERROR_TOKEN_EXPIRE.getIndex());
//            result.setMsg(ErrorCode.ERROR_ERROR_TOKEN_EXPIRE.getMessage());
//        }catch (Exception e) {
//            e.printStackTrace();
//            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
//        }
//        return result;
//    }
//
//    /*是否有昵称*/
//    @ApiOperation(value = "修改昵称", notes = "修改昵称")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "accessToken", value = "用户登录后获取的票据", required = true, dataType = "String", paramType = "query"),
//            @ApiImplicitParam(name = "nick", value = "新昵称", required = true, dataType = "String", paramType = "query"),
//    })
//    @RequestMapping(value = "changeNick",method = RequestMethod.POST)
//    public Result changeNick(@ApiIgnore @Validated YangMemberToken yangMemberToken, BindingResult bindingResult, @ApiIgnore @Validated(value = {ChangeNick.class}) YangMember yangMember, BindingResult bindingResult1){
//        Result result = new Result();
//        result.setCode(Result.Code.ERROR);
//        try{
//            /*数据验证*/
//            if(bindingResult.hasErrors()){
//                result.setMsg(getStringErrors(bindingResult));
//            }else if(bindingResult1.hasErrors()){
//                result.setMsg(getStringErrors(bindingResult1));
//            }else{
//                yangMemberTokenService.changeNick(yangMemberToken,yangMember);
//                result.setCode(Result.Code.SUCCESS);
//            }
//        }catch (ErrorTokenException e) {
//            e.printStackTrace();
//            result.setErrorCode(ErrorCode.ERROR_ERROR_TOKEN.getIndex());
//            result.setMsg(ErrorCode.ERROR_ERROR_TOKEN.getMessage());
//        }catch (TokenExpirException e) {
//            e.printStackTrace();
//            result.setErrorCode(ErrorCode.ERROR_ERROR_TOKEN_EXPIRE.getIndex());
//            result.setMsg(ErrorCode.ERROR_ERROR_TOKEN_EXPIRE.getMessage());
//        }catch (Exception e) {
//            e.printStackTrace();
//            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
//        }
//        return result;
//    }
//
//
//    /**
//     * 是否设置交易密码
//     * @param yangMemberToken
//     * @return
//     */
//    @ApiOperation(value = "是否已经设置了交易密码", notes = "是否已经设置了交易密码")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "accessToken", value = "用户登录后获取的票据", required = true, dataType = "String", paramType = "query"),
//    })
//    @RequestMapping(value = "hasTradePasswd",method = RequestMethod.POST)
//    public Result hasTradePasswd(@Validated YangMemberToken yangMemberToken)
//    {
//        Result result = new Result();
//        result.setCode(Result.Code.ERROR);
//        try {
//
//            result = yangMemberTokenService.hasTradePasswd(yangMemberToken);
//
//        }catch (Exception e)
//        {
//            result.setCode(Result.Code.ERROR);
//            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
//
//        }
//        return result;
//    }
//
//
//    /*设置交易密码*/
//    @ApiOperation(value = "设置交易密码", notes = "设置交易密码")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "accessToken", value = "用户登录后获取的票据", required = true, dataType = "String", paramType = "query"),
//            @ApiImplicitParam(name = "pass", value = "用户登录密码", required = false, dataType = "String", paramType = "query"),
//            @ApiImplicitParam(name = "treadPass", value = "交易密码", required = true, dataType = "String", paramType = "query"),
//    })
//    @RequestMapping(value = "setTreadPasswd",method = RequestMethod.POST)
//    public Result setTreadPasswd(@Validated YangMemberToken yangMemberToken,String pass,String treadPass)
//    {
//        Result result = new Result();
//        result.setCode(Result.Code.ERROR);
//        try {
//
//            result = yangMemberTokenService.setTreadPasswd(yangMemberToken,pass,treadPass);
//
//        }catch (Exception e)
//        {
//            result.setCode(Result.Code.ERROR);
//            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
//        }
//        return result;
//    }
//
//    /*修改交易密码*/
//    @ApiOperation(value = "修改交易密码", notes = "修改交易密码")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "accessToken", value = "用户登录后获取的票据", required = true, dataType = "String", paramType = "query"),
//            @ApiImplicitParam(name = "pass", value = "用户登录密码", required = false, dataType = "String", paramType = "query"),
//            @ApiImplicitParam(name = "treadPassold", value = "原交易密码", required = true, dataType = "String", paramType = "query"),
//            @ApiImplicitParam(name = "treadPassnew", value = "新交易密码", required = true, dataType = "String", paramType = "query"),
//    })
//    @RequestMapping(value = "changeTreadPasswd",method = RequestMethod.POST)
//    public Result changeTreadPasswd(@Validated YangMemberToken yangMemberToken,String pass,String treadPassold,String treadPassnew)
//    {
//        Result result = new Result();
//        result.setCode(Result.Code.ERROR);
//        try {
//
//            result = yangMemberTokenService.changeTreadPasswd(yangMemberToken,pass,treadPassold,treadPassnew);
//
//        }catch (Exception e)
//        {
//            result.setCode(Result.Code.ERROR);
//            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
//        }
//        return result;
//    }
//
//
//
//    /**
//     * 初级认证
//     * @param yangMemberToken
//     * @param cardType
//     * @param idCard
//     * @param name
//     * @return
//     */
//    @ApiOperation(value = "初级认证", notes = "初级认证")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "accessToken", value = "用户登录后获取的票据", required = true, dataType = "String", paramType = "query"),
//            @ApiImplicitParam(name = "cardType", value = "身份类别", required = true, dataType = "int", paramType = "query"),
//            @ApiImplicitParam(name = "idCard", value = "idCard号码", required = true, dataType = "String", paramType = "query"),
//            @ApiImplicitParam(name = "name", value = "用户的姓名", required = true, dataType = "String", paramType = "query"),
//    })
//    @RequestMapping(value = "primaryCertification",method = RequestMethod.POST)
//    public Result primaryCertification(@Validated YangMemberToken yangMemberToken,int cardType,String idCard,String name)
//    {
//        Result result = new Result();
//        result.setCode(Result.Code.ERROR);
//        try {
//
//            result = yangMemberTokenService.primaryCertification(yangMemberToken,cardType,idCard,name);
//
//        }catch (Exception e)
//        {
//            result.setCode(Result.Code.ERROR);
//            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
//        }
//        return result;
//    }
//
//    /*修改头像*/
//    @ApiOperation(value = "修改头像", notes = "修改头像")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "accessToken", value = "用户登录后获取的票据", required = true, dataType = "String", paramType = "query"),
//            @ApiImplicitParam(name = "pic", value = "头像地址", required = true, dataType = "String", paramType = "query"),
//    })
//    @RequestMapping(value = "updateUserPic",method = RequestMethod.POST)
//    public Result updateUserPic(@Validated YangMemberToken yangMemberToken,String pic)
//    {
//        Result result = new Result();
//        result.setCode(Result.Code.ERROR);
//        try {
//
//            result = yangMemberTokenService.updateUserPic(yangMemberToken,pic);
//
//        }catch (Exception e)
//        {
//            result.setCode(Result.Code.ERROR);
//            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
//        }
//        return result;
//    }
//
//    /*修改头像*/
//    @ApiOperation(value = "根据token获取memberId", notes = "根据token获取memberId")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "accessToken", value = "用户登录后获取的票据", required = true, dataType = "String", paramType = "query"),
//    })
//    @RequestMapping(value = "getMemberIdByToken",method = RequestMethod.POST)
//    public Result getMemberIdByToken(@Validated YangMemberToken yangMemberToken)
//    {
//
//        Result result = new Result();
//        try {
//            YangMemberToken yangMemberToken1 = yangMemberTokenService.getYangMemberToken(yangMemberToken);
//            result.setData(yangMemberToken1);
//            result.setCode(Result.Code.SUCCESS);
//        }catch (Exception e)
//        {
//            result.setCode(Result.Code.ERROR);
//            result.setMsg("tokenError");
//            result.setErrorCode(ErrorCode.ERROR_ERROR_TOKEN.getIndex());
//            result.setMsg(ErrorCode.ERROR_ERROR_TOKEN.getMessage());
//        }
//
//        return result;
//
//    }
//
//    /*找回密码*/
//    @ApiOperation(value = "用户密码找回", notes = "用户密码找回")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "email", value = "需要找回密码的邮箱", required = true, dataType = "String", paramType = "query"),
//            @ApiImplicitParam(name = "pwd", value = "设置的新的密码", required = true, dataType = "String", paramType = "query"),
//            @ApiImplicitParam(name = "emailCode", value = "用来验证合法性的code", required = true, dataType = "String", paramType = "query"),
//    })
//    @RequestMapping(value = "findAndChangeUserPasswd",method = RequestMethod.POST)
//    public Result findAndChangeUserPasswd(YangMember yangMember,String emailCode)
//    {
//
//        Result result = new Result();
//        try {
//                String key = CoinConst.EMAILFORFINDCODE+yangMember.getEmail()+"_2";
//            if(redisService.exists(key))
//            {
//                 EmailLog emailLog =        JSONObject.parseObject(redisService.get(key).toString(), EmailLog.class);
//                if(emailCode.equals(emailLog.getContent()))
//                {
//                    YangMember yangMemberUpdate = new YangMember();
//                    yangMemberUpdate.setEmail(yangMember.getEmail());
//
//                    //根据email获取用户信息
//                    yangMemberUpdate = yangMemberMapper.selectOne(yangMemberUpdate);
//
//                    if(yangMemberUpdate==null){
//                        result.setCode(Result.Code.ERROR);
//                        result.setErrorCode(ErrorCode.ERROR_EMAIL_NOT_EXIST.getIndex());
//                        result.setMsg(ErrorCode.ERROR_EMAIL_NOT_EXIST.getMessage());
//                        return result;
//                    }
//                    yangMemberUpdate.setPwd(DigestUtils.md5Hex(yangMember.getPwd()));
//                    yangMemberMapper.updateByPrimaryKeySelective(yangMemberUpdate);
//
//                    //记入时间轴
//                    yangTimeLineService.addTimeLineByMemberId(yangMemberUpdate.getMemberId(),Byte.valueOf("10"),"忘记登录密码","");
//
//                    result.setCode(Result.Code.SUCCESS);
//                    //找到用户Token
//                    result.setData( redisService.get(CoinConst.TOKENKEYMEMBER+yangMemberUpdate.getMemberId()));
//                }else{
//                    result.setCode(Result.Code.ERROR);
//                    result.setMsg("codeError");
//                    result.setErrorCode(ErrorCode.ERROR_ERROR_CODE.getIndex());
//                    result.setMsg(ErrorCode.ERROR_ERROR_CODE.getMessage());
//                }
//            }else{
//                result.setCode(Result.Code.ERROR);
//                result.setMsg("codeError");
//                result.setErrorCode(ErrorCode.ERROR_ERROR_CODE.getIndex());
//                result.setMsg(ErrorCode.ERROR_ERROR_CODE.getMessage());
//            }
//
//        }catch (Exception e)
//        {
//            result.setCode(Result.Code.ERROR);
//            result.setMsg("tokenError");
//            result.setErrorCode(ErrorCode.ERROR_ERROR_TOKEN.getIndex());
//            result.setMsg(e.getMessage());
//            e.printStackTrace();
//        }
//
//        return result;
//
//    }
//
//
//
//
//
//    /*获取用户推荐*/
//    @ApiOperation(value = "获取用户推荐", notes = "获取用户推荐")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "accessToken", value = "用户登录后获取的票据", required = true, dataType = "String", paramType = "query"),
//    })
//    @RequestMapping(value = "getUserRecommend",method = RequestMethod.POST)
//    public Result  getUserRecommend(@Validated YangMemberToken yangMemberToken)
//    {
//        Result result = new Result();
//        result.setCode(Result.Code.ERROR);
//        try {
//            //现获取用户信息
//            YangMember yangMemberSelf = yangMemberTokenService.findMember(yangMemberToken);
//            if(yangMemberSelf == null)
//            {
//                throw  new TokenExpirException("token 错误");
//            }
//
//            PageHelper.startPage(yangMemberToken.getPageNum(),yangMemberToken.getPageSize());
//
//            tk.mybatis.mapper.entity.Example example = new Example(YangMember.class);
//            example.createCriteria().andEqualTo("pid",yangMemberSelf.getMemberId().toString());
//
//            example.setOrderByClause("reg_time desc");
//            List<YangMember> yangMemberList = yangMemberMapper.selectByExample(example);
//
//
//
//            PageInfo<YangMember> pageInfo = new PageInfo<YangMember>(yangMemberList);
//
//            result.setData(pageInfo);
//            result.setCode(Result.Code.SUCCESS);
//        }catch (TokenExpirException e){
//
//            result.setErrorCode(ErrorCode.ERROR_ERROR_TOKEN.getIndex());
//            result.setMsg(ErrorCode.ERROR_ERROR_TOKEN.getMessage());
//            result.setCode(Result.Code.ERROR);
//
//        }
//        catch (Exception e)
//        {
//            e.printStackTrace();
//            result.setCode(Result.Code.ERROR);
//        }
//        return result;
//    }
//
//
//    /*判断是否需要输入交易密码*/
//    @ApiOperation(value = "判断是否需要输入交易密码", notes = "判断是否需要输入交易密码")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "accessToken", value = "用户登录后获取的票据", required = true, dataType = "String", paramType = "query"),
//    })
//    @RequestMapping(value = "needThreadPasswd",method = RequestMethod.POST)
//    public Result  needThreadPasswd(@Validated YangMemberToken yangMemberToken)
//    {
//        Result result = new Result();
//        result.setCode(Result.Code.ERROR);
//        try {
//            //现获取用户信息
//            YangMember yangMemberSelf = yangMemberTokenService.findMember(yangMemberToken);
//            if(yangMemberSelf == null)
//            {
//                throw  new TokenExpirException("token 错误");
//            }
//
//            if(redisService.exists(CoinConst.NONEEDTHREADPASSWD+yangMemberSelf.getMemberId().intValue())){
//                result.setCode(Result.Code.SUCCESS);
//            }else{
//                result.setCode(Result.Code.ERROR);
//            }
//        }catch (TokenExpirException e){
//
//            result.setErrorCode(ErrorCode.ERROR_ERROR_TOKEN.getIndex());
//            result.setMsg(ErrorCode.ERROR_ERROR_TOKEN.getMessage());
//            result.setCode(Result.Code.ERROR);
//
//        }
//        catch (Exception e)
//        {
//            e.printStackTrace();
//            result.setCode(Result.Code.ERROR);
//        }
//        return result;
//    }
//
//
//
//    /*锁仓*/
//    @ApiOperation(value = "锁仓", notes = "锁仓")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "accessToken", value = "用户登录后获取的票据", required = true, dataType = "String", paramType = "query"),
//            @ApiImplicitParam(name = "num", value = "需要锁仓的数量", required = true, dataType = "String", paramType = "query")
//    })
//    @RequestMapping(value = "platFormCurrencyLock",method = RequestMethod.POST)
//    public Result  platFormCurrencyLock(@Validated YangMemberToken yangMemberToken, BigDecimal num)
//    {
//        Result result = new Result();
//        result.setCode(Result.Code.ERROR);
//        try {
//            result = yangMemberService.platFormCurrencyLock(yangMemberToken,num);
//
//        }
//        catch (Exception e)
//        {
//            e.printStackTrace();
//            result.setCode(Result.Code.ERROR);
//        }
//        return result;
//    }
//
//
//
//    /*解锁*/
//    @ApiOperation(value = "解锁", notes = "解锁")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "accessToken", value = "用户登录后获取的票据", required = true, dataType = "String", paramType = "query"),
//            @ApiImplicitParam(name = "num", value = "需要锁仓的数量", required = true, dataType = "String", paramType = "query")
//    })
//    @RequestMapping(value = "platFormCurrencyUnLock",method = RequestMethod.POST)
//    public Result  platFormCurrencyUnLock(@Validated YangMemberToken yangMemberToken, BigDecimal num)
//    {
//        Result result = new Result();
//        result.setCode(Result.Code.ERROR);
//        try {
//            result = yangMemberService.platFormCurrencyUnLock(yangMemberToken,num);
//        }
//        catch (Exception e)
//        {
//            e.printStackTrace();
//            result.setCode(Result.Code.ERROR);
//        }
//        return result;
//    }
//
//
//    /*获取挖矿数据*/
//    @ApiOperation(value = "获取挖矿数据", notes = "获取挖矿数据")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "accessToken", value = "用户登录后获取的票据", required =false, dataType = "String", paramType = "query"),
//    })
//    @RequestMapping(value = "miningInfos",method = RequestMethod.POST)
//    public Result  miningInfos(String accessToken)
//    {
//        Result result = new Result();
//        result.setCode(Result.Code.ERROR);
//        try {
//            result = yangMemberService.getMiningInfos(accessToken);
//        }
//        catch (Exception e)
//        {
//            e.printStackTrace();
//            result.setCode(Result.Code.ERROR);
//        }
//        return result;
//    }
//
//
//
//    /*获取挖矿数据个人*/
//    @ApiOperation(value = "获取挖矿数据个人", notes = "获取挖矿数据个人")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "accessToken", value = "用户登录后获取的票据", required = true, dataType = "String", paramType = "query"),
//    })
//    @RequestMapping(value = "memberMiningInfos",method = RequestMethod.POST)
//    public Result  memberMiningInfos(@Validated YangMemberToken yangMemberToken)
//    {
//        Result result = new Result();
//        result.setCode(Result.Code.ERROR);
//        try {
//            result = yangMemberService.getMemberMiningInfos(yangMemberToken);
//        }
//        catch (Exception e)
//        {
//            e.printStackTrace();
//            result.setCode(Result.Code.ERROR);
//        }
//        return result;
//    }
//
//
//    /*获取个人挖矿数据*/
//    @ApiOperation(value = "获取挖矿数据", notes = "获取挖矿数据")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "accessToken", value = "用户登录后获取的票据", required = false, dataType = "String", paramType = "query"),
//            @ApiImplicitParam(name = "begin", value = "开始时间戳", required = false, dataType = "String", paramType = "query"),
//            @ApiImplicitParam(name = "end", value = "结束时间戳", required = false, dataType = "String", paramType = "query"),
//            @ApiImplicitParam(name = "pageNum", value = "页码", required = true, dataType = "String", paramType = "query"),
//            @ApiImplicitParam(name = "pageSize", value = "每页大小", required = true, dataType = "String", paramType = "query")
//
//    })
//    @RequestMapping(value = "miningInfosByMember",method = RequestMethod.POST)
//    public Result  miningInfosByMember(String accessToken,String begin,String end,int pageNum,int pageSize)
//    {
//
//        Result result = new Result();
//        result.setCode(Result.Code.ERROR);
//        try {
//            Page page = new Page();
//            page.setPageNum(pageNum);
//            page.setPageSize(pageSize);
//            YangMemberToken yangMemberToken=new YangMemberToken();
//            yangMemberToken.setAccessToken(accessToken);
//            result = yangMemberService.getMiningInfosByMember(yangMemberToken,page,begin,end);
//        }
//        catch (Exception e)
//        {
//            e.printStackTrace();
//            result.setCode(Result.Code.ERROR);
//        }
//        return result;
//    }
//
//
//
//    /*找回密码*/
//    @ApiOperation(value = "用户交易密码找回", notes = "用户交易密码找回")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "accessToken", value = "accessToken", required = true, dataType = "String", paramType = "query"),
//            @ApiImplicitParam(name = "pass", value = "设置的新的密码", required = true, dataType = "String", paramType = "query"),
//            @ApiImplicitParam(name = "emailCode", value = "用来验证合法性的code", required = true, dataType = "String", paramType = "query"),
//    })
//    @RequestMapping(value = "findAndChangeUserTradePasswd",method = RequestMethod.POST)
//    public Result findAndChangeUserTradePasswd(String accessToken,String pass,String emailCode)
//    {
//
//        Result result = new Result();
//
//
//        YangMemberToken yangMemberToken=new YangMemberToken();
//        yangMemberToken.setAccessToken(accessToken);
//
//        result=yangMemberService.findBackTradePass(yangMemberToken,pass,emailCode);
//
//
//        return result;
//
//    }
//
//    /*发送邮件*/
//   /* @ApiOperation(value = "发送邮箱验证码", notes = "发送邮箱验证码")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "email", value = "邮箱", required = true, dataType = "String", paramType = "query"),
//            @ApiImplicitParam(name = "type", value = "验证码类型", required = true, dataType = "String", paramType = "query"),
//            @ApiImplicitParam(name = "title", value = "邮件标题", required = true, dataType = "String", paramType = "query"),
//            @ApiImplicitParam(name = "content", value = "邮件内容", required = true, dataType = "String", paramType = "query"),
//            @ApiImplicitParam(name = "ip", value = "ip", required = true, dataType = "String", paramType = "query"),
//    })
//    @RequestMapping(value = "sendEmailCodes",method = RequestMethod.POST)
//    public Result sendEmailCodes(String email,String type,String title,String content,String ip)
//    {
//        Result result = new Result();
//        result=yangMemberService.sendEmailCode(email,type,title,content,ip,0);
//        return result;
//
//    }*/
//    /*发送邮件*/
//    @ApiOperation(value = "发送邮箱验证码", notes = "发送邮箱验证码")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "email", value = "邮箱", required = true, dataType = "String", paramType = "query"),
//            @ApiImplicitParam(name = "title", value = "邮件标题", required = true, dataType = "String", paramType = "query"),
//            @ApiImplicitParam(name = "content", value = "邮件内容", required = true, dataType = "String", paramType = "query")
//    })
//    @RequestMapping(value = "sendNormalEmailCode",method = RequestMethod.POST)
//    public Result sendNormalEmailCode(String email,String title,String content)
//    {
//        Result result = new Result();
//        result=yangMemberService.sendNormalEmailCode(email,title,content,0);
//        return result;
//
//    }
//
//}
