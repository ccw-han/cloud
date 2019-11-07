package net.cyweb.service;

import com.alibaba.fastjson.JSONObject;
import cyweb.utils.CoinConst;
import cyweb.utils.CommonTools;
import cyweb.utils.DateUtils;
import cyweb.utils.ErrorCode;
import net.cyweb.config.custom.EmailUtils;
import net.cyweb.exception.EmailNotExistException;
import net.cyweb.exception.EmailRegistRepeatException;
import net.cyweb.exception.PasswordErrorException;
import net.cyweb.mapper.YangCurrencyUserMapper;
import net.cyweb.mapper.YangEmailLogMapper;
import net.cyweb.mapper.YangEmailMapper;
import net.cyweb.mapper.YangMemberMapper;
import net.cyweb.model.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class YangMemberService extends BaseService<YangMember>{

    @Autowired
    private YangConfigService yangConfigService;

    @Autowired
    private YangMemberMapper memberMapper;

    @Autowired
    private YangCurrencyUserMapper yangCurrencyUserMapper;

    @Autowired
    private YangEmailMapper yangEmailMapper;

    @Autowired
    private YangEmailLogMapper yangEmailLogMapper;


    /**
     *
     * @param memberId
     * @param cyId
     * @param num
     * @param numOptions inc dec
     * @param forzen
     * @param forzenOptions inc dec
     * @return
     */
    public  Integer assets(
            int memberId, String cyId, BigDecimal num, String numOptions, BigDecimal forzen, String forzenOptions,
            int memberId1, String cyId1, BigDecimal num1, String numOptions1, BigDecimal forzen1, String forzenOptions1,
            int memberId2, String cyId2, BigDecimal num2, String numOptions2, BigDecimal forzen2, String forzenOptions2,
            int memberId3, String cyId3, BigDecimal num3, String numOptions3, BigDecimal forzen3, String forzenOptions3,
            int memberId4, String cyId4, BigDecimal num4, String numOptions4, BigDecimal forzen4, String forzenOptions4
            ){
        HashMap pama = new HashMap();
        pama.put("memberId",memberId);
        pama.put("cyId",cyId);
        pama.put("num",num);
        pama.put("numOptions",numOptions);
        pama.put("forzen",forzen);
        pama.put("forzenOptions",forzenOptions);


        pama.put("memberId1",memberId1);
        pama.put("cyId1",cyId1);
        pama.put("num1",num1);
        pama.put("numOptions1",numOptions1);
        pama.put("forzen1",forzen1);
        pama.put("forzenOptions1",forzenOptions1);

        pama.put("memberId2",memberId2);
        pama.put("cyId2",cyId2);
        pama.put("num2",num2);
        pama.put("numOptions2",numOptions2);
        pama.put("forzen2",forzen2);
        pama.put("forzenOptions2",forzenOptions2);

        pama.put("memberId3",memberId3);
        pama.put("cyId3",cyId3);
        pama.put("num3",num3);
        pama.put("numOptions3",numOptions3);
        pama.put("forzen3",forzen3);
        pama.put("forzenOptions3",forzenOptions3);

        pama.put("memberId4",memberId4);
        pama.put("cyId4",cyId4);
        pama.put("num4",num4);
        pama.put("numOptions4",numOptions4);
        pama.put("forzen4",forzen4);
        pama.put("forzenOptions4",forzenOptions4);

                return  memberMapper.assets(pama);
    }

    public  Integer assets_copy(
            int memberId, String cyId, BigDecimal num, String numOptions, BigDecimal forzen, String forzenOptions,int trade_order_id,
            int memberId1, String cyId1, BigDecimal num1, String numOptions1, BigDecimal forzen1, String forzenOptions1,int trade_order_id1,
            int memberId2, String cyId2, BigDecimal num2, String numOptions2, BigDecimal forzen2, String forzenOptions2,int trade_order_id2,
            int memberId3, String cyId3, BigDecimal num3, String numOptions3, BigDecimal forzen3, String forzenOptions3,int trade_order_id3,
            int memberId4, String cyId4, BigDecimal num4, String numOptions4, BigDecimal forzen4, String forzenOptions4,int trade_order_id4
    ){
        HashMap pama = new HashMap();
        pama.put("memberId",memberId);
        pama.put("cyId",cyId);
        pama.put("num",num);
        pama.put("numOptions",numOptions);
        pama.put("forzen",forzen);
        pama.put("forzenOptions",forzenOptions);
        pama.put("trade_order_id",trade_order_id);


        pama.put("memberId1",memberId1);
        pama.put("cyId1",cyId1);
        pama.put("num1",num1);
        pama.put("numOptions1",numOptions1);
        pama.put("forzen1",forzen1);
        pama.put("forzenOptions1",forzenOptions1);
        pama.put("trade_order_id1",trade_order_id1);

        pama.put("memberId2",memberId2);
        pama.put("cyId2",cyId2);
        pama.put("num2",num2);
        pama.put("numOptions2",numOptions2);
        pama.put("forzen2",forzen2);
        pama.put("forzenOptions2",forzenOptions2);
        pama.put("trade_order_id2",trade_order_id2);

        pama.put("memberId3",memberId3);
        pama.put("cyId3",cyId3);
        pama.put("num3",num3);
        pama.put("numOptions3",numOptions3);
        pama.put("forzen3",forzen3);
        pama.put("forzenOptions3",forzenOptions3);
        pama.put("trade_order_id3",trade_order_id3);

        pama.put("memberId4",memberId4);
        pama.put("cyId4",cyId4);
        pama.put("num4",num4);
        pama.put("numOptions4",numOptions4);
        pama.put("forzen4",forzen4);
        pama.put("forzenOptions4",forzenOptions4);
        pama.put("trade_order_id4",trade_order_id4);

        return  memberMapper.assets_cpoy(pama);
    }

    public int orderConfirm(
            int memberId, String cyId, BigDecimal num, String numOptions, BigDecimal forzen, String forzenOptions,
            int memberId1, String cyId1, BigDecimal num1, String numOptions1, BigDecimal forzen1, String forzenOptions1,
            int memberId2, String cyId2, BigDecimal num2, String numOptions2, BigDecimal forzen2, String forzenOptions2,
            int memberId3, String cyId3, BigDecimal num3, String numOptions3, BigDecimal forzen3, String forzenOptions3,
            int memberId4, String cyId4, BigDecimal num4, String numOptions4, BigDecimal forzen4, String forzenOptions4,

            int orderId1 ,BigDecimal tradeNum1, long tradeTime1,
            int orderId2 ,BigDecimal tradeNum2, long tradeTime2,

            String tradeNo1,int t_memberId1,int t_currencyId1,int currencyTradeId1,BigDecimal price1,BigDecimal t_num1,BigDecimal t_money1,BigDecimal fee1,String t_type1, long t_addTime1,int t_status1,int show1, int t_orders_id1,

            String tradeNo2 ,int t_memberId2 ,int t_currencyId2,int currencyTradeId2,BigDecimal price2,BigDecimal t_num2,BigDecimal t_money2,BigDecimal fee2,String t_type2, long t_addTime2,int t_status2,int show2,int t_orders_id2,

            int f_memberId1,int f_type1 ,int f_moneyType1,BigDecimal f_money1 ,long f_addTime1 ,int f_currencyId1 ,String  f_ip1 ,String  f_content1 ,
            int f_memberId2,int f_type2 ,int f_moneyType2,BigDecimal f_money2 ,long f_addTime2 ,int f_currencyId2 ,String  f_ip2 ,String  f_content2
            )
    {

        HashMap pama = new HashMap();
        pama.put("memberId",memberId);
        pama.put("cyId",cyId);
        pama.put("num",num);
        pama.put("numOptions",numOptions);
        pama.put("forzen",forzen);
        pama.put("forzenOptions",forzenOptions);


        pama.put("memberId1",memberId1);
        pama.put("cyId1",cyId1);
        pama.put("num1",num1);
        pama.put("numOptions1",numOptions1);
        pama.put("forzen1",forzen1);
        pama.put("forzenOptions1",forzenOptions1);

        pama.put("memberId2",memberId2);
        pama.put("cyId2",cyId2);
        pama.put("num2",num2);
        pama.put("numOptions2",numOptions2);
        pama.put("forzen2",forzen2);
        pama.put("forzenOptions2",forzenOptions2);

        pama.put("memberId3",memberId3);
        pama.put("cyId3",cyId3);
        pama.put("num3",num3);
        pama.put("numOptions3",numOptions3);
        pama.put("forzen3",forzen3);
        pama.put("forzenOptions3",forzenOptions3);

        pama.put("memberId4",memberId4);
        pama.put("cyId4",cyId4);
        pama.put("num4",num4);
        pama.put("numOptions4",numOptions4);
        pama.put("forzen4",forzen4);
        pama.put("forzenOptions4",forzenOptions4);


        pama.put("orderId1",orderId1);
        pama.put("tradeNum1",tradeNum1);
        pama.put("tradeTime1",tradeTime1);

        pama.put("orderId2",orderId2);
        pama.put("tradeNum2",tradeNum2);
        pama.put("tradeTime2",tradeTime2);


        pama.put("tradeNo1",tradeNo1);
        pama.put("t_memberId1",t_memberId1);
        pama.put("t_currencyId1",t_currencyId1);
        pama.put("currencyTradeId1",currencyTradeId1);
        pama.put("price1",price1);
        pama.put("t_num1",t_num1);
        pama.put("t_money1",t_money1);
        pama.put("fee1",fee1);
        pama.put("t_type1",t_type1);
        pama.put("t_addTime1",t_addTime1);
        pama.put("t_status1",t_status1);
        pama.put("show1",show1);
        pama.put("t_orders_id1",t_orders_id1);



        pama.put("tradeNo2",tradeNo2);
        pama.put("t_memberId2",t_memberId2);
        pama.put("t_currencyId2",t_currencyId2);
        pama.put("currencyTradeId2",currencyTradeId2);
        pama.put("price2",price2);
        pama.put("t_num2",t_num2);
        pama.put("t_money2",t_money2);
        pama.put("fee2",fee2);
        pama.put("t_type2",t_type2);
        pama.put("t_addTime2",t_addTime2);
        pama.put("t_status2",t_status2);
        pama.put("show2",show2);
        pama.put("t_orders_id2",t_orders_id2);


        pama.put("f_memberId1",f_memberId1);
        pama.put("f_type1",f_type1);
        pama.put("f_moneyType1",f_moneyType1);
        pama.put("f_money1",f_money1);
        pama.put("f_addTime1",f_addTime1);
        pama.put("f_currencyId1",f_currencyId1);
        pama.put("f_ip1",f_ip1);
        pama.put("f_content1",f_content1);

        pama.put("f_memberId2",f_memberId2);
        pama.put("f_type2",f_type2);
        pama.put("f_moneyType2",f_moneyType2);
        pama.put("f_money2",f_money2);
        pama.put("f_addTime2",f_addTime2);
        pama.put("f_currencyId2",f_currencyId2);
        pama.put("f_ip2",f_ip2);
        pama.put("f_content2",f_content2);



        return  memberMapper.orderConfirm(pama);
    }



    /**
     * 发送邮箱提示
     * @param email
     * @param type
     * @param title
     * @param content
     * @return
     */
    public boolean sendEmailCode(String email, String type, String title, String content, String ip, int sendTimes,String logContent){
        Map map=new HashMap();
        map.put("email",email);
        map.put("type",type);
        map.put("content",logContent);
        map=yangEmailLogMapper.getLastEmail(map);
        if(map!=null){
            long addTime=(long)map.get("addTime");
            long nowTimes= DateUtils.getNowTimesLong();
            if(nowTimes-addTime<=CoinConst.EMAIL_WDC_KD_TS_SEND_TIMES){
                return true;
            }

        }
        Result result=new Result();
        result.setCode(Result.Code.SUCCESS);
        //key
        Example example=new Example(YangEmail.class);
        example.setOrderByClause("send_time asc");
        example.createCriteria().andEqualTo("status",CoinConst.EMAIL_STATUS_NORMAL);
        List<YangEmail> emailList= yangEmailMapper.selectByExample(example);

        YangEmail yangEmail=emailList.get(0);
        //发件人账户名
        String senderAccount = yangEmail.getUsername();
        //发件人账户密码
        String senderPassword=yangEmail.getPassword();
        try{
            //1、连接邮件服务器的参数配置
            Properties props = new Properties();
            //设置用户的认证方式
            props.setProperty("mail.smtp.auth", "true");
            //设置传输协议s
            props.setProperty("mail.transport.protocol", "smtp");
            //设置发件人的SMTP服务器地址
            props.setProperty("mail.smtp.host", yangEmail.getEmailHost());

            // 如果使用ssl，则去掉使用25端口的配置，进行如下配置
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

            props.put("mail.smtp.socketFactory.port", yangEmail.getPort().toString());

            props.put("mail.smtp.port", yangEmail.getPort().toString());

            //2、创建定义整个应用程序所需的环境信息的 Session 对象
            Session session = Session.getInstance(props);
            //设置调试信息在控制台打印出来
            session.setDebug(false);
            //3、创建邮件的实例对象
            Message msg = EmailUtils.getMimeMessage(session,email,yangEmail.getSendUser(),title,"",content);
            //4、根据session对象获取邮件传输对象Transport
            Transport transport = session.getTransport();
            //设置发件人的账户名和密码
            transport.connect(senderAccount, senderPassword);
            //发送邮件，并发送到所有收件人地址，message.getAllRecipients() 获取到的是在创建邮件对象时添加的所有收件人, 抄送人, 密送人
            transport.sendMessage(msg,msg.getAllRecipients());

            //如果只想发送给指定的人，可以如下写法
            //transport.sendMessage(msg, new Address[]{new InternetAddress("xxx@qq.com")});
            //5、关闭邮件连接
            transport.close();
            //更新邮箱信息
            yangEmail.setSendNum(yangEmail.getSendNum()+1);
            yangEmail.setSendTime(DateUtils.getNowTimes());
            yangEmailMapper.updateByPrimaryKeySelective(yangEmail);
            //更新邮箱日志
            YangEmailLog yangEmailLog=new YangEmailLog();
            yangEmailLog.setAcceptMail(email);
            yangEmailLog.setSendMail(yangEmail.getSendUser());
            yangEmailLog.setAddTime(DateUtils.getNowTimes());
            yangEmailLog.setTitle(title);
            yangEmailLog.setContent(logContent);
            yangEmailLog.setIp(ip);
            yangEmailLog.setType(Integer.valueOf(type));
            yangEmailLog.setWrongTimes(0);
            yangEmailLogMapper.insert(yangEmailLog);
        }catch (Exception e){
            sendTimes++;
            e.printStackTrace();
            yangEmail.setError(String.valueOf(Integer.valueOf(yangEmail.getError()).intValue()+1));
            yangEmail.setSendNum(yangEmail.getSendNum()+1);
            yangEmail.setSendTime(DateUtils.getNowTimes());
            yangEmailMapper.updateByPrimaryKeySelective(yangEmail);
            sendEmailCode( email, type, title, content, ip,sendTimes,logContent);
        }
        return true;
    }

}
