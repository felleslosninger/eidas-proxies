package no.difi.eidas.cproxy.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.difi.eidas.cproxy.config.ConfigProvider;
import no.difi.eidas.cproxy.integration.mf.MFService;
import no.difi.eidas.cproxy.integration.mf.json.FolkeregisterPerson;
import no.difi.eidas.idpproxy.integrasjon.dsf.restapi.PersonLookupResult;
import no.difi.eidas.idpproxy.test.ResourceReader;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.client.RestTemplate;

import static org.junit.Assert.*;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MFServiceTest {

    private static final String foedselsnummer = "01104200113";
    private static final String nonExistingFoedselsnummer = "81104200113";
    private static final String fornavn = "Norman";
    private static final String etternavn = "Holm";
    private static final LocalDate foedselsdato = new LocalDate(1942,10,1);
    private static final String foedeland = "ASM";
    private static final String mfGatewayUrl = "http://localhost:8080/person/";

    @Mock
    private ConfigProvider configProvider;

    @Mock
    private RestTemplate restTemplate;

    private MFService mfService;

    @Before
    public void before() {
        reset(configProvider);
        mfService = new MFService(configProvider, restTemplate);
        when(configProvider.getMfGatewayUrl()).thenReturn("http://localhost:8080");
    }

    @Test
    public void lookup_person_exists() throws Exception {
        String response = ResourceReader.read("mfGatewayResponse.json");
        FolkeregisterPerson personStub = new ObjectMapper().readValue(response, FolkeregisterPerson.class);
        when(restTemplate.getForObject(mfGatewayUrl + foedselsnummer, FolkeregisterPerson.class))
                .thenReturn(personStub);
        PersonLookupResult personResult = mfService.lookup(foedselsnummer);
        assertEquals(PersonLookupResult.Status.OK, personResult.status());
        assertTrue(personResult.person().isPresent());
        assertEquals(fornavn, personResult.person().get().fornavn());
        assertEquals(etternavn, personResult.person().get().etternavn());
        assertEquals(foedselsnummer, personResult.person().get().fødselsnummer());
        assertEquals(foedselsdato, personResult.person().get().fødselsdato());
        assertEquals(foedeland, personResult.person().get().fødeland());
    }

    @Test
    public void lookup_person_exists_no_foedsel_not_read() throws Exception {
        String response = ResourceReader.read("mfGatewayNoFoedselResponse.json");
        FolkeregisterPerson personStub = new ObjectMapper().readValue(response, FolkeregisterPerson.class);
        when(restTemplate.getForObject(mfGatewayUrl + foedselsnummer, FolkeregisterPerson.class))
                .thenReturn(personStub);
        PersonLookupResult personResult = mfService.lookup(foedselsnummer);
        assertEquals(PersonLookupResult.Status.MULTIPLEFOUND, personResult.status());
    }

    @Test
    public void lookup_person_does_not_exist() throws Exception {
        PersonLookupResult personResult = mfService.lookup(nonExistingFoedselsnummer);
        assertEquals(PersonLookupResult.Status.MULTIPLEFOUND, personResult.status());
        assertFalse(personResult.person().isPresent());
    }

}
