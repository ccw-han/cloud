package net.cyweb.service;

import com.alibaba.fastjson.JSONObject;
import cyweb.utils.ErrorCode;
import net.cyweb.mapper.*;
import net.cyweb.model.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.math.BigDecimal;

import static net.cyweb.controller.ApiBaseController.MQ_ORDER_QUEUE;

@Service
@EnableAsync
public class RobotService {

    @Autowired
    RedisService redisService;

    private  String RobotConfigKeypre  = "ROBOTCONFIG-";

    @Autowired
    private YangCurrencyPairMapper yangCurrencyPairMapper;



    @Autowired
    private YangCurrencyMapper yangCurrencyMapper;

    @Autowired
    private YangTradeMapper yangTradeMapper;


    @Autowired
    private YangMemberTokenService yangMemberTokenService;


    @Autowired
    private YangTradeFeeMapper yangTradeFeeMapper;


    @Autowired
    private YangOrderService yangOrderService;

    @Autowired
    private YangOrdersMapper yangOrdersMapper;

    @Autowired
    private YangMemberService yangMemberService;



}
