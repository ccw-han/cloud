package net.cyweb.model;

import java.math.BigDecimal;
import javax.persistence.*;

@Table(name = "yang_tibi_new")
public class YangTibiNew extends BaseEntity {
    @Id
    private Integer id;

    @Column(name = "user_id")
    private Integer userId;

    private String myurl;

    private String url;

    private String name;

    @Column(name = "add_time")
    private Integer addTime;

    private BigDecimal num;

    /**
     * 0为提币中 1为提币成功  2为充值中 3位充值成功,4,提币被取消
     */
    private Byte status;

    @Column(name = "ti_id")
    private String tiId;

    private String height;

    @Column(name = "check_time")
    private Integer checkTime;

    @Column(name = "currency_id")
    private Integer currencyId;

    private BigDecimal fee;

    private BigDecimal actual;

    /**
     * 确认数
     */
    private Integer confirmations;

    /**
     * @return id
     */
    public Integer getId() {
        return id;
    }

    /**
     * @param id
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * @return user_id
     */
    public Integer getUserId() {
        return userId;
    }

    /**
     * @param userId
     */
    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    /**
     * @return myurl
     */
    public String getMyurl() {
        return myurl;
    }

    /**
     * @param myurl
     */
    public void setMyurl(String myurl) {
        this.myurl = myurl;
    }

    /**
     * @return url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @param url
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return add_time
     */
    public Integer getAddTime() {
        return addTime;
    }

    /**
     * @param addTime
     */
    public void setAddTime(Integer addTime) {
        this.addTime = addTime;
    }

    /**
     * @return num
     */
    public BigDecimal getNum() {
        return num;
    }

    /**
     * @param num
     */
    public void setNum(BigDecimal num) {
        this.num = num;
    }

    /**
     * 获取0为提币中 1为提币成功  2为充值中 3位充值成功,4,提币被取消
     *
     * @return status - 0为提币中 1为提币成功  2为充值中 3位充值成功,4,提币被取消
     */
    public Byte getStatus() {
        return status;
    }

    /**
     * 设置0为提币中 1为提币成功  2为充值中 3位充值成功,4,提币被取消
     *
     * @param status 0为提币中 1为提币成功  2为充值中 3位充值成功,4,提币被取消
     */
    public void setStatus(Byte status) {
        this.status = status;
    }

    /**
     * @return ti_id
     */
    public String getTiId() {
        return tiId;
    }

    /**
     * @param tiId
     */
    public void setTiId(String tiId) {
        this.tiId = tiId;
    }

    /**
     * @return height
     */
    public String getHeight() {
        return height;
    }

    /**
     * @param height
     */
    public void setHeight(String height) {
        this.height = height;
    }

    /**
     * @return check_time
     */
    public Integer getCheckTime() {
        return checkTime;
    }

    /**
     * @param checkTime
     */
    public void setCheckTime(Integer checkTime) {
        this.checkTime = checkTime;
    }

    /**
     * @return currency_id
     */
    public Integer getCurrencyId() {
        return currencyId;
    }

    /**
     * @param currencyId
     */
    public void setCurrencyId(Integer currencyId) {
        this.currencyId = currencyId;
    }

    /**
     * @return fee
     */
    public BigDecimal getFee() {
        return fee;
    }

    /**
     * @param fee
     */
    public void setFee(BigDecimal fee) {
        this.fee = fee;
    }

    /**
     * @return actual
     */
    public BigDecimal getActual() {
        return actual;
    }

    /**
     * @param actual
     */
    public void setActual(BigDecimal actual) {
        this.actual = actual;
    }

    /**
     * 获取确认数
     *
     * @return confirmations - 确认数
     */
    public Integer getConfirmations() {
        return confirmations;
    }

    /**
     * 设置确认数
     *
     * @param confirmations 确认数
     */
    public void setConfirmations(Integer confirmations) {
        this.confirmations = confirmations;
    }
}