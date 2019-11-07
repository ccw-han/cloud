package net.cyweb.model;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "yang_member_log")
public class YangMemberLog extends BaseEntity{
    @Id
    @Column(name = "id")
    private Integer id;
    @Column(name = "member_id")
    private Integer memberId;
    @Column(name = "type")
    private Integer type;
    @Column(name = "content")
    private String content;
    @Column(name = "ip")
    private String ip;
    @Column(name = "add_time")
    private Integer addTime;
    @Column(name = "login_type")
    private String loginType;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getMemberId() {
        return memberId;
    }

    public void setMemberId(Integer memberId) {
        this.memberId = memberId;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Integer getAddTime() {
        return addTime;
    }

    public void setAddTime(Integer addTime) {
        this.addTime = addTime;
    }

    public String getLoginType() {
        return loginType;
    }

    public void setLoginType(String loginType) {
        this.loginType = loginType;
    }
}
