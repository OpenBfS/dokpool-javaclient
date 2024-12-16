package de.bfs.dokpool.client.content;

import java.util.Map;

import de.bfs.dokpool.client.base.BaseObject;
import de.bfs.dokpool.client.base.DocpoolBaseService;

/**
 * Wraps the Plone File object.
 *
 */
public class File extends BaseObject {
	public File(DocpoolBaseService service, String path, Object[] alldata) {
		super(service, path, alldata);
	}

	public File(DocpoolBaseService service, String path, Map<String,Object> data) {
		super(service, path, data);
	}

}
