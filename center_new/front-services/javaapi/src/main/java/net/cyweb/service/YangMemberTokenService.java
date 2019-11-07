package net.cyweb.service;

import com.alibaba.druid.support.json.JSONUtils;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.netflix.discovery.converters.Auto;
import cyweb.utils.CoinConst;
import cyweb.utils.CoinConstUser;
import cyweb.utils.ErrorCode;
import cyweb.utils.OkHttpUtil;
import net.cyweb.exception.*;
import net.cyweb.mapper.*;
import net.cyweb.model.*;
import net.cyweb.model.modelExt.PageList;
import net.cyweb.model.modelExt.YangCurrencyExt;
import net.cyweb.model.modelExt.YangTibiExt;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import org.springframework.web.client.RestTemplate;
import tk.mybatis.mapper.entity.Example;

import java.math.BigDecimal;
import java.net.SocketTimeoutException;
import java.util.Currency;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@EnableAsync
public class YangMemberTokenService extends BaseService<YangMemberToken> {

    @Autowired
    private YangMemberMapper yangMemberMapper;

    @Autowired
    private YangGoogleauthMapper yangGoogleauthMapper;

    @Autowired
    private YangCurrencyUserMapper yangCurrencyUserMapper;

    @Autowired
    private YangCurrencyMapper yangCurrencyMapper;

    @Autowired
    private YangTibiMapper yangTibiMapper;

    @Autowired
    private   RedisService redisService;


    @Autowired
    private YangMemberService yangMemberService;

    public enum incDecType {
        inc("inc"),dec("dec");
        private String type;
        incDecType(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }
    };


    /*根据token获取memberID*/
    public YangMemberToken getYangMemberToken(YangMemberToken yangMemberToken) {

        //先获取token
        Integer memberId;
        if(redisService.keysExist(CoinConst.TOKENKEYTOKEN+yangMemberToken.getAccessToken()))
        {
             memberId = (Integer)redisService.get(CoinConst.TOKENKEYTOKEN+yangMemberToken.getAccessToken());
            yangMemberToken.setMemberId(memberId);

            //后去token副本 用来确认token是否正确
            if(redisService.keysExist(CoinConst.TOKENKEYMEMBER+memberId.intValue()))
            {
                String token = (String)redisService.get(CoinConst.TOKENKEYMEMBER+memberId.intValue());
                if(!token.equals(yangMemberToken.getAccessToken()))
                {
                    throw new ErrorTokenException(ErrorCode.ERROR_ERROR_TOKEN.getMessage());
                }
            }else{
                throw new ErrorTokenException(ErrorCode.ERROR_ERROR_TOKEN.getMessage());
            }

        }
        else  if(redisService.keysExist(CoinConst.TOKENKEYTOKEN+CoinConst.LOGIN_TYP_PHONE+"_"+yangMemberToken.getAccessToken())) {
            memberId = (Integer)redisService.get(CoinConst.TOKENKEYTOKEN+CoinConst.LOGIN_TYP_PHONE+"_"+yangMemberToken.getAccessToken());
            yangMemberToken.setMemberId(memberId);

            if(redisService.keysExist(CoinConst.TOKENKEYMEMBER+CoinConst.LOGIN_TYP_PHONE+"_"+memberId.intValue()))
            {
                String token = (String)redisService.get(CoinConst.TOKENKEYMEMBER+CoinConst.LOGIN_TYP_PHONE+"_"+memberId.intValue());
                if(!token.equals(yangMemberToken.getAccessToken()))
                {
                    throw new ErrorTokenException(ErrorCode.ERROR_ERROR_TOKEN.getMessage());
                }
            }else{
                throw new ErrorTokenException(ErrorCode.ERROR_ERROR_TOKEN.getMessage());
            }

        }
        else{
            throw new ErrorTokenException(ErrorCode.ERROR_ERROR_TOKEN.getMessage());
        }
        return  yangMemberToken;


    }

