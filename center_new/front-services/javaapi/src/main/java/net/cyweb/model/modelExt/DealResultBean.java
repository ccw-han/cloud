package net.cyweb.model.modelExt;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DealResultBean {

    private  boolean isOk;

    BigDecimal nums_need;

    private boolean isError;

}
