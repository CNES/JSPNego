/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.restlet.ext.httpclient4;

import fr.cnes.httpclient.HttpClientFactory;
import fr.cnes.httpclient.HttpClientFactory.Type;
import fr.cnes.httpclient.ProxySPNegoHttpClientWithJAAS;
import fr.cnes.httpclient.configuration.ProxySPNegoAPIConfiguration;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import org.apache.http.client.HttpClient;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.data.Parameter;
import org.restlet.data.Protocol;
import org.restlet.engine.adapter.ClientCall;
import org.restlet.engine.util.ReferenceUtils;
import org.restlet.util.Series;

/**
 *
 * @author malapert
 */
public class HttpClientHelper extends org.restlet.engine.connector.HttpClientHelper {
    
    private volatile HttpClient httpClient;

    public HttpClientHelper(Client client) {
        super(client);
        getProtocols().add(Protocol.HTTP);
        getProtocols().add(Protocol.HTTPS);
        this.httpClient = null;
    }

    @Override
    public void setHelped(Client helpedRestlet) {
        super.setHelped(helpedRestlet); //To change body of generated methods, choose Tools | Templates.
        Context.setCurrent(helpedRestlet.getContext());
    }
    
    

    @Override
    public Series<Parameter> getHelpedParameters() {
        Series<Parameter> params = super.getHelpedParameters();
        final String userID = params.getFirstValue("userID");
        final String keytabFilePath = params.getFirstValue("keytabFilePath");
        final String proxyHost = params.getFirstValue("proxyHost");
        final String proxyPort = params.getFirstValue("proxyPort");
        ProxySPNegoAPIConfiguration.PRINCIPAL.setValue(userID);
        ProxySPNegoAPIConfiguration.USE_KEYTAB.setValue("true");
        ProxySPNegoAPIConfiguration.KEY_TAB.setValue(keytabFilePath);
        ProxySPNegoAPIConfiguration.HTTP_PROXY.setValue(proxyHost+":"+proxyPort);
        ProxySPNegoAPIConfiguration.SERVICE_PROVIDER_NAME.setValue("HTTP@"+proxyHost);
        this.httpClient = new ProxySPNegoHttpClientWithJAAS(false);
        return params;
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
    public ProxySPNegoHttpClientWithJAAS getHttpClient() {
        return (ProxySPNegoHttpClientWithJAAS) this.httpClient;
    }   

    @Override
    public synchronized void start() throws Exception {
        getHelpedParameters();
        super.start();        
    }

    @Override
    public synchronized void stop() throws Exception {
        super.stop();
        if(this.httpClient != null) {
            this.getHttpClient().close();
            this.httpClient = null;
        }
    }              
    
}
