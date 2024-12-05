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
import de.bfs.dokpool.client.base.JSON;

/**
 * Wraps all Folder types and functions as a base class for all folderish types.
 *
 */
public class Folder extends BaseObject {
	protected Map<String, Object> contents = null;
	protected JSON.Node contentsNode = null;

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
	private Map<String, Object> getContentsX() {
		if (contents == null) {
			contents = (Map<String, Object>) ((Map<String, Object>) ((Object[]) (getObjectDataX()[1]))[2])
					.get("contents");
		}
		return contents;
	}

	/**
	 * @return the complete contents of this folder
	 */
	private JSON.Node getContentsNode() {
		if (contentsNode == null) {
			try {
				contentsNode = service.nodeFromGetRequest(pathAfterPlonesite,"metadata_fields=id");
				if (contentsNode.errorInfo != null) {
					log.info(contentsNode.errorInfo.toString());
					return null;
				}
			} catch (Exception ex) {
				log.error(exceptionToString(ex));
			}
		}
		return contentsNode;
	}

	/**
	 * Get a subfolder.
	 * 
	 * @param subpath:
	 *            the relative path of the subfolder
	 * @return the subfolder
	 */
	public Folder getFolderX(String subpath) {
		Vector<String> params = new Vector<String>();
		params.add(fullpath());
		params.add(subpath);
		Object[] res = (Object[]) DocpoolBaseService.execute(client, "get_plone_object", params);
		return new Folder(client, (String) res[0], (Object[]) res[1]);
	}

	/**
	 * Get a subfolder.
	 * 
	 * @param subpath:
	 *            the relative path of the subfolder
	 * @return the subfolder
	 */
	public Folder getFolder(String subpath) {
		subpath = subpath.startsWith("/") ? subpath: ("/" + subpath);
		try {
			JSON.Node subpathNode = service.nodeFromGetRequest(pathAfterPlonesite + subpath);
			if (subpathNode.errorInfo != null) {
				log.info(subpathNode.errorInfo.toString());
				return null;
			}
			return new Folder(service, service.pathWithoutPrefix(subpathNode), subpathNode.toMap());
		} catch (Exception ex) {
			log.error(exceptionToString(ex));
			return null;
		}
	}


	// data = service.mapFromGetRequest(pathAfterPlonesite);

