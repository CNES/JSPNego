/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.jspnego;

import fr.cnes.httpclient.ProxySPNegoHttpClient;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.data.Protocol;
import org.restlet.engine.Engine;
import org.restlet.engine.connector.ConnectorHelper;
import org.restlet.ext.httpclient4.HttpClientHelper;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

/**
 * @author malapert
 */
@Category(UnitTest.class)
public class ProxySPNegoHttpClientTest {
    
    /**
     * Get actual class name to be printed on.
     */
    private static final Logger LOG = LogManager.getLogger(ProxySPNegoHttpClientTest.class.getName());
    private static final Logger LOG_TITLE = LogManager.getLogger("testTitle");    

    private static final String proxyHost = System.getProperty("proxyHost");
    private static final String proxyPort = System.getProperty("proxyPort");
    private static final String userID = System.getProperty("userID");
    private static final String keytabFilePath = System.getProperty("keytabFilePath");
    private static final String ticketCachePath = System.getProperty("ticketCachePath");
    
    static {
        Engine.getInstance().getRegisteredClients().add(0, new HttpClientHelper(null));
    }

    public ProxySPNegoHttpClientTest() {

    }

    @BeforeClass
    public static void setUpClass() {

    }

    @AfterClass
    public static void tearDownClass() {

    }

    @Before
    public void setUp() {
        //try {
        //    checkInputParameters();
        //} catch (Exception ex) {
        //    java.util.logging.Logger.getLogger(ProxySPNegoHttpClientTest.class.getName()).
        //            log(Level.SEVERE, null, ex);
        //}
    }

    @After
    public void tearDown() {
    }
    
    private void checkInputParameters() throws Exception {
        int error = 0;
        if (proxyHost == null || proxyHost.isEmpty()) {
            LOG.warn("Please add -DproxyHost=<proxyHost> to your command line");
            error++;
        }
        if (proxyPort == null || proxyPort.isEmpty()) {
            LOG.warn("Please add -DproxyPort=<proxyPort> to your command line");
            error++;
        }
        if (userID == null || userID.isEmpty()) {
            LOG.warn("Please add -DuserID=<userID> to your command line");
            error++;
        }
        if (keytabFilePath == null || keytabFilePath.isEmpty()) {
            LOG.warn("Please add -DkeytabFilePath=<keytabFilePath> to your command line");
            error++;
        }
        if (error > 0) {
            throw new Exception("Missing input parameters");
        }  
        
        ConnectorHelper<Client> connClient = Engine.getInstance().getRegisteredClients().get(0);
        Context ctx = new Context();
        ctx.getParameters().add("proxyHost", proxyHost);
        ctx.getParameters().add("proxyPort", proxyPort);
        ctx.getParameters().add("userID", userID);
        ctx.getParameters().add("keytabFilePath", keytabFilePath);
        Client client = new Client(ctx, Arrays.asList(Protocol.HTTP, Protocol.HTTPS));
        connClient.setHelped(client);
    }

    @Test   
    public void testRequestHttps() throws Exception {
        LOG_TITLE.info(" --- Running one https request ---");
        File jaas = new File("/tmp/jaas.conf");
        //checkInputParameters();
        HttpResponse response;
        HttpClient httpclient = null;
        try {
            httpclient = new ProxySPNegoHttpClient(jaas, proxyHost, Integer.parseInt(proxyPort), "HTTP@"+proxyHost, null, false);
            HttpUriRequest request = new HttpGet("https://www.google.com");           
            response = httpclient.execute(request);

        } catch (IOException e) {
            LOG.error(e);
            response = null;
        } finally {
            if (httpclient != null) {
                //httpclient.close();
            }
        }
        assertTrue("Testing https request:", response != null && response.getStatusLine().getStatusCode() == 200);
    }
//
//    @Test
//    public void testRequestHttp() throws Exception {
//        LOG_TITLE.info(" --- Running one http request ---");
//        checkInputParameters();
//        HttpResponse response;
//        ProxySPNegoHttpClient httpclient = null;
//        try {
//            httpclient = new ProxySPNegoHttpClient(
//                    userID, new File(keytabFilePath), new File(ticketCachePath), proxyHost,
//                    Integer.parseInt(proxyPort)
//            );
//
//            HttpUriRequest request = new HttpGet("http://www.larousse.fr");
//            response = httpclient.execute(request);
//
//        } catch (IOException e) {
//            LOG.error(e);
//            response = null;
//        } finally {
//            if (httpclient != null) {
//                httpclient.close();
//            }
//        }
//        assertTrue("Testing http request: ",response != null && response.getStatusLine().getStatusCode() == 200);
//    }
//
//    @Test
//    public void testRequests() throws Exception {
//        LOG_TITLE.info(" -HttpHost-- Running several requests ---");
//        checkInputParameters();
//        int sum = 0;
//        ProxySPNegoHttpClient httpclient = new ProxySPNegoHttpClient(
//                userID, new File(keytabFilePath), new File(ticketCachePath), proxyHost,
//                Integer.parseInt(proxyPort), true
//        );
//
//        HttpUriRequest request = new HttpGet("http://www.larousse.fr");
//        HttpResponse response = httpclient.execute(request);
//        sum += response.getStatusLine().getStatusCode();
//
//        request = new HttpGet("https://www.nasa.gov");
//        response = httpclient.execute(request);
//        sum += response.getStatusLine().getStatusCode();
//
//        request = new HttpGet("https://www.google.com");
//        response = httpclient.execute(request);
//        sum += response.getStatusLine().getStatusCode();
//
//        httpclient.close();
//        
//        assertTrue("Testing http(s) requests: ", sum == 600);
//
//    }
    
    //@Test
    public void clientResourceHttp() throws Exception {  
        ClientResource cl = new ClientResource("http://www.larousse.fr");
        Representation rep = cl.get();
        String txt = rep.getText();
        System.out.println(txt);
        cl.release();
        assertTrue("Testing http restlet: ", txt.length()!=0);
    }
    
    //@Test
    public void clientResourceHttps() throws Exception {    
        //Engine.getInstance().getRegisteredClients().get(0).getContext()
        ClientResource cl = new ClientResource("https://www.google.com");
        Representation rep = cl.get();
        String txt = rep.getText();
        System.out.println(txt);
        cl.release();
        assertTrue("Testing https restlet: ", txt.length()!=0);
    }    
    
    
}
