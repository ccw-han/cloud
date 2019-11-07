/*
 * Copyright (C) WZ-Soft.  All rights reserved.
 *
 * This software is the confidential and proprietary
 * information of WZ-Soft. ("Confidential Information").
 * You shall not disclose such Confidential Information
 * and shall use it only in accordance with the terms of
 * the license agreement you entered into with WZ-Soft.
 */
package net.cyweb.config.mes;

import java.net.URLEncoder;

import org.apache.http.client.fluent.Request;


/**
 * @author sms
 */

public class SendUtf8 extends DemoBase {

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {

        int transferEncoding = 3; // URLEncode+UTF8
        int responseFormat = 1; // 返回格式为xml格式

        StringBuilder sb = new StringBuilder();
        sb.append(http).append("mt?")
                .append("un=").append(username)
                .append("&pw=").append(URLEncoder.encode(password, "utf8"))
                .append("&da=").append(mobile)
               // .append("&sa=").append(shortCode)
                //.append("&ex=").append(getExternalId())
                .append("&dc=").append(dataCoding)
                .append("&tf=").append(transferEncoding)
                .append("&rf=").append(responseFormat)
                .append("&sm=").append(URLEncoder.encode(message, "utf8"));

        String req = sb.toString();
        System.out.println("request: " + req);
        String result = Request.Get(req).execute().returnContent().asString();

        System.out.println("response: " + result);
    }

}
