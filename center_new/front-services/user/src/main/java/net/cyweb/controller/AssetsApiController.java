package net.cyweb.controller;

import cyweb.utils.ErrorCode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import net.cyweb.exception.CurrencyUserNotFoundException;
import net.cyweb.exception.ErrorTokenException;
import net.cyweb.exception.TokenExpirException;
import net.cyweb.model.Result;
import net.cyweb.model.YangCurrencyUser;
import net.cyweb.model.YangMember;
import net.cyweb.model.YangMemberToken;
import net.cyweb.model.modelExt.YangCurrencyExt;
import net.cyweb.service.YangMemberTokenService;
import net.cyweb.validate.SpecAssets;
import net.cyweb.validate.UpdateAssets;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

/*用户资产*/
@RestController
@RequestMapping(value = "assets")
@Api(tags = "用户资产",position = 3)
public class AssetsApiController extends ApiBaseController{

    @Autowired
    private YangMemberTokenService yangMemberTokenService;

    /*用户所有资产*/
    @RequestMapping(value = "all",method = RequestMethod.POST)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "accessToken", value = "用户touken", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "currencyMark", value = "币种标识 赛选用",  dataType = "String", paramType = "query")
    })
    public Result allAssets(@ApiIgnore @Validated YangMemberToken yangMemberToken, YangCurrencyExt yangCurrencyExt){
        Result result = new Result();
        try{
            if (StringUtils.isBlank(yangMemberToken.getAccessToken()))
            {
                result.setErrorCode(ErrorCode.ERROR_ERROR_TOKEN.getIndex());
                throw new Exception(ErrorCode.ERROR_ERROR_TOKEN.getMessage());
            }
          YangMember yangMember =   yangMemberTokenService.getAllAssets(yangMemberToken,yangCurrencyExt);
          result.setData(yangMember);
        }catch (Exception e){
            e.printStackTrace();
            result.setCode(Result.Code.ERROR);
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
//            result.setMsg(ErrorCode.ERROR_SYSTEM.getMessage());
            result.setMsg(e.getMessage());
        }
        return result;
    }

    /*用户指定资产*/
    @RequestMapping(value = "specAssets",method = RequestMethod.POST)
    @ApiOperation(value = "获取用户指定资产", notes = "获取用户指定资产")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "accessToken", value = "用户登录后获取的票据", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "currencyId", value = "币种id", required = true, dataType = "Int", paramType = "query"),
    })
    public Result specAssets(@ApiIgnore @Validated YangMemberToken yangMemberToken, @ApiIgnore @Validated({SpecAssets.class}) YangCurrencyUser yangCurrencyUser, BindingResult bindingResult){
        Result result = new Result();
        result.setCode(Result.Code.ERROR);
        try{
            /*数据验证*/
            if(bindingResult.hasErrors()){
                result.setMsg(getStringErrors(bindingResult));
            }else{
                YangMember yangMember = yangMemberTokenService.getSpecAssets(yangMemberToken, yangCurrencyUser);
                result.setCode(Result.Code.SUCCESS);
                result.setData(yangMember);
            }

        }catch (ErrorTokenException e) {
            e.printStackTrace();
            result.setErrorCode(ErrorCode.ERROR_ERROR_TOKEN.getIndex());
            result.setMsg(ErrorCode.ERROR_ERROR_TOKEN.getMessage());
        }catch (TokenExpirException e) {
            e.printStackTrace();
            result.setErrorCode(ErrorCode.ERROR_ERROR_TOKEN_EXPIRE.getIndex());
            result.setMsg(ErrorCode.ERROR_ERROR_TOKEN_EXPIRE.getMessage());
        }catch (Exception e) {
            e.printStackTrace();
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
        }
        return result;
    }

    /*改变用户资产*/
    @RequestMapping(value = "updateAssets",method = RequestMethod.POST)
    @ApiOperation(value = "改变用户资产", notes = "改变用户资产")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "accessToken", value = "用户登录后获取的票据", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "currencyId", value = "币种id", required = true, dataType = "Integer", paramType = "query"),
            @ApiImplicitParam(name = "num", value = "用户资金（币种ID为非0时更新）", required = true, dataType = "Number", paramType = "query"),
            @ApiImplicitParam(name = "forzenNum", value = "用户冻结资金（币种ID为非0时更新）", required = true, dataType = "Number", paramType = "query"),
            @ApiImplicitParam(name = "numType", value = "用户资金增减【inc:增加 dec:减】（币种ID为非0时更新）", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "forzenNumType", value = "用户冻结资金增减【inc:增加 dec:减】（币种ID为非0时更新）", required = true, dataType = "String", paramType = "query"),
    })
    public Result updateAssets(@ApiIgnore @Validated YangMemberToken yangMemberToken,@ApiIgnore @Validated({UpdateAssets.class}) YangCurrencyUser yangCurrencyUser ,BindingResult bindingResult, @ApiIgnore  YangMember yangMember,BindingResult bindingResul2){
        Result result = new Result();
        result.setCode(Result.Code.ERROR);
        try{

            /*数据验证*/
            if(bindingResult.hasErrors()){
                result.setMsg(getStringErrors(bindingResult));
            }else if(bindingResul2.hasErrors()){
                result.setMsg(getStringErrors(bindingResul2));
            }else{
                yangMemberTokenService.updateAssets(yangMemberToken, yangCurrencyUser,yangMember);
                result.setCode(Result.Code.SUCCESS);
            }

        }catch (ErrorTokenException e) {
            e.printStackTrace();
            result.setErrorCode(ErrorCode.ERROR_ERROR_TOKEN.getIndex());
            result.setMsg(ErrorCode.ERROR_ERROR_TOKEN.getMessage());
        }catch (TokenExpirException e) {
            e.printStackTrace();
            result.setErrorCode(ErrorCode.ERROR_ERROR_TOKEN_EXPIRE.getIndex());
            result.setMsg(ErrorCode.ERROR_ERROR_TOKEN_EXPIRE.getMessage());
        }catch (CurrencyUserNotFoundException e){
            e.printStackTrace();
            result.setErrorCode(ErrorCode.ERROR_CURRENCY_USER_NOT_FOUND.getIndex());
            result.setMsg(ErrorCode.ERROR_CURRENCY_USER_NOT_FOUND.getMessage());
        }catch (Exception e) {
            e.printStackTrace();
            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
        }
        return result;
    }

}
