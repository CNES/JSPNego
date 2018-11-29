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
 * JAVA library built to make HTTP requests through a proxy using a Single Sign On (SSO).
 * <h2>Architecture</h2>
 * <img src="{@docRoot}/doc-files/proxyKerberos.png" alt="Proxy Kerberos"><br>
 * A client to make an HTTP request through a proxy using SSO follow the following steps:
 * <ul>
 *  <li>1 - Ask a TGT ticket to IPA</li>
 *  <li>2 - Return  a TGT</li>
 *  <li>3 - Request a HTTP request to the proxy</li>
 *  <li>4 - Return a 407 code (Authentication Required)</li>
 *  <li>5 - Ask a TGS (Ticket Granting Service) ticket to acccess to HTTP/<i>proxy_host</i> 
 * service</li>
 *  <li>6 - Return a TGS ticket</li>
 *  <li>7 - Request a HTTP request with the TGS included in the HTTP header</li>
 *  <li>8 - The proxy validates the authentication</li>
 *  <li>9-10 - Check the authorization according to IPA registry 
 * </ul> 
 * <h2>Keytab creation</h2>
 * A keytab (short for “key table”) stores long-term keys for one or more principals. Keytabs are
 * normally represented by files in a standard format, although in rare cases they can be
 * represented in other ways. Keytabs are used most often to allow server applications to accept
 * authentications from clients, but can also be used to obtain initial credentials for client
 * applications. Keytabs are named using the format type:value. Usually type is FILE and value is
 * the absolute pathname of the file. Other possible values for type are SRVTAB, which indicates a
 * file in the deprecated Kerberos 4 srvtab format, and MEMORY, which indicates a temporary keytab
 * stored in the memory of the current process.<br>
 * A keytab contains one or more entries, where each entry consists of a timestamp (indicating when
 * the entry was written to the keytab), a principal name, a key version number, an encryption type,
 * and the encryption key itself.<br>
 * A keytab must be created using <i>ipa-getkeytab</i>:
 * <pre>  {@code
 * ipa-getkeytab -p <login>@<server> -k <keytabPath> -P
 * }
 * </pre>
 * A keytab can be displayed using the <i>klist</i>. A Keytabs can be destroyed using
 * <i>kdestroy</i>.
 * 
 * <h2>The code to request a page through a proxy using SSO</h2>
 * <pre>
 * {@code
 *      try (
 *          ProxySPNegoHttpClient httpclient = new ProxySPNegoHttpClient(
 *              userId, keytabPath, proxyHost, proxyPort
 *          )) {
 *
 *          HttpHost target = new HttpHost("www.google.com", 443, "https");
 *          HttpResponse response = httpclient.execute(target);
 *          LOG.info("Request to https://www.google.com", response.getStatusLine());
 *
 *          target = new HttpHost("www.nasa.gov", 443, "https");
 *          response = httpclient.execute(target);
 *          LOG.info("Request to https://www.nasa.gov", response.getStatusLine());
 *
 *          target = new HttpHost("www.larousse.fr", 80, "http");
 *          response = httpclient.execute(target);
 *          LOG.info("Request to http://www.larousse.fr", response.getStatusLine());
 *
 *       } catch (IOException e) {
 *           LOG.error(e.toString());
 *       }
 * }
 * </pre>
 */
package fr.cnes.jspnego;
