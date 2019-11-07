package net.cyweb.model;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Data
@Table(name = "yang_member_acceptances")
public class YangAcceptance extends BaseEntity {
    @Id
    private Integer id;

    @Column(name = "acceptances_id")
    private String acceptancesId;

    @Column(name = "member_id")
    private Integer memberId;

    private String name;

    private String phone;

    @Column(name = "wx_code")
    private String wxCode;

    private String email;

    @Column(name = "emergency_contact_name")
    private String emergencyContactName;

    @Column(name = "emergency_contact_phone")
    private String emergencyContactPhone;

    private String address;

    private String relationship;

    @Column(name = "self_card_hold_pic")
    private String selfCardHoldPic;

    @Column(name = "registration_book")
    private String registrationBook;

    @Column(name = "accest_pic")
    private String accestPic;

    @Column(name = "video_authentication")
    private String videoAuthentication;

    private String states;

    @Column(name = "is_ready")
    private String isReady;

    @Column(name = "create_time")
    private Date createTime;

    @Column(name = "create_by")
    private Integer createBy;

    @Column(name = "update_time")
    private Date updateTime;

    @Column(name = "update_by")
    private Integer updateBy;

    @Column(name = "del_flag")
    private String delFlag;

}
