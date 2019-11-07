package net.cyweb.model;

import javax.persistence.*;

@Table(name="yang_bank")
public class YangBank extends BaseEntity{
    @Id
    @Column(name="id")
    @GeneratedValue(generator = "JDBC")//此处加上注解
    private Integer id;
    @Column(name="bankname")
    private String bankname;
    @Column(name="uid")
    private Integer uid;
    @Column(name="cardname")
    private String cardname;
    @Column(name="address")
    private String address;
    @Column(name="cardnum")
    private String cardnum;
    @Column(name="status")
    private Integer status;
    @Column(name="bank_branch")
    private String bankBranch;
    @Transient
    private String city;
    @Transient
    private String province;

    @Column(name="type")
    private  String type;
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getBankname() {
        return bankname;
    }

    public void setBankname(String bankname) {
        this.bankname = bankname;
    }

    public Integer getUid() {
        return uid;
    }

    public void setUid(Integer uid) {
        this.uid = uid;
    }

    public String getCardname() {
        return cardname;
    }

    public void setCardname(String cardname) {
        this.cardname = cardname;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCardnum() {
        return cardnum;
    }

    public void setCardnum(String cardnum) {
        this.cardnum = cardnum;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getBankBranch() {
        return bankBranch;
    }

    public void setBanlBranch(String bankBranch) {
        this.bankBranch = bankBranch;
    }

    public void setBankBranch(String bankBranch) {
        this.bankBranch = bankBranch;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }
}
