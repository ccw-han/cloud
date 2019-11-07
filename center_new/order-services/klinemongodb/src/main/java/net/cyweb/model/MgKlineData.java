package net.cyweb.model;

import org.springframework.data.mongodb.core.mapping.Document;


@Document
public class MgKlineData {
    //时间轴刻度
    private Long scaleKey;

    //刻度内交易记录总量
    private Long count;

    //时间刻度
    private Long groupTime;

    //交易时间
    private Long tradeTime;

    //最大价格
    private Double maxPrice;

    //最小价格
    private Double minPrice;

    //总量
    private Double num;

    //开盘价
    private Double openPrice;

    //闭盘价格
    private Double closePrice;

    //当前入库的交易时间
    private Long nowTradeTime;

    //最早交易时间
    private Long lastTradeTime;

    //币种ID
    private Integer currencyId;
    //市场ID
    private Integer currencyTradeId;




    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public Long getGroupTime() {
        return groupTime;
    }

    public void setGroupTime(Long groupTime) {
        this.groupTime = groupTime;
    }

    public Long getTradeTime() {
        return tradeTime;
    }

    public void setTradeTime(Long tradeTime) {
        this.tradeTime = tradeTime;
    }

    public Double getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(Double maxPrice) {
        this.maxPrice = maxPrice;
    }

    public Double getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(Double minPrice) {
        this.minPrice = minPrice;
    }

    public Double getNum() {
        return num;
    }

    public void setNum(Double num) {
        this.num = num;
    }

    public Double getOpenPrice() {
        return openPrice;
    }

    public void setOpenPrice(Double openPrice) {
        this.openPrice = openPrice;
    }

    public Double getClosePrice() {
        return closePrice;
    }

    public void setClosePrice(Double closePrice) {
        this.closePrice = closePrice;
    }

    public Long getScaleKey() {
        return scaleKey;
    }

    public void setScaleKey(Long scaleKey) {
        this.scaleKey = scaleKey;
    }

    public Long getNowTradeTime() {
        return nowTradeTime;
    }

    public void setNowTradeTime(Long nowTradeTime) {
        this.nowTradeTime = nowTradeTime;
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

    public Long getLastTradeTime() {
        return lastTradeTime;
    }

    public void setLastTradeTime(Long lastTradeTime) {
        this.lastTradeTime = lastTradeTime;
    }


}
