package net.cyweb.model;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

/**
 * 用户邀请码表
 */
@Table(name = "Yang_invite_code")
@Data
public class YangInviteCode {

    @Id
    private Integer id;

    @Column(name = "member_id")
    private String memberId;

    @Column(name = "invite_code")
    private Integer inviteCode;

}
