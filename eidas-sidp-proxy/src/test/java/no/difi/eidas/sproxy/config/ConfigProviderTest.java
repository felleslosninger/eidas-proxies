package no.difi.eidas.sproxy.config;

import org.junit.Test;
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

}
