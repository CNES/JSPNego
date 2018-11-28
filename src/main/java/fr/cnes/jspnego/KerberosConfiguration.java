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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Security;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;

/**
 * This class removes the need for a jaas.conf file to configure the
 * com.sun.security.auth.module.Krb5LoginModule to be used for JAAS login for Kerberos client
 * (initiators).
 *
 * @see com.sun.security.auth.module.Krb5LoginModule
 * @author S. ETCHEVERRY
 */
public class KerberosConfiguration extends Configuration {

    /**
     * True as string.
     */
    private static final String STR_TRUE = "true";

    /**
     * Login module.
     */
    private static final String LOGIN_MODULE = com.sun.security.auth.module.Krb5LoginModule.class.
            getName();

    /**
     * Option to get the principal's key from the the keytab: {@value #USE_KEY_TAB}.
     */
    private static final String USE_KEY_TAB = "useKeyTab";

    /**
     * File name of the keytab to get principal's secret key: {@value #KEY_TAB}.
     */
    private static final String KEY_TAB = "keyTab";

    /**
     * Option do not prompted for the password if credentials can not be obtained from the cache,
     * the keytab, or through shared state: {@value #DO_NOT_PROMPT}.
     */
    private static final String DO_NOT_PROMPT = "doNotPrompt";

    /**
     * Option to get the TGT to be obtained from the ticket cache: {@value #USE_TICKET_CACHE}.
     */
    private static final String USE_TICKET_CACHE = "useTicketCache";

    /**
     * Name of the ticket cache that contains user's TGT: {@value #TICKET_CACHE}.
     */
    private static final String TICKET_CACHE = "ticketCache";

    /**
     * Debug.
     */
    private static final String DEBUG = "debug";

    /**
     * The name of the principal that should be used: {@value #PRINCIPAL}.
     */
    private static final String PRINCIPAL = "principal";

    /**
     * Option for the configuration to be refreshed before the login method is called:
     * {@value #REFRESH_KRB5_CONFIG}.
     */
    private static final String REFRESH_KRB5_CONFIG = "refreshKrb5Config";

    /**
     * Default value for {@value #USE_KEY_TAB}: {@value #DEFAULT_USE_KEY_TAB}.
     */
    private static final String DEFAULT_USE_KEY_TAB = STR_TRUE;

    /**
     * Default value for {@value #DO_NOT_PROMPT}: {@value #DEFAULT_DO_NOT_PROMPT}.
     */
    private static final String DEFAULT_DO_NOT_PROMPT = STR_TRUE;

    /**
     * Default value for {@value #USE_TICKET_CACHE}: {@value #DEFAULT_USE_TICKET_CACHE}.
     */
    private static final String DEFAULT_USE_TICKET_CACHE = STR_TRUE;

    /**
     * Default value for {@value #DEBUG}: {@value #DEFAULT_DEBUG}.
     */
    private static final String DEFAULT_DEBUG = STR_TRUE;

    /**
     * Default value for {@value #REFRESH_KRB5_CONFIG}: {@value #DEFAULT_REFRESH_KRB5}.
     */
    private static final String DEFAULT_REFRESH_KRB5 = STR_TRUE;

    /**
     * The user ID.
     */
    private final String principalName;

    /**
     * Options.
     */
    private final Map<String, String> options = new ConcurrentHashMap<>();

    /**
     * configuration.
     */
    private AppConfigurationEntry[] appConfigEntries;

    /**
     * Creates a Kerberos configuration object.
     *
     * @param principalName The principal can be a simple username such as "testuser" or a service
     * name such as "host/testhost.eng.sun.com".
     */
    public KerberosConfiguration(final String principalName) {
        super();
        this.principalName = principalName;
    }

    /**
     * Sets up kerberos configuration to use a keytab to initiate a ticket-granting ticket (TGT).
     * When keytabFilename is null, {@value #USE_KEY_TAB} is set to false otherwise
     * {@value #DEFAULT_USE_KEY_TAB}
     *
     * @param keytabFilename The path of the keytab file
     */
    public void setKeytab(final String keytabFilename) {
        final String useKeyTabVal = (keytabFilename == null) ? "false" : DEFAULT_USE_KEY_TAB;
        options.put(USE_KEY_TAB, useKeyTabVal);
        if (Boolean.parseBoolean(useKeyTabVal)) {
            options.put(KEY_TAB, keytabFilename);
        }
        options.put(DO_NOT_PROMPT, DEFAULT_DO_NOT_PROMPT);
    }

    /**
     * Sets up kerberos configuration to use the ticket cache file to retrieve TGT from cache. When
     * ticketCacheFileName is null or ticketCacheFileName not readable, {@value #USE_TICKET_CACHE}
     * is set to false otherwise {@value #DEFAULT_USE_TICKET_CACHE}
     *
     * @param ticketCacheFileName The path of the ticket cache file
     */
    public void setTicketCache(final String ticketCacheFileName) {
        // first use kerberos cache as specified
        final String useTicketCacheVal = (ticketCacheFileName == null
                || !Files.isReadable(Paths.get(ticketCacheFileName)))
                ? "false" : DEFAULT_USE_TICKET_CACHE;
        options.put(USE_TICKET_CACHE, useTicketCacheVal);
        if (Boolean.parseBoolean(useTicketCacheVal)) {
            options.put(TICKET_CACHE, ticketCacheFileName);
        }
    }

    /**
     * Initialize the kerberos configuration. These parameters
     * {@value #DEBUG}, {@value #PRINCIPAL}, {@value #REFRESH_KRB5_CONFIG} are inititialized to
     * true.
     */
    public void initialize() {

        options.put(DEBUG, DEFAULT_DEBUG);
        options.put(PRINCIPAL, principalName); // Ensure the correct TGT is used.
        options.put(REFRESH_KRB5_CONFIG, DEFAULT_REFRESH_KRB5);

        appConfigEntries = new AppConfigurationEntry[1];
        appConfigEntries[0] = new AppConfigurationEntry(LOGIN_MODULE,
                AppConfigurationEntry.LoginModuleControlFlag.REQUIRED, options);

        Security.setProperty("login.configuration.provider", getClass().getName());

        // For each Kerberos client that the loads we
        // need a separate instance of this class, it gets set here, so next call
        // on the LoginContext will use this instance.
        setConfiguration(this);
    }

    /**
     * (non-Javadoc)
     *
     * @param arg0 the name used as the index into the Configuration
     * @return The appConfigEntries
     * @see javax.security.auth.login.Configuration#getAppConfigurationEntry
     */
    @Override
    public AppConfigurationEntry[] getAppConfigurationEntry(final String arg0) {
        return appConfigEntries == null ? null : appConfigEntries.clone();
    }

}