	/**
	 * Return all folder contents, can be filtered by type.
	 * 
	 * @param type:
	 *            the Plone type name or null
	 * @return folder contents, possibly filtered by type
	 */
	public List<Object> getContentsX(String type) {
		if (getContentsX() != null) {
			ArrayList<Object> res = new ArrayList<Object>();
			for (String path : getContentsX().keySet()) {
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
	 * Return all folder contents, can be filtered by type.
	 * 
	 * @param type:
	 *            the Plone type name or null
	 * @return folder contents, possibly filtered by type
	 */
	public List<Object> getContents(String type) {
		if (getContentsNode() != null) {
			ArrayList<Object> res = new ArrayList<Object>();
			for (JSON.Node itemNode : contentsNode.get("items")) {
				String portal_type = itemNode.get("@type").toString();
				portal_type = portal_type == null ? "" : portal_type;
				String path = service.pathWithoutPrefix(itemNode);
				if ((type == null) || (type.equals(portal_type))) {
					if (portal_type.equals("SimpleFolder") || portal_type.equals("ELANTransferFolder")) {
						res.add(new Folder(service, path, (Object[]) null));
					} else if (portal_type.equals("DPDocument") || portal_type.equals("InfoDocument")) {
						res.add(new Document(service, path, (Object[]) null));
					} else if (portal_type.equals("File")) {
						res.add(new File(service, path, (Object[]) null));
					} else if (portal_type.equals("Image")) {
						res.add(new Image(service, path, (Object[]) null));
					}
				}
			}
			return res;
		} else {
			return null;
		}
	}

	/**
	 * Return the content item with ID `id`.
	 * The return value can be casted to a descendant of
	 * BaseObject based on the `@type` value.
	 * 
	 * @param id the id of the requested item
	 * @return object representing the contentsItem with id `id`.
	 */
	public BaseObject getContentItem(String id) {
		if (getContentsNode() != null) {
			try {
				for (JSON.Node itemNode : contentsNode.get("items")) {
					if (id.equals(itemNode.get("id").toString())) {
						String portal_type = itemNode.get("@type").toString();
						portal_type = portal_type == null ? "" : portal_type;
						String path = service.pathWithoutPrefix(itemNode);
						switch(portal_type) {
							case "SimpleFolder":
							case "ELANTransferFolder":
								return new Folder(service, path, itemNode.toMap());
							case "DPDocument":
							case "InfoDocument":
								return new Document(service, path, itemNode.toMap());
							case "File":
								return new File(service, path, itemNode.toMap());
							case "Image":
								return new Image(service, path, itemNode.toMap());
							default:
								return new BaseObject(service, path, itemNode.toMap());
						}
					}
				}
			} catch (Exception ex) {
				log.error(exceptionToString(ex));
			}
		}
		return null;
	}

	/**
	 * @return only subfolders of type ELANFolder
	 */
	public List<Object> getSubFolders() {
		//TODO: remove XMLRPC part
		return client != null ? getContentsX("SimpleFolder"): getContents("SimpleFolder");
	}

	/**
	 * Return this folders full path (http://mydokppol.example.com:8080/dokpool/content -> /dokpool/content).
	 * @deprecated Use BaseObject.getPathAfterPlonesite() or BaseObject.getPathWithPlonesite() instead.
	 * The constructor of all BaseObject descendants expects a path WITHOUT the plonesite.
	 * This method will not be removed, it is only marked as deprecated to stop users
	 * from using the returned path in constructors.
	 * @return path of Folder INCLUDING the plonesite.
	 */
	@Deprecated public String getFolderPath() {
		return this.fullpath();
	}

	/**
	 * @return only documents within this folder
	 */
	public List<Object> getDocuments() {
		//TODO: remove XMLRPC part
		return client != null ? getContentsX("DPDocument"): getContents("DPDocument");
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
		return client != null ? createDPDocumentX(id, properties) : createDPDocumentX(id, properties);
	}

	public Document createDPDocumentX(String id, Map<String, Object> properties) {
		Vector<Object> params = new Vector<Object>();
		params.add(fullpath());
		params.add(id);
		params.add(properties);
		params.add("DPDocument");
		String newpath = (String) executeX("create_dp_object", params);
		return new Document(client, newpath, null);
	}

	//TODO: does not work, because of some error in Python code
	public Document createDPDocument(String id, Map<String, Object> properties) {
		try {
			JSON.Node createJS = new JSON.Node(properties);
			createJS
				.set("@type","DPDocument")
				.set("id", id)
			;
			//TODO: which attributes are actually mandatory and which are only mandatory beacuse of server side bugs?
			//set the mandatory attributes if not given:
			if (createJS.get("title") == null) {
				createJS.set("title", "no title provided");
			}
			if (createJS.get("text") == null) {
				createJS.set("text", "no text provided");
			}
			if (createJS.get("docType") == null) {
				createJS.set("docType", "doksysdok");
			}
			if (createJS.get("local_behaviors") == null) {
				createJS.set("local_behaviors", (new JSON.Node ("[]")).append("elan"));
			}

			JSON.Node rspNode = service.postRequestWithNode(pathAfterPlonesite, createJS);
			if (rspNode.errorInfo != null) {
				log.info(rspNode.errorInfo.toString());
				return null;
			}
			String newpath = service.pathWithoutPrefix(rspNode);
			return new Document(service, newpath, (Object[]) null);
		} catch (Exception ex) {
			log.error(exceptionToString(ex));
			return null;
		}
	}

	public BaseObject createObjectX(String id, Map<String, Object> properties, String type) {
		Vector<Object> params = new Vector<Object>();
		params.add(fullpath());
		params.add(id);
		params.add(properties);
		params.add(type);
		String newpath = (String) executeX("create_dp_object", params);
		return new BaseObject(client, newpath, null);
	}

	//TODO: does not work, because of some error in Python code
	//TODO: if @type is needed, we should check it ourself before sending the request
	public BaseObject createObject(String id, Map<String, Object> properties, String type) {
		try {
			JSON.Node createJS = new JSON.Node(properties);
			JSON.Node rspNode = service.postRequestWithNode(pathAfterPlonesite, createJS);
			if (rspNode.errorInfo != null) {
				log.info(rspNode.errorInfo.toString());
				return null;
			}
			String newpath = service.pathWithoutPrefix(rspNode);
			return new BaseObject(service, newpath, (Object[]) null);
		} catch (Exception ex) {
			log.error(exceptionToString(ex));
			return null;
		}
	}

	/**
	 * Create a copy of the object found und the given path in this Folfer.
	 * @param srcPath the path (after plonesite) of the source object.
	 * @return a BaseObject representing the copy, the result can ce casted to the
	 * same subclass (e.g. Document, Event, Image) as the argument bo.
	 */
	public BaseObject createCopyOf(String srcPath) {
		return createCopyOf(new BaseObject(service, srcPath,  (Object[]) null));
	}

	/**
	 * Create a copy of the referenced object in this Folfer.
	 * @param bo
	 * @return a BaseObject representing the copy, the result can ce casted to the
	 * same subclass (e.g. Document, Event, Image) as the argument bo.
	 */
	public BaseObject createCopyOf(BaseObject bo) {
		try {
			String srcPath = bo.getPathAfterPlonesite();
			JSON.Node copyJS = new JSON.Node("{}")
				.set("source", srcPath)
			;
			JSON.Node rspNode = service.postRequestWithNode(pathAfterPlonesite + "/@copy", copyJS);
			if (rspNode.errorInfo != null) {
				log.info(rspNode.errorInfo.toString());
				return null;
			}
			return bo.getClass().getConstructor(DocpoolBaseService.class, String.class, Object[].class).newInstance(
				service, service.pathWithoutPrefix(rspNode.get(0).get("target").toString()), (Object[]) null
			);
		} catch (Exception ex) {
			log.error(exceptionToString(ex));
			return null;
		}
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
		//TODO: remove XMLRPC
		if (client!= null) {document.updateX(properties);} else {document.update(properties);}
		return document;
	}

	/**
	 * Deletes the BaseObject with id in this Folder.
	 * @return true if deletion succeeds, false otherwise
	 */
	public boolean deleteObject(String id) {
		return (new BaseObject(service, pathAfterPlonesite + "/" + id, (Object[]) null)).delete();
	}

}
