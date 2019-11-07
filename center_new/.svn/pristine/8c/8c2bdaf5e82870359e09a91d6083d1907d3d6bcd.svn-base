package net.cyweb.service;

import cyweb.utils.CoinConst;
import cyweb.utils.ErrorCode;
import net.cyweb.mapper.YangAreasMapper;
import net.cyweb.mapper.YangBankMapper;
import net.cyweb.model.Result;
import net.cyweb.model.YangArea;
import net.cyweb.model.YangBank;
import net.cyweb.model.YangMember;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.util.StringUtil;

import javax.xml.ws.Action;
import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class YangBankService extends BaseService<YangBank>{

    @Autowired
    private YangBankMapper yangBankMapper;

    @Autowired
    private YangAreasMapper yangAreasMapper;

    @Autowired
    private RedisService redisService;

    @Autowired
    private YangMemberTokenService yangMemberTokenService;

    public Result getBankList(String accessToken,String id){
        Result result=new Result();
        try{
            validateAccessToken(accessToken,result,yangMemberTokenService);
            if(result.getCode().intValue()==Result.Code.ERROR.intValue()){
                return result;
            }
            YangMember yangMember=(YangMember)result.getData();
            Example example=new Example(YangBank.class);
            Example.Criteria criteria=example.createCriteria();
            if(StringUtils.isNotEmpty(id)){
                criteria.andEqualTo("id",id);
            }
            criteria.andEqualTo("uid",yangMember.getMemberId()).andEqualTo("status",CoinConst.BANK_STATUS_AVA);
            List<YangBank> list=yangBankMapper.selectByExample(example);

            if(list!=null&list.size()>0){
                for(YangBank yangBank:list){
                    String areadId=yangBank.getAddress();
                    Example areaExample=new Example(YangArea.class);
                    areaExample.createCriteria().andEqualTo("areaId",areadId);
                    List<YangArea> areaList=yangAreasMapper.selectByExample(areaExample);
                    if(areaList==null||areaList.size()==0){
                        yangBank.setCity("");
                        yangBank.setProvince("");
                    }else{
                        yangBank.setCity(areaList.get(0).getAreaName());
                        Example area2Example=new Example(YangArea.class);
                        area2Example.createCriteria().andEqualTo("areaId",areaList.get(0).getParentId());
                        List<YangArea> area2List=yangAreasMapper.selectByExample(area2Example);
                        if(area2List==null||area2List.size()==0){
                            yangBank.setProvince("");
                        }else{
                            yangBank.setProvince(area2List.get(0).getAreaName());
                        }

                    }
                }
            }
            result.setData((list==null||list.size()==0)?new ArrayList<YangBank>():list);
        }catch (Exception e){
            e.printStackTrace();
            result.setCode(Result.Code.ERROR);
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
            result.setMsg(ErrorCode.ERROR_SYSTEM.getMessage());
        }
        return result;
    }

    public Result addBank(YangBank yangBank,String accessToken){
        Result result=new Result();
        try{
            validateAccessToken(accessToken,result,yangMemberTokenService);
            if(result.getCode().intValue()==Result.Code.ERROR.intValue()){
                return result;
            }
            YangMember yangMember=(YangMember)result.getData();
            yangBank.setStatus(CoinConst.BANK_STATUS_AVA);
            yangBank.setUid(yangMember.getMemberId());
            yangBankMapper.insertSelective(yangBank);
            yangBank.getId();
            result.setData(yangBank);
        }catch (Exception e){
            e.printStackTrace();
            result.setCode(Result.Code.ERROR);
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
            result.setMsg(ErrorCode.ERROR_SYSTEM.getMessage());
        }
        return result;
    }

    public Result delBank(String accessToken,String id){
        Result result=new Result();
        try{
            validateAccessToken(accessToken,result,yangMemberTokenService);
            if(result.getCode().intValue()==Result.Code.ERROR.intValue()){
                return result;
            }
            YangMember yangMember=(YangMember)result.getData();
            Example example=new Example(YangBank.class);
            example.createCriteria().andEqualTo("id",id).andEqualTo("uid",yangMember.getMemberId());
            //查询当前用户银行卡信息是否存在  1 存在 修改状态  2 不存在 报错返回
            List<YangBank> list=yangBankMapper.selectByExample(example);
            if(list==null||list.size()==0){
                result.setCode(Result.Code.ERROR);
                result.setErrorCode(ErrorCode.ERROR_BANK_NOT_FOOUD.getIndex());
                result.setMsg(ErrorCode.ERROR_BANK_NOT_FOOUD.getMessage());
                return result;
            }else{
                YangBank yangBank=new YangBank();
                yangBank.setStatus(CoinConst.BANK_STATUS_DIS);
                yangBank.setId(Integer.valueOf(id));
                yangBankMapper.updateByPrimaryKeySelective(yangBank);
            }
        }catch (Exception e){
            e.printStackTrace();
            result.setCode(Result.Code.ERROR);
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
            result.setMsg(ErrorCode.ERROR_SYSTEM.getMessage());
        }
        return result;
    }

    /**
     * 添加收款信息
     * 耶耶
     * time：2019/9/27
     * @param map
     * @return
     */
    public Result saveBankInfoByUser(Map<String, String> map) {
        Result result = new Result();
        result.setCode(0);
        result.setMsg("添加失败");
        String accessToken = map.get("accessToken");
        String bankname = map.get("bankname");
        String cardname = map.get("cardname");
        String address = map.get("address");
        String cardnum = map.get("cardnum");
        String bankBranch = map.get("bankBranch");
        String type = map.get("type");
        YangBank yangBank = new YangBank();
        try{
            if (StringUtils.isNotBlank(accessToken)){
                //通过token获取用户id
                Integer memberId = (Integer) redisService.get(CoinConst.TOKENKEYTOKEN + accessToken);
                if (memberId != null){
                    yangBank.setUid(memberId);
                    yangBank.setBankname(bankname);
                    yangBank.setCardname(cardname);
                    yangBank.setCardnum(cardnum);
                    yangBank.setAddress(address);
                    yangBank.setBankBranch(bankBranch);
                    yangBank.setType(type);
                    //是否无效，0否，1是
                    yangBank.setStatus(0);
                    int i = yangBankMapper.saveBankInfoByUser(yangBank);
                    if (i > 0){
                        result.setCode(1);
                        result.setMsg("添加成功");
                    }
                }
            }
        }catch (Exception e){
            result.setCode(0);
            result.setMsg(ErrorCode.ERROR_SYSTEM.getMessage());
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
        }
        return result;
    }

    /**
     * 检查银行卡是否存在
     * 耶耶
     *
     * @param map
     * @return
     */
    public Result checkCardOnly(Map<String, String> map) {
        Result result = new Result();
        try {
            String cardnum = map.get("cardnum");
            Map<String,String> dataMap = yangBankMapper.checkCardOnly(cardnum);
            if (dataMap != null){
                result.setMsg("该银行卡信息已存在");
                result.setCode(0);
            }else{
                result.setCode(1);
                result.setMsg("验证通过");
            }
        }catch (Exception e){
            result.setCode(0);
            result.setMsg(ErrorCode.ERROR_SYSTEM.getMessage());
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
        }
        return result;
    }

    public Result deleteBankInfo(Map<String, String> map) {
        Result result = new Result();
        String accessToken = map.get("accessToken");
        String bankId = map.get("bankId");
        try{
            if (StringUtils.isNotBlank(accessToken)){
                //通过token获取用户id
                Integer memberId = (Integer) redisService.get(CoinConst.TOKENKEYTOKEN + accessToken);
                if (memberId == null){
                    result.setCode(0);
                    result.setMsg("token过期");
                    return result;
                }
                int i = yangBankMapper.deleteBankInfo(bankId);
                if (i > 0){
                    result.setCode(1);
                    result.setMsg("删除成功");
                }else{
                    result.setCode(0);
                    result.setMsg("删除失败");
                }
            }

        }catch (Exception e){
            result.setCode(0);
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
            result.setMsg(ErrorCode.ERROR_SYSTEM.getMessage());
        }
        return result;
    }
}
