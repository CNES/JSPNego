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
public class ProxySPNegoJAASConfigurationTest {
    
    public ProxySPNegoJAASConfigurationTest() {
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
     * Test of values method, of class ProxySPNegoJAASConfiguration.
     */
    @Test
    public void testValues() {
        ProxySPNegoJAASConfiguration[] expResult = new ProxySPNegoJAASConfiguration[]{
            ProxySPNegoJAASConfiguration.HTTP_PROXY,
            ProxySPNegoJAASConfiguration.NO_PROXY,            
            ProxySPNegoJAASConfiguration.JAAS,
            ProxySPNegoJAASConfiguration.JAAS_CONTEXT,
            ProxySPNegoJAASConfiguration.SERVICE_PROVIDER_NAME,            
            ProxySPNegoJAASConfiguration.KRB5
        };
        ProxySPNegoJAASConfiguration[] result = ProxySPNegoJAASConfiguration.values();
        assertArrayEquals(expResult, result);
    }

    /**
     * Test of valueOf method, of class ProxySPNegoJAASConfiguration.
     */    
    @Ignore
    public void testValueOf() {
        ProxySPNegoJAASConfiguration expResult = ProxySPNegoJAASConfiguration.JAAS;
        ProxySPNegoJAASConfiguration result = ProxySPNegoJAASConfiguration.valueOf("JAAS");
        assertEquals(expResult, result);
    }

    /**
     * Test of getKey method, of class ProxySPNegoJAASConfiguration.
     */
    @Test
    public void testGetKey() {
        ProxySPNegoJAASConfiguration instance = ProxySPNegoJAASConfiguration.NO_PROXY;
        String expResult = "no_proxy";
        String result = instance.getKey();
        assertEquals(expResult, result);
    }

    /**
     * Test of getValue method, of class ProxySPNegoJAASConfiguration.
     */
    @Test
    public void testGetValue() {
        ProxySPNegoJAASConfiguration instance = ProxySPNegoJAASConfiguration.JAAS_CONTEXT;
        String expResult = "client";
        String result = instance.getValue();
        assertEquals(expResult, result);
    }

    /**
     * Test of setValue method, of class ProxySPNegoJAASConfiguration.
     */
    @Test
    public void testSetValue() {
        String value = "test";
        ProxySPNegoJAASConfiguration instance = ProxySPNegoJAASConfiguration.JAAS_CONTEXT;
        instance.setValue(value);
        assertTrue(true);
    }

    /**
     * Test of getConfig method, of class ProxySPNegoJAASConfiguration.
     */
    @Test
    public void testGetConfig() {
        Map<String, String> expResult = new HashMap(){{
           put("spn","");
           put("http_proxy", "");
           put("no_proxy", "");
           put("jassContext", "client");
           put("krb5File", "/etc/krb5.conf");
           put("jassFile", "");
        }};
        Map<String, String> result = ProxySPNegoJAASConfiguration.getConfig();
        assertEquals(expResult, result);
    }

    /**
     * Test of isValid method, of class ProxySPNegoJAASConfiguration.
     */
    @Test
    public void testIsValid() {
        StringBuilder error = new StringBuilder();
        boolean expResult = false;
        boolean result = ProxySPNegoJAASConfiguration.isValid(error);
        assertEquals(expResult, result);
    }
    
}
