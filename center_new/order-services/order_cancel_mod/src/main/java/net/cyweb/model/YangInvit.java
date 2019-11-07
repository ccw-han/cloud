package net.cyweb.model;

import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;

@Table(name = "yang_invit")
@Data
public class YangInvit extends BaseEntity {
    @Id
    @Column(name = "invit_id")
    private Integer invitId;

    /**
     * 奖励者用户编号
     */
    @Column(name = "member_id")
    private Integer memberId;

    /**
     * 产生记录者id
     */
    @Column(name = "member_id_bottom")
    private Integer memberIdBottom;

    /**
     * 创建时间
     */
    private Long ctime;

    /**
     * 订单类型
     */
    @Column(name = "order_type")
    private String orderType;

    /**
     * 产生分利的订单编号
     */
    @Column(name = "order_id")
    private Integer orderId;

    /**
     * 总产生手续费
     */
    private BigDecimal cfee;

    /**
     * 返利值
     */
    private BigDecimal rebate;

    /**
     * 返利类型:1币；2账户余额
     */
    private Integer rebatetype;

    /**
     * 0未返点,1已经返点
     */
    private Integer status;

    /**
     * 返点结算时间
     */
    private Integer endtime;

    @Column(name = "currency_id")
    private Integer currencyId;

    @Column(name = "trade_currency_id")
    private Integer tradeCurrencyId;


}