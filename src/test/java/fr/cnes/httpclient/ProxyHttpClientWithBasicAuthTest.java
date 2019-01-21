/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.httpclient;

import fr.cnes.httpclient.configuration.ProxyConfiguration;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.experimental.categories.Category;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.integration.ClientAndProxy;
import org.mockserver.integration.ClientAndServer;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import org.mockserver.model.Header;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import org.mockserver.verify.VerificationTimes;

/**
 *
 * @author malapert
 */
@Category(UnitTest.class)
public class ProxyHttpClientWithBasicAuthTest {

    private static ClientAndProxy mockServerProxy;
    private static ClientAndServer mockServerTarget;

    public ProxyHttpClientWithBasicAuthTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        mockServerProxy = ClientAndProxy.startClientAndDirectProxy(1080, "127.0.0.1",1081);
        mockServerTarget = startClientAndServer(1081);
    }

    @AfterClass
    public static void tearDownClass() {
        mockServerProxy.stop();
        mockServerTarget.stop();
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }
    
    private void createExpectationForTarget() {
        new MockServerClient("127.0.0.1", 1081)
                .when(
                        request()
                                .withMethod("GET")  
                                .withPath("/")                                                              
                )                       
                .respond(
                        response()
                                .withStatusCode(200)
                                .withBody("OK target").
                                withDelay(TimeUnit.SECONDS, 1)
                );        
    }

    private void createExpectationForAuth() {
        new MockServerClient("127.0.0.1", 1080)
                .when(
                        request()
                                .withMethod("GET")  
                                .withPath("/")
                                .withHeaders(
                                        new Header("Host", "127.0.0.1:1081")//,
                                        //new Header("Authorization", "Basic Zm9vOmJhcg==")
                                )                                
                )                       
                .respond(
                        response()
                                .withStatusCode(200)
                                .withBody("OK").
                                withDelay(TimeUnit.SECONDS, 1)
                );
    }

    private void verifyGetRequest() {
        new MockServerClient("127.0.0.1", 1080).verify(
                request()
                        .withMethod("GET")
                        .withPath("/")
                        .withHeaders(
                                new Header("Host", "127.0.0.1:1081")//,
                                //new Header("Authorization", "Basic Zm9vOmJhcg==")
                        ),
                VerificationTimes.exactly(1)
        );
    }

    @Test
    public void testSomeMethod() throws IOException {
        createExpectationForTarget();
        createExpectationForAuth();
        ProxyConfiguration.HTTP_PROXY.setValue("127.0.0.1:1080");
        ProxyConfiguration.USERNAME.setValue("foo");
        ProxyConfiguration.PASSWORD.setValue("bar");
        HttpClient client = HttpClientFactory.create(HttpClientFactory.Type.PROXY_BASIC);
        HttpResponse response = client.execute(new HttpGet("http://127.0.0.1:1081"));
        HttpEntity entity = response.getEntity();
        String content = EntityUtils.toString(entity);
        verifyGetRequest();
        assertTrue(response.getStatusLine().getStatusCode() == 200 && content.equals("OK target"));
    }

}
