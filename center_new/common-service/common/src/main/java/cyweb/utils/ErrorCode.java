package cyweb.utils;

public enum  ErrorCode {


    ERROR_SYSTEM("系统错误", 1000),
    /*登录注册*/
    ERROR_EMAIL_REPEAT("email重复", 1001),
    ERROR_EMAIL_NOT_EXIST("email不存在",1002),
    ERROR_PASSWORD_ERR("密码错误",1003),

    /*用户信息*/
    ERROR_ERROR_TOKEN("错误的token",2001),
    ERROR_ERROR_TOKEN_EXPIRE("token已过期",2002),
    ERROR_PWD_TRADE_SUCCESS("交易密码验证成功",2003),
    ERROR_PWD_TRADE_FAILED("交易密码验证失败",2004),
    ERROR_OLD_PWD_ERROR("旧密码验证失败",2005),
    ERROR_NEED_GOOGLE_TRUE("需要google验证",2006),
    ERROR_NEED_GOOGLE_FALSE("不需要google验证",2007),
    ERROR_CURRENCY_USER_NOT_FOUND("当前币种下未找到关联用户",2008),
    ERROR_CURRENCY_NOT_FOUND("当前币种下未找到关联用户",2009),
    ERROR_SOCKET_TIMEOUT("连接超时",2010),
    ERROR_CHECK_NICK_TRUE("有昵称",2011),
    ERROR_CHECK_NICKE_FALSE("没有昵称",2012),
    ERROR_VALIDATE_ADDRESS("有效的地址",2013),
    ERROR_NOT_VALIDATE_ADDRESS("无效的地址",2014),
    ERROR_PWD_TRADE_ISNETSED("交易密码尚未设置",2015),
    ERROR_PWD_TRADE_ISEMPTY("交易密码不能为空",2016),
    ERROR_PWD_CHECK_ERROR("用户信息验证失败",2017),
    ERROR_PWD_TRADE_HASEXISTS("用户信息验证失败",2018),
    MEMBER_HAS_LOCKED("用户已经锁定",2019),
    ERROR_ERROR_CODE("错误的校验码",2020),
    REPEATING_ID_CARD("重复的身份证",2021),
    HAS_SEND_EMAILCODE("验证码已发送",2022),
    SEND_EMAILCODE_ERROR("系统繁忙,请稍后重试",2023),
    ERROR_BANK_NOT_FOOUD("未找到银行卡信息",2024),
    ERROR_C2C_ORDER_BUY_LIMIT("超过C2C买单下单限制",2025),
    ERROR_C2C_GUA_NOT_FOUND("未找到当前C2C挂单",2026),
    ERROR_C2C_ORDER_NOT_FOUND("未找到当前订单",2027),
    ERROR_C2C_ORDER_BUY_NUM_LIMIT("C2C买单数量超过挂单量",2028),
    ERROR_C2C_ORDER_SELL_LIMIT("超过C2C卖单下单限制",2029),
    ERROR_C2C_ACCOUNT_FOREZN("C2C账户冻结异常",2030),
    ERROR_CURRENCY_NO_FOUND("未找到当前币种信息",2031),
    ERROR_QIANBAO_ADDRESS_NO_FOUND("未找到当前钱包地址",2032),
    ERROR_ORDER_DAY_LIMIT("超过当日下单限制",2033),
    ERROR_ORDER_UNNORMAL("订单异常",2034),
    ERROR_PIC_UPLOAD("图片上传失败",2035),
    ERROR_PIC_D("请上传全部图片",2036),
    ERROR_GOOGLECODE("谷歌验证码错误",2037),

    //交易信息
    ERROR_TRADE_CURRENCY_DENIED("当前币种禁止交易",3001),
    ERROR_TRADE_CURRENCY_PARAMA_ERROR("参数错误",3002),
    ERROR_TRADE_CURRENCY_PRICE_LIMIT("购买金额不能低于最低金额",3003),
    ERROR_TRADE_CURRENCY_USER_PRICE_NOTENOUGH("账户余额不足",3004),
    //做市商api 错误提示
    ERROR_MAKE_MARKET_NO_TRADE_CURRENCY("当前币种禁止交易",101),
    ERROR_MAKE_MARKET_NO_CURRENCY("交易对不存在",102),
    ERROR_MAKE_MARKET_NO_KEY("密钥不存在",103),
    ERROR_MAKE_MARKET_ERROR_SIGN("签名不匹配",104),
    ERROR_MAKE_MARKET_NO_AUTH("权限不足",105),
    ERROR_MAKE_MARKET_NO_CURRENCY_ID("币种不存在",106),
    ERROR_MAKE_MARKET_LIMIT_ORDER("超过下单频率",107),
    ERROR_MAKE_MARKET_NO_LEFTNUM("账户余额不足",200),
    ERROR_MAKE_MARKET_NUM_ERROR("买卖的数量小于最小买卖额度",201),
    ERROR_MAKE_MARKET_PRICE_ERROR("下单价格必须大于0",202),


    ;

    private String message;
    private int index;

    ErrorCode(String message, int index) {
        this.message = message;
        this.index = index;
    }


    public String getMessage() {
        return message;
    }

    public int getIndex() {
        return index;
    }
}
