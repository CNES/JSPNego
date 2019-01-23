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

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Configuration for SPNego using JAAS configuration file.
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public enum ProxySPNegoJAASConfiguration {
    /**
     * HTTP proxy variable (as hostname:port). 
     * By default it loads the <b>http_proxy</b> variable.
     */
    HTTP_PROXY("http_proxy", System.getenv("http_proxy")),
    /**
     * No proxy variable. 
     * A set of separated hostname/IP by comma. By default it loads <b>no_proxy</b> variable.
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
     * Get actual class name to be printed on.
     */
    private static final Logger LOG = LogManager.getLogger(ProxySPNegoJAASConfiguration.class.getName());

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
     *
     * @param key key
     * @param value vlaue
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
     * Returns the value or an empty string if null.
     *
     * @return the value
     */
    public String getValue() {
        return this.value == null ? "" : this.value;
    }

    /**
     * Sets the value.
     * @param value value
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
        LOG.traceEntry();
        final Map<String, String> map = new ConcurrentHashMap<>();
        final ProxySPNegoJAASConfiguration[] confs = ProxySPNegoJAASConfiguration.values();
        for (ProxySPNegoJAASConfiguration conf : confs) {
            LOG.debug("config - "+conf.getKey()+"="+conf.getValue());
            map.put(conf.getKey(), conf.getValue());
        }
        return LOG.traceExit(map);
    }

    /**
     * Returns true when the configuration seems to be valid.
     *
     * @param error errors
     * @return true when the configuration is valid otherwise false
     */
    public static boolean isValid(final StringBuilder error) {
        LOG.traceEntry();
        boolean isValid = true;
        final StringBuilder validation = new StringBuilder();
        if (ProxySPNegoJAASConfiguration.HTTP_PROXY.getValue().isEmpty()) {
            validation.append(ProxySPNegoJAASConfiguration.HTTP_PROXY.getKey()).append(
                    " cannot be null or empty\n");
            LOG.error(ProxySPNegoJAASConfiguration.HTTP_PROXY.getKey()+": "+ProxySPNegoJAASConfiguration.HTTP_PROXY.getValue()+" cannot be null or empty\n");
            isValid = false;
        }
        if (!Files.isReadable(Paths.get(ProxySPNegoJAASConfiguration.KRB5.getValue()))) {
            validation.append("Kerberos configuration file must be readable");
            LOG.error(ProxySPNegoJAASConfiguration.KRB5.getKey()+": "+ProxySPNegoJAASConfiguration.KRB5.getValue()+" - Kerberos configuration file must be readable");
            isValid = false;
        }
        if (ProxySPNegoJAASConfiguration.SERVICE_PROVIDER_NAME.getValue().isEmpty()) {
            validation.append(ProxySPNegoJAASConfiguration.SERVICE_PROVIDER_NAME.getKey()).append(
                    " must be set");
            LOG.error(ProxySPNegoJAASConfiguration.SERVICE_PROVIDER_NAME.getKey()+": value is not set");
            isValid = false;
        }
        if (ProxySPNegoJAASConfiguration.JAAS.getValue().isEmpty() || !Files.isReadable(Paths.get(
                ProxySPNegoJAASConfiguration.JAAS.getValue()))) {
            validation.append(ProxySPNegoJAASConfiguration.JAAS.getKey()).append(
                    " must be a readable file\n");
            LOG.error(ProxySPNegoJAASConfiguration.JAAS.getKey()+": "+ProxySPNegoJAASConfiguration.JAAS.getValue()+" must be a readable file");
            isValid = false;
        }
        if (ProxySPNegoJAASConfiguration.JAAS_CONTEXT.getValue().isEmpty()) {
            validation.append(ProxySPNegoJAASConfiguration.JAAS_CONTEXT.getKey()).append(
                    " cannot be null or empty\n");
            LOG.error(ProxySPNegoJAASConfiguration.JAAS_CONTEXT.getKey()+": "+ProxySPNegoJAASConfiguration.JAAS_CONTEXT.getValue()+" cannot be null or empty");
            isValid = false;            
        }
        if (ProxySPNegoJAASConfiguration.KRB5.getValue().isEmpty() || !Files.isReadable(Paths.get(
                ProxySPNegoJAASConfiguration.KRB5.getValue()))) {
            validation.append(ProxySPNegoJAASConfiguration.KRB5.getKey()).append(
                    " must be a readable file\n");
            LOG.error(ProxySPNegoJAASConfiguration.KRB5.getKey()+": "+ProxySPNegoJAASConfiguration.KRB5.getValue()+" must be a readable file");
            isValid = false;
        }
        error.append(validation);

        return LOG.traceExit(isValid);
    }

}
