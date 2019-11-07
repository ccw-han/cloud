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
 * 此代码需要2.6.116版本以上的服务器。
 *
 * @author sms
 */
public class SendVarUtf8Mask extends DemoBase {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		String mobile = "|"; // 分割符

		int transferEncoding = 3; // URLEncode+UTF8
		int responseFormat = 1; // 返回格式为xml格式
		String message = "13612345678#888822#这是一条测试短信1|13612345679#888833#这是一条测试短信2";
		int dataCoding = 15;

		StringBuilder sb = new StringBuilder();
		sb.append( http ).append( "mt?")
				.append("un=").append( username )
				.append("&pw=").append( URLEncoder.encode(password, "utf8") )
				.append("&da=").append( URLEncoder.encode(mobile,"utf8") )
				.append("&sa=").append( shortCode )
				.append("&dc=").append( dataCoding )
				.append("&tf=").append( transferEncoding )
				.append("&rf=").append( responseFormat )
				.append("&fm=").append( 5 ) // 手机号，扩展码有效
				.append("&sm=").append( URLEncoder.encode(message,  "utf8") );


		String req = sb.toString();
		System.out.println("request: " + req);

		String result = Request.Get( req ).execute().returnContent().asString();
		System.out.println( "response: " + result );
	}

}
