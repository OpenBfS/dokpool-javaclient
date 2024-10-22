package de.bfs.dokpool;

// import java.io.*;
// import java.text.DecimalFormat;
import java.util.*;
import org.junit.*;
import de.bfs.dokpool.client.base.*;

public class dokpoolTest
{
   private static final String ENCODING       = "UTF-8";
   private static final String PACKAGE        = "de.bfs.dokpool";
   private static final String PROTO          = System.getenv("DOKPOOL_PROTO");
   private static final String HOST           = System.getenv("DOKPOOL_HOST");
   private static final String PORT           = System.getenv("DOKPOOL_PORT");
   private static final String PLONESITE      = System.getenv("DOKPOOL_PLONESITE");
   private static final String USER           = System.getenv("DOKPOOL_USER");
   private static final String PW             = System.getenv("DOKPOOL_PW");
//       String proto = bfsIrixBrokerProperties.getProperty("irix-dokpool.PROTO");
//       String host = bfsIrixBrokerProperties.getProperty("irix-dokpool.HOST");
//       String port = bfsIrixBrokerProperties.getProperty("irix-dokpool.PORT");
//       String ploneSite = bfsIrixBrokerProperties.getProperty("irix-dokpool.PLONE_SITE");
//       String documentOwner = bfsIrixBrokerProperties.getProperty("irix-dokpool.PLONE_DOKPOOLDOCUMENTOWNER");
//       String user = bfsIrixBrokerProperties.getProperty("irix-dokpool.USER");
//       String pw = bfsIrixBrokerProperties.getProperty("irix-dokpool.PW");
//       Element dt = extractSingleElement(dokpoolmeta, TAG_DOKPOOLCONTENTTYPE);
//       //FIXME remove this static String
//       String desc = "Original date: " + DateTime.toString() + " " + ReportContext + " " + Confidentiality;

   /** Die main()-Methode ist nur fuer manuelle Testzwecke */
   public static void main( String[] args ) throws Exception
   {

   }

   @Test
   public void someTestFunction() throws Exception
   {


      System.out.println(PROTO + "://" + HOST + ":" + PORT + "/" + PLONESITE + "user:" + USER + "pw:" + PW);
//       //connect to Dokpool using API (wsapi4plone/wsapi4elan)
      DocpoolBaseService docpoolBaseService = new DocpoolBaseService(PROTO + "://" + HOST + ":" + PORT + "/" + PLONESITE, USER, PW);
      Assert.assertEquals( 5, 5 );
   }


   @Test
   public void secondTestFunction() throws Exception
   {
      Assert.assertEquals( 5, 5 );
   }

   /**
    * not used
    */
   static class HelperClass
   {
      public List<Object> elemente = new ArrayList<Object>();

   }
}

