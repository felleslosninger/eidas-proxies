package no.difi.eidas.sproxy.config;

import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

public class ConfigProviderTest {

    private ConfigProvider configProvider = new ConfigProvider();

    @Test
    public void testConcatUrl() {
        assertEquals("http://www.difi.no/eid", configProvider.concatUrl("http://www.difi.no/", "/eid"));
        assertEquals("http://www.difi.no/eid", configProvider.concatUrl("http://www.difi.no/", "eid"));
        assertEquals("http://www.difi.no/eid", configProvider.concatUrl("http://www.difi.no", "/eid"));
        assertEquals("http://www.difi.no/eid", configProvider.concatUrl("http://www.difi.no", "eid"));
    }

    @Test
    public void testParseTestUsers() {
        Map<String, String> testUserMap = configProvider.parseTestUsers("20000101.EE/NO/60001019906:60125500857");
        assertEquals("60125500857", testUserMap.get("60001019906"));
    }

    @Test
    public void testParseOtherTestUsers() {
        Map<String, String> testUserMap = configProvider.parseTestUsers("20000101.EST.60001019906:60125500857");
        assertEquals(null, testUserMap.get("60001019906"));

    }

}
