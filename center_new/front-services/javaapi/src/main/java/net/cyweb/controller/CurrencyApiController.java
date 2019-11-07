//package net.cyweb.controller;
//
//import com.github.pagehelper.PageInfo;
//import cyweb.utils.ErrorCode;
//import io.swagger.annotations.Api;
//import io.swagger.annotations.ApiImplicitParam;
//import io.swagger.annotations.ApiImplicitParams;
//import io.swagger.annotations.ApiOperation;
//import net.cyweb.exception.ErrorTokenException;
//import net.cyweb.exception.TokenExpirException;
//import net.cyweb.model.Result;
//import net.cyweb.model.YangCurrencyUser;
//import net.cyweb.model.YangMemberToken;
//import net.cyweb.service.YangMemberTokenService;
//import net.cyweb.validate.ChargeRecord;
//import net.cyweb.validate.CurrencyGetAddress;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.validation.BindingResult;
//import org.springframework.validation.annotation.Validated;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestMethod;
//import org.springframework.web.bind.annotation.ResponseBody;
//import org.springframework.web.bind.annotation.RestController;
//import springfox.documentation.annotations.ApiIgnore;
//
//import java.util.HashMap;
//import java.util.Map;
//
//@RestController
//@RequestMapping(value = "currency")
//@Api(tags = "充币、提币",position = 4)
//public class CurrencyApiController extends ApiBaseController{
//
//    @Autowired
//    private YangMemberTokenService yangMemberTokenService;
//
//    @RequestMapping(value = "getAddress",method = RequestMethod.POST)
//    @ApiOperation(value = "获取用户充币地址", notes = "获取用户充币地址")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "accessToken", value = "用户登录后获取的票据", required = true, dataType = "String", paramType = "query"),
//            @ApiImplicitParam(name = "currencyId", value = "币种id", required = true, dataType = "Int", paramType = "query"),
//    })
//    public Result getAddress(@ApiIgnore @Validated YangMemberToken yangMemberToken,BindingResult bindingResult1, @ApiIgnore @Validated({CurrencyGetAddress.class}) YangCurrencyUser yangCurrencyUser, BindingResult bindingResult){
//        Result result = new Result();
//        result.setCode(Result.Code.ERROR);
//        try{
//            /*数据验证*/
//            if(bindingResult.hasErrors()){
//                result.setMsg(getStringErrors(bindingResult));
//            }else if(bindingResult1.hasErrors()){
//                result.setMsg(getStringErrors(bindingResult1));
//            }else{
//                YangCurrencyUser currencyUser = yangMemberTokenService.getAddress(yangMemberToken, yangCurrencyUser);
//                result.setCode(Result.Code.SUCCESS);
//                result.setData(currencyUser);
//            }
//
//        }catch (ErrorTokenException e) {
//            e.printStackTrace();
//            result.setErrorCode(ErrorCode.ERROR_ERROR_TOKEN.getIndex());
//            result.setMsg(ErrorCode.ERROR_ERROR_TOKEN.getMessage());
//        }catch (TokenExpirException e) {
//            e.printStackTrace();
//            result.setErrorCode(ErrorCode.ERROR_ERROR_TOKEN_EXPIRE.getIndex());
//            result.setMsg(ErrorCode.ERROR_ERROR_TOKEN_EXPIRE.getMessage());
//        }catch (Exception e) {
//            e.printStackTrace();
//            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
//            result.setMsg(e.getMessage());
//        }
//        return result;
//    }
//
//    @RequestMapping(value = "validateAddress",method = RequestMethod.POST)
//    @ApiOperation(value = "获取用户充币地址", notes = "获取用户充币地址")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "accessToken", value = "用户登录后获取的票据", required = true, dataType = "String", paramType = "query"),
//            @ApiImplicitParam(name = "currencyId", value = "币种id", required = true, dataType = "Int", paramType = "query"),
//            @ApiImplicitParam(name = "chongzhiUrl", value = "地址", required = true, dataType = "String", paramType = "query"),
//    })
//    public Result validateAddress(@ApiIgnore @Validated YangMemberToken yangMemberToken,BindingResult bindingResult1, @ApiIgnore @Validated({CurrencyGetAddress.class}) YangCurrencyUser yangCurrencyUser, BindingResult bindingResult){
//        Result result = new Result();
//        result.setCode(Result.Code.ERROR);
//        try{
//            /*数据验证*/
//            if(bindingResult.hasErrors()){
//                result.setMsg(getStringErrors(bindingResult));
//            }else if(bindingResult1.hasErrors()){
//                result.setMsg(getStringErrors(bindingResult1));
//            }else{
//
//                Map whilist = new HashMap();
//                whilist.put(50,1);
//                whilist.put(37,1);
//
//                Boolean checked;
//
//                if(whilist.containsKey(yangCurrencyUser.getCurrencyId().intValue()))
//                {
//                    checked = true;
//                }else{
//                    checked = yangMemberTokenService.validateAddress(yangMemberToken, yangCurrencyUser);
//
//                }
//
//                if(true == checked){
//                    result.setErrorCode(ErrorCode.ERROR_VALIDATE_ADDRESS.getIndex());
//                    result.setCode(Result.Code.SUCCESS);
//                }else{
//                    result.setErrorCode(ErrorCode.ERROR_NOT_VALIDATE_ADDRESS.getIndex());
//                }
//
//            }
//
//        }catch (ErrorTokenException e) {
//            e.printStackTrace();
//            result.setErrorCode(ErrorCode.ERROR_ERROR_TOKEN.getIndex());
//            result.setMsg(ErrorCode.ERROR_ERROR_TOKEN.getMessage());
//        }catch (TokenExpirException e) {
//            e.printStackTrace();
//            result.setErrorCode(ErrorCode.ERROR_ERROR_TOKEN_EXPIRE.getIndex());
//            result.setMsg(ErrorCode.ERROR_ERROR_TOKEN_EXPIRE.getMessage());
//        }catch (Exception e) {
//            e.printStackTrace();
//            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
//            result.setMsg(e.getMessage());
//        }
//        return result;
//    }
//
//
//    @ResponseBody
//    @RequestMapping(value = "retrieveByTxId",method = RequestMethod.POST)
//    @ApiOperation(value = "给用户找回币", notes = "给用户找回币")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "currencyId", value = "币种id", required = true, dataType = "Int", paramType = "query"),
//            @ApiImplicitParam(name = "txId", value = "地址", required = true, dataType = "String", paramType = "query"),
//    })
//    public Result retrieveByTxId(String txId,Integer currencyId ){
//        Result result = new Result();
//        result.setCode(Result.Code.ERROR);
//        try{
//            /*数据验证*/
//            result = yangMemberTokenService.retrieveByTxId(txId, currencyId);
//        }catch (Exception e) {
//            e.printStackTrace();
//            result.setMsg(e.getMessage());
//        }
//        return result;
//    }
//
//
//
//    //充币记录
//
//    @RequestMapping(value = "chargeRecord",method = RequestMethod.POST)
//    @ApiOperation(value = "获取用户充币地址", notes = "获取用户充币地址")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "accessToken", value = "用户登录后获取的票据", required = true, dataType = "String", paramType = "query"),
//            @ApiImplicitParam(name = "currencyId", value = "币种id", required = true, dataType = "Int", paramType = "query"),
//            @ApiImplicitParam(name = "page", value = "当前分页", required = true, dataType = "Int", paramType = "query"),
//            @ApiImplicitParam(name = "pageSize", value = "分页条数", required = true, dataType = "Int", paramType = "query"),
//    })
//    public Result chargeRecord(@ApiIgnore @Validated YangMemberToken yangMemberToken, BindingResult bindingResult1, @ApiIgnore @Validated({ChargeRecord.class}) YangCurrencyUser yangCurrencyUser, BindingResult bindingResult){
//        Result result = new Result();
//        result.setCode(Result.Code.ERROR);
//        try{
//            /*数据验证*/
//            if(bindingResult.hasErrors()){
//                result.setMsg(getStringErrors(bindingResult));
//            }else if(bindingResult1.hasErrors()){
//                result.setMsg(getStringErrors(bindingResult1));
//            }else{
//                PageInfo yangTibis = yangMemberTokenService.chargeRecord(yangMemberToken, yangCurrencyUser);
//                result.setCode(Result.Code.SUCCESS);
//                result.setData(yangTibis);
//            }
//
//        }catch (ErrorTokenException e) {
//            e.printStackTrace();
//            result.setErrorCode(ErrorCode.ERROR_ERROR_TOKEN.getIndex());
//            result.setMsg(ErrorCode.ERROR_ERROR_TOKEN.getMessage());
//        }catch (TokenExpirException e) {
//            e.printStackTrace();
//            result.setErrorCode(ErrorCode.ERROR_ERROR_TOKEN_EXPIRE.getIndex());
//            result.setMsg(ErrorCode.ERROR_ERROR_TOKEN_EXPIRE.getMessage());
//        }catch (Exception e) {
//            e.printStackTrace();
//            result.setErrorCode(ErrorCode.ERROR_SYSTEM.getIndex());
//            result.setMsg(e.getMessage());
//        }
//        return result;
//    }
//
//}
