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
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The client makes HTTP requests via a proxy for which the client is authenticated through a SSO
 * and configured by the {@link fr.cnes.httpclient.configuration.ProxySPNegoAPIConfiguration API}
 *
 * The SSO uses <i>The Simple and Protected GSS-API Negotiation Mechanism (IETF RFC 2478)</i>
 * (<b>SPNEGO</b>) as protocol.
 * <br>
 * <img src="https://cdn.ttgtmedia.com/digitalguide/images/Misc/kerberos_1.gif" alt="Kerberos">
 *
 * @author S. ETCHEVERRY
 * @author Jean-Christophe Malapert
 */
public final class ProxySPNegoHttpClientWithAPI extends AbstractProxySPNegoHttpClient {

    /**
     * Get actual class name to be printed on.
     */
    private static final Logger LOG = LogManager.getLogger(ProxySPNegoHttpClientWithAPI.class.
            getName());

    /**
     * Creates a HTTP client based on a proxy having a SSO authentication (API).     
     */
    public ProxySPNegoHttpClientWithAPI() {
        this(false);
    }

    /**
     * Creates a HTTP client based on a proxy having a SSO authentication (API).
     * The {@link fr.cnes.httpclient.configuration.ProxySPNegoAPIConfiguration} must be configured 
     * before using this constructor.   
     * @param isDisabledSSL True when the SSL certificate check is disabled otherwise False.
     */
    public ProxySPNegoHttpClientWithAPI(final boolean isDisabledSSL) {
        super(isDisabledSSL, Type.PROXY_SPNEGO_API);
    }

    /**
     * Creates a HTTP client based on a proxy having a SSO authentication (API) and options for 
     * HTTP client.
     * The {@link fr.cnes.httpclient.configuration.ProxySPNegoAPIConfiguration} must be configured 
     * before using this constructor.   
     * @param isDisabledSSL True when the SSL certificate check is disabled otherwise False.
     * @param config options for HTTP client
     */
    public ProxySPNegoHttpClientWithAPI(final boolean isDisabledSSL,
            final Map<String, String> config) {
        super(isDisabledSSL, Type.PROXY_SPNEGO_API, config);
    }

}
