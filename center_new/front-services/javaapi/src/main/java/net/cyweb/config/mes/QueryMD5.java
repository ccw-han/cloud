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
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.fluent.Request;

/**
 * @author sms
 */
public class QueryMD5 extends DemoBase {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		// 计算密码MD5
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		String timestamp = sdf.format(new Date() );
		MessageDigest md5 = MessageDigest.getInstance("MD5");
		md5.update( username.getBytes("utf8") );
		md5.update( password.getBytes("utf8") );
		md5.update( timestamp.getBytes("utf8") );
		password = Base64.encodeBase64String( md5.digest() );

		StringBuilder sb = new StringBuilder();
		sb.append( http ).append( "mo?" )
				.append("un=").append( username )
				.append("&pw=").append( URLEncoder.encode(password,"utf8") )
				.append("&fs=").append( fetchSize )
				.append("&ts=").append( timestamp )
				.append("&rf=").append( 1 ); // 控制返回格式xml

		String req = sb.toString();
		System.out.println("request: " + req);

		String result = Request.Get( req ).execute().returnContent().asString();
		System.out.println( "response: " + result );
	}

}
