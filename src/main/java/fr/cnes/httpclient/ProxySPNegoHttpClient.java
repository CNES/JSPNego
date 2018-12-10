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
public final class ProxySPNegoHttpClient extends AbstractProxyHttpClient {
    
    /**
     * Get actual class name to be printed on.
     */
    private static final Logger LOG = LogManager.getLogger(ProxySPNegoHttpClient.class.getName());    

    /**
     * Default Kerberos configuration file {@value #KRB_CONF_PATH}.
     */
    private static final String KRB_CONF_PATH = "/etc/krb5.conf";

    /**
     * Environment variable that defines the path of krb5 configuration file: {@value #ENV_KRB5}.
     */
    private static final String ENV_KRB5 = "KRB5CCNAME";

    
    public ProxySPNegoHttpClient(Type type, final boolean isDisabledSSL) {
        //final File krbConf = getKrbConf(krbConfPath);
        final String noProxy = getNoProxy(type);
        final HttpHost proxy = getProxy(type);
        final List<String> excludedHosts = new ArrayList<>();        
        Collections.addAll(excludedHosts, noProxy.split("\\s*,\\s*"));         
        HttpClientBuilder builder = HttpClients.custom()
                .setDefaultCredentialsProvider(createCredsProvider(proxy))
                .setRoutePlanner(configureRouterPlanner(proxy, excludedHosts))
                .setDefaultAuthSchemeRegistry(registerSPNegoProvider(type));
        if (isDisabledSSL) {
            LOG.warn("SSL Certificate checking is disabled. The connection is insecured.");
            builder = builder.setSSLContext(disableSSLCertificateChecking())
                    .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE);
        }
        setHttpClient(builder.build());
        setProxyConfiguration(proxy);
    }
    
    private String getNoProxy(Type type) {
        String noProxy;
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
    
    private HttpHost getProxy(Type type) {
        HttpHost proxy;
        switch(type) {
            case PROXY_SPNEGO_API:
                proxy = new HttpHost(ProxySPNegoAPIConfiguration.HTTP_PROXY.getValue());
                break;
            case PROXY_SPNEGO_JAAS:
                proxy = new HttpHost(ProxySPNegoJAASConfiguration.HTTP_PROXY.getValue());
                break;
            default:
                throw new IllegalArgumentException(type.name()+" is not supported");
        }
        return proxy;
    }    


//    /**
//     * Creates KRB configuration file location.
//     *
//     * @param krbConfPath KRB configuration file location from user.
//     * @return KRB configuration file location
//     */
//    private File getKrbConf(final File krbConfPath) {
//        LOG.traceEntry("krbConfPath: {}", krbConfPath);
//        final String defaultKrbConf;
//        if (Files.isReadable(Paths.get(System.getenv(ENV_KRB5)))) {
//            LOG.debug("default KRB conf is set to {}", ENV_KRB5);
//            defaultKrbConf = System.getenv(ENV_KRB5);
//        } else {
//            LOG.warn("environment variable {} is not set or not a readable file, "
//                    + "setting defaultKrbConf to {}", ENV_KRB5, KRB_CONF_PATH);
//            defaultKrbConf = KRB_CONF_PATH;
//        }
//        final String conf;
//        if (krbConfPath == null) {
//            conf = defaultKrbConf;
//        } else {
//            conf = KRB_CONF_PATH;
//        }
//        LOG.info("Loading krbConf : {}", conf);
//        return LOG.traceExit(new File(conf));
//    }

    /**
     * Creates a dummy kerberos credential provider for the kerberized proxy.
     *
     * @param proxy proxy configuration
     * @return a dummy kerberos credential provider for the kerberized proxy
     */
    private CredentialsProvider createCredsProvider(final HttpHost proxy) {
        LOG.traceEntry("proxy: {}", proxy);
        final CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(new AuthScope(proxy.getHostName(), proxy.getPort()),
                new KerberosCredentials(null));
        return LOG.traceExit(credsProvider);
    }

    /**
     * Registers the SPNego scheme.
     * @return the registry
     */
    private Registry<AuthSchemeProvider> registerSPNegoProvider(final Type type) {
//        LOG.traceEntry("userId: {}\n"
//                + "keytabFileName: {}\n"
//                + "ticketCache: {}\n"
//                + "krb5: {}\n"
//                + "spn: {}",
//                userId, keytabFileName, ticketCacheFileName, krbConfPath, spn);
        // init an registerSPNegoProviderDefaultHttp a SPNEGO auth scheme

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
                        return new SPNegoScheme(type);
                    }
                }).build());
    }

}
