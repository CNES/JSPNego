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
public enum ProxySPNegoAPIConfiguration {
    HTTP_PROXY("http_proxy", System.getenv("http_proxy")),
    NO_PROXY("no_proxy", System.getenv("no_proxy") == null ? "localhost,127.0.0.1":System.getenv("no_proxy")),
    REFRESH_KRB5_CONFIG("refreshKrb5Config", "true"),
    USE_TICKET_CACHE("useTicketCache", "false"),
    TICKET_CACHE("ticketCache", System.getenv("KRB5CCNAME")),
    RENEW_TGT("renewTGT", ""),
    DO_NOT_PROMPT("doNotPrompt", "true"),
    USE_KEYTAB("useKeyTab", "false"),
    KEY_TAB("keyTab", ""),
    STORE_KEY("storeKey", "true"),
    PRINCIPAL("principal", ""),
    IS_INITIATOR("isInitiator", "true"),
    SERVICE_PROVIDER_NAME("spn", ""),
    KRB5("krb5File", "/etc/krb5.conf"),
    JAAS_CONTEXT("jassContext", "other");

    private final String key;
    private String value;

    ProxySPNegoAPIConfiguration(final String key, final String value) {
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
    
    public static boolean isValid(StringBuilder error) {
        boolean isValid = true;
        final StringBuilder validation = new StringBuilder();
        if(ProxySPNegoAPIConfiguration.HTTP_PROXY.getValue().isEmpty()) {
            validation.append(ProxySPNegoAPIConfiguration.HTTP_PROXY.getKey()).append(" cannot be null or empty\n");
            isValid = false;
        }    
        if(Boolean.parseBoolean(ProxySPNegoAPIConfiguration.DO_NOT_PROMPT.getValue())&& 
           !Boolean.parseBoolean(ProxySPNegoAPIConfiguration.USE_TICKET_CACHE.getValue())&&
           !Boolean.parseBoolean(ProxySPNegoAPIConfiguration.USE_KEYTAB.getValue())) {
           validation.append("Illegal combination : DO_NOT_PROMPT=true && USE_TICKET_CACHE = false"
                   + "&& USE_KEYTAB = false");
           isValid = false;
        }
        if(!Boolean.parseBoolean(ProxySPNegoAPIConfiguration.USE_TICKET_CACHE.getValue()) &&
           !ProxySPNegoAPIConfiguration.TICKET_CACHE.getValue().isEmpty()) {
           validation.append("Illegal combination : USE_TICKET_CACHE=false && TICKET_CACHE is set");
           isValid = false;            
        } else if (Boolean.parseBoolean(ProxySPNegoAPIConfiguration.USE_TICKET_CACHE.getValue()) &&
          !Files.isReadable(Paths.get(ProxySPNegoAPIConfiguration.TICKET_CACHE.getValue()))) {
           validation.append(ProxySPNegoAPIConfiguration.TICKET_CACHE.getValue()).append(" is not a redable file");
           isValid = false;
        }
        if(Boolean.parseBoolean(ProxySPNegoAPIConfiguration.RENEW_TGT.getValue())
           && !Boolean.parseBoolean(ProxySPNegoAPIConfiguration.USE_TICKET_CACHE.getValue())) {
            validation.append("Illegal combination : RENEW_TGT=true &&  USE_TICKET_CACHE=false");
            isValid = false;
        }
        if(Boolean.parseBoolean(ProxySPNegoAPIConfiguration.STORE_KEY.getValue())
           && Boolean.parseBoolean(ProxySPNegoAPIConfiguration.USE_TICKET_CACHE.getValue())
           && Boolean.parseBoolean(ProxySPNegoAPIConfiguration.DO_NOT_PROMPT.getValue())) {
            validation.append("Illegal combination : STORE_KEY=true && USE_TICKET_CACHE=true && DO_NOT_PROMPT=true");
            isValid = false;
        }            
        
        error.append(validation);
        return isValid;
    }
    
    

}
