package net.cyweb.model;

import lombok.Data;
import net.cyweb.validate.OrderQueue;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@Table(name = "yang_orders_robot")
public class YangOrdersRobot extends BaseEntity {
    @Id
    @Column(name = "orders_id")
    @NotNull(message = "订单号不能为空",groups = {OrderQueue.class})
    @Min(value = 0, message = "订单号必须大于0",groups = {OrderQueue.class})
    @Digits(integer = 10, fraction = 0)
    private Integer ordersId;

    @Column(name = "member_id")
    private Integer memberId;

    /**
     * 主币种ID
     */
    @Column(name = "currency_id")
    private Integer currencyId;

    /**
     * 对应交易币种ID
     */
    @Column(name = "currency_trade_id")
    private Integer currencyTradeId;


    /**
     * buy sell
     */
    @Column(name = "`type`")
    private String type;

    private BigDecimal price;

    /**
     * 挂单数量
     */
    private BigDecimal num;

    /**
     * 成交数量
     */
    @Column(name = "trade_num")
    private BigDecimal tradeNum;

    /**
     * 记录的是比例
     */
    private BigDecimal fee;



    @Column(name = "add_time")
    private Integer addTime;

    /**
     * 成交时间
     */
    @Column(name = "trade_time")
    private Long tradeTime;

    /**
     * 0是挂单，1是部分成交,2成交， -1撤销
     */
    private Byte status;

    @Column(name = "is_shua")
    private Byte isShua;



    @Column(name="hasDo")
    private Integer hasDo;




}