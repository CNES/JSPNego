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
import java.nio.charset.Charset;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.auth.AUTH;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.InvalidCredentialsException;
import org.apache.http.auth.MalformedChallengeException;
import org.apache.http.impl.auth.AuthSchemeBase;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.Args;
import org.apache.http.util.CharArrayBuffer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.Oid;

/**
 * SPNEGO (Simple and Protected GSSAPI Negotiation Mechanism) authentication scheme adapted for
 * Proxy Authentication.
 *
 * @author S. ETCHEVERRY
 */
public class SPNegoScheme extends AuthSchemeBase {

    /**
     * Authentication state.
     */
    private enum State {
        /**
         * Not initiated.
         */
        UNINITIATED,
        /**
         * Authentication received.
         */
        CHALLENGE_RECEIVED,
        /**
         * Token generated.
         */
        TOKEN_GENERATED,
        /**
         * Togken generation failed.
         */
        FAILED,
    }

    /** 
     * Get actual class name to be printed on 
     */
    private static final Logger LOG = LogManager.getLogger(SPNegoScheme.class.getName());

    /**
     * Mechanism OID assigned to the pseudo-mechanism SPNEGO to negotiate the best common GSS-API
     * mechanism between two communication peers.
     */
    private static final String SPNEGO_OID = "1.3.6.1.5.5.2";

    /**
     *
     */
    private final GSSClient gssClient;

    /**
     * the SPN (HTTP@proxy host)
     */
    private final String servicePrincipalName;

    /**
     * the oid representing the type of service name form
     */
    private final Oid servicePrincipalOid;

    /**
     * Provides Base64 encoding and decoding as defined by RFC 2045.
     */
    private final Base64 base64codec;

    /**
     * Authentication process state
     */
    private State state;
    /**
     * base64 decoded challenge *
     */
    private byte[] token;

    /**
     * Init a Proxy SPNego Scheme
     *
     * @param gssClient the gss client with all the client configuration
     * @param servicePrincipalName the SPN (proxy host)
     * @param servicePrincipalOid the oid representing the type of service name form
     */
    public SPNegoScheme(final GSSClient gssClient, final String servicePrincipalName,
            final Oid servicePrincipalOid) {
        super();
        LOG.traceEntry("Init constructor with parameters : {}",
                gssClient.getName(), servicePrincipalName, servicePrincipalOid);
        this.base64codec = new Base64();
        // sets the state to no initiated at the beginning.
        this.state = State.UNINITIATED;
        this.gssClient = gssClient;
        this.servicePrincipalName = servicePrincipalName;
        this.servicePrincipalOid = servicePrincipalOid;
    }

    /**
     * Returns the Generic Security Service manager.
     *
     * @return a GSSManager's instance
     */
    protected GSSManager getManager() {
        LOG.traceEntry();
        return LOG.traceExit(GSSManager.getInstance());
    }

    /**
     * Generate a Generic Security Service (GSS) Token.
     *
     * @param oid The universal object identifier
     * @return a Kerberos token as a byte array
     * @throws GSSException This exception is thrown whenever a GSS-API error occurs
     */
    protected byte[] generateGSSToken(final Oid oid) throws GSSException {
        LOG.traceEntry("Parameters : {}", oid);

        LOG.debug("Init token for {0}", servicePrincipalName);

        final byte[] tokenInit = new byte[0];
        final GSSManager manager = getManager();
        final GSSName serverName = manager.createName(servicePrincipalName, servicePrincipalOid);
        final GSSContext gssContext = manager.createContext(serverName.canonicalize(oid), oid, null,
                GSSContext.DEFAULT_LIFETIME);

        // Get client to login if not already done
        return LOG.traceExit(gssClient.negotiate(gssContext, tokenInit));
    }

    /**
     * (non-Javadoc)
     *
     * @return true if authentication process state is failed or token generated
     * @see org.apache.http.auth.AuthScheme#isComplete()
     */
    @Override
    public boolean isComplete() {
        LOG.traceEntry();
        return LOG.traceExit(this.state == State.TOKEN_GENERATED || this.state == State.FAILED);
    }

    /**
     * @param credentials (not used)
     * @param request the request being authenticated (not used)
     * @throws AuthenticationException if authentication string cannot be generated due to an
     * authentication failure
     * @return SPNEGO authentication Header
     * @deprecated (4.2) Use {@link ContextAwareAuthScheme#authenticate}
     */
    @Deprecated
    @Override
    public Header authenticate(final Credentials credentials, final HttpRequest request)
            throws AuthenticationException {
        LOG.traceEntry("Parameters {} : ", credentials, request);
        return LOG.traceExit(authenticate(credentials, request, null));
    }

