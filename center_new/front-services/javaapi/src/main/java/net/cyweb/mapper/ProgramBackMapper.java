package net.cyweb.mapper;

import net.cyweb.config.mybatis.TkMapper;
import net.cyweb.model.YangCurrencyPair;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface ProgramBackMapper {
    void insertBigAvePriceList(List<Map<String, String>> list);

    Integer getYangMemberLast();

    void insertYangMemberList(List<Map<String, Object>> list);
}
