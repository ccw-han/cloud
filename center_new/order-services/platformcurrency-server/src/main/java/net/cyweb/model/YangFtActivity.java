package net.cyweb.model;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;

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

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public BigDecimal getNum() {
        return num;
    }

    public void setNum(BigDecimal num) {
        this.num = num;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getMemberId() {
        return memberId;
    }

    public void setMemberId(Integer memberId) {
        this.memberId = memberId;
    }

    public Integer getAddTime() {
        return addTime;
    }

    public void setAddTime(Integer addTime) {
        this.addTime = addTime;
    }
}
