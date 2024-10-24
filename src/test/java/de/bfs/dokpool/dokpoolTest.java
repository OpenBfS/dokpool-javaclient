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
	private static final String DOKPOOL        = System.getenv("DOKPOOL_DOKPOOL");
	private static final String GROUPFOLDER    = System.getenv("DOKPOOL_GROUPFOLDER");
	private static final String DOCID          = "java-docpool-test-doc";

	/** Die main()-Methode ist nur fuer manuelle Testzwecke */
	public static void main( String[] args ) throws Exception {

	}

	@Test
	public void someTestFunction() throws Exception {


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

		Map<String, Object> docpoolProperties = new HashMap<String, Object>();
		docpoolProperties.put("title", "JavaDocpoolTestDocument");
		docpoolProperties.put("description", "Created by mvn test.");
		docpoolProperties.put("text", "This is just a Test and can be deleted.");
// 		docpoolProperties.put("docType", dt.getTextContent());
// 		docpoolProperties.putAll(setBehaviors(myDocpool));
// 		docpoolProperties.putAll(setSubjects());
		log.info("Creating new Dokument at " + myGroupFolder.getFolderPath() + "/" + DOCID);
		Document d = myGroupFolder.createDPDocument(DOCID, docpoolProperties);
		d.setWorkflowStatus("publish");

		log.info("Trying to delete "+myGroupFolder.getFolderPath() + "/" + DOCID);
		Field clientField = Class.forName("de.bfs.dokpool.client.base.BaseObject").getDeclaredField("client");
		clientField.setAccessible(true);
		XmlRpcClient client = (XmlRpcClient) clientField.get(myGroupFolder);
		Vector<String[]> delParams = new Vector<String[]>();
		delParams.add(new String[]{myGroupFolder.getFolderPath() + "/" + DOCID});
		Object[] res = (Object[]) Utils.execute(client, "delete_object", delParams);

		Assert.assertEquals(5,5);
	}


	@Test
	public void secondTestFunction() throws Exception {
		Assert.assertEquals(5,5);
	}

	/**
	 * not used
	 */
	static class HelperClass {
		public List<Object> elemente = new ArrayList<Object>();
	}
}

