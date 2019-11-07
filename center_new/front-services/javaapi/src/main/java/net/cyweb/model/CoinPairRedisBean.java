package net.cyweb.model;

import lombok.Data;

import java.math.BigDecimal;



public class CoinPairRedisBean {

    private int cy_id ;
    private int currency_id;
    private int currency_trade_id;
    private String currency_logo;
    private String trade_logo;
    private BigDecimal input_price_num;
    private int rate_num;
    private String currency_mark;
    private String trade_mark;
    private String is_chuang;
    private String is_old;
    private BigDecimal new_price;
    private BigDecimal krw_price;

    public String getIs_old() {
        return is_old;
    }

    public void setIs_old(String is_old) {
        this.is_old = is_old;
    }

    private int price_status;
    private double H24_change;
    private String H24_num;
    private String H24_money;

    public int getCy_id() {

        return cy_id;
    }

    public void setCy_id(int cy_id) {
        this.cy_id = cy_id;
    }

    public int getCurrency_id() {
        return currency_id;
    }

    public void setCurrency_id(int currency_id) {
        this.currency_id = currency_id;
    }

    public int getCurrency_trade_id() {
        return currency_trade_id;
    }

    public void setCurrency_trade_id(int currency_trade_id) {
        this.currency_trade_id = currency_trade_id;
    }

    public String getCurrency_logo() {
        return currency_logo;
    }

    public void setCurrency_logo(String currency_logo) {
        this.currency_logo = currency_logo;
    }

    public String getTrade_logo() {
        return trade_logo;
    }

    public void setTrade_logo(String trade_logo) {
        this.trade_logo = trade_logo;
    }

    public BigDecimal getInput_price_num() {
        return input_price_num;
    }

    public void setInput_price_num(BigDecimal input_price_num) {
        this.input_price_num = input_price_num;
    }

    public int getRate_num() {
        return rate_num;
    }

    public void setRate_num(int rate_num) {
        this.rate_num = rate_num;
    }

    public String getCurrency_mark() {
        if(currency_mark == null)
        {
            return "";
        }
        return currency_mark;
    }

    public void setCurrency_mark(String currency_mark) {
        this.currency_mark = currency_mark;
    }

    public String getTrade_mark() {
        return trade_mark;
    }

    public void setTrade_mark(String trade_mark) {
        this.trade_mark = trade_mark;
    }

    public String getIs_chuang() {
        return is_chuang;
    }

    public void setIs_chuang(String is_chuang) {
        this.is_chuang = is_chuang;
    }

    public BigDecimal getNew_price() {
        return new_price;
    }

    public void setNew_price(BigDecimal new_price) {
        this.new_price = new_price;
    }

    public BigDecimal getKrw_price() {
        return krw_price;
    }

    public void setKrw_price(BigDecimal krw_price) {
        this.krw_price = krw_price;
    }

    public int getPrice_status() {
        return price_status;
    }

    public void setPrice_status(int price_status) {
        this.price_status = price_status;
    }

    public double getH24_change() {
        return H24_change;
    }

    public void setH24_change(double h24_change) {
        H24_change = h24_change;
    }

    public String getH24_num() {
        return H24_num;
    }

    public void setH24_num(String h24_num) {
        H24_num = h24_num;
    }

    public String getH24_money() {
        return H24_money;
    }

    public void setH24_money(String h24_money) {
        H24_money = h24_money;
    }

    public String getH24_max() {
        if(H24_max == null)
        {
            H24_max = "0";
        }
        return H24_max;
    }

    public void setH24_max(String h24_max) {
        H24_max = h24_max;
    }

    public String getH24_min() {
        if(H24_min == null)
        {
            H24_min = "0";
        }
        return H24_min;
    }

    public void setH24_min(String h24_min) {
        H24_min = h24_min;
    }

    private String H24_max;
    private String H24_min;

}
