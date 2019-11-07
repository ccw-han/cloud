package net.cyweb.controller;

import com.alibaba.fastjson.JSONObject;
import cyweb.utils.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.cyweb.config.custom.*;
import net.cyweb.config.mes.MesConfig;
import net.cyweb.exception.EmailNotExistException;
import net.cyweb.exception.MemberIsLocketException;
import net.cyweb.exception.PasswordErrorException;
import net.cyweb.mapper.ProgramBackMapper;
import net.cyweb.mapper.YangCurrencyPairMapper;
import net.cyweb.model.*;
import net.cyweb.model.modelExt.YangC2CGuaEvt;
import net.cyweb.service.*;
import net.cyweb.task.OrderDealTask;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.*;
import tk.mybatis.mapper.util.StringUtil;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Time：2019/9/2
 * 创建人：椰椰
 * 前端接口
 */
@RestController
@Api(tags = "前端all", position = 2)
@RequestMapping(value = "font")
public class FontController extends ApiBaseController {

    @Autowired
    private YangC2CGuaService yangC2CGuaService;

    @Autowired
    private YangCurrencyService yangCurrencyService;

    @Autowired
    private FunFlashService funFlashService;

    @Autowired
    private YangMemberService yangMemberService;

    @Autowired
    private YangNationalityService yangNationalityService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private YangGoogleAuthService yangGoogleAuthService;

    @Autowired
    private MesConfig mesConfig;

    @Autowired
    private YangOrderService yangOrderService;

    @Autowired
    private RobotService robotService;

    @Autowired
    private YangCurrencyPairService yangCurrencyPairService;

    @Autowired
    private YangTibiService yangTibiService;

    @Autowired
    private YangTibiOutService yangTibiOutService;

    @Autowired
    private YangCurrencyMarketService marketService;

    @Autowired
    private YangWorkOrderService yangWorkOrderService;

    @Autowired
    private YangArticleService yangArticleService;

    @Autowired
    private YangCurrencyPairMapper yangCurrencyPairMapper;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private YangCurrencyUserService yangCurrencyUserService;

    @Autowired
    private RevertCNYUtils revertCNYUtils;

    @Autowired
    private YangAcceptanceService yangAcceptanceService;

    @Autowired
    private YangNewBiService yangNewBiService;

    @Autowired
    private YangMemberService memberService;

    @Autowired
    private YangBankService yangBankService;

    @Autowired
    private YangC2COrdersService yangC2COrdersService;

    @Autowired
    private ProgramBackService programBackService;

    @Autowired
    private ProgramBackMapper programBackMapper;

    /*********************************************ccw-start*****************************************************/
    /**
     * 查询交易 图标1数据接口
     * ccw
     *
     * @param
     * @return
     * @version: 2019/10/15
     */
    @RequestMapping(value = "getAllSymbols", method = RequestMethod.GET)
    @ApiOperation(value = "获取所有币对", notes = "获取所有币对")
    public List getAllSymbols() {
        TreeMap<String, String> param = new TreeMap<String, String>();
        List<Map<String, String>> data = programBackService.getAllSymbols(param);
        if (data != null) {
            return data;
        }
        data = new ArrayList();
        Map result = new HashMap();
        result.put("res", "访问失败");
        data.add(result);
        return data;
    }

    /**
     * 查询交易 图标1数据接口
     * ccw
     *
     * @param
     * @return
     * @version: 2019/10/15
     */
    @RequestMapping(value = "getAvePrice", method = RequestMethod.GET)
    @ApiOperation(value = "获取均价", notes = "获取均价")
    public String getAvePrice() {
        TreeMap<String, String> param = new TreeMap<String, String>();
        List<Map<String, String>> datas = programBackService.getAvePrice(param);
        Map<String, List<Map<String, String>>> data = programBackService.aveToDataBase(datas);
        try {
            programBackMapper.insertList(data.get("buy"));
            programBackMapper.insertList(data.get("sell"));
        } catch (Exception e) {
            return "失败";
        }


        //存到数据库

//        if (data != null) {
//            System.out.println(data.size());
//            return data;
//        }
//        data = new ArrayList();
//        Map result = new HashMap();
//        result.put("res", "访问失败");
//        data.add(result);
        return "成功";
    }

    /**
     * 查询交易 图标1数据接口
     * ccw
     *
     * @param
     * @return
     * @version: 2019/10/15
     */
    @RequestMapping(value = "testOrg", method = RequestMethod.GET)
    @ApiOperation(value = "testOrg", notes = "testOrg")
    public Map<String, Object> testOrg() {
        TreeMap<String, String> param = new TreeMap<>();
        param.put("user_id", "468851405973919488");
        String response = RemoteAccessUtils.postOrg(param, "/user/query_user_list");
        Map<String, Object> map = (Map<String, Object>) JSONObject.parse(response);
        return map;
    }

    /*********************************************ccw-end*****************************************************/


    /*********************************************宋-start*****************************************************/
    /**
     * 首页获取承兑商卖单挂单
     * 耶耶
     *
     * @param map
     * @return
     */
    @RequestMapping(value = "buyCoins", method = RequestMethod.POST)
    @ApiOperation(value = "首页获取承兑商卖单挂单", notes = "首页获取承兑商卖单挂单")
    public Result buyCoins(@RequestBody Map<String, String> map) {
        Result result = new Result();
        Boolean blank = ParticipationIsBlankUtils.isBlank(map);
        if (blank) {
            result.setCode(0);
            result.setMsg("参数格式错误");
            return result;
        }
        Map<String, String> map1 = ParticipationRemoveBlankSpaceUtils.removeBlankSpace(map);
        String currencyId = map1.get("currencyId");
        String buyMoney = map1.get("buyMoney");
        YangC2CGuaEvt yangC2CGuaEvt = new YangC2CGuaEvt();
        yangC2CGuaEvt.setCurrencyId(currencyId);
        yangC2CGuaEvt.setBuyMoney(Integer.parseInt(buyMoney));
        result = yangC2CGuaService.buyCoins(yangC2CGuaEvt);
        return result;
    }

    /**
     * 首页支持法币购买的币种列表
     * 耶耶
     *
     * @return
     */
    @RequestMapping(value = "getLegalCurrencys", method = RequestMethod.POST)
    @ApiOperation(value = "首页支持法币购买的币种列表", notes = "首页支持法币购买的币种列表")
    public Result getCoins() {
        Result result = new Result();
        result = yangCurrencyService.getCoins();
        return result;
    }

    /**
     * 获取国籍列表
     * 椰椰
     *
     * @return
     */
    @RequestMapping(value = "getNationality", method = RequestMethod.POST)
    @ApiOperation(value = "国籍列表", notes = "国籍列表")
    public Result getNationality() {
        Result result = new Result();
        result = yangNationalityService.getNationalityList();
        return result;
    }

    /**
     * 发送邮件
     * 椰椰
     *
     * @param map
     * @param request
     * @return
     */
    @ApiOperation(value = "发送邮件", notes = "发送邮件")
    @RequestMapping(value = "sendEmailCodes", method = RequestMethod.POST)
    public Result sendEmailCodes(@RequestBody Map<String, String> map, HttpServletRequest request) {
        Result result = new Result();
        Boolean blank = ParticipationIsBlankUtils.isBlank(map);
        if (blank) {
            result.setCode(0);
            result.setMsg("参数格式错误");
            return result;
        }
        Map<String, String> map1 = ParticipationRemoveBlankSpaceUtils.removeBlankSpace(map);
        String email = map1.get("email");
        result = yangMemberService.sendEmailCode(email, "1", "邮箱验证码", EmailUtils.getRandomNumber(6), 0, request);
        return result;

    }

    /**
     * 椰椰
     * 邮箱注册
     *
     * @param map
     * @param request
     * @return
     */
    @RequestMapping(value = "registerByEmail", method = RequestMethod.POST)
    @ApiOperation(value = "邮箱注册", notes = "邮箱注册")
    public Result registerByEmail(@RequestBody Map<String, String> map, HttpServletRequest request) {
        Result result = new Result();
        Boolean blank = ParticipationIsBlankUtils.isBlank(map);
        if (blank) {
            result.setCode(0);
            result.setMsg("参数格式错误");
            return result;
        }
        Map<String, String> map1 = ParticipationRemoveBlankSpaceUtils.removeBlankSpace(map);
        String email = map1.get("email");
        String emailCode = map1.get("emailCode");
        String pwd = map1.get("pwd");
        String nationality = map1.get("nationality");
        String inviteCode = map1.get("inviteCode");
        String nationalityCode = map1.get("nationalityCode");

        result = yangMemberService.userRegisterByEmail(email, emailCode, pwd, nationality, inviteCode, request, nationalityCode);
        return result;
    }

    /**
     * 椰椰
     * 手机注册
     *
     * @param map
     * @param request
     * @return
     */
    @RequestMapping(value = "registerByPhone", method = RequestMethod.POST)
    @ApiOperation(value = "手机注册", notes = "手机注册")
    public Result registerByPhone(@RequestBody Map<String, String> map, HttpServletRequest request) {
        Result result = new Result();
        Boolean blank = ParticipationIsBlankUtils.isBlank(map);
        if (blank) {
            result.setCode(0);
            result.setMsg("参数格式错误");
            return result;
        }
        Map<String, String> map1 = ParticipationRemoveBlankSpaceUtils.removeBlankSpace(map);
        String phone = map1.get("phone");
        String noteCode = map1.get("noteCode");
        String pwd = map1.get("pwd");
        String nationality = map1.get("nationality");
        String inviteCode = map1.get("inviteCode");
        String nationalityCode = map1.get("nationalityCode");

        result = yangMemberService.userRegisterByPhone(phone, noteCode, pwd, nationality, inviteCode, request, nationalityCode);
        return result;
    }


