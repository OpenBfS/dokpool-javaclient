package de.bfs.dokpool.client.base;


import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.bfs.dokpool.client.base.HttpClient.Headers;
import de.bfs.dokpool.client.content.DocumentPool;


/**
 * The root class to access REST-API services for DOKPOOL.
 * XMLRPC has been renamed with suffix X.
 *
 */
public class DocpoolBaseService {
	private final DocpoolBaseService.Log log = new DocpoolBaseService.Log(DocpoolBaseService.class);

	private String proto;
	private String host;
	private String port;
	/*package-private*/ String plonesite;
	private String username;
	private String password;
	private final String urlPrefix;
	private final int urlPrefixLength;

	/*package-private*/ PrivateDocpoolBaseService privateService;

	/**
	 * Get a service object.
	 * 
	 * @param url:
	 *            the address of the ELAN instance root
	 * @param username
	 * @param password
	 */
	public DocpoolBaseService(String url, String username, String password) {
		//new REST-Code:
		this.privateService = new PrivateDocpoolBaseService(this);
		try {
			URL urlObject = new URL(url);
			this.proto = urlObject.getProtocol();
			this.host = urlObject.getHost();
			this.port = urlObject.getPort() == -1 ? "" : Integer.toString(urlObject.getPort());
			this.plonesite = urlObject.getPath();
			this.plonesite = this.plonesite.startsWith("/") ? this.plonesite.substring(1) : this.plonesite;
			this.username = username;
			this.password = password;
		} catch (MalformedURLException mue) {
			log.fatal("Incorrect URL provided!", mue);
		}
		this.urlPrefix = HttpClient.composeUrl(this.proto,this.host,this.port,"/"+ this.plonesite);
		this.urlPrefixLength = urlPrefix.length();
	}

	/**
	 * Get a service object.
	 * 
	 * @param proto http/https
	 * @param host name or ip of the host
	 * @param port can be null or "" if standard for http or https, respectively
	 * @param plonesite usually "dokpool"
	 * @param username
	 * @param password
	 */
	public DocpoolBaseService(String proto, String host, String port, String plonesite, String username, String password) {
		this.privateService = new PrivateDocpoolBaseService(this);
		this.urlPrefix = HttpClient.composeUrl(proto,host,port,"/"+ plonesite);
		this.urlPrefixLength = urlPrefix.length();
		this.proto = proto;
		this.host = host;
		this.port = port != null? port : "";
		this.plonesite = plonesite.startsWith("/") ? plonesite.substring(1) : plonesite;
		this.username = username;
		this.password = password;
	}

	public String pathWithoutPrefix(String path) {
		return path.substring(urlPrefixLength);
	}

	private String uidToPathAfterPlonesite(String uid) {
		try {
			HttpClient.Response rsp = HttpClient.doGetRequest(proto,host,port,urlPrefix+"/resolveuid/"+uid,defaultHeaders());
			if (rsp.status == 404 || rsp.headers.get("Location") == null) {
				return null;
			}
			return rsp.headers.get("Location").substring(urlPrefixLength);
		} catch (Exception ex) {
			log.error(exceptionToString(ex));
			return null;
		}
	}

	public String pathWithoutPrefix(JSON.Node node) {
		String atid = node.get("@id").toString();
		//If the `@id` is a dview, we need to fetch the actual path differently.
		//In this case, the path is always realtive like: "esd/..." for ELAN,
		//so it will not start with the urlPrefix
		if (!atid.startsWith(urlPrefix)) {
			String uid = node.get("UID") != null ? node.get("UID").toString() : null;
			if (uid == null) { //The node has no attribute `UID`, so we will get it from the dview String
				int uidStart = atid.indexOf("@@dview?d=")+10;
				int uidEnd = atid.indexOf("&", uidStart);
				uid = atid.substring(uidStart, uidEnd);
				log.info("uid from dview: "+ uid);
			}
			String path = uidToPathAfterPlonesite(uid);
			if (path == null) {
				log.error("UID " + uid + " cannot be resolved");
			} else {
				log.info("path from UID: " + path);
			}
			return path;
		}
		return node.get("@id").toString().substring(urlPrefixLength);
	}

	public String getUsername() {
		return username;
	}

	/*package-private*/ static String exceptionToString(Exception ex) {
		Writer stBuffer = new StringWriter();
		PrintWriter stPrintWriter = new PrintWriter(stBuffer);
		ex.printStackTrace(stPrintWriter);
		return ex.toString() + ": " + ex.getLocalizedMessage() + "\n" + stBuffer.toString();
	}

