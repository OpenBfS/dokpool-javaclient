package de.bfs.dokpool.client;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.apache.xmlrpc.client.XmlRpcClient;

/**
 * Wraps the ELANESD type
 *
 */
public class DocumentPool extends Folder {
	
	public DocumentPool(XmlRpcClient client, String path, Object[] data) {
		super(client, path, data);
	}

	
	/**
	 * @return all DocTypes within this ESD
	 */
	public List<DocType> getTypes() {
		Map<String, Object> types = Utils.queryObjects(client, path, "DocType");
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
	 */
	public List<Scenario> getScenarios() {
		Map<String, Object> scen = Utils.queryObjects(client, path, "ELANScenario");
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
	 * @return filtered Scenarios within this ESD
	 */
	public List<Scenario> getScenarios(List<String> filter) {
		Map<String, Object> scen = Utils.queryObjects(client, path, "ELANScenario");
		if (scen != null) {
			ArrayList<Scenario> res = new ArrayList<Scenario>();
			for (String path: scen.keySet()) {
				Scenario myscen = new Scenario(client, path, null);
				if (filter.size() > 0 || filter.contains("myscen.status")) {
					res.add(myscen);
				}
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
		params.add(path);
		Object[] res = (Object[])execute("get_user_folder", params);
		return new Folder(client, (String)res[0], (Object[])res[1]);
	}
	
	/**
	 * @return all group folders for the current user
	 */
	public List<Folder> getGroupFolders() {
		Vector<String> params = new Vector<String>();
		params.add(path);
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
		params.add(path);
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
			user = new User(client, path, userId, password, fullname, esd);
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
			group = new Group(client, path, groupId, title, description, esd);
		}
		return group;
	}

	/**
	 * Create a new scenario within this esd.
	 *
	 * @param id: the id for the scenario (must be unique within the folder)
	 * @param title
	 * @param timeOfEvent
	 * @param description
	 * @return the newly created document
	 */
	public Scenario createScenario(String id, String title, String description, String timeOfEvent) {
		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put("title",title);
		properties.put("timeOfEvent", timeOfEvent);
		properties.put("description",description);
		properties.put("docType","ELANScenario");
		return createScenario(id, properties);
	}

	public Scenario createScenario(String id, Map<String, Object> properties){
		Vector<Object> params = new Vector<Object>();
		params.add(path);
		params.add(id);
		params.add(properties);
		params.add("ELANScenario");
		String newpath = (String)execute("create_scenario", params);
		return new Scenario(client, newpath, null);
	}

}
