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

import fr.cnes.httpclient.configuration.ProxyConfiguration;
import fr.cnes.httpclient.configuration.ProxySPNegoAPIConfiguration;
import fr.cnes.httpclient.configuration.ProxySPNegoJAASConfiguration;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Creates a HttpClient based on a proxy type.
 *
 * @author Jean-Christophe Malapert
 */
public class HttpClientFactory {

    /**
     * Type of HTTP client.
     */
    public enum Type {
        /**
         * HTTP client through a proxy authenticated by SPNego and configured by a JAAS
         * configuration file.
         */
        PROXY_SPNEGO_JAAS,
        /**
         * HTTP client through a proxy authenticated by SPNego and configured by the API.
         */
        PROXY_SPNEGO_API,
        /**
         * HTTP client through a proxy with/without a basic authentication.
         */
        PROXY_BASIC,
        /**
         * HTTP client without a proxy.
         */
        NO_PROXY;
    }

    /**
     * Get actual class name to be printed on.
     */
    private static final Logger LOG = LogManager.getLogger(HttpClientFactory.class.getName());

    /**
     * Creates a HTTP client according to a given proxy type.
     *
     * @param type proxy type
     * @return the HTTP client.
     * @throws IllegalArgumentException Unknown httpclient type
     */
    public static HttpClient create(final Type type) {
        return HttpClientFactory.create(type, false, new HashMap());
    }

    /**
     * Creates a HTTP client according to a proxy type, a parameter to disable the SSL certificate
     * checking and options for HTTP client.
     *
     * @param type proxy type
     * @param isDisabledSSL True when the SSL certificate checking is disabled otherwise False
     * @param config options for HTTP Client
     * @return the HttpClient
     * @throws IllegalArgumentException Unknown httpclient type
     */
    public static HttpClient create(final Type type, final boolean isDisabledSSL, final Map<String, String> config) {
        LOG.traceEntry("Type: {}\nisDisabledSSL: {}\nconfig: {}", type, isDisabledSSL, config);
        final HttpClient httpclient;
        switch (type) {
            case PROXY_SPNEGO_JAAS:
                LOG.debug("Uses PROXY_SPNEGO_JAAS");
                httpclient = new ProxySPNegoHttpClientWithJAAS(isDisabledSSL, config);
                break;
            case PROXY_SPNEGO_API:
                LOG.debug("Uses PROXY_SPNEGO_API");
                httpclient = new ProxySPNegoHttpClientWithAPI(isDisabledSSL, config);
                break;
            case PROXY_BASIC:
                LOG.debug("Uses PROXY_BASIC");
                if(ProxyConfiguration.getConfig().get("username").isEmpty()) {
                    LOG.debug("Uses proxy without authentication");
                    httpclient = new ProxyHttpClientWithoutAuth(isDisabledSSL, config);                    
                } else {
                    LOG.debug("Uses proxy with authentication");
                    httpclient = new ProxyHttpClientWithBasicAuth(isDisabledSSL, config);
                }                
                break;
            case NO_PROXY:
                LOG.debug("Uses NO_PROXY");
                httpclient = new HttpClient(isDisabledSSL, config);
                break;
            default:
                throw LOG.throwing(new IllegalArgumentException("Unknown httpclient type"));
        }
        return LOG.traceExit(httpclient);
    }

}
