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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class DokpoolTest {
	private static final Log log = LogFactory.getLog(DokpoolTest.class);
	private static final String envOrEmpty(String envVar){
		String env = System.getenv(envVar);
		return (env != null ? env: "");
	}

	private static final String ENCODING       = "UTF-8";
	private static final String PACKAGE        = "de.bfs.dokpool";
	private static final String PROTO          = envOrEmpty("DOKPOOL_PROTO");
	private static final String HOST           = envOrEmpty("DOKPOOL_HOST");
	private static final String PORT           = envOrEmpty("DOKPOOL_PORT");
	private static final String PLONESITE      = envOrEmpty("DOKPOOL_PLONESITE");
	private static final String USER           = envOrEmpty("DOKPOOL_USER");
	private static final String PW             = envOrEmpty("DOKPOOL_PW");
	private static final String DOCUMENTOWNER  = envOrEmpty("DOKPOOL_DOCUMENTOWNER");
	private static final String DOKPOOL        = envOrEmpty("DOKPOOL_DOKPOOL");
	private static final String GROUPFOLDER    = envOrEmpty("DOKPOOL_GROUPFOLDER");
	//MEMBER should have an associated user folder
	private static final String MEMBER         = envOrEmpty("DOKPOOL_MEMBER");
	private static final String EVENT          = envOrEmpty("DOKPOOL_EVENT");
	private static final String DOCID          = "java-docpool-test-doc";
	private static final String PROTOP5        = envOrEmpty("DOKPOOL_PROTOP5");
	private static final String HOSTP5         = envOrEmpty("DOKPOOL_HOSTP5");
	private static final String PORTP5         = envOrEmpty("DOKPOOL_PORTP5");


	public static DocumentPool obtainDocumentPoolXMLRPC() throws Exception {
		log.info("URL: " + PROTOP5 + "://" + HOSTP5 + ":" + PORTP5 + "/" + PLONESITE + " User:" + USER + " Password:" + PW);
		DocpoolBaseService docpoolBaseService = new DocpoolBaseService(PROTOP5 + "://" + HOSTP5 + ":" + PORTP5 + "/" + PLONESITE, USER, PW);
		List<DocumentPool> myDocpools = docpoolBaseService.getDocumentPoolsX();
		DocumentPool mainDocpool = docpoolBaseService.getPrimaryDocumentPoolX();

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

	public static DocumentPool obtainDocumentPoolREST() throws Exception {
		log.info("URL: " + PROTO + "://" + HOST + ":" + PORT + "/" + PLONESITE + " User:" + USER + " Password:" + PW);
		DocpoolBaseService docpoolBaseService = new DocpoolBaseService(PROTO + "://" + HOST + ":" + PORT + "/" + PLONESITE, USER, PW);
		List<DocumentPool> myDocpools = docpoolBaseService.getDocumentPools();
		DocumentPool mainDocpool = docpoolBaseService.getPrimaryDocumentPool();

		log.info("Number of Dokppols: " + myDocpools.size());
		// log.info("Main Dokpool: " + mainDocpool.getFolderPath());

		// for (DocumentPool sDocpool : myDocpools) {
		// 	if (sDocpool.getFolderPath().matches("/" + PLONESITE + "/" + DOKPOOL)) {
		// 		mainDocpool = sDocpool;
		// 		log.info("Main Dokpool is now: " + mainDocpool.getFolderPath());
		// 		break;
		// 	}
		// }

		return mainDocpool;
	}

	public static void deleteObjectXMLRPC(Folder folder, String objId) throws Exception {
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
	public void documentTestXMLRPC() throws Exception {
		log.info("=== TEST: documentTest ======");
		DocumentPool mainDocpool = obtainDocumentPoolXMLRPC();

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
			deleteObjectXMLRPC(myGroupFolder, DOCID);
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

		byte[] fileData = Files.readAllBytes(Paths.get("README.md"));
		d.uploadFile("readme", "Read me!", "A file you should read.", fileData, "README.txt");
		byte[] imageData = Files.readAllBytes(Paths.get("src/test/resources/image.png"));
		d.uploadImage("image", "Look at me!", "An image you should look at.", imageData, "image.png");

		//TODO: needed? What does this do?
		d.autocreateSubdocuments();

		d.setWorkflowStatus("publish");

	}

	/**
	 * Test document creation, file upload and setting properties.
	 *
	 */
	@Test
	public void documentTestREST() throws Exception {
		log.info("=== TEST: documentTest ======");
		DocumentPool mainDocpool = obtainDocumentPoolREST();

		// Folder myGroupFolder = null;
		// try {
		// 	myGroupFolder = mainDocpool.getGroupFolders().get(0);
		// } catch (NullPointerException e) {
		// 	throw new NullPointerException("Could not find any valid GroupFolder for Dokpool " + mainDocpool.getFolderPath());
		// }

		// log.info("Group folder path (first from Dokpool): " + myGroupFolder.getFolderPath());

		// try {
		// 	myGroupFolder = mainDocpool.getFolder(/*mainDocpool.getFolderPath() + "/*/"content/Groups/" + GROUPFOLDER);
		// 	log.info("Group folder now set from from env: " +  myGroupFolder.getFolderPath());
		// } catch (NullPointerException e) {
		// 	log.warn("Could not find DOKPOOL_GROUPFOLDER: " + mainDocpool.getFolderPath() + "/content/Groups/" + GROUPFOLDER);
		// 	log.info("Group folder remains: " +  myGroupFolder.getFolderPath());
		// }

		// boolean docExists = false;
		// try {
		// 	myGroupFolder.getFolder(DOCID);
		// 	log.info("Object exists: " +   myGroupFolder.getFolderPath() + "/" + DOCID);
		// 	docExists = true;
		// } catch (NullPointerException e) {
		// 	log.info("Object does not exist: " +  myGroupFolder.getFolderPath() + "/" + DOCID);
		// }
		// if (docExists) {
		// 	deleteObjectXMLRPC(myGroupFolder, DOCID);
		// }

		// Map<String, Object> docProperties = new HashMap<String, Object>();
		// docProperties.put("title", "JavaDocpoolTestDocument");
		// docProperties.put("description", "Created by mvn test.");
		// docProperties.put("text", "This is just a Test and can be deleted.");

		// List<String> creatorsList = new ArrayList<String>();
		// creatorsList.add(DOCUMENTOWNER);
		// creatorsList.add(USER);
		// docProperties.put("creators", creatorsList);

		// log.info("Creating new document at " + myGroupFolder.getFolderPath() + "/" + DOCID);
		// Document d = myGroupFolder.createDPDocument(DOCID, docProperties);

		// byte[] fileData = Files.readAllBytes(Paths.get("README.md"));
		// d.uploadFile("readme", "Read me!", "A file you should read.", fileData, "README.txt");
		// byte[] imageData = Files.readAllBytes(Paths.get("src/test/resources/image.png"));
		// d.uploadImage("image", "Look at me!", "An image you should look at.", imageData, "image.png");

		// //TODO: needed? What does this do?
		// d.autocreateSubdocuments();

		// d.setWorkflowStatus("publish");

	}


	/**
	 * Document handling parts of old main Test method.
	 *
	 * @throws Exception
	 */
	@Test
	public void miscObjectTestXMLRPC() throws Exception {
		log.info("=== TEST: miscObjectTest ======");
		log.info("URL: " + PROTOP5 + "://" + HOSTP5 + ":" + PORTP5 + "/" + PLONESITE + " User:" + USER + " Password:" + PW);
		DocpoolBaseService docpoolBaseService = new DocpoolBaseService(PROTOP5 + "://" + HOSTP5 + ":" + PORTP5 + "/" + PLONESITE, USER, PW);

		List<DocumentPool> documentpools = docpoolBaseService.getDocumentPoolsX();
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
		deleteObjectXMLRPC(groupFolder,randId);

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
		deleteObjectXMLRPC(groupFolder,randId);
	}

	/**
	 * Test user and group  handling.
	 *
	 */
	@Test
	public void userManagementTestXMLRPC() throws Exception {
		log.info("=== TEST: userManagementTest ======");
		DocumentPool myDocumentPool = obtainDocumentPoolXMLRPC();
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
	 * Test basic http(s) functionality.
	 *
	 */
	@Test
	public void httpClientTest() throws Exception {
		HttpClient.tlsLogging = true;
		Map<String,String> headers = new HashMap<String,String>();
		headers.put(HttpClient.Headers.ACCEPT,HttpClient.MimeTypes.PLAIN);
		HttpClient.Response rsp = HttpClient.doGetRequest(PROTO,HOST,PORT,HttpClient.composeUrl(PROTO,HOST,PORT,"/"+PLONESITE),headers);
		log.info(rsp.content.length());
		HashMap<String,String> postPar = new HashMap<>();
		postPar.put("__ac_name", USER);
		postPar.put("__ac_password", PW);
		rsp = HttpClient.doPostRequest(PROTO,HOST,PORT,HttpClient.composeUrl(PROTO,HOST,PORT,"/"+PLONESITE+"/login_form"),headers,postPar,null,null);
		log.info(rsp.content.length());
		rsp = HttpClient.doPutRequest(PROTO,HOST,PORT,HttpClient.composeUrl(PROTO,HOST,PORT,"/"+PLONESITE+"/testupload"),headers,"text/plain",("hellö!").getBytes(StandardCharsets.UTF_8));
		log.info(rsp.content.length());
		rsp = HttpClient.doPatchRequest(PROTO,HOST,PORT,HttpClient.composeUrl(PROTO,HOST,PORT,"/"+PLONESITE+"/testupload"),headers,"text/plain",("hellö nöchma!").getBytes(StandardCharsets.UTF_8));
		log.info(rsp.content.length());
		rsp = HttpClient.doDeleteRequest(PROTO,HOST,PORT,HttpClient.composeUrl(PROTO,HOST,PORT,"/"+PLONESITE+"/testupload"),headers);
		log.info(rsp.content.length());
	}

	/**
	 * Some REST tests just using the HttpClient class and none of the Dokpool specific classes.
	 */
	@Test
	public void httpPureRestTest() throws Exception {
		//"/@get_primary_documentpool/test-land"
		Map<String,String> headers = new HashMap<String,String>();
		headers.put(HttpClient.Headers.ACCEPT,HttpClient.MimeTypes.JSON);
		HttpClient.addBasicAuthToHeaders(headers,USER,PW);

		//These should also work with Plone 5 instances, except where stated otherwise:
		String[] endpoints = new String[] {
			// "/@users",//requires admin privileges, for normal users 401 is expected
			// "/@groups",//requires admin privileges, for normal users 401 is expected
			"/"+DOKPOOL+"/content/Members",//200
			"/"+DOKPOOL+"/content/Groups",//200
			"/@users/"+USER,//200
			"/@get_group_folders/",//not part of Plone.restapi -> only plone6-Dokpool
			"/@get_group_folders/saarland",//not part of Plone.restapi -> only plone6-Dokpool
			"/"+DOKPOOL+"/content/Members/"+MEMBER,//200
			"/"+DOKPOOL+"/content/Transfers",//200
			"/"+DOKPOOL+"/contentconfig/scen/@search?portal_type=DPEvent&dp_type=active",//200
			"/"+DOKPOOL+"/contentconfig/scen/"+EVENT,//200
			"/"+DOKPOOL+"/@search?review_state=pending",//200
		};
		String path;
		HttpClient.Response rsp = null;
		for (String ep : endpoints){
			path = HttpClient.composeUrl(PROTO,HOST,PORT,"/"+PLONESITE + ep);
			rsp = HttpClient.doGetRequest(PROTO,HOST,PORT,path,headers);
			log.info(rsp.content.length());
		}
		// log.info(rsp != null?rsp.content:"");
		JSON.Node pendingRoot = new JSON.Node(rsp.content);
		pendingRoot.get("items");
		if (pendingRoot != null && pendingRoot.get("items") != null && pendingRoot.get("items").get(0) != null){
			log.info(pendingRoot.get("items").get(0).get("@id").toJSON());
		}
		String createUrl = HttpClient.composeUrl(PROTO,HOST,PORT,"/"+PLONESITE+"/"+DOKPOOL+"/content/Groups/"+ GROUPFOLDER);
		JSON.Node createJS = new JSON.Node("{}")
			.set("@type","DPDocument")
			.set("title", "JavaAPIDocumentNameCreatedByPOSTRequest")
			.set("id", DOCID)
			.set("transferred_by", USER)
			.set("description", "Created by java test.")
			.set("text", "This is just a Test and can be deleted.")
		;
		byte[] createData = createJS.toJSON().getBytes();
		rsp = HttpClient.doPostRequest(PROTO,HOST,PORT,createUrl,headers,null,HttpClient.MimeTypes.JSON,createData);
		log.info(rsp.content);

		String patchUrl = HttpClient.composeUrl(PROTO,HOST,PORT,"/"+PLONESITE+"/"+DOKPOOL+"/content/Groups/"+ GROUPFOLDER+"/"+DOCID);
		JSON.Node patchJS = new JSON.Node("{}")
			.set("title", "JavaAPIDocumentNameChangedByPATCHRequest")
			.set("description", "Changed by java test.")
		;
		byte[] patchData = patchJS.toJSON().getBytes();
		rsp = HttpClient.doPatchRequest(PROTO,HOST,PORT,patchUrl,headers,HttpClient.MimeTypes.JSON,patchData);
		log.info(rsp.content);
		Assert.assertEquals(204, rsp.status);

		String deleteUrl = HttpClient.composeUrl(PROTO,HOST,PORT,"/"+PLONESITE+"/"+DOKPOOL+"/content/Groups/"+ GROUPFOLDER+"/copy_of_"+DOCID);
		rsp = HttpClient.doDeleteRequest(PROTO,HOST,PORT,deleteUrl,headers);
		log.info(rsp.content);

		String copySrcUrl = patchUrl;
		String copyTgtUrl = createUrl+"/@copy";
		JSON.Node copyJS = new JSON.Node("{}")
			.set("source", copySrcUrl)
			.set("target", DOCID+"-two")
		;
		byte[] copyData = copyJS.toJSON().getBytes();
		rsp = HttpClient.doPostRequest(PROTO,HOST,PORT,copyTgtUrl,headers,null,HttpClient.MimeTypes.JSON,copyData);
		log.info(rsp.content);

		//does not work: id cannot be changed this way, neither in Plone 5 nor 6
		String renameUrl = HttpClient.composeUrl(PROTO,HOST,PORT,"/"+PLONESITE+"/"+DOKPOOL+"/content/Groups/"+ GROUPFOLDER+"/copy_of_"+DOCID);
		JSON.Node renameJS = new JSON.Node("{}")
			.set("id", DOCID+"-two")
		;
		byte[] renameData = renameJS.toJSON().getBytes();
		rsp = HttpClient.doPatchRequest(PROTO,HOST,PORT,renameUrl,headers,HttpClient.MimeTypes.JSON,renameData);
		log.info(rsp.content);
		Assert.assertEquals(204, rsp.status);
	}

	/**
	 * Tests for JSON handling.
	 */
	@Test
	public void jsonTest() throws Exception {
		JSON.Node root = new JSON.Node("{ \"num\": 7.3, \"arr\": [0,1,2,\"three\"] }");
		log.info("root type: "+root.type());
		JSON.Node num = root.get("num");
		log.info("num type: "+num.type() + " num: " + num.toDouble());
		JSON.Node arr = root.get("arr");
		log.info("arr type: "+arr.type());
		log.info("arr JSON: "+arr.toJSON());
		JSON.Node three = arr.get(3);
		log.info("three type: "+three.type());
		//set creates deep copys before adding
		arr.insert(-1,root);
		log.info("root JSON: "+root.toJSON());
		root.set("arr2",arr);
		Assert.assertEquals("{\"num\":7.3,\"arr\":[{\"num\":7.3,\"arr\":[0,1,2,\"three\"]},0,1,2,\"three\"],\"arr2\":[{\"num\":7.3,\"arr\":[0,1,2,\"three\"]},0,1,2,\"three\"]}",root.toJSON());
		log.info("root JSON: "+root.toJSON());
		Map<String,Object> rootMap = root.toMap();
		JSON.Node root2 = new JSON.Node(rootMap);
		Assert.assertEquals("{\"arr\":[{\"arr\":[0,1,2,\"three\"],\"num\":7.3},0,1,2,\"three\"],\"num\":7.3,\"arr2\":[{\"arr\":[0,1,2,\"three\"],\"num\":7.3},0,1,2,\"three\"]}",root2.toJSON());
		
		Map<String,Object> hmap = new HashMap<String,Object>();
		Map<String,Object> mthree = new HashMap<String,Object>();
		hmap.put("one","1");
		hmap.put("two","2");
		mthree.put("four","4");
		hmap.put("three",mthree);
		JSON.Node hmapnode = new JSON.Node(hmap);
		log.info("root JSON: "+hmapnode.toJSON());
		Assert.assertEquals("{\"one\":\"1\",\"two\":\"2\",\"three\":{\"four\":\"4\"}}", hmapnode.toJSON());
	}

}

