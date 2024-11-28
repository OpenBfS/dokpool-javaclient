package de.bfs.dokpool.client.content;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.apache.xmlrpc.client.XmlRpcClient;

import de.bfs.dokpool.client.base.DocpoolBaseService;
import de.bfs.dokpool.client.base.HttpClient;
import de.bfs.dokpool.client.base.JSON;

/**
 * Wraps the ELANDocument / DPDocument type
 *
 */
public class Document extends Folder {
	public Document(XmlRpcClient client, String path, Object[] data) {
		super(client, path, data);
	}

	public Document(DocpoolBaseService service, String path, Object[] alldata) {
		super(service, path, alldata);
	}

	public Document(DocpoolBaseService service, String path, Map<String,Object> data) {
		super(service, path, data);
	}
	
	/**
	 * Uploads a file into the document
	 * @param id: short name for the file
	 * @param title
	 * @param description
	 * @param data: binary data of the file
	 * @param filename
	 * @return
	 */
	public File uploadFileX(String id, String title, String description, byte[] data, String filename) {
		Vector<Object> params = new Vector<Object>();
		System.out.println("Dokument: "+id+title+description+filename);
		params.add(fullpath());
		params.add(id);
		params.add(title);
		params.add(description);
		params.add(data);
		params.add(filename);
		String newpath = (String)executeX("upload_file", params);
		return new File(client, newpath, null);
	}

	/**
	 * Uploads a file into the document
	 * @param id: short name for the file
	 * @param title
	 * @param description
	 * @param data: binary data of the file
	 * @param filename
	 * @return the File object representing the file on the server
	 */
	public File uploadFile(String id, String title, String description, byte[] data, String filename) {
		try {
			JSON.Node createJS = new JSON.Node("{}")
				.set("@type","File")
				.set("id", id)
				.set("title", title)
				.set("description", description)
				.set("file", new JSON.Node("{}")
					.set("encoding", "base64")
					.set("data", new String(Base64.getEncoder().encode(data)))
					.set("filename", filename)
				)
			;
			HttpClient.Response rsp = service.postRequestWithNode(pathAfterPlonesite, createJS);
			JSON.Node rspNode = new JSON.Node(rsp.content);
			if (rspNode.errorInfo != null) {
				log.info(rspNode.errorInfo.toString());
				return null;
			}
			String newpath = service.pathWithoutPrefix(rspNode);
			return new File(service, newpath, (Object[]) null);
		} catch (Exception ex){
			log.error(exeptionToString(ex));
			return null;
		}
	}

	/**
	 * Uploads an image into the document
	 * @param id: short name for the image
	 * @param title
	 * @param description
	 * @param data: binary data of the image
	 * @param filename
	 * @return
	 */	public Image uploadImageX(final String id, final String title, final String description, final byte[] data, final String filename) {
		Vector<Object> params = new Vector<Object>();
		System.out.println("Dokument: "+id+title+description+filename);
		params.add(fullpath());
		params.add(id);
		params.add(title);
		params.add(description);
		params.add(data);
		params.add(filename);
		String newpath = (String)executeX("upload_image", params);
		return new Image(client, newpath, null);
	}

	protected String mimeTypeFromFilename(String filename) {
		String ext = filename.substring(filename.lastIndexOf(".")+1).toLowerCase();
		switch(ext) {
			case "bmp":
				return "image/bmp";
			case "eps":
				return "image/x-eps";
			case "gif":
				return "image/gif";
			case "jpeg":
			case "jpg":
				return "image/jpeg";
			case "pdf":
				return "application/pdf";
			case "png":
				return "image/png";
			case "svg":
				return "image/svg+xml";
			case "svgz":
				return "image/svg+xml-compressed";
			case "tif":
			case "tiff":
				return "image/tiff";
		}
		return "text/plain";
	}

