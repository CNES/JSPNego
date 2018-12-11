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
import org.apache.http.client.CredentialsProvider;
import org.apache.http.config.Registry;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The client makes HTTP requests via a proxy without authentication and configured by the
 * {@link fr.cnes.httpclient.configuration.ProxyConfiguration API}
 *
 * @author Jean-Christophe Malapert
 */
public class ProxyHttpClientWithoutAuth extends AbstractProxyHttpClient {

    /**
     * Get actual class name to be printed on.
     */
    private static final Logger LOG = LogManager.getLogger(ProxyHttpClientWithoutAuth.class.
            getName());

    public ProxyHttpClientWithoutAuth(final boolean isDisabledSSL) {
        super(isDisabledSSL);
    }

    /**
     * No credentials.
     *
     * @param proxy proxy
     * @return {@code null}
     */
    @Override
    protected CredentialsProvider createCredsProvider(final HttpHost proxy) {
        LOG.debug("Does not provide credentials");
        return null;
    }

    /**
     * No register authentication scheme.
     *
     * @return {@code null}
     */
    @Override
    protected Registry<AuthSchemeProvider> registerAuthSchemeProvider() {
        LOG.debug("Does not provide authentication scheme to register");
        return null;
    }

    @Override
    protected HttpClientBuilder createBuilderProxy(HttpClientBuilder builder) {
        LOG.traceEntry("builder : {}", builder);
        final HttpHost proxy = stringToProxy(ProxyConfiguration.HTTP_PROXY.getValue());
        final List<String> excludedHosts = new ArrayList<>();
        Collections.addAll(excludedHosts, ProxyConfiguration.NO_PROXY.getValue().split("\\s*,\\s*"));
        return LOG.traceExit(this.createBuilder(builder, proxy, excludedHosts));
    }

}