    /**
     * 椰椰
     * 登录接口
     *
     * @param map
     * @param request
     * @return
     */
    @RequestMapping(value = "login", method = RequestMethod.POST)
    @ApiOperation(value = "登录", notes = "用户登录")
    public Result login(@RequestBody Map<String, String> map, HttpServletRequest request) {
        Result result = new Result();
        Boolean blank = ParticipationIsBlankUtils.isBlank(map);
        if (blank) {
            result.setCode(0);
            result.setMsg("参数格式错误");
            return result;
        }
        Map<String, String> map1 = ParticipationRemoveBlankSpaceUtils.removeBlankSpace(map);
        String loginUserName = map1.get("loginUserName");
        String pwd = map1.get("pwd");
        YangMember yangMember = new YangMember();
        yangMember.setPwd(pwd);

        result.setCode(Result.Code.ERROR);
        try {
            //判断是邮箱登录还是手机号登录
            boolean contains = loginUserName.contains("@");
            YangMember loginyangMember;
            if (contains) {
                yangMember.setEmail(loginUserName);
                loginyangMember = yangMemberService.loginByEmail(yangMember);
                    /*String emailKey = CoinConst.EMAILFORFINDCODE+loginUserName+"_"+CoinConst.EMAIL_CODE_TYPE_ZC;
                    //需要验证码
                    if (code != null && !"".equals(code)){
                        if (redisService.exists(emailKey)){
                            EmailLog emailLog = JSONObject.parseObject(redisService.get(emailKey).toString(), EmailLog.class);
                            //验证码错误
                            if (!code.equals(emailLog.getContent())){
                                result.setMsg("验证码错误");
                                result.setCode(0);
                                return result;
                            }
                        }else{
                            result.setMsg("验证码过期");
                            result.setCode(0);
                            return result;
                        }
                    }*/
            } else {
                yangMember.setPhone(loginUserName);
                loginyangMember = yangMemberService.loginByPhone(yangMember);
                    /*String phoneKey = CoinConst.EMAILFORFINDCODE + loginUserName + "_" + "1";
                    //需要验证码
                    if (code != null && !"".equals(code)){
                        if (redisService.exists(phoneKey)){
                            String noteCode = (String) redisService.get(phoneKey);
                            //验证码错误
                            if (!code.equals(noteCode)){
                                result.setMsg("验证码错误");
                                result.setCode(0);
                                return result;
                            }
                        }else{
                            result.setMsg("验证码过期");
                            result.setCode(0);
                            return result;
                        }
                    }*/
            }
            yangMember.setLoginTime(BigInteger.valueOf(System.currentTimeMillis() / 1000).intValue());
            String ipAddr = GetIpAddr.getIpAddr(request);
            loginyangMember.setLoginIp(ipAddr);
            //生成用户的token
            String token = CommonTools.Md5(System.currentTimeMillis() + loginyangMember.getLoginIp());
            redisService.set(CoinConst.TOKENKEYMEMBER + loginyangMember.getMemberId().intValue(), token, CoinConstUser.LOGIN_TIME_OUT, TimeUnit.SECONDS);
            redisService.set(CoinConst.TOKENKEYTOKEN + token, loginyangMember.getMemberId(), CoinConstUser.LOGIN_TIME_OUT, TimeUnit.SECONDS);
            yangMemberService.updateYangmember(loginyangMember);
            result.setCode(Result.Code.SUCCESS);
            //result.setData(loginyangMember);
            result.setData(token);
            result.setMsg("登录成功");
            System.out.println("登录成功 : token" + token);

        } catch (EmailNotExistException e) {
            result.setErrorCode(ErrorCode.ERROR_EMAIL_NOT_EXIST.getIndex());
            result.setMsg("该用户不存在");
        } catch (PasswordErrorException e) {
            result.setErrorCode(ErrorCode.ERROR_PASSWORD_ERR.getIndex());
            result.setMsg(ErrorCode.ERROR_PASSWORD_ERR.getMessage());
        } catch (MemberIsLocketException e) {
            result.setErrorCode(ErrorCode.MEMBER_HAS_LOCKED.getIndex());
            result.setMsg(ErrorCode.MEMBER_HAS_LOCKED.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
            result.setMsg(e.getMessage());
        }
        return result;
    }


    /**
     * 椰椰
     * 短信发送接口
     *
     * @param map
     * @return
     */
    @RequestMapping(value = "sendPhoneCodes", method = RequestMethod.POST)
    @ApiOperation(value = "短信发送", notes = "短信发送")
    public Result mseSend(@RequestBody Map<String, String> map) {
        Result result = new Result();
        Boolean blank = ParticipationIsBlankUtils.isBlank(map);
        if (blank) {
            result.setCode(0);
            result.setMsg("参数格式错误");
            return result;
        }
        Map<String, String> map1 = ParticipationRemoveBlankSpaceUtils.removeBlankSpace(map);
        String phone = map1.get("phone");
        result = NoteUtils.mesSend(phone, mesConfig, redisService);
        return result;
    }

    /**
     * 椰椰
     * 2019/9/12
     * 重置密码
     *
     * @param map
     * @return
     */
    @RequestMapping(value = "resetPwd", method = RequestMethod.POST)
    @ApiOperation(value = "重置密码", notes = "重置密码")
    public Result resetPwd(@RequestBody Map<String, String> map) {
        Result result = new Result();
        Boolean blank = ParticipationIsBlankUtils.isBlank(map);
        if (blank) {
            result.setCode(0);
            result.setMsg("参数格式错误");
            return result;
        }
        Map<String, String> map1 = ParticipationRemoveBlankSpaceUtils.removeBlankSpace(map);
        String account = map1.get("account");
        String pwd = map1.get("pwd");
        YangMember yangMember = new YangMember();
        yangMember.setPwd(DigestUtils.md5Hex(pwd));
        if (account.contains("@")) {
            //账号是邮箱
            yangMember.setEmail(account);
            result = yangMemberService.resetPwdByEmail(yangMember);

        } else {
            //账号是手机
            yangMember.setPhone(account);
            result = yangMemberService.resetPwdByPhone(yangMember);
        }
        return result;
    }

    /**
     * 椰椰
     * 2019/9/12
     * 验证账号
     *
     * @param map
     * @return
     */
    @RequestMapping(value = "checkAccount", method = RequestMethod.POST)
    @ApiOperation(value = "验证账号", notes = "验证账号")
    public Result checkAccount(@RequestBody Map<String, String> map) {
        Result result = new Result();
        Boolean blank = ParticipationIsBlankUtils.isBlank(map);
        if (blank) {
            result.setCode(0);
            result.setMsg("参数格式错误");
            return result;
        }
        Map<String, String> map1 = ParticipationRemoveBlankSpaceUtils.removeBlankSpace(map);
        String account = map1.get("account");
        YangMember yangMember = new YangMember();
        if (account.contains("@")) {
            //账号是邮箱
            yangMember.setEmail(account);
        } else {
            //账号是手机
            yangMember.setPhone(account);
        }
        result = yangMemberService.checkAccount(yangMember);
        return result;
    }

    /**
     * 椰椰
     * 2019/9/12
     * 验证验证码是否正确
     *
     * @param map
     * @return
     */
    @RequestMapping(value = "checkVerifyCode", method = RequestMethod.POST)
    @ApiOperation(value = "验证验证码", notes = "验证验证码")
    public Result checkVerifyCode(@RequestBody Map<String, String> map) {
        Result result = new Result();
        Boolean blank = ParticipationIsBlankUtils.isBlank(map);
        if (blank) {
            result.setCode(0);
            result.setMsg("参数格式错误");
            return result;
        }
        Map<String, String> map1 = ParticipationRemoveBlankSpaceUtils.removeBlankSpace(map);
        String account = map1.get("account");
        String code = map1.get("code");
        if (account.contains("@")) {
            //账号是邮箱
            String key = CoinConst.EMAILFORFINDCODE + account + "_" + CoinConst.EMAIL_CODE_TYPE_ZC;
            if (redisService.exists(key)) {
                //存在
                EmailLog emailLog = JSONObject.parseObject(redisService.get(key).toString(), EmailLog.class);
                if (emailLog.getContent().equals(code)) {
                    result.setCode(1);
                    result.setMsg("验证成功");
                } else {
                    result.setCode(0);
                    result.setMsg("验证码错误，请重新输入");
                }
            } else {
                result.setCode(0);
                result.setMsg("验证码过期");
            }
        } else {
            //账号是手机
            String key = CoinConst.EMAILFORFINDCODE + account + "_" + "1";
            if (redisService.exists(key)) {
                String phoneCode = (String) redisService.get(key);
                if (phoneCode.equals(code)) {
                    result.setCode(1);
                    result.setMsg("验证成功");
                } else {
                    result.setCode(0);
                    result.setMsg("验证码错误，请重新输入");
                }
            } else {
                result.setCode(0);
                result.setMsg("验证码过期");
            }
        }
        return result;
    }

    /**
     * 椰椰
     * 2019/9/12
     * 修改密码
     *
     * @param map
     * @return
     */
    @RequestMapping(value = "changePwd", method = RequestMethod.POST)
    @ApiOperation(value = "修改密码", notes = "修改密码")
    public Result changePwd(@RequestBody Map<String, String> map) {
        Result result = new Result();
        Boolean blank = ParticipationIsBlankUtils.isBlank(map);
        if (blank) {
            result.setCode(0);
            result.setMsg("参数格式错误");
            return result;
        }
        Map<String, String> map1 = ParticipationRemoveBlankSpaceUtils.removeBlankSpace(map);
        String accessToken = map1.get("accessToken");
        String pwd = map1.get("pwd");
        Integer memberId = (Integer) redisService.get(CoinConst.TOKENKEYTOKEN + accessToken);
        if (memberId == null) {
            result.setCode(0);
            result.setMsg("token已失效，请重新登录");
            return result;
        }
        result = yangMemberService.changePwd(memberId, pwd);
        return result;
    }


    /**
     * 椰椰
     * 2019/9/16
     * 登录是否需要谷歌验证
     *
     * @param map
     * @return
     */
    @RequestMapping(value = "loginNeedGoogle", method = RequestMethod.POST)
    @ApiOperation(value = "登录是否需要谷歌验证", notes = "登录是否需要谷歌验证")
    public Result loginNeedGoogle(@RequestBody Map<String, String> map) {
        Result result = new Result();
        Boolean blank = ParticipationIsBlankUtils.isBlank(map);
        if (blank) {
            result.setCode(0);
            result.setMsg("参数格式错误");
            return result;
        }
        Map<String, String> map1 = ParticipationRemoveBlankSpaceUtils.removeBlankSpace(map);
        String loginUser = map1.get("loginUser");
        result = yangGoogleAuthService.loginNeedGoogle(loginUser);
        return result;
    }


    /**
     * 耶耶
     * time：2019/9/10
     * 发送验证码接口
     *
     * @param map
     * @return
     */
    @RequestMapping(value = "sendVerificationCode", method = RequestMethod.POST)
    @ApiOperation(value = "发送验证码接口", notes = "发送验证码接口")
    public Result sendVerificationCode(@RequestBody Map<String, String> map, HttpServletRequest request) {
        Result result = new Result();
        Boolean blank = ParticipationIsBlankUtils.isBlank(map);
        if (blank) {
            result.setCode(0);
            result.setMsg("参数格式错误");
            return result;
        }
        Map<String, String> map1 = ParticipationRemoveBlankSpaceUtils.removeBlankSpace(map);
        String account = map1.get("account");
        if (account.contains("@")) {
            result = yangMemberService.sendEmailCode(account, "1", "邮箱验证码", EmailUtils.getRandomNumber(6), 0, request);
        } else {
            result = NoteUtils.mesSend(account, mesConfig, redisService);
        }
        return result;
    }

    /**
     * 生成订单
     * 耶耶
     * time：2019/9/20
     *
     * @return
     */
    @ApiOperation(value = "生成订单", notes = "生成订单并自动进队列")
    @RequestMapping(value = "createOrder", method = RequestMethod.POST)
    public Result createOrder(@RequestBody Map<String, String> map) {
        Result result = new Result();
        Boolean blank = ParticipationIsBlankUtils.isBlank(map);
        if (blank) {
            result.setCode(0);
            result.setMsg("参数格式错误");
            return result;
        }
        Map<String, String> map1 = ParticipationRemoveBlankSpaceUtils.removeBlankSpace(map);
        Result resulta = new Result();
        try {
            String cId = map1.get("cid");
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
                //yangOrderService.updateRedis(orderId);
            }
//            redisService.add(MQ_ORDER_QUEUE+"_"+cId,id);
            //直接更新用户前五条委托记录

        } catch (Exception e) {
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
            result.setCode(Result.Code.ERROR);
            result.setMsg(e.getMessage());
            e.printStackTrace();
        } finally {
            //最后做  是否增加每日下单量处理
            if (result != null && result.getCode() != null && result.getCode().intValue() == Result.Code.SUCCESS && resulta.getData() != null) {
                String dateNow = DateUtils.getDateStrPre(0);
                Long endTime = DateUtils.getTimeStamp(dateNow + "  23:59:59") / 1000;
                Long nowTime = DateUtils.getNowTimesLong();
                int mums = (int) redisService.get(String.valueOf(resulta.getData()));
                redisService.set(String.valueOf(resulta.getData()), mums + 1, endTime - nowTime, TimeUnit.SECONDS);
            }
        }
        return result;
    }