	/**
	 * Uploads an image into the document
	 * @param id: short name for the image
	 * @param title
	 * @param description
	 * @param data: binary data of the image
	 * @param filename
	 * @param MIME-Type of the Image (e.g. image/png)
	 * @return the Image object representing the image on the server
	 */
	public Image uploadImage(final String id, final String title, final String description, final byte[] data, final String filename, String mimeType) {
		try {
			JSON.Node createJS = new JSON.Node("{}")
				.set("@type","Image")
				.set("id", id)
				.set("title", title)
				.set("description", description)
				.set("image", new JSON.Node("{}")
					.set("encoding", "base64")
					.set("content-type", mimeType)
					.set("data", new String(Base64.getEncoder().encode(data)))
					.set("filename", filename)
				)
			;
			HttpClient.Response rsp = service.postRequestWithNode(pathAfterPlonesite, createJS);
			JSON.Node rspNode = new JSON.Node(rsp.content);
			if (rspNode.errorInfo != null) {
				log.info(rspNode.errorInfo.toString());
				return null;
			}
			String newpath = service.pathWithoutPrefix(rspNode);
			return new Image(service, newpath, rspNode.toMap());
		} catch (Exception ex){
			log.error(exeptionToString(ex));
			return null;
		}
	}

	/**
	 * Uploads an image into the document
	 * @param id: short name for the image
	 * @param title
	 * @param description
	 * @param data: binary data of the image
	 * @param filename The image type must be deducible from the file name extension (.jpeg/.jpg/.png)
	 * @return the Image object representing the image on the server
	 */
	public Image uploadImage(final String id, final String title, final String description, final byte[] data, final String filename) {
		return uploadImage(id, title, description, data, filename, mimeTypeFromFilename(filename));
	}

	/*
	 * This is the current python implememtation:
		def autocreateSubdocuments(self):
	   """
	   TODO: specifically for XMLRPC usage
	   """
	   # * Von den allowed Types alle autocreatable Types durchgehen und ihre Muster "ausprobieren"
	   # * Wenn Files oder Images gefunden zu einem Muster: entsprechendes DPDocument erzeugen und Files/Images verschieben
	   return "ok"
	 */
	/**
	 * NOT IMPLEMENTED for REST-API (and was not for XMLRPC either).
	 * @deprecated
	 * @return null
	 */
	@Deprecated public String autocreateSubdocuments() {
		return null;
	}
	 
	public String readPropertiesFromFileX() {
		Vector<Object> params = new Vector<Object>();
		params.add(fullpath());
		String msg = (String)executeX("read_properties_from_file", params);
		return msg;
	}

	/**
	  * NOT IMPLEMENTED for REST-API
	  * @deprecated If you need this functionality, fetch the file "properties.txt" within the document and set each property using setAttribute(name, value) yourself.
	  * @return null
	  */
	@Deprecated public String readPropertiesFromFile() {
		return null;
	}

	/**
	 * Sets property name to the given value.
	 * This Method can no longer create a new property with the REST-API,
	 * wich was the only use of the argument `type`.
	 * @deprecated Use BaseObject.setAttribute(name, value) instead
	 * @param name
	 * @param value
	 * @param type ignored
	 * @return
	 */
	@Deprecated public String setProperty(final String name, final String value, final String type) {
		return setAttribute(name, value) ? "ok" : "error";
	}

	/**
	 * Unsets property `name` (does not delete it).
	 * This Method can no longer delete a property with the REST-API
	 * @deprecated Use BaseObject.setAttribute(name, null) instead
	 * @param name
	 * @return
	 */
	@Deprecated public String deleteProperty(final String name) {
		return setAttribute(name, null) ? "ok" : "error";
	}

	/**
	 * Returns property `name`.
	 * @deprecated Use BaseObject.getStringAttribute(name) instead
	 * @param name
	 * @return
	 */
	@Deprecated public String getProperty(final String name) {
		return getStringAttribute(name);
	}

	/**
	 * Returns all propties as a map.
	 * @deprecated Use BaseObject.getAllAttributes() instead
	 * @return map of all attributes (values are casted to (String))
	 */
	@Deprecated public Map<String,String> getProperties() {
        Map<String,Object> soMap = getAllAttributes();
		Map<String,String> sMap = new HashMap<String,String>();
		for (Map.Entry<String,Object> entry: soMap.entrySet()) {
			sMap.put(entry.getKey(), entry.getValue() != null ? entry.getValue().toString(): null);
		}
		return sMap;
    }

	

}