    /**
     * Produces SPNEGO authorization Proxy Header based on token created by processChallenge.
     *
     * @param credentials (not used by the Proxy SPNEGO scheme).
     * @param request The request being authenticated (not used by the Proxy SPNEGO scheme)
     * @param context The context used for authentication (not used by the Proxy SPNEGO scheme)
     * @throws AuthenticationException if authentication string cannot be generated due to an
     * authentication failure
     * @throws IllegalArgumentException HTTP request may not be null
     * @throws InvalidCredentialsException When token cannot be generated
     * @return SPNEGO authentication Header
     */
    @Override
    public Header authenticate(final Credentials credentials, final HttpRequest request,
            final HttpContext context) throws AuthenticationException {
        LOG.traceEntry("Parameters {} : ", credentials, request, context);
        if (request == null) {
            throw LOG.throwing(new IllegalArgumentException("HTTP request may not be null"));
        }
        switch (state) {
            case UNINITIATED:
                throw LOG.throwing(new AuthenticationException(
                        getSchemeName() + " authentication has not been initiated"));
            case FAILED:
                throw LOG.throwing(new AuthenticationException(
                        getSchemeName() + " authentication has failed"));
            case CHALLENGE_RECEIVED:
                try {
                    token = generateToken();
                    state = State.TOKEN_GENERATED;
                } catch (GSSException gsse) {
                    state = State.FAILED;
                    if (gsse.getMajor() == GSSException.DEFECTIVE_CREDENTIAL
                            || gsse.getMajor() == GSSException.CREDENTIALS_EXPIRED
                            || gsse.getMajor() == GSSException.NO_CRED) {
                        throw LOG.throwing(new InvalidCredentialsException(gsse.getMessage(), gsse));
                    }
                    // if (gsse.getMajor() == GSSException.DEFECTIVE_TOKEN
                    // || gsse.getMajor() == GSSException.DUPLICATE_TOKEN
                    // || gsse.getMajor() == GSSException.OLD_TOKEN) {
                    // throw new AuthenticationException(gsse.getMessage(),
                    // gsse);
                    // }
                    // other error
                    throw LOG.throwing(new AuthenticationException(gsse.getMessage(), gsse));
                }
                break;
            case TOKEN_GENERATED:
                break;

            default:
                throw LOG.throwing(new IllegalStateException("Illegal state: " + state));
        }

        return LOG.traceExit(getBasicHeader());
    }

    /**
     * @return the basicHeader
     */
    private BasicHeader getBasicHeader() {
        LOG.traceEntry();
        final String tokenStr = new String(base64codec.encode(token), Charset.defaultCharset());
        LOG.debug("Sending response '{0}' back to the auth server", tokenStr);
        return LOG.traceExit(new BasicHeader(AUTH.PROXY_AUTH_RESP, "Negotiate " + tokenStr));
    }

    /**
     * (non-Javadoc)
     * 
     * @param buffer
     * @param beginIndex
     * @param endIndex
     * @throws org.apache.http.auth.MalformedChallengeException
     * @see org.apache.http.impl.auth.AuthSchemeBase#parseChallenge
     */
    @Override
    protected void parseChallenge(final CharArrayBuffer buffer, final int beginIndex,
            final int endIndex) throws MalformedChallengeException {
        LOG.traceEntry("Parameters {} : ", buffer, beginIndex, endIndex);
        final String challenge = buffer.substringTrimmed(beginIndex, endIndex);
        LOG.debug("Received challenge {0} from the auth server", challenge);
        if (state == State.UNINITIATED) {
            LOG.debug("Authentication received");
            state = State.CHALLENGE_RECEIVED;
        } else {
            LOG.debug("Authentication already attempted");
            state = State.FAILED;
        }
    }

    /**
     * Token generation
     *
     * @return the token as a byte array
     * @throws GSSException This exception is thrown whenever a GSS-API error occurs
     */
    protected byte[] generateToken() throws GSSException {
        LOG.traceEntry();
        return LOG.traceExit(generateGSSToken(new Oid(SPNEGO_OID)));
    }

    /**
     * There are no valid parameters for SPNEGO authentication so this method always
     *
     * @param name Name of the parameter
     * @return {@code null}
     */
    @Override
    public String getParameter(final String name) {
        LOG.traceEntry("Parameter {} : ", name);
        Args.notNull(name, "Parameter name");
        LOG.traceExit("null");
        return null;
    }

    /**
     * Returns the scheme name.
     * @return Negotiate
     */
    @Override
    public String getSchemeName() {
        LOG.traceEntry();
        return LOG.traceExit("Negotiate");
    }

    /**
     * Returns {@code true}. SPNEGO authentication scheme is connection based.
     *
     * @return {@code true}.
     */
    @Override
    public boolean isConnectionBased() {
        LOG.traceEntry();
        return LOG.traceExit(true);
    }

    /**
     * The concept of an authentication realm is not supported by the Negotiate authentication
     * scheme. Always returns {@code null}.
     *
     * @return {@code null}
     */
    @Override
    public String getRealm() {
        LOG.traceEntry();
        LOG.traceEntry("null");
        return null;
    }
}
