package de.bfs.dokpool.client.base;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.StatusLine;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

// import java.util.EnumMap;
// import java.util.Map;

/**
 * This class will be a wrapper around Apache HttpClient so a switch to the integrated Java Http Client (Java 11+)
 * remains possible.
 */
public class HttpClient {

	private static final Log log = LogFactory.getLog(HttpClient.class);
	public static boolean tlsLogging = false;

	public final static Response doGetRequest(final String proto, final String host, String port, final String url) throws Exception {
		try (CloseableHttpClient httpclient = HttpClients.createSystem()) {

			final int portInt = Integer.parseInt(port);
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

