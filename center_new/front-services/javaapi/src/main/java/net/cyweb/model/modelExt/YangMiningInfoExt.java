package net.cyweb.model.modelExt;

import lombok.Data;
import net.cyweb.model.YangMiningInfo;

import java.math.BigDecimal;

@Data
public class YangMiningInfoExt extends YangMiningInfo {

    private BigDecimal ethNum;

}
