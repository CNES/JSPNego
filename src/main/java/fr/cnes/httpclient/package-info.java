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
 * 
 * <p>
 * <img src="{@docRoot}/doc-files/httpclient.png" alt="HTTP client">
 * </p>
 * 
 * <h2>Request without a proxy</h2>
 * <pre>
 * <code>
 * HttpClient client = HttpClientFactory.create(HttpClientFactory.Type.NO_PROXY);
 * HttpResponse response = client.execute(new HttpGet("http://www.google.fr"));
 * </code>
 * </pre>
 * 
 * 
 * <h2>Request with a proxy using BASIC HTTP</h2>
 * <pre>
 * <code>
 * ProxyConfiguration.HTTP_PROXY.setValue("127.0.0.1:1080");
 * ProxyConfiguration.USERNAME.setValue("foo");
 * ProxyConfiguration.PASSWORD.setValue("bar");
 * HttpClient client = HttpClientFactory.create(HttpClientFactory.Type.PROXY_BASIC);
 * HttpResponse response = client.execute(new HttpGet("http://www.google.fr"));
 * </code>
 * </pre>
 * 
 * <h2>Request with a proxy using JSPNego (JAAS)</h2>
 * <pre>
 * <code>
 * ProxySPNegoJAASConfiguration.HTTP_PROXY.setValue("127.0.0.1:1080");
 * ProxySPNegoJAASConfiguration.JAAS_CONTEXT.setValue("KRB5");
 * ProxySPNegoJAASConfiguration.JAAS.setValue("/tmp/jaas.conf");
 * ProxySPNegoJAASConfiguration.SERVICE_PROVIDER_NAME.setValue("HTTP@127.0.0.1");
 * HttpClient client = HttpClientFactory.create(HttpClientFactory.Type.PROXY_SPNEGO_JAAS);
 * HttpResponse response = client.execute(new HttpGet("http://www.google.fr")); 
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
 * <h2>Request with a proxy using JSPNego (API)</h2>
 * TODO
 */
package fr.cnes.httpclient;