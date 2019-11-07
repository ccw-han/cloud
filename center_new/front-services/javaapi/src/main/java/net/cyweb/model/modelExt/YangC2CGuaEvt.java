package net.cyweb.model.modelExt;

import lombok.Data;

/**
 * 首页法币购买查询实体类
 */
@Data
public class YangC2CGuaEvt {

    //查询币种id
    private String currencyId;
    //购买金额
    private Integer buyMoney;


}
