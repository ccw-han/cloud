package net.cyweb.config.custom;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 参数去除空格工具类
 * 耶耶
 */
public class ParticipationRemoveBlankSpaceUtils {

    /**
     * 去除空格
     * 耶耶
     * @param map
     * @return
     */
    public static Map<String,String> removeBlankSpace (Map<String,String> map){
        Map<String,String> dataMap = new HashMap<>();
        Set<Map.Entry<String, String>> entries = map.entrySet();
        for (Map.Entry<String, String> entry : entries) {
            dataMap.put(entry.getKey(),entry.getValue().replace(" ",""));
        }
        return dataMap;
    }

    /*public static void main(String[] args) {
        Map<String,String> map = new HashMap<>();
        map.put("a","    111    2222");
        Map map1 = removeBlankSpace(map);
        Set<Map.Entry<String, String>> set = map1.entrySet();
        for (Map.Entry<String, String> stringStringEntry : set) {
            System.out.println(stringStringEntry.getValue());
        }
    }*/
}
