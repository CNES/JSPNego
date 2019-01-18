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
import java.io.IOException;
import java.net.InetAddress;
import java.util.List;
import java.util.Map;
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
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Handls a Http client through a proxy.
 *
 * @author Jean-Christophe Malapert
 */
public abstract class AbstractProxyHttpClient extends HttpClient {

    /**
     * Get actual class name to be printed on.
     */
    private static final Logger LOG = LogManager.getLogger(AbstractProxyHttpClient.class.getName());

    /**
     * requestConfig that contains the proxy configuration.
     */
    private RequestConfig requestConfig;

    /**
     * Creates an AbstractProxyHttpClient based on http client options.
     *
     * @param isDisabledSSL True when SSL certificates are disabled otherwise False
     * @param config options for Http client
     * @param type type of Http client
     */
    protected AbstractProxyHttpClient(final boolean isDisabledSSL, final Map<String, String> config, final Type type) {
        super(isDisabledSSL, config, type);
    }

    /**
     * Creates builder proxy.
     *
     * @param builder builder
     * @return builder
     */
    @Override
    protected abstract HttpClientBuilder createBuilderProxy(final HttpClientBuilder builder);

    /**
     * Creates proxy builder.
     *
     * @param builder builder
     * @param proxy proxy
     * @param excludedHosts excluded hosts
     * @return builder including proxy
     */
    protected final HttpClientBuilder createBuilder(final HttpClientBuilder builder,
            final HttpHost proxy,
            final List<String> excludedHosts) {
        LOG.traceEntry("builder: {}\n"
                + "proxy: {}\n"
                + "excludedHosts: {}", builder, proxy, excludedHosts);
        final CredentialsProvider crp = createCredsProvider(proxy);
        final Registry<AuthSchemeProvider> regAuth = registerAuthSchemeProvider();
        builder.setRoutePlanner(configureRouterPlanner(proxy, excludedHosts));

        if (crp != null) {
            LOG.debug("Adds credentials to builder");
            builder.setDefaultCredentialsProvider(crp);
        }

        if (regAuth != null) {
            LOG.debug("Adds auth scheme");
            builder.setDefaultAuthSchemeRegistry(regAuth);
        }

        this.setProxyConfiguration(proxy);

        return LOG.traceExit(builder);
    }

    /**
     * Build the proxy.
     *
     * @param value proxy value as hostname:port
     * @return the proxy
     */
    protected final HttpHost stringToProxy(final String value) {
        LOG.traceEntry("value: {}", value);
        final String[] proxyFragments = value.split(":");
        return LOG.traceExit((proxyFragments.length == 2)
                ? new HttpHost(proxyFragments[0], Integer.parseInt(proxyFragments[1]))
                : new HttpHost(proxyFragments[0]));
    }

    /**
     * Sets the proxy configuration.
     *
     * @param proxy http proxy
     */
    protected final void setProxyConfiguration(final HttpHost proxy) {
        LOG.traceEntry("proxy : {}", proxy);
        this.requestConfig = RequestConfig.custom().setProxy(proxy).build();
        LOG.traceExit();
    }

    /**
     * Returns the proxy configuration.
     *
     * @return the proxy configuration
     */
    protected HttpHost getProxyConfiguration() {
        LOG.traceEntry();
        return LOG.traceExit(this.requestConfig.getProxy());
    }

