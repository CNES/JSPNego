/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.httpclient.integration;

import fr.cnes.httpclient.HttpClient;
import fr.cnes.httpclient.HttpClientFactory;
import fr.cnes.httpclient.configuration.ProxyConfiguration;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
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
import org.junit.Assume;
import org.junit.experimental.categories.Category;

@Category(IntegrationTest.class)
public class ITProxyHttpClientWithBasicAuth {

    private static Properties properties;
    private static String host;
    private static String port;
    private static String login;
    private static String pwd;

    public ITProxyHttpClientWithBasicAuth() {
    }

    @BeforeClass
    public static void setUpClass() throws IOException {    
        properties = InitConfig.getProperties(InitConfig.CONFIG_IT);
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        host = properties.getProperty(InitConfig.PROXY_HOST);
        port = properties.getProperty(InitConfig.PROXY_PORT);
        login = properties.getProperty(InitConfig.PROXY_LOGIN);
        pwd = properties.getProperty(InitConfig.PROXY_PWD);
        Assume.assumeTrue("HTTP basic authentication for proxy is not configured", isProxyConfigured());
    }
    
    private boolean isProxyConfigured() {
        int error = 0;
        if(host == null || host.isEmpty() ) {
            System.out.println("Please, fill "+InitConfig.PROXY_HOST+" in config-it.properties");
            error++;
        }
        if(port == null || port.isEmpty() ) {
            System.out.println("Please, fill "+InitConfig.PROXY_PORT+" in config-it.properties");
            error++;
        }  
        if(login == null || login.isEmpty()) {
            System.out.println("Please, fill "+InitConfig.PROXY_LOGIN+" in config-it.properties");
            error++;
        } 
        if(pwd == null || pwd.isEmpty()) {
            System.out.println("Please, fill "+InitConfig.PROXY_PWD+" in config-it.properties");
            error++;
        }           
        return error == 0;
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testSomeMethod() throws IOException {
        ProxyConfiguration.HTTP_PROXY.setValue(host+":"+port);
        ProxyConfiguration.USERNAME.setValue(login);
        ProxyConfiguration.PASSWORD.setValue(pwd);
        HttpClient client = HttpClientFactory.create(HttpClientFactory.Type.PROXY_BASIC);
        HttpResponse response = client.execute(new HttpGet("https://www.google.fr"));
        HttpEntity entity = response.getEntity();
        String content = EntityUtils.toString(entity);
        System.out.println(content);
        assertTrue(response.getStatusLine().getStatusCode() == 200 && content.contains("<title>Google</title>"));
    }

}
