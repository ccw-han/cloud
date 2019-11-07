package net.cyweb.model;

import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;

@Table(name = "yang_mining_info")
@Data
public class YangMiningInfo  {
    @Id
    private Integer id;

    @Column(name = "addTime")
    private Integer addtime;

    @Column(name = "addDate")
    private String adddate;

    private BigDecimal num;

    @Column(name = "memberId")
    private Integer memberid;


    @Column(name = "tradeId")
    private Integer tradeid;

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
     * @return addTime
     */
    public Integer getAddtime() {
        return addtime;
    }

    /**
     * @param addtime
     */
    public void setAddtime(Integer addtime) {
        this.addtime = addtime;
    }

    /**
     * @return addDate
     */
    public String getAdddate() {
        return adddate;
    }

    /**
     * @param adddate
     */
    public void setAdddate(String adddate) {
        this.adddate = adddate;
    }

    public BigDecimal getNum() {
        return num;
    }

    public void setNum(BigDecimal num) {
        this.num = num;
    }

    /**
     * @return memberId
     */
    public Integer getMemberid() {
        return memberid;
    }

    /**
     * @param memberid
     */
    public void setMemberid(Integer memberid) {
        this.memberid = memberid;
    }
}