package de.bfs.dokpool.client.content;

import java.util.*;

import org.apache.xmlrpc.client.XmlRpcClient;

import de.bfs.dokpool.client.base.DocpoolBaseService;
import de.bfs.dokpool.client.base.JSON;

/**
 * Wraps the ELANESD type
 *
 */
public class DocumentPool extends Folder {
	
	public DocumentPool(XmlRpcClient client, String path, Object[] data) {
		super(client, path, data);
	}

	public DocumentPool(DocpoolBaseService service, String path, Object[] data) {
		super(service, path, data);
	}

	public DocumentPool(DocpoolBaseService service, String path, Map<String,Object> data) {
		super(service, path, data);
	}

	
	/**
	 * @return all DocTypes within this ESD
	 */
	public List<DocType> getTypesX() {
		Map<String, Object> types = DocpoolBaseService.queryObjects(client, fullpath(), "DocType");
		if (types != null) {
			ArrayList<DocType> res = new ArrayList<DocType>();
			for (String path: types.keySet()) {
				res.add(new DocType(client, path, null));
			}
			return res;
		} else {
			return null;
		}
	}

	/**
	 * @return all DocTypes within this ESD
	 */
	public List<DocType> getTypes() {
		JSON.Node typeListNode = null;
		try {
			typeListNode = privateService.nodeFromGetRequest(pathAfterPlonesite + "/@types");
			if (typeListNode.errorInfo != null) {
				log.info(typeListNode.errorInfo.toString());
				return null;
			}
		} catch (Exception ex) {
			log.error(exceptionToString(ex));
			return null;
		}
		if (typeListNode != null) {
			ArrayList<DocType> res = new ArrayList<DocType>();
			for (JSON.Node typeNode : typeListNode) {
				res.add(new DocType(service, service.pathWithoutPrefix(typeNode), dataFromNode(typeNode)));
			}
			return res;
		} else {
			return null;
		}
	}
	
	/**
	 * @return all Scenarios within this ESD
	 * @deprecated
	 */
	@Deprecated public List<Scenario> getScenariosX() {
		Map<String, Object> scen = DocpoolBaseService.queryObjects(client, fullpath(), "ELANScenario");
		if (scen != null) {
			ArrayList<Scenario> res = new ArrayList<Scenario>();
			for (String path: scen.keySet()) {
				res.add(new Scenario(client, path, null));
			}
			return res;
		} else {
			return null;
		}
	}

	/**
	 * @return all Scenarios within this ESD
	 * @deprecated
	 */
	@Deprecated public List<Scenario> getScenarios() {
		JSON.Node itemsNode = null;
		try {
			//TODO: only search /contentconfig/scen/?
			JSON.Node rspNode = privateService.nodeFromGetRequest(pathAfterPlonesite + "/@search", "portal_type=ELANScenario&metadata_fields=id");
			if (rspNode.errorInfo != null) {
				log.info(rspNode.errorInfo.toString());
				return null;
			}
			itemsNode = rspNode.get("items");
		} catch (Exception ex) {
			log.error(exceptionToString(ex));
			return null;
		}
		if (itemsNode != null) {
			ArrayList<Scenario> res = new ArrayList<Scenario>();
			for (JSON.Node scenarioNode : itemsNode) {
				res.add(new Scenario(service, service.pathWithoutPrefix(scenarioNode), dataFromNode(scenarioNode)));
			}
			return res;
		} else {
			return null;
		}
	}

	/**
	 * @return all active Scenarios within this ESD
	 * @deprecated
	 */
	@Deprecated public List<Scenario> getActiveScenariosX() {
		HashMap<String, String> filterparams = new HashMap<String, String>();
		filterparams.put("dp_type", "active");
		Map<String, Object> scen = DocpoolBaseService.queryObjects(client, fullpath(), "ELANScenario", filterparams);
		if (scen != null) {
			ArrayList<Scenario> res = new ArrayList<Scenario>();
			for (String path: scen.keySet()) {
				res.add(new Scenario(client, path, null));
			}
			return res;
		} else {
			return null;
		}
	}

