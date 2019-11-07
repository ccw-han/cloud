package net.cyweb.service;

import com.alibaba.druid.support.json.JSONUtils;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.netflix.discovery.converters.Auto;
import cyweb.utils.CoinConst;
import cyweb.utils.ErrorCode;
import net.cyweb.exception.*;
import net.cyweb.mapper.*;
import net.cyweb.model.*;
import net.cyweb.model.modelExt.PageList;
import net.cyweb.model.modelExt.YangCurrencyExt;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import org.springframework.web.client.RestTemplate;
import tk.mybatis.mapper.entity.Example;

import java.math.BigDecimal;
import java.net.SocketTimeoutException;
import java.util.*;

@Service
@EnableAsync
public class YangMemberTokenService extends BaseService<YangMemberToken> {

    @Autowired
    private YangMemberMapper yangMemberMapper;

    @Autowired
    private YangGoogleauthMapper yangGoogleauthMapper;

    @Autowired
    private YangCurrencyUserMapper yangCurrencyUserMapper;

    @Autowired
    private YangCurrencyMapper yangCurrencyMapper;

    @Autowired
    private YangTibiMapper yangTibiMapper;


    @Autowired
    private YangOrdersMapper yangOrdersMapper;

    @Autowired
    private YangOrderService yangOrderService;

    @Autowired
    private YangMemberService yangMemberService;

    public enum incDecType {
        inc("inc"),dec("dec");
        private String type;
        incDecType(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }
    };



}

