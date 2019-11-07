package net.cyweb.service;

import com.github.pagehelper.PageInfo;
import cyweb.utils.CoinConst;
import cyweb.utils.DateUtils;
import cyweb.utils.ErrorCode;
import net.cyweb.mapper.YangCurrencyFindMapper;
import net.cyweb.mapper.YangCurrencyMapper;
import net.cyweb.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.math.BigDecimal;
import java.util.List;

@Service
public class YangCurrencyFindService extends BaseService<YangCurrencyFind>{

    @Autowired
    private YangCurrencyFindMapper yangCurrencyFindMapper;

    @Autowired
    private YangMemberTokenService yangMemberTokenService;

    @Autowired
    private YangCurrencyMapper yangCurrencyMapper;

    public Result currencyFindAdd(String accessToken,String num,String address,String currencyId){
        Result result=new Result();
        try{
            validateAccessToken(accessToken,result,yangMemberTokenService);
            if(result.getCode().intValue()==Result.Code.ERROR.intValue()){
                return result;
            }
            YangMember yangMember=(YangMember)result.getData();
            Example example=new Example(YangCurrency.class);
            example.createCriteria().andEqualTo("currencyId",currencyId);
            List<YangCurrency> list=yangCurrencyMapper.selectByExample(example);
            if(list==null||list.size()==0){
                result.setCode(Result.Code.ERROR);
                result.setErrorCode(ErrorCode.ERROR_CURRENCY_NO_FOUND.getIndex());
                result.setMsg(ErrorCode.ERROR_CURRENCY_NO_FOUND.getMessage());
                return result;
            }
            YangCurrencyFind yangCurrencyFind=new YangCurrencyFind();
            yangCurrencyFind.setAddress(address);
            yangCurrencyFind.setAddTime(DateUtils.getNowTimes());
            yangCurrencyFind.setCurrencyId(Integer.valueOf(currencyId));
            yangCurrencyFind.setCurrencyMark(list.get(0).getCurrencyMark());
            yangCurrencyFind.setEmail(yangMember.getEmail());
            yangCurrencyFind.setMemberId(yangMember.getMemberId());
            yangCurrencyFind.setNum(new BigDecimal(num));
            yangCurrencyFind.setRemark("");
            yangCurrencyFind.setStatus(CoinConst.CURRNCY_FIND_STATUS_NORMAL);
            yangCurrencyFindMapper.insertSelective(yangCurrencyFind);
        }catch (Exception e){
            e.printStackTrace();
            result.setCode(Result.Code.ERROR);
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
            result.setMsg(ErrorCode.ERROR_SYSTEM.getMessage());
        }

        return result;
    }

    public Result currencyFindList(String accessToken,int page,int pageSize){
        Result result=new Result();
        try{
            validateAccessToken(accessToken,result,yangMemberTokenService);
            if(result.getCode().intValue()==Result.Code.ERROR.intValue()){
                return result;
            }
            YangMember yangMember=(YangMember)result.getData();

            Example example=new Example(YangCurrencyFind.class);
            example.createCriteria().andEqualTo("memberId",yangMember.getMemberId());
            int total= yangCurrencyFindMapper.selectCountByExample(example);//总条数
            example.setOrderByClause("add_time desc limit "+(page-1)*pageSize+","+pageSize);
            List<YangCurrencyFind> list=yangCurrencyFindMapper.selectByExample(example);
            PageInfo<YangCurrencyFind> pageInfo = new PageInfo<YangCurrencyFind>(list);
            pageInfo.setTotal(Long.valueOf(total));
            result.setCode(Result.Code.SUCCESS);
            result.setData(pageInfo);
        }catch (Exception e){
            e.printStackTrace();
            result.setCode(Result.Code.ERROR);
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
            result.setMsg(ErrorCode.ERROR_SYSTEM.getMessage());
        }
        return result;
    }

}
