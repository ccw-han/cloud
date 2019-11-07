package net.cyweb.mapper;

import net.cyweb.config.mybatis.TkMapper;
import net.cyweb.model.YangFinance;
import net.cyweb.model.YangInviteCode;

public interface YangInviteCodeMapper extends TkMapper<YangInviteCode> {
    YangInviteCode getInviteCode(String inviteCode);

    void addInviteCode(String invCode);
}
