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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.kerberos.KerberosTicket;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.apache.commons.io.HexDump;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSException;

/**
 * Creates a Kerberos client and manages the logging into the Kerberos KDC to acquire the TGT.
 *
 * @author S. ETCHEVERRY
 */
public class GSSClient {

    /**
     * Get actual class name to be printed on
     */
    private static final Logger LOG = LogManager.getLogger(GSSClient.class.getName());

    /**
     * The initiator subject. This object will hold the TGT and all service tickets in its private
     * credentials cache.
     */
    private Subject subject;

    /**
     * UserID.
     */
    private final String clientPrincipalName;

    /**
     * the path of the keytab file
     */
    private final String clientKeytabFileName;

    /**
     * the path of the ticket cache file
     */
    private final String ticketCacheFileName;

    /**
     * Init a Generic Security Services client using a cache.
     *
     * @param clientPrincipalName userID
     * @param clientKeytabFileName the path of the keytab file
     * @param ticketCacheFileName the path of the ticket cache file
     * @param krb5ConfFile the path of the krb conf file
     */
    public GSSClient(final String clientPrincipalName, final String clientKeytabFileName,
            final String ticketCacheFileName, final File krb5ConfFile) {
        LOG.traceEntry("Parameters {} : ", clientPrincipalName, clientKeytabFileName,
                ticketCacheFileName, krb5ConfFile);
        System.setProperty("java.security.krb5.conf", krb5ConfFile.toString());
        this.clientPrincipalName = clientPrincipalName;
        this.clientKeytabFileName = clientKeytabFileName;
        this.ticketCacheFileName = ticketCacheFileName;
    }

    /**
     * Init a Generic Security Services client.
     *
     * @param clientPrincipalName userID
     * @param clientKeytabFileName the path of the keytab file
     * @param krb5ConfFile the path of the krb conf file
     */
    public GSSClient(final String clientPrincipalName, final String clientKeytabFileName,
            final File krb5ConfFile) {
        this(clientPrincipalName, clientKeytabFileName, null, krb5ConfFile);
    }

    /**
     * Returns the SPN.
     *
     * @return the SPN
     */
    public String getName() {

        return clientPrincipalName;
    }

    /**
     * Login to Kerberos KDC and acquire a TGT.
     *
     * @throws GSSException This exception is thrown whenever a GSS-API error occurs
     */
    private void loginViaJAAS() throws GSSException {
        LOG.traceEntry();

        try {
            final KerberosConfiguration config = createGssKerberosConfiguration();
            config.setKeytab(clientKeytabFileName);
            config.setTicketCache(ticketCacheFileName);
            config.initialize();

            final LoginContext loginContext = new LoginContext("other");
            loginContext.login();

            // Subject will be populated with the Kerberos Principal name and the TGT.
            // Krb5LoginModule obtains a TGT (KerberosTicket) for the user either from the
            // KDC or from an existing ticket cache, and stores this TGT in the private credentials
            // set of a Subject
            subject = loginContext.getSubject();

            LOG.debug("Logged in successfully as subject=\n{0}", subject.getPrincipals().toString());

            LOG.traceExit();

        } catch (LoginException e) {
            LOG.error("Error", e);
            throw LOG.throwing(new GSSException(GSSException.DEFECTIVE_CREDENTIAL,
                    GSSException.BAD_STATUS,
                    "Kerberos client '" + getName() + "' failed to login to KDC. Error: "
                    + e.getMessage()));
        }
    }

    /**
     * @return a KerberosConfiguration object initiated with the user Id
     */
    public KerberosConfiguration createGssKerberosConfiguration() {
        LOG.traceEntry();
        return LOG.traceExit(new KerberosConfiguration(getName()));
    }

    /**
     * Called when SPNEGO client-service authentication is taking place.
     *
     * @param context the current GSS context
     * @param negotiationToken a previous token
     * @return a kerberos token as a byte array
     * @throws GSSException This exception is thrown whenever a GSS-API error occurs
     */
    public byte[] negotiate(final GSSContext context, final byte[] negotiationToken) throws
            GSSException {
        LOG.traceEntry();
        if (subject == null) {
            loginViaJAAS(); // throw GSSException if fail to login
        }

        // If we do not have the service ticket it will be retrieved from the TGS on a
        // call to initSecContext().
        final NegotiateContextAction negotiationAction = new NegotiateContextAction(context,
                negotiationToken);

        // Run the negotiation as the initiator
        // The service ticket will then be cached in the Subject's private credentials,
        // as the subject.
        final byte[] token = (byte[]) Subject.doAs(subject, negotiationAction);

        if (negotiationAction != null && negotiationAction.getGSSException() != null) {
            throw LOG.throwing(negotiationAction.getGSSException());
        }

        return LOG.traceExit(token);
    }

    /**
     * Action to call initSecContext() for initiator side of context negotiation. Run as the
     * initiator Subject so that any service tickets are cached in the subject's private
     * credentials.
     */
    private class NegotiateContextAction implements PrivilegedAction<Object> {

