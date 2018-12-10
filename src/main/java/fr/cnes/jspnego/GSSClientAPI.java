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
package fr.cnes.jspnego;

import fr.cnes.httpclient.configuration.ProxySPNegoAPIConfiguration;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ietf.jgss.GSSException;

/**
 * GSS (Generic Security Service) client with the programmatic API as configuration.
 * The class {@link fr.cnes.httpclient.configuration.ProxySPNegoAPIConfiguration} is used to 
 * configure this client.
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 * @author S. ETCHEVERRY
 */
public final class GSSClientAPI extends AbstractGSSClient {

    /**
     * Get actual class name to be printed on.
     */
    private static final Logger LOG = LogManager.getLogger(GSSClientAPI.class.getName());
    
    /**
     * Creates the GSS client.
     * This constructor sets the following variables using {@link fr.cnes.httpclient.configuration.ProxySPNegoJAASConfiguration}.
     * <ul>
     * <li>the JAVA environment variable {@value #JAVA_SECURITY_KRB5_ENV}</li>
     * <li>the Service Principal Name {@link #setServiceSpincipalName(java.lang.String)}
     * </ul>   
     */    
    public GSSClientAPI() {
        System.setProperty(JAVA_SECURITY_KRB5_ENV, ProxySPNegoAPIConfiguration.KRB5.
                getValue());
        this.setServiceSpincipalName(ProxySPNegoAPIConfiguration.SERVICE_PROVIDER_NAME.getValue());

    }

    /**
     * {@inheritDoc}
     */    
    @Override
    protected Subject login() throws GSSException {
        final LoginContext loginContext;
        try {
            final KerberosConfiguration config = new KerberosConfiguration();
            config.initialize();
            loginContext = new LoginContext(ProxySPNegoAPIConfiguration.JAAS_CONTEXT.getValue());
        } catch (LoginException ex) {
            throw new GSSException(GSSException.DEFECTIVE_CREDENTIAL,
                    GSSException.BAD_STATUS,
                    "Kerberos client '" + getName() + "' failed to login to KDC. Error: "
                    + ex.getMessage());            
        }
        return loginContext.getSubject();
    }

}
