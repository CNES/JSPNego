/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.httpclient;

import fr.cnes.httpclient.configuration.ProxyConfiguration;
import fr.cnes.httpclient.configuration.ProxySPNegoAPIConfiguration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author malapert
 */
public class HttpClientFactory {
    
    public enum Type {
        PROXY_SPNEGO_JAAS,
        PROXY_SPNEGO_API,        
        PROXY_BASIC,
        NO_PROXY;
    }    
    
    public static HttpClient create(final Type type) {
        final HttpClient httpclient;
        switch (type) {
            case PROXY_SPNEGO_JAAS:
            case PROXY_SPNEGO_API:
                httpclient = new ProxySPNegoHttpClient(type, false);
                break;
            case PROXY_BASIC:
                httpclient = new ProxyHttpClient(false);
                break;
            case NO_PROXY:
                httpclient = new HttpClient();
                break;
            default:
                throw new IllegalArgumentException("Unknown httpclient type");
        }
        return httpclient;
    }     
    
}
