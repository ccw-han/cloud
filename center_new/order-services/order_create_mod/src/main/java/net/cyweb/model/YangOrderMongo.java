package net.cyweb.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class YangOrderMongo {
    private Integer orderId;
    private BigDecimal total_num;
    private BigDecimal trade_num;
    private BigDecimal price;
    private BigDecimal left_num;
    private BigDecimal left_rate;
    private BigDecimal left_money;

    public BigDecimal getLeft_num() {
        return total_num.subtract(trade_num);
    }

    public BigDecimal getLeft_rate() {
        return getLeft_num().divide(total_num);
    }

    public BigDecimal getLeft_money() {
        return getLeft_num().multiply(price);
    }
}
