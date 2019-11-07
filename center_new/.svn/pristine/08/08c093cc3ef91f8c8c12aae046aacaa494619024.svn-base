package net.cyweb.service;

import com.alibaba.druid.support.json.JSONUtils;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.netflix.discovery.converters.Auto;
import cyweb.utils.ErrorCode;
import net.cyweb.exception.*;
import net.cyweb.mapper.*;
import net.cyweb.model.*;
import net.cyweb.model.modelExt.PageList;
import net.cyweb.model.modelExt.YangCurrencyExt;
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
    public YangMemberToken getYangMemberToken(YangMemberToken yangMemberToken){
        YangMemberToken yangMemberTokenFind = selectOne(yangMemberToken);
        if(null == yangMemberTokenFind || null == yangMemberTokenFind.getMemberId()){
            throw new ErrorTokenException(ErrorCode.ERROR_ERROR_TOKEN.getMessage());
        }
        /*判断是否过期*/
        if(yangMemberTokenFind.getExpire() + yangMemberTokenFind.getAddTime() < System.currentTimeMillis()/1000){
            throw new TokenExpirException(ErrorCode.ERROR_ERROR_TOKEN_EXPIRE.getMessage());
        }
        return yangMemberTokenFind;
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
        return yangMember;
    }

    /*获取会员详细信息*/
    public YangMember findMember(YangMemberToken yangMemberToken) {
        YangMemberToken yangMemberTokenFind = getYangMemberToken(yangMemberToken);
        YangMember yangMember = getYangMember(yangMemberTokenFind);
        yangMember.setPwd(null);
        yangMember.setPwdtrade(null);
        return yangMember;
    }

    /*检查交易密码是否正确*/
    public Boolean checkPwdTrade(YangMemberToken yangMemberToken, YangMember yangMember) {
        YangMemberToken yangMemberTokenFind = getYangMemberToken(yangMemberToken);
        YangMember yangMemberFind = getYangMember(yangMemberTokenFind);
        /*密码验证成功*/
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

        if(!yangMemberFind.getPwd().equals(DigestUtils.md5Hex(pass)))
        {
            result.setCode(Result.Code.ERROR);
            result.setErrorCode(ErrorCode.ERROR_PWD_CHECK_ERROR.getIndex());
            return result;
        }

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

        if(!yangMemberFind.getPwd().equals(DigestUtils.md5Hex(pass)))
        {
            result.setCode(Result.Code.ERROR);
            result.setErrorCode(ErrorCode.ERROR_PWD_CHECK_ERROR.getIndex());
            return result;
        }

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
    public void changePwd(YangMemberToken yangMemberToken, YangMember yangMember) {
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
        YangMember yangMemberQuery = new YangMember();
        yangMemberQuery.setEmail(yangMember.getEmail());

        YangMember yangMemberFind = yangMemberMapper.selectOne(yangMemberQuery);

        YangGoogleauth yangGoogleauth = new YangGoogleauth();
        yangGoogleauth.setMemberId(yangMemberFind.getMemberId());
        return yangGoogleauthMapper.selectCount(yangGoogleauth) > 0;
    }


    /*用户指定资产*/
    public YangMember getSpecAssets(YangMemberToken yangMemberToken,YangCurrencyUser yangCurrencyUser) {
        YangMemberToken yangMemberTokenFind = getYangMemberToken(yangMemberToken);
        YangMember yangMemberFind = getYangMember(yangMemberTokenFind);

        YangMember yangMemberReturn = new YangMember();

        if(yangCurrencyUser.getCurrencyId() == 0) {
            yangMemberReturn.setForzenRmb(yangMemberFind.getForzenRmb() == null ? BigDecimal.ZERO : yangMemberFind.getForzenRmb());
            yangMemberReturn.setRmb(yangMemberFind.getRmb() == null ? BigDecimal.ZERO : yangMemberFind.getRmb());
        }else{
            //循环currency_user
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
//    @Async
    public Result updateAssets(YangMemberToken yangMemberToken, YangCurrencyUser yangCurrencyUser,YangMember yangMember) {
        Result result = new Result();
        result.setCode(Result.Code.SUCCESS);

        YangMemberToken yangMemberTokenFind = yangMemberToken;

        if(yangMemberToken.getMemberId() == null || yangMemberToken.getMemberId().equals(0))
        {
            yangMemberTokenFind = getYangMemberToken(yangMemberToken);
        }

        String lock = yangMemberTokenFind.getMemberId()+"";
//        yangMemberService.assets(memberId,String.valueOf(currencyId.intValue()),num,type,BigDecimal.ZERO,type);


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
                throw new  RuntimeException();
            }

            return result;


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
    public PageInfo<YangTibi> chargeRecord(YangMemberToken yangMemberToken, YangCurrencyUser yangCurrencyUser) {
        YangMemberToken yangMemberTokenFind = getYangMemberToken(yangMemberToken);
        YangMember yangMemberFind = getYangMember(yangMemberTokenFind);

        PageHelper.startPage(yangMemberToken.getPageNum(),yangMemberToken.getPageSize());

        YangTibi yangTibiQuery = new YangTibi();
        yangTibiQuery.setCurrencyId(yangCurrencyUser.getCurrencyId());
        yangTibiQuery.setUserId(yangMemberFind.getMemberId());
        PageHelper.startPage(yangMemberToken.getPageNum(),yangMemberToken.getPageSize());




        List<YangTibi> list = yangTibiMapper.selectTibi(yangTibiQuery);




        PageInfo<YangTibi> pageInfo = new PageInfo<YangTibi>(list);

        System.out.println(pageInfo);

//        PageList<YangTibi> pageList = new PageList<>();
//        pageList.setTotalNum(yangTibiMapper.selectCountByExample(example));
//        pageList.setLists(list);

        return pageInfo;
    }




}

