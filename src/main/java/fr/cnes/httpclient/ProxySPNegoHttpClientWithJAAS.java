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
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The client makes HTTP requests via a proxy for which the client is authenticated through a SSO an
 * configured by a JAAS configuration file and the
 * {@link fr.cnes.httpclient.configuration.ProxySPNegoJAASConfiguration API}.
 *
 * The SSO uses <i>The Simple and Protected GSS-API Negotiation Mechanism (IETF RFC 2478)</i>
 * (<b>SPNEGO</b>) as protocol.
 * <br>
 * <img src="https://cdn.ttgtmedia.com/digitalguide/images/Misc/kerberos_1.gif" alt="Kerberos">
 *
 * <p>
 * The configuration file has this syntax :
 * <pre>{@code
 * <entry name> {
 *  com.sun.security.auth.module.Krb5LoginModule required
 *  <key>=<value>
 * };
 * }
 * where
 * - <i>entry name</i> is the {@link fr.cnes.httpclient.configuration.ProxySPNegoJAASConfiguration#JAAS_CONTEXT}
 * - <i>key</i>=<i>value</i> are defined <a href="https://docs.oracle.com/javase/7/docs/jre/api/security/jaas/spec/com/sun/security/auth/module/Krb5LoginModule.html">here</a>
 * </pre>
 *
 * @author S. ETCHEVERRY
 * @author Jean-Christophe Malapert
 */
public final class ProxySPNegoHttpClientWithJAAS extends AbstractProxySPNegoHttpClient {

    /**
     * Get actual class name to be printed on.
     */
    private static final Logger LOG = LogManager.getLogger(ProxySPNegoHttpClientWithJAAS.class.
            getName());

    /**
     * Creates a HTTP client that makes requests to a proxy authenticated with SSO and configured by
     * a JAAAS configuration file. Uses
     * {@link fr.cnes.httpclient.configuration.ProxySPNegoJAASConfiguration} class to configure this
     * proxy.
     */
    public ProxySPNegoHttpClientWithJAAS() {
        this(false);
    }

    /**
     * Creates a HTTP client that makes requests to a proxy authenticated with SSO and configured by
     * a JAAAS configuration file. Uses
     * {@link fr.cnes.httpclient.configuration.ProxySPNegoJAASConfiguration} class to configure this
     * proxy.
     *
     * @param isDisabledSSL True when the SSL certificate check is disabled otherwise False.
     */
    public ProxySPNegoHttpClientWithJAAS(final boolean isDisabledSSL) {
        super(isDisabledSSL, Type.PROXY_SPNEGO_JAAS);
    }

    /**
     * Creates a HTTP client that makes requests to a proxy authenticated with SSO and configured by
     * a JAAAS configuration file. Uses
     * {@link fr.cnes.httpclient.configuration.ProxySPNegoJAASConfiguration} class to configure this
     * proxy.
     *
     * @param isDisabledSSL True when the SSL certificate check is disabled otherwise False.
     * @param config options for Http client
     */
    public ProxySPNegoHttpClientWithJAAS(final boolean isDisabledSSL,
            final Map<String, String> config) {
        super(isDisabledSSL, Type.PROXY_SPNEGO_JAAS, config);
    }

}
