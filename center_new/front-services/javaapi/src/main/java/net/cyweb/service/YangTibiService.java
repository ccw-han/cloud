package net.cyweb.service;

import net.cyweb.mapper.*;
import net.cyweb.model.YangTibi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;
import net.cyweb.model.*;

import java.util.List;

import cyweb.utils.*;

@Service
public class YangTibiService extends BaseService<YangTibi> {


    @Autowired
    private YangTibiOutMapper yangTibiOutMapper;
    @Autowired
    private YangQianbaoAddressMapper yangQianbaoAddressMapper;

    @Autowired
    private YangMemberTokenService yangMemberTokenService;

    @Autowired
    private YangFinanceMapper yangFinanceMapper;

    @Autowired
    private RedisService redisService;

    @Autowired
    private YangTibiMapper yangTibiMapper;

    /**
     * 查询币种充币记录
     *
     * @return 币种充币记录
     */
    public Result getDepositRecord(String accessToken) {
        Result result = new Result();
        String token = CoinConst.TOKENKEYTOKEN + accessToken;
        //验证token
        Object memberId = redisService.get(token);
        try {
            List<YangTibi> list = yangTibiMapper.getDepositRecord((Integer) memberId);
            if (list != null) {
                result.setCode(Result.Code.SUCCESS);
                result.setData(list);
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
