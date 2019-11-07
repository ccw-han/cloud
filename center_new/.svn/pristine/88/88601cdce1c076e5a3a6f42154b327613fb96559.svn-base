package net.cyweb.service;


import net.cyweb.mapper.YangCurrencyUserMapper;
import net.cyweb.model.*;
import net.cyweb.model.modelExt.YangCurrencyUserExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;


import java.math.BigDecimal;
import java.math.BigInteger;

import java.util.Arrays;

import java.util.List;

@Service
public class YangCurrencyUserService extends BaseService<YangCurrencyUser> {



    @Autowired
    private YangCurrencyUserMapper yangCurrencyUserMapper;

    @Autowired
    private YangTibiService yangTibiService;

    @Autowired
    private YangTibiNewService yangTibiNewService;


    public List<YangCurrencyUser> getValidateCurrcyUser(YangCurrencyUser funCurrencyUser)
    {
//        return  funCurrencyUserMapper.getValidateCurrcyUser(funCurrencyUser);
        return null;
    }


    @Transactional
    public Result chongzhi(YangTibi yangTibi) throws RuntimeException
    {
        Result result = new Result();
        result.setCode(Result.Code.SUCCESS);

        //先判断当前的充值记录数据库中是否有
        YangCurrencyUser yangCurrencyUser = new YangCurrencyUser();

        yangCurrencyUser.setCurrencyId(yangTibi.getCurrencyId());
        yangCurrencyUser.setChongzhiUrl(yangTibi.getUrl()); //url是平台地址


        YangCurrencyUser yangCurrencyUser1 = this.selectOne(yangCurrencyUser);  //找到是哪个用户的地址
        if(null != yangCurrencyUser1)
        {
            //开始充值
            //首先加入记录

            System.out.println("抓到一比充值记录记录，用户是"+yangCurrencyUser1.getMemberId()+"--充值的币种是"+yangCurrencyUser1.getCurrencyId()+"----充值的url是"+yangCurrencyUser1.getChongzhiUrl());

            yangTibi.setUserId(yangCurrencyUser1.getMemberId());
            yangTibiService.save(yangTibi);

            //如果状态是3的话  加余额
            if (yangTibi.getStatus().intValue() == 3)
            {
                System.out.println("给 用户加余额"+yangCurrencyUser1.getMemberId()+"--充值的币种是"+yangCurrencyUser1.getCurrencyId()+"----充值的url是"+yangCurrencyUser1.getChongzhiUrl());

                yangCurrencyUser1.setNum(yangCurrencyUser1.getNum().add(yangTibi.getNum()));
                this.mapper.updateByPrimaryKey(yangCurrencyUser1);
            }

        }

        return result;
    }



    public int replaceIntochongzhiUrl(YangCurrencyUser yangCurrencyUser)
    {
        return this.yangCurrencyUserMapper.replaceIntochongzhiUrl(yangCurrencyUser);
    }

    /**
     * 确认充值
     * @param blockNum
     * @param currencyIds
     * @throws RuntimeException
     */
    @Transactional
    public  List<YangTibi>  preConfirm (BigInteger blockNum,String currencyIds ,int needConfirms) throws RuntimeException
    {
        List linkedList = Arrays.asList(currencyIds.split(","));


        Example example = new Example(YangTibi.class);
        example.createCriteria().andEqualTo("status",2)
                .andIn("currencyId",linkedList);

        List<YangTibi> yangTibiList = yangTibiService.selectByExample(example);

        return yangTibiList;

    }


    /**
     * 确认充值
     * @param yangTibisSuccess
     * @param yangTibisFalse
     * @throws RuntimeException
     */
    @Transactional
    public  void  Confirm (List<YangTibi> yangTibisSuccess,List<YangTibi> yangTibisFalse,BigInteger blockNum,int needConfirms) throws RuntimeException
    {

        if(null != yangTibisSuccess)
        {
            for (YangTibi y :yangTibisSuccess
                    ) {
                Integer confirMations = blockNum.subtract(BigInteger.valueOf(Long.valueOf(y.getHeight()))).intValue();
                y.setConfirmations(confirMations);
                if(confirMations >= needConfirms)
                {
                    y.setStatus(Byte.valueOf("3"));

                    //先判断当前的充值记录数据库中是否有
                    YangCurrencyUser yangCurrencyUser = new YangCurrencyUser();

                    yangCurrencyUser.setCurrencyId(y.getCurrencyId());
                    yangCurrencyUser.setMemberId(y.getUserId());

                    System.out.println("开始充值到账：相关参数为：cuurency——id"+y.getCurrencyId()+"用户id"+y.getUserId());

                    YangCurrencyUser yangCurrencyUser1 = this.selectOne(yangCurrencyUser);  //找到是哪个用户的地址
                    yangCurrencyUser1.setNum(yangCurrencyUser1.getNum().add(y.getNum()));
                    this.mapper.updateByPrimaryKey(yangCurrencyUser1);

                    System.out.println("有一笔充值确认成功，用户是"+yangCurrencyUser1.getMemberId()+"--充值的币种是"+yangCurrencyUser1.getCurrencyId()+"----充值的url是"+yangCurrencyUser1.getChongzhiUrl());


                }
                yangTibiService.updateByPrimaryKeySelective(y);

            }
        }


        //这里处理验证失败的数据

        if(null != yangTibisFalse)
        {
            for (YangTibi y :yangTibisFalse
                    ) {
                y.setStatus(Byte.valueOf("-1"));
                yangTibiService.updateByPrimaryKeySelective(y);

            }
        }


    }


    public List<YangCurrencyUserExt> getYangCurrencyUserAndEmail(YangCurrency yangCurrency)
    {
        return this.yangCurrencyUserMapper.getYangCurrencyUserAndEmail(yangCurrency);
    }


    /**
     *  检查用户余额是否足够
     * @param money
     * @param forzen  如果为 true 则把冻结金额计算在内
     * @return
     */
    public boolean checkUserMoney(BigDecimal money,boolean forzen,String memberId,String currencyId)
    {
        YangCurrencyUser yangCurrencyUser = new YangCurrencyUser();
        yangCurrencyUser.setCurrencyId(Integer.valueOf(currencyId));
        yangCurrencyUser.setMemberId(Integer.valueOf(memberId));
        yangCurrencyUser = this.yangCurrencyUserMapper.selectOne(yangCurrencyUser);
        if(yangCurrencyUser == null) //找不到记录 则肯定金额是不足的
        {
            return false;
        }else{
            BigDecimal left = yangCurrencyUser.getNum();
            if(forzen)
            {
                left.add(yangCurrencyUser.getForzenNum());
            }
            if(money.compareTo(left) > 0 )
            {
                return  false;
            }else{
                return  true ;
            }
        }
    }


}
