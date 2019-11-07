package net.cyweb.service;

import cyweb.utils.ErrorCode;
import net.cyweb.mapper.YangAreasMapper;
import net.cyweb.model.Result;
import net.cyweb.model.YangArea;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.xml.ws.Action;
import java.util.List;

@Service
public class YangAreaService extends BaseService<YangArea>{

    @Autowired
    private YangAreasMapper yangAreasMapper;

    public Result getArea(String parentId){
        Result result=new Result();
        try{
            Example example=new Example(YangArea.class);
            example.createCriteria().andEqualTo("parentId",parentId);
            List<YangArea> list= yangAreasMapper.selectByExample(example);
            result.setCode(Result.Code.SUCCESS);
            result.setData(list);
        }catch (Exception e){
            result.setCode(Result.Code.ERROR);
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
            result.setMsg(ErrorCode.ERROR_SYSTEM.getMessage());
        }
        return result;
    }
}
