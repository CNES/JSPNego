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
import java.io.Closeable;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Http client
 *
 * @author Jean-Christophe Malapert
 */
public class HttpClient implements org.apache.http.client.HttpClient, Closeable {

    /**
     * Maximum total of connections per route.
     */
    public static final String CONNECTION_MAX_PER_ROUTE = "connectionMaxPerRoute";
    /**
     * Maximum total of connections.
     */
    public static final String CONNECTION_MAX_TOTAL = "connectionMaxTotal";
    /**
     * maximum time to live for persistent connections.
     */
    public static final String CONNECTION_TIME_TO_LIVE_MS = "connectionTimeToLiveMs";
    /**
     * number of retries before the request fails.
     */
    public static final String MAX_RETRY = "maxRetry";    
    /**
     * Type of Http client.
     */
    public static final String HTTP_CLIENT_TYPE = "type";
    /**
     * IsdisabledSSL.
     */
    public static final String IS_DISABLED_SSL = "isDisabledSSL";
    /**
     * Key store type.
     */    
    public static final String KEYSTORE_TYPE = "keystoreType";
    /**
     * Key store path.
     */
    public static final String KEYSTORE_PATH = "keystorePath";
    /**
     * Key store password.
     */    
    public static final String KEYSTORE_PWD = "keystorePassword";
    /**
     * Trust store type.
     */    
    public static final String TRUSTSTORE_TYPE = "truststoreType";
    /**
     * Trust store path.
     */
    public static final String TRUSTSTORE_PATH = "truststorePath";
    /**
     * Trust store password.
     */    
    public static final String TRUSTSTORE_PWD = "truststorePassword";    
    /**
     * Key manager algorithm.
     */
    private static final String KEY_MANAGER_ALGO = "SunX509";

    /**
     * Disable SSL certificate checking.
     */
    protected static final TrustManager TRUST_MANAGER = new X509TrustManager() {

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
         * @return {@code null}
         */
        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    };

    /**
     * Get actual class name to be printed on.
     */
    private static final Logger LOG = LogManager.getLogger(HttpClient.class.getName());

    /**
     * Http client.
     */
    private final CloseableHttpClient httpClient;
    
    /**
     * Httpclient type.
     */
    private final HttpClientFactory.Type type;    

    /**
     * Creates a Http client.
     */
    public HttpClient() {
        this(false);
    }

    /**
     * Creates a http client that ignores the SSL certificates.
     *
     * @param isDisabledSSL True when SSL certificates are disabled otherwise False
     */
    public HttpClient(final boolean isDisabledSSL) {
        this(isDisabledSSL, new HashMap<>());
    }

    /**
     * Creates a http client that ignores the SSL certificates.
     *
     * @param isDisabledSSL True when SSL certificates are disabled otherwise False
     * @param config Options for HTTP client.
     */
    public HttpClient(final boolean isDisabledSSL, final Map<String, String> config) {
        this(isDisabledSSL, config, Type.NO_PROXY);
    }
    
    /**
     * Creates a type of http client that ignores the SSL certificates.
     *
     * @param isDisabledSSL True when SSL certificates are disabled otherwise False
     * @param config Options for HTTP client.
     * @param type type of http client
     */    
    protected HttpClient(final boolean isDisabledSSL, final Map<String, String> config, final Type type) {
        this.type = type;
        this.httpClient = createBuilder(isDisabledSSL, config).build();
    }

    /**
     * Creates the Http client builder.
     *
     * @param isDisabledSSL True when SSL certificates are disabled otherwise False
     * @param config options for Http client
     * @return the Http client builder
     */
    protected final HttpClientBuilder createBuilder(final boolean isDisabledSSL,
            final Map<String, String> config) {
        LOG.traceEntry("isDisabledSSL: {}\nconfig: {}", isDisabledSSL, config);
        HttpClientBuilder builder = HttpClients.custom();
        if (isDisabledSSL) {
            LOG.warn("SSL Certificate checking is disabled. The connection is insecured.");
            builder = builder.setSSLContext(disableSSLCertificateChecking())
                    .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE);
        } else {            
            SSLContext sslCtx = createJKSContext(config);
            if (sslCtx != null) {
                LOG.info("Creating a SSL configuration with JKS");
                builder.setSSLContext(sslCtx);
            }
        }

