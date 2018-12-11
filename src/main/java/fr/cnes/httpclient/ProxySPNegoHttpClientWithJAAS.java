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
import fr.cnes.httpclient.configuration.ProxySPNegoJAASConfiguration;
import fr.cnes.jspnego.SPNegoScheme;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
 * The client makes HTTP requests via a proxy for which the client is authenticated through a SSO an
 * configured by a JAAS configuration file and the
 * {@link fr.cnes.httpclient.configuration.ProxySPNegoJAASConfiguration API}.
 *
 * The SSO uses <i>The Simple and Protected GSS-API Negotiation Mechanism (IETF RFC 2478)</i>
 * (<b>SPNEGO</b>) as protocol.
 * <br>
 * <img src="https://cdn.ttgtmedia.com/digitalguide/images/Misc/kerberos_1.gif" alt="Kerberos">
 *
 * <p>
 * The configuration file has this syntax :
 * <pre>{@code
 * <entry name> {
 *  com.sun.security.auth.module.Krb5LoginModule required
 *  <key>=<value>
 * };
 * }
 * where
 * - <i>entry name</i> is the {@link fr.cnes.httpclient.configuration.ProxySPNegoJAASConfiguration#JAAS_CONTEXT}
 * - <i>key</i>=<i>value</i> are defined <a href="https://docs.oracle.com/javase/7/docs/jre/api/security/jaas/spec/com/sun/security/auth/module/Krb5LoginModule.html">here</a>
 * </pre>
 *
 * @author S. ETCHEVERRY
 * @author Jean-Christophe Malapert
 */
public final class ProxySPNegoHttpClientWithJAAS extends ProxyHttpClientWithoutAuth {

    /**
     * Get actual class name to be printed on.
     */
    private static final Logger LOG = LogManager.getLogger(ProxySPNegoHttpClientWithJAAS.class.
            getName());

    /**
     * Creates a HTTP client that makes requests to a proxy authenticated with SSO and configured by
     * a JAAAS configuration file. Uses
     * {@link fr.cnes.httpclient.configuration.ProxySPNegoJAASConfiguration} class to configure this
     * proxy.
     *
     * @param isDisabledSSL True when the SSL certificate check is disabled otherwise False.
     */
    public ProxySPNegoHttpClientWithJAAS(final boolean isDisabledSSL) {
        super(isDisabledSSL);
    }

    /**
     * {@inheritDoc}
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
                        return new SPNegoScheme(Type.PROXY_SPNEGO_JAAS);
                    }
                }).build());
    }

    /**
     * Creates a proxy builder based on
     * {@link fr.cnes.httpclient.configuration.ProxySPNegoJAASConfiguration}. This method calls {@link AbstractProxyHttpClient#createBuilder(org.apache.http.impl.client.HttpClientBuilder, org.apache.http.HttpHost, java.util.List)
     * }
     *
     * @param builder builder
     * @return builder including proxy
     * @throws IllegalArgumentException when a validation error happens in ProxySPNegoJAASConfiguration
     */
    @Override
    protected HttpClientBuilder createBuilderProxy(final HttpClientBuilder builder) {
        LOG.traceEntry("builder: {}", builder);
        final StringBuilder error = new StringBuilder();
        final boolean isValid = ProxySPNegoJAASConfiguration.isValid(error);
        if (!isValid) {
            LOG.error("Error validation : {}", error);
            throw LOG.throwing(new IllegalArgumentException(error.toString()));
        }        
        final HttpHost proxy = stringToProxy(ProxySPNegoJAASConfiguration.HTTP_PROXY.getValue());
        final List<String> excludedHosts = new ArrayList<>();
        Collections.addAll(excludedHosts, ProxySPNegoJAASConfiguration.NO_PROXY.getValue().split(
                "\\s*,\\s*"));
        return LOG.traceExit(this.createBuilder(builder, proxy, excludedHosts));
    }

}
