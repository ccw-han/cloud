package net.cyweb.service;

import cyweb.utils.CoinConst;
import cyweb.utils.ErrorCode;
import net.cyweb.config.custom.Base64Utils;
import net.cyweb.mapper.YangCurrencyMapper;
import net.cyweb.mapper.YangNewBiMapper;
import net.cyweb.model.Result;
import net.cyweb.model.UserProPs;
import net.cyweb.model.YangNewbi;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class YangNewBiService extends BaseService<YangNewbi> {
    @Autowired
    private YangNewBiMapper yangNewBiMapper;


    /**
     * @param currencyId 币种的id
     * @return 币种的信息
     * @Author: yxt
     * @version: 2019/9/27
     * 获取币种介绍信息
     */
    public Result getCurrencyInfoById(Integer currencyId) {
        Result result = new Result();
        try {
            YangNewbi yn = yangNewBiMapper.getCurrencyProfile(currencyId);
            if (yn != null && yn.getCurrencyId() != null) {
                result.setCode(Result.Code.SUCCESS);
                result.setData(yn);
                result.setMsg("查询成功");

            } else {
                result.setCode(Result.Code.ERROR);
                result.setMsg("查询失败");
                result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.setCode(Result.Code.ERROR);
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
            result.setMsg(ErrorCode.ERROR_SYSTEM.getMessage());
        }
        return result;
    }
}
