package net.cyweb.service;

import cyweb.utils.ErrorCode;
import net.cyweb.mapper.SysAreaMapper;
import net.cyweb.model.Result;
import net.cyweb.model.SysArea;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class SysAreaService extends BaseService<SysArea>{

    @Autowired
    private SysAreaMapper sysAreaMapper;


    public Result getArea(String parentId){
        Result result=new Result();
        try{
            Example example=new Example(SysArea.class);
            example.createCriteria().andEqualTo("parentId",parentId);
            List<SysArea> list= sysAreaMapper.selectByExample(example);
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
