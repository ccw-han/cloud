package net.cyweb.mapper;

import net.cyweb.config.mybatis.TkMapper;
import net.cyweb.model.YangBank;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface YangBankMapper extends TkMapper<YangBank> {
    List<Map> selectYangBankById(@Param(value = "uid") String uid, @Param(value = "type") String type);

    int saveBankInfoByUser(YangBank yangBank);

    Map<String, String> checkCardOnly(String cardnum);

    int deleteBankInfo(String bankId);
}
