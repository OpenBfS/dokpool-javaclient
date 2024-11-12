package de.bfs.dokpool.client.base;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;

import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpHost;
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
	public static boolean tlsLogging = false;

	private static int intPortFromPortOrProtocol(String port, final String proto){
		port = port != null? port : "";
		port = port.equals("") ? (proto.equals("https")?"443":"80"): port;
		return Integer.parseInt(port);
	}

	public static String composeUrl(final String proto, final String host, String port, final String path){
		port = port.equals("") ? "" : (":" +  port);
		return proto + "://" + host + port + path;
	}

	public final static Response doGetRequest(final String proto, final String host, String port, final String url) throws Exception {
		try (CloseableHttpClient httpclient = HttpClients.createSystem()) {

			final int portInt = intPortFromPortOrProtocol(port,proto);
			final HttpHost target = new HttpHost(proto, host, portInt);
			final HttpGet httpget = new HttpGet(url);

			log.info("Executing request " + httpget.getMethod() + " " + httpget.getUri());

			final HttpClientContext clientContext = HttpClientContext.create();
			Response response = httpclient.execute(target, httpget, clientContext, rsp -> {
				log.info(httpget + "->" + new StatusLine(rsp));
				if (proto.equals("https") && tlsLogging){
					logTLS(clientContext);
				}
				HttpEntity entity = rsp.getEntity();
				String content = EntityUtils.toString(entity);
				EntityUtils.consume(entity);
				return new Response(rsp.getCode(), content);
			});

			return response;
		}
	}

	public final static Response doDeleteRequest(final String proto, final String host, String port, final String url) throws Exception {
		try (CloseableHttpClient httpclient = HttpClients.createSystem()) {

			final int portInt = intPortFromPortOrProtocol(port,proto);
			final HttpHost target = new HttpHost(proto, host, portInt);
			final HttpDelete httpdel = new HttpDelete(url);

			log.info("Executing request " + httpdel.getMethod() + " " + httpdel.getUri());

			final HttpClientContext clientContext = HttpClientContext.create();
			Response response = httpclient.execute(target, httpdel, clientContext, rsp -> {
				log.info(httpdel + "->" + new StatusLine(rsp));
				if (proto.equals("https") && tlsLogging){
					logTLS(clientContext);
				}
				HttpEntity entity = rsp.getEntity();
				String content = EntityUtils.toString(entity);
				EntityUtils.consume(entity);
				return new Response(rsp.getCode(), content);
			});

			return response;
		}
	}

	public final static Response doPostRequest(final String proto, final String host, String port, final String url, Map<String,String> parameters) throws Exception {
		try (CloseableHttpClient httpclient = HttpClients.createSystem()) {
			final int portInt = intPortFromPortOrProtocol(port,proto);
			final HttpHost target = new HttpHost(proto, host, portInt);
			final HttpPost httppost = new HttpPost(url);

			log.info("Executing request " + httppost.getMethod() + " " + httppost.getUri());

			List<NameValuePair> paramList = new ArrayList<>();
			for (Map.Entry<String, String> entry : parameters.entrySet()) {
				paramList.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
			}
        	httppost.setEntity(new UrlEncodedFormEntity(paramList));

			final HttpClientContext clientContext = HttpClientContext.create();
			// final BasicCookieStore cookieStore = new BasicCookieStore();
			// clientContext.setCookieStore(cookieStore);
			Response response = httpclient.execute(target, httppost, clientContext, rsp -> {
				log.info(httppost + "->" + new StatusLine(rsp));
				if (proto.equals("https") && tlsLogging){
					logTLS(clientContext);
				}
				HttpEntity entity = rsp.getEntity();
				String content = EntityUtils.toString(entity);
				EntityUtils.consume(entity);
				return new Response(rsp.getCode(), content);
			});

			return response;
		}
	}

	public final static Response doPutRequest(final String proto, final String host, String port, final String url, String contentType, byte[] data) throws Exception {
		try (CloseableHttpClient httpclient = HttpClients.createSystem()) {
			final int portInt = intPortFromPortOrProtocol(port,proto);
			final HttpHost target = new HttpHost(proto, host, portInt);
			final HttpPut httpput = new HttpPut(url);

			httpput.setHeader("Content-type", contentType);
			ByteArrayEntity putEntity = new ByteArrayEntity(data,ContentType.create(contentType));
			httpput.setEntity(putEntity);

			log.info("Executing request " + httpput.getMethod() + " " + httpput.getUri());

			final HttpClientContext clientContext = HttpClientContext.create();
			// final BasicCookieStore cookieStore = new BasicCookieStore();
			// clientContext.setCookieStore(cookieStore);
			Response response = httpclient.execute(target, httpput, clientContext, rsp -> {
				log.info(httpput + "->" + new StatusLine(rsp));
				if (proto.equals("https") && tlsLogging){
					logTLS(clientContext);
				}
				HttpEntity entity = rsp.getEntity();
				String content = EntityUtils.toString(entity);
				EntityUtils.consume(entity);
				return new Response(rsp.getCode(), content);
			});

			return response;
		}
	}

	public static class Response {
        public final int status;
        public final String content;

        Response(final int status, final String content) {
            this.status = status;
            this.content = content;
        }
    }

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
}

