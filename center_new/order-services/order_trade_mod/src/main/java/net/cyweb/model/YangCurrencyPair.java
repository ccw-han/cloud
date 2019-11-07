package net.cyweb.model;

import lombok.Data;

import java.math.BigDecimal;
import javax.persistence.*;

@Table(name = "yang_currency_pair")
@Data
public class YangCurrencyPair extends BaseEntity {
    @Id
    @Column(name = "cy_id")
    private Integer cyId;

    /**
     * 币种名称
     */
    @Column(name = "currency_mark")
    private String currencyMark;

    /**
     * 订单买入币种id
     */
    @Column(name = "currency_trade_id")
    private Integer currencyTradeId;

    /**
     * 主交易币种ID
     */
    @Column(name = "currency_id")
    private Integer currencyId;

    /**
     * 所在交易区
     */
    @Column(name = "currency_group")
    private Integer currencyGroup;

    /**
     * 买入手续费
     */
    @Column(name = "currency_buy_fee")
    private BigDecimal currencyBuyFee;

    /**
     * 卖出手续费
     */
    @Column(name = "currency_sell_fee")
    private BigDecimal currencySellFee;

    /**
     * 更新时间
     */
    @Column(name = "up_time")
    private Integer upTime;

    /**
     * 排序编号
     */
    @Column(name = "order_num")
    private Integer orderNum;

    /**
     * 价格显示位数
     */
    @Column(name = "input_price_num")
    private Integer inputPriceNum;

    /**
     * 数量显示小数位数
     */
    @Column(name = "show_num")
    private Integer showNum;

    /**
     * 总金额保留小数位
     */
    @Column(name = "all_money_num")
    private Integer allMoneyNum;

    /**
     * 转换为相应汇率的价格后保留小数位数
     */
    @Column(name = "rate_num")
    private Integer rateNum;

    /**
     * 最小委托金额
     */
    @Column(name = "min_money")
    private Integer minMoney;

    /**
     * 最小刷单数量
     */
    @Column(name = "min_buy_shua")
    private String minBuyShua;

    /**
     * 最大刷单数量
     */
    @Column(name = "max_buy_shua")
    private String maxBuyShua;


    /**
     * 刷单基本价
     */
    @Column(name = "base_price")
    private BigDecimal basePrice;


    /**
     * 趋势是涨还是跌 按照3分支2的大概率来 1涨 -1 跌
     */
    @Column(name = "upOrDown")
    private Integer upOrDown;

    /**
     * 操盘机器人id
     */
    @Column(name = "robotId")
    private Integer robotId;


    /**
     * 范围1 最小值
     */
    @Column(name = "min1")
    private Double min1;

    /**
     * 范围1 最大值
     */
    @Column(name = "max1")
    private Double max1;

    /**
     * 概率1
     */
    @Column(name = "probability1")
    private Double probability1;


    /**
     * 范围1 最小值
     */
    @Column(name = "min2")
    private Double min2;


    /**
     * 范围1 最大值
     */
    @Column(name = "max2")
    private Double max2;

    /**
     * 概率1
     */
    @Column(name = "probability2")
    private Integer probability2;


    @Column(name = "shuaBl") //每个价格之间的比例不超过
    private Double shuaBl;


    @Column(name = "shuaBlMin") //每个价格之间的比例不超过
    private Double shuaBlMin;


    /**
     * 范围1 最小值
     */
    @Column(name = "min3")
    private Double min3;

    /**
     * 范围1 最大值
     */
    @Column(name = "max3")
    private Double max3;

    /**
     * 概率1
     */
    @Column(name = "probability3")
    private Double probability3;


    @Column(name = "max_price")
    private BigDecimal maxPrice;

    @Column(name = "min_price")
    private BigDecimal minPrice;

    /**
     * 是否在创新区
     */
    @Column(name = "is_chuang")
    private Byte isChuang;

    /**
     * 删除标记
     */
    @Column(name = "del_flag")
    private String delFlag;


    @Column(name="is_mid_robot")
    private Integer isMidRobot;

    @Column(name="create_rate")
    private BigDecimal createRate;

    @Column(name="gua_create_rate")
    private BigDecimal guaCreateRate;

    @Column(name="extend_id")
    private Integer extendId;

    @Transient
    private YangCurrencyPairExtendMidOrderJSON yangCurrencyPairExtendMidOrder;

