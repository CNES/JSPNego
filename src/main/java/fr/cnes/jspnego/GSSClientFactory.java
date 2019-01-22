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
package fr.cnes.jspnego;

import fr.cnes.httpclient.HttpClientFactory.Type;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Creates a factory on GSS (Generic Security Service) Client.
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 * @author S. ETCHEVERRY
 */
public class GSSClientFactory {

    /**
     * Get actual class name to be printed on.
     */
    private static final Logger LOG = LogManager.getLogger(GSSClientFactory.class.getName());

    /**
     * Creates a GSS client according to a given type.
     *
     * @param type type to select
     * @return the GSS client
     * @throws IllegalArgumentException When type is not one of the following types
     * PROXY_SPNEGO_API, PROXY_SPNEGO_JAAS
     */
    public static AbstractGSSClient create(final Type type) {
        LOG.traceEntry("Type: {}", type);
        final AbstractGSSClient gssClient;
        switch (type) {
            case PROXY_SPNEGO_API:
                LOG.debug("Uses PROXY_SPNEGO_API");
                gssClient = new GSSClientAPI();
                break;
            case PROXY_SPNEGO_JAAS:
                LOG.debug("Uses PROXY_SPNEGO_JAAS");
                gssClient = new GSSClientJASS();
                break;
            default:
                throw LOG.throwing(new IllegalArgumentException("Cannot support " + type.name()));
        }
        return LOG.traceExit(gssClient);
    }

}
