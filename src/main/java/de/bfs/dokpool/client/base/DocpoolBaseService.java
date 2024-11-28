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
import java.util.Optional;
import java.util.Vector;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;

import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

import de.bfs.dokpool.client.base.HttpClient.Headers;
import de.bfs.dokpool.client.content.DocumentPool;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The root class to access REST-API services for DOKPOOL.
 * XMLRPC has been renamed with suffix X.
 *
 */
public class DocpoolBaseService {
	private final Log log = LogFactory.getLog(DocpoolBaseService.class);
	private XmlRpcClient client = null;

	private String proto;
	private String host;
	private String port;
	/*package-private*/ String plonesite;
	private String username;
	private String password;
	private final String urlPrefix;
	private final int urlPrefixLength;

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
		try {
			URL urlObject = new URL(url);
			this.proto = urlObject.getProtocol();
			this.host = urlObject.getHost();
			this.port = urlObject.getPort() == -1 ? "" : Integer.toString(urlObject.getPort());
			this.plonesite = urlObject.getPath();
			this.plonesite = this.plonesite.startsWith("/") ? this.plonesite.substring(1) : this.plonesite;
			this.username = username;
			this.password = password;
		} catch (MalformedURLException mue){
			log.fatal("Incorrect URL provided!", mue);
		}
		this.urlPrefix = HttpClient.composeUrl(this.proto,this.host,this.port,"/"+ this.plonesite);
		this.urlPrefixLength = urlPrefix.length();

