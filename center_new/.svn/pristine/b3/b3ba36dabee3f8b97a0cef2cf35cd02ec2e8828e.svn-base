package net.cyweb.service;

import net.cyweb.mapper.YangInviteCodeMapper;
import net.cyweb.model.YangInviteCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class YangInviteCodeService {

    @Autowired
    private YangInviteCodeMapper yangInviteCodeMapper;

    public YangInviteCode getInviteCode(String inviteCode) {
        return yangInviteCodeMapper.getInviteCode(inviteCode);
    }

    public void addInviteCode(String invCode) {
        yangInviteCodeMapper.addInviteCode(invCode);
    }
}