    /*根据memberId获取yangmember*/
    public YangMember getYangMember(YangMemberToken yangMemberTokenFind){
        //根据memberId获取用户信息
        YangMember yangMemberQuery = new YangMember();
        yangMemberQuery.setMemberId(yangMemberTokenFind.getMemberId());
        YangMember yangMember = yangMemberMapper.selectOne(yangMemberQuery);
        if(null == yangMember){
            throw new EmailNotExistException(ErrorCode.ERROR_EMAIL_NOT_EXIST.getMessage());
        }

        //刷新过期时间
        redisService.expire(CoinConst.TOKENKEYMEMBER+yangMember.getMemberId().intValue(), CoinConstUser.LOGIN_TIME_OUT,TimeUnit.SECONDS);
        redisService.expire(CoinConst.TOKENKEYTOKEN+yangMemberTokenFind.getAccessToken(), CoinConstUser.LOGIN_TIME_OUT,TimeUnit.SECONDS);

        return yangMember;
    }

    /*获取会员详细信息*/
    public YangMember findMember(YangMemberToken yangMemberToken) {
        YangMemberToken yangMemberTokenFind;
        YangMember yangMember;
        try {
             yangMemberTokenFind = getYangMemberToken(yangMemberToken);
             yangMember = getYangMember(yangMemberTokenFind);
            yangMember.setPwd(null);
            yangMember.setPwdtrade(null);
        }catch (Exception e)
        {
            //这里清楚掉这个token下的全部信息

//            e.printStackTrace();
            return null;
        }

        return yangMember;
    }

    /*检查交易密码是否正确*/
    public Boolean checkPwdTrade(YangMemberToken yangMemberToken, YangMember yangMember) {
        YangMemberToken yangMemberTokenFind;
        YangMember yangMemberFind;
        try {
             yangMemberTokenFind = getYangMemberToken(yangMemberToken);
             yangMemberFind = getYangMember(yangMemberTokenFind);
            /*密码验证成功*/
        }catch (Exception e)
        {
            return false;
        }
        if(DigestUtils.md5Hex(yangMember.getPwdtrade()).equals(yangMemberFind.getPwdtrade())){
            return true;
        }else{ //验证失败
            return false;
        }
    }

    /*检查交易密码是否已经设置*/
    public Result hasTradePasswd(YangMemberToken yangMemberToken) throws  Exception {

        Result result = new Result();
        result.setCode(Result.Code.ERROR);

        YangMemberToken yangMemberTokenFind = getYangMemberToken(yangMemberToken);
        YangMember yangMemberFind = getYangMember(yangMemberTokenFind);
        if(yangMemberFind == null)
        {
            result.setCode(Result.Code.ERROR);
            result.setErrorCode(ErrorCode.ERROR_ERROR_TOKEN.getIndex());
            return result;

        }
        if(StringUtils.isNotEmpty(yangMemberFind.getPwdtrade()))
        {
            result.setCode(Result.Code.SUCCESS);
        }else{
            result.setCode(Result.Code.ERROR);
        }
        return result;

    }

    /**
     *
     * @param yangMemberToken
     * @param pass 用户登录密码
     * @param treadPass 用户需要设置的交易密码
     * @throws Exception
     */
    public Result setTreadPasswd(YangMemberToken yangMemberToken,String pass,String treadPass) throws Exception {

        Result result = new Result();
        result.setCode(Result.Code.SUCCESS);

        YangMemberToken yangMemberTokenFind = getYangMemberToken(yangMemberToken);
        YangMember yangMemberFind = getYangMember(yangMemberTokenFind);

        if(yangMemberFind == null)
        {
            result.setCode(Result.Code.ERROR);
            result.setErrorCode(ErrorCode.ERROR_ERROR_TOKEN.getIndex());
            return result;

        }

//        if(!yangMemberFind.getPwd().equals(DigestUtils.md5Hex(pass)))
//        {
//            result.setCode(Result.Code.ERROR);
//            result.setErrorCode(ErrorCode.ERROR_PWD_CHECK_ERROR.getIndex());
//            return result;
//        }

        if(this.hasTradePasswd(yangMemberToken).getCode().equals(Result.Code.SUCCESS))
        {
            result.setCode(Result.Code.ERROR);
            result.setErrorCode(ErrorCode.ERROR_PWD_TRADE_HASEXISTS.getIndex());
            return result;
        }


        String tradePasswd = DigestUtils.md5Hex(treadPass);
        yangMemberFind.setPwdtrade(tradePasswd);
        yangMemberMapper.updateByPrimaryKeySelective(yangMemberFind);

        return result;


    }


