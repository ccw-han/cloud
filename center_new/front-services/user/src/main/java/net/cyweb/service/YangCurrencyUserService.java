package net.cyweb.service;


import cyweb.utils.CoinConst;
import cyweb.utils.DateUtils;
import net.cyweb.mapper.YangCurrencyUserMapper;
import net.cyweb.mapper.YangFtActivityMapper;
import net.cyweb.model.*;
import net.cyweb.model.modelExt.YangCurrencyUserExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;


import java.math.BigDecimal;
import java.math.BigInteger;

import java.math.RoundingMode;
import java.util.Arrays;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class YangCurrencyUserService extends BaseService<YangCurrencyUser> {



    @Autowired
    private YangCurrencyUserMapper yangCurrencyUserMapper;

    @Autowired
    private YangTibiService yangTibiService;

    @Autowired
    private YangTibiNewService yangTibiNewService;

    @Autowired
    private YangMemberService yangMemberService;

    @Autowired
    private YangFtActivityMapper yangFtActivityMapper;

    @Autowired
    private RedisService redisService;


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
        yangCurrencyUser.setNumType("inc");

        YangCurrencyUser yangCurrencyUser1 = this.selectOne(yangCurrencyUser);  //找到是哪个用户的地址
        if(null != yangCurrencyUser1)
        {
            //开始充值
            //首先加入记录

            System.out.println("抓到一比充值记录记录，用户是"+yangCurrencyUser1.getMemberId()+"--充值的币种是"+yangCurrencyUser1.getCurrencyId()+"----充值的url是"+yangCurrencyUser1.getChongzhiUrl());


            //过滤
//            yangTibi =  this.validateLimit(yangTibi,yangCurrencyUser1);

            if(yangTibi != null)
            {
                yangTibi.setUserId(yangCurrencyUser1.getMemberId());
                yangTibiService.save(yangTibi);

            }


        }else{
//            System.out.println("找不到用户信息");
        }

        return result;
    }

    /**
     * 过滤一下
     * @return
     */
    private YangTibi  validateLimit(YangTibi yangTibi,YangCurrencyUser yangCurrencyUser1)
    {
        //限制ai 每天能够充币的每个人 只能是100个
        if (yangTibi.getCurrencyId().equals(Integer.valueOf(79))) //aicn
        {

            String date =  DateUtils.getDateStrPre(0);
            String key = CoinConst.COINLIMIT+yangTibi.getCurrencyId()+date+"_"+yangCurrencyUser1.getMemberId();
            Object o = this.redisService.get(key);

            BigDecimal max = BigDecimal.valueOf(100);

            BigDecimal hasIn;

            if(o != null)
            {
                hasIn =  BigDecimal.valueOf(Double.valueOf(o.toString()));
            }else{
                hasIn =  BigDecimal.ZERO;
            }



            BigDecimal left = max.subtract(hasIn);

            if(left.compareTo(BigDecimal.ZERO) <= 0)
            {
                return null;
            }


            Double canIn = Math.min(yangTibi.getNum().doubleValue(),left.doubleValue());

            yangTibi.setNum(BigDecimal.valueOf(canIn));



            this.redisService.set(key,BigDecimal.valueOf(canIn).add(hasIn),2l, TimeUnit.DAYS);

            return yangTibi;


        }

        return yangTibi;

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
    public  void  Confirm (List<YangTibi> yangTibisSuccess,List<YangTibi> yangTibisFalse,BigInteger blockNum,Double needConfirms) throws RuntimeException
    {


        if(null != yangTibisSuccess)
        {
            for (YangTibi y :yangTibisSuccess
                    ) {
                Double confirMations = blockNum.subtract(BigInteger.valueOf(Long.valueOf(y.getHeight()))).doubleValue();
                y.setConfirmations(confirMations);
                if(confirMations >= needConfirms)
                {
                    y.setStatus(Byte.valueOf("3"));
                    y.setConfirmations(needConfirms); //防止int类型超出范围
                    //先判断当前的充值记录数据库中是否有
                    YangCurrencyUser yangCurrencyUser = new YangCurrencyUser();

                    yangCurrencyUser.setCurrencyId(y.getCurrencyId());
                    yangCurrencyUser.setMemberId(y.getUserId());

                    System.out.println("开始充值到账：相关参数为：cuurency——id"+y.getCurrencyId()+"用户id"+y.getUserId());

                /*******充值开始********/
                    int i = yangMemberService.assets(
                            y.getUserId(),String.valueOf(y.getCurrencyId().intValue()),y.getNum(),yangCurrencyUser.getNumType(),BigDecimal.valueOf(0),"dec",
                            0,null,null,null,null,null,
                            0,null,null,null,null,null,
                            0,null,null,null,null,null,
                            0,null,null,null,null,null

                    );

                    if(i == 1)
                    {

                        System.out.println("调用assets储存过程出现异常");
                        throw new RuntimeException();
                    }

                    System.out.println("开始增加额度");

                    if(y.getCurrencyId().equals(Integer.valueOf(37)) || y.getCurrencyId().equals(Integer.valueOf(40)) ){

                        System.out.println("开始增加额度2");

                        BigDecimal nums = BigDecimal.ZERO;
                        if(y.getCurrencyId().equals(Integer.valueOf(37)) )
                        {
                            if(y.getNum().compareTo(BigDecimal.valueOf(0.2)) >=0)
                            {
                                nums = y.getNum().divide(BigDecimal.valueOf(0.2),6, RoundingMode.DOWN).multiply(BigDecimal.valueOf(200));

                            }
                        }else{
                            if(y.getNum().compareTo(BigDecimal.valueOf(0.01)) >=0)
                            nums = y.getNum().divide(BigDecimal.valueOf(0.01),6, RoundingMode.DOWN).multiply(BigDecimal.valueOf(200));

                        }


                        if(nums.compareTo(BigDecimal.ZERO) > 0)
                        {
                            System.out.println("开始增加额度"+nums);



                            YangFtActivity yangFtActivity = new YangFtActivity();
                            yangFtActivity.setMemberId(y.getUserId());

                            List<YangFtActivity> list1 = yangFtActivityMapper.select(yangFtActivity);

                            if(list1 != null && list1.size() > 0)
                            {
                                System.out.println("增加额度用户");

                                yangFtActivity.setNum(nums.add(list1.get(0).getNum()));

                                Example example = new Example(YangFtActivity.class);
                                example.createCriteria().andEqualTo("memberId",y.getUserId());

                                yangFtActivityMapper.updateByExampleSelective(yangFtActivity,example);

                            }else{
                                System.out.println("新增额度用户");

                                yangFtActivity.setAddTime(Integer.valueOf(String.valueOf(System.currentTimeMillis()/1000)));
                                yangFtActivity.setMemberId(y.getUserId());
                                yangFtActivity.setNum(nums);
                                yangFtActivity.setType(Integer.valueOf(0));
                                yangFtActivityMapper.insert(yangFtActivity);
                            }
                            System.out.println("增加额度成功"+nums);

                        }


                    }
                    /*******充值开始********/


//                    YangCurrencyUser yangCurrencyUser1 = this.selectOne(yangCurrencyUser);  //找到是哪个用户的地址
//                    yangCurrencyUser1.setNum(yangCurrencyUser1.getNum().add(y.getNum()));
//                    this.mapper.updateByPrimaryKey(yangCurrencyUser1);

                    System.out.println("有一笔充值确认成功，用户是"+y.getUserId()+"--充值的币种是"+y.getCurrencyId());


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
