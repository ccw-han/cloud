package net.cyweb.model;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * 提现表
 * creater:椰椰
 * time：2019/9/12
 */
@Table(name = "yang_withdraw")
@Data
public class YangWithdraw extends BaseEntity {
    /**
     * 提币记录id
     */
    @Id
    @Column(name = "id")
    private String id;

    /***
     * 用户id
     */
    @Column(name = "member_id")
    private String memberId;

    /**
     * 币种id
     */
    @Column(name = "currency_id")
    private String currencyId;

    /**
     * 币种名
     */
    @Column(name = "currency_mark")
    private String currencyMark;

    /**
     * 提现数量
     */
    @Column(name = "amount")
    private String amount;

    /**
     * 手续费
     */
    @Column(name = "fee")
    private String fee;

    /**
     * 交易上连手续费
     */
    @Column(name = "realfee")
    private String realFee;

    /**
     * 交易上连手续费币种
     */
    @Column(name = "realfee_coin")
    private String realFeeCoin;

    /**
     * 付款金额
     */
    @Column(name = "payment")
    private String payment;

    /**
     * 创建时间
     */
    @Column(name = "create_time")
    private String createTime;

    /**
     * 修改时间
     */
    @Column(name = "update_time")
    private String updateTime;

    /**
     * 系统地址
     */
    @Column(name = "address_from")
    private String addressFrom;

    /**
     * 用户提现地址
     */
    @Column(name = "address_to")
    private String addressTo;

    /**
     * 交易哈希
     */
    @Column(name = "tx_id")
    private String txId;

    /**
     * 确认数
     */
    @Column(name = "confirms")
    private String confirms;

    /**
     * 状态，0待审核，1通过，2不通过，3支付中，4支付失败，5支付成功，6异常
     */
    @Column(name = "status")
    private String status;

}