    /**
     * 设置交易密码
     * @param yangMemberToken
     * @param pass
     * @param treadPassold
     * @param treadPassnew
     * @return
     * @throws Exception
     */
    public Result changeTreadPasswd(YangMemberToken yangMemberToken,String pass,String treadPassold,String treadPassnew) throws Exception {

        Result result = new Result();
        result.setCode(Result.Code.SUCCESS);

        YangMemberToken yangMemberTokenFind = getYangMemberToken(yangMemberToken);
        YangMember yangMemberFind = getYangMember(yangMemberTokenFind);

        if(yangMemberFind == null)
        {
            result.setCode(Result.Code.ERROR);
            result.setErrorCode(ErrorCode.ERROR_ERROR_TOKEN.getIndex());
            return result;

        }

//        if(!yangMemberFind.getPwd().equals(DigestUtils.md5Hex(pass)))
//        {
//            result.setCode(Result.Code.ERROR);
//            result.setErrorCode(ErrorCode.ERROR_PWD_CHECK_ERROR.getIndex());
//            return result;
//        }

        if(!yangMemberFind.getPwdtrade().equals(DigestUtils.md5Hex(treadPassold)))
        {
            result.setCode(Result.Code.ERROR);
            result.setErrorCode(ErrorCode.ERROR_PWD_TRADE_FAILED.getIndex());
            return result;
        }



        String tradePasswd = DigestUtils.md5Hex(treadPassnew);
        yangMemberFind.setPwdtrade(tradePasswd);

        yangMemberMapper.updateByPrimaryKeySelective(yangMemberFind);

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
    public Result primaryCertification(YangMemberToken yangMemberToken,int cardType,String idCard,String name) throws Exception
    {
        Result result = new Result();
        result.setCode(Result.Code.SUCCESS);
        YangMemberToken yangMemberTokenFind = getYangMemberToken(yangMemberToken);
        YangMember yangMemberFind = getYangMember(yangMemberTokenFind);

        if(yangMemberFind == null)
        {
            result.setCode(Result.Code.ERROR);
            result.setErrorCode(ErrorCode.ERROR_ERROR_TOKEN.getIndex());
            return result;

        }

        //加上身份证
        Example example = new Example(YangMember.class);
        example.createCriteria().andEqualTo("idcard",idCard);
        List<YangMember> list = yangMemberService.selectByExample(example);
        if(list.size() > 0)
        {
            result.setCode(Result.Code.ERROR);
            result.setErrorCode(ErrorCode.REPEATING_ID_CARD.getIndex());
            return result;
        }


        yangMemberFind.setCardtype(String.valueOf(cardType));
        yangMemberFind.setIdcard(idCard);
        yangMemberFind.setName(name);
        yangMemberMapper.updateByPrimaryKeySelective(yangMemberFind);

        return result;

    }


    /**
     * 设置用户头像
     * @param yangMemberToken
     * @param pic
     * @return
     * @throws Exception
     */
    public Result updateUserPic(YangMemberToken yangMemberToken,String pic) throws Exception
    {
        Result result = new Result();
        result.setCode(Result.Code.SUCCESS);
        YangMemberToken yangMemberTokenFind = getYangMemberToken(yangMemberToken);
        YangMember yangMemberFind = getYangMember(yangMemberTokenFind);

        if(yangMemberFind == null)
        {
            result.setCode(Result.Code.ERROR);
            result.setErrorCode(ErrorCode.ERROR_ERROR_TOKEN.getIndex());
            return result;

        }

        yangMemberFind.setHead(pic);
        yangMemberMapper.updateByPrimaryKeySelective(yangMemberFind);

        return result;

    }





    /*更新用户密码*/
    public void changePwd(YangMemberToken yangMemberToken, YangMember yangMember) throws Exception {

            YangMemberToken yangMemberTokenFind = getYangMemberToken(yangMemberToken);
            YangMember yangMemberFind = getYangMember(yangMemberTokenFind);

            /*检查旧密码是否正确*/
            if(!yangMemberFind.getPwd().equals(DigestUtils.md5Hex(yangMember.getOldPwd()))){
                throw new OldPwdException(ErrorCode.ERROR_OLD_PWD_ERROR.getMessage());
            }

            //更新密码
            YangMember yangMemberUpdate = new YangMember();
            yangMemberUpdate.setMemberId(yangMemberFind.getMemberId());
            yangMemberUpdate.setPwd(DigestUtils.md5Hex(yangMember.getNewPwd()));

            yangMemberMapper.updateByPrimaryKeySelective(yangMemberUpdate);

    }

    /*更新身份证图片地址*/
    public void certificate(YangMemberToken yangMemberToken, YangMember yangMember) throws Exception {
        YangMemberToken yangMemberTokenFind = getYangMemberToken(yangMemberToken);
        YangMember yangMemberFind = getYangMember(yangMemberTokenFind);

        if(yangMemberFind == null)
        {
            throw new Exception("找不到对象");
        }

        YangMember yangMemberUpdate = new YangMember();
        yangMemberUpdate.setMemberId(yangMemberFind.getMemberId());
        yangMemberUpdate.setPic1(yangMember.getPic1());
        yangMemberUpdate.setPic2(yangMember.getPic2());
        yangMemberUpdate.setPic3(yangMember.getPic3());
        yangMemberUpdate.setShenhestatus(Integer.valueOf(3));

        yangMemberUpdate.setSafeTime(String.valueOf(System.currentTimeMillis()/1000));

        int res = yangMemberMapper.updateByPrimaryKeySelective(yangMemberUpdate);


    }

    /*登陆是否需要google*/
    public Boolean loginNeedGoogle(YangMember yangMember) {
        try {
            YangMember yangMemberQuery = new YangMember();

            Example example = new Example(YangMember.class);
            example.createCriteria().andEqualTo("email",yangMember.getEmail());

            List <YangMember> list = yangMemberMapper.selectByExample(example);
            if(list.size() > 0)
            {
                YangGoogleauth yangGoogleauth = new YangGoogleauth();
                yangGoogleauth.setMemberId(list.get(0).getMemberId());
                yangGoogleauth.setLoginLock(1);
                if(null == yangGoogleauth)
                {
                    return false;
                }
                return yangGoogleauthMapper.selectCount(yangGoogleauth) > 0;

            }else{
                return  false;
            }

//            yangMemberQuery.setEmail(yangMember.getEmail());
//
//            YangMember yangMemberFind = yangMemberMapper.selectOne(yangMemberQuery);


        }catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }


    }


