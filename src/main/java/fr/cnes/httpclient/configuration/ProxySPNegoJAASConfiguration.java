/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.httpclient.configuration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author malapert
 */
public enum ProxySPNegoJAASConfiguration {
    HTTP_PROXY("http_proxy", System.getenv("http_proxy")),
    NO_PROXY("no_proxy", System.getenv("no_proxy")),
    JAAS("jassFile", ""),
    JAAS_CONTEXT("jassContext", "client"),       
    SERVICE_PROVIDER_NAME("spn", ""),
    KRB5("krb5File", "/etc/krb5.conf");

    private final String key;
    private String value;

    ProxySPNegoJAASConfiguration(final String key, final String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return this.key;
    }

    public String getValue() {
        return this.value;
    }
    
    public void setValue(final String value) {
        this.value = value;
    }    

    public static Map<String, String> getConfig() {
        final Map<String, String> map = new ConcurrentHashMap<>();
        final ProxySPNegoAPIConfiguration[] confs = ProxySPNegoAPIConfiguration.values();
        for (ProxySPNegoAPIConfiguration conf : confs) {
            map.put(conf.getKey(), conf.getValue());
        }
        return map;
    }

}

