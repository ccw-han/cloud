package net.cyweb.mapper;

import net.cyweb.config.mybatis.TkMapper;
import net.cyweb.model.YangFtLock;
import net.cyweb.model.YangMember;
import net.cyweb.model.YangQianBaoAddress;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.HashMap;
import java.util.Map;


@Mapper
public interface YangMemberMapper extends TkMapper<YangMember> {

//    call assets(3211,'0',125.001,'dec',56.89,'dec');

//    call assets_one2one(
// 3211,'40',125.001,'asc',56.89,'asc',
//         0,'40',125.001,'asc',102,'dec'
//    );

    int findMemberPwdByEmail(@Param(value = "email") String email, @Param(value = "pwd") String pwd);

    int findMemberPwdByPhone(@Param(value = "phone") String phone, @Param(value = "pwd") String pwd);

    int resetPasswordByEmail(@Param(value = "email") String email, @Param(value = "pwd") String pwd);

    int resetPasswordByPhone(@Param(value = "phone") String phone, @Param(value = "pwd") String pwd);

    int assets(HashMap pama);

    int platFormCurrencyLock(YangFtLock yangFtLock);

    int platFormCurrencyUnLock(YangFtLock yangFtLock);

    void updateByInviteCode(String inviteCode);

    void updateYangmember(YangMember loginyangMember);

    Map<String, String> getMemberByEmail(String loginUserName);

    Map<String, String> getMemberByPhone(String loginUserName);

    YangMember getMemberIdByEmail(String email);

    YangMember getMemberIdByPhone(String phone);

    int resetPwdByEmail(YangMember yangMember);

    int resetPwdByPhone(YangMember yangMember);

    Map<String,String> checkAccount(YangMember yangMember);

    int changePwd(YangMember yangMember);

    int updateMemberCertification(YangMember yangMember);

    int setFiatMoneyPwd(@Param(value = "id") Integer id, @Param(value = "fiatMoneyPwd") String fiatMoneyPwd);

    int findFiatMoneyPwd(@Param(value = "id") Integer id, @Param(value = "fiatMoneyPwd") String fiatMoneyPwd);

    YangQianBaoAddress getAddress(@Param(value = "userId") Integer userId,
                                  @Param(value = "currencyId") Integer currencyId);
    YangMember getUserInfo(Integer memberId); //根据用户的id获取用户信息

    int setsFiatMoneyPwd(YangMember yangMember);//设置资金密码接口

    int updateFiatMoneyPwd(YangMember yangMember);//修改资金密码

    int resetFiatMoneyPwdByEmail(YangMember yangMember);//通过邮箱找回交易密码

    int resetFiatMoneyPwdByPhone(YangMember yangMember);//通过手机找回交易密码

    Map<String, String> getFiatMoneyPwdByEmail(@Param(value = "loginUserName") String loginUserName);//重置交易密码

    Map<String, String> getFiatMoneyPwdByPhone(@Param(value = "loginUserName") String loginUserName);

    int orderConfirm(HashMap pama);
}