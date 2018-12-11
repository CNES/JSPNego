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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Http client factory.
 *
 * @author Jean-Christophe Malapert
 */
public class HttpClientFactory {

    /**
     * Type of Http client.
     */
    public enum Type {
        /**
         * http client through a proxy authenticated by SPNego and configured by a JAAS
         * configuration file.
         */
        PROXY_SPNEGO_JAAS,
        /**
         * http client through a proxy authenticated by SPNego and configured by the API.
         */
        PROXY_SPNEGO_API,
        /**
         * http client through a proxy with/without a basic authentication.
         */
        PROXY_BASIC,
        /**
         * http client wihtout a proxy.
         */
        NO_PROXY;
    }

    /**
     * Get actual class name to be printed on.
     */
    private static final Logger LOG = LogManager.getLogger(HttpClientFactory.class.getName());

    /**
     * Creates a Http client according to a given type.
     *
     * @param type type of http client
     * @return the Http client.
     */
    public static HttpClient create(final Type type) {
        LOG.traceEntry("Type: {}", type);
        return LOG.traceExit(HttpClientFactory.create(type, false));
    }

    /**
     * Create a Http client according to a given type and a parameter to disable the SSL certificate
     * checking.
     *
     * @param type type of http client
     * @param isDisabledSSL True when the SSL certificate checking is disabled otherwise False
     * @return the HttpClient
     * @throws IllegalArgumentException Unknown httpclient type
     */
    public static HttpClient create(final Type type, final boolean isDisabledSSL) {
        LOG.traceEntry("Type: {}\nisDisabledSSL: {}", type, isDisabledSSL);
        final HttpClient httpclient;
        switch (type) {
            case PROXY_SPNEGO_JAAS:
                LOG.debug("Uses PROXY_SPNEGO_JAAS");
                httpclient = new ProxySPNegoHttpClientWithJAAS(isDisabledSSL);
                break;
            case PROXY_SPNEGO_API:
                LOG.debug("Uses PROXY_SPNEGO_API");
                httpclient = new ProxySPNegoHttpClientWithAPI(isDisabledSSL);
                break;
            case PROXY_BASIC:
                LOG.debug("Uses PROXY_BASIC");
                httpclient = new ProxyHttpClientWithBasicAuth(isDisabledSSL);
                break;
            case NO_PROXY:
                LOG.debug("Uses NO_PROXY");
                httpclient = new HttpClient(isDisabledSSL);
                break;
            default:
                throw LOG.throwing(new IllegalArgumentException("Unknown httpclient type"));
        }
        return LOG.traceExit(httpclient);
    }

}
