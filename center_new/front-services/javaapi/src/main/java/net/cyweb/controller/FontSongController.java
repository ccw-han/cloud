package net.cyweb.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import net.cyweb.model.Result;
import net.cyweb.service.YangBankService;
import net.cyweb.service.YangOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Time：2019/9/27
 * 创建人：椰椰
 * 前端接口
 */
@RestController
@Api(tags = "耶耶", position = 2)
@RequestMapping(value = "fontsong")
public class FontSongController {
}
