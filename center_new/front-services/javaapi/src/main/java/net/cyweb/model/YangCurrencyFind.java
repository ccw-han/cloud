package net.cyweb.model;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;

@Table(name="yang_currency_find")
public class YangCurrencyFind extends BaseEntity{
    @Id
    @Column(name="id")
    private Integer id;
    @Column(name="num")
    private BigDecimal num;
    @Column(name="currency_id")
    private Integer currencyId;
    @Column(name="address")
    private String address;
    @Column(name="currency_mark")
    private String currencyMark;
    @Column(name="status")
    private Integer status;
    @Column(name="remark")
    private String remark;
    @Column(name="add_time")
    private Integer addTime;
    @Column(name="member_id")
    private Integer memberId;
    @Column(name="email")
    private String email;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public BigDecimal getNum() {
        return num;
    }

    public void setNum(BigDecimal num) {
        this.num = num;
    }

    public Integer getCurrencyId() {
        return currencyId;
    }

    public void setCurrencyId(Integer currencyId) {
        this.currencyId = currencyId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCurrencyMark() {
        return currencyMark;
    }

    public void setCurrencyMark(String currencyMark) {
        this.currencyMark = currencyMark;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Integer getAddTime() {
        return addTime;
    }

    public void setAddTime(Integer addTime) {
        this.addTime = addTime;
    }

    public Integer getMemberId() {
        return memberId;
    }

    public void setMemberId(Integer memberId) {
        this.memberId = memberId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
