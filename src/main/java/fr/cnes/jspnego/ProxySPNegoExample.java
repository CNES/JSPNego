package fr.cnes.jspnego;

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
import java.io.IOException;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class is an example of the used of the ProxySPNego classes It uses a ProxySPNegoHttpClient
 * to connect to authenticate to a proxy host Several websites are called to demonstrate the cache
 * of the kerberos token
 *
 * @author S. ETCHEVERRY
 */
public class ProxySPNegoExample {

    /** 
     * Get actual class name to be printed on 
     */
    private static final Logger LOG = LogManager.getLogger(ProxySPNegoExample.class.getName());

    /**
     * Example of the use of the Proxy SPNego scheme
     *
     * @param args proxyHost proxyPort userId (keytabFilePath) (ticketCachePath)
     * @throws Exception
     */
    public static void main(final String[] args) throws Exception {

        if (args.length < 3) {
            System.out.println(
                    "Usage: ProxySPNegoExample <proxyHost> <proxyPort> <userId> (<keytabFilePath>) "
                            + "(<ticketCachePath>)");
            System.exit(1);
        }

        final String proxyHost = args[0];
        final int proxyPort = Integer.parseInt(args[1]);
        final String userId = args[2];

        String keytabPath = null;
        if (args.length > 3) {
            keytabPath = args[3];
        }

        String ticketCachePath = null;
        if (args.length > 4) {
            ticketCachePath = args[4];
        }

        try (
                ProxySPNegoHttpClient httpclient = new ProxySPNegoHttpClient(
                        userId, keytabPath, ticketCachePath, proxyHost, proxyPort
                )) {

            HttpHost target = new HttpHost("www.google.com", 443, "https");
            HttpResponse response = httpclient.execute(target);
            LOG.info("---------------------------------\n{}", response.getStatusLine());

            target = new HttpHost("www.nasa.gov", 443, "https");
            response = httpclient.execute(target);
            LOG.info("-----------------------------------\n{}", response.getStatusLine());

            target = new HttpHost("www.larousse.fr", 80, "http");
            response = httpclient.execute(target);
            LOG.info("----------------------------------------\n{}", response.getStatusLine());

        } catch (IOException e) {
            LOG.error(e.toString());
        }
    }

}
