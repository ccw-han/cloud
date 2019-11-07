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

import org.apache.commons.codec.binary.Hex;
import org.apache.http.client.fluent.Request;

/**
 * @author sms
 */
public class SendHex extends DemoBase {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		// 使用Hex编码内容
		String hex = Hex.encodeHexString( message.getBytes("UTF-16BE") );

		StringBuilder sb = new StringBuilder();
		sb.append( http ).append("mt?")
				.append("un=").append( username )
				.append("&pw=").append( URLEncoder.encode(password,"utf8") )
				.append("&da=").append( mobile )
				.append("&dc=").append( dataCoding )
				.append("&sm=").append( hex );

		String req = sb.toString();
		System.out.println("request: " + req);
		String result = Request.Get( req ).execute().returnContent().asString();
		System.out.println( "response: " + result );

	}

}
