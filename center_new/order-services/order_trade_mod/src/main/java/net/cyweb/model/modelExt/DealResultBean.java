package net.cyweb.model.modelExt;

import lombok.Data;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class DealResultBean {

    private  boolean isOk;

    BigDecimal nums_need;

    private boolean isError;

}
