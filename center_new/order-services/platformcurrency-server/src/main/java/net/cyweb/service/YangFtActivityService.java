package net.cyweb.service;

import net.cyweb.mapper.YangFtActivityMapper;
import net.cyweb.model.YangFtActivity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@EnableAsync
public class YangFtActivityService extends  BaseService<YangFtActivity>{
    @Autowired
    private YangFtActivityMapper yangFtActivityMapper;

    @Transactional
    public int updateFtLeftBatch(List<YangFtActivity> list){
      return  yangFtActivityMapper.updateFtLeftBatch(list);
    }
}
