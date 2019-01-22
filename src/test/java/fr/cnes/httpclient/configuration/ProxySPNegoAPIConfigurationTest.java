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
import org.junit.experimental.categories.Category;

/**
 *
 * @author Jean-Christophe Malapert
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
        String value = "test";
        ProxySPNegoAPIConfiguration instance = ProxySPNegoAPIConfiguration.KRB5;
        instance.setValue(value);;
    }

    /**
     * Test of getConfig method, of class ProxySPNegoAPIConfiguration.
     */
    @Test
    public void testGetConfig() {
        Map<String, String> expResult = new HashMap(){{
            put("http_proxy", (System.getenv("http_proxy") == null) ? "" : System.getenv("http_proxy"));
            put("no_proxy", (System.getenv("no_proxy") == null) ? "" : System.getenv("no_proxy"));
            put("jassContext", "other");
            put("renewTGT", "");
            put("isInitiator", "true");
            put("storeKey", "true");
            put("keyTab", "");
            put("ticketCache", (System.getenv("KRB5CCNAME") == null) ? "" : System.getenv("KRB5CCNAME"));
            put("principal", (System.getenv("sun.security.krb5.principal") == null) ? "" : System.getenv("sun.security.krb5.principal"));
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
        StringBuilder error = new StringBuilder();
        boolean expResult = false;
        boolean result = ProxySPNegoAPIConfiguration.isValid(error);
        assertEquals(expResult, result);
    }
    
}
