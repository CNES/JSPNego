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
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScheme;

import org.apache.http.auth.AuthSchemeProvider;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.KerberosCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ietf.jgss.GSSName;

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
public final class ProxySPNegoHttpClient implements HttpClient, Closeable {
    
    public enum DefaultConfiguration {
        HTTP_HOST("http.host", ""),
        HTTP_PORT("http.port", ""),
        REFRESH_KRB5_CONFIG("refreshKrb5Config", "true"),
        USE_TICKET_CACHE("useTicketCache", "false"),
        TICKET_CACHE("ticketCache", ""),
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

    /**
     * Disable SSL certificate checking.
     */
    private static final TrustManager TRUST_MANAGER = new X509TrustManager() {

        /**
         * Given the partial or complete certificate chain provided by the peer, ignores the
         * certificate checking.
         *
         * @param chain the peer certificate chain
         * @param authType the authentication type based on the client certificate
         */
        @Override
        public void checkClientTrusted(final X509Certificate[] chain, final String authType) {
            // This will never throw an exception.
            // this doesn't check anything at all
            // it is insecure            
        }

        /**
         * Given the partial or complete certificate chain provided by the peer, ignores the
         * certificate checking.
         *
         * @param chain the peer certificate chain
         * @param authType the authentication type based on the client certificate
         * @throws CertificateException
         */
        @Override
        public void checkServerTrusted(final X509Certificate[] chain, final String authType) throws
                CertificateException {
            // This will never throw an exception.
            // this doesn't check anything at all
            // it is insecure
        }

        /**
         * Return null, everybody is trusted.
         *
         * @return null
         */
        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    };

    /**
     * http client.
     */
    private final CloseableHttpClient httpClient;

    /**
     * configuration for proxy.
     */
    private RequestConfig config;    

    
    public ProxySPNegoHttpClient(final Map<String, String> config) {
        this(config, null, false);
    }
    
    public ProxySPNegoHttpClient(final Map<String, String> config, final boolean isDisabledSSL) {
        this(config, null, isDisabledSSL);
    }
    
    public ProxySPNegoHttpClient(final Map<String, String> config, final File krbConfPath, final boolean isDisabledSSL) {
        final File krbConf = getKrbConf(krbConfPath);
        final HttpHost proxy = new HttpHost(
                config.get(DefaultConfiguration.HTTP_HOST.getKey()), 
                Integer.parseInt(config.get(DefaultConfiguration.HTTP_PORT.getKey()))
        );
        HttpClientBuilder builder = HttpClients.custom()
                .setDefaultCredentialsProvider(createCredsProvider(proxy))
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
        this.httpClient = builder.build();
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
     * Sets the proxy configuration.
     *
     * @param proxy http proxy
     */
    private void setProxyConfiguration(final HttpHost proxy) {
        LOG.traceEntry("proxy : {}", proxy);
        this.config = RequestConfig.custom().setProxy(proxy).build();
        LOG.traceExit();
    }

    /**
     * Disables the SSL certificate checking.
     *
     * @return the SSL context
     * @throws RuntimeException When a NoSuchAlgorithmException or KeyManagementException happens
     */
    private SSLContext disableSSLCertificateChecking() {
        try {
            final SSLContext sslCtx = SSLContext.getInstance("TLS");
            sslCtx.init(null, new TrustManager[]{TRUST_MANAGER}, null);
            return sslCtx;
        } catch (NoSuchAlgorithmException | KeyManagementException ex) {
            throw LOG.throwing(new RuntimeException(ex));
        }
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

    /**
     * Return the http proxy.
     *
     * @return the http proxy
     */
    public HttpHost getProxyConfiguration() {
        LOG.traceEntry();
        return LOG.traceExit(this.config.getProxy());
    }

    @Override
    public HttpParams getParams() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ClientConnectionManager getConnectionManager() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public HttpResponse execute(HttpUriRequest request) throws IOException, ClientProtocolException {
        return this.execute(request, new HttpClientContext());
    }

    @Override
    public HttpResponse execute(HttpUriRequest request, HttpContext context) throws IOException,
            ClientProtocolException {
        LOG.traceEntry("request : {}\n"
                + "context: {}",
                request, context);
        LOG.info("Executing request to {}  via {}:{}", request.getRequestLine(), this.getProxyConfiguration().getHostName(),
                this.getProxyConfiguration().getPort());        
        if(config != null) {
            context.setAttribute(HttpClientContext.REQUEST_CONFIG, config);
        }
        return this.httpClient.execute(request, context);
    }

    @Override
    public HttpResponse execute(HttpHost target, HttpRequest request) throws IOException,
            ClientProtocolException {
        return this.execute(target, request, new HttpClientContext());
    }

    @Override
    public HttpResponse execute(HttpHost target, HttpRequest request, HttpContext context) throws
            IOException, ClientProtocolException {
        LOG.traceEntry("target : {}\n"
                + "request: {}\n"
                + "context: {}",
                target, request, context);      
        context.setAttribute(HttpClientContext.REQUEST_CONFIG, config);        
        LOG.info("Executing request to {}  via {}:{}", target, this.getProxyConfiguration().getHostName(),
                this.getProxyConfiguration().getPort());          
        return LOG.traceExit(this.httpClient.execute(target, request, context));
    }

    @Override
    public <T> T execute(HttpUriRequest request,
            ResponseHandler<? extends T> responseHandler) throws IOException,
            ClientProtocolException {
        return this.execute(request, responseHandler, new HttpClientContext());
    }

    @Override
    public <T> T execute(HttpUriRequest request,
            ResponseHandler<? extends T> responseHandler, HttpContext context) throws IOException,
            ClientProtocolException {
        LOG.traceEntry("request : {}\n"
                + "responseHandler: {}\n"
                + "context: {}",
                request, responseHandler, context);       
        context.setAttribute(HttpClientContext.REQUEST_CONFIG, config);        
        LOG.info("Executing request to {}  via {}:{}", request.getRequestLine(), this.getProxyConfiguration().getHostName(),
                this.getProxyConfiguration().getPort());         
        return LOG.traceExit(this.httpClient.execute(request, responseHandler, context));
    }

    @Override
    public <T> T execute(HttpHost target, HttpRequest request,
            ResponseHandler<? extends T> responseHandler) throws IOException,
            ClientProtocolException {
        return this.execute(target, request, responseHandler, new HttpClientContext());
    }

    @Override
    public <T> T execute(HttpHost target, HttpRequest request,
            ResponseHandler<? extends T> responseHandler, HttpContext context) throws IOException,
            ClientProtocolException {
        LOG.traceEntry("target : {}\n"
                + "responseHandler: {}\n"
                + "context: {}",
                target, responseHandler, context);               
        context.setAttribute(HttpClientContext.REQUEST_CONFIG, config);
        LOG.info("Executing request to {}  via {}:{}", target, this.getProxyConfiguration().getHostName(),
                this.getProxyConfiguration().getPort());        
        return LOG.traceExit(this.httpClient.execute(target, request, responseHandler, context));
    }
    

    /**
     * Close the http connection.
     *
     */
    @Override
    public void close() {
        LOG.traceEntry();
        try {
            this.httpClient.close();
        } catch (IOException ex) {
            LOG.error(ex);
        }
        LOG.traceExit();
    }    
}