    /**
     * 获取交易对行情列表
     * 耶耶
     * time：2019/9/28
     *
     * @param map
     * @return
     */
    @RequestMapping(value = "getCurrencyListChange", method = RequestMethod.POST)
    @ApiOperation(value = "获取交易对行情列表", notes = "获取交易对行情列表")
    public Result getCurrencyListChange(@RequestBody Map<String, String> map) {
        Result result = new Result();
        result = yangCurrencyPairService.getCurrencyListChanges(map);
        return result;
    }

    /**
     * 执行撮单系统
     * 耶耶
     * 2019/9/28
     */
    @RequestMapping(value = "orderTraderMod", method = RequestMethod.POST)
    @ApiOperation(value = "执行撮单系统", notes = "执行撮单系统")
    public void orderTraderMod() {
        YangCurrencyPair yangCurrencyPair = new YangCurrencyPair();
        OrderDealTask orderDealTask = applicationContext.getBean(OrderDealTask.class);
        orderDealTask.init();
        List<YangCurrencyPair> yangCurrencyPairs = yangCurrencyPairService.select(yangCurrencyPair);
        for (YangCurrencyPair currencyPair : yangCurrencyPairs) {
            orderDealTask.oldDeal(currencyPair);
        }
    }

    /**
     * 获取首页涨幅榜列表
     * 耶耶
     * time：2019/9/28
     *
     * @return
     */
    @RequestMapping(value = "getUpCurrencyInfos", method = RequestMethod.POST)
    @ApiOperation(value = "获取首页涨幅榜列表", notes = "获取首页涨幅榜列表")
    public Result getUpCurrencyInfos() {
        Result result = new Result();
        result = yangCurrencyPairService.getUpCurrencyInfos();
        return result;
    }

    /**
     * 获取首页成交榜列表
     * 耶耶
     * 2019/9/28
     *
     * @return
     */
    @RequestMapping(value = "getTradeCurrencyInfos", method = RequestMethod.POST)
    @ApiOperation(value = "获取首页成交榜列表", notes = "获取首页成交榜列表")
    public Result getTradeCurrencyInfos() {
        Result result = new Result();
        result = yangCurrencyPairService.getTradeCurrencyInfos();
        return result;
    }

    /**
     * 检验该用户的法币账户是否含有足够的BM
     * 耶耶
     * time：2019/9/26
     *
     * @param map
     * @return
     */
    @RequestMapping(value = "isHasEnoughBM", method = RequestMethod.POST)
    @ApiOperation(value = "法币账户是否有足够的BM", notes = "法币账户是否有足够的BM")
    public Result isHasEnoughBM(@RequestBody Map<String, String> map) {
        Result result = new Result();
        Boolean blank = ParticipationIsBlankUtils.isBlank(map);
        if (blank) {
            result.setCode(0);
            result.setMsg("参数格式错误");
            return result;
        }
        Map<String, String> map1 = ParticipationRemoveBlankSpaceUtils.removeBlankSpace(map);
        result = yangCurrencyUserService.isHasEnoughBM(map1);
        return result;
    }

    /**
     * 承兑商申请证明信息保存（图片地址，视频地址）-picInfo
     * 耶耶
     * time：2019/9/27
     *
     * @param map
     * @return
     */
    @RequestMapping(value = "updateAcceptancesPicInfo", method = RequestMethod.POST)
    @ApiOperation(value = "承兑商申请证明信息保存（图片地址，视频地址）-picInfo", notes = "承兑商申请证明信息保存（图片地址，视频地址）-picInfo")
    public Result updateAcceptancesPicInfo(@RequestBody Map<String, String> map) {
        Result result = new Result();
        Boolean blank = ParticipationIsBlankUtils.isBlank(map);
        if (blank) {
            result.setCode(0);
            result.setMsg("参数格式错误");
            return result;
        }
        Map<String, String> map1 = ParticipationRemoveBlankSpaceUtils.removeBlankSpace(map);
        result = yangAcceptanceService.updateAcceptancesPicInfo(map1);
        return result;
    }

    /**
     * 撤单接口
     * 耶耶
     *
     * @param map
     * @return
     */
    @RequestMapping(value = "cancelOrder", method = RequestMethod.POST)
    @ApiOperation(value = "撤单", notes = "撤单")
    public Result cancelOrder(@RequestBody Map<String, String> map) {
        Result result = new Result();
        Boolean blank = ParticipationIsBlankUtils.isBlank(map);
        if (blank) {
            result.setCode(0);
            result.setMsg("参数格式错误");
            return result;
        }
        Map<String, String> map1 = ParticipationRemoveBlankSpaceUtils.removeBlankSpace(map);
        String ordersId = map1.get("ordersId");
        String accessToken = map1.get("accessToken");
        result = yangOrderService.cancelOrder(ordersId, accessToken);
        return result;
    }

    /**
     * 添加银行卡信息
     * 耶耶
     *
     * @param map
     * @return
     */
    @RequestMapping(value = "saveBankInfoByUser", method = RequestMethod.POST)
    @ApiOperation(value = "添加银行卡信息", notes = "添加银行卡信息")
    public Result saveBankInfoByUser(@RequestBody Map<String, String> map) {
        Result result = new Result();
        Boolean blank = ParticipationIsBlankUtils.isBlank(map);
        if (blank) {
            result.setCode(0);
            result.setMsg("参数格式错误");
            return result;
        }
        Map<String, String> map1 = ParticipationRemoveBlankSpaceUtils.removeBlankSpace(map);
        result = yangBankService.saveBankInfoByUser(map1);
        return result;
    }

    /**
     * 检查银行卡是否存在
     * 耶耶
     *
     * @param map
     * @return
     */
    @RequestMapping(value = "checkCardOnly", method = RequestMethod.POST)
    @ApiOperation(value = "检查银行卡是否存在", notes = "检查银行卡是否存在")
    public Result checkCardOnly(@RequestBody Map<String, String> map) {
        Result result = new Result();
        Boolean blank = ParticipationIsBlankUtils.isBlank(map);
        if (blank) {
            result.setCode(0);
            result.setMsg("参数格式错误");
            return result;
        }
        Map<String, String> map1 = ParticipationRemoveBlankSpaceUtils.removeBlankSpace(map);
        result = yangBankService.checkCardOnly(map1);
        return result;
    }


    /**
     * 删除收款账户
     * 耶耶
     *
     * @param map
     * @return
     */
    @RequestMapping(value = "deleteBankInfo", method = RequestMethod.POST)
    @ApiOperation(value = "删除收款账户", notes = "删除收款账户")
    public Result deleteBankInfo(@RequestBody Map<String, String> map) {
        Result result = new Result();
        Boolean blank = ParticipationIsBlankUtils.isBlank(map);
        if (blank) {
            result.setCode(0);
            result.setMsg("参数格式错误");
            return result;
        }
        Map<String, String> map1 = ParticipationRemoveBlankSpaceUtils.removeBlankSpace(map);
        result = yangBankService.deleteBankInfo(map1);
        return result;
    }

