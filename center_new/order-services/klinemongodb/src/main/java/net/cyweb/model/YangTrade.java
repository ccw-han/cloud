package net.cyweb.model;

import lombok.Data;

import javax.persistence.Table;
import javax.persistence.Transient;
import java.math.BigDecimal;

@Table(name = "yang_trade")
@Data
public class YangTrade extends BaseEntity{

    /**
     * 交易表 交易表的id
     */

    private Integer tradeId;

    /**
     * 订单号
     */
    private String tradeNo;

    /**
     * 买家uid即member_id
     */
    private Integer memberId;

    private Integer orderId;

    /**
     * 货币id
     */
    private Integer currencyId;

    private Integer currencyTradeId;

    /**
     * 价格
     */
    private BigDecimal price;

    /**
     * 数量
     */
    private BigDecimal num;

    private BigDecimal money;

    /**
     * 手续费
     */
    private BigDecimal fee;

    /**
     * buy 或sell
     */
    private String type;

    /**
     * 成交时间 （添加表的时间）
     */
    private Long addTime;

    private Byte status;

    /**
     * 默认不显示 1我主动方显示
     */
    private Integer show;


    @Transient
    private String currencyMark;

    @Transient
    private String currencyTitle;

    @Transient
    private Byte isShua;

    @Transient
    private String memberName;


    public Integer getTradeId() {
        return tradeId;
    }

    public void setTradeId(Integer tradeId) {
        this.tradeId = tradeId;
    }

    public String getTradeNo() {
        return tradeNo;
    }

    public void setTradeNo(String tradeNo) {
        this.tradeNo = tradeNo;
    }

    public Integer getMemberId() {
        return memberId;
    }

    public void setMemberId(Integer memberId) {
        this.memberId = memberId;
    }

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public Integer getCurrencyId() {
        return currencyId;
    }

    public void setCurrencyId(Integer currencyId) {
        this.currencyId = currencyId;
    }

    public Integer getCurrencyTradeId() {
        return currencyTradeId;
    }

    public void setCurrencyTradeId(Integer currencyTradeId) {
        this.currencyTradeId = currencyTradeId;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getNum() {
        return num;
    }

    public void setNum(BigDecimal num) {
        this.num = num;
    }

    public BigDecimal getMoney() {
        return money;
    }

    public void setMoney(BigDecimal money) {
        this.money = money;
    }

    public BigDecimal getFee() {
        return fee;
    }

    public void setFee(BigDecimal fee) {
        this.fee = fee;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getAddTime() {
        return addTime;
    }

    public void setAddTime(Long addTime) {
        this.addTime = addTime;
    }

    public Byte getStatus() {
        return status;
    }

    public void setStatus(Byte status) {
        this.status = status;
    }

    public Integer getShow() {
        return show;
    }

    public void setShow(Integer show) {
        this.show = show;
    }

    public String getCurrencyMark() {
        return currencyMark;
    }

    public void setCurrencyMark(String currencyMark) {
        this.currencyMark = currencyMark;
    }

    public String getCurrencyTitle() {
        return currencyTitle;
    }

    public void setCurrencyTitle(String currencyTitle) {
        this.currencyTitle = currencyTitle;
    }

    public Byte getIsShua() {
        return isShua;
    }

    public void setIsShua(Byte isShua) {
        this.isShua = isShua;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }
}