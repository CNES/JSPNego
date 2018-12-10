package fr.cnes.httpclient;

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
import fr.cnes.httpclient.HttpClientFactory.Type;
import fr.cnes.httpclient.configuration.ProxySPNegoAPIConfiguration;
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
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HttpContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The client makes HTTP requests via a proxy for which the client is authenticated through a SSO.
 *
 * The SSO uses <i>The Simple and Protected GSS-API Negotiation Mechanism (IETF RFC 2478)</i>
 * (<b>SPNEGO</b>) as protocol.
 * <br>
 * <img src="https://cdn.ttgtmedia.com/digitalguide/images/Misc/kerberos_1.gif" alt="Kerberos">
 *
 * @author S. ETCHEVERRY
 * @author Jean-Christophe Malapert
 */
public final class ProxySPNegoHttpClientWithJAAS extends ProxyHttpClientWithoutAuth {
    
    /**
     * Get actual class name to be printed on.
     */
    private static final Logger LOG = LogManager.getLogger(ProxySPNegoHttpClientWithJAAS.class.getName());    

    /**
     * Default Kerberos configuration file {@value #KRB_CONF_PATH}.
     */
    private static final String KRB_CONF_PATH = "/etc/krb5.conf";

    /**
     * Environment variable that defines the path of krb5 configuration file: {@value #ENV_KRB5}.
     */
    private static final String ENV_KRB5 = "KRB5CCNAME";

    
    public ProxySPNegoHttpClientWithJAAS(final boolean isDisabledSSL) {
        super(ProxySPNegoJAASConfiguration.HTTP_PROXY.getValue(), ProxySPNegoJAASConfiguration.NO_PROXY.getValue(), isDisabledSSL);
        //final File krbConf = getKrbConf(krbConfPath);
//        final String noProxy = getNoProxy(type);
//        final HttpHost proxy = getProxy(type);
//        final List<String> excludedHosts = new ArrayList<>();        
//        Collections.addAll(excludedHosts, noProxy.split("\\s*,\\s*")); 
//        HttpClientBuilder builder = createBuilder(proxy, excludedHosts, isDisabledSSL);
//        builder = builder.setDefaultAuthSchemeRegistry(registerSPNegoProvider(type));
//        setHttpClient(builder.build());
//        setProxyConfiguration(proxy);
    }
    
    private String getNoProxy(final Type type) {
        final String noProxy;
        switch(type) {
            case PROXY_SPNEGO_API:
                noProxy = ProxySPNegoAPIConfiguration.NO_PROXY.getValue();
                break;
            case PROXY_SPNEGO_JAAS:
                noProxy = ProxySPNegoJAASConfiguration.NO_PROXY.getValue();
                break;
            default:
                throw new IllegalArgumentException(type.name()+" is not supported");
        }
        return noProxy;
    }
    
    private HttpHost getProxy(final Type type) {
        final HttpHost proxy;
        switch(type) {
            case PROXY_SPNEGO_API:
                proxy = buildProxy(ProxySPNegoAPIConfiguration.HTTP_PROXY.getValue());
                break;
            case PROXY_SPNEGO_JAAS:
                proxy = buildProxy(ProxySPNegoJAASConfiguration.HTTP_PROXY.getValue());
                break;
            default:
                throw new IllegalArgumentException(type.name()+" is not supported");
        }
        return proxy;
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


}
