package de.bfs.dokpool.client.content;

import java.util.*;

import org.apache.xmlrpc.client.XmlRpcClient;

import de.bfs.dokpool.client.base.DocpoolBaseService;
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
			itemsNode = service.nodeFromGetRequest(pathAfterPlonesite + "/contentconfig/scen").get("items");
		} catch (Exception ex){
			log.error(exeptionToString(ex));
			return null;
		}
		if (itemsNode != null) {
			ArrayList<Event> res = new ArrayList<Event>();
			for (JSON.Node eventNode : itemsNode) {
				res.add(new Event(service, service.pathWithoutPrefix(eventNode), (Object[]) null));
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
			itemsNode = service.nodeFromGetRequest(pathAfterPlonesite + "/contentconfig/scen/@search?portal_type=DPEvent&dp_type=active").get("items");
		} catch (Exception ex){
			log.error(exeptionToString(ex));
			return null;
		}
		if (itemsNode != null) {
			ArrayList<Event> res = new ArrayList<Event>();
			for (JSON.Node eventNode : itemsNode) {
				res.add(new Event(service, service.pathWithoutPrefix(eventNode), (Object[]) null));
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
	 * @return the user folder of the current user
	 */
	public Folder getUserFolder() {
		return getUserFolder(service.getUsername());
	}

	/**
	 * @return the user folder of the current user /content/Members/<username>
	 */
	public Folder getUserFolder(String username) {
		if (username.contains("-") && !username.contains("--")){
			username = username.replaceAll("-","--");
		}
		try {
			JSON.Node folderNode = service.nodeFromGetRequest(pathAfterPlonesite + "/content/Members/" + username);
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
	public List<Folder> getGroupFolders() {
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
	 * @return all transfer folders for the current user
	 */
	public List<Folder> getTransferFolders() {
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
	
	public User createUser(String userId, String password, String fullname, String esd){
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
	
	public Group createGroup(String groupId, String title, String description, String esd) {
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


	public Optional<Folder> getGroupFolder(String name) {
		List<Folder> groupFolders = getGroupFolders();
		return groupFolders.stream().filter(folder -> folder.getId().equals(name)).findFirst();
	}
	
}
