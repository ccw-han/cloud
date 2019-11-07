package net.cyweb.model;

        import lombok.Data;
        import net.cyweb.validate.CurrencyGetAddress;
        import net.cyweb.validate.SpecAssets;
        import net.cyweb.validate.UpdateAssets;

        import java.math.BigDecimal;
        import javax.persistence.*;
        import javax.validation.constraints.NotNull;
        import javax.validation.constraints.Pattern;

@Table(name = "yang_currency_user")
@Data
public class YangCurrencyUser extends BaseEntity {
    @Id
    @Column(name = "cu_id")
    private Integer cuId;

    /**
     * 用户id
     */
    @Column(name = "member_id")
    private Integer memberId;

    /**
     * 货币id
     */
    @Column(name = "currency_id")
    @NotNull(message = "货币ID不能为空",groups = {SpecAssets.class,UpdateAssets.class,CurrencyGetAddress.class})
    private Integer currencyId;

    /**
     * 数量
     */
    @NotNull(message = "资金不能为空",groups = {UpdateAssets.class})
    private BigDecimal num;

    /**
     * 冻结数量
     */
    @Column(name = "forzen_num")
    @NotNull(message = "冻结资金不能为空",groups = {UpdateAssets.class})
    private BigDecimal forzenNum;

    @Column(name = "`status`")
    private Byte status;

    /**
     * 钱包充值地址
     */
    @Column(name = "chongzhi_url")
    private String chongzhiUrl;

    /**
     * 1显示持仓排行,0隐藏持仓排行
     */
    @Column(name = "`show`")
    private Integer show;

    /*变更用户资产时，增或减*/
    @Transient
    @NotNull(message = "numType不能为空",groups = {UpdateAssets.class})
    @Pattern(regexp = "inc|dec",message = "numType类型只允许inc | dec",groups = {UpdateAssets.class})
    public String numType;


    /*变更用户资产时，增或减*/
    @Transient
    @NotNull(message = "forzenNumType不能为空",groups = {UpdateAssets.class})
    @Pattern(regexp = "inc|dec",message = "forzenNumType类型只允许inc | dec",groups = {UpdateAssets.class})
    public String forzenNumType;

    @Transient
    private String currencyMark;
//    /**
//     * @return cu_id
//     */
//    public Integer getCuId() {
//        return cuId;
//    }
//
//    /**
//     * @param cuId
//     */
//    public void setCuId(Integer cuId) {
//        this.cuId = cuId;
//    }
//
//    /**
//     * 获取用户id
//     *
//     * @return member_id - 用户id
//     */
//    public Integer getMemberId() {
//        return memberId;
//    }
//
//    /**
//     * 设置用户id
//     *
//     * @param memberId 用户id
//     */
//    public void setMemberId(Integer memberId) {
//        this.memberId = memberId;
//    }
//
//    /**
//     * 获取货币id
//     *
//     * @return currency_id - 货币id
//     */
//    public Integer getCurrencyId() {
//        return currencyId;
//    }
//
//    /**
//     * 设置货币id
//     *
//     * @param currencyId 货币id
//     */
//    public void setCurrencyId(Integer currencyId) {
//        this.currencyId = currencyId;
//    }
//
//    /**
//     * 获取数量
//     *
//     * @return num - 数量
//     */
//    public BigDecimal getNum() {
//        return num;
//    }
//
//    /**
//     * 设置数量
//     *
//     * @param num 数量
//     */
//    public void setNum(BigDecimal num) {
//        this.num = num;
//    }
//
//    /**
//     * 获取冻结数量
//     *
//     * @return forzen_num - 冻结数量
//     */
//    public BigDecimal getForzenNum() {
//        return forzenNum;
//    }
//
//    /**
//     * 设置冻结数量
//     *
//     * @param forzenNum 冻结数量
//     */
//    public void setForzenNum(BigDecimal forzenNum) {
//        this.forzenNum = forzenNum;
//    }
//
//    /**
//     * @return status
//     */
//    public Byte getStatus() {
//        return status;
//    }
//
//    /**
//     * @param status
//     */
//    public void setStatus(Byte status) {
//        this.status = status;
//    }
//
//    /**
//     * 获取钱包充值地址
//     *
//     * @return chongzhi_url - 钱包充值地址
//     */
//    public String getChongzhiUrl() {
//        return chongzhiUrl;
//    }
//
//    /**
//     * 设置钱包充值地址
//     *
//     * @param chongzhiUrl 钱包充值地址
//     */
//    public void setChongzhiUrl(String chongzhiUrl) {
//        this.chongzhiUrl = chongzhiUrl;
//    }
//
//    /**
//     * 获取1显示持仓排行,0隐藏持仓排行
//     *
//     * @return show - 1显示持仓排行,0隐藏持仓排行
//     */
//    public Integer getShow() {
//        return show;
//    }
//
//    /**
//     * 设置1显示持仓排行,0隐藏持仓排行
//     *
//     * @param show 1显示持仓排行,0隐藏持仓排行
//     */
//    public void setShow(Integer show) {
//        this.show = show;
//    }


}