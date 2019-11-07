package net.cyweb.model;

import jnr.ffi.annotations.In;
import lombok.Data;

import java.util.Date;

@Data
public class YangCurrencySelf extends BaseEntity{
    private Integer id;
    private Integer memberId;
    private Integer currencyId;
    private Integer currencyPairId;
    private String currencyPairName;
    private String states;
    private String createdTime;

}