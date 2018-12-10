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
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author malapert
 */
public class ProxyHttpClientWithBasicAuth extends ProxyHttpClientWithoutAuth {
    
    private static final Logger LOG = LogManager.getLogger(ProxyHttpClientWithBasicAuth.class.getName());
    
    public ProxyHttpClientWithBasicAuth(final boolean isDisabledSSL) {
        super(isDisabledSSL);
    }
    
    @Override
    protected CredentialsProvider createCredsProvider(final HttpHost proxy) {
        final CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(
                new AuthScope(proxy),
                new UsernamePasswordCredentials(
                        ProxyConfiguration.USERNAME.getValue(), 
                        ProxyConfiguration.PASSWORD.getValue()
                )
        );
        return LOG.traceExit(credsProvider);
    }    
}