    /**
     * 检测账户是否被绑定或注册过
     * 耶耶
     * time：2019/9/28
     *
     * @return
     */
    @RequestMapping(value = "checkAccountIsOnly", method = RequestMethod.POST)
    @ApiOperation(value = "检测账户是否被绑定或注册过", notes = "检测账户是否被绑定或注册过")
    public Result checkAccountIsOnly(@RequestBody Map<String, String> map) {
        Result result = new Result();
        Boolean blank = ParticipationIsBlankUtils.isBlank(map);
        if (blank) {
            result.setCode(0);
            result.setMsg("参数格式错误");
            return result;
        }
        Map<String, String> map1 = ParticipationRemoveBlankSpaceUtils.removeBlankSpace(map);
        result = yangMemberService.checkAccountIsOnly(map1);
        return result;
    }

    /*********************************************宋-end*****************************************************/

    /*********************************************吴-start*****************************************************/
    /**
     * wf
     * 获取轮询币对列表
     *
     * @return
     */
    @RequestMapping(value = "getIncreaseList", method = RequestMethod.POST)
    @ApiOperation(value = "获取轮询币对列表", notes = "获取轮询币对列表")
    public Result getIncreaseList() {
        Result result = new Result();
        result = yangCurrencyPairService.getIncreaseList();
        return result;
    }

    /**
     * wf
     * 获取首页新币榜列表
     *
     * @return
     */
    @RequestMapping(value = "getNewCurrenciesList", method = RequestMethod.POST)
    @ApiOperation(value = "获取新币列表", notes = "获取新币列表")
    public Result getNewCurrenciesList() {
        Result result = new Result();
        result = yangCurrencyPairService.getNewCurrenciesList();
        return result;
    }

    /**
     * wf
     * 承兑商挂卖
     *
     * @return
     */
    @RequestMapping(value = "creatAcceptancesOrder", method = RequestMethod.POST)
    @ApiOperation(value = "承兑商挂卖/买", notes = "承兑商挂卖/买")
    public Result creatAcceptancesOrder(@RequestBody Map<String, String> map) {
        Result result = new Result();
        result = yangCurrencyPairService.creatAcceptancesOrder(map);
        return result;
    }

    /**
     * wf
     * 承兑商确认收款后，放行币
     *
     * @return
     */
    @RequestMapping(value = "acceptancesOrderRelease", method = RequestMethod.POST)
    @ApiOperation(value = "承兑商确认收款后，放行币", notes = "承兑商确认收款后，放行币")
    public Result acceptancesOrderRelease(@RequestBody Map<String, String> map) {
        Result result = new Result();
        result = yangCurrencyPairService.acceptancesOrderRelease(map);
        return result;
    }

    /***
     * WF
     * 查询银行卡
     * @param map
     * @return
     */
    @RequestMapping(value = "findBanksInfoByUser", method = RequestMethod.POST)
    @ApiOperation(value = "查询银行卡", notes = "查询银行卡")
    public Result findBanksInfoByUser(@RequestBody Map<String, String> map) {
        Result result = new Result();
        result = yangC2COrdersService.findBanksInfoByUser(map);
        return result;
    }
    /*********************************************吴-end*****************************************************/

    /*********************************************蔡-start*****************************************************/
    /**
     * 获取所有币种
     *
     * @return
     * @author ccw
     */
    @RequestMapping(value = "getAllCoins", method = RequestMethod.POST)
    @ApiOperation(value = "获取所有币种", notes = "获取所有币种")
    public Result getAllCoins() {
        Result result = yangCurrencyService.getAllCoins();
        return result;
    }

    /**
     * 用户提交工单
     *
     * @param map
     * @return
     * @author ccw
     */
    @RequestMapping(value = "addWorkOrder", method = RequestMethod.POST)
    @ApiOperation(value = "用户提交工单", notes = "用户提交工单")
    public Result addWorkOrder(@RequestBody Map<String, String> map) {
        Result result = new Result();
        map = ParticipationRemoveBlankSpaceUtils.removeBlankSpace(map);
        if (ParticipationIsBlankUtils.isBlank(map)) {
            result.setCode(Result.Code.ERROR);
            result.setMsg("参数格式错误");
            return result;
        }
        result = yangWorkOrderService.addWorkOrder(map);
        return result;
    }

    /**
     * 用户查看历史工单
     *
     * @param map
     * @return
     * @author ccw
     */
    @RequestMapping(value = "findOldWorkOrders", method = RequestMethod.POST)
    @ApiOperation(value = "用户查看历史工单", notes = "用户查看历史工单")
    public Result findOldWorkOrders(@RequestBody Map<String, String> map) {
        Result result = new Result();
        map = ParticipationRemoveBlankSpaceUtils.removeBlankSpace(map);
        if (ParticipationIsBlankUtils.isBlank(map)) {
            result.setCode(Result.Code.ERROR);
            result.setMsg("参数格式错误");
            return result;
        }
        result = yangWorkOrderService.findOldWorkOrders(map);
        return result;
    }

    /**
     * 用户查询单个工单
     *
     * @param map
     * @return
     * @author ccw
     */
    @RequestMapping(value = "findWorkOrderById", method = RequestMethod.POST)
    @ApiOperation(value = "用户查询单个工单", notes = "用户查询单个工单")
    public Result findWorkOrderById(@RequestBody Map<String, String> map) {
        Result result = new Result();
        map = ParticipationRemoveBlankSpaceUtils.removeBlankSpace(map);
        if (ParticipationIsBlankUtils.isBlank(map)) {
            result.setCode(Result.Code.ERROR);
            result.setMsg("参数格式错误");
            return result;
        }
        result = yangWorkOrderService.findWorkOrderById(map);
        return result;
    }

    /**
     * 获取自选好币
     *
     * @param map
     * @return
     * @author ccw
     */
    @RequestMapping(value = "getSelfCurrencys", method = RequestMethod.POST)
    @ApiOperation(value = "获取自选好币", notes = "获取自选好币")
    public Result getSelfCurrencys(@RequestBody Map<String, String> map) {
        Result result = new Result();
        map = ParticipationRemoveBlankSpaceUtils.removeBlankSpace(map);
        if (ParticipationIsBlankUtils.isBlank(map)) {
            result.setCode(Result.Code.ERROR);
            result.setMsg("参数格式错误");
            return result;
        }
        result = yangWorkOrderService.getSelfCurrencys(map);
        return result;
    }

    /**
     * 添加自选好币
     *
     * @param map
     * @return
     * @author ccw
     */
    @RequestMapping(value = "addSelfCurrencys", method = RequestMethod.POST)
    @ApiOperation(value = "添加自选好币", notes = "添加自选好币")
    public Result addSelfCurrencys(@RequestBody Map<String, String> map) {
        Result result = new Result();
        map = ParticipationRemoveBlankSpaceUtils.removeBlankSpace(map);
        if (ParticipationIsBlankUtils.isBlank(map)) {
            result.setCode(Result.Code.ERROR);
            result.setMsg("参数格式错误");
            return result;
        }
        result = yangWorkOrderService.addSelfCurrencys(map);
        return result;
    }

    /**
     * 获取公告列表
     *
     * @param map
     * @return
     * @author ccw
     */
    @RequestMapping(value = "getArticleList", method = RequestMethod.POST)
    @ApiOperation(value = "获取公告列表", notes = "获取公告列表")
    public Result getArticleList(@RequestBody Map<String, String> map) {
        Result result = new Result();
        map = ParticipationRemoveBlankSpaceUtils.removeBlankSpace(map);
        if (ParticipationIsBlankUtils.isBlank(map)) {
            result.setCode(Result.Code.ERROR);
            result.setMsg("参数格式错误");
            return result;
        }
        String page = map.get("page");
        String language = map.get("language");
        String pageSize = map.get("pageSize");
        int pageInt = Integer.parseInt(page);
        int pageSizeInt = Integer.parseInt(pageSize);
        result = yangArticleService.getArticleList(language, "1", "", pageInt, pageSizeInt);
        return result;
    }

    /**
     * 获取公告详情
     *
     * @param map
     * @return
     * @author ccw
     */
    @RequestMapping(value = "getArticleByArticleId", method = RequestMethod.POST)
    @ApiOperation(value = "获取公告详情", notes = "获取公告详情")
    public Result getArticleByArticleId(@RequestBody Map<String, String> map) {

        Result result = yangArticleService.getArticleByArticleId("1", "", map);
        return result;
    }

    /**
     * 初级认证
     *
     * @param map
     * @return
     * @author ccw
     */
    @RequestMapping(value = "primaryCertification", method = RequestMethod.POST)
    @ApiOperation(value = "初级认证", notes = "初级认证")
    public Result primaryCertification(@RequestBody Map<String, String> map) {
        Result result = new Result();
        map = ParticipationRemoveBlankSpaceUtils.removeBlankSpace(map);
        if (ParticipationIsBlankUtils.isBlank(map)) {
            result.setCode(Result.Code.ERROR);
            result.setMsg("参数格式错误");
            return result;
        }
        String accessToken = map.get("accessToken");
        result = yangMemberService.primaryCertification(accessToken, map);
        return result;
    }

    /**
     * 获取用户信息接口
     *
     * @param map
     * @return
     * @author ccw
     */
    @RequestMapping(value = "getUserInfo", method = RequestMethod.POST)
    @ApiOperation(value = "获取用户信息接口", notes = "获取用户信息接口")
    public Result getUserInfo(@RequestBody Map<String, String> map) {
        Result result = new Result();
        map = ParticipationRemoveBlankSpaceUtils.removeBlankSpace(map);
        if (ParticipationIsBlankUtils.isBlank(map)) {
            result.setCode(Result.Code.ERROR);
            result.setMsg("参数格式错误");
            return result;
        }
        String accessToken = map.get("accessToken");
        result = yangMemberService.getUserInfo(accessToken);
        return result;
    }

    /**
     * 获取历史委托订单
     *
     * @param map
     * @return
     * @author ccw
     */
    @RequestMapping(value = "getHistoryOrdersByMemberId", method = RequestMethod.POST)
    @ApiOperation(value = "获取历史委托订单", notes = "获取历史委托订单")
    public Result getHistoryOrdersByMemberId(@RequestBody Map<String, String> map) {
        Result result = new Result();
        map = ParticipationRemoveBlankSpaceUtils.removeBlankSpace(map);
        if (ParticipationIsBlankUtils.isBlank(map)) {
            result.setCode(Result.Code.ERROR);
            result.setMsg("参数格式错误");
            return result;
        }
        String accessToken = map.get("accessToken");
        result = yangOrderService.getHistoryOrdersByMemberId(accessToken);
        return result;
    }


