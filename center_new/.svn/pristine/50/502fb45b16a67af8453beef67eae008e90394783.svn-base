package net.cyweb.service;

import cyweb.utils.ErrorCode;
import net.cyweb.mapper.YangTradeFeeMapper;
import net.cyweb.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

@Service
public class YangTradeFeeService extends BaseService<YangTradeFee>{

    @Autowired
    private YangMemberTokenService yangMemberTokenService;

    @Autowired
    private YangTradeFeeMapper yangTradeFeeMapper;

    public Result getTradeFee(String accessToken,String cyId){
        Result result=new Result();
        try{
            result.setCode(Result.Code.SUCCESS);
            //验证用户信息
            YangMemberToken yangMemberToken=new YangMemberToken();
            yangMemberToken.setAccessToken(accessToken);
            YangMember yangMemberSelf = yangMemberTokenService.findMember(yangMemberToken);
            if(yangMemberSelf==null){
                result.setCode(Result.Code.ERROR);
                result.setErrorCode(ErrorCode.ERROR_ERROR_TOKEN.getIndex());
                result.setMsg(ErrorCode.ERROR_ERROR_TOKEN.getMessage());
                return result;
            }
            Example example=new Example(YangTradeFee.class);
            example.createCriteria().andEqualTo("cyId",cyId).andEqualTo("memberId",yangMemberSelf.getMemberId());
            List<YangTradeFee> list= yangTradeFeeMapper.selectByExample(example);
            if(list==null||list.size()==0){
                result.setData(new HashMap());
            }else{
                result.setData(list.get(0));
            }
        }catch (Exception e){
            e.printStackTrace();
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex() );
            result.setCode(Result.Code.ERROR);
            result.setMsg(ErrorCode.ERROR_SYSTEM.getMessage());
        }
        return result;
    }
}
