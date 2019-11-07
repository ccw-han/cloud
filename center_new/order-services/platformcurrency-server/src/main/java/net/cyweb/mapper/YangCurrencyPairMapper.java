package net.cyweb.mapper;

import net.cyweb.config.mybatis.TkMapper;
import net.cyweb.model.YangCurrencyPair;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface YangCurrencyPairMapper extends TkMapper<YangCurrencyPair> {

    List<YangCurrencyPair> getRobotList();
}