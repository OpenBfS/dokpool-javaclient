package de.bfs.dokpool.client.content;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.apache.xmlrpc.client.XmlRpcClient;

import de.bfs.dokpool.client.base.BaseObject;
import de.bfs.dokpool.client.base.DocpoolBaseService;
import de.bfs.dokpool.client.base.HttpClient;
import de.bfs.dokpool.client.base.JSON;

public class Group extends BaseObject {
	
	private String groupId = "";
	private String dp = "";
	private String title = "";
	private String description = "";
	private Set<User> members = new HashSet<User>();
	private List<String> allowedDocTypes;
	
	protected Group(XmlRpcClient client, String path, String groupId, String title, String description, String dp) {
		super(client, path, null);
		this.groupId = groupId;
		this.title = title;
		this.description = description;
		this.dp = dp;
	}

	protected Group(DocpoolBaseService service, String path, String groupId, String title, String description, String dp) {
		super(service, path, (Object[])null);
		this.groupId = groupId;
		this.title = title;
		this.description = description;
		this.dp = dp;
	}

	public void setAllowedDocTypesX(String[] doctypes) {
		Vector<Object> params = new Vector<Object>();
		params.add(groupId);
		params.add(title);
		params.add(description);
		params.add(dp);
		params.add(doctypes);
		Object o = executeX("put_group", params);
		if (((String) o).equals("changed")) {
			allowedDocTypes = Arrays.asList(doctypes);
		}
	}

	/**
	 * NOT IMPLEMENTED.
	 * @param doctypes
	 */
	public void setAllowedDocTypes(String[] doctypes) {
	}
	
	public void addUserX(User user,String esd) {
		Vector<String> params = new Vector<String>();
		params.add(user.getUserId());
		params.add(groupId);
		params.add(esd);
		Object o = executeX("add_user_to_group", params);
		if (((String) o).equals("added")) {
			members.add(user);
		}
		
	}

	/**
	 * Adds a given User to this Group (old version with ignored dp argument)
	 * @param user the user you want to add to the group
	 * @param dp ignored (was used by XMLRPC)
	 */
	public void addUser(User user, String dp) {
		addUser(user);
	}

	/**
	 * Adds a given User to this Group
	 * @param user the user you want to add to the group
	 */
	public void addUser(User user) {
		try {
			JSON.Node patchJS = new JSON.Node("{}").set("users", new JSON.Node("{}")
				.set(user.getUserId(), true)
			);
			HttpClient.Response rsp = service.patchRequestWithNode("/@groups/"+groupId, patchJS);
			JSON.Node rspNode = new JSON.Node(rsp.content);
			if (rspNode != null && rspNode.get("message") != null) {
				log.info(rspNode.toJSON());
				return;
			}
			members.add(user);
		} catch (Exception ex){
			log.error(exeptionToString(ex));
		}
	}

	/**
	 * Removes a given User from this Group
	 * @param user the User you want remove from the group
	 */
	public void removeUser(User user) {
		try {
			JSON.Node patchJS = new JSON.Node("{}").set("users", new JSON.Node("{}")
				.set(user.getUserId(), false)
			);
			HttpClient.Response rsp = service.patchRequestWithNode("/@groups/"+groupId, patchJS);
			JSON.Node rspNode = new JSON.Node(rsp.content);
			if (rspNode != null && rspNode.get("message") != null) {
				log.info(rspNode.toJSON());
				return;
			}
			members.remove(user);
		} catch (Exception ex){
			log.error(exeptionToString(ex));
		}
	}
	
	public String getGroupId() {
		return groupId;
	}
	
	public String getEsd() {
		return dp;
	}
	
	public String getTitle() {
		return title;
	}
	
	public String getDescription() {
		return description;
	}

	public List<String> getAllowedDocTypes() {
		return allowedDocTypes;
	}

}