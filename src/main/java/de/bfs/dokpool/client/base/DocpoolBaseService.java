package de.bfs.dokpool.client.base;


import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Vector;

import org.apache.xmlrpc.client.XmlRpcClient;

import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

import de.bfs.dokpool.client.content.DocumentPool;
import de.bfs.dokpool.client.utils.Utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The root class to access XMLRPC services for DOKPOOL.
 *
 */
public class DocpoolBaseService {
	private final Log log = LogFactory.getLog(DocpoolBaseService.class);
	private XmlRpcClient client = null;

	/**
	 * Get a service object.
	 * 
	 * @param url:
	 *            the address of the ELAN instance root
	 * @param username
	 * @param password
	 */
	public DocpoolBaseService(String url, String username, String password) {
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
	 * Get all ESDs available to the current user.
	 * 
	 * @return
	 */
	public List<DocumentPool> getDocumentPools() {
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
	 * @return the ESD, which the user is a member of - or the first available ESD
	 *         for global users
	 */
	public DocumentPool getPrimaryDocumentPool() {
		Object[] res = (Object[]) Utils.execute(client, "get_primary_documentpool", new Vector());
		log.info(res.length);
		return new DocumentPool(client, (String) res[0], (Object[]) res[1]);
	}

	private Optional<DocumentPool> getDocumentPool(String name) {
		List<DocumentPool> documentPools = getDocumentPools();
		return documentPools.stream().filter(pool -> pool.getId().equals(name)).findFirst();
	}

}
