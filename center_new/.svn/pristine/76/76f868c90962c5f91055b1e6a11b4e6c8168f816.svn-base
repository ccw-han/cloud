package net.cyweb.controller;

import cyweb.utils.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import net.cyweb.config.custom.CNYPriceUtils;
import net.cyweb.config.custom.NoteUtils;
import net.cyweb.config.custom.RevertCNYUtils;
import net.cyweb.config.mes.MesConfig;
import net.cyweb.exception.EmailNotExistException;
import net.cyweb.exception.MemberIsLocketException;
import net.cyweb.exception.PasswordErrorException;
import net.cyweb.mapper.YangCurrencyPairMapper;
import net.cyweb.model.*;
import net.cyweb.model.modelExt.YangC2CGuaEvt;
import net.cyweb.service.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.spring.web.json.JacksonModuleRegistrar;
import springfox.documentation.spring.web.json.Json;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Time：2019/9/12
 * 创建人：ccw
 * 前端接口
 */
@RestController
@Api(tags = "前端", position = 2)
@RequestMapping(value = "/font1")
public class Font1Controller extends ApiBaseController {

    @Autowired
    RevertCNYUtils revertCNYUtils;


    /**
     * 测试汇率
     *
     * @return
     * @Param map
     */
    @RequestMapping(value = "testCNY", method = RequestMethod.POST)
    @ApiOperation(value = "测试汇率", notes = "测试汇率")
    public Result testCNY(@RequestBody Map<String, String> map) {
        Result result = new Result();
        String price = revertCNYUtils.revertCNYPrice("", 20);
        result.setData(price);
        return result;
    }


}
