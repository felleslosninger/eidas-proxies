package no.difi.eidas.sproxy;

import no.difi.eidas.sproxy.AbstractBaseTest.EidasSproxyTest;
import no.difi.eidas.sproxy.web.AuthController;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertNotNull;

@EidasSproxyTest
public class SpringDITest extends AbstractBaseTest {
    @Autowired
    private AuthController authController;

    @Test
    public void authControllerDependencyInjection() {
        assertNotNull(authController);

    }
}
