package net.cyweb.model;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name="yang_currency_market")
public class YangCurrencyMarket extends BaseEntity{
    @Id
    @Column(name="id")
    private Integer id;
    @Column(name="title")
    private String title;
    @Column(name="`show`")
    private Integer show;
    @Column(name="order_num")
    private Integer orderNum;
    @Column(name="currencyid")
    private Integer currencyId;
    @Column(name="logo")
    private String logo;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getShow() {
        return show;
    }

    public void setShow(Integer show) {
        this.show = show;
    }

    public Integer getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(Integer orderNum) {
        this.orderNum = orderNum;
    }

    public Integer getCurrencyId() {
        return currencyId;
    }

    public void setCurrencyId(Integer currencyId) {
        this.currencyId = currencyId;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }
}
