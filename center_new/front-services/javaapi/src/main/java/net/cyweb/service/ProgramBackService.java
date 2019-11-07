package net.cyweb.service;

import cyweb.utils.DateUtils;
import cyweb.utils.DecimalUtils;
import net.cyweb.config.custom.RemoteAccessUtils;
import net.cyweb.mapper.ProgramBackMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@PropertySource(value = "classpath:conf/config.properties")
public class ProgramBackService {
    @Autowired
    private ProgramBackMapper programBackMapper;

    public List getAllSymbols(TreeMap param) {
        List list = new ArrayList();
        String url = RemoteAccessUtils.accessUrl("/s_api/basic/symbols", "www");
        try {
            Object object = RemoteAccessUtils.get(url);
            list = (List<Map<String, String>>) object;
            return list;
        } catch (Exception e) {
            //返回异常
            return null;
        }
    }

    public List getAvePrice(TreeMap param) {
        List<Map<String, String>> list = new ArrayList();
        List<Map<String, String>> lastDatas = new ArrayList();
        TreeMap<String, String> paramData = new TreeMap<String, String>();
        List<Map<String, String>> symbols = this.getAllSymbols(paramData);
        if (symbols != null && symbols.size() > .0) {
            for (Map<String, String> data : symbols) {
                String symbolName = data.get("symbolName");
                //去调用另外一个查询订单接口
                String firstUrl = "/openapi/quote/v1/trades";
                String paramUrl = "?symbol=" + symbolName + "".trim();
                String lastUrl = firstUrl + paramUrl;
                String url = RemoteAccessUtils.accessUrl(lastUrl, "open");
                Object object = null;
                try {
                    object = RemoteAccessUtils.get(url);
                    if (object != null) {
                        list = (List<Map<String, String>>) object;
                        for (Map<String, String> firstData : list) {
                            lastDatas.add(firstData);
                        }
                    }
                } catch (IOException e) {
                    return null;
                }
            }
        } else {
            return null;
        }
        return lastDatas;
    }

