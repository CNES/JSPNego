/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.jspnego;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Set;
import javax.security.auth.Subject;
import javax.security.auth.kerberos.KerberosTicket;
import org.apache.commons.io.HexDump;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.Oid;

/**
 *
 * @author malapert
 */
public abstract class AbstractGSSClient {
    
    /**
     * Get actual class name to be printed on.
     */
    private static final Logger LOG = LogManager.getLogger(AbstractGSSClient.class.getName());

    /**
     * Mechanism OID assigned to the pseudo-mechanism SPNEGO to negotiate the best common GSS-API
     * mechanism between two communication peers.
     */
    private static final String SPNEGO_OID = "1.3.6.1.5.5.2";    

    private String servicePrincipalName;

    /**
     * The initiator subject. This object will hold the TGT and all service tickets in its private
     * credentials cache.
     */
    private Subject subject;
    
    /**
     * Checks whether the principal is already logged.
     *
     * @return true when the principal is already logged otherwise false
     */
    private boolean isNotLogged() {
        return (this.subject == null);
    }    

    protected abstract Subject login() throws GSSException;
    
    protected abstract void setServiceSpincipalName(final String servicePrincipalName);
    
    protected String getServicePrincipalName() {
        return this.servicePrincipalName;
    }

    public byte[] generateGSSToken() throws GSSException {
        final Oid oid = new Oid(SPNEGO_OID);

        final byte[] tokenInit = new byte[0];

        // Get the GSS-API
        final GSSManager manager = GSSManager.getInstance();

        // convert a servicePrincipalName from the specified namespace to a GSSName object
        final GSSName serverName = manager.createName(this.getServicePrincipalName(),
                GSSName.NT_HOSTBASED_SERVICE);

        // Instantiate and initialize a security context that will be established with the server
        final GSSContext gssContext = manager.createContext(serverName.canonicalize(oid), oid, null,
                GSSContext.DEFAULT_LIFETIME);

        if (this.isNotLogged()) {
            this.subject = login(); // throw GSSException if fail to login
        }

        // If we do not have the service ticket it will be retrieved from the TGS
        final AbstractGSSClient.NegotiateContextAction negotiationAction = new AbstractGSSClient.NegotiateContextAction(gssContext,
                tokenInit);

        if (negotiationAction.getGSSException() != null) {
            throw LOG.throwing(negotiationAction.getGSSException());
        }

        // The service ticket will then be cached in the Subject's private credentials 
        // as the subject.
        final byte[] token = (byte[]) Subject.doAs(subject, negotiationAction);

        return LOG.traceExit(token);
    }
    
    /**
     * Returns the user ID.
     *
     * @return the user ID
     */
    public String getName() {
        return this.subject.getPrincipals().iterator().next().getName();
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
