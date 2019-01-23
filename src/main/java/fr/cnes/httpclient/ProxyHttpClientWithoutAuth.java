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
package fr.cnes.httpclient;

import fr.cnes.httpclient.HttpClientFactory.Type;
import fr.cnes.httpclient.configuration.ProxyConfiguration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    /**
     * Creates a HTTP client using a proxy with no authentication.
     * The {@link fr.cnes.httpclient.configuration.ProxyConfiguration} must be configured before using this constructor.
     */
    public ProxyHttpClientWithoutAuth() {
        this(false);
    }

    /**
     * Creates a HTTP client using a proxy with no authentication.
     * The {@link fr.cnes.httpclient.configuration.ProxyConfiguration} must be configured before using this constructor.
     * @param isDisabledSSL True when the SSL certificate check is disabled otherwise False.
     */
    public ProxyHttpClientWithoutAuth(final boolean isDisabledSSL) {
        this(isDisabledSSL, new HashMap());
    }
    
    /**
     * Creates a HTTP client using a proxy with no authentication and options for HTTP client.
     * The {@link fr.cnes.httpclient.configuration.ProxyConfiguration} must be configured before using this constructor.
     * @param isDisabledSSL True when the SSL certificate check is disabled otherwise False.
     * @param config options for HTTP client
     */
    public ProxyHttpClientWithoutAuth(final boolean isDisabledSSL, final Map<String, String> config) {
        this(
                isDisabledSSL, 
                new HashMap(){{
                    putAll(config);
                    putAll(ProxyConfiguration.getConfig());
                }},
                Type.PROXY_BASIC
        );
    }    

    /**
     * Creates a HTTP client using a proxy with no authentication and options for HTTP client.
     *
     * @param isDisabledSSL True when the SSL certificate check is disabled otherwise False.
     * @param config options for HTTP client that must contain proxy parameters
     * @param type proxy type
     */
    protected ProxyHttpClientWithoutAuth(final boolean isDisabledSSL, final Map<String, String> config, final Type type) {
        super(isDisabledSSL, config, type);
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

    /**
     * Creates proxy builder without authentication.
     *
     * @param builder builder
     * @return builder
     * @throws IllegalArgumentException when a validation error happens in ProxyConfiguration
     */
    @Override
    protected HttpClientBuilder createBuilderProxy(final HttpClientBuilder builder) {
        LOG.traceEntry("builder : {}", builder);
        final StringBuilder error = new StringBuilder();
        final boolean isValid = ProxyConfiguration.isValid(error);
        if (!isValid) {
            LOG.error("Error validation : {}", error);
            throw LOG.throwing(new IllegalArgumentException(error.toString()));
        }
        final HttpHost proxy = stringToProxy(ProxyConfiguration.HTTP_PROXY.getValue());
        final List<String> excludedHosts = new ArrayList<>();
        Collections.addAll(excludedHosts, ProxyConfiguration.NO_PROXY.getValue().split("\\s*,\\s*"));
        return LOG.traceExit(this.createBuilder(builder, proxy, excludedHosts));
    }

}
