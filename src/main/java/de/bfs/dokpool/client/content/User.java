package de.bfs.dokpool.client.content;

import java.util.Map;

import org.apache.xmlrpc.client.XmlRpcClient;

import de.bfs.dokpool.client.base.BaseObject;
import de.bfs.dokpool.client.base.DocpoolBaseService;

public class User extends BaseObject {
	private String userId = "";
	private String fullname = "";
	private String dp = "";
	
	protected User(XmlRpcClient client, String path, String userId, String password, String fullname, String dp) {
		super(client, path, null);
		this.userId = userId;
		this.fullname = fullname;
		this.dp = dp;
	}

	public User(DocpoolBaseService service, String path, String userId, String password, String fullname, String dp) {
		super(service, path, (Object[])null);
		this.userId = userId;
		this.fullname = fullname;
		this.dp = dp;
	}
	
	public void addToGroup(Group group) {
		/*Vector<String> params = new Vector<String>();
		params.add(this.userId);
		params.add(group.getGroupId());
		Object o = execute("add_user_to_group", params);*/
		group.addUser(this,this.fullpath());
	}
	
	public String getUserId() {
		return this.userId;
	}
	
	public String getFullname() {
		return this.fullname;
	}
	
	public String getEsd() {
		return  this.dp;
	}
}