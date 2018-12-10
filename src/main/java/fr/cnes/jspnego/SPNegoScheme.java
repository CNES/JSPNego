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
import fr.cnes.httpclient.HttpClientFactory.Type;
import java.nio.charset.Charset;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.auth.AUTH;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.ContextAwareAuthScheme;
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
import org.ietf.jgss.GSSException;

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
     * Get actual class name to be printed on.
     */
    private static final Logger LOG = LogManager.getLogger(SPNegoScheme.class.getName());

    /**
     * Kerberos client.
     */
    private final GSSClient gssClient;

    /**
     * Provides Base64 encoding and decoding as defined by RFC 2045.
     */
    private final Base64 base64codec = new Base64();

    /**
     * Authentication process state.
     */
    private State state;
    /**
     * base64 decoded challenge.
     */
    private byte[] token;
    
    public SPNegoScheme(final Type type) {        
        switch(type) {
            case PROXY_SPNEGO_API:                
                break;
            case PROXY_SPNEGO_JAAS:
                break;
            default:
                throw new IllegalArgumentException("Cannot support "+type);
        }
        this.state = State.UNINITIATED;
        this.gssClient = new GSSClient(type);        
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
     * Produces SPNEGO authorization Proxy Header based on token created by processChallenge.
     *
     * @param credentials (not used)
     * @param request the request being authenticated (not used)
     * @throws AuthenticationException if authentication string cannot be generated due to an
     * authentication failure
     * @return SPNEGO authentication Header
     * @deprecated (4.2) Use {@link ContextAwareAuthScheme#authenticate(org.apache.http.auth.Credentials, org.apache.http.HttpRequest, org.apache.http.protocol.HttpContext)
     * }
     */
    @Deprecated
    @Override
    public Header authenticate(final Credentials credentials, final HttpRequest request)
            throws AuthenticationException {
        LOG.traceEntry("credentials: {}\n"
                + "request: {}",
                credentials, request);
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
        LOG.traceEntry("credentials: {}\n"
                + "request: {}\n"
                + "context: {}",
                credentials, request, context);
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
                    token = gssClient.generateGSSToken();
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
     * Returns the header that includes the token.
     *
     * @return the basicHeader
     */
    private BasicHeader getBasicHeader() {
        LOG.traceEntry();
        final String tokenStr = new String(base64codec.encode(token), Charset.defaultCharset());
        LOG.debug("Sending response '{}' back to the auth server", tokenStr);
        return LOG.traceExit(new BasicHeader(AUTH.PROXY_AUTH_RESP, "Negotiate " + tokenStr));
    }

    /**
     * (non-Javadoc)
     *
     * @param buffer buffer
     * @param beginIndex begin index
     * @param endIndex end index
     * @throws org.apache.http.auth.MalformedChallengeException Signals that authentication 
     * challenge is in some way invalid or illegal in the given context
     * @see org.apache.http.impl.auth.AuthSchemeBase#parseChallenge
     */
    @Override
    protected void parseChallenge(final CharArrayBuffer buffer, final int beginIndex,
            final int endIndex) throws MalformedChallengeException {
        LOG.traceEntry("buffer: {}\n"
                + "beginIndex: {}\n"
                + "endIndex: {}",
                buffer, beginIndex, endIndex);
        final String challenge = buffer.substringTrimmed(beginIndex, endIndex);
        LOG.debug("Received challenge {} from the auth server", challenge);
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
        return gssClient.generateGSSToken();
    }

    /**
     * There are no valid parameters for SPNEGO authentication so this method always
     *
     * @param name Name of the parameter
     * @return {@code null}
     */
    @Override
    public String getParameter(final String name) {
        LOG.traceEntry("name: {}", name);
        Args.notNull(name, "Parameter name");
        LOG.traceExit("null");
        return null;
    }

    /**
     * Returns the scheme name.
     *
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
