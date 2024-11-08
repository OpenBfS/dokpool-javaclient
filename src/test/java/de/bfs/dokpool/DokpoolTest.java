/**
 * @authors fuf-ber - German Federal Office for Radiation Protection www.bfs.de
 */

package de.bfs.dokpool;

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

public class DokpoolTest {
	private static final Log log = LogFactory.getLog(DokpoolTest.class);

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


	public static DocumentPool obtainDocumentPool() throws Exception {
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

		return mainDocpool;
	}

	public static void deleteObject(Folder folder, String objId) throws Exception {
		log.info("Trying to delete "+folder.getFolderPath() + "/" + objId);
		Field clientField = Class.forName("de.bfs.dokpool.client.base.BaseObject").getDeclaredField("client");
		clientField.setAccessible(true);
		XmlRpcClient client = (XmlRpcClient) clientField.get(folder);
		Vector<String[]> delParams = new Vector<String[]>();
		delParams.add(new String[]{folder.getFolderPath() + "/" + objId});
		Utils.execute(client, "delete_object", delParams);
	}

	public static String extractPath(BaseObject bo) throws Exception {
		Field pathField = Class.forName("de.bfs.dokpool.client.base.BaseObject").getDeclaredField("path");
		pathField.setAccessible(true);
		return (String) pathField.get(bo);
	}

	/**
	 * Test document creation, file upload and setting properties.
	 *
	 */
	@Test
	public void documentTest() throws Exception {
		log.info("=== TEST: documentTest ======");
		DocumentPool mainDocpool = obtainDocumentPool();

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

		boolean docExists = false;
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


	/**
	 * Document handling parts of old main Test method.
	 *
	 * @throws Exception
	 */
	@Test
	public void miscObjectTest() throws Exception {
		log.info("=== TEST: miscObjectTest ======");
		log.info("URL: " + PROTO + "://" + HOST + ":" + PORT + "/" + PLONESITE + " User:" + USER + " Password:" + PW);
		DocpoolBaseService docpoolBaseService = new DocpoolBaseService(PROTO + "://" + HOST + ":" + PORT + "/" + PLONESITE, USER, PW);

		List<DocumentPool> documentpools = docpoolBaseService.getDocumentPools();
		if (documentpools.isEmpty()) {
			log.warn("No DocumentPools found!");
		}

		Method gdPField = Class.forName("de.bfs.dokpool.client.base.DocpoolBaseService").getDeclaredMethod("getDocumentPool",Class.forName("java.lang.String"));
		gdPField.setAccessible(true);
		DocumentPool myDocumentPool = ((Optional<DocumentPool>) gdPField.invoke(docpoolBaseService,DOKPOOL)).get();
		log.info(myDocumentPool.getTitle());
		log.info(myDocumentPool.getDescription());
		List<DocType> types = myDocumentPool.getTypes();
		for (DocType t : types) {
			log.info(t.getId());
			log.info(t.getTitle());
		}

		Folder groupFolder = myDocumentPool.getGroupFolder(GROUPFOLDER).get();
		Random r = new Random();
		log.info(groupFolder);
		log.info(groupFolder.getTitle());
		List<Object> documents = groupFolder.getContents(null);
		List<Folder> tf = myDocumentPool.getTransferFolders();
		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put("title", "Generic Title");
		properties.put("description", "Generic Description");
		properties.put("text", "<b>Text</b>");
		properties.put("docType", "ifinprojection");
		properties.put("subjects", new String[] { "Tag1", "Tag2" });
		properties.put("local_behaviors", new String[] { "elan" });
		String randId = "generic" + r.nextInt();
		BaseObject bo = groupFolder.createObject(randId, properties, "DPDocument");
		properties.clear();
		properties.put("scenarios", new String[] { "scenario1", "scenario2" });
		bo.update(properties);
		log.info(bo.getStringAttribute("created_by"));
		log.info(bo.getDateAttribute("effective"));
		deleteObject(groupFolder,randId);

		Map<String, Object> elanProperties = new HashMap<String, Object>();
		elanProperties.put("scenarios", new String[] { "demo-on-2024-04-01" });
		Map<String, Object> rodosProperties = new HashMap<String, Object>();
		rodosProperties.put("reportId", "REPORT");
		randId = "fromjava" + r.nextInt();
		Document d = groupFolder.createAppSpecificDocument(randId, "New from Java",
				"Description from Java", "<p>Text from Java!</p>", "ifinprojection", new String[] { "elan", "rodos" },
				elanProperties,
				null,
				rodosProperties,
				null
				);
		log.info(d.getTitle());
		log.info(d.getWorkflowStatus());
		log.info(d.getStringsAttribute("local_behaviors"));
		deleteObject(groupFolder,randId);
	}

	/**
	 * Test user and group  handling.
	 *
	 */
	@Test
	public void userManagementTest() throws Exception {
		log.info("=== TEST: userManagementTest ======");
		DocumentPool myDocumentPool = obtainDocumentPool();
		User user = myDocumentPool.createUser("testuserId", "testuserPW", "Test User Full Name", extractPath(myDocumentPool));
		if (user == null) {
			log.error("No User created!");
		} else {
			log.info("User " + user.getUserId() + " created.");
		}
		Group group = myDocumentPool.createGroup("testgroupId", "Test Group Full Name","for java tests", extractPath(myDocumentPool));
		if (group == null) {
			log.error("No group created.");
		} else {
			log.info("Group " + group.getGroupId() + " created.");
		}
		group.addUser(user, extractPath(myDocumentPool));
		String[] docTypes = {"airactivity", "ifinprojection", "protectiveactions"};
		group.setAllowedDocTypes(docTypes);
		List<String> gDoctypes = group.getAllowedDocTypes();
		log.info("docTypes " + docTypes);
		log.info("gDocTypes " + gDoctypes);
		if (gDoctypes != null && gDoctypes.equals(Arrays.asList(docTypes))) {
			log.info("Group properties were successfully set.");
		} else {
			log.error("Error while setting group properties.");
		}
// 		Assert.assertEquals(5,5);
	}

	/**
	 * not used
	 */
	static class HelperClass {
		public List<Object> elemente = new ArrayList<Object>();
	}
}

