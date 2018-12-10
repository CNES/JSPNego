/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.jspnego;

import fr.cnes.httpclient.configuration.ProxySPNegoJAASConfiguration;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import org.ietf.jgss.GSSException;

/**
 *
 * @author malapert
 */
public final class GSSClientJASS extends AbstractGSSClient {

    public GSSClientJASS() {
        System.setProperty("java.security.krb5.conf", ProxySPNegoJAASConfiguration.KRB5.
                getValue());
        System.setProperty("java.security.auth.login.config", ProxySPNegoJAASConfiguration.JAAS.
                getValue());
        this.setServiceSpincipalName(ProxySPNegoJAASConfiguration.SERVICE_PROVIDER_NAME.getValue());
    }

    @Override
    protected Subject login() throws GSSException {
        final LoginContext loginContext;
        try {
             loginContext = new LoginContext(ProxySPNegoJAASConfiguration.JAAS_CONTEXT.getValue());
        } catch (LoginException ex) {
            throw new GSSException(GSSException.DEFECTIVE_CREDENTIAL,
                    GSSException.BAD_STATUS,
                    "Kerberos client '" + getName() + "' failed to login to KDC. Error: "
                    + ex.getMessage()); 
        }
        return loginContext.getSubject();
    }

    @Override
    protected void setServiceSpincipalName(String servicePrincipalName) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