		//TODO: old XMLRPC-Code:
		XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
		// you can now do authenticated XML-RPC calls with the proxy
		try {
			URL serverurl = new URL(url);
			config.setServerURL(serverurl);
			config.setBasicUserName(username);
			config.setBasicPassword(password);
			client = new XmlRpcClient();
			// concatenate and base64 encode the username and password (suitable for use in
			// HTTP Basic Authentication)
			// final String auth =
			// javax.xml.bind.DatatypeConverter.printBase64Binary((username + ":" +
			// password).getBytes());
			// set the HTTP Header for Basic Authentication
			// client.setRequestProperty("Authorization", "Basic " + auth);
			client.setConfig(config);
			client.setTypeFactory(new DocpoolBaseTypeFactory(client));
		} catch (MalformedURLException e) {
			log.fatal("Incorrect URL provided!", e);
		}
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
		this.urlPrefix = HttpClient.composeUrl(proto,host,port,"/"+ plonesite);
		this.urlPrefixLength = urlPrefix.length();
		this.proto = proto;
		this.host = host;
		this.port = port != null? port : "";
		this.plonesite = plonesite.startsWith("/") ? plonesite.substring(1) : plonesite;
		this.username = username;
		this.password = password;
	}

	public String pathWithoutPrefix(String path){
		return path.substring(urlPrefixLength);
	}

	public String pathWithoutPrefix(JSON.Node node){
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
			try {
				HttpClient.Response rsp = HttpClient.doGetRequest(proto,host,port,urlPrefix+"/resolveuid/"+uid,defaultHeaders());
				if (rsp.status == 404 || rsp.headers.get("Location") == null) {
					log.error("UID " + uid + " cannot be resolved");
					return null;
				}
				log.info("@id from UID: " + rsp.headers.get("Location"));
				return rsp.headers.get("Location").substring(urlPrefixLength);

			} catch (Exception ex) {
				log.error(exeptionToString(ex));
				return null;
			}
		}
		return node.get("@id").toString().substring(urlPrefixLength);
	}

	public String getUsername() {
		return username;
	}

	/*package-private*/ static String exeptionToString(Exception ex) {
		Writer stBuffer = new StringWriter();
		PrintWriter stPrintWriter = new PrintWriter(stBuffer);
		ex.printStackTrace(stPrintWriter);
		return ex.toString() + ": " + ex.getLocalizedMessage() + "\n" + stBuffer.toString();
	}

	public JSON.Node addErrorInfo(JSON.Node baseNode, HttpClient.Response rsp) throws Exception {
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
	private Map<String,String> defaultHeaders(){
		Map<String,String> headers = new HashMap<String,String>();
		headers.put(HttpClient.Headers.ACCEPT,HttpClient.MimeTypes.JSON);
		HttpClient.addBasicAuthToHeaders(headers,username,password);
		return headers;
	}

	public JSON.Node nodeFromGetRequest(String endpoint, String queryString) throws Exception {
		queryString = (queryString == null || queryString.equals("")) ? "" : ("?"+queryString);
		String path = urlPrefix + endpoint;
		HttpClient.Response	rsp;
		rsp = HttpClient.doGetRequest(proto,host,port,path+queryString,defaultHeaders());
		log.info("response content length: " + rsp.content.length());
		JSON.Node node = new JSON.Node(rsp.content);
		if (node != null && node.get("batching") != null){
			long itemsTotal = node.get("items_total").toLong();
			if (queryString.equals("")){
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


	// TODO: we do not actually want these methods to be part of the public api
	// -> make them package-private and add references to them to the base object
	public JSON.Node nodeFromGetRequest(String endpoint) throws Exception {
		return nodeFromGetRequest(endpoint, null);
	}

	public HttpClient.Response patchRequestWithNode(String endpoint, JSON.Node patchNode) throws Exception {
		String patchUrl = urlPrefix + endpoint;
		byte[] patchData = patchNode.toJSON().getBytes();
		return HttpClient.doPatchRequest(proto,host,port,patchUrl,defaultHeaders(),HttpClient.MimeTypes.JSON,patchData);
	}

	public HttpClient.Response postRequestWithNode(String endpoint, JSON.Node patchNode) throws Exception {
		String patchUrl = urlPrefix + endpoint;
		byte[] patchData = patchNode.toJSON().getBytes();
		return HttpClient.doPostRequest(proto,host,port,patchUrl,defaultHeaders(),null,HttpClient.MimeTypes.JSON,patchData);
	}
	
	public HttpClient.Response deleteRequest(String endpoint) throws Exception {
		String path = urlPrefix + endpoint;
		HttpClient.Response	rsp;
		rsp = HttpClient.doDeleteRequest(proto,host,port,path,defaultHeaders());
		log.info("response content length: " + rsp.content.length());
		return rsp;
	}

	/**
	 * Get all ESDs available to the current user.
	 * 
	 * @return
	 */
	public List<DocumentPool> getDocumentPoolsX() {
		Map<String, Object> esds = DocpoolBaseService.queryObjects(this.client, "/", "DocumentPool");
		if (esds != null) {
			ArrayList<DocumentPool> res = new ArrayList<DocumentPool>();
			for (String path : esds.keySet()) {
				res.add(new DocumentPool(client, path, null));
			}
			return res;
		} else {
			return null;
		}
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
		} catch (Exception ex){
			log.error(exeptionToString(ex));
			return null;
		}
		List<DocumentPool> dpList = new ArrayList<>();
		for (JSON.Node child : node){
			dpList.add(new DocumentPool(this, "/"+child.toString(), (Map<String,Object>) null));
		}
		return dpList;
	}

	/**
	 * @return the ESD, which the user is a member of - or the first available ESD
	 *         for global users
	 */
	public DocumentPool getPrimaryDocumentPoolX() {
		Object[] res = (Object[]) DocpoolBaseService.execute(client, "get_primary_documentpool", new Vector());
		log.info(res.length);
		return new DocumentPool(client, (String) res[0], (Object[]) res[1]);
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
			log.error(exeptionToString(ex));
			return null;
		}
	}

	private Optional<DocumentPool> getDocumentPoolX(String name) {
		List<DocumentPool> documentPools = getDocumentPoolsX();
		return documentPools.stream().filter(pool -> pool.getId().equals(name)).findFirst();
	}

	private Optional<DocumentPool> getDocumentPool(String name) {
		List<DocumentPool> documentPools = getDocumentPools();
		return documentPools.stream().filter(pool -> pool.getId().equals(name)).findFirst();
	}

	/**
	 * Helper method for querying objects.
	 * @param client: the XMLRPC client
	 * @param path: path in Plone
	 * @param type: Plone type to search for
	 * @return
	 */
	@Deprecated public static Map queryObjects(XmlRpcClient client, String path, String type) {
		Vector<Object> params = new Vector<Object>();
		HashMap<String, Object> query = new HashMap<String, Object>();
		query.put("path", path);
		query.put("portal_type", type);
		params.add(query);
		Map res = (Map)execute(client, "query", params);
		return res;
	}

	/**
	 * Helper method for querying objects.
	 * @param client: the XMLRPC client
	 * @param path: path in Plone
	 * @param type: Plone type to search for
	 * @param filterparams: Plne params to search for
	 * @return
	 */
	@Deprecated public static Map queryObjects(XmlRpcClient client, String path, String type, HashMap<String, String> filterparams) {
		Vector<Object> params = new Vector<Object>();
		HashMap<String, Object> query = new HashMap<String, Object>();
		query.put("path", path);
		query.put("portal_type", type);
		for (Map.Entry<String, String> filterparam : filterparams.entrySet()) {
			query.put(filterparam.getKey(), filterparam.getValue());
		}
		params.add(query);
		Map res = (Map)execute(client, "query", params);
		return res;
	}

	/**
	 * Helper method to execute XMLRPC calls
	 * 
	 * @param client: the XMLRPC client
	 * @param command: the command (i.e. method of the WSAPI module)
	 * @param params: the parameters for the call
	 * @return
	 */
	@Deprecated public static Object execute(XmlRpcClient client, String command, Vector params) {
		Log log = LogFactory.getLog(DocpoolBaseService.class);
		try {
			return client.execute(command, params);
		} catch (XmlRpcException e) {
			log.error(e);
			return null;
		}
	}

}
