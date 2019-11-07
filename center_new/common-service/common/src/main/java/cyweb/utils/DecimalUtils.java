package cyweb.utils;

import java.math.BigDecimal;

public class DecimalUtils {

    /**
     * BigDecimal加法计算
     *
     * @param first  被加数值
     * @param second 需要加的数值
     * @return
     */
    public static String addition(String first, String second) {
        String res = "0.00";
        //转换类型
        BigDecimal d1 = new BigDecimal(first);
        BigDecimal d2 = new BigDecimal(second);
        //计算结果(加法)
        BigDecimal d3 = d1.add(d2);
        //结果转换stirng
        res = d3.toPlainString();
        return res;
    }

    /**
     * BigDecimal减法计算
     *
     * @param first  被减数值
     * @param second 需要减的数值
     * @return
     */
    public static String subtraction(String first, String second) {
        String res = "0.00";
        //转换类型
        if (!first.equals(second)) {
            BigDecimal b1 = new BigDecimal(first);
            BigDecimal b2 = new BigDecimal(second);
            //计算结果(减法)
            BigDecimal b3 = b1.subtract(b2);
            //结果转换stirng
            res = b3.toPlainString();
        }
        return res;
    }

    /**
     * BigDecimal乘法计算
     *
     * @param first  被乘数值
     * @param second 需要乘的数值
     * @return
     */
    public static String multiplication(String first, String second) {
        String res = "0.00";
        //转换类型
        BigDecimal b1 = new BigDecimal(first);
        BigDecimal b2 = new BigDecimal(second);
        //计算结果(减法)
        BigDecimal b3 = b1.multiply(b2);
        //结果转换stirng
        res = b3.toPlainString();
        return res;
    }

    /**
     * BigDecimal乘法计算
     *
     * @param first  被乘数值
     * @param second 需要乘的数值
     * @return
     */
    public static String multiplicationForNum(String first, String second) {
        String res = "0.00";
        //转换类型
        BigDecimal b1 = new BigDecimal(first);
        BigDecimal b2 = new BigDecimal(second);
        //计算结果(减法)
        BigDecimal b3 = b1.multiply(b2);
        //结果转换stirng
        b3 = b3.setScale(2, BigDecimal.ROUND_DOWN);
        res = b3.toPlainString();
        return res;
    }

    /**
     * BigDecimal除法计算
     *
     * @param first  被除数值
     * @param second 需要除的数值
     * @param length 保留位数
     * @return
     */
    public static String division(String first, String second, int length) {
        String res = "0.00";
        //转换类型
        BigDecimal b1 = new BigDecimal(first);
        BigDecimal b2 = new BigDecimal(second);
        //计算结果(减法)
        BigDecimal b3 = b1.divide(b2, length,BigDecimal.ROUND_HALF_DOWN);
        //结果转换stirng
        res = b3.toPlainString();
        return res;
    }

    public static Double add(Double v1, Double v2) {
        BigDecimal b1 = new BigDecimal(v1.toString());
        BigDecimal b2 = new BigDecimal(v2.toString());
        return b1.add(b2).doubleValue();
    }


    public static void main(String[] args) {
//        BigDecimal b1 = new BigDecimal("226255.37266887");
//        BigDecimal b2 = new BigDecimal("605");
//        BigDecimal price = b1.divide(b2, 8, BigDecimal.ROUND_CEILING);
        String price = division("226255.37266887", "605", 10);
        System.out.println(price);
    }
}