	/**
	 * @return all active Scenarios within this ESD
	 * @deprecated
	 */
	@Deprecated public List<Scenario> getActiveScenarios() {
		JSON.Node itemsNode = null;
		try {
			//TODO: only search /contentconfig/scen/?
			JSON.Node rspNode = privateService.nodeFromGetRequest(pathAfterPlonesite + "/@search", "portal_type=ELANScenario&dp_type=active&metadata_fields=id");
			if (rspNode.errorInfo != null) {
				log.info(rspNode.errorInfo.toString());
				return null;
			}
			itemsNode = rspNode.get("items");
		} catch (Exception ex) {
			log.error(exceptionToString(ex));
			return null;
		}
		if (itemsNode != null) {
			ArrayList<Scenario> res = new ArrayList<Scenario>();
			for (JSON.Node scenarioNode : itemsNode) {
				res.add(new Scenario(service, service.pathWithoutPrefix(scenarioNode), dataFromNode(scenarioNode)));
			}
			return res;
		} else {
			return null;
		}
	}

	/**
	 * @return all Events within this ESD
	 */
	public List<Event> getEventsX() {
		Map<String, Object> events = DocpoolBaseService.queryObjects(client, fullpath(), "DPEvent");
		if (events != null) {
			ArrayList<Event> res = new ArrayList<Event>();
			for (String path: events.keySet()) {
				res.add(new Event(client, path, null));
			}
			return res;
		} else {
			return null;
		}
	}

	/**
	 * @return all Events within this ESD
	 */
	public List<Event> getEvents() {
		JSON.Node itemsNode = null;
		try {
			JSON.Node rspNode = privateService.nodeFromGetRequest(pathAfterPlonesite + "/contentconfig/scen/@search", "portal_type=DPEvent&metadata_fields=id");
			if (rspNode.errorInfo != null) {
				log.info(rspNode.errorInfo.toString());
				return null;
			}
			itemsNode = rspNode.get("items");
		} catch (Exception ex) {
			log.error(exceptionToString(ex));
			return null;
		}
		if (itemsNode != null) {
			ArrayList<Event> res = new ArrayList<Event>();
			for (JSON.Node eventNode : itemsNode) {
				res.add(new Event(service, service.pathWithoutPrefix(eventNode), dataFromNode(eventNode)));
			}
			return res;
		} else {
			return null;
		}
	}

	/**
	 * @return all active Events within this ESD
	 */
	public List<Event> getActiveEventsX() {
		HashMap<String, String> filterparams = new HashMap<String, String>();
		filterparams.put("dp_type", "active");
		Map<String, Object> events = DocpoolBaseService.queryObjects(client, fullpath(), "DPEvent", filterparams);
		if (events != null) {
			ArrayList<Event> res = new ArrayList<Event>();
			for (String path: events.keySet()) {
				res.add(new Event(client, path, null));
			}
			return res;
		} else {
			return null;
		}
	}

	/**
	 * @return all Events within this ESD
	 */
	public List<Event> getActiveEvents() {
		JSON.Node itemsNode = null;
		try {
			JSON.Node rspNode = privateService.nodeFromGetRequest(pathAfterPlonesite + "/contentconfig/scen/@search", "portal_type=DPEvent&dp_type=active&metadata_fields=id");
			if (rspNode.errorInfo != null) {
				log.info(rspNode.errorInfo.toString());
				return null;
			}
			itemsNode = rspNode.get("items");
		} catch (Exception ex) {
			log.error(exceptionToString(ex));
			return null;
		}
		if (itemsNode != null) {
			ArrayList<Event> res = new ArrayList<Event>();
			for (JSON.Node eventNode : itemsNode) {
				res.add(new Event(service, service.pathWithoutPrefix(eventNode), dataFromNode(eventNode)));
			}
			return res;
		} else {
			return null;
		}
	}

	/**
	 * @return the user folder of the current user /content/Members/&lt;username&gt;
	 */
	public Folder getUserFolderX() {
		Vector<String> params = new Vector<String>();
		params.add(fullpath());
		Object[] res = (Object[])executeX("get_user_folder", params);
		return new Folder(client, (String)res[0], (Object[])res[1]);
	}

	/**
	 * @return the user folder of the current user (via @get_user_folder)
	 */
	public Folder getUserFolder() {
		try {
			//the Dokpool is an argument to the endpoint, so we append ist
			JSON.Node folderNode = privateService.nodeFromGetRequest("/@get_user_folder" + pathAfterPlonesite);
			if (folderNode.errorInfo != null) {
				log.info(folderNode.errorInfo.toString());
				return null;
			}
			return new Folder(service, service.pathWithoutPrefix(folderNode), folderNode.toMap());
		} catch (Exception ex) {
			log.error(exceptionToString(ex));
			return null;
		}
	}

