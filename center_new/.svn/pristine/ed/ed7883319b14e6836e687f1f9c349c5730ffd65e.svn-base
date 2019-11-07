package net.cyweb.model;

import java.math.BigDecimal;
import javax.persistence.*;

@Table(name = "yang_card_num")
public class YangCardNum extends BaseEntity {
    @Id
    private Integer id;

    /**
     * FT数量
     */
    private BigDecimal num;

    /**
     * 解锁数量
     */
    @Column(name = "dealNum")
    private BigDecimal dealnum;

    /**
     * 创建时间
     */
    @Column(name = "addTime")
    private Integer addtime;

    /**
     * 用户id
     */
    @Column(name = "memberId")
    private Integer memberid;

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
     * 获取FT数量
     *
     * @return num - FT数量
     */
    public BigDecimal getNum() {
        return num;
    }

    /**
     * 设置FT数量
     *
     * @param num FT数量
     */
    public void setNum(BigDecimal num) {
        this.num = num;
    }

    /**
     * 获取解锁数量
     *
     * @return dealNum - 解锁数量
     */
    public BigDecimal getDealnum() {
        return dealnum;
    }

    /**
     * 设置解锁数量
     *
     * @param dealnum 解锁数量
     */
    public void setDealnum(BigDecimal dealnum) {
        this.dealnum = dealnum;
    }

    /**
     * 获取创建时间
     *
     * @return addTime - 创建时间
     */
    public Integer getAddtime() {
        return addtime;
    }

    /**
     * 设置创建时间
     *
     * @param addtime 创建时间
     */
    public void setAddtime(Integer addtime) {
        this.addtime = addtime;
    }

    /**
     * 获取用户id
     *
     * @return memberId - 用户id
     */
    public Integer getMemberid() {
        return memberid;
    }

    /**
     * 设置用户id
     *
     * @param memberid 用户id
     */
    public void setMemberid(Integer memberid) {
        this.memberid = memberid;
    }
}