package cyweb.utils;

import org.apache.commons.lang.StringUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtils {

    /**
     * @param date
     * @param depth -1 代币昨天
     * @return
     */
    public static Date getDay(Date date, int depth) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, depth);
        date = calendar.getTime();
        return date;
    }


    public static String getDateStrPre(int depth) {
        Calendar c = Calendar.getInstance();
        Date d = new Date();
        System.out.println(d);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(DateUtils.getDay(d, depth));
    }

    public static long getTimeStamp(String times) {
        long timestamp = 0;
        try {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd H:m:s");
//            Date date = df.parse("2013-03-04 23:59:59");//原文是"20130304",格式不对，但是还可以写为"2013-3-04"
            Date date = df.parse(times);//原文是"20130304",格式不对，但是还可以写为"2013-3-04"
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            timestamp = cal.getTimeInMillis(); //单位为毫秒
        } catch (Exception e) {
            e.printStackTrace();
        }
        return timestamp;
    }

    public static long getTimeStampSeconds(String times) {
        long timestamp = 0;
        try {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd H:m:s");
            //            Date date = df.parse("2013-03-04 23:59:59");//原文是"20130304",格式不对，但是还可以写为"2013-3-04"
            String dataStr = String.valueOf(df.parse(times).getTime() / 1000);
            timestamp = Integer.parseInt(dataStr); //单位为毫秒
        } catch (Exception e) {
            e.printStackTrace();
        }
        return timestamp;
    }

    /**
     * 获取当前时间戳  返回INT值
     *
     * @return
     */
    public static int getNowTimes() {
        long a = System.currentTimeMillis() / 1000;
        int b = (int) a;
        return b;
    }

    /**
     * 获取当前时间戳  返回INT值
     *
     * @return
     */
    public static long getNowTimesLong() {
        long a = System.currentTimeMillis() / 1000;
        return a;
    }

    /**
     * 获取当前时间字符串
     *
     * @param date
     * @return
     */
    public static String getNoDateDay(Date date, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(date);
    }

    /*
时间戳字符串改时间
 */
    public static String getDataStampStr(Long times) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date(times));
    }

    public static String getTimeStampByUserStart(String interval) {
        //时间参数
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd H:m:s");
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        String start = "" + year + "-" + interval + "-01 00:00:00";
        Date date = null;//原文是"20130304",格式不对，但是还可以写为"2013-3-04"
        try {
            date = df.parse(start);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        cal.setTime(date);
        long timestamp = cal.getTimeInMillis(); //单位为毫秒
        return timestamp + "";
    }

    public static String getTimeStampByUserEnd() {
        //时间参数
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd H:m:s");
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        String start = "" + year + "-12-31 23:59:59";
        Date date = null;//原文是"20130304",格式不对，但是还可以写为"2013-3-04"
        try {
            date = df.parse(start);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        cal.setTime(date);
        long timestamp = cal.getTimeInMillis(); //单位为毫秒
        return timestamp + "";
    }

    //获取某年某月的第一天日期

    public static Date getStartMonthDate(int year, int month) {

        Calendar calendar = Calendar.getInstance();

        calendar.set(year, month - 1, 1, 0, 0, 0);

        return calendar.getTime();

    }

    //获取某年某月的最后一天日期

    public static Date getEndMonthDate(int year, int month) {

        Calendar calendar = Calendar.getInstance();

        calendar.set(year, month - 1, 1);

        int day = calendar.getActualMaximum(5);

        calendar.set(year, month - 1, day, 23, 59, 59);

        return calendar.getTime();

    }

    public static void main(String[] args) {

    }

}
