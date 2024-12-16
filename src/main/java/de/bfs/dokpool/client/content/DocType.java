package de.bfs.dokpool.client.content;

import java.util.Map;

import de.bfs.dokpool.client.base.BaseObject;
import de.bfs.dokpool.client.base.DocpoolBaseService;

/**
 * Wraps the ELANDocType 
 *
 */
public class DocType extends BaseObject {
	public DocType(DocpoolBaseService service, String path, Object[] data) {
		super(service, path, data);
	}

	public DocType(DocpoolBaseService service, String path, Map<String,Object> data) {
		super(service, path, data);
	}
}
