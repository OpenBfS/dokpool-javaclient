package de.bfs.dokpool.client.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;

public class Utils {
    private static Log log = LogFactory.getLog(Utils.class);

	/**
	 * Helper method for querying objects.
	 * @param client: the XMLRPC client
	 * @param path: path in Plone
	 * @param type: Plone type to search for
	 * @return
	 */
	public static Map queryObjects(XmlRpcClient client, String path, String type) {
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
	public static Map queryObjects(XmlRpcClient client, String path, String type, HashMap<String, String> filterparams) {
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
	public static Object execute(XmlRpcClient client, String command, Vector params) {
		try {
			return client.execute(command, params);
		} catch (XmlRpcException e) {
			log.error(e);
			return null;
		}
	}

}