	private JSON.Node addErrorInfo(JSON.Node baseNode, HttpClient.Response rsp) throws Exception {
		if (baseNode == null || baseNode.type().equals("null")) {
			baseNode = new JSON.Node("null");
			baseNode.errorInfo = "REST response error: No content or null content";
			return baseNode;
		}
		if (baseNode.get("error") != null) {
			baseNode.errorInfo = baseNode.get("error").get("message");
			baseNode.errorInfo = "REST response error: " + (baseNode.errorInfo != null ? baseNode.errorInfo : "error");
			return baseNode;
		}
		final List<Integer> errorCodes = Arrays.asList(400, 401, 403, 404, 405, 409, 500);
		if (errorCodes.contains(rsp.status)) {
			String message = baseNode.get("message") != null ? "; " + baseNode.get("message").toString() : "";
			baseNode.errorInfo = "REST response error: HTTP " + rsp.status + message;
			return baseNode;
		}
		if (baseNode.get("type") != null &&  baseNode.get("message") != null) {
			baseNode.errorInfo = "REST response error: " + baseNode.get("message");
			return baseNode;
		}
		return baseNode;
	}

	/**
	 * 
	 * @return A Map with authentication and accept (JSON) headers.
	 */
	private Map<String,String> defaultHeaders() {
		Map<String,String> headers = new HashMap<String,String>();
		headers.put(HttpClient.Headers.ACCEPT,HttpClient.MimeTypes.JSON);
		HttpClient.addBasicAuthToHeaders(headers,username,password);
		return headers;
	}

	private JSON.Node nodeFromGetRequest(String endpoint, String queryString) throws Exception {
		queryString = (queryString == null || queryString.equals("")) ? "" : ("?"+queryString);
		String path = urlPrefix + endpoint;
		HttpClient.Response	rsp;
		rsp = HttpClient.doGetRequest(proto,host,port,path+queryString,defaultHeaders());
		log.info("response content length: " + rsp.content.length());
		JSON.Node node = new JSON.Node(rsp.content);
		if (node != null && node.get("batching") != null) {
			long itemsTotal = node.get("items_total").toLong();
			if (queryString.equals("")) {
				queryString = "?b_size=" + itemsTotal;
			} else {
				queryString += "&b_size=" + itemsTotal;
			}
			rsp = HttpClient.doGetRequest(proto,host,port,path+queryString,defaultHeaders());
			log.info("response content length: " + rsp.content.length());
			node = new JSON.Node(rsp.content);
		}
		return addErrorInfo(node,rsp);
	}

	private JSON.Node nodeFromGetRequest(String endpoint) throws Exception {
		return nodeFromGetRequest(endpoint, null);
	}

	private JSON.Node patchRequestWithNode(String endpoint, JSON.Node patchNode) throws Exception {
		String patchUrl = urlPrefix + endpoint;
		byte[] patchData = patchNode.toJSON().getBytes();
		HttpClient.Response rsp = HttpClient.doPatchRequest(proto,host,port,patchUrl,defaultHeaders(),HttpClient.MimeTypes.JSON,patchData);
		return addErrorInfo(new JSON.Node(rsp.content), rsp);
	}

	private JSON.Node postRequestWithNode(String endpoint, JSON.Node patchNode) throws Exception {
		String patchUrl = urlPrefix + endpoint;
		byte[] patchData = patchNode.toJSON().getBytes();
		HttpClient.Response rsp = HttpClient.doPostRequest(proto,host,port,patchUrl,defaultHeaders(),null,HttpClient.MimeTypes.JSON,patchData);
		return addErrorInfo(new JSON.Node(rsp.content), rsp);
	}
	
	private JSON.Node deleteRequest(String endpoint) throws Exception {
		String path = urlPrefix + endpoint;
		HttpClient.Response rsp = HttpClient.doDeleteRequest(proto,host,port,path,defaultHeaders());
		return addErrorInfo(new JSON.Node(rsp.content), rsp);
	}

	/**
	 * Get all ESDs available to the current user.
	 * Calls the endpoint /@get_documentpools.
	 * 
	 * @return a list of Dokpools
	 */
	public List<DocumentPool> getDocumentPools() {
		return getDocumentPools("");
	}

