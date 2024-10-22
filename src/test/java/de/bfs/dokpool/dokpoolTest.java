package de.bfs.dokpool;

// import java.io.*;
// import java.text.DecimalFormat;
import java.util.*;
import org.junit.*;
import de.bfs.dokpool.client.base.*;
import de.bfs.dokpool.client.content.*;

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

   /** Die main()-Methode ist nur fuer manuelle Testzwecke */
   public static void main( String[] args ) throws Exception
   {

   }

   @Test
   public void someTestFunction() throws Exception
   {


      System.out.println("URL: " + PROTO + "://" + HOST + ":" + PORT + "/" + PLONESITE + " User:" + USER + " Password:" + PW);
//       //connect to Dokpool using API (wsapi4plone/wsapi4elan)
      DocpoolBaseService docpoolBaseService = new DocpoolBaseService(PROTO + "://" + HOST + ":" + PORT + "/" + PLONESITE, USER, PW);
      List<DocumentPool> myDocpools = docpoolBaseService.getDocumentPools();
      System.out.println(myDocpools.size());
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

