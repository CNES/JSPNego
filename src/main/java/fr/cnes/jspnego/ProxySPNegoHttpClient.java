package fr.cnes.jspnego;

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
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScheme;
import org.apache.http.auth.AuthSchemeFactory;

import org.apache.http.auth.AuthSchemeProvider;
import org.apache.http.auth.AuthSchemeRegistry;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.KerberosCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.AuthPolicy;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ietf.jgss.GSSName;

/**
 * The client via a proxy authenticates itself to the Authentication Server (AS) which forwards the
 * username to a key distribution center (KDC).
 *
 * The KDC issues a ticket-granting ticket (TGT), which is time stamped and encrypts it using the
 * ticket-granting service's (TGS) secret key and returns the encrypted result to the user's
 * workstation. This is done infrequently, typically at user logon; the TGT expires at some point
 * although it may be transparently renewed by the user's session manager while they are logged in.
 *
 * When the client needs to communicate with another node ("principal" in Kerberos parlance) to some
 * service on that node the client sends the TGT to the TGS, which usually shares the same host as
 * the KDC. Service must be registered at TGT with a Service Principal Name (SPN). The client uses
 * the SPN to request access to this service. After verifying that the TGT is valid and that the
 * user is permitted to access the requested service, the TGS issues ticket and session keys to the
 * client. The client then sends the ticket to the service server (SS) along with its service
 * request.
 *
 * @author S. ETCHEVERRY
 */
public final class ProxySPNegoHttpClient implements Closeable {

    /**
     * Get actual class name to be printed on
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
     * Disable SSL certtificate checking.
     */
    private static final TrustManager TRUST_MANAGER = new X509TrustManager() {

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            // This will never throw an exception.
            // this doesn't check anything at all
            // it is insecure
        }

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
     * proxy.
     */
    private final HttpGet request = new HttpGet("/");

    /**
     * Updates an HttpClient for crossing a proxy using SPNego.
     *
     * @param userId User ID
     * @param keytabFileName Keytab filename
     * @param proxyHost proxy host
     * @param proxyPort proxy port
     * @param httpClient http client
     */
    public ProxySPNegoHttpClient(final String userId,
            final String keytabFileName, final String proxyHost, final int proxyPort,
            final DefaultHttpClient httpClient) {
        this(userId, keytabFileName, null, null, proxyHost, proxyPort, httpClient);
    }

    /**
     * Updates an HttpClient for crossing a proxy using SPNego with a cache on file system.
     *
     * @param userId user ID
     * @param keytabFileName keytab filename
     * @param ticketCacheFileName ticket cache filename
     * @param proxyHost proxy host
     * @param proxyPort proxy port
     * @param httpClient http client
     */
    public ProxySPNegoHttpClient(final String userId,
            final String keytabFileName,
            final String ticketCacheFileName, final String proxyHost, final int proxyPort,
            final DefaultHttpClient httpClient) {
        this(userId, keytabFileName, ticketCacheFileName, null, proxyHost, proxyPort,
                httpClient);
    }

    /**
     * Updates an HttpClient for crossing a proxy using SPNego with a cache on file system.
     *
     * @param userId user ID
     * @param keytabFileName keytab filename
     * @param ticketCacheFileName ticket cache filename
     * @param krbConfPath krb conf path
     * @param proxyHost proxy host
     * @param proxyPort proxy port
     * @param httpClient http client
     */
    public ProxySPNegoHttpClient(final String userId,
            final String keytabFileName,
            final String ticketCacheFileName, final String krbConfPath, final String proxyHost,
            final int proxyPort, final DefaultHttpClient httpClient) {
        this(userId, keytabFileName, ticketCacheFileName, krbConfPath,
                new HttpHost(proxyHost, proxyPort, HttpHost.DEFAULT_SCHEME_NAME), httpClient);
    }

