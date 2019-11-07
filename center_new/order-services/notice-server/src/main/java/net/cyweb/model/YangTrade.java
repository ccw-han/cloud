package net.cyweb.model;

import lombok.Data;

import java.math.BigDecimal;
import javax.persistence.*;

@Table(name = "yang_trade")
@Data
public class YangTrade extends BaseEntity {
    /**
     * 交易表 交易表的id
     */
    @Id
    @Column(name = "trade_id")
    private Integer tradeId;

    /**
     * 订单号
     */
    @Column(name = "trade_no")
    private String tradeNo;

    /**
     * 买家uid即member_id
     */
    @Column(name = "member_id")
    private Integer memberId;

    @Column(name = "order_id")
    private Integer orderId;

    /**
     * 货币id
     */
    @Column(name = "currency_id")
    private Integer currencyId;

    @Column(name = "currency_trade_id")
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
    @Column(name = "add_time")
    private Long addTime;

    @Column(name = "`status`")
    private Byte status;

    /**
     * 默认不显示 1我主动方显示
     */
    @Column(name = "`show`")
    private Integer show;


}