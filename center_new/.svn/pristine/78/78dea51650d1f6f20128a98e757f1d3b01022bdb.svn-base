package net.cyweb.model;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;

@Table(name = "yang_ft_lock")
public class YangFtLock extends BaseEntity{
    @Id
    private Integer id;

    private BigDecimal num;

    @Column(name = "forzen_num")
    private BigDecimal forzenNum;

    @Column(name = "member_id")
    private Integer memberId;

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
     * @return num
     */
    public BigDecimal getNum() {
        return num;
    }

    /**
     * @param num
     */
    public void setNum(BigDecimal num) {
        this.num = num;
    }

    /**
     * @return forzen_num
     */
    public BigDecimal getForzenNum() {
        return forzenNum;
    }

    /**
     * @param forzenNum
     */
    public void setForzenNum(BigDecimal forzenNum) {
        this.forzenNum = forzenNum;
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
}