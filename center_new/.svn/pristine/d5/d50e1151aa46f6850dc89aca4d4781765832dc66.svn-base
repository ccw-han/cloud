package net.cyweb.model;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;

@Table(name="yang_c2c_asset")
public class YangC2CAsset extends BaseEntity{
    @Id
    @Column(name = "id")
    private Integer id;
    @Column(name = "currency_id")
    private Integer currencyId;
    @Column(name = "num")
    private BigDecimal num;
    @Column(name = "forzen_num")
    private BigDecimal forzenNum;
    @Column(name = "member_id")
    private Integer memberId;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getCurrencyId() {
        return currencyId;
    }

    public void setCurrencyId(Integer currencyId) {
        this.currencyId = currencyId;
    }

    public BigDecimal getNum() {
        return num;
    }

    public void setNum(BigDecimal num) {
        this.num = num;
    }

    public BigDecimal getForzenNum() {
        return forzenNum;
    }

    public void setForzenNum(BigDecimal forzenNum) {
        this.forzenNum = forzenNum;
    }

    public Integer getMemberId() {
        return memberId;
    }

    public void setMemberId(Integer memberId) {
        this.memberId = memberId;
    }
}
