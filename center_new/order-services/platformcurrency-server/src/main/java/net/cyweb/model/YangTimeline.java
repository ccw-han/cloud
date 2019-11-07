package net.cyweb.model;

import javax.persistence.*;

@Table(name = "yang_timeline")
public class YangTimeline extends BaseEntity {
    @Id
    private Integer id;

    @Column(name = "member_id")
    private Integer memberId;

    private Byte type;

    private String content;

    @Column(name = "add_time")
    private String addTime;

    /**
     * 币种数量
     */
    @Column(name = "data_json")
    private String dataJson;

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
     * @return type
     */
    public Byte getType() {
        return type;
    }

    /**
     * @param type
     */
    public void setType(Byte type) {
        this.type = type;
    }

    /**
     * @return content
     */
    public String getContent() {
        return content;
    }

    /**
     * @param content
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * @return add_time
     */
    public String getAddTime() {
        return addTime;
    }

    /**
     * @param addTime
     */
    public void setAddTime(String addTime) {
        this.addTime = addTime;
    }

    /**
     * 获取币种数量
     *
     * @return data_json - 币种数量
     */
    public String getDataJson() {
        return dataJson;
    }

    /**
     * 设置币种数量
     *
     * @param dataJson 币种数量
     */
    public void setDataJson(String dataJson) {
        this.dataJson = dataJson;
    }
}