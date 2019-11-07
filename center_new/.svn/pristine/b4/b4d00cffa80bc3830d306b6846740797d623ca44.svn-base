package net.cyweb.mapper;

import net.cyweb.config.mybatis.TkMapper;
import net.cyweb.model.YangCurrency;
import net.cyweb.model.YangCurrencySelf;
import net.cyweb.model.YangWorkOrder;
import net.cyweb.model.modelExt.YangCurrencyExt;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 工单Mapper
 * 创建人：ccw
 * Time：2019/9/3
 *
 * @return
 */
@Mapper
public interface YangWorkOrderMapper extends TkMapper<YangWorkOrder> {

    void addWorkOrder(YangWorkOrder order);

    List<YangWorkOrder> findOldWorkOrders(Integer memberId);

    YangWorkOrder findWorkOrderById(@Param(value = "id") String id, @Param(value = "memberId") Integer memberId);

    List<YangCurrencySelf> getSelfCurrencys(@Param(value = "memberId") Integer memberId);

    List<Map> getSelfCurrencysById(List ids);

    int addSelfCurrencys(Map<String, String> map);

}