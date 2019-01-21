/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.jspnego;

import fr.cnes.httpclient.HttpClient;
import fr.cnes.httpclient.HttpClientFactory;
import fr.cnes.httpclient.ProxySPNegoHttpClientWithJAAS;
import fr.cnes.httpclient.configuration.ProxySPNegoJAASConfiguration;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
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
//@Category(UnitTest.class)
public class ProxySPNegoHttpClientTest {

    /**
     * Get actual class name to be printed on.
     */
    private static final Logger LOG = LogManager.
            getLogger(ProxySPNegoHttpClientTest.class.getName());
    private static final Logger LOG_TITLE = LogManager.getLogger("testTitle");

    //private static final String proxyHost = System.getProperty("proxyHost");
    //private static final String proxyPort = System.getProperty("proxyPort");
    //private static final String userID = System.getProperty("userID");
    //private static final String keytabFilePath = System.getProperty("keytabFilePath");
    //private static final String ticketCachePath = System.getProperty("ticketCachePath");
    //private static final String principal = System.getProperty("principal");

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

//    private void checkInputParameters() throws Exception {
//        int error = 0;
//        if (proxyHost == null || proxyHost.isEmpty()) {
//            LOG.warn("Please add -DproxyHost=<proxyHost> to your command line");
//            error++;
//        }
//        if (proxyPort == null || proxyPort.isEmpty()) {
//            LOG.warn("Please add -DproxyPort=<proxyPort> to your command line");
//            error++;
//        }
//
//        if (error > 0) {
//            throw new Exception("Missing input parameters");
//        }
//
//        ProxySPNegoJAASConfiguration.HTTP_PROXY.setValue(proxyHost + ":" + proxyPort);
//        ProxySPNegoJAASConfiguration.JAAS_CONTEXT.setValue("KRB5");
//        ProxySPNegoJAASConfiguration.JAAS.setValue("/tmp/jaas.conf");
//        ProxySPNegoJAASConfiguration.SERVICE_PROVIDER_NAME.setValue("HTTP@" + proxyHost);
//    }

//    @Test
//    @Ignore
//    public void testRequestHttps() throws Exception {
//        LOG_TITLE.info(" --- Running one https request ---");
//        //File jaas = new File("/tmp/jaas.conf");
//        ProxySPNegoJAASConfiguration.HTTP_PROXY.setValue(proxyHost + ":" + proxyPort);
//        ProxySPNegoJAASConfiguration.JAAS_CONTEXT.setValue("KRB5");
//        ProxySPNegoJAASConfiguration.JAAS.setValue("/tmp/jaas.conf");
//        ProxySPNegoJAASConfiguration.SERVICE_PROVIDER_NAME.setValue("HTTP@" + proxyHost);
//        HttpResponse response;
//        try (HttpClient httpclient = new ProxySPNegoHttpClientWithJAAS(false)) {
//            HttpUriRequest request = new HttpGet("https://www.google.com");
//            response = httpclient.execute(request);
//
//        } catch (IOException e) {
//            LOG.error(e);
//            response = null;
//        }
//        assertTrue("Testing https request:", response != null && response.getStatusLine().
//                getStatusCode() == 200);
//    }
   
}
