package utils.collectUtils;

import net.cyweb.model.YangCollectConfig;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class CollecterBase implements Collect {

    protected YangCollectConfig yangCollectConfig;

    @Override
    public BigDecimal getPrice() {
        return null;
    }
}
