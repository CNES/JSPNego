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
import java.util.Map;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author malapert
 */
public class ProxyHttpClient extends AbstractProxyHttpClient {
    
    private static final Logger LOG = LogManager.getLogger(ProxyHttpClient.class.getName());
    
    public ProxyHttpClient(final boolean isDisabledSSL) {
        final List<String> excludedHosts = new ArrayList<>();
        final HttpHost proxy = new HttpHost(ProxyConfiguration.HTTP_PROXY.getValue());
        Collections.addAll(excludedHosts, ProxyConfiguration.NO_PROXY.getValue().split("\\s*,\\s*"));         
        HttpClientBuilder builder = HttpClients.custom()
                .setRoutePlanner(configureRouterPlanner(proxy, excludedHosts));
        if (isDisabledSSL) {
            LOG.warn("SSL Certificate checking is disabled. The connection is insecured.");
            builder = builder.setSSLContext(disableSSLCertificateChecking())
                    .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE);
        }
        setHttpClient(builder.build());
        setProxyConfiguration(proxy);        
    }
    
    private CredentialsProvider createCredsProvider(final Map<String, String> config) {
        final CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(
                new AuthScope(new HttpHost(ProxyConfiguration.HTTP_PROXY.getValue())),
                new UsernamePasswordCredentials(
                        ProxyConfiguration.USERNAME.getValue(), 
                        ProxyConfiguration.PASSWORD.getValue()
                )
        );
        return LOG.traceExit(credsProvider);
    }    
}
