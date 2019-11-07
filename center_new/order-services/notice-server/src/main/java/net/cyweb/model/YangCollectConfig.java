package net.cyweb.model;

import lombok.Data;

import java.math.BigDecimal;
import javax.persistence.*;

@Table(name = "yang_collect_config")
@Data
public class YangCollectConfig extends BaseEntity {
    @Id
    private Integer id;

    @Column(name = "cId")
    private Integer cid;

    /**
     * 采集类别 1 火币 2 非小号
     */
    @Column(name = "collect_type")
    private Integer collectType;

    /**
     * 采集所需要的额外信息
     */
    @Column(name = "extData")
    private String extdata;

    /**
     * 状态 0 关闭 1 开启
     */
    private Integer status;

    private BigDecimal price;

    @Column(name = "safe_price_min")
    private BigDecimal safePriceMin;

    @Column(name = "safe_price_max")
    private BigDecimal safePriceMax;

    @Column(name="userDefinedBl")
    private BigDecimal userDefinedBl;

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
     * @return cId
     */
    public Integer getCid() {
        return cid;
    }

    /**
     * @param cid
     */
    public void setCid(Integer cid) {
        this.cid = cid;
    }

    /**
     * 获取采集类别 1 火币 2 非小号
     *
     * @return collect_type - 采集类别 1 火币 2 非小号
     */
    public Integer getCollectType() {
        return collectType;
    }

    /**
     * 设置采集类别 1 火币 2 非小号
     *
     * @param collectType 采集类别 1 火币 2 非小号
     */
    public void setCollectType(Integer collectType) {
        this.collectType = collectType;
    }

    /**
     * 获取采集所需要的额外信息
     *
     * @return extData - 采集所需要的额外信息
     */
    public String getExtdata() {
        return extdata;
    }

    /**
     * 设置采集所需要的额外信息
     *
     * @param extdata 采集所需要的额外信息
     */
    public void setExtdata(String extdata) {
        this.extdata = extdata;
    }

    /**
     * 获取状态 0 关闭 1 开启
     *
     * @return status - 状态 0 关闭 1 开启
     */
    public Integer getStatus() {
        return status;
    }

    /**
     * 设置状态 0 关闭 1 开启
     *
     * @param status 状态 0 关闭 1 开启
     */
    public void setStatus(Integer status) {
        this.status = status;
    }

    /**
     * @return price
     */
    public BigDecimal getPrice() {
        return price;
    }

    /**
     * @param price
     */
    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    /**
     * @return safe_price_min
     */
    public BigDecimal getSafePriceMin() {
        return safePriceMin;
    }

    /**
     * @param safePriceMin
     */
    public void setSafePriceMin(BigDecimal safePriceMin) {
        this.safePriceMin = safePriceMin;
    }

    /**
     * @return safe_price_max
     */
    public BigDecimal getSafePriceMax() {
        return safePriceMax;
    }

    /**
     * @param safePriceMax
     */
    public void setSafePriceMax(BigDecimal safePriceMax) {
        this.safePriceMax = safePriceMax;
    }
}