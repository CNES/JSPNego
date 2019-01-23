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
package fr.cnes.httpclient.configuration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Proxy configuration with basic authentication or without authentication.
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public enum ProxyConfiguration {
    /**
     * HTTP proxy variable (as hostname:port). 
     * By default it loads the <b>http_proxy</b> variable.
     */
    HTTP_PROXY("http_proxy", System.getenv("http_proxy")),
    /**
     * No proxy variable. 
     * A set of separated hostname/IP by comma. By default it loads the <b>no_proxy</b> variable.
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

    /**
     * key.
     */
    private final String key;
    /**
     * value.
     */
    private String value;

    /**
     * Creates enum.
     *
     * @param key key
     * @param value value
     */
    ProxyConfiguration(final String key, final String value) {
        this.key = key;
        this.value = value;
    }

    /**
     * Returns the key.
     *
     * @return the key
     */
    public String getKey() {
        return this.key;
    }

    /**
     * Returns the value or an empty string if null.
     *
     * @return the value.
     */
    public String getValue() {
        return this.value == null ? "" : this.value;
    }

    /**
     * Sets the value.
     *
     * @param value the value
     */
    public void setValue(final String value) {
        this.value = value;
    }

    /**
     * Returns the configuration as a map.
     *
     * @return the configuration
     */
    public static Map<String, String> getConfig() {
        final Map<String, String> map = new ConcurrentHashMap<>();
        final ProxyConfiguration[] confs = ProxyConfiguration.values();
        for (final ProxyConfiguration conf : confs) {
            map.put(conf.getKey(), conf.getValue());
        }
        return map;
    }

    /**
     * Validates the configuration.
     *
     * @param error error
     * @return True when the configuration is valid otherwise False
     */
    public static boolean isValid(final StringBuilder error) {
        boolean isValid = true;
        final StringBuilder validation = new StringBuilder();
        if (ProxyConfiguration.HTTP_PROXY.getValue().isEmpty()) {
            validation.append(ProxyConfiguration.HTTP_PROXY.getKey()).append(
                    " cannot be null or empty\n");
            isValid = false;
        }
        error.append(validation);
        return isValid;
    }
}
