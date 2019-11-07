package net.cyweb.mapper;

import net.cyweb.config.mybatis.TkMapper;
import net.cyweb.model.YangTibi;
import net.cyweb.model.modelExt.YangTibiExt;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface YangTibiMapper extends TkMapper<YangTibi> {

    List<YangTibiExt> selectTibi(YangTibiExt yangTibi);

    List<YangTibi> getDepositRecord(@Param(value = "userId") Integer userId);

    Integer insertYangTiBi(YangTibi yangTibi);

    YangTibi selectByTiId(String tiId);
}