package de.bfs.dokpool.client.content;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.xmlrpc.client.XmlRpcClient;

import de.bfs.dokpool.client.base.App;
import de.bfs.dokpool.client.base.BaseObject;
import de.bfs.dokpool.client.base.DocpoolBaseService;
import de.bfs.dokpool.client.utils.Utils;

/**
 * Wraps all Folder types and functions as a base class for all folderish types.
 *
 */
public class Folder extends BaseObject {
	protected Map<String, Object> contents = null;

	private static boolean mapEmptyOrNull(final Map<String, Object> map) {
		return map == null || map.isEmpty();
	}

	public Folder(XmlRpcClient client, String path, Object[] alldata) {
		super(client, path, alldata);
		if (alldata != null) {
			contents = (Map<String, Object>) ((Map<String, Object>) alldata[2]).get("contents");
		}
	}

	public Folder(DocpoolBaseService service, String path, Object[] alldata) {
		super(service, path, alldata);
		if (alldata != null) {
			contents = (Map<String, Object>) ((Map<String, Object>) alldata[2]).get("contents");
		}
	}

	public Folder(DocpoolBaseService service, String path, Map<String,Object> data) {
		super(service, path, data);
	}

	/**
	 * @return the complete contents of this folder
	 */
	private Map<String, Object> getContents() {
		if (contents == null) {
			contents = (Map<String, Object>) ((Map<String, Object>) ((Object[]) (getObjectDataX()[1]))[2])
					.get("contents");
		}
		return contents;
	}

	/**
	 * Get a subfolder.
	 * 
	 * @param subpath:
	 *            the relative path of the subfolder
	 * @return the subfolder
	 */
	public Folder getFolder(String subpath) {
		Vector<String> params = new Vector<String>();
		params.add(fullpath());
		params.add(subpath);
		Object[] res = (Object[]) Utils.execute(client, "get_plone_object", params);
		return new Folder(client, (String) res[0], (Object[]) res[1]);
	}

	/**
	 * Return all folder contents, can be filtered by type.
	 * 
	 * @param type:
	 *            the Plone type name or null
	 * @return folder contents, possibly filtered by type
	 */
	public List<Object> getContents(String type) {
		if (getContents() != null) {
			ArrayList<Object> res = new ArrayList<Object>();
			for (String path : getContents().keySet()) {
				Map<String, Object> metadata = (Map<String, Object>) contents.get(path);
				String portal_type = (String) metadata.get("Type");
				portal_type = portal_type == null ? "" : portal_type;
				if ((type == null) || (type.equals(portal_type))) {
					if (portal_type.equals("SimpleFolder") || portal_type.equals("ELANTransferFolder")) {
						res.add(new Folder(client, path, null));
					} else if (portal_type.equals("DPDocument") || portal_type.equals("InfoDocument")) {
						res.add(new Document(client, path, null));
					} else if (portal_type.equals("File")) {
						res.add(new File(client, path, null));
					} else if (portal_type.equals("Image")) {
						res.add(new Image(client, path, null));
					}
				}
			}
			return res;
		} else {
			return null;
		}

	}

	/**
	 * @return only subfolders of type ELANFolder
	 */
	public List<Object> getSubFolders() {
		return getContents("SimpleFolder");
	}

	/**
	 * @return path of Folder
	 */
	public String getFolderPath() {
		return this.fullpath();
	}

	/**
	 * @return only documents within this folder
	 */
	public List<Object> getDocuments() {
		return getContents("DPDocument");
	}

	/**
	 * Create a new document within this folder.
	 * 
	 * @param id:
	 *            the short name for the document (must be unique within the folder)
	 * @param title
	 * @param description
	 * @param text
	 * @param docType
	 * @param behaviors
	 * @return the newly created document
	 */
	public Document createDPDocument(String id, String title, String description, String text, String docType,
			String[] behaviors) {
		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put("title", title);
		properties.put("description", description);
		properties.put("text", text);
		properties.put("docType", docType);
		properties.put("local_behaviors", behaviors);
		return createDPDocument(id, properties);
	}

	public Document createDPDocument(String id, Map<String, Object> properties) {
		Vector<Object> params = new Vector<Object>();
		params.add(fullpath());
		params.add(id);
		params.add(properties);
		params.add("DPDocument");
		String newpath = (String) executeX("create_dp_object", params);
		return new Document(client, newpath, null);
	}

	public BaseObject createObject(String id, Map<String, Object> properties, String type) {
		Vector<Object> params = new Vector<Object>();
		params.add(fullpath());
		params.add(id);
		params.add(properties);
		params.add(type);
		String newpath = (String) executeX("create_dp_object", params);
		return new BaseObject(client, newpath, null);
	}

	public Document createAppSpecificDocument(String id, String title, String description, String text, String docType,
			String[] behaviors, Map<String, Object> elanProperties, Map<String, Object> doksysProperties,
			Map<String, Object> rodosProperties, Map<String, Object> reiProperties) {
		Map<String, Object> properties = new HashMap<>();
		for (String appName : behaviors) {
			App app = App.fromString(appName);
			switch (app) {
			case ELAN:
				assert(!mapEmptyOrNull(elanProperties));
				properties.putAll(elanProperties);
				break;
			case RODOS:
				assert(!mapEmptyOrNull(rodosProperties));
				properties.putAll(rodosProperties);
				break;
			case DOKSYS:
				assert(!mapEmptyOrNull(doksysProperties));
				properties.putAll(doksysProperties);
				break;
			case REI:
				assert(!mapEmptyOrNull(reiProperties));
				properties.putAll(reiProperties);
				break;
			}
		}
		Document document = createDPDocument(id, title, description, text, docType, behaviors);
		document.updateX(properties);
		return document;
	}

}
