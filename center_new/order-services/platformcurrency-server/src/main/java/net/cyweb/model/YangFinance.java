package net.cyweb.model;

import lombok.Data;

import java.math.BigDecimal;
import javax.persistence.*;

@Table(name = "yang_finance")
@Data
public class YangFinance extends BaseEntity {
    /**
     * 财务日志表
     */
    @Id
    @Column(name = "finance_id")
    private Integer financeId;

    /**
     * 用户id
     */
    @Column(name = "member_id")
    private Integer memberId;

    /**
     * 类型
     */
    private Byte type;

    /**
     * 收入=1/支出=2
     */
    @Column(name = "money_type")
    private Byte moneyType;

    /**
     * 价格
     */
    private BigDecimal money;

    /**
     * 添加时间
     */
    @Column(name = "add_time")
    private Long addTime;

    /**
     * 币种
     */
    @Column(name = "currency_id")
    private Integer currencyId;

    private String ip;

    /**
     * 内容
     */
    private String content;


}