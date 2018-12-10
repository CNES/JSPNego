/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.httpclient;

import fr.cnes.httpclient.configuration.ProxyConfiguration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthSchemeProvider;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.config.Registry;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author malapert
 */
public class ProxyHttpClientWithoutAuth extends AbstractProxyHttpClient {

    private static final Logger LOG = LogManager.getLogger(ProxyHttpClientWithoutAuth.class.getName());
    
    public ProxyHttpClientWithoutAuth(final boolean isDisabledSSL) {
        this(ProxyConfiguration.HTTP_PROXY.getValue(), ProxyConfiguration.NO_PROXY.getValue(), isDisabledSSL);      
    }
    
    public ProxyHttpClientWithoutAuth(final String proxyAsStr, final String excludedHostsAsStr, final boolean isDisabledSSL) {        
        final HttpHost proxy = buildProxy(proxyAsStr);
        final List<String> excludedHosts = new ArrayList<>();
        Collections.addAll(excludedHosts, excludedHostsAsStr.split("\\s*,\\s*"));         
        final HttpClientBuilder builder = createBuilder(proxy, excludedHosts, isDisabledSSL);
        setHttpClient(builder.build());
        setProxyConfiguration(proxy);          
    }
    
    @Override
    protected CredentialsProvider createCredsProvider(final HttpHost proxy) {
        return null;
    } 

    @Override
    protected Registry<AuthSchemeProvider> registerAuthSchemeProvider() {
        return null;
    }
    
}
