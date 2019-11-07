package net.cyweb.model;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;

@Table(name = "yang_card_num")
@Data
public class YangCardNum extends  BaseEntity{
    @Id
    private Integer id;
    @Column(name = "num")
    private BigDecimal num;
    @Column(name = "dealNum")
    private BigDecimal dealNum;
    @Column(name = "addTime")
    private Integer addTime;
    @Column(name = "memberId")
    private Integer memberId;
    @Column(name = "dealTimes")
    private Integer dealTimes;

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

    public BigDecimal getDealNum() {
        return dealNum;
    }

    public void setDealNum(BigDecimal dealNum) {
        this.dealNum = dealNum;
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

    public Integer getDealTimes() {
        return dealTimes;
    }

    public void setDealTimes(Integer dealTimes) {
        this.dealTimes = dealTimes;
    }
}
