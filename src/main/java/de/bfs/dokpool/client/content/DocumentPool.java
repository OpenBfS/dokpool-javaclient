package de.bfs.dokpool.client.content;

import java.util.*;

import org.apache.xmlrpc.client.XmlRpcClient;

import de.bfs.dokpool.client.base.DocpoolBaseService;
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
	public List<DocType> getTypes() {
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
	@Deprecated public List<Scenario> getScenarios() {
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
	@Deprecated public List<Scenario> getActiveScenarios() {
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
	public List<Event> getEvents() {
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
	 * @return all active Events within this ESD
	 */
	public List<Event> getActiveEvents() {
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
	 * @return the user folder of the current user
	 */
	public Folder getUserFolder() {
		Vector<String> params = new Vector<String>();
		params.add(fullpath());
		Object[] res = (Object[])execute("get_user_folder", params);
		return new Folder(client, (String)res[0], (Object[])res[1]);
	}
	
	/**
	 * @return all group folders for the current user
	 */
	public List<Folder> getGroupFolders() {
		Vector<String> params = new Vector<String>();
		params.add(fullpath());
		Map<String, Object> folders = (Map<String, Object>)execute("get_group_folders", params);
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
		Map<String, Object> folders = (Map<String, Object>)execute("get_transfer_folders", params);
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
		Object o = execute("post_user", params);
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
		Object o = execute("post_group", params);
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
