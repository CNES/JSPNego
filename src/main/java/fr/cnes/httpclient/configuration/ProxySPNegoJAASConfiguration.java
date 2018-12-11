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

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Configuration for SPNego using JAAS configuration file.
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public enum ProxySPNegoJAASConfiguration {
    /**
     * HTTP proxy variable (as hostname:port). By default loads the http_proxy variable.
     */
    HTTP_PROXY("http_proxy", System.getenv("http_proxy")),
    /**
     * No proxy variable. A set of separated hostname/IP by comma.
     */
    NO_PROXY("no_proxy", System.getenv("no_proxy")),
    /**
     * JAAS configuration file.
     */
    JAAS("jassFile", ""),
    /**
     * One of the JAAS context in the JAAS configuration file.
     */
    JAAS_CONTEXT("jassContext", "client"),
    /**
     * Service principal name
     */
    SERVICE_PROVIDER_NAME("spn", ""),
    /**
     * Location of the kerberos configuration file. By default, look at /et/krb5.conf
     */
    KRB5("krb5File", "/etc/krb5.conf");

    /**
     * key.
     */
    private final String key;
    /**
     * Value.
     */
    private String value;

    /**
     * Creates enum.
     * @param key key
     * @param value  vlaue
     */
    ProxySPNegoJAASConfiguration(final String key, final String value) {
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
     * Returns the value.
     *
     * @return the value
     */
    public String getValue() {
        return this.value == null ? "" : this.value;
    }

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
        final ProxySPNegoJAASConfiguration[] confs = ProxySPNegoJAASConfiguration.values();
        for (ProxySPNegoJAASConfiguration conf : confs) {
            map.put(conf.getKey(), conf.getValue());
        }
        return map;
    }

    /**
     * Returns true when the configuration seems to be valid.
     *
     * @param error errors
     * @return true when the configuration is valid otherwise false
     */
    public static boolean isValid(final StringBuilder error) {
        boolean isValid = true;
        final StringBuilder validation = new StringBuilder();
        if (ProxySPNegoJAASConfiguration.HTTP_PROXY.getValue().isEmpty()) {
            validation.append(ProxySPNegoJAASConfiguration.HTTP_PROXY.getKey()).append(
                    " cannot be null or empty\n");
            isValid = false;
        }
        if (!Files.isReadable(Paths.get(ProxySPNegoJAASConfiguration.KRB5.getValue()))) {
            validation.append("Kerberos configuration file must be readable");
            isValid = false;
        }
        if (ProxySPNegoJAASConfiguration.SERVICE_PROVIDER_NAME.getValue().isEmpty()) {
            validation.append(ProxySPNegoJAASConfiguration.SERVICE_PROVIDER_NAME.getKey()).append(
                    " must be set");
            isValid = false;
        }
        if (ProxySPNegoJAASConfiguration.JAAS.getValue().isEmpty() || Files.isReadable(Paths.get(
                ProxySPNegoJAASConfiguration.JAAS.getValue()))) {
            validation.append(ProxySPNegoJAASConfiguration.JAAS.getKey()).append(
                    " must be a readable file\n");
            isValid = false;
        }
        if (ProxySPNegoJAASConfiguration.JAAS_CONTEXT.getValue().isEmpty()) {
            validation.append(ProxySPNegoJAASConfiguration.JAAS_CONTEXT.getKey()).append(
                    " cannot be null or empty\n");
            isValid = false;
        }
        if (ProxySPNegoJAASConfiguration.KRB5.getValue().isEmpty() || Files.isReadable(Paths.get(
                ProxySPNegoJAASConfiguration.KRB5.getValue()))) {
            validation.append(ProxySPNegoJAASConfiguration.KRB5.getKey()).append(
                    " must be a readable file\n");
            isValid = false;
        }
        if (ProxySPNegoJAASConfiguration.SERVICE_PROVIDER_NAME.getValue().isEmpty()) {
            validation.append(ProxySPNegoJAASConfiguration.SERVICE_PROVIDER_NAME.getKey()).append(
                    " cannot be null or empty\n");
            isValid = false;
        }
        return isValid;
    }

}
