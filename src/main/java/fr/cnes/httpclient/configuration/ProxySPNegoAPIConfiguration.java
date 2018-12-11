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
 * Configuration for SPNego using the programmatic API.
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public enum ProxySPNegoAPIConfiguration {
    /**
     * HTTP proxy variable (as hostname:port). By default loads the http_proxy variable.
     */
    HTTP_PROXY("http_proxy", System.getenv("http_proxy")),
    /**
     * No proxy variable. A set of separated hostname/IP by comma.
     */
    NO_PROXY("no_proxy", System.getenv("no_proxy")),
    /**
     * Set this to true, if you want the configuration to be refreshed before the login method is
     * called. Default is true
     */
    REFRESH_KRB5_CONFIG("refreshKrb5Config", "true"),
    /**
     * Set this to true, if you want the TGT to be obtained from the ticket cache. Set this option
     * to false if you do not want this module to use the ticket cache. (Default is False).
     */
    USE_TICKET_CACHE("useTicketCache", "false"),
    /**
     * Set this to the name of the ticket cache that contains user's TGT. If this is set,
     * useTicketCache must also be set to true; Otherwise a configuration error will be returned.
     * Default is environment variable KRB5CCNAME
     */
    TICKET_CACHE("ticketCache", System.getenv("KRB5CCNAME")),
    /**
     * Set this to true, if you want to renew the TGT. If this is set, useTicketCache must also be
     * set to true; otherwise a configuration error will be returned.
     */
    RENEW_TGT("renewTGT", ""),
    /**
     * Set this to true if you do not want to be prompted for the password if credentials can not be
     * obtained from the cache, the keytab, or through shared state. (Default is false) If set to
     * true, credential must be obtained through cache, keytab, or shared state. Otherwise,
     * authentication will fail.
     */
    DO_NOT_PROMPT("doNotPrompt", "true"),
    /**
     * Set this to true if you want the module to get the principal's key from the the keytab.
     * Default value is False.
     */
    USE_KEYTAB("useKeyTab", "false"),
    /**
     * Set this to the file name of the keytab to get principal's secret key.
     */
    KEY_TAB("keyTab", ""),
    /**
     * Set this to true to if you want the keytab or the principal's key to be stored in the
     * Subject's private credentials. For isInitiator being false, if principal is "*", the KeyTab
     * stored can be used by anyone, otherwise, it's restricted to be used by the specified
     * principal only.
     */
    STORE_KEY("storeKey", "true"),
    /**
     * The name of the principal that should be used. The principal can be a simple username such as
     * "testuser" or a service name such as "host/testhost.eng.sun.com". You can use the principal
     * option to set the principal when there are credentials for multiple principals in the keyTab
     * or when you want a specific ticket cache only. The principal can also be set using the system
     * property sun.security.krb5.principal.
     */
    PRINCIPAL("principal", System.getenv("sun.security.krb5.principal")),
    /**
     * Set this to true, if initiator. Set this to false, if acceptor only. Default is true.
     */
    IS_INITIATOR("isInitiator", "true"),
    /**
     * the SPN.
     */
    SERVICE_PROVIDER_NAME("spn", ""),
    /**
     * Kereros configuration file/
     */
    KRB5("krb5File", "/etc/krb5.conf"),
    /**
     * the JAAS context sets to "other".
     */
    JAAS_CONTEXT("jassContext", "other");

    /**
     * key.
     */
    private final String key;
    /**
     * value.
     */
    private String value;

    ProxySPNegoAPIConfiguration(final String key, final String value) {
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
        return this.value;
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
        final ProxySPNegoAPIConfiguration[] confs = ProxySPNegoAPIConfiguration.values();
        for (final ProxySPNegoAPIConfiguration conf : confs) {
            map.put(conf.getKey(), conf.getValue());
        }
        return map;
    }

    /**
     * Returns true when the configuration seems to be valid.
     *
     * @param error errors
     * @return true when the configuration is valid otherwise false
     * @see <a href="https://docs.oracle.com/javase/8/docs/jre/api/security/jaas/spec/com/sun/security/auth/module/Krb5LoginModule.html">Krb5LoginModule</a>
     */
    public static boolean isValid(final StringBuilder error) {
        boolean isValid = true;
        final StringBuilder validation = new StringBuilder();
        if (ProxySPNegoAPIConfiguration.HTTP_PROXY.getValue().isEmpty()) {
            validation.append(ProxySPNegoAPIConfiguration.HTTP_PROXY.getKey()).append(
                    " cannot be null or empty\n");
            isValid = false;
        }
        if (!Files.isReadable(Paths.get(ProxySPNegoAPIConfiguration.KRB5.getValue()))) {
            validation.append("Kerberos configuration file must be readable");
            isValid = false;
        }
        if (ProxySPNegoAPIConfiguration.PRINCIPAL.getValue().isEmpty()) {
            validation.append(ProxySPNegoAPIConfiguration.PRINCIPAL.getKey()).append(" must be set");
            isValid = false;
        }
        if (ProxySPNegoAPIConfiguration.SERVICE_PROVIDER_NAME.getValue().isEmpty()) {
            validation.append(ProxySPNegoAPIConfiguration.SERVICE_PROVIDER_NAME.getKey()).append(
                    " must be set");
            isValid = false;
        }
        if (Boolean.parseBoolean(ProxySPNegoAPIConfiguration.DO_NOT_PROMPT.getValue())
                && !Boolean.parseBoolean(ProxySPNegoAPIConfiguration.USE_TICKET_CACHE.getValue())
                && !Boolean.parseBoolean(ProxySPNegoAPIConfiguration.USE_KEYTAB.getValue())) {
            validation.append("Illegal combination : DO_NOT_PROMPT=true && USE_TICKET_CACHE = false"
                    + "&& USE_KEYTAB = false");
            isValid = false;
        }
        if (!Boolean.parseBoolean(ProxySPNegoAPIConfiguration.USE_TICKET_CACHE.getValue())
                && !ProxySPNegoAPIConfiguration.TICKET_CACHE.getValue().isEmpty()) {
            validation.append("Illegal combination : USE_TICKET_CACHE=false && TICKET_CACHE is set");
            isValid = false;
        } else if (Boolean.parseBoolean(ProxySPNegoAPIConfiguration.USE_TICKET_CACHE.getValue())
                && !Files.isReadable(Paths.get(ProxySPNegoAPIConfiguration.TICKET_CACHE.getValue()))) {
            validation.append(ProxySPNegoAPIConfiguration.TICKET_CACHE.getValue()).append(
                    " is not a redable file");
            isValid = false;
        }
        if (Boolean.parseBoolean(ProxySPNegoAPIConfiguration.RENEW_TGT.getValue())
                && !Boolean.parseBoolean(ProxySPNegoAPIConfiguration.USE_TICKET_CACHE.getValue())) {
            validation.append("Illegal combination : RENEW_TGT=true &&  USE_TICKET_CACHE=false");
            isValid = false;
        }
        if (Boolean.parseBoolean(ProxySPNegoAPIConfiguration.STORE_KEY.getValue())
                && Boolean.parseBoolean(ProxySPNegoAPIConfiguration.USE_TICKET_CACHE.getValue())
                && Boolean.parseBoolean(ProxySPNegoAPIConfiguration.DO_NOT_PROMPT.getValue())) {
            validation.append(
                    "Illegal combination : STORE_KEY=true && USE_TICKET_CACHE=true && DO_NOT_PROMPT=true");
            isValid = false;
        }
        error.append(validation);
        return isValid;
    }

}
