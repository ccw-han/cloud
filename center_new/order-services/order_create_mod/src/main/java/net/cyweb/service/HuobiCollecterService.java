package net.cyweb.service;

import com.alibaba.druid.support.json.JSONUtils;
import com.alibaba.fastjson.JSONObject;
import cyweb.utils.CoinConst;
import net.cyweb.config.huobiapi.*;
import net.cyweb.model.YangCollectConfig;
import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class HuobiCollecterService {

    private YangCollectConfig yangCollectConfig;

    @Autowired
    private YangCollectConfigService yangCollectConfigService;

    @Autowired
    private RedisService redisService;


    public void  initBaseInfo(YangCollectConfig yangCollectConfig){
        this.yangCollectConfig = yangCollectConfig;
    }

    public boolean doCollect() throws Exception
    {

        try {

            String url = "https://www.huobi.com/zh-cn/";
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla")
                    .timeout(3000)
                    .get();

            Elements price = doc.select("div .coin_list"); //带有href属性的a元素


            /*用來封裝要保存的参数*/
            Details details=new Details();
            String usdtRate = this.yangCollectConfig.getExtdata();
            String usdtGlobal="";
            if(redisService.exists(CoinConst.USDT_GLOBAL)){
             usdtGlobal=JSONUtils.toJSONString(redisService.get(CoinConst.USDT_GLOBAL));
            }
            String usdtFinalRate;
            if(StringUtils.isNotEmpty(usdtRate)){
                usdtFinalRate=usdtRate;
            }else{
                if(StringUtils.isNotEmpty(usdtGlobal)){
                    usdtFinalRate=usdtGlobal;
                }else{
                    System.out.printf(this.yangCollectConfig.getCid()+"--------火币未设置汇率");
                    return false;
                }
            }

//            初始化api  传入accessKeyId 和 accessKeySecret
            Apiclient apiclient =new Apiclient("073ba040-c2d2787a-0f490b33-68189","fd77b6c1-6c2ee989-5067bb63-b2753");
//            KlineResponse<List<Kline>> klineResponse= apiclient.kline(yangCollectConfig.getCurrencyMark().toLowerCase()+"usdt","1min","1");
//            MergedResponse<List<Merged>> klineResponseA= apiclient.merged(yangCollectConfig.getCurrencyMark().toLowerCase()+"usdt");
            DetailResponse<Details> klineResponse= apiclient.detail(yangCollectConfig.getCurrencyMark().toLowerCase()+"usdt");
            if(klineResponse!=null&&"ok".equals(klineResponse.getStatus())){
//                List<Kline> list=JSONObject.parseArray(klineResponse.data.toString(), Kline.class);
                Details list= klineResponse.getTick();
                if(list==null){
                    System.out.println("采集配置区----"+yangCollectConfig.getCurrencyMark()+"币种  对应火币价格设置失败");
                    return false;
                }else{
                    details=list;
                }
            }else{
                System.out.println("采集配置区----"+yangCollectConfig.getCurrencyMark()+"币种  对应火币价格设置失败");
                return false;
            }

            BigDecimal priceFinal = new BigDecimal(details.getClose()).multiply(new BigDecimal(usdtFinalRate));
            if(priceFinal.compareTo( this.yangCollectConfig.getSafePriceMax() ) > 0  ||  priceFinal.compareTo(this.yangCollectConfig.getSafePriceMin() ) < 0)
            {
                System.out.printf(this.yangCollectConfig.getCid()+"火币价格超过限制");
                return false;
            }

            //然后设置krw的价格
            BigDecimal priceFinalKrw  = priceFinal.multiply(BigDecimal.valueOf(165));

            //修正为管理员设定的价格
            if(this.yangCollectConfig.getUserDefinedBl() != null && this.yangCollectConfig.getUserDefinedBl().compareTo(BigDecimal.ZERO) > 0)
            {
                System.out.println("设定比例为"+this.yangCollectConfig.getUserDefinedBl());
                priceFinalKrw =  priceFinalKrw.multiply(this.yangCollectConfig.getUserDefinedBl());

            }

            //存入redis  注释掉  就只走 非小号
            String key = CoinConst.CURRENCYPRICEBYCOLLECT+this.yangCollectConfig.getCid();
            redisService.set(key,priceFinalKrw);
            System.out.println(priceFinalKrw+"<><><><><><>");
            this.yangCollectConfig.setPrice(priceFinalKrw);

            //价格换算　反填　　配置表
            yangCollectConfigService.updateByPrimaryKeySelective(this.yangCollectConfig);
            System.out.println("火币采集的价格为"+ priceFinal.toString());
            System.out.println(klineResponse.toString());
            return false;
        }catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }

    }

}
