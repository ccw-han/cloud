package net.cyweb.config.custom;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Joiner;
import net.cyweb.exception.DataErrorException;
import okhttp3.*;
import org.apache.commons.codec.binary.Hex;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import org.apache.commons.codec.binary.Hex;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class RemoteAccessUtils {
    private static final String USERAPI = "https://www.bimin.co";
    private static final String API = "https://api.bimin.co";
    private static final String domain = "https://api.bimin.co";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final String apiKey = "W6TFX1O7h4G7RfFe2BRArRCXQb4ORYyfTPNJ4lk5NnagpwGXUkJ59PM8YN3AeZDJ";
    private static final String apiSecret = "etsOy57tzP1Tb30309Y7mVqJmkQCgiQY3t2h7IGf4YJD9vkXruauaQlssMluDQPG";

    private static OkHttpClient okHttpClient
            = new OkHttpClient.Builder()
            .connectTimeout(2, TimeUnit.SECONDS)
            .writeTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .build();

    private static AtomicLong idGen = new AtomicLong(System.currentTimeMillis());
    // DO NOT change these
    private static final String API_KEY_HEADER = "X-ACCESS-KEY";
    private static String apiUrlPrefix = domain + "/api/v2/org";

    public static String postOrg(TreeMap<String, String> paramsMap, String url) {
        okhttp3.Request.Builder requestBuilder = new okhttp3.Request.Builder();
        paramsMap.put("signature", sign(paramsMap, apiSecret));

        FormBody.Builder paramBuilder = new FormBody.Builder();
        for (String s : paramsMap.keySet()) {
            paramBuilder.add(s, paramsMap.get(s));
        }

        requestBuilder.url(apiUrlPrefix + url).post(paramBuilder.build());

        // add apiKey header
        requestBuilder.addHeader(API_KEY_HEADER, apiKey);

        // sign the request
        okhttp3.Request finalRequest = requestBuilder.build();

        try (Response response = okHttpClient.newCall(finalRequest).execute()) {
            if (response.isSuccessful()) {
                String responseData = response.body().string();
                // todo parse the response body data
                return responseData;
            } else {
                // todo log the error response

            }
        } catch (IOException e) {
            // todo deal with the network error
        }
        return "";

    }

    public void getOrg(String url) {
        okhttp3.Request.Builder requestBuilder = new okhttp3.Request.Builder();
        TreeMap<String, String> paramsMap = new TreeMap<>();
        paramsMap.put("signature", sign(paramsMap, apiSecret));
        FormBody.Builder paramBuilder = new FormBody.Builder();
        for (String s : paramsMap.keySet()) {
            paramBuilder.add(s, paramsMap.get(s));
        }
        String lastUrl = apiUrlPrefix + url + "?signature=" + sign(paramsMap, apiSecret);
        requestBuilder.url(lastUrl).get();

        // add apiKey header
        requestBuilder.addHeader(API_KEY_HEADER, apiKey);

        // sign the request
        okhttp3.Request finalRequest = requestBuilder.build();

        try (Response response = okHttpClient.newCall(finalRequest).execute()) {
            if (response.isSuccessful()) {
                String responseData = response.body().string();
                // todo parse the response body data

            } else {
                // todo log the error response

            }
        } catch (IOException e) {
            // todo deal with the network error
        }

    }

    public static Object post(TreeMap<String, String> map, String url) throws IOException, DataErrorException {
        String res = Request.Post(url).bodyString(com.alibaba.fastjson.JSON.toJSONString(map), ContentType.APPLICATION_JSON).execute().returnContent().asString();
        Object object = com.alibaba.fastjson.JSON.parse(res);
        return object;
    }

    public static Object get(String url) throws IOException, DataErrorException {
        String res = Request.Get(url).addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.146 Safari/537.36").execute().returnContent().asString();
        Object object = com.alibaba.fastjson.JSON.parse(res);
        return object;
    }


    private static String sign(TreeMap<String, String> requestParamMap, String secret) {
        try {
            String originalStr = Joiner.on("&").withKeyValueSeparator("=").join(requestParamMap);

            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
            sha256_HMAC.init(secretKeySpec);
            return new String(Hex.encodeHex(sha256_HMAC.doFinal(originalStr.getBytes())));
        } catch (Exception e) {
            throw new RuntimeException("Unable to sign message.", e);
        }
    }

    public static String accessUrl(String url, String type) {
        if ("www".equals(type)) {
            String accessUrl = USERAPI + url;
            return accessUrl;
        }
        String accessUrl = API + url;
        return accessUrl;
    }
}
