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
import org.junit.experimental.categories.Category;

/**
 *
 * @author malapert
 */
@Category(UnitTest.class)
public class ProxyConfigurationTest {
    
    public ProxyConfigurationTest() {
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
     * Test of values method, of class ProxyConfiguration.
     */
    @Test
    public void testValues() {
        System.out.println("values");
        ProxyConfiguration[] expResult = new ProxyConfiguration[]{
            ProxyConfiguration.HTTP_PROXY, ProxyConfiguration.NO_PROXY, 
            ProxyConfiguration.USERNAME, ProxyConfiguration.PASSWORD
        };
        ProxyConfiguration[] result = ProxyConfiguration.values();
        assertArrayEquals(expResult, result);
    }

    /**
     * Test of valueOf method, of class ProxyConfiguration.
     */
    @Test
    public void testValueOf() {
        System.out.println("valueOf");
        String name = "";
        ProxyConfiguration expResult = ProxyConfiguration.NO_PROXY;
        ProxyConfiguration result = ProxyConfiguration.valueOf("NO_PROXY");
        assertEquals(expResult, result);
    }

    /**
     * Test of getKey method, of class ProxyConfiguration.
     */
    @Test
    public void testGetKey() {
        System.out.println("getKey");
        ProxyConfiguration instance = ProxyConfiguration.NO_PROXY;
        String expResult = "no_proxy";
        String result = instance.getKey();
        assertEquals(expResult, result);
    }

    /**
     * Test of getValue method, of class ProxyConfiguration.
     */
    @Test
    public void testGetValue() {
        System.out.println("getValue");
        ProxyConfiguration instance = ProxyConfiguration.USERNAME;
        String expResult = "";
        String result = instance.getValue();
        assertEquals(expResult, result);
    }

    /**
     * Test of setValue method, of class ProxyConfiguration.
     */
    @Test
    public void testSetValue() {
        System.out.println("setValue");
        String value = "pass";
        ProxyConfiguration instance = ProxyConfiguration.PASSWORD;
        instance.setValue(value);
        assertTrue(true);
    }

    /**
     * Test of getConfig method, of class ProxyConfiguration.
     */
    @Test
    public void testGetConfig() {
        System.out.println("getConfig");
        Map<String, String> expResult = new HashMap<>();
        expResult.put("http_proxy", "");
        expResult.put("no_proxy", "");
        expResult.put("username", "");
        expResult.put("password", "");
        Map<String, String> result = ProxyConfiguration.getConfig();
        assertEquals(expResult, result);
    }

    /**
     * Test of isValid method, of class ProxyConfiguration.
     */
    @Test
    public void testIsValid() {
        System.out.println("isValid");
        StringBuilder error = new StringBuilder();
        boolean expResult = false;
        boolean result = ProxyConfiguration.isValid(error);
        assertEquals(expResult, result);
    }
    
}
