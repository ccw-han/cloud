package net.cyweb.mapper;

import net.cyweb.config.mybatis.TkMapper;
import net.cyweb.model.YangFtActivity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface YangFtActivityMapper extends TkMapper<YangFtActivity> {
    int updateFtLeftBatch(List<YangFtActivity> list);
}
