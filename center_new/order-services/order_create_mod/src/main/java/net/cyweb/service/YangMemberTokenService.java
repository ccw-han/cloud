package net.cyweb.service;

import com.alibaba.druid.support.json.JSONUtils;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.netflix.discovery.converters.Auto;
import cyweb.utils.CoinConst;
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
import java.util.*;

@Service
@EnableAsync
public class YangMemberTokenService extends BaseService<YangMemberToken> {

    @Autowired
    private YangMemberMapper yangMemberMapper;

    @Autowired
    private YangCurrencyUserMapper yangCurrencyUserMapper;

    @Autowired
    private YangMemberService yangMemberService;


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





    /*更新用户资产*/
    @Transactional
//    @Async
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
                System.out.println("用户资产变动异常 : id"+ yangMemberTokenFind.getMemberId()+" 资产"+yangCurrencyUser.getCurrencyId().intValue()+": "+yangCurrencyUser.getNumType()+" :"+yangCurrencyUser.getNum());
                System.out.println("用户资产变动异常 : id"+ yangMemberTokenFind.getMemberId()+" 冻结资产"+yangCurrencyUser.getCurrencyId().intValue()+": "+yangCurrencyUser.getForzenNumType()+" :"+yangCurrencyUser.getForzenNum());
                throw new  RuntimeException("用户资产变动异常");
            }

            return result;
    }

}