    /**
     * 获取用户充币地址
     *
     * @param map
     * @return
     * @author ccw
     */
    @RequestMapping(value = "getAddress", method = RequestMethod.POST)
    @ApiOperation(value = "获取用户充币地址", notes = "获取用户充币地址")
    public Result getAddress(@RequestBody Map<String, String> map) {
        Result result = new Result();
        map = ParticipationRemoveBlankSpaceUtils.removeBlankSpace(map);
        if (ParticipationIsBlankUtils.isBlank(map)) {
            result.setCode(Result.Code.ERROR);
            result.setMsg("参数格式错误");
            return result;
        }
        String accessToken = map.get("accessToken");
        String currencyId = map.get("currencyId");
        result = yangMemberService.getAddress(accessToken, currencyId);
        return result;
    }

    /**
     * 用户发出提币请求
     *
     * @param map
     * @return
     * @author ccw
     */
    @RequestMapping(value = "withdrawRequest", method = RequestMethod.POST)
    @ApiOperation(value = "用户发出提币请求", notes = "用户发出提币请求")
    public Result withdrawRequest(@RequestBody Map<String, String> map) {
        Result result = new Result();
        map = ParticipationRemoveBlankSpaceUtils.removeBlankSpace(map);
        if (ParticipationIsBlankUtils.isBlank(map)) {
            result.setCode(Result.Code.ERROR);
            result.setMsg("参数格式错误");
            return result;
        }
        String accessToken = map.get("accessToken");
        String currencyId = map.get("currencyId");
        String url = map.get("addressTo");
        String num = map.get("amount");
        String fee = map.get("fee");
        result = yangTibiOutService.withdrawRequest(accessToken, currencyId, fee, url, num);
        return result;
    }

    /**
     * 返回审核通过的提币记录 Symbol   string Amount  decimal
     * AddressTo string
     * TransID   int64
     *
     * @param map
     * @return Symbols Limit
     * @author ccw
     */
    @RequestMapping(value = "withdrawListReq", method = RequestMethod.POST)
    @ApiOperation(value = "强哥返回的提币记录，记录表中", notes = "强哥返回的提币记录，记录表中")
    public Result withdrawListReq(@RequestBody Map<String, String> map) {
        String[] currencyMarks = map.get("symbols").replace(" ", "").split(",");
        Integer limit = Integer.valueOf(map.get("limit"));
        Result result = yangTibiOutService.getWithdrawRecordByStatus(currencyMarks, limit);
        return result;
    }

    /**
     * 添加提币地址
     *
     * @param map
     * @return
     * @author ccw
     */
    @RequestMapping(value = "addDepositAddress", method = RequestMethod.POST)
    @ApiOperation(value = "添加提币地址", notes = "添加提币地址")
    public Result addDepositAddress(@RequestBody Map<String, String> map) {
        Result result = new Result();
        map = ParticipationRemoveBlankSpaceUtils.removeBlankSpace(map);
        if (ParticipationIsBlankUtils.isBlank(map)) {
            result.setCode(Result.Code.ERROR);
            result.setMsg("参数格式错误");
            return result;
        }
        String accessToken = map.get("accessToken");
        result = yangTibiOutService.addDepositAddress(accessToken, map);
        return result;
    }

    /**
     * 强哥返回的提币记录，记录表中
     *
     * @param map
     * @return TransID   int         `json:"trans_id"`
     * Symbol    string      `json:"symbol"`
     * AddressTo string      `json:"address_to"`
     * TxID      string      `json:"txid"`
     * Amount    json.Number `json:"amount"`
     * Status    int         `json:"status"`
     * RealFee   json.Number `json:"real_fee"`
     * Sign      string      `json:"sign"`
     * Timestamp int64       `json:"timestamp"`
     * @author ccw
     */
    @RequestMapping(value = "withdrawNotifyReq", method = RequestMethod.POST)
    @ApiOperation(value = "强哥返回的提币记录，记录表中", notes = "强哥返回的提币记录，记录表中")
    public Result withdrawNotifyReq(@RequestBody Map<String, String> map) {
        Result result = yangTibiOutService.withdrawNotifyReq(map);
        return result;
    }


    /**
     * 获取幻灯片
     *
     * @param map
     * @return
     * @author ccw
     */
    @RequestMapping(value = "getFlash", method = RequestMethod.POST)
    @ApiOperation(value = "获取幻灯片", notes = "获取幻灯片")
    public Result getFlash(@RequestBody Map<String, String> map) {
        Result result = new Result();
        map = ParticipationRemoveBlankSpaceUtils.removeBlankSpace(map);
        if (ParticipationIsBlankUtils.isBlank(map)) {
            result.setCode(Result.Code.ERROR);
            result.setMsg("参数格式错误");
            return result;
        }
        String type = map.get("usedType");
        result = funFlashService.getFlash("", type);
        return result;
    }


    /**
     * 强哥返回的充币记录，记录表中
     *
     * @param map AddressTo string      `json:"address_to"`
     *            Amount    json.Number `json:"amount"`
     *            Confirm   int         `json:"confirm"`
     *            IsMining  byte        `json:"is_mining"`
     *            Sign      string      `json:"sign"`
     *            Symbol    string      `json:"symbol"`
     *            TimeStamp int64       `json:"timestamp"`
     *            TxID      string      `json:"txid"`
     * @return
     * @author ccw
     */
    @RequestMapping(value = "depositNotifyReq", method = RequestMethod.POST)
    @ApiOperation(value = "强哥返回的充币记录，记录表中", notes = "强哥返回的充币记录，记录表中")
    public Result depositNotifyReq(@RequestBody Map<String, String> map) {
        Result result = yangTibiOutService.depositNotifyReq(map);
        return result;
    }

    /**
     * 获取前5条委托记录总和（币币）买
     * 有问题
     *
     * @param map
     * @return type
     * @author ccw
     */
    @RequestMapping(value = "getUserOrderFiveRecordBuy", method = RequestMethod.POST)
    @ApiOperation(value = "获取前5条委托记录总和（币币）", notes = "获取前5条委托记录总和（币币）")
    public Result getUserOrderFiveRecordBuy(@RequestBody Map<String, String> map) {
        Result result = new Result();
        result = yangOrderService.getUserOrderFiveRecordBuy(map);
        return result;
    }

    /**
     * 获取前5条委托记录总和（币币）卖
     * 有问题
     *
     * @param map
     * @return type
     * @author ccw
     */
    @RequestMapping(value = "getUserOrderFiveRecordSell", method = RequestMethod.POST)
    @ApiOperation(value = "获取前5条委托记录总和（币币）", notes = "获取前5条委托记录总和（币币）")
    public Result getUserOrderFiveRecordSell(@RequestBody Map<String, String> map) {
        Result result = new Result();
        result = yangOrderService.getUserOrderFiveRecordSell(map);
        return result;
    }

    /**
     * K线数据
     *
     * @param map seconds(时间戳) currencyId currencyTradeId start end(时间字符串)
     *            前端传来symbol 币种 resolution   from 开始时间 to 结束时间 参数使用这个
     * @return
     * @author ccw
     */

