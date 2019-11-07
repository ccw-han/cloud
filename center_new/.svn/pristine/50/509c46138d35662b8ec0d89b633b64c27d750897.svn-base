package net.cyweb.model;

import javax.persistence.*;

@Table(name = "yang_googleauth")
public class YangGoogleauth extends BaseEntity {
    @Id
    @Column(name = "google_id")
    private Integer googleId;

    /**
     * secret编号
     */
    private String secret;

    /**
     * 用户编号
     */
    @Column(name = "member_id")
    private Integer memberId;

    /**
     * 1开始,0关闭
     */
    @Column(name = "login_lock")
    private Integer loginLock;

    /**
     * 1开始,0关闭
     */
    @Column(name = "order_lock")
    private Integer orderLock;

    /**
     * 1开始,0关闭
     */
    @Column(name = "money_lock")
    private Integer moneyLock;

    /**
     * 用户邮箱（冗余）
     */
    private String email;

    /**
     * 谷歌生成二维码的地址
     */
    @Column(name = "qr_barcode_url")
    private String QRBarcodeURL;


    /**
     * @return google_id
     */
    public Integer getGoogleId() {
        return googleId;
    }

    /**
     * @param googleId
     */
    public void setGoogleId(Integer googleId) {
        this.googleId = googleId;
    }

    /**
     * 获取secret编号
     *
     * @return secret - secret编号
     */
    public String getSecret() {
        return secret;
    }

    /**
     * 设置secret编号
     *
     * @param secret secret编号
     */
    public void setSecret(String secret) {
        this.secret = secret;
    }

    /**
     * 获取用户编号
     *
     * @return member_id - 用户编号
     */
    public Integer getMemberId() {
        return memberId;
    }

    /**
     * 设置用户编号
     *
     * @param memberId 用户编号
     */
    public void setMemberId(Integer memberId) {
        this.memberId = memberId;
    }

    /**
     * 获取1开始,0关闭
     *
     * @return login_lock - 1开始,0关闭
     */
    public Integer getLoginLock() {
        return loginLock;
    }

    /**
     * 设置1开始,0关闭
     *
     * @param loginLock 1开始,0关闭
     */
    public void setLoginLock(Integer loginLock) {
        this.loginLock = loginLock;
    }

    /**
     * 获取1开始,0关闭
     *
     * @return order_lock - 1开始,0关闭
     */
    public Integer getOrderLock() {
        return orderLock;
    }

    /**
     * 设置1开始,0关闭
     *
     * @param orderLock 1开始,0关闭
     */
    public void setOrderLock(Integer orderLock) {
        this.orderLock = orderLock;
    }

    /**
     * 获取1开始,0关闭
     *
     * @return money_lock - 1开始,0关闭
     */
    public Integer getMoneyLock() {
        return moneyLock;
    }

    /**
     * 设置1开始,0关闭
     *
     * @param moneyLock 1开始,0关闭
     */
    public void setMoneyLock(Integer moneyLock) {
        this.moneyLock = moneyLock;
    }

    /**
     * 获取用户邮箱（冗余）
     *
     * @return email - 用户邮箱（冗余）
     */
    public String getEmail() {
        return email;
    }

    /**
     * 设置用户邮箱（冗余）
     *
     * @param email 用户邮箱（冗余）
     */
    public void setEmail(String email) {
        this.email = email;
    }

    public String getQRBarcodeURL() {
        return QRBarcodeURL;
    }

    public void setQRBarcodeURL(String QRBarcodeURL) {
        this.QRBarcodeURL = QRBarcodeURL;
    }
}