package de.bfs.dokpool.client.base;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


/**
 * Base class for all API objects. Contains helper methods for all types.
 *
 */
public class BaseObject {
	protected final DocpoolBaseService.Log log = new DocpoolBaseService.Log(this.getClass());

	protected DocpoolBaseService service = null;
	protected DocpoolBaseService.PrivateDocpoolBaseService privateService;
	//
	private String pathWithPlonesite = null;
	protected String pathAfterPlonesite = null;
	protected Map<String,Object> data = null;
	//we may initilize an Object with partial data
	protected boolean dataComplete = false;
	protected Map<String,Object> metadata = null;

	@SuppressWarnings("unchecked")
	public BaseObject(DocpoolBaseService service, String path, Object[] alldata) {
		this.service = service;
		this.privateService = service.privateService;
		this.pathAfterPlonesite = path;
		if (alldata != null) {
			data = (Map<String,Object>)alldata[0];
		}
	}

	protected static final Map<String,Object> noData = null;

	public BaseObject(DocpoolBaseService service, String path, Map<String,Object> data) {
		this.service = service;
		this.privateService = service.privateService;
		this.pathAfterPlonesite = path;
		this.data = data;
	}

	protected String fullpath() {
		if (pathAfterPlonesite != null) {
			return "/" + service.plonesite + pathAfterPlonesite;
		} else {
			return pathWithPlonesite;
		}
	}

	/**
	 * e.g. if pathAfterPlonesite = /bund/... -&gt; bund
	 * @return the part of the path that specifies the Dokpool.
	 */
	protected String dokpoolId() {
		return pathAfterPlonesite.substring(1,pathAfterPlonesite.indexOf('/', 1));
	}

	/**
	 * Get the path within the dokpool plone instance.
	 * @return the path after the plonesite including the first /,
	 * e.g. http://dokpool.example.com:8080/dokpool/bund/content -&gt; /bund/content
	 */
	public String getPathAfterPlonesite() {
		return pathAfterPlonesite;
	}

	/**
	 * Get path on the server including the name of the ploneite (e.g. /dokpool).
	 * DO NOT USE this function to construct a path for the cosntructors of
	 * BaseObject and its descendant.
	 * @return the path after domain (and port) including the first /,
	 * e.g. http://dokpool.example.com:8080/dokpool/bund/content -&gt; to /dokpool/bund/content
	 */
	public String getPathWithPlonesite() {
		return fullpath();
	}

	/**
	 * Gets just the attributes for the object via a REST-Request.
	 * @return object attributes as map
	 */
	private Map<String,Object> getData() {
		if (data == null || !dataComplete) {
			try {
				JSON.Node rspNode = privateService.nodeFromGetRequest(pathAfterPlonesite);
				if (rspNode.errorInfo != null) {
					log.info(rspNode.errorInfo.toString());
					data = new HashMap<String,Object> ();
				} else {
					data = rspNode.toMap();
				}
			} catch (Exception ex) {
				log.error(exceptionToString(ex));
			}
			dataComplete = true;
		}
		return data;
	}

	/**
	 * Helper to get value of an attribute.
	 * @param name: the name of the attribute
	 * @return the value
	 */
	public Object getAttribute(String name) {
		//we fetch data if and only if we have no data or
		//(incomplete data with the requesting string missing)
		if (data == null || (!dataComplete && data.get(name) == null)) {
			getData();
		}
		//data or data.get(name) may still be null
		if (data != null) {
			return data.get(name);			
		} else {
			return null;
		}
	}

	/**
	 * Helper to get all attributes.
	 * @return a map of all attributes
	 */
	public Map<String,Object> getAllAttributes() {
		//we fetch data if and only if we have no data or
		//(incomplete data with the requesting string missing)
		if (data == null || !dataComplete) {
			getData();
		}
		return data;
	}
	
	/**
	 * Helper to get value of a string valued attribute.
	 * @param name: the name of the attribute
	 * @return the String value
	 */
	public String getStringAttribute(String name) {
		return (String) getAttribute(name);
	}
	
	//TODO: Dates will be likely be Strings, so we can simplify this in a REST-only world.
	public Date getDateAttribute(String name) {
		if (getAttribute(name) != null) {
			Object dateObject = getAttribute(name);
			if (dateObject instanceof Date) {
				return (Date)dateObject;
			} else {
				try {
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("[yyyy-MM-dd'T'HH:mm:ss[.SSS][XXX]]", Locale.ENGLISH).withZone(ZoneOffset.UTC);
					ZonedDateTime zdt = formatter.parse((String) dateObject, ZonedDateTime ::from);
					return Date.from(zdt.toInstant());
				} catch (java.time.format.DateTimeParseException pe) {
					log.error("Malformed Date: "+ (String) dateObject);
					log.error(exceptionToString(pe));
					return null;
				}
			}
		}
		else {
			return null;
		}		
	}
	
	public List<String> getStringsAttribute(String name) {
		if (getAttribute(name) != null) {
			List<String> values = new ArrayList<>();
			Object resultsObj = getAttribute(name);
			if (resultsObj instanceof Object[]) {
				Object[] results = (Object[]) resultsObj;
				for (Object result: results) {
					if (result instanceof String) {
						values.add((String)result);
					} else {
						//add a String representation as a last resort
						values.add(result.toString());
					}
				}
			} else if (resultsObj instanceof List) {
				@SuppressWarnings("unchecked")
				List<Object> results = (List<Object>) resultsObj;
				for (Object result: results) {
					if (result instanceof String) {
						values.add((String)result);
					} else {
						//add a String representation as a last resort
						values.add(result.toString());
					}
				}
			}
			return values;		
		}
		else {
			return new ArrayList<>();
		}
	}
	
