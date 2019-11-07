package cyweb.utils;

import java.util.HashMap;

public class CoinConst {

    public static String  redis_currency_user_chizhiurl_keys = "currency_user_chizhiurl_keys";

    public static String  redis_currency_user_chizhiurl_key_ = "currency_address_map_";





    //把挂单的订单数据 存到redis中 完成在内存中的交易
    //  keys = CoinConst.REDIS_COIN_TRADE_BUY_+yangOrders.getCurrencyId()+"_"+yangOrders.getCurrencyTradeId();


    public static String REDIS_COIN_TRADE_SALE_ = "redis_coin_trade_sale_";

    public static String REDIS_COIN_TRADE_BUY_ = "redis_coin_trade_buy_";







    //币种交易对的reidskey
    public static String REDIS_COIN_PAIR_BASE_ = "redis_coin_pair_base_";

    //币种交易中 每种币的最新krw金额
    public static String REDIS_COIN_KRW_PRICE_ = "redis_coin_krw_price_";



    //币种交易中 24小时 成交单价
    public static String REDIS_TREAD_24_ = "redis_tread_24_";


    //缓存交易对的基本信息 不要每次都去数据库查询
    public static String REDIS_YANG_PARI_INFO_ = "redis_yang_pari_info_";

    //缓存交易对的基本信息 不要每次都去数据库查询
    public static String REDIS_YANG_PARI_DAY_INFO_ = "redis_yang_pari_info_day_";


    //缓存币种的基本信息 不要每次都去数据库查询
    public static String REDIS_YANG_CURRENCY_INFO_ = "redis_yang_currency_info_";


    //买家买家手续费 key+memberid+"_"+cyid(交易对id)
    public static String REDIS_TRADE_FEE_MEMBER_ = "redis_trade_fee_member_";








    public static String CURRENCYPRICEBYCOLLECT = "currencypricebycollect_";

//    public static final String PAIR_MONGO_BUY = "pair_mongo_buy_"; //交易对mongo买单信息
//    public static final String PAIR_MONGO_SELL = "pair_mongo_sell_";//交易对mongo卖单信息
//    public static final String PAIR_MONGO_TRADE = "pair_mongo_trade_"; //交易对mongo交易信息

    public static final String PAIR_BUY_PRICE = "pair_buy_price_";
    public static final String PAIR_SELL_PRICE = "pair_sell_price_";



    public static final String FINANCE_KEY_INSERT = "finance_key_insert_";

    public static final String GUATYPE = "guatype_";

    public static final String TSORDERS = "tsorders_";

    public static final String GUAPRICE = "guaprice_"; // 记录挂单价格
    public static final String GUAPRICE_BUY = "guaprice_buy_"; // 记录挂单价格
    public static final String GUAPRICE_SELL = "guaprice_sell_"; // 记录挂单价格


    public static final String TOKENKEYMEMBER = "funcointokenformember_";
    public static final String TOKENKEYTOKEN = "funcointokenfortoken_";


    public static String ORDER2PAIR = "order2pair_";  //用来存放订单信息
    public static String TRADE2PAIR = "trade2pair_";  //用来存放交易信息


    public static String KLINE_TRADE_DATA="kline_trade_data_";//k线 redis key

    public static String EMAILFORFINDCODE="emailforfindcode_";
    //cnyPrice
    public static String CNYPRICE="cnyprice_";
    //邮箱验证码类型
    public static String EMAIL_CODE_TYPE_ZC="1";
    public static String EMAIL_CODE_TYPE_LOGINPWD="2";
    public static String EMAIL_CODE_TYPE_TB="4";
    public static String EMAIL_CODE_TYPE_TRADEPWD="5";
    public static String EMAIL_CODE_TYPE_BINDGOOGLE="6";
    public static String EMAIL_CODE_TYPE_CANCLEGOOGLE="7";
    public static String EMAIL_CODE_TYPE_MODGOOGLE="8";
    public static String EMAIL_WDC_KD_TS="99";//wdc 卡单提示

    public static int EMAIL_WDC_KD_TS_SEND_TIMES=1800;//半个小时

    //时间轴日志

    public static String TIMELINE_LOGIN="1";
    public static String TIMELINE_LOGIN_STR ="userLogin";
    //  忘记 登录密码
    public static byte TIME_LINE_FORGET_LOGIN_PASSWD=10;
    public static String TIME_LINE_FORGET_LOGIN_PASSWD_STR="忘记登录密码";
    //登陆来源
    public static int LOGIN_TYP_PHONE=3;



    public static String MONDB_TRADE = "mondb_trade_";


    public static String NONEEDTHREADPASSWD = "noneedthreadpasswd_";

