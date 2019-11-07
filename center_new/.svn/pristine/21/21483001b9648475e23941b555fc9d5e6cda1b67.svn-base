package net.cyweb.mapper;

import net.cyweb.config.mybatis.TkMapper;
import net.cyweb.model.YangC2CAsset;
import net.cyweb.model.modelExt.YangCurrencyExt;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface YangC2CAssetMapper extends TkMapper<YangC2CAsset> {

    int assetsChange(Map map);

    int assetTrade(Map map);

    YangC2CAsset getAssetOne(@Param(value = "memberId") Integer memberId, @Param(value = "currencyId") Integer currencyId);

    List<YangC2CAsset> getAssets(@Param(value = "memberId") Integer memberId);

    List<YangCurrencyExt> getCurrencyAndBiBiAssets(Map map);

    List<YangCurrencyExt> getCurrencyAndFaBiAssets(Map map);

    Integer updateById(YangC2CAsset yangC2CAsset);

}
