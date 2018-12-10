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
package fr.cnes.httpclient.configuration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Proxy configuration with basic authentication or without authentication.
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public enum ProxyConfiguration {
    /**
     * HTTP proxy variable (as hostname:port).
     * By default loads the http_proxy variable.
     */    
    HTTP_PROXY("http_proxy", System.getenv("http_proxy")),
    /**
     * No proxy variable.
     * A set of separated hostname/IP by comma.
     */  
    NO_PROXY("no_proxy", System.getenv("no_proxy")),
    /**
     * Username if needed.
     */
    USERNAME("username", ""),
    /**
     * Password if needed.
     */
    PASSWORD("password", "");

    private final String key;
    private String value;

    ProxyConfiguration(final String key, final String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return this.key;
    }

    public String getValue() {
        return this.value == null ? "" : this.value;
    }
    
    public void setValue(final String value) {
        this.value = value;
    }    

    public static Map<String, String> getConfig() {
        final Map<String, String> map = new ConcurrentHashMap<>();
        final ProxySPNegoAPIConfiguration[] confs = ProxySPNegoAPIConfiguration.values();
        for (ProxySPNegoAPIConfiguration conf : confs) {
            map.put(conf.getKey(), conf.getValue());
        }
        return map;
    }    

    public static boolean isValid(StringBuilder error) {
        boolean isValid = true;
        final StringBuilder validation = new StringBuilder();
        if(ProxyConfiguration.HTTP_PROXY.getValue().isEmpty()) {
            validation.append(ProxyConfiguration.HTTP_PROXY.getKey()).append(" cannot be null or empty\n");
            isValid = false;
        }    
        error.append(validation);
        return isValid;
    }
}
