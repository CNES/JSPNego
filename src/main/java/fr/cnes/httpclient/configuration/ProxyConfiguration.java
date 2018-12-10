/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.httpclient.configuration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.restlet.engine.util.StringUtils;

/**
 *
 * @author malapert
 */
public enum ProxyConfiguration {
    HTTP_PROXY("http_proxy", System.getenv("http_proxy")),
    NO_PROXY("no_proxy", System.getenv("no_proxy")),
    USERNAME("username", ""),
    PASSWORD("password", "");

    private final String key;
    private String value;

    ProxyConfiguration(final String key, final String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return this.key;
    }

    public String getValue() {
        return this.value == null ? "" : this.value;
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

    public static boolean isValid(StringBuilder error) {
        boolean isValid = true;
        final StringBuilder validation = new StringBuilder();
        if(ProxyConfiguration.HTTP_PROXY.getValue().isEmpty()) {
            validation.append(ProxyConfiguration.HTTP_PROXY.getKey()).append(" cannot be null or empty\n");
            isValid = false;
        }    
        error.append(validation);
        return isValid;
    }
}
