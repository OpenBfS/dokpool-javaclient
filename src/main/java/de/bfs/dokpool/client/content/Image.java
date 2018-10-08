package de.bfs.dokpool.client.content;

import org.apache.xmlrpc.client.XmlRpcClient;

import de.bfs.dokpool.client.base.BaseObject;

/**
 * Wraps the Plone Image type
 *
 */
public class Image extends BaseObject {
	public Image(XmlRpcClient client, String path, Object[] data) {
		super(client, path, data);
	}
}