    /**
     * @return cy_id
     */
    public Integer getCyId() {
        return cyId;
    }

    /**
     * @param cyId
     */
    public void setCyId(Integer cyId) {
        this.cyId = cyId;
    }

    /**
     * 获取币种名称
     *
     * @return currency_mark - 币种名称
     */
    public String getCurrencyMark() {
        return currencyMark;
    }

    /**
     * 设置币种名称
     *
     * @param currencyMark 币种名称
     */
    public void setCurrencyMark(String currencyMark) {
        this.currencyMark = currencyMark;
    }

    /**
     * 获取订单买入币种id
     *
     * @return currency_trade_id - 订单买入币种id
     */
    public Integer getCurrencyTradeId() {
        return currencyTradeId;
    }

    /**
     * 设置订单买入币种id
     *
     * @param currencyTradeId 订单买入币种id
     */
    public void setCurrencyTradeId(Integer currencyTradeId) {
        this.currencyTradeId = currencyTradeId;
    }

    /**
     * 获取主交易币种ID
     *
     * @return currency_id - 主交易币种ID
     */
    public Integer getCurrencyId() {
        return currencyId;
    }

    /**
     * 设置主交易币种ID
     *
     * @param currencyId 主交易币种ID
     */
    public void setCurrencyId(Integer currencyId) {
        this.currencyId = currencyId;
    }

    /**
     * 获取所在交易区
     *
     * @return currency_group - 所在交易区
     */
    public Integer getCurrencyGroup() {
        return currencyGroup;
    }

    /**
     * 设置所在交易区
     *
     * @param currencyGroup 所在交易区
     */
    public void setCurrencyGroup(Integer currencyGroup) {
        this.currencyGroup = currencyGroup;
    }

    /**
     * 获取买入手续费
     *
     * @return currency_buy_fee - 买入手续费
     */
    public BigDecimal getCurrencyBuyFee() {
        return currencyBuyFee;
    }

    /**
     * 设置买入手续费
     *
     * @param currencyBuyFee 买入手续费
     */
    public void setCurrencyBuyFee(BigDecimal currencyBuyFee) {
        this.currencyBuyFee = currencyBuyFee;
    }

    /**
     * 获取卖出手续费
     *
     * @return currency_sell_fee - 卖出手续费
     */
    public BigDecimal getCurrencySellFee() {
        return currencySellFee;
    }

    /**
     * 设置卖出手续费
     *
     * @param currencySellFee 卖出手续费
     */
    public void setCurrencySellFee(BigDecimal currencySellFee) {
        this.currencySellFee = currencySellFee;
    }

    /**
     * 获取更新时间
     *
     * @return up_time - 更新时间
     */
    public Integer getUpTime() {
        return upTime;
    }

    /**
     * 设置更新时间
     *
     * @param upTime 更新时间
     */
    public void setUpTime(Integer upTime) {
        this.upTime = upTime;
    }

    /**
     * 获取排序编号
     *
     * @return order_num - 排序编号
     */
    public Integer getOrderNum() {
        return orderNum;
    }

    /**
     * 设置排序编号
     *
     * @param orderNum 排序编号
     */
    public void setOrderNum(Integer orderNum) {
        this.orderNum = orderNum;
    }

    /**
     * 获取价格显示位数
     *
     * @return input_price_num - 价格显示位数
     */
    public Integer getInputPriceNum() {
        return inputPriceNum;
    }

    /**
     * 设置价格显示位数
     *
     * @param inputPriceNum 价格显示位数
     */
    public void setInputPriceNum(Integer inputPriceNum) {
        this.inputPriceNum = inputPriceNum;
    }

    /**
     * 获取数量显示小数位数
     *
     * @return show_num - 数量显示小数位数
     */
    public Integer getShowNum() {
        return showNum;
    }

    /**
     * 设置数量显示小数位数
     *
     * @param showNum 数量显示小数位数
     */
    public void setShowNum(Integer showNum) {
        this.showNum = showNum;
    }

    /**
     * 获取总金额保留小数位
     *
     * @return all_money_num - 总金额保留小数位
     */
    public Integer getAllMoneyNum() {
        return allMoneyNum;
    }