    @RequestMapping(value = "history", method = RequestMethod.POST)
    @ApiOperation(value = "K线数据", notes = "K线数据")
    public Result klineData(@RequestBody Map<String, String> map) {
        //处理前端传过来的参数，
        Result result = new Result();
        map = ParticipationRemoveBlankSpaceUtils.removeBlankSpace(map);
        if (ParticipationIsBlankUtils.isBlank(map)) {
            result.setCode(Result.Code.ERROR);
            result.setMsg("参数格式错误");
            return result;
        }
        String symbol = map.get("symbol");
        String resolution = map.get("resolution");
        String from = map.get("from");
        String to = map.get("to");
        //根据mark查询pair表中的 id 和 trade_id
        Map sParam = new HashMap();
        sParam.put("cyId", symbol);
        List<Map> list = yangCurrencyPairMapper.getCurrencyPairList(sParam);
        Integer timeCount;
//        YangCurrencyPair yangCurrencyPair = yangCurrencyPairMapper.getYangCurrencyPairByMark(currencyMark);
        switch (resolution) {
            case "1":
                timeCount = 1;
                break;
            case "3":
                timeCount = 3;
                break;
            case "5":
                timeCount = 5;
                break;
            case "15":
                timeCount = 15;
                break;
            case "30":
                timeCount = 30;
                break;
            case "60":
                timeCount = 60;
                break;
            case "120":
                timeCount = 120;
                break;
            case "240":
                timeCount = 240;
                break;
            case "360":
                timeCount = 360;
                break;
            case "720":
                timeCount = 720;
                break;
            case "1440":
                timeCount = 1440;
                break;
            case "D":
                timeCount = 60 * 24;
                break;
            case "3D":
                timeCount = 60 * 24 * 3;
                break;
            case "W":
                timeCount = 60 * 24 * 7;
                break;
            case "M":
                timeCount = 60 * 24 * 30;
                break;
            default:
                timeCount = 5;
                break;

        }
        Integer addTime = timeCount * 60;

        try {
            //放入参数
            Map param = new HashMap();
            param.put("addTime", addTime);
//            if (yangCurrencyPair != null) {
//                if (StringUtils.isNotBlank(yangCurrencyPair.getCurrencyId().toString())) {
//                    param.put("currencyId", yangCurrencyPair.getCurrencyId());
//                }
//                if (StringUtils.isNotBlank(yangCurrencyPair.getCurrencyTradeId().toString())) {
//                    param.put("currencyTradeId", yangCurrencyPair.getCurrencyTradeId());
//                }
//            }
            String currencyId = "";
            String currencyTradeId = "";
            if (list.size() > 0) {
                Map data = list.get(0);
                currencyId = data.get("currency_id").toString();
                currencyTradeId = data.get("currency_trade_id").toString();
            }

            if (StringUtils.isNotBlank(currencyId)) {
                param.put("currencyId", currencyId);
            }

            if (StringUtils.isNotBlank(currencyTradeId)) {
                param.put("currencyTradeId", currencyTradeId);
            }

            YangCurrencyPair yangCurrencyPair = yangCurrencyPairMapper.getYangCurrencyPairByPairId(currencyId, currencyTradeId);
            param.put("start", from);
            param.put("end", to);
            //返回的数据,主要问题在这
            List<Map> maps = yangOrderService.getKlineDataApi(param);
            if (list.size() > 0) {
                //填充中间没有交易产生的数据
                List<Map> datas = yangOrderService.dealEmpty(maps, addTime);
                //五个集合
                List<Double> o = new ArrayList<>();
                List<Double> c = new ArrayList<>();
                List<Double> h = new ArrayList<>();
                List<Double> l = new ArrayList<>();
                List<Double> vo = new ArrayList<>();
                List<Integer> t = new ArrayList<>();
                List<Map> end = new ArrayList<>();
                Integer scale = 5;
                for (Map data : datas) {
                    if (yangCurrencyPair != null) {
                        scale = yangCurrencyPair.getInputPriceNum();
                    }
                    Map endResult = new HashMap();
                    BigDecimal open = new BigDecimal(data.get("open_price").toString());
                    double openPrice1 = open.setScale(scale, BigDecimal.ROUND_HALF_UP).doubleValue();
                    o.add(openPrice1);
                    endResult.put("o", openPrice1);
                    BigDecimal close = new BigDecimal(data.get("close_price").toString());
                    double closePrice1 = close.setScale(scale, BigDecimal.ROUND_HALF_UP).doubleValue();
                    c.add(closePrice1);
                    endResult.put("c", closePrice1);
                    BigDecimal max = new BigDecimal(data.get("max_price").toString());
                    double maxPrice1 = max.setScale(scale, BigDecimal.ROUND_HALF_UP).doubleValue();
                    h.add(maxPrice1);
                    endResult.put("h", maxPrice1);
                    BigDecimal min = new BigDecimal(data.get("min_price").toString());
                    double minPrice1 = min.setScale(scale, BigDecimal.ROUND_HALF_UP).doubleValue();
                    l.add(minPrice1);
                    endResult.put("l", minPrice1);
                    BigDecimal n = new BigDecimal(data.get("num").toString());
                    double num1 = n.setScale(scale, BigDecimal.ROUND_HALF_UP).doubleValue();
                    vo.add(num1);
                    endResult.put("vo", num1);
                    Integer trade = Integer.parseInt(data.get("trade_time").toString()) + (9 * 3600);
                    t.add(trade);
                    endResult.put("t", trade);
                    end.add(endResult);
                }
                Map resultData = new HashMap();
                resultData.put("o", o);
                resultData.put("c", c);
                resultData.put("h", h);
                resultData.put("l", l);
                resultData.put("vo", vo);
                resultData.put("t", t);
                result.setCode(Result.Code.SUCCESS);
                result.setData(end);
                result.setMsg("查询k线成功");
            } else {
                //没有数据 直接返回
                result.setCode(Result.Code.SUCCESS);
                result.setMsg("查询k线数据失败");
                return result;

            }

        } catch (Exception e) {
            result.setMsg(e.getMessage());
            result.setCode(Result.Code.ERROR);
            e.printStackTrace();
        }
        return result;
    }

    /**
     * k线涨跌幅
     * ccw
     * time：2019/9/28
     *
     * @return
     */
    @RequestMapping(value = "kCurrencyInfo", method = RequestMethod.POST)
    @ApiOperation(value = "k线涨跌幅", notes = "k线涨跌幅")
    public Result kCurrencyInfo(@RequestBody Map<String, String> map) {
        Result result = new Result();
        String symbol = map.get("symbol");
        result = yangCurrencyPairService.getUpCurrencyInfo(symbol);
        return result;
    }

    /**
     * k 获取币种信息
     *
     * @returne
     * @date 2019 9 27 17:00
     * @Param map
     */
    @RequestMapping(value = "symbols", method = RequestMethod.POST)
    @ApiOperation(value = " k 获取币种信息", notes = " k 获取币种信息")
    //获取币种信息
    public Map symbols() {
        Map result = new HashMap();
        Map param = new HashMap();
        List<Map> list = yangCurrencyPairMapper.getCurrencyPairList(param);
        List<Map> resultDatas = new ArrayList<>();
        if (list.size() > 0) {
            for (Map data : list) {
                Map resultData = new HashMap();
                String mark = data.get("currency_mark").toString();
                String mark1 = data.get("tradeMark").toString();
                String symbol = mark + "/" + mark1;
                resultData.put("symbol", symbol);
                resultData.put("coinSymbol", mark);
                resultData.put("baseSymbol", mark1);
                resultData.put("coinScale", Integer.parseInt(data.get("input_price_num").toString()));
                resultData.put("baseCoinScale", Integer.parseInt(data.get("input_price_num").toString()));
                resultData.put("ticker", data.get("cy_id"));
                resultData.put("timezone", "Asia/Seoul");
                resultData.put("minmov", "1");
                resultData.put("minmov2", "0");
                resultDatas.add(resultData);
            }

//            Map data = list.get(0);
//            String mark = data.get("'currency_mark'").toString();
//            String mark1 = data.get("'trade_mark'").toString();
//            String name = mark + "/" + mark1;
//            result.put("name", name);
//            result.put("ticker", data.get("'cy_id'"));
//            result.put("description", "");
//            result.put("type", mark1);
//            result.put("session", "0000-2359:1234567");
//            result.put("has_intraday", true);
//            result.put("has_daily", true);
//            result.put("intraday_multipliers", new String[]{"1", "3", "5", "15", "30", "60", "240"});
//            result.put("has_weekly_and_monthly", true);
//            result.put("listed_exchange", "funcoin");
//            result.put("timezone", "Asia/Seoul");
//            result.put("minmov", "1");
//            result.put("minmov2", "0");
//            result.put("pricescale", Math.pow(10, Integer.parseInt(data.get("input_price_num").toString())));
            result.put("data", resultDatas);
        } else {
            result.put("data", "参数错误");
            return result;
        }
        return result;
    }


    /**
     * K线数据 ccw
     *
     * @return
     */
    @RequestMapping(value = "delMongodb", method = RequestMethod.POST)
    @ApiOperation(value = "删除Mongodb", notes = "K线数据")
    public Result delMongodb(@RequestBody Map<String, String> map) {
        Result result = new Result();
        result.setCode(Result.Code.ERROR);
//        if ("1".equals(map.get("delFlag"))) {
//            yangOrderService.delMongoDB();
//        }
        return result;
    }


    /**
     * ccw
     * 当前用户的所有挂单信息
     *
     * @param map
     * @return 币种的挂单信息
     */
    @RequestMapping(value = "findC2COrdersByAcceptances", method = RequestMethod.POST)
    @ApiOperation(value = "获取挂单记录", notes = "获取挂单记录")
    public Result findC2COrdersByAcceptances(@RequestBody Map<String, String> map) {
        Result result = new Result();
        map = ParticipationRemoveBlankSpaceUtils.removeBlankSpace(map);
        if (ParticipationIsBlankUtils.isBlank(map)) {
            result.setCode(Result.Code.ERROR);
            result.setMsg("参数格式错误");
            return result;
        }
        result = yangC2CGuaService.findC2COrdersByAcceptances(map.get("accessToken"));
        return result;
    }

    /**
     * 用户下单记录列表 cq
     *
     * @return
     */
    @RequestMapping(value = "findMembersOrdersByMemberId", method = RequestMethod.POST)
    @ApiOperation(value = "用户下单记录列表", notes = "用户下单记录列表")
    public Result findMembersOrdersByMemberId(@RequestBody Map<String, String> map) {
        Result result = new Result();
        map = ParticipationRemoveBlankSpaceUtils.removeBlankSpace(map);
        if (ParticipationIsBlankUtils.isBlank(map)) {
            result.setCode(Result.Code.ERROR);
            result.setMsg("参数格式错误");
            return result;
        }
        result = yangC2CGuaService.findMembersOrdersByAcceptances(map.get("accessToken"), map.get("buyStatus"));
        return result;
    }

    /**
     * 根据承兑商id获取该承兑商下面的所有相关法币订单
     * 修改人 cq
     * 2019/10/3
     *
     * @param map
     * @return 该承兑商下面的所有相关法币订单
     */
    @RequestMapping(value = "findMembersOrdersByAcceptances", method = RequestMethod.POST)
    @ApiOperation(value = "用户下单记录列表", notes = "用户下单记录列表")
    public Result findMembersOrdersByAcceptances(@RequestBody Map<String, String> map) {
        Result result = new Result();
        map = ParticipationRemoveBlankSpaceUtils.removeBlankSpace(map);
        if (ParticipationIsBlankUtils.isBlank(map)) {
            result.setCode(Result.Code.ERROR);
            result.setMsg("参数格式错误");
            return result;
        }
        result = yangC2CGuaService.findMembersOrdersByAcceptances(map.get("accessToken"), map.get("buyStatus"));
        return result;
    }

    /**
     * 承兑商申请基本信息保存-baseInfo
     *
     * @return
     * @Param map
     */
    @RequestMapping(value = "saveAcceptancesBaseInfo", method = RequestMethod.POST)
    @ApiOperation(value = "承兑商申请基本信息保存", notes = "承兑商申请基本信息保存")
    public Result saveAcceptancesBaseInfo(@RequestBody Map<String, String> map) {
        Result result = new Result();
        map = ParticipationRemoveBlankSpaceUtils.removeBlankSpace(map);
        if (ParticipationIsBlankUtils.isBlank(map)) {
            result.setCode(Result.Code.ERROR);
            result.setMsg("参数格式错误");
            return result;
        }
        result = yangAcceptanceService.saveAcceptancesBaseInfo(map);
        return result;
    }

