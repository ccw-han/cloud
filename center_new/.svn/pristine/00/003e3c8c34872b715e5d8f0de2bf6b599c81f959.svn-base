package net.cyweb.mapper;

import net.cyweb.config.mybatis.TkMapper;
import net.cyweb.model.YangTibiOut;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface YangTibiOutMapper extends TkMapper<YangTibiOut> {

    List<Map> findTiBiList(Map map);

    int selectTotalRecord(Map map);

    /**
     * 获取指定币的所有历史提币记录
     *
     * @param currencyId 币种的id
     * @return 提币记录的信息 status(提币的状态) num(提币的数量) addTime(提币的次数)
     */
    List<YangTibiOut> getWithdrawRecordByCurrencyId(@Param(value = "currencyId") String currencyId, @Param(value = "memberId") Integer memberId);

    List<Map<String, Object>> getWithdrawRecordByStatus(List currencyIds);

    YangTibiOut selectYangTibiOutById(Integer id);

    Integer updateYangTiBiOut(YangTibiOut yangTibiOut);
}
