/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.cyweb.config.mes;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;

/**
 * 演示代码，切勿用在生产环境
 *
 * @author sms
 */
public class HttpsUtil {

	public static class TrustedStrategy implements TrustStrategy {

		@Override
		public boolean isTrusted(final X509Certificate[] chain, final String authType) throws CertificateException {
			return true;
		}

	}

	public static String doHttp(HttpRequestBase request) throws Exception {

		SSLConnectionSocketFactory sslctx = new SSLConnectionSocketFactory(SSLContexts
				.custom()
				.loadTrustMaterial(null, new TrustedStrategy() )
				.build() );

		CloseableHttpClient httpClient  = HttpClients.custom()
				.setSSLSocketFactory( sslctx )
				.build();

		return EntityUtils.toString( httpClient.execute(request).getEntity());
	}


}
