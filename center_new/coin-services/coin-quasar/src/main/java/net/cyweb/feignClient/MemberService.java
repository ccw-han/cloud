package net.cyweb.feignClient;

import net.cyweb.model.Result;
import net.cyweb.model.YangTibi;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigInteger;

@FeignClient(value = "user-server",fallback = MemberServiceHystric.class)
public interface MemberService {



    @RequestMapping(value = "/member/getmember",method = RequestMethod.GET)
    Result getmember(@RequestParam("memberId") Integer memberId);

    @RequestMapping(value = "/member/getMemberCurrency",method = RequestMethod.GET)
    Result getMemberCurrency(@RequestParam("memberId") Integer memberId, @RequestParam("currency_id") Integer currency_id);

    @RequestMapping(value = "/member/createMemberCurrency",method = RequestMethod.GET)
    Result createMemberCurrency(@RequestParam("memberId") Integer memberId, @RequestParam("currency_id") Integer currency_id, @RequestParam("chongzhi_url") String chongzhi_url);


    @RequestMapping(value = "/member/chongzhi",method = RequestMethod.POST,consumes="application/json")
    Result chongzhi(YangTibi yangTibi);

    @RequestMapping(value = "/member/confirm",method = RequestMethod.POST,consumes="application/json")
    Result confirm(@RequestParam("blockNum") BigInteger blockNum, @RequestParam("currencyIds") String currencyIds, @RequestParam("needConfirms") int needConfirms);

    @RequestMapping(value = "/member/finishConfirm",method = RequestMethod.POST,consumes="application/json")
    Result finishConfirm(@RequestParam("blockNum") BigInteger blockNum, @RequestParam("yangTibisSuccess") String yangTibisSuccess, @RequestParam("yangTibisFalse") String yangTibisFalse, @RequestParam("needConfirms") int needConfirms);


    @RequestMapping(value = "/member/getMemberCurrencyList",method = RequestMethod.GET)
    Result getMemberCurrencyList(@RequestParam("currency_id") Integer currency_id);


    @RequestMapping(value = "/member/flushCurrencyFlash",method = RequestMethod.GET)
    Result flushCurrencyFlash(@RequestParam("currency_id") String currency_id, @RequestParam("key") String key);

}
