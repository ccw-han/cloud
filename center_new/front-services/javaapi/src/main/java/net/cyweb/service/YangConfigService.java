package net.cyweb.service;

import cyweb.utils.ErrorCode;
import net.cyweb.mapper.YangConfigMapper;
import net.cyweb.model.Result;
import net.cyweb.model.YangConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class YangConfigService extends BaseService<YangConfig> {

    @Autowired
    private YangConfigMapper yangConfigMapper;

    public Result getConfig(String configName){
        Result result=new Result();
        try{
            Example example=new Example(YangConfig.class);
            example.createCriteria().andEqualTo("key",configName);
            List<YangConfig> list=yangConfigMapper.selectByExample(example);
            result.setCode(Result.Code.SUCCESS);
            result.setData(list.get(0));
        }catch (Exception e){
            e.printStackTrace();
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex() );
            result.setCode(Result.Code.ERROR);
            result.setMsg(ErrorCode.ERROR_SYSTEM.getMessage());
        }
        return result;
    }


}