	public String getId() {
		return getStringAttribute("id");
	}
		
	public String getTitle() {
		return getStringAttribute("title");
	}
	
	public String getDescription() {
		return getStringAttribute("description");
	}

	/**
	 * @return The workflow status of the object (i.e. 'published', 'private', ...)
	 */
	public String getWorkflowStatus() {
		try {
			JSON.Node rspNode = privateService.nodeFromGetRequest(pathAfterPlonesite+"/@workflow");
			if (rspNode.errorInfo != null) {
				log.info(rspNode.errorInfo.toString());
				return null;
			}
			return rspNode.get("state").get("id").toString();
		} catch (Exception ex) {
			log.error(exceptionToString(ex));
			return null;
		}
	}

	/**
	 * Attempts to execute a transition to set a new workflow status.
	 * @param transition: the name of the transition
	 */
	public void setWorkflowStatus(String transition) {
		try {
			String endpoint = "/@workflow";
			JSON.Node transNode = new JSON.Node("{}");
			switch(transition) {
				case "publish":
					transNode
						.set("action","publish")
						.set("review_state", "published")
					;
					endpoint = endpoint + "/publish";
					break;
				case "retract":
					transNode
						.set("action","retract")
						.set("review_state", "private")
					;
					endpoint = endpoint + "/retract";
					break;
			}
			JSON.Node rspNode = privateService.postRequestWithNode(pathAfterPlonesite+endpoint, transNode);
			if (rspNode.errorInfo != null) {
				log.info(rspNode.errorInfo.toString());
			}
		} catch (Exception ex) {
			log.error(exceptionToString(ex));
		}

	}

	protected List<Object> ensureObjectIsList(Object o) {
		if (o instanceof List) {
			@SuppressWarnings("unchecked")
			List<Object> list = (List<Object>) o;
			return list;
		} else if (o.getClass().isArray()) {
			List<Object> list = Arrays.<Object>asList((Object[]) o);
			return list;
		} else {
			String clazz = o != null ? o.getClass().toString() : "null";
			log.error("Cannot convert to list, object has class: " + clazz);
			return null;
		}

	}

	protected void attributeCompatibilityAdjustment(Map<String,Object> attributes) {
		if (attributes == null) {
			return;
		}
		for (Map.Entry<String,Object> entry: attributes.entrySet()) {
			switch (entry.getKey()) {
				case "events":
				case "scenarios":
					attributes.put(entry.getKey(),eventIdsToUids(ensureObjectIsList(entry.getValue())));
					break;
			}
		}
	}

	protected List<Object> eventIdsToUids (List<Object> eventIds) {
		List<Object> ret = new ArrayList<Object>();
		//we assume the event ids refer to events of the current BasePbject's Dokpool
		String dpId = dokpoolId();
		for (Object evIdObj : eventIds) {
			String evId = (String) evIdObj;
			String uid = (new de.bfs.dokpool.client.content.Event(service, "/"+dpId+"/contentconfig/scen/"+evId, noData)).getStringAttribute("UID");
			if (uid == null) {
				//Maybe the evId already was a uid? Then we will get a non-null path fot it:
				if (privateService.uidToPathAfterPlonesite(evId) != null) {
					uid = evId;
					ret.add(uid);
				} else {
					log.error("Could not get event uid for event with id: " + evId);
				}
			} else {
				ret.add(uid);
			}
		}
		return ret;
	}

	/**
	 * Update the object's attributes with the given map.
	 * Any attribute that is not explicitly set will keep its value;
	 * @param attributes
	 * @return true, if the update succeeded; false otherwise
	 */
	public boolean update(Map<String, Object> attributes) {
		/* we reset the data, as setting some attribute to a new value
		 * might trigger changes in other attributes
		 */
		data = null;
		dataComplete = false;
		//Some attributes (e.g. scenarios) need to be handled differently in Plone6
		attributeCompatibilityAdjustment(attributes);
		try {
			log.info("update: " + new JSON.Node(attributes).toJSON());
			JSON.Node rspNode = privateService.patchRequestWithNode(pathAfterPlonesite, new JSON.Node(attributes));
			if (rspNode.errorInfo != null) {
				log.info(rspNode.errorInfo.toString());
				return false;
			}
			return true;
		} catch(Exception ex) {
			log.error(exceptionToString(ex));
			return false;
		}
	}

	/**
	 * Update a single attribute with a given value.
	 * To unset a value set, set it to null.
	 * @param name the name of the attribute
	 * @param value its designated value
	 */
	public boolean setAttribute(String name, Object value) {
		Map<String,Object> attribute = new HashMap<String,Object>();
		attribute.put(name,value);
		return update(attribute);
	}

	/**
	 * Deletes this BaseObject.
	 * @return true if deletion succeeds, false otherwise
	 */
	public boolean delete() {
		try {
			JSON.Node rspNode = privateService.deleteRequest(pathAfterPlonesite);
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

	protected static String exceptionToString(Exception ex) {
		return DocpoolBaseService.exceptionToString(ex);
	}

	protected Map<String,Object> dataFromNode(JSON.Node node) {
		try {
			return node.toMap();
		} catch (Exception ex) {
			log.error(exceptionToString(ex));
			return null;
		}
	}
	
	
}
