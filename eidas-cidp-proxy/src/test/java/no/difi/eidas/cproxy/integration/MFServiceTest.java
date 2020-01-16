package no.difi.eidas.cproxy.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.difi.eidas.cproxy.config.ConfigProvider;
import no.difi.eidas.cproxy.integration.mf.MFService;
import no.difi.eidas.idpproxy.integrasjon.dsf.restapi.PersonLookupResult;
import no.difi.eidas.idpproxy.integrasjon.mf.MFPersonResource;
import no.difi.eidas.idpproxy.test.ResourceReader;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MFServiceTest {
    private static final String CLIENT_ID_HEADER = "Client-Id";

    private static final String foedselsnummer = "01104200113";
    private static final String nonExistingFoedselsnummer = "81104200113";
    private static final String fornavn = "TERJE";
    private static final String etternavn = "FINSTAD";
    private static final LocalDate foedselsdato = new LocalDate(1942,10,1);
    private static final String foedeland = "NOR";
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
        when(configProvider.getMfGatewayUsername()).thenReturn("user");
        when(configProvider.getMfGatewayPassword()).thenReturn("password");
    }

    @Test
    public void lookup_person_exists() throws Exception {
        String response = ResourceReader.read("mfGatewayResponse.json");
        MFPersonResource personStub = new ObjectMapper().readValue(response, MFPersonResource.class);
        when(restTemplate.exchange(anyString(), any(), any(), any(Class.class))).thenReturn(new ResponseEntity<>(personStub, HttpStatus.OK));
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
        MFPersonResource personStub = new ObjectMapper().readValue(response, MFPersonResource.class);
        when(restTemplate.exchange(anyString(), any(), any(), any(Class.class))).thenReturn(new ResponseEntity<>(personStub, HttpStatus.OK));
        PersonLookupResult personResult = mfService.lookup(foedselsnummer);
        assertEquals(PersonLookupResult.Status.MULTIPLEFOUND, personResult.status());
    }

    @Test
    public void lookup_person_does_not_exist() {
        PersonLookupResult personResult = mfService.lookup(nonExistingFoedselsnummer);
        assertEquals(PersonLookupResult.Status.MULTIPLEFOUND, personResult.status());
        assertFalse(personResult.person().isPresent());
    }

    private HttpHeaders createHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.setBasicAuth(configProvider.getMfGatewayUsername(), configProvider.getMfGatewayPassword());
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON_UTF8));
        headers.add(CLIENT_ID_HEADER, "eidas-sidp");
        return headers;
    }
}
