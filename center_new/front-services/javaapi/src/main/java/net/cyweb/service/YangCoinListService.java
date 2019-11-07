package net.cyweb.service;

import com.sun.org.apache.regexp.internal.RE;
import cyweb.utils.DateUtils;
import cyweb.utils.ErrorCode;
import net.cyweb.mapper.YangCoinListMapper;
import net.cyweb.model.Result;
import net.cyweb.model.YangCoinList;
import net.cyweb.model.YangMember;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class YangCoinListService extends BaseService<YangCoinList>{

    @Autowired
    private YangMemberTokenService yangMemberTokenService;

    @Autowired
    private YangCoinListMapper yangCoinListMapper;

    public Result applyCoin(YangCoinList yangCoinList, String accessToken){
        Result result=new Result();
        try{
            validateAccessToken(accessToken,result,yangMemberTokenService);
            if(result.getCode().intValue()==Result.Code.ERROR.intValue()){
                return result;
            }
            YangMember yangMember=(YangMember)result.getData();
            yangCoinList.setAddTime(DateUtils.getNowTimes());
            yangCoinList.setMemberId(yangMember.getMemberId());
            yangCoinListMapper.insertSelective(yangCoinList);
            result.setCode(Result.Code.SUCCESS);
        }catch (Exception e){
            e.printStackTrace();
            result.setCode(Result.Code.ERROR);
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
            result.setMsg(ErrorCode.ERROR_SYSTEM.getMessage());
        }
        return result;
    }
}
