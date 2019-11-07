package net.cyweb.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class YangTradeMongo {
    private Integer trade_id;
    private BigDecimal price;
    private BigDecimal num;
    private String type;
    private long add_time;
}
