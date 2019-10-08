package de.bfs.dokpool.client.content;

import de.bfs.dokpool.client.base.BaseObject;
import org.apache.xmlrpc.client.XmlRpcClient;

/**
 * Wraps the DPEvent
 *
 */
public class Event extends BaseObject {
	public Event(XmlRpcClient client, String path, Object[] data) {
		super(client, path, data);
	}
}
