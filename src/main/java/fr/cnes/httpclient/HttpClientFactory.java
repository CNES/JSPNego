/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.httpclient;

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
        return HttpClientFactory.create(type, false);
    } 

    public static HttpClient create(final Type type, final boolean isDisabledSSL) {
        final HttpClient httpclient;
        switch (type) {
            case PROXY_SPNEGO_JAAS:
                httpclient = new ProxySPNegoHttpClientWithJAAS(isDisabledSSL);
                break;
            case PROXY_SPNEGO_API:
                httpclient = new ProxySPNegoHttpClientWithAPI(isDisabledSSL);
                break;
            case PROXY_BASIC:
                httpclient = new ProxyHttpClientWithBasicAuth(isDisabledSSL);
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
