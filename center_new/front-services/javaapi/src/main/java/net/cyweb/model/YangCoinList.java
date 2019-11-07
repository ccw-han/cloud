package net.cyweb.model;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "yang_coin_list")
public class YangCoinList extends BaseEntity{
    @Id
    @Column(name = "id")
    private Integer id;
    @Column(name = "full_name")
    private String fullName;		// 全称
    @Column(name = "sub_name")
    private String subName;		// 简称
    @Column(name = "area")
    private String area;		// 地区
    @Column(name = "date")
    private String date;		// 上线日期
    @Column(name = "suan")
    private String suan;		// 算法
    @Column(name = "type")
    private String type;		// 币种类型
    @Column(name = "total")
    private String total;		// 发行总量
    @Column(name = "liu")
    private String liu;		// 流通量
    @Column(name = "price")
    private String price;		// 价格
    @Column(name = "is_sell")
    private String isSell;		// 是否进行代币售卖
    @Column(name = "mu")
    private String mu;		// 募集资金总量
    @Column(name = "url")
    private String url;		// 项目网址
    @Column(name = "wallet")
    private String wallet;		// 钱包下载地址
    @Column(name = "wallet_desc")
    private String walletDesc;		// 钱包安装使用说明
    @Column(name = "api")
    private String api;		// API文档
    @Column(name = "white")
    private String white;		// 白皮书
    @Column(name = "contact")
    private String contact;		// 负责人联系方式
    @Column(name = "pro_desc")
    private String proDesc;		// 项目介绍
    @Column(name = "pro_gu")
    private String proGu;		// 项目价值
    @Column(name = "platform")
    private String platform;		// 已上线平台
    @Column(name = "she")
    private String she;		// 社区地址
    @Column(name = "user")
    private String user;		// 活跃用户
    @Column(name = "send")
    private String send;		// 赠送代币活动方案
    @Column(name = "fei")
    private String fei;		// 非小号等链接
    @Column(name = "other")
    private String other;		// 其他
    @Column(name = "member_id")
    private Integer memberId;		// 用户id
    @Column(name = "add_time")
    private Integer addTime;		// 申请时间


    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getSubName() {
        return subName;
    }

    public void setSubName(String subName) {
        this.subName = subName;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getSuan() {
        return suan;
    }

    public void setSuan(String suan) {
        this.suan = suan;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public String getLiu() {
        return liu;
    }

    public void setLiu(String liu) {
        this.liu = liu;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getIsSell() {
        return isSell;
    }

    public void setIsSell(String isSell) {
        this.isSell = isSell;
    }

    public String getMu() {
        return mu;
    }

    public void setMu(String mu) {
        this.mu = mu;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getWallet() {
        return wallet;
    }

    public void setWallet(String wallet) {
        this.wallet = wallet;
    }

    public String getWalletDesc() {
        return walletDesc;
    }

    public void setWalletDesc(String walletDesc) {
        this.walletDesc = walletDesc;
    }

    public String getApi() {
        return api;
    }

    public void setApi(String api) {
        this.api = api;
    }

    public String getWhite() {
        return white;
    }

    public void setWhite(String white) {
        this.white = white;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getProDesc() {
        return proDesc;
    }

    public void setProDesc(String proDesc) {
        this.proDesc = proDesc;
    }

    public String getProGu() {
        return proGu;
    }

    public void setProGu(String proGu) {
        this.proGu = proGu;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getShe() {
        return she;
    }

    public void setShe(String she) {
        this.she = she;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getSend() {
        return send;
    }

    public void setSend(String send) {
        this.send = send;
    }

    public String getFei() {
        return fei;
    }

    public void setFei(String fei) {
        this.fei = fei;
    }

    public String getOther() {
        return other;
    }

    public void setOther(String other) {
        this.other = other;
    }

    public Integer getMemberId() {
        return memberId;
    }

    public void setMemberId(Integer memberId) {
        this.memberId = memberId;
    }

    public Integer getAddTime() {
        return addTime;
    }

    public void setAddTime(Integer addTime) {
        this.addTime = addTime;
    }
}
