package net.cyweb.feignClient;

import net.cyweb.message.CodeMsg;
import net.cyweb.model.Result;
import net.cyweb.model.YangTibi;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigInteger;


@Component
public class MemberServiceHystric implements MemberService{
    @Override
    public Result getmember(@RequestParam("memberId") Integer memberId) {
        Result result = new Result();
        result.setCode(CodeMsg.ERROR_OUT_OF_SERVICE.getIndex());
        result.setMsg(CodeMsg.ERROR_OUT_OF_SERVICE.getMsg());
        return result;
    }

    public Result getMemberCurrency(@RequestParam("memberId") Integer memberId,@RequestParam("currency_id") Integer currency_id){
        Result result = new Result();
        result.setCode(CodeMsg.ERROR_OUT_OF_SERVICE.getIndex());
        result.setMsg(CodeMsg.ERROR_OUT_OF_SERVICE.getMsg());
        return result;
    }


    public Result createMemberCurrency(@RequestParam("memberId") Integer memberId,@RequestParam("currency_id") Integer currency_id,@RequestParam("chongzhi_url")  String chongzhi_url){
        Result result = new Result();
        result.setCode(CodeMsg.ERROR_OUT_OF_SERVICE.getIndex());
        result.setMsg(CodeMsg.ERROR_OUT_OF_SERVICE.getMsg());
        return result;
    }

    public Result chongzhi(@RequestBody(required = false) YangTibi yangTibi){
        Result result = new Result();
        result.setCode(CodeMsg.ERROR_OUT_OF_SERVICE.getIndex());
        result.setMsg(CodeMsg.ERROR_OUT_OF_SERVICE.getMsg());
        return result;
    }

    public Result confirm(@RequestParam BigInteger blockNum,@RequestParam String currencyIds,@RequestParam int  needConfirms){
        Result result = new Result();
        result.setCode(CodeMsg.ERROR_OUT_OF_SERVICE.getIndex());
        result.setMsg(CodeMsg.ERROR_OUT_OF_SERVICE.getMsg());
        return result;
    }

    public Result finishConfirm(@RequestParam("blockNum") BigInteger blockNum,@RequestParam("yangTibisSuccess") String yangTibisSuccess,@RequestParam("yangTibisFalse") String yangTibisFalse ,@RequestParam("needConfirms") int  needConfirms){
        Result result = new Result();
        result.setCode(CodeMsg.ERROR_OUT_OF_SERVICE.getIndex());
        result.setMsg(CodeMsg.ERROR_OUT_OF_SERVICE.getMsg());
        return result;
    }



    public Result getMemberCurrencyList(@RequestParam("currency_id") Integer currency_id){
        Result result = new Result();
        result.setCode(CodeMsg.ERROR_OUT_OF_SERVICE.getIndex());
        result.setMsg(CodeMsg.ERROR_OUT_OF_SERVICE.getMsg());
        return result;
    }

    public Result flushCurrencyFlash(@RequestParam("currency_id") String currency_id,@RequestParam("key") String key){
        Result result = new Result();
        result.setCode(CodeMsg.ERROR_OUT_OF_SERVICE.getIndex());
        result.setMsg(CodeMsg.ERROR_OUT_OF_SERVICE.getMsg());
        return result;
    }


}
