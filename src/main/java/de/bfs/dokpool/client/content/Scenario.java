package de.bfs.dokpool.client.content;

import java.util.Map;

import de.bfs.dokpool.client.base.BaseObject;
import de.bfs.dokpool.client.base.DocpoolBaseService;

/**
 * Wraps the ELANScenario
 *
 */
public class Scenario extends BaseObject {
	public Scenario(DocpoolBaseService service, String path, Object[] data) {
		super(service, path, data);
	}

	public Scenario(DocpoolBaseService service, String path, Map<String,Object> data) {
		super(service, path, data);
	}
}
