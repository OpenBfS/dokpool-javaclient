package de.bfs.dokpool.client.content;

import org.apache.xmlrpc.client.XmlRpcClient;

import de.bfs.dokpool.client.base.BaseObject;

/**
 * Wraps the Plone File object.
 *
 */
public class File extends BaseObject {
	public File(XmlRpcClient client, String path, Object[] data) {
		super(client, path, data);
	}

}
