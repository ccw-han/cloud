package net.cyweb.model.modelExt;

import lombok.Data;
import net.cyweb.model.YangCurrency;

import java.math.BigDecimal;

@Data
public class YangCurrencyExt extends YangCurrency{

    private BigDecimal num;

    private BigDecimal forzenNum;

    private String memberId;

}
