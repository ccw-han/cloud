package net.cyweb.service;

import com.alibaba.fastjson.JSONObject;
import cyweb.utils.CoinConst;
import cyweb.utils.ErrorCode;
import cyweb.utils.GoogleAuthenticator;
import jnr.ffi.annotations.In;
import net.cyweb.mapper.YangGoogleauthMapper;
import net.cyweb.mapper.YangMemberMapper;
import net.cyweb.model.EmailLog;
import net.cyweb.model.Result;
import net.cyweb.model.YangGoogleauth;
import net.cyweb.model.YangMember;
import net.sf.jsqlparser.expression.operators.relational.RegExpMySQLOperator;
import org.apache.commons.codec.language.Metaphone;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class YangGoogleAuthService extends BaseService<YangGoogleauth> {
    @Autowired
    private YangGoogleauthMapper yangGoogleauthMapper;

    @Autowired
    private YangMemberTokenService yangMemberTokenService;

    @Autowired
    private RedisService redisService;

    @Autowired
    YangMemberService yangMemberService;

    @Autowired
    private YangMemberMapper memberMapper;

    @Autowired
    private YangGoogleAuthService yangGoogleAuthService;

    public Result googleUpdate(String accessToken, String loginLock, String orderLock, String moneyLock, String emailCode) {
        Result result = new Result();
        result.setCode(Result.Code.SUCCESS);
        try {
            //现获取用户信息
            validateAccessToken(accessToken, result, yangMemberTokenService);
            if (result.getCode().intValue() == Result.Code.ERROR.intValue()) {
                return result;
            }
            YangMember yangMemberSelf = (YangMember) result.getData();
            String key = CoinConst.EMAILFORFINDCODE + yangMemberSelf.getEmail() + "_" + CoinConst.EMAIL_CODE_TYPE_MODGOOGLE;
            if (redisService.exists(key)) {
                EmailLog emailLog = JSONObject.parseObject(redisService.get(key).toString(), EmailLog.class);
                if (emailCode.equals(emailLog.getContent())) {
                    Example example = new Example(YangGoogleauth.class);
                    example.createCriteria().andEqualTo("memberId", yangMemberSelf.getMemberId());
                    List<YangGoogleauth> list = yangGoogleauthMapper.selectByExample(example);
                    if (list == null || list.size() != 1) {
                        result.setCode(Result.Code.ERROR);
                        result.setMsg("未查询到谷歌验证信息");
                        result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
                        return result;
                    }
                    //绑定谷歌验证信息
                    YangGoogleauth yangGoogleauth = list.get(0);
                    yangGoogleauth.setMemberId(yangMemberSelf.getMemberId());
                    yangGoogleauth.setLoginLock(Integer.valueOf(loginLock));
                    yangGoogleauth.setOrderLock(Integer.valueOf(orderLock));
                    yangGoogleauth.setMoneyLock(Integer.valueOf(moneyLock));
                    yangGoogleauthMapper.updateByPrimaryKeySelective(yangGoogleauth);
                    result.setCode(Result.Code.SUCCESS);
                } else {
                    result.setCode(Result.Code.ERROR);
                    result.setMsg("验证码错误,请重新请求");
                    result.setErrorCode(ErrorCode.ERROR_ERROR_CODE.getIndex());
                }
            } else {
                result.setCode(Result.Code.ERROR);
                result.setMsg("验证码不存在或已过期");
                result.setErrorCode(ErrorCode.ERROR_ERROR_CODE.getIndex());
            }

        } catch (Exception e) {
            e.printStackTrace();
            result.setErrorCode(ErrorCode.SEND_EMAILCODE_ERROR.getIndex());
            result.setCode(Result.Code.ERROR);
            result.setMsg(ErrorCode.SEND_EMAILCODE_ERROR.getMessage());
        }
        return result;
    }

    public Result googleCancle(String accessToken, String emailCode) {
        Result result = new Result();
        result.setCode(Result.Code.SUCCESS);
        try {
            validateAccessToken(accessToken, result, yangMemberTokenService);
            if (result.getCode().intValue() == Result.Code.ERROR.intValue()) {
                return result;
            }
            YangMember yangMemberSelf = (YangMember) result.getData();
            String key = CoinConst.EMAILFORFINDCODE + yangMemberSelf.getEmail() + "_" + CoinConst.EMAIL_CODE_TYPE_CANCLEGOOGLE;
            if (redisService.exists(key)) {
                EmailLog emailLog = JSONObject.parseObject(redisService.get(key).toString(), EmailLog.class);
                if (emailCode.equals(emailLog.getContent())) {
                    Example example = new Example(YangGoogleauth.class);
                    example.createCriteria().andEqualTo("memberId", yangMemberSelf.getMemberId());
                    List<YangGoogleauth> list = yangGoogleauthMapper.selectByExample(example);
                    if (list == null || list.size() != 1) {
                        result.setCode(Result.Code.ERROR);
                        result.setMsg("未查询到谷歌验证信息");
                        result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
                        return result;
                    }
                    //删除谷歌验证信息
                    yangGoogleauthMapper.deleteByPrimaryKey(list.get(0));
                    result.setCode(Result.Code.SUCCESS);
                } else {
                    result.setCode(Result.Code.ERROR);
                    result.setMsg("验证码错误,请重新请求");
                    result.setErrorCode(ErrorCode.ERROR_ERROR_CODE.getIndex());
                }
            } else {
                result.setCode(Result.Code.ERROR);
                result.setMsg("验证码不存在或已过期");
                result.setErrorCode(ErrorCode.ERROR_ERROR_CODE.getIndex());
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.setErrorCode(ErrorCode.SEND_EMAILCODE_ERROR.getIndex());
            result.setCode(Result.Code.ERROR);
            result.setMsg(ErrorCode.SEND_EMAILCODE_ERROR.getMessage());
        }
        return result;
    }

    public Result getGoogle(String accessToken) {
        Result result = new Result();
        result.setCode(Result.Code.SUCCESS);

        try {
            //现获取用户信息
            validateAccessToken(accessToken, result, yangMemberTokenService);
            if (result.getCode().intValue() == Result.Code.ERROR.intValue()) {
                return result;
            }
            YangMember yangMemberSelf = (YangMember) result.getData();
            Example example = new Example(YangGoogleauth.class);
            example.createCriteria().andEqualTo("memberId", yangMemberSelf.getMemberId());
            List<YangGoogleauth> list = yangGoogleauthMapper.selectByExample(example);
            if (list != null && list.size() == 1) {
                result.setCode(1);
                result.setData(list.get(0));
                result.setMsg("获取谷歌信息成功");
            } else {
                result.setData(new HashMap());
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.setErrorCode(ErrorCode.SEND_EMAILCODE_ERROR.getIndex());
            result.setCode(Result.Code.ERROR);
            result.setMsg(ErrorCode.SEND_EMAILCODE_ERROR.getMessage());
        }
        return result;
    }


    public Result googleBind(String accessToken, String secret, String loginLock, String orderLock, String moneyLock,
                             String emailCode) {
        Result result = new Result();
        result.setCode(Result.Code.SUCCESS);
        try {
            //现获取用户信息
            validateAccessToken(accessToken, result, yangMemberTokenService);
            if (result.getCode().intValue() == Result.Code.ERROR.intValue()) {
                return result;
            }
            YangMember yangMemberSelf = (YangMember) result.getData();
            String key = CoinConst.EMAILFORFINDCODE + yangMemberSelf.getEmail() + "_" + CoinConst.EMAIL_CODE_TYPE_BINDGOOGLE;
            if (redisService.exists(key)) {
                EmailLog emailLog = JSONObject.parseObject(redisService.get(key).toString(), EmailLog.class);
                if (emailCode.equals(emailLog.getContent())) {
                    //绑定谷歌验证信息
                    YangGoogleauth yangGoogleauth = new YangGoogleauth();
                    yangGoogleauth.setMemberId(yangMemberSelf.getMemberId());
                    yangGoogleauth.setLoginLock(Integer.valueOf(loginLock));
                    yangGoogleauth.setSecret(secret);
                    yangGoogleauth.setOrderLock(Integer.valueOf(orderLock));
                    yangGoogleauth.setMoneyLock(Integer.valueOf(moneyLock));
                    yangGoogleauthMapper.insertSelective(yangGoogleauth);
                    result.setCode(Result.Code.SUCCESS);
                } else {
                    result.setCode(Result.Code.ERROR);
                    result.setMsg("验证码错误,请重新请求");
                    result.setErrorCode(ErrorCode.ERROR_ERROR_CODE.getIndex());
                }
            } else {
                result.setCode(Result.Code.ERROR);
                result.setMsg("验证码不存在或已过期");
                result.setErrorCode(ErrorCode.ERROR_ERROR_CODE.getIndex());
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
     * 修改谷歌的信息
     *
     * @param accessToken 用户的token
     * @param loginLock   是否绑定登录
     * @param orderLock   是否绑定接受
     * @param moneyLock   是否绑定付款
     * @param account     手机号或者是邮箱
     * @param code        手机号或者是邮箱的验证码
     * @return
     */
    public Result updateGoogle(String accessToken, String loginLock, String orderLock, String moneyLock,
                               String account, String code) {
        Result result = new Result();
        result.setCode(Result.Code.SUCCESS);
        String token = CoinConst.TOKENKEYTOKEN + accessToken;
        Object memberId = redisService.get(token);
        try {
            //现获取用户信息
            validateAccessToken(accessToken, result, yangMemberTokenService);
            if (result.getCode().intValue() == Result.Code.ERROR.intValue()) {
                return result;
            }
            YangMember yangMemberSelf = (YangMember) result.getData();
            //判断是邮箱验证码还是手机验证码
            if (account.contains("@")) {
                String key =
                        CoinConst.EMAILFORFINDCODE + account + "_" + CoinConst.EMAIL_CODE_TYPE_ZC;
                if (redisService.exists(key)) {
                    EmailLog emailLog = JSONObject.parseObject(redisService.get(key).toString(), EmailLog.class);
                    if (emailLog.getContent().equals(code)) {
                        Example example = new Example(YangGoogleauth.class);
                        example.createCriteria().andEqualTo("memberId", yangMemberSelf.getMemberId());
                        List<YangGoogleauth> list = yangGoogleauthMapper.selectByExample(example);
                        if (list == null || list.size() != 1) {
                            result.setCode(Result.Code.ERROR);
                            result.setMsg("未查询到谷歌验证信息");
                            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
                            return result;
                        }
                        //绑定谷歌验证信息
                        YangGoogleauth yangGoogleauth = list.get(0);
                        yangGoogleauth.setMemberId(yangMemberSelf.getMemberId());
                        yangGoogleauth.setLoginLock(Integer.valueOf(loginLock));
                        yangGoogleauth.setOrderLock(Integer.valueOf(orderLock));
                        yangGoogleauth.setMoneyLock(Integer.valueOf(moneyLock));
                        yangGoogleauthMapper.updateGoogle(yangGoogleauth);
                        result.setCode(Result.Code.SUCCESS);
                        result.setMsg("修改成功");
                    } else {
                        result.setCode(Result.Code.ERROR);
                        result.setMsg("验证码错误,请重新请求");
                        result.setErrorCode(ErrorCode.ERROR_ERROR_CODE.getIndex());
                    }
                } else {
                    result.setCode(Result.Code.ERROR);
                    result.setMsg("验证码不存在或已过期");
                    result.setErrorCode(ErrorCode.ERROR_ERROR_CODE.getIndex());
                }

            } else {
                String key = CoinConst.EMAILFORFINDCODE + account + "_" + "1";
                if (redisService.exists(key)) {
                    String numStr = (String) redisService.get(key);
                    if (code != null && code.equals(numStr)) {
                        Example example = new Example(YangGoogleauth.class);
                        example.createCriteria().andEqualTo("memberId", yangMemberSelf.getMemberId());
                        List<YangGoogleauth> list = yangGoogleauthMapper.selectByExample(example);
                        if (list == null || list.size() != 1) {
                            result.setCode(Result.Code.ERROR);
                            result.setMsg("未查询到谷歌验证信息");
                            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
                            return result;
                        }
                        //绑定谷歌验证信息
                        YangGoogleauth yangGoogleauth = list.get(0);
                        yangGoogleauth.setMemberId(yangMemberSelf.getMemberId());
                        yangGoogleauth.setLoginLock(Integer.valueOf(loginLock));
                        yangGoogleauth.setOrderLock(Integer.valueOf(orderLock));
                        yangGoogleauth.setMoneyLock(Integer.valueOf(moneyLock));
                        yangGoogleauthMapper.updateGoogle(yangGoogleauth);
                        result.setCode(Result.Code.SUCCESS);
                    } else {
                        result.setCode(Result.Code.ERROR);
                        result.setMsg("验证码错误,请重新请求");
                        result.setErrorCode(ErrorCode.ERROR_ERROR_CODE.getIndex());
                    }
                } else {
                    result.setCode(Result.Code.ERROR);
                    result.setMsg("验证码不存在或已过期");
                    result.setErrorCode(ErrorCode.ERROR_ERROR_CODE.getIndex());
                }

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
     * 解绑谷歌验证
     *
     * @param accessToken
     * @param account
     * @param code
     * @return
     */
    public Result cancleGoogle(String accessToken, String account, String code) {
        Result result = new Result();
        result.setCode(Result.Code.SUCCESS);
        String token = CoinConst.TOKENKEYTOKEN + accessToken;
        Object memberId = redisService.get(token);
        try {
            validateAccessToken(accessToken, result, yangMemberTokenService);
            if (result.getCode().intValue() == Result.Code.ERROR.intValue()) {
                return result;
            }
            YangMember yangMemberSelf = (YangMember) result.getData();
            //取到用户信息
            //判断是邮箱验证码还是手机验证码
            if (account.contains("@")) {
                String key = CoinConst.EMAILFORFINDCODE + account + "_" + "1";
                if (redisService.exists(key)) {
                    EmailLog emailLog = JSONObject.parseObject(redisService.get(key).toString(), EmailLog.class);
                    if (code.equals(emailLog.getContent())) {
                        Example example = new Example(YangGoogleauth.class);
                        example.createCriteria().andEqualTo("memberId", yangMemberSelf.getMemberId());
                        List<YangGoogleauth> list = yangGoogleauthMapper.selectByExample(example);
                        if (list == null || list.size() != 1) {
                            result.setCode(Result.Code.ERROR);
                            result.setMsg("未查询到谷歌验证信息");
                            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
                            return result;
                        }
                        //删除谷歌验证信息
                        yangGoogleauthMapper.cancleGoogle((Integer) memberId);
                        result.setCode(Result.Code.SUCCESS);
                        result.setMsg("解绑成功");
                    } else {
                        result.setCode(Result.Code.ERROR);
                        result.setMsg("验证码错误,请重新请求");
                        result.setErrorCode(ErrorCode.ERROR_ERROR_CODE.getIndex());
                    }
                } else {
                    result.setCode(Result.Code.ERROR);
                    result.setMsg("验证码不存在或已过期");
                    result.setErrorCode(ErrorCode.ERROR_ERROR_CODE.getIndex());
                }
            } else {
                String key = CoinConst.EMAILFORFINDCODE + account + "_" + "1";
                if (redisService.exists(key)) {
                    String numStr = (String) redisService.get(key);
                    if (code != null && code.equals(numStr)) {
                        Example example = new Example(YangGoogleauth.class);
                        example.createCriteria().andEqualTo("memberId", yangMemberSelf.getMemberId());
                        List<YangGoogleauth> list = yangGoogleauthMapper.selectByExample(example);
                        if (list == null || list.size() != 1) {
                            result.setCode(Result.Code.ERROR);
                            result.setMsg("未查询到谷歌验证信息");
                            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
                            return result;
                        }
                        //删除谷歌验证信息
                        yangGoogleauthMapper.cancleGoogle((Integer) memberId);
                        result.setCode(Result.Code.SUCCESS);
                    } else {
                        result.setCode(Result.Code.ERROR);
                        result.setMsg("验证码错误,请重新请求");
                        result.setErrorCode(ErrorCode.ERROR_ERROR_CODE.getIndex());
                    }
                } else {
                    result.setCode(Result.Code.ERROR);
                    result.setMsg("验证码不存在或已过期");
                    result.setErrorCode(ErrorCode.ERROR_ERROR_CODE.getIndex());
                }
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
     * 椰椰
     * time：2019/9/16
     * 根据邮箱获取谷歌验证秘钥
     *
     * @param loginUserName
     * @return
     */
    public String getGoogleByEmail(String loginUserName) {
        Map<String, String> map = yangMemberService.getMemberByEmail(loginUserName);
        if (map != null) {
            Example example = new Example(YangMember.class);
            example.createCriteria().andEqualTo("memberId", map.get("memberId"));
            List<YangGoogleauth> yangGoogleauths = yangGoogleauthMapper.selectByExample(example);
            if (yangGoogleauths != null && yangGoogleauths.size() == 1) {
                YangGoogleauth yangGoogleauth = yangGoogleauths.get(0);
                String secret = yangGoogleauth.getSecret();
                return secret;
            }
        }
        return "";
    }

    /**
     * 椰椰
     * time：2019/9/16
     * 根据手机号获取谷歌秘钥
     *
     * @param loginUserName
     * @return
     */
    public String getGoogleByPhone(String loginUserName) {
        Map<String, String> map = yangMemberService.getMemberByPhone(loginUserName);
        if (map != null) {
            Example example = new Example(YangMember.class);
            example.createCriteria().andEqualTo("memberId", map.get("memberId"));
            List<YangGoogleauth> yangGoogleauths = yangGoogleauthMapper.selectByExample(example);
            if (yangGoogleauths != null && yangGoogleauths.size() == 1) {
                YangGoogleauth yangGoogleauth = yangGoogleauths.get(0);
                String secret = yangGoogleauth.getSecret();
                return secret;
            }
        }
        return "";
    }

    public Result loginNeedGoogle(String loginUser) {
        Result result = new Result();
        Example example = new Example(YangMember.class);
        Example.Criteria criteria = example.createCriteria();
        if (loginUser.contains("@")) {
            //邮箱
            criteria.andEqualTo("email", loginUser);
        } else {
            criteria.andEqualTo("phone", loginUser);
        }
        List<YangMember> yangMembers = yangMemberService.selectByExample(example);
        if (yangMembers != null && yangMembers.size() > 0) {
            YangMember yangMember = yangMembers.get(0);
            Map<String, String> map = yangGoogleauthMapper.getGoogleauthByStatus(yangMember.getMemberId());
            if (map != null) {
                result.setMsg("需要");
                result.setCode(1);
                return result;
            } else {
                result.setCode(0);
                result.setMsg("不需要");
                return result;
            }
        }
        result.setMsg("此用户不存在");
        result.setCode(0);
        return result;
    }

    /**
     * 查找用户的谷歌id
     *
     * @param accessToken 用戶的id
     * @return
     */
    public Result getGoogles(String accessToken) {
        YangGoogleauth yangGoogleauth = new YangGoogleauth();
        //验证token
        Result result = new Result();
        String token = CoinConst.TOKENKEYTOKEN + accessToken;
        //验证token
        Object memberId = redisService.get(token);
        if (memberId == null) {
            result.setCode(Result.Code.SUCCESS);
            result.setErrorCode(ErrorCode.ERROR_ERROR_TOKEN_EXPIRE.getIndex());
            result.setMsg(ErrorCode.ERROR_ERROR_TOKEN_EXPIRE.getMessage());
            return result;
        }
        try {
            yangGoogleauth = yangGoogleauthMapper.getGoogle((Integer) memberId);
            result.setCode(Result.Code.SUCCESS);
            result.setData(yangGoogleauth);
            result.setMsg("获取谷歌信息成功");
        } catch (Exception e) {
            e.printStackTrace();
            result.setCode(Result.Code.ERROR);
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
            result.setMsg(ErrorCode.ERROR_SYSTEM.getMessage());
        }
        return result;
    }

    /**
     * 查找谷歌二维码和秘钥
     *
     * @param accessToken
     * @return
     */
    public Result findGoogleSecretAndQR(String accessToken) {
        YangGoogleauth yang = new YangGoogleauth();
        //验证token
        Result result = new Result();
        String token = accessToken;
        //验证token
        Object memberId = redisService.get(token);
        if (memberId == null) {
            result.setCode(Result.Code.SUCCESS);
            result.setErrorCode(ErrorCode.ERROR_ERROR_TOKEN_EXPIRE.getIndex());
            result.setMsg(ErrorCode.ERROR_ERROR_TOKEN_EXPIRE.getMessage());
            return result;
        }
        try {
            yang = yangGoogleauthMapper.findGoogleSecretAndQR((Integer) memberId);
            if (yang != null) {
                result.setData(yang);
                result.setMsg("查找成功");
                result.setCode(1);
                return result;
            } else if (yang == null) {
                yangGoogleauthMapper.addGoogleSecretAndQR(yang);
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.setCode(Result.Code.ERROR);
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
            result.setMsg(ErrorCode.ERROR_SYSTEM.getMessage());
        }
        return result;
    }

    /**
     * 生成二维码
     *
     * @param accessToken
     * @return
     */
    public Result addGoogleSecretAndQR(String accessToken) {
        Result result = new Result();
        YangGoogleauth yangGoogleauth = new YangGoogleauth();
        String token = CoinConst.TOKENKEYTOKEN + accessToken;
        Integer memberId = (Integer) redisService.get(token);
        YangMember yangMember = memberMapper.getUserInfo((Integer) memberId);
        String email = yangMember.getEmail();

        // 如果MEMBERID存在，直接返回结果
        YangGoogleauth yangOld = new YangGoogleauth();
        yangOld = yangGoogleauthMapper.findGoogleSecretAndQR((Integer) memberId);
        if (yangOld != null) {
            result.setCode(1);
            result.setMsg("生成用户谷歌秘钥和二维码路径成功");
            result.setData(yangOld);
            return result;
        }

        if (memberId == null) {
            result.setCode(Result.Code.SUCCESS);
            result.setErrorCode(ErrorCode.ERROR_ERROR_TOKEN_EXPIRE.getIndex());
            result.setMsg(ErrorCode.ERROR_ERROR_TOKEN_EXPIRE.getMessage());
            return result;
        }
        try {
            String secretKey = GoogleAuthenticator.genSecret(email, "BM");
            String qr = GoogleAuthenticator.getQRBarcodeURL(email, "BM", secretKey);
            String generateSecretKey = GoogleAuthenticator.generateSecretKey();
            YangGoogleauth googleauth = new YangGoogleauth();
            googleauth.setSecret(generateSecretKey);
            googleauth.setQRBarcodeURL(qr);
            googleauth.setMemberId(memberId);
            googleauth.setOrderLock(1);
            googleauth.setMoneyLock(1);
            googleauth.setLoginLock(1);
            Integer info = yangGoogleauthMapper.addGoogleSecretAndQR(googleauth);
            if (!googleauth.getMemberId().equals(memberId)) {
                if (findGoogleSecretAndQR(accessToken) == null) {
                    if (info != 0) {
                        result.setCode(1);
                        result.setMsg("生成用户谷歌秘钥和二维码路径成功");
                        result.setData(findGoogleSecretAndQR(token));
                    } else {
                        result.setCode(0);
                        result.setMsg("生成用户谷歌秘钥和二维码路径失败");
                    }
                } else {
                    result = findGoogleSecretAndQR(token);
                    return result;
                }
            } else {
                result = findGoogleSecretAndQR(token);
                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.setCode(Result.Code.ERROR);
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
            result.setMsg(ErrorCode.ERROR_SYSTEM.getMessage());
        }
        return result;
    }

    /**
     * 找到用户的谷歌秘钥
     *
     * @param account
     * @return
     */
    public YangGoogleauth findSecrets(String account) {
        YangGoogleauth yang = new YangGoogleauth();
        YangMember yangMember = new YangMember();
        Result result = new Result();
        if (account.contains("@")) {
            yangMember = yangMemberService.getMemberIdByEmail(account);
            if (yang == null) {
                result.setCode(Result.Code.SUCCESS);
                result.setErrorCode(ErrorCode.ERROR_ERROR_TOKEN_EXPIRE.getIndex());
                result.setMsg(ErrorCode.ERROR_ERROR_TOKEN_EXPIRE.getMessage());
                return yang;
            }
            try {
                yang = yangGoogleauthMapper.findSecret(yangMember.getMemberId());
                if (yang != null) {
                    return yang;
                } else {
                    return null;
                }
            } catch (Exception e) {
                e.printStackTrace();
                result.setCode(Result.Code.ERROR);
                result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
                result.setMsg(ErrorCode.ERROR_SYSTEM.getMessage());
            }
        } else {
            yangMember = yangMemberService.getMemberIdByPhone(account);
            if (yang == null) {
                result.setCode(Result.Code.SUCCESS);
                result.setErrorCode(ErrorCode.ERROR_ERROR_TOKEN_EXPIRE.getIndex());
                result.setMsg(ErrorCode.ERROR_ERROR_TOKEN_EXPIRE.getMessage());
                return yang;
            }
            try {
                yang = yangGoogleauthMapper.findSecret(yangMember.getMemberId());
                if (yang != null) {
                    return yang;
                } else {
                    return null;
                }
            } catch (Exception e) {
                e.printStackTrace();
                result.setCode(Result.Code.ERROR);
                result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
                result.setMsg(ErrorCode.ERROR_SYSTEM.getMessage());
            }
        }
        return yang;
    }
}
