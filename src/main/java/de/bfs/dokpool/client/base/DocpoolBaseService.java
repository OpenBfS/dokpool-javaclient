package de.bfs.dokpool.client.base;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Vector;

import org.apache.xmlrpc.client.XmlRpcClient;

import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

import de.bfs.dokpool.client.content.DocType;
import de.bfs.dokpool.client.content.Document;
import de.bfs.dokpool.client.content.DocumentPool;
import de.bfs.dokpool.client.content.Folder;
import de.bfs.dokpool.client.content.Group;
import de.bfs.dokpool.client.content.User;
import de.bfs.dokpool.client.utils.Utils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The root class to access XMLRPC services for DOKPOOL.
 *
 */
public class DocpoolBaseService {
	private Log log = LogFactory.getLog(DocpoolBaseService.class);
	private XmlRpcClient client = null;

	/**
	 * Get a service object.
	 * 
	 * @param url:
	 *            the address of the ELAN instance root
	 * @param username
	 * @param password
	 */
	public DocpoolBaseService(String url, String username, String password) {
		XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();

		// you can now do authenticated XML-RPC calls with the proxy
		try {
			URL serverurl = new URL(url);
			config.setServerURL(serverurl);
			config.setBasicUserName(username);
			config.setBasicPassword(password);
			client = new XmlRpcClient();
			// concatenate and base64 encode the username and password (suitable for use in
			// HTTP Basic Authentication)
			// final String auth =
			// javax.xml.bind.DatatypeConverter.printBase64Binary((username + ":" +
			// password).getBytes());
			// set the HTTP Header for Basic Authentication
			// client.setRequestProperty("Authorization", "Basic " + auth);
			client.setConfig(config);
			client.setTypeFactory(new DocpoolBaseTypeFactory(client));
		} catch (MalformedURLException e) {
			log.fatal("Incorrect URL provided!", e);
		}
	}

	/**
	 * Get all ESDs available to the current user.
	 * 
	 * @return
	 */
	public List<DocumentPool> getDocumentPools() {
		Map<String, Object> esds = Utils.queryObjects(this.client, "/", "DocumentPool");
		if (esds != null) {
			ArrayList<DocumentPool> res = new ArrayList<DocumentPool>();
			for (String path : esds.keySet()) {
				res.add(new DocumentPool(client, path, null));
			}
			return res;
		} else {
			return null;
		}
	}

	/**
	 * @return the ESD, which the user is a member of - or the first available ESD
	 *         for global users
	 */
	public DocumentPool getPrimaryDocumentPool() {
		Object[] res = (Object[]) Utils.execute(client, "get_primary_documentpool", new Vector());
		log.info(res.length);
		return new DocumentPool(client, (String) res[0], (Object[]) res[1]);
	}

	/**
	 * Test method
	 * 
	 * @param args:unused
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		System.out.println("starting connection test");
		Log log = LogFactory.getLog(DocpoolBaseService.class);
		DocpoolBaseService baseService;

		if (args.length > 2) {
			baseService = new DocpoolBaseService(args[0], args[1], args[2]);
		} else {
			baseService = new DocpoolBaseService("http://localhost:8081/dokpool", "elanmanager", "admin");
		}
		List<DocumentPool> documentpools = baseService.getDocumentPools();
		if (documentpools.size() < 1) {
			log.warn("No DocumentPools found!");
		}
		
		Optional<DocumentPool> myDocumentPool = baseService.getDocumentPool("bund");
		if (!myDocumentPool.isPresent()) {
			return;
		}
		log.info(myDocumentPool.get().getTitle());
		log.info(myDocumentPool.get().getDescription());
		List<DocType> types = myDocumentPool.get().getTypes();
		for (DocType t : types) {
			log.info(t.getId());
			log.info(t.getTitle());
		}
		Optional<Folder> groupFolder = myDocumentPool.get().getGroupFolder("bund_e-at");
		if (!groupFolder.isPresent()) {
			return;
		}
		Random r = new Random();
		List<Object> documents = groupFolder.get().getContents(null);
		log.info(groupFolder.get().getTitle());
		List<Folder> tf = myDocumentPool.get().getTransferFolders();
		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put("title", "Generischer Titel");
		properties.put("description", "Generische Beschreibung");
		properties.put("text", "<b>Text</b>");
		properties.put("docType", "ifinprojection");
		properties.put("subjects", new String[] { "Tag1", "Tag2" });
		properties.put("local_behaviors", new String[] { "elan" });
		BaseObject bo = groupFolder.get().createObject("generisch" + r.nextInt(), properties, "DPDocument");
		properties.clear();
		properties.put("scenarios", new String[] { "scenario1", "scenario2" });
		bo.update(properties);
		log.info(bo.getStringAttribute("created_by"));
		log.info(bo.getDateAttribute("effective"));
		
		Map<String, Object> elanProperties = new HashMap<String, Object>();
		elanProperties.put("scenarios", new String[] { "demo-am-24-4" });
		Map<String, Object> rodosProperties = new HashMap<String, Object>();
		rodosProperties.put("reportId", "REPORT");
		Document d = groupFolder.get().createAppSpecificDocument("ausjava" + r.nextInt(), "Neu aus Java",
				"Beschreibung über Java", "<p>Text aus Java!</p>", "ifinprojection", new String[] { "elan", "rodos" },
				elanProperties,
				null,
				rodosProperties,				
				null
				);
		log.info(d.getTitle());
//		java.io.File file = new java.io.File("test.pdf");
//		d.uploadFile("neue_datei", "Neue Datei", "Datei Beschreibung", FileUtils.readFileToByteArray(file), "test.pdf");
//		file = new java.io.File("test.jpg");
//		d.uploadImage("neues_bild", "Neues Bild", "Bild Beschreibung", FileUtils.readFileToByteArray(file), "test.jpg");
		log.info(d.getWorkflowStatus());
		log.info(d.getStringsAttribute("local_behaviors"));
		System.out.println(myDocumentPool.get().path);
		User user = myDocumentPool.get().createUser("testuserxml", "testuserxml", "XMLTESTER", myDocumentPool.get().path);
		if (user == null) {
			log.error("Kein Nutzer angelegt!");
		} else {
			log.info("Nutzer " + user.getUserId() + " angelegt.");
		}
		Group group = myDocumentPool.get().createGroup("groupxml", "GroupXML", "Fuer XMLRPC", myDocumentPool.get().path);
		if (group == null) {
			log.error("Keine Gruppe angelegt.");
		} else {
			log.info("Gruppe " + group.getGroupId() + " angelegt.");
		}
		// user.addToGroup(group);
		group.addUser(user, myDocumentPool.get().path);
		String[] docTypes = { "airactivity", "ifinprojection", "protectiveactions" };
		group.setAllowedDocTypes(docTypes);
		List<String> gDoctypes = group.getAllowedDocTypes();
		log.info("docTypes " + docTypes);
		log.info("gDocTypes " + gDoctypes);
		if (gDoctypes != null && gDoctypes.equals(Arrays.asList(docTypes))) {
			log.info("Gruppenproperties erfolgreich angepasst.");
		} else {
			log.error("Fehler bei der Anpassung der Gruppenproperties.");
		}

	}

	private Optional<DocumentPool> getDocumentPool(String name) {
		List<DocumentPool> documentPools = getDocumentPools();
		return documentPools.stream().filter(pool -> pool.getId().equals(name)).findFirst();
	}

}