    /*用户指定资产*/
    public YangMember  getSpecAssets(YangMemberToken yangMemberToken,YangCurrencyUser yangCurrencyUser) {
        YangMember yangMemberReturn;
        try {
            YangMemberToken yangMemberTokenFind = getYangMemberToken(yangMemberToken);
            YangMember yangMemberFind = getYangMember(yangMemberTokenFind);

            yangMemberReturn = new YangMember();

            if(yangCurrencyUser.getCurrencyId().intValue() == 0) {
                yangMemberReturn.setForzenRmb(yangMemberFind.getForzenRmb() == null ? BigDecimal.ZERO : yangMemberFind.getForzenRmb());
                yangMemberReturn.setRmb(yangMemberFind.getRmb() == null ? BigDecimal.ZERO : yangMemberFind.getRmb());
            }else{
                //循环currency_userwww
                YangCurrencyUser yangCurrencyUserQuery = new YangCurrencyUser();
                yangCurrencyUserQuery.setMemberId(yangMemberFind.getMemberId());
                yangCurrencyUserQuery.setCurrencyId(yangCurrencyUser.getCurrencyId());

                List<YangCurrencyUser> yangCurrencyUserList = yangCurrencyUserMapper.select(yangCurrencyUserQuery);

                List<YangCurrencyUser> yangCurrencyUserListReturn = Lists.newArrayList();

                for(YangCurrencyUser yangCurrencyUser1 : yangCurrencyUserList){
                    YangCurrencyUser yangCurrencyUser2 = new YangCurrencyUser();
                    yangCurrencyUser2.setNum(yangCurrencyUser1.getNum());
                    yangCurrencyUser2.setForzenNum(yangCurrencyUser1.getForzenNum());
                    yangCurrencyUser2.setCurrencyId(yangCurrencyUser1.getCurrencyId());
                    yangCurrencyUser2.setMemberId(yangCurrencyUser1.getMemberId());

                    yangCurrencyUserListReturn.add(yangCurrencyUser2);
                }

                yangMemberReturn.setYangCurrencyUserList(yangCurrencyUserListReturn);
            }
        }catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }

