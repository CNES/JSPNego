/*
 * Copyright (C) 2017-2019 Centre National d'Etudes Spatiales (CNES).
 *
 * This file is part of DOI-server.
 *
 * This JSPNego is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * JSPNego is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package fr.cnes.httpclient.integration;

import fr.cnes.httpclient.HttpClient;
import fr.cnes.httpclient.HttpClientFactory;
import fr.cnes.httpclient.ProxyHttpClientWithBasicAuth;
import fr.cnes.httpclient.configuration.ProxyConfiguration;
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
    public void testRequestFactory() throws IOException {
        ProxyConfiguration.HTTP_PROXY.setValue(host+":"+port);
        ProxyConfiguration.USERNAME.setValue(login);
        ProxyConfiguration.PASSWORD.setValue(pwd);
        HttpClient client = HttpClientFactory.create(HttpClientFactory.Type.PROXY_BASIC);
        HttpResponse response = client.execute(new HttpGet("https://www.google.fr"));
        HttpEntity entity = response.getEntity();
        String content = EntityUtils.toString(entity);
        client.close();
        assertTrue(response.getStatusLine().getStatusCode() == 200 && content.contains("<title>Google</title>"));
    }
    
    @Test
    public void testRequestBasicAuth() throws IOException {
        ProxyConfiguration.HTTP_PROXY.setValue(host+":"+port);
        ProxyConfiguration.USERNAME.setValue(login);
        ProxyConfiguration.PASSWORD.setValue(pwd);
        HttpClient client = new ProxyHttpClientWithBasicAuth();
        HttpResponse response = client.execute(new HttpGet("https://www.google.fr"));
        HttpEntity entity = response.getEntity();
        String content = EntityUtils.toString(entity);
        client.close();
        assertTrue(response.getStatusLine().getStatusCode() == 200 && content.contains("<title>Google</title>"));
    }    
    
    @Test
    public void testRequestPerfo() throws IOException {
        long startTime = System.currentTimeMillis();
        ProxyConfiguration.HTTP_PROXY.setValue(host+":"+port);
        ProxyConfiguration.USERNAME.setValue(login);
        ProxyConfiguration.PASSWORD.setValue(pwd);
        HttpClient client = HttpClientFactory.create(HttpClientFactory.Type.PROXY_BASIC);     
        int nbRequestOK = 0;
        //The client is using a pool of connection to reach the web server. 
        //See HttpClientBuilder#build(). When creating a default httpclient and nothing is specified
        //it creates a pool with size of 2. So after 2 is used, it waits indefinitely trying to get 
        //the third connection from the pool.You must read the response or close the connection, in 
        //order to re-use the client object.
        for (int i=0 ; i<50; i++) {
            HttpResponse response = client.execute(new HttpGet("https://www.google.fr"));
            if (response.getStatusLine().getStatusCode() == 200) {
                nbRequestOK++;
            }
            response.getEntity().getContent().close();
        }
        client.close();
        long stopTime = System.currentTimeMillis();
        long runTime = stopTime - startTime;
        System.out.println("Mean run time per request: "+runTime/50f/1000f+" s");
        assertTrue(nbRequestOK == 50 && runTime/50f < 1000);
    }     

}