        return LOG.traceExit(createBuilderExtension(builder, config));
    }

    /**
     * Creates builder extension.
     *
     * @param builder builder
     * @param config options for Http client
     * @return builder with extensions.
     */
    protected HttpClientBuilder createBuilderExtension(final HttpClientBuilder builder,
            final Map<String, String> config) {
        LOG.traceEntry("builder: {}\nconfig: {}", builder, config);
        HttpClientBuilder extBuilder = createBuilderProxy(builder);
        extBuilder = createRedirect(extBuilder);
        if (config.containsKey(CONNECTION_MAX_PER_ROUTE) && config.containsKey(CONNECTION_MAX_TOTAL)) {
            LOG.debug("configure connectionManager");
            extBuilder = createConnectionNumber(extBuilder, Integer.parseInt(config.get(
                    CONNECTION_MAX_PER_ROUTE)), Integer.parseInt(config.get(CONNECTION_MAX_TOTAL)));
        }
        if (config.containsKey(CONNECTION_TIME_TO_LIVE_MS)) {
            LOG.debug("configure timeout");
            extBuilder = createConnectionTimeout(extBuilder, Long.parseLong(config.get(
                    CONNECTION_TIME_TO_LIVE_MS)));
        }
        if (config.containsKey(MAX_RETRY)) {
            LOG.debug("configure retry");
            extBuilder = createRetry(extBuilder, Integer.parseInt(config.get(MAX_RETRY)));
        }
        return LOG.traceExit(extBuilder);
    }

    /**
     * Adds builder proxy extension. In this case, add no extension.
     *
     * @param builder builder
     * @return the same builder
     */
    protected HttpClientBuilder createBuilderProxy(final HttpClientBuilder builder) {
        LOG.traceEntry("builder: {}", builder);
        return LOG.traceExit(builder);
    }

    /**
     * Creates connection number builder.
     *
     * @param builder builder
     * @param connPerRoute connection per route
     * @param total total connection
     * @return builder
     */
    private HttpClientBuilder createConnectionNumber(final HttpClientBuilder builder,
            final int connPerRoute, final int total) {
        LOG.traceEntry("builder: {}\nconnPerRoute: {}\ntotal: {}", builder, connPerRoute, total);
        LOG.debug("set default max per route: {}", connPerRoute);
        builder.setMaxConnPerRoute(connPerRoute);
        LOG.debug("set max total: {}", total);        
        builder.setMaxConnTotal(total);
        return LOG.traceExit(builder);
    }

    /**
     * Creates timeout connection.
     *
     * @param builder builder
     * @param timeMs time in ms
     * @return builder
     */
    private HttpClientBuilder createConnectionTimeout(final HttpClientBuilder builder,
            final long timeMs) {
        LOG.traceEntry("builder: {}\ntimeMs: {}", builder, timeMs);
        return LOG.traceExit(builder.setConnectionTimeToLive(timeMs, TimeUnit.MILLISECONDS));
    }

    /**
     * Creates redirect.
     *
     * @param builder builder
     * @return builder
     */
    private HttpClientBuilder createRedirect(final HttpClientBuilder builder) {
        LOG.traceEntry("builder: {}", builder);
        return LOG.traceExit(builder.setRedirectStrategy(new LaxRedirectStrategy()));
    }

    /**
     * Creates retry.
     *
     * @param builder builder
     * @param retry number of retries
     * @return builder
     */
    protected HttpClientBuilder createRetry(final HttpClientBuilder builder, final int retry) {
        LOG.traceEntry("builder: {}\nretry: {}", builder, retry);
        return LOG.traceExit(builder.
                setRetryHandler(new DefaultHttpRequestRetryHandler(retry, true)));
    }

    /**
     * Disables the SSL certificate checking.
     *
     * @return the SSL context
     * @throws RuntimeException When a NoSuchAlgorithmException or KeyManagementException happens
     */
    private SSLContext disableSSLCertificateChecking() {
        LOG.traceEntry();
        try {
            final SSLContext sslCtx = SSLContext.getInstance("TLS");
            sslCtx.init(null, new TrustManager[]{TRUST_MANAGER}, null);
            return LOG.traceExit(sslCtx);
        } catch (NoSuchAlgorithmException | KeyManagementException ex) {
            throw LOG.throwing(new RuntimeException(ex));
        }        
    }
    
    /**
     * Configures key store.
     * @param config keystore parameters
     * @return Key manager or null
     */
    private KeyManagerFactory configureKeyStore(final Map<String, String> config) {
        LOG.traceEntry("config: {}", config);
        final String keyStoreType = config.getOrDefault(KEYSTORE_TYPE, System.getProperty("javax.net.ssl.keyStoreType"));
        final String keyStorePath = config.getOrDefault(KEYSTORE_PATH, System.getProperty("javax.net.ssl.keyStore"));
        final String keyStorePwd = config.getOrDefault(KEYSTORE_PWD, System.getProperty("javax.net.ssl.keyStorePassword"));
        KeyManagerFactory kmf;
        if(keyStoreType != null && keyStorePath != null && keyStorePwd != null) {
            LOG.debug("keyStoreType: {}",keyStoreType);
            LOG.debug("keyStorePath: {}",keyStorePath);
            LOG.debug("keyStorePwd: ******");            
            try {                
                final KeyStore ks = KeyStore.getInstance(keyStoreType);
                final char[] keyPassphrase = keyStorePwd.toCharArray();                
                ks.load(new FileInputStream(keyStorePath), keyPassphrase);
                kmf = KeyManagerFactory.getInstance(KEY_MANAGER_ALGO);
                kmf.init(ks, keyPassphrase);
            } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException | IOException | CertificateException ex) {
                LOG.catching(ex);
                kmf = null;
            }            
        } else {
            kmf = null;
        }
        return LOG.traceExit(kmf);
    }
    
    /**
     * Configures trust store.
     * @param config truststore parameters
     * @return Trust manager or null
     */    
    private TrustManagerFactory configureTrustStore(final Map<String, String> config) {
        LOG.traceEntry("config: {}", config);
        final String trustStoreType = config.getOrDefault(TRUSTSTORE_TYPE, System.getProperty("javax.net.ssl.trustStoreType"));
        final String trustStorePath = config.getOrDefault(TRUSTSTORE_PATH, System.getProperty("javax.net.ssl.trustStore"));
        final String trustStorePwd = config.getOrDefault(TRUSTSTORE_PWD, System.getProperty("javax.net.ssl.trustStorePassword"));      
        TrustManagerFactory tmf;
        if(trustStoreType != null && trustStorePath != null && trustStorePwd != null) {
            LOG.debug("trustStoreType: {}",trustStoreType);
            LOG.debug("trustStorePath: {}",trustStorePath);
            LOG.debug("trustStorePwd: ******");
            try {
                final KeyStore tks = KeyStore.getInstance(trustStoreType);
                final char[] trustPassphrase = trustStorePwd.toCharArray();                
                tks.load(new FileInputStream(trustStorePath), trustPassphrase);
                tmf = TrustManagerFactory.getInstance(KEY_MANAGER_ALGO);
                tmf.init(tks);
            } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException ex) {
                LOG.catching(ex);
                tmf = null;
            }         
        } else {
            tmf = null;
        }
        return LOG.traceExit(tmf);
    }    
    
    /**
     * Creates SSL context.
     * @param config SSL parameters
     * @return SSL context or null
     */
    private SSLContext createJKSContext(final Map<String, String> config) {
        LOG.traceEntry("config: {}", config);
        SSLContext sslCtx;
        try {
            sslCtx = SSLContext.getInstance("TLS");
           
            final KeyManagerFactory kmf = configureKeyStore(config);
            final TrustManagerFactory tmf = configureTrustStore(config);
            final KeyManager[] keys;
            if(kmf == null) {
                keys = null;
            } else {
                keys = kmf.getKeyManagers();
            }
            
            final TrustManager[] trusts;
            if(tmf == null) {
                trusts = null;
            } else {
                trusts = tmf.getTrustManagers();
            }            
            sslCtx.init(keys, trusts, new SecureRandom());            
            
        } catch (NoSuchAlgorithmException | KeyManagementException ex) {
            LOG.catching(ex);
            sslCtx = null;
        }
      
        return LOG.traceExit(sslCtx);
    }

    /**
     * Returns the http client.
     *
     * @return the Http client
     */
    protected CloseableHttpClient getHttpClient() {
        LOG.traceEntry();
        return LOG.traceExit(this.httpClient);
    }
    
    /**
     * Returns the type of httpClient.
     * @return the type of httpClient
     */
    public Type getType() {
        return this.type;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Deprecated
    public HttpParams getParams() {
        return this.httpClient.getParams();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Deprecated
    public ClientConnectionManager getConnectionManager() {
        return this.httpClient.getConnectionManager();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HttpResponse execute(final HttpUriRequest request) throws IOException,
            ClientProtocolException {
        return this.httpClient.execute(request);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HttpResponse execute(final HttpUriRequest request, final HttpContext context) throws
            IOException,
            ClientProtocolException {
        return this.httpClient.execute(request, context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HttpResponse execute(final HttpHost target, final HttpRequest request) throws IOException,
            ClientProtocolException {
        return this.httpClient.execute(target, request);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HttpResponse execute(final HttpHost target, final HttpRequest request,
            final HttpContext context) throws
            IOException, ClientProtocolException {
        return this.httpClient.execute(target, request, context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T execute(final HttpUriRequest request,
            final ResponseHandler<? extends T> responseHandler) throws IOException,
            ClientProtocolException {
        return this.httpClient.execute(request, responseHandler);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T execute(final HttpUriRequest request,
            final ResponseHandler<? extends T> responseHandler, final HttpContext context) throws
            IOException,
            ClientProtocolException {
        return this.httpClient.execute(request, responseHandler, context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T execute(final HttpHost target, final HttpRequest request,
            final ResponseHandler<? extends T> responseHandler) throws IOException,
            ClientProtocolException {
        return this.httpClient.execute(target, request, responseHandler);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T execute(final HttpHost target, final HttpRequest request,
            final ResponseHandler<? extends T> responseHandler, final HttpContext context) throws
            IOException,
            ClientProtocolException {
        return this.httpClient.execute(target, request, responseHandler, context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        this.httpClient.close();
    }

}
