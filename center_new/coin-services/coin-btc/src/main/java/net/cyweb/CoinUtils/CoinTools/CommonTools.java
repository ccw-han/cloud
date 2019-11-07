package net.cyweb.CoinUtils.CoinTools;

import java.io.*;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.security.MessageDigest;

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


}
