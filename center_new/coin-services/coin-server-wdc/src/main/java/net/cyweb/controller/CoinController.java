package net.cyweb.controller;

import net.cyweb.CoinUtils.CoinEntry.Base.BaseCoinI;
import net.cyweb.CoinUtils.CoinTools.CoinFactory;
import net.cyweb.feignClient.MemberService;
import net.cyweb.model.Result;
import net.cyweb.model.YangCurrency;
import net.cyweb.service.CoinService;
import net.cyweb.service.RedisService;
import net.cyweb.service.YangMemservice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(value = "/coin")
public class CoinController {

    private BaseCoinI coin;

    @Autowired
    private CoinService coinService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private YangMemservice yangMemservice;




    public void init(HttpServletRequest request) throws Exception
    {
        String CoinId = request.getParameter("currency_id");

        YangCurrency yangCurrency = coinService.getCurrencyInfo(Integer.valueOf(CoinId));

        this.coin = CoinFactory.createCoin(yangCurrency,redisService);

    }

    public Result getBalance(String address, HttpServletRequest request) {
        Result result = new Result();
        try {
            result.setCode(Result.Code.SUCCESS);
            result.setMsg(this.coin.num2real(this.coin.getBalance(address)).toString());
        }catch (Exception e)
        {

        }
        return null;
    }


    @RequestMapping(value = "creatUser")
    public Result createUser(String memberId,String currency_id,HttpServletRequest request) {
        Result result_return  = new Result();
        try {
            this.init(request);
            //这里是开始生成eth对象
            System.out.println(this.coin);
            System.out.println(this.coin.getAllAccount());
            result_return = yangMemservice.createUser(memberId,currency_id,this.coin);

        }catch (Exception e)
        {
            result_return.setCode(Result.Code.ERROR);
            result_return.setMsg(e.getMessage());
        }
//        加密算法 用来对邮箱进行加密
        return result_return;
    }


    @RequestMapping(value = "validateAddress")
    public Result validateAddress(String address,HttpServletRequest request)
    {
        Result result = new Result();

        try {
            this.init(request);
            if(this.coin.validateAddress(address))
            {
                result.setCode(Result.Code.SUCCESS);
            }else{
                result.setCode(Result.Code.ERROR);
            }
        }catch (Exception e)
        {
            result.setCode(Result.Code.ERROR);
            result.setMsg(e.getMessage());
        }

        return result;

    }





}
