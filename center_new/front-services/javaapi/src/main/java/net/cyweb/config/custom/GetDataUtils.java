package net.cyweb.config.custom;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * 获取时间工具类
 * 耶耶
 */
public class GetDataUtils {


    //获取当天的开始时间
    public static Long getDayBegin() {
        Calendar cal = new GregorianCalendar();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis() / 1000;
    }

    public static Long getStartTime(Date date) {
        Calendar dateStart = Calendar.getInstance();
        dateStart.setTime(date);
        dateStart.set(Calendar.HOUR_OF_DAY, 0);
        dateStart.set(Calendar.MINUTE, 0);
        dateStart.set(Calendar.SECOND, 0);
        return dateStart.getTimeInMillis() / 1000;
    }

    public static void main(String[] args) {
        System.out.println(getStartTime(new Date()));
    }

}
