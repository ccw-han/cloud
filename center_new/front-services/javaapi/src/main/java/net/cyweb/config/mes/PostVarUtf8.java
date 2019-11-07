/*
 * Copyright (C) WZ-Soft.  All rights reserved.
 *
 * This software is the confidential and proprietary
 * information of WZ-Soft. ("Confidential Information").
 * You shall not disclose such Confidential Information
 * and shall use it only in accordance with the terms of
 * the license agreement you entered into with WZ-Soft.
 */
package net.cyweb.config.mes;;

import java.net.URLEncoder;

import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;

/**
 * @author sms
 */
public class PostVarUtf8 extends DemoBase {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		String mobile = "$$"; // 分割符

		int transferEncoding = 3; // URLEncode+UTF8
		int responseFormat = 1; // 返回格式为xml格式
		String message = "13612345678#1#这是一条$$测试短信1$$13612345679#这是一条$测试短信2$$";
		dataCoding = 15;

		StringBuilder sb = new StringBuilder();
		sb.append("un=").append( username )
				.append("&pw=").append( URLEncoder.encode(password, "utf8") )
				.append("&da=").append( URLEncoder.encode(mobile,"utf8") )
				.append("&sa=").append( shortCode )
				.append("&dc=").append( dataCoding )
				.append("&tf=").append( transferEncoding )
				.append("&rf=").append( responseFormat )
				.append("&ex=").append( getExternalId() )
				.append("&sm=").append( URLEncoder.encode(message,  "utf8") );

		String req = sb.toString();
		System.out.println("request: " + req);

		String result = Request.Post( http + "mt" )
				.bodyString(req,  ContentType.APPLICATION_FORM_URLENCODED )
				.execute().returnContent().asString();
		System.out.println( "response: " + result );

	}

}
