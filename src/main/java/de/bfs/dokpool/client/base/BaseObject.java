package de.bfs.dokpool.client.base;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.xmlrpc.client.XmlRpcClient;

import de.bfs.dokpool.client.utils.Utils;


/**
 * Base class for all API objects. Contains helper methods for all types.
 *
 */
public class BaseObject {
	protected XmlRpcClient client = null;
	protected DocpoolBaseService service = null;
	//
	protected String pathWithPlonesite = null;
	private String pathAfterPlonesite = null;
	protected Map<String,Object> data = null;
	protected Map<String,Object> metadata = null;
	
	public BaseObject(XmlRpcClient client, String path, Object[] alldata) {
		this.client = client;
		this.pathWithPlonesite = path;
		if (alldata != null) {
			data = (Map<String,Object>)alldata[0];
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
	 * @return object data as XMLRPC structure
	 */
	protected Object[] getObjectData() {
		Vector<String> params = new Vector<String>();
		params.add(fullpath());
		params.add("");
		Object[] res = (Object[])execute("get_plone_object", params);
		return res;
	}
	
	/**
	 * Gets just the attributes for the object from XMLRPC data.
	 * @return object attributes as map
	 */
	private Map<String,Object> getData() {
		if (data == null) {
			data = (Map<String,Object>)((Object[])(getObjectData()[1]))[0];
		}
		return data;
	}
	
	
	protected Object execute(String command, Vector params) {
		return Utils.execute(this.client, command, params);
	}
	
	/**
	 * Helper to get value of a string valued attribute.
	 * @param name: the name of the attribute
	 * @return the String value
	 */
	public String getStringAttribute(String name) {
		if (getData() != null) {
			return (String)getData().get(name);			
		}
		else {
			return null;
		}
	}
	
	public Date getDateAttribute(String name) {
		if (getData() != null) {
			return (Date)getData().get(name);			
		}
		else {
			return null;
		}		
	}
	
	public List<String> getStringsAttribute(String name) {
		if (getData() != null) {
			List<String> values = new ArrayList<>();
			Object[] results = (Object[])getData().get(name);
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
		Vector<String> params = new Vector<String>();
		params.add(fullpath());
		Map<String, Object> res = (Map<String, Object>)execute("get_workflow", params);
		return (String)res.get("state");
	}
	
	/**
	 * Attempts to execute a transition to set a new workflow status.
	 * @param transition: the name of the transition
	 */
	public void setWorkflowStatus(String transition) {
		Vector<String> params = new Vector<String>();
		params.add(transition);
		params.add(fullpath());
		execute("set_workflow", params);		
	}
	
	/**
	 * Update the object with the given properties.
	 * @param properties
	 */
	public void update(Map<String, Object> properties) {
		Vector<Object> params = new Vector<Object>();
		params.add(fullpath());
		params.add(properties);
		execute("update_dp_object", params);

	}
	
	
}
