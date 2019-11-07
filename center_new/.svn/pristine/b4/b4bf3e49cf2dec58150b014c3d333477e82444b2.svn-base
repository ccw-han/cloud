package net.cyweb.mapper;

import net.cyweb.config.mybatis.TkMapper;
import net.cyweb.model.YangGoogleauth;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

@Mapper
public interface YangGoogleauthMapper extends TkMapper<YangGoogleauth> {

    Map<String, String> getGoogleauthByStatus(@Param(value = "memberId") Integer memberId);

    YangGoogleauth getGoogle(@Param(value = "memberId") Integer memberId);//获取google信息

    int addGoogleSecretAndQR(YangGoogleauth googleauth);//添加google二维码和密钥

    int updateGoogle(YangGoogleauth yangGoogleauth);//更新google信息

    int cancleGoogle(@Param(value = "memberId") Integer memberId);//删除google信息

    YangGoogleauth findGoogleSecretAndQR(@Param(value = "memberId") Integer memberId);//获取google的二维码和密钥

    YangGoogleauth findSecret(@Param(value = "memberId") Integer memberId);
}