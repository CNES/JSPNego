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
 * This package provides the classes to configure the proxies.
 *
 * Several classes are available:
 * <ul>
 * <li>ProxyConfiguration to configure a proxy with no authentication or with a basic
 * authentication.</li>
 * <li>ProxySPNegoAPIConfiguration to configure a proxy with an authentication by SSO and configured
 * by a programmatic API.</li>
 * <li>ProxySPNegoJAASConfiguration to configure a proxy with an authentication by SSI and
 * configured by a JAAS configuration file</li>
 * </ul>
 * <p>
 * <img src="{@docRoot}/doc-files/configuration.png" alt="configuration for HTTP client">
 * </p> 
 * 
 * <h2>Proxy configuration with HTTP basic (without authentication)</h2>
 * <pre>
 * <code>
 * ProxyConfiguration.HTTP_PROXY.setValue("127.0.0.1:1080");
 * ProxyConfiguration.USERNAME.setValue("");
 * ProxyConfiguration.PASSWORD.setValue("");
 * </code>
 * </pre>
 * 
 * <h2>Proxy configuration with HTTP basic (with authentication)</h2>
 * <pre>
 * <code>
 * ProxyConfiguration.HTTP_PROXY.setValue("127.0.0.1:1080");
 * ProxyConfiguration.USERNAME.setValue("foo");
 * ProxyConfiguration.PASSWORD.setValue("bar");
 * </code>
 * </pre>
 * 
 * <h2>Proxy configuration with JSPNego using JAAS</h2>
 * <pre>
 * <code>
 * ProxySPNegoJAASConfiguration.HTTP_PROXY.setValue("127.0.0.1:1080");
 * ProxySPNegoJAASConfiguration.JAAS_CONTEXT.setValue("KRB5");
 * ProxySPNegoJAASConfiguration.JAAS.setValue("/tmp/jaas.conf");
 * ProxySPNegoJAASConfiguration.SERVICE_PROVIDER_NAME.setValue("HTTP@127.0.0.1");
 * 
 * with jaas.conf:
 * KRB5 {
 *   com.sun.security.auth.module.Krb5LoginModule required
 *   useKeyTab=true
 *   keyTab="/home/ad/doi_kerberos/doi_kerberos.keytab"
 *   debug=true
 *   principal="doi_kerberos@SIS.CNES.FR";
 * };
 * </code>
 * </pre>
 * 
 * <h2>Proxy configuration with JSPNego using API</h2>
 * TODO
 */
package fr.cnes.httpclient.configuration;
