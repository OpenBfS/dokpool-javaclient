/* Copyright (C) 2015-2026 by Bundesamt fuer Strahlenschutz
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 * See LICENSE for details.
 */

package de.bfs.dokpool.client.base;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;

import java.io.IOException;
import java.net.http.HttpClient.Redirect;
// import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
// import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;

// TODO: Import to specify timeouts.
// import java.time.Duration;

import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.INFO;




/**
 * This class is a wrapper around Apache HttpClient so a switch to the integrated Java Http Client (Java 11+)
 * remains possible.
 */
public class HttpClient {

    //utility classes, no instances
    private HttpClient() {}

    private static final java.lang.System.Logger log = System.getLogger(HttpClient.class.getName());

    private static class HttpRuntimeException extends DokpoolRuntimeException {
        HttpRuntimeException(String msg, Throwable cause) {
            super(msg, cause);
        }
        HttpRuntimeException(String msg) {
            super(msg, null);
        }
    }

    public class Headers {
        private Headers() {}
        public static final String AUTHORIZATION = "Authorization";
        public static final String ACCEPT = "Accept";
    }

    public class MimeTypes {
        private MimeTypes() {}
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

        private Response(final int status, final String content, Map<String,List<String>> headers) {
            this.status = status;
            this.content = content;
            this.headers = new TreeMap<String,String>(String.CASE_INSENSITIVE_ORDER);
            for (Map.Entry<String,List<String>> header: headers.entrySet()) {
                this.headers.put(header.getKey(), header.getValue().get(0));
            }
        }
    }

    // TODO: not needed, unless we have a method where can decouple url and host/port
    // private static int intPortFromPortOrProtocol(String port, final String proto) {
    //     port = port != null? port : "";
    //     port = port.equals("") ? (proto.equals("https")?"443":"80"): port;
    //     return Integer.parseInt(port);
    // }

    public static String composeUrl(final String proto, final String host, String port, final String path) {
        port = port.equals("") ? "" : (":" +  port);
        return proto + "://" + host + port + path;
    }

    public static void addBasicAuthToHeaders(Map<String, String> headers, final String username, final String password) {
        final String auth = username + ":" + password;
        //if we need multiple charset support, there is also: Charset.forName("UTF-8")
        byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
        final String authHeaderVal = "Basic " + new String(encodedAuth);
        headers.put(Headers.AUTHORIZATION, authHeaderVal);
    }

    private static String first1000(String str) {
        return str.substring(0,Math.min(1000,str.length()));
    }

