package cyweb.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class CommonTools {

    public static  Class getClasses(String coinName) throws Exception {
        Class clazz = CommonTools.class;
        ClassLoader loader = clazz.getClassLoader();
        String perfix = "net.cyweb.CoinUtils.CoinEntry.";
        return  loader.loadClass(perfix+coinName);

    }

    public static  String Md5(String inputStr)
    {
        String ext = "dreamofsea";
        inputStr = inputStr+ext;
        BigInteger sha =null;
        byte[] inputData = inputStr.getBytes();
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA");
            messageDigest.update(inputData);
            sha = new BigInteger(messageDigest.digest());
        } catch (Exception e) {e.printStackTrace();}

        return sha.toString(32);
    }

    public static  String Md5(String inputStr,String type)
    {
        String ext = "";
        inputStr = inputStr+ext;
        BigInteger sha =null;
        byte[] inputData = inputStr.getBytes();
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA");
            messageDigest.update(inputData);
            sha = new BigInteger(messageDigest.digest());
        } catch (Exception e) {e.printStackTrace();}

        return sha.toString(32);
    }

    public static <T> List<T> json2Object(JSONArray jsonArray, List<T> list,Class<T> tClass)
    {
        for (Object jsonObject : jsonArray
                ) {
            T t = JSONObject.parseObject(jsonObject.toString(), tClass);
            list.add(t);
        }
        return list;
    }

    public  static String formatNull2String(Object s)
    {
        if(s == null)
        {
            return  "";
        }else{
            return s.toString();
        }
    }

    public  static BigDecimal formatNull2BigDecimal(Object s)
    {
        if(s == null)
        {
            return  BigDecimal.ZERO;
        }else{
            return BigDecimal.valueOf(Double.valueOf(s.toString()));
        }
    }


    public  static int formatNull2int(Object s)
    {
        if(s == null)
        {
            return  0;
        }else{
            return Integer.valueOf(s.toString()).intValue();
        }
    }

}
