package de.bfs.dokpool.client.content;

import java.util.Map;

import de.bfs.dokpool.client.base.BaseObject;
import de.bfs.dokpool.client.base.DocpoolBaseService;

/**
 * Wraps the Plone Image type
 *
 */
public class Image extends BaseObject {
	public Image(DocpoolBaseService service, String path, Object[] alldata) {
		super(service, path, alldata);
	}

	public Image(DocpoolBaseService service, String path, Map<String,Object> data) {
		super(service, path, data);
	}
}
