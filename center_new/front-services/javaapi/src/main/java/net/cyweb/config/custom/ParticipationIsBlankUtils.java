package net.cyweb.config.custom;

import org.apache.commons.lang.StringUtils;

import java.util.Map;
import java.util.Set;

/**
 *  参数是否为空工具类
 *  耶耶
 */
public class ParticipationIsBlankUtils {

    /**
     * yy
     * @param map
     * @return
     */
    public static Boolean isBlank(Map<String,String> map){
        Set<Map.Entry<String, String>> entries = map.entrySet();
        for (Map.Entry<String, String> entry : entries) {
            if (StringUtils.isBlank(entry.getValue())){
                return true;
            }
        }
        return false;
    }
}
