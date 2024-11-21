package de.bfs.dokpool.client.content;

import java.util.*;

import org.apache.xmlrpc.client.XmlRpcClient;

import de.bfs.dokpool.client.base.DocpoolBaseService;
import de.bfs.dokpool.client.base.HttpClient;
import de.bfs.dokpool.client.base.JSON;
import de.bfs.dokpool.client.utils.Utils;

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
		Map<String, Object> types = Utils.queryObjects(client, fullpath(), "DocType");
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
			typeListNode = service.nodeFromGetRequest(pathAfterPlonesite + "/@types");
		} catch (Exception ex){
			log.error(exeptionToString(ex));
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
		Map<String, Object> scen = Utils.queryObjects(client, fullpath(), "ELANScenario");
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
			itemsNode = service.nodeFromGetRequest(pathAfterPlonesite + "/@search", "portal_type=ELANScenario").get("items");
		} catch (Exception ex){
			log.error(exeptionToString(ex));
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
		Map<String, Object> scen = Utils.queryObjects(client, fullpath(), "ELANScenario", filterparams);
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
			itemsNode = service.nodeFromGetRequest(pathAfterPlonesite + "/@search", "portal_type=ELANScenario&dp_type=active").get("items");
		} catch (Exception ex){
			log.error(exeptionToString(ex));
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
		Map<String, Object> events = Utils.queryObjects(client, fullpath(), "DPEvent");
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
			itemsNode = service.nodeFromGetRequest(pathAfterPlonesite + "/contentconfig/scen/@search", "portal_type=DPEvent").get("items");
		} catch (Exception ex){
			log.error(exeptionToString(ex));
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
		Map<String, Object> events = Utils.queryObjects(client, fullpath(), "DPEvent", filterparams);
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
			itemsNode = service.nodeFromGetRequest(pathAfterPlonesite + "/contentconfig/scen/@search", "portal_type=DPEvent&dp_type=active").get("items");
		} catch (Exception ex){
			log.error(exeptionToString(ex));
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
	 * @return the user folder of the current user /content/Members/<username>
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
			JSON.Node folderNode = service.nodeFromGetRequest("/@get_user_folder" + pathAfterPlonesite);
			//be careful: While plone usually returns some json with "NotFound", this gives a plain null (and HTTP 200)
			if (folderNode.type().equals("null")) {
				return null;
			}
			//we still keep the usual check, as the endpoint behavior might change some day
			if (folderNode.get("type") != null && folderNode.get("type").toString().equals("NotFound")){
				log.info(folderNode.get("message"));
				return null;
			}
			return new Folder(service, service.pathWithoutPrefix(folderNode), folderNode.toMap());
		} catch (Exception ex){
			log.error(exeptionToString(ex));
			return null;
		}
	}

	/**
	 * Get user folders for any user. This does **not** use a special endpoint, but
	 * as the endpoint get_user_folder constructs its path with the constant
	 * "{esdpath}/content/Members/{username}", we can do the same.
	 * @return the user folder under /DocumentPool.fullpath()/content/Members/<user>
	 */
	public Folder getUserFolder(String user) {
		if (user.contains("-") && !user.contains("--")){
			user = user.replaceAll("-","--");
		}
		try {
			JSON.Node folderNode = service.nodeFromGetRequest(pathAfterPlonesite + "/content/Members/" + user);
			if (folderNode.get("type") != null && folderNode.get("type").toString().equals("NotFound")){
				log.info(folderNode.get("message"));
				return null;
			}
			return new Folder(service, service.pathWithoutPrefix(folderNode), folderNode.toMap());
		} catch (Exception ex){
			log.error(exeptionToString(ex));
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
			JSON.Node gfListNode = service.nodeFromGetRequest("/@get_group_folders" + pathAfterPlonesite);
			if (gfListNode.get("type") != null && gfListNode.get("type").toString().equals("NotFound")){
				log.info(gfListNode.get("message"));
				return null;
			}
			ArrayList<Folder> res = new ArrayList<Folder>();
			for (JSON.Node gfNode : gfListNode.get("items")) {
				res.add(new Folder(service, service.pathWithoutPrefix(gfNode), dataFromNode(gfNode)));
			}
			return res;
		} catch (Exception ex){
			log.error(exeptionToString(ex));
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
			JSON.Node gfListNode = service.nodeFromGetRequest(pathAfterPlonesite + "/content/Groups/");
			if (gfListNode.get("type") != null && gfListNode.get("type").toString().equals("NotFound")){
				log.info(gfListNode.get("message"));
				return null;
			}
			ArrayList<Folder> res = new ArrayList<Folder>();
			for (JSON.Node gfNode : gfListNode.get("items")) {
				res.add(new Folder(service, service.pathWithoutPrefix(gfNode), dataFromNode(gfNode)));
			}
			return res;
		} catch (Exception ex){
			log.error(exeptionToString(ex));
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
			JSON.Node gfListNode = service.nodeFromGetRequest("/@get_transfer_folders" + pathAfterPlonesite);
			if (gfListNode.get("type") != null && gfListNode.get("type").toString().equals("NotFound")){
				log.info(gfListNode.get("message"));
				return null;
			}
			ArrayList<Folder> res = new ArrayList<Folder>();
			for (JSON.Node tfNode : gfListNode.get("items")) {
				res.add(new Folder(service, service.pathWithoutPrefix(tfNode), dataFromNode(tfNode)));
			}
			return res;
		} catch (Exception ex){
			log.error(exeptionToString(ex));
			return null;
		}
	}
	
	public User createUserX(String userId, String password, String fullname, String esd){
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
	 * Creates a new user. Currently the dokpool dp cannot be set and is silently ignored.
	 * @return the created User.
	 */
	public User createUser(String userId, String password, String fullname, String dp) {
		try {
			JSON.Node createJS = new JSON.Node("{}")
				.set("username", userId)
				.set("email", "none@none.none")
				.set("password", password)
				.set("fullname", fullname)
				//TODO: cannot set dp from REST?
				//.set("unknown", dp)
			;
			HttpClient.Response rsp = service.postRequestWithNode("/@users", createJS);
			JSON.Node rspNode = new JSON.Node(rsp.content);
			if (rspNode.get("error") != null) {
				log.info(rspNode.toJSON());
				return null;
			}
			return new User(service, service.pathWithoutPrefix(rspNode), userId, password, fullname, dp);
		} catch (Exception ex){
			log.error(exeptionToString(ex));
			return null;
		}
	}

	/**
	 * Deletes the given user.
	 * DO NOT USE. Currently causes an error on the server side that crashes
	 * @return true if deletion succeds, false otherwise
	 */
	public boolean deleteUser(String userId) {
		try {
			HttpClient.Response rsp = service.deleteRequest("/@users/"+userId);
			JSON.Node rspNode = new JSON.Node(rsp.content);
			if (rspNode.get("type") != null && rspNode.get("type").toString().equals("NotFound")){
				log.info(rspNode.get("message"));
				return false;
			}
			return true;
		} catch (Exception ex){
			log.error(exeptionToString(ex));
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
			JSON.Node createJS = new JSON.Node("{}")
				.set("groupname", groupId)
				.set("title", title)
				.set("description", description)
				//TODO: cannot set dp from REST?
				//.set("unknown", dp)
			;
			HttpClient.Response rsp = service.postRequestWithNode("/@groups", createJS);
			JSON.Node rspNode = new JSON.Node(rsp.content);
			if (rspNode.get("error") != null) {
				log.info(rspNode.toJSON());
				return null;
			}
			return new Group(service, service.pathWithoutPrefix(rspNode), groupId, title, description, dp);
		} catch (Exception ex){
			log.error(exeptionToString(ex));
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
			HttpClient.Response rsp = service.deleteRequest("/@groups/"+groupId);
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

	public Optional<Folder> getGroupFolder(String name) {
		List<Folder> groupFolders = client != null ? getGroupFoldersX() : getGroupFolders();
		return groupFolders.stream().filter(folder -> folder.getId().equals(name)).findFirst();
	}
	
}
