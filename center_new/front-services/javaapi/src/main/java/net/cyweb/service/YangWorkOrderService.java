package net.cyweb.service;

import com.thoughtworks.xstream.mapper.Mapper;
import cyweb.utils.CoinConst;
import cyweb.utils.ErrorCode;
import io.swagger.annotations.ApiOperation;
import jnr.ffi.annotations.In;
import net.cyweb.config.custom.Base64Utils;
import net.cyweb.config.custom.NoteUtils;
import net.cyweb.mapper.YangCurrencyMapper;
import net.cyweb.mapper.YangWorkOrderMapper;
import net.cyweb.model.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 工单服务
 * 创建人：ccw
 * Time：2019/9/3
 *
 * @return
 */
@Service
public class YangWorkOrderService extends BaseService<YangWorkOrder> {

    @Autowired
    private YangWorkOrderMapper yangWorkOrderMapper;

    @Autowired
    private RedisService redisService;


    /**
     * 用户提交工单
     *
     * @param map
     * @return
     * @author ccw
     */
    public Result addWorkOrder(Map<String, String> map) {
        Result result = new Result();
        YangWorkOrder order = new YangWorkOrder();
        order.setTitle(map.get("title"));
        order.setContent(map.get("content"));
        order.setPic1(map.get("pic1"));
        order.setPic2(map.get("pic2"));
        order.setPic3(map.get("pic3"));
        order.setCreatedTime(new Date());
        order.setMemberName(map.get("memberName"));
        String token = CoinConst.TOKENKEYTOKEN + map.get("accessToken");
        //验证token
        Object memberId = redisService.get(token);
        if (memberId == null) {
            result.setCode(Result.Code.ERROR);
            result.setErrorCode(ErrorCode.ERROR_ERROR_TOKEN_EXPIRE.getIndex());
            result.setMsg(ErrorCode.ERROR_ERROR_TOKEN_EXPIRE.getMessage());
            return result;
        }
        order.setId(UUID.randomUUID().toString());
        order.setMemberId((Integer) memberId);
        //插入数据库
        try {
            yangWorkOrderMapper.addWorkOrder(order);
            result.setCode(Result.Code.SUCCESS);
            result.setMsg("插入工单成功");

        } catch (Exception e) {
            e.printStackTrace();
            result.setCode(Result.Code.ERROR);
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
            result.setMsg(ErrorCode.ERROR_SYSTEM.getMessage());
        }
        return result;
    }

    /**
     * 用户查看历史工单
     *
     * @param map
     * @return
     * @author ccw
     */
    public Result findOldWorkOrders(Map<String, String> map) {
        Result result = new Result();
        String token = CoinConst.TOKENKEYTOKEN + map.get("accessToken");
        //验证token
        Object memberId = redisService.get(token);
        if (memberId == null) {
            result.setCode(Result.Code.ERROR);
            result.setErrorCode(ErrorCode.ERROR_ERROR_TOKEN_EXPIRE.getIndex());
            result.setMsg(ErrorCode.ERROR_ERROR_TOKEN_EXPIRE.getMessage());
            return result;
        }
        //根据memberId查询工单
        try {
            List<YangWorkOrder> orders = yangWorkOrderMapper.findOldWorkOrders((Integer) memberId);
            if (orders.size() > 0) {
                result.setCode(Result.Code.SUCCESS);
                result.setData(orders);
                result.setMsg("查询工单成功");

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

    /**
     * 用户查询单个工单
     *
     * @param map
     * @return
     * @author ccw
     */
    public Result findWorkOrderById(Map<String, String> map) {
        Result result = new Result();
        String id = map.get("id");
        String token = CoinConst.TOKENKEYTOKEN + map.get("accessToken");
        //验证token
        Object memberId = redisService.get(token);
        if (memberId == null) {
            result.setCode(Result.Code.ERROR);
            result.setErrorCode(ErrorCode.ERROR_ERROR_TOKEN_EXPIRE.getIndex());
            result.setMsg(ErrorCode.ERROR_ERROR_TOKEN_EXPIRE.getMessage());
            return result;
        }
        //根据memberId查询工单
        try {
            YangWorkOrder order = yangWorkOrderMapper.findWorkOrderById(id, (Integer) memberId);
            if (order != null) {
                result.setCode(Result.Code.SUCCESS);
                result.setData(order);
                result.setMsg("查询工单成功");

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

    /**
     * 获取自选好币
     *
     * @param map
     * @return
     * @author ccw
     */
    public Result getSelfCurrencys(Map<String, String> map) {
        List currencyIds = new ArrayList();
        Result result = new Result();
        String token = CoinConst.TOKENKEYTOKEN + map.get("accessToken");
        //验证token
        Object memberId = redisService.get(token);
        if (memberId == null) {
            result.setCode(Result.Code.ERROR);
            result.setErrorCode(ErrorCode.ERROR_ERROR_TOKEN_EXPIRE.getIndex());
            result.setMsg(ErrorCode.ERROR_ERROR_TOKEN_EXPIRE.getMessage());
            return result;
        }
        //根据memberId查询
        try {
            List<YangCurrencySelf> selves = yangWorkOrderMapper.getSelfCurrencys((Integer) memberId);
            //将currencyId 查出来，放入集合
            if (selves.size() > 0) {
                for (YangCurrencySelf self : selves) {
                    if (StringUtils.isNotBlank(String.valueOf(self.getCurrencyId()))) {
                        currencyIds.add(self.getCurrencyId());
                    }
                }
                //去查集合
                List<Map> datas = yangWorkOrderMapper.getSelfCurrencysById(currencyIds);
                if (datas.size() > 0) {
                    result.setCode(Result.Code.SUCCESS);
                    result.setData(datas);
                    result.setMsg("查询自选好币成功");

                } else {
                    result.setCode(Result.Code.ERROR);
                    result.setMsg("查询失败");
                }

            } else {
                result.setCode(Result.Code.ERROR);
                result.setMsg("查询失败");
            }

        } catch (Exception e) {
            e.printStackTrace();
            result.setCode(Result.Code.ERROR);
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
            result.setMsg(ErrorCode.ERROR_SYSTEM.getMessage());
        }
        return result;
    }

    /**
     * 添加自选好币
     *
     * @param map
     * @return
     * @author ccw
     */
    public Result addSelfCurrencys(Map<String, String> map) {
        Result result = new Result();
        String token = CoinConst.TOKENKEYTOKEN + map.get("accessToken");
        //验证token
        Object memberId = redisService.get(token);
        if (memberId == null) {
            result.setCode(Result.Code.ERROR);
            result.setErrorCode(ErrorCode.ERROR_ERROR_TOKEN_EXPIRE.getIndex());
            result.setMsg(ErrorCode.ERROR_ERROR_TOKEN_EXPIRE.getMessage());
            return result;
        }
        map.put("memberId", memberId.toString());
        //根据memberId查询
        try {
            yangWorkOrderMapper.addSelfCurrencys(map);
            result.setCode(Result.Code.SUCCESS);
            result.setMsg("添加自选好币成功");
        } catch (Exception e) {
            e.printStackTrace();
            result.setCode(Result.Code.ERROR);
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
            result.setMsg(ErrorCode.ERROR_SYSTEM.getMessage());
        }
        return result;
    }

}
