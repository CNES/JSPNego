/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.restlet.ext.httpclient4;

import fr.cnes.jspnego.ProxySPNegoHttpClient;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import org.apache.http.client.HttpClient;
import org.restlet.Client;
import org.restlet.Request;
import org.restlet.data.Protocol;
import org.restlet.engine.adapter.ClientCall;
import org.restlet.engine.util.ReferenceUtils;

/**
 *
 * @author malapert
 */
public class HttpClientHelper extends org.restlet.engine.connector.HttpClientHelper {
    
    private volatile HttpClient httpClient;
    private volatile String userID;
    private volatile File keytabFileName;
    private volatile String proxyHost;
    private volatile int proxyPort;

    public HttpClientHelper(Client client) {
        super(client);
        getProtocols().add(Protocol.HTTP);
        getProtocols().add(Protocol.HTTPS);
        this.httpClient = null;
    }
    
    public void setKerberosProxy(final String userId, final File keytabFileName, final String proxyHost, final int proxyPort) {
        this.userID = userId;
        this.keytabFileName = keytabFileName;
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
    }
    

    @Override
    public ClientCall create(Request request) {
        ClientCall result = null;

        try {
            result = new HttpMethodCall(this, request.getMethod().toString(),
                    ReferenceUtils.update(request.getResourceRef(), request)
                            .toString(), request.isEntityAvailable());
        } catch (IOException ioe) {
            getLogger().log(Level.WARNING,
                    "Unable to create the HTTP client call", ioe);
        }

        return result;
    }
    
    /**
     * Returns the wrapped Apache HTTP Client.
     * 
     * @return The wrapped Apache HTTP Client.
     */
    public ProxySPNegoHttpClient getHttpClient() {
        return (ProxySPNegoHttpClient) this.httpClient;
    }   

    @Override
    public synchronized void start() throws Exception {
        super.start();
        System.out.println("-------- start -------");
        this.httpClient = new ProxySPNegoHttpClient(userID, keytabFileName, proxyHost, proxyPort);
        
    }

    @Override
    public synchronized void stop() throws Exception {
        super.stop();
        System.out.println("-------- stop -------");
        if(this.httpClient != null) {
            this.getHttpClient().close();
            this.httpClient = null;
        }
    }              
    
}
