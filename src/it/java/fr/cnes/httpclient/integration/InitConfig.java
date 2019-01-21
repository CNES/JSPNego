package fr.cnes.httpclient.integration;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author malapert
 */
public class InitConfig {
    
    public final static String PROXY_HOST = "Starter.Proxy.host";
    public final static String PROXY_PORT = "Starter.Proxy.port";
    public final static String PROXY_LOGIN = "Starter.Proxy.login";
    public final static String PROXY_PWD = "Starter.Proxy.pwd";
    public final static String PROXY_NOPROXY = "Starter.NoProxy.hosts";
    
    public final static String PROXY_JAAS_HOST = "Starter.Proxy.Jaas.host";    
    public final static String PROXY_JAAS_PORT = "Starter.Proxy.Jaas.port";       
    public final static String PROXY_JAAS_SPN = "Starter.Proxy.Jaas.Spn";
    public final static String PROXY_JAAS_FILE = "Starter.Proxy.Jaas.File";
    public final static String PROXY_JAAS_CONTEXT = "Starter.Proxy.Jaas.Context";
    public final static String PROXY_JAAS_NOPROXY = "Starter.NoProxy.Jaas.hosts";

    public final static String PROXY_API_HOST = "Starter.Proxy.API.host";    
    public final static String PROXY_API_PORT = "Starter.Proxy.API.port";       
    public final static String PROXY_API_SPN = "Starter.Proxy.API.Spn";
    public final static String PROXY_API_LOGIN = "Starter.Proxy.API.Login";
    public final static String PROXY_API_KEYTAB = "Starter.Proxy.API.KeyTab";
    public final static String PROXY_API_NOPROXY = "Starter.NoProxy.API.hosts";       
       
    public final static String CONFIG_IT = "config-it.properties";
    
    public static Properties getProperties(final String filename) throws IOException {
        final Properties properties = new Properties();
        try (InputStream inputStream = ITProxyHttpClientWithBasicAuth.class.getResourceAsStream(
                "/"+CONFIG_IT)) {
            properties.load(inputStream);
        } 
        return properties;
    }
    
}
