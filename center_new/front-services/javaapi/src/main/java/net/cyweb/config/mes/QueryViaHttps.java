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

import org.apache.http.client.methods.HttpGet;

/**
 * 通过https接口查询上行短信和状态报告
 *
 * @author sms
 */
public class QueryViaHttps extends DemoBase {


	public static void main(String[] args) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append( https ).append("mo?")
				.append("un=").append( username )
				.append("&pw=").append( URLEncoder.encode(password, "utf8")  )
				.append("&fs=").append( fetchSize )
				.append("&rf=").append( 2 ); // 期望返回为Json格式

		String req = sb.toString();
		System.out.println("request: " + req);

		HttpGet get = new HttpGet( req );
		String result = HttpsUtil.doHttp(get);
		System.out.println( "response: " + result );
	}

}
