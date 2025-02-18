package de.bfs.dokpool.client.content;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.bfs.dokpool.client.base.BaseObject;
import de.bfs.dokpool.client.base.DocpoolBaseService;
import de.bfs.dokpool.client.base.JSON;

public class Group extends BaseObject {
	
	private String groupId = "";
	private String dp = "";
	private String title = "";
	private String description = "";
	private Set<User> members = new HashSet<User>();
	private List<String> allowedDocTypes;

	protected Group(DocpoolBaseService service, String path, String groupId, String title, String description, String dp) {
		super(service, path, (Object[])null);
		this.groupId = groupId;
		this.title = title;
		this.description = description;
		this.dp = dp;
	}

	/**
	 * NOT IMPLEMENTED.
	 * @param doctypes
	 */
	public void setAllowedDocTypes(String[] doctypes) {
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
			JSON.Node rspNode = privateService.patchRequestWithNode("/@groups/"+groupId, patchJS);
			if (rspNode.errorInfo != null) {
				log.info(rspNode.errorInfo.toString());
				return;
			}
			members.add(user);
		} catch (Exception ex) {
			log.error(exceptionToString(ex));
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
			JSON.Node rspNode = privateService.patchRequestWithNode("/@groups/"+groupId, patchJS);
			if (rspNode.errorInfo != null) {
				log.info(rspNode.errorInfo.toString());
				return;
			}
			members.remove(user);
		} catch (Exception ex) {
			log.error(exceptionToString(ex));
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