/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.httpclient.configuration;

import java.nio.file.Files;
import java.nio.file.Paths;
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
        if(ProxySPNegoJAASConfiguration.HTTP_PROXY.getValue().isEmpty()) {
            validation.append(ProxySPNegoJAASConfiguration.HTTP_PROXY.getKey()).append(" cannot be null or empty\n");
            isValid = false;
        } 
        if(!Files.isReadable(Paths.get(ProxySPNegoJAASConfiguration.KRB5.getValue()))) {
            validation.append("Kerberos configuration file must be readable");
            isValid = false;
        }
        if(ProxySPNegoJAASConfiguration.SERVICE_PROVIDER_NAME.getValue().isEmpty()) {
            validation.append(ProxySPNegoJAASConfiguration.SERVICE_PROVIDER_NAME.getKey()).append(" must be set");
            isValid = false;
        }         
        if(ProxySPNegoJAASConfiguration.JAAS.getValue().isEmpty() || Files.isReadable(Paths.get(ProxySPNegoJAASConfiguration.JAAS.getValue()))) {
            validation.append(ProxySPNegoJAASConfiguration.JAAS.getKey()).append(" must be a readable file\n");
            isValid = false;            
        }
        if(ProxySPNegoJAASConfiguration.JAAS_CONTEXT.getValue().isEmpty()) {
            validation.append(ProxySPNegoJAASConfiguration.JAAS_CONTEXT.getKey()).append(" cannot be null or empty\n");
            isValid = false;            
        }  
        if(ProxySPNegoJAASConfiguration.KRB5.getValue().isEmpty() || Files.isReadable(Paths.get(ProxySPNegoJAASConfiguration.KRB5.getValue()))) {
            validation.append(ProxySPNegoJAASConfiguration.KRB5.getKey()).append(" must be a readable file\n");            
            isValid =  false;
        }
        if(ProxySPNegoJAASConfiguration.SERVICE_PROVIDER_NAME.getValue().isEmpty()) {
            validation.append(ProxySPNegoJAASConfiguration.SERVICE_PROVIDER_NAME.getKey()).append(" cannot be null or empty\n");            
            isValid =  false;            
        }
        return isValid;
    }

}

