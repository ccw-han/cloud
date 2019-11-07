package net.cyweb.model.modelExt;

import lombok.Data;
import net.cyweb.model.YangTrade;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document
public class YangTradeExt extends YangTrade {

    Double numD;

    Double priceD;

//    private Long addTime;

}
