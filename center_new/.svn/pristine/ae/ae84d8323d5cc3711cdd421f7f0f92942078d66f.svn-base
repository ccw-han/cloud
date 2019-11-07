package net.cyweb.model;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name="yang_qianbao_address")
public class YangQianBaoAddress extends BaseEntity{
    @Id
    @Column(name="id")
    @GeneratedValue(generator = "JDBC")//此处加上注解
    private Integer id;
    @Column(name="user_id")
    private Integer userId;
    @Column(name="name")
    private String name;
    @Column(name="qianbao_url")
    private String qianbaoUrl;
    @Column(name="status")
    private Integer status;
    @Column(name="add_time")
    private Integer addTime;
    @Column(name="currency_id")
    private Integer currencyId;
    @Column(name="remark")
    private String remark;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getQianbaoUrl() {
        return qianbaoUrl;
    }

    public void setQianbaoUrl(String qianbaoUrl) {
        this.qianbaoUrl = qianbaoUrl;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getAddTime() {
        return addTime;
    }

    public void setAddTime(Integer addTime) {
        this.addTime = addTime;
    }

    public Integer getCurrencyId() {
        return currencyId;
    }

    public void setCurrencyId(Integer currencyId) {
        this.currencyId = currencyId;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
