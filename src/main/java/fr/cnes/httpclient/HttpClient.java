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

import java.io.Closeable;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
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
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
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
        this.httpClient = createBuilder(isDisabledSSL, config).build();
    }    

    /**
     * Creates the Http client builder.
     *
     * @param isDisabledSSL True when SSL certificates are disabled otherwise False
     * @param config options for Http client
     * @return the Http client builder
     */
    protected final HttpClientBuilder createBuilder(final boolean isDisabledSSL, final Map<String, String> config) {
        HttpClientBuilder builder = HttpClients.custom();
        if (isDisabledSSL) {
            LOG.warn("SSL Certificate checking is disabled. The connection is insecured.");
            builder = builder.setSSLContext(disableSSLCertificateChecking())
                    .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE);
        }

        return createBuilderExtension(builder, config);
    }
    
    /**
     * Creates builder extension.
     *
     * @param builder builder
     * @param config options for Http client
     * @return builder with extensions.
     */
    protected HttpClientBuilder createBuilderExtension(final HttpClientBuilder builder, final Map<String, String> config) {
        HttpClientBuilder extBuilder = createBuilderProxy(builder);
        extBuilder = createRedirect(extBuilder);
        if(config.containsKey(CONNECTION_MAX_PER_ROUTE) && config.containsKey(CONNECTION_MAX_TOTAL)) {
            extBuilder = createConnectionManager(extBuilder, Integer.parseInt(config.get(CONNECTION_MAX_PER_ROUTE)), Integer.parseInt(config.get(CONNECTION_MAX_TOTAL)));
        }
        if(config.containsKey(CONNECTION_TIME_TO_LIVE_MS)) {
            extBuilder = createConnectionTimeout(extBuilder, Integer.parseInt(config.get(CONNECTION_TIME_TO_LIVE_MS)));
        }
        if(config.containsKey(MAX_RETRY)) {
            extBuilder = createRetry(extBuilder, Integer.parseInt(config.get(MAX_RETRY)));
        }
        return extBuilder;
    }
    
    /**
     * Adds builder extension. In this case, add no extension.
     *
     * @param builder builder
     * @return the same builder
     */    
    protected HttpClientBuilder createBuilderProxy(final HttpClientBuilder builder) {
        LOG.traceEntry("builder: {}", builder);
        return LOG.traceExit(builder);        
    }    
    
    private HttpClientBuilder createConnectionManager(final HttpClientBuilder builder, int connPerRoute, int total) {
        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
        connManager.setDefaultMaxPerRoute(connPerRoute);
        connManager.setMaxTotal(total);
        return builder.setConnectionManager(connManager);
    }
    
    private HttpClientBuilder createConnectionTimeout(final HttpClientBuilder builder, int timeMs) {
        return builder.setConnectionTimeToLive(timeMs, TimeUnit.MILLISECONDS);
    }
    
    private HttpClientBuilder createRedirect(final HttpClientBuilder builder) {
        return builder.setRedirectStrategy(new LaxRedirectStrategy());
    }   
    
    protected HttpClientBuilder createRetry(final HttpClientBuilder builder, int retry) {
        return builder.setRetryHandler(new DefaultHttpRequestRetryHandler(retry, true));
    }       

    /**
     * Disables the SSL certificate checking.
     *
     * @return the SSL context
     * @throws RuntimeException When a NoSuchAlgorithmException or KeyManagementException happens
     */
    protected final SSLContext disableSSLCertificateChecking() {
        try {
            final SSLContext sslCtx = SSLContext.getInstance("TLS");
            sslCtx.init(null, new TrustManager[]{TRUST_MANAGER}, null);
            return sslCtx;
        } catch (NoSuchAlgorithmException | KeyManagementException ex) {
            throw LOG.throwing(new RuntimeException(ex));
        }
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
     * {@inheritDoc}
     */
    @Override
    public HttpParams getParams() {
        return this.httpClient.getParams();
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
