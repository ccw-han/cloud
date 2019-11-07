package net.cyweb.service;

import com.github.pagehelper.PageInfo;
import com.netflix.discovery.converters.Auto;
import cyweb.utils.CoinConst;
import cyweb.utils.DateUtils;
import cyweb.utils.ErrorCode;
import jnr.x86asm.ERROR_CODE;
import net.cyweb.mapper.YangMemberLogMapper;
import net.cyweb.mapper.YangMemberMapper;
import net.cyweb.mapper.YangTimelineMapper;
import net.cyweb.model.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.awt.geom.RectangularShape;
import java.net.URLDecoder;
import java.util.Date;
import java.util.List;

@Service
public class YangMemberLogService extends BaseService<YangMemberLog>{
    @Autowired
    private YangMemberLogMapper yangMemberLogMapper;

    @Autowired
    private YangMemberService yangMemberService;

    @Autowired
    private YangMemberTokenService yangMemberTokenService;

    @Autowired
    private YangMemberMapper yangMemberMapper;

    @Autowired
    private YangTimelineMapper yangTimelineMapper;


    //用户日志添加
    public Result addLog(String accessToken,String ip,String loginType){
        Result result=new Result();
        try{
            validateAccessToken(accessToken,result,yangMemberTokenService);
            if(result.getCode().intValue()==Result.Code.ERROR.intValue()){
                return result;
            }
            YangMember yangMember=(YangMember)result.getData();
            YangMemberLog yangMemberLog=new YangMemberLog();
            yangMemberLog.setAddTime(DateUtils.getNowTimes());
            yangMemberLog.setMemberId(yangMember.getMemberId());
            yangMemberLog.setIp(ip);
            yangMemberLog.setLoginType(loginType);
            yangMemberLog.setType(1);
            yangMemberLog.setContent(CoinConst.TIMELINE_LOGIN_STR);
            yangMemberLogMapper.insertSelective(yangMemberLog);

            YangMember yangMember1=new YangMember();
            yangMember1.setMemberId(yangMember.getMemberId());
            yangMember1.setLoginIp(ip);
            yangMember1.setLoginTime(DateUtils.getNowTimes());
            yangMemberMapper.updateByPrimaryKeySelective(yangMember1);
        }catch (Exception e){
            e.printStackTrace();
            result.setCode(Result.Code.ERROR);
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
            result.setMsg(ErrorCode.ERROR_SYSTEM.getMessage());
        }
        return result;
    }
    //查询日志
    public Result getLog(String accessToken,String ip,int page,int pageSize){
        Result result=new Result();
        try{
            validateAccessToken(accessToken,result,yangMemberTokenService);
            if(result.getCode().intValue()==Result.Code.ERROR.intValue()){
                return result;
            }
            YangMember yangMember=(YangMember)result.getData();
            Example example=new Example(YangMemberLog.class);
            Example.Criteria criteria=example.createCriteria();
            if(StringUtils.isNotEmpty(ip)){
                criteria.andEqualTo("ip",ip);
            }
            criteria.andEqualTo("memberId",yangMember.getMemberId());
            int total= yangMemberLogMapper.selectCountByExample(example);

            example.setOrderByClause("add_time desc limit "+(page-1)*pageSize+","+pageSize);
            List<YangMemberLog> list=yangMemberLogMapper.selectByExample(example);
            PageInfo<YangMemberLog> pageInfo = new PageInfo<YangMemberLog>(list);
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

    /**
     * 用户登录 日志操作
     * @param accessToken
     * @param ip
     * @return
     */
    public Result loginRecord(String accessToken,String ip,String loginType,String content,String title){
        Result result=new Result();
        try{
            validateAccessToken(accessToken,result,yangMemberTokenService);
            if(result.getCode().intValue()==Result.Code.ERROR.intValue()){
                return result;
            }
            content= URLDecoder.decode(content,"UTF-8");
            YangMember yangMember=(YangMember)result.getData();
            Example example=new Example(YangMemberLog.class);
            example.createCriteria().andEqualTo("ip",ip).andEqualTo("memberId",yangMember.getMemberId());
            List<YangMemberLog> list=yangMemberLogMapper.selectByExample(example);
            if(list==null||list.size()<=0){
                //发送邮件
                yangMemberService.sendNormalEmailCode(yangMember.getEmail(),title,content,0);
            }
            //记录登陆日志
            YangMemberLog yangMemberLog=new YangMemberLog();
            yangMemberLog.setAddTime(DateUtils.getNowTimes());
            yangMemberLog.setMemberId(yangMember.getMemberId());
            yangMemberLog.setIp(ip);
            yangMemberLog.setLoginType(loginType);
            yangMemberLog.setType(1);
            yangMemberLog.setContent(CoinConst.TIMELINE_LOGIN_STR);
            yangMemberLogMapper.insertSelective(yangMemberLog);

            YangTimeline yangTimeline=new YangTimeline();
            yangTimeline.setAddTime(String.valueOf(DateUtils.getNowTimes()));
            yangTimeline.setType(Byte.valueOf(CoinConst.TIMELINE_LOGIN));
            yangTimeline.setMemberId(yangMember.getMemberId());
            yangTimeline.setContent(CoinConst.TIMELINE_LOGIN_STR);
            yangTimeline.setDataJson("");
            yangTimelineMapper.insert(yangTimeline);
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