    /**
     * Configures route.
     *
     * @param proxy proxy
     * @param excludedHosts host for which hth proxy is not needed
     * @return Http router planner
     */
    protected final HttpRoutePlanner configureRouterPlanner(final HttpHost proxy,
            final List<String> excludedHosts) {
        LOG.traceEntry("proxy: {}\n"
                + "excludedHosts: {}", proxy, excludedHosts);
        final HttpRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy) {

            /**
             * {@inheritDoc }
             */
            @Override
            public HttpRoute determineRoute(final HttpHost host, final HttpRequest request,
                    final HttpContext context) throws HttpException {
                LOG.traceEntry("host: {}\nrequest: {}\ncontext: {}", host, request, context);
                final HttpClientContext clientContext = HttpClientContext.adapt(context);
                final RequestConfig config = clientContext.getRequestConfig();
                final InetAddress local = config.getLocalAddress();
                final HttpHost proxy = config.getProxy();

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
                    LOG.debug(host.getHostName()+" is excluded from proxy");
                    return LOG.traceExit(new HttpRoute(target, local, secure));
                } else {
                    LOG.debug(host.getHostName()+" uses the proxy");
                    return LOG.traceExit(new HttpRoute(target, local, proxy, secure));
                }
            }
        };

        return LOG.traceExit(routePlanner);
    }

    /**
     * Creates the credentials.
     *
     * @param proxy proxy
     * @return the credentials
     */
    protected abstract CredentialsProvider createCredsProvider(final HttpHost proxy);

    /**
     * Registers the authentication scheme.
     *
     * @return the authentication scheme or {@code null}
     */
    protected abstract Registry<AuthSchemeProvider> registerAuthSchemeProvider();

    /**
     * {@inheritDoc }
     */
    @Override
    public HttpParams getParams() {
        LOG.traceEntry();
        return LOG.traceExit(this.getHttpClient().getParams());
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public ClientConnectionManager getConnectionManager() {
        LOG.traceEntry();
        return LOG.traceExit(this.getHttpClient().getConnectionManager());
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public HttpResponse execute(final HttpUriRequest request) throws IOException,
            ClientProtocolException {
        return this.execute(request, new HttpClientContext());
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public HttpResponse execute(final HttpUriRequest request, final HttpContext context) throws
            IOException,
            ClientProtocolException {
        LOG.traceEntry("request : {}\n"
                + "context: {}",
                request, context);
        LOG.info("Executing request to {}  via {}:{}", request.getRequestLine(), this.
                getProxyConfiguration().getHostName(),
                this.getProxyConfiguration().getPort());
        if (requestConfig != null) {
            context.setAttribute(HttpClientContext.REQUEST_CONFIG, requestConfig);
        }
        return this.getHttpClient().execute(request, context);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public HttpResponse execute(final HttpHost target, final HttpRequest request) throws IOException,
            ClientProtocolException {
        return this.execute(target, request, new HttpClientContext());
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public HttpResponse execute(final HttpHost target, final HttpRequest request,
            final HttpContext context) throws
            IOException, ClientProtocolException {
        LOG.traceEntry("target : {}\n"
                + "request: {}\n"
                + "context: {}",
                target, request, context);
        context.setAttribute(HttpClientContext.REQUEST_CONFIG, requestConfig);
        LOG.info("Executing request to {}  via {}:{}", target, this.getProxyConfiguration().
                getHostName(),
                this.getProxyConfiguration().getPort());
        return LOG.traceExit(this.getHttpClient().execute(target, request, context));
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public <T> T execute(final HttpUriRequest request,
            final ResponseHandler<? extends T> responseHandler) throws IOException,
            ClientProtocolException {
        return this.execute(request, responseHandler, new HttpClientContext());
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public <T> T execute(final HttpUriRequest request,
            final ResponseHandler<? extends T> responseHandler, final HttpContext context) throws
            IOException,
            ClientProtocolException {
        LOG.traceEntry("request : {}\n"
                + "responseHandler: {}\n"
                + "context: {}",
                request, responseHandler, context);
        context.setAttribute(HttpClientContext.REQUEST_CONFIG, requestConfig);
        LOG.info("Executing request to {}  via {}:{}", request.getRequestLine(), this.
                getProxyConfiguration().getHostName(),
                this.getProxyConfiguration().getPort());
        return LOG.traceExit(this.getHttpClient().execute(request, responseHandler, context));
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public <T> T execute(final HttpHost target, final HttpRequest request,
            final ResponseHandler<? extends T> responseHandler) throws IOException,
            ClientProtocolException {
        return this.execute(target, request, responseHandler, new HttpClientContext());
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public <T> T execute(final HttpHost target, final HttpRequest request,
            final ResponseHandler<? extends T> responseHandler, final HttpContext context) throws
            IOException,
            ClientProtocolException {
        LOG.traceEntry("target : {}\n"
                + "responseHandler: {}\n"
                + "context: {}",
                target, responseHandler, context);
        context.setAttribute(HttpClientContext.REQUEST_CONFIG, requestConfig);
        LOG.info("Executing request to {}  via {}:{}", target, this.getProxyConfiguration().
                getHostName(),
                this.getProxyConfiguration().getPort());
        return LOG.
                traceExit(this.getHttpClient().execute(target, request, responseHandler, context));
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void close() {
        LOG.traceEntry();
        try {
            this.getHttpClient().close();
        } catch (IOException ex) {
            LOG.error(ex);
        }
        LOG.traceExit();
    }

}
