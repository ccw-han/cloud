package net.cyweb.config.custom;

import net.cyweb.model.FunFlash;
import net.cyweb.model.Result;
import net.cyweb.model.UserProPs;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Map;

public class Base64Utils {


    //网站图片读取
    public static byte[] readImage(String path, String readPathA, String readPathB, String readPathC, String rootPath) throws Exception {
        if (StringUtils.isNotEmpty(path)) {
            path = path.replace(readPathA, "");
            path = path.replace(readPathB, "");
            path = path.replace(readPathC, "");
        }
        if (path.indexOf(rootPath) != 0) {
            path = rootPath + path;
        }
        byte[] dataa = null;
        byte[] data = null;
        File image = new File(path);
        FileInputStream inputStream = new FileInputStream(image);
        int length = inputStream.available();
        data = new byte[length];
        inputStream.read(data);
        inputStream.close();
        data = Base64.encodeBase64(data);
//            dataa= Base64.decodeBase64(data);

        return data;
    }


    public static void getRealImageUrl(String nowImagePath, String oldReadHost, String rootPath, Map resultMap, String logoName) {
        if (StringUtils.isNotEmpty(nowImagePath) && StringUtils.isNotBlank(nowImagePath)) {
            if (nowImagePath.indexOf(oldReadHost) == 0) {
                String path = rootPath + nowImagePath.replace(oldReadHost, "");
                resultMap.put(logoName, path);
            }
        }
    }


    public static String getRealImageUrlA(String nowImagePath, String oldReadHost, String newReadHost, String rootPath) {
        if (StringUtils.isNotEmpty(nowImagePath) && StringUtils.isNotBlank(nowImagePath)) {
            if (nowImagePath.indexOf(oldReadHost) == 0) {
                String path = rootPath + nowImagePath.replace(oldReadHost, "");
                return path;
            }
            if (nowImagePath.indexOf(newReadHost) == 0) {
                String path = rootPath + nowImagePath.replace(newReadHost, "");
                return path;
            }
        }
        return "";
    }
}
