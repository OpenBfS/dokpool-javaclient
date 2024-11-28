package de.bfs.dokpool.client.base;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;

import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPatch;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.ByteArrayEntity;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.http.message.StatusLine;
import org.apache.hc.core5.http.NameValuePair;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * This class will be a wrapper around Apache HttpClient so a switch to the integrated Java Http Client (Java 11+)
 * remains possible.
 */
public class HttpClient {

	private static final Log log = LogFactory.getLog(HttpClient.class);

	public class Headers {
		public static final String AUTHORIZATION = "Authorization";
		public static final String ACCEPT = "Accept";
	}

	public class MimeTypes {
		public static final String JSON = "application/json";
		/* Plone-REST-API does not actually use the application/json-patch+json format,
		 * but expects normal JSON with only the updated attributes set, see
		 * https://6.docs.plone.org/plone.restapi/docs/source/usage/content.html
		 * We nevertheless introduce JSONPATCH:
		 */
		public static final String JSONPATCH = "application/json-patch+json";
		public static final String PLAIN = "text/plain";
	}

	public static class Response {
		public final int status;
		public final String content;
		public final Map<String,String> headers;

		private Response(final int status, final String content, Header[] headers) {
			this.status = status;
			this.content = content;
			this.headers = new HashMap<>();
			for (Header header: headers) {
				this.headers.put(header.getName(), header.getValue());
			}
		}
	}

	private static int intPortFromPortOrProtocol(String port, final String proto){
		port = port != null? port : "";
		port = port.equals("") ? (proto.equals("https")?"443":"80"): port;
		return Integer.parseInt(port);
	}

	public static String composeUrl(final String proto, final String host, String port, final String path){
		port = port.equals("") ? "" : (":" +  port);
		return proto + "://" + host + port + path;
	}

	public static void addBasicAuthToHeaders(Map<String, String> headers, final String username, final String password){
		final String auth = username + ":" + password;
		//if we need multiple charset support, there is also: Charset.forName("UTF-8")
		byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
		final String authHeaderVal = "Basic " + new String(encodedAuth);
		headers.put(Headers.AUTHORIZATION, authHeaderVal);
	}

	private static void addHeadersToRequest(HttpRequest request, Map<String,String> headers){
		if (headers == null){
			return;
		}
		for (Map.Entry<String, String> entry : headers.entrySet()) {
			request.setHeader(entry.getKey(), entry.getValue());
		}
	}

	public static boolean tlsLogging = false;
	private final static void logTLS(HttpClientContext clientContext){
		final SSLSession sslSession = clientContext.getSSLSession();
		if (sslSession != null) {
			try {
				log.info("Peer: " + sslSession.getPeerPrincipal());
				log.info("TLS protocol: " + sslSession.getProtocol());
				log.info("TLS cipher suite: " + sslSession.getCipherSuite());
			} catch (final SSLPeerUnverifiedException ignore) {
			}
		}
	}

	public final static Response doGetRequest(final String proto, final String host, String port, final String url, Map<String,String> headers) throws Exception {
		// Use a try-with-resources, as CloseableHttpClient should be closed():
		try (CloseableHttpClient httpclient = HttpClients.custom().disableRedirectHandling().build()) {

			final int portInt = intPortFromPortOrProtocol(port,proto);
			final HttpHost target = new HttpHost(proto, host, portInt);
			final HttpGet httpget = new HttpGet(url);
			addHeadersToRequest(httpget,headers);

			log.info("Executing request " + httpget.getMethod() + " " + httpget.getUri());

			final HttpClientContext clientContext = HttpClientContext.create();
			Response response = httpclient.execute(target, httpget, clientContext, rsp -> {
				log.info(httpget + "->" + new StatusLine(rsp));
				if (proto.equals("https") && tlsLogging){
					logTLS(clientContext);
				}
				HttpEntity entity = rsp.getEntity();
				String content = entity != null ? EntityUtils.toString(entity): "";
				EntityUtils.consume(entity);
				return new Response(rsp.getCode(), content, rsp.getHeaders());
			});

			return response;
		}
	}

	public final static Response doDeleteRequest(final String proto, final String host, String port, final String url, Map<String,String> headers) throws Exception {
		try (CloseableHttpClient httpclient = HttpClients.custom().disableRedirectHandling().build()) {

			final int portInt = intPortFromPortOrProtocol(port,proto);
			final HttpHost target = new HttpHost(proto, host, portInt);
			final HttpDelete httpdel = new HttpDelete(url);
			addHeadersToRequest(httpdel,headers);

			log.info("Executing request " + httpdel.getMethod() + " " + httpdel.getUri());

			final HttpClientContext clientContext = HttpClientContext.create();
			Response response = httpclient.execute(target, httpdel, clientContext, rsp -> {
				log.info(httpdel + "->" + new StatusLine(rsp));
				if (proto.equals("https") && tlsLogging){
					logTLS(clientContext);
				}
				HttpEntity entity = rsp.getEntity();
				String content = entity != null ? EntityUtils.toString(entity): "";
				EntityUtils.consume(entity);
				return new Response(rsp.getCode(), content, rsp.getHeaders());
			});

			return response;
		}
	}

