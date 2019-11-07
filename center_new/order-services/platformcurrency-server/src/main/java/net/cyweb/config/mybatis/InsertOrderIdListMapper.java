package net.cyweb.config.mybatis;

import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Options;
import tk.mybatis.mapper.provider.SpecialProvider;

public interface InsertOrderIdListMapper<T> extends TkMapper<T>{
    /**
     * 插入数据，限制为实体包含`id`属性并且必须为自增列，实体配置的主键策略无效
     *
     * @param record
     * @return
     */
    @Options(useGeneratedKeys = true, keyProperty = "ordersId")
    @InsertProvider(type = SpecialProvider.class, method = "dynamicSQL")
    int insertUseGeneratedKeys(T record);

}
