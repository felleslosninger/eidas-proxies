package no.difi.eidas.sproxy.integration.eidas.response;

import no.difi.eidas.sproxy.ResourceReader;
import no.difi.eidas.sproxy.domain.authentication.AuthenticationLevel;
import no.difi.eidas.sproxy.domain.saml.SamlResponseXml;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class EidasResponseTest {

    private EidasResponse eidasResponse;
    protected String firstname = "firstname";
    protected String lastname = "lastname";
    protected String birth = "111280";
    protected SamlResponseXml samlXml = new SamlResponseXml(ResourceReader.eidasAuthnResponse());

    @Before
    public void setup() {
        eidasResponse = EidasResponse.builder()
                .samlXml(samlXml)
                .authnContextClassRef(AuthenticationLevel.LEVEL3.idPortenAuthnContextClassRef())
                .currentFamilyName(lastname)
                .currentGivenName(firstname)
                .dateOfBirth(birth)
                .build();
    }

    @Test
    public void testAttributeRetrieval() {
        assertEquals(firstname, eidasResponse.currentGivenName());
        assertEquals(lastname, eidasResponse.currentFamilyName());
        assertEquals(birth, eidasResponse.dateOfBirth());
        assertEquals(3, eidasResponse.attributes().size());
        assertTrue(eidasResponse.attributes().values().contains(firstname));
        assertTrue(eidasResponse.attributes().values().contains(lastname));
        assertTrue(eidasResponse.attributes().values().contains(birth));
    }

    @Test
    public void testCreateEidasName() {
        assertNotNull(eidasResponse.name());
        Optional<EidasResponse.Name> eidasName = eidasResponse.name();
        assertTrue(eidasName.isPresent());
        assertEquals(firstname, eidasName.get().firstName());
        assertEquals(lastname, eidasName.get().lastName());
        assertEquals(birth, eidasName.get().birth());
    }

    @Test
    public void hasCannotCreateEidasdNameWhenDateOfBirthIsMissing() {
        eidasResponse = EidasResponse.builder().currentGivenName("cgn").currentFamilyName("cfn").build();
        Optional<EidasResponse.Name> eidasName = eidasResponse.name();
        assertFalse(eidasName.isPresent());
    }

}
