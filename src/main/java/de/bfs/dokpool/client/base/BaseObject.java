package de.bfs.dokpool.client.base;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import org.apache.xmlrpc.client.XmlRpcClient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Base class for all API objects. Contains helper methods for all types.
 *
 */
public class BaseObject {
	protected final Log log = LogFactory.getLog(DocpoolBaseService.class);

	protected XmlRpcClient client = null;
	protected DocpoolBaseService service = null;
	//
	private String pathWithPlonesite = null;
	protected String pathAfterPlonesite = null;
	protected Map<String,Object> data = null;
	//we may initilize an Object with partial data
	protected boolean dataComplete = false;
	protected Map<String,Object> metadata = null;
	
	public BaseObject(XmlRpcClient client, String path, Object[] alldata) {
		this.client = client;
		this.pathWithPlonesite = path;
		if (alldata != null) {
			data = (Map<String,Object>)alldata[0];
			dataComplete = true;
		}
	}

	public BaseObject(DocpoolBaseService service, String path, Object[] alldata) {
		this.service = service;
		this.pathAfterPlonesite = path;
		if (alldata != null) {
			data = (Map<String,Object>)alldata[0];
		}
	}

	public BaseObject(DocpoolBaseService service, String path, Map<String,Object> data) {
		this.service = service;
		this.pathAfterPlonesite = path;
		this.data = data;
		idFromAtIdIfMissing();
	}

	protected String fullpath(){
		if (pathAfterPlonesite != null){
			return "/" + service.plonesite + pathAfterPlonesite;
		} else {
			return pathWithPlonesite;
		}
	}
	

	/**
	 * Fetches all data and contents for this object via XMLRPC.
	 * There will be no REST equivalent for this method as its only
	 * use except in getData() is in Folder.
	 * @return object data as XMLRPC structure
	 */
	protected Object[] getObjectDataX() {
		Vector<String> params = new Vector<String>();
		params.add(fullpath());
		params.add("");
		Object[] res = (Object[])executeX("get_plone_object", params);
		return res;
	}
	
	/**
	 * Gets just the attributes for the object from XMLRPC data.
	 * @return object attributes as map
	 */
	private Map<String,Object> getDataX() {
		if (data == null) {
			data = (Map<String,Object>)((Object[])(getObjectDataX()[1]))[0];
			dataComplete = true;
		}
		return data;
	}

	/**
	 * Gets just the attributes for the object via a REST-Request.
	 * @return object attributes as map
	 */
	private Map<String,Object> getData() {
		if (data == null || !dataComplete) {
			//TODO: remove XMLRPC-Part
			if (client != null){
				return getDataX();
			}
			try {
				data = service.mapFromGetRequest(pathAfterPlonesite);
			} catch (Exception ex) {
				log.error(exeptionToString(ex));
			}
			dataComplete = true;
		}
		return data;
	}
	
	
	protected Object executeX(String command, Vector params) {
		return DocpoolBaseService.execute(this.client, command, params);
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
	 * Helper to get value of an attribute.
	 * @param name: the name of the attribute
	 * @return the value
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
	public Date getDateAttributeX(String name) {
		if (getAttribute(name) != null) {
			Object dateObject = getAttribute(name);
			if (dateObject instanceof Date){
				return (Date)dateObject;
			} else {
				//TODO:This assumes ISO-Format, check date format(s) actually used.
				SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);
				try {
					return iso.parse((String) dateObject);
				} catch (ParseException pe) {
					log.error("Malformed Date: "+ (String) dateObject);
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
			Object[] results = (Object[]) getAttribute(name);
			for (Object result: results) {
				values.add((String)result);
			}
			return values;		
		}
		else {
			return new ArrayList<>();
		}		
	}
	
	public String getId() {
		idFromAtIdIfMissing();
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
	public String getWorkflowStatusX() {
		Vector<String> params = new Vector<String>();
		params.add(fullpath());
		Map<String, Object> res = (Map<String, Object>)executeX("get_workflow", params);
		return (String)res.get("state");
	}

	/**
	 * @return The workflow status of the object (i.e. 'published', 'private', ...)
	 */
	public String getWorkflowStatus() {
		try {
			JSON.Node node = service.nodeFromGetRequest(pathAfterPlonesite+"/@workflow").get("state").get("id");
			return node.toString();
		} catch (Exception ex){
			log.error(exeptionToString(ex));
			return null;
		}
	}
	
	/**
	 * Attempts to execute a transition to set a new workflow status.
	 * @param transition: the name of the transition
	 */
	public void setWorkflowStatusX(String transition) {
		Vector<String> params = new Vector<String>();
		params.add(transition);
		params.add(fullpath());
		executeX("set_workflow", params);		
	}

	/**
	 * Attempts to execute a transition to set a new workflow status.
	 * @param transition: the name of the transition
	 */
	public void setWorkflowStatus(String transition) {
		try {
			String endpoint = "/@workflow";
			JSON.Node transNode = new JSON.Node("{}");
			switch(transition){
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
			service.postRequestWithNode(pathAfterPlonesite+endpoint, transNode);
		} catch (Exception ex) {
			log.error(exeptionToString(ex));
		}

	}
	
	/**
	 * Update the object with the given properties.
	 * @param properties
	 */
	public void updateX(Map<String, Object> properties) {
		Vector<Object> params = new Vector<Object>();
		params.add(fullpath());
		params.add(properties);
		executeX("update_dp_object", params);
	}

	/**
	 * Update the object's attributes with the given properties.
	 * Any attribute that is not explicitly set will keep its value;
	 * @param properties
	 */
	public boolean update(Map<String, Object> properties) {
		/* we reset the data, as setting some attribute to a new value
		 * might trigger changes in other attributes
		 */
		data = null;
		dataComplete = false;
		try {
			service.patchRequestWithMap(pathAfterPlonesite, properties);
			return true;
		} catch(Exception ex) {
			log.error(exeptionToString(ex));
			return false;
		}
	}

	/**
	 * Update a single attribute with a given value.
	 * To unset a value set, set it to null.
	 * @param properties
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
			HttpClient.Response rsp = service.deleteRequest(pathAfterPlonesite);
			JSON.Node rspNode = new JSON.Node(rsp.content);
			if (rspNode.get("type") != null && rspNode.get("type").toString().equals("NotFound")){
				log.info(rspNode.get("message"));
				return false;
			}
			return true;
		} catch (Exception ex) {
			log.error(exeptionToString(ex));
			return false;
		}
	}

	public static String exeptionToString(Exception ex) {
		return DocpoolBaseService.exeptionToString(ex);
	}

	protected Map<String,Object> dataFromNode(JSON.Node node) {
		try {
			return node.toMap();
		} catch (Exception ex){
			log.error(exeptionToString(ex));
			return null;
		}
	}

	private void idFromAtIdIfMissing() {
		if (data == null || data.get("id") != null || data.get("id") != null) {
			return;
		}
		String atid = (String) data.get("@id");
		String id = atid.substring(atid.lastIndexOf("/")+1);
		data.put("id",id);
	}
	
	
}
