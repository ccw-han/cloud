package net.cyweb.model;

import javax.persistence.*;

@Table(name = "yang_card_num_thaw_log")
public class YangCardNumThawLog extends BaseEntity {
    @Id
    private Integer id;

    @Column(name = "memberId")
    private Integer memberid;

    @Column(name = "addTIme")
    private Integer addtime;

    private Long nums;

    /**
     * 用于保证每天只会发一次 
     */
    private String date;

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

    /**
     * @return addTIme
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
     * @return nums
     */
    public Long getNums() {
        return nums;
    }

    /**
     * @param nums
     */
    public void setNums(Long nums) {
        this.nums = nums;
    }

    /**
     * 获取用于保证每天只会发一次 
     *
     * @return date - 用于保证每天只会发一次 
     */
    public String getDate() {
        return date;
    }

    /**
     * 设置用于保证每天只会发一次 
     *
     * @param date 用于保证每天只会发一次 
     */
    public void setDate(String date) {
        this.date = date;
    }
}