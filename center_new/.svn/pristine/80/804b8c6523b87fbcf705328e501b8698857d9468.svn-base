package net.cyweb.service;

import net.cyweb.mapper.YangFtLockMapper;
import net.cyweb.model.YangFtLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class YangFtLockService extends BaseService<YangFtLock>{

    @Autowired
    private YangFtLockMapper yangFtLockMapper;

    public  YangFtLock getUserLockInfo(Map map){
        Example example = new Example(YangFtLock.class);
        example.createCriteria().andEqualTo("memberId", (int)map.get("memberId"));
        List<YangFtLock> list= yangFtLockMapper.selectByExample(example);
        return list.get(0);
    }

    public int updateYangFtLock(YangFtLock yangFtLock){
        return yangFtLockMapper.updateByPrimaryKeySelective(yangFtLock);
    }

    public BigDecimal getTotalFrozenNum(){
        Map map= yangFtLockMapper.getTotalFrozenNum();
        return (BigDecimal)map.get("totalFrozenNum");
    }

    public List<YangFtLock> getFrozenNumGtZeroUserList(){
        Example example=new Example(YangFtLock.class);
        example.createCriteria().andGreaterThan("forzenNum",0);
        List<YangFtLock> list= yangFtLockMapper.selectByExample(example);
        return list;
    }
}
