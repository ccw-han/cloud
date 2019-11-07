package net.cyweb.service;

import net.cyweb.mapper.YangCardNumMapper;
import net.cyweb.model.YangCardNum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@EnableAsync
public class YangCardNumService extends  BaseService<YangCardNum>{

    @Autowired
    private YangCardNumMapper yangCardNumMapper;

    public BigDecimal getDealNum(){
        Map a=yangCardNumMapper.getDealNums();
        return (BigDecimal) a.get("totalNum");
    }

    public List<YangCardNum> getAllCardNumInfos(){
        Example example=new Example(YangCardNum.class);
        List<YangCardNum> list=yangCardNumMapper.selectByExample(example);
        return list;
    }

    public int updateYangCardNum(YangCardNum yangCardNum){
        return yangCardNumMapper.updateByPrimaryKeySelective(yangCardNum);
    }
}