    /**
     * 设置总金额保留小数位
     *
     * @param allMoneyNum 总金额保留小数位
     */
    public void setAllMoneyNum(Integer allMoneyNum) {
        this.allMoneyNum = allMoneyNum;
    }

    /**
     * 获取转换为相应汇率的价格后保留小数位数
     *
     * @return rate_num - 转换为相应汇率的价格后保留小数位数
     */
    public Integer getRateNum() {
        return rateNum;
    }

    /**
     * 设置转换为相应汇率的价格后保留小数位数
     *
     * @param rateNum 转换为相应汇率的价格后保留小数位数
     */
    public void setRateNum(Integer rateNum) {
        this.rateNum = rateNum;
    }

    /**
     * 获取最小委托金额
     *
     * @return min_money - 最小委托金额
     */
    public Integer getMinMoney() {
        return minMoney;
    }

    /**
     * 设置最小委托金额
     *
     * @param minMoney 最小委托金额
     */
    public void setMinMoney(Integer minMoney) {
        this.minMoney = minMoney;
    }

    /**
     * 获取最小刷单金额
     *
     * @return min_buy_shua - 最小刷单金额
     */
    public String getMinBuyShua() {
        return minBuyShua;
    }

    /**
     * 设置最小刷单金额
     *
     * @param minBuyShua 最小刷单金额
     */
    public void setMinBuyShua(String minBuyShua) {
        this.minBuyShua = minBuyShua;
    }

    /**
     * 获取最大刷单金额
     *
     * @return max_buy_shua - 最大刷单金额
     */
    public String getMaxBuyShua() {
        return maxBuyShua;
    }

    /**
     * 设置最大刷单金额
     *
     * @param maxBuyShua 最大刷单金额
     */
    public void setMaxBuyShua(String maxBuyShua) {
        this.maxBuyShua = maxBuyShua;
    }

    /**
     * 获取是否在创新区
     *
     * @return is_chuang - 是否在创新区
     */
    public Byte getIsChuang() {
        return isChuang;
    }

    /**
     * 设置是否在创新区
     *
     * @param isChuang 是否在创新区
     */
    public void setIsChuang(Byte isChuang) {
        this.isChuang = isChuang;
    }

    /**
     * 获取删除标记
     *
     * @return del_flag - 删除标记
     */
    public String getDelFlag() {
        return delFlag;
    }

    /**
     * 设置删除标记
     *
     * @param delFlag 删除标记
     */
    public void setDelFlag(String delFlag) {
        this.delFlag = delFlag;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }

    public Integer getUpOrDown() {
        return upOrDown;
    }

    public void setUpOrDown(Integer upOrDown) {
        this.upOrDown = upOrDown;
    }

    public Integer getRobotId() {
        return robotId;
    }

    public void setRobotId(Integer robotId) {
        this.robotId = robotId;
    }

    public Double getMin1() {
        return min1;
    }

    public void setMin1(Double min1) {
        this.min1 = min1;
    }

    public Double getMax1() {
        return max1;
    }

    public void setMax1(Double max1) {
        this.max1 = max1;
    }

    public Double getProbability1() {
        return probability1;
    }

    public void setProbability1(Double probability1) {
        this.probability1 = probability1;
    }

    public Double getMin2() {
        return min2;
    }

    public void setMin2(Double min2) {
        this.min2 = min2;
    }

    public Double getMax2() {
        return max2;
    }

    public void setMax2(Double max2) {
        this.max2 = max2;
    }

    public Integer getProbability2() {
        return probability2;
    }

    public void setProbability2(Integer probability2) {
        this.probability2 = probability2;
    }

    public Double getShuaBl() {
        return shuaBl;
    }

    public void setShuaBl(Double shuaBl) {
        this.shuaBl = shuaBl;
    }

    public Double getMin3() {
        return min3;
    }

    public void setMin3(Double min3) {
        this.min3 = min3;
    }

    public Double getMax3() {
        return max3;
    }

    public void setMax3(Double max3) {
        this.max3 = max3;
    }

    public Double getProbability3() {
        return probability3;
    }

    public void setProbability3(Double probability3) {
        this.probability3 = probability3;
    }

    public BigDecimal getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(BigDecimal maxPrice) {
        this.maxPrice = maxPrice;
    }

    public BigDecimal getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(BigDecimal minPrice) {
        this.minPrice = minPrice;
    }
}