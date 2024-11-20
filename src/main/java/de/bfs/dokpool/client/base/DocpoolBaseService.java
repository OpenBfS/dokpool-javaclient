package de.bfs.dokpool.client.base;


import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Vector;

import org.apache.xmlrpc.client.XmlRpcClient;

import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

import de.bfs.dokpool.client.base.HttpClient.Headers;
import de.bfs.dokpool.client.content.DocumentPool;
import de.bfs.dokpool.client.utils.Utils;

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
	public final String urlPrefix;
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

		//old XMLRPC-Code:
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
		return node.get("@id").toString().substring(urlPrefixLength);
	}

	public String getUsername() {
		return username;
	}

	/**
	 * 
	 * @return A Map with authentication and accept (JSON) headers.
	 */
	public Map<String,String> defaultHeaders(){
		Map<String,String> headers = new HashMap<String,String>();
		headers.put(HttpClient.Headers.ACCEPT,HttpClient.MimeTypes.JSON);
		HttpClient.addBasicAuthToHeaders(headers,username,password);
		return headers;
	}

	public Map<String,Object> mapFromGetRequest(String endpoint) throws Exception {
		String path = urlPrefix + endpoint;
		HttpClient.Response	rsp;
		rsp = HttpClient.doGetRequest(proto,host,port,path,defaultHeaders());
		log.info("response content length: " + rsp.content.length());
		JSON.Node resJs = new JSON.Node(rsp.content);
		return resJs.toMap();
	}

	public JSON.Node nodeFromGetRequest(String endpoint) throws Exception {
		String path = urlPrefix + endpoint;
		HttpClient.Response	rsp;
		rsp = HttpClient.doGetRequest(proto,host,port,path,defaultHeaders());
		log.info("response content length: " + rsp.content.length());
		return new JSON.Node(rsp.content);
	}

	public HttpClient.Response patchRequestWithMap(String endpoint, Map<String,Object> patchMap) throws Exception {
		String patchUrl = urlPrefix + endpoint;
		JSON.Node patchJS = new JSON.Node(patchMap);
		byte[] patchData = patchJS.toJSON().getBytes();
		return HttpClient.doPatchRequest(proto,host,port,patchUrl,defaultHeaders(),HttpClient.MimeTypes.JSON,patchData);
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

	/**
	 * Get all ESDs available to the current user.
	 * 
	 * @return
	 */
	public List<DocumentPool> getDocumentPoolsX() {
		Map<String, Object> esds = Utils.queryObjects(this.client, "/", "DocumentPool");
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
		try {//TODO: current API does not throw exceptions, change?
			node = nodeFromGetRequest(ep);
		} catch (Exception ex){
			log.error(ex.toString()+": "+ ex.getLocalizedMessage());
			return null;
		}
		List<DocumentPool> dpList = new ArrayList<>();
		for (JSON.Node child : node){
			dpList.add(new DocumentPool(this, "/"+child.toString(), (Map<String,Object>) null));
		}
		return dpList;//new DocumentPool(this, pathWithoutPrefix((String) map.get("@id")), map);
	}

	/**
	 * @return the ESD, which the user is a member of - or the first available ESD
	 *         for global users
	 */
	public DocumentPool getPrimaryDocumentPoolX() {
		Object[] res = (Object[]) Utils.execute(client, "get_primary_documentpool", new Vector());
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
		Map<String,Object> map;
		try {//TODO: current API does not throw exceptions, change?
			map = mapFromGetRequest(ep);
		} catch (Exception ex){
			log.error(ex.toString()+": "+ ex.getLocalizedMessage());
			return null;
		}
		return new DocumentPool(this, pathWithoutPrefix((String) map.get("@id")), map);
	}

	private Optional<DocumentPool> getDocumentPoolX(String name) {
		List<DocumentPool> documentPools = getDocumentPoolsX();
		return documentPools.stream().filter(pool -> pool.getId().equals(name)).findFirst();
	}

	private Optional<DocumentPool> getDocumentPool(String name) {
		List<DocumentPool> documentPools = getDocumentPools();
		return documentPools.stream().filter(pool -> pool.getId().equals(name)).findFirst();
	}

}
