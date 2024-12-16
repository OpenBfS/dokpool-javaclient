package de.bfs.dokpool.client.content;

import de.bfs.dokpool.client.base.BaseObject;
import de.bfs.dokpool.client.base.DocpoolBaseService;

import java.util.Map;

/**
 * Wraps the DPEvent
 *
 */
public class Event extends BaseObject {
	public Event(DocpoolBaseService service, String path, Object[] data) {
		super(service, path, data);
	}

	public Event(DocpoolBaseService service, String path, Map<String,Object> data) {
		super(service, path, data);
	}
}