    /**
     * Updates an HttpClient for crossing a proxy using SPNego with a cache on file system.
     *
     * @param userId user ID
     * @param keytabFileName keytab filename
     * @param ticketCacheFileName ticket cache filename
     * @param krbConfPath krb conf path
     * @param proxy proxy
     * @param httpClient http client
     */
    public ProxySPNegoHttpClient(final String userId,
            final String keytabFileName,
            final String ticketCacheFileName, final String krbConfPath, final HttpHost proxy,
            final DefaultHttpClient httpClient) {
        final String defaultKrbConf = (Files.isReadable(Paths.get(System.getenv(ENV_KRB5)))) ? 
                System.getenv(ENV_KRB5) : KRB_CONF_PATH;
        final String krbConf = (krbConfPath == null) ? defaultKrbConf : krbConfPath;
        final String spn = "HTTP@" + proxy.getHostName();
        httpClient.setCredentialsProvider(createCredsProvider(proxy));
        httpClient.setAuthSchemes(registerSPNegoProviderDefaultHttp(userId, keytabFileName,
                ticketCacheFileName, krbConf, spn));
        SSLSocketFactory sf = new SSLSocketFactory(disableSSLCertificateChecking());
        Scheme httpsScheme = new Scheme("https", 443, sf);       
        this.httpClient = httpClient;
        this.httpClient.getConnectionManager().getSchemeRegistry().register(httpsScheme);
        setProxy(proxy);
    }

    /**
     * Creates a http client that crosses a proxy using SPNego.
     *
     * @param userId user ID
     * @param keytabFileName keytab filename
     * @param proxyHost proxy host
     * @param proxyPort proxy port
     */
    public ProxySPNegoHttpClient(final String userId,
            final String keytabFileName, final String proxyHost, final int proxyPort) {
        this(userId, keytabFileName, null, null, proxyHost, proxyPort);
    }

    /**
     * Creates a http client that crosses a proxy using SPNego with a cache on the file system.
     *
     * @param userId user ID
     * @param keytabFileName keytab filename
     * @param ticketCacheFileName ticket cache filename
     * @param proxyHost proxy host
     * @param proxyPort proxy port
     */
    public ProxySPNegoHttpClient(final String userId,
            final String keytabFileName,
            final String ticketCacheFileName, final String proxyHost, final int proxyPort) {
        this(userId, keytabFileName, ticketCacheFileName, null, proxyHost, proxyPort);
    }

    /**
     * Creates a http client that crosses a proxy using SPNego with a cache on the file system.
     *
     * @param userId user ID
     * @param keytabFileName keytab filename
     * @param ticketCacheFileName ticket cache filename
     * @param krbConfPath krb conf path
     * @param proxyHost proxy host
     * @param proxyPort proxy port
     */
    public ProxySPNegoHttpClient(final String userId,
            final String keytabFileName,
            final String ticketCacheFileName, final String krbConfPath, final String proxyHost,
            final int proxyPort) {
        this(userId, keytabFileName, ticketCacheFileName, krbConfPath,
                new HttpHost(proxyHost, proxyPort, HttpHost.DEFAULT_SCHEME_NAME));
    }

