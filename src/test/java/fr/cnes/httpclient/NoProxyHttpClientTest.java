/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.httpclient;

import fr.cnes.httpclient.configuration.ProxyConfiguration;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;
import org.junit.experimental.categories.Category;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.matchers.Times.exactly;
import org.mockserver.model.Header;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.StringBody.exact;
import org.mockserver.verify.VerificationTimes;

/**
 *
 * @author malapert
 */
@Category(UnitTest.class)
public class NoProxyHttpClientTest {

    private static ClientAndServer mockServer;

    public NoProxyHttpClientTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        mockServer = startClientAndServer(1080);
    }

    @AfterClass
    public static void tearDownClass() {
        mockServer.stop();
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    private void createExpectationForAuth() {
        new MockServerClient("127.0.0.1", 1080)
                .when(
                        request()
                                .withMethod("GET")  
                                .withPath("/test.html")
                )                       
                .respond(
                        response()
                                .withStatusCode(200)
                                .withBody("OK")
                                .withDelay(TimeUnit.SECONDS, 1)
                );
    }

    private void verifyGetRequest() {
        new MockServerClient("127.0.0.1", 1080).verify(
                request()
                        .withMethod("GET")
                        .withPath("/test.html"),
                VerificationTimes.exactly(1)
        );
    }

    @Test
    public void testSomeMethod() throws IOException {
        createExpectationForAuth();
        HttpClient client = HttpClientFactory.create(HttpClientFactory.Type.NO_PROXY);
        HttpResponse response = client.execute(new HttpGet("http://127.0.0.1:1080/test.html"));
        verifyGetRequest();
        assertTrue(response.getStatusLine().getStatusCode() == 200);
    }

}
