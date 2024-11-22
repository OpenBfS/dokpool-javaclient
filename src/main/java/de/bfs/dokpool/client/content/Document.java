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
			if (rspNode.get("type") != null){
				log.info(rspNode.get("message"));
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

	/**
	 * Uploads an image into the document
	 * @param id: short name for the image
	 * @param title
	 * @param description
	 * @param data: binary data of the image
	 * @param filename
	 * @return the Image object representing the image on the server
	 */
	public Image uploadImage(final String id, final String title, final String description, final byte[] data, final String filename) {
		try {
			JSON.Node createJS = new JSON.Node("{}")
				.set("@type","Image")
				.set("id", id)
				.set("title", title)
				.set("description", description)
				.set("image", new JSON.Node("{}")
					.set("encoding", "base64")
					.set("content-type", "image/png")
					.set("data", new String(Base64.getEncoder().encode(data)))
					.set("filename", filename)
				)
			;
			HttpClient.Response rsp = service.postRequestWithNode(pathAfterPlonesite, createJS);
			JSON.Node rspNode = new JSON.Node(rsp.content);
			if (rspNode.get("type") != null){
				log.info(rspNode.get("message"));
				return null;
			}
			String newpath = service.pathWithoutPrefix(rspNode);
			return new Image(service, newpath, rspNode.toMap());
		} catch (Exception ex){
			log.error(exeptionToString(ex));
			return null;
		}
	}

	public String autocreateSubdocumentsX() {
		Vector<Object> params = new Vector<Object>();
		params.add(fullpath());
		String msg = (String)executeX("autocreate_subdocuments", params);
		return msg;
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
	  * NOT IMPLEMENTED for REST-API.
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
	  * @deprecated
	  * @return null
	  */
	@Deprecated public String readPropertiesFromFile() {
		return null;
	}

	public String setPropertyX(final String name, final String value, final String type) {
		Vector<Object> params = new Vector<Object>();
		params.add(fullpath());
		params.add(name);
		params.add(value);
		params.add(type);
		String msg = (String)executeX("set_property", params);
		return msg;
	}

	/**
	 * Sets property name to the given value.
	 * This Method can no longer create a new property with the REST-API,
	 * wich was the only use of the argument type.
	 * @param name
	 * @param value
	 * @param type ignored
	 * @return
	 */
	public String setProperty(final String name, final String value, final String type) {
		Map<String,Object> property = new HashMap<String,Object>();
		property.put(name,value);
		update(property);
		return null;
	}

	public String deleteProperty(final String name) {
		Vector<Object> params = new Vector<Object>();
		params.add(fullpath());
		params.add(name);
		String msg = (String)executeX("delete_property", params);
		return msg;
	}

	public String getProperty(final String name) {
		Vector<Object> params = new Vector<Object>();
		params.add(fullpath());
		params.add(name);
		String msg = (String)executeX("get_property", params);
		return msg;
	}

	public Map<String,String> getProperties() {
        Vector<Object> params = new Vector<Object>();
        params.add(fullpath());
        Map<String,String> msg = (Map<String,String>)executeX("get_properties", params);
        return msg;

    }

}