    /**
     * Creates a http client that crosses a proxy using SPNego with a cache on the file system.
     *
     * @param userId user ID
     * @param keytabFileName keytab filename
     * @param ticketCacheFileName ticket cache filename
     * @param krbConfPath krb conf path
     * @param proxy proxy
     */
    public ProxySPNegoHttpClient(final String userId,
            final String keytabFileName,
            final String ticketCacheFileName, final String krbConfPath, final HttpHost proxy) {
        LOG.traceEntry("Parameters : {}", userId, keytabFileName, ticketCacheFileName, krbConfPath,
                proxy);
        final String defaultKrbConf = (Files.isReadable(Paths.get(System.getenv(ENV_KRB5)))) ? 
                System.getenv(ENV_KRB5) : KRB_CONF_PATH;
        final String krbConf = (krbConfPath == null) ? defaultKrbConf : krbConfPath;
        final String spn = "HTTP@" + proxy.getHostName();
        HttpClientBuilder builder = HttpClients.custom()
                .setDefaultCredentialsProvider(createCredsProvider(proxy))
                .setDefaultAuthSchemeRegistry(registerSPNegoProvider(
                        userId, keytabFileName,ticketCacheFileName, krbConf, spn)
                )
                .setSSLContext(disableSSLCertificateChecking())
                .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE);
        this.httpClient = builder.build();
        setProxy(proxy);
    }

    /**
     * Sets the proxy
     *
     * @param proxy http proxy
     */
    private void setProxy(final HttpHost proxy) {
        LOG.traceEntry("Parameter : {}", proxy);
        final RequestConfig config = RequestConfig.custom().setProxy(proxy).build();
        this.request.setConfig(config);
        LOG.traceExit();
    }
    
    private SSLContext disableSSLCertificateChecking() {
        try {
            SSLContext sslCtx = SSLContext.getInstance("TLS");
            sslCtx.init(null, new TrustManager[] { TRUST_MANAGER }, null);
            return sslCtx;
        } catch (NoSuchAlgorithmException | KeyManagementException ex) {
            throw LOG.throwing(new RuntimeException(ex));
        }
    }

    /**
     * Return the http proxy.
     *
     * @return the http proxy
     */
    public HttpHost getProxy() {
        LOG.traceEntry();
        return LOG.traceExit(this.request.getConfig().getProxy());
    }

    /**
     * Creates a dummy kerberos credential provider for the kerberized proxy.
     *
     * @param proxy proxy configuration
     * @return a dummy kerberos credential provider for the kerberized proxy
     */
    private CredentialsProvider createCredsProvider(final HttpHost proxy) {
        LOG.traceEntry("Parameter: {}", proxy);
        final CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(new AuthScope(proxy.getHostName(), proxy.getPort()),
                new KerberosCredentials(null));
        return LOG.traceExit(credsProvider);
    }

    /**
     * Registers the SPNego scheme.
     *
     * @param userId user ID
     * @param keytabFileName keytab filename
     * @param ticketCacheFileName ticket cache filename
     * @param krbConfPath krb conf path
     * @param spn Service Principal Name
     * @return the registry
     */
    private Registry<AuthSchemeProvider> registerSPNegoProvider(final String userId,
            final String keytabFileName, final String ticketCacheFileName,
            final String krbConfPath, final String spn) {
        LOG.traceEntry("Parameters: {}", userId, keytabFileName, ticketCacheFileName, krbConfPath,
                spn);        
        // init an registerSPNegoProviderDefaultHttp a SPNEGO auth scheme
        final GSSClient gssClient = new GSSClient(userId, keytabFileName,
                ticketCacheFileName, new File(krbConfPath));
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
                        return new SPNegoScheme(
                                gssClient, spn, GSSName.NT_HOSTBASED_SERVICE
                        );
                    }
                }).build());
    }

    /**
     * Registers the SPNego scheme based on defaultHttpClient.
     *
     * @param userId user ID
     * @param keytabFileName keytab filename
     * @param ticketCacheFileName ticket cache filename
     * @param krbConfPath krb conf path
     * @param spn Service Principal Name
     * @return the registry
     */
    private AuthSchemeRegistry registerSPNegoProviderDefaultHttp(final String userId,
            final String keytabFileName, final String ticketCacheFileName,
            final String krbConfPath, final String spn) {
        LOG.traceEntry("Parameters: {}", userId, keytabFileName, ticketCacheFileName, krbConfPath,
                spn);
        final GSSClient gssClient = new GSSClient(userId, keytabFileName,
                ticketCacheFileName, new File(krbConfPath));

        final AuthSchemeRegistry authSchemeRegistry = new AuthSchemeRegistry();
        authSchemeRegistry.register(AuthPolicy.SPNEGO, new AuthSchemeFactory() {
            /**
             * Creates an authentication scheme.
             *
             * @param params http parameters
             * @return the authentication scheme
             */
            @Override
            public AuthScheme newInstance(final HttpParams params) {
                return new SPNegoScheme(gssClient, spn, GSSName.NT_HOSTBASED_SERVICE);
            }
        });
        return LOG.traceExit(authSchemeRegistry);
    }

    /**
     * Executes HTTP request using the given context.
     *
     * @param target target
     * @param context context
     * @return the response to the request. This is always a final response, never an intermediate
     * response with an 1xx status code. Whether redirects or authentication challenges will be
     * returned or handled automatically depends on the implementation and configuration of this
     * client.
     * @throws IOException in case of a problem or the connection was aborted
     * @throws ClientProtocolException in case of an http protocol error
     */
    public CloseableHttpResponse execute(final HttpHost target, final HttpContext context)
            throws IOException, ClientProtocolException {
        LOG.traceEntry("Parameters : {}", target, context);
        LOG.info("Executing request to {} via {}:{}",
                target, this.getProxy().getHostName(), this.getProxy().getPort());
        return LOG.traceExit(this.httpClient.execute(target, this.request, context));
    }

    /**
     * Executes HTTP request using the default context.
     *
     * @param target target
     * @return the response to the request. This is always a final response, never an intermediate
     * response with an 1xx status code. Whether redirects or authentication challenges will be
     * returned or handled automatically depends on the implementation and configuration of this
     * client.
     * @throws IOException in case of a problem or the connection was aborted
     * @throws ClientProtocolException in case of an http protocol error
     */
    public CloseableHttpResponse execute(final HttpHost target) throws IOException,
            ClientProtocolException {
        LOG.traceEntry("Parameters : {}", target);
        LOG.info("Executing request to {}  via {}:{}", target, this.getProxy().getHostName(),
                this.getProxy().getPort());
        return LOG.traceExit(this.httpClient.execute(target, this.request));
    }

    /**
     * Executes a request using the default context and processes the response using the given
     * response handler.
     *
     * The content entity associated with the response is fully consumed and the underlying
     * connection is released back to the connection manager automatically in all cases relieving
     * individual ResponseHandlers from having to manage resource deallocation internally.
     *
     * @param <T>
     * @param target the target host for the request. Implementations may accept null if they can
     * still determine a route, for example to a default target or by inspecting the request.
     * @param responseHandler the response handler
     * @return the response object as generated by the response handler.
     * @throws IOException in case of a problem or the connection was aborted
     * @throws ClientProtocolException in case of an http protocol error
     */
    public <T extends Object> T execute(final HttpHost target,
            final ResponseHandler<? extends T> responseHandler) throws IOException,
            ClientProtocolException {
        LOG.traceEntry("Parameters : {}", target, responseHandler);
        LOG.info("Executing request to {}  via {}:{}", target, this.getProxy().getHostName(),
                this.getProxy().getPort());
        return LOG.traceExit(this.httpClient.execute(target, this.request, responseHandler));
    }

    /**
     * Executes a request using a given context and processes the response using the given response
     * handler.
     *
     * The content entity associated with the response is fully consumed and the underlying
     * connection is released back to the connection manager automatically in all cases relieving
     * individual ResponseHandlers from having to manage resource deallocation internally.
     *
     * @param <T>
     * @param target the target host for the request. Implementations may accept null if they can
     * still determine a route, for example to a default target or by inspecting the request.
     * @param responseHandler the response handler
     * @param context context
     * @return the response object as generated by the response handler.
     * @throws IOException in case of a problem or the connection was aborted
     * @throws ClientProtocolException in case of an http protocol error
     */
    public <T extends Object> T execute(final HttpHost target,
            final ResponseHandler<? extends T> responseHandler, final HttpContext context) throws
            IOException,
            ClientProtocolException {
        LOG.traceEntry("Parameters : {}", target, responseHandler, context);
        LOG.info("Executing request to {}  via {}:{}", target, this.getProxy().getHostName(),
                this.getProxy().getPort());
        return LOG.
                traceExit(this.httpClient.execute(target, this.request, responseHandler, context));
    }

    /**
     * Close the http connection.
     * @throws IOException 
     */
    @Override
    public void close() throws IOException {
        LOG.traceEntry();
        this.httpClient.close();
        LOG.traceExit();
    }
}

