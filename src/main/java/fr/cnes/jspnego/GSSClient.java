package fr.cnes.jspnego;

/*
 * Copyright 2017 Axway Software
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
 * Creates a Kerberos client and manages the logging into the Kerberos The <i>key distribution
 * center</i> (<b>KDC</b>) to acquire the <i>ticket granting ticket</i> (<b>TGT</b>).
 *
 * The <i>key distribution center</i> <b>KDC</b> is the component that houses both the
 * <i>ticket granting service</i> (<b>TGS</b>) and <i>authentication service</i> (<b>AS</b>). The
 * <b>KDC</b>, along with the client, or principal, and server, or secured service, are the three
 * pieces required to perform Kerberos authentication.
 *
 * The <i>ticket granting service</i> <b>TGS</b> is responsible for issuing <i>service tickets</i>
 * (<b>ST</b>) and specific session information to principals and the target server they are
 * attempting to access. This is based on the <b>TGT</b> and destination information provided by the
 * principal. This <b>ST</b> and session information is then used to establish a connection to the
 * destination and access the desired secured service or resource.
 *
 * The <i>authentication service</i> <b>AS</b> challenges principals to log in when they first log
 * into the network. The <b>AS</b> is responsible for issuing a <b>TGT</b>, which is needed for
 * authenticating against the <b>TGS</b> and subsequent access to secured services and resources.
 *
 * A <i>ticket granting ticket</i> <b>TGT</b> is a type of ticket (or token) issued to a principal
 * by the <b>AS</b>. The <b>TGT</b> is granted once a principal successfully authenticates against
 * the <b>AS</b> using their username and password. The <b>TGT</b> is cached locally by the client,
 * but is encrypted such that only the <b>KDC</b> can read it and is unreadable by the client. This
 * allows the <b>AS</b> to securely store authorization data and other information in the
 * <b>TGT</b> for use by the <b>TGS</b> and enables the <b>TGS</b> to make authorization decisions
 * using this data.
 *
 * A <i>service ticket</i> (<b>ST</b>) is a type of ticket issued to a principal by the TGS based on
 * their TGT and the intended destination. The principal provides the TGS with their TGT and the
 * intended destination, and the TGS verifies the principal has access to the destination based on
 * the authorization data in the TGT. If successful, the TGS issues an ST to the client for both the
 * client as well as the destination server which is the server containing the secured service or
 * resource. This grants the client access to the destination server. The ST, which is cached by the
 * client and readable by both the client and server, also contains session information that allows
 * the client and server to communicate securely.
 *
 * @author S. ETCHEVERRY
 * @author Jean-Christophe Malapert
 * @see
 * <a href="https://access.redhat.com/documentation/en-us/red_hat_jboss_enterprise_application_platform/7.1/html-single/how_to_set_up_sso_with_kerberos/">Redhat
 * documentation</a>
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
     * Username for which the kerberos token should be generated.
     */
    private final String clientPrincipalName;

    /**
     * The location of the keytab.
     *
     * The keytab file enables a trust link between the CAS server and the Key Distribution Center
     * (KDC).
     */
    private final File clientKeytabFileName;

    /**
     * The path of the ticket cache file
     */
    private final File ticketCacheFileName;

    /**
     * Init a Generic Security Services client using a cache.
     *
     * @param clientPrincipalName username for which the kerberos token should be generated
     * @param clientKeytabFileName the path of the keytab file
     * @param ticketCacheFileName the path of the ticket cache file
     * @param krb5ConfFile the path of the krb conf file
     */
    public GSSClient(final String clientPrincipalName, final File clientKeytabFileName,
            final File ticketCacheFileName, final File krb5ConfFile) {
        LOG.traceEntry("clientPrincipalName: {}\n"
                + "Key tab: {}\n "
                + "ticketCache: {}\n"
                + "krb5: {}",
                clientPrincipalName, clientKeytabFileName, ticketCacheFileName, krb5ConfFile);
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
    public GSSClient(final String clientPrincipalName, final File clientKeytabFileName,
            final File krb5ConfFile) {
        this(clientPrincipalName, clientKeytabFileName, null, krb5ConfFile);
    }

    /**
     * Returns the user ID.
     *
     * @return the user ID
     */
    public String getName() {
        return this.clientPrincipalName;
    }

    /**
     * Login to Kerberos KDC and acquire a TGT.
     *
     * When a principal first accesses the network or attempts to access a secured service without a
     * ticket granting ticket (TGT), they are challenged to authenticate against the authentication
     * service (AS) with their credentials. The AS validates the user’s provided credentials against
     * the configured identity store, and upon successful authentication, the principal is issued a
     * TGT which is cached by the client. The TGT also contains some session information so future
     * communication between the client and KDC is secured.
     *
     * @throws GSSException This exception is thrown whenever a GSS-API error occurs
     */
    private void loginViaJAAS() throws GSSException {
        LOG.traceEntry();

        try {
            final KerberosConfiguration config = createGssKerberosConfiguration();
            if(this.clientKeytabFileName != null) {
                config.setKeytab(this.clientKeytabFileName.toString());
            }
            if(this.ticketCacheFileName != null) {
                config.setTicketCache(this.ticketCacheFileName.toString());
            }
            config.initialize();

            final LoginContext loginContext = new LoginContext("other");
            loginContext.login();

            // Subject will be populated with the Kerberos Principal name and the TGT.
            // Krb5LoginModule obtains a TGT (KerberosTicket) for the user either from the
            // KDC or from an existing ticket cache, and stores this TGT in the private credentials
            // set of a Subject
            this.subject = loginContext.getSubject();

            LOG.debug("Logged in successfully as subject=\n{}",
                    this.subject.getPrincipals().toString());

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
     * Checks whether the principal is already logged.
     *
     * @return true when the principal is already logged otherwise false
     */
    private boolean isNotLogged() {
        return (this.subject == null);
    }

    /**
     * Creates the Kerberos configuration.
     *
     * @return a KerberosConfiguration object initiated with the user Id
     */
    public KerberosConfiguration createGssKerberosConfiguration() {
        LOG.traceEntry();
        return LOG.traceExit(new KerberosConfiguration(getName()));
    }

    /**
     * Called when SPNEGO client-service authentication is taking place.
     *
     * To provide authentication, Kerberos relies on a third party, the KDC, to provide
     * authentication decision for clients accessing servers. These decision happen in two steps:
     * <ul>
     * <li>Authentication exchange</li>
     * <li>Ticket granting, or authorization, exchange.</li>
     * </ul>
     * The service ticket will then be cached in the Subject's private credentials as the subject.
     *
     * @param context the current GSS context
     * @param negotiationToken a previous token
     * @return a kerberos token as a byte array
     * @throws GSSException This exception is thrown whenever a GSS-API error occurs
     */
    public byte[] negotiate(final GSSContext context, final byte[] negotiationToken) throws
            GSSException {
        LOG.traceEntry("context: {}\n"
                + "negotiationToken: {}",
                context, negotiationToken);

        if (this.isNotLogged()) {
            loginViaJAAS(); // throw GSSException if fail to login
        }

        // If we do not have the service ticket it will be retrieved from the TGS
        final NegotiateContextAction negotiationAction = new NegotiateContextAction(context,
                negotiationToken);

        if (negotiationAction.getGSSException() != null) {
            throw LOG.throwing(negotiationAction.getGSSException());
        }

        // The service ticket will then be cached in the Subject's private credentials 
        // as the subject.
        final byte[] token = (byte[]) Subject.doAs(subject, negotiationAction);

        return LOG.traceExit(token);
    }

    /**
     * Negotiate the token.
     *
     * The context establishment occurs in a loop until the context is established. While in this
     * loop tokens are produced that the application sends over to the peer. The peer passes any
     * such token as input to be cached in the subject's private credentials.
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
         * Exception catch in the runnable job.
         */
        private GSSException exception;

        /**
         * Creates an object to negotiate the token.
         *
         * @param context the current GSS context
         * @param negotiationToken a previous token
         */
        public NegotiateContextAction(final GSSContext context, final byte[] negotiationToken) {
            LOG.traceEntry("context: {}\n"
                    + "negotiationToken: {}",
                    context, negotiationToken);
            this.context = context;
            this.negotiationToken = negotiationToken.clone();
        }

        /**
         * Returns the ST or token.
         *
         * Once the principal has been issued a TGT, they may attempt to access secured services or
         * resources. The principal sends a request to the ticket granting service (TGS), passing
         * the TGT it was issued by the KDC and requesting a service ticket (ST) for a specific
         * destination. The TGS checks the TGT provided by the principal and verifies they have
         * proper permissions to access the requested resource. If successful, the TGS issues an ST
         * for the principal to access that specific destination.
         *
         * @return the ST or token
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

                if (LOG.isDebugEnabled()) {
                    final Subject subject = Subject.getSubject(AccessController.getContext());
                    LOG.debug(subject.toString());
                }

                this.negotiationToken = this.context.initSecContext(this.negotiationToken, 0,
                        this.negotiationToken.length);

                if (LOG.isDebugEnabled()) {
                    this.traceAfterNegotiate(this.traceBeforeNegotiate());
                }

            } catch (GSSException e) {
                // Trace out some info
                if (LOG.isDebugEnabled()) {
                    traceServiceTickets();
                }
                LOG.error(e.toString());
                exception = e;
            }

            return LOG.traceExit(this.negotiationToken);
        }

        /**
         * Returns the exception from the runnable method.
         *
         * @return The exception
         */
        public GSSException getGSSException() {
            LOG.traceEntry();
            return LOG.traceExit(exception);
        }

        /**
         * Trace before negotiate for debug purpose.
         *
         * @return Number of credentials held by the subject
         */
        private int traceBeforeNegotiate() {
            LOG.traceEntry();
            int beforeNumSubjectCreds = 0;
            // Traces all credentials too.
            if (subject != null) {
                LOG.debug("[{}] AUTH_NEGOTIATE as subject {}",
                        getName(), subject.getPrincipals().toString());
                beforeNumSubjectCreds = subject.getPrivateCredentials().size();
            }

            if (this.negotiationToken != null && this.negotiationToken.length > 0) {
                try {
                    final OutputStream outputStream = new ByteArrayOutputStream();
                    HexDump.dump(this.negotiationToken, 0, outputStream, 0);
                    LOG.debug("[{}] AUTH_NEGOTIATE Process token from service==>\n", getName());
                } catch (IOException e) {
                    LOG.catching(e);
                }
            }

            return LOG.traceExit(beforeNumSubjectCreds);
        }

        /**
         * Trace after negotiate for debug purpose.
         *
         * @param beforeNumSubjectCreds Number of credentials held by the subject
         */
        private void traceAfterNegotiate(final int beforeNumSubjectCreds) {
            LOG.traceEntry("beforeNumSubjectCreds : {}", beforeNumSubjectCreds);
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

            if (this.negotiationToken != null && this.negotiationToken.length > 0) {
                try {
                    final OutputStream outputStream = new ByteArrayOutputStream();
                    HexDump.dump(this.negotiationToken, 0, outputStream, 0);
                    LOG.debug("[{}] AUTH_NEGOTIATE Send token to service\n", getName());
                } catch (IOException e) {
                    LOG.catching(e);
                }
            }
            LOG.traceExit();
        }

        /**
         * Write logs for each Kerberos ticket held by the subject if there is any for debug
         * purpose.
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
                        LOG.debug("[{}] Service ticket belonging to client principal [{1}] "
                                + "for server principal [{2}] End time=[{3}] isCurrent={4}",
                                getName(), ticket.getClient().getName(),
                                ticket.getServer().getName(), ticket.getEndTime(),
                                ticket.isCurrent());

                    }
                }
            }

            LOG.traceExit();
        }
    }
}