	/**
	 * Get user folders for any user. This does **not** use a special endpoint, but
	 * as the endpoint get_user_folder constructs its path with the constant
	 * "{esdpath}/content/Members/{username}", we can do the same.
	 * @return the user folder under /DocumentPool.fullpath()/content/Members/&lt;user&gt;
	 */
	public Folder getUserFolder(String user) {
		if (user.contains("-") && !user.contains("--")) {
			user = user.replaceAll("-","--");
		}
		try {
			JSON.Node folderNode = privateService.nodeFromGetRequest(pathAfterPlonesite + "/content/Members/" + user);
			if (folderNode.errorInfo != null) {
				log.info(folderNode.errorInfo.toString());
				return null;
			}
			return new Folder(service, service.pathWithoutPrefix(folderNode), folderNode.toMap());
		} catch (Exception ex) {
			log.error(exceptionToString(ex));
			return null;
		}
	}

	
	
	/**
	 * @return all group folders for the current user
	 */
	public List<Folder> getGroupFoldersX() {
		Vector<String> params = new Vector<String>();
		params.add(fullpath());
		Map<String, Object> folders = (Map<String, Object>)executeX("get_group_folders", params);
		if (folders != null) {
			ArrayList<Folder> res = new ArrayList<Folder>();
			for (String path: folders.keySet()) {
				res.add(new Folder(client, path, null));
			}
			return res;
		} else {
			return null;
		}		
	}

	/**
	 * Get the current user's group folders.
	 * The corresponding API endpoint get_group_folders gets all groups for the current user
	 * and searches for folder with theier ids in /DocumentPool.fullpath()/content/Groups.
	 * @return all group folders for the current user
	 */
	public List<Folder> getGroupFolders() {
		try {
			//the Dokpool is an argument to the endpoint, so we append ist
			JSON.Node gfListNode = privateService.nodeFromGetRequest("/@get_group_folders" + pathAfterPlonesite, "metadata_fields=id");
			if (gfListNode.errorInfo != null) {
				log.info(gfListNode.errorInfo.toString());
				return null;
			}
			ArrayList<Folder> res = new ArrayList<Folder>();
			for (JSON.Node gfNode : gfListNode.get("items")) {
				res.add(new Folder(service, service.pathWithoutPrefix(gfNode), dataFromNode(gfNode)));
			}
			return res;
		} catch (Exception ex) {
			log.error(exceptionToString(ex));
			return null;
		}
	}

	/**
	 * This method returns all group folders, irrespective of the current users membership
	 * within any of the returned groups (via. DocumentPool.fullpath()//content/Groups/).
	 * @return all group folders of the current dokpool.
	 */
	public List<Folder> getAllGroupFolders() {
		try {
			JSON.Node gfListNode = privateService.nodeFromGetRequest(pathAfterPlonesite + "/content/Groups/", "metadata_fields=id");
			if (gfListNode.errorInfo != null) {
				log.info(gfListNode.errorInfo.toString());
				return null;
			}
			ArrayList<Folder> res = new ArrayList<Folder>();
			for (JSON.Node gfNode : gfListNode.get("items")) {
				res.add(new Folder(service, service.pathWithoutPrefix(gfNode), dataFromNode(gfNode)));
			}
			return res;
		} catch (Exception ex) {
			log.error(exceptionToString(ex));
			return null;
		}
	}

	/**
	 * @return all transfer folders for the current user
	 */
	public List<Folder> getTransferFoldersX() {
		Vector<String> params = new Vector<String>();
		params.add(fullpath());
		Map<String, Object> folders = (Map<String, Object>)executeX("get_transfer_folders", params);
		if (folders != null) {
			ArrayList<Folder> res = new ArrayList<Folder>();
			for (String path: folders.keySet()) {
				res.add(new Folder(client, path, null));
			}
			return res;
		} else {
			return null;
		}		
	}

	/**
	 * @return all transfer folders for this Dokpool.
	 */
	public List<Folder> getTransferFolders() {
		try {
			//the Dokpool is an argument to the endpoint, so we append ist
			JSON.Node tfListNode = privateService.nodeFromGetRequest("/@get_transfer_folders" + pathAfterPlonesite, "metadata_fields=id");
			if (tfListNode.errorInfo != null) {
				log.info(tfListNode.errorInfo.toString());
				return null;
			}
			ArrayList<Folder> res = new ArrayList<Folder>();
			for (JSON.Node tfNode : tfListNode.get("items")) {
				res.add(new Folder(service, service.pathWithoutPrefix(tfNode), dataFromNode(tfNode)));
			}
			return res;
		} catch (Exception ex) {
			log.error(exceptionToString(ex));
			return null;
		}
	}
	
