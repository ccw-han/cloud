package net.cyweb.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import net.cyweb.CoinUtils.CoinEntry.Base.BaseCoinI;
import net.cyweb.CoinUtils.CoinTools.CommonTools;
import net.cyweb.feignClient.MemberService;
import net.cyweb.model.Result;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class YangMemservice {

    @Autowired
    private CoinService coinService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private MemberService memberService;

    @Transactional
    public Result createUser(String memberId, String CoinId, BaseCoinI baseCoinI) throws RuntimeException,Exception
    {
        //先看看是不是已经有了 有的话 就直接返回对应的充值地址
        System.out.println("开始创建用户");
        Result result_return  = new Result();
        Result memberCurrency = memberService.getMemberCurrency(new Integer(memberId),Integer.valueOf(CoinId));
        System.out.println("获取到用户币种信息的信息"+memberCurrency);
        if(memberCurrency.getCode().equals(Result.Code.SUCCESS))
        {
            JSONObject memberCurrencyObject = JSON.parseObject(memberCurrency.getData().toString());

            if(StringUtils.isNotEmpty(memberCurrencyObject.getString("chongzhiUrl"))) //如果充值地址 存在的话
            {
                result_return.setCode(Result.Code.SUCCESS);
                result_return.setData(memberCurrencyObject.getString("chongzhiUrl"));  // 如果不存在充值地址
                return result_return;
            }

        }

        //如果上面都没有发现这个交易下没有地址 那么 继续
        Result result =  memberService.getmember(Integer.valueOf(memberId));

        System.out.println("获取到用户币种信息的信息"+result);

        if(result.getCode().equals(Result.Code.SUCCESS))
        {
            JSONObject memberObject = JSON.parseObject(result.getData().toString());
            String email = memberObject.getString("email");

            result_return.setCode(Result.Code.SUCCESS);
            String password2 = CommonTools.Md5(email);

            System.out.println("开始生成新的用户start---");
            String chongzhi_url = baseCoinI.createUser(password2);
            System.out.println("开始生成新的用户end---"+chongzhi_url);

            //更新用户的数据
            System.out.println("开始更新用户数据");
            Result creatememberCurrencyResult = memberService.createMemberCurrency(Integer.valueOf(memberId),Integer.valueOf(CoinId),chongzhi_url);
            System.out.println("更新用户数据完成");
            if(creatememberCurrencyResult.getCode().equals(Result.Code.SUCCESS))
            {
                result_return.setData(chongzhi_url);
            }else{
                throw new Exception("新增用户注册地址失败");
            }

        }else{
            result_return = result;
        }
        return result_return;
    }

}
