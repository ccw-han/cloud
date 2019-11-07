package net.cyweb.service;

import com.sun.org.apache.regexp.internal.RE;
import cyweb.utils.CoinConst;
import cyweb.utils.ErrorCode;
import cyweb.utils.UUIDUtils;
import net.cyweb.mapper.YangAcceptanceMapper;
import net.cyweb.mapper.YangAreasMapper;
import net.cyweb.model.Result;
import net.cyweb.model.YangAcceptance;
import net.cyweb.model.YangArea;
import net.cyweb.model.YangMember;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class YangAcceptanceService extends BaseService<YangAcceptance> {

    @Autowired
    private YangAcceptanceMapper yangAcceptanceMapper;

    @Autowired
    private YangMemberTokenService yangMemberTokenService;

    @Autowired
    private RedisService redisService;
    /**
     * 承兑商申请基本信息保存-baseInfo
     *
     * @return
     * @Param map
     */
    public Result saveAcceptancesBaseInfo(Map<String, String> map) {
        Result result = new Result();
        String accessToken = map.get("accessToken");
        String name = map.get("name");
        String phone = map.get("phone");
        String wxCode = map.get("wxCode");
        String email = map.get("email");
        String address = map.get("address");
        String emergencyContactName = map.get("emergencyContactName");
        String emergencyContactPhone = map.get("emergencyContactPhone");
        String relationship = map.get("relationship");
        try {
            //验证token
            if (StringUtils.isNotBlank(accessToken)) {
                result = this.validateAccessToken(accessToken, result, yangMemberTokenService);
                if (result.getData() == null) {
                    result.setCode(Result.Code.ERROR);
                    result.setData("");
                    result.setMsg(ErrorCode.ERROR_ERROR_TOKEN_EXPIRE.getMessage());
                    return result;
                }
                YangMember yangMember = (YangMember) result.getData();
                YangAcceptance yangAcceptance = new YangAcceptance();
                yangAcceptance.setAcceptancesId(UUIDUtils.genUUID());
                yangAcceptance.setMemberId(yangMember.getMemberId());
                yangAcceptance.setName(name);
                yangAcceptance.setPhone(phone);
                yangAcceptance.setWxCode(wxCode);
                yangAcceptance.setEmail(email);
                yangAcceptance.setAddress(address);
                yangAcceptance.setEmergencyContactName(emergencyContactName);
                yangAcceptance.setEmergencyContactPhone(emergencyContactPhone);
                yangAcceptance.setRelationship(relationship);
                yangAcceptance.setSelfCardHoldPic("");
                yangAcceptance.setRegistrationBook("");
                yangAcceptance.setAccestPic("");
                yangAcceptance.setVideoAuthentication("");
                yangAcceptance.setStates("0");
                yangAcceptance.setIsReady("0");
                yangAcceptance.setCreateTime(new Date());
                yangAcceptance.setCreateBy(0);
                yangAcceptance.setUpdateTime(new Date());
                yangAcceptance.setUpdateBy(0);
                yangAcceptance.setDelFlag("0");
                yangAcceptanceMapper.insertYangAcceptance(yangAcceptance);
                result.setCode(Result.Code.SUCCESS);
                result.setData("");
                result.setMsg("承兑商保存信息成功");
            }
        } catch (Exception e) {
            result.setData("");
            result.setCode(Result.Code.ERROR);
            result.setMsg(ErrorCode.ERROR_SYSTEM.getMessage());
        }
        return result;
    }

    /**
     * 承兑商申请证明信息保存（图片地址，视频地址）-picInfo
     * 耶耶
     * time:2019/9/27
     * @param map
     * @return
     */
    public Result updateAcceptancesPicInfo(Map<String, String> map) {
        Result result = new Result();
        String accessToken = map.get("accessToken");
        map.get("accessToken");
        String selfCardholdpic = map.get("selfCardholdpic");
        String registrationBook = map.get("registrationBook");
        String videoAuthentication = map.get("videoAuthentication");
        String accestPic = map.get("accestPic");
        YangAcceptance yangAcceptance = new YangAcceptance();
        yangAcceptance.setSelfCardHoldPic(selfCardholdpic);
        yangAcceptance.setRegistrationBook(registrationBook);
        yangAcceptance.setVideoAuthentication(videoAuthentication);
        yangAcceptance.setAccestPic(accestPic);
        if (StringUtils.isNotBlank(accessToken)){
            //通过token获取用户id
            Integer memberId = (Integer) redisService.get(CoinConst.TOKENKEYTOKEN + accessToken);
            if (memberId == null){
                result.setMsg("token过期");
                result.setCode(0);
                return result;
            }
            yangAcceptance.setMemberId(memberId);
            int i = yangAcceptanceMapper.updateAcceptancesPicInfo(yangAcceptance);
            if (i < 1){
                result.setMsg(ErrorCode.ERROR_SYSTEM.getMessage());
                result.setCode(0);
                return result;
            }
            result.setCode(1);
            result.setMsg("更新信息成功");
        }
        return result;
    }

    /**
     * 承兑商撤销审核
     *
     * @return
     * @Param map
     */
    public Result deletAcceptancesById(Map<String, String> map) {
        Result result = new Result();
        String accessToken = map.get("accessToken");
        try {
            //验证token
            if (StringUtils.isNotBlank(accessToken)) {
                result = this.validateAccessToken(accessToken, result, yangMemberTokenService);
                if (result.getData() == null) {
                    result.setCode(Result.Code.ERROR);
                    result.setData("");
                    result.setMsg(ErrorCode.ERROR_ERROR_TOKEN_EXPIRE.getMessage());
                    return result;
                }
                YangMember yangMember = (YangMember) result.getData();
                yangAcceptanceMapper.updateStatesByMemberId(yangMember.getMemberId(), "2");
                result.setCode(Result.Code.SUCCESS);
                result.setData("");
                result.setMsg("撤销审核成功");
            }
        } catch (Exception e) {
            result.setData("");
            result.setCode(Result.Code.ERROR);
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
            result.setMsg(ErrorCode.ERROR_SYSTEM.getMessage());
        }
        return result;
    }

    /**
     * 承兑商申请修改审核状态
     *
     * @return
     * @Param map
     */
    public Result updateAcceptancesIsReady(Map<String, String> map) {
        Result result = new Result();
        String accessToken = map.get("accessToken");
        String isReady = map.get("isReady");
        try {
            //验证token
            if (StringUtils.isNotBlank(accessToken)) {
                result = this.validateAccessToken(accessToken, result, yangMemberTokenService);
                if (result.getData() == null) {
                    result.setCode(Result.Code.ERROR);
                    result.setData("");
                    result.setMsg(ErrorCode.ERROR_ERROR_TOKEN_EXPIRE.getMessage());
                    return result;
                }
                YangMember yangMember = (YangMember) result.getData();
                if ("1".equals(isReady)) {
                    yangAcceptanceMapper.updateStatesByMemberId(yangMember.getMemberId(), "3");
                }
                result.setCode(Result.Code.SUCCESS);
                result.setData("");
                result.setMsg("撤销审核成功");
            }
        } catch (Exception e) {
            result.setData("");
            result.setCode(Result.Code.ERROR);
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
            result.setMsg(ErrorCode.ERROR_SYSTEM.getMessage());
        }
        return result;
    }
}
