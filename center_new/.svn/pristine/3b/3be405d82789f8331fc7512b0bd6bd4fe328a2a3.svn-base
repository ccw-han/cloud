package net.cyweb.model;

import javax.persistence.*;

@Table(name = "yang_trade_fee")
public class YangTradeFee extends BaseEntity {
    @Id
    @Column(name = "trade_fee_id")
    private Integer tradeFeeId;

    /**
     * 币种id
     */
    @Column(name = "cy_id")
    private Integer cyId;

    /**
     * 用户编号
     */
    @Column(name = "member_id")
    private Integer memberId;

    /**
     * 创建时间
     */
    private Integer ctime;

    /**
     * 买入费率
     */
    @Column(name = "currency_buy_fee")
    private Float currencyBuyFee;

    /**
     * 卖出手续费
     */
    @Column(name = "currency_sell_fee")
    private Float currencySellFee;

    /**
     * @return trade_fee_id
     */
    public Integer getTradeFeeId() {
        return tradeFeeId;
    }

    /**
     * @param tradeFeeId
     */
    public void setTradeFeeId(Integer tradeFeeId) {
        this.tradeFeeId = tradeFeeId;
    }

    /**
     * 获取币种id
     *
     * @return cy_id - 币种id
     */
    public Integer getCyId() {
        return cyId;
    }

    /**
     * 设置币种id
     *
     * @param cyId 币种id
     */
    public void setCyId(Integer cyId) {
        this.cyId = cyId;
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
     * 获取创建时间
     *
     * @return ctime - 创建时间
     */
    public Integer getCtime() {
        return ctime;
    }

    /**
     * 设置创建时间
     *
     * @param ctime 创建时间
     */
    public void setCtime(Integer ctime) {
        this.ctime = ctime;
    }

    /**
     * 获取买入费率
     *
     * @return currency_buy_fee - 买入费率
     */
    public Float getCurrencyBuyFee() {
        return currencyBuyFee;
    }

    /**
     * 设置买入费率
     *
     * @param currencyBuyFee 买入费率
     */
    public void setCurrencyBuyFee(Float currencyBuyFee) {
        this.currencyBuyFee = currencyBuyFee;
    }

    /**
     * 获取卖出手续费
     *
     * @return currency_sell_fee - 卖出手续费
     */
    public Float getCurrencySellFee() {
        return currencySellFee;
    }

    /**
     * 设置卖出手续费
     *
     * @param currencySellFee 卖出手续费
     */
    public void setCurrencySellFee(Float currencySellFee) {
        this.currencySellFee = currencySellFee;
    }
}