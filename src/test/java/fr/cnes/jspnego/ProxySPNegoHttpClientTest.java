/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.jspnego;

import java.io.File;
import java.io.IOException;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

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
    }

    @Test
    public void testRequestHttps() throws Exception {
        LOG_TITLE.info(" --- Running one https request ---");
        checkInputParameters();
        HttpResponse response;
        ProxySPNegoHttpClient httpclient = null;
        try {
            httpclient = new ProxySPNegoHttpClient(
                    userID, new File(keytabFilePath), new File(ticketCachePath), proxyHost,
                    Integer.parseInt(proxyPort)
            );

            HttpHost target = new HttpHost("www.google.com", 443, "https");
            response = httpclient.execute(target);

        } catch (IOException e) {
            LOG.error(e);
            response = null;
        } finally {
            if (httpclient != null) {
                httpclient.close();
            }
        }
        assertTrue("Testing https request:", response != null && response.getStatusLine().getStatusCode() == 200);
    }

    @Test
    public void testRequestHttp() throws Exception {
        LOG_TITLE.info(" --- Running one http request ---");
        checkInputParameters();
        HttpResponse response;
        ProxySPNegoHttpClient httpclient = null;
        try {
            httpclient = new ProxySPNegoHttpClient(
                    userID, new File(keytabFilePath), new File(ticketCachePath), proxyHost,
                    Integer.parseInt(proxyPort)
            );

            HttpHost target = new HttpHost("www.larousse.fr", 80, "http");
            response = httpclient.execute(target);

        } catch (IOException e) {
            LOG.error(e);
            response = null;
        } finally {
            if (httpclient != null) {
                httpclient.close();
            }
        }
        assertTrue("Testing http request: ",response != null && response.getStatusLine().getStatusCode() == 200);
    }

    @Test
    public void testRequests() throws Exception {
        LOG_TITLE.info(" --- Running several requests ---");
        checkInputParameters();
        int sum = 0;
        ProxySPNegoHttpClient httpclient = new ProxySPNegoHttpClient(
                userID, new File(keytabFilePath), new File(ticketCachePath), proxyHost,
                Integer.parseInt(proxyPort), true
        );

        HttpHost target = new HttpHost("www.larousse.fr", 80, "http");
        HttpResponse response = httpclient.execute(target);
        sum += response.getStatusLine().getStatusCode();

        target = new HttpHost("www.nasa.gov", 443, "https");
        response = httpclient.execute(target);
        sum += response.getStatusLine().getStatusCode();

        target = new HttpHost("www.google.com", 443, "https");
        response = httpclient.execute(target);
        sum += response.getStatusLine().getStatusCode();

        httpclient.close();
        
        assertTrue("Testing http(s) requests: ", sum == 600);

    }
    
    @Test
    @Ignore
    public void testRequestHttpsDefaultHttpClient() throws Exception {
        LOG_TITLE.info(" --- Running one https request for Default HttpClient---");
        checkInputParameters();
        DefaultHttpClient defaultHttpClient = new DefaultHttpClient();
        HttpResponse response;        
        ProxySPNegoHttpClient httpclient = null;
        try {
            httpclient = new ProxySPNegoHttpClient(
                    userID, new File(keytabFilePath), new File(ticketCachePath), proxyHost,
                    Integer.parseInt(proxyPort), defaultHttpClient
            );

            HttpHost target = new HttpHost("www.google.com", 443, "https");
            response = httpclient.execute(target);

        } catch (IOException e) {
            LOG.error(e);
            response = null;
        } finally {
            if (httpclient != null) {
                httpclient.close();
            }
        }
        assertTrue("Testing https request:", response != null && response.getStatusLine().getStatusCode() == 200);
    }

    @Test
    @Ignore
    public void testRequestHttpDefaultHttpClient() throws Exception {
        LOG_TITLE.info(" --- Running one http request for defaultHttpClient ---");
        checkInputParameters();
        HttpResponse response;
        DefaultHttpClient defaultHttpClient = new DefaultHttpClient();
        ProxySPNegoHttpClient httpclient = null;
        try {
            httpclient = new ProxySPNegoHttpClient(
                    userID, new File(keytabFilePath), new File(ticketCachePath), proxyHost,
                    Integer.parseInt(proxyPort), defaultHttpClient
            );

            HttpHost target = new HttpHost("www.larousse.fr", 80, "http");
            response = httpclient.execute(target);

        } catch (IOException e) {
            LOG.error(e);
            response = null;
        } finally {
            if (httpclient != null) {
                httpclient.close();
            }
        }
        assertTrue("Testing http request: ",response != null && response.getStatusLine().getStatusCode() == 200);
    }    

}
