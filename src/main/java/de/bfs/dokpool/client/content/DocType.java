package de.bfs.dokpool.client.content;

import org.apache.xmlrpc.client.XmlRpcClient;

import de.bfs.dokpool.client.base.BaseObject;

/**
 * Wraps the ELANDocType 
 *
 */
public class DocType extends BaseObject {
	public DocType(XmlRpcClient client, String path, Object[] data) {
		super(client, path, data);
	}
}