	/**
	 * Get all ESDs available to a user.
	 * Calls the endpoint /@get_documentpools.
	 * 
	 * @param user The user for whom we want to get the Dokpools.
	 * @return a list of Dokpools
	 */
	public List<DocumentPool> getDocumentPools(String user) {
		user = (user == null || user == "") ? "" : ("/"+user);
		String ep = "/@get_documentpools" + user;
		JSON.Node node;
		try {
			node = nodeFromGetRequest(ep);
			if (node.errorInfo != null) {
				log.info(node.errorInfo.toString());
				return null;
			}
		} catch (Exception ex) {
			log.error(exceptionToString(ex));
			return null;
		}
		List<DocumentPool> dpList = new ArrayList<>();
		for (JSON.Node child : node) {
			dpList.add(new DocumentPool(this, "/"+child.toString(), (Map<String,Object>) null));
		}
		return dpList;
	}

    /**
	 * 
	 * Calls the endpoint /@get_primary_documentpool.
	 * @return the ESD, which the user is a member of - or the first available ESD
	 *         for global users.
	 * 
	 */
	public DocumentPool getPrimaryDocumentPool() {
		return getPrimaryDocumentPool("");
	}

	/**
	 * Get the primary Dokpool for user `user`.
	 * Calls the endpoint /@get_primary_documentpool.
	 * @param user The user for which we want to get the dokpool,
	 *             if empty or null, this will return the primary Dokpool for the login user.
	 * @return the ESD, which the user is a member of - or the first available ESD
	 *         for global users.
	 * 
	 */
	public DocumentPool getPrimaryDocumentPool(String user) {
		user = (user == null || user == "") ? "" : ("/"+user);
		String ep = "/@get_primary_documentpool" + user;
		JSON.Node rspNode = null;
		try {
			rspNode = nodeFromGetRequest(ep);
			if (rspNode.errorInfo != null) {
				log.info(rspNode.errorInfo.toString());
				return null;
			}
			return new DocumentPool(this, pathWithoutPrefix(rspNode), rspNode.toMap());
		} catch (Exception ex) {
			log.error(exceptionToString(ex));
			return null;
		}
	}

	/**
	 * This class only exists as a visibility hack across (sub)packages.
	 */
	public class PrivateDocpoolBaseService {
		DocpoolBaseService service;
		PrivateDocpoolBaseService(DocpoolBaseService service) {
			this.service = service;
		}

		public String uidToPathAfterPlonesite(String uid) {
			return service.uidToPathAfterPlonesite(uid);
		};
		public JSON.Node nodeFromGetRequest(String endpoint, String queryString) throws Exception {
			return service.nodeFromGetRequest(endpoint,queryString);
		}
		public JSON.Node nodeFromGetRequest(String endpoint) throws Exception {
			return service.nodeFromGetRequest(endpoint);
		}
		public JSON.Node patchRequestWithNode(String endpoint, JSON.Node patchNode) throws Exception {
			return service.patchRequestWithNode(endpoint, patchNode);
		}
		public JSON.Node postRequestWithNode(String endpoint, JSON.Node patchNode) throws Exception {
			return service.postRequestWithNode(endpoint, patchNode);
		}
		public JSON.Node deleteRequest(String endpoint) throws Exception {
			return service.deleteRequest(endpoint);
		}
	}

	public static class Log {
		
		private final String namePrefix;
		private final java.lang.System.Logger logger;

		public Log(Class<?> clazz) {
			namePrefix = clazz.getName().substring(clazz.getName().lastIndexOf(".")+1) + ": ";
			logger = System.getLogger(clazz.getName());
		}

		public void fatal(String msg, Throwable ex) {
			logger.log(java.lang.System.Logger.Level.ERROR, namePrefix + msg, ex);
		}

		public void error(String msg, Throwable ex)  {
			logger.log(java.lang.System.Logger.Level.ERROR, namePrefix + msg, ex);
		}

		public void error(String msg) {
			logger.log(java.lang.System.Logger.Level.ERROR, namePrefix + msg);
		}

		public void info(String msg, Throwable ex) {
			logger.log(java.lang.System.Logger.Level.INFO, namePrefix + msg, ex);
		}

		public void info(String msg) {
			logger.log(java.lang.System.Logger.Level.INFO, namePrefix + msg);
		}

		public void info(Object o) {
			if (o == null) {
				o = "null";
			}
			logger.log(java.lang.System.Logger.Level.INFO, o);
		}

		public void warn(String msg) {
			logger.log(java.lang.System.Logger.Level.WARNING, namePrefix + msg);
		}
	}

}