    public static String COINLIMIT = "coinlimit_"; //币种限制


    public static String MININGINFOS = "miningInfos_"; //币种限制

    public static int FUNT_CURRENCY_ID=80;//FUNT 币种ID

    public static int FUNT_TRADE_ROOT_ID=58;//FUNT 机器人ID

    public static String ASSETSTYPE_INC="inc";//增加

    public static String ASSETSTYPE_DEC="dec";//减少

    public static String FINANCE_REMARK="挖矿返佣";//减少
    public static String FINANCE_REMARK_VIP_UNLOCK="会员卡解锁";//
    public static String FINANCE_REMARK_LOCK_FT="锁仓解锁";//
    public static String FINANCE_REMARK_LOCK_REBAT="锁仓分红";//

    public static String FINANCE_REMARK_IN_C2C="转入C2C账户";
    public static String FINANCE_REMARK_OUT_C2C="转出C2C账户";
    public static String FINANCE_REMARK_CHANGE_C2C="C2C资产变更";
    public static String FINANCE_REMARK_TIBI_OUT="提币";

    public static int SYS_ADMIN_ID=1;//系统管理员

    //资产变更类型　　　１　收入　　２　支出
    public static byte FINANCE_RECORD_MONEY_SR=1;
    public static byte FINANCE_RECORD_MONEY_ZC=2;

    //资产变更类型  管理员充值
    public static byte FINANCE_RECORD_ADD_BY_ADMIN=3;


    //C2C资产变更
    public static byte FINANCE_RECORD_CHANGE_BY_C2C=26;
    //转入c2c账户
    public static byte FINANCE_RECORD_ADD_BY_C2C=27;

    //转出C2C账户
    public static byte FINANCE_RECORD_DEC_BY_C2C=28;

    //会员卡解锁
    public static byte FINANCE_RECORD_ADD_BY_VIP_UNLOCK=30;
    //锁仓解锁
    public static byte FINANCE_RECORD_ADD_BY_FT_LOCK=33;
    //挖矿返佣
    public static byte FINANCE_RECORD_ADD_BY_RAKE=31;
    //锁仓分红
    public static byte FINANCE_RECORD_ADD_BY_LOCK_RAKE=32;
    //提币
    public static byte FINANCE_RECORD_TIBI_OUT=34;
    //资产变更类型  管理员扣款
    public static byte FINANCE_RECORD_REDUCE_BY_ADMIN=4;

    //MongoDB k线表名前缀
    public static String KLINE_MONGODB_PREX="kline_mongodb_";
    public static String MONGODB_YANG_FINANCE="yangfinance";

    //k线 时间戳
    public static String KLINE_HISTORY_FINAL_END_TIME="kline_history_final_end_time";

    //k线历史数据 当前  时间进程
    public static String KLINE_HISTORY_NOW_TIME_START="kline_history_now_time_start";
    public static String KLINE_HISTORY_NOW_TIME_END="kline_history_now_time_end";

    //K线历史数据获取结束标志时间
    public static String KLINE_HISTORY_END_FLAG="kline_history_end_flag";
    //mongodb trade集合
    public static String MONGODB_TRADE_COLLECTION ="yangtrade";
    public static String MONGODB_ORDERS_COLLECTION ="yangorders";



    public static String KTRADEFORUSER = "ktradeforuser_"; //后面接用户id

    //页面 我的委托 5条数据
    public static String FONT_USER_ORDERS_FIVE_RECORDS="font_user_orders_five_records_";
    //页面 我的交易 5条数据
    public static String FONT_USER_TRADE_FIVE_RECORDS="font_user_trade_five_records_";

    //机器人刷单
    public static Byte ROBOT_SHUA_YES=1;

    //最新交易时间
    public static String FONT_TRADE_LAST_TIME="font_trade_last_time_";

    //转出中
    public static int FT_LOCK_STATUS_TURNOUT=3;
    //转出完成
    public static int FT_LOCK_STATUS_FT_LOCK_UNLOCK_SUCEESS=4;
    //全局ｕｓｄｔ汇率
    public static String USDT_GLOBAL="usdt_global_";
    //邮箱状态值　　
    public static int EMAIL_STATUS_NORMAL=1;

    //多语言
    public static String FONT_LANGUAGE_CN="1";
    public static String FONT_LANGUAGE_EN="2";
    public static String FONT_LANGUAGE_FT="3";
    public static String FONT_LANGUAGE_KR="4";

