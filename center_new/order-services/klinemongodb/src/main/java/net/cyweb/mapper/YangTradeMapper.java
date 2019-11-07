package net.cyweb.mapper;

import net.cyweb.config.mybatis.TkMapper;
import net.cyweb.model.YangTrade;
import org.web3j.crypto.Hash;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface YangTradeMapper extends TkMapper<YangTrade> {

    public List<HashMap> klineData(Map map);

    public List<HashMap> klineDataA(Map map);

    public List<HashMap> queryKlineHistoryData(Map map);

    public List<HashMap> queryKlineHistoryDataA(Map map);

    List<YangTrade> getAllYangTrades();

    public List<YangTrade> tradeData(Map map);
}