        return yangMemberReturn;

    }


    public YangMember  getSpecAssetsForMarektMake(YangMember yangMemberFind,YangCurrencyUser yangCurrencyUser) {
        YangMember yangMemberReturn;
        try {
            yangMemberReturn = new YangMember();

            if(yangCurrencyUser.getCurrencyId().intValue() == 0) {
                yangMemberReturn.setForzenRmb(yangMemberFind.getForzenRmb() == null ? BigDecimal.ZERO : yangMemberFind.getForzenRmb());
                yangMemberReturn.setRmb(yangMemberFind.getRmb() == null ? BigDecimal.ZERO : yangMemberFind.getRmb());
            }else{
                //循环currency_userwww
                YangCurrencyUser yangCurrencyUserQuery = new YangCurrencyUser();
                yangCurrencyUserQuery.setMemberId(yangMemberFind.getMemberId());
                yangCurrencyUserQuery.setCurrencyId(yangCurrencyUser.getCurrencyId());

                List<YangCurrencyUser> yangCurrencyUserList = yangCurrencyUserMapper.select(yangCurrencyUserQuery);

                List<YangCurrencyUser> yangCurrencyUserListReturn = Lists.newArrayList();

                for(YangCurrencyUser yangCurrencyUser1 : yangCurrencyUserList){
                    YangCurrencyUser yangCurrencyUser2 = new YangCurrencyUser();
                    yangCurrencyUser2.setNum(yangCurrencyUser1.getNum());
                    yangCurrencyUser2.setForzenNum(yangCurrencyUser1.getForzenNum());
                    yangCurrencyUser2.setCurrencyId(yangCurrencyUser1.getCurrencyId());
                    yangCurrencyUser2.setMemberId(yangCurrencyUser1.getMemberId());

                    yangCurrencyUserListReturn.add(yangCurrencyUser2);
                }

                yangMemberReturn.setYangCurrencyUserList(yangCurrencyUserListReturn);
            }
        }catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }

        return yangMemberReturn;

    }

    /*用户全部资产*/
    public YangMember getAllAssets(YangMemberToken yangMemberToken, YangCurrencyExt yangCurrencyExt) throws  Exception {

        YangMemberToken yangMemberTokenFind = getYangMemberToken(yangMemberToken);
        YangMember yangMemberFind = getYangMember(yangMemberTokenFind);

        yangCurrencyExt.setMemberId(yangMemberFind.getMemberId().toString());
        List<YangCurrencyExt> list =  yangCurrencyMapper.getCurrencyandUserAssets(yangCurrencyExt);

        //这里获取全部交易对  并且连表查询和用户的余额

        YangMember yangMemberReturn = new YangMember();
        yangMemberReturn.setForzenRmb(yangMemberFind.getForzenRmb());
        yangMemberReturn.setRmb(yangMemberFind.getRmb());

        yangMemberReturn.setYangCurrencyExtsList(list);
        return yangMemberReturn;

    }

    /*更新用户资产*/
    @Transactional
    public Result updateAssets(YangMemberToken yangMemberToken, YangCurrencyUser yangCurrencyUser,YangMember yangMember) throws RuntimeException {
        Result result = new Result();
        result.setCode(Result.Code.SUCCESS);

        YangMemberToken yangMemberTokenFind = yangMemberToken;

        if(yangMemberToken.getMemberId() == null || yangMemberToken.getMemberId().equals(0))
        {
            yangMemberTokenFind = getYangMemberToken(yangMemberToken);
        }

        String lock = yangMemberTokenFind.getMemberId()+"";
//        yangMemberService.assets(memberId,String.valueOf(currencyId.intValue()),num,type,BigDecimal.ZERO,type);

        synchronized (lock.intern())
        {
            YangMember yangMemberFind = getYangMember(yangMemberTokenFind);

            BigDecimal zero = new BigDecimal(0);

            if(yangCurrencyUser.getCurrencyId() != 0)
            {
                YangCurrencyUser yangCurrencyUserQuery = new YangCurrencyUser();
                yangCurrencyUserQuery.setMemberId(yangMemberFind.getMemberId());
                yangCurrencyUserQuery.setCurrencyId(yangCurrencyUser.getCurrencyId());
                YangCurrencyUser yangCurrencyUserFind = yangCurrencyUserMapper.selectOne(yangCurrencyUserQuery);

                if(null == yangCurrencyUserFind){
//                throw new CurrencyUserNotFoundException(ErrorCode.ERROR_CURRENCY_USER_NOT_FOUND.getMessage());
                    //如果找不到这个用户的这个资产，要给用户增加一条记录
                    yangCurrencyUserQuery.setForzenNum(BigDecimal.ZERO);
                    yangCurrencyUserQuery.setNum(BigDecimal.ZERO);
                    int insertNums = yangCurrencyUserMapper.insert(yangCurrencyUserQuery);
                    if(insertNums<=0)
                    {
                        throw new CurrencyUserNotFoundException(ErrorCode.ERROR_CURRENCY_USER_NOT_FOUND.getMessage());
                    }else{
                        // TODO: 2018/6/18 验证一下新增之后的id是否有过来
                        yangCurrencyUserFind  = yangCurrencyUserQuery; //新增之后的数据反向替代
                    }

                }
            }
            int i = yangMemberService.assets(
                    yangMemberTokenFind.getMemberId(),String.valueOf(yangCurrencyUser.getCurrencyId().intValue()),yangCurrencyUser.getNum(),yangCurrencyUser.getNumType(),yangCurrencyUser.getForzenNum(),yangCurrencyUser.getForzenNumType(),
                    0,null,null,null,null,null,
                    0,null,null,null,null,null,
                    0,null,null,null,null,null,
                    0,null,null,null,null,null

                   );
            if(i == 1)
            {

                result.setCode(Result.Code.ERROR);
                result.setMsg("调用assets储存过程出现异常");
                throw new RuntimeException();
            }

            return result;
        }

    }

    /*更新用户资产*/
    @Transactional
    public Result updateAssetsForMarkerMake(YangCurrencyUser yangCurrencyUser,YangMember yangMemberFind) throws RuntimeException {
        Result result = new Result();
        result.setCode(Result.Code.SUCCESS);


        String lock = yangMemberFind.getMemberId()+"";
//        yangMemberService.assets(memberId,String.valueOf(currencyId.intValue()),num,type,BigDecimal.ZERO,type);

        synchronized (lock.intern())
        {
            BigDecimal zero = new BigDecimal(0);

            if(yangCurrencyUser.getCurrencyId() != 0)
            {
                YangCurrencyUser yangCurrencyUserQuery = new YangCurrencyUser();
                yangCurrencyUserQuery.setMemberId(yangMemberFind.getMemberId());
                yangCurrencyUserQuery.setCurrencyId(yangCurrencyUser.getCurrencyId());
                YangCurrencyUser yangCurrencyUserFind = yangCurrencyUserMapper.selectOne(yangCurrencyUserQuery);

                if(null == yangCurrencyUserFind){
//                throw new CurrencyUserNotFoundException(ErrorCode.ERROR_CURRENCY_USER_NOT_FOUND.getMessage());
                    //如果找不到这个用户的这个资产，要给用户增加一条记录
                    yangCurrencyUserQuery.setForzenNum(BigDecimal.ZERO);
                    yangCurrencyUserQuery.setNum(BigDecimal.ZERO);
                    int insertNums = yangCurrencyUserMapper.insert(yangCurrencyUserQuery);
                    if(insertNums<=0)
                    {
                        throw new CurrencyUserNotFoundException(ErrorCode.ERROR_CURRENCY_USER_NOT_FOUND.getMessage());
                    }else{
                        // TODO: 2018/6/18 验证一下新增之后的id是否有过来
                        yangCurrencyUserFind  = yangCurrencyUserQuery; //新增之后的数据反向替代
                    }

                }
            }
            int i = yangMemberService.assets(
                    yangMemberFind.getMemberId(),String.valueOf(yangCurrencyUser.getCurrencyId().intValue()),yangCurrencyUser.getNum(),yangCurrencyUser.getNumType(),yangCurrencyUser.getForzenNum(),yangCurrencyUser.getForzenNumType(),
                    0,null,null,null,null,null,
                    0,null,null,null,null,null,
                    0,null,null,null,null,null,
                    0,null,null,null,null,null

            );
            if(i == 1)
            {

                result.setCode(Result.Code.ERROR);
                result.setMsg("调用assets储存过程出现异常");
                throw new RuntimeException();
            }

            return result;
        }

    }



    /*获取用户钱包地址*/
    public YangCurrencyUser getAddress(YangMemberToken yangMemberToken, YangCurrencyUser yangCurrencyUser) throws SocketTimeoutException {
        YangMemberToken yangMemberTokenFind = getYangMemberToken(yangMemberToken);
        YangMember yangMemberFind = getYangMember(yangMemberTokenFind);

        //获取币种信息
        YangCurrency yangCurrencyQuery = new YangCurrency();
        yangCurrencyQuery.setCurrencyId(yangCurrencyUser.getCurrencyId());
        YangCurrency yangCurrencyFind = yangCurrencyMapper.selectOne(yangCurrencyQuery);

        if(null == yangCurrencyFind){
            throw new CurrencyNotFoundException(ErrorCode.ERROR_CURRENCY_NOT_FOUND.getMessage());
        }

        //拼接币种的请求地址
        String addressUrl = "http://"+yangCurrencyFind.getRpcUrl()+":"+yangCurrencyFind.getPortNumber()+"/coin/creatUser";

        MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<>();
        requestEntity.add("currency_id", String.valueOf(yangCurrencyFind.getCurrencyId()));
        requestEntity.add("memberId", String.valueOf(yangMemberFind.getMemberId()));
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(3000);
        requestFactory.setReadTimeout(3000);
        RestTemplate restTemplate = new RestTemplate(requestFactory);
        String result = restTemplate.postForObject(addressUrl, requestEntity, String.class);
        LinkedHashMap jsonObject = (LinkedHashMap) JSONUtils.parse(result);
        YangCurrencyUser yangCurrencyUserReturn = new YangCurrencyUser();
        if(null != jsonObject && jsonObject.get("code") != null && Integer.valueOf(String.valueOf(jsonObject.get("code"))) == 1 && null != jsonObject.get("data") && StringUtils.isNotEmpty(String.valueOf(jsonObject.get("data")))){
            yangCurrencyUserReturn.setChongzhiUrl(String.valueOf(jsonObject.get("data")));
        }

        return yangCurrencyUserReturn;
    }


    public Boolean validateAddress(YangMemberToken yangMemberToken, YangCurrencyUser yangCurrencyUser) {
        YangMemberToken yangMemberTokenFind = getYangMemberToken(yangMemberToken);
        YangMember yangMemberFind = getYangMember(yangMemberTokenFind);

        //获取币种信息
        YangCurrency yangCurrencyQuery = new YangCurrency();
        yangCurrencyQuery.setCurrencyId(yangCurrencyUser.getCurrencyId());
        YangCurrency yangCurrencyFind = yangCurrencyMapper.selectOne(yangCurrencyQuery);

        if(null == yangCurrencyFind){
            throw new CurrencyNotFoundException(ErrorCode.ERROR_CURRENCY_NOT_FOUND.getMessage());
        }

        //拼接币种的请求地址
        String addressUrl = "http://"+yangCurrencyFind.getRpcUrl()+":"+yangCurrencyFind.getPortNumber()+"/coin/validateAddress";

        MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<>();
        requestEntity.add("currency_id", String.valueOf(yangCurrencyFind.getCurrencyId()));
        requestEntity.add("memberId", String.valueOf(yangMemberFind.getMemberId()));
        requestEntity.add("address", String.valueOf(yangCurrencyUser.getChongzhiUrl()));

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(3000);
        requestFactory.setReadTimeout(3000);
        RestTemplate restTemplate = new RestTemplate(requestFactory);
        String result = restTemplate.postForObject(addressUrl, requestEntity, String.class);
        LinkedHashMap jsonObject = (LinkedHashMap) JSONUtils.parse(result);
        if(null != jsonObject && jsonObject.get("code") != null && Integer.valueOf(String.valueOf(jsonObject.get("code"))) == 1 ){
            return true;
        }else{
            return false;
        }

    }


    public Result retrieveByTxId(String tx_id,Integer currencyId) {

        Result result = new Result();
        result.setCode(Result.Code.ERROR);
        try {
            //获取币种信息
            YangCurrency yangCurrencyQuery = new YangCurrency();
            yangCurrencyQuery.setCurrencyId(currencyId);
            YangCurrency yangCurrencyFind = yangCurrencyMapper.selectOne(yangCurrencyQuery);

            if(null == yangCurrencyFind){
                throw new CurrencyNotFoundException(ErrorCode.ERROR_CURRENCY_NOT_FOUND.getMessage());
            }

            //拼接币种的请求地址
            String addressUrl = "http://"+yangCurrencyFind.getRpcUrl()+":"+yangCurrencyFind.getPortNumber()+"/coin/retrieveByTxId";

            HashMap<String, String> requestEntity = new HashMap<String,String>();
            requestEntity.put("currency_id", String.valueOf(yangCurrencyFind.getCurrencyId()));
            requestEntity.put("tx_id",tx_id);

            String s =  OkHttpUtil.post(addressUrl,requestEntity);


            Result result1 = JSONObject.parseObject(s,Result.class);

            System.out.println(result1.getCode());
            System.out.println(result1.getMsg());

            result.setCode(Result.Code.SUCCESS);
            result.setMsg(s);

        }catch (Exception e)
        {
            result.setMsg(e.getMessage());
            result.setCode(Result.Code.ERROR);

        }
        return result;


    }


    /*检测是否有昵称*/
    public boolean checkHasNick(YangMemberToken yangMemberToken) {
        YangMemberToken yangMemberTokenFind = getYangMemberToken(yangMemberToken);
        YangMember yangMemberFind = getYangMember(yangMemberTokenFind);
        return StringUtils.isNotBlank(yangMemberFind.getNick());
    }

    /*修改昵称*/
    public void changeNick(YangMemberToken yangMemberToken, YangMember yangMember) {
        YangMemberToken yangMemberTokenFind = getYangMemberToken(yangMemberToken);
        YangMember yangMemberFind = getYangMember(yangMemberTokenFind);

        YangMember yangMemberUpdate = new YangMember();
        yangMemberUpdate.setMemberId(yangMemberFind.getMemberId());
        yangMemberUpdate.setNick(yangMember.getNick());

        yangMemberMapper.updateByPrimaryKeySelective(yangMemberUpdate);
    }

