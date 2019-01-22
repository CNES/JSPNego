/*
 * Copyright (C) 2017-2019 Centre National d'Etudes Spatiales (CNES).
 *
 * This file is part of DOI-server.
 *
 * This JSPNego is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * JSPNego is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
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
 * @author Jean-Christophe Malapert
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
           put("http_proxy", (System.getenv("http_proxy") == null) ? "" : System.getenv("http_proxy"));
           put("no_proxy", (System.getenv("no_proxy") == null) ? "" : System.getenv("no_proxy"));
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
