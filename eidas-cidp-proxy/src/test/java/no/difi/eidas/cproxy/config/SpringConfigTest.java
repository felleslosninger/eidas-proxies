package no.difi.eidas.cproxy.config;

import no.difi.eidas.cproxy.AbstractBaseTest;
import no.difi.eidas.cproxy.web.CitizenController;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertNotNull;

@AbstractBaseTest.EidasCproxyTest
public class SpringConfigTest extends AbstractBaseTest {

    @Autowired
    private CitizenController controller;

    @Test
    public void testControllerCreated() {
        assertNotNull(controller);

    }
}
