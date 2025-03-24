package de.bfs.dokpool.client.content;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

	@SuppressWarnings("unchecked")
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
	private JSON.Node getContentsNode() {
		if (contentsNode == null) {
			try {
				contentsNode = privateService.nodeFromGetRequest(pathAfterPlonesite,"metadata_fields=id");
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
	public Folder getFolder(String subpath) {
		subpath = subpath.startsWith("/") ? subpath: ("/" + subpath);
		try {
			JSON.Node subpathNode = privateService.nodeFromGetRequest(pathAfterPlonesite + subpath);
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
		return getContents("SimpleFolder");
	}

	/**
	 * Return this folders full path (http://mydokppol.example.com:8080/dokpool/content -&gt; /dokpool/content).
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
		Map<String, Object> attributes = new HashMap<String, Object>();
		if (title != null) { attributes.put("title", title); }
		if (description != null) { attributes.put("description", description); }
		if (text != null) { attributes.put("text", text); }
		if (docType != null) { attributes.put("docType", docType); }
		if (behaviors != null) { attributes.put("local_behaviors", behaviors); }
		return createDPDocument(id, attributes);
	}

	public Document createDPDocument(String id, Map<String, Object> attributes) {
		try {
			attributeCompatibilityAdjustment(attributes);
			JSON.Node createJS = new JSON.Node(attributes);
			createJS
				.set("@type","DPDocument")
				.set("id", id)
			;
			if (createJS.get("title") == null) {
				createJS.set("title", "no title provided");
			}
			if (createJS.get("text") == null) {
				createJS.set("text", "no text provided");
			}
			JSON.Node localBehaviors = createJS.get("local_behaviors");
			if (localBehaviors == null) {
				localBehaviors = (new JSON.Node ("[]")).append("elan");
				createJS.set("local_behaviors", localBehaviors);
			}
			if (localBehaviors.arrayHasValue("doksys")) {
				if (createJS.get("OperationMode") == null) {
					createJS.set("OperationMode", "Routine");
				}
			}
			if (localBehaviors.arrayHasValue("rodos")) {
				Document.rodosCheck(createJS, true);
			}
			if (localBehaviors.arrayHasValue("rei")) {
				if (createJS.get("MStIDs") == null) {
					createJS.set("MStIDs", new JSON.Node("null"));
				}
				if (createJS.get("docType") == null) {
					createJS.set("docType", "reireport");
				}
				if ((createJS.get("Authority") == null) || (createJS.get("ReiLegalBases") == null) ||
						(createJS.get("NuclearInstallations") == null) || (createJS.get("Year") == null) ||
						(createJS.get("Period") == null) || (createJS.get("Origins") == null) ||
						(createJS.get("PDFVersion") == null)
				) {
					log.error("Authority, ReiLegalBases, NuclearInstallations, Year, Period, Origins and PDFVersion are mandatory for REI with no sensible default.");
				}
			}

			
			if (createJS.get("docType") == null) {
				createJS.set("docType", "doksysdok");
			}

			JSON.Node rspNode = privateService.postRequestWithNode(pathAfterPlonesite, createJS);
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

	public BaseObject createObject(String id, Map<String, Object> attributes, String type) {
		try {
			if (type == null) {
				log.error("Objects need a type (e.g. DPDocument).");
				return null;
			}
			//if you ask for a DPDocument, we call the specialized method to ensure mandatory attributes are set
			if (type == "DPDocument") {
				return createDPDocument(id,attributes);
			}
			attributeCompatibilityAdjustment(attributes);
			JSON.Node createJS = new JSON.Node(attributes)
				.set("@type",type)
			;
			JSON.Node rspNode = privateService.postRequestWithNode(pathAfterPlonesite, createJS);
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
			JSON.Node rspNode = privateService.postRequestWithNode(pathAfterPlonesite + "/@copy", copyJS);
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
		Map<String, Object> attributes = new HashMap<>();
		for (String appName : behaviors) {
			App app = App.fromString(appName);
			switch (app) {
			case ELAN:
				assert(!mapEmptyOrNull(elanProperties));
				attributes.putAll(elanProperties);
				break;
			case RODOS:
				assert(!mapEmptyOrNull(rodosProperties));
				attributes.putAll(rodosProperties);
				break;
			case DOKSYS:
				assert(!mapEmptyOrNull(doksysProperties));
				attributes.putAll(doksysProperties);
				break;
			case REI:
				assert(!mapEmptyOrNull(reiProperties));
				attributes.putAll(reiProperties);
				break;
			}
		}
		if (title != null) { attributes.put("title", title); }
		if (description != null) { attributes.put("description", description); }
		if (text != null) { attributes.put("text", text); }
		if (docType != null) { attributes.put("docType", docType); }
		if (behaviors != null) { attributes.put("local_behaviors", behaviors); }
		return createDPDocument(id, attributes);
	}

	/**
	 * Deletes the BaseObject with id in this Folder.
	 * @return true if deletion succeeds, false otherwise
	 */
	public boolean deleteObject(String id) {
		return (new BaseObject(service, pathAfterPlonesite + "/" + id, (Object[]) null)).delete();
	}

}
