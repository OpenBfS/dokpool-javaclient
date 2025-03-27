/**
 * @authors fuf-ber - German Federal Office for Radiation Protection www.bfs.de
 */

package de.bfs.dokpool;

import java.util.*;
import org.junit.*;
import de.bfs.dokpool.client.base.*;
import de.bfs.dokpool.client.content.*;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;

public class DokpoolTest {
	private static final DocpoolBaseService.Log log = new DocpoolBaseService.Log(DokpoolTest.class);
	private static final String envOrEmpty(String envVar) {
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
	private static final String EVENTUID       = envOrEmpty("DOKPOOL_EVENTUID");
	private static final String DOCID          = envOrEmpty("DOKPOOL_DOCID");

	// private static class ObtainDPReturn {
	// 	public DocumentPool dp;
	// 	public DocpoolBaseService service;
	// 	public ObtainDPReturn(DocumentPool dp, DocpoolBaseService service) {
	// 		this.service = service;
	// 		this.dp = dp;
	// 	}
	// }

	public static DocumentPool obtainDocumentPoolREST() throws Exception {
		log.info("URL: " + PROTO + "://" + HOST + ":" + PORT + "/" + PLONESITE + " User:" + USER + " Password:" + PW);
		DocpoolBaseService docpoolBaseService = new DocpoolBaseService(PROTO + "://" + HOST + ":" + PORT + "/" + PLONESITE, USER, PW);
		List<DocumentPool> myDocpools = docpoolBaseService.getDocumentPools();
		DocumentPool mainDocpool = docpoolBaseService.getPrimaryDocumentPool();

		log.info("Number of Dokppols: " + myDocpools.size());
		log.info("Main Dokpool: " + mainDocpool.getPathWithPlonesite());

		for (DocumentPool sDocpool : myDocpools) {
			if (sDocpool.getPathWithPlonesite().matches("/" + PLONESITE + "/" + DOKPOOL)) {
				mainDocpool = sDocpool;
				log.info("Main Dokpool is now: " + mainDocpool.getPathWithPlonesite());
				break;
			}
		}

		// new Folder(docpoolBaseService, "/bund/content/Groups/bund_zdb/java-docpool-test-doc", (Object[])null).setWorkflowStatus("retract");
		// log.info(new Folder(docpoolBaseService, "/bund/content/Groups/bund_zdb/java-docpool-test-doc", (Object[])null).getWorkflowStatus());
		return mainDocpool;
	}

	/**
	 * Test document creation, file upload and setting properties.
	 *
	 */
	@Test
	public void documentTestREST() throws Exception {
		log.info("=== TEST: documentTest ======");
		DocumentPool mainDocpool = obtainDocumentPoolREST();
		log.info(mainDocpool.getWorkflowStatus());

		log.info("numer of events: " + mainDocpool.getEvents().size());
		log.info("numer of active events: " + mainDocpool.getActiveEvents().size());
		List<Event> events = mainDocpool.getEvents();
		try {
			Event ev = events.get(0);
			log.info("First event from Dokpool has title: " + ev.getTitle() + " and decription: " + ev.getDescription());
		} catch (NullPointerException e) {
			log.info("Could not find any events for " + mainDocpool.getPathWithPlonesite());
		}

		events = mainDocpool.getActiveEvents();
		try {
			Event ev = events.get(0);
			log.info("First active event from Dokpool has title: " + ev.getTitle() + " and decription: " + ev.getDescription());
		} catch (NullPointerException e) {
			log.info("Could not find any active events for " + mainDocpool.getPathWithPlonesite());
		}

		List<Scenario> scenarios = mainDocpool.getScenarios();
		try {
			Scenario ev = scenarios.get(0);
			log.info("First scenario from Dokpool has title: " + ev.getTitle() + " and decription: " + ev.getDescription());
		} catch (NullPointerException | IndexOutOfBoundsException e) {
			log.info("Could not find any scenarios for " + mainDocpool.getPathWithPlonesite());
		}

		scenarios = mainDocpool.getActiveScenarios();
		try {
			Scenario ev = scenarios.get(0);
			log.info("First active scenario from Dokpool has title: " + ev.getTitle() + " and decription: " + ev.getDescription());
		} catch (NullPointerException | IndexOutOfBoundsException e) {
			log.info("Could not find any active scenarios for " + mainDocpool.getPathWithPlonesite());
		}

		log.info("My very own user folder: " + mainDocpool.getUserFolder());
		log.info("The user folder of some well known user: " + mainDocpool.getUserFolder(MEMBER));


		Folder myGroupFolder = null;
		try {
			myGroupFolder = mainDocpool.getGroupFolders().get(0);
		} catch (NullPointerException e) {
			throw new NullPointerException("Could not find any valid GroupFolder for Dokpool " + mainDocpool.getPathWithPlonesite());
		}

		log.info("Group folder path (first from Dokpool): " + myGroupFolder.getPathWithPlonesite());

		try {
			myGroupFolder = mainDocpool.getFolder("content/Groups/" + GROUPFOLDER);
			log.info("Group folder now set from from env: " +  myGroupFolder.getPathWithPlonesite());
			myGroupFolder = mainDocpool.getGroupFolder(GROUPFOLDER).get();
			log.info("Group folder set from from env again: " +  myGroupFolder.getPathWithPlonesite());
		} catch (NullPointerException e) {
			log.warn("Could not find DOKPOOL_GROUPFOLDER: " + mainDocpool.getPathWithPlonesite() + "/content/Groups/" + GROUPFOLDER);
			log.info("Group folder remains: " +  myGroupFolder.getPathWithPlonesite());
		}

		List<Object> contentList = myGroupFolder.getContents(null);
		for (Object contentItem : contentList) {
			log.info("group folder item id: " + ((BaseObject) contentItem).getId());
		}

		Folder myTransferFolder = null;
		try {
			List<Folder> tfFolders = mainDocpool.getTransferFolders();
			log.info("number of transfer folders: " + tfFolders.size());
			myTransferFolder = tfFolders.get(0);
			log.info("Transfer folder path (first from Dokpool): " + myTransferFolder.getPathWithPlonesite());
		} catch (NullPointerException e) {
			log.info("Could not find any valid TransferFolder for Dokpool " + mainDocpool.getPathWithPlonesite());
		}
		

		boolean docExists = false;
		Document oldDoc = null;
		try {
			oldDoc = (Document) myGroupFolder.getContentItem(DOCID);
			log.info("Object exists: " +  oldDoc.getPathWithPlonesite());
			docExists = true;
		} catch (NullPointerException e) {
			log.info("Object does not exist: " +  myGroupFolder.getPathWithPlonesite() + "/" + DOCID);
		}
		if (docExists) {
			oldDoc.delete();
		}

		Map<String, Object> docProperties = new HashMap<String, Object>();
		docProperties.put("title", "JavaDocpoolTestDocument");
		docProperties.put("description", "Created by mvn test.");
		docProperties.put("text", "This is just a Test and can be deleted.");

		List<String> creatorsList = new ArrayList<String>();
		creatorsList.add(DOCUMENTOWNER);
		creatorsList.add(USER);
		docProperties.put("creators", creatorsList);

		List<String> eventList = new ArrayList<String>();
		eventList.add("routinemode");
		docProperties.put("scenarios", eventList);

		log.info("Creating new document at " + myGroupFolder.getPathWithPlonesite() + "/" + DOCID);
		Document d = myGroupFolder.createDPDocument(DOCID, docProperties);

		byte[] fileData = ("Readme, I'm a string!").getBytes();
		File readme = d.uploadFile("readme", "Read me!", "A file you should read.", fileData, "README.txt");
		fileData = Files.readAllBytes(Paths.get("README.md"));
		File readme2 = d.uploadFile("readme", "Read me!", "A file you should read.", fileData, "README.txt");
		if (readme2 != null) {
			throw new Exception("Upload to an existing file should not work and return null, but we got non-null value.");
		}
		File readme3 = d.uploadOrReplaceFile("readme", "Read me!", "A file you should read.", fileData, "README.txt", null);
		if (readme3 == null) {
			throw new Exception("UploadOrReplace to an existing file should work, but we got a null value.");
		}
		String newTitle = "No, read me!";
		readme.replace(newTitle, "readme from file", fileData,"README.txt" , "text/plain");
		if (!readme.getStringAttribute("title").equals(newTitle)) {
			throw new Exception("File metadata replacement did not work.");
		}
		byte[] imageData = Files.readAllBytes(Paths.get("src/test/resources/image.png"));
		//we purposely use a wrong mimetype (image/svg) here to check that Plone still does not care:
		Image img = d.uploadOrReplaceImage("image", "Look at me!", "An image you should look at.", imageData, "image.png", "image/svg");
		img = d.uploadOrReplaceImage("image", "Look at me!", "An image you should look at.", imageData, "image.png", "image/svg");
		newTitle = "look at me again";
		imageData = Files.readAllBytes(Paths.get("src/test/resources/image.jpeg"));
		img.replace(newTitle, "An image you should still look at.", imageData, "image.jpeg", "image/jpeg");
		if (!img.getStringAttribute("title").equals(newTitle)) {
			throw new Exception("Image metadata replacement did not work.");
		}


		log.info(d.getContentItem("image"));
		log.info(d.getContentItem("imaggee"));

		log.info(d.getPathAfterPlonesite());
		log.info("modified: " + d.getDateAttribute("modified"));
		log.info("mdate: " + d.getDateAttribute("mdate"));

		d.setWorkflowStatus("publish");
	}

	/**
	 * Test doksys document creation.
	 *
	 */
	@Test
	public void doksysTest() throws Exception {
		String doksysDocId = DOCID+"-doksys";

		DocumentPool mainDocpool = obtainDocumentPoolREST();

		Folder myGroupFolder = mainDocpool.getGroupFolder(GROUPFOLDER).get();
		boolean docExists = false;
		Document oldDoc = null;
		try {
			oldDoc = (Document) myGroupFolder.getContentItem(doksysDocId);
			log.info("Object exists: " +  oldDoc.getPathWithPlonesite());
			docExists = true;
		} catch (NullPointerException e) {
			log.info("Object does not exist: " +  myGroupFolder.getPathWithPlonesite() + "/" + doksysDocId);
		}
		if (docExists) {
			oldDoc.delete();
		}

		Map<String, Object> docProperties = new HashMap<String, Object>();
		docProperties.put("title", "DoksysTestDocument");
		docProperties.put("description", "Created by mvn test.");
		docProperties.put("text", "This is just a Doksys Test and can be deleted.");
		docProperties.put("local_behaviors", new String[] { "doksys"});
		// docProperties.put("Status", "plausibel");
		docProperties.put("TrajectoryStartLocation", "somewhere");
		docProperties.put("TrajectoryEndLocation", "somewhere else");
		// docProperties.put("Dom", new String[] {"Gamma-ODL"});
		docProperties.put("SamplingBegin", "2025-03-27T13:50:53.000Z");
		docProperties.put("Duration", "1d");
		// docProperties.put("Purpose", new String[] {"Standard-Info Bundesmessnetze"});
		docProperties.put("LegalBase", new String[] {"AVV IMIS"});
		// docProperties.put("MeasuringProgram", new String[] { "Routinemessprogramm"});
		docProperties.put("SamplingEnd", "2025-03-27T13:50:53.000Z");
		// docProperties.put("NetworkOperator", new String[] { "Z"});
		docProperties.put("TrajectoryEndTime", "2025-03-27T13:50:53.000Z");
		docProperties.put("TrajectoryStartTime", "2025-03-27T13:50:53.000Z");
		// docProperties.put("OperationMode", "Übung");
		// docProperties.put("SampleType", new String[] {"Gamma-Ortsdosisleistung"});

		Document d = myGroupFolder.createDPDocument(doksysDocId, docProperties);
		log.info(d.getPathAfterPlonesite());

	}

	/**
	 * Document handling parts of old main Test method.
	 *
	 * @throws Exception
	 */
	@Test
	public void miscObjectTestREST() throws Exception {
		log.info("=== TEST: miscObjectTest REST ======");
		log.info("URL: " + PROTO + "://" + HOST + ":" + PORT + "/" + PLONESITE + " User:" + USER + " Password:" + PW);
		DocpoolBaseService docpoolBaseService = new DocpoolBaseService(PROTO + "://" + HOST + ":" + PORT + "/" + PLONESITE, USER, PW);

		List<DocumentPool> documentpools = docpoolBaseService.getDocumentPools();
		if (documentpools.isEmpty()) {
			log.warn("No DocumentPools found!");
		}

		DocumentPool myDocumentPool = null;
		for (DocumentPool sDocpool : documentpools) {
			if (sDocpool.getPathWithPlonesite().matches("/" + PLONESITE + "/" + DOKPOOL)) {
				myDocumentPool = sDocpool;
				log.info("Main Dokpool is now: " + myDocumentPool.getPathWithPlonesite());
				break;
			}
		}
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
		properties.put("docType", "gammadoserate");
		properties.put("subjects", new String[] {"Tag1", "Tag2"});
		properties.put("local_behaviors", new String[] {"elan", "doksys"});
		// These attributes are not required, we use them to test date handling:
		properties.put("TrajectoryStartTime", Date.from(Instant.now().minusSeconds(3600)));
		properties.put("TrajectoryEndTime", Date.from(Instant.now()));
		//OperationMode is a required REST argument for behavior doksys, but createObject adds it if missing
		// properties.put("OperationMode", "Routine");
		properties.put("scenarios", new String[] { "scenario1", null, EVENTUID, EVENT });
		String randId = "generic" + r.nextInt();
		BaseObject bo = groupFolder.createObject(randId, properties, "DPDocument");
		// BaseObject bo = groupFolder.createCopyOf(groupFolder.getContentItem(DOCID));
		properties.clear();
		properties.put("scenarios", new String[] { "routinemode", "scenario2", null });
		bo.update(properties);
		log.info(bo.getStringAttribute("created_by"));
		log.info(bo.getDateAttribute("effective"));
		bo.delete();

		Map<String, Object> elanProperties = new HashMap<String, Object>();
		elanProperties.put("scenarios", new String[] {"demo-on-2024-04-01"});
		Map<String, Object> rodosProperties = new HashMap<String, Object>();
		//reportId is no longer a rodos property, but this should just be ignored
		rodosProperties.put("reportId", "REPORT");
		//PrognosisForm is not mandatory, but if present it should be in a list of accepted values
		rodosProperties.put("PrognosisForm", "invalid value");
		randId = "fromjava" + r.nextInt();
		Document d = (Document) groupFolder.createCopyOf(groupFolder.getContentItem(DOCID));
		d.delete();

		d = groupFolder.createAppSpecificDocument(randId, "New from Java",
			"Description from Java", "<p>Text from Java!</p>","rodosprojection", new String[] {"elan", "rodos"},
			elanProperties,
			null,
			rodosProperties,
			null
		);
		log.info("title: " + d.getTitle());
		log.info("status: " + d.getWorkflowStatus());
		log.info("behaviors: " + d.getStringsAttribute("local_behaviors"));

		rodosProperties = new HashMap<String, Object>();
		rodosProperties.put("PrognosisForm", "invalid value");
		rodosProperties.put("PrognosisType", "RODOS Prognose");
		d.update(rodosProperties);
		d.delete();

		randId = "fromjava" + r.nextInt();
		Map<String, Object> reiProperties = new HashMap<String, Object>();
		reiProperties.put("Authority","de_nw");
		reiProperties.put("ReiLegalBases",new String[] {"REI-I"});
		reiProperties.put("NuclearInstallations", new String[] {"U05T"});
		reiProperties.put("Year", "2020");
		reiProperties.put("Period","Q4");
		reiProperties.put("Origins", "Strahlenschutzverantwortlicher");
		reiProperties.put("PDFVersion", "PDF/A-1b");
		d = groupFolder.createAppSpecificDocument(randId, "New from Java",
			"Description from Java", "<p>Text from Java!</p>","reireport", new String[] {"rei"},
			null,
			null,
			null,
			reiProperties
		);
		log.info("title: " + d.getTitle());
		log.info("status: " + d.getWorkflowStatus());
		log.info("behaviors: " + d.getStringsAttribute("local_behaviors"));
		d.delete();
	}

	/**
	 * Test user and group  handling.
	 *
	 */
	@Test
	public void userManagementTestREST() throws Exception {
		log.info("=== TEST: userManagementTest REST ======");
		Random r = new Random();
		DocumentPool myDocumentPool = obtainDocumentPoolREST();
		User user = myDocumentPool.createUser("javaTestUser"+r.nextInt(), "testuserPW", "Test User Full Name", myDocumentPool.getPathAfterPlonesite());
		if (user == null) {
			log.error("No User created!");
		} else {
			log.info("User " + user.getUserId() + " created.");
		}
		Group group = myDocumentPool.createGroup("javaTestGroup"+r.nextInt(), "Test Group Full Name","for java tests", myDocumentPool.getPathAfterPlonesite());
		if (group == null) {
			log.error("No group created.");
		} else {
			log.info("Group " + group.getGroupId() + " created.");
		}
		group.addUser(user, myDocumentPool.getPathAfterPlonesite());
		String[] docTypes = {"airactivity", "ifinprojection", "protectiveactions"};
		//not implemented for REST:
		group.setAllowedDocTypes(docTypes);
		List<String> gDoctypes = group.getAllowedDocTypes();
		log.info("docTypes " + docTypes);
		log.info("gDocTypes " + gDoctypes);
		if (gDoctypes != null && gDoctypes.equals(Arrays.asList(docTypes))) {
			log.info("Group properties were successfully set.");
		} else {
			log.error("Error while setting group properties.");
		}
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
		for (String ep : endpoints) {
			path = HttpClient.composeUrl(PROTO,HOST,PORT,"/"+PLONESITE + ep);
			rsp = HttpClient.doGetRequest(PROTO,HOST,PORT,path,headers);
			log.info(rsp.content.length());
		}
		// log.info(rsp != null?rsp.content:"");
		JSON.Node pendingRoot = new JSON.Node(rsp.content);
		pendingRoot.get("items");
		if (pendingRoot != null && pendingRoot.get("items") != null && pendingRoot.get("items").get(0) != null) {
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
		hmap.put("date",Date.from(Instant.ofEpochSecond(1234678910)));
		JSON.Node hmapnode = new JSON.Node(hmap);
		log.info("root JSON: "+hmapnode.toJSON());
		Assert.assertEquals("{\"date\":\"2009-02-15T06:21:50.000Z\",\"one\":\"1\",\"two\":\"2\",\"three\":{\"four\":\"4\"}}", hmapnode.toJSON());
	}

}

