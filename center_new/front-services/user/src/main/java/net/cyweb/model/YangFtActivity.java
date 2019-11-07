package net.cyweb.model;

import java.math.BigDecimal;
import javax.persistence.*;

@Table(name = "yang_ft_activity")
public class YangFtActivity extends BaseEntity {
    @Id
    private Integer id;

    /**
     * 金额
     */
    private BigDecimal num;

    /**
     * 类别 1是注册送的
     */
    private Integer type;

    @Column(name = "member_id")
    private Integer memberId;

    @Column(name = "add_time")
    private Integer addTime;

    /**
     * @return id
     */
    public Integer getId() {
        return id;
    }

    /**
     * @param id
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * 获取金额
     *
     * @return num - 金额
     */
    public BigDecimal getNum() {
        return num;
    }

    /**
     * 设置金额
     *
     * @param num 金额
     */
    public void setNum(BigDecimal num) {
        this.num = num;
    }

    /**
     * 获取类别 1是注册送的
     *
     * @return type - 类别 1是注册送的
     */
    public Integer getType() {
        return type;
    }

    /**
     * 设置类别 1是注册送的
     *
     * @param type 类别 1是注册送的
     */
    public void setType(Integer type) {
        this.type = type;
    }

    /**
     * @return member_id
     */
    public Integer getMemberId() {
        return memberId;
    }

    /**
     * @param memberId
     */
    public void setMemberId(Integer memberId) {
        this.memberId = memberId;
    }

    /**
     * @return add_time
     */
    public Integer getAddTime() {
        return addTime;
    }

    /**
     * @param addTime
     */
    public void setAddTime(Integer addTime) {
        this.addTime = addTime;
    }
}