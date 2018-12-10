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
import fr.cnes.httpclient.configuration.ProxySPNegoAPIConfiguration;
import java.security.Security;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class removes the need for a jaas.conf file to configure the
 * com.sun.security.auth.module.Krb5LoginModule to be used for JAAS login for Kerberos client
 * (initiators).
 *
 * @author S. ETCHEVERRY
 */
public class KerberosConfiguration extends Configuration {

    /**
     * Get actual class name to be printed on.
     */
    private static final Logger LOG = LogManager.getLogger(KerberosConfiguration.class.getName());


    /**
     * Login module.
     */
    private static final String LOGIN_MODULE = com.sun.security.auth.module.Krb5LoginModule.class.
            getName();
   

    /**
     * Debug.
     */
    private static final String DEBUG = "debug";


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
     */
    public KerberosConfiguration() {
        super();
    }

    /**
     * Initialize the kerberos configuration. These parameters null     {@value KerberosConfiguration#DEBUG}, {@value KerberosConfiguration#PRINCIPAL}, 
     * {@value KerberosConfiguration#REFRESH_KRB5_CONFIG} are initialized to true.
     */
    public void initialize() {
        if (LOG.isDebugEnabled()) {
            options.put(DEBUG, "true");
        }
        final Map<String, String> config = ProxySPNegoAPIConfiguration.getConfig();
        final Set<Entry<String, String>> entries = config.entrySet();
        for (Entry<String, String> entry : entries) {
            if (entry.getKey().equals(ProxySPNegoAPIConfiguration.HTTP_PROXY.getKey())
                    || entry.getKey().equals(ProxySPNegoAPIConfiguration.NO_PROXY.getKey())) {
                // skip the configuration for those elements
            } else if (entry.getValue().isEmpty()) {
                // skip the configuration because there is no elements
            } else {
                this.options.put(entry.getKey(), entry.getValue());
            }
        }
        
        appConfigEntries = new AppConfigurationEntry[1];
        appConfigEntries[0] = new AppConfigurationEntry(LOGIN_MODULE,
                AppConfigurationEntry.LoginModuleControlFlag.REQUIRED, this.options);

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
