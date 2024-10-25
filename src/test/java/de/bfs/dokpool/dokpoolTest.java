/**
 * @authors fuf-ber - German Federal Office for Radiation Protection www.bfs.de
 */

package de.bfs.dokpool;

// import java.io.*;
// import java.text.DecimalFormat;
import java.util.*;
import org.junit.*;
import de.bfs.dokpool.client.base.*;
import de.bfs.dokpool.client.content.*;
import de.bfs.dokpool.client.utils.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

// import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import java.nio.file.Files;
import java.nio.file.Paths;

public class dokpoolTest {
	private Log log = LogFactory.getLog(dokpoolTest.class);

	private static final String ENCODING       = "UTF-8";
	private static final String PACKAGE        = "de.bfs.dokpool";
	private static final String PROTO          = System.getenv("DOKPOOL_PROTO");
	private static final String HOST           = System.getenv("DOKPOOL_HOST");
	private static final String PORT           = System.getenv("DOKPOOL_PORT");
	private static final String PLONESITE      = System.getenv("DOKPOOL_PLONESITE");
	private static final String USER           = System.getenv("DOKPOOL_USER");
	private static final String PW             = System.getenv("DOKPOOL_PW");
	private static final String DOCUMENTOWNER  = System.getenv("DOKPOOL_DOCUMENTOWNER");
	private static final String DOKPOOL        = System.getenv("DOKPOOL_DOKPOOL");
	private static final String GROUPFOLDER    = System.getenv("DOKPOOL_GROUPFOLDER");
	private static final String DOCID          = "java-docpool-test-doc";

	/** Die main()-Methode ist nur fuer manuelle Testzwecke */
	public static void main( String[] args ) throws Exception {

	}

	/**
	 * Test document creation, file upload and setting properties.
	 *
	 */
	@Test
	public void documentTest() throws Exception {


		log.info("URL: " + PROTO + "://" + HOST + ":" + PORT + "/" + PLONESITE + " User:" + USER + " Password:" + PW);
		DocpoolBaseService docpoolBaseService = new DocpoolBaseService(PROTO + "://" + HOST + ":" + PORT + "/" + PLONESITE, USER, PW);
		List<DocumentPool> myDocpools = docpoolBaseService.getDocumentPools();
		DocumentPool mainDocpool = docpoolBaseService.getPrimaryDocumentPool();

		log.info("Number of Dokppols: " + myDocpools.size());
		log.info("Main Dokpool: " + mainDocpool.getFolderPath());

		for (DocumentPool sDocpool : myDocpools) {
			if (sDocpool.getFolderPath().matches("/" + PLONESITE + "/" + DOKPOOL)) {
				mainDocpool = sDocpool;
				log.info("Main Dokpool is now: " + mainDocpool.getFolderPath());
				break;
			}
		}

		Folder myGroupFolder = null;
		try {
			myGroupFolder = mainDocpool.getGroupFolders().get(0);
		} catch (NullPointerException e) {
			throw new NullPointerException("Could not find any valid GroupFolder for Dokpool " + mainDocpool.getFolderPath());
		}

		log.info("Group folder path (first from Dokpool): " + myGroupFolder.getFolderPath());

		try {
			myGroupFolder = mainDocpool.getFolder(/*mainDocpool.getFolderPath() + "/*/"content/Groups/" + GROUPFOLDER);
			log.info("Group folder now set from from env: " +  myGroupFolder.getFolderPath());
		} catch (NullPointerException e) {
			log.warn("Could not find DOKPOOL_GROUPFOLDER: " + mainDocpool.getFolderPath() + "/content/Groups/" + GROUPFOLDER);
			log.info("Group folder remains: " +  myGroupFolder.getFolderPath());
		}

		Boolean docExists = false;
		try {
			myGroupFolder.getFolder(DOCID);
			log.info("Object exists: " +   myGroupFolder.getFolderPath() + "/" + DOCID);
			docExists = true;
		} catch (NullPointerException e) {
			log.info("Object does not exist: " +  myGroupFolder.getFolderPath() + "/" + DOCID);
		}
		if (docExists) {
			deleteObject(myGroupFolder, DOCID);
		}

		Map<String, Object> docProperties = new HashMap<String, Object>();
		docProperties.put("title", "JavaDocpoolTestDocument");
		docProperties.put("description", "Created by mvn test.");
		docProperties.put("text", "This is just a Test and can be deleted.");
// 		docProperties.put("docType", dt.getTextContent());
// 		docProperties.putAll(setBehaviors(myDocpool));
// 		docProperties.putAll(setSubjects());

		List<String> creatorsList = new ArrayList<String>();
		creatorsList.add(DOCUMENTOWNER);
		creatorsList.add(USER);
		docProperties.put("creators", creatorsList);

		log.info("Creating new document at " + myGroupFolder.getFolderPath() + "/" + DOCID);
		Document d = myGroupFolder.createDPDocument(DOCID, docProperties);

		byte[] fileData = Files.readAllBytes(Paths.get("README"));
		d.uploadFile("readme", "Read me!", "A file you should read.", fileData, "README.txt");
		byte[] imageData = Files.readAllBytes(Paths.get("src/test/resources/image.png"));
		d.uploadImage("image", "Look at me!", "An image you should look at.", imageData, "image.png");

		//TODO: needed? What does this do?
		d.autocreateSubdocuments();

		d.setWorkflowStatus("publish");

	}


