/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.httpclient.configuration;

import fr.cnes.httpclient.UnitTest;
import java.util.HashMap;
import java.util.Map;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;
import org.junit.experimental.categories.Category;

/**
 *
 * @author malapert
 */
@Category(UnitTest.class)
public class ProxySPNegoAPIConfigurationTest {
    
    public ProxySPNegoAPIConfigurationTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of values method, of class ProxySPNegoAPIConfiguration.
     */
    @Test
    public void testValues() {
        System.out.println("values");
        ProxySPNegoAPIConfiguration[] expResult = new ProxySPNegoAPIConfiguration[]{
            ProxySPNegoAPIConfiguration.HTTP_PROXY, ProxySPNegoAPIConfiguration.NO_PROXY,
            ProxySPNegoAPIConfiguration.REFRESH_KRB5_CONFIG, ProxySPNegoAPIConfiguration.USE_TICKET_CACHE,
            ProxySPNegoAPIConfiguration.TICKET_CACHE, ProxySPNegoAPIConfiguration.RENEW_TGT,
            ProxySPNegoAPIConfiguration.DO_NOT_PROMPT, ProxySPNegoAPIConfiguration.USE_KEYTAB,
            ProxySPNegoAPIConfiguration.KEY_TAB, ProxySPNegoAPIConfiguration.STORE_KEY,
            ProxySPNegoAPIConfiguration.PRINCIPAL, ProxySPNegoAPIConfiguration.IS_INITIATOR,
            ProxySPNegoAPIConfiguration.SERVICE_PROVIDER_NAME, ProxySPNegoAPIConfiguration.KRB5,
            ProxySPNegoAPIConfiguration.JAAS_CONTEXT
        };
        ProxySPNegoAPIConfiguration[] result = ProxySPNegoAPIConfiguration.values();
        assertArrayEquals(expResult, result);
    }

    /**
     * Test of valueOf method, of class ProxySPNegoAPIConfiguration.
     */
    @Test
    public void testValueOf() {
        System.out.println("valueOf");
        String name = "KRB5";
        ProxySPNegoAPIConfiguration expResult = ProxySPNegoAPIConfiguration.KRB5;
        ProxySPNegoAPIConfiguration result = ProxySPNegoAPIConfiguration.valueOf(name);
        assertEquals(expResult, result);
    }

    /**
     * Test of getKey method, of class ProxySPNegoAPIConfiguration.
     */
    @Test
    public void testGetKey() {
        System.out.println("getKey");
        ProxySPNegoAPIConfiguration instance = ProxySPNegoAPIConfiguration.NO_PROXY;
        String expResult = "no_proxy";
        String result = instance.getKey();
        assertEquals(expResult, result);
    }

    /**
     * Test of getValue method, of class ProxySPNegoAPIConfiguration.
     */
    @Test
    public void testGetValue() {
        System.out.println("getValue");
        ProxySPNegoAPIConfiguration instance = ProxySPNegoAPIConfiguration.IS_INITIATOR;
        String expResult = "true";
        String result = instance.getValue();
        assertEquals(expResult, result);
    }

    /**
     * Test of setValue method, of class ProxySPNegoAPIConfiguration.
     */
    @Test
    public void testSetValue() {
        System.out.println("setValue");
        String value = "test";
        ProxySPNegoAPIConfiguration instance = ProxySPNegoAPIConfiguration.KRB5;
        instance.setValue(value);;
    }

    /**
     * Test of getConfig method, of class ProxySPNegoAPIConfiguration.
     */
    @Test
    public void testGetConfig() {
        System.out.println("getConfig");
        Map<String, String> expResult = new HashMap(){{
            put("http_proxy","");
            put("no_proxy", "");
            put("jassContext", "other");
            put("renewTGT", "");
            put("isInitiator", "true");
            put("storeKey", "true");
            put("keyTab", "");
            put("ticketCache", "");
            put("principal", "");
            put("spn", "");
            put("doNotPrompt", "true");
            put("useKeyTab", "false");
            put("useTicketCache", "false");
            put("krb5File", "/etc/krb5.conf");
            put("refreshKrb5Config", "true");
        }};
        Map<String, String> result = ProxySPNegoAPIConfiguration.getConfig();
        assertEquals(expResult, result);
    }

    /**
     * Test of isValid method, of class ProxySPNegoAPIConfiguration.
     */
    @Test
    public void testIsValid() {
        System.out.println("isValid");
        StringBuilder error = new StringBuilder();
        boolean expResult = false;
        boolean result = ProxySPNegoAPIConfiguration.isValid(error);
        assertEquals(expResult, result);
    }
    
}
