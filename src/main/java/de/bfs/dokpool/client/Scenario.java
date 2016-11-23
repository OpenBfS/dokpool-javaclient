package de.bfs.dokpool.client;

import org.apache.xmlrpc.client.XmlRpcClient;

/**
 * Wraps the ELANScenario
 *
 */
public class Scenario extends BaseObject {
	public Scenario(XmlRpcClient client, String path, Object[] data) {
		super(client, path, data);
	}
}
