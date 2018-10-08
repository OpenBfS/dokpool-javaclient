package de.bfs.dokpool.client.content;

import org.apache.xmlrpc.client.XmlRpcClient;

import de.bfs.dokpool.client.base.BaseObject;

/**
 * Wraps the ELANScenario
 *
 */
public class Scenario extends BaseObject {
	public Scenario(XmlRpcClient client, String path, Object[] data) {
		super(client, path, data);
	}
}
