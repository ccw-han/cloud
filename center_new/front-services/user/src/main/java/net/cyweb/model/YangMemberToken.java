package net.cyweb.model;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Table(name = "yang_member_token")
@Data
public class YangMemberToken extends BaseEntity {
    @Id
    private Integer id;

    @Column(name = "access_token")
    @NotNull(message = "token不能为空")
    private String accessToken;

    @Column(name = "member_id")
    private Integer memberId;

    @Column(name = "add_time")
    private Long addTime;

    /**
     * 有效期时长 秒
     */
    private Integer expire;


}