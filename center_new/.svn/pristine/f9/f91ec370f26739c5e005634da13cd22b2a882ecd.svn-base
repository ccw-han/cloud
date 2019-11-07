package net.cyweb.config.custom;

import javax.mail.BodyPart;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.*;
import java.util.Date;
import java.util.Random;

public class EmailUtils {

    public static String reMailString() throws Exception {
        //String info="";
        StringBuffer buff = new StringBuffer();
        InputStreamReader in = null;
        BufferedReader br = null;
        String path = System.getProperty("user.dir") + "/src/html/email2.html";
        File file = new File(path);
        try {
            in = new InputStreamReader(new FileInputStream(file));
            br = new BufferedReader(in);
            String line = null;
            while ((line = br.readLine()) != null) {
                //System.out.println(line);
                buff.append(line).append("\n");
            }


        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        return buff.toString();
    }

    public static String emailContent(String code, String content, String title) {
        StringBuffer sf = new StringBuffer();
        sf.append("<!DOCTYPE html>");
        sf.append("<html><head>");
        sf.append("<title>【BiMin】安全验证</title>");
        sf.append("<style type='text/css'>");
        sf.append("*{padding: 0;margin:0;box-sizing: border-box;}");
        sf.append(".E-box{ border: 1px solid #cdcdcd; width: 640px;margin: auto;font-size: 12px;color: #1E2731;line-height: 20px; }");
        sf.append(" .head{background-color: #454c6d;height: 80px;line-height: 80px;text-align: center;}");
        sf.append(" .head a{color: white;display: inline-block;margin: auto;text-decoration: none;font-size: 40px;font-weight: bold;font-style:italic;}");
        sf.append(".content{padding: 20px;}");
        sf.append("</style></head>");
        sf.append("<body> ");
        sf.append("<div class='E-box' >");
        sf.append("<div class='head'><a href='http://bimin.co/' target='_blank'>BiMin</a></div>");

        sf.append("<div class='content'>");
        sf.append("亲爱的用户您好:<br/><br/><br/><br/>");
        sf.append(title + " : ");
        /*sf.append(code + "<br/><br/>");*/
        sf.append(content + "<br/><br/><br/><br/>");
        sf.append("BIMIN TEAM<br/>");
        sf.append("<a href='https://www.bimin.co/' target='_blank'>https://www.bimin.co/</a>");
        sf.append("</div>");
        sf.append("</div>");
        sf.append("</body></html>");
        return sf.toString();
    }

    public static String emailNormalContent(String content, String title) {
        StringBuffer sf = new StringBuffer();
        sf.append("<!DOCTYPE html>");
        sf.append("<html><head>");
        sf.append("<title>【BiMin】安全验证</title> ");
        sf.append("<style type='text/css'>");
        sf.append("*{padding: 0;margin:0;box-sizing: border-box;}");
        sf.append(".E-box{ border: 1px solid #cdcdcd; width: 640px;margin: auto;font-size: 12px;color: #1E2731;line-height: 20px;}");
        sf.append(" .head{background-color: #454c6d;height: 80px;line-height: 80px;text-align: center;}");
        sf.append(" .head a{color: white;display: inline-block;margin: auto;text-decoration: none;font-size: 40px;font-weight: bold;font-style:italic;}");
        sf.append(".content{padding: 20px;}");
        sf.append("</style></head>");

        sf.append("<body>");
        sf.append("<div class='E-box'>");
        sf.append("<div class='head'><a href='http://bimin.co/' target='_blank'>BiMin</a></div>");

        sf.append("<div class='content'>");
        sf.append("亲爱的用户您好:<br/><br/><br/><br/>");
        sf.append(title + " : ");
        sf.append(content + "<br/><br/><br/><br/>");
        sf.append("BIMIN TEAM<br/> ");
        sf.append("<a href='https://www.bimin.co/' target='_blank'>https://www.bimin.co/</a>");
        sf.append("</div>");
        sf.append("</div>");
        sf.append("</body></html>");
        return sf.toString();
    }

    public static MimeMessage getMimeMessage(Session session, String email, String senderAddress, String title, String emailCode, String content) throws Exception {
        //创建一封邮件的实例对象
        MimeMessage msg = new MimeMessage(session);
        Multipart mainPart = new MimeMultipart();
        BodyPart body = new MimeBodyPart();
        body.setContent(EmailUtils.emailContent(emailCode, content, title), "text/html;charset=utf-8");
        mainPart.addBodyPart(body);
        msg.setContent(mainPart);
        //收件人地址
        String recipientAddress = email;
        //设置发件人地址
        msg.setFrom(new InternetAddress(senderAddress));
        /**
         * 设置收件人地址（可以增加多个收件人、抄送、密送），即下面这一行代码书写多行
         * MimeMessage.RecipientType.TO:发送
         * MimeMessage.RecipientType.CC：抄送
         * MimeMessage.RecipientType.BCC：密送
         */
        msg.setRecipient(MimeMessage.RecipientType.TO, new InternetAddress(recipientAddress));
        //设置邮件主题
        msg.setSubject(title, "UTF-8");
        //设置邮件的发送时间,默认立即发送
        msg.setSentDate(new Date());
        return msg;
    }

    public static MimeMessage getNormalMimeMessage(Session session, String email, String senderAddress, String title, String content) throws Exception {
        //创建一封邮件的实例对象
        MimeMessage msg = new MimeMessage(session);
        Multipart mainPart = new MimeMultipart();
        BodyPart body = new MimeBodyPart();
        body.setContent(EmailUtils.emailNormalContent(content, title), "text/html;charset=utf-8");
        mainPart.addBodyPart(body);
        msg.setContent(mainPart);
        //收件人地址
        String recipientAddress = email;
        //设置发件人地址
        msg.setFrom(new InternetAddress(senderAddress));
        /**
         * 设置收件人地址（可以增加多个收件人、抄送、密送），即下面这一行代码书写多行
         * MimeMessage.RecipientType.TO:发送
         * MimeMessage.RecipientType.CC：抄送
         * MimeMessage.RecipientType.BCC：密送
         */
        msg.setRecipient(MimeMessage.RecipientType.TO, new InternetAddress(recipientAddress));
        //设置邮件主题
        msg.setSubject(title, "UTF-8");
        //设置邮件的发送时间,默认立即发送
        msg.setSentDate(new Date());
        return msg;
    }

    public static String getRandomNumber(int digCount) {
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder(digCount);
        for (int i = 0; i < digCount; i++)
            sb.append((char) ('0' + rnd.nextInt(10)));
        return sb.toString();
    }


}