    /**
     * 获取委托订单
     *
     * @param map
     * @return
     * @author ccw
     */
    @RequestMapping(value = "getOrdersByMemberId", method = RequestMethod.POST)
    @ApiOperation(value = "获取委托订单", notes = "获取委托订单")
    public Result getOrdersByMemberId(@RequestBody Map<String, String> map) {
        Result result = new Result();
        map = ParticipationRemoveBlankSpaceUtils.removeBlankSpace(map);
        if (ParticipationIsBlankUtils.isBlank(map)) {
            result.setCode(Result.Code.ERROR);
            result.setMsg("参数格式错误");
            return result;
        }
        String accessToken = map.get("accessToken");
        result = yangOrderService.getOrdersByMemberId(accessToken);
        return result;
    }

    /**
     * 承兑商撤销审核
     *
     * @return
     * @date 2019 9 27 17:00
     * @Param map
     */
    @RequestMapping(value = "deletAcceptancesById", method = RequestMethod.POST)
    @ApiOperation(value = "承兑商撤销审核", notes = "承兑商撤销审核")
    public Result deletAcceptancesById(@RequestBody Map<String, String> map) {
        Result result = new Result();
        map = ParticipationRemoveBlankSpaceUtils.removeBlankSpace(map);
        if (ParticipationIsBlankUtils.isBlank(map)) {
            result.setCode(Result.Code.ERROR);
            result.setMsg("参数格式错误");
            return result;
        }
        result = yangAcceptanceService.deletAcceptancesById(map);
        return result;
    }


    /**
     * 承兑商挂单撤单（买）
     *
     * @return
     * @date 2019 9 27 17:00
     * @Param map
     */
    @RequestMapping(value = "cancleAcceptancesBuyOrder", method = RequestMethod.POST)
    @ApiOperation(value = " 承兑商挂单撤单（买）", notes = " 承兑商挂单撤单（买）")
    public Result cancleAcceptancesBuyOrder(@RequestBody Map<String, String> map) {
        Result result = new Result();
        map = ParticipationRemoveBlankSpaceUtils.removeBlankSpace(map);
        if (ParticipationIsBlankUtils.isBlank(map)) {
            result.setCode(Result.Code.ERROR);
            result.setMsg("参数格式错误");
            return result;
        }
        result = yangC2CGuaService.cancleAcceptancesBuyOrder(map, "buy");
        return result;
    }

    /**
     * 承兑商挂单撤单（卖）
     *
     * @return
     * @date 2019 9 27 17:00
     * @Param map
     */
    @RequestMapping(value = "cancleAcceptancesSellOrder", method = RequestMethod.POST)
    @ApiOperation(value = " 承兑商挂单撤单（卖）", notes = " 承兑商挂单撤单（卖）")
    public Result cancleAcceptancesSellOrder(@RequestBody Map<String, String> map) {
        Result result = new Result();
        map = ParticipationRemoveBlankSpaceUtils.removeBlankSpace(map);
        if (ParticipationIsBlankUtils.isBlank(map)) {
            result.setCode(Result.Code.ERROR);
            result.setMsg("参数格式错误");
            return result;
        }
        result = yangC2CGuaService.cancleAcceptancesBuyOrder(map, "sell");
        return result;
    }

    /**
     * 承兑商申请修改审核状态
     *
     * @return
     * @date 2019 9 27 17:00
     * @Param map
     */
    @RequestMapping(value = "updateAcceptancesIsReady", method = RequestMethod.POST)
    @ApiOperation(value = " 承兑商申请修改审核状态", notes = " 承兑商申请修改审核状态")
    public Result updateAcceptancesIsReady(@RequestBody Map<String, String> map) {
        Result result = new Result();
        map = ParticipationRemoveBlankSpaceUtils.removeBlankSpace(map);
        if (ParticipationIsBlankUtils.isBlank(map)) {
            result.setCode(Result.Code.ERROR);
            result.setMsg("参数格式错误");
            return result;
        }
        result = yangAcceptanceService.updateAcceptancesIsReady(map);
        return result;
    }
    /*********************************************蔡-end*****************************************************/


    /*********************************************杨-start*****************************************************/

    /**
     * yxt
     * 2019/9/16
     * 设置资金密码
     *
     * @param map
     * @return
     */
    @RequestMapping(value = "setFiatMoneyPwd", method = RequestMethod.POST)
    @ApiOperation(value = "设置资金密码", notes = "设置资金密码")
    public Result setFiatMoneyPwd(@RequestBody Map<String, String> map) {
        Result result = new Result();
        String accessToken = map.get("accessToken");
        String fiatMoneyPwd = map.get("fiatMoneyPwd");
        Integer memberId = (Integer) redisService.get(CoinConst.TOKENKEYTOKEN + accessToken);
        if (memberId == null) {
            result.setCode(0);
            result.setMsg("token已失效，请重新登录");
            return result;
        }
        map = ParticipationRemoveBlankSpaceUtils.removeBlankSpace(map);
        if (ParticipationIsBlankUtils.isBlank(map)) {
            result.setCode(Result.Code.ERROR);
            result.setMsg("参数格式错误");
            return result;
        }
        result = memberService.setsFiatMoneyPwd(memberId, fiatMoneyPwd);
        return result;
    }

    /**
     * yxt
     * 2019/9/16
     * 修改资金密码
     *
     * @param map
     * @return
     */
    @RequestMapping(value = "updateFiatMoneyPwd", method = RequestMethod.POST)
    @ApiOperation(value = "修改资金密码", notes = "修改资金密码")
    public Result updateFiatMoneyPwd(@RequestBody Map<String, String> map) {
        Result result = new Result();
        map = ParticipationRemoveBlankSpaceUtils.removeBlankSpace(map);
        if (ParticipationIsBlankUtils.isBlank(map)) {
            result.setCode(Result.Code.ERROR);
            result.setMsg("参数格式错误");
            return result;
        }

        String accessToken = map.get("accessToken");
        String fiatMoneyPwd = map.get("fiatMoneyPwd");
        Integer memberId = (Integer) redisService.get(CoinConst.TOKENKEYTOKEN + accessToken);
        if (memberId == null) {
            result.setCode(0);
            result.setMsg("token已失效，请重新登录");
            return result;
        }
        result = memberService.updateFiatMoneyPwd(memberId, fiatMoneyPwd);
        return result;
    }


    /**
     * yxt
     * 2019/9/16
     * 找到资金密码
     *
     * @param map
     * @return
     */
    @RequestMapping(value = "findFiatMoneyPwd", method = RequestMethod.POST)
    @ApiOperation(value = "找回资金密码", notes = "找回资金密码")
    public Result findFiatMoneyPwd(@RequestBody Map<String, String> map) {
        Result result = new Result();
        map = ParticipationRemoveBlankSpaceUtils.removeBlankSpace(map);
        if (ParticipationIsBlankUtils.isBlank(map)) {
            result.setCode(Result.Code.ERROR);
            result.setMsg("参数格式错误");
            return result;
        }
        String accessToken = map.get("accessToken");
        String account = map.get("account");
        String pwd = map.get("fiatMoneyPwd");
        String verificationCode = map.get("verificationCode");
        YangMember yangMembers = new YangMember();
        result = yangMemberService.findFiatMoneyPwds(accessToken, account, pwd, verificationCode);
        return result;
    }

    /**
     * 找回密码
     *
     * @param map
     * @return
     */
    @RequestMapping(value = "findMemberPwd", method = RequestMethod.POST)
    @ApiOperation(value = "找回密码", notes = "找回密码")
    public Result findMemberPwd(@RequestBody Map<String, String> map) {
        Result result = new Result();
        map = ParticipationRemoveBlankSpaceUtils.removeBlankSpace(map);
        if (ParticipationIsBlankUtils.isBlank(map)) {
            result.setCode(Result.Code.ERROR);
            result.setMsg("参数格式错误");
            return result;
        }
        String account = map.get("account");
        String pwd = map.get("pwd");
        String verificationCode = map.get("verificationCode");
        result = yangMemberService.findMemberPwd(account, pwd, verificationCode);
        return result;
    }
//
//    /**
//     * 重置密码
//     *
//     * @param map
//     * @return
//     */
//    @RequestMapping(value = "resetPwd", method = RequestMethod.POST)
//    @ApiOperation(value = "重置密码", notes = "重置密码")
//    public Result resetPwd(@RequestBody Map<String, String> map) {
//        Result result = new Result();
//        String account = map.get("account");
//        String pwd = map.get("pwd");
//        String verificationCode = map.get("verificationCode");
//        result = yangMemberService.findMemberPwd(account, pwd, verificationCode);
//        return result;
//    }

    /**
     * yxt
     * 2019/9/17
     * 指定币的所有历史提币记录
     *
     * @param map acessToken(用户的信息) currencyId(币种的id)
     * @return 定币的所有历史提币记录
     */
    @RequestMapping(value = "getWithdrawRecordByCurrencyId", method = RequestMethod.POST)
    @ApiOperation(value = "指定币的所有历史充币记录", notes = "指定币的所有历史充币 记录")
    public Result getWithdrawRecordByCurrencyId(@RequestBody Map<String, String> map) {
        Result result = new Result();
        map = ParticipationRemoveBlankSpaceUtils.removeBlankSpace(map);
        if (ParticipationIsBlankUtils.isBlank(map)) {
            result.setCode(Result.Code.ERROR);
            result.setMsg("参数格式错误");
            return result;
        }
        String accessToken = map.get("accessToken");
        String currencyId = map.get("currencyId");
        Integer userId = (Integer) redisService.get(CoinConst.TOKENKEYTOKEN + accessToken);
        if (userId == null) {
            result.setCode(0);
            result.setMsg("token已失效，请重新登录");
            return result;
        }
        result = yangTibiOutService.getWithdrawRecordByCurrencyId(currencyId, accessToken);
        return result;
    }

    /**
     * yxt
     * 2019/9/17
     * 获取历史提币记录
     *
     * @param map acessToken(用户的信息)
     * @return 提币的记录信息
     */
    @RequestMapping(value = "getDepositRecord", method = RequestMethod.POST)
    @ApiOperation(value = "获取历史提币记录", notes = "获取历史提币记录")
    public Result getDepositRecord(@RequestBody Map<String, String> map) {
        Result result = new Result();
        map = ParticipationRemoveBlankSpaceUtils.removeBlankSpace(map);
        if (ParticipationIsBlankUtils.isBlank(map)) {
            result.setCode(Result.Code.ERROR);
            result.setMsg("参数格式错误");
            return result;
        }
        String accessToken = map.get("accessToken");
        Integer memberId = (Integer) redisService.get(CoinConst.TOKENKEYTOKEN + accessToken);
        if (memberId == null) {
            result.setCode(0);
            result.setMsg("token已失效，请重新登录");
            return result;
        }
        result = yangTibiService.getDepositRecord(accessToken);
        return result;
    }

