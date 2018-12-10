/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.httpclient;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthSchemeProvider;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author malapert
 */
public abstract class AbstractProxyHttpClient extends HttpClient {
    
    private static final Logger LOG = LogManager.getLogger(AbstractProxyHttpClient.class.getName());
   
    protected final HttpClientBuilder createBuilder(final HttpHost proxy, final List<String> excludedHosts, final boolean isDisabledSSL) {
        final CredentialsProvider crp = createCredsProvider(proxy);
        final Registry<AuthSchemeProvider> regAuth = registerAuthSchemeProvider();
        HttpClientBuilder builder = HttpClients.custom()   
                .setRoutePlanner(configureRouterPlanner(proxy, excludedHosts));  
        
        if(crp != null) {
            builder = builder.setDefaultCredentialsProvider(crp);
        }
        
        if(regAuth != null) {
            builder = builder.setDefaultAuthSchemeRegistry(regAuth);
        }
        
        if (isDisabledSSL) {
            LOG.warn("SSL Certificate checking is disabled. The connection is insecured.");
            builder = builder.setSSLContext(disableSSLCertificateChecking())
                    .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE);
        }  
        return builder;
    }
    
    protected final HttpHost buildProxy(final String value) {
        String[] proxyFragments = value.split(":");
        return (proxyFragments.length == 2) 
                ? new HttpHost(proxyFragments[0], Integer.parseInt(proxyFragments[1]))
                : new HttpHost(proxyFragments[0]);
                        
    }    
    
    /**
     * http client.
     */
    private CloseableHttpClient httpClient;
    private RequestConfig config;        
    
    protected final void setHttpClient(final CloseableHttpClient httpClient) {
        this.httpClient = httpClient;
    }
    
    /**
     * Sets the proxy configuration.
     *
     * @param proxy http proxy
     */
    protected final void setProxyConfiguration(final HttpHost proxy) {
        LOG.traceEntry("proxy : {}", proxy);
        this.config = RequestConfig.custom().setProxy(proxy).build();
        LOG.traceExit();
    }    
    
    protected HttpHost getProxyConfiguration() {
        return this.config.getProxy();
    }
    
    protected CloseableHttpClient getHttpClient() {
        return this.httpClient;
    }       
    
    protected final HttpRoutePlanner configureRouterPlanner(HttpHost proxy, List<String> excludedHosts) {

        HttpRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy) {

            @Override
            public HttpRoute determineRoute(HttpHost host, HttpRequest request,
                    HttpContext context) throws HttpException {
                final HttpClientContext clientContext = HttpClientContext.adapt(context);
                final RequestConfig config = clientContext.getRequestConfig();
                final InetAddress local = config.getLocalAddress();
                HttpHost proxy = config.getProxy();

                final HttpHost target;
                if (host.getPort() > 0
                        && (host.getSchemeName().equalsIgnoreCase("http")
                        && host.getPort() == 80
                        || host.getSchemeName().equalsIgnoreCase("https")
                        && host.getPort() == 443)) {
                    target = new HttpHost(host.getHostName(), -1, host.getSchemeName());
                } else {
                    target = host;
                }
                final boolean secure = target.getSchemeName().equalsIgnoreCase("https");
                if (excludedHosts.contains(host.getHostName())) {
                    return new HttpRoute(target, local, secure);
                } else {
                    return new HttpRoute(target, local, proxy, secure);
                }
            }
        };

        return routePlanner;
    }  
    
    protected abstract CredentialsProvider createCredsProvider(final HttpHost proxy);
    
    protected abstract Registry<AuthSchemeProvider> registerAuthSchemeProvider();
    
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
        LOG.info("Executing request to {}  via {}:{}", request.getRequestLine(), this.
                getProxyConfiguration().getHostName(),
                this.getProxyConfiguration().getPort());
        if (config != null) {
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
        LOG.info("Executing request to {}  via {}:{}", target, this.getProxyConfiguration().
                getHostName(),
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
        LOG.info("Executing request to {}  via {}:{}", request.getRequestLine(), this.
                getProxyConfiguration().getHostName(),
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
        LOG.info("Executing request to {}  via {}:{}", target, this.getProxyConfiguration().
                getHostName(),
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
