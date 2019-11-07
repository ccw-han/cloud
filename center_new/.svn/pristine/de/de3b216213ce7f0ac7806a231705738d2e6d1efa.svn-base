package net.cyweb.service;

import net.cyweb.model.YangCollectConfig;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.math.BigDecimal;

import static cyweb.utils.CoinConst.CURRENCYPRICEBYCOLLECT;

@Service
public class FeiXiaoHaoCollecterService {

        private YangCollectConfig yangCollectConfig;


        @Autowired
        private RedisService redisService;

        @Autowired
        private YangCollectConfigService yangCollectConfigService;


        public void  initBaseInfo(YangCollectConfig yangCollectConfig){
            this.yangCollectConfig = yangCollectConfig;
        }

//        @Async
        public boolean doCollect() throws Exception
        {

            String url = this.yangCollectConfig.getExtdata();
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla")
                    .timeout(3000)
                    .get();
            Elements price = doc.select("div .coinprice"); //带有href属性的a元素
            String prices = price.first().childNodes().get(0).toString().replaceAll("\\n|,|￥","");
            BigDecimal priceFinal = BigDecimal.valueOf(Double.valueOf(prices));
            if(priceFinal.compareTo( this.yangCollectConfig.getSafePriceMax() ) > 0  ||  priceFinal.compareTo(this.yangCollectConfig.getSafePriceMin() ) < 0)
            {
                System.out.printf(this.yangCollectConfig.getCid()+"价格超过限制");
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

            String key = CURRENCYPRICEBYCOLLECT+this.yangCollectConfig.getCid();
            redisService.set(key,priceFinalKrw);
            this.yangCollectConfig.setPrice(priceFinalKrw);

            yangCollectConfigService.updateByPrimaryKeySelective(this.yangCollectConfig);
            System.out.printf("采集的价格为"+ BigDecimal.valueOf(Double.valueOf(prices)));
            return false;
        }

        public static void main(String[] args)
        {
            FeiXiaoHaoCollecterService feiXiaoHaoCollecter = new FeiXiaoHaoCollecterService();
            try {
                YangCollectConfig yangCollectConfig = new YangCollectConfig();
                yangCollectConfig.setExtdata("https://www.feixiaohao.com/currencies/ethereum/");
                feiXiaoHaoCollecter.initBaseInfo(yangCollectConfig);
                feiXiaoHaoCollecter.doCollect();

            }catch (Exception e)
            {

            }
        }

}
