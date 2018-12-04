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
import fr.cnes.jspnego.SPNegoScheme;
import java.io.File;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.auth.AuthScheme;

import org.apache.http.auth.AuthSchemeProvider;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.KerberosCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
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

    public enum DefaultConfiguration {
        HTTP_PROXY("http_proxy", System.getenv("http_proxy")),
        NO_PROXY("no_proxy", System.getenv("no_proxy")),
        REFRESH_KRB5_CONFIG("refreshKrb5Config", "true"),
        USE_TICKET_CACHE("useTicketCache", "false"),
        TICKET_CACHE("ticketCache", System.getenv("KRB5CCNAME")),
        RENEW_TGT("renewTGT", ""),
        DO_NOT_PROMPT("doNotPrompt", "true"),
        USE_KEYTAB("useKeyTab", "false"),
        KEY_TAB("keyTab", ""),
        STORE_KEY("storeKey", "true"),
        PRINCIPAL("principal", ""),
        IS_INITIATOR("isInitiator", "true"),
        SERVICE_PROVIDER_NAME("spn", "");

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

    /**
     * Default Kerberos configuration file {@value #KRB_CONF_PATH}.
     */
    private static final String KRB_CONF_PATH = "/etc/krb5.conf";

    /**
     * Environment variable that defines the path of krb5 configuration file: {@value #ENV_KRB5}.
     */
    private static final String ENV_KRB5 = "KRB5CCNAME";

    /**
     * configuration for proxy.
     */
    private RequestConfig config;

    public ProxySPNegoHttpClient(final File jassConf, final HttpHost proxy, final String noProxy,
            final String servicePrincipalName, final File krbConfPath, final boolean isDisabledSSL) {
        final File krbConf = getKrbConf(krbConfPath);
        final List<String> excludedHosts = new ArrayList<>();
        Collections.addAll(excludedHosts, noProxy.split("\\s*,\\s*"));         
        HttpClientBuilder builder = HttpClients.custom()
                .setDefaultCredentialsProvider(createCredsProvider(proxy))
                .setRoutePlanner(configureRouterPlanner(proxy, excludedHosts))
                .setDefaultAuthSchemeRegistry(
                        registerSPNegoProvider(
                                jassConf,
                                servicePrincipalName,
                                krbConf
                        )
                );
        if (isDisabledSSL) {
            LOG.warn("SSL Certificate checking is disabled. The connection is insecured.");
            builder = builder.setSSLContext(disableSSLCertificateChecking())
                    .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE);
        }
        setHttpClient(builder.build());
        setProxyConfiguration(proxy);
    }

    public ProxySPNegoHttpClient(final Map<String, String> config) {
        this(config, null, false);
    }

    public ProxySPNegoHttpClient(final Map<String, String> config, final boolean isDisabledSSL) {
        this(config, null, isDisabledSSL);
    }

    public ProxySPNegoHttpClient(final Map<String, String> config, final File krbConfPath,
            final boolean isDisabledSSL) {
        final File krbConf = getKrbConf(krbConfPath);
        final HttpHost proxy = new HttpHost(config.get(DefaultConfiguration.HTTP_PROXY.getKey()));
        final List<String> excludedHosts = new ArrayList<>();
        Collections.addAll(excludedHosts, config.get(DefaultConfiguration.NO_PROXY.getKey()).split(
                "\\s*,\\s*"));        
        HttpClientBuilder builder = HttpClients.custom()
                .setDefaultCredentialsProvider(createCredsProvider(proxy))
                .setRoutePlanner(configureRouterPlanner(proxy, excludedHosts))
                .setDefaultAuthSchemeRegistry(
                        registerSPNegoProvider(
                                config,
                                krbConf
                        )
                );
        if (isDisabledSSL) {
            LOG.warn("SSL Certificate checking is disabled. The connection is insecured.");
            builder = builder.setSSLContext(disableSSLCertificateChecking())
                    .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE);
        }
        setHttpClient(builder.build());
        setProxyConfiguration(proxy);
    }

    /**
     * Creates KRB configuration file location.
     *
     * @param krbConfPath KRB configuration file location from user.
     * @return KRB configuration file location
     */
    private File getKrbConf(final File krbConfPath) {
        LOG.traceEntry("krbConfPath: {}", krbConfPath);
        final String defaultKrbConf;
        if (Files.isReadable(Paths.get(System.getenv(ENV_KRB5)))) {
            LOG.debug("default KRB conf is set to {}", ENV_KRB5);
            defaultKrbConf = System.getenv(ENV_KRB5);
        } else {
            LOG.warn("environment variable {} is not set or not a readable file, "
                    + "setting defaultKrbConf to {}", ENV_KRB5, KRB_CONF_PATH);
            defaultKrbConf = KRB_CONF_PATH;
        }
        final String conf;
        if (krbConfPath == null) {
            conf = defaultKrbConf;
        } else {
            conf = KRB_CONF_PATH;
        }
        LOG.info("Loading krbConf : {}", conf);
        return LOG.traceExit(new File(conf));
    }

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
     *
     * @param userId User ID for authentication to authentication service
     * @param keytabFileName Keytab filename that contains the kerberos ticket
     * @param ticketCacheFileName ticket cache filename
     * @param krbConfPath krb conf path
     * @param spn Service Principal Name
     * @return the registry
     */
    private Registry<AuthSchemeProvider> registerSPNegoProvider(final Map<String, String> config,
            final File krbConfPath) {
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
                        return new SPNegoScheme(config, krbConfPath);
                    }
                }).build());
    }

    private Registry<AuthSchemeProvider> registerSPNegoProvider(File jassConf,
            String servicePrincipalName,
            File krbConfPath) {
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
                        return new SPNegoScheme(jassConf, servicePrincipalName, krbConfPath);
                    }
                }).build());
    }
}