        /**
         * Interface encapsulates the GSS-API security context and provides the security services
         * that are available over the context.
         */
        private final GSSContext context;
        /**
         * Token negotiation.
         */
        private byte[] negotiationToken;
        /**
         * Exception catched in the runnable job.
         */
        private GSSException exception;

        /**
         * Creates an object to negociate the token.
         *
         * @param context the current GSS context
         * @param negotiationToken a previous token
         */
        public NegotiateContextAction(final GSSContext context, final byte[] negotiationToken) {
            LOG.traceEntry("Parameters : {}", context, negotiationToken);
            this.context = context;
            this.negotiationToken = negotiationToken.clone();
        }

        /**
         * (non-Javadoc)
         *
         * @see java.security.PrivilegedAction#run()
         * @return The negotiation token
         */
        @Override
        public Object run() {
            LOG.traceEntry();

            try {
                // If we do not have the service ticket it will be retrieved from the TGS on the
                // first call to initSecContext().
                // The subject's private credentials are checked for the service ticket.
                // If we run this action as the initiator subject, the service ticket will be
                // stored in the subject's credentials and will not need
                // to be retrieved next time the client wishes to talk to the server (acceptor).

                final Subject subject = Subject.getSubject(AccessController.getContext());

                LOG.debug(subject.toString());

                negotiationToken = context.initSecContext(negotiationToken, 0,
                        negotiationToken.length);

                traceAfterNegotiate(traceBeforeNegotiate());

            } catch (GSSException e) {
                // Trace out some info
                traceServiceTickets();
                LOG.error(e.toString());
                exception = e;
            }

            return LOG.traceExit(negotiationToken);
        }

        /**
         * @return The exception
         */
        public GSSException getGSSException() {
            LOG.traceEntry();
            return LOG.traceExit(exception);
        }

        /**
         * @return int Number of credentials held by the subject
         */
        private int traceBeforeNegotiate() {
            LOG.traceEntry();
            int beforeNumSubjectCreds = 0;
            // Traces all credentials too.
            if (subject != null) {
                LOG.debug("[{}] AUTH_NEGOTIATE as subject {1}",
                        getName(), subject.getPrincipals().toString());
                beforeNumSubjectCreds = subject.getPrivateCredentials().size();
            }

            if (negotiationToken != null && negotiationToken.length > 0) {
                try {
                    final OutputStream outputStream = new ByteArrayOutputStream();
                    HexDump.dump(negotiationToken, 0, outputStream, 0);
                    LOG.debug("[{}] AUTH_NEGOTIATE Process token from service==>\n", getName());
                } catch (IOException e) {
                    LOG.catching(e);
                }
            }

            return LOG.traceExit(beforeNumSubjectCreds);
        }

        /**
         * @param beforeNumSubjectCreds Number of credentials held by the subject
         */
        private void traceAfterNegotiate(final int beforeNumSubjectCreds) {
            LOG.traceEntry("Parameter : {}", beforeNumSubjectCreds);
            if (subject != null) {
                final int afterNumSubjectCreds = subject.getPrivateCredentials().size();
                if (afterNumSubjectCreds > beforeNumSubjectCreds) {
                    LOG.debug("[{}] AUTH_NEGOTIATE have extra credentials.", getName());
                    // Traces all credentials too.
                    LOG.debug("[{}] AUTH_NEGOTIATE updated subject {1}",
                            getName(), subject.toString());
                }
            }

            traceServiceTickets();

            if (negotiationToken != null && negotiationToken.length > 0) {
                try {
                    final OutputStream outputStream = new ByteArrayOutputStream();
                    HexDump.dump(negotiationToken, 0, outputStream, 0);
                    LOG.debug("[{}] AUTH_NEGOTIATE Send token to service\n", getName());
                } catch (IOException e) {
                    LOG.catching(e);
                }
            }
            LOG.traceExit();
        }

        /**
         * Write logs for each Kerberos ticket held by the subject if there is any
         */
        public void traceServiceTickets() {
            LOG.traceEntry();
            if (subject == null) {
                LOG.debug("Subject null");
                LOG.traceExit();
                return;
            }
            final Set<Object> creds = subject.getPrivateCredentials();
            if (creds.isEmpty()) {
                LOG.debug("[{}] No service tickets ", getName());
            }

            synchronized (creds) {
                // The Subject's private credentials is a synchronizedSet
                // We must manually synchronize when iterating through the set.
                for (final Object ecred : creds) {
                    if (ecred instanceof KerberosTicket) {
                        final KerberosTicket ticket = (KerberosTicket) ecred;
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("[{}] Service ticket belonging to client principal [{1}] "
                                    + "for server principal [{2}] End time=[{3}] isCurrent={4}",
                                    getName(), ticket.getClient().getName(),
                                    ticket.getServer().getName(), ticket.getEndTime(),
                                    ticket.isCurrent());
                        }
                    }
                }
            }

            LOG.traceExit();
        }
    }
}
