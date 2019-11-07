package net.cyweb.mapper;

import net.cyweb.config.mybatis.TkMapper;
import net.cyweb.model.YangQianBaoAddress;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface YangQianbaoAddressMapper extends TkMapper<YangQianBaoAddress> {
    Integer addDepositAddress(YangQianBaoAddress yangQianBaoAddress);

    YangQianBaoAddress getYangQianBaoAddressByQianBaoUrl(String qianbaoUrl);

    Integer insertYangQiaoBaoAddress(YangQianBaoAddress yangQianBaoAddress);
}
