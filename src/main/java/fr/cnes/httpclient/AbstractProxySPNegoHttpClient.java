/*
 * Copyright (C) 2017-2018 Centre National d'Etudes Spatiales (CNES).
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
import fr.cnes.httpclient.configuration.ProxySPNegoAPIConfiguration;
import fr.cnes.httpclient.configuration.ProxySPNegoJAASConfiguration;
import fr.cnes.jspnego.SPNegoScheme;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScheme;
import org.apache.http.auth.AuthSchemeProvider;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.KerberosCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HttpContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * SPNego interface
 *
 * @author Jean-Christophe Malapert
 */
public abstract class AbstractProxySPNegoHttpClient extends ProxyHttpClientWithoutAuth {

    /**
     * Get actual class name to be printed on.
     */
    private static final Logger LOG = LogManager.getLogger(AbstractProxySPNegoHttpClient.class.
            getName());

    /**
     * Creates an AbstractProxySPNegoHttpClient
     *
     * @param isDisabledSSL True when SSL certificates are disabled otherwise False
     * @param type Type of SPNego
     */
    protected AbstractProxySPNegoHttpClient(final boolean isDisabledSSL, final Type type) {
        this(isDisabledSSL, type, new HashMap());
    }

    /**
     * Creates an AbstractProxySPNegoHttpClient based on options for Http client.
     *
     * @param isDisabledSSL True when SSL certificates are disabled otherwise False
     * @param type Type of SPNego
     * @param config options for Http client
     */
    protected AbstractProxySPNegoHttpClient(final boolean isDisabledSSL, final Type type,
            final Map<String, String> config) {
        super(isDisabledSSL, config, type);
    }

    /**
     * Creates a dummy kerberos credential provider for the kerberized proxy.
     *
     * @param proxy proxy configuration
     * @return a dummy kerberos credential provider for the kerberized proxy
     */
    @Override
    protected CredentialsProvider createCredsProvider(final HttpHost proxy) {
        LOG.traceEntry("proxy: {}", proxy);
        final CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(new AuthScope(proxy.getHostName(), proxy.getPort()),
                new KerberosCredentials(null));
        return LOG.traceExit(credsProvider);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Registry<AuthSchemeProvider> registerAuthSchemeProvider() {
        return LOG.traceExit(RegistryBuilder.
                <AuthSchemeProvider>create()
                .register(AuthSchemes.SPNEGO, new AuthSchemeProvider() {
                    /**
                     * Creates an authentication scheme.
                     *
                     * @param context context
                     * @return the authentication scheme
                     */
                    @Override
                    public AuthScheme create(final HttpContext context) {
                        return new SPNegoScheme(getType());
                    }
                }).build());
    }

    /**
     * Creates a proxy builder
     *
     * @param builder builder
     * @return builder including proxy
     * @throws IllegalArgumentException when an unknow type or when a validation error happens in
     * ProxySPNegoJAASConfiguration or ProxySPNegoAPIConfiguration
     */
    @Override
    protected HttpClientBuilder createBuilderProxy(final HttpClientBuilder builder) {
        LOG.traceEntry("builder: {}", builder);
        final StringBuilder error = new StringBuilder();
        final boolean isValid;
        final String proxyStr;
        final String noProxyStr;
        switch (this.getType()) {
            case PROXY_SPNEGO_API:
                isValid = ProxySPNegoAPIConfiguration.isValid(error);
                proxyStr = ProxySPNegoAPIConfiguration.HTTP_PROXY.getValue();
                noProxyStr = ProxySPNegoAPIConfiguration.NO_PROXY.getValue();
                LOG.debug("set proxy from SPNEGO_API : "+proxyStr);
                LOG.debug("set noproxy from SPNEGO_API : "+noProxyStr);
                break;
            case PROXY_SPNEGO_JAAS:
                isValid = ProxySPNegoJAASConfiguration.isValid(error);
                proxyStr = ProxySPNegoJAASConfiguration.HTTP_PROXY.getValue();
                noProxyStr = ProxySPNegoJAASConfiguration.NO_PROXY.getValue();
                LOG.debug("set proxy from SPNEGO_JAAS : "+proxyStr);
                LOG.debug("set noproxy from SPNEGO_JAAS : "+noProxyStr);                
                break;
            default:
                throw new IllegalArgumentException(this.getType().name() + " is not supported");
        }
        if (!isValid) {
            LOG.error("Error validation : {}", error);
            throw LOG.throwing(new IllegalArgumentException(error.toString()));
        }
        final HttpHost proxy = stringToProxy(proxyStr);
        final List<String> excludedHosts = new ArrayList<>();
        Collections.addAll(excludedHosts, noProxyStr.split("\\s*,\\s*"));
        return LOG.traceExit(this.createBuilder(builder, proxy, excludedHosts));
    }

}