    //Del_flag
    public static String DEL_FLAG_YES="1";
    public static String DEL_FLAG_NO="0";
    //平台公告
    public static String ARTICLE_PTGG="1";
    public static String ARTICLE_PTGG_KEYWORDS="InterestCurrencyAnnouncement";
    public static String ARTICLE_XBSX="2";//新币上线
    public static String ARTICLE_BZJS="3";//资产介绍
    public static String ARTICLE_XBSX_KEYWORDS="newCoinUp";
    public static String ARTICLE_BZJS_KEYWORDS="IntroductionToDigitalAssets";

    public static int KRW_CURRENCY_TRADE_ID=0;//韩元区市场Id
    public static String KRW_CURRENCY_TRADE_STR="KRW";//韩元区市场名称

    public static int CURRENCY_IS_LINE_YES=1;//币种是否上线

    public static String USER_CURRENCY_ONLY_KEY="user_currency_only_key_";

    public static String ORDER_CANCLE_LIST="order_cancle_list";

    //银行卡 状态
    public static int BANK_STATUS_AVA=0;
    public static int BANK_STATUS_DIS=-1;
    //订单类型
    public static String ORDER_BUY="buy";
    public static String ORDER_SELL="sell";

    //订单状态
    public static int C2C_ORDER_STATUS_WAITPAY=0;//等待付款
    public static int C2C_ORDER_STATUS_BUYERPAY=1;//买家确认汇款
    public static int C2C_ORDER_STATUS_HASSEND=2;//等待卖家确认并发货
    public static int C2C_ORDER_STATUS_CANCLE=6;//等待卖家确认并发货


    public static int C2C_ORDER_STATUS_WAITPAY_SELL=0;//等待买家打款
    public static int C2C_ORDER_STATUS_CANCLE_SELL=6;//等待买家打款
    //C2C 挂单  状态   1  未成交 2  部分成交
    public static int C2C_GUA_STATUS_NOTRADE=0;
    public static int C2C_GUA_STATUS_PARTTRADE=1;

    //币种找回工单  1 正常
    public static int CURRNCY_FIND_STATUS_NORMAL=1;

    //提币地址
    public static int QIANBAO_ADDRESS_NORMAL=1;
    public static int QIANBAO_ADDRESS_DEL=-1;
    //提币zhuangtai
    public static int TIBI_OUT_STATUS_ING=0;//待审核
    public static int TIBI_OUT_STATUS_FIN=1;//审核通过
    public static int TIBI_OUT_STATUS_PAY=2;//支付中
    public static int TIBI_OUT_STATUS_SUC=3;//支付成功
    public static int TIBI_OUT_STATUS_FAI=4;//支付失败
    public static int TIBI_OUT_STATUS_REJ=5;//审核拒绝
    //文章  1 目录节点 2 单一文章链接  3 文章列表
    public static String CATEGORY_NODE="node";
    public static String CATEGORY_SINGLE_ARTICLE="singlearticle";
    public static String CATEGORY_LINK="link";
    public static String SINGLE_ARTICLE_PAGE_URL="http://www.funcoin.co/content.html";
    //以太坊系列币种
    public static String ETH_CURRENCY_A="ETH";
    public static String ETH_CURRENCY_B="EthContract";

    //存储过程处理订单
    public static int MYSQL_GC_DEAL_TYPE_TRADE=1;//交易
    public static int MYSQL_GC_DEAL_TYPE_CANCEL=2;//撤单
    public static int MYSQL_GC_DEAL_TYPE_CREATE=3;//生成订单

    //订单状态
    public static Byte ORDER_STATUS_ERROR=-3;//订单异常
    public static Byte ORDER_STATUS_CANCEL_ING=-2;//处理中
    //用户每日下单量
    public static String CREATE_ORDER_DAY_LIMIT_="create_order_day_limit_";
    public static int CREATE_ORDER_DAY_LIMIT_NUMS=50;

    //是否黑名单
    public static int IS_BLACK_MAN_YES=1;
    public static int IS_BLACK_MAN_NO=0;

    //刷中间单
    public static int ROBOT_SHUA_TYPE_NORMAL=0;
    public static int ROBOT_SHUA_TYPE_MID=1;
    public static int ROBOT_SHUA_TYPE_GUAANDMID=2;

    //管理员的邮箱
    public static String ADMIN_EMAIL="276468689@qq.com";

    //YangCurrencyPairList  机器人 获取的 配置信息  redisKey
    public static String ROBOT_CURRENCY_PAIR_REDIS="robot_currency_pair_redis";

    //交易记录 使用的用户名称信息
    public static String TRADE_RECORD_USER_INFO="trade_record_user_info_";

    //hasDo状态
    public static int HAS_DO_YES=1;

    //撤单列表
    public static String MQ_CANCEL_ORDER_QUEUE="mq_cancel_order_queue";
    //快速生成订单
    public static int FASTMAKERORDERNUMS=1;
    public static int MAXORDERNUMS=30;


}
