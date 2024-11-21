package de.bfs.dokpool.client.content;

import java.util.Map;

import org.apache.xmlrpc.client.XmlRpcClient;

import de.bfs.dokpool.client.base.BaseObject;
import de.bfs.dokpool.client.base.DocpoolBaseService;

/**
 * Wraps the ELANDocType 
 *
 */
public class DocType extends BaseObject {
	public DocType(XmlRpcClient client, String path, Object[] data) {
		super(client, path, data);
	}

	public DocType(DocpoolBaseService service, String path, Object[] data) {
		super(service, path, data);
	}

	public DocType(DocpoolBaseService service, String path, Map<String,Object> data) {
		super(service, path, data);
	}
}
