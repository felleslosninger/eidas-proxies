package no.difi.eidas.sproxy.domain.authentication;

import static org.junit.Assert.*;

import eu.eidas.auth.commons.protocol.eidas.LevelOfAssurance;
import org.junit.Test;

public class AuthenticationLevelTest {

    @Test
    public void testConvertToIdPorten() {
        for (AuthenticationLevel authenticationLevel : AuthenticationLevel.list()) {
            assertEquals(authenticationLevel.idPortenAuthnContextClassRef(), AuthenticationLevel.convertToIdPorten(authenticationLevel.eidasLevelOfAssurance()));
        }
    }

    @Test
    public void testConvertToEidas() {
        for (AuthenticationLevel authenticationLevel : AuthenticationLevel.list()) {
            assertEquals(authenticationLevel.eidasLevelOfAssurance(), AuthenticationLevel.convertToEidas(authenticationLevel.idPortenAuthnContextClassRef()));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDoNotConvertFromUnkownIdPortenValue() {
        AuthenticationLevel.convertToEidas("foo");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDoNotConvertFromUnkownEidasValue() {
        AuthenticationLevel.convertToIdPorten(LevelOfAssurance.LOW);
    }

}
