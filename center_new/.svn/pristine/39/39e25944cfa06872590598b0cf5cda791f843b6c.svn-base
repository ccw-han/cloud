package net.cyweb.service;

import cyweb.utils.CoinConst;
import cyweb.utils.DateUtils;
import cyweb.utils.ErrorCode;
import net.cyweb.mapper.YangQianbaoAddressMapper;
import net.cyweb.model.Result;
import net.cyweb.model.YangMember;
import net.cyweb.model.YangQianBaoAddress;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

@Service
public class YangQianBaoAddressService extends BaseService<YangQianBaoAddress>{

    @Autowired
    private YangQianbaoAddressMapper yangQianbaoAddressMapper;

    @Autowired
    private YangMemberTokenService yangMemberTokenService;

    public Result getQianbaoAddress(String accessToken, String currencyId,String id){
        Result result=new Result();

        try{
            validateAccessToken(accessToken,result,yangMemberTokenService);
            if(result.getCode().intValue()==Result.Code.ERROR.intValue()){
                return result;
            }
            YangMember yangMember=(YangMember)result.getData();

            Example example=new Example(YangQianBaoAddress.class);
            Example.Criteria criteria=example.createCriteria();
            criteria.andEqualTo("status",CoinConst.QIANBAO_ADDRESS_NORMAL);
            criteria.andEqualTo("userId",yangMember.getMemberId());
            if(StringUtils.isNotEmpty(id)){
            criteria.andEqualTo("id",id);
            }
            if(StringUtils.isNotEmpty(currencyId)){
                criteria.andEqualTo("currencyId",currencyId);
            }
            List<YangQianBaoAddress> list=yangQianbaoAddressMapper.selectByExample(example);
            if(list==null||list.size()==0){
                result.setData(new ArrayList<YangQianBaoAddress>());
            }else{
                result.setData(list);
            }
        }catch (Exception e){
            e.printStackTrace();
            result.setCode(Result.Code.ERROR);
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
            result.setMsg(ErrorCode.ERROR_SYSTEM.getMessage());
        }
        return result;
    }

    public Result addQianbaoAddress(String accessToken,String currencyId,String name,String qianbaoUrl){
        Result result=new Result();
        try{
            validateAccessToken(accessToken,result,yangMemberTokenService);
            if(result.getCode().intValue()==Result.Code.ERROR.intValue()){
                return result;
            }
            YangMember yangMember=(YangMember)result.getData();

            YangQianBaoAddress yangQianBaoAddress=new YangQianBaoAddress();
            yangQianBaoAddress.setAddTime(DateUtils.getNowTimes());
            yangQianBaoAddress.setCurrencyId(Integer.valueOf(currencyId));
            yangQianBaoAddress.setName(name);
            yangQianBaoAddress.setQianbaoUrl(qianbaoUrl);
            yangQianBaoAddress.setStatus(CoinConst.QIANBAO_ADDRESS_NORMAL);
            yangQianBaoAddress.setUserId(yangMember.getMemberId());
            yangQianbaoAddressMapper.insertSelective(yangQianBaoAddress);
            result.setData(yangQianBaoAddress);
        }catch (Exception e){
            e.printStackTrace();
            result.setCode(Result.Code.ERROR);
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
            result.setMsg(ErrorCode.ERROR_SYSTEM.getMessage());
        }
        return result;
    }

    public Result delQianbaoAddress(String accessToken,String id){
        Result result=new Result();
        try{
            validateAccessToken(accessToken,result,yangMemberTokenService);
            if(result.getCode().intValue()==Result.Code.ERROR.intValue()){
                return result;
            }
            YangMember yangMember=(YangMember)result.getData();

            Example example=new Example(YangQianBaoAddress.class);
            Example.Criteria criteria=example.createCriteria();
            criteria.andEqualTo("status",CoinConst.QIANBAO_ADDRESS_NORMAL);
            criteria.andEqualTo("userId",yangMember.getMemberId());
            criteria.andEqualTo("id",id);
            List<YangQianBaoAddress> list=yangQianbaoAddressMapper.selectByExample(example);
            if(list==null||list.size()==0){
                result.setCode(Result.Code.ERROR);
                result.setErrorCode(ErrorCode.ERROR_QIANBAO_ADDRESS_NO_FOUND.getIndex());
                result.setMsg(ErrorCode.ERROR_QIANBAO_ADDRESS_NO_FOUND.getMessage());
                return result;
            }

            YangQianBaoAddress yangQianBaoAddress=list.get(0);
            yangQianBaoAddress.setStatus(CoinConst.QIANBAO_ADDRESS_DEL);
            yangQianbaoAddressMapper.updateByPrimaryKeySelective(yangQianBaoAddress);
        }catch (Exception e){
            e.printStackTrace();
            result.setCode(Result.Code.ERROR);
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
            result.setMsg(ErrorCode.ERROR_SYSTEM.getMessage());
        }
        return result;
    }

}