    public Map<String, List<Map<String, String>>> aveToDataBase(List<Map<String, String>> datas) {
        List<Map<String, String>> buyDatas = new ArrayList<>();
        List<Map<String, String>> sellDatas = new ArrayList<>();
        for (Map data : datas) {
            String type = data.get("isBuyerMaker") + "";
            if ("true".equals(type)) {
                //买
                buyDatas.add(data);
            } else {
                sellDatas.add(data);
            }

        }
        //时间间隔
        long start1 = DateUtils.getStartMonthDate(2019, 1).getTime();
        long end1 = DateUtils.getEndMonthDate(2019, 1).getTime();
        long start2 = DateUtils.getStartMonthDate(2019, 2).getTime();
        long end2 = DateUtils.getEndMonthDate(2019, 2).getTime();
        long start3 = DateUtils.getStartMonthDate(2019, 3).getTime();
        long end3 = DateUtils.getEndMonthDate(2019, 3).getTime();
        long start4 = DateUtils.getStartMonthDate(2019, 4).getTime();
        long end4 = DateUtils.getEndMonthDate(2019, 4).getTime();
        long start5 = DateUtils.getStartMonthDate(2019, 5).getTime();
        long end5 = DateUtils.getEndMonthDate(2019, 5).getTime();
        long start6 = DateUtils.getStartMonthDate(2019, 6).getTime();
        long end6 = DateUtils.getEndMonthDate(2019, 6).getTime();
        long start7 = DateUtils.getStartMonthDate(2019, 7).getTime();
        long end7 = DateUtils.getEndMonthDate(2019, 7).getTime();
        long start8 = DateUtils.getStartMonthDate(2019, 8).getTime();
        long end8 = DateUtils.getEndMonthDate(2019, 8).getTime();
        long start9 = DateUtils.getStartMonthDate(2019, 9).getTime();
        long end9 = DateUtils.getEndMonthDate(2019, 9).getTime();
        long start10 = DateUtils.getStartMonthDate(2019, 10).getTime();
        long end10 = DateUtils.getEndMonthDate(2019, 10).getTime();
        long start11 = DateUtils.getStartMonthDate(2019, 11).getTime();
        long end11 = DateUtils.getEndMonthDate(2019, 11).getTime();
        long start12 = DateUtils.getStartMonthDate(2019, 12).getTime();
        long end12 = DateUtils.getEndMonthDate(2019, 12).getTime();
        //算买的
        //满足时间要求的list
        List<Map<String, String>> oneDatas1 = new ArrayList<>();
        List<Map<String, String>> twoDatas1 = new ArrayList<>();
        List<Map<String, String>> threeDatas1 = new ArrayList<>();
        List<Map<String, String>> fourDatas1 = new ArrayList<>();
        List<Map<String, String>> fiveDatas1 = new ArrayList<>();
        List<Map<String, String>> sixDatas1 = new ArrayList<>();
        List<Map<String, String>> sevenDatas1 = new ArrayList<>();
        List<Map<String, String>> eightDatas1 = new ArrayList<>();
        List<Map<String, String>> nineDatas1 = new ArrayList<>();
        List<Map<String, String>> tenDatas1 = new ArrayList<>();
        List<Map<String, String>> eleDatas1 = new ArrayList<>();
        List<Map<String, String>> tweDatas1 = new ArrayList<>();
        for (Map data : buyDatas) {
            long time = Long.parseLong(data.get("time").toString());
            if (time >= start1 && time <= end1) {
                oneDatas1.add(data);
            }
            if (time >= start2 && time <= end2) {
                twoDatas1.add(data);
            }
            if (time >= start3 && time <= end3) {
                threeDatas1.add(data);
            }
            if (time >= start4 && time <= end4) {
                fourDatas1.add(data);
            }
            if (time >= start5 && time <= end5) {
                fiveDatas1.add(data);
            }
            if (time >= start6 && time <= end6) {
                sixDatas1.add(data);
            }
            if (time >= start7 && time <= end7) {
                sevenDatas1.add(data);
            }
            if (time >= start8 && time <= end8) {
                eightDatas1.add(data);
            }
            if (time >= start9 && time <= end9) {
                nineDatas1.add(data);
            }
            if (time >= start10 && time <= end10) {
                tenDatas1.add(data);
            }
            if (time >= start11 && time <= end11) {
                eleDatas1.add(data);
            }
            if (time >= start12 && time <= end12) {
                tweDatas1.add(data);
            }
        }
        //第一个月的数据
        Map<String, String> data1 = new HashMap<>();
        String allPrice1 = "0";
        String count1 = oneDatas1.size() + "";
        if (oneDatas1.size() > 0) {
            for (Map<String, String> data : oneDatas1) {
                String price = data.get("price");
                allPrice1 = DecimalUtils.addition(allPrice1, price);
            }
            String avePrice1 = DecimalUtils.division(allPrice1, count1, 8);
            data1.put("time", DateUtils.getDataStampStr(start1));
            data1.put("avePrice", avePrice1);
            data1.put("buy", "1");
        } else {
            data1.put("time", DateUtils.getDataStampStr(start1));
            data1.put("avePrice", "0");
            data1.put("buy", "1");
        }

        //第2个月的数据
        Map<String, String> data2 = new HashMap<>();
        String allPrice2 = "0";
        String count2 = twoDatas1.size() + "";
        if (twoDatas1.size() > 0) {
            for (Map<String, String> data : twoDatas1) {
                String price = data.get("price");
                allPrice2 = DecimalUtils.addition(allPrice2, price);
            }
            String avePrice2 = DecimalUtils.division(allPrice2, count2, 8);
            data2.put("time", DateUtils.getDataStampStr(start2));
            data2.put("avePrice", avePrice2);
            data2.put("buy", "1");
        } else {
            data2.put("time", DateUtils.getDataStampStr(start2));
            data2.put("avePrice", "0");
            data2.put("buy", "1");
        }

        //第3个月的数据
        Map<String, String> data3 = new HashMap<>();
        String allPrice3 = "0";
        String count3 = threeDatas1.size() + "";
        if (threeDatas1.size() > 0) {
            for (Map<String, String> data : threeDatas1) {
                String price = data.get("price");
                allPrice3 = DecimalUtils.addition(allPrice3, price);
            }
            String avePrice3 = DecimalUtils.division(allPrice3, count3, 8);
            data3.put("time", DateUtils.getDataStampStr(start3));
            data3.put("avePrice", avePrice3);
            data3.put("buy", "1");
        } else {
            data3.put("time", DateUtils.getDataStampStr(start3));
            data3.put("avePrice", "0");
            data3.put("buy", "1");
        }

        //第4个月的数据
        Map<String, String> data4 = new HashMap<>();
        String allPrice4 = "0";
        String count4 = fourDatas1.size() + "";
        if (fourDatas1.size() > 0) {
            for (Map<String, String> data : fourDatas1) {
                String price = data.get("price");
                allPrice4 = DecimalUtils.addition(allPrice4, price);
            }
            String avePrice4 = DecimalUtils.division(allPrice4, count4, 8);
            data4.put("time", DateUtils.getDataStampStr(start4));
            data4.put("avePrice", avePrice4);
            data4.put("buy", "1");
        } else {
            data4.put("time", DateUtils.getDataStampStr(start4));
            data4.put("avePrice", "0");
            data4.put("buy", "1");
        }

        //第5个月的数据
        Map<String, String> data5 = new HashMap<>();
        String allPrice5 = "0";
        String count5 = fiveDatas1.size() + "";
        if (fiveDatas1.size() > 0) {
            for (Map<String, String> data : fiveDatas1) {
                String price = data.get("price");
                allPrice5 = DecimalUtils.addition(allPrice5, price);
            }
            String avePrice5 = DecimalUtils.division(allPrice5, count5, 8);
            data5.put("time", DateUtils.getDataStampStr(start5));
            data5.put("avePrice", avePrice5);
            data5.put("buy", "1");
        } else {
            data5.put("time", DateUtils.getDataStampStr(start5));
            data5.put("avePrice", "0");
            data5.put("buy", "1");
        }

        //第6个月的数据
        Map<String, String> data6 = new HashMap<>();
        String allPrice6 = "0";
        String count6 = sixDatas1.size() + "";
        if (sixDatas1.size() > 0) {
            for (Map<String, String> data : sixDatas1) {
                String price = data.get("price");
                allPrice6 = DecimalUtils.addition(allPrice6, price);
            }
            String avePrice6 = DecimalUtils.division(allPrice6, count6, 8);
            data6.put("time", DateUtils.getDataStampStr(start6));
            data6.put("avePrice", avePrice6);
            data6.put("buy", "1");
        } else {
            data6.put("time", DateUtils.getDataStampStr(start6));
            data6.put("avePrice", "0");
            data6.put("buy", "1");
        }

        //第7个月的数据
        Map<String, String> data7 = new HashMap<>();
        String allPrice7 = "0";
        String count7 = sevenDatas1.size() + "";
        if (sevenDatas1.size() > 0) {
            for (Map<String, String> data : sevenDatas1) {
                String price = data.get("price");
                allPrice7 = DecimalUtils.addition(allPrice7, price);
            }
            String avePrice7 = DecimalUtils.division(allPrice7, count7, 8);
            data7.put("time", DateUtils.getDataStampStr(start7));
            data7.put("avePrice", avePrice7);
            data7.put("buy", "1");
        } else {
            data7.put("time", DateUtils.getDataStampStr(start7));
            data7.put("avePrice", "0");
            data7.put("buy", "1");
        }

        //第8个月的数据
        Map<String, String> data8 = new HashMap<>();
        String allPrice8 = "0";
        String count8 = eightDatas1.size() + "";
        if (eightDatas1.size() > 0) {
            for (Map<String, String> data : eightDatas1) {
                String price = data.get("price");
                allPrice8 = DecimalUtils.addition(allPrice8, price);
            }
            String avePrice8 = DecimalUtils.division(allPrice8, count8, 8);
            data8.put("time", DateUtils.getDataStampStr(start8));
            data8.put("avePrice", avePrice8);
            data8.put("buy", "1");
        } else {
            data8.put("time", DateUtils.getDataStampStr(start8));
            data8.put("avePrice", "0");
            data8.put("buy", "1");
        }

        //第9个月的数据
        Map<String, String> data9 = new HashMap<>();
        String allPrice9 = "0";
        String count9 = nineDatas1.size() + "";
        if (nineDatas1.size() > 0) {
            for (Map<String, String> data : nineDatas1) {
                String price = data.get("price");
                allPrice9 = DecimalUtils.addition(allPrice9, price);
            }
            String avePrice9 = DecimalUtils.division(allPrice9, count9, 8);
            data9.put("time", DateUtils.getDataStampStr(start9));
            data9.put("avePrice", avePrice9);
            data9.put("buy", "1");
        } else {
            data9.put("time", DateUtils.getDataStampStr(start9));
            data9.put("avePrice", "0");
            data9.put("buy", "1");
        }

        //第10个月的数据
        Map<String, String> data10 = new HashMap<>();
        String allPrice10 = "0";
        String count10 = tenDatas1.size() + "";
        if (tenDatas1.size() > 0) {
            for (Map<String, String> data : tenDatas1) {
                String price = data.get("price");
                allPrice10 = DecimalUtils.addition(allPrice10, price);
            }
            String avePrice10 = DecimalUtils.division(allPrice10, count10, 8);
            data10.put("time", DateUtils.getDataStampStr(start10));
            data10.put("avePrice", avePrice10);
            data10.put("buy", "1");
        } else {
            data10.put("time", DateUtils.getDataStampStr(start10));
            data10.put("avePrice", "0");
            data10.put("buy", "1");
        }

        //第11个月的数据
        Map<String, String> data11 = new HashMap<>();
        String allPrice11 = "0";
        String count11 = eleDatas1.size() + "";
        if (eleDatas1.size() > 0) {
            for (Map<String, String> data : eleDatas1) {
                String price = data.get("price");
                allPrice11 = DecimalUtils.addition(allPrice11, price);
            }
            String avePrice11 = DecimalUtils.division(allPrice11, count11, 8);
            data11.put("time", DateUtils.getDataStampStr(start11));
            data11.put("avePrice", avePrice11);
            data11.put("buy", "1");
        } else {
            data11.put("time", DateUtils.getDataStampStr(start11));
            data11.put("avePrice", "0");
            data11.put("buy", "1");
        }

        //第12个月的数据
        Map<String, String> data12 = new HashMap<>();
        String allPrice12 = "0";
        String count12 = tweDatas1.size() + "";
        if (tweDatas1.size() > 0) {
            for (Map<String, String> data : tweDatas1) {
                String price = data.get("price");
                allPrice12 = DecimalUtils.addition(allPrice12, price);
            }
            String avePrice12 = DecimalUtils.division(allPrice12, count12, 8);
            data12.put("time", DateUtils.getDataStampStr(start12));
            data12.put("avePrice", avePrice12);
            data12.put("buy", "1");
        } else {
            data12.put("time", DateUtils.getDataStampStr(start12));
            data12.put("avePrice", "0");
            data12.put("buy", "1");
        }

        List<Map<String, String>> lastBuyAve = new ArrayList<>();
        lastBuyAve.add(data1);
        lastBuyAve.add(data2);
        lastBuyAve.add(data3);
        lastBuyAve.add(data4);
        lastBuyAve.add(data5);
        lastBuyAve.add(data6);
        lastBuyAve.add(data7);
        lastBuyAve.add(data8);
        lastBuyAve.add(data9);
        lastBuyAve.add(data10);
        lastBuyAve.add(data11);
        lastBuyAve.add(data12);

        //算卖的
        //满足时间要求的list
        List<Map<String, String>> oneDatas11 = new ArrayList<>();
        List<Map<String, String>> twoDatas11 = new ArrayList<>();
        List<Map<String, String>> threeDatas11 = new ArrayList<>();
        List<Map<String, String>> fourDatas11 = new ArrayList<>();
        List<Map<String, String>> fiveDatas11 = new ArrayList<>();
        List<Map<String, String>> sixDatas11 = new ArrayList<>();
        List<Map<String, String>> sevenDatas11 = new ArrayList<>();
        List<Map<String, String>> eightDatas11 = new ArrayList<>();
        List<Map<String, String>> nineDatas11 = new ArrayList<>();
        List<Map<String, String>> tenDatas11 = new ArrayList<>();
        List<Map<String, String>> eleDatas11 = new ArrayList<>();
        List<Map<String, String>> tweDatas11 = new ArrayList<>();
        for (Map data : sellDatas) {
            long time = Long.parseLong(data.get("time").toString());
            if (time >= start1 && time <= end1) {
                oneDatas11.add(data);
            }
            if (time >= start2 && time <= end2) {
                twoDatas11.add(data);
            }
            if (time >= start3 && time <= end3) {
                threeDatas11.add(data);
            }
            if (time >= start4 && time <= end4) {
                fourDatas11.add(data);
            }
            if (time >= start5 && time <= end5) {
                fiveDatas11.add(data);
            }
            if (time >= start6 && time <= end6) {
                sixDatas11.add(data);
            }
            if (time >= start7 && time <= end7) {
                sevenDatas11.add(data);
            }
            if (time >= start8 && time <= end8) {
                eightDatas11.add(data);
            }
            if (time >= start9 && time <= end9) {
                nineDatas11.add(data);
            }
            if (time >= start10 && time <= end10) {
                tenDatas11.add(data);
            }
            if (time >= start11 && time <= end11) {
                eleDatas11.add(data);
            }
            if (time >= start12 && time <= end12) {
                tweDatas11.add(data);
            }
        }
        //第一个月的数据
        Map<String, String> sdata1 = new HashMap<>();
        String sallPrice1 = "0";
        String scount1 = oneDatas11.size() + "";
        if (oneDatas11.size() > 0) {
            for (Map<String, String> data : oneDatas11) {
                String price = data.get("price");
                sallPrice1 = DecimalUtils.addition(sallPrice1, price);
            }
            String savePrice1 = DecimalUtils.division(sallPrice1, scount1, 8);
            sdata1.put("time", DateUtils.getDataStampStr(start1));
            sdata1.put("avePrice", savePrice1);
            sdata1.put("buy", "2");
        } else {
            sdata1.put("time", DateUtils.getDataStampStr(start1));
            sdata1.put("avePrice", "0");
            sdata1.put("buy", "2");
        }

        //第2个月的数据
        Map<String, String> sdata2 = new HashMap<>();
        String sallPrice2 = "0";
        String scount2 = twoDatas11.size() + "";
        if (twoDatas11.size() > 0) {
            for (Map<String, String> data : twoDatas11) {
                String price = data.get("price");
                sallPrice2 = DecimalUtils.addition(sallPrice2, price);
            }
            String savePrice2 = DecimalUtils.division(sallPrice2, scount2, 8);
            sdata2.put("time", DateUtils.getDataStampStr(start2));
            sdata2.put("avePrice", savePrice2);
            sdata2.put("buy", "2");
        } else {
            sdata2.put("time", DateUtils.getDataStampStr(start2));
            sdata2.put("avePrice", "0");
            sdata2.put("buy", "2");
        }

        //第3个月的数据
        Map<String, String> sdata3 = new HashMap<>();
        String sallPrice3 = "0";
        String scount3 = threeDatas11.size() + "";
        if (threeDatas11.size() > 0) {
            for (Map<String, String> data : threeDatas11) {
                String price = data.get("price");
                sallPrice3 = DecimalUtils.addition(sallPrice3, price);
            }
            String savePrice3 = DecimalUtils.division(sallPrice3, scount3, 8);
            sdata3.put("time", DateUtils.getDataStampStr(start3));
            sdata3.put("avePrice", savePrice3);
            sdata3.put("buy", "2");
        } else {
            sdata3.put("time", DateUtils.getDataStampStr(start3));
            sdata3.put("avePrice", "0");
            sdata3.put("buy", "2");
        }

        //第4个月的数据
        Map<String, String> sdata4 = new HashMap<>();
        String sallPrice4 = "0";
        String scount4 = fourDatas11.size() + "";
        if (fourDatas11.size() > 0) {
            for (Map<String, String> data : fourDatas11) {
                String price = data.get("price");
                sallPrice4 = DecimalUtils.addition(sallPrice4, price);
            }
            String savePrice4 = DecimalUtils.division(sallPrice4, scount4, 8);
            sdata4.put("time", DateUtils.getDataStampStr(start4));
            sdata4.put("avePrice", savePrice4);
            sdata4.put("buy", "2");
        } else {
            sdata4.put("time", DateUtils.getDataStampStr(start4));
            sdata4.put("avePrice", "0");
            sdata4.put("buy", "1");
        }

        //第5个月的数据
        Map<String, String> sdata5 = new HashMap<>();
        String sallPrice5 = "0";
        String scount5 = fiveDatas11.size() + "";
        if (fiveDatas11.size() > 0) {
            for (Map<String, String> data : fiveDatas11) {
                String price = data.get("price");
                sallPrice5 = DecimalUtils.addition(sallPrice5, price);
            }
            String savePrice5 = DecimalUtils.division(sallPrice5, scount5, 8);
            sdata5.put("time", DateUtils.getDataStampStr(start5));
            sdata5.put("avePrice", savePrice5);
            sdata5.put("buy", "2");
        } else {
            sdata5.put("time", DateUtils.getDataStampStr(start5));
            sdata5.put("avePrice", "0");
            sdata5.put("buy", "2");
        }

        //第6个月的数据
        Map<String, String> sdata6 = new HashMap<>();
        String sallPrice6 = "0";
        String scount6 = sixDatas11.size() + "";
        if (sixDatas11.size() > 0) {
            for (Map<String, String> data : sixDatas11) {
                String price = data.get("price");
                sallPrice6 = DecimalUtils.addition(sallPrice6, price);
            }
            String savePrice6 = DecimalUtils.division(sallPrice6, scount6, 8);
            sdata6.put("time", DateUtils.getDataStampStr(start6));
            sdata6.put("avePrice", savePrice6);
            sdata6.put("buy", "2");
        } else {
            sdata6.put("time", DateUtils.getDataStampStr(start6));
            sdata6.put("avePrice", "0");
            sdata6.put("buy", "2");
        }

        //第7个月的数据
        Map<String, String> sdata7 = new HashMap<>();
        String sallPrice7 = "0";
        String scount7 = sevenDatas11.size() + "";
        if (sevenDatas11.size() > 0) {
            for (Map<String, String> data : sevenDatas11) {
                String price = data.get("price");
                sallPrice7 = DecimalUtils.addition(sallPrice7, price);
            }
            String savePrice7 = DecimalUtils.division(sallPrice7, scount7, 8);
            sdata7.put("time", DateUtils.getDataStampStr(start7));
            sdata7.put("avePrice", savePrice7);
            sdata7.put("buy", "2");
        } else {
            sdata7.put("time", DateUtils.getDataStampStr(start7));
            sdata7.put("avePrice", "0");
            sdata7.put("buy", "2");
        }

        //第8个月的数据
        Map<String, String> sdata8 = new HashMap<>();
        String sallPrice8 = "0";
        String scount8 = eightDatas11.size() + "";
        if (eightDatas11.size() > 0) {
            for (Map<String, String> data : eightDatas11) {
                String price = data.get("price");
                sallPrice8 = DecimalUtils.addition(sallPrice8, price);
            }
            String savePrice8 = DecimalUtils.division(sallPrice8, scount8, 8);
            sdata8.put("time", DateUtils.getDataStampStr(start8));
            sdata8.put("avePrice", savePrice8);
            sdata8.put("buy", "2");
        } else {
            sdata8.put("time", DateUtils.getDataStampStr(start8));
            sdata8.put("avePrice", "0");
            sdata8.put("buy", "2");
        }

        //第9个月的数据
        Map<String, String> sdata9 = new HashMap<>();
        String sallPrice9 = "0";
        String scount9 = nineDatas11.size() + "";
        if (nineDatas11.size() > 0) {
            for (Map<String, String> data : nineDatas11) {
                String price = data.get("price");
                sallPrice9 = DecimalUtils.addition(sallPrice9, price);
            }
            String savePrice9 = DecimalUtils.division(sallPrice9, scount9, 8);
            sdata9.put("time", DateUtils.getDataStampStr(start9));
            sdata9.put("avePrice", savePrice9);
            sdata9.put("buy", "2");
        } else {
            sdata9.put("time", DateUtils.getDataStampStr(start9));
            sdata9.put("avePrice", "0");
            sdata9.put("buy", "2");
        }

        //第10个月的数据
        Map<String, String> sdata10 = new HashMap<>();
        String sallPrice10 = "0";
        String scount10 = tenDatas11.size() + "";
        if (tenDatas11.size() > 0) {
            for (Map<String, String> data : tenDatas11) {
                String price = data.get("price");
                sallPrice10 = DecimalUtils.addition(sallPrice10, price);
            }
            String savePrice10 = DecimalUtils.division(sallPrice10, scount10, 8);
            sdata10.put("time", DateUtils.getDataStampStr(start10));
            sdata10.put("avePrice", savePrice10);
            sdata10.put("buy", "2");
        } else {
            sdata10.put("time", DateUtils.getDataStampStr(start10));
            sdata10.put("avePrice", "0");
            sdata10.put("buy", "2");
        }

        //第11个月的数据
        Map<String, String> sdata11 = new HashMap<>();
        String sallPrice11 = "0";
        String scount11 = eleDatas11.size() + "";
        if (eleDatas11.size() > 0) {
            for (Map<String, String> data : eleDatas11) {
                String price = data.get("price");
                sallPrice11 = DecimalUtils.addition(sallPrice11, price);
            }
            String savePrice11 = DecimalUtils.division(sallPrice11, scount11, 8);
            sdata11.put("time", DateUtils.getDataStampStr(start11));
            sdata11.put("avePrice", savePrice11);
            sdata11.put("buy", "2");
        } else {
            sdata11.put("time", DateUtils.getDataStampStr(start11));
            sdata11.put("avePrice", "0");
            sdata11.put("buy", "2");
        }

        //第12个月的数据
        Map<String, String> sdata12 = new HashMap<>();
        String sallPrice12 = "0";
        String scount12 = tweDatas11.size() + "";
        if (tweDatas11.size() > 0) {
            for (Map<String, String> data : tweDatas11) {
                String price = data.get("price");
                sallPrice12 = DecimalUtils.addition(sallPrice12, price);
            }
            String savePrice12 = DecimalUtils.division(sallPrice12, scount12, 8);
            sdata12.put("time", DateUtils.getDataStampStr(start12));
            sdata12.put("avePrice", savePrice12);
            sdata12.put("buy", "2");
        } else {
            sdata12.put("time", DateUtils.getDataStampStr(start12));
            sdata12.put("avePrice", "0");
            sdata12.put("buy", "2");
        }

        List<Map<String, String>> lastSellAve = new ArrayList<>();
        lastSellAve.add(sdata1);
        lastSellAve.add(sdata2);
        lastSellAve.add(sdata3);
        lastSellAve.add(sdata4);
        lastSellAve.add(sdata5);
        lastSellAve.add(sdata6);
        lastSellAve.add(sdata7);
        lastSellAve.add(sdata8);
        lastSellAve.add(sdata9);
        lastSellAve.add(sdata10);
        lastSellAve.add(sdata11);
        lastSellAve.add(sdata12);
        Map<String, List<Map<String, String>>> lastDatas = new HashMap<String, List<Map<String, String>>>();
        lastDatas.put("buy", lastBuyAve);
        lastDatas.put("sell", lastSellAve);
        return lastDatas;

    }

}