	public User createUserX(String userId, String password, String fullname, String esd) {
		User user = null;
		Vector<String> params = new Vector<String>();
		params.add(userId);
		params.add(password);
		params.add(fullname);
		params.add(esd);
		Object o = executeX("post_user", params);
		if (((String) o).equals(userId)) {
			user = new User(client, fullpath(), userId, password, fullname, esd);
		}
		return user;
	}

	/**
	 * Creates a new user.
	 * @param dp the primary dokpool for the new user (e.g. "hessen")
	 * @return the created User.
	 */
	public User createUser(String userId, String password, String fullname, String dp, String email) {
		try {
			String dpUid = null;
			if (dp != null && !dp.equals("")) {
				dp = dp.startsWith("/") ? dp : "/"+dp;
				DocumentPool docPool = new DocumentPool(service, dp, (Object[]) null);
				dpUid = docPool.getStringAttribute("UID");
			}
			JSON.Node createJS = new JSON.Node("{}")
				.set("username", userId)
				.set("email", email)
				.set("password", password)
				.set("fullname", fullname)
				.set("dp", dpUid)
			;
			JSON.Node rspNode = privateService.postRequestWithNode("/@users", createJS);
			if (rspNode.errorInfo != null) {
				log.info(rspNode.errorInfo.toString());
				return null;
			}
			return new User(service, service.pathWithoutPrefix(rspNode), userId, password, fullname, dp);
		} catch (Exception ex) {
			log.error(exceptionToString(ex));
			return null;
		}
	}

	/**
	 * Creates a new user.
	 * 
	 * This version sets the email address to "ihotline@bfs.de".
	 * @param dp the primary dokpool for the new user (e.g. "hessen").
	 * @deprecated Please also provide an actual email adress if you can.
	 * @return the created User.
	 */
	@Deprecated
	public User createUser(String userId, String password, String fullname, String dp) {
		return createUser(userId, password, fullname, dp, "ihotline@bfs.de");
	}

	/**
	 * Deletes the given user.
	 * DO NOT USE. Currently causes an error on the server side that crashes
	 * @return true if deletion succeds, false otherwise
	 */
	public boolean deleteUser(String userId) {
		try {
			JSON.Node rspNode = privateService.deleteRequest("/@users/"+userId);
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
	
	public Group createGroupX(String groupId, String title, String description, String esd) {
		Group group = null;
		Vector<String> params = new Vector<String>();
		params.add(groupId);
		params.add(title);
		params.add(description);
		params.add(esd);
		Object o = executeX("post_group", params);
		System.out.println((String) o+"  "+groupId);
		if (((String) o).equals(groupId)) {
			group = new Group(client, fullpath(), groupId, title, description, esd);
		}
		return group;
	}

	/**
	 * Creates a new user. Currently the dokpool dp cannot be set and is silently ignored.
	 * @return the created User.
	 */
	public Group createGroup(String groupId, String title, String description, String dp) {
		try {
			String dpUid = null;
			if (dp != null && !dp.equals("")) {
				dp = dp.startsWith("/") ? dp : "/"+dp;
				DocumentPool docPool = new DocumentPool(service, dp, (Object[]) null);
				dpUid = docPool.getStringAttribute("UID");
			}
			JSON.Node createJS = new JSON.Node("{}")
				.set("groupname", groupId)
				.set("title", title)
				.set("description", description)
				//TODO: can wie set dp from REST? Is it used?
				//the following line has to visible effect
				// .set("dp", dpUid)
			;
			JSON.Node rspNode = privateService.postRequestWithNode("/@groups", createJS);
			if (rspNode.errorInfo != null) {
				log.info(rspNode.errorInfo.toString());
				return null;
			}
			return new Group(service, service.pathWithoutPrefix(rspNode), groupId, title, description, dp);
		} catch (Exception ex) {
			log.error(exceptionToString(ex));
			return null;
		}
	}

	/**
	 * Deletes the given group.
	 * This methods actually works (but see deleteUser).
	 * @return true if deletion succeds, false otherwise
	 */
	public boolean deleteGroup(String groupId) {
		try {
			JSON.Node rspNode = privateService.deleteRequest("/@groups/"+groupId);
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

	public Optional<Folder> getGroupFolder(String name) {
		List<Folder> groupFolders = client != null ? getGroupFoldersX() : getGroupFolders();
		return groupFolders.stream().filter(folder -> folder.getId().equals(name)).findFirst();
	}
	
}