    /**
     * 获取币种的基本信息
     * yxt
     * 2019/9/17
     *
     * @param map acessToken(用户的信息) currencyId(币种的id)
     * @return 币种的基本信息
     */
    @RequestMapping(value = "getCurrencyInfoById", method = RequestMethod.POST)
    @ApiOperation(value = "获取币种基本信息", notes = "获取币种基本信息")
    public Result getCurrencyInfoById(@RequestBody Map<String, String> map) {
        Result result = new Result();
        map = ParticipationRemoveBlankSpaceUtils.removeBlankSpace(map);
        if (ParticipationIsBlankUtils.isBlank(map)) {
            result.setCode(Result.Code.ERROR);
            result.setMsg("参数格式错误");
            return result;
        }
        String accessToken = map.get("accessToken");
        Integer currencyId = Integer.valueOf(map.get("currencyId"));
        Integer userId = (Integer) redisService.get(CoinConst.TOKENKEYTOKEN + accessToken);
        if (userId == null) {
            result.setCode(0);
            result.setMsg("token已失效，请重新登录");
            return result;
        }
        result = yangCurrencyService.getCurrencyInfoById(currencyId);
        return result;
    }

    /**
     * 当前币种的所有挂单记录
     * yxt
     * 2019/9/17
     *
     * @param map acessToken(用户的信息) currencyId(币种的id)
     * @return 当前币种的所有挂单记录
     */
    @RequestMapping(value = "getC2CGuaByCurrencyId", method = RequestMethod.POST)
    @ApiOperation(value = "查询当前币种的所有挂单记录（法币）", notes = "查询当前币种的所有挂单记录（法币）")
    public Result getC2CGuaByCurrencyId(@RequestBody Map<String, String> map) {
        Result result = new Result();
        map = ParticipationRemoveBlankSpaceUtils.removeBlankSpace(map);
        if (ParticipationIsBlankUtils.isBlank(map)) {
            result.setCode(Result.Code.ERROR);
            result.setMsg("参数格式错误");
            return result;
        }
        String accessToken = map.get("accessToken");
        Integer currencyId = Integer.valueOf(map.get("currencyId"));
        Integer userId = (Integer) redisService.get(CoinConst.TOKENKEYTOKEN + accessToken);
        if (userId == null) {
            result.setCode(0);
            result.setMsg("token已失效，请重新登录");
            return result;
        }
        result = yangC2CGuaService.getC2CGuaByCurrencyId(currencyId, accessToken);
        return result;
    }

    /**
     * 获取谷歌信息
     * yxt
     *
     * @param map
     * @return
     */
    @RequestMapping(value = "getGoogle", method = RequestMethod.POST)
    @ApiOperation(value = "获取谷歌信息", notes = "获取谷歌信息")
    public Result getGoogle(@RequestBody Map<String, String> map) {
        Result result = new Result();
        /**
         * 判断map是否为空
         */
        map = ParticipationRemoveBlankSpaceUtils.removeBlankSpace(map);
        if (ParticipationIsBlankUtils.isBlank(map)) {
            result.setCode(Result.Code.ERROR);
            result.setMsg("参数格式错误");
            return result;
        }
        //从map中获取accessToken
        String accessToken = map.get("accessToken");
        //获取memberId
        Integer memberId = (Integer) redisService.get(CoinConst.TOKENKEYTOKEN + accessToken);
        //判断memberId是否存在
        if (memberId == null) {
            result.setCode(0);
            result.setMsg("token已失效，请重新登录");
            return result;
        }
        //获取谷歌的信息
        result = yangGoogleAuthService.getGoogles(accessToken);
        //返回获取的用户信息
        return result;
    }

    /**
     * 验证谷歌的验证码
     * yxt
     *
     * @param map
     * @return
     */
    @RequestMapping(value = "verifyGoogleCode", method = RequestMethod.POST)
    @ApiOperation(value = "验证谷歌验证码", notes = "验证谷歌验证码")
    public Result verifyGoogleCode(@RequestBody Map<String, String> map) {
        Result result = new Result();
        /**
         * 判断map是否为空
         */
        map = ParticipationRemoveBlankSpaceUtils.removeBlankSpace(map);
        if (ParticipationIsBlankUtils.isBlank(map)) {
            result.setCode(Result.Code.ERROR);
            result.setMsg("参数格式错误");
            return result;
        }
        String account = map.get("account");
        String code = map.get("code");
        try {
            String savedSecret = yangGoogleAuthService.findSecrets(account).getSecret();
            Boolean count = GoogleAuthenticator.authcode(code, savedSecret);
            if (count == true) {
                result.setMsg("验证成功");
                result.setCode(1);
            } else {
                result.setMsg("验证失败");
                result.setCode(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.setErrorCode(ErrorCode.SEND_EMAILCODE_ERROR.getIndex());
            result.setCode(Result.Code.ERROR);
            result.setMsg(ErrorCode.SEND_EMAILCODE_ERROR.getMessage());
        }
        return result;
    }

    /**
     * 生成谷歌的秘钥和二维码路径
     * yxt
     *
     * @param map
     * @return
     */
    @RequestMapping(value = "addGoogleSecretAndQR", method = RequestMethod.POST)
    @ApiOperation(value = "生成用户谷歌秘钥和二维码路径", notes = "生成用户谷歌秘钥和二维码路径")
    public Result addGoogleSecretAndQR(@RequestBody Map<String, String> map) {
        Result result = new Result();
        /**
         * 判断map是否为空
         */
        map = ParticipationRemoveBlankSpaceUtils.removeBlankSpace(map);
        if (ParticipationIsBlankUtils.isBlank(map)) {
            result.setCode(Result.Code.ERROR);
            result.setMsg("参数格式错误");
            return result;
        }
        String accessToken = map.get("accessToken");
        Integer memberId = (Integer) redisService.get(CoinConst.TOKENKEYTOKEN + accessToken);
        //判断memberId是否存在
        if (memberId == null) {
            result.setCode(0);
            result.setMsg("token已失效，请重新登录");
            return result;
        }
        result = yangGoogleAuthService.addGoogleSecretAndQR(accessToken);
        return result;
    }

    /**
     * 解绑谷歌验证
     * yxt
     *
     * @param map
     * @return
     */
    @RequestMapping(value = "cancleGoogle", method = RequestMethod.POST)
    @ApiOperation(value = "解绑谷歌验证", notes = "解绑谷歌验证")
    public Result cancleGoogle(@RequestBody Map<String, String> map) {
        Result result = new Result();
        /*判断map是否为空*/
        map = ParticipationRemoveBlankSpaceUtils.removeBlankSpace(map);
        if (ParticipationIsBlankUtils.isBlank(map)) {
            result.setCode(Result.Code.ERROR);
            result.setMsg("参数格式错误");
            return result;
        }
        String accessToken = map.get("accessToken");
        String account = map.get("account");
        String code = map.get("code");
        Integer memberId = (Integer) redisService.get(CoinConst.TOKENKEYTOKEN + accessToken);
        if (memberId == null) {
            result.setCode(0);
            result.setMsg("token已失效，请重新登录");
            return result;
        }
        result = yangGoogleAuthService.cancleGoogle(accessToken, account, code);
        return result;
    }

    /**
     * 修改谷歌验证
     * yxt
     *
     * @param map
     * @return
     */
    @RequestMapping(value = "updateGoogle", method = RequestMethod.POST)
    @ApiOperation(value = "修改谷歌验证", notes = "修改谷歌验证")
    public Result googleUpdate(@RequestBody Map<String, String> map) {
        Result result = new Result();
        /*判断map是否为空*/
        map = ParticipationRemoveBlankSpaceUtils.removeBlankSpace(map);
        if (ParticipationIsBlankUtils.isBlank(map)) {
            result.setCode(Result.Code.ERROR);
            result.setMsg("参数格式错误");
            return result;
        }
        String accessToken = map.get("accessToken");
        String account = map.get("account");
        String loginLock = map.get("loginLock");
        String orderLock = map.get("orderLock");
        String moneyLock = map.get("moneyLock");
        String code = map.get("code");
        result = yangGoogleAuthService.updateGoogle(accessToken, loginLock, orderLock, moneyLock, account, code);
        return result;
    }

    /**
     * 获取交易市场,查找可以显示的交易区
     * yxt
     *
     * @return
     */
    @RequestMapping(value = "getTradeCurrencys", method = RequestMethod.POST)
    @ApiOperation(value = "获取交易市场（计价币）,查找可显示的交易区", notes = "获取交易市场（计价币）,查找可显示的交易区")
    public Result getTradeCurrencys() {
        Result result = new Result();
        result = marketService.getTradeCurrencys();
        if (result != null) {
            result.setCode(1);
            result.setMsg("获取成功");
        } else {
            result.setCode(0);
            result.setMsg("获取失败,没有找到信息");
        }
        return result;
    }

    /**
     * 获取币种的介绍信息
     * yxt
     *
     * @param map acccessToken(用户的token) currencyId(货币的id)
     * @return 币种的介绍信息
     */
    @RequestMapping(value = "getCurrencyProfile", method = RequestMethod.POST)
    @ApiOperation(value = "获取币种介绍信息", notes = "获取币种介绍信息")
    public Result getCurrencyProfile(@RequestBody Map<String, String> map) {
        Result result = new Result();
        /*判断map是否为空*/
        map = ParticipationRemoveBlankSpaceUtils.removeBlankSpace(map);
        if (ParticipationIsBlankUtils.isBlank(map)) {
            result.setCode(Result.Code.ERROR);
            result.setMsg("参数格式错误");
            return result;
        }
        Integer currencyId = Integer.valueOf(map.get("currencyId"));
        result = yangNewBiService.getCurrencyInfoById(currencyId);
        return result;
    }
    /*********************************************杨-end*****************************************************/
}
