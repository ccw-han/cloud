package net.cyweb.controller;

import net.cyweb.message.CodeMsg;

import net.cyweb.model.Result;
import net.cyweb.model.YangCurrencyUser;
import net.cyweb.model.YangTibi;
import net.cyweb.model.YangTibiNew;
import net.cyweb.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;
import tk.mybatis.mapper.entity.Example;

@RestController
@ApiIgnore
@RequestMapping(value = "/coin")
public class CoinController {

    @Autowired
    private YangMemberService funMemberService;

    @Autowired
    private YangCurrencyUserService funCurrencyUserService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private YangTibiNewService yangTibiNewService;

    @Autowired
    private YangTibiService yangTibiService;



    /*用户列表*/
    @RequestMapping(value = "list" , method = RequestMethod.GET)
    public Result list(){
        Result result = new Result();
        result.setCode(CodeMsg.ERROR.getIndex());
        result.setMsg(CodeMsg.ERROR.getMsg());
        try{

            YangCurrencyUser yangCurrencyUser = new YangCurrencyUser();
            yangCurrencyUser.setCurrencyId(Integer.valueOf(37));

            result.setData(funCurrencyUserService.getValidateCurrcyUser(yangCurrencyUser));

            result.setCode(CodeMsg.SUCCESS.getIndex());
            result.setMsg(CodeMsg.SUCCESS.getMsg());
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }


    /**
     * 给用户入账
     * @param yangTibi
     * @return
     */
    @RequestMapping(value = "updateUserCurrency")
    @Transactional
    public Result updateUserCurrency(YangTibi yangTibi)
    {

        Result result = new Result();
        yangTibiService.save(yangTibi);

        YangCurrencyUser yangCurrencyUser = new YangCurrencyUser();
        yangCurrencyUser.setMemberId(yangTibi.getUserId());
        yangCurrencyUser.setCurrencyId(yangTibi.getCurrencyId());
        yangCurrencyUser = funCurrencyUserService.selectOne(yangCurrencyUser);


        Example example = new Example(YangCurrencyUser.class);
        Example.Criteria criteria =  example.createCriteria();
        criteria.andEqualTo("memberId",yangTibi.getUserId());
        criteria.andEqualTo("currencyId",yangTibi.getCurrencyId());
        yangCurrencyUser.setNum(yangCurrencyUser.getNum().add(yangTibi.getNum()));

        funCurrencyUserService.updateByExampleSelective(yangCurrencyUser,example);
        result.setCode(Result.Code.SUCCESS);

        return result;

    }


}


