package no.difi.eidas.cproxy.domain.idp;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Created by est on 07.07.2015.
 */
public class IdportenSessionTest {
public static final String nameID = "nameid";
    public static final String sessionIndex = "sessionindex";


    @Test
    public void testGetSPNameId() throws Exception {
        IdportenSession testSession = new IdportenSession(nameID,sessionIndex);
        assertTrue(testSession.getSPNameId().equals(nameID));
    }

    @Test
    public void testGetSSOSessionIndex() throws Exception {
        IdportenSession testSession = new IdportenSession(nameID,sessionIndex);
        assertTrue(testSession.getSSOSessionIndex().equals(sessionIndex));
    }
}