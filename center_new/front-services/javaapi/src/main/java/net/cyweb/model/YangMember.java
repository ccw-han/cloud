package net.cyweb.model;

import lombok.Data;
import net.cyweb.model.modelExt.YangCurrencyExt;
import net.cyweb.validate.*;
import org.hibernate.validator.constraints.Email;

import java.math.BigDecimal;
import java.util.List;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Table(name = "yang_member")
@Data
public class YangMember extends BaseEntity {
    @Id
    @Column(name = "member_id")
    private Integer memberId;

    private String openid;

    /**
     * 邮箱
     */
    @Column(name = "`email`")
    @NotNull(message = "注册用户名不能为空",groups = {RegisterValidateGroup.class,LoginValidateGroup.class,GoogleAuth.class})
    @Email(message = "注册用户名格式错误",groups = {RegisterValidateGroup.class,LoginValidateGroup.class,GoogleAuth.class})
    private String email;

    /**
     * 密码
     */
    @NotNull(message = "登录密码不能为空",groups = {RegisterValidateGroup.class,LoginValidateGroup.class})
    @Column(name = "`pwd`")
    private String pwd;

    /**
     * 邀请人
     */
    private String pid;

    /**
     * 支付密码
     */
    @Column(name = "`pwdtrade`")
    @NotNull(message = "交易密码不能为空",groups = {CheckPwdTrade.class})
    private String pwdtrade;

    /**
     * 昵称
     */
    @NotNull(message = "昵称不能为空",groups = {ChangeNick.class})
    private String nick;

    /**
     * 真实姓名
     */
    @Column(name = "`name`")
//    @NotNull(message = "姓名不能为空",groups = {RegisterValidateGroup.class})
    private String name;

    /**
     * 1=身份证2=护照
     */
    @Column(name = "`cardtype`")
//    @NotNull(message = "证件类型不能为空",groups = {RegisterValidateGroup.class})
    private String cardtype;

    /**
     * 身份证
     */
    @Column(name = "`idcard`")
//    @NotNull(message = "证件号码不能为空",groups = {RegisterValidateGroup.class})
    private String idcard;

    /**
     * 手机号
     */
    @Column(name = "`phone`")
    private String phone;

    /**
     * 注册IP
     */
    private String ip;

    /**
     * 注册时间
     */
    @Column(name = "reg_time")
    private Long regTime;

    /**
     * 本次登录IP
     */
    @Column(name = "login_ip")
    private String loginIp;

    /**
     * 登录时间
     */
    @Column(name = "login_time")
    private Integer loginTime;

    /**
     * vip等级
     */
    @Column(name = "vip_level")
    private Integer vipLevel;

    /**
     * vip到期时间
     */
    @Column(name = "vip_end_time")
    private Integer vipEndTime;

    /**
     * 人民币
     */
    @NotNull(message = "资金不能为空",groups = {UpdateAssets.class})
    private BigDecimal rmb;

    /**
     * forzen_rmb
     */
    @Column(name = "forzen_rmb")
    @NotNull(message = "冻结资金不能为空",groups = {UpdateAssets.class})
    private BigDecimal forzenRmb;

    /**
     * head
     */
    private String head;

    /**
     * 省市
     */
    private Integer province;

    /**
     * 城市
     */
    private Integer city;

    /**
     * 职位/头衔
     */
    private String job;

    /**
     * 0是正常 1是锁定
     */
    @Column(name = "is_lock")
    private Byte isLock;

    /**
     * 0=有效未填写个人信息1=有效并且填写完个人信息2=无效
     */
    private Byte status;

    @Column(name = "dividend_num")
    private BigDecimal dividendNum;

    private String threepwd;

    /**
     * 交易密码设置类型1,2,3（1；每次都输入，2：每次都不输入：3：第一次输入）
     */
    private Integer typepwd1;

    /**
     * 若是1，表示首次交易，其它设置为0
     */
    private Integer typepwd2;

    /**
     * 银行卡号
     */
    @Column(name = "bank_id")
    private String bankId;

    /**
     * 银行
     */
    private String bank;

    /**
     * 银行预留手机号
     */
    private String phone1;

    @Column(name = "safe_time")
    private String safeTime;

    /**
     * QQopenid
     */
    private String qqopenid;

    /**
     * 身份证正面 
     */
    @NotNull(message = "身份证正面图片地址不能为空",groups = {Certificate.class})
    private String pic1;

    /**
     * 身份证反面 
     */
    @NotNull(message = "身份证反面图片地址不能为空",groups = {Certificate.class})
    private String pic2;

    /**
     * 受持正面
     */
    @NotNull(message = "手持正面图片地址不能为空",groups = {Certificate.class})
    private String pic3;

    /**
     * 3 待审核 1 审核通过  2 审核不通过 0为认证
     */
    private Integer shenhestatus;

    private String zhifubao;

    private Double cmoney;

    /**
     * 返利截止时间
     */
    @Column(name = "invit_time")
    private Long invitTime;

    /**
     * 已废弃
     */
    @Column(name = "google_id")
    private Integer googleId;

    /**
     * 返利天数
     */
    private Integer invit;

    /**
     * super
     */
    @Column(name = "trade_super")
    private Integer tradeSuper;

    @Column(name = "is_system")
    private Byte isSystem;


    @Transient
    @NotNull(message = "旧密码不能为空",groups = {ChangePwd.class})
    public String oldPwd;

    @Transient
    @NotNull(message = "新密码不能为空",groups = {ChangePwd.class})
    public String newPwd;

    @Transient
    public List<YangCurrencyUser> yangCurrencyUserList;


    @Transient
    public List<YangCurrencyExt> yangCurrencyExtsList;


    /*变更用户资产时，增或减*/
    @Transient
    @NotNull(message = "rmbType不能为空",groups = {UpdateAssets.class})
    @Pattern(regexp = "inc|dec",message = "rmbType类型只允许inc | dec",groups = {UpdateAssets.class})
    public String rmbType;

    /*变更用户冻结资产时，增或减*/
    @Transient
    @NotNull(message = "forzenRmbType",groups = {UpdateAssets.class})
    @Pattern(regexp = "inc|dec",message = "forzenRmbType类型只允许inc | dec",groups = {UpdateAssets.class})
    public String forzenRmbType;

    @Column(name = "is_blackname")
    private Integer isBlackName;

    @Transient
    private Integer loginType;

    /**
     * 邀请码
     */
    @Column(name = "invite_code")
    private String inviteCode;

    /**
     * 国籍
     */
    @Column(name = "nationality")
    private String nationality;

    /**
     * 下线人数
     */
    @Column(name = "referrals_num")
    private Integer referralsNum;

    @Column(name = "nationality_code")
    private String nationalityCode;

    @Column(name = "user_level")
    private Integer userLevel;

    private Integer isblacklist;

    @Column(name = "user_vip_type")
    private Integer userVipType;

    @Column(name = "invit_count")
    private Double invitCount;

    @Column(name = "is_acceptance")
    private Integer isAcceptance;

    @Column(name = "is_bigcustmer")
    private Integer isBigcustmer;

    @Column(name = "is_project")
    private Integer isProject;

    @Column(name = "profile")
    private String profile;

    @Column(name = "fiat_money_pwd")
    private String fiatMoneyPwd;

    @Column(name = "pinvite_code")
    private String pInviteCode;
}