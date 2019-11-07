package net.cyweb.service;

import com.github.pagehelper.PageInfo;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import cyweb.utils.CoinConst;
import cyweb.utils.ErrorCode;
import net.cyweb.mapper.YangCardNumMapper;
import net.cyweb.mapper.YangFtActivityMapper;
import net.cyweb.model.Result;
import net.cyweb.model.YangFinance;
import net.cyweb.model.YangFtActivity;
import net.cyweb.model.YangMember;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class YangFtActivityService extends BaseService<YangFtActivity>{

    @Autowired
    private YangFtActivityMapper yangFtActivityMapper;

    @Autowired
    private YangCardNumMapper yangCardNumMapper;
    @Autowired
    private  YangMemberTokenService yangMemberTokenService;

    @Autowired
    private MongoTemplate mongoTemplate;

    public Result getNum(String accessToken){
        Result result=new Result();
        try{
            validateAccessToken(accessToken,result,yangMemberTokenService);
            if(result.getCode().intValue()==Result.Code.ERROR.intValue()){
                return result;
            }
            YangMember yangMember=(YangMember)result.getData();
            Map map=new HashMap();
            Map mapa=new HashMap();
            Map mapb=new HashMap();
            map.put("memberId",yangMember.getMemberId());
            mapa=yangFtActivityMapper.getFuntNum(map);
            mapb=yangCardNumMapper.getFuntNum(map);
            if(mapa!=null){
                mapa.putAll(mapb==null?new HashMap():mapb);
                result.setData(mapa);
                return result;
            }
            if(mapb!=null){
                mapb.putAll(mapa==null?new HashMap():mapa);
                result.setData(mapb);
                return result;
            }
            if(mapa==null&&mapb==null){
                Map noData=new HashMap();
                noData.put("num",0);
                noData.put("dealNum",0);
                noData.put("mining",0);
                result.setData(noData);
                return result;
            }
        }catch (Exception e){
            e.printStackTrace();
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex() );
            result.setCode(Result.Code.ERROR);
            result.setMsg(ErrorCode.ERROR_SYSTEM.getMessage());
        }
        return result;
    }

    public Result getCardOrder(String accessToken,int page,int pageSize){
        Result result=new Result();
        try{
            validateAccessToken(accessToken,result,yangMemberTokenService);
            if(result.getCode().intValue()==Result.Code.ERROR.intValue()){
                return result;
            }
            YangMember yangMember=(YangMember)result.getData();
            //查询数据
            Map map=new HashMap();
            map.put("memberId",yangMember.getMemberId());
            map.put("start",(page-1)*pageSize);
            map.put("end",pageSize);
            List<Map> list=yangFtActivityMapper.getCardOrder(map);
            //分页查询数据
            PageInfo<Map> pageInfo=new PageInfo<Map>();
            pageInfo.setTotal(yangFtActivityMapper.getCardOrderCount(map));
            pageInfo.setList(list);
            result.setData(pageInfo);
        }catch (Exception e){
            e.printStackTrace();
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex() );
            result.setCode(Result.Code.ERROR);
            result.setMsg(ErrorCode.ERROR_SYSTEM.getMessage());
        }
        return result;
    }


    public Result getFtLockRecord(String accessToken,int page,int pageSize){
        Result result=new Result();
        try{
            validateAccessToken(accessToken,result,yangMemberTokenService);
            if(result.getCode().intValue()==Result.Code.ERROR.intValue()){
                return result;
            }
            YangMember yangMember=(YangMember)result.getData();
            //查询数据
            Map map=new HashMap();
            map.put("memberId",yangMember.getMemberId());
            map.put("start",(page-1)*pageSize);
            map.put("end",pageSize);
            List<Map> list=yangFtActivityMapper.getFtLockRecord(map);
            //分页查询数据
            PageInfo<Map> pageInfo=new PageInfo<Map>();
            pageInfo.setTotal(yangFtActivityMapper.getFtLockRecordCount(map));
            pageInfo.setList(list);
            result.setData(pageInfo);
        }catch (Exception e){
            e.printStackTrace();
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex() );
            result.setCode(Result.Code.ERROR);
            result.setMsg(ErrorCode.ERROR_SYSTEM.getMessage());
        }
        return result;
    }

    public Result getFtLockRecordSum(String accessToken){
        Result result=new Result();
        try{
            validateAccessToken(accessToken,result,yangMemberTokenService);
            if(result.getCode().intValue()==Result.Code.ERROR.intValue()){
                return result;
            }
            YangMember yangMember=(YangMember)result.getData();
            //查询数据
            Map resultMap=new HashMap();
            Map param=new HashMap();
            param.put("status",CoinConst.FT_LOCK_STATUS_TURNOUT);
            param.put("memberId",yangMember.getMemberId());
            //转出中
            Map a=yangFtActivityMapper.getFtLockRecordSum(param);
            resultMap.put("unlocking",(a==null||a.get("num")==null)?0:a.get("num"));
            //转出完成
            param.put("status",CoinConst.FT_LOCK_STATUS_FT_LOCK_UNLOCK_SUCEESS);
            Map b=yangFtActivityMapper.getFtLockRecordSum(param);
            resultMap.put("unlocked",(b==null||b.get("num")==null)?0:b.get("num"));
            //冻结数量
            Map c=yangFtActivityMapper.getFtLockInfo(param);
            resultMap.put("locked",(c==null||c.get("locked")==null)?0:c.get("locked"));
            result.setData(resultMap);
        }catch (Exception e){
            e.printStackTrace();
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex() );
            result.setCode(Result.Code.ERROR);
            result.setMsg(ErrorCode.ERROR_SYSTEM.getMessage());
        }
        return result;
    }


    public Result getCardLock(String accessToken,int page,int pageSize){
        Result result=new Result();
        try{
            validateAccessToken(accessToken,result,yangMemberTokenService);
            if(result.getCode().intValue()==Result.Code.ERROR.intValue()){
                return result;
            }
            YangMember yangMember=(YangMember)result.getData();
            Query query=new Query();
            query.with(new Sort(new Sort.Order(Sort.Direction.DESC,"addTime")));
            query.addCriteria(Criteria.where("memberId").is(yangMember.getMemberId()).and("type").is(CoinConst.FINANCE_RECORD_ADD_BY_VIP_UNLOCK));
            long total=this.mongoTemplate.count(query,Map.class,CoinConst.MONGODB_YANG_FINANCE);//总数量

            query.skip((page-1)*pageSize).limit(pageSize);
            List<Map> resultList= this.mongoTemplate.find(query,Map.class,CoinConst.MONGODB_YANG_FINANCE);
            for(Map map:resultList){
                map.remove("_id");
            }
            PageInfo<Map> pageInfo=new PageInfo<Map>();
            pageInfo.setTotal(total);
            pageInfo.setList(resultList);
            result.setData(pageInfo);
        }catch (Exception e){
            e.printStackTrace();
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex() );
            result.setCode(Result.Code.ERROR);
            result.setMsg(ErrorCode.ERROR_SYSTEM.getMessage());
        }
        return result;
    }
}
