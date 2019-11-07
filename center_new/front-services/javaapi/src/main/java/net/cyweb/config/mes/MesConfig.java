package net.cyweb.config.mes;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "note")
@PropertySource(value = "classpath:conf/config.properties")
public class MesConfig {

    /**
     * 短信内容传输编码，设为 3 即可，3 为 URLEncoder - UTF8 编码
     */

    private  String transferEncoding;
    /**
     * 返回格式固定为 2，返回 JSON 格式,1为xml
     */

    private String responseFormat;
    /**
     * 消息编码，用 15 即可
     * 8表示UNICODE编码
     */

    private String dataCoding;
    /**
     * 扩展码
     */

    private String externalCoding;

    /**
     * 用户名
     */

    private String userName;

    /**
     * 密码
     */
    private String passWord;
    /**
     * HTTP接口地址
     */

    private String httpUrl;
    /**
     * url编码格式
     */

    private String urlEncodeConfig;
    /**
     * url后缀
     */

    private String urlLast;

    private String cnyBTCUrl;

    private String cnyUSDTUrl;

    private String cnyETHUrl;


    /**
     * 注册验证码
     */
    private String registerCode;

    public String getRegisterCode() {
        return registerCode;
    }

    public void setRegisterCode(String registerCode) {
        this.registerCode = registerCode;
    }

    public String getTransferEncoding() {
        return transferEncoding;
    }

    public void setTransferEncoding(String transferEncoding) {
        this.transferEncoding = transferEncoding;
    }

    public String getResponseFormat() {
        return responseFormat;
    }

    public void setResponseFormat(String responseFormat) {
        this.responseFormat = responseFormat;
    }

    public String getDataCoding() {
        return dataCoding;
    }

    public void setDataCoding(String dataCoding) {
        this.dataCoding = dataCoding;
    }

    public String getExternalCoding() {
        return externalCoding;
    }

    public void setExternalCoding(String externalCoding) {
        this.externalCoding = externalCoding;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassWord() {
        return passWord;
    }

    public void setPassWord(String passWord) {
        this.passWord = passWord;
    }

    public String getHttpUrl() {
        return httpUrl;
    }

    public void setHttpUrl(String httpUrl) {
        this.httpUrl = httpUrl;
    }

    public String getUrlEncodeConfig() {
        return urlEncodeConfig;
    }

    public void setUrlEncodeConfig(String urlEncodeConfig) {
        this.urlEncodeConfig = urlEncodeConfig;
    }

    public String getUrlLast() {
        return urlLast;
    }

    public void setUrlLast(String urlLast) {
        this.urlLast = urlLast;
    }

    public String getCnyBTCUrl() {
        return cnyBTCUrl;
    }

    public void setCnyBTCUrl(String cnyBTCUrl) {
        this.cnyBTCUrl = cnyBTCUrl;
    }

    public String getCnyUSDTUrl() {
        return cnyUSDTUrl;
    }

    public void setCnyUSDTUrl(String cnyUSDTUrl) {
        this.cnyUSDTUrl = cnyUSDTUrl;
    }

    public String getCnyETHUrl() {
        return cnyETHUrl;
    }

    public void setCnyETHUrl(String cnyETHUrl) {
        this.cnyETHUrl = cnyETHUrl;
    }
}
