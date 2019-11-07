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
public class Query extends DemoBase {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		StringBuilder sb = new StringBuilder();
		sb.append( http ).append("mo?")
			.append("un=").append( username )
			.append("&pw=").append( URLEncoder.encode(password,"utf8")  )
			.append("&fs=").append( fetchSize );
		
		String req = sb.toString();
		System.out.println("request: " + req);
		String result = Request.Get( req ).execute().returnContent().asString();
		System.out.println( "response: " + result );
	}

}
