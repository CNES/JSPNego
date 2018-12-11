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
/**
 * Clients to make Http requests.
 *
 * It exists several clients:
 * <ul>
 * <li>HttpClient to execute requests without a proxy</li>
 * <li>ProxyHttpClientWithoutAuth to execute requests through a proxy without authentication</li>
 * <li>ProxyHttpClientWithBasicAuth to execute requests through a proxy with a basic
 * authentication</li>
 * <li>ProxySPNegoHttpClientWithAPI to execute requests through a proxy with a SSO authentication
 * and configured by a programmatic API.</li>
 * <li>ProxySPNegoJAASClientWithAPI to execute requests through a proxy with a SSO authentication
 * and configured by a configuration file.</li>
 * </ul>
 *
 * In addition to that, a factory is available to create one of the clients. However, before
 * creating a proxy, the proxy must be configured using the configuration
 * {@link fr.cnes.httpclient.configuration package}.
 */
package fr.cnes.httpclient;