	public final static Response doPostRequest(final String proto, final String host, String port, final String url, Map<String,String> headers, Map<String,String> parameters, String contentType, byte[] data) throws Exception {
		try (CloseableHttpClient httpclient = HttpClients.custom().disableRedirectHandling().build()) {
			final int portInt = intPortFromPortOrProtocol(port,proto);
			final HttpHost target = new HttpHost(proto, host, portInt);
			final HttpPost httppost = new HttpPost(url);
			addHeadersToRequest(httppost,headers);

			log.info("Executing request " + httppost.getMethod() + " " + httppost.getUri());

			if (parameters != null){
				List<NameValuePair> paramList = new ArrayList<>();
				for (Map.Entry<String, String> entry : parameters.entrySet()) {
					paramList.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
				}
				httppost.setEntity(new UrlEncodedFormEntity(paramList));
			}

			if (data != null){
				httppost.setHeader("Content-type", contentType);
				ByteArrayEntity dataEntity = new ByteArrayEntity(data,ContentType.create(contentType));
				httppost.setEntity(dataEntity);
			}

			final HttpClientContext clientContext = HttpClientContext.create();
			// final BasicCookieStore cookieStore = new BasicCookieStore();
			// clientContext.setCookieStore(cookieStore);
			Response response = httpclient.execute(target, httppost, clientContext, rsp -> {
				log.info(httppost + "->" + new StatusLine(rsp));
				if (proto.equals("https") && tlsLogging){
					logTLS(clientContext);
				}
				HttpEntity entity = rsp.getEntity();
				String content = entity != null ? EntityUtils.toString(entity): "";
				EntityUtils.consume(entity);
				return new Response(rsp.getCode(), content, rsp.getHeaders());
			});

			return response;
		}
	}

	public final static Response doPutRequest(final String proto, final String host, String port, final String url, Map<String,String> headers, String contentType, byte[] data) throws Exception {
		try (CloseableHttpClient httpclient = HttpClients.custom().disableRedirectHandling().build()) {
			final int portInt = intPortFromPortOrProtocol(port,proto);
			final HttpHost target = new HttpHost(proto, host, portInt);
			final HttpPut httpput = new HttpPut(url);
			addHeadersToRequest(httpput,headers);

			httpput.setHeader("Content-type", contentType);
			ByteArrayEntity putEntity = new ByteArrayEntity(data,ContentType.create(contentType));
			httpput.setEntity(putEntity);

			log.info("Executing request " + httpput.getMethod() + " " + httpput.getUri());

			final HttpClientContext clientContext = HttpClientContext.create();
			Response response = httpclient.execute(target, httpput, clientContext, rsp -> {
				log.info(httpput + "->" + new StatusLine(rsp));
				if (proto.equals("https") && tlsLogging){
					logTLS(clientContext);
				}
				HttpEntity entity = rsp.getEntity();
				String content = entity != null ? EntityUtils.toString(entity): "";
				EntityUtils.consume(entity);
				return new Response(rsp.getCode(), content, rsp.getHeaders());
			});

			return response;
		}
	}

	public final static Response doPatchRequest(final String proto, final String host, String port, final String url, Map<String,String> headers, String contentType, byte[] data) throws Exception {
		try (CloseableHttpClient httpclient = HttpClients.custom().disableRedirectHandling().build()) {
			final int portInt = intPortFromPortOrProtocol(port,proto);
			final HttpHost target = new HttpHost(proto, host, portInt);
			final HttpPatch httppatch = new HttpPatch(url);
			addHeadersToRequest(httppatch,headers);

			httppatch.setHeader("Content-type", contentType);
			ByteArrayEntity putEntity = new ByteArrayEntity(data,ContentType.create(contentType));
			httppatch.setEntity(putEntity);

			log.info("Executing request " + httppatch.getMethod() + " " + httppatch.getUri());

			final HttpClientContext clientContext = HttpClientContext.create();
			Response response = httpclient.execute(target, httppatch, clientContext, rsp -> {
				log.info(httppatch + "->" + new StatusLine(rsp));
				if (proto.equals("https") && tlsLogging){
					logTLS(clientContext);
				}
				HttpEntity entity = rsp.getEntity();
				String content = entity != null ? EntityUtils.toString(entity): "";
				EntityUtils.consume(entity);
				return new Response(rsp.getCode(), content, rsp.getHeaders());
			});

			return response;
		}
	}
}