    private static void addHeadersToRequest(HttpRequest.Builder builder, Map<String,String> headers) {
        if (headers == null) {
            return;
        }
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            builder.setHeader(entry.getKey(), entry.getValue());
        }
    }

    public static boolean tlsLogging = false;
    private static void logTLS(Optional<SSLSession> optSslSession) {
        if (!optSslSession.isEmpty()) {
            final SSLSession sslSession = optSslSession.get();
            try {
                log.log(INFO, "Peer: " + sslSession.getPeerPrincipal());
                log.log(INFO, "TLS protocol: " + sslSession.getProtocol());
                log.log(INFO, "TLS cipher suite: " + sslSession.getCipherSuite());
            } catch (final SSLPeerUnverifiedException ignore) {
            }
        }
    }

    public static final Response doGetRequest(final String proto, final String host, String port, final String url, Map<String,String> headers) throws HttpRuntimeException {
        // HttpClient has no auto-close, so normal try and not try-with-resources
        try {
            java.net.http.HttpClient client = java.net.http.HttpClient.newBuilder().followRedirects(Redirect.NEVER).build();

            // HttpClient does not separate URL and connection, so port etc. must match or we need another api.
            // final HttpHost target = new HttpHost(proto, host, portInt);

            HttpRequest.Builder httpgetb = HttpRequest.newBuilder()
                // .timeout(Duration.ofMillis(timeout))
                .GET()//for clarity, actually GET is the default
                .uri(new URI(url));

            addHeadersToRequest(httpgetb, headers);
            HttpRequest httpget = httpgetb.build();

            log.log(INFO, "Executing request " + httpget.method() + " " + httpget.uri());

            HttpResponse<String> rsp = client.send(httpget, BodyHandlers.ofString());
            Response reponse = new Response(rsp.statusCode(), rsp.body(), rsp.headers().map());

            log.log(INFO, rsp.uri() + "->" + rsp.statusCode());
            if (proto.equals("https") && tlsLogging) {
                logTLS(rsp.sslSession());
            }

            return reponse;

        } catch (InterruptedException | IOException ioe) {
            log.log(ERROR, "IO error while connecting to " + url);
            throw new HttpRuntimeException("IO error while connecting to " + url, ioe);
        } catch (URISyntaxException use) {
            throw new HttpRuntimeException("bad url: " + url, use);
        }
    }

    public static final Response doDeleteRequest(final String proto, final String host, String port, final String url, Map<String,String> headers) throws HttpRuntimeException {
        try {
            java.net.http.HttpClient client = java.net.http.HttpClient.newBuilder().followRedirects(Redirect.NEVER).build();

            // HttpClient does not separate URL and connection, so port etc. must match or we need another api.
            // final HttpHost target = new HttpHost(proto, host, portInt);

            HttpRequest.Builder httpdeleteb = HttpRequest.newBuilder()
                // .timeout(Duration.ofMillis(timeout))
                .DELETE()
                .uri(new URI(url));

            addHeadersToRequest(httpdeleteb, headers);
            HttpRequest httpdelete = httpdeleteb.build();

            log.log(INFO, "Executing request " + httpdelete.method() + " " + httpdelete.uri());

            HttpResponse<String> rsp = client.send(httpdelete, BodyHandlers.ofString());
            Response reponse = new Response(rsp.statusCode(), rsp.body(), rsp.headers().map());

            log.log(INFO, rsp.uri() + "->" + rsp.statusCode());
            if (proto.equals("https") && tlsLogging) {
                logTLS(rsp.sslSession());
            }

            return reponse;

        } catch (InterruptedException | IOException ioe) {
            log.log(ERROR, "IO error while connecting to " + url);
            throw new HttpRuntimeException("IO error while connecting to " + url, ioe);
        } catch (URISyntaxException use) {
            throw new HttpRuntimeException("bad url: " + url, use);
        }
    }

    public static final Response doPostRequest(final String proto, final String host, String port, final String url, Map<String,String> headers, Map<String,String> parameters, String contentType, byte[] data) throws HttpRuntimeException {
        try {
            java.net.http.HttpClient client = java.net.http.HttpClient.newBuilder().followRedirects(Redirect.NEVER).build();

            // HttpClient does not separate URL and connection, so port etc. must match or we need another api.
            // final HttpHost target = new HttpHost(proto, host, portInt);

            HttpRequest.Builder httppostb = HttpRequest.newBuilder().uri(new URI(url));

            BodyPublisher bp = BodyPublishers.noBody();

            if (parameters != null) {
                List<String> bodyList = new ArrayList<String>();
                boolean amp = false;
                for (Map.Entry<String,String> entry : parameters.entrySet()) {
                    if (amp) {
                        bodyList.add("&");
                    }
                    bodyList.add(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
                    bodyList.add("=");
                    bodyList.add(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
                    amp = true;
                }

                httppostb.setHeader("Content-type", "application/x-www-form-urlencoded");
                String body = String.join("", bodyList);
                bp = BodyPublishers.ofString(body);
            }

            if (data != null) {
                httppostb.setHeader("Content-type", contentType);
                log.log(INFO, "Request data: " + first1000(new String(data, StandardCharsets.UTF_8)));
                bp = BodyPublishers.ofByteArray(data);
            }

            httppostb.POST(bp);

            addHeadersToRequest(httppostb, headers);
            HttpRequest httppost = httppostb.build();

            log.log(INFO, "Executing request " + httppost.method() + " " + httppost.uri());

            HttpResponse<String> rsp = client.send(httppost, BodyHandlers.ofString());
            Response reponse = new Response(rsp.statusCode(), rsp.body(), rsp.headers().map());

            log.log(INFO, rsp.uri() + "->" + rsp.statusCode());
            if (proto.equals("https") && tlsLogging) {
                logTLS(rsp.sslSession());
            }

            return reponse;

        } catch (InterruptedException | IOException ioe) {
            log.log(ERROR, "IO error while connecting to " + url);
            throw new HttpRuntimeException("IO error while connecting to " + url, ioe);
        } catch (URISyntaxException use) {
            throw new HttpRuntimeException("bad url: " + url, use);
        }
    }

    public static final Response doPutRequest(final String proto, final String host, String port, final String url, Map<String,String> headers, String contentType, byte[] data) throws HttpRuntimeException {
        try {
            java.net.http.HttpClient client = java.net.http.HttpClient.newBuilder().followRedirects(Redirect.NEVER).build();

            // HttpClient does not separate URL and connection, so port etc. must match or we need another api.
            // final HttpHost target = new HttpHost(proto, host, portInt);

            HttpRequest.Builder httpputb = HttpRequest.newBuilder().uri(new URI(url));

            BodyPublisher bp = BodyPublishers.noBody();

            if (data != null) {
                httpputb.setHeader("Content-type", contentType);
                log.log(INFO, "Request data: " + first1000(new String(data, StandardCharsets.UTF_8)));
                bp = BodyPublishers.ofByteArray(data);
            }

            httpputb.PUT(bp);

            addHeadersToRequest(httpputb, headers);
            HttpRequest httpput = httpputb.build();

            log.log(INFO, "Executing request " + httpput.method() + " " + httpput.uri());

            HttpResponse<String> rsp = client.send(httpput, BodyHandlers.ofString());
            Response reponse = new Response(rsp.statusCode(), rsp.body(), rsp.headers().map());

            log.log(INFO, rsp.uri() + "->" + rsp.statusCode());
            if (proto.equals("https") && tlsLogging) {
                logTLS(rsp.sslSession());
            }

            return reponse;

        } catch (InterruptedException | IOException ioe) {
            log.log(ERROR, "IO error while connecting to " + url);
            throw new HttpRuntimeException("IO error while connecting to " + url, ioe);
        } catch (URISyntaxException use) {
            throw new HttpRuntimeException("bad url: " + url, use);
        }
    }

    public static final Response doPatchRequest(final String proto, final String host, String port, final String url, Map<String,String> headers, String contentType, byte[] data) throws HttpRuntimeException {
        try {
            java.net.http.HttpClient client = java.net.http.HttpClient.newBuilder().followRedirects(Redirect.NEVER).build();

            // HttpClient does not separate URL and connection, so port etc. must match or we need another api.
            // final HttpHost target = new HttpHost(proto, host, portInt);

            HttpRequest.Builder httppatchb = HttpRequest.newBuilder().uri(new URI(url));

            BodyPublisher bp = BodyPublishers.noBody();

            if (data != null) {
                httppatchb.setHeader("Content-type", contentType);
                log.log(INFO, "Request data: " + first1000(new String(data, StandardCharsets.UTF_8)));
                bp = BodyPublishers.ofByteArray(data);
            }

            httppatchb.method("PATCH", bp);

            addHeadersToRequest(httppatchb, headers);
            HttpRequest httppatch = httppatchb.build();

            log.log(INFO, "Executing request " + httppatch.method() + " " + httppatch.uri());

            HttpResponse<String> rsp = client.send(httppatch, BodyHandlers.ofString());
            Response reponse = new Response(rsp.statusCode(), rsp.body(), rsp.headers().map());

            log.log(INFO, rsp.uri() + "->" + rsp.statusCode());
            if (proto.equals("https") && tlsLogging) {
                logTLS(rsp.sslSession());
            }

            return reponse;

        } catch (InterruptedException | IOException ioe) {
            log.log(ERROR, "IO error while connecting to " + url);
            throw new HttpRuntimeException("IO error while connecting to " + url, ioe);
        } catch (URISyntaxException use) {
            throw new HttpRuntimeException("bad url: " + url, use);
        }
    }
}

