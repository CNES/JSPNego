/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.jspnego;

import fr.cnes.httpclient.HttpClientFactory.Type;

/**
 *
 * @author malapert
 */
public class GSSClientFactory {
    
    public static AbstractGSSClient create(Type type) {
        final AbstractGSSClient gssClient;
        switch(type) {
            case PROXY_SPNEGO_API:
                gssClient = new GSSClientAPI();
                break;
            case PROXY_SPNEGO_JAAS:
                gssClient = new GSSClientJASS();
                break;
            default:
                throw new IllegalArgumentException("Cannot support "+type.name());
        }
        return gssClient;
    }
    
}
