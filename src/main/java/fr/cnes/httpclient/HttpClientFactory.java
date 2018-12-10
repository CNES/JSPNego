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
        PROXY_SPNEGO_JAAS(new ConcurrentHashMap<String, String>(){
            {
                putAll(ProxySPNegoAPIConfiguration.getConfig());
            }
        }),
        PROXY_SPNEGO_API(new ConcurrentHashMap<String, String>(){
            {
                putAll(ProxySPNegoAPIConfiguration.getConfig());
            }
        }),        
        PROXY_BASIC(new ConcurrentHashMap<String, String>(){
            {
                putAll(ProxyConfiguration.getConfig());
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
