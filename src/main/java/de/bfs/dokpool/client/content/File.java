package de.bfs.dokpool.client.content;

import java.util.Base64;
import java.util.Map;

import de.bfs.dokpool.client.base.BaseObject;
import de.bfs.dokpool.client.base.DocpoolBaseService;
import de.bfs.dokpool.client.base.JSON;

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

	/**
	 * Replaces and existing file with new content or changes its metadata.
	 * Arguments set to null will not be changed.
	 * @param title
	 * @param description
	 * @param data binary data of the file
	 * @param filename (used for display and download)
	 * @param mimeType
	 * @return true if deletion succeeds, false otherwise
	 */
	public boolean replace(String title, String description, byte[] data, String filename, String mimeType) {
		try {
			//TODO: we might keep some data, but this is the save, easy and costly way:
			clearData();
			JSON.Node createJS = new JSON.Node("{}")
				.setNonNull("title", title)
				.setNonNull("description", description)
				.set("file", new JSON.Node("{}")
					.set("encoding", "base64")
					.setNonNull("content-type", mimeType)
					.setNonNull("data", new String(Base64.getEncoder().encode(data)))
					.setNonNull("filename", filename)
				)
			;
			JSON.Node rspNode = privateService.patchRequestWithNode(pathAfterPlonesite, createJS);
			if (rspNode.errorInfo != null) {
				log.info(rspNode.errorInfo.toString());
				return false;
			}
			return true;
		} catch (Exception ex) {
			log.error(exceptionToString(ex));
			return false;
		}
	}

}
