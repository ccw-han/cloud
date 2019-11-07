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
import org.apache.http.entity.ContentType;

/**
 * @author sms
 */
public class PostVarUCS2 extends DemoBase {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		String mobile = "$$"; // 分割符

		int dataCoding = 8;

		String[] messages = { "这是一条$$Тестовое сообщение", "这是一条Тестовое сообщение2" };
		String[] dests =  { "13812340001", "13812340002" };

		StringBuilder msg = new StringBuilder();
		for(int i = 0; i < messages.length; i++) {
			if ( i > 0 ) msg.append( mobile );
			msg.append(dests[i] ).append("#");
			msg.append( new String( messages[i].getBytes("UTF-16BE"), "ISO8859_1") );
		}

		int transferEncoding = 2; // URLEncode
		int responseFormat = 2; // 返回格式为Json格式

		String message = msg.toString();


		StringBuilder sb = new StringBuilder();
		sb.append("un=").append( username )
				.append("&pw=").append( URLEncoder.encode(password, "utf8") )
				.append("&da=").append( URLEncoder.encode(mobile,"utf8") )
				.append("&sa=").append( shortCode )
				.append("&dc=").append( dataCoding )
				.append("&tf=").append( transferEncoding )
				.append("&rf=").append( responseFormat )
				.append("&ex=").append( getExternalId() )
				.append("&sm=").append( URLEncoder.encode(message,  "ISO8859_1") );

		String req = sb.toString();
		System.out.println("request: " + req);

		String result = Request.Post( http + "mt" )
				.bodyString(req,  ContentType.APPLICATION_FORM_URLENCODED )
				.execute().returnContent().asString();
		System.out.println( "response: " + result );
	}

}
