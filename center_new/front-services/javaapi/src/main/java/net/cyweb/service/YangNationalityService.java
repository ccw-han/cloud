package net.cyweb.service;

import cyweb.utils.ErrorCode;
import net.cyweb.mapper.YangNationalityMapper;
import net.cyweb.model.Result;
import net.cyweb.model.YangNationality;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 国籍服务层
 */
@Service
public class YangNationalityService {

    @Autowired
    private YangNationalityMapper yangNationalityMapper;
    /**
     * 获取国籍集合
     * 椰椰
     * Time：2019/9/5
     * @return
     */
    public Result getNationalityList() {
        Result result = new Result();
        try{
            List<Map> list = yangNationalityMapper.getNationalityList();
            if (list != null){
                result.setCode(Result.Code.SUCCESS);
                result.setData(list);
                result.setMsg("查询成功");
            }else {
                result.setCode(Result.Code.ERROR);
                result.setMsg("查询失败");
                result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
            }
        }catch (Exception e){
            e.printStackTrace();
            result.setCode(Result.Code.ERROR);
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
            result.setMsg(ErrorCode.ERROR_SYSTEM.getMessage());
        }
        return result;
    }
}
