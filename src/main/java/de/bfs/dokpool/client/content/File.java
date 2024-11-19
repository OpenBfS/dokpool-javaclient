package de.bfs.dokpool.client.content;

import java.util.Map;

import org.apache.xmlrpc.client.XmlRpcClient;

import de.bfs.dokpool.client.base.BaseObject;
import de.bfs.dokpool.client.base.DocpoolBaseService;

/**
 * Wraps the Plone File object.
 *
 */
public class File extends BaseObject {
	public File(XmlRpcClient client, String path, Object[] data) {
		super(client, path, data);
	}

	public File(DocpoolBaseService service, String path, Object[] alldata) {
		super(service, path, alldata);
	}

	public File(DocpoolBaseService service, String path, Map<String,Object> data) {
		super(service, path, data);
	}

}
