/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.httpclient;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author malapert
 */
public class HttpClientFactory {
    
    public enum Type {
        SPNEGO_PROXY(new ConcurrentHashMap<String, String>(){
            {
                putAll(ProxySPNegoHttpClient.DefaultConfiguration.getConfig());
            }
        }),
        PROXY(new ConcurrentHashMap<String, String>(){
            {
                putAll(ProxyHttpClient.DefaultConfiguration.getConfig());
            }
        }),
        NO_PROXY(new ConcurrentHashMap<String, String>());
        
        private final Map<String, String> options;
        
        Type(final Map<String, String> options) {
            this.options = options;
        }
        
        public Map<String, String> getOptions() {
            return this.options;
        }
    }    
    
    public static HttpClient create(final Type type) {
        final HttpClient httpclient;
        switch (type) {
            case SPNEGO_PROXY:
                httpclient = new ProxySPNegoHttpClient(type.getOptions());
                break;
            case PROXY:
                httpclient = new ProxyHttpClient(type.getOptions());
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
