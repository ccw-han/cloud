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
import org.apache.commons.codec.binary.Hex;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;

/**
 * @author sms
 */
public class PostVarUCS2HexMD5 extends DemoBase {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		int dataCoding = 8;

		String[] messages = { "这是第一条Тестовое сообщение", "这是第二条Тестовое сообщение2" };
		String[] dests =  { "13001000080", "13001000102" };

		StringBuilder msg = new StringBuilder();
		for(int i = 0; i < messages.length; i++) {
			if ( i > 0 ) msg.append( '|' );
			msg.append(dests[i] ).append("#");
			byte[] content = messages[i].getBytes("UTF-16BE");
			msg.append( Hex.encodeHexString(content) );
		}

		int transferEncoding = 0; // HEX
		int responseFormat = 2; // 返回格式为Json格式

		String message = msg.toString();
		System.out.println( message);

		// 计算密码摘要
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		String timestamp = sdf.format(new Date() );

		MessageDigest md5 = MessageDigest.getInstance("MD5");
		md5.update( username.getBytes("utf8") );
		md5.update( password.getBytes("utf8") );
		md5.update( timestamp.getBytes("utf8") );
		md5.update( message.getBytes("ISO8859_1") );
		String password = Base64.encodeBase64String( md5.digest() );

		StringBuilder sb = new StringBuilder();
		sb.append("un=").append( username )
				.append("&pw=").append( URLEncoder.encode(password, "utf8") )
				.append("&ts=").append( timestamp )
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
