/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.httpclient.integration;

import fr.cnes.httpclient.HttpClient;
import fr.cnes.httpclient.HttpClientFactory;
import fr.cnes.httpclient.configuration.ProxyConfiguration;
import fr.cnes.httpclient.configuration.ProxySPNegoAPIConfiguration;
import fr.cnes.httpclient.configuration.ProxySPNegoJAASConfiguration;
import java.io.IOException;
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

/**
 *
 * @author malapert
 */
@Category(IntegrationTest.class)
public class ITProxySPNegoHttpClientWithAPI {
    
    private static Properties properties;
    private static String host;
    private static String port;
    private static String spn;
    private static String login;    
    private static String keytab;    
    
    public ITProxySPNegoHttpClientWithAPI() {
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
        host = properties.getProperty(InitConfig.PROXY_API_HOST);
        port = properties.getProperty(InitConfig.PROXY_API_PORT);
        spn = properties.getProperty(InitConfig.PROXY_API_SPN);
        login = properties.getProperty(InitConfig.PROXY_API_LOGIN);        
        keytab = properties.getProperty(InitConfig.PROXY_API_KEYTAB);
        Assume.assumeTrue("SPNego (API) authentication for proxy is not configured", isProxyConfigured());
    }
    
    private boolean isProxyConfigured() {
        int error = 0;
        if(host == null || host.isEmpty() ) {
            System.out.println("Please, fill "+InitConfig.PROXY_API_HOST+" in config-it.properties");
            error++;
        }
        if(port == null || port.isEmpty() ) {
            System.out.println("Please, fill "+InitConfig.PROXY_API_PORT+" in config-it.properties");
            error++;
        }  
        if(spn == null || spn.isEmpty()) {
            System.out.println("Please, fill "+InitConfig.PROXY_API_SPN+" in config-it.properties");
            error++;
        } 
        if(login == null || login.isEmpty()) {
            System.out.println("Please, fill "+InitConfig.PROXY_API_LOGIN+" in config-it.properties");
            error++;
        }           
        if(keytab == null || keytab.isEmpty()) {
            System.out.println("Please, fill "+InitConfig.PROXY_API_KEYTAB+" in config-it.properties");
            error++;
        }           
        return error == 0;
    }  
    
    @After
    public void tearDown() {
    }

    @Test
    public void testSomeMethod() throws IOException {
        ProxySPNegoAPIConfiguration.HTTP_PROXY.setValue(host+":"+port);
        ProxySPNegoAPIConfiguration.KEY_TAB.setValue(keytab);
        ProxySPNegoAPIConfiguration.PRINCIPAL.setValue(login);
        ProxySPNegoAPIConfiguration.SERVICE_PROVIDER_NAME.setValue(spn);
        ProxySPNegoAPIConfiguration.USE_KEYTAB.setValue("true");
        HttpClient client = HttpClientFactory.create(HttpClientFactory.Type.PROXY_SPNEGO_API);
        HttpResponse response = client.execute(new HttpGet("https://www.google.fr"));
        HttpEntity entity = response.getEntity();
        String content = EntityUtils.toString(entity);
        System.out.println(content);
        assertTrue(response.getStatusLine().getStatusCode() == 200 && content.contains("<title>Google</title>"));
    }
    
}