//
//    /*查询充币记录*/
//    public PageList<YangTibi> chargeRecord(YangMemberToken yangMemberToken, YangCurrencyUser yangCurrencyUser) {
//        YangMemberToken yangMemberTokenFind = getYangMemberToken(yangMemberToken);
//        YangMember yangMemberFind = getYangMember(yangMemberTokenFind);
//
//        YangTibi yangTibiQuery = new YangTibi();
//        yangTibiQuery.setCurrencyId(yangCurrencyUser.getCurrencyId());
//        yangTibiQuery.setUserId(yangMemberFind.getMemberId());
//        PageHelper.startPage(yangMemberToken.getPageNum(),yangMemberToken.getPageSize());
//
//        Example example = new Example(YangTibi.class);
//        List<String> status = Lists.newArrayList();
//        status.add("2");
//        status.add("3");
//        example.createCriteria().andIn("status",status);
//        List<YangTibi> list = yangTibiMapper.selectByExample(example);
//
//        PageList<YangTibi> pageList = new PageList<>();
//        pageList.setTotalNum(yangTibiMapper.selectCountByExample(example));
//        pageList.setLists(list);
//
//        return pageList;
//    }


    /*查询充币记录*/
    public PageInfo<YangTibiExt> chargeRecord(YangMemberToken yangMemberToken, YangCurrencyUser yangCurrencyUser) {
        YangMemberToken yangMemberTokenFind = getYangMemberToken(yangMemberToken);
        YangMember yangMemberFind = getYangMember(yangMemberTokenFind);

        PageHelper.startPage(yangMemberToken.getPageNum(),yangMemberToken.getPageSize());

//        YangTibi yangTibiQuery = new YangTibi();
//        yangTibiQuery.setCurrencyId(yangCurrencyUser.getCurrencyId());
//        yangTibiQuery.setUserId(yangMemberFind.getMemberId());
//        PageHelper.startPage(yangMemberToken.getPageNum(),yangMemberToken.getPageSize());


//        Example example = new Example(YangTibi.class);
//        example.createCriteria().
//                andEqualTo("currencyId",yangCurrencyUser.getCurrencyId())
//                .andEqualTo("userId",yangMemberFind.getMemberId())
//                ;
//        example.orderBy("id desc");
//        List<YangTibi> list = yangTibiMapper.selectByExample(example);

        YangTibiExt yangTibiExt = new YangTibiExt();
        yangTibiExt.setUserId(yangMemberFind.getMemberId());
        yangTibiExt.setCurrencyId(yangCurrencyUser.getCurrencyId());


        List<YangTibiExt> list = yangTibiMapper.selectTibi(yangTibiExt);




        PageInfo<YangTibiExt> pageInfo = new PageInfo<YangTibiExt>(list);

        System.out.println(pageInfo);

//        PageList<YangTibi> pageList = new PageList<>();
//        pageList.setTotalNum(yangTibiMapper.selectCountByExample(example));
//        pageList.setLists(list);

        return pageInfo;
    }




}

