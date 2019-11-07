package net.cyweb.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import cyweb.utils.CoinConst;
import net.cyweb.message.CodeMsg;
import net.cyweb.model.*;
import net.cyweb.model.modelExt.YangCurrencyUserExt;
import net.cyweb.service.RedisService;
import net.cyweb.service.YangCurrencyUserService;
import net.cyweb.service.YangMemberService;
import net.cyweb.service.YangTibiService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;
import tk.mybatis.mapper.entity.Example;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;


@RestController
@ApiIgnore
@RequestMapping(value = "/member")
public class FunMemberController {
    @Autowired
    private YangMemberService funMemberService;

    @Autowired
    private YangCurrencyUserService yangCurrencyUserService;


    @Autowired
    private YangTibiService tibiService;

    @Autowired
    private RedisService redisService;

    @RequestMapping(value = "xssTest",method = RequestMethod.GET)
    public String xssTest(String xs)
    {
        return xs;
    }


    /*用户列表*/
    @RequestMapping(value = "list" , method = RequestMethod.GET)
    public Result list(){
        Result result = new Result();
        result.setCode(CodeMsg.ERROR.getIndex());
        result.setMsg(CodeMsg.ERROR.getMsg());
        try{
            result.setData(funMemberService.selectAll());
            result.setCode(CodeMsg.SUCCESS.getIndex());
            result.setMsg(CodeMsg.SUCCESS.getMsg());
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }

    @RequestMapping(value = "getmember" , method = RequestMethod.GET)
    public Result getMember(Integer memberId)
    {
        Result result = new Result();
        try {
            YangMember funMember = new YangMember();
            funMember.setMemberId(memberId);

            result.setCode(Result.Code.SUCCESS);
            result.setData(funMemberService.selectOne(funMember));

        }catch (Exception e)
        {
            result.setCode(Result.Code.ERROR);
            result.setMsg(e.getMessage());

        }

        return result;

    }

    @RequestMapping(value = "getMemberCurrency" , method = RequestMethod.GET)
    public Result getMemberCurrency(Integer memberId,Integer currency_id)
    {
        Result result = new Result();
        try {

            YangCurrencyUser yangCurrencyUser = new YangCurrencyUser();
            yangCurrencyUser.setCurrencyId(currency_id);
            yangCurrencyUser.setMemberId(memberId);

            YangCurrencyUser yangCurrencyUser1;

            if(currency_id==null || memberId == null)
            {
                throw new Exception("用户id或者币种id为空");
            }
            yangCurrencyUser1 = yangCurrencyUserService.selectOne(yangCurrencyUser);

            if(yangCurrencyUser1!= null)
            {
                result.setCode(Result.Code.SUCCESS);
                result.setData(yangCurrencyUser1);
            }else{
                result.setCode(Result.Code.ERROR);
                result.setMsg("找不到对应的用户币种信息");
            }



        }catch (Exception e)
        {
            e.printStackTrace();
            result.setCode(Result.Code.ERROR);
            result.setMsg(e.getMessage());

        }
        return result;
    }


    @RequestMapping(value = "getMemberCurrencyList" , method = RequestMethod.GET)
    public Result getMemberCurrencyList(Integer currency_id)
    {
        Result result = new Result();
        try {

            YangCurrency yangCurrency = new YangCurrency();
            yangCurrency.setCurrencyId(currency_id);
            List<YangCurrencyUserExt> yangCurrencyUser1 = yangCurrencyUserService.getYangCurrencyUserAndEmail(yangCurrency);


            if(yangCurrencyUser1!= null)
            {
                result.setCode(Result.Code.SUCCESS);
                result.setData(yangCurrencyUser1);
            }else{
                result.setCode(Result.Code.ERROR);
                result.setMsg("找不到对应的用户币种信息");
            }



        }catch (Exception e)
        {
            e.printStackTrace();
            result.setCode(Result.Code.ERROR);
            result.setMsg(e.getMessage());

        }
        return result;
    }

    @RequestMapping(value = "createMemberCurrency" , method = RequestMethod.GET)
    public Result createMemberCurrency(Integer memberId,Integer currency_id,String chongzhi_url)
    {
        Result result = new Result();
        try {

            System.out.println("接受到用户新增币种账号请求");
            YangCurrencyUser yangCurrencyUser = new YangCurrencyUser();

            //先看看账户里面有没有这个记录
            Example example = new Example(YangCurrencyUser.class);
            example.createCriteria().andEqualTo("memberId",memberId).andEqualTo("currencyId",currency_id);
            List<YangCurrencyUser> list =  yangCurrencyUserService.selectByExample(example);
            System.out.println("找到记录"+list.size());
            if(null != list && list.size() == 1)
            {
                System.out.println("找到用户记录，说明是有数据的");
                yangCurrencyUser = list.get(0);
                yangCurrencyUser.setChongzhiUrl(chongzhi_url);
            }else{
                yangCurrencyUser = new YangCurrencyUser();
                yangCurrencyUser.setCurrencyId(currency_id);
                yangCurrencyUser.setMemberId(memberId);
                yangCurrencyUser.setChongzhiUrl(chongzhi_url);
                yangCurrencyUser.setShow(Integer.valueOf(1));
                yangCurrencyUser.setForzenNum(BigDecimal.valueOf(0));
                yangCurrencyUser.setStatus(Byte.valueOf("0"));
                yangCurrencyUser.setNum(BigDecimal.valueOf(0));
            }

            yangCurrencyUserService.replaceIntochongzhiUrl(yangCurrencyUser);



            System.out.println("完成用户新增币种账号请求");


            result.setData(yangCurrencyUser);
            result.setCode(Result.Code.SUCCESS);

        }catch (Exception e)
        {
            result.setCode(Result.Code.ERROR);
            result.setMsg(e.getMessage());
            e.printStackTrace();
        }
        return  result;
    }

    @RequestMapping(value = "chongzhi" , method = RequestMethod.POST)
    public Result chongzhi(@RequestBody YangTibi yangTibi)
    {
        Result result = new Result();
        try {

            if(yangTibi.getCurrencyId() == null || StringUtils.isEmpty(yangTibi.getUrl()))
            {
                throw new Exception("currnet_id="+yangTibi.getCurrencyId()+"  url="+yangTibi.getUrl());
            }

            result = yangCurrencyUserService.chongzhi(yangTibi);
        }catch (Exception e)
        {
            result.setCode(Result.Code.ERROR);
            result.setMsg(e.getMessage());
//            e.printStackTrace();
        }
        return  result;
    }




    /**
     * 为以太系列需要多少个确认才能算充值成功的用
     * @param blockNum
     * @param currencyIds
     * @return
     */
    @RequestMapping(value = "confirm" , method = RequestMethod.POST)
    public Result confirm(BigInteger  blockNum, String currencyIds,int needConfirms)
    {
        Result result = new Result();
        try {
            //先拿到tibi表中 对应currencyId下的需要看确认数的
            List<YangTibi> list = yangCurrencyUserService.preConfirm(blockNum,currencyIds,needConfirms);
            result.setData(list);
            result.setCode(Result.Code.SUCCESS);

        }catch (Exception e)
        {
            result.setCode(Result.Code.ERROR);
            result.setMsg(e.getMessage());
            e.printStackTrace();
        }
        return  result;
    }

    /**
     *  List<YangTibi> yangTibisSuccess
     * @param blockNum
     * @param yangTibisSuccess
     * @param yangTibisFalse
     * @param needConfirms
     * @return
     */
    @RequestMapping(value = "finishConfirm" , method = RequestMethod.POST,consumes="application/json")
    public Result finishConfirm(BigInteger  blockNum,  String  yangTibisSuccess, String yangTibisFalse,double needConfirms)
    {
        Result result = new Result();

        JSONArray yangTibisSuccessArray = (JSONArray)JSONObject.parse(yangTibisSuccess);
        JSONArray yangTibisFalseArray = (JSONArray) JSONObject.parse(yangTibisFalse);

        List<YangTibi> yangTibisSuccesslist = new LinkedList<YangTibi>();
        yangTibisSuccesslist = cyweb.utils.CommonTools.json2Object(yangTibisSuccessArray,yangTibisSuccesslist,YangTibi.class);

        List<YangTibi> yangTibisFalselist = new LinkedList<YangTibi>();
        yangTibisFalselist = cyweb.utils.CommonTools.json2Object(yangTibisFalseArray,yangTibisFalselist,YangTibi.class);

        try {
            //先拿到tibi表中 对应currencyId下的需要看确认数的
           yangCurrencyUserService.Confirm(yangTibisSuccesslist,yangTibisFalselist,blockNum,needConfirms);
            result.setCode(Result.Code.SUCCESS);
        }catch (Exception e)
        {
            result.setCode(Result.Code.ERROR);
            result.setMsg(e.getMessage());
            e.printStackTrace();
        }
        return  result;
    }


    @RequestMapping(value = "flushCurrencyFlash" , method = RequestMethod.GET)
    public Result flushCurrencyFlash(String currency_ids, String key)
    {
        Result result = new Result();
        //获取当前币种的所有信息 包含代币
        try {
            String[] currency_ids_arr = currency_ids.split(",");

            List l = new LinkedList();
            for (String s: currency_ids_arr
                    ) {
                l.add(s);
            }

            Example example = new Example(YangCurrencyUser.class);
            Example.Criteria criteria =  example.createCriteria();
            criteria.andIn("currencyId",l);
            List<YangCurrencyUser> list =  yangCurrencyUserService.selectByExample(example);

            HashMap<String,YangCurrencyUser> hashMap = new HashMap();

            for (YangCurrencyUser y:list
                 ) {
                hashMap.put(y.getChongzhiUrl(),y);
            }

            redisService.hmSet(CoinConst.redis_currency_user_chizhiurl_keys,key,hashMap);
            result.setCode(Result.Code.SUCCESS);


        }catch (Exception e)
        {
            result.setCode(Result.Code.ERROR);
            result.setMsg(e.getMessage());
            e.printStackTrace();
        }
        return  result;


    }




}
