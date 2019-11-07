package net.cyweb.model;

import javax.persistence.*;
import java.math.BigDecimal;
import java.math.BigInteger;

@Table(name = "yang_currency")
public class YangCurrency extends BaseEntity {
    @Id
    @Column(name = "currency_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer currencyId;

    @Column(name = "currency_namekr")
    private String currencyNamekr;

    /**
     * 货币名称
     */
    @Column(name = "currency_name")
    private String currencyName;

    /**
     * 货币logo
     */
    @Column(name = "currency_logo")
    private String currencyLogo;

    /**
     * 英文标识
     */
    @Column(name = "currency_mark")
    private String currencyMark;

    /**
     * 总市值
     */
    @Column(name = "currency_all_money")
    private BigDecimal currencyAllMoney;

    /**
     * 币总数量
     */
    @Column(name = "currency_all_num")
    private BigDecimal currencyAllNum;

    /**
     * 买入手续费
     */
    @Column(name = "currency_buy_fee")
    private Float currencyBuyFee;

    /**
     * 卖出手续费
     */
    @Column(name = "currency_sell_fee")
    private Float currencySellFee;

    /**
     * 该币种的链接地址
     */
    @Column(name = "currency_url")
    private String currencyUrl;

    /**
     * 可以进行交易的币种
     */
    @Column(name = "trade_currency_id")
    private Integer tradeCurrencyId;

    @Column(name = "is_line")
    private Byte isLine;

    /**
     * 是否交易 0 是交易许可 1是交易不许可
     */
    @Column(name = "is_lock")
    private Byte isLock;

    @Column(name = "port_number")
    private Integer portNumber;

    @Column(name = "add_time")
    private Integer addTime;

    private Byte status;

    /**
     * rpc路径
     */
    @Column(name = "rpc_url")
    private String rpcUrl;

    /**
     * rpc密码
     */
    @Column(name = "rpc_pwd")
    private String rpcPwd;

    /**
     * rpc账号（用户名）
     */
    @Column(name = "rpc_user")
    private String rpcUser;

    /**
     * 最大提币额
     */
    @Column(name = "currency_all_tibi")
    private Integer currencyAllTibi;

    /**
     * 详情跳转链接
     */
    @Column(name = "detail_url")
    private String detailUrl;

    /**
     * 钱包储存路径
     */
    @Column(name = "qianbao_url")
    private String qianbaoUrl;

    /**
     * 钱包密钥
     */
    @Column(name = "qianbao_key")
    private String qianbaoKey;

    /**
     * 涨停
     */
    @Column(name = "price_up")
    private Float priceUp;

    /**
     * 跌停
     */
    @Column(name = "price_down")
    private Float priceDown;





    private Integer sort;

    /**
     * 限制位数
     */
    @Column(name = "currency_digit_num")
    private Integer currencyDigitNum;

    @Column(name = "guanwang_url")
    private String guanwangUrl;

    @Column(name = "tibi_fee")
    private Float tibiFee;

    @Column(name = "tibi_qrs")
    private Integer tibiQrs;

    @Column(name = "tibi_small")
    private Float tibiSmall;

    @Column(name = "chongbi_open")
    private Integer chongbiOpen;

    @Column(name = "tibi_open")
    private Integer tibiOpen;

    @Column(name = "currency_digit_cny")
    private Integer currencyDigitCny;

    @Column(name = "currency_digit_usd")
    private Integer currencyDigitUsd;

    private String txurl;

    /**
     * 当韩元区未上，写对应交易币种的currency_id
     */
    @Column(name = "trade_market")
    private String tradeMarket;

    public BigDecimal getTibiFeeNum() {
        return tibiFeeNum;
    }

    public void setTibiFeeNum(BigDecimal tibiFeeNum) {
        this.tibiFeeNum = tibiFeeNum;
    }

    @Column(name = "currency_accuracy")
    private Integer currencyAccuracy;

    @Column(name = "currency_content")
    private String currencyContent;

    @Column(name = "main_id")
    private String mainId;

    @Column(name = "exten_info")
    private String extenInfo;


    @Column(name = "bean")
    private String bean;

    @Column(name = "tibi_fee_num")
    private BigDecimal tibiFeeNum;

    @Column(name = "min_num")
    private BigDecimal minNum;

    public BigInteger getGasLimit() {
        return gasLimit;
    }

    public void setGasLimit(BigInteger gasLimit) {
        this.gasLimit = gasLimit;
    }

    @Column(name = "gas_limit")
    private BigInteger gasLimit;

    public BigDecimal getMinNum() {
        return minNum;
    }

    public void setMinNum(BigDecimal minNum) {
        this.minNum = minNum;
    }

    /**
     * @return currency_id
     */
    public Integer getCurrencyId() {
        return currencyId;
    }

    /**
     * @param currencyId
     */
    public void setCurrencyId(Integer currencyId) {
        this.currencyId = currencyId;
    }

    /**
     * @return currency_namekr
     */
    public String getCurrencyNamekr() {
        return currencyNamekr;
    }

    /**
     * @param currencyNamekr
     */
    public void setCurrencyNamekr(String currencyNamekr) {
        this.currencyNamekr = currencyNamekr;
    }

    /**
     * 获取货币名称
     *
     * @return currency_name - 货币名称
     */
    public String getCurrencyName() {
        return currencyName;
    }

    /**
     * 设置货币名称
     *
     * @param currencyName 货币名称
     */
    public void setCurrencyName(String currencyName) {
        this.currencyName = currencyName;
    }

    /**
     * 获取货币logo
     *
     * @return currency_logo - 货币logo
     */
    public String getCurrencyLogo() {
        return currencyLogo;
    }

    /**
     * 设置货币logo
     *
     * @param currencyLogo 货币logo
     */
    public void setCurrencyLogo(String currencyLogo) {
        this.currencyLogo = currencyLogo;
    }

    /**
     * 获取英文标识
     *
     * @return currency_mark - 英文标识
     */
    public String getCurrencyMark() {
        return currencyMark;
    }

    /**
     * 设置英文标识
     *
     * @param currencyMark 英文标识
     */
    public void setCurrencyMark(String currencyMark) {
        this.currencyMark = currencyMark;
    }

    /**
     * 获取总市值
     *
     * @return currency_all_money - 总市值
     */
    public BigDecimal getCurrencyAllMoney() {
        return currencyAllMoney;
    }

    /**
     * 设置总市值
     *
     * @param currencyAllMoney 总市值
     */
    public void setCurrencyAllMoney(BigDecimal currencyAllMoney) {
        this.currencyAllMoney = currencyAllMoney;
    }

    /**
     * 获取币总数量
     *
     * @return currency_all_num - 币总数量
     */
    public BigDecimal getCurrencyAllNum() {
        return currencyAllNum;
    }

    /**
     * 设置币总数量
     *
     * @param currencyAllNum 币总数量
     */
    public void setCurrencyAllNum(BigDecimal currencyAllNum) {
        this.currencyAllNum = currencyAllNum;
    }

    /**
     * 获取买入手续费
     *
     * @return currency_buy_fee - 买入手续费
     */
    public Float getCurrencyBuyFee() {
        return currencyBuyFee;
    }

    /**
     * 设置买入手续费
     *
     * @param currencyBuyFee 买入手续费
     */
    public void setCurrencyBuyFee(Float currencyBuyFee) {
        this.currencyBuyFee = currencyBuyFee;
    }

    /**
     * 获取卖出手续费
     *
     * @return currency_sell_fee - 卖出手续费
     */
    public Float getCurrencySellFee() {
        return currencySellFee;
    }

    /**
     * 设置卖出手续费
     *
     * @param currencySellFee 卖出手续费
     */
    public void setCurrencySellFee(Float currencySellFee) {
        this.currencySellFee = currencySellFee;
    }

    /**
     * 获取该币种的链接地址
     *
     * @return currency_url - 该币种的链接地址
     */
    public String getCurrencyUrl() {
        return currencyUrl;
    }

    /**
     * 设置该币种的链接地址
     *
     * @param currencyUrl 该币种的链接地址
     */
    public void setCurrencyUrl(String currencyUrl) {
        this.currencyUrl = currencyUrl;
    }

    /**
     * 获取可以进行交易的币种
     *
     * @return trade_currency_id - 可以进行交易的币种
     */
    public Integer getTradeCurrencyId() {
        return tradeCurrencyId;
    }

    /**
     * 设置可以进行交易的币种
     *
     * @param tradeCurrencyId 可以进行交易的币种
     */
    public void setTradeCurrencyId(Integer tradeCurrencyId) {
        this.tradeCurrencyId = tradeCurrencyId;
    }

    /**
     * @return is_line
     */
    public Byte getIsLine() {
        return isLine;
    }

    /**
     * @param isLine
     */
    public void setIsLine(Byte isLine) {
        this.isLine = isLine;
    }

    /**
     * 获取是否交易 0 是交易许可 1是交易不许可
     *
     * @return is_lock - 是否交易 0 是交易许可 1是交易不许可
     */
    public Byte getIsLock() {
        return isLock;
    }

    /**
     * 设置是否交易 0 是交易许可 1是交易不许可
     *
     * @param isLock 是否交易 0 是交易许可 1是交易不许可
     */
    public void setIsLock(Byte isLock) {
        this.isLock = isLock;
    }

    /**
     * @return port_number
     */
    public Integer getPortNumber() {
        return portNumber;
    }

    /**
     * @param portNumber
     */
    public void setPortNumber(Integer portNumber) {
        this.portNumber = portNumber;
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

    /**
     * @return status
     */
    public Byte getStatus() {
        return status;
    }

    /**
     * @param status
     */
    public void setStatus(Byte status) {
        this.status = status;
    }

    /**
     * 获取rpc路径
     *
     * @return rpc_url - rpc路径
     */
    public String getRpcUrl() {
        return rpcUrl;
    }

    /**
     * 设置rpc路径
     *
     * @param rpcUrl rpc路径
     */
    public void setRpcUrl(String rpcUrl) {
        this.rpcUrl = rpcUrl;
    }

    /**
     * 获取rpc密码
     *
     * @return rpc_pwd - rpc密码
     */
    public String getRpcPwd() {
        return rpcPwd;
    }

    /**
     * 设置rpc密码
     *
     * @param rpcPwd rpc密码
     */
    public void setRpcPwd(String rpcPwd) {
        this.rpcPwd = rpcPwd;
    }

    /**
     * 获取rpc账号（用户名）
     *
     * @return rpc_user - rpc账号（用户名）
     */
    public String getRpcUser() {
        return rpcUser;
    }

    /**
     * 设置rpc账号（用户名）
     *
     * @param rpcUser rpc账号（用户名）
     */
    public void setRpcUser(String rpcUser) {
        this.rpcUser = rpcUser;
    }

    /**
     * 获取最大提币额
     *
     * @return currency_all_tibi - 最大提币额
     */
    public Integer getCurrencyAllTibi() {
        return currencyAllTibi;
    }

    /**
     * 设置最大提币额
     *
     * @param currencyAllTibi 最大提币额
     */
    public void setCurrencyAllTibi(Integer currencyAllTibi) {
        this.currencyAllTibi = currencyAllTibi;
    }

    /**
     * 获取详情跳转链接
     *
     * @return detail_url - 详情跳转链接
     */
    public String getDetailUrl() {
        return detailUrl;
    }

    /**
     * 设置详情跳转链接
     *
     * @param detailUrl 详情跳转链接
     */
    public void setDetailUrl(String detailUrl) {
        this.detailUrl = detailUrl;
    }

    /**
     * 获取钱包储存路径
     *
     * @return qianbao_url - 钱包储存路径
     */
    public String getQianbaoUrl() {
        return qianbaoUrl;
    }

    /**
     * 设置钱包储存路径
     *
     * @param qianbaoUrl 钱包储存路径
     */
    public void setQianbaoUrl(String qianbaoUrl) {
        this.qianbaoUrl = qianbaoUrl;
    }

    /**
     * 获取钱包密钥
     *
     * @return qianbao_key - 钱包密钥
     */
    public String getQianbaoKey() {
        return qianbaoKey;
    }

    /**
     * 设置钱包密钥
     *
     * @param qianbaoKey 钱包密钥
     */
    public void setQianbaoKey(String qianbaoKey) {
        this.qianbaoKey = qianbaoKey;
    }

    /**
     * 获取涨停
     *
     * @return price_up - 涨停
     */
    public Float getPriceUp() {
        return priceUp;
    }

    /**
     * 设置涨停
     *
     * @param priceUp 涨停
     */
    public void setPriceUp(Float priceUp) {
        this.priceUp = priceUp;
    }

    /**
     * 获取跌停
     *
     * @return price_down - 跌停
     */
    public Float getPriceDown() {
        return priceDown;
    }

    /**
     * 设置跌停
     *
     * @param priceDown 跌停
     */
    public void setPriceDown(Float priceDown) {
        this.priceDown = priceDown;
    }

    /**
     * @return sort
     */
    public Integer getSort() {
        return sort;
    }

    /**
     * @param sort
     */
    public void setSort(Integer sort) {
        this.sort = sort;
    }

    /**
     * 获取限制位数
     *
     * @return currency_digit_num - 限制位数
     */
    public Integer getCurrencyDigitNum() {
        return currencyDigitNum;
    }

    /**
     * 设置限制位数
     *
     * @param currencyDigitNum 限制位数
     */
    public void setCurrencyDigitNum(Integer currencyDigitNum) {
        this.currencyDigitNum = currencyDigitNum;
    }

    /**
     * @return guanwang_url
     */
    public String getGuanwangUrl() {
        return guanwangUrl;
    }

    /**
     * @param guanwangUrl
     */
    public void setGuanwangUrl(String guanwangUrl) {
        this.guanwangUrl = guanwangUrl;
    }

    /**
     * @return tibi_fee
     */
    public Float getTibiFee() {
        return tibiFee;
    }

    /**
     * @param tibiFee
     */
    public void setTibiFee(Float tibiFee) {
        this.tibiFee = tibiFee;
    }

    /**
     * @return tibi_qrs
     */
    public Integer getTibiQrs() {
        return tibiQrs;
    }

    /**
     * @param tibiQrs
     */
    public void setTibiQrs(Integer tibiQrs) {
        this.tibiQrs = tibiQrs;
    }

    /**
     * @return tibi_small
     */
    public Float getTibiSmall() {
        return tibiSmall;
    }

    /**
     * @param tibiSmall
     */
    public void setTibiSmall(Float tibiSmall) {
        this.tibiSmall = tibiSmall;
    }

    /**
     * @return chongbi_open
     */
    public Integer getChongbiOpen() {
        return chongbiOpen;
    }

    /**
     * @param chongbiOpen
     */
    public void setChongbiOpen(Integer chongbiOpen) {
        this.chongbiOpen = chongbiOpen;
    }

    /**
     * @return tibi_open
     */
    public Integer getTibiOpen() {
        return tibiOpen;
    }

    /**
     * @param tibiOpen
     */
    public void setTibiOpen(Integer tibiOpen) {
        this.tibiOpen = tibiOpen;
    }

    /**
     * @return currency_digit_cny
     */
    public Integer getCurrencyDigitCny() {
        return currencyDigitCny;
    }

    /**
     * @param currencyDigitCny
     */
    public void setCurrencyDigitCny(Integer currencyDigitCny) {
        this.currencyDigitCny = currencyDigitCny;
    }

    /**
     * @return currency_digit_usd
     */
    public Integer getCurrencyDigitUsd() {
        return currencyDigitUsd;
    }

    /**
     * @param currencyDigitUsd
     */
    public void setCurrencyDigitUsd(Integer currencyDigitUsd) {
        this.currencyDigitUsd = currencyDigitUsd;
    }

    /**
     * @return txurl
     */
    public String getTxurl() {
        return txurl;
    }

    /**
     * @param txurl
     */
    public void setTxurl(String txurl) {
        this.txurl = txurl;
    }

    /**
     * 获取当韩元区未上，写对应交易币种的currency_id
     *
     * @return trade_market - 当韩元区未上，写对应交易币种的currency_id
     */
    public String getTradeMarket() {
        return tradeMarket;
    }

    /**
     * 设置当韩元区未上，写对应交易币种的currency_id
     *
     * @param tradeMarket 当韩元区未上，写对应交易币种的currency_id
     */
    public void setTradeMarket(String tradeMarket) {
        this.tradeMarket = tradeMarket;
    }

    /**
     * @return currency_accuracy
     */
    public Integer getCurrencyAccuracy() {
        return currencyAccuracy;
    }

    /**
     * @param currencyAccuracy
     */
    public void setCurrencyAccuracy(Integer currencyAccuracy) {
        this.currencyAccuracy = currencyAccuracy;
    }

    /**
     * @return currency_content
     */
    public String getCurrencyContent() {
        return currencyContent;
    }

    /**
     * @param currencyContent
     */
    public void setCurrencyContent(String currencyContent) {
        this.currencyContent = currencyContent;
    }

    public String getMainId() {
        return mainId;
    }

    public void setMainId(String mainId) {
        this.mainId = mainId;
    }

    public String getExtenInfo() {
        return extenInfo;
    }

    public void setExtenInfo(String extenInfo) {
        this.extenInfo = extenInfo;
    }

    public String getBean() {
        return bean;
    }

    public void setBean(String bean) {
        this.bean = bean;
    }
}