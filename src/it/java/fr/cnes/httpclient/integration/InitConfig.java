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
package fr.cnes.httpclient.integration;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


/**
 *
 * @author Jean-Christophe Malapert
 */
public class InitConfig {
    
    public final static String PROXY_HOST = "Starter.Proxy.host";
    public final static String PROXY_PORT = "Starter.Proxy.port";
    public final static String PROXY_LOGIN = "Starter.Proxy.login";
    public final static String PROXY_PWD = "Starter.Proxy.pwd";
    public final static String PROXY_NOPROXY = "Starter.NoProxy.hosts";
    
    public final static String PROXY_JAAS_HOST = "Starter.Proxy.Jaas.host";    
    public final static String PROXY_JAAS_PORT = "Starter.Proxy.Jaas.port";       
    public final static String PROXY_JAAS_SPN = "Starter.Proxy.Jaas.Spn";
    public final static String PROXY_JAAS_FILE = "Starter.Proxy.Jaas.File";
    public final static String PROXY_JAAS_CONTEXT = "Starter.Proxy.Jaas.Context";
    public final static String PROXY_JAAS_NOPROXY = "Starter.NoProxy.Jaas.hosts";

    public final static String PROXY_API_HOST = "Starter.Proxy.API.host";    
    public final static String PROXY_API_PORT = "Starter.Proxy.API.port";       
    public final static String PROXY_API_SPN = "Starter.Proxy.API.Spn";
    public final static String PROXY_API_LOGIN = "Starter.Proxy.API.Login";
    public final static String PROXY_API_KEYTAB = "Starter.Proxy.API.KeyTab";
    public final static String PROXY_API_NOPROXY = "Starter.NoProxy.API.hosts";       
       
    public final static String CONFIG_IT = "config-it.properties";
    
    public static Properties getProperties(final String filename) throws IOException {
        final Properties properties = new Properties();
        try (InputStream inputStream = ITProxyHttpClientWithBasicAuth.class.getResourceAsStream(
                "/"+CONFIG_IT)) {
            properties.load(inputStream);
        } 
        return properties;
    }
    
}