	public void deleteObject(Folder folder, String objId) throws Exception {
			log.info("Trying to delete "+folder.getFolderPath() + "/" + objId);
			Field clientField = Class.forName("de.bfs.dokpool.client.base.BaseObject").getDeclaredField("client");
			clientField.setAccessible(true);
			XmlRpcClient client = (XmlRpcClient) clientField.get(folder);
			Vector<String[]> delParams = new Vector<String[]>();
			delParams.add(new String[]{folder.getFolderPath() + "/" + objId});
			Object[] res = (Object[]) Utils.execute(client, "delete_object", delParams);
	}


	/**
	 * old main Test method
	 *
	 * @throws Exception
	 */
	@Test
	public void miscTest() throws Exception {
		log.info("URL: " + PROTO + "://" + HOST + ":" + PORT + "/" + PLONESITE + " User:" + USER + " Password:" + PW);
		DocpoolBaseService docpoolBaseService = new DocpoolBaseService(PROTO + "://" + HOST + ":" + PORT + "/" + PLONESITE, USER, PW);

		List<DocumentPool> documentpools = docpoolBaseService.getDocumentPools();
		if (documentpools.size() < 1) {
			log.warn("No DocumentPools found!");
		}

		Method gdPField = Class.forName("de.bfs.dokpool.client.base.DocpoolBaseService").getDeclaredMethod("getDocumentPool",Class.forName("java.lang.String"));
		gdPField.setAccessible(true);
		Optional<DocumentPool> myDocumentPool = (Optional<DocumentPool>) gdPField.invoke(docpoolBaseService,DOKPOOL);
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
		Optional<Folder> groupFolder = myDocumentPool.get().getGroupFolder(GROUPFOLDER);
		if (!groupFolder.isPresent()) {
			return;
		}
		Random r = new Random();
		log.info(groupFolder.get());
// 		List<Object> documents = groupFolder.get().getContents(null);
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
// 		System.out.println(myDocumentPool.get().path);
// 		User user = myDocumentPool.get().createUser("testuserxml", "testuserxml", "XMLTESTER", myDocumentPool.get().path);
// 		if (user == null) {
// 			log.error("Kein Nutzer angelegt!");
// 		} else {
// 			log.info("Nutzer " + user.getUserId() + " angelegt.");
// 		}
// 		Group group = myDocumentPool.get().createGroup("groupxml", "GroupXML", "Fuer XMLRPC", myDocumentPool.get().path);
// 		if (group == null) {
// 			log.error("Keine Gruppe angelegt.");
// 		} else {
// 			log.info("Gruppe " + group.getGroupId() + " angelegt.");
// 		}
// 		// user.addToGroup(group);
// 		group.addUser(user, myDocumentPool.get().path);
// 		String[] docTypes = { "airactivity", "ifinprojection", "protectiveactions" };
// 		group.setAllowedDocTypes(docTypes);
// 		List<String> gDoctypes = group.getAllowedDocTypes();
// 		log.info("docTypes " + docTypes);
// 		log.info("gDocTypes " + gDoctypes);
// 		if (gDoctypes != null && gDoctypes.equals(Arrays.asList(docTypes))) {
// 			log.info("Gruppenproperties erfolgreich angepasst.");
// 		} else {
// 			log.error("Fehler bei der Anpassung der Gruppenproperties.");
// 		}

	}

	/**
	 * Test user and group  handling.
	 *
	 */
	@Test
	public void userManagementTest() throws Exception {
		Assert.assertEquals(5,5);
	}

	/**
	 * not used
	 */
	static class HelperClass {
		public List<Object> elemente = new ArrayList<Object>();
	}
}

