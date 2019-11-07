package net.cyweb.config.custom;


import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.*;

import org.apache.commons.codec.binary.Base64;

/**
 * 签名算法
 */
public class ApiSignatureUtils {

    /**
     *          * 生成指定位数的随机秘钥
     *          * @param KeyLength
     *          * @return	Keysb
     *          
     */
    public static String KeyCreate(int KeyLength) {

        String base = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuffer Keysb = new StringBuffer();
        for (int i = 0; i < KeyLength; i++)    //生成指定位数的随机秘钥字符串
        {
            int number = random.nextInt(base.length());
            Keysb.append(base.charAt(number));
        }
        return Keysb.toString();
    }

    /**
     * 将加密后的字节数组转换成字符串
     *
     * @param b 字节数组
     * @return 字符串
     */
    public static String byteArrayToHexString(byte[] b) {
        StringBuilder hs = new StringBuilder();
        String stmp;
        for (int n = 0; b != null && n < b.length; n++) {
            stmp = Integer.toHexString(b[n] & 0XFF);
            if (stmp.length() == 1)
                hs.append('0');
            hs.append(stmp);
        }
        return hs.toString().toLowerCase();
    }

    /**
     * sha256算法
     *
     * @param str
     * @return
     * @throws Exception
     */

    public static String sha256_make(String key, String str) throws Exception {
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(key.getBytes(), "HmacSHA256");
        sha256_HMAC.init(secret_key);
        byte[] bytes = sha256_HMAC.doFinal(str.getBytes());

        String hash = Base64.encodeBase64String(sha256_HMAC.doFinal(str.getBytes()));
//            hash = byteArrayToHexString(bytes);
        return hash;
    }

    public static String encrypt(String input, String key) {
        byte[] crypted = null;
        try {
            SecretKeySpec skey = new SecretKeySpec(key.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, skey);
            crypted = cipher.doFinal(input.getBytes());
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        return new String(Base64.encodeBase64(crypted));
    }

    /**
     * 把数组所有元素排序，并按照“参数=参数值”的模式用“&”字符拼接成字符串
     *
     * @param params 需要排序并参与字符拼接的参数组
     * @return 拼接后字符串
     */
    public static String createLinkString(Map<String, String> params) {

        List<String> keys = new ArrayList<String>(params.keySet());
        Collections.sort(keys);
        String prestr = "";
        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            String value = params.get(key);
            if (i == keys.size() - 1) {// 拼接时，不包括最后一个&字符
                prestr = prestr + key + "=" + value;
            } else {
                prestr = prestr + key + "=" + value + "&";
            }
        }
        return prestr;
    }


    public static void main(String[] args) {
//        System.out.println(KeyCreate(32));
        Map sb = new HashMap();
        sb.put("key", "aRoIZAnFE2x3oLnP6Bj8iCK3fVBAOsdg");
        sb.put("cyInfo", "BTC_KRW");
        sb.put("begin", "1543219700");
        sb.put("end", "1545219700");
        sb.put("type", "buy");
        sb.put("nonce", "1545220296");
        try {
//            System.out.println(encrypt(createLinkString(sb),"dMDdLlxzJHLFJThQ"));
            System.out.println(sha256_make("YqSFfMijM7OCugWVx4aA2PxTuSXFxDCJ", createLinkString(sb)));
        } catch (Exception e) {
            e.printStackTrace();
        }
//        long a= System.currentTimeMillis()/1000;
//        int b=(int)a;
//        System.out.println(a);
//        System.out.println(b);

    }
}
