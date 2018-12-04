/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.httpclient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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
    
    public enum DefaultConfiguration {
        HTTP_PROXY("http_proxy", System.getenv("http_proxy")),
        NO_PROXY("no_proxy", System.getenv("no_proxy")),
        USERNAME("username", ""),
        PASSWORD("password", "");

        private final String key;
        private final String value;

        DefaultConfiguration(final String key, final String value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return this.key;
        }

        public String getValue() {
            return this.value;
        }

        public static Map<String, String> getConfig() {
            final Map<String, String> map = new ConcurrentHashMap<>();
            final DefaultConfiguration[] confs = DefaultConfiguration.values();
            for (DefaultConfiguration conf : confs) {
                map.put(conf.getKey(), conf.getValue());
            }
            return map;
        }

    }
    
    public ProxyHttpClient(final Map<String, String> config) {
        this(config, false);
    }
    
    public ProxyHttpClient(final Map<String, String> config, final boolean isDisabledSSL) {
        final List<String> excludedHosts = new ArrayList<>();
        final HttpHost proxy = new HttpHost(config.get(DefaultConfiguration.HTTP_PROXY.getKey()));
        Collections.addAll(excludedHosts, config.get(DefaultConfiguration.NO_PROXY.getKey()).split("\\s*,\\s*"));         
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
                new AuthScope(new HttpHost(config.get(DefaultConfiguration.HTTP_PROXY.getKey()))),
                new UsernamePasswordCredentials(
                        config.get(DefaultConfiguration.USERNAME.getKey()), 
                        config.get(DefaultConfiguration.PASSWORD.getKey())
                )
        );
        return LOG.traceExit(credsProvider);
    }    
}
