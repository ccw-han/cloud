package cyweb.utils;

import java.util.UUID;

public class UUIDUtils {
    public static String genUUID(){
        return UUID.randomUUID().toString().replace("-", "");
    }
}
