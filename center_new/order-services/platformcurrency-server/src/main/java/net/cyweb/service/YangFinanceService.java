package net.cyweb.service;

import net.cyweb.mapper.YangFinanceMapper;
import net.cyweb.model.YangFinance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@EnableAsync
public class YangFinanceService extends BaseService<YangFinance>{

    @Autowired
    private YangFinanceMapper YangFinanceMapper;

    @Transactional
    public void currencyUserChangeToSave(List<YangFinance> list) {
        YangFinanceMapper.insertFinanceBatch(list);
    }

}
