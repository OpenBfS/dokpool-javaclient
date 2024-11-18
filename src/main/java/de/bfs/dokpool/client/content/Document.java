package de.bfs.dokpool.client.content;

import java.util.Map;
import java.util.Vector;

import org.apache.xmlrpc.client.XmlRpcClient;

/**
 * Wraps the ELANDocument type
 *
 */
public class Document extends Folder {
	public Document(XmlRpcClient client, String path, Object[] data) {
		super(client, path, data);
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
	public File uploadFile(String id, String title, String description, byte[] data, String filename) {
		Vector<Object> params = new Vector<Object>();
		System.out.println("Dokument: "+id+title+description+filename);
		params.add(fullpath());
		params.add(id);
		params.add(title);
		params.add(description);
		params.add(data);
		params.add(filename);
		String newpath = (String)execute("upload_file", params);
		return new File(client, newpath, null);
	}

	/**
	 * Uploads an image into the document
	 * @param id: short name for the image
	 * @param title
	 * @param description
	 * @param data: binary data of the image
	 * @param filename
	 * @return
	 */	public Image uploadImage(final String id, final String title, final String description, final byte[] data, final String filename) {
		Vector<Object> params = new Vector<Object>();
		System.out.println("Dokument: "+id+title+description+filename);
		params.add(fullpath());
		params.add(id);
		params.add(title);
		params.add(description);
		params.add(data);
		params.add(filename);
		String newpath = (String)execute("upload_image", params);
		return new Image(client, newpath, null);
	}
	public String autocreateSubdocuments() {
		Vector<Object> params = new Vector<Object>();
		params.add(fullpath());
		String msg = (String)execute("autocreate_subdocuments", params);
		return msg;
	 }
	 
	 public String readPropertiesFromFile() {
		Vector<Object> params = new Vector<Object>();
		params.add(fullpath());
		String msg = (String)execute("read_properties_from_file", params);
		return msg;
	 }

	public String setProperty(final String name, final String value, final String type) {
		Vector<Object> params = new Vector<Object>();
		params.add(fullpath());
		params.add(name);
		params.add(value);
		params.add(type);
		String msg = (String)execute("set_property", params);
		return msg;
	}

	public String deleteProperty(final String name) {
		Vector<Object> params = new Vector<Object>();
		params.add(fullpath());
		params.add(name);
		String msg = (String)execute("delete_property", params);
		return msg;
	}

	public String getProperty(final String name) {
		Vector<Object> params = new Vector<Object>();
		params.add(fullpath());
		params.add(name);
		String msg = (String)execute("get_property", params);
		return msg;
	}

	public Map<String,String> getProperties() {
        Vector<Object> params = new Vector<Object>();
        params.add(fullpath());
        Map<String,String> msg = (Map<String,String>)execute("get_properties", params);
        return msg;

    }

}
