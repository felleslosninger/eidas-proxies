package no.difi.eidas.cproxy.config;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class ConfigProviderTest {

    @Test
    public void testToStringIsNotNull(){
        ConfigProvider configProvider = new ConfigProvider();
        assertNotNull(configProvider.toString());
    }
}
