package net.cyweb.mapper;

import net.cyweb.config.mybatis.TkMapper;
import net.cyweb.model.YangEmailLog;
import org.apache.ibatis.annotations.Mapper;

import java.util.Map;

@Mapper
public interface YangEmailLogMapper extends TkMapper<YangEmailLog> {

    Map getLastEmail(Map map);
}
