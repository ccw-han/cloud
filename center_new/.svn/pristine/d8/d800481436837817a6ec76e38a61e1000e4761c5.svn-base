package net.cyweb.service;

import cyweb.utils.DateUtils;
import cyweb.utils.ErrorCode;
import net.cyweb.mapper.YangTimelineMapper;
import net.cyweb.model.Result;
import net.cyweb.model.YangMember;
import net.cyweb.model.YangTimeline;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.List;

@Service
public class YangTimeLineService extends BaseService<YangTimeline>{
    @Autowired
    private YangTimelineMapper yangTimelineMapper;

    @Autowired
    private YangMemberTokenService yangMemberTokenService;



    public Result getTimeLine(String accessToken,String type,String start,String end){
        Result result=new Result();
        try{
            //验证用户 token
        result=validateAccessToken(accessToken,result,yangMemberTokenService);
        if(result.getCode().intValue()==Result.Code.ERROR.intValue()){
            return result;
        }
        YangMember yangMember=(YangMember)result.getData();
        result.setData(null);
        Example example=new Example(YangTimeline.class);
        Example.Criteria criteria=example.createCriteria();
        criteria.andEqualTo("memberId",yangMember.getMemberId());
        if(StringUtils.isNotEmpty(type)){
            criteria.andEqualTo("type",type);
        }
        if(StringUtils.isNotEmpty(start)){
            criteria.andGreaterThanOrEqualTo("addTime",start);
        }
        if(StringUtils.isNotEmpty(end)){
            criteria.andLessThanOrEqualTo("addTime",end);
        }
        example.setOrderByClause("add_time desc");
        List<YangTimeline> list=yangTimelineMapper.selectByExample(example);
        result.setData(list==null?new ArrayList<YangTimeline>():list);
        result.setCode(Result.Code.SUCCESS);
        }catch (Exception e){
            e.printStackTrace();
            result.setErrorCode(ErrorCode.SEND_EMAILCODE_ERROR.getIndex() );
            result.setCode(Result.Code.ERROR);
            result.setMsg(ErrorCode.SEND_EMAILCODE_ERROR.getMessage());
        }
        return  result;
    }



    public Result addTimeLine(String accessToken,String type,String content,String json){
        Result result=new Result();
        try{
            //验证用户 token
            result=validateAccessToken(accessToken,result,yangMemberTokenService);
            if(result.getCode().intValue()==Result.Code.ERROR.intValue()){
                return result;
            }
            YangMember yangMember=(YangMember)result.getData();
            result.setData(null);
            YangTimeline yangTimeline=new YangTimeline();
            yangTimeline.setAddTime(String.valueOf(DateUtils.getNowTimes()));
            yangTimeline.setType(Byte.valueOf(type));
            yangTimeline.setMemberId(yangMember.getMemberId());
            yangTimeline.setContent(content);
            yangTimeline.setDataJson(json);
            yangTimelineMapper.insert(yangTimeline);
            result.setCode(Result.Code.SUCCESS);
        }catch (Exception e){
            e.printStackTrace();
            result.setErrorCode(ErrorCode.SEND_EMAILCODE_ERROR.getIndex() );
            result.setCode(Result.Code.ERROR);
            result.setMsg(ErrorCode.SEND_EMAILCODE_ERROR.getMessage());
        }
        return result;
    }

    public void addTimeLineByMemberId(int memberId,Byte type,String content,String json) throws Exception{
        YangTimeline yangTimeline=new YangTimeline();
        yangTimeline.setAddTime(String.valueOf(DateUtils.getNowTimes()));
        yangTimeline.setType(type);
        yangTimeline.setMemberId(memberId);
        yangTimeline.setContent(content);
        yangTimeline.setDataJson(json);
        yangTimelineMapper.insert(yangTimeline);
    }
}
