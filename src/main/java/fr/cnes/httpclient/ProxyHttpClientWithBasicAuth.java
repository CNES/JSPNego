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

import fr.cnes.httpclient.configuration.ProxyConfiguration;
import java.util.HashMap;
import java.util.Map;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.logging.log4j.CloseableThreadContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The client makes HTTP requests via a proxy with a basic authentication and configured by the
 * {@link fr.cnes.httpclient.configuration.ProxyConfiguration API}
 *
 * @author Jean-Christophe Malapert
 */
public class ProxyHttpClientWithBasicAuth extends ProxyHttpClientWithoutAuth {

    /**
     * Get actual class name to be printed on.
     */
    private static final Logger LOG = LogManager.getLogger(ProxyHttpClientWithBasicAuth.class.
            getName());

    /**
     * Creates a HTTP client using a proxy with a basic authentication.
     * The {@link fr.cnes.httpclient.configuration.ProxyConfiguration} must be configured before using this constructor.
     */
    public ProxyHttpClientWithBasicAuth() {
        this(false, new HashMap());
    }

    /**
     * Creates a HTTP client using a proxy with a basic authentication and options for HTTP client.
     * The {@link fr.cnes.httpclient.configuration.ProxyConfiguration} must be configured before using this constructor.
     * 
     * @param isDisabledSSL True when the SSL certificate check is disabled otherwise False.
     * @param config Options for HttpClient
     */
    public ProxyHttpClientWithBasicAuth(final boolean isDisabledSSL, final Map<String, String> config) {
        super(
                isDisabledSSL, 
                new HashMap() {{
                    putAll(config); 
                    putAll(ProxyConfiguration.getConfig());
                }}
        );
    }

    /**
     * Provides credentials by setting username/password.
     *
     * @param proxy HTTP proxy
     * @return credentials
     * @throws IllegalArgumentException when a validation error happens in ProxyConfiguration
     */
    @Override
    protected CredentialsProvider createCredsProvider(final HttpHost proxy) {
        LOG.traceEntry("proxy: {}", proxy);
        final StringBuilder error = new StringBuilder();
        final boolean isValid = ProxyConfiguration.isValid(error);
        if (!isValid) {
            LOG.error("Error validation : {}", error);
            throw LOG.throwing(new IllegalArgumentException(error.toString()));
        }
        final CredentialsProvider credsProvider = new BasicCredentialsProvider();
        LOG.debug(
                "Authentication with username={} pwd=**** on {}", 
                ProxyConfiguration.USERNAME.getValue(),proxy
        );
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